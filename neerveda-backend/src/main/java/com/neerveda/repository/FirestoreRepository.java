package com.neerveda.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * 🗄️ FirestoreRepository — Generic Firestore CRUD operations.
 *
 * All collection-specific repositories delegate to this class.
 * Handles serialization / deserialization to/from Firestore documents.
 */
@Slf4j
@Component
public class FirestoreRepository {

    private final Firestore firestore;

    @Autowired
    public FirestoreRepository(@Autowired(required = false) Firestore firestore) {
        this.firestore = firestore;
        if (firestore == null) {
            log.warn("⚠️  FirestoreRepository initialized without Firestore — all DB calls will return empty results.");
        }
    }

    private void checkFirestore() {
        if (firestore == null) {
            throw new RuntimeException(
                "Firestore is not initialized. Set GOOGLE_APPLICATION_CREDENTIALS_JSON on Render."
            );
        }
    }

    // -------------------------------------------------------
    // SAVE (Create / Update)
    // -------------------------------------------------------

    public <T> String save(String collection, String documentId, T entity) {
        checkFirestore();
        try {
            CollectionReference ref = firestore.collection(collection);
            DocumentReference docRef = (documentId != null)
                ? ref.document(documentId)
                : ref.document();
            ApiFuture<WriteResult> result = docRef.set(entity);
            result.get();
            log.debug("✅ Saved document to {}/{}", collection, docRef.getId());
            return docRef.getId();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Error saving to Firestore: {}", e.getMessage());
            throw new RuntimeException("Firestore save failed", e);
        }
    }

    // -------------------------------------------------------
    // FIND BY ID
    // -------------------------------------------------------

    public Optional<Map<String, Object>> findById(String collection, String documentId) {
        checkFirestore();
        try {
            DocumentSnapshot doc = firestore.collection(collection)
                .document(documentId).get().get();
            if (doc.exists()) {
                Map<String, Object> data = doc.getData();
                if (data != null) data.put("id", doc.getId());
                return Optional.ofNullable(data);
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Error fetching document: {}", e.getMessage());
            throw new RuntimeException("Firestore fetch failed", e);
        }
    }

    // -------------------------------------------------------
    // FIND ALL
    // -------------------------------------------------------

    public List<Map<String, Object>> findAll(String collection) {
        checkFirestore();
        try {
            QuerySnapshot snapshot = firestore.collection(collection).get().get();
            List<Map<String, Object>> results = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> data = new HashMap<>(Objects.requireNonNullElse(doc.getData(), Map.of()));
                data.put("id", doc.getId());
                results.add(data);
            }
            return results;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Error fetching all documents: {}", e.getMessage());
            throw new RuntimeException("Firestore findAll failed", e);
        }
    }

    // -------------------------------------------------------
    // FIND BY FIELD
    // -------------------------------------------------------

    public List<Map<String, Object>> findByField(String collection, String field, Object value) {
        try {
            QuerySnapshot snapshot = firestore.collection(collection)
                .whereEqualTo(field, value).get().get();
            List<Map<String, Object>> results = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> data = new HashMap<>(Objects.requireNonNullElse(doc.getData(), Map.of()));
                data.put("id", doc.getId());
                results.add(data);
            }
            return results;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Error querying Firestore: {}", e.getMessage());
            throw new RuntimeException("Firestore query failed", e);
        }
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    public void update(String collection, String documentId, Map<String, Object> updates) {
        try {
            firestore.collection(collection).document(documentId).update(updates).get();
            log.debug("✅ Updated document {}/{}", collection, documentId);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Error updating document: {}", e.getMessage());
            throw new RuntimeException("Firestore update failed", e);
        }
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------

    public void delete(String collection, String documentId) {
        try {
            firestore.collection(collection).document(documentId).delete().get();
            log.debug("🗑️  Deleted document {}/{}", collection, documentId);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Error deleting document: {}", e.getMessage());
            throw new RuntimeException("Firestore delete failed", e);
        }
    }

    // -------------------------------------------------------
    // COUNT
    // -------------------------------------------------------

    public long count(String collection) {
        try {
            return firestore.collection(collection).get().get().size();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore count failed", e);
        }
    }

    // -------------------------------------------------------
    // ORDERED + LIMITED QUERY
    // -------------------------------------------------------

    public List<Map<String, Object>> findRecentByField(
            String collection, String field, Object value,
            String orderBy, int limit) {
        try {
            QuerySnapshot snapshot = firestore.collection(collection)
                .whereEqualTo(field, value)
                .orderBy(orderBy, Query.Direction.DESCENDING)
                .limit(limit)
                .get().get();

            List<Map<String, Object>> results = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> data = new HashMap<>(Objects.requireNonNullElse(doc.getData(), Map.of()));
                data.put("id", doc.getId());
                results.add(data);
            }
            return results;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore ordered query failed", e);
        }
    }
}
