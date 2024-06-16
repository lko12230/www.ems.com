package com.example.demo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.demo.entities.User;
import com.example.demo.service.EmailService;
import com.example.demo.service.servicelayer;

@SpringBootApplication
@EnableScheduling
public class EMSMAIN {
	@Autowired
	private servicelayer servicelayer;
	@Autowired
	private EmailService emailService;

	public static void main(String[] args) {
		SpringApplication.run(EMSMAIN.class, args);
	}

	public static Map<String, java.util.Date> session_map_data = new HashMap<>();
	public static Map<String, java.util.Date> captcha_validate_map = new HashMap<>();
	public static Map<Integer, java.util.Date> OTP_validate_map = new HashMap<>();
	public static HashMap<String, String> failed_login_Attempt = new HashMap<>();
	public static HashMap<String, String> success_login_Attempt = new HashMap<>();
	public static HashMap<String, String> device_os = new HashMap<>();
	public static HashMap<String, String> device_version = new HashMap<>();
	public static HashMap<String, String> device_Architecture = new HashMap<>();
	public static HashMap<String, Date> login_date_time = new HashMap<>();
	public static HashMap<String, Date> login_captcha = new HashMap<>();
	public static HashMap<String, Integer> admin_send_otp = new HashMap<>();
	public static HashMap<Integer, String> id_with_email = new HashMap<>();
	public static HashMap<Integer, String> id_with_cc = new HashMap<>();
	public static HashMap<Integer, String> id_with_username = new HashMap<>();
	public static HashMap<Integer, Date> id_with_last_working_day_date = new HashMap<>();
	public static HashMap<Integer, String> id_with_team_id = new HashMap<>();
	public static HashMap<Integer, String> id_with_team_desc = new HashMap<>();
	public static HashMap<String, Integer> forgot_password_email_sent =new HashMap<>();
	public static HashMap<String, String> payment_success_email_alert =new HashMap<>();
	public static HashMap<String, String> license_number =new HashMap<>();
	public static HashMap<String, Date> payment_time =new HashMap<>();
	public static HashMap<String, String> license_status =new HashMap<>();
	public static HashMap<String, String> license_payment_status =new HashMap<>();
	public static HashMap<String, String> payment_invoice_email =new HashMap<>();
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

	public void Login_Delete_Job() {
		try {
			String Status = servicelayer.getjob_active_or_not("Login_Delete_Job");
			if (Status.equalsIgnoreCase("Y")) {
				servicelayer.getAllLoginAdddate();
			} else {
				servicelayer.jobnotrunning("Login_Delete_Job");
			}
			;
		} catch (Exception e) {
			servicelayer.jobtime("Login_Delete_Job");
		}
	}

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

	@Scheduled(cron = "0 0/1 * * * *")
	public void m5() {
		try {
			String status = servicelayer.getjob_active_or_not("Is_Disabled_Inactive_User_Job");
			if (status.equalsIgnoreCase("Y")) {
//			throw new Exception();
				servicelayer.InactiveUserDisabled();
			} else {
				servicelayer.jobnotrunning("Is_Disabled_Inactive_User_Job");
			}
		} catch (Exception e) {
			servicelayer.jobtime("Is_Disabled_Inactive_User_Job");
		}
	}

	@Scheduled(cron = "0 0 0 * * *")

	public void m6() {
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

	public void m7() {
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

	public void m8() {
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

	public void m9() {
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
					Thread.sleep(3000);
					servicelayer.correct_login_record_table();
					Thread.sleep(3000);
					servicelayer.sync_employee_employeedetail();
				}
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
			Class<?> currentClass = servicelayer.class;

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

//	@Scheduled(cron = "0 0/1 * * * *")
//	
//	public void m13() {
//		try {
//			String status = servicelayer.getjob_active_or_not("login_employeedetail_user_status_correct");
//			if (status.equals("Y")) {
////			throw new Exception();
//				servicelayer.employee_login_user_status_sync_correction();
//			} else {
//				servicelayer.jobnotrunning("login_employeedetail_user_status_correct");
//			}
//		} catch (Exception e) {
//			servicelayer.jobtime("login_employeedetail_user_status_correct");
//		}
//	}

	@Scheduled(cron = "0 0/1 * * * *")
	public void m10() {
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
	public void m11() {
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
	public void m12() {
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

	@Scheduled(cron = "* * * * * *")
	public void m13() throws Exception {
		try {
			String status = servicelayer.getjob_active_or_not("failed_attempt_alert");
			System.out.println("failed_attempt_alert " + status);
			if (status.equalsIgnoreCase("Y")) {
				Set<Map.Entry<String, String>> get_email_ipaddress = failed_login_Attempt.entrySet();
				Set<Map.Entry<String, String>> get_device_os = device_os.entrySet();
				Set<Map.Entry<String, String>> get_device_version = device_version.entrySet();
				Set<Map.Entry<String, String>> get_device_architecture = device_Architecture.entrySet();
				Set<Map.Entry<String, Date>> get_failed_login_date_time = login_date_time.entrySet();
				System.out.println("FAIL EMAIL IPADDRESS " + failed_login_Attempt);
				System.out.println("FAIL EMAIL DEVICE OS " + device_os);
				System.out.println("FAIL EMAIL DEVICE VERSION " + device_version);
				System.out.println("FAIL EMAIL DEVICE ARCHITECTURE " + device_Architecture);
				System.out.println("FAIL EMAIL DEVICE LOGIN DATE TIME " + login_date_time);
				for (Map.Entry<String, String> entry : get_email_ipaddress) {

					String email = entry.getKey();
					String ipaddress = entry.getValue();
					User user1 = servicelayer.get_user(email);
					String username = user1.getUsername();
					String subject = "LOGIN ALERT (" + user1.getUsername() + ")";
					for (Map.Entry<String, String> entry_get_device_os : get_device_os) {
						if (email.equals(entry_get_device_os.getKey())) {
							for (Map.Entry<String, String> entry_get_device_version : get_device_version) {
								if (email.equals(entry_get_device_version.getKey())) {
									for (Map.Entry<String, String> entry_get_device_architecture : get_device_architecture) {
										if (email.equals(entry_get_device_architecture.getKey())) {
											for (Map.Entry<String, Date> entry_failed_login_date_time : get_failed_login_date_time) {
												if (email.equals(entry_failed_login_date_time.getKey())) {
													String device_os = entry_get_device_os.getValue();
													String device_version = entry_get_device_version.getValue();
													String device_architecture = entry_get_device_architecture
															.getValue();
													Date get_login_date_time = entry_failed_login_date_time.getValue();
													servicelayer.sentMessage5(email, subject, ipaddress, username,
															device_os, device_version, device_architecture,
															get_login_date_time);
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if (success_login_Attempt.size() == 0) {
					servicelayer.jobrunning("failed_attempt_alert");
				}
			} else {
				servicelayer.jobnotrunning("failed_attempt_alert");
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

	@Scheduled(cron = "* * * * * *")

	public void m14() throws Exception {

		try {
			String status = servicelayer.getjob_active_or_not("success_attempt_alert");
			System.out.println("success_attempt_alert " + status);
			if (status.equalsIgnoreCase("Y")) {
				Set<Map.Entry<String, String>> get_email_ipaddress = success_login_Attempt.entrySet();
				Set<Map.Entry<String, String>> get_device_os = device_os.entrySet();
				Set<Map.Entry<String, String>> get_device_version = device_version.entrySet();
				Set<Map.Entry<String, String>> get_device_architecture = device_Architecture.entrySet();
				Set<Map.Entry<String, Date>> get_success_login_date_time = login_date_time.entrySet();
				System.out.println("SUCCESS EMAIL IPADDRESS " + success_login_Attempt);
				System.out.println("SUCCESS EMAIL DEVICE OS " + device_os);
				System.out.println("SUCCESS EMAIL DEVICE VERSION " + device_version);
				System.out.println("SUCCESS EMAIL DEVICE ARCHITECTURE " + device_Architecture);
				System.out.println("SUCCESS EMAIL DEVICE LOGIN DATE TIME " + login_date_time);
				for (Map.Entry<String, String> entry : get_email_ipaddress) {
					String email = entry.getKey();
					String ipaddress = entry.getValue();
					User user1 = servicelayer.get_user(email);
					String username = user1.getUsername();
					String subject = "LOGIN ALERT (" + user1.getUsername() + ")";
					for (Map.Entry<String, String> entry_get_device_os : get_device_os) {
						if (email.equals(entry_get_device_os.getKey())) {
							for (Map.Entry<String, String> entry_get_device_version : get_device_version) {
								if (email.equals(entry_get_device_version.getKey())) {
									for (Map.Entry<String, String> entry_get_device_architecture : get_device_architecture) {
										if (email.equals(entry_get_device_architecture.getKey())) {
											for (Map.Entry<String, Date> entry_succes_login_date_time : get_success_login_date_time) {
												if (email.equals(entry_succes_login_date_time.getKey())) {
													String device_os = entry_get_device_os.getValue();
													String device_version = entry_get_device_version.getValue();
													String device_architecture = entry_get_device_architecture
															.getValue();
													Date get_login_date_time = entry_succes_login_date_time.getValue();
													servicelayer.sentMessage6(email, subject, ipaddress, username,
															device_os, device_version, device_architecture,
															get_login_date_time);
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if (success_login_Attempt.size() == 0) {
					servicelayer.jobrunning("success_attempt_alert");
				}
			} else {
				servicelayer.jobnotrunning("success_attempt_alert");
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

	@Scheduled(cron = "* * * * * *")
	public void m15() {
		try {
			String status = servicelayer.getjob_active_or_not("admin_otp_sent_verification");
			System.out.println("admin_otp_sent_verification " + status);
			if (status.equalsIgnoreCase("Y")) {
				Set<Map.Entry<String, Integer>> admin_otp_sent_during_registration = admin_send_otp.entrySet();
				System.out.println("ADMIN SENT OTP WITH EMAIL " + admin_send_otp);
				for (Map.Entry<String, Integer> entry : admin_otp_sent_during_registration) {
					String to = entry.getKey();
					Integer otp = entry.getValue();
					String subject = "Google : Admin Verification";
					String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<h1>" + "OTP :"
							+ "<b>" + otp + "</n>" + "</h1>" + "</div>";
					boolean flag = this.emailService.sendEmail(message, subject, to);
					System.out.println(to + " FLAG " + flag);
					if (flag) {
						admin_send_otp.remove(to);
					}
				}
				servicelayer.jobrunning("admin_otp_sent_verification");
			} else {
				servicelayer.jobnotrunning("admin_otp_sent_verification");
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

	@Scheduled(cron = "* * * * * *")
	public void m16() {
		try {
			String status = servicelayer.getjob_active_or_not("seperation_email_sent");
			System.out.println("seperation_email_sent " + status);
			if (status.equalsIgnoreCase("Y")) {
				Set<Map.Entry<Integer, String>> id_with_email_entry = id_with_email.entrySet();
				Set<Map.Entry<Integer, String>> id_with_cc_entry = id_with_cc.entrySet();
				Set<Map.Entry<Integer, String>> id_with_username_entry = id_with_username.entrySet();
				Set<Map.Entry<Integer, Date>> id_with_lastworkingday_entry = id_with_last_working_day_date.entrySet();
				System.out.println("SUCCESS ID WITH EMAIL " + id_with_email);
				System.out.println("SUCCESS ID WITH CC " + id_with_cc);
				System.out.println("SUCCESS ID WITH USERNAME " + id_with_username);
				System.out.println("SUCCESS ID WITH LASTWORKINGDAY " + id_with_last_working_day_date);
				for (Map.Entry<Integer, String> id_with_email_entry_loop : id_with_email_entry) {
					int id = id_with_email_entry_loop.getKey();
					String to = id_with_email_entry_loop.getValue();
					for (Map.Entry<Integer, String> id_with_cc_entry_loop : id_with_cc_entry) {
						int id1 = id_with_cc_entry_loop.getKey();
						String cc = id_with_cc_entry_loop.getValue();
						if (id == id1) {
							for (Map.Entry<Integer, String> id_with_username_entry_loop : id_with_username_entry) {
								int id2 = id_with_username_entry_loop.getKey();
								String username = id_with_username_entry_loop.getValue();
								if (id1 == id2) {

									for (Map.Entry<Integer, Date> id_with_lastworkingday_entry_loop : id_with_lastworkingday_entry) {
										int id3 = id_with_lastworkingday_entry_loop.getKey();
										String subject = "Google : Seperation Request EMPID: GOOGLEIN00" + id3;
										Date lastdate = id_with_lastworkingday_entry_loop.getValue();
										if (id2 == id3) {
											servicelayer.sentMessage2(to, subject, username, lastdate, cc, id3);
											servicelayer.jobrunning("seperation_email_sent");
										}
									}
								}
							}
						}
					}
				}
				if (id_with_email.size() == 0) {
					servicelayer.jobrunning("seperation_email_sent");
				} else {
					servicelayer.jobnotrunning("seperation_email_sent");
				}
			}
			else {
				servicelayer.jobnotrunning("seperation_email_sent");
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
//			{
//			}

		}
	}

	@Scheduled(cron = "* * * * * *")
	public void m17() {
		try {
			String status = servicelayer.getjob_active_or_not("team_email_sent");
			System.out.println("team_email_sent " + status);
			if (status.equalsIgnoreCase("Y")) {
				Set<Map.Entry<Integer, String>> team_email_sent = id_with_email.entrySet();
				Set<Map.Entry<Integer, String>> team_username_sent = id_with_username.entrySet();
				Set<Map.Entry<Integer, String>> team_desc_sent = id_with_team_desc.entrySet();
				Set<Map.Entry<Integer, String>> team_id_sent = id_with_team_id.entrySet();
				System.out.println("SUCCESS ID WITH EMAIL " + id_with_email);
				System.out.println("SUCCESS ID WITH TEAM DESCRIPTION " + id_with_team_desc);
				System.out.println("SUCCESS ID WITH USERNAME " + id_with_username);
				System.out.println("SUCCESS ID WITH TEAM ID " + id_with_team_id);
				for (Map.Entry<Integer, String> team_email_sent_loop : team_email_sent) {
					int id = team_email_sent_loop.getKey();
					String to = team_email_sent_loop.getValue();
					for (Map.Entry<Integer, String> team_username_sent_loop : team_username_sent) {
						int id1 = team_username_sent_loop.getKey();
						String username = team_username_sent_loop.getValue();
						if (id == id1) {
							for (Map.Entry<Integer, String> team_desc_sent_loop : team_desc_sent) {
								int id2 = team_desc_sent_loop.getKey();
								String team_desc = team_desc_sent_loop.getValue();
								if (id1 == id2) {
									for (Map.Entry<Integer, String> team_id_sent_loop : team_id_sent) {
										int id3 = team_id_sent_loop.getKey();
										String team_iid = team_id_sent_loop.getValue();
										if (id2 == id3) {
											String subject = "Google : Employee GOOGLEIN" + id3 + " Team Assigned";
											servicelayer.sentMessage1(id, to, subject, team_iid, username, team_desc);
											servicelayer.jobrunning("team_email_sent");
										}
									}
								}
							}
						}
					}
				}
				if (id_with_email.size() == 0) {
					servicelayer.jobrunning("team_email_sent");
				} else {
					servicelayer.jobnotrunning("team_email_sent");
				}
			}
			else {
				servicelayer.jobnotrunning("team_email_sent");
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
	
	
	@Scheduled(cron = "* * * * * *")
	public void m18() {
		try {
			String status = servicelayer.getjob_active_or_not("forgot_otp_sent_verification");
			System.out.println("forgot_otp_sent_verification " + status);
			if (status.equalsIgnoreCase("Y")) {
				Set<Map.Entry<String, Integer>> forgot_otp_sent_during_registration = forgot_password_email_sent.entrySet();
				System.out.println("FORGOT SENT OTP WITH EMAIL " + forgot_password_email_sent);
				System.out.println("MAP OTP " + OTP_validate_map);
				for (Map.Entry<String, Integer> entry : forgot_otp_sent_during_registration) {
					String to = entry.getKey();
					Integer otp = entry.getValue();
					String subject = "Google : Forgot Email OTP Verification";
					String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<h1>" + "OTP :" + "<b>"
							+ otp + "</n>" + "</h1>" + "</div>";
					boolean flag = this.emailService.sendEmail(message, subject, to);
					System.out.println(to + " FLAG " + flag);
					if (flag) {
						forgot_password_email_sent.remove(to);
					}
				}
				servicelayer.jobrunning("forgot_otp_sent_verification");
			} else {
				servicelayer.jobnotrunning("forgot_otp_sent_verification");
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
	
	@Scheduled(cron = "* * * * * *")
	public void payment_success_email_alert() {
		try
		{
			String subject = "Payment Successful";
			String status = servicelayer.getjob_active_or_not("payment_success_email_alert");
			System.out.println("payment_success_email_alert " + status);
			if (status.equalsIgnoreCase("Y")) {
				Set<Map.Entry<String, String>> payment_success_email_alert_map=payment_success_email_alert.entrySet();
				Set<Map.Entry<String, Date>> payment_success_time_alert_map=payment_time.entrySet();
				Set<Map.Entry<String, String>> payment_success_license_number_alert_map=license_number.entrySet();
				Set<Map.Entry<String, String>> payment_success_license_status_alert_map=license_status.entrySet();
				Set<Map.Entry<String, String>> payment_success_status_alert_map=license_payment_status.entrySet();
				Set<Map.Entry<String, String>> payment_invoice_email_alert=payment_invoice_email.entrySet();
				for(Map.Entry<String, String> email_entry : payment_success_email_alert_map)
				{
					String email=email_entry.getKey();
					for(Map.Entry<String, Date> payment_success_time_alert_map_iterate : payment_success_time_alert_map )
					{
						String email1=payment_success_time_alert_map_iterate.getKey();
						Date payment_time=payment_success_time_alert_map_iterate.getValue();
					if(email.equals(email1))
					{
						for(Map.Entry<String, String> payment_license_number_alert_map_iterate : payment_success_license_number_alert_map )
						{
							String email2=payment_license_number_alert_map_iterate.getKey();
							String license_number=payment_license_number_alert_map_iterate.getValue();
							if(email1.equals(email2))
							{
								for(Map.Entry<String, String> payment_success_license_status_alert_map_iterate : payment_success_license_status_alert_map)
								{
									String email3=payment_success_license_status_alert_map_iterate.getKey();
									String license_status=payment_success_license_status_alert_map_iterate.getValue();
									if(email2.equals(email3))
									{
										for(Map.Entry<String, String> payment_success_status_alert_map_iterate : payment_success_status_alert_map)
										{
											String email4=payment_success_status_alert_map_iterate.getKey();
											String payment_status=payment_success_status_alert_map_iterate.getValue();
											if(email3.equals(email4))
											{
												for(Map.Entry<String, String> payment_invoice_alert_email_iterate : payment_invoice_email_alert)
												{
													String email5=payment_invoice_alert_email_iterate.getKey();
													String invoicePath=payment_invoice_alert_email_iterate.getValue();
													if(email4.equals(email5))
													{
														servicelayer.sentMessage7(payment_status, license_number, payment_time,
																license_status, subject, email5, invoicePath);
													}
												}
											}
										}
									}
								}
							}
						}
					}
					}
				}
				if (payment_success_email_alert.size() == 0) {
					servicelayer.jobrunning("payment_success_email_alert");
				} else {
					servicelayer.jobnotrunning("payment_success_email_alert");
				}
			}
			else
			{
				servicelayer.jobnotrunning("payment_success_email_alert");
			}
		}
		catch (Exception e) {
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

}
