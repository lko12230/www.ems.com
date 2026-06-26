package com.ayush.ems.config;

import com.ayush.ems.dao.UserDao;
import com.ayush.ems.dao.UserDetailDao;
import com.ayush.ems.dao.UserLoginDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private UserDetailDao userDetailDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserLoginDao userLoginDao;

    @Override
    @Transactional
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        String username = authentication != null ? authentication.getName() : null;

        if (username != null) {
            System.out.println(username + " logged out at " + new Date());
            userDetailDao.update_user_status(username);
            userDao.update_user_status(username);

            Optional<Integer> sessionIdOpt = userLoginDao.findLatestSessionRecordId(username);
            sessionIdOpt.ifPresent(sessionId -> {
                userLoginDao.markSessionAsLoggedOut(sessionId);
                System.out.println("Session manually logged out for user: " + username + ", session ID: " + sessionId);
            });
        }

        // Explicitly invalidate session only during logout
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }

        // Remove cookies
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        response.addCookie(sessionCookie);

        // Redirect after logout
        response.sendRedirect(authentication != null ? "/signin?logout" : "/signin?expiredsession=true");
    }
}
