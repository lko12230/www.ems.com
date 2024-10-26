package com.ayush.ems;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public static void main(String[] args) {
		SpringApplication.run(EMSMAIN.class, args);
	}

	public static Map<String, java.util.Date> session_map_data = new HashMap<>();
	public static Map<String, java.util.Date> captcha_validate_map = new HashMap<>();
	public static Map<Integer, java.util.Date> OTP_validate_map = new HashMap<>();
	public static HashMap<String, List<String>> failed_login_Attempt = new HashMap<>();
	public static HashMap<String, List<String>> failed_os_name = new HashMap<>();
	public static HashMap<String, List<String>> failed_device_version = new HashMap<>();
	public static HashMap<String, List<String>> failed_device_Architecture = new HashMap<>();
	public static HashMap<String, List<Date>> failed_login_date_time = new HashMap<>();
	public static HashMap<String, List<String>> success_login_Attempt = new HashMap<>();
	public static HashMap<String, List<String>> device_os = new HashMap<>();
	public static HashMap<String, List<String>> device_version = new HashMap<>();
	public static HashMap<String, List<String>> device_Architecture = new HashMap<>();
	public static HashMap<String, List<Date>> login_date_time = new HashMap<>();
	public static HashMap<String, Date> login_captcha = new HashMap<>();
	public static HashMap<String, Integer> admin_send_otp = new HashMap<>();
	public static HashMap<Integer, String> id_with_email = new HashMap<>();
	public static HashMap<Integer, String> id_with_cc = new HashMap<>();
	public static HashMap<Integer, String> id_with_username = new HashMap<>();
	public static HashMap<Integer, Date> id_with_last_working_day_date = new HashMap<>();
	public static HashMap<Integer, String> id_with_team_id = new HashMap<>();
	public static HashMap<Integer, String> id_with_team_desc = new HashMap<>();
	public static HashMap<String, Integer> forgot_password_email_sent = new HashMap<>();
	public static HashMap<String, String> payment_success_email_alert = new HashMap<>();
	public static HashMap<String, String> license_number = new HashMap<>();
	public static HashMap<String, Date> payment_time = new HashMap<>();
	public static HashMap<String, String> license_status = new HashMap<>();
	public static HashMap<String, String> license_payment_status = new HashMap<>();
	public static HashMap<String, String> payment_invoice_email = new HashMap<>();
	/*
	 * This Method ADDED By Ayush Gupta on 10th February 2024 Purpose : This
	 * Scheduler Is Used For Unlock Account After 24 Hrs
	 * 
	 */

	@Scheduled(cron = "0 0/1 * * * *")

	public void Account_Locked_job() {
		try {
			String Status = servicelayer.getjob_active_or_not("Account_Locked_job");
			if (Status.equalsIgnoreCase("Y")) {
				servicelayer.getAllUsersByAccount_Non_LockedAndFailed_Attempts();
			} else {
				servicelayer.jobnotrunning("Account_Locked_job");
			}
		} catch (Exception e) {
			servicelayer.jobtime("Account_Locked_job");
		}
	}
	/*---------------------------*/

	/*
	 * This Method ADDED By Ayush Gupta on 10th February 2024 Purpose : This
	 * Scheduler Is Used For Delete Old Login Record More Than 30 Days
	 * 
	 */

	@Scheduled(cron = "0 0/1 * * * *")

	public void Old_Orders_Archive_Job() {
		try {
			String Status = servicelayer.getjob_active_or_not("Login_Old_Orders_Job");
			if (Status.equalsIgnoreCase("Y")) {
				servicelayer.getAllOrdersAdddate();
			} else {
				servicelayer.jobnotrunning("Login_Old_Orders_Job");
			}
		} catch (Exception e) {
			servicelayer.jobtime("Login_Old_Orders_Job");
		}
	}

	@Scheduled(cron = "0 0/1 * * * *")
	public void Login_Old_Data_Archive_Job() {
		try {
			String Status = servicelayer.getjob_active_or_not("Login_Archive_Job");
			if (Status.equalsIgnoreCase("Y")) {
				servicelayer.getAllLoginAdddate();
			} else {
				servicelayer.jobnotrunning("Login_Archive_Job");
			}
			;
		} catch (Exception e) {
			servicelayer.jobtime("Login_Archive_Job");
		}
	}
	
	
	@Scheduled(cron = "0 0/1 * * * *")

	public void Archive_Disabled_Old_User_Job() {
		try {
			String Status = servicelayer.getjob_active_or_not("Archive_Disabled_Old_User_Job");
			if (Status.equalsIgnoreCase("Y")) {
				servicelayer.Archive_Disabled_Old_UserDetail_Job();
				servicelayer.Archive_Disabled_Old_User_Job();
			} else {
				servicelayer.jobnotrunning("Archive_Disabled_Old_User_Job");
			}
			;
		} catch (Exception e) {
			servicelayer.jobtime("Archive_Disabled_Old_User_Job");
		}
	}

	
//	@Scheduled(cron = "0 0/1 * * * *")
//	public void Archive_Disabled_Old_UserDetail_Job() {
//		try {
//			String Status = servicelayer.getjob_active_or_not("Archive_Disabled_Old_UserDetail_Job");
//			if (Status.equalsIgnoreCase("Y")) {
//				servicelayer.Archive_Disabled_Old_UserDetail_Job();
//			} else {
//				servicelayer.jobnotrunning("Archive_Disabled_Old_UserDetail_Job");
//			}
//			;
//		} catch (Exception e) {
//			servicelayer.jobtime("Archive_Disabled_Old_UserDetail_Job");
//		}
//	}

	
	/*----------------------------------*/

	/*
	 * This Method ADDED By Ayush Gupta on 15th February 2024 Purpose : If User
	 * Enabled is false then User update INACTIVE STATUS WITH Enabled false
	 * 
	 */

	@Scheduled(cron = "0 0/1 * * * *")

	public void Is_Enabled_Job() {
		try {
			String status = servicelayer.getjob_active_or_not("Is_Enabled_Job");
			if (status.equalsIgnoreCase("Y")) {
//			throw new Exception();
				servicelayer.schedulerInactivateAccount();
			} else {
				servicelayer.jobnotrunning("Is_Enabled_Job");
			}
		} catch (Exception e) {
			servicelayer.jobtime("Is_Enabled_Job");
		}
	}

	/*---------------------------*/

	/*
	 * This Method ADDED By Ayush Gupta on 10th February 2024 Purpose : This
	 * Scheduler Is Used For Delete Old Login Record More Than 30 Days
	 * 
	 */

//	@Scheduled(cron = "0 0/1 * * * *")
//	public void Is_Disabled_Inactive_User_Job() {
//		try {
//			String status = servicelayer.getjob_active_or_not("Is_Disabled_Inactive_User_Job");
//			if (status.equalsIgnoreCase("Y")) {
////			throw new Exception();
//				servicelayer.InactiveUserDisabled();
//			} else {
//				servicelayer.jobnotrunning("Is_Disabled_Inactive_User_Job");
//			}
//		} catch (Exception e) {
//			servicelayer.jobtime("Is_Disabled_Inactive_User_Job");
//		}
//	}

	@Scheduled(cron = "0 0 0 * * *")

	public void Password_FailedAttempt_Reset() {
		try {
			String status = servicelayer.getjob_active_or_not("Password_FailedAttempt_Reset");
			if (status.equalsIgnoreCase("Y")) {
//			throw new Exception();
				servicelayer.reset_failed_attempts_password();
			} else {
				servicelayer.jobnotrunning("Password_FailedAttempt_Reset");
			}
		} catch (Exception e) {
			servicelayer.jobtime("Password_FailedAttempt_Reset");
		}
	}

	@Scheduled(cron = "0 0/1 * * * *")

	public void Update_User_Inactive_Status() {
		try {
			String status = servicelayer.getjob_active_or_not("Update_User_Inactive_Status");
			if (status.equalsIgnoreCase("Y")) {
//			throw new Exception();
				servicelayer.user_inactive();
			} else {
				servicelayer.jobnotrunning("Update_User_Inactive_Status");
			}
		} catch (Exception e) {
			servicelayer.jobtime("Update_User_Inactive_Status");
		}
	}

	@Scheduled(cron = "0 0/1 * * * *")

	public void get_user_status() {
		try {
			String status = servicelayer.getjob_active_or_not("get_user_status");
			if (status.equalsIgnoreCase("Y")) {
//			throw new Exception();
				servicelayer.update_interrupt_user_status();
			} else {
				servicelayer.jobnotrunning("get_user_status");
			}
		} catch (Exception e) {
			servicelayer.jobtime("get_user_status");
		}
	}

	@Scheduled(cron = "0 0/1 * * * *")

	public void delete_old_error_log() {
		try {
			String status = servicelayer.getjob_active_or_not("delete_old_error_log");
			if (status.equalsIgnoreCase("Y")) {
//			throw new Exception();
				servicelayer.delete_old_error_log();
				System.out.println("MAP CAPTCHA " + captcha_validate_map);
				System.out.println("MAP CAPTCHA SIZE " + captcha_validate_map.size());
			} else {
				servicelayer.jobnotrunning("delete_old_error_log");
			}
		} catch (Exception e) {
			servicelayer.jobtime("delete_old_error_log");
		}
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void downtime() {
		try {
			String server_name = "downtime_maintaince";
			String status = servicelayer.getjob_active_or_not(server_name);
			if (status.equalsIgnoreCase("Y")) {
				System.out.println("SERVER DOWN");
				boolean result = servicelayer.check_server_status(server_name);
				if (result) {
					servicelayer.disabled_server_down_permitted(server_name);
					servicelayer.correct_login_record_table();
				}
				servicelayer.disabled_server_down_permitted(server_name);
				Thread.sleep(3000);
				servicelayer.syncEmployeeAndEmployeeDetailTable();
				Thread.sleep(3000);
				servicelayer.enabled_server_up_permitted(server_name);
				System.out.println("SERVER UP");
				servicelayer.jobrunning("downtime_maintaince");
			} else {
				servicelayer.jobnotrunning("downtime_maintaince");
			}
		} catch (Exception e) {
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = Servicelayer.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//			return "SomethingWentWrong";)
//				return "redirect:/swr";
//			}
//			else
//			{
//			}

		}
	}

//	@Scheduled(cron = "0 0/1  * * * *")
//	public void login_employeedetail_user_status_correct() {
//		try {
//			String status = servicelayer.getjob_active_or_not("sync_employee_and_employeedetail_table");
//			if (status.equalsIgnoreCase("Y")) {
////			throw new Exception();
//				System.out.println("sync_employee_and_employeedetail_table "+status);
//				servicelayer.syncEmployeeAndEmployeeDetailTable();
//			} else {
//				servicelayer.jobnotrunning("sync_employee_and_employeedetail_table");
//			}
//		} catch (Exception e) {
//			servicelayer.jobtime("sync_employee_and_employeedetail_table");
//		}
//	}

	@Scheduled(cron = "0 0/1 * * * *")
	public void remove_garbage_data_session_id() {
		try {
			String status = servicelayer.getjob_active_or_not("remove_garbage_data_session_id");
			System.out.println("remove_garbage_data_session_id " + status);
			if (status.equalsIgnoreCase("Y")) {
				System.out.println("MAP " + session_map_data);
//			throw new Exception();
				servicelayer.check_garbage_dat_map_session_id();
				System.out.println("MAP " + session_map_data);
				System.out.println("MAP CAPTCHA " + captcha_validate_map);
			} else {
				servicelayer.jobnotrunning("remove_garbage_data_session_id");
			}
		} catch (Exception e) {
//				String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
//				String exString=e.toString();
//				if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//				{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = EMSMAIN.class;
			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//				return "SomethingWentWrong";)
//					return "redirect:/swr";
//				}
//				else

		}

	}

	int i = 0;

	@Scheduled(cron = "0 0/1 * * * *")
	public void captchaValidate() {
		try {

			String status = servicelayer.getjob_active_or_not("Captcha Validate");
			System.out.println("Captcha Validate " + status);
			if (status.equalsIgnoreCase("Y")) {
				i = i + 1;
				servicelayer.validate_home_captcha();
				System.out.println("MAP CAPTCHA " + captcha_validate_map);
				System.out.println(i + " MAP CAPTCHA SIZE " + captcha_validate_map.size());
			} else {
				servicelayer.jobnotrunning("Captcha Validate");
			}
		} catch (Exception e) {
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = EMSMAIN.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//			return "SomethingWentWrong";)
//				return "redirect:/swr";
//			}
//			else

		}

	}

	int j = 0;

	@Scheduled(cron = "0 0/1 * * * *")
	public void otpValidate() {
		try {
			String status = servicelayer.getjob_active_or_not("OTP Validate");
			System.out.println("OTP Validate " + status);
			if (status.equalsIgnoreCase("Y")) {
				j = j + 1;
				servicelayer.validate_otp();
				System.out.println("MAP OTP " + OTP_validate_map);
				System.out.println(i + " MAP OTP SIZE " + OTP_validate_map.size());
			} else {
				servicelayer.jobnotrunning("OTP Validate");
			}
		} catch (Exception e) {
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = EMSMAIN.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//			return "SomethingWentWrong";)
//				return "redirect:/swr";
//			}
//			else

		}

	}
	
	@Scheduled(cron = "0 0/1 * * * *")
	public void disable_expired_plan_users() {
		try {
			String status = servicelayer.getjob_active_or_not("disbaled_expired_plan_users");
			System.out.println("disbaled_expired_plan_users " + status);
			if (status.equalsIgnoreCase("Y")) {
				servicelayer.disbaled_expired_plan_users("disbaled_expired_plan_users");
			} else {
				servicelayer.jobnotrunning("disbaled_expired_plan_users");
			}
		} catch (Exception e) {
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = EMSMAIN.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Scheduled(cron = "0 0/1 * * * *")
	public void expired_license_status() {
		try {
			String status = servicelayer.getjob_active_or_not("expired_license_status");
			System.out.println("expired_license_status " + status);
			if (status.equalsIgnoreCase("Y")) {

//				servicelayer.expired_license_status();
			} else {
				servicelayer.jobnotrunning("expired_license_status");
			}
		} catch (Exception e) {
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = EMSMAIN.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Scheduled(cron = "0 0/1 * * * *")
	public void LoginRetryEmails() {
		System.out.println("Attempting to retry failed emails...");
		emailService.retryFailedEmails();
//        forgotOTPEmailService.retryFailedEmails();
//        loginHistoryExportEmail.retryFailedEmails();
//        paymentSucessEmailService.retryFailedEmails();
//        teamEmailService.retryFailedEmails();
	}

	@Scheduled(cron = "0 0/1 * * * *")
	public void ForgotOTPRetryEmails() {
		System.out.println("Attempting to retry failed emails...");
//        emailService.retryFailedEmails();
		forgotOTPEmailService.retryFailedEmails();
//        loginHistoryExportEmail.retryFailedEmails();
//        paymentSucessEmailService.retryFailedEmails();
//        teamEmailService.retryFailedEmails();
	}

	@Scheduled(cron = "0 0/1 * * * *")
	public void LoginHistoryExportExcelRetryEmails() {
		System.out.println("Attempting to retry failed emails...");
//        emailService.retryFailedEmails();
//        forgotOTPEmailService.retryFailedEmails();
		loginHistoryExportEmail.retryFailedEmails();
//        paymentSucessEmailService.retryFailedEmails();
//        teamEmailService.retryFailedEmails();
	}

	@Scheduled(cron = "0 0/1 * * * *")
	public void PaymentSuccessEmailServiceRetryEmails() {
		System.out.println("Attempting to retry failed emails...");
//        emailService.retryFailedEmails();
//        forgotOTPEmailService.retryFailedEmails();
//        loginHistoryExportEmail.retryFailedEmails();
		paymentSucessEmailService.retryFailedEmails();
//        teamEmailService.retryFailedEmails();
	}

	@Scheduled(cron = "0 0/1 * * * *")
	public void TeamEmailServiceRetryEmails() {
		System.out.println("Attempting to retry failed emails...");
//        emailService.retryFailedEmails();
//        forgotOTPEmailService.retryFailedEmails();
//        loginHistoryExportEmail.retryFailedEmails();
//        paymentSucessEmailService.retryFailedEmails();
		teamEmailService.retryFailedEmails();
	}

}