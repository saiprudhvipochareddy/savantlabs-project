package com.savantlabs.adapters.helpers;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.savantlabs.adapters.model.Issue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class FirestoreClient {
    private final Firestore firestore;
    private final String collection;

    public FirestoreClient(Firestore db, Firestore firestore,
                           @Value("${firestore.collection}") String collection) {
        this.firestore = firestore;
        this.collection = collection;
    }

    public void upsert(Issue issue) throws Exception {
        issue.syncedAt = Instant.now();
        Map<String, Object> data = issueToMap(issue);
        ApiFuture<WriteResult> future = firestore.collection(collection)
                .document(issue.id)
                .set(data, SetOptions.merge());
        future.get();
    }

    private Map<String, Object> issueToMap(Issue issue) {
        return Map.of(
                "id", issue.id,
                "owner", issue.owner,
                "repo", issue.repo,
                "title", issue.title,
                "state", issue.state,
                "htmlUrl", issue.htmlUrl,
                "createdAt", issue.createdAt.toString(),
                "syncedAt", issue.syncedAt.toString()
        );
    }
}