package com.savantlabs.adapters.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.savantlabs.adapters.exception.CustomException;
import com.savantlabs.adapters.model.Issue;
import com.savantlabs.adapters.model.IssueMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@Service
public class GitHubClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);
    private final String token;
    private final String baseUrl;
    private final String issueUrl;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_NETWORK_RETRIES = 3;

    public GitHubClient(@Value("${github.token}") String token,
                        @Value("${github.base-url}") String baseUrl,
                        @Value("${github.issues-url}") String issueUrl) {
        this.token = token;
        this.baseUrl = baseUrl;
        this.issueUrl = issueUrl;
    }

    public List<Issue> fetchTopIssues(String owner, String repo, int limit) {
        int remaining = limit;
        int page = 1;
        List<Issue> results = new ArrayList<>();

        while (remaining > 0) {
            int pageSize = Math.min(MAX_PAGE_SIZE, remaining);
            String url = String.format(issueUrl, baseUrl, owner, repo, pageSize, page);

            ResponseEntity<JsonNode> response = restTemplateExchangeGet(url);
            response = handleRateLimitAndErrors(response, url);

            JsonNode arr = response.getBody();
            if (arr == null || !arr.isArray() || arr.isEmpty()) break;

            for (JsonNode n : arr) {
                if (IssueMapper.isPullRequest(n)) continue; // skip PRs
                results.add(IssueMapper.toIssue(n).withOwnerRepo(owner, repo));
                remaining--;
                if (remaining == 0) break;
            }
            page++;
        }

        return results;
    }

    private ResponseEntity<JsonNode> handleRateLimitAndErrors(ResponseEntity<JsonNode> response, String url) {
        HttpStatusCode status = response.getStatusCode();

        if (status == HttpStatus.UNAUTHORIZED) {
            System.err.println("Unauthorized: missing or invalid GitHub token");
            throw new CustomException(status, "Unauthorized: missing or invalid GitHub token");
        }

        if (status == HttpStatus.FORBIDDEN) {
            HttpHeaders headers = response.getHeaders();
            String remaining = headers.getFirst("X-RateLimit-Remaining");
            String reset = headers.getFirst("X-RateLimit-Reset");

            if ("0".equals(remaining) && reset != null) {
                long resetEpoch = Long.parseLong(reset);
                long waitMs = (resetEpoch - Instant.now().getEpochSecond()) * 1000L;

                if (waitMs > 0) {
                    System.out.println("Rate limited. Waiting " + (waitMs / 1000));
                    try {
                        Thread.sleep(waitMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return restTemplateExchangeGet(url);
            }

            System.err.println("Forbidden: rate-limit exceeded");
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

    private ResponseEntity<JsonNode> restTemplateExchangeGet(String url) {
        long backoff = 500;

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
                String errorBody = ex.getResponseBodyAsString();
                log.error("GitHub API error [{}] on {} : {}", status, url, errorBody);
                throw new CustomException(status, "GitHub API error: " + ex.getMessage());
            } catch (Exception ex) {
                if (attempt == MAX_NETWORK_RETRIES) {
                    log.error("Giving up after {} attempts to call {}. Cause: {}", attempt, url, ex.getMessage());
                    throw new RuntimeException("Failed after retries: " + url, ex);
                }

                log.warn("Network error calling {} (attempt {} of {}), retrying in {} ms...",
                        url, attempt, MAX_NETWORK_RETRIES, backoff);

                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }

                backoff *= 2;
            }
        }

        throw new RuntimeException("Unexpected error: retry loop exited for " + url);
    }

    private HttpHeaders headers(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
        httpHeaders.set(HttpHeaders.USER_AGENT, "gh-commits-api/1.0");
        if (token != null && !token.isBlank()) httpHeaders.setBearerAuth(token);
        return httpHeaders;
    }

}