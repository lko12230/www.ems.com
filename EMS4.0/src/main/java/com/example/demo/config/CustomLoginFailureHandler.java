package com.example.demo.config;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

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

import com.example.demo.EMSMAIN;
import com.example.demo.dao.Downtime_Maintaince_Dao;
import com.example.demo.dao.Userdao;
import com.example.demo.entities.User;
import com.example.demo.service.UserServices;

@Component
@Transactional
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private Userdao userdao;
	@Autowired
	private UserServices userServices;
	@Autowired
	private Downtime_Maintaince_Dao downtime_Maintaince_Dao;
//	@Autowired
//	private UserDetailDao userDetailDao;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		InetAddress localHost = InetAddress.getLocalHost();
		String str1 = localHost.toString();
		String email = request.getParameter("username");
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String osArchitecture = System.getProperty("os.arch");
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
//						EMSMAIN.failed_lofin_Attempt.add(email);
								EMSMAIN.failed_login_Attempt.put(email, str1);
								EMSMAIN.device_os.put(email, osName);
								EMSMAIN.device_version.put(email, osVersion);
								EMSMAIN.device_Architecture.put(email, osArchitecture);
								EMSMAIN.login_date_time.put(email, new Date());
							} else {
								userServices.lock(user);
								exception = new BadCredentialsException(
										"Bad Credentails " + (3 - (user.getFailedAttempt() + 1)) + " Attempt Left");
//						EMSMAIN.failed_lofin_Attempt.add(email);
								EMSMAIN.failed_login_Attempt.put(email, str1);
								EMSMAIN.device_os.put(email, osName);
								EMSMAIN.device_version.put(email, osVersion);
								EMSMAIN.device_Architecture.put(email, osArchitecture);
								EMSMAIN.login_date_time.put(email, new Date());
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
