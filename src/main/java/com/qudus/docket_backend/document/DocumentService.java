package com.qudus.docket_backend.document;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.qudus.docket_backend.cloudinary.CloudinaryException;
import com.qudus.docket_backend.cloudinary.CloudinaryService;
import com.qudus.docket_backend.family.Family;
import com.qudus.docket_backend.family.FamilyService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class DocumentService {
    private final CloudinaryService cloudinaryService;
    private final FamilyService familyService;
    private final Firestore db;
    private final Logger log = LoggerFactory.getLogger(DocumentService.class);

    public DocumentService(CloudinaryService cloudinaryService, FamilyService familyService, Firestore db) {
        this.cloudinaryService = cloudinaryService;
        this.familyService = familyService;
        this.db = db;
    }

    public Map<String, Object> getSignedUrl(String familyId, String userId) {
        return cloudinaryService.createSignedUrl(familyId, userId);
    }

    public void confirmUpload(String familyId, @NonNull String userId, @NonNull ConfirmRequest request) {
        try {
            if(!userId.equals(request.ownerId()) && !familyService.canUploadForOthers(familyId, userId)) throw new CloudinaryException("cannot_upload_for_others", "Only admins and the vault owner can add documents for someone else", HttpStatus.FORBIDDEN);
            if(!cloudinaryService.isSignatureValid(familyId, userId, request.publicId(), request.cloudinaryVersion(), request.signature())) throw new CloudinaryException("invalid_upload_signature", "Upload could not be verified", HttpStatus.BAD_REQUEST);
            var docRef = db.collection("families").document(familyId).collection("documents").document(request.docId());
            var familyRef = db.collection("families").document(familyId);
            db.runTransaction(tx -> {
                var family = tx.get(familyRef).get().toObject(Family.class);
                var currentKey = family.getKeyVersion();
                if(currentKey != Integer.decode(request.kv())) throw new CloudinaryException("key_version_stale", "The family key changed. Re-encrypt and upload again.", HttpStatus.CONFLICT);
                tx.create(docRef, new Document(request.encryptedMetadata(), Integer.decode(request.kv()), userId, Instant.now().toString(), request.ownerId(), request.publicId(), request.cloudinaryVersion()));
                return null;
            }).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new CloudinaryException("interna_server_error", "An unknown error occurred, please try again", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<DocumentResponse> getUserDocumentsInAFamily(String userId, String familyId) {
        try {
            if(!familyService.isUserInAFamily(familyId, userId)) throw new CloudinaryException("not_in_family", "You are not a member of this family", HttpStatus.FORBIDDEN);
            var docRef = db.collection("families").document(familyId).collection("documents").orderBy("addedAt", Query.Direction.DESCENDING);
            var snapshot = docRef.get().get();
            return snapshot.getDocuments().stream().map(d -> {
                Document doc = d.toObject(Document.class);
                return new DocumentResponse(d.getId(), doc.getEncryptedMetadata(), (int) doc.getKeyVersion(), doc.getAddedBy(), doc.getAddedAt(), doc.getOwnerId(), doc.getPublicId(), doc.getCloudinaryVersion());
            }).toList();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getDownloadUrl(String familyId, String docId, String userId) {
        try {
            if(!familyService.isUserInAFamily(familyId, userId)) throw new CloudinaryException("not_in_family", "You cannot view documents inside of this family", HttpStatus.FORBIDDEN);
            var docRef = db.collection("families").document(familyId).collection("documents").document(docId);
            var snapshot = docRef.get().get();
            if(!snapshot.exists()) throw new CloudinaryException("not_found", "Document not found", HttpStatus.NOT_FOUND);
            Document parsedSnapshot = snapshot.toObject(Document.class);
            return Map.of("url", cloudinaryService.createSignedDownloadUrl(parsedSnapshot.getPublicId(), parsedSnapshot.getCloudinaryVersion()));
        } catch (ExecutionException | InterruptedException e) {
            log.error("An error occurred while getting a download url for user: {} in family: {}. The error: {}", userId, familyId, e.getMessage());
            throw new CloudinaryException("unknown_error", "An unexpected error occurred, please try again", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String, String> delete(String userId, String docId, String familyId) {
        try {
            var docRef = db.collection("families").document(familyId).collection("documents").document(docId);
            var snapshot = docRef.get().get();
            if(!snapshot.exists()) throw new CloudinaryException("not_found", "Document not found", HttpStatus.NOT_FOUND);
            Document document = snapshot.toObject(Document.class);
            if(!familyService.canDeleteDocument(familyId, userId, document.getOwnerId())) throw new CloudinaryException("delete_forbidden", "You don't have permission to delete this document", HttpStatus.FORBIDDEN);
            cloudinaryService.deleteAFile(document.getPublicId());
            docRef.delete().get();
            log.info("Document {} deleted from family {} by user {}", docId, familyId, userId);
            return Map.of("message", "File/Document deleted successfully");
        } catch (ExecutionException | InterruptedException e) {
            log.error("An error occurred while deleting document: {} in family: {}. The error: {}", docId, familyId, e.getMessage());
            throw new CloudinaryException("unknown_error", "An unexpected error occurred, please try again", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
