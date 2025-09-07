package com.savantlabs.adapters.business;

import com.savantlabs.adapters.enums.RepositoryAdapterType;
import com.savantlabs.adapters.helpers.FirestoreClient;
import com.savantlabs.adapters.helpers.GitHubClient;
import com.savantlabs.adapters.model.Issue;
import com.savantlabs.adapters.model.SyncIssuesRequest;
import com.savantlabs.adapters.model.SyncIssuesResponse;
import com.savantlabs.adapters.service.RepositoryAdapterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitHubRepositoryServiceImpl implements RepositoryAdapterService {
  private final GitHubClient gitHubClient;
  private final FirestoreClient firestoreClient;

  public GitHubRepositoryServiceImpl(GitHubClient gitHubClient, FirestoreClient firestoreClient) {
    this.gitHubClient = gitHubClient;
    this.firestoreClient = firestoreClient;
  }

  @Override
  public SyncIssuesResponse syncIssues(SyncIssuesRequest syncIssuesRequest) throws Exception {
    String owner = syncIssuesRequest.getOwner();
    String repo = syncIssuesRequest.getRepository();
    Integer limit = syncIssuesRequest.getLimit();
    List<Issue> issues = gitHubClient.fetchTopIssues(owner, repo, limit);
    for (Issue issue : issues) {
      firestoreClient.upsert(issue);
    }
    SyncIssuesResponse syncIssuesResponse = new SyncIssuesResponse();
    syncIssuesResponse.setOwner(owner);
    syncIssuesResponse.setRepository(repo);
    syncIssuesResponse.setRequested(limit);
    syncIssuesResponse.setSaved(issues.size());

    return syncIssuesResponse;
  }

  @Override
  public RepositoryAdapterType getRepository() {
    return RepositoryAdapterType.GIT_HUB_REPOSITORY;
  }
}