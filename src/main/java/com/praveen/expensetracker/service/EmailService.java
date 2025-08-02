package com.praveen.expensetracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // Optional: many SMTP providers ignore this
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body); // Plain text only

            log.info("Sending email to {}", to);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
