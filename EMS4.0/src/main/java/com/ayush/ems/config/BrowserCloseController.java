package com.ayush.ems.config;

import com.ayush.ems.dao.UserDao;
import com.ayush.ems.dao.UserDetailDao;
import com.ayush.ems.dao.UserLoginDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.transaction.Transactional;
import java.util.Map;

@RestController
public class BrowserCloseController {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserDetailDao userDetailDao;

    @Autowired
    private UserLoginDao userLoginDao;

    @PostMapping(value = "/browser-closed", consumes = "text/plain")
    @Transactional
    public String handleBrowserClose(@RequestBody String requestBody) {
        try {
            @SuppressWarnings("unchecked")
			Map<String, String> parsedRequest = new ObjectMapper().readValue(requestBody, Map.class);
            String email = parsedRequest.get("email");

            if (email != null) {
                userDao.update_user_status(email);
                userDetailDao.update_user_status(email);
                userLoginDao.updateSessionInterruptedStatusBrowserClosed(email);

                sessionRegistry.getAllPrincipals().forEach(principal -> {
                    if (principal instanceof org.springframework.security.core.userdetails.User) {
                        org.springframework.security.core.userdetails.User user =
                                (org.springframework.security.core.userdetails.User) principal;
                        if (user.getUsername().equals(email)) {
                            sessionRegistry.getAllSessions(principal, true).forEach(session -> session.expireNow());
                        }
                    }
                });
                System.out.println("Browser closed for user: " + email);
            }
            return "Browser closed for user: " + email;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to handle browser close.";
        }
    }
}
