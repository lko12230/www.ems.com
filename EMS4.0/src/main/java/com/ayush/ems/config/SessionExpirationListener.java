package com.ayush.ems.config;

import com.ayush.ems.dao.UserLoginDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SessionExpirationListener implements ApplicationListener<HttpSessionDestroyedEvent> {

    @Autowired
    private UserLoginDao userLoginDao;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onApplicationEvent(HttpSessionDestroyedEvent event) {
        // Capture session attributes at the time of session destruction
        Boolean sessionManualLogout = (Boolean) event.getSession().getAttribute("sessionManualLogout");
        SecurityContext securityContext = (SecurityContext) event.getSession().getAttribute("SPRING_SECURITY_CONTEXT");

        // Schedule the session expiration check with a delay
        scheduler.schedule(() -> handleSessionExpiration(sessionManualLogout, securityContext), 500, TimeUnit.MILLISECONDS);
    }

    @Transactional
    private void handleSessionExpiration(Boolean sessionManualLogout, SecurityContext securityContext) {
        try {
            // Skip expiration handling if session was manually logged out
            if (Boolean.TRUE.equals(sessionManualLogout)) {
                System.out.println("Session manually logged out, skipping expiration handling.");
                return;
            }

            if (securityContext != null) {
                Authentication authentication = securityContext.getAuthentication();
                if (authentication != null && authentication.getName() != null) {
                    String username = authentication.getName();

                    Optional<Integer> sessionIdOpt = userLoginDao.findLatestSessionRecordId(username);
                    if (sessionIdOpt.isPresent()) {
                        Integer userSessionId = sessionIdOpt.get();

                        Integer isLoggedOut = userLoginDao.isSessionManuallyLoggedOut(userSessionId);
                        boolean manuallyLoggedOut = isLoggedOut != null && isLoggedOut == 1;
                        System.out.println("EXPIRED SESSION "+manuallyLoggedOut);
                        if (!manuallyLoggedOut) {
                            userLoginDao.updateSessionInterruptedStatus(userSessionId);
                            userLoginDao.setDefaultLogoutTime(userSessionId);
                            System.out.println("Session expired for user: " + username + ", session ID: " + userSessionId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in handleSessionExpiration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Shutdown the scheduler when the application stops
    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdown();
    }
}
