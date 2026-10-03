package com.qudus.docket_backend.email;

import brevo.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final ApiClient apiClient;
    private final String apiKey;
    private final String fromName;
    private final String fromEmail;

    public EmailService(ApiClient apiClient, @Value("${brevo.api-key}") String apiKey, @Value("${brevo.fromName}") String fromName, @Value("${brevo.fromEmail}") String fromEmail) {
        this.apiClient = apiClient;
        this.apiKey = apiKey;
        this.fromName = fromName;
        this.fromEmail = fromEmail;
    }
}
