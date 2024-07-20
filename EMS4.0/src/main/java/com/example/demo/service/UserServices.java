package com.example.demo.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dao.Userdao;
import com.example.demo.entities.User;

@Service
@Transactional
public class UserServices {
	@Autowired
	private Userdao userdao;

	public User getByEmail(String email) {
		Optional<User> user = userdao.findByUserName(email);
		User user1 = user.get();
//		   String res=user.toString();
		return user1;
	}

	public void increaseFailedAttempt(User user) {
		int attempt = user.getFailedAttempt() + 1;
		userdao.updateFailedAttempt(attempt, user.getEmail());
	}

	public void lock(User user) {
		user.setAccountNonLocked(false);
		user.setLockDateAndTime(new Date());
		Instant i = Instant.now();
		Instant i1 = i.plus(Duration.ofDays(1));
		Date myDate = Date.from(i1);
		user.setExpirelockDateAndTime(myDate);
		user.setLast_failed_attempt(new Date());
		userdao.save(user);
	}

	public static final int MAX_FAILED_ATTEMPTS = 3;

//	   private static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000; // 24 hours
//	   public boolean unlockAccountTimeExpired(User user)
//	   {
//		   long lockTimeInMills=user.getLockDateAndTime().getTime();
//		   long currentTimeMills=System.currentTimeMillis();
//		   System.out.println("-----"+  LOCK_TIME_DURATION);
//		   if(lockTimeInMills+LOCK_TIME_DURATION < currentTimeMills)
//		   {
//			   user.setAccountNonLocked(true);
//			   user.setLockDateAndTime(null);
//			   user.setFailedAttempt(0);
//			   userdao.save(user);
//			   return true;
//		   }
//		   return false;
//	   }

}