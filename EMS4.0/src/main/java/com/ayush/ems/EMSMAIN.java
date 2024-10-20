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

	@Scheduled(cron = "* * * * * *")
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

	@Scheduled(cron = "* * * * * *")
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

//	@Scheduled(cron = "* * * * * *")
//	public void loginAlertWithFail() throws Exception {
//		try {
//			String status = servicelayer.getjob_active_or_not("failed_attempt_alert");
//			System.out.println("failed_attempt_alert " + status);
//			if (status.equalsIgnoreCase("Y")) {
////				Set<Map.Entry<String, String>> get_email_ipaddress = failed_login_Attempt.entrySet();
////				Set<Map.Entry<String, String>> get_device_os = device_os.entrySet();
////				Set<Map.Entry<String, String>> get_device_version = device_version.entrySet();
////				Set<Map.Entry<String, String>> get_device_architecture = device_Architecture.entrySet();
////				Set<Map.Entry<String, Date>> get_failed_login_date_time = login_date_time.entrySet();
//				System.out.println("FAIL EMAIL IPADDRESS " + failed_login_Attempt);
//				System.out.println("FAIL EMAIL DEVICE OS " + device_os);
//				System.out.println("FAIL EMAIL DEVICE VERSION " + device_version);
//				System.out.println("FAIL EMAIL DEVICE ARCHITECTURE " + device_Architecture);
//				System.out.println("FAIL EMAIL DEVICE LOGIN DATE TIME " + login_date_time);
//				for (Map.Entry<String, String> entry : get_email_ipaddress) {
//
//					String email = entry.getKey();
//					String ipaddress = entry.getValue();
//					User user1 = servicelayer.get_user(email);
//					String username = user1.getUsername();
//					String subject = "LOGIN ALERT (" + user1.getUsername() + ")";
//					for (Map.Entry<String, String> entry_get_device_os : get_device_os) {
//						if (email.equals(entry_get_device_os.getKey())) {
//							for (Map.Entry<String, String> entry_get_device_version : get_device_version) {
//								if (email.equals(entry_get_device_version.getKey())) {
//									for (Map.Entry<String, String> entry_get_device_architecture : get_device_architecture) {
//										if (email.equals(entry_get_device_architecture.getKey())) {
//											for (Map.Entry<String, Date> entry_failed_login_date_time : get_failed_login_date_time) {
//												if (email.equals(entry_failed_login_date_time.getKey())) {
//													String device_os = entry_get_device_os.getValue();
//													String device_version = entry_get_device_version.getValue();
//													String device_architecture = entry_get_device_architecture
//															.getValue();
//													Date get_login_date_time = entry_failed_login_date_time.getValue();
//													servicelayer.sentMessage5(email, subject, ipaddress, username,
//															device_os, device_version, device_architecture,
//															get_login_date_time);
//												}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//				if (failed_login_Attempt.size() == 0) {
//					servicelayer.jobrunning("failed_attempt_alert");
//				}
//			} else {
//				servicelayer.jobnotrunning("failed_attempt_alert");
//			}
//		} catch (Exception e) {
////			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
////			String exString=e.toString();
////			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////			{
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = EMSMAIN.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
////			return "SomethingWentWrong";)
////				return "redirect:/swr";
////			}
////			else
//
//		}
//	}
//
//	@Scheduled(cron = "* * * * * *")
//
//	public void loginAlertWithSuccess() throws Exception {
//
//		try {
//			String status = servicelayer.getjob_active_or_not("success_attempt_alert");
//			System.out.println("success_attempt_alert " + status);
//			if (status.equalsIgnoreCase("Y")) {
////				Set<Map.Entry<String, String> get_email_ipaddress = success_login_Attempt.entrySet();
////				Set<Map.Entry<String, String>> get_device_os = device_os.entrySet();
////				Set<Map.Entry<String, String>> get_device_version = device_version.entrySet();
////				Set<Map.Entry<String, String>> get_device_architecture = device_Architecture.entrySet();
////				Set<Map.Entry<String, Date>> get_success_login_date_time = login_date_time.entrySet();
//				System.out.println("SUCCESS EMAIL IPADDRESS " + success_login_Attempt);
//				System.out.println("SUCCESS EMAIL DEVICE OS " + device_os);
//				System.out.println("SUCCESS EMAIL DEVICE VERSION " + device_version);
//				System.out.println("SUCCESS EMAIL DEVICE ARCHITECTURE " + device_Architecture);
//				System.out.println("SUCCESS EMAIL DEVICE LOGIN DATE TIME " + login_date_time);
//				for (Map.Entry<String, List<String>> entry : success_login_Attempt.entrySet()) {
//					String email = entry.getKey();
//					List<String> ipaddress = entry.getValue();
//					User user1 = servicelayer.get_user(email);
//					String username = user1.getUsername();
//					String subject = "LOGIN ALERT (" + user1.getUsername() + ")";
//					for (Map.Entry<String, List<String>> entry_get_device_os : device_os.entrySet()) {
//						if (email.equals(entry_get_device_os.getKey())) {
//							for (Map.Entry<String, List<String>> entry_get_device_version : device_version.entrySet()) {
//								if (email.equals(entry_get_device_version.getKey())) {
//									for (Map.Entry<String, List<String>> entry_get_device_architecture : device_Architecture.entrySet()) {
//										if (email.equals(entry_get_device_architecture.getKey())) {
//											for (Map.Entry<String, List<Date>> entry_succes_login_date_time : login_date_time.entrySet()) {
//												if (email.equals(entry_succes_login_date_time.getKey())) {
//													List<String> device_os = entry_get_device_os.getValue();
//													List<String> device_version = entry_get_device_version.getValue();
//													List<String> device_architecture = entry_get_device_architecture
//															.getValue();
//													List<Date> get_login_date_time = entry_succes_login_date_time.getValue();
//													servicelayer.sentMessage6(email, subject, ipaddress, username,
//															device_os, device_version, device_architecture,
//															get_login_date_time);
//												}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//				if (success_login_Attempt.size() == 0) {
//					servicelayer.jobrunning("success_attempt_alert");
//				}
//			} else {
//				servicelayer.jobnotrunning("success_attempt_alert");
//			}
//		} catch (Exception e) {
////			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
////			String exString=e.toString();
////			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////			{
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = EMSMAIN.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
////			return "SomethingWentWrong";)
////				return "redirect:/swr";
////			}
////			else
//
//		}
//	}
//
//	@Scheduled(cron = "* * * * * *")
//	public void admin_otp_sent_verification() {
//		try {
//			String status = servicelayer.getjob_active_or_not("admin_otp_sent_verification");
//			System.out.println("admin_otp_sent_verification " + status);
//			if (status.equalsIgnoreCase("Y")) {
//				Set<Map.Entry<String, Integer>> admin_otp_sent_during_registration = admin_send_otp.entrySet();
//				System.out.println("ADMIN SENT OTP WITH EMAIL " + admin_send_otp);
//				for (Map.Entry<String, Integer> entry : admin_otp_sent_during_registration) {
//					String to = entry.getKey();
//					Integer otp = entry.getValue();
//					String subject = "Admin Verification";
//					String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<h1>" + "OTP :"
//							+ "<b>" + otp + "</n>" + "</h1>" + "</div>";
//					boolean flag = this.emailService.sendEmail(message, subject, to);
//					System.out.println(to + " FLAG " + flag);
//					if (flag) {
//						admin_send_otp.remove(to);
//					}
//				}
//				servicelayer.jobrunning("admin_otp_sent_verification");
//			} else {
//				servicelayer.jobnotrunning("admin_otp_sent_verification");
//			}
//		} catch (Exception e) {
////			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
////			String exString=e.toString();
////			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////			{
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = EMSMAIN.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
////			return "SomethingWentWrong";)
////				return "redirect:/swr";
////			}
////			else
//
//		}
//
//	}
//
//	@Scheduled(cron = "* * * * * *")
//	public void seperation_email_sent() {
//		try {
//			String status = servicelayer.getjob_active_or_not("seperation_email_sent");
//			System.out.println("seperation_email_sent " + status);
//			if (status.equalsIgnoreCase("Y")) {
//				Set<Map.Entry<Integer, String>> id_with_email_entry = id_with_email.entrySet();
//				Set<Map.Entry<Integer, String>> id_with_cc_entry = id_with_cc.entrySet();
//				Set<Map.Entry<Integer, String>> id_with_username_entry = id_with_username.entrySet();
//				Set<Map.Entry<Integer, Date>> id_with_lastworkingday_entry = id_with_last_working_day_date.entrySet();
//				System.out.println("SUCCESS ID WITH EMAIL " + id_with_email);
//				System.out.println("SUCCESS ID WITH CC " + id_with_cc);
//				System.out.println("SUCCESS ID WITH USERNAME " + id_with_username);
//				System.out.println("SUCCESS ID WITH LASTWORKINGDAY " + id_with_last_working_day_date);
//				for (Map.Entry<Integer, String> id_with_email_entry_loop : id_with_email_entry) {
//					int id = id_with_email_entry_loop.getKey();
//					String to = id_with_email_entry_loop.getValue();
//					for (Map.Entry<Integer, String> id_with_cc_entry_loop : id_with_cc_entry) {
//						int id1 = id_with_cc_entry_loop.getKey();
//						String cc = id_with_cc_entry_loop.getValue();
//						if (id == id1) {
//							for (Map.Entry<Integer, String> id_with_username_entry_loop : id_with_username_entry) {
//								int id2 = id_with_username_entry_loop.getKey();
//								String username = id_with_username_entry_loop.getValue();
//								if (id1 == id2) {
//
//									for (Map.Entry<Integer, Date> id_with_lastworkingday_entry_loop : id_with_lastworkingday_entry) {
//										int id3 = id_with_lastworkingday_entry_loop.getKey();
//										String subject = "Seperation Request EMPID: EMPID" + id3;
//										Date lastdate = id_with_lastworkingday_entry_loop.getValue();
//										if (id2 == id3) {
//											servicelayer.sentMessage2(to, subject, username, lastdate, cc, id3);
//											servicelayer.jobrunning("seperation_email_sent");
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//				if (id_with_email.size() == 0) {
//					servicelayer.jobrunning("seperation_email_sent");
//				} else {
//					servicelayer.jobnotrunning("seperation_email_sent");
//				}
//			}
//			else {
//				servicelayer.jobnotrunning("seperation_email_sent");
//			}
//		} catch (Exception e) {
////			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
////			String exString=e.toString();
////			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////			{
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = EMSMAIN.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
////			return "SomethingWentWrong";)
////				return "redirect:/swr";
////			}
////			else
////			{
////			}
//
//		}
//	}
//
//	@Scheduled(cron = "* * * * * *")
//	public void team_email_sent() {
//		try {
//			String status = servicelayer.getjob_active_or_not("team_email_sent");
//			System.out.println("team_email_sent " + status);
//			if (status.equalsIgnoreCase("Y")) {
//				Set<Map.Entry<Integer, String>> team_email_sent = id_with_email.entrySet();
//				Set<Map.Entry<Integer, String>> team_username_sent = id_with_username.entrySet();
//				Set<Map.Entry<Integer, String>> team_desc_sent = id_with_team_desc.entrySet();
//				Set<Map.Entry<Integer, String>> team_id_sent = id_with_team_id.entrySet();
//				System.out.println("SUCCESS ID WITH EMAIL " + id_with_email);
//				System.out.println("SUCCESS ID WITH TEAM DESCRIPTION " + id_with_team_desc);
//				System.out.println("SUCCESS ID WITH USERNAME " + id_with_username);
//				System.out.println("SUCCESS ID WITH TEAM ID " + id_with_team_id);
//				for (Map.Entry<Integer, String> team_email_sent_loop : team_email_sent) {
//					int id = team_email_sent_loop.getKey();
//					String to = team_email_sent_loop.getValue();
//					for (Map.Entry<Integer, String> team_username_sent_loop : team_username_sent) {
//						int id1 = team_username_sent_loop.getKey();
//						String username = team_username_sent_loop.getValue();
//						if (id == id1) {
//							for (Map.Entry<Integer, String> team_desc_sent_loop : team_desc_sent) {
//								int id2 = team_desc_sent_loop.getKey();
//								String team_desc = team_desc_sent_loop.getValue();
//								if (id1 == id2) {
//									for (Map.Entry<Integer, String> team_id_sent_loop : team_id_sent) {
//										int id3 = team_id_sent_loop.getKey();
//										String team_iid = team_id_sent_loop.getValue();
//										if (id2 == id3) {
//											String subject = "Employee EMPID" + id3 + " Team Assigned";
//											servicelayer.sentMessage1(id, to, subject, team_iid, username, team_desc);
//											servicelayer.jobrunning("team_email_sent");
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//				if (id_with_email.size() == 0) {
//					servicelayer.jobrunning("team_email_sent");
//				} else {
//					servicelayer.jobnotrunning("team_email_sent");
//				}
//			}
//			else {
//				servicelayer.jobnotrunning("team_email_sent");
//			}
//		} catch (Exception e) {
////			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
////			String exString=e.toString();
////			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////			{
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = EMSMAIN.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
////			return "SomethingWentWrong";)
////				return "redirect:/swr";
////			}
////			else
//
//		}
//
//	}
//
	@Scheduled(cron = "* * * * * *")
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

	@Scheduled(cron = "* * * * * *")
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

//	@Scheduled(cron = "0 0/1 * * * *")
//	public void LoginFailRetryEmails() {
//        System.out.println("Attempting to retry failed emails...");
//        emailService.retryFailedEmails();
//    }

//	
//	
//	@Scheduled(cron = "* * * * * *")
//	public void forgot_otp_sent_verification() {
//		try {
//			String status = servicelayer.getjob_active_or_not("forgot_otp_sent_verification");
//			System.out.println("forgot_otp_sent_verification " + status);
//			if (status.equalsIgnoreCase("Y")) {
//				Set<Map.Entry<String, Integer>> forgot_otp_sent_during_registration = forgot_password_email_sent.entrySet();
//				System.out.println("FORGOT SENT OTP WITH EMAIL " + forgot_password_email_sent);
//				System.out.println("MAP OTP " + OTP_validate_map);
//				for (Map.Entry<String, Integer> entry : forgot_otp_sent_during_registration) {
//					String to = entry.getKey();
//					Integer otp = entry.getValue();
//					String subject = "Forgot Email OTP Verification";
//					String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<h1>" + "OTP :" + "<b>"
//							+ otp + "</n>" + "</h1>" + "</div>";
//					boolean flag = this.emailService.sendEmail(message, subject, to);
//					System.out.println(to + " FLAG " + flag);
//					if (flag) {
//						forgot_password_email_sent.remove(to);
//					}
//				}
//				servicelayer.jobrunning("forgot_otp_sent_verification");
//			} else {
//				servicelayer.jobnotrunning("forgot_otp_sent_verification");
//			}
//		} catch (Exception e) {
////			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
////			String exString=e.toString();
////			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////			{
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = EMSMAIN.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
////			return "SomethingWentWrong";)
////				return "redirect:/swr";
////			}
////			else
//
//		}
//
//	}
//	
//	@Scheduled(cron = "* * * * * *")
//	public void payment_success_email_alert() {
//		try
//		{
//			String subject = "Payment Successful";
//			String status = servicelayer.getjob_active_or_not("payment_success_email_alert");
//			System.out.println("payment_success_email_alert " + status);
//			if (status.equalsIgnoreCase("Y")) {
//				Set<Map.Entry<String, String>> payment_success_email_alert_map=payment_success_email_alert.entrySet();
//				Set<Map.Entry<String, Date>> payment_success_time_alert_map=payment_time.entrySet();
//				Set<Map.Entry<String, String>> payment_success_license_number_alert_map=license_number.entrySet();
//				Set<Map.Entry<String, String>> payment_success_license_status_alert_map=license_status.entrySet();
//				Set<Map.Entry<String, String>> payment_success_status_alert_map=license_payment_status.entrySet();
//				Set<Map.Entry<String, String>> payment_invoice_email_alert=payment_invoice_email.entrySet();
//				for(Map.Entry<String, String> email_entry : payment_success_email_alert_map)
//				{
//					String email=email_entry.getKey();
//					for(Map.Entry<String, Date> payment_success_time_alert_map_iterate : payment_success_time_alert_map )
//					{
//						String email1=payment_success_time_alert_map_iterate.getKey();
//						Date payment_time=payment_success_time_alert_map_iterate.getValue();
//					if(email.equals(email1))
//					{
//						for(Map.Entry<String, String> payment_license_number_alert_map_iterate : payment_success_license_number_alert_map )
//						{
//							String email2=payment_license_number_alert_map_iterate.getKey();
//							String license_number=payment_license_number_alert_map_iterate.getValue();
//							if(email1.equals(email2))
//							{
//								for(Map.Entry<String, String> payment_success_license_status_alert_map_iterate : payment_success_license_status_alert_map)
//								{
//									String email3=payment_success_license_status_alert_map_iterate.getKey();
//									String license_status=payment_success_license_status_alert_map_iterate.getValue();
//									if(email2.equals(email3))
//									{
//										for(Map.Entry<String, String> payment_success_status_alert_map_iterate : payment_success_status_alert_map)
//										{
//											String email4=payment_success_status_alert_map_iterate.getKey();
//											String payment_status=payment_success_status_alert_map_iterate.getValue();
//											if(email3.equals(email4))
//											{
//												for(Map.Entry<String, String> payment_invoice_alert_email_iterate : payment_invoice_email_alert)
//												{
//													String email5=payment_invoice_alert_email_iterate.getKey();
//													String invoicePath=payment_invoice_alert_email_iterate.getValue();
//													if(email4.equals(email5))
//													{
//														servicelayer.sentMessage7(payment_status, license_number, payment_time,
//																license_status, subject, email5, invoicePath);
//													}
//												}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//					}
//				}
//				if (payment_success_email_alert.size() == 0) {
//					servicelayer.jobrunning("payment_success_email_alert");
//				} else {
//					servicelayer.jobnotrunning("payment_success_email_alert");
//				}
//			}
//			else
//			{
//				servicelayer.jobnotrunning("payment_success_email_alert");
//			}
//		}
//		catch (Exception e) {
////			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
////			String exString=e.toString();
////			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////			{
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = EMSMAIN.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
////			return "SomethingWentWrong";)
////				return "redirect:/swr";
////			}
////			else
//
//		}
//
//	}

//	@Override
//	public void run(String... args) throws Exception {
//		
//		// Added By Ayush Gupta 21 June 2024 
////		 Purpose :  if job not inserted then first insert job in job table
//		Job job=new Job();
//		int get_count1=jobDao.getJobCount();
//		if(get_count1==0)
//		{
//		List<String> job_list=new ArrayList<>();
//			job_list.add("Account_Locked_job");
//			job_list.add("Login_Delete_Job");
//			job_list.add("Is_Enabled_Job");
//			job_list.add("Is_Disabled_Inactive_User_Job");
//			job_list.add("Password_FailedAttempt_Reset");
//			job_list.add("Update_User_Inactive_Status");
//			job_list.add("get_user_status");
//			job_list.add("delete_old_error_log");
//			job_list.add("login_employeedetail_user_status_correct");
//			job_list.add("remove_garbage_data_session_id");
//			job_list.add("Captcha Validate");
//			job_list.add("OTP Validate");
//			job_list.add("failed_attempt_alert");
//			job_list.add("success_attempt_alert");
//			job_list.add("downtime_maintaince");
//			job_list.add("admin_otp_sent_verification");
//			job_list.add("seperation_email_sent");
//			job_list.add("team_email_sent");
//			job_list.add("disbaled_expired_plan_users");
//			job_list.add("expired_license_status");
//			job_list.add("forgot_otp_sent_verification");
//			job_list.add("payment_success_email_alert");
//			for(String c : job_list)
//			{
//				int get_count=jobDao.getJobCount();
//				if(get_count>0)
//				{
//					int id=jobDao.getJobLastId();
//				job.setId(++id);
//				job.setJob_active_or_not("Y");
//				job.setJob_description(c);
//				jobDao.save(job);
//				
//				}
//				else
//				{
//					job.setId(1);
//					job.setJob_active_or_not("Y");
//					job.setJob_description(c);	
//					jobDao.save(job);
//				}
//			}
//			job_list.remove("Account_Locked_job");
//			job_list.remove("Login_Delete_Job");
//			job_list.remove("Is_Enabled_Job");
//			job_list.remove("Is_Disabled_Inactive_User_Job");
//			job_list.remove("Password_FailedAttempt_Reset");
//			job_list.remove("Update_User_Inactive_Status");
//			job_list.remove("get_user_status");
//			job_list.remove("delete_old_error_log");
//			job_list.remove("login_employeedetail_user_status_correct");
//			job_list.remove("remove_garbage_data_session_id");
//			job_list.remove("Captcha Validate");
//			job_list.remove("OTP Validate");
//			job_list.remove("failed_attempt_alert");
//			job_list.remove("success_attempt_alert");
//			job_list.remove("downtime_maintaince");
//			job_list.remove("admin_otp_sent_verification");
//			job_list.remove("seperation_email_sent");
//			job_list.remove("team_email_sent");
//			job_list.remove("disbaled_expired_plan_users");
//			job_list.remove("expired_license_status");
//			job_list.remove("forgot_otp_sent_verification");
//			job_list.remove("payment_success_email_alert");
//		}
//			 
//			int companyInfo_count=company_dao.getCompanyCount();
//			if(companyInfo_count==0)
//			{
//			CompanyInfo companyInfo=new CompanyInfo();
//			companyInfo.setSno(1);
//			companyInfo.setCompany_id("EMS110092");
//			companyInfo.setCompany_email("ayush.gupta@ems.ac.in");
//			companyInfo.setCompany_address("4068 Post Avenue Newfolden, MN 56738");
//	        companyInfo.setCompany_image_logo_url("data:image/jpeg;base64,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");
//	        companyInfo.setCompany_registration_date("2024-05-23 15:38:06");
//	        companyInfo.setGst_no("GSTIN09AAACH7409R1ZZ");
//	        companyInfo.setCompany_phone("001-234-5678");
//	        companyInfo.setCompany_name("EMS INDIA PVT LTD");
//	        companyInfo.setAdddate(new Date());
//	        companyInfo.setEditdate(new Date());
//	        companyInfo.setEditwho("System");
//	        companyInfo.setAddwho("System");
//	        company_dao.save(companyInfo);
//			}
//			
//			int downtime_count=downtime_Maintaince_Dao.getDowntimeMaintainceCount();
//			if(downtime_count==0)
//			{
//				Downtime_Maintaince downtime_Maintaince=new Downtime_Maintaince();
//				downtime_Maintaince.setSno(1);
//				downtime_Maintaince.setDowntime_description("downtime_maintaince");
//				downtime_Maintaince.setStatus("Y");
//				downtime_Maintaince.setServer_down_or_not(true);
//				downtime_Maintaince_Dao.save(downtime_Maintaince);
//			}
//			
//			Team team=new Team();
//			int get_team_count=teamdao.getTeamCount();
//			if(get_team_count==0)
//			{
//			List<String> list_team=new ArrayList<>();
//			list_team.add("Java Team");
//			list_team.add("Mobile Team");
//			list_team.add("Android Team");
//			list_team.add("Account Team");
//			list_team.add("Oracle Team");
//			list_team.add("AI Team");
//			list_team.add("HR Team");
//			list_team.add("IT Team");
//			list_team.add("QA Team");
//			list_team.add("Infra Team");
//			list_team.add("React Team");
//				for(String c : list_team)
//				{
//					int get_team_count1=teamdao.getTeamCount();
//					
//					if(get_team_count1==0)
//					{
//						team.setId(1);
//						team.setTeam_description(c);
//						team.setTeam_id("EMS110092");
//						teamdao.save(team);
//					}
//					else
//					{
//						// Assuming the team object is already created and available here.
//						String lastTeamId = teamdao.getLastTeamId();
//						String prefix = lastTeamId.substring(0, 3); // Correct prefix extraction
//						String numericPart = lastTeamId.substring(3); // Extract numeric part
//
//						// Convert the numeric part to an integer and increment it
//						int numeric = Integer.parseInt(numericPart);
//						++numeric;
//
//						// Format the new numeric part with leading zeros
//						String formattedNumericPart = String.format("%06d", numeric); // Ensure it's 6 digits
//
//						// Create the new team ID
//						String newTeamId = prefix + formattedNumericPart;
//
//						// Retrieve the last serial number for ID purposes
//						int lastTeamSno = teamdao.getLastSno();
//
//						// Set team details
//						team.setId(lastTeamSno + 1); // Increment the serial number
//						team.setTeam_description(c); // Assuming `c` is the team description
//						team.setTeam_id(newTeamId); // Set the new team ID
//
//						// Print the new team ID
//						System.out.println("Generated Team ID: " + team.getTeam_id());
//
//						// Save the team to the database
//						teamdao.save(team);
//
//					}
//				}
//				list_team.remove("Java Team");
//				list_team.remove("Mobile Team");
//				list_team.remove("Android Team");
//				list_team.remove("Account Team");
//				list_team.remove("Oracle Team");
//				list_team.remove("AI Team");
//				list_team.remove("HR Team");
//				list_team.remove("IT Team");
//				list_team.remove("QA Team");
//				list_team.remove("Infra Team");
//				list_team.remove("React Team");
//	}
//	}

}
