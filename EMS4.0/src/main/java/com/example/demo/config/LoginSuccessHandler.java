package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.dao.UserDao;
import com.example.demo.entities.User;
import com.example.demo.service.EmailService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Autowired
	private EmailService emailService;
	@Autowired
	private UserDao userdao;

	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String role = auth.getAuthorities().toString();

		if (role == null) {
			throw new UsernameNotFoundException("hi");
		} else {

			User user = userdao.getUserByUserName(auth.getName());

			// Get the username and IP address
			String ipAddress = request.getRemoteAddr();
			String username = user.getUsername();
			String osName = System.getProperty("os.name");
			String osVersion = System.getProperty("os.version");
			String osArchitecture = System.getProperty("os.arch");

			// Prepare email subject
			String subject = "Security Update: Successful Login Detected";

			// Prepare email content
//            String emailContent = "Dear " + username + ", your IP address is " + ipAddress;
			System.out.println("Sending email to: " + auth.getName());

			String emailContent = "" +
				    "<!DOCTYPE html>" +
				    "<html lang='en'>" +
				    "<head>" +
				    "    <meta charset='UTF-8'>" +
				    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
				    "    <style>" +
				    "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }" +
				    "        .email-container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); overflow: hidden; text-align: center; }" +
				    "        .header { background-color: #007BFF; padding: 20px; color: #ffffff; }" +
				    "        .header h1 { margin: 0; font-size: 22px; }" +
				    "        .content { padding: 30px; color: #333333; line-height: 1.6; font-size: 16px; text-align: left; }" +
				    "        .content p { margin-bottom: 20px; }" +
				    "        .content .highlight { font-weight: bold; color: #007BFF; }" +
				    "        .footer { padding: 20px; background-color: #f1f1f1; font-size: 12px; color: #888888; }" +
				    "        .info-box { background-color: #f9f9f9; padding: 15px; border-radius: 8px; border: 1px solid #e2e2e2; margin-bottom: 20px; }" +
				    "        .action-list { list-style-type: none; padding: 0; }" +
				    "        .action-list li { margin-bottom: 10px; }" +
				    "        .colored-logo { font-size: 14px; margin-top: 10px; }" +
				    "    </style>" +
				    "</head>" +
				    "<body>" +
				    "    <div class='email-container'>" +
				    "        <div class='header'>" +
				    "            <h1>Login Attempt Notification</h1>" +
				    "        </div>" +
				    "        <div class='content'>" +
				    "            <p>Dear " + username + ",</p>" +
				    "            <p>Your account has been successfully logged into. Below are the details:</p>" +
				    "            <div class='info-box'>" +
				    "                <p><strong>Username:</strong> <span class='highlight'>" + username + "</span></p>" +
				    "                <p><strong>Email:</strong> <span class='highlight'>" + auth.getName() + "</span></p>" +
				    "                <p><strong>IP Address:</strong> <span class='highlight'>" + ipAddress + "</span></p>" +
				    "                <p><strong>Device Login Time:</strong> <span class='highlight'>" + new Date() + "</span></p>" +
				    "                <p><strong>Device OS:</strong> <span class='highlight'>" + osName + "</span></p>" +
				    "                <p><strong>Device Version:</strong> <span class='highlight'>" + osVersion + "</span></p>" +
				    "                <p><strong>Device Architecture:</strong> <span class='highlight'>" + osArchitecture + "</span></p>" +
				    "            </div>" +
				    "            <p>If this was not you, we recommend taking the following actions:</p>" +
				    "            <ul class='action-list'>" +
				    "                <li>Change your password.</li>" +
				    "                <li>Enable two-factor authentication if not already done.</li>" +
				    "                <li>Review recent account activity.</li>" +
				    "            </ul>" +
				    "            <p>If you need assistance, please contact our support team at <a href='mailto:support@example.com'>support@example.com</a>.</p>" +
				    "            <p>Thank you for your attention to this matter.</p>" +
				    "            <p>Best regards,</p>" +
				    "            <p><strong>Cyber Security Team</strong></p>" +
				    "        </div>" +
				    "        <div class='footer'>" +
				    "            <p>Need help? <a href='#'>Visit our Help Center</a> or contact us at [Support Contact Info]</p>" +
				    "            <p class='colored-logo'>" +
				    "                <span class='colored-char' style='color: rgb(66, 133, 244);'>w</span><span class='colored-char' style='color: rgb(255, 0, 0);'>w</span><span class='colored-char' style='color: rgb(255, 165, 0);'>w</span>" +
				    "                <span class='colored-char' style='color: rgb(0, 0, 255);'>.</span><span class='colored-char' style='color: rgb(60, 179, 113);'>e</span><span class='colored-char' style='color: rgb(255, 0, 0);'>m</span><span class='colored-char' style='color: rgb(0, 0, 255);'>s</span><span class='colored-char' style='color: rgb(255, 0, 0);'>.</span><span class='colored-char' style='color: rgb(255, 165, 0);'>c</span><span class='colored-char' style='color: rgb(0, 0, 255);'>o</span><span class='colored-char' style='color: rgb(255, 0, 0);'>m</span>" +
				    "            </p>" +
				    "        </div>" +
				    "    </div>" +
				    "</body>" +
				    "</html>";


			// Send email asynchronously
			CompletableFuture<Boolean> resultFuture = emailService.sendEmail(emailContent, subject, auth.getName());

			// Handle the result asynchronously
			resultFuture.thenAccept(result -> {
				if (result) {
					System.out.println("Email sent successfully to " + username);
				} else {
					System.out.println("Failed to send email to " + username);
				}
			});

			String targetUrl = getTargetUrlBasedOnRole(role);
			return targetUrl;
		}
	}

	private String getTargetUrlBasedOnRole(String role) {
		if (role.contains("ROLE_USER")) {
			return "/user/new";
		} else if (role.contains("ROLE_ADMIN")) {
			return "/admin/new";
		} else if (role.contains("ROLE_MANAGER")) {
			return "/manager/new";
		} else if (role.contains("ROLE_HR")) {
			return "/hr/new";
		} else if (role.contains("ROLE_IT")) {
			return "/IT/new";
		}
		return "/";
	}
}
