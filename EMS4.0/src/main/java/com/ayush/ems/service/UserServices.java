package com.ayush.ems.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ayush.ems.dao.NSqlConfigDao;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.dao.UserDetailDao;
import com.ayush.ems.entities.User;
import com.ayush.ems.entities.UserDetail;

@Service
@Transactional
public class UserServices {
	@Autowired
	private UserDao userdao;
	@Autowired
	private UserDetailDao userDetailDao;
	@Autowired
	private NSqlConfigDao sqlConfigDao;

	public User getByEmail(String email) {
		
		Optional<User> user = userdao.findByEmail(email);
		User user1 = user.get();
//		   String res=user.toString();
		return user1;
	}

	@Transactional
	public void increaseFailedAttempt(User user) {
		int attempt = user.getFailedAttempt() + 1;
		userdao.updateFailedAttempt(attempt, user.getEmail());
	}

	@Transactional
	public void lock(User user) {
	    Optional<UserDetail> userDetailOpt = userDetailDao.findByIdField(user.getId());

	    if (userDetailOpt.isEmpty()) {
	        throw new IllegalStateException("UserDetail not found for user ID: " + user.getId());
	    }

	    // ✅ Get dynamic lock days from config table for this user's company
	    String lockDaysStr = sqlConfigDao.findbyConfigKeyAndCompanyId("UNLOCKUSER", user.getCompany_id());

	    int lockDays;
	    try {
	        lockDays = Integer.parseInt(lockDaysStr);
	    } catch (NumberFormatException e) {
	        lockDays = 1; // fallback to 1 day if config is invalid
	    }

	    Date now = new Date();
	    Date expiryDate = Date.from(Instant.now().plus(Duration.ofDays(lockDays)));

	    // Update User
	    user.setAccountNonLocked(false);
	    user.setLockDateAndTime(now);
	    user.setExpirelockDateAndTime(expiryDate);
	    user.setLast_failed_attempt(now);

	    // Update UserDetail
	    UserDetail userDetail = userDetailOpt.get();
	    userDetail.setAccountNonLocked(false);
	    userDetail.setLockDateAndTime(now);
	    userDetail.setExpirelockDateAndTime(expiryDate);

	    // Save both
	    userdao.save(user);
	    userDetailDao.save(userDetail);
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