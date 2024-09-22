package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.dao.Userdao;
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
    private Userdao userdao;

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
			String subject = "Success Login Attempt";
			
            // Prepare email content
//            String emailContent = "Dear " + username + ", your IP address is " + ipAddress;
            System.out.println("Sending email to: " + auth.getName());
            
            
			String emailContent = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear " + auth.getName()
			+ "<br>" + "<br>" + "Login Success" + "<br>" + "<br>" + "Username : " + "<b>" + username + "</b>" + "<br>"
			+ "IP ADDRESS : " + "<b>" + ipAddress + "</b>" + "<br>" + "Device LOGIN TIME : " + "<b>"
			+ new Date() + "</b>" + "</b>" + "<br>" + "Device OS : " + "<b>" + osName + "</b>" + "<br>"
			+ "Device Version : " + "<b>" + osVersion + "</b>" + "<br>" + "Device Architecture : " + "<b>"
			+ osArchitecture + "</b>" + "<br>" + "<br>" + "Cyber Team " + "</p>" + "</div>";
            
          
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
