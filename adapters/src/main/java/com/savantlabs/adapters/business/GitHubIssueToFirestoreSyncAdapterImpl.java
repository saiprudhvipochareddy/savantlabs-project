package com.savantlabs.adapters.business;

import com.savantlabs.adapters.enums.SyncIssueAdapterType;
import com.savantlabs.adapters.helpers.FirestoreClient;
import com.savantlabs.adapters.helpers.GitHubClient;
import com.savantlabs.adapters.model.Issue;
import com.savantlabs.adapters.model.SyncIssuesRequest;
import com.savantlabs.adapters.model.SyncIssuesResponse;
import com.savantlabs.adapters.service.SyncIssueAdapterService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service implementation for synchronizing GitHub issues into Firestore.
 **/
@Service
public class GitHubIssueToFirestoreSyncAdapterImpl implements SyncIssueAdapterService {
  private final GitHubClient gitHubClient;
  private final FirestoreClient firestoreClient;

  public GitHubIssueToFirestoreSyncAdapterImpl(GitHubClient gitHubClient, FirestoreClient firestoreClient) {
    this.gitHubClient = gitHubClient;
    this.firestoreClient = firestoreClient;
  }

  @Override
  public SyncIssuesResponse syncIssues(SyncIssuesRequest syncIssuesRequest) throws Exception {
    String owner = syncIssuesRequest.getOwner();
    String repo = syncIssuesRequest.getRepository();
    Integer limit = syncIssuesRequest.getLimit();
    Map<String, Issue> issueMap = gitHubClient.fetchTopIssues(owner, repo, limit);
    for (Issue issue : issueMap.values()) {
      firestoreClient.upsert(issue);
    }
    SyncIssuesResponse syncIssuesResponse = new SyncIssuesResponse();
    syncIssuesResponse.setOwner(owner);
    syncIssuesResponse.setRepository(repo);
    syncIssuesResponse.setRequested(limit);
    syncIssuesResponse.setSaved(issueMap.values().size());

    return syncIssuesResponse;
  }

  @Override
  public SyncIssueAdapterType getRepository() {
    return SyncIssueAdapterType.GIT_HUB_REPOSITORY_FIRESTORE;
  }
}