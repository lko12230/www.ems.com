package com.ayush.ems.config;

import java.util.Optional;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

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

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    
    @Autowired
    private UserDao userdao;
    @Autowired
    private HttpServletResponse response;

    private DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws UsernameNotFoundException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        
        // Get the provider ID (Google or GitHub)
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = null;

        // Handle different providers
        if ("github".equals(provider)) {
            // Fetch email from GitHub's email API
            String accessToken = userRequest.getAccessToken().getTokenValue();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map[]> responseEntity = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                Map[].class
            );

            Map[] emails = responseEntity.getBody();
            // Check if any email is returned
            if (emails != null) {
                for (Map emailInfo : emails) {
                    boolean verified = (boolean) emailInfo.get("verified");
                    if (verified) {
                        email = (String) emailInfo.get("email");
                        break;
                    }
                }
            }
        } else if ("google".equals(provider)) {
            // Extract email directly from Google user info response
            email = oauth2User.getAttribute("email");
        }

        // Check if user exists in the database
        Optional<User> userOpt = userdao.findByUserName(email);
        
        if (!userOpt.isPresent()) {
            // User not found, throw an exception
            throw new UsernameNotFoundException("User does not exist, please contact administrator.");
        }

        User user = userOpt.get();

        // Set a cookie with the email or other identifier
        Cookie cookie = new Cookie("JSESSIONID", user.getEmail());
        System.out.println("GIT SIGNING IN WITH " + email);
        cookie.setMaxAge(60 * 60 * 24); // Cookie expiration time (1 day)
        response.addCookie(cookie);

        // Return the OAuth2User with custom authorities (roles) from the database
        return new CustomOAuth2User(oauth2User, user);
    }
}
