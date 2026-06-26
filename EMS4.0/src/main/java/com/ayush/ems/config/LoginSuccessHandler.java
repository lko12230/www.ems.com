package com.ayush.ems.config;

import com.ayush.ems.EMSMAIN;
import com.ayush.ems.EMSMAIN.OTPInfo;
import com.ayush.ems.dao.OrderDao;
import com.ayush.ems.entities.Payment_Order_Info;
import com.ayush.ems.entities.User;
import com.ayush.ems.service.EmailService;
import com.ayush.ems.service.Servicelayer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private Servicelayer servicelayer;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private OrderDao orderDao;

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

                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                String registrationId = oauthToken.getAuthorizedClientRegistrationId();

                OAuth2AuthorizedClient authorizedClient = authorizedClientService
                        .loadAuthorizedClient(registrationId, oauthToken.getName());

                String accessToken = authorizedClient.getAccessToken().getTokenValue();

                if (email == null && registrationId.equalsIgnoreCase("github")) {
                    email = fetchPrimaryEmailFromGithub(accessToken);
                }

                LOGGER.info("OAuth2 login success for: " + email + " via " + registrationId);
                handleOAuth2Login(username, email, request, response);
            } else {
                username = authentication.getName();
                handleNormalLogin(username, request, response);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error during authentication success handling", ex);
            routeToError(response, ex.getMessage());
        }
    }

    private void handleNormalLogin(String username, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Page<User> users = servicelayer.fetchUsersByEmail(username, 0, 1);
        if (users != null && !users.getContent().isEmpty()) {
            User user = users.getContent().get(0);
            Optional<Payment_Order_Info> order = orderDao.findbycompany(user.getCompany_id());

            if (!order.isEmpty() || (order.isEmpty() && user.getRole().equals("ROLE_ADMIN"))) {
                sendLoginNotificationEmail(user, request);
                handleTwoFactorLogic(user, request.getSession(), response);
            } else {
                response.sendRedirect("/signin?notactivated=true");
            }
        } else {
            response.sendRedirect("/signin?logout");
        }
    }

    private void handleOAuth2Login(String username, String email, HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        Page<User> users = servicelayer.fetchUsersByEmail(email, 0, 1);
        if (users != null && !users.getContent().isEmpty()) {
            User user = users.getContent().get(0);
            Optional<Payment_Order_Info> order = orderDao.findbycompany(user.getCompany_id());

            if (!order.isEmpty() || (order.isEmpty() && user.getRole().equals("ROLE_ADMIN"))) {
                sendLoginNotificationEmail(user, request);
                handleTwoFactorLogic(user, request.getSession(), response);
            } else {
                response.sendRedirect("/signin?notactivated=true");
            }
        } else {
            response.sendRedirect("/signin?logout");
        }
    }

    private void handleTwoFactorLogic(User user, HttpSession session, HttpServletResponse response) throws IOException {
        String redirectUrl = getTargetUrlBasedOnRole(user.getRole());
        session.setAttribute("2fa_enabled", user.isTwoStepEnabled());

        if (user.isTwoStepEnabled()) {
            sendAndStoreOtp(user, session);
            session.setAttribute("post2fa_redirect_url", redirectUrl);
            response.sendRedirect("/verify-otp2fa");
        } else {
            session.setAttribute("2fa_verified", true); // ✅ Mark 2FA as verified
            response.sendRedirect(redirectUrl);
        }
    }

    private void sendLoginNotificationEmail(User user, HttpServletRequest request) {
        try {
            String ipAddress = request.getRemoteAddr();
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String osArch = System.getProperty("os.arch");

            String subject = "🔐 EMS Security Alert: Login Detected";

            String content = String.format(
                "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
                "body{font-family:'Segoe UI',sans-serif;background-color:#f9f9f9;padding:0;margin:0;}" +
                ".container{max-width:600px;margin:40px auto;background:#fff;padding:30px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.1);}" +
                ".header{color:#2C3E50;font-size:22px;font-weight:600;margin-bottom:20px;}" +
                ".content{font-size:15px;color:#333;line-height:1.6;}" +
                ".footer{font-size:12px;color:#999;text-align:center;margin-top:30px;}" +
                "</style></head><body><div class='container'>" +
                "<div class='header'>🔐 EMS Login Notification</div>" +
                "<div class='content'><p>Hello <b>%s</b>,</p>" +
                "<p>A login to your EMS account was made with the following details:</p>" +
                "<p><b>Email:</b> %s<br><b>Role:</b> %s<br><b>IP Address:</b> %s<br>" +
                "<b>Operating System:</b> %s %s (%s)</p>" +
                "<p>If this was not you, please secure your account immediately.</p>" +
                "<p>Regards,<br><b>EMS Security Team</b></p></div>" +
                "<div class='footer'>© %d EMS. All rights reserved.</div></div></body></html>",
                user.getUsername(), user.getEmail(), user.getRole(), ipAddress, osName, osVersion, osArch,
                Calendar.getInstance().get(Calendar.YEAR)
            );

            emailService.sendEmail(content, subject, user.getEmail());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send login alert email", e);
        }
    }

    private void sendAndStoreOtp(User user, HttpSession session) {
        int otp = ThreadLocalRandom.current().nextInt(100000, 1000000);
        String otpString = String.valueOf(otp);

        session.setAttribute("2fa_user", user.getEmail());
        session.setAttribute("2fa_otp", otpString);
        EMSMAIN.otpValidateMap.put(user.getEmail(), new OTPInfo(otpString, new Date()));
        session.setAttribute("otp_success_message", "✅ OTP sent successfully to " + user.getEmail());

        System.out.println("🔐 OTP generated for " + user.getEmail() + ": " + otpString);

        String subject = "🔐 Your EMS One-Time Password (OTP) for Secure Login";
        String content = String.format(
            "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
            "body{font-family:'Segoe UI',sans-serif;background-color:#f9f9f9;margin:0;padding:0;}" +
            ".container{max-width:600px;margin:40px auto;background:#fff;padding:30px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.1);}" +
            ".header{font-size:20px;font-weight:600;color:#2C3E50;margin-bottom:20px;}" +
            ".otp-box{font-size:28px;color:#2E86C1;font-weight:bold;background:#f1f8ff;padding:15px;border-radius:6px;text-align:center;margin:20px 0;}" +
            ".content{font-size:15px;color:#333;line-height:1.6;}" +
            ".footer{font-size:12px;color:#888;text-align:center;margin-top:30px;}" +
            "</style></head><body><div class='container'>" +
            "<div class='header'>🔐 EMS OTP Verification</div>" +
            "<div class='content'><p>Dear <b>%s</b>,</p>" +
            "<p>Your OTP is:</p><div class='otp-box'>%s</div>" +
            "<p>This OTP is valid for 5 minutes. Do not share it with anyone.</p>" +
            "<p>Regards,<br><b>EMS Security Team</b></p></div>" +
            "<div class='footer'>© %d EMS. All rights reserved.</div></div></body></html>",
            user.getUsername(), otpString, Calendar.getInstance().get(Calendar.YEAR)
        );

        emailService.sendEmail(content, subject, user.getEmail());
    }

    private String fetchPrimaryEmailFromGithub(String accessToken) {
        try {
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
        } catch (Exception e) {
            LOGGER.warning("Failed to fetch GitHub primary email: " + e.getMessage());
        }
        return null;
    }

    private String getTargetUrlBasedOnRole(String role) {
        switch (role) {
            case "ROLE_USER": return "/user/new";
            case "ROLE_ADMIN": return "/admin/new";
            case "ROLE_HR": return "/hr/new";
            case "ROLE_IT": return "/IT/new";
            case "ROLE_MANAGER": return "/manager/new";
            case "ROLE_SUPER_ADMIN": return "/super_admin/new";
            case "ROLE_SUPPORT": return "/support/new";
            default: throw new IllegalStateException("Unexpected role: " + role);
        }
    }

    private void routeToError(HttpServletResponse response, String message) throws IOException {
        response.sendRedirect("/error?message=" + message);
    }

    @SuppressWarnings("unused")
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities.add(() -> user.getRole());
        }
        return authorities;
    }
}
