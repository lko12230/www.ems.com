package com.ayush.ems.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.entities.User;
import com.ayush.ems.service.EmailService;
import com.ayush.ems.service.UserServices;

@Component
@Transactional
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserDao userdao;
    @Autowired
    private UserServices userServices;
    @Autowired
    private EmailService emailService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("username");
        User user = userdao.getUserByUserName(email);
//        boolean get_status = downtime_Maintaince_Dao.server_status_check_active_or_not("Server Maintenance");

        if (user != null) {
//            if (get_status) {
                if (user.isEnabled()) {
                    if (user.isAccountNonLocked()) {
                        if (user.getFailedAttempt() < UserServices.MAX_FAILED_ATTEMPTS - 1) {
                            userServices.increaseFailedAttempt(user);
                            exception = new BadCredentialsException("Bad Credentials " +
                                    (3 - (user.getFailedAttempt() + 1)) + " Attempt Left");

                            String ipAddress = request.getRemoteAddr();
                            email = user.getEmail();
                            String osName = System.getProperty("os.name");
                            String osVersion = System.getProperty("os.version");
                            String osArchitecture = System.getProperty("os.arch");

                            String subject = "🔐 Security Notice: Unsuccessful Login Attempt - EMS";

                            String emailContent = String.format(
                                    "<!DOCTYPE html>" +
                                            "<html><head><meta charset='UTF-8'>" +
                                            "<style>" +
                                            "body { font-family: 'Segoe UI', sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }" +
                                            ".card { max-width: 600px; margin: auto; background: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                                            ".header { font-size: 20px; color: #d9534f; font-weight: bold; margin-bottom: 20px; }" +
                                            ".info { font-size: 15px; color: #333; line-height: 1.6; }" +
                                            ".info strong { color: #555; }" +
                                            ".footer { font-size: 12px; color: #888; text-align: center; margin-top: 30px; }" +
                                            "</style>" +
                                            "</head><body>" +
                                            "<div class='card'>" +
                                            "<div class='header'>Failed Login Attempt Detected</div>" +
                                            "<div class='info'>" +
                                            "<p>Dear %s,</p>" +
                                            "<p>We detected an unsuccessful login attempt on your EMS account. Please review the details below:</p>" +
                                            "<p><strong>Email:</strong> %s<br>" +
                                            "<strong>IP Address:</strong> %s<br>" +
                                            "<strong>Operating System:</strong> %s %s (%s)<br>" +
                                            "<strong>Attempt Time:</strong> %s</p>" +
                                            "<p>If this attempt was made by you and you entered the wrong password, no action is required. If not, we recommend the following:</p>" +
                                            "<ul>" +
                                            "<li>Change your EMS password immediately</li>" +
                                            "<li>Enable two-factor authentication (2FA)</li>" +
                                            "<li>Contact our support team if you notice any unusual activity</li>" +
                                            "</ul>" +
                                            "<p style='text-align:center; margin-top: 20px;'>" +
                                            "<a href='https://wwwemscom-production.up.railway.app/2fa' style='display:inline-block;background-color:#2E86C1;color:white;padding:10px 20px;text-decoration:none;border-radius:6px;font-weight:bold;'>🔐 Enable 2-Step Verification</a>" +
                                            "</p>" +
                                            "<p>Thank you for staying vigilant.</p>" +
                                            "<p>Regards,<br><strong>EMS Security Team</strong></p>" +
                                            "</div>" +
                                            "<div class='footer'>&copy; %d EMS. All rights reserved.</div>" +
                                            "</div></body></html>",
                                    user.getUsername(), user.getEmail(), ipAddress,
                                    osName, osVersion, osArchitecture, new Date(), LocalDateTime.now().getYear()
                            );

                            try {
								emailService.sendEmail(emailContent, subject, email).get();
							} catch (InterruptedException | ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        } else {
                            String ipAddress = request.getRemoteAddr();
                            String username = user.getUsername();
                            email = user.getEmail();
                            String subject = "🔒 Account Locked Due to Multiple Failed Attempts - EMS";

                            String emailContent = String.format(
                                    "<!DOCTYPE html>" +
                                            "<html><head><meta charset='UTF-8'>" +
                                            "<style>" +
                                            "body { font-family: 'Segoe UI', sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }" +
                                            ".card { max-width: 600px; margin: auto; background: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                                            ".header { font-size: 20px; color: #d9534f; font-weight: bold; margin-bottom: 20px; }" +
                                            ".info { font-size: 15px; color: #333; line-height: 1.6; }" +
                                            ".info strong { color: #555; }" +
                                            ".footer { font-size: 12px; color: #888; text-align: center; margin-top: 30px; }" +
                                            "ul { padding-left: 18px; }" +
                                            "</style>" +
                                            "</head><body>" +
                                            "<div class='card'>" +
                                            "<div class='header'>Account Locked</div>" +
                                            "<div class='info'>" +
                                            "<p>Dear %s,</p>" +
                                            "<p>Your EMS account has been temporarily <strong style='color:red;'>locked</strong> due to multiple failed login attempts.</p>" +
                                            "<p><strong>Email:</strong> %s<br>" +
                                            "<strong>IP Address:</strong> %s<br>" +
                                            "<strong>Lock Time:</strong> %s</p>" +
                                            "<p>To restore access, please follow these steps:</p>" +
                                            "<ul>" +
                                            "<li>Wait for the lockout period to expire (if applicable)</li>" +
                                            "<li>Reset your password via the <a href='https://wwwemscom-production.up.railway.app/forgot/'>Forgot Password</a> link</li>" +
                                            "<li>Contact EMS Support if you need further assistance</li>" +
                                            "<li>Enable 2FA using the button below</li>" +
                                            "</ul>" +
                                            "<p style='text-align:center; margin-top: 20px;'>" +
                                            "<a href='https://wwwemscom-production.up.railway.app/2fa' style='display:inline-block;background-color:#2E86C1;color:white;padding:10px 20px;text-decoration:none;border-radius:6px;font-weight:bold;'>🔐 Enable 2-Step Verification</a>" +
                                            "</p>" +
                                            "<p>Regards,<br><strong>EMS Security Team</strong></p>" +
                                            "</div>" +
                                            "<div class='footer'>&copy; %d EMS. All rights reserved.</div>" +
                                            "</div></body></html>",
                                    username, email, ipAddress, new Date(), LocalDateTime.now().getYear()
                            );

                            userServices.lock(user);
                            exception = new BadCredentialsException("Your account is locked.");
                            try {
								emailService.sendEmail(emailContent, subject, email).get();
							} catch (InterruptedException | ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        }
                    } else {
                        exception = new LockedException("Account is locked until: " + user.getExpirelockDateAndTime());
                    }
                } else {
                    exception = new LockedException("Your account is disabled. Please contact the administrator.");
                }
//            } else {
//                exception = new LockedException("Server under maintenance between 11:00 PM and 11:05 PM.");
//            }
        } else {
            exception = new LockedException("User does not exist. Please contact the administrator.");
        }

        super.setDefaultFailureUrl("/signin?error");
        super.onAuthenticationFailure(request, response, exception);
    }
}
