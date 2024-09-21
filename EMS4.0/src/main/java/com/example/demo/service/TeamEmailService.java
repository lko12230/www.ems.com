package com.example.demo.service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
public class TeamEmailService {

    @Async
    public CompletableFuture<Boolean> sendEmail(String message, String subject, String to) {
        boolean success = false;
        String from = "guptaayush12418@gmail.com";
        String host = "smtp.gmail.com";

        // Set email properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Authenticate and create session
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("guptaayush12418@gmail.com", "notjjadglqnlsjqk");
            }
        });

        try {
            // Create the message
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(from);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setSubject(subject);
            mimeMessage.setContent(message, "text/html");

            // Send the message
            Transport.send(mimeMessage);
            System.out.println("Email sent successfully!");
            success = true;
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        // Return a CompletableFuture
        return CompletableFuture.completedFuture(success);
    }
}
