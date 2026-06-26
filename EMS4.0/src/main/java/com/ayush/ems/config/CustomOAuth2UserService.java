package com.ayush.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.ayush.ems.dao.NSqlConfigDao;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.entities.NSqlConfig;
import com.ayush.ems.entities.User;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserDao userDao;

    @Autowired
    private NSqlConfigDao nSqlConfigDao;

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = extractEmailFromProvider(provider, userRequest, oauth2User);

        // Check user in database
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("User does not exist, please contact administrator.");
        }

        User user = userOpt.get();

        // Fetch configuration for new user activation
        Optional<NSqlConfig> configOpt = nSqlConfigDao.findByConfigkey("NEWUSERACTIVEORNOT");
        String newUserActiveSetting = configOpt.map(NSqlConfig::getNSqlValue).orElse("0");

        // Validate account status
        validateUserStatus(user, newUserActiveSetting);

        // Mark new user as active if needed
        if (!user.isNewUserActiveOrInactive()) {
            user.setNewUserActiveOrInactive(true);
            userDao.save(user);
        }

        return new CustomOAuth2User(oauth2User, user);
    }

    /** Validate account conditions and throw appropriate exceptions */
    private void validateUserStatus(User user, String newUserActiveSetting) {
        if (!user.isEnabled()) {
            throw new DisabledException("Your account is disabled. Please contact the administrator.");
        }

        if (!user.isAccountNonLocked()) {
            throw new LockedException("Account is locked until: " + user.getExpirelockDateAndTime());
        }

        if (!user.isNewUserActiveOrInactive() && "1".equals(newUserActiveSetting)) {
            String username = (user.getUsername() != null) ? user.getUsername() : "User";
            throw new UsernameNotFoundException(
                "Dear " + username + 
                ", first-time users must log in with User ID and Password. " +
                "After initial login, you can use Google or GitHub."
            );
        }
    }

    /** Extract email based on provider */
    private String extractEmailFromProvider(String provider, OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        switch (provider) {
            case "github":
                return fetchEmailFromGitHub(userRequest);
            case "google":
                return oauth2User.getAttribute("email");
            case "twitter":
                return fetchEmailFromTwitter(userRequest);
            case "facebook":
                return oauth2User.getAttribute("email");
            default:
                throw new UsernameNotFoundException("Provider not supported: " + provider);
        }
    }

    /** Fetch email from GitHub API */
    @SuppressWarnings("rawtypes")
    private String fetchEmailFromGitHub(OAuth2UserRequest userRequest) {
        String accessToken = userRequest.getAccessToken().getTokenValue();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map[]> responseEntity = restTemplate.exchange(
                "https://api.github.com/user/emails", HttpMethod.GET, entity, Map[].class);

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

    /** Fetch email from Twitter API */
    @SuppressWarnings("rawtypes")
    private String fetchEmailFromTwitter(OAuth2UserRequest userRequest) {
        String accessToken = userRequest.getAccessToken().getTokenValue();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                "https://api.twitter.com/2/me?user.fields=email", HttpMethod.GET, entity, Map.class);

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
