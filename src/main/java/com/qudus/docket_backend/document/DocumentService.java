//package com.qudus.docket_backend.document;
//
//import com.google.cloud.firestore.Firestore;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class DocumentService {
//    private final Firestore db;
//    public DocumentService(Firestore db) {
//        this.db = db;
//    }
//
//    public void upload(String familyId, String userId) {
//        var docRef = db.collection("documents").document(familyId).collection("users").document(userId);
//        Map<String, Object> data = new HashMap<>();
//        data.put("documentId", )
//    }
//}
