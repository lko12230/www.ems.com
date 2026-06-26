package com.ayush.ems.service;

import com.ayush.ems.controller.AdminController;
import com.ayush.ems.entities.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.mail.internet.MimeMessage;

@Service
public class ForgotOTPEmailService {

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

            helper.setFrom("guptaayush12230@gmail.com", "EMS - No Reply");
            helper.setTo(to.trim());
            helper.setSubject(subject);
            helper.setText(message, true); // HTML enabled

            mailSender.send(mimeMessage);

            System.out.println("✅ Forgot OTP Email sent via Brevo SMTP");
            success = true;

        } catch (Exception e) {

            e.printStackTrace();

            String exceptionAsString = e.toString();
            String className = AdminController.class.getName();
            String errorMessage = e.getMessage();

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String fullStackTrace = sw.toString();

            StackTraceElement topElement =
                    e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;

            String methodName = topElement != null ? topElement.getMethodName() : "N/A";
            int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

            // Log error to DB
            servicelayer.insert_error_log(
                    exceptionAsString,
                    className,
                    errorMessage,
                    methodName,
                    lineNumber,
                    fullStackTrace
            );

            System.err.printf("❌ Error in %s.%s at line %d%n", className, methodName, lineNumber);
            System.err.println(fullStackTrace);

            // Add to retry queue
            retryQueue.add(new EmailRequest(message, subject, to));
        }

        return CompletableFuture.completedFuture(success);
    }

    // Retry failed mails
    public void retryFailedEmails() {

        Set<EmailRequest> retryList = new HashSet<>(retryQueue);
        retryQueue.clear();

        for (EmailRequest request : retryList) {
            sendEmail(request.getMessage(), request.getSubject(), request.getTo())
                    .thenAccept(success -> {
                        if (!success) retryQueue.add(request);
                    });
        }
    }
}
