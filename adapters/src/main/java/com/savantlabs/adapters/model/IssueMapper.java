package com.savantlabs.adapters.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public class IssueMapper {
    public static boolean isPullRequest(JsonNode jsonNode) {
        return jsonNode.has("pull_request");
    }

    public static Issue toIssue(JsonNode jsonNode) {
        Issue issue = new Issue();
        issue.id = jsonNode.get("id").asText();
        issue.title = text(jsonNode, "title");
        issue.state = text(jsonNode, "state");
        issue.htmlUrl = text(jsonNode, "html_url");
        String created = text(jsonNode, "created_at");
        issue.createdAt = created == null ? null : Instant.parse(created);
        return issue;
    }

    private static String text(JsonNode n, String k) {
        return n.hasNonNull(k) ? n.get(k).asText() : null;
    }
}