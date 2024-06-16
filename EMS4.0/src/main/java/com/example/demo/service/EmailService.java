package com.example.demo.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	@Autowired
	private servicelayer servicelayer;
	public boolean sendEmail(String message, String subject, String to) {
		boolean f = false;
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
			m.setSubject(subject);
			// m.setText(message);
			m.setContent(message, "text/html");
			Transport.send(m);
			System.out.println("sent success");

			f = true;
		} catch (Exception e) {
//			e.printStackTrace();
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = EmailService.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
		}
		return f;
	}

}
