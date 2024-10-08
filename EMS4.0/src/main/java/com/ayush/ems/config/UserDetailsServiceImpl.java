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
		Optional<User> user = userdao.findByUserName(username);
		User user1 = user.get();
		user1.setNewUserActiveOrInactive(true);
		userdao.save(user1);
		Cookie cookie = new Cookie("JSSIONID", user1.getEmail()); // ("user_name",username);
		response.addCookie(cookie);
		CustomUserDetails customUserDetails = new CustomUserDetails(user.get());
		return customUserDetails;

	}
}
