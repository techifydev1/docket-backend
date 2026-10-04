package com.qudus.docket_backend;

import brevoApi.TransactionalEmailsApi;
import com.qudus.docket_backend.email.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void runEmailTest() {
        emailService.sendEmail("techifydev1@gmail.com", "Testing email, testing testing testing testing");
    }
}
