package com.qudus.docket_backend.cloudinary;

import com.cloudinary.Cloudinary;
import com.qudus.docket_backend.family.FamilyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

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
        metaData.put("max_file_size", 10485760);

        String signature = cloudinary.apiSignRequest(metaData, apiSecret,1);

        metaData.put("signature", signature);
        metaData.put("apiKey", apiKey);
        metaData.put("cloudName", cloudName);

        return metaData;

    }

    public boolean isSignatureValid(String familyId, String userId, String publicId, String cloudinaryVersion, String signature) {
        if(!familyService.hasUploadAccess(familyId, userId)) throw new CloudinaryException("upload_forbidden", "You don't have permission to upload to this family", HttpStatus.FORBIDDEN);
        Map<String, Object> toSign = new HashMap<>();
        toSign.put("public_id", publicId);
        toSign.put("version", cloudinaryVersion);

        String expected = cloudinary.apiSignRequest(toSign, apiSecret, 1);

        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    }

    public String createSignedDownloadUrl(String publicId, String version) {
        return cloudinary.url().resourceType("raw").type("authenticated").version(version).signed(true).generate(publicId);
    }

    public void deleteAFile(String publicId) {
        try {
            Map<String, Object> options = Map.of(
                    "resource_type", "raw",
                    "type", "authenticated",
                    "invalidate", true
            );
            Map result = cloudinary.uploader().destroy(publicId, options);
            Object status = result.get("result");
            if (!"ok".equals(status)) {
                log.error("Cloudinary did not delete document with public id: {}, result: {}", publicId, status);
                throw new CloudinaryException("error_deleting", "Failed to delete your file", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            log.error("Failed to delete document from cloudinary public id: {}, with err: {}", publicId, e.getMessage());
            throw new CloudinaryException("error_deleting", "Failed to delete your file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
