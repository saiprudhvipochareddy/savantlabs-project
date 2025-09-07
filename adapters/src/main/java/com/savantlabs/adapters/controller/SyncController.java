package com.savantlabs.adapters.controller;

import com.savantlabs.adapters.business.GitHubRepositoryServiceImpl;
import com.savantlabs.adapters.business.RepositoryAdapterFactory;
import com.savantlabs.adapters.enums.RepositoryAdapterType;
import com.savantlabs.adapters.exception.CustomException;
import com.savantlabs.adapters.model.SyncIssuesRequest;
import com.savantlabs.adapters.model.SyncIssuesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {
    private final RepositoryAdapterFactory repositoryAdapterFactory;
//    @Value("${github.default-limit}")
//    private final Integer defaultLimit;

    public SyncController(RepositoryAdapterFactory repositoryAdapterFactory
//                          Integer defaultLimit
    ) {
        this.repositoryAdapterFactory = repositoryAdapterFactory;
//        this.defaultLimit = defaultLimit;
    }

    @PostMapping("/issues")
    public ResponseEntity<SyncIssuesResponse> syncIssues(@RequestBody SyncIssuesRequest syncIssuesRequest)
            throws Exception {
      RepositoryAdapterType repositoryAdapterType =
              RepositoryAdapterType.valueOf(syncIssuesRequest.getRepositoryType());
      if (repositoryAdapterFactory.getRepositoryAdapterService(repositoryAdapterType).isPresent()) {
        return ResponseEntity.ok(repositoryAdapterFactory
                .getRepositoryAdapterService(repositoryAdapterType).get().syncIssues(syncIssuesRequest));
      }

      throw new CustomException(HttpStatusCode.valueOf(400), "repositoryAdapterType: is not implemented");
    }
}