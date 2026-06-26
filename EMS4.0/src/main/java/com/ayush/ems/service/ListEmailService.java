package com.ayush.ems.service;

import com.ayush.ems.controller.AdminController;
import com.ayush.ems.entities.EmailRequestCCList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class ListEmailService {

    @Autowired
    private Servicelayer servicelayer;

    @Autowired
    private JavaMailSender mailSender;

    private final Set<EmailRequestCCList> retryQueue = new HashSet<>();

    @Async
    public CompletableFuture<Boolean> sendEmail(
            String message,
            String subject,
            String to,
            String cc,
            List<String> ccList) {

        boolean success = false;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("guptaayush12230@gmail.com", "EMS - Notifications");
            helper.setTo(to.trim());

            if (cc != null && !cc.trim().isEmpty()) {
                helper.setCc(cc.trim());
            }

            if (ccList != null) {
                for (String email : ccList) {
                    if (email != null && !email.trim().isEmpty()) {
                        helper.addCc(email.trim());
                    }
                }
            }

            helper.setSubject(subject);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
            System.out.println("✅ List Email sent via Brevo");
            success = true;

        } catch (Exception e) {
            logError(e);
            retryQueue.add(new EmailRequestCCList(message, subject, to, cc, ccList));
        }

        return CompletableFuture.completedFuture(success);
    }

    public void retryFailedEmails() {
        Set<EmailRequestCCList> retryList = new HashSet<>(retryQueue);
        retryQueue.clear();

        for (EmailRequestCCList req : retryList) {
            sendEmail(
                    req.getMessage(),
                    req.getSubject(),
                    req.getTo(),
                    req.getCc(),
                    req.getEmailList()
            ).thenAccept(success -> {
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

        System.err.println("❌ List Email failed");
    }
}
