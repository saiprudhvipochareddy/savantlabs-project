package com.savantlabs.adapters.model;

public class SyncIssuesResponse {
    private String owner;
    private String repository;
    private Integer requested;
    private Integer saved;

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

    public Integer getRequested() {
        return requested;
    }

    public void setRequested(Integer requested) {
        this.requested = requested;
    }

    public Integer getSaved() {
        return saved;
    }

    public void setSaved(Integer saved) {
        this.saved = saved;
    }
}
