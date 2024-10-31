package com.ayush.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ayush.ems.dao.UserDetailDao;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private UserDetailDao userDetailDao;

    @Override
    @Transactional
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) 
            throws IOException {
        if (authentication != null) {
            // Get the username from the authenticated user
            String username = authentication.getName();
            System.out.println(username + " logged out at " + new Date());

            // Update user status to offline (or 0) in the database
            userDetailDao.update_user_status(username);

            // Invalidate the session explicitly
            request.getSession().invalidate();

            // Clear the session ID cookie if it exists
            Cookie sessionCookie = new Cookie("JSESSIONID", null);
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(0);
            response.addCookie(sessionCookie);

            // Redirect to login page with a logout message
            response.sendRedirect("/signin?logout");
        } else {
            // Handle case where the user is already logged out or session expired
            response.sendRedirect("/signin?expiredsession=true");
        }
    }
}
