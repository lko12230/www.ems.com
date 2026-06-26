package com.ayush.ems.service;

import com.ayush.ems.controller.AdminController;
import com.ayush.ems.entities.PaymentEmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class PaymentSucessEmailService {

    @Autowired
    private Servicelayer servicelayer;

    @Autowired
    private JavaMailSender mailSender;

    private final Set<PaymentEmailRequest> retryQueue = new HashSet<>();

    @Async
    public CompletableFuture<Boolean> sendEmail(
            String filePath,
            String message,
            String subject,
            List<String> toList) {

        boolean success = false;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("guptaayush12230@gmail.com", "EMS - No Reply");
            helper.setSubject(subject);
            helper.setText(message, true);

            // Add multiple recipients
            if (toList != null) {
                for (String to : toList) {
                    if (to != null && !to.trim().isEmpty()) {
                        helper.addTo(to.trim());
                    }
                }
            }

            // Attachment
            File file = new File(filePath);
            if (file.exists()) {
                helper.addAttachment(file.getName(), file);
            }

            mailSender.send(mimeMessage);

            System.out.println("✅ Payment Success Email sent via Brevo SMTP");
            success = true;

        } catch (Exception e) {

            logError(e);
            retryQueue.add(new PaymentEmailRequest(filePath, message, subject, toList));
        }

        return CompletableFuture.completedFuture(success);
    }

    public void retryFailedEmails() {
        Set<PaymentEmailRequest> retryList = new HashSet<>(retryQueue);
        retryQueue.clear();

        for (PaymentEmailRequest request : retryList) {
            sendEmail(
                    request.getInvoice(),
                    request.getMessage(),
                    request.getSubject(),
                    request.getToList()
            ).thenAccept(success -> {
                if (!success) retryQueue.add(request);
            });
        }
    }

    private void logError(Exception e) {

        String exceptionAsString = e.toString();
        String className = AdminController.class.getName();
        String errorMessage = e.getMessage();

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String fullStackTrace = sw.toString();

        StackTraceElement top = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
        String methodName = top != null ? top.getMethodName() : "N/A";
        int lineNumber = top != null ? top.getLineNumber() : -1;

        servicelayer.insert_error_log(
                exceptionAsString,
                className,
                errorMessage,
                methodName,
                lineNumber,
                fullStackTrace
        );

        System.err.println("❌ Payment Email Failed");
        System.err.println(fullStackTrace);
    }
}
