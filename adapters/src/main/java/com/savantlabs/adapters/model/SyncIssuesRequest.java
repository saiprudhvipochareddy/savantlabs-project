package com.savantlabs.adapters.model;

public class SyncIssuesRequest {
    private String owner;
    private String repository;
    private String syncIssuesType;
    private Integer limit;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getSyncIssuesType() {
        return syncIssuesType;
    }

    public void setSyncIssuesType(String syncIssuesType) {
        this.syncIssuesType = syncIssuesType;
    }
}
