package com.studyfinder.service;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/** Sends the one-time SJSU email signup code through the configured SMTP account. */
public class EmailVerificationService {
    public void sendCode(String recipient, String code) throws Exception {
        String host = required("STUDYFINDER_SMTP_HOST");
        String port = System.getenv().getOrDefault("STUDYFINDER_SMTP_PORT", "587");
        String username = required("STUDYFINDER_SMTP_USER");
        String password = required("STUDYFINDER_SMTP_PASSWORD");
        String from = System.getenv().getOrDefault("STUDYFINDER_SMTP_FROM", username);

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session mailSession = Session.getInstance(properties, new jakarta.mail.Authenticator() {
            @Override protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
        message.setSubject("Your SJSU Study Finder verification code");
        message.setText("Your Study Finder verification code is: " + code
                + "\n\nThis code expires in 10 minutes. If you did not request an account, ignore this email.");
        Transport.send(message);
    }

    private String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is not configured.");
        return value;
    }
}
