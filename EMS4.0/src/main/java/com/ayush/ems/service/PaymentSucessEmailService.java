package com.ayush.ems.service;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ayush.ems.entities.PaymentEmailRequest;

@Service
public class PaymentSucessEmailService {
	
	
	// Retry queue for failed emails (using a Set to prevent duplicates)
    private final Set<PaymentEmailRequest> retryQueue = new HashSet<>();
    
	@Async
	public CompletableFuture<Boolean> sendEmail(String get_path,String message, String subject, String to) {
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

		
session.setDebug(true);
		
		//Step 2 : compose the message [text,multi media]
		MimeMessage m = new MimeMessage(session);
		
		try {
		
		//from email
		m.setFrom(from);
		
		//adding recipient to message
		m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		
		//adding subject to message
		m.setSubject(subject);
	
		
		//attachement..
		
		//file path
		String path=get_path;
		
		
		MimeMultipart mimeMultipart = new MimeMultipart();
		//text
		//file
		
		MimeBodyPart textMime = new MimeBodyPart();
		
		MimeBodyPart fileMime = new MimeBodyPart();
		
		try {
			
		    textMime.setContent(message, "text/html");

			
			File file=new File(path);
			fileMime.attachFile(file);
			
			
			mimeMultipart.addBodyPart(textMime);
			mimeMultipart.addBodyPart(fileMime);
		
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
		m.setContent(mimeMultipart);
		
		
		//send 
		
		//Step 3 : send the message using Transport class
		Transport.send(m);
		
		
		success=true;
		} catch (Exception e) {
//				  // If sending fails, add to retry queue
            System.out.println("Failed to send email, adding to retry queue");
            retryQueue.add(new PaymentEmailRequest(get_path,message, subject, to));
            e.printStackTrace();
		}
		// Return a CompletableFuture
        return CompletableFuture.completedFuture(success);
	}

	
	  // Method to retry sending emails from the queue
    public void retryFailedEmails() {
    	  Set<PaymentEmailRequest> retryList = new HashSet<>(retryQueue); // Copy the queue
//        List<EmailRequest> retryList = new LinkedList<>(retryQueue);
        retryQueue.clear(); // Clear queue before retry
       System.out.println("CLEAR LIST AFTER COPY LIST INTO RETRY LIST -> "+retryQueue);
       System.out.println("RETRY LIST -> "+retryList);
       System.out.println("Size "+retryList.size());
        for (PaymentEmailRequest request : retryList) {
            CompletableFuture<Boolean> result = sendEmail(request.getInvoice(),request.getMessage(), request.getSubject(), request.getTo());
            result.thenAccept(success -> {
                if (!success) {
                    retryQueue.add(request); // Add back to queue if it fails again
                }
            });
            System.out.println("SUCCESS LIST "+retryList);
        }
    }
}
