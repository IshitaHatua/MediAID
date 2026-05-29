package com.cts.auth.service;

public interface EmailService {

    void sendOtpEmail(String to, String otp);
}
