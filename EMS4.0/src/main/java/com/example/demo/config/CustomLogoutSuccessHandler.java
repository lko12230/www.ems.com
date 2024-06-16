package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.dao.UserDetailDao;
import com.example.demo.dao.UserLoginDao;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import java.io.IOException;
import java.util.Date;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

	@Autowired
	private UserLoginDao userLoginDao;
	@Autowired
	private UserDetailDao userDetailDao;

	@Transactional
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		try {
			String username = authentication.getName();
			System.out.println("AUTH USER " + username);
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie c : cookies) {
					if ("JSSIONID".equals(c.getName())) { // Assuming the cookie storing the session ID is named
															// "sessionId"
						String sessionId = c.getValue(); // Retrieve the value of the cookie (session ID)
						System.out.println("OLD SESSION " + sessionId);
						System.out.println(username + " logout time -> " + new Date());
						userLoginDao.getUpdateUserLogoutTimeUserByUserName(username);
//						Thread.sleep(1000);
						userDetailDao.update_user_status(username);
						response.sendRedirect("/signin?logout"); // Redirect to login page after logout
						return;
					} else {
						System.out.println("NEW SESSION");
					}
				}
			} else {

				System.out.println("COOKIE IS NULL");
				response.sendRedirect("/signin?logout"); // Redirect to login page after logout
			}
		} catch (Exception e) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie c : cookies) {
					System.out.println("SESSION EXPIRE BEFORE IF " + c.getValue());
					if (("JSSIONID") != (c.getName()) && c.getValue().endsWith(".com")) { // Assuming the cookie storing the session ID is named
															// "sessionId"
						String sessionId = c.getValue(); // Retrieve the value of the cookie (session ID)
						// Use sessionId as needed
						System.out.println("EXPIRED USER NAME " + sessionId);
						try {
							userLoginDao.getUserSessionInterrupted(sessionId);
//							Thread.sleep(1000);
							userLoginDao.getUserStatusSessionExpired(sessionId);
//							Thread.sleep(1000);
							userDetailDao.update_user_status(sessionId);
							System.out.println("EMAIL REMOVE "+sessionId);
							response.sendRedirect("/signin?expiredsession=true");
						} catch (Exception e1) {
							userLoginDao.updateuserstatus();
							userDetailDao.update_user_status(sessionId);
						}

						return;

					}
				}
			}
		}
	}
}
