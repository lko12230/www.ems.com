package com.ayush.ems.config;

import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ayush.ems.dao.UserDao;
import com.ayush.ems.entities.User;

@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserDao userdao;
    @Autowired
    private HttpServletResponse response;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userdao.findByUserName(username);
        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        User user = userOpt.get();
        user.setNewUserActiveOrInactive(true);  // Enable user if needed
        userdao.save(user);

        // Set a cookie with the session ID or any other identifier
        Cookie cookie = new Cookie("custom_session", user.getEmail());
        response.addCookie(cookie);

        return new CustomUserDetails(user);
    }
}
