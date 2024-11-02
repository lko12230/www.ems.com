package com.ayush.ems.config;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ayush.ems.dao.UserDao;
import com.ayush.ems.entities.User;
import com.ayush.ems.service.EmailService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private OAuth2AuthorizedClientService authorizedClientService;

	private static final Logger LOGGER = Logger.getLogger(LoginSuccessHandler.class.getName());

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		try {
			String username;

			if (authentication.getPrincipal() instanceof OAuth2User) {
				OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
				username = oAuth2User.getAttribute("name");
				String email = oAuth2User.getAttribute("email");

				// Retrieve the OAuth2AuthenticationToken
				OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

				// Get the authorized client
				OAuth2AuthorizedClient authorizedClient = authorizedClientService
						.loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());

				// Attempt to get access token
				String accessToken = authorizedClient.getAccessToken().getTokenValue();

				// Check if email is null, and fetch from GitHub API if needed
				if (email == null) {
					email = fetchPrimaryEmailFromGithub(accessToken);
				}

				System.out.println("EMAIL SIGNIN WITH " + email);
				handleOAuth2Login(username, email, request, response);
			} else {
				username = authentication.getName();
				handleNormalLogin(username, request, response);
			}
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Error during authentication success handling", ex);
			response.sendRedirect("/error?message=" + ex.getMessage());
		}
	}

	private String fetchPrimaryEmailFromGithub(String accessToken) {
		RestTemplate restTemplate = new RestTemplate();
		String url = "https://api.github.com/user/emails";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

		JSONArray emails = new JSONArray(response.getBody());
		for (int i = 0; i < emails.length(); i++) {
			JSONObject emailObject = emails.getJSONObject(i);
			if (emailObject.getBoolean("primary") && emailObject.getBoolean("verified")) {
				return emailObject.getString("email");
			}
		}
		return null;
	}

	private void handleNormalLogin(String username, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String ipAddress = request.getRemoteAddr();
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String osArchitecture = System.getProperty("os.arch");

		List<User> users = userDao.findByUserName(username, 100, 0);

		// Check if user exists
		if (users != null && !users.isEmpty()) {
			User user = users.get(0);
			sendLoginNotificationEmail(user.getUsername(), ipAddress, osName, osVersion, osArchitecture,
					user.getEmail());

			// Redirect based on user role
			String redirectUrl = getTargetUrlBasedOnRole(user.getRole());
			response.sendRedirect(redirectUrl);
		} else {
			response.sendRedirect("/signin?logout");
		}
	}

	private void handleOAuth2Login(String username, String email, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// Check if the user exists in the database based on email
		List<User> users = userDao.findByUserName(email, 100, 0);

		if (users != null && !users.isEmpty()) {
			User user = users.get(0);
			String ipAddress = request.getRemoteAddr();
			String osName = System.getProperty("os.name");
			String osVersion = System.getProperty("os.version");
			String osArchitecture = System.getProperty("os.arch");

			sendLoginNotificationEmail(user.getUsername(), ipAddress, osName, osVersion, osArchitecture,
					user.getEmail());

			// Redirect based on user role
			String redirectUrl = getTargetUrlBasedOnRole(user.getRole());
			response.sendRedirect(redirectUrl);
		} else {
			response.sendRedirect("/signin?logout");
		}
	}

	private void sendLoginNotificationEmail(String username, String ipAddress, String osName, String osVersion,
			String osArchitecture, String email) {
		String subject = "Security Update: Successful Login Detected";
		String loginTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String emailContent = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>" + "    <meta charset='UTF-8'>"
				+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" + "    <style>"
				+ "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }"
				+ "        .email-container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); overflow: hidden; text-align: center; }"
				+ "        .header { background-color: #007BFF; padding: 20px; color: #ffffff; }"
				+ "        .header h1 { margin: 0; font-size: 22px; }"
				+ "        .content { padding: 30px; color: #333333; line-height: 1.6; font-size: 16px; text-align: left; }"
				+ "        .content p { margin-bottom: 20px; }"
				+ "        .content .highlight { font-weight: bold; color: #007BFF; }"
				+ "        .footer { padding: 20px; background-color: #f1f1f1; font-size: 12px; color: #888888; }"
				+ "        .info-box { background-color: #f9f9f9; padding: 15px; border-radius: 8px; border: 1px solid #e2e2e2; margin-bottom: 20px; }"
				+ "        .action-list { list-style-type: none; padding: 0; }"
				+ "        .action-list li { margin-bottom: 10px; }"
				+ "        .colored-logo { font-size: 14px; margin-top: 10px; }" + "    </style>" + "</head>" + "<body>"
				+ "    <div class='email-container'>" + "        <div class='header'>"
				+ "            <h1>Login Attempt Notification</h1>" + "        </div>" + "        <div class='content'>"
				+ "            <p>Dear " + username + ",</p>"
				+ "            <p>Your account has been successfully logged into. Below are the details:</p>"
				+ "            <div class='info-box'>"
				+ "                <p><strong>Username:</strong> <span class='highlight'>" + username + "</span></p>"
				+ "                <p><strong>Email:</strong> <span class='highlight'>" + email + "</span></p>"
				+ "                <p><strong>IP Address:</strong> <span class='highlight'>" + ipAddress + "</span></p>"
				+ "                <p><strong>Device Login Time:</strong> <span class='highlight'>" + new Date()
				+ "</span></p>" + "                <p><strong>Device OS:</strong> <span class='highlight'>" + osName
				+ "</span></p>" + "                <p><strong>Device Version:</strong> <span class='highlight'>"
				+ osVersion + "</span></p>"
				+ "                <p><strong>Device Architecture:</strong> <span class='highlight'>" + osArchitecture
				+ "</span></p>" + "            </div>"
				+ "            <p>If this was not you, we recommend taking the following actions:</p>"
				+ "            <ul class='action-list'>" + "                <li>Change your password.</li>"
				+ "                <li>Enable two-factor authentication if not already done.</li>"
				+ "                <li>Review recent account activity.</li>" + "            </ul>"
				+ "            <p>If you need assistance, please contact our support team at <a href='mailto:support@example.com'>support@example.com</a>.</p>"
				+ "            <p>Thank you for your attention to this matter.</p>" + "            <p>Best regards,</p>"
				+ "            <p><strong>Cyber Security Team</strong></p>" + "        </div>"
				+ "        <div class='footer'>"
				+ "            <p>Need help? <a href='#'>Visit our Help Center</a> or contact us at [Support Contact Info]</p>"
				+ "            <p class='colored-logo'>"
				+ "                <span class='colored-char' style='color: rgb(66, 133, 244);'>w</span><span class='colored-char' style='color: rgb(255, 0, 0);'>w</span><span class='colored-char' style='color: rgb(255, 165, 0);'>w</span>"
				+ "                <span class='colored-char' style='color: rgb(0, 0, 255);'>.</span><span class='colored-char' style='color: rgb(60, 179, 113);'>e</span><span class='colored-char' style='color: rgb(255, 0, 0);'>m</span><span class='colored-char' style='color: rgb(0, 0, 255);'>s</span><span class='colored-char' style='color: rgb(255, 0, 0);'>.</span><span class='colored-char' style='color: rgb(255, 165, 0);'>c</span><span class='colored-char' style='color: rgb(0, 0, 255);'>o</span><span class='colored-char' style='color: rgb(255, 0, 0);'>m</span>"
				+ "            </p>" + "        </div>" + "    </div>" + "</body>" + "</html>";

		CompletableFuture<Boolean> resultFuture = emailService.sendEmail(emailContent, subject, email);
		resultFuture.thenAccept(result -> {
			if (result) {
				LOGGER.info("Email sent successfully to " + username);
			} else {
				LOGGER.warning("Failed to send email to " + username);
			}
		}).exceptionally(ex -> {
			LOGGER.log(Level.SEVERE, "Error sending email to " + username, ex);
			return null;
		});
	}

	private String getTargetUrlBasedOnRole(String role) {
		switch (role) {
		case "ROLE_USER":
			return "/user/new";
		case "ROLE_ADMIN":
			return "/admin/new";
		case "ROLE_HR":
			return "/hr/new";
		case "ROLE_IT":
			return "/IT/new";
		case "ROLE_MANAGER":
			return "/manager/new";
		default:
			throw new IllegalStateException("Unexpected role: " + role);
		}
	}

	// Optional: You may implement this if you need to check the authorities.
	private Collection<? extends GrantedAuthority> getAuthorities(User user) {
		List<GrantedAuthority> authorities = new ArrayList<>();

		// Example: Add roles based on your application's logic
		if (user.getRole() != null) {
			authorities.add(() -> user.getRole());
		}

		return authorities;
	}
}
