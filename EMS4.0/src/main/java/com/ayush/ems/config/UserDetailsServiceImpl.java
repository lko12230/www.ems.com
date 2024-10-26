package com.ayush.ems.config;

import java.util.List;
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
    private UserDao userDao;

    @Autowired
    private HttpServletResponse response; // Consider removing this and handling cookies elsewhere

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        int offset = 0;
        int batchSize = 100; // Define the size of each batch
        List<User> userBatch;

        // Loop to process users in batches
        do {
            userBatch = userDao.findByUserName(username, batchSize, offset);

            // If the batch is not empty, process the users
            if (!userBatch.isEmpty()) {
                for (User user : userBatch) {
                    System.out.println("BATCH " + user);
                    
                    // Check if the user matches the username and process it
                    if (user.getEmail().equals(username)) {
                    	System.out.println(user.getEmail()+" USERNAME EMAIL "+username);
                        user.setNewUserActiveOrInactive(true);  // Enable user if needed
                        userDao.save(user);

                        // Set a cookie with the session ID or any other identifier
                        Cookie cookie = new Cookie("JSSIONID", user.getEmail());
                        cookie.setPath("/"); // Set the cookie path
                        cookie.setHttpOnly(true); // Prevent access via JavaScript
                        cookie.setMaxAge(60 * 60); // Set cookie expiration (1 hour)
                        response.addCookie(cookie);

                        return new CustomUserDetails(user); // Return the matched user details
                    }
                }
            }

            // Update the offset for the next batch
            offset += batchSize; 
        } while (!userBatch.isEmpty()); // Exit when no more users are returned

        // If we exit the loop without finding the user
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
