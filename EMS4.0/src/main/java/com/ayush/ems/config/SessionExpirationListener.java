package com.ayush.ems.config;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.ayush.ems.dao.UserDetailDao;

@Component
public class SessionExpirationListener implements ApplicationListener<SessionDestroyedEvent> {

    @Autowired
    private UserDetailDao userDetailDao;

    @Override
    @Transactional
    public void onApplicationEvent(SessionDestroyedEvent event) {
        event.getSecurityContexts().forEach(securityContext -> {
            if (securityContext.getAuthentication() != null) {
                UserDetails user = (UserDetails) securityContext.getAuthentication().getPrincipal();
                String username = user.getUsername();
                System.out.println("SESSION EXPIRED "+username);
                userDetailDao.update_user_status(username);
                System.out.println("Session expired for user: " + username);
            }
        });
    }
}
