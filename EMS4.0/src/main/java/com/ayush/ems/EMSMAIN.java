package com.ayush.ems;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ayush.ems.service.EmailService;
import com.ayush.ems.service.ForgotOTPEmailService;
import com.ayush.ems.service.LoginHistoryExportEmail;
import com.ayush.ems.service.PaymentSucessEmailService;
import com.ayush.ems.service.Servicelayer;
import com.ayush.ems.service.TeamEmailService;

@SpringBootApplication
@EnableScheduling
public class EMSMAIN {
	@Autowired
	private Servicelayer servicelayer;
	@Autowired
	private EmailService emailService;
	@Autowired
	private PaymentSucessEmailService paymentSucessEmailService;
	@Autowired
	private LoginHistoryExportEmail loginHistoryExportEmail;
	@Autowired
	private ForgotOTPEmailService forgotOTPEmailService;
	@Autowired
	private TeamEmailService teamEmailService;

	private final ReentrantLock lock = new ReentrantLock();

	public static void main(String[] args) {
		SpringApplication.run(EMSMAIN.class, args);
	}

	// Shared Maps for session and validation handling
	public static Map<String, Date> captchaValidateMap = new HashMap<>();
	public static Map<String, Integer> forgot_password_email_sent = new HashMap<>();
	public static Map<String, Integer> admin_send_otp = new HashMap<>();
	public static Map<Integer, Date> otpValidateMap = new HashMap<>();
//	public static Map<String, List<String>> failedLoginAttempt = new HashMap<>();
	public static Map<String, Date> loginCaptcha = new HashMap<>();
	public static Map<String, String> licenseStatus = new HashMap<>();

	@Scheduled(cron = "* * * * * *")
	public void accountLockedJob() {
		executeJob("Account Locked Job", () -> servicelayer.getAllUsersByAccount_Non_LockedAndFailed_Attempts());
	}

//	@Scheduled(cron = "0 0/1  * * * *")
//	public void oldOrdersArchiveJob() {
//		executeJob("Archive Old Orders Job", servicelayer::getAllOrdersAdddate);
//	}

	@Scheduled(cron = "0 0/1  * * * *")
	public void loginOldDataArchiveJob() {
		executeJob("Login Archive Job", servicelayer::getAllLoginAdddate);
	}

	@Scheduled(cron = "* * * * * *")
	public void archiveDisabledOldUserJob() {
		executeJob("Archive Disabled Old User Job", () -> {
			servicelayer.Archive_Disabled_Old_UserDetail_Job();
			servicelayer.Archive_Disabled_Old_User_Job();
		});
	}

	@Scheduled(cron = "* * * * * *")
	public void isEnabledJob() {
		executeJob("Is Enabled Job", servicelayer::schedulerInactivateAccount);
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void passwordFailedAttemptReset() {
		executeJob("Password Failed Attempt Reset", servicelayer::reset_failed_attempts_password);
	}

//	@Scheduled(cron = "* * * * * *")
//	public void updateUserInactiveStatus() {
//		executeJob("Update_User_Inactive_Status", servicelayer::user_inactive);
//	}
//
//	@Scheduled(cron = "* * * * * *")
//	public void getUserStatus() {
//		executeJob("get_user_status", servicelayer::update_interrupt_user_status);
//	}

	@Scheduled(cron = "* * * * * *")
	public void deleteOldErrorLog() {
		executeJob("Delete Old Error Log", () -> {
			servicelayer.delete_old_error_log();
			System.out.println("MAP CAPTCHA " + captchaValidateMap);
			System.out.println("MAP CAPTCHA SIZE " + captchaValidateMap.size());
		});
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void downtime() {
		executeJob("Downtime Maintaince", () -> {
			servicelayer.syncEmployeeAndEmployeeDetailTable();
			servicelayer.correct_login_record_table();
			servicelayer.enabled_server_up_permitted("Downtime Maintaince");
			System.out.println("SERVER UP");
		});
	}

	@Scheduled(cron = "* * * * * *")
	public void captchaValidate() {
		executeJob("Captcha Validate", servicelayer::validate_home_captcha);
	}

	@Scheduled(cron = "* * * * * *")
	public void otpValidate() {
		executeJob("OTP Validate", servicelayer::validate_otp);
	}

	@Scheduled(cron = "* * * * * *")
	public void disableExpiredPlanUsers() {
		executeJob("Disbaled Expired Plan Users", () -> servicelayer.disbaled_expired_plan_users("Disbaled Expired Plan Users"));
	}

//	@Scheduled(cron = "* * * * * *")
//	public void expiredLicenseStatus() {
//		executeJob("expired_license_status", servicelayer::expired_license_status);
//	}

	@Scheduled(cron = "* * * * * *")
	public void loginRetryEmails() {
		executeJob("Email Retry", emailService::retryFailedEmails);
	}

	@Scheduled(cron = "* * * * * *")
	public void forgotOTPRetryEmails() {
		executeJob("Forgot OTP Email Retry", forgotOTPEmailService::retryFailedEmails);
	}

	@Scheduled(cron = "* * * * * *")
	public void loginHistoryExportExcelRetryEmails() {
		executeJob("Login History Export Email Retry", loginHistoryExportEmail::retryFailedEmails);
	}

	@Scheduled(cron = "* * * * * *")
	public void paymentSuccessEmailServiceRetryEmails() {
		executeJob("Payment Success Email Retry", paymentSucessEmailService::retryFailedEmails);
	}

	@Scheduled(cron = "* * * * * *")
	public void teamEmailServiceRetryEmails() {
		executeJob("Team Email Retry", teamEmailService::retryFailedEmails);
	}

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	private void executeJob(String jobName, Runnable jobLogic) {
	    if (!lock.tryLock()) {
	        System.out.println("Job " + jobName + " is already running. Skipping execution.");
	        return;
	    }
	    
	    // Check if EntityManagerFactory is open before proceeding
	    if (!entityManagerFactory.isOpen()) {
	        System.out.println("EntityManagerFactory is closed. Skipping job execution for " + jobName);
	        return;
	    }
	    
	    try {
	        String status = servicelayer.getjob_active_or_not(jobName);
	    	System.out.println(jobName+"EXCEPTION "+status);

	        if ("Y".equalsIgnoreCase(status)) {
	            jobLogic.run();
	            System.out.println(jobName + " Job Status " + status);
	            servicelayer.jobrunning(jobName);
	        } else {
	            servicelayer.jobnotrunning(jobName);
	        }
	    } catch (Exception e) {
//	        logAndHandleException(e, jobName);
	    	System.out.println(e);
	    } finally {
	        lock.unlock();
	    }
	}


	@SuppressWarnings("unused")
	private void logAndHandleException(Exception e, String jobName) {
		String exceptionAsString = e.toString();
		String className = this.getClass().getName();
		String errorMessage = e.getMessage();
		StackTraceElement stackTrace = e.getStackTrace()[0];
		String methodName = stackTrace.getMethodName();
		int lineNumber = stackTrace.getLineNumber();

		System.out.println("Error in job " + jobName + " at " + methodName + " line " + lineNumber);
		servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
	}
}
