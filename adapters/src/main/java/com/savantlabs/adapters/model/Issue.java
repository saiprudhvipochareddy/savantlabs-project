package com.savantlabs.adapters.model;

import java.time.Instant;

public class Issue {
    public String id;
    public String owner;
    public String repo;
    public String title;
    public String state;
    public String htmlUrl;
    public Instant createdAt;
    public Instant syncedAt;

    public Issue withOwnerRepo(String owner, String repo) {
        this.owner = owner;
        this.repo = repo;
        return this;
    }
}