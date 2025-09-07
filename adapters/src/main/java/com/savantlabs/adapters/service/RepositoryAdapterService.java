package com.savantlabs.adapters.service;

import com.savantlabs.adapters.enums.RepositoryAdapterType;
import com.savantlabs.adapters.model.SyncIssuesRequest;
import com.savantlabs.adapters.model.SyncIssuesResponse;

public interface RepositoryAdapterService {

    SyncIssuesResponse syncIssues(SyncIssuesRequest syncIssuesRequest) throws Exception;

    RepositoryAdapterType getRepository();
}
