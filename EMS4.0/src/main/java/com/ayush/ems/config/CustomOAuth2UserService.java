package com.ayush.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.ayush.ems.dao.UserDao;
import com.ayush.ems.entities.User;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.util.List;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserDao userDao;
    
    @Autowired
    private HttpServletResponse response; // Consider removing this and handling cookies elsewhere

    private DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws UsernameNotFoundException {
        // Load the user from the OAuth provider
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = null;

        // Fetch the email from GitHub's email API
        if ("github".equals(provider)) {
            String accessToken = userRequest.getAccessToken().getTokenValue();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<Map[]> responseEntity = restTemplate.exchange(
                    "https://api.github.com/user/emails", 
                    HttpMethod.GET, 
                    entity, 
                    Map[].class
                );

                Map[] emails = responseEntity.getBody();
                if (emails != null && emails.length > 0) {
                    for (Map emailInfo : emails) {
                        boolean verified = (boolean) emailInfo.get("verified");
                        boolean primary = (boolean) emailInfo.get("primary");
                        if (verified && primary) {
                            email = (String) emailInfo.get("email");
                            System.out.println("GIT Email: " + email); // Debugging line
                            break;
                        }
                    }
                } else {
                    throw new UsernameNotFoundException("No email found for GitHub user.");
                }
            } catch (Exception e) {
                throw new UsernameNotFoundException("Failed to retrieve email from GitHub: " + e.getMessage());
            }
        }
          else if ("google".equals(provider)) {
            email = oauth2User.getAttribute("email"); // Directly get the email from the user attributes
            System.out.println("Google Email: " + email); // Debugging line
            Cookie cookie = new Cookie("JSSSIONID", email);
            cookie.setPath("/"); // Set the cookie path
            cookie.setHttpOnly(true); // Prevent access via JavaScript
            cookie.setMaxAge(60 * 60); // Set cookie expiration (1 hour)
            response.addCookie(cookie);
          }
        // Before fetching from the database
        System.out.println("Looking for user with email: " + email);
        Optional<User> userOpt = userDao.findByEmail(email); // Fetch the user directly by email

        // Check if the user was found
        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("User does not exist, please contact administrator.");
        }

        // Return user with authorities from the database
        User user = userOpt.get();
        return new CustomOAuth2User(oauth2User, user);
    }
}
