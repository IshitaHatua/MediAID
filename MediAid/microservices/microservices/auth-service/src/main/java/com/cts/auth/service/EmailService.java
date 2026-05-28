package com.cts.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject("MediAID - Password Reset OTP");
                message.setText("Your OTP for password reset is: " + otp
                        + ". It is valid for 10 minutes.");
                mailSender.send(message);
                log.info("OTP email sent to {}", to);
                return;
            } catch (Exception e) {
                log.warn("Failed to send OTP email to {} (falling back to console): {}", to, e.getMessage());
            }
        }
        // Fallback when SMTP isn't configured — useful for local dev / tests.
        log.info("OTP for {}: {}", to, otp);
    }
}
