package com.savantlabs.adapters.controller;

import com.savantlabs.adapters.business.SyncIssuesAdapterFactory;
import com.savantlabs.adapters.enums.SyncIssueAdapterType;
import com.savantlabs.adapters.exception.CustomException;
import com.savantlabs.adapters.model.SyncIssuesRequest;
import com.savantlabs.adapters.model.SyncIssuesResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle sync issues API requests.
 */
@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {
    private final SyncIssuesAdapterFactory syncIssuesAdapterFactory;

    public SyncController(SyncIssuesAdapterFactory syncIssuesAdapterFactory) {
        this.syncIssuesAdapterFactory = syncIssuesAdapterFactory;
    }

    @PostMapping("/issues")
    public ResponseEntity<SyncIssuesResponse> syncIssues(@RequestBody SyncIssuesRequest syncIssuesRequest)
            throws Exception {
        SyncIssueAdapterType syncIssueAdapterType;
        try {
            syncIssueAdapterType = SyncIssueAdapterType.valueOf(syncIssuesRequest.getSyncIssuesType());
        } catch (IllegalArgumentException ex) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "Invalid syncIssueAdapterType: " + syncIssuesRequest.getSyncIssuesType());
        }
        if (syncIssuesAdapterFactory.getRepositoryAdapterService(syncIssueAdapterType).isPresent()) {
            return ResponseEntity.ok(syncIssuesAdapterFactory
                    .getRepositoryAdapterService(syncIssueAdapterType).get().syncIssues(syncIssuesRequest));
        }

        throw new CustomException(HttpStatusCode.valueOf(400), "syncIssueAdapterType: is not implemented");
    }
}