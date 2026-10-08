package com.qudus.docket_backend.cloudinary;

import com.cloudinary.Cloudinary;
import com.qudus.docket_backend.document.ConfirmRequest;
import com.qudus.docket_backend.family.FamilyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;
    private final String apiSecret;
    private final String cloudName;
    private final String apiKey;
    private final FamilyService familyService;

    public CloudinaryService(Cloudinary cloudinary, @Value("${cloudinary.apiSecret}") String apiSecret, @Value("${cloudinary.cloudName}") String cloudName, @Value("${cloudinary.apiKey}") String apiKey, FamilyService familyService) {
        this.cloudinary = cloudinary;
        this.apiSecret = apiSecret;
        this.apiKey = apiKey;
        this.cloudName = cloudName;
        this.familyService = familyService;
    }

    public Map<String, Object> createSignedUrl(String familyId, String userId) {

        if(!familyService.hasUploadAccess(familyId, userId)) throw new CloudinaryException("invalid_user_family", "You cannot upload to this family or you're not a member", HttpStatus.BAD_REQUEST);

        String newId = UUID.randomUUID().toString();
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("public_id", familyId + "/" + newId);
        metaData.put("timestamp", System.currentTimeMillis() / 1000);
        metaData.put("type", "authenticated");
        metaData.put("overwrite", false);

        String signature = cloudinary.apiSignRequest(metaData, apiSecret,1);

        metaData.put("signature", signature);
        metaData.put("apiKey", apiKey);
        metaData.put("cloudName", cloudName);

        return metaData;

    }

    public boolean isSignatureValid(String familyId, String userId, String cloudinaryVersion, String docId, String signature) {
        if(!familyService.hasUploadAccess(familyId, userId)) throw new CloudinaryException("upload_forbidden", "You don't have permission to upload to this family", HttpStatus.FORBIDDEN);
        String publicId = familyId + "/" + docId;
        Map<String, Object> toSign = new HashMap<>();
        toSign.put("public_id", publicId);
        toSign.put("version", cloudinaryVersion);

        String expected = cloudinary.apiSignRequest(toSign, apiSecret, 1);

        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    }
}
