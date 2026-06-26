package com.ayush.ems.service;

import com.ayush.ems.entities.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
public class EmailService {

    @Autowired
    private Servicelayer servicelayer;

    @Autowired
    private JavaMailSender mailSender;

    private final Set<EmailRequest> retryQueue = new HashSet<>();

    @Async
    public CompletableFuture<Boolean> sendEmail(String message, String subject, String to) {

        boolean success = false;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

//            helper.setFrom("no-reply@ems.com", "NoReply - EMS Portal");
            helper.setFrom("guptaayush12230@gmail.com", "EMS - No Reply");

            helper.setTo(to.trim());
            helper.setSubject(subject);
            helper.setText(message, true);   // HTML enable

            JavaMailSenderImpl sender = (JavaMailSenderImpl) mailSender;

            System.out.println("=================================");
            System.out.println("HOST = " + sender.getHost());
            System.out.println("PORT = " + sender.getPort());
            System.out.println("USER = " + sender.getUsername());
            System.out.println("PASS = " + sender.getPassword());
            System.out.println("=================================");
            mailSender.send(mimeMessage);

            System.out.println("✅ Email sent via Brevo SMTP");
            success = true;

        } catch (Exception e) {

            // Basic info
            String exceptionAsString = e.toString();
            String className = this.getClass().getName();
            String errorMessage = e.getMessage();

            // Full stack trace
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String fullStackTrace = sw.toString();

            // Extract top frame
            StackTraceElement top = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
            String methodName = top != null ? top.getMethodName() : "N/A";
            int lineNumber = top != null ? top.getLineNumber() : -1;

            // Save error in DB
            servicelayer.insert_error_log(
                    exceptionAsString,
                    className,
                    errorMessage,
                    methodName,
                    lineNumber,
                    fullStackTrace
            );

            System.err.println("-------------------------------------------------------");
            System.err.println("❌ Email Sending Failed (Brevo SMTP)");
            System.err.println("Class: " + className);
            System.err.println("Method: " + methodName);
            System.err.println("Line: " + lineNumber);
            System.err.println("Error: " + errorMessage);
            System.err.println("-------------------------------------------------------");
            System.err.println(fullStackTrace);
            System.err.println("-------------------------------------------------------");

            retryQueue.add(new EmailRequest(message, subject, to));
        }

        return CompletableFuture.completedFuture(success);
    }

    // Retry mechanism
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
}
