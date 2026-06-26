package com.ayush.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.ayush.ems.dao.UserDao;
import com.ayush.ems.entities.User;
import com.ayush.ems.service.Servicelayer;

@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private Servicelayer servicelayer;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        int page = 0;  // Start from the first page
        int batchSize = 100;
        Page<User> userBatch;

        do {
            // Fetch users in batches using pageable
            userBatch = servicelayer.fetchUsersByEmail(username, page, batchSize);

            // Log the batch content and total elements to debug
            System.out.println("Batch " + page + " size: " + userBatch.getSize());
            System.out.println("Total elements: " + userBatch.getTotalElements());
            System.out.println("Users in current batch: " + userBatch.getContent());

            // Process the batch
            if (!userBatch.isEmpty()) {
                for (User user : userBatch.getContent()) {
                    if (user.getEmail().equals(username)) {
                        user.setNewUserActiveOrInactive(true);
                        userDao.save(user);  // Save only if needed
                        return new CustomUserDetails(user);
                    }
                }
            }

            page++;  // Increment the page number to move to the next batch
        } while (!userBatch.isEmpty());

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
