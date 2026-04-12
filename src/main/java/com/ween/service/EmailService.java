package com.ween.service;

// Email Service (DISABLED - Mail configuration commented out)
/*
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${ween.mail.from:noreply@ween.com}")
    private String fromEmail;

    @Value("${ween.app.name:Ween}")
    private String appName;

    public void sendVerificationEmail(String to, String fullName, String verificationLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(appName + " - Email Verification");

            String htmlContent = buildVerificationEmailHtml(fullName, verificationLink);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Verification email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", to, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendPasswordResetEmail(String to, String fullName, String resetLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(appName + " - Password Reset Request");

            String htmlContent = buildPasswordResetEmailHtml(fullName, resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendCertificateEmail(String to, String fullName, String eventTitle, String certificateNumber, String downloadLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(appName + " - Certificate of Participation");

            String htmlContent = buildCertificateEmailHtml(fullName, eventTitle, certificateNumber, downloadLink);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Certificate email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send certificate email to: {}", to, e);
            throw new RuntimeException("Failed to send certificate email", e);
        }
    }

    public void sendWelcomeEmail(String to, String fullName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Welcome to " + appName + "!");

            String htmlContent = buildWelcomeEmailHtml(fullName);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Welcome email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", to, e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    public void sendEventNotificationEmail(String to, String fullName, String eventTitle, String eventDetails) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(appName + " - Event: " + eventTitle);
            message.setText("Dear " + fullName + ",\n\n" + eventDetails);

            mailSender.send(message);
            log.info("Event notification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send event notification email to: {}", to, e);
            throw new RuntimeException("Failed to send event notification email", e);
        }
    }

    private String buildVerificationEmailHtml(String fullName, String verificationLink) {
        return "<html><body style=\"font-family: Arial, sans-serif;\">" +
                "<h2>Email Verification</h2>" +
                "<p>Dear " + fullName + ",</p>" +
                "<p>Thank you for registering with " + appName + "!</p>" +
                "<p>Please verify your email by clicking the button below:</p>" +
                "<a href=\"" + verificationLink + "\" style=\"background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">Verify Email</a>" +
                "<p>If you did not create this account, please ignore this email.</p>" +
                "<p>Best regards,<br>" + appName + " Team</p>" +
                "</body></html>";
    }

    private String buildPasswordResetEmailHtml(String fullName, String resetLink) {
        return "<html><body style=\"font-family: Arial, sans-serif;\">" +
                "<h2>Password Reset Request</h2>" +
                "<p>Dear " + fullName + ",</p>" +
                "<p>We received a request to reset your password. Click the button below to reset it:</p>" +
                "<a href=\"" + resetLink + "\" style=\"background-color: #008CBA; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">Reset Password</a>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>If you did not request this, please ignore this email.</p>" +
                "<p>Best regards,<br>" + appName + " Team</p>" +
                "</body></html>";
    }

    private String buildCertificateEmailHtml(String fullName, String eventTitle, String certificateNumber, String downloadLink) {
        return "<html><body style=\"font-family: Arial, sans-serif;\">" +
                "<h2>Certificate of Participation</h2>" +
                "<p>Dear " + fullName + ",</p>" +
                "<p>Congratulations! You have successfully completed the event:</p>" +
                "<p><strong>" + eventTitle + "</strong></p>" +
                "<p>Certificate Number: " + certificateNumber + "</p>" +
                "<p>Download your certificate:</p>" +
                "<a href=\"" + downloadLink + "\" style=\"background-color: #FF9800; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">Download Certificate</a>" +
                "<p>Best regards,<br>" + appName + " Team</p>" +
                "</body></html>";
    }

    private String buildWelcomeEmailHtml(String fullName) {
        return "<html><body style=\"font-family: Arial, sans-serif;\">" +
                "<h2>Welcome to " + appName + "!</h2>" +
                "<p>Dear " + fullName + ",</p>" +
                "<p>Welcome to our community! Get started by exploring upcoming events and making a difference in your community.</p>" +
                "<p>Happy volunteering!<br>" + appName + " Team</p>" +
                "</body></html>";
    }
}
*/
