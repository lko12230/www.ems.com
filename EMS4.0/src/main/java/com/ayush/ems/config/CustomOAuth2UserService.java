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

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserDao userDao;

    private DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws UsernameNotFoundException {
        // Load user from the OAuth provider
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = extractEmailFromProvider(provider, userRequest, oauth2User);

        // Check if email exists in database
        Optional<User> userOpt = userDao.findByEmail(email);

        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("User does not exist, please contact administrator.");
        }
        
        User userGet = userOpt.get();
        System.out.println("OAUTH2USER "+userGet.isEnabled());
        if (!userGet.isEnabled() && userOpt.isPresent()) {
        	   System.out.println("OAUTH2USER EXCEPTION "+userGet.isEnabled());
            throw new UsernameNotFoundException("Your Acoount Is Disabled Due to Some Reasons , Please Contact Administrator.");
        }

        // Return user with authorities from the database
        User user = userOpt.get();
        return new CustomOAuth2User(oauth2User, user);
    }

    private String extractEmailFromProvider(String provider, OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String email = null;

        switch (provider) {
            case "github":
                email = fetchEmailFromGitHub(userRequest);
                break;
            case "google":
                email = oauth2User.getAttribute("email");
                break;
            case "twitter":
                email = fetchEmailFromTwitter(userRequest);
                break;
            case "facebook":
                email = oauth2User.getAttribute("email"); // Modify if needed based on Facebook API
                break;
            default:
                throw new UsernameNotFoundException("Provider not supported: " + provider);
        }

        if (email == null) {
            throw new UsernameNotFoundException("Failed to retrieve email from " + provider);
        }

        return email;
    }

    private String fetchEmailFromGitHub(OAuth2UserRequest userRequest) {
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
            if (emails != null) {
                for (Map emailInfo : emails) {
                    boolean verified = (boolean) emailInfo.get("verified");
                    boolean primary = (boolean) emailInfo.get("primary");
                    if (verified && primary) {
                        return (String) emailInfo.get("email");
                    }
                }
            }
        } catch (Exception e) {
            throw new UsernameNotFoundException("Failed to retrieve email from GitHub: " + e.getMessage());
        }

        throw new UsernameNotFoundException("No verified primary email found for GitHub user.");
    }

    private String fetchEmailFromTwitter(OAuth2UserRequest userRequest) {
        String accessToken = userRequest.getAccessToken().getTokenValue();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                "https://api.twitter.com/2/me?user.fields=email",
                HttpMethod.GET,
                entity,
                Map.class
            );

            Map responseBody = responseEntity.getBody();
            if (responseBody != null && responseBody.containsKey("email")) {
                return (String) responseBody.get("email");
            }
        } catch (Exception e) {
            throw new UsernameNotFoundException("Failed to retrieve email from Twitter: " + e.getMessage());
        }

        throw new UsernameNotFoundException("No email found for Twitter user.");
    }
}
