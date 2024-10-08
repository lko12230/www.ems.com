package com.ayush.ems.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ayush.ems.entities.EmailRequest;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class TeamEmailService {
	// Retry queue for failed emails
//    private final List<EmailRequest> retryQueue = new LinkedList<>();

	// Retry queue for failed emails (using a Set to prevent duplicates)
	private final Set<EmailRequest> retryQueue = new HashSet<>();

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
			// If sending fails, add to retry queue
			System.out.println("Failed to send email, adding to retry queue");
			retryQueue.add(new EmailRequest(message, subject, to));
			e.printStackTrace();
		}

		// Return a CompletableFuture
		return CompletableFuture.completedFuture(success);
	}

	// Method to retry sending emails from the queue
	public void retryFailedEmails() {
		Set<EmailRequest> retryList = new HashSet<>(retryQueue); // Copy the queue
//        List<EmailRequest> retryList = new LinkedList<>(retryQueue);
		retryQueue.clear(); // Clear queue before retry
		System.out.println("CLEAR LIST AFTER COPY LIST INTO RETRY LIST -> " + retryQueue);
		System.out.println("RETRY LIST -> " + retryList);
		System.out.println("Size " + retryList.size());
		for (EmailRequest request : retryList) {
			CompletableFuture<Boolean> result = sendEmail(request.getMessage(), request.getSubject(), request.getTo());
			result.thenAccept(success -> {
				if (!success) {
					retryQueue.add(request); // Add back to queue if it fails again
				}
			});
			System.out.println("SUCCESS LIST " + retryList);
		}
	}
}
