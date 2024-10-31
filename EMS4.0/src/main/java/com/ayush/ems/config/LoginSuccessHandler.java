package com.ayush.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                           Authentication authentication) throws IOException {
        String username = authentication.getName();
        String ipAddress = request.getRemoteAddr();
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArchitecture = System.getProperty("os.arch");

        List<User> users = userdao.findByUserName(username, 100, 0);
        if (users != null && !users.isEmpty()) {
            User user = users.get(0);

            // Send login notification email
            sendLoginNotificationEmail(user.getUsername(), ipAddress, osName, osVersion, osArchitecture, user.getEmail());

            // Redirect based on user role
            String redirectUrl = getTargetUrlBasedOnRole(user.getRole());
            response.sendRedirect(redirectUrl);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not registered.");
        }
    }

    private void sendLoginNotificationEmail(String username, String ipAddress, String osName, String osVersion, String osArchitecture, String email) {
        String subject = "Security Update: Successful Login Detected";
        String emailContent = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'><style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; } .email-container { max-width: 600px; " +
                "margin: 40px auto; background-color: #ffffff; border-radius: 8px; text-align: center; }</style></head>" +
                "<body><div class='email-container'><h1>Login Attempt Notification</h1><p>Dear " + username +
                ",</p><p>Your account was successfully logged into. Below are the details:</p>" +
                "<p>Username: " + username + "<br>Email: " + email + "<br>IP Address: " + ipAddress +
                "<br>Device Login Time: " + new Date() + "<br>Device OS: " + osName + "<br>OS Version: " + osVersion +
                "<br>Architecture: " + osArchitecture + "</p></body></html>";

        CompletableFuture<Boolean> resultFuture = emailService.sendEmail(emailContent, subject, email);
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
