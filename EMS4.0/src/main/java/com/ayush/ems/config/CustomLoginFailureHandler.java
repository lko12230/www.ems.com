package com.ayush.ems.config;

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

import com.ayush.ems.dao.DowntimeMaintaince_Dao;
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
//							String subject = "Account Locked";
//							String content = "Dear " + email
//									+ ",\n\nYour account has been locked due to multiple failed login attempts. "
//									+ "Please contact the administrator if this was not you.\n\nDetails:\nIP Address: "
//									+ "\nOperating System: " + ")\n\nThank you!";

//							emailService.sendEmail(content, subject, email);
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
								"Account Is Locked Due To 3 Failed Login Attempt For 24 HOURS , And Your Account Will Be Unlock At  :: ("
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
