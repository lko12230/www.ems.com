package com.ayush.ems.service;

import com.ayush.ems.controller.AdminController;
import com.ayush.ems.entities.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class TeamEmailService {

    @Autowired
    private Servicelayer servicelayer;

    @Autowired
    private JavaMailSender mailSender;

    private final Set<EmailRequest> retryQueue = new HashSet<>();

    @Async
    public CompletableFuture<Boolean> sendEmail(
            String message,
            String subject,
            String to) {

        boolean success = false;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("guptaayush12230@gmail.com", "EMS - Team");
            helper.setTo(to.trim());
            helper.setSubject(subject);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Team Email sent via Brevo");
            success = true;

        } catch (Exception e) {
            logError(e);
            retryQueue.add(new EmailRequest(message, subject, to));
        }

        return CompletableFuture.completedFuture(success);
    }

    public void retryFailedEmails() {
        Set<EmailRequest> retryList = new HashSet<>(retryQueue);
        retryQueue.clear();

        for (EmailRequest req : retryList) {
            sendEmail(req.getMessage(), req.getSubject(), req.getTo())
                    .thenAccept(success -> {
                        if (!success) retryQueue.add(req);
                    });
        }
    }

    private void logError(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        servicelayer.insert_error_log(
                e.toString(),
                AdminController.class.getName(),
                e.getMessage(),
                e.getStackTrace()[0].getMethodName(),
                e.getStackTrace()[0].getLineNumber(),
                sw.toString()
        );
    }
}
