package com.example.demo.config;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

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

import com.example.demo.dao.DowntimeMaintaince_Dao;
import com.example.demo.dao.UserDao;
import com.example.demo.entities.User;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserServices;

@Component
@Transactional
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private UserDao userdao;
	@Autowired
	private UserServices userServices;
	@Autowired
	private DowntimeMaintaince_Dao downtime_Maintaince_Dao;
//	@Autowired
//	private UserDetailDao userDetailDao;
	@Autowired
	private EmailService emailService; // Inject EmailService

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
//		InetAddress localHost = InetAddress.getLocalHost();
//		String str1 = localHost.toString();
		String email = request.getParameter("username");
//		String osName = System.getProperty("os.name");
//		String osVersion = System.getProperty("os.version");
//		String osArchitecture = System.getProperty("os.arch");
		User user = userdao.getUserByUserName(email);
		boolean get_status = downtime_Maintaince_Dao.server_status_check_active_or_not("downtime_maintaince");
//		boolean getresponse=servicelayer.active_user_email(user);
//		System.out.println("USER ACTIVE OR NOT "+getresponse);
		if (user != null) {
//			Optional<UserDetail> userDetail = userDetailDao.findById(user.getId());
//			UserDetail userDetail2 = userDetail.get();
			if (get_status) {
//				if (!userDetail2.isUser_status()) {
				if (user.isEnabled()) {
					if (user.isAccountNonLocked()) {
						if (user.getFailedAttempt() < UserServices.MAX_FAILED_ATTEMPTS - 1) {
							userServices.increaseFailedAttempt(user);
							exception = new BadCredentialsException(
									"Bad Credentails " + (3 - (user.getFailedAttempt() + 1)) + " Attempt Left");

							// Get the username and IP address
							String ipAddress = request.getRemoteAddr();
							String username = user.getUsername();
							email = user.getEmail();
							String osName = System.getProperty("os.name");
							String osVersion = System.getProperty("os.version");
							String osArchitecture = System.getProperty("os.arch");

							// Send email for every failed login attempt
							String subject = "Security Alert: Failed Login Attempt on Your Account";

							String emailContent = "" +
								    "<!DOCTYPE html>" +
								    "<html lang='en'>" +
								    "<head>" +
								    "    <meta charset='UTF-8'>" +
								    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
								    "    <style>" +
								    "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }" +
								    "        .email-container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); overflow: hidden; text-align: center; }" +
								    "        .header { background-color: #FF4C4C; padding: 20px; color: #ffffff; border-top-left-radius: 8px; border-top-right-radius: 8px; }" +
								    "        .header h1 { margin: 0; font-size: 24px; }" +
								    "        .content { padding: 30px; color: #333333; line-height: 1.6; font-size: 16px; text-align: left; }" +
								    "        .content p { margin-bottom: 20px; }" +
								    "        .content .highlight { font-weight: bold; color: #FF4C4C; }" +
								    "        .footer { padding: 20px; background-color: #f1f1f1; font-size: 12px; color: #888888; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; }" +
								    "        .info-box { background-color: #f9f9f9; padding: 15px; border-radius: 8px; border: 1px solid #e2e2e2; margin-bottom: 20px; }" +
								    "        .action-list { list-style-type: none; padding: 0; }" +
								    "        .action-list li { margin-bottom: 10px; }" +
								    "    </style>" +
								    "</head>" +
								    "<body>" +
								    "    <div class='email-container'>" +
								    "        <div class='header'>" +
								    "            <h1>Failed Login Attempt</h1>" +
								    "        </div>" +
								    "        <div class='content'>" +
								    "            <p>Dear <b>" + username + "</b>,</p>" +
								    "            <p>We detected a failed login attempt on your account. Below are the details:</p>" +
								    "            <div class='info-box'>" +
								    "                <p><strong>Username:</strong> <span class='highlight'>" + username + "</span></p>" +
								    "                <p><strong>Email:</strong> <span class='highlight'>" + email + "</span></p>" +
								    "                <p><strong>IP Address:</strong> <span class='highlight'>" + ipAddress + "</span></p>" +
								    "                <p><strong>Device Login Time:</strong> <span class='highlight'>" + new Date() + "</span></p>" +
								    "                <p><strong>Device OS:</strong> <span class='highlight'>" + osName + "</span></p>" +
								    "                <p><strong>Device Version:</strong> <span class='highlight'>" + osVersion + "</span></p>" +
								    "                <p><strong>Device Architecture:</strong> <span class='highlight'>" + osArchitecture + "</span></p>" +
								    "            </div>" +
								    "            <p>If this was you and you simply entered the wrong password, no further action is required. However, " +
								    "if this wasn't you, we recommend taking the following actions to secure your account:</p>" +
								    "            <ul class='action-list'>" +
								    "                <li>Change your password immediately.</li>" +
								    "                <li>Enable two-factor authentication (if not already enabled).</li>" +
								    "                <li>Review your recent account activity.</li>" +
								    "            </ul>" +
								    "            <p>If you need assistance, please reach out to our support team at <a href='mailto:support@example.com'>support@example.com</a>.</p>" +
								    "            <p>Your security is our top priority.</p>" +
								    "            <p>Best regards,</p>" +
								    "            <p><strong>Cyber Security Team</strong></p>" +
								    "        </div>" +
								    "        <div class='footer'>" +
								    "            <p>If you did not attempt to log in, please disregard this message.</p>" +
								    "            <p>Need help? <a href='#' style='color: #007BFF; text-decoration: none;'>Visit our Help Center</a> or contact us at <a href='mailto:support@example.com' style='color: #007BFF; text-decoration: none;'>support@example.com</a>.</p>" +
								    "            <p style='margin: 0;'> &copy; " + new Date().getYear() + 1900 + " Your Company. All Rights Reserved.</p>" +
								    "        </div>" +
								    "    </div>" +
								    "</body>" +
								    "</html>";


							CompletableFuture<Boolean> flagFuture = emailService.sendEmail(emailContent, subject,
									email);
							// This will block until the result is available
							try {
								Boolean flag = flagFuture.get(); // Blocking call to get the result
								if (flag) {
									System.out.println(true);
								} else {
									System.out.println(false);
								}
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println(false);
							}

//						EMSMAIN.failed_lofin_Attempt.add(email);
//								EMSMAIN.failed_login_Attempt.computeIfAbsent(email,k ->  new ArrayList<>()).add(str1);
//								EMSMAIN.failed_os_name.computeIfAbsent(email, k -> new ArrayList<>()).add(osName);
//								EMSMAIN.failed_device_version.computeIfAbsent(email, k -> new ArrayList<>()).add(osVersion);
//								EMSMAIN.failed_device_Architecture.computeIfAbsent(email, k -> new ArrayList<>()).add(osArchitecture);
//								EMSMAIN.failed_login_date_time.computeIfAbsent(email, k -> new ArrayList<>()).add(new Date());
						} else {
							userServices.lock(user);
							exception = new BadCredentialsException(
									"Bad Credentails " + (3 - (user.getFailedAttempt() + 1)) + " Attempt Left");
							// Send email when the account is locked
							String subject = "Account Locked";
							String content = "Dear " + email
									+ ",\n\nYour account has been locked due to multiple failed login attempts. "
									+ "Please contact the administrator if this was not you.\n\nDetails:\nIP Address: "
									+ "\nOperating System: " + ")\n\nThank you!";

							emailService.sendEmail(content, subject, email);
//						EMSMAIN.failed_lofin_Attempt.add(email);
//								EMSMAIN.failed_login_Attempt.computeIfAbsent(email,k ->  new ArrayList<>()).add(str1);
//								EMSMAIN.failed_os_name.computeIfAbsent(email, k -> new ArrayList<>()).add(osName);
//								EMSMAIN.failed_device_version.computeIfAbsent(email, k -> new ArrayList<>()).add(osVersion);
//								EMSMAIN.failed_device_Architecture.computeIfAbsent(email, k -> new ArrayList<>()).add(osArchitecture);
//								EMSMAIN.failed_login_date_time.computeIfAbsent(email, k -> new ArrayList<>()).add(new Date());
						}
					} else if (!user.isAccountNonLocked()) {
//					if(userServices.unlockAccountTimeExpired(user))
//					{
//					exception=new LockedException("Your Account Is UnLocked");
//					}
//					else
//					{
						exception = new LockedException(
								"Your Account Is Locked Due to 3 Failed Login Attempt For 24 HOURS , And Your Account Will be Unlock At  :: ("
										+ user.getExpirelockDateAndTime()
										+ ") UnLockDate Shown This Formatted (YYYY-MM-DD HH:MM:SS)");
//					}
					}
//				else
//				{
//					exception=new LockedException("Your Acoount Is Locked Due to 3 Failed Attempts , Please Try After Sometime");
//				}
				} else {
					exception = new LockedException(
							"Your Acoount Is Disabled Due to Some Reasons , Please Contact Administrator");
				}
//				} else {
//					exception = new LockedException("Your Have Already Loginned !!");
//				}
			} else {
				exception = new LockedException("Server Under Maintaince !! Between 11:00 PM PM To 11:05 PM ");
			}
		} else {
			exception = new LockedException("User Not Exist , Please Contact Administrator");

		}
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

}
