package com.ayush.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ayush.ems.dao.UserDao;
import com.ayush.ems.entities.User;
import com.ayush.ems.service.EmailService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
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
        
        // Get the username (email) and IP address
        String ipAddress = request.getRemoteAddr();
        String username;
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArchitecture = System.getProperty("os.arch");

        if (auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            // OAuth2 login (e.g., Google)
            org.springframework.security.oauth2.core.user.OAuth2User oauthUser = (org.springframework.security.oauth2.core.user.OAuth2User) auth.getPrincipal();
            username = oauthUser.getAttribute("email");  // Assuming you're using Google OAuth2 email as username
        } else {
            username = auth.getName(); // For form-based login
        }

        // Fetch user from the database using the email
        User user = userdao.getUserByUserName(username);
        System.out.println(username+ " SIGNIN WITH GOOGLE "+user);
        if (user == null) {
            // User not found, send error response
            try {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not registered.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return null; // Stop further processing
        }

        // Prepare email content and send notification
        sendLoginNotificationEmail(username, ipAddress, osName, osVersion, osArchitecture);

        // Get role and redirect
        return getTargetUrlBasedOnRole(user.getRole());
    }

    private void sendLoginNotificationEmail(String username, String ipAddress, String osName, String osVersion, String osArchitecture) {
        // Prepare email subject
        String subject = "Security Update: Successful Login Detected";

        // Prepare email content (similar to your existing content)
        String emailContent = "<!DOCTYPE html>" +
            "<html lang='en'>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "    <style>" +
            "        /* Your styles here */" +
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
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";

        // Send email asynchronously
        CompletableFuture<Boolean> resultFuture = emailService.sendEmail(emailContent, subject, username);
        resultFuture.thenAccept(result -> {
            if (result) {
                System.out.println("Email sent successfully to " + username);
            } else {
                System.out.println("Failed to send email to " + username);
            }
        });
    }

    private String getTargetUrlBasedOnRole(String role) {
        if (role.contains("ROLE_USER")) {
            return "/user/new";
        } else if (role.contains("ROLE_ADMIN")) {
            return "/admin/new";
        } else if (role.contains("ROLE_HR")) {
            return "/hr/new";
        } else if (role.contains("ROLE_IT")) {
            return "/IT/new";
        } else if (role.contains("ROLE_MANAGER")) {
            return "/manager/new";
        } else {
            throw new IllegalStateException();
        }
    }
}
