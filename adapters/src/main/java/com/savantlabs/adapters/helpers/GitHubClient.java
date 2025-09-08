package com.savantlabs.adapters.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.savantlabs.adapters.exception.CustomException;
import com.savantlabs.adapters.model.Issue;
import com.savantlabs.adapters.model.IssueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@Component
public class GitHubClient {
    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_NETWORK_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 500;

    private static final String ACCEPT_HEADER = "application/vnd.github+json";
    private static final String USER_AGENT = "gh-issues-sync/1.0";

    private final RestTemplate restTemplate = new RestTemplate();
    private final String token;
    private final String baseUrl;
    private final String issueUrl;

    public GitHubClient(@Value("${github.token}") String token,
                        @Value("${github.base-url}") String baseUrl,
                        @Value("${github.issues-url}") String issueUrl) {
        this.token = token;
        this.baseUrl = baseUrl;
        this.issueUrl = issueUrl;
    }

    /**
     * Fetches the top N issues from a GitHub repository.
     *
     * @param owner repo owner
     * @param repo  repository name
     * @param limit max number of issues to fetch
     * @return map of unique issue keys to Issue objects
     */
    public Map<String, Issue> fetchTopIssues(String owner, String repo, int limit) {
        int remaining = limit;
        int page = 1;
        Map<String, Issue> issueMap = new LinkedHashMap<>();

        while (remaining > 0) {
            int pageSize = Math.min(MAX_PAGE_SIZE, remaining);
            String url = String.format(issueUrl, baseUrl, owner, repo, pageSize, page);

            ResponseEntity<JsonNode> response = restTemplateExchangeGet(url);
            response = handleRateLimitAndErrors(response, url);

            JsonNode jsonArray = response.getBody();
            if (jsonArray == null || !jsonArray.isArray() || jsonArray.isEmpty()) break;

            for (JsonNode jsonNode : jsonArray) {
                if (IssueMapper.isPullRequest(jsonNode)) continue; // skip PRs
                if (!issueMap.containsKey(jsonNode.get("id").asText())) {
                    issueMap.put(jsonNode.get("id").asText(), IssueMapper.toIssue(jsonNode).withOwnerRepo(owner, repo));
                    remaining--;
                }

                if (remaining == 0) break;
            }
            page++;
        }

        return issueMap;
    }

    private ResponseEntity<JsonNode> restTemplateExchangeGet(String url) {
        long backoff = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_NETWORK_RETRIES; attempt++) {
            try {
                return restTemplate.exchange(
                        URI.create(url),
                        HttpMethod.GET,
                        new HttpEntity<>(headers(token)),
                        JsonNode.class
                );
            } catch (HttpStatusCodeException ex) {
                HttpStatusCode status = ex.getStatusCode();
                log.error("GitHub API error [{}] on {} → {}", status, url, ex.getResponseBodyAsString());
                return ResponseEntity.status(status).headers(ex.getResponseHeaders())
                        .body(ex.getResponseBodyAs(JsonNode.class));
            } catch (Exception ex) {
                if (attempt == MAX_NETWORK_RETRIES) {
                    log.error("Failed after {} attempts to call {}. Cause: {}", attempt, url, ex.getMessage());
                    throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed after retries: "
                            + ex.getMessage());
                }
                log.warn("Network error calling {} (attempt {} of {}), retrying in {} ms", url, attempt,
                        MAX_NETWORK_RETRIES, backoff);
                sleep(backoff);
                backoff *= 2;
            }
        }
        throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: retry loop exited for " + url);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Retry interrupted" + ie);
        }
    }

    private ResponseEntity<JsonNode> handleRateLimitAndErrors(ResponseEntity<JsonNode> response, String url) {
        HttpStatusCode status = response.getStatusCode();

        if (status.equals(HttpStatus.UNAUTHORIZED)) {
            log.error("Unauthorized: missing or invalid GitHub token");
            throw new CustomException(status, "Unauthorized: missing or invalid GitHub token");
        }

        if (status.equals(HttpStatus.FORBIDDEN)) {
            HttpHeaders headers = response.getHeaders();
            String remaining = headers.getFirst("X-RateLimit-Remaining");
            // X-RateLimit-Reset -> the UNIX epoch timestamp (in seconds) when your current quota window resets
            String reset = headers.getFirst("X-RateLimit-Reset");

            if ("0".equals(remaining) && reset != null) {
                long resetEpoch = Long.parseLong(reset);
                long waitMs = (resetEpoch - Instant.now().getEpochSecond()) * 1000L;
                if (waitMs > 0 && waitMs <= 10_000) { // near reset perform retry
                    log.warn("Rate limited. Waiting {} sec before retry...", waitMs / 1000);
                    try {
                        Thread.sleep(waitMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Retry interrupted"
                                + e.getMessage());
                    }
                    return restTemplateExchangeGet(url);
                }

                // reset too far perform fail fast
                throw new CustomException(status,
                        "Rate limit exceeded. Retry after " + (waitMs / 1000) + " seconds");
            }

            log.error("Forbidden: rate-limit exceeded");
            throw new CustomException(status, "Forbidden: rate-limit exceeded");
        }

        if (status == HttpStatus.NOT_FOUND) {
            System.err.println("Not found: " + url);
            throw new CustomException(status, "Not found: " + url);
        }

        if (!status.is2xxSuccessful()) {
            System.err.println("GitHub error: " + status);
            throw new CustomException(status, "GitHub error: " + status);
        }

        return response;
    }

    private HttpHeaders headers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, ACCEPT_HEADER);
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        if (token != null && !token.isBlank()) headers.setBearerAuth(token);
        return headers;
    }

}