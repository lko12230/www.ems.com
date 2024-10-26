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
import java.util.List;
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
        
        // Get the IP address
        String ipAddress = request.getRemoteAddr();
        String username = null;
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArchitecture = System.getProperty("os.arch");

        // Check if it's an OAuth2 login
        if (auth instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) auth;
            
            // Get the provider (registration ID)
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            // Distinguish between Google and GitHub based on provider (registrationId)
            if ("google".equalsIgnoreCase(registrationId)) {
                // Google OAuth2 login
                username = oauthToken.getPrincipal().getAttribute("email");
                System.out.println("GOOGLE login with email: " + username);
            } else if ("github".equalsIgnoreCase(registrationId)) {
                // GitHub OAuth2 login
                if (oauthToken.getPrincipal().getAttributes().containsKey("email")) {
                    username = oauthToken.getPrincipal().getAttribute("email");
                    System.out.println("GITHUB login with email: " + username);
                } else {
                    username = oauthToken.getPrincipal().getAttribute("login");
                    System.out.println("GITHUB login with username: " + username);
                }
            } else {
                // Fallback for other OAuth2 providers
                username = oauthToken.getPrincipal().getAttribute("sub");
                System.out.println("Other provider login with sub: " + username);
            }
        } else {
            // Form-based login
            username = auth.getName();
        }

//        int offset = 0;
//        int batchSize = 100; // Define the size of each batch
//        List<User> userBatch;
//        userBatch = null;
//        do
//        {
//        	 userBatch = userdao.findByUserName(username, batchSize, offset);
//        	   // If the batch is not empty, process the users
//             if (!userBatch.isEmpty()) {
//                 for (User user : userBatch) {
////                	  Check if the user matches the username and process it
//                     if (user.getEmail().equals(username)) {
////                          Send login notification email
//                         sendLoginNotificationEmail(username, ipAddress, osName, osVersion, osArchitecture);
//
//                         // Determine target URL based on role
//                         return getTargetUrlBasedOnRole(user.getRole());
//                     }
//
//                 }
//             }
//             // Update the offset for the next batch
//             offset += batchSize; 
//        }
//        while (!userBatch.isEmpty()); // Exit when no more users are returned
//            return null; // Stop further processing
        
        int offset = 0;
        int batchSize = 100; // Define the size of each batch
        List<User> userBatch;

        do {
            // Fetch a batch of users
            userBatch = userdao.findByUserName(username, batchSize, offset);
            
            // If the batch is not empty, process the users
            if (userBatch != null && !userBatch.isEmpty()) {
                for (User user : userBatch) {
                    // Check if the user matches the username and process it
                    if (user.getEmail().equals(username)) {
                        // Send login notification email
                        sendLoginNotificationEmail(username, ipAddress, osName, osVersion, osArchitecture);

                        // Determine target URL based on role
                        return getTargetUrlBasedOnRole(user.getRole());
                    }
                }
                // Update the offset for the next batch
                offset += batchSize; 
            } else {
                // If the batch is empty, we will exit the loop
                break;
            }
        } while (true); // Keep looping until we break

        // If we reach here, it means the user was not found
        try {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not registered.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Stop further processing

    }

    private void sendLoginNotificationEmail(String username, String ipAddress, String osName, String osVersion, String osArchitecture) {
        // Prepare email subject
        String subject = "Security Update: Successful Login Detected";

        // Prepare email content
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

//         Send email asynchronously
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
            throw new IllegalStateException("Unexpected role: " + role);
        }
    }
}
