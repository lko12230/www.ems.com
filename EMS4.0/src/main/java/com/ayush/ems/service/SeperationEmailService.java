package com.ayush.ems.service;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ayush.ems.entities.EmailRequestCC;

@Service
public class SeperationEmailService {
	
	// Retry queue for failed emails (using a Set to prevent duplicates)
		private final Set<EmailRequestCC> retryQueue = new HashSet<>();
	
	@Async
	public CompletableFuture<Boolean> sendEmail(String message, String subject, String to, String cc) {
		boolean success = false;
		// variable for gmail host
		String from = "guptaayush12418@gmail.com";
		String host = "smtp.gmail.com";

		// get the system properties
		Properties properties = System.getProperties();
		System.out.println("properties " + properties);

		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// step 1
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication("guptaayush12418@gmail.com", "notjjadglqnlsjqk");
			}

		});

		// step 2
		session.setDebug(true);
		MimeMessage m = new MimeMessage(session);

		try {
			// from email
			m.setFrom(from);
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			m.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
			m.setSubject(subject);
			// m.setText(message);
			m.setContent(message, "text/html");
			Transport.send(m);
			System.out.println("sent success");
		}
			 catch (MessagingException e) {
					// If sending fails, add to retry queue
					System.out.println("Failed to send email, adding to retry queue");
					retryQueue.add(new EmailRequestCC(message, subject, to, cc));
//					e.printStackTrace();
				}
		return CompletableFuture.completedFuture(success);
	}
	
	
	// Method to retry sending emails from the queue
		public void retryFailedEmails() {
			Set<EmailRequestCC> retryList = new HashSet<>(retryQueue); // Copy the queue
//	        List<EmailRequest> retryList = new LinkedList<>(retryQueue);
			retryQueue.clear(); // Clear queue before retry
			System.out.println("CLEAR LIST AFTER COPY LIST INTO RETRY LIST -> " + retryQueue);
			System.out.println("RETRY LIST -> " + retryList);
			System.out.println("Size " + retryList.size());
			for (EmailRequestCC request : retryList) {
				CompletableFuture<Boolean> result = sendEmail(request.getMessage(), request.getSubject(), request.getTo(), request.getCc());
				result.thenAccept(success -> {
					if (!success) {
						retryQueue.add(request); // Add back to queue if it fails again
					}
				});
				System.out.println("SUCCESS LIST " + retryList);
			}
		}
}
