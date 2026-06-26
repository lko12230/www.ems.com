package com.ayush.ems.service;

import com.ayush.ems.controller.AdminController;
import com.ayush.ems.entities.EmailRequestCC;
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
public class TaskEmailService {

    @Autowired
    private Servicelayer servicelayer;

    @Autowired
    private JavaMailSender mailSender;

    private final Set<EmailRequestCC> retryQueue = new HashSet<>();

    @Async
    public CompletableFuture<Boolean> sendEmail(
            String message,
            String subject,
            String to,
            String cc) {

        boolean success = false;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("guptaayush12230@gmail.com", "EMS - Tasks");
            helper.setTo(to.trim());
            if (cc != null && !cc.trim().isEmpty()) helper.setCc(cc.trim());
            helper.setSubject(subject);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Task Email sent via Brevo");
            success = true;

        } catch (Exception e) {
            logError(e);
            retryQueue.add(new EmailRequestCC(message, subject, to, cc));
        }

        return CompletableFuture.completedFuture(success);
    }

    public void retryFailedEmails() {
        Set<EmailRequestCC> retryList = new HashSet<>(retryQueue);
        retryQueue.clear();

        for (EmailRequestCC req : retryList) {
            sendEmail(req.getMessage(), req.getSubject(), req.getTo(), req.getCc())
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
