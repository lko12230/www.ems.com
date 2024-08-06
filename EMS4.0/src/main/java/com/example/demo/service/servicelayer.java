package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.EMSMAIN;
import com.example.demo.dao.Downtime_Maintaince_Dao;
import com.example.demo.dao.JobDao;
import com.example.demo.dao.SubscriptionPlanDao;
import com.example.demo.dao.Teamdao;
import com.example.demo.dao.UserDetailDao;
import com.example.demo.dao.UserLoginDao;
import com.example.demo.dao.Userdao;
import com.example.demo.dao.adminDao;
import com.example.demo.dao.company_dao;
import com.example.demo.dao.error_log_dao;
import com.example.demo.dao.orderDao;
import com.example.demo.dao.performancedao;
import com.example.demo.dao.record_activity_dao;
import com.example.demo.entities.Admin;
import com.example.demo.entities.CompanyInfo;
import com.example.demo.entities.Error_Log;
import com.example.demo.entities.Job;
import com.example.demo.entities.Payment_Order_Info;
import com.example.demo.entities.Performance;
import com.example.demo.entities.RecordActivity;
import com.example.demo.entities.SubscriptionPlans;
import com.example.demo.entities.User;
import com.example.demo.entities.UserDetail;
import com.example.demo.entities.UserLoginDateTime;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.razorpay.Order;
import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.noise.StraightLineNoiseProducer;
import cn.apiclub.captcha.text.producer.DefaultTextProducer;

@Service
public class servicelayer {
	
	@Autowired
	private UserLoginDao userLoginDao;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private EmailService1 emailService1;
	// for register
	@Autowired
	private adminDao adminDao;
	
	@Autowired
	private UserDetailDao userDetailDao;
	
	@Autowired
	private Teamdao teamdao;
	
	@Autowired
	private Userdao userdao;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private error_log_dao error_log_dao;
	
	@Autowired
	private performancedao performancedao;
	
	@Autowired
	private record_activity_dao record_activity_dao;
	
	@Autowired
	private Downtime_Maintaince_Dao downtime_Maintaince_Dao;
	
	@Autowired
	private orderDao orderDao;
	
	@Autowired
	private company_dao company_dao;
	
	@Autowired
	private SubscriptionPlanDao subscriptionPlansDao;
	
	@Autowired
	private PaymentSucessEmailService paymentSucessEmailService;

//@Autowired
//private userdao userdao;
	public User register(User user) throws Exception {
		System.out.println(user.getDob());
		System.out.println(user.getUsername());
		Calendar calendar = Calendar.getInstance();
		int currentYear = calendar.get(Calendar.YEAR);
		Optional<User> option = userdao.findByUserNameAndPhone(user.getEmail(), user.getPhone());
		Optional<Admin> option1 = adminDao.findByUserName(user.getEmail());
		if (option.isPresent()) {
			throw new Exception("Email And Phone Number is  Already Exist");
		} else if (option1.isPresent()) {
			throw new Exception(user.getUsername() + " Something Went Wrong Please Contact Adninistrator");
		} else {
			System.out.println("<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>> " + user.getImage_Url());
			System.out.println(user.getAaid());
			System.out.println(user.getGender());
			int generateRandomPassword = (int) (Math.random() * 900000) + 100000;
//			InetAddress localHost = InetAddress.getLocalHost();
//			String str1 = localHost.toString();
			user.setAccountNonLocked(true);
			user.setPhone(user.getPhone().trim().replaceAll("\\s", ""));
			user.setPassword(passwordEncoder.encode(Integer.toString(generateRandomPassword)));
			user.setRepassword(passwordEncoder.encode(Integer.toString(generateRandomPassword)));
			user.setRole(user.getRole());
			user.setGender(user.getGender());
			user.setAccountNonLocked(true);
			user.setUsername(user.getUsername().toUpperCase());
			user.setEnabled(true);
			user.setBank_account_holder_name("NA");
			user.setBank_account_number(0); 
			user.setBank_name("NA");
			user.setBase_location("NA");
			user.setIfsc_code("NA");
			user.setLaptop_brand("NA");
			user.setLaptop_id("NA");
			user.setLaptop_serial_number("NA");
			user.setEditwho("NA");
			user.setResume_file_url("NA");
			user.setManager_or_not("NA");
			user.setStatus("ACTIVE");
			user.setSystemDateAndTime(new Date());
			// This Logic Added By AYush Gupta 21 June 2024 For Split -> in Designation
String designarionArrowSplit = user.getDesignation();
	        
	        // Split the input string by " -> " to get parts
	        String[] parts = designarionArrowSplit.split(" -> ");
	        
	        // Check if split produced exactly two parts
	        if (parts.length == 2) {
	            // Single name scenario
	            System.out.println("Extracted Name: " + parts[1]);
	        } else if (parts.length > 2) {
	            // Full name scenario
	            StringBuilder fullName = new StringBuilder();
	            for (int i = 1; i < parts.length; i++) {
	                fullName.append(parts[i]);
	                if (i < parts.length - 1) {
	                    fullName.append(" ");
	                }
	            }
	            user.setDesignation(designarionArrowSplit);
	            System.out.println("Extracted Full Name: " + fullName.toString());
	        } else {
	            // Invalid input format scenario
	            System.out.println("Invalid input format");
	        }
			String subject = "Google : Your Crendential Created";
			String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear "
					+ user.getUsername() + "<br>" + "<br>" + "Your Default Password: " + "<b>" + generateRandomPassword
					+ "</b>" + ",Kindly we request you to please reset this password ." + "</p>" + "</div>";
			boolean flag = false;
			if (user.getRole() != null) {
				if (user.getRole().equals("ROLE_ADMIN")) {
					Admin admin = new Admin();
					int admin_last_id = adminDao.getLastId();
					admin.setAid(++admin_last_id);
					admin.setEmail(user.getEmail());
					admin.setSystemDateAndTime(new Date());
					admin.setPassword(passwordEncoder.encode("admin"));
					admin.setRole(user.getRole());
//					adminDao.save(admin);
					String to = user.getEmail();
//					if (flag) {
//
//						user.setDefaultPasswordSent(1);
//					} else {
//						user.setDefaultPasswordSent(0);
//					}
					int getA = user.getAaid();
					Optional<Admin> getAdmin = adminDao.findById(getA);
					Admin adminn = getAdmin.get();
					user.setAdmin(adminn);
					admin.getUserList().add(user);
					User result = userdao.save(user);
					boolean sentornot = false;
					System.out.println(result + "---------------");
					if (result.getEmail() != null) {
						sentornot = true;
					}
					Optional<User> result3 = userdao.findById(result.getId());
					User user3 = result3.get();
					System.out.println("___+++++   " + result.getUsername());
					System.out.println("////////////////" + result);
					UserDetail userdetail = new UserDetail();
					userdetail.setId(result.getId());
					userdetail.setAaid(result.getAaid());
					userdetail.setAccountNonLocked(true);
					userdetail.setBank_account_holder_name("NA");
					userdetail.setBank_account_number(0); 
					userdetail.setBank_name("NA");
					userdetail.setBase_location("NA");
					userdetail.setStatus("ACTIVE");
					userdetail.setDesignation(designarionArrowSplit);
					userdetail.setAddress(result.getAddress());
					userdetail.setAlert_message_sent(result.getAlert_message_sent());
					userdetail.setCountry(result.getCountry());
					userdetail.setImage_Url(result.getImage_Url());
					userdetail.setDob(result.getDob());
					userdetail.setUsername(result.getUsername().toUpperCase());
					userdetail.setEmail(result.getEmail());
					userdetail.setTeam("0");
					userdetail.setEmployeeOnBench(true);
					userdetail.setImage_Url(result.getImage_Url());
					userdetail.setDesignation(user.getDesignation());
					userdetail.setEnabled(result.isEnabled());
					userdetail.setGender(result.getGender());
					userdetail.setIpAddress(result.getIpAddress());
					userdetail.setPhone(result.getPhone());
					userdetail.setRole(result.getRole());
					userdetail.setSystemDateAndTime(result.getSystemDateAndTime());
					userdetail.setAdmin(adminn.getAid());
					userdetail.setImage_Url(user.getImage_Url());
					userdetail.setUsername(result.getUsername());
					userdetail.setPassword(result.getPassword());
					userdetail.setRepassword(result.getRepassword());
					userdetail.setIfsc_code("NA");
					userdetail.setLaptop_serial_number("NA");
					userdetail.setLaptop_brand("NA");
					userdetail.setLaptop_id("NA");
					userdetail.setLaptop_status("NA");
					userdetail.setEditwho("NA");
					userdetail.setWho_assign_laptop("NA");
					userdetail.setReview_rating("NA");
					userdetail.setUser(user3);
					Performance performance = new Performance();
					performance.setId(user.getId());
					performance.setJanuary(0);
					performance.setFebruary(0);
					performance.setMarch(0);
					performance.setApril(0);
					performance.setMay(0);
					performance.setJune(0);
					performance.setJuly(0);
					performance.setAugust(0);
					performance.setSeptember(0);
					performance.setOctober(0);
					performance.setNovember(0);
					performance.setDecember(0);
					performance.setYear(currentYear);
					performancedao.save(performance);
					userDetailDao.save(userdetail);
					adminDao.save(admin);
					if (sentornot) {
						flag = this.emailService.sendEmail(message, subject, to);
						if (flag) {
							user.setDefaultPasswordSent(true);
						} else {
							user.setDefaultPasswordSent(false);
						}
						userdao.save(user);
					}
					return result;
				} else if (user.getRole().equals("ROLE_USER") || user.getRole().equals("ROLE_MANAGER")
						|| user.getRole().equals("ROLE_HR") || user.getRole().equals("ROLE_IT")) {
					String to = user.getEmail();
//					System.out.println(flag);
//					if (flag) {
//
//						user.setDefaultPasswordSent(1);
//					} else {
//						user.setDefaultPasswordSent(0);
//					}
					int getA = user.getAaid();
					Optional<Admin> getAdmin = adminDao.findById(getA);
					Admin admin = getAdmin.get();
					user.setAdmin(admin);
					admin.getUserList().add(user);
					User result = userdao.save(user);
					System.out.println("////////////////" + result);
					Optional<User> result3 = userdao.findById(result.getId());
					User user3 = result3.get();
					boolean sentornot = false;
					System.out.println(result + "---------------");
					if (result.getEmail() != null) {
						sentornot = true;
					}
					UserDetail userdetail = new UserDetail();
					userdetail.setId(result.getId());
					userdetail.setAaid(result.getAaid());
					userdetail.setAccountNonLocked(true);
					userdetail.setAddress(result.getAddress());
					userdetail.setAlert_message_sent(result.getAlert_message_sent());
					userdetail.setCountry(result.getCountry());
					userdetail.setDob(result.getDob());
					userdetail.setDesignation(designarionArrowSplit);
					userdetail.setEmail(result.getEmail());
					userdetail.setImage_Url(result.getImage_Url());
					userdetail.setUsername(result.getUsername());
					userdetail.setTeam("0");
					userdetail.setImage_Url(result.getImage_Url());
					userdetail.setBank_account_holder_name("NA");
					userdetail.setBank_account_number(0); 
					userdetail.setBank_name("NA");
					userdetail.setBase_location("NA");
					userdetail.setEmployeeOnBench(true);
					userdetail.setDesignation(user.getDesignation());
					userdetail.setEnabled(result.isEnabled());
					userdetail.setGender(result.getGender());
					userdetail.setIpAddress(result.getIpAddress());
					userdetail.setPhone(result.getPhone());
					userdetail.setStatus("ACTIVE");
					userdetail.setRole(result.getRole());
					userdetail.setAdmin(admin.getAid());
					userdetail.setPassword(result.getPassword());
					userdetail.setRepassword(result.getRepassword());
					userdetail.setLaptop_brand("NA");
					userdetail.setLaptop_id("NA");
					userdetail.setLaptop_serial_number("NA");
					userdetail.setLaptop_status("NA");
					userdetail.setWho_assign_laptop("NA");
					userdetail.setReview_rating("NA");
					userdetail.setSystemDateAndTime(result.getSystemDateAndTime());
					userdetail.setUser(user3);
					Performance performance = new Performance();
					performance.setId(user.getId());
					performance.setJanuary(0);
					performance.setFebruary(0);
					performance.setMarch(0);
					performance.setApril(0);
					performance.setMay(0);
					performance.setJune(0);
					performance.setJuly(0);
					performance.setAugust(0);
					performance.setSeptember(0);
					performance.setOctober(0);
					performance.setNovember(0);
					performance.setDecember(0);
					performance.setYear(currentYear);
					performancedao.save(performance);
					userDetailDao.save(userdetail);
					if (sentornot) {
						flag = this.emailService.sendEmail(message, subject, to);
						if (flag) {
							user.setDefaultPasswordSent(true);
						} else {
							user.setDefaultPasswordSent(false);
						}
						userdao.save(user);
					}

					return result;
				} else {
					throw new Exception("User Not Registered in ADMIN");
				}
//				String to = user.getEmail();
//				flag = this.emailService.sendEmail(message, subject, to);
			} else {
				throw new Exception("User Role Cannot Be Empty");
			}
//		}
//		else
//		{
//			throw new Exception("Team Id Not Valid");	
//		}
		}
	}

	public static Captcha createCaptcha(int width, int height) {
		return new Captcha.Builder(width, height).addBackground(new GradiatedBackgroundProducer())
				.addText(new DefaultTextProducer()).addNoise(new StraightLineNoiseProducer()).build();

	}

	public static String encodeCaptcha(Captcha captcha) {
		String imageData = null;

		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(captcha.getImage(), "png", os);
			byte[] arr = Base64.getEncoder().encode(os.toByteArray());
			imageData = new String(arr);
			System.out.println("image created" + imageData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return imageData;
	}

	public String generateString() {
		try {
			String uuid = UUID.randomUUID().toString();
			return uuid;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	public boolean saveNewPassword(String NewPassword, String Email) {
		try {
			Optional<User> user = userdao.findByUserName(Email);
			User user1 = user.get();
			user1.setPassword(passwordEncoder.encode(NewPassword));
			user1.setRepassword(passwordEncoder.encode(NewPassword));
//		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//		Date date = new Date();
//		String formatted = formatter.format(date);
			user1.setEditdate(new Date());
			user1.setEditwho(user1.getUsername());
			userdao.save(user1);
			return true;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return false;
		}

	}

	public void AllIntanceVariableClear(User user) {
		user.setAddress(null);
		user.setCountry(null);
		user.setDefaultPasswordSent(false);
		user.setDob(null);
		user.setEditdate(null);
		user.setEditwho(null);
		user.setEmail(null);
		user.setEmail(null);
		user.setEnabled(false);
		user.setFailedAttempt(0);
		user.setGender(null);
		user.setPassword(null);
		user.setPhone(null);
		user.setRepassword(null);
		user.setRole(null);
		user.setUsername(null);
		user.setLastWorkingDay(null);
		user.setSperationDate(null);

	}

	@Transactional
	public void getAllUsersByAccount_Non_LockedAndFailed_Attempts() {
		try {
			List<Date> list = userdao.getAllLock_Date_And_Time_Records();
			for (int i = 0; i < list.size(); i++) {
				Date lockDateAndTime = list.get(i);
				jobrunning("Account_Locked_job");
				if (lockDateAndTime != null) {
					userdao.getAllAccount_LockedAndUnlokedDetails(lockDateAndTime);
					System.out.println(list.get(i));
				}
			}
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = servicelayer.class;
			jobDao.getJobRunningTimeInterrupted("Account_Locked_job");
			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
		}
	}

//	@Transactional
//	public void getAllUserByExperience() {
//		List<Integer> list = daolayer.getAllExp();
//		for (int i = 0; i < list.size(); i++) {
//			daolayer.skills(list.get(i));
//		}
//	}

	@Transactional
	public void getAllLoginAdddate() {
		try {
			List<Date> login = userLoginDao.findBySystemAddate();
			for (int i = 0; i < login.size(); i++) {
				Date adddate = login.get(i);
				jobrunning("Login_Delete_Job");
				if (adddate != null) {
					userLoginDao.deleteOldLoginRecord(adddate);
//					System.out.println(adddate);
				}

			}
			jobDao.getJobRunningTime("Login_Delete_Job");
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = servicelayer.class;
			jobDao.getJobRunningTimeInterrupted("Login_Delete_Job");
			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
		}
	}

//	public void getAllEmployees()
//	{
//		List<UserDetail> all_users=new ArrayList<>();
//		all_users=userDetailDao.findAll();
//		for(int i=0;i<all_users.size();i++)
//		{
//			UserDetail userd=all_users.get(i);
//			System.out.println("Find ALL "+userd);
//			
//		}
//	}

	public void sentMessage(String to, String subject, String team_id, String username) throws Exception {
		try {
			String teamDescwithid = teamdao.getAllDataFromTeamDescription(team_id);
			boolean flag = false;
			String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear " + username
					+ "<br>" + "<br>" + "Welcome To New Team " + "<b>" + teamDescwithid + "</b>"
					+ ",You will get mail from your manager within 2 working days." + "</p>" + "</div>";
			flag = this.emailService.sendEmail(message, subject, to);
			if (flag == true) {
				System.out.println(true);
			} else {
				System.out.println(false);
			}
//		userDetailDao.save(userDetail);	
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public void sentMessage1(int id, String to, String subject, String team_id, String username, String team_desc)
			throws Exception {
		try {
//		String teamDescwithid = teamdao.getAllDataFromTeamDescription(team_id);
			boolean flag = false;
			String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear " + username
					+ "<br>" + "<br>" + "Your Team Changed and your new team is " + "<b>" + team_id + " -> " + team_desc
					+ "</b>" + ",You will get mail from your manager within 2 working days." + "<br><br>"
					+ "Resource Management Team" + "</p>" + "</div>";
			flag = this.emailService.sendEmail(message, subject, to);
			if (flag == true) {
				System.out.println(true);
				EMSMAIN.id_with_email.remove(id);
				EMSMAIN.id_with_team_id.remove(id);
				EMSMAIN.id_with_username.remove(id);
				EMSMAIN.id_with_team_desc.remove(id);
				jobrunning("team_email_sent");
			} else {
				System.out.println(false);
				jobrunning("team_email_sent");
			}
//		userDetailDao.save(userDetail);	
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("team_email_sent");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public void sentMessage3(String to, String subject, String team_id, String username) throws Exception {
		try {
			String teamDescwithid = teamdao.getAllDataFromTeamDescription(team_id);
			boolean flag = false;
			String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear " + username
					+ "<br>" + "<br>" + "You have removed from team ," + "<b>" + teamDescwithid + "</b>" + " on "
					+ "<b>" + new Date() + "</b>" + "<br><br>" + "Resource Management Team" + "</p>" + "</div>";
			flag = this.emailService.sendEmail(message, subject, to);
			if (flag == true) {
				System.out.println(true);
			} else {
				System.out.println(false);
			}
//		userDetailDao.save(userDetail);	
		} catch (Exception e) {
//			jobDao.getJobRunningTimeInterrupted("remove_garbage_data_session_id");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public void sentMessage2(String to, String subject, String username, Date lastworkingday, String cc, int id) {
		try {
			boolean flag = false;
			String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear " + username
					+ "<br>" + "<br>" + "Your Resignation Request Accepted and your last working day is " + "<b>"
					+ lastworkingday + "</b>" + "<br><br>" + "All the best for your future endavours" + "<br>"
					+ "HR Team " + "</p>" + "</div>";
			flag = this.emailService1.sendEmail(message, subject, to, cc);
			if (flag == true) {
				System.out.println(true);
				EMSMAIN.id_with_email.remove(id);
				EMSMAIN.id_with_cc.remove(id);
				EMSMAIN.id_with_username.remove(id);
				EMSMAIN.id_with_last_working_day_date.remove(id);
			} else {
				System.out.println(false);
			}
//		userDetailDao.save(userDetail);	
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("seperation_email_sent");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public void sentMessage4(String to, String subject, String username, Date lastworkingday, String cc) {
		try {
			boolean flag = false;
			String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear " + username
					+ "<br>" + "<br>" + "You are job service is terminated by GOOGLE and your last working day is "
					+ "<b>" + lastworkingday + "</b>" + "<br><br>" + "All the best for your future endavours" + "<br>"
					+ "HR Team " + "</p>" + "</div>";
			flag = this.emailService1.sendEmail(message, subject, to, cc);
			if (flag == true) {
				System.out.println(true);
			} else {
				System.out.println(false);
			}
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("----");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public void sentMessage5(String to, String subject, String ipaddress, String username, String device_os,
			String device_version, String device_architecture, Date login_date_time) throws Exception {
		try {
			boolean flag = false;
			String osName = device_os;
			String osVersion = device_version;
			String osArchitecture = device_architecture;
			String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear " + username
					+ "<br>" + "<br>"
					+ "SomeOne Try To Attempt Login Failed Attempt . If not you , Please Change Password Immediately . "
					+ "<br>" + "<br>" + "Username : " + "<b>" + to + "</b>" + "<br>" + "IP ADDRESS : " + "<b>"
					+ ipaddress + "</b>" + "<br>" + "Device LOGIN TIME : " + "<b>" + login_date_time + "</b>" + "</b>"
					+ "<br>" + "Device OS : " + "<b>" + osName + "</b>" + "<br>" + "Device Version : " + "<b>"
					+ osVersion + "</b>" + "<br>" + "Device Architecture : " + "<b>" + osArchitecture + "</b>" + "<br>"
					+ "<br>" + "Cyber Team " + "</p>" + "</div>";
			flag = this.emailService.sendEmail(message, subject, to);
			System.out.println(" EMAIL IPADDRESS !!!!!!!!!!!!" + flag);
			System.out.println(" EMAIL DEVICE OS  !!!!!!!!!!!!!" + flag);
			System.out.println(" EMAIL DEVICE VERSION !!!!!!!!!" + flag);
			System.out.println(" EMAIL DEVICE ARCHITECTURE !!!!!" + flag);
			if (flag == true) {
				System.out.println(true);
				EMSMAIN.failed_login_Attempt.remove(to);
				EMSMAIN.device_os.remove(to);
				EMSMAIN.device_version.remove(to);
				EMSMAIN.device_Architecture.remove(to);
				EMSMAIN.login_date_time.remove(to);
				Optional<User> get_user= userdao.findByUserName(to);
				User user1=get_user.get();
				if(user1.getAlert_message_sent()==0)
				{
				user1.setAlert_message_sent(1);
				userdao.save(user1);
				}
				else
				{
					user1.setAlert_message_sent(user1.getAlert_message_sent()+1);
					userdao.save(user1);
				}
			} else {
				System.out.println(false);
			}
//		userDetailDao.save(userDetail);	
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("failed_attempt_alert");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Transactional
	public void sentMessage6(String to, String subject, String ipaddress, String username, String device_os,
			String device_version, String device_architecture, Date login_date_time) throws Exception {
		try {
			boolean flag = false;
			String osName = device_os;
			String osVersion = device_version;
			String osArchitecture = device_architecture;
			String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear " + username
					+ "<br>" + "<br>" + "Login Success" + "<br>" + "<br>" + "Username : " + "<b>" + to + "</b>" + "<br>"
					+ "IP ADDRESS : " + "<b>" + ipaddress + "</b>" + "<br>" + "Device LOGIN TIME : " + "<b>"
					+ login_date_time + "</b>" + "</b>" + "<br>" + "Device OS : " + "<b>" + osName + "</b>" + "<br>"
					+ "Device Version : " + "<b>" + osVersion + "</b>" + "<br>" + "Device Architecture : " + "<b>"
					+ osArchitecture + "</b>" + "<br>" + "<br>" + "Cyber Team " + "</p>" + "</div>";
			flag = this.emailService.sendEmail(message, subject, to);
			if (flag == true) {
				System.out.println(true);
				EMSMAIN.success_login_Attempt.remove(to);
				EMSMAIN.device_os.remove(to);
				EMSMAIN.device_version.remove(to);
				EMSMAIN.device_Architecture.remove(to);
				EMSMAIN.login_date_time.remove(to);
				Optional<User> user=userdao.findByUserName(to);
				User user1=user.get();
				if(user1.getAlert_message_sent()==0)
				{
					user1.setAlert_message_sent(1);
					userdao.save(user1);
				}
				else
				{
					user1.setAlert_message_sent(user1.getAlert_message_sent()+1);
					userdao.save(user1);
				}
			} else {
				System.out.println(false);
			}
//		userDetailDao.save(userDetail);	
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("success_attempt_alert");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public void sentMessage7(String payment_status, String license_number, Date payment_time, String license_status,
			String subject, String to, String invoicepath) throws Exception {
		try {
			Optional<User> user = userdao.findByUserName(to);
			User user1 = user.get();
			boolean flag = false;
			String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear "
					+ user1.getUsername() + "<br>" + "<br>" + "Payment Success" + "<br>" + "<br>" + "Username : "
					+ "<b>" + to + "</b>" + "<br>" + "Payment Time : " + "<b>" + payment_time + "</b>" + "<br>"
					+ "License Number : " + "<b>" + license_number + "</b>" + "</b>" + "<br>" + "License Status : "
					+ "<b style='color:green'>" + license_status + "</b>" + "<br>" + "Payment Status : "
					+ "<b style='text-transform: uppercase; color: green'>" + payment_status + "</b>" + "<br>" + "<br>"
					+ "Payment Team " + "</p>" + "</div>";

			flag = this.paymentSucessEmailService.sendEmail(invoicepath, message, subject, to);
			if (flag == true) {
				System.out.println(true);
				EMSMAIN.payment_success_email_alert.remove(to);
				EMSMAIN.license_number.remove(to);
				EMSMAIN.license_status.remove(to);
				EMSMAIN.payment_time.remove(to);
				EMSMAIN.license_payment_status.remove(to);
				EMSMAIN.payment_invoice_email.remove(to);
			} else {
				System.out.println(false);
			}
//		userDetailDao.save(userDetail);	
		} catch (Exception e) {
//			jobDao.getJobRunningTimeInterrupted("remove_garbage_data_session_id");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Transactional
	public void seperationLogic(Integer id, User user) {
		try {
			user.setSperationDate(new Date());
			Instant i = Instant.now();
			Instant i1 = i.plus(Duration.ofDays(2));
			Date myDate = Date.from(i1);
			user.setLastWorkingDay(myDate);
//		   userDetailDao.save(userDetail);
			System.out.println("|||||||||| " + user.getLastWorkingDay());
			userdao.save(user);
			Optional<UserDetail> userDetail = userDetailDao.findById(id);
			UserDetail userDetail2 = userDetail.get();
			userDetail2.setLastWorkingDay(myDate);
			userDetailDao.save(userDetail2);
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}

	}

	@Transactional
	public void schedulerInactivateAccount() {
		try {
			List<Integer> map = userdao.getLastWorkingDay_Records();
			ListIterator<Integer> itr = map.listIterator();
			while (itr.hasNext()) {
				Optional<User> user = userdao.findById(itr.next());
				User user1 = user.get();
				Date lastDateGet = user1.getLastWorkingDay();
				if (lastDateGet != null) {
					userdao.getEnableFalse(lastDateGet);
					if (user1.isEnabled() == false) {
						Optional<UserDetail> userDetail = userDetailDao.findById(user1.getId());
						UserDetail userDetail2 = userDetail.get();
						userDetail2.setEnabled(false);
						userDetailDao.save(userDetail2);
					}
				}
			}
			jobrunning("Is_Enabled_Job");
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("Is_Enabled_Job");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
//		Map<Integer,Date> map=daolayer.getLastWorkingDay_Records();
//		Iterator<Entry<Integer, Date>> itr=map.entrySet().iterator();
//		while(itr.hasNext())
//		{
//			Map.Entry<Integer, Date> GetMap=(Map.Entry<Integer,Date>)itr.next();
//			Date getDateRes=GetMap.getValue();
//			if(getDateRes!=null)

//			daolayer.getEnableFalse(getDateRes);
//			Optional<UserDetail> userDetail=  userDetailDao.findById(GetMap.getKey());
//			UserDetail userdetail1=userDetail.get();
//			userdetail1.setEnabled(false);
//			userDetailDao.save(userdetail1);
//			}
//		}
	}

	public List<UserDetail> findUserByTeam(int id) {
		try {
			Optional<UserDetail> userDetail = userDetailDao.findById(id);
			UserDetail userDetail2 = userDetail.get();
			String team_id = userDetail2.getTeam();
			List<UserDetail> userDetail3 = userDetailDao.getUserByTeam(team_id);
			return userDetail3;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	@Transactional
	public void jobtime(String name) {
		try {
			jobDao.getJobRunningTimeInterrupted(name);
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public String getjob_active_or_not(String name) {
		try {
			String result = jobDao.getJobStatus(name);
			return result;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}

	}

	@Transactional
	public void jobnotrunning(String name) {
		try {
			jobDao.getJobNotRunning(name);
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Transactional
	public void jobrunning(String name) {
		try {
			jobDao.getJobRunningTime(name);
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Transactional
	public void InactiveUserDisabled() {
		List<User> list = userdao.findAll();
		ListIterator<User> listIterator = list.listIterator();
		while (listIterator.hasNext()) {
			User user = listIterator.next();
			if (user.isNewUserActiveOrInactive() == false) {
				System.out.println("USER ID IS DISABLED " + user);
				userdao.disableuserbyid(user.getId());
			}
		}
		jobrunning("Is_Disabled_Inactive_User_Job");
	}

	@Transactional
	public void reset_failed_attempts_password() {
		jobrunning("Password_FailedAttempt_Reset");
		userdao.reset_failed_attempt_job();
	}

	@Transactional
	public void user_inactive() {
		jobrunning("Update_User_Inactive_Status");
		userLoginDao.Update_Inactive_user_Status();
	}

	@Transactional
	public void update_interrupt_user_status() {
		try {
			jobrunning("get_user_status");
			userLoginDao.updateuserstatus();
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = servicelayer.class;
			jobDao.getJobRunningTimeInterrupted("get_user_status");
			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
		}
	}

	@Transactional
	public void delete_old_error_log() {
		try {
			jobrunning("delete_old_error_log");
			error_log_dao.deleteOldErrorLog();
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = servicelayer.class;
			jobDao.getJobRunningTimeInterrupted("delete_old_error_log");
			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
		}
	}

	public void insert_error_log(String error_description, String java_file_name, String error_message,
			String method_name, int linenumber) {
		try {
			int count = error_log_dao.getCount();
			if (count > 0) {
				int getLoginLastId = error_log_dao.getLastId();
				Error_Log error_Log = new Error_Log();
				error_Log.setSno(++getLoginLastId);
				error_Log.setError_description(error_description);
				error_Log.setErrorDate(new Date());
				error_Log.setJava_class_Name(java_file_name);
				error_Log.setError_message(error_message);
				error_Log.setMethod_name(method_name);
				error_Log.setError_line_number(linenumber);
				error_log_dao.save(error_Log);
			} else {
				Error_Log error_Log = new Error_Log();
				error_Log.setSno(1);
				error_Log.setError_description(error_description);
				error_Log.setErrorDate(new Date());
				error_Log.setJava_class_Name(java_file_name);
				error_Log.setError_message(error_message);
				error_Log.setMethod_name(method_name);
				error_Log.setError_line_number(linenumber);
				error_log_dao.save(error_Log);
			}
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
		}
	}

//	List<UserLoginDateTime> data_insert_excel_list = new ArrayList<>();
//
//	public boolean data_insert_excel() throws IOException { 
//		try {
//			String path = "C:\\Users\\ayush.gupta\\Desktop\\USER_LOGIN_DATA.xlsx";
//			data_insert_excel_list = userLoginDao.findAll();
//			ListIterator<UserLoginDateTime> excel = data_insert_excel_list.listIterator();
//			File file = new File(path);
//			if (!file.exists()) {
//				OutputStream fileOut = new FileOutputStream(file);
//				Workbook workbook = new XSSFWorkbook();
//				org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("USER_LOGIN_DATA");
//				sheet.setColumnWidth(0, 4000);
//				sheet.setColumnWidth(1, 5000);
//				sheet.setColumnWidth(2, 5000);
//				sheet.setColumnWidth(3, 7000);
//				sheet.setColumnWidth(4, 7000);
//				sheet.setColumnWidth(5, 5000);
//				sheet.setColumnWidth(6, 5000);
//				sheet.setColumnWidth(7, 5000);
//				sheet.setColumnWidth(8, 5000);
//				sheet.setColumnWidth(9, 10000);
//
//				int rowCount = 0;
////			Row row = sheet.createRow(rowCount + 4);
//				sheet.createRow(rowCount + 4);
//				org.apache.poi.ss.usermodel.Sheet sheetAtt = workbook.getSheetAt(0);
//
//				System.out.println(sheetAtt);
//
//				Font font = workbook.createFont();
//				font.setFontHeightInPoints((short) 10);
//				font.setFontName("Arial");
//				font.setBold(true);
//				font.setItalic(true);
//
//				int lastRowNum = sheetAtt.getLastRowNum();
//				// XSSFRow row = sheetAt.getRow(lastRowNum);
//				System.out.println(lastRowNum);
//				Font fontt = workbook.createFont();
//				fontt.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
//				CellStyle style = workbook.createCellStyle();
//				style.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
//				style.setFillPattern(FillPatternType.DIAMONDS);
//				style.setAlignment(HorizontalAlignment.CENTER);
//				style.setFont(fontt);
//				CellStyle style2 = workbook.createCellStyle();
//				style2.setFont(font);
//				// Row header = sheet.createRow(0);
//
////		        header.createCell(0).setCellValue("ID");
////		        header.createCell(1).setCellValue("DATE");
////		        header.createCell(2).setCellValue("ABOUT");
////		        header.createCell(3).setCellValue("EMAIL");
////		        header.createCell(4).setCellValue("ENABLED");
////		        header.createCell(5).setCellValue("IMAGE_URL");
////		        header.createCell(6).setCellValue("NAME");
////		        header.createCell(7).setCellValue("PASSWORD");
////		        header.createCell(8).setCellValue("ROLE");
//
//				// create for excel file for create date heading on top
//
//				XSSFRow rowww = (XSSFRow) sheet.createRow(0);
//				XSSFCell celll = rowww.createCell(0);
//				celll.setCellValue("Date (dd/MM/yyyy):");
//				celll.setCellStyle(style2);
//				celll = rowww.createCell(1);
//				// create for excel file creation date
//				SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
//				String timeStamp = df.format(new Date());
//				celll.setCellValue(timeStamp);
//				celll.setCellStyle(style2);
//
//				rowww = (XSSFRow) sheet.createRow(1);
//				celll = rowww.createCell(0);
//				celll.setCellValue("Time(HH:mm:ss):");
//				celll.setCellStyle(style2);
//				celll = rowww.createCell(1);
//				// create for excel file creation date
//				SimpleDateFormat dff = new SimpleDateFormat("HH:mm:ss");
//				String timeStampp = dff.format(new Date());
//
////			SimpleDateFormat dfff = new SimpleDateFormat("dd/MM/yyyy.HH:mm:ss");
////			String timeStampppp = dfff.format(new Date());
//				celll.setCellValue(timeStampp);
//				celll.setCellStyle(style2);
//
//				CreationHelper createHelper = workbook.getCreationHelper();
//				CellStyle dateCellStyle = workbook.createCellStyle();
//				dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss")); // Set
//																												// the
//																												// desired
//																												// date
//																												// format
//
//				XSSFRow roww = (XSSFRow) sheet.createRow(3);
//				XSSFCell cell = roww.createCell(0);
//				cell.setCellValue("ID");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(1);
//				cell.setCellValue("LOGIN DATE TIME");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(2);
//				cell.setCellValue("LOGOUT DATE TIME");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(3);
//				cell.setCellValue("EMAIL");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(4);
//				cell.setCellValue("IP ADDRESS");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(5);
//				cell.setCellValue("IS SESSION EXPIRED");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(6);
//				cell.setCellValue("USERNAME");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(7);
//				cell.setCellValue("USER STATUS");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(8);
//				cell.setCellValue("SESSION ID");
//				cell.setCellStyle(style);
//
//				while (excel.hasNext()) {
//					CellStyle style1 = workbook.createCellStyle();
//					style1.setAlignment(HorizontalAlignment.CENTER);
//					UserLoginDateTime userDateTime = excel.next();
//					Row datarow = sheetAtt.createRow(++lastRowNum);
//					datarow.createCell(0).setCellValue(userDateTime.getId());
//					datarow.createCell(1).setCellValue(userDateTime.getLoginDateAndTime());
//					datarow.getCell(1).setCellStyle(dateCellStyle);
//					datarow.createCell(2).setCellValue(userDateTime.getLogoutDateAndTime());
//					datarow.getCell(2).setCellStyle(dateCellStyle);
//					datarow.createCell(3).setCellValue(userDateTime.getEmail());
//					datarow.createCell(4).setCellValue(userDateTime.getIpAddress());
//					datarow.createCell(5).setCellValue(userDateTime.is_session_interrupted());
//					datarow.createCell(6).setCellValue(userDateTime.getUsername());
//					datarow.createCell(7).setCellValue(userDateTime.isUser_status());
//					datarow.createCell(8).setCellValue(userDateTime.getSession_Id());
//					String st = (userDateTime.getId() + " " + userDateTime.getLoginDateAndTime() + " "
//							+ userDateTime.getLogoutDateAndTime() + " " + userDateTime.getEmail() + " "
//							+ userDateTime.getIpAddress() + " " + userDateTime.is_session_interrupted() + " "
//							+ userDateTime.getUsername() + " " + userDateTime.isUser_status() + " "
//							+ userDateTime.getSession_Id());
//					System.out.println(st);
//				}
////				 FileOutputStream fileOutputStream=new FileOutputStream(path,true);
//				workbook.write(fileOut);
//				fileOut.flush();
//				fileOut.close();
//				workbook.close();
//				return true;
//
//			} else {
//				OutputStream fileOut = new FileOutputStream(file);
//				Workbook workbook = new XSSFWorkbook();
//				org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("USER_LOGIN_DATA");
//				sheet.setColumnWidth(0, 4000);
//				sheet.setColumnWidth(1, 5000);
//				sheet.setColumnWidth(2, 5000);
//				sheet.setColumnWidth(3, 7000);
//				sheet.setColumnWidth(4, 7000);
//				sheet.setColumnWidth(5, 5000);
//				sheet.setColumnWidth(6, 5000);
//				sheet.setColumnWidth(7, 5000);
//				sheet.setColumnWidth(8, 5000);
//				sheet.setColumnWidth(9, 10000);
//
//				int rowCount = 0;
////			Row row = sheet.createRow(rowCount + 4);
//				sheet.createRow(rowCount + 4);
//				org.apache.poi.ss.usermodel.Sheet sheetAtt = workbook.getSheetAt(0);
//
//				System.out.println(sheetAtt);
//
//				Font font = workbook.createFont();
//				font.setFontHeightInPoints((short) 10);
//				font.setFontName("Arial");
//				font.setBold(true);
//				font.setItalic(true);
//
//				int lastRowNum = sheetAtt.getLastRowNum();
//				// XSSFRow row = sheetAt.getRow(lastRowNum);
//				System.out.println(lastRowNum);
//				Font fontt = workbook.createFont();
//				fontt.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
//				CellStyle style = workbook.createCellStyle();
//				style.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
//				style.setFillPattern(FillPatternType.DIAMONDS);
//				style.setAlignment(HorizontalAlignment.CENTER);
//				style.setFont(fontt);
//				CellStyle style2 = workbook.createCellStyle();
//				style2.setFont(font);
//				// Row header = sheet.createRow(0);
//
////		        header.createCell(0).setCellValue("ID");
////		        header.createCell(1).setCellValue("DATE");
////		        header.createCell(2).setCellValue("ABOUT");
////		        header.createCell(3).setCellValue("EMAIL");
////		        header.createCell(4).setCellValue("ENABLED");
////		        header.createCell(5).setCellValue("IMAGE_URL");
////		        header.createCell(6).setCellValue("NAME");
////		        header.createCell(7).setCellValue("PASSWORD");
////		        header.createCell(8).setCellValue("ROLE");
//
//				// create for excel file for create date heading on top
//
//				XSSFRow rowww = (XSSFRow) sheet.createRow(0);
//				XSSFCell celll = rowww.createCell(0);
//				celll.setCellValue("Date (dd/MM/yyyy):");
//				celll.setCellStyle(style2);
//				celll = rowww.createCell(1);
//				// create for excel file creation date
//				SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
//				String timeStamp = df.format(new Date());
//				celll.setCellValue(timeStamp);
//				celll.setCellStyle(style2);
//
//				rowww = (XSSFRow) sheet.createRow(1);
//				celll = rowww.createCell(0);
//				celll.setCellValue("Time(HH:mm:ss):");
//				celll.setCellStyle(style2);
//				celll = rowww.createCell(1);
//				// create for excel file creation date
//				SimpleDateFormat dff = new SimpleDateFormat("HH:mm:ss");
//				String timeStampp = dff.format(new Date());
//
////			SimpleDateFormat dfff = new SimpleDateFormat("dd/MM/yyyy.HH:mm:ss");
////			String timeStampppp = dfff.format(new Date());
//				celll.setCellValue(timeStampp);
//				celll.setCellStyle(style2);
//
//				CreationHelper createHelper = workbook.getCreationHelper();
//				CellStyle dateCellStyle = workbook.createCellStyle();
//				dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss")); // Set
//																												// the
//																												// desired
//																												// date
//																												// format
//
//				XSSFRow roww = (XSSFRow) sheet.createRow(3);
//				XSSFCell cell = roww.createCell(0);
//				cell.setCellValue("ID");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(1);
//				cell.setCellValue("LOGIN DATE TIME");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(2);
//				cell.setCellValue("LOGOUT DATE TIME");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(3);
//				cell.setCellValue("EMAIL");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(4);
//				cell.setCellValue("IP ADDRESS");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(5);
//				cell.setCellValue("IS SESSION EXPIRED");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(6);
//				cell.setCellValue("USERNAME");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(7);
//				cell.setCellValue("USER STATUS");
//				cell.setCellStyle(style);
//
//				cell = roww.createCell(8);
//				cell.setCellValue("SESSION ID");
//				cell.setCellStyle(style);
//
//				while (excel.hasNext()) {
//					CellStyle style1 = workbook.createCellStyle();
//					style1.setAlignment(HorizontalAlignment.CENTER);
//					UserLoginDateTime userDateTime = excel.next();
//					Row datarow = sheetAtt.createRow(++lastRowNum);
//					datarow.createCell(0).setCellValue(userDateTime.getId());
//					datarow.createCell(1).setCellValue(userDateTime.getLoginDateAndTime());
//					datarow.getCell(1).setCellStyle(dateCellStyle);
//					datarow.createCell(2).setCellValue(userDateTime.getLogoutDateAndTime());
//					datarow.getCell(2).setCellStyle(dateCellStyle);
//					datarow.createCell(3).setCellValue(userDateTime.getEmail());
//					datarow.createCell(4).setCellValue(userDateTime.getIpAddress());
//					datarow.createCell(5).setCellValue(userDateTime.is_session_interrupted());
//					datarow.createCell(6).setCellValue(userDateTime.getUsername());
//					datarow.createCell(7).setCellValue(userDateTime.isUser_status());
//					datarow.createCell(8).setCellValue(userDateTime.getSession_Id());
//					String st = (userDateTime.getId() + " " + userDateTime.getLoginDateAndTime() + " "
//							+ userDateTime.getLogoutDateAndTime() + " " + userDateTime.getEmail() + " "
//							+ userDateTime.getIpAddress() + " " + userDateTime.is_session_interrupted() + " "
//							+ userDateTime.getUsername() + " " + userDateTime.isUser_status() + " "
//							+ userDateTime.getSession_Id());
//					System.out.println(st);
//				}
////				 FileOutputStream fileOutputStream=new FileOutputStream(path,true);
//				workbook.write(fileOut);
//				fileOut.flush();
//				fileOut.close();
//				workbook.close();
//				return true;
//			}
//		} catch (Exception e) {
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = servicelayer.class;
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//			return false;
//		}
//	}
	
	public String generateExcel() throws IOException {
	    List<UserLoginDateTime> records = userLoginDao.findAll();
	    Workbook workbook = new XSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Login History");

	    // Set column widths
	    sheet.setColumnWidth(0, 4000);
	    sheet.setColumnWidth(1, 5000);
	    sheet.setColumnWidth(2, 5000);
	    sheet.setColumnWidth(3, 7000);
	    sheet.setColumnWidth(4, 7000);
	    sheet.setColumnWidth(5, 5000);
	    sheet.setColumnWidth(6, 5000);
	    sheet.setColumnWidth(7, 5000);
	    sheet.setColumnWidth(8, 5000);

	    // Create styles
	    Font headerFont = workbook.createFont();
	    headerFont.setFontHeightInPoints((short) 10);
	    headerFont.setFontName("Arial");
	    headerFont.setBold(true);
	    headerFont.setColor(IndexedColors.WHITE.getIndex());

	    CellStyle headerStyle = workbook.createCellStyle();
	    headerStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
	    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    headerStyle.setFont(headerFont);

	    CellStyle dateCellStyle = workbook.createCellStyle();
	    CreationHelper createHelper = workbook.getCreationHelper();
	    dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

	    // Create header row
	    Row headerRow = sheet.createRow(0);
	    String[] columns = {"Sno", "Employee ID", "Name", "Email", "Login Time", "Logout Time", "IP Address", "Is Session Interrupted", "Location"};

	    for (int i = 0; i < columns.length; i++) {
	        org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
	        cell.setCellValue(columns[i]);
	        cell.setCellStyle(headerStyle);
	    }

	    int rowNum = 1;
	    for (UserLoginDateTime record : records) {
	        Row row = sheet.createRow(rowNum++);

	        row.createCell(0).setCellValue(rowNum - 1);
	        row.createCell(1).setCellValue(record.getId());
	        row.createCell(2).setCellValue(record.getUsername());
	        row.createCell(3).setCellValue(record.getEmail());

	        org.apache.poi.ss.usermodel.Cell loginTimeCell = row.createCell(4);
	        loginTimeCell.setCellValue(record.getLoginDateAndTime());
	        loginTimeCell.setCellStyle(dateCellStyle);

	        org.apache.poi.ss.usermodel.Cell logoutTimeCell = row.createCell(5);
	        logoutTimeCell.setCellValue(record.getLogoutDateAndTime());
	        logoutTimeCell.setCellStyle(dateCellStyle);

	        row.createCell(6).setCellValue(record.getIpAddress());
	        row.createCell(7).setCellValue(record.is_session_interrupted());
	        row.createCell(8).setCellValue(record.getLocation());
	    }

	    // Use a temporary directory for storing the Excel file
	    Path tempDirectory = Files.createTempDirectory("excelFiles");
	    Path filePath = Paths.get(tempDirectory.toString(), "login_history.xlsx");

	    try (FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {
	        workbook.write(fileOut);
	    } finally {
	        workbook.close();
	    }

	    return filePath.toString();
	}
	
	public ByteArrayOutputStream exportUserLoginData() throws IOException {
        List<UserLoginDateTime> dataInsertExcelList = userLoginDao.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("USER_LOGIN_DATA");

        // Set column widths
        sheet.setColumnWidth(0, 4000);
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 7000);
        sheet.setColumnWidth(4, 7000);
        sheet.setColumnWidth(5, 5000);
        sheet.setColumnWidth(6, 5000);
        sheet.setColumnWidth(7, 5000);

        // Create styles
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setFontName("Arial");
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFont(headerFont);

        CellStyle dateCellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] columnHeaders = {
                "Employee ID", "LOGIN DATE TIME", "LOGOUT DATE TIME", "EMAIL", "IP ADDRESS",
                "IS SESSION EXPIRED", "USERNAME", "USER STATUS"
        };

        for (int i = 0; i < columnHeaders.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        // Populate data rows
        int rowNum = 1;
        for (UserLoginDateTime userLogin : dataInsertExcelList) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(userLogin.getId());
            org.apache.poi.ss.usermodel.Cell loginDateCell = row.createCell(1);
            loginDateCell.setCellValue(userLogin.getLoginDateAndTime());
            loginDateCell.setCellStyle(dateCellStyle);

            org.apache.poi.ss.usermodel.Cell logoutDateCell = row.createCell(2);
            logoutDateCell.setCellValue(userLogin.getLogoutDateAndTime());
            logoutDateCell.setCellStyle(dateCellStyle);

            row.createCell(3).setCellValue(userLogin.getEmail());
            row.createCell(4).setCellValue(userLogin.getIpAddress());
            row.createCell(5).setCellValue(userLogin.is_session_interrupted());
            row.createCell(6).setCellValue(userLogin.getUsername());
            row.createCell(7).setCellValue(userLogin.isUser_status());
        }

        workbook.write(out);
        workbook.close();
        return out;
    }
	
	public boolean update_profile(User user) {
		try {
			Optional<UserDetail> userDetail = userDetailDao.findById(user.getId());
			if (userDetail.isPresent()) {
				UserDetail userDetail2 = userDetail.get();
				userDetail2.setImage_Url(user.getImage_Url());
				userDetail2.setBank_account_holder_name(user.getBank_account_holder_name());
				userDetail2.setBank_account_number(user.getBank_account_number());
				userDetail2.setBank_name(user.getBank_name());
				userDetail2.setIfsc_code(user.getIfsc_code());
				userDetail2.setLaptop_brand(user.getLaptop_brand());
				userDetail2.setLaptop_serial_number(user.getLaptop_serial_number());
				userDetail2.setLaptop_id(user.getLaptop_id());
				userdao.save(user);
				userDetailDao.save(userDetail2);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return false;
		}
	}

	@Transactional
	public void emp_bank_profile_update(UserDetail userDetail, String current_user) {
	    System.out.println(userDetail.getBank_account_holder_name());
	    System.out.println(userDetail.getBank_account_number());
	    System.out.println(userDetail.getBank_name());
		Optional<UserDetail> userDetail2 = userDetailDao.findById(userDetail.getId());
		if(userDetail2.isPresent())
		{
			if(userDetail.getBank_account_holder_name().trim().isEmpty())
			{
		UserDetail userDetail3=userDetail2.get();
		userDetail3.setBank_name("NA");
		userDetail3.setBank_account_number(0);
		userDetail3.setBank_account_holder_name("NA");
		userDetail3.setIfsc_code("NA");
		Optional<User> user = userdao.findById(userDetail3.getId());
		User user1 = user.get();
		user1.setBank_name("NA");
		user1.setBank_account_number(0);
		user1.setBank_account_holder_name("NA");
		user1.setIfsc_code("NA");
		userDetailDao.save(userDetail3);
		userdao.save(user1);
			}
	else
	{
		UserDetail userDetail3=userDetail2.get();
		userDetail3.setBank_name(userDetail.getBank_name());
		userDetail3.setBank_account_number(userDetail.getBank_account_number());
		userDetail3.setBank_account_holder_name(userDetail.getBank_account_holder_name());
		userDetail3.setIfsc_code(userDetail.getIfsc_code());
		Optional<User> user = userdao.findById(userDetail3.getId());
		User user1 = user.get();
		user1.setBank_name(userDetail.getBank_name());
		user1.setBank_account_number(userDetail.getBank_account_number());
		user1.setBank_account_holder_name(userDetail.getBank_account_holder_name());
		user1.setIfsc_code(userDetail.getIfsc_code());
		userDetailDao.save(userDetail3);
		userdao.save(user1);			
	}
		}
	}
	
	@Transactional
	public void emp_update_profile(UserDetail userDetail, String CurrentUser) throws Exception {
		try {
			System.out.println(userDetail.getId());
			System.out.println(userDetail.getLaptop_brand());
			System.out.println(userDetail.getLaptop_id());
			System.out.println(userDetail.getLaptop_serial_number());
			Optional<UserDetail> userDetail2 = userDetailDao.findById(userDetail.getId());
			if (userDetail2.isPresent()) {
				System.out.println("USERDETAIL INPUT GET " + userDetail.getLaptop_brand()+" <<<<<<< "+userDetail.getBank_account_holder_name()+">>>");
//				if (userDetail.getLaptop_brand().trim().isEmpty()) {
				if (userDetail.getLaptop_brand().equals("NA")) {
					UserDetail userDetail3 = userDetail2.get();
					Optional<User> user = userdao.findById(userDetail3.getId());
					User user1 = user.get();
					userDetail3.setLaptop_assign_or_not(false);
					userDetail3.setLaptop_brand("NA");
					userDetail3.setLaptop_id("NA");
					userDetail3.setLaptop_serial_number("NA");
					userDetail3.setLaptop_assign_date(new Date());
					userDetail3.setWho_assign_laptop(CurrentUser);
					userDetail3.setLaptop_status("NOT ASSIGNED");
					userDetail3.setWho_assign_laptop_employee_id(user1.getId());
					user1.setLaptop_brand(userDetail3.getLaptop_brand());
					user1.setLaptop_id(userDetail3.getLaptop_id());
					user1.setLaptop_serial_number(userDetail3.getLaptop_serial_number());
					user1.setLaptop_assign_date(userDetail3.getLaptop_assign_date());
					userDetailDao.save(userDetail3);
					userdao.save(user1);
				} else {
					UserDetail userDetail3 = userDetail2.get();
					Optional<User> user = userdao.findById(userDetail3.getId());
					User user1 = user.get();
					userDetail3.setLaptop_assign_or_not(true);
					userDetail3.setLaptop_brand(userDetail.getLaptop_brand());
					userDetail3.setLaptop_id(userDetail.getLaptop_id());
					userDetail3.setLaptop_serial_number(userDetail.getLaptop_serial_number());
					userDetail3.setLaptop_assign_date(new Date());
					userDetail3.setWho_assign_laptop(CurrentUser);
					userDetail3.setWho_assign_laptop_employee_id(user1.getId());
					userDetail3.setLaptop_status("ASSIGNED");
					user1.setLaptop_brand(userDetail3.getLaptop_brand());
					user1.setLaptop_id(userDetail3.getLaptop_id());
					user1.setLaptop_serial_number(userDetail3.getLaptop_serial_number());
					user1.setLaptop_assign_date(userDetail3.getLaptop_assign_date());
					userDetailDao.save(userDetail3);
					userdao.save(user1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Double> performance(User user, HttpSession httpSession) {
		try {
			Optional<Performance> performance = performancedao.findById(user.getId());
			Performance performance2 = performance.get();
			List<Double> chartData = new ArrayList<>();// Example data
			httpSession.setAttribute("year", performance2.getYear());
			chartData.add(performance2.getJanuary());
			chartData.add(performance2.getFebruary());
			chartData.add(performance2.getMarch());
			chartData.add(performance2.getApril());
			chartData.add(performance2.getMay());
			chartData.add(performance2.getJune());
			chartData.add(performance2.getJuly());
			chartData.add(performance2.getAugust());
			chartData.add(performance2.getSeptember());
			chartData.add(performance2.getOctober());
			chartData.add(performance2.getNovember());
			chartData.add(performance2.getDecember());
			return chartData;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	@Transactional
	public void record_user_activity(User user, String functionality, String ip_address) {
		try {
			RecordActivity recordActivity = new RecordActivity();
			long count = record_activity_dao.count();
			System.out.println("COUNT " + count);
			if (count == 0) {
				recordActivity.setSno(1);
			} else {
				int sno = record_activity_dao.getLastId();
				System.out.println("LAST SNO" + sno);
				recordActivity.setSno(++sno);
			}
			recordActivity.setEmployee_id(user.getId());
			recordActivity.setEmployee_name(user.getUsername());
			recordActivity.setFunctionality(functionality);
			recordActivity.setIpAddress(ip_address);
			recordActivity.setDate(new Date());
			record_activity_dao.save(recordActivity);
//		return true;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
		}
	}

	public void login_record_save(User user, HttpSession session, String Ip_address, String location) throws InterruptedException {
		try {
			UserLoginDateTime userLoginDateTime = new UserLoginDateTime();
			int employee_login_record_count=userLoginDao.getLoginCount();
			if(employee_login_record_count>0)
			{
			int last_sno = userLoginDao.getLastId();
			userLoginDateTime.setSno(++last_sno);
			userLoginDateTime.setId(user.getId());
			userLoginDateTime.setEmail(user.getEmail());
			userLoginDateTime.setIpAddress(Ip_address);
			userLoginDateTime.setLocation(location);
			userLoginDateTime.setUser_status(true);
			user.setUser_status(true);
			Optional<UserDetail> userDetail1 = userDetailDao.findById(user.getId());
			UserDetail userDetail2 = userDetail1.get();
			String getSession = session.getId();
			if (getSession == null) {
				userLoginDateTime.setSession_Id("NOT AVAILABLE");
			} else {
				userLoginDateTime.setSession_Id(getSession);
			}
			user.setSession_Id(getSession);
			userDetail2.setUser_status(true);
			  // Get current time in IST
	        ZonedDateTime nowIST = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
	        // Format the ZonedDateTime for display
	        String formattedDate = nowIST.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss z"));
	        System.out.println("----------------------------------------INDIA ASIA/KOLKATA-------------------------------- " + formattedDate);

	        // Convert formattedDate back to Date object with IST time zone
	        Date loginDate = parseDateInIST(formattedDate);
	        userLoginDateTime.setLoginDateAndTime(loginDate);
			userLoginDateTime.setUsername(user.getUsername());
			userLoginDao.save(userLoginDateTime);
			userdao.save(user);
//		Thread.sleep(1000);
			userDetailDao.save(userDetail2);
			}
			else
			{
				userLoginDateTime.setSno(1);
				userLoginDateTime.setId(user.getId());
				userLoginDateTime.setEmail(user.getEmail());
				userLoginDateTime.setIpAddress(Ip_address);
				userLoginDateTime.setLocation(location);
				userLoginDateTime.setUser_status(true);
				user.setUser_status(true);
				Optional<UserDetail> userDetail1 = userDetailDao.findById(user.getId());
				UserDetail userDetail2 = userDetail1.get();
				String getSession = session.getId();
				if (getSession == null) {
					userLoginDateTime.setSession_Id("NOT AVAILABLE");
				} else {
					userLoginDateTime.setSession_Id(getSession);
				}
				user.setSession_Id(getSession);
				userDetail2.setUser_status(true);
				  // Get current time in IST
		        ZonedDateTime nowIST = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
		        // Format the ZonedDateTime for display
		        String formattedDate = nowIST.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss z"));
		        System.out.println("----------------------------------------INDIA ASIA/KOLKATA-------------------------------- " + formattedDate);

		        // Convert formattedDate back to Date object with IST time zone
		        Date loginDate = parseDateInIST(formattedDate);
		        userLoginDateTime.setLoginDateAndTime(loginDate);
				userLoginDateTime.setUsername(user.getUsername());
				userLoginDao.save(userLoginDateTime);
				userdao.save(user);
//			Thread.sleep(1000);
				userDetailDao.save(userDetail2);
				
			}
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	private Date parseDateInIST(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.parse(dateString);
    }
	
	public String downtime_satus(String server) {
		try {
			String status = downtime_Maintaince_Dao.server_status_check(server);
			return status;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	@Transactional
	public void disabled_server_down_permitted(String server_name) {
		try {
			downtime_Maintaince_Dao.update_server_status_down(server_name);
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public boolean check_server_status(String server) {
		try {
			boolean result = downtime_Maintaince_Dao.server_status_check_active_or_not(server);
			return result;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return false;
		}
	}

	@Transactional
	public void correct_login_record_table() {
		try {
			userLoginDao.updateuserstatusreset();
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public boolean sync_employee_employeedetail() {
		try {
			List<User> user = userdao.findAll();
			List<UserDetail> userDetails = userDetailDao.findAll();
			ListIterator<User> get_user_one_by_one = user.listIterator();
			ListIterator<UserDetail> get_userDetail_one_by_one = userDetails.listIterator();
			while (get_user_one_by_one.hasNext()) {
				while (get_userDetail_one_by_one.hasNext()) {
					User user1 = get_user_one_by_one.next();
					UserDetail userDetail = get_userDetail_one_by_one.next();
					int id = user1.getId();
					int id1 = userDetail.getId();
					if (id == id1) {
						String email = user1.getUsername();
						String email1 = userDetail.getUsername();
						if (email != email1) {
							userDetail.setEmail(email);
						}
						String user_phone = user1.getPhone();
						String userDetail_phone = userDetail.getPhone();
						if (user_phone != userDetail_phone) {
							userDetail.setPhone(user_phone);
						}
						boolean user_enabled = user1.isEnabled();
						boolean userDetail_enabled = userDetail.isEnabled();
						if (user_enabled != userDetail_enabled) {
							userDetail.setEnabled(user_enabled);
						}
						Date user_last_working_day = user1.getLastWorkingDay();
						Date userDetail_last_working_day = userDetail.getLastWorkingDay();
						if (user_last_working_day != userDetail_last_working_day) {
							userDetail.setLastWorkingDay(user_last_working_day);
						}
					}
				}
			}

			return true;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//			return "SomethingWentWrong";)
//				return "redirect:/swr";
//			}
//			else

			return false;
//			}

		}
	}

	@Transactional
	public void enabled_server_up_permitted(String server_name) {
		try {
			downtime_Maintaince_Dao.update_server_status_up(server_name);
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Transactional
	public void employee_login_user_status_sync_correction() {
		try {
			jobrunning("login_employeedetail_user_status_correct");
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Transactional
	public void check_garbage_dat_map_session_id() {
		try {
			// Create a java.util.Date object (current date and time)
			Date date = new Date();

			// Convert java.util.Date to LocalDateTime
			LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

			// Get minutes and seconds from LocalDateTime
//        int minutes = localDateTime.getMinute();
//        int seconds = localDateTime.getSecond();

			Set<Map.Entry<String, Date>> entrySet = EMSMAIN.session_map_data.entrySet();
			entrySet.forEach(entry -> {
				String key = entry.getKey();
				Date value = entry.getValue();
				LocalDateTime localDateTime1 = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				// Calculate the difference between the two LocalDateTime objects
				Duration duration = Duration.between(localDateTime1, localDateTime);
				System.out.println("SYSTEM TIME " + localDateTime);
				System.out.println("RECORD TIME " + localDateTime1);
				System.out.println("DURATION " + duration.toMinutes());
				if (duration.toMinutes() >= 5 && EMSMAIN.session_map_data.containsKey(key)) {
					EMSMAIN.session_map_data.remove(key);
					System.out.println(EMSMAIN.session_map_data);
				}
			});
			jobrunning("remove_garbage_data_session_id");
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("remove_garbage_data_session_id");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Transactional
	public void validate_home_captcha() {
		try {
			Date date = new Date();
			LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			Set<Map.Entry<String, Date>> entrySet = EMSMAIN.captcha_validate_map.entrySet();
//		entrySet.forEach(entry -> {
//		String Captchaa=entry.getKey();
//		Date captcha_valid_or_not=entry.getValue();
//		LocalDateTime localDateTime1 = captcha_valid_or_not.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//		Duration duration=Duration.between(localDateTime1, localDateTime);
//		if(duration.toMinutes() >=1)
//		{
//			EMSMAIN.captcha_validate_map.remove(Captchaa);
//		}
//		});

			for (Map.Entry<String, Date> entry : entrySet) {
				String Captchaa = entry.getKey();
				Date captcha_valid_or_not = entry.getValue();
				LocalDateTime localDateTime1 = captcha_valid_or_not.toInstant().atZone(ZoneId.systemDefault())
						.toLocalDateTime();
				Duration duration = Duration.between(localDateTime1, localDateTime);
				if (duration.toMinutes() >= 5) {
					EMSMAIN.captcha_validate_map.remove(Captchaa);
				}
			}
			jobrunning("Captcha Validate");
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("Captcha Validate");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Transactional
	public void validate_otp() {
		try {
			Date date = new Date();
			LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			Set<Map.Entry<Integer, Date>> entrySet = EMSMAIN.OTP_validate_map.entrySet();
			for (Map.Entry<Integer, Date> entry : entrySet) {
				Integer otp = entry.getKey();
				Date captcha_valid_or_not = entry.getValue();
				LocalDateTime localDateTime1 = captcha_valid_or_not.toInstant().atZone(ZoneId.systemDefault())
						.toLocalDateTime();
				Duration duration = Duration.between(localDateTime1, localDateTime);
				if (duration.toMinutes() >= 5) {
					EMSMAIN.OTP_validate_map.remove(otp);
				}
			}
			jobrunning("OTP Validate");
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("OTP Validate");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public User get_user(String email) {
		try {
			User user = userdao.getUserByUserName(email);
			return user;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	@Transactional
	public void update_team_by_team_id(UserDetail userDetail, String team_id, String team_desc) {
		try {
			System.out.println("USERDETAIL " + userDetail);
			userDetail.setTeam(team_id);
			userDetail.setTeam_desc(team_desc);
			userDetail.setEmployeeOnBench(false);
			userDetailDao.save(userDetail);
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public void processing_payment(Order order, Principal principal) {
		try {
			int amt = 0;
			Payment_Order_Info order_Info = new Payment_Order_Info();
			Optional<User> user = userdao.findByUserName(principal.getName());
			int last_id = orderDao.countt();
			if (last_id > 0) {
				order_Info.setSno(++last_id);
			} else {
				order_Info.setSno(1);
			}
			User user1 = user.get();
			amt = order.get("amount");
			int paise_to_rupee = amt / 100;
			order_Info.setAmount(paise_to_rupee);
			order_Info.setOrderId(order.get("id"));
			order_Info.setPaymentId(null);
			order_Info.setStatus("created");
			order_Info.setReceipt(order.get("receipt"));
			order_Info.setEmail(user1.getEmail());
			order_Info.setPhone(user1.getPhone());
			order_Info.setCompany(user1.getCompany());
			order_Info.setSystem_date_and_time(new Date());
			order_Info.setCompany_id(user1.getCompany_id());
			order_Info.setLicense_number(null);
			orderDao.save(order_Info);
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}
	
	@Transactional
	public boolean update_payment(User user,@RequestBody Map<String, Object> data)
	{
		Payment_Order_Info payment_Order_Info = orderDao.findByOrderId(data.get("order_id").toString());
		payment_Order_Info.setPaymentId(data.get("payment_id").toString());
		payment_Order_Info.setStatus(data.get("status").toString());
		// Generate a random UUID
		UUID uuid = UUID.randomUUID();

		// Convert UUID to string and format it as needed
		String licenseNumber = uuid.toString().toUpperCase().replace("-", "");

		// Optionally, add some custom formatting or prefixes
		String formattedLicenseNumber = "LICNO" + licenseNumber;
		payment_Order_Info.setLicense_number(formattedLicenseNumber);
		payment_Order_Info.setSubscription_start_date(new Date());
		Instant i = Instant.now();
		Instant i1 = i.plus(Duration.ofDays(1));
		Date subscriptionExpiryDate = Date.from(i1);
		payment_Order_Info.setSubscription_expiry_date(subscriptionExpiryDate);
		payment_Order_Info.setLicense_status("ACTIVE");
		System.out.println("USER COMPANY ID" + user.getCompany_id());
		update_enable_user_after_success_payment(user.getCompany_id());
		SubscriptionPlans subscriptionPlans=findSubscriptionPlans();
		float gst=subscriptionPlans.getGst() * 100;
		String gst_no=Float.toString(gst);
		payment_Order_Info.setDiscount(subscriptionPlans.getDiscount());
		payment_Order_Info.setTax(gst_no+'%');
		Optional<SubscriptionPlans> subscriptionPlansOptional = subscriptionPlansDao.getAllPlans();
		SubscriptionPlans subscriptionPlans2 = subscriptionPlansOptional.get();
		payment_Order_Info.setGst_amount(payment_Order_Info.getAmount() * subscriptionPlans2.getGst());
		float without_gst_amount=payment_Order_Info.getAmount() - payment_Order_Info.getGst_amount();
		payment_Order_Info.setAmount_without_gst(without_gst_amount);
		CompanyInfo companyInfo = findCompanyInfo();
		payment_Order_Info.setGst_no(companyInfo.getGst_no());
		orderDao.save(payment_Order_Info);
		try {
			generateAndSendInvoice(payment_Order_Info,subscriptionPlans,companyInfo,user,formattedLicenseNumber);
		} catch (IOException e) {
			jobDao.getJobRunningTimeInterrupted("disbaled_expired_plan_users");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return false;
		} catch (MessagingException e) {
			jobDao.getJobRunningTimeInterrupted("disbaled_expired_plan_users");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return false;
		}
		System.out.println(data);
		return true;
	}

	public CompanyInfo fetch_date_from_company_records(String company_id) {
		try {
			CompanyInfo companyInfo = new CompanyInfo();
			companyInfo = company_dao.getCompanyByCompanyId(company_id);
			return companyInfo;
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("disbaled_expired_plan_users");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	@Transactional
	public void update_enable_user_after_success_payment(String company_id) {
		try {
			userdao.update_user_enabled_after_success_payment(company_id);
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	@Transactional
	public void disbaled_expired_plan_users(String jobname) {
		try {
			List<Payment_Order_Info> order = orderDao.findAll();
			ListIterator<Payment_Order_Info> orders_iterate = order.listIterator();
			while (orders_iterate.hasNext()) {
				Payment_Order_Info payment_Order_Info = orders_iterate.next();
				if (payment_Order_Info.getStatus().equalsIgnoreCase("paid")) {
					int get_output = orderDao.check_users_subscription_plan(payment_Order_Info.getCompany_id());
					System.out.println("BEFORE WHILE LOOP " + get_output + " " + payment_Order_Info.getCompany_id());
					if (get_output > 0) {
						System.out.println("AFTER WHILE LOOP " + get_output + " " + payment_Order_Info.getCompany_id());
						userdao.disbaled_expired_plan_users(payment_Order_Info.getCompany_id());
						orderDao.expired_license_status(payment_Order_Info.getCompany_id());
					} else {
						continue;
					}
				} else {
					continue;
				}
			}
			jobrunning("disbaled_expired_plan_users");
		}

		catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("disbaled_expired_plan_users");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}

	public User findByUsername(String email) {
		try {
			Optional<User> user = userdao.findByUserName(email);
			User user1 = user.get();
			return user1;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	public Payment_Order_Info findOrderByCompanyId(String transaction_id) {
		try {
			Optional<Payment_Order_Info> payment_Order_Info = orderDao.findOrderByTransactionId(transaction_id);
			Payment_Order_Info payment_Order_Info2 = payment_Order_Info.get();
			return payment_Order_Info2;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	public CompanyInfo findCompanyInfo() {
		try {
			CompanyInfo companyInfo = company_dao.getCompany();
			return companyInfo;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	public SubscriptionPlans findSubscriptionPlans() {
		try {
			Optional<SubscriptionPlans> subscriptionPlans = subscriptionPlansDao.getAllPlans();
			SubscriptionPlans subscriptionPlans2 = subscriptionPlans.get();
			return subscriptionPlans2;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	public List<Payment_Order_Info> transaction_history(String company_id) {
		try {
			List<Payment_Order_Info> payment_Order_Infos = orderDao.transactionHistoryFindByCompanyId(company_id);
			return payment_Order_Infos;
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	public SubscriptionPlans getAllPlans() {
		try {
			Optional<SubscriptionPlans> subscriptionPlans = subscriptionPlansDao.getAllPlans();
			SubscriptionPlans subscriptionPlans2 = subscriptionPlans.get();
			return subscriptionPlans2;
		} catch (Exception e) {
			jobDao.getJobRunningTimeInterrupted("expired_license_status");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
			return null;
		}
	}

	@Transactional
	public void expired_license_status() {
		try {
			int order_count=orderDao.countt();
			if(order_count > 0)
			{
				List<Payment_Order_Info> info= orderDao.findAll();
				for(Payment_Order_Info order_Info : info)
				{
					String company_id=order_Info.getCompany_id();
					int expire_order_count=orderDao.check_users_subscription_plan(company_id);
					if(expire_order_count>0)
					{
						orderDao.expired_license_status(company_id);
						userdao.disbaled_expired_plan_users(company_id);
					}
				}
			jobrunning("expired_license_status");
			}
		} catch (Exception e) {
		jobDao.getJobRunningTimeInterrupted("expired_license_status");
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

		}
	}


	public void generateAndSendInvoice(Payment_Order_Info payment, SubscriptionPlans subscriptionPlans,
			CompanyInfo companyInfo, User user,String formattedLicenseNumber) throws IOException, MessagingException {
		String invoicePath = "C:\\Users\\ayush.gupta\\Documents\\Invoice Records\\invoice_" + payment.getPaymentId() + ".pdf";
		generatePdfInvoice(invoicePath, payment, subscriptionPlans, companyInfo, user);
//	        sendInvoiceEmail("customer@example.com", "Your Invoice", "Please find attached your invoice.", invoicePath);
		try {
			EMSMAIN.payment_success_email_alert.put(user.getEmail(), user.getUsername());
			EMSMAIN.license_number.put(user.getEmail(), formattedLicenseNumber);
			EMSMAIN.license_status.put(user.getEmail(), payment.getLicense_status());
			EMSMAIN.payment_time.put(user.getEmail(), payment.getSystem_date_and_time());
			EMSMAIN.license_payment_status.put(user.getEmail(),payment.getStatus());
			EMSMAIN.payment_invoice_email.put(user.getEmail(), invoicePath);
		} catch (Exception e) {
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
			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
		}

	}

	private void generatePdfInvoice(String filePath, Payment_Order_Info payment, SubscriptionPlans subscriptionPlans,
			CompanyInfo company_Info, User user) throws IOException {
		PdfWriter writer = new PdfWriter(filePath);
		com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
		Document document = new Document(pdf);
		// Load fonts
		PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
		PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

		// Format the date
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String formattedDate = dateFormat.format(payment.getSystem_date_and_time());

		// Title
		Paragraph title = new Paragraph("Invoice").setFont(boldFont).setFontSize(20)
				.setTextAlignment(TextAlignment.CENTER).setMarginBottom(20);
		document.add(title);
		
		// Create an array with the characters of the text
		char[] textChars = "www.ems.com".toCharArray();
     // Define the colors for each character
        Color[] colors = {
            new DeviceRgb(66, 133, 244),   // Blue
            new DeviceRgb(219, 68, 55),    // Red
            new DeviceRgb(244, 180, 0),    // Yellow
            new DeviceRgb(66, 133, 244),   // Blue
            new DeviceRgb(15, 157, 88),    // Green
            new DeviceRgb(219, 68, 55),    // Red
            new DeviceRgb(66, 133, 244),   // Blue
            new DeviceRgb(219, 68, 55),    // Red
            new DeviceRgb(244, 180, 0)     // Yellow
        };
        Paragraph title4 = new Paragraph();
     // Add each character with its corresponding color
        for (int i = 0; i < textChars.length; i++) {
            // Create a Text object for each character
            Text coloredText = new Text(String.valueOf(textChars[i]))
                    .setFontColor(colors[i % colors.length]); // Rotate through the colors
            
            // Add the Text object to the Paragraph
            title4.add(coloredText);
        }
	title4.setFont(boldFont).setFontSize(26)
				.setTextAlignment(TextAlignment.LEFT).setMarginBottom(10);
		document.add(title4);
		
		// Company Info
		Paragraph title1 = new Paragraph("Billed From").setFont(boldFont).setFontSize(12)
				.setTextAlignment(TextAlignment.LEFT).setMarginBottom(20);
		document.add(title1);
		Paragraph BilledFrom = new Paragraph(payment.getCompany() + "\n" + user.getBase_location() + "\n"
				+ payment.getEmail() + "\n" + payment.getPhone()).setFont(font).setFontSize(12)
				.setTextAlignment(TextAlignment.LEFT).setMarginBottom(12);
		document.add(BilledFrom);

		Paragraph title2 = new Paragraph("Billed To").setFont(boldFont).setFontSize(12)
				.setTextAlignment(TextAlignment.LEFT).setMarginBottom(9);
		document.add(title2);
		Paragraph BilledTo = new Paragraph(company_Info.getCompany_name() + "\n" + company_Info.getCompany_address()
				+ "\n" + company_Info.getCompany_phone() + "\n" + company_Info.getCompany_email()).setFont(font)
				.setFontSize(12).setTextAlignment(TextAlignment.LEFT).setMarginBottom(12);
		document.add(BilledTo);

		// Invoice details table
		Table invoiceTable = new Table(UnitValue.createPercentArray(new float[] { 1, 2 })).useAllAvailableWidth()
				.setMarginBottom(12);

		invoiceTable.addCell(new Cell().add(new Paragraph("Payment ID:").setFont(boldFont).setFontSize(12))
				.setBorder(Border.NO_BORDER));
		invoiceTable.addCell(new Cell().add(new Paragraph(payment.getPaymentId()).setFont(font).setFontSize(12))
				.setBorder(Border.NO_BORDER));

		invoiceTable.addCell(new Cell().add(new Paragraph("Order ID:").setFont(boldFont).setFontSize(12))
				.setBorder(Border.NO_BORDER));
		invoiceTable.addCell(new Cell().add(new Paragraph(payment.getOrderId()).setFont(font).setFontSize(12))
				.setBorder(Border.NO_BORDER));

		invoiceTable.addCell(new Cell().add(new Paragraph("License Number:").setFont(boldFont).setFontSize(12))
				.setBorder(Border.NO_BORDER));
		invoiceTable.addCell(new Cell().add(new Paragraph(payment.getLicense_number()).setFont(font).setFontSize(12))
				.setBorder(Border.NO_BORDER));

		invoiceTable.addCell(new Cell().add(new Paragraph("License Status:").setFont(boldFont).setFontSize(12))
				.setBorder(Border.NO_BORDER));
		invoiceTable.addCell(new Cell().add(new Paragraph(payment.getLicense_status()).setFont(font).setFontSize(12))
				.setBorder(Border.NO_BORDER));

		invoiceTable.addCell(new Cell().add(new Paragraph("Receipt:").setFont(boldFont).setFontSize(12))
				.setBorder(Border.NO_BORDER));
		invoiceTable.addCell(new Cell().add(new Paragraph(payment.getReceipt()).setFont(font).setFontSize(12))
				.setBorder(Border.NO_BORDER));

		// Status with uppercase and green text
		invoiceTable.addCell(
				new Cell().add(new Paragraph("Status:").setFont(boldFont).setFontSize(12)).setBorder(Border.NO_BORDER));
		invoiceTable.addCell(new Cell().add(new Paragraph(payment.getStatus().toUpperCase()).setFont(font)
				.setFontSize(12).setFontColor(ColorConstants.GREEN)).setBorder(Border.NO_BORDER));

		invoiceTable.addCell(new Cell().add(new Paragraph("Payment Date/Time:").setFont(boldFont).setFontSize(12))
				.setBorder(Border.NO_BORDER));
		invoiceTable.addCell(
				new Cell().add(new Paragraph(formattedDate).setFont(font).setFontSize(12)).setBorder(Border.NO_BORDER));

		document.add(invoiceTable);

		// Divider
		document.add(new Paragraph("\n"));

		/*
		 * Cell invoiceDetailsCell = new Cell() .add(new
		 * Paragraph("Invoice No:").setFont(boldFont).setFontSize(16).setMarginBottom(10
		 * )) .add(new Paragraph(payment.getReceipt()).setFont(font).setFontSize(12))
		 * .add(new
		 * Paragraph("Invoice Date:").setFont(boldFont).setFontSize(16).setMarginTop(10)
		 * ) .add(new Paragraph(formattedDate).setFont(font).setFontSize(12)) .add(new
		 * Paragraph("Order No:").setFont(boldFont).setFontSize(16).setMarginTop(10))
		 * .add(new Paragraph(payment.getOrderId()).setFont(font).setFontSize(12))
		 * .add(new
		 * Paragraph("Payment ID:").setFont(boldFont).setFontSize(16).setMarginTop(10))
		 * .add(new Paragraph(payment.getPaymentId()).setFont(font).setFontSize(12))
		 * .setTextAlignment(TextAlignment.RIGHT) .setBorder(Border.NO_BORDER);
		 * 
		 * billingTable.addCell(billedToCell); billingTable.addCell(invoiceDetailsCell);
		 * document.add(billingTable);
		 */
		// Divider
		document.add(new Paragraph("\n"));

		// Order Summary Table
		Table orderSummaryTable = new Table(UnitValue.createPercentArray(new float[] { 1, 4, 2, 2, 1, 2 }))
				.useAllAvailableWidth().setMarginBottom(20);

		orderSummaryTable.addHeaderCell(new Cell().add(new Paragraph("No.").setFont(boldFont).setFontSize(12))
				.setBackgroundColor(ColorConstants.LIGHT_GRAY));
		orderSummaryTable.addHeaderCell(new Cell().add(new Paragraph("Item").setFont(boldFont).setFontSize(12))
				.setBackgroundColor(ColorConstants.LIGHT_GRAY));
		orderSummaryTable.addHeaderCell(new Cell().add(new Paragraph("Price (INR)").setFont(boldFont).setFontSize(12))
				.setBackgroundColor(ColorConstants.LIGHT_GRAY));
		orderSummaryTable.addHeaderCell(new Cell().add(new Paragraph("Discount (INR)").setFont(boldFont).setFontSize(12))
				.setBackgroundColor(ColorConstants.LIGHT_GRAY));
		orderSummaryTable.addHeaderCell(new Cell().add(new Paragraph("Tax (GST)").setFont(boldFont).setFontSize(12))
				.setBackgroundColor(ColorConstants.LIGHT_GRAY));
//		orderSummaryTable.addHeaderCell(new Cell().add(new Paragraph("Quantity").setFont(boldFont).setFontSize(12))
//				.setBackgroundColor(ColorConstants.LIGHT_GRAY));
		orderSummaryTable.addHeaderCell(new Cell().add(new Paragraph("Total").setFont(boldFont).setFontSize(12))
				.setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.RIGHT));

		orderSummaryTable.addCell(new Cell().add(new Paragraph("01").setFont(font).setFontSize(12)));
		orderSummaryTable.addCell(new Cell().add(new Paragraph(
				"EMS SUBSCRIPTION\nValidity: " + subscriptionPlans.getPlan_description() + "\nLicense Number: "
						+ payment.getLicense_number() + "\nLic. Issue Date: " + payment.getSubscription_start_date()
						+ "\nLic. End Date: " + payment.getSubscription_expiry_date()+""
								+ "\nQuantity: 1")
				.setFont(font).setFontSize(12)));
		float amt_without_gst=payment.getAmount()-payment.getGst_amount();
		float discount=payment.getDiscount();
		float gst_amount=payment.getGst_amount();
		// Formatted values
        String formattedAmtWithoutDiscount = String.format("%.2f", amt_without_gst);
        String formattedDiscount = String.format("%.2f", discount);
        String formattedGstAmount = String.format("%.2f", gst_amount);
		orderSummaryTable.addCell(new Cell().add(new Paragraph("₹" + formattedAmtWithoutDiscount).setFont(font).setFontSize(12)));
		orderSummaryTable.addCell(new Cell().add(new Paragraph("₹ -" +formattedDiscount)).setFont(font).setFontSize(12));
		orderSummaryTable.addCell(new Cell().add(new Paragraph("₹" + payment.getTax() + "   "+formattedGstAmount)).setFont(font).setFontSize(12));
//		orderSummaryTable.addCell(new Cell().add(new Paragraph("1").setFont(font).setFontSize(12)));
		orderSummaryTable.addCell(new Cell().add(new Paragraph("₹" + payment.getAmount()).setFont(font).setFontSize(12))
				.setTextAlignment(TextAlignment.RIGHT));

		orderSummaryTable.addCell(new Cell(1, 5).add(
				new Paragraph("Sub Total").setFont(boldFont).setFontSize(12).setTextAlignment(TextAlignment.RIGHT)));
		orderSummaryTable.addCell(new Cell().add(new Paragraph("₹" + payment.getAmount()).setFont(font).setFontSize(12))
				.setTextAlignment(TextAlignment.RIGHT));

		orderSummaryTable.addCell(new Cell(1, 5).add(new Paragraph("Discount").setFont(boldFont).setFontSize(12)
				.setTextAlignment(TextAlignment.RIGHT)));
		if(subscriptionPlans.getDiscount()==0)
		{
			orderSummaryTable
			.addCell(new Cell().add(new Paragraph("₹ NA ").setFont(font).setFontSize(12))
					.setTextAlignment(TextAlignment.RIGHT));
		}
		else
		{
		orderSummaryTable
				.addCell(new Cell().add(new Paragraph("₹" + subscriptionPlans.getDiscount()).setFont(font).setFontSize(12))
						.setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
		}
		orderSummaryTable.addCell(new Cell(1, 5)
				.add(new Paragraph("Total").setFont(boldFont).setFontSize(12).setTextAlignment(TextAlignment.RIGHT)));
		orderSummaryTable.addCell(new Cell().add(new Paragraph("₹" + payment.getAmount()).setFont(font).setFontSize(12))
				.setTextAlignment(TextAlignment.RIGHT));

		document.add(orderSummaryTable);
		
		// Divider
	    document.add(new Paragraph("\n"));

	    // Terms and Conditions
	    Paragraph termsTitle = new Paragraph("Terms and Conditions")
	            .setFont(boldFont)
	            .setFontSize(14)
	            .setTextAlignment(TextAlignment.LEFT)
	            .setMarginBottom(10);
	    document.add(termsTitle);

	    Div termsDiv = new Div();
	    termsDiv.add(new Paragraph("1. Payment Invoice valid for 1 Day.").setFont(font).setFontSize(12));
	    termsDiv.add(new Paragraph("2. Late payments may be subject to additional charges.").setFont(font).setFontSize(12));
	    termsDiv.add(new Paragraph("3. Please contact support for any discrepancies in your invoice.").setFont(font).setFontSize(12));
	    termsDiv.add(new Paragraph("4. All sales are final. No refunds.").setFont(font).setFontSize(12));
	    termsDiv.add(new Paragraph("5. This invoice is subject to the terms and conditions of our service agreement.").setFont(font).setFontSize(12));
	    document.add(termsDiv);

		document.close();
	} 
	
	public void save_null_value_laptop_assign_by_IT(UserDetail detail)
	{
		userDetailDao.save(detail);
	}
	
	public void insert_Job_First_Time()
	{
		List<Job> job=jobDao.findAll();
		List<String> job_list=new ArrayList<>();
	   if(job==null)
		   {
		 job_list.add("Account_Locked_job");
		 job_list.add("Login_Delete_Job");
		 job_list.add("Is_Enabled_Job");
		 job_list.add("Is_Disabled_Inactive_User_Job");
		 job_list.add("Password_FailedAttempt_Reset");
		 job_list.add("Update_User_Inactive_Status");
		 job_list.add("get_user_status");
		 job_list.add("delete_old_error_log");
		 job_list.add("login_employeedetail_user_status_correct");
		 job_list.add("remove_garbage_data_session_id");
		 job_list.add("Captcha Validate");
		 job_list.add("OTP Validate");
		 job_list.add("failed_attempt_alert");
		 job_list.add("success_attempt_alert");
		 job_list.add("admin_otp_sent_verification");
		 job_list.add("seperation_email_sent");
		 job_list.add("team_email_sent");
		 job_list.add("disbaled_expired_plan_users");
		 job_list.add("expired_license_status");
		 job_list.add("team_email_sent");
		 job_list.add("forgot_otp_sent_verification");
		 job_list.add("team_email_sent");
		 job_list.add("payment_success_email_alert");
		 for(String c : job_list)
		 {
			 Job job1=new Job();
			int count=jobDao.getJobCount();
			if(count==0)
			{
				job1.setId(1);
				job1.setJob_description(c);
				job1.setJob_active_or_not("Y");
				jobDao.save(job1);
			}
			else
			{
				int lastid=jobDao.getJobLastId();
				job1.setId(++lastid);
				job1.setJob_description(c);
				job1.setJob_active_or_not("Y");
				jobDao.save(job1);
			}
		 }
		   }
//		Login_Delete_Job
//		Is_Enabled_Job
//		Is_Disabled_Inactive_User_Job
//		Password_FailedAttempt_Reset
//		Update_User_Inactive_Status
//		get_user_status
//		delete_old_error_log
//		login_employeedetail_user_status_correct
//		remove_garbage_data_session_id
//		Captcha Validate
//		OTP Validate
//		failed_attempt_alert
//		success_attempt_alert
//		downtime_maintaince
//		admin_otp_sent_verification
//		seperation_email_sent
//		team_email_sent
//		disbaled_expired_plan_users
//		expired_license_status
//		forgot_otp_sent_verification
//		payment_success_email_alert
	}
}
