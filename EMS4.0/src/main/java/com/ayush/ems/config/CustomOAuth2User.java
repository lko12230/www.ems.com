package com.ayush.ems.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ayush.ems.entities.User;

public class CustomOAuth2User implements OAuth2User {
    private final OAuth2User oauth2User;
    private final User user;

    public CustomOAuth2User(OAuth2User oauth2User, User user) {
        if (oauth2User == null || user == null) {
            throw new IllegalArgumentException("OAuth2User and User must not be null");
        }
        this.oauth2User = oauth2User;
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // If a user can have multiple roles, collect them into a list
        // This assumes that user.getRoles() returns a List<String> of role names
        return Collections.singletonList(() -> user.getRole());
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    public User getUser() {
        return user;
    }
}
