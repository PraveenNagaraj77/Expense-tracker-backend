
package com.praveen.expensetracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RestClient.Builder restClientBuilder;

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String fromEmail;

    private static final String BREVO_API_URL =
            "https://api.brevo.com/v3/smtp/email";

    public void sendEmail(String to, String subject, String body) {
        Map<String, Object> payload = Map.of(
                "sender", Map.of("email", fromEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", body
        );

        sendViaBrevo(payload, to);
    }

    public void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String filename) {

        Map<String, Object> attachmentData = Map.of(
                "content", Base64.getEncoder().encodeToString(attachment),
                "name", filename
        );

        Map<String, Object> payload = Map.of(
                "sender", Map.of("email", fromEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", body,
                "attachment", List.of(attachmentData)
        );

        sendViaBrevo(payload, to);
    }

    private void sendViaBrevo(Map<String, Object> payload, String to) {
        try {
            restClientBuilder.build()
                    .post()
                    .uri(BREVO_API_URL)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email successfully submitted to Brevo for {}", to);

        } catch (RestClientResponseException e) {
            log.error("Brevo API error. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send email through Brevo", e);

        } catch (Exception e) {
            log.error("Failed to send email through Brevo: {}", e.getMessage());
            throw new RuntimeException("Failed to send email through Brevo", e);
        }
    }
}
