package com.ayush.ems.config;

import java.util.List;
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        int offset = 0;
        int batchSize = 100;
        List<User> userBatch;

        do {
            userBatch = userDao.findByUserName(username, batchSize, offset);

            if (!userBatch.isEmpty()) {
                for (User user : userBatch) {
                    if (user.getEmail().equals(username)) {
                        user.setNewUserActiveOrInactive(true);
                        userDao.save(user); // Save only if needed
                      
                        return new CustomUserDetails(user);
                    }
                }
            }
            offset += batchSize;
        } while (!userBatch.isEmpty());

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
