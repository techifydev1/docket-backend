package com.qudus.docket_backend.email;

import brevo.ApiClient;
import brevo.ApiException;
import brevo.Configuration;
import brevo.auth.ApiKeyAuth;
import brevoApi.TransactionalEmailsApi;
import brevoModel.CreateSmtpEmail;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {
    private final ApiClient apiClient;
    private final String fromName;
    private final String fromEmail;
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(@Value("${brevo.api-key}") String apiKey, @Value("${brevo.fromName}") String fromName, @Value("${brevo.fromEmail}") String fromEmail) {
        this.apiClient = Configuration.getDefaultApiClient();
        this.apiClient.setBasePath("https://api.brevo.com/v3");
        this.apiClient.setApiKey(apiKey);
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) this.apiClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);
        this.fromName = fromName;
        this.fromEmail = fromEmail;
    }

    @Async
    public void sendEmail(String toEmail, String content) {
        try {
            SendSmtpEmail email = new SendSmtpEmail()
                    .sender(new SendSmtpEmailSender().name(fromName).email(fromEmail)).to(List.of(new SendSmtpEmailTo().email(toEmail)))
                    .subject("Verify you docket account").htmlContent(content);
            CreateSmtpEmail response = new TransactionalEmailsApi(apiClient).sendTransacEmail(email);
            log.info("Email dispatched to email: {} with id: {}", toEmail, response.getMessageId());
        } catch (ApiException e) {
            log.error("Failed to dispatch email to email: {} {} code={}", toEmail, e.getMessage(), e.getCode());
        }
    }


}
