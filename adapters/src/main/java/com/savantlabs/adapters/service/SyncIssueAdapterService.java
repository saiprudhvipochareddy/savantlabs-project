package com.savantlabs.adapters.service;

import com.savantlabs.adapters.enums.SyncIssueAdapterType;
import com.savantlabs.adapters.model.SyncIssuesRequest;
import com.savantlabs.adapters.model.SyncIssuesResponse;

public interface SyncIssueAdapterService {

    SyncIssuesResponse syncIssues(SyncIssuesRequest syncIssuesRequest) throws Exception;

    SyncIssueAdapterType getRepository();
}
