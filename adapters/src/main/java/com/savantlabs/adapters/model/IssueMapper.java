package com.savantlabs.adapters.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public class IssueMapper {
  public static boolean isPullRequest(JsonNode n) { return n.has("pull_request"); }

  public static Issue toIssue(JsonNode n) {
    Issue i = new Issue();
    i.id = n.get("id").asText();
    i.title = text(n, "title");
    i.state = text(n, "state");
    i.htmlUrl = text(n, "html_url");
    String created = text(n, "created_at");
    i.createdAt = created == null ? null : Instant.parse(created);
    return i;
  }

  private static String text(JsonNode n, String k) { return n.hasNonNull(k) ? n.get(k).asText() : null; }
}