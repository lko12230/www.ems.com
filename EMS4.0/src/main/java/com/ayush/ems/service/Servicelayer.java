package com.ayush.ems.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
//import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
//import org.apache.poi.ss.usermodel.HorizontalAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import com.ayush.ems.config.NotificationListener;
import com.ayush.ems.config.NumberToWordsConverter;
import com.ayush.ems.dao.AdminDao;
import com.ayush.ems.dao.ArchiveDisabledUserDao;
import com.ayush.ems.dao.ArchiveDisabledUserDetailDao;
import com.ayush.ems.dao.ArchiveLoginDao;
import com.ayush.ems.dao.ArchiveOldOrdersDao;
import com.ayush.ems.dao.AttendanceDao;
import com.ayush.ems.dao.CompanyDao;
import com.ayush.ems.dao.ContactDao;
import com.ayush.ems.dao.DowntimeMaintaince_Dao;
import com.ayush.ems.dao.ErrorLogDao;
import com.ayush.ems.dao.GatePassDao;
import com.ayush.ems.dao.HolidayDao;
import com.ayush.ems.dao.JobDao;
import com.ayush.ems.dao.LaptopDao;
import com.ayush.ems.dao.LeavePolicyDao;
import com.ayush.ems.dao.NSqlConfigDao;
import com.ayush.ems.dao.NotificationArchiveDao;
import com.ayush.ems.dao.NotificationDao;
import com.ayush.ems.dao.OrderDao;
import com.ayush.ems.dao.PerformanceDao;
import com.ayush.ems.dao.RecordActivityDao;
import com.ayush.ems.dao.SalarySlipDao;
import com.ayush.ems.dao.StageUserDao;
import com.ayush.ems.dao.SubscriptionPlanDao;
import com.ayush.ems.dao.TaskDao;
import com.ayush.ems.dao.TeamDao;
import com.ayush.ems.dao.UploadHistoryReportDao;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.dao.UserDetailDao;
import com.ayush.ems.dao.UserLoginDao;
import com.ayush.ems.entities.Admin;
import com.ayush.ems.entities.ArchiveDisabledUser;
import com.ayush.ems.entities.ArchiveDisabledUserDetail;
import com.ayush.ems.entities.ArchiveOldOrders;
import com.ayush.ems.entities.Attendance;
import com.ayush.ems.entities.CompanyInfo;
import com.ayush.ems.entities.Contact;
import com.ayush.ems.entities.Error_Log;
import com.ayush.ems.entities.GatePassGenerator;
import com.ayush.ems.entities.Holiday;
import com.ayush.ems.entities.Job;
import com.ayush.ems.entities.LoginDataArchive;
import com.ayush.ems.entities.NSqlConfig;
import com.ayush.ems.entities.Notification;
import com.ayush.ems.entities.NotificationArchive;
import com.ayush.ems.entities.Payment_Order_Info;
import com.ayush.ems.entities.Performance;
import com.ayush.ems.entities.RecordActivity;
import com.ayush.ems.entities.SalarySlip;
import com.ayush.ems.entities.SubscriptionPlans;
import com.ayush.ems.entities.TaskPerformance;
import com.ayush.ems.entities.Tasks;
import com.ayush.ems.entities.Team;
import com.ayush.ems.entities.UploadHistory;
import com.ayush.ems.entities.User;
import com.ayush.ems.entities.UserDetail;
import com.ayush.ems.entities.UserLoginDateTime;
import com.ayush.ems.entities.Laptop;
import com.ayush.ems.entities.stage_user;
import com.ayush.ems.helper.FileToMultipartFile;
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.FileContent;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.util.Value;
import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
//import com.google.auth.http.HttpCredentialsAdapter;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.auth.oauth2.UserCredentials;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.noise.StraightLineNoiseProducer;
import cn.apiclub.captcha.text.producer.DefaultTextProducer;

@SuppressWarnings("deprecation")
@Service
public class Servicelayer {

	@Autowired
	private UserLoginDao userLoginDao;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private EmailService emailService;
	// for register
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private UserDetailDao userDetailDao;
	@Autowired
	private UserDao userdao;
	@Autowired
	private JobDao jobDao;
	@Autowired
	private  ErrorLogDao error_log_dao;
	@Autowired
	private PerformanceDao performancedao;
	@Autowired
	private RecordActivityDao record_activity_dao;
	@Autowired
	private DowntimeMaintaince_Dao downtime_Maintaince_Dao;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private CompanyDao company_dao;
	@Autowired
	private SubscriptionPlanDao subscriptionPlansDao;
	@Autowired
	private PaymentSucessEmailService paymentSucessEmailService;
	@Autowired
	private ArchiveLoginDao archiveLoginDao;
	@Autowired
	private ArchiveDisabledUserDao archiveDisabledUserDao;
	@Autowired
	private ArchiveDisabledUserDetailDao archiveDisabledUserDetailDao;
	@Autowired
	private StageUserDao stageUserDao;
	@Autowired
	private ArchiveOldOrdersDao archiveOldOrdersDao;
	@Autowired
	private NSqlConfigDao nSqlConfigDao;
	@Autowired
	private ContactDao contactDao;
	@Autowired
	private AttendanceDao attendanceDao;
	@Autowired
	private LaptopDao laptopDao;
	@Autowired
	private GatePassDao gatePassDao;
	@Autowired
	private GatePassEmailService gatePassEmailService;
	@Autowired
	private TeamDao teamDao;
	@Autowired
	private TeamEmailService teamEmailService;
	@Autowired
	private TaskDao taskDao;
	@Autowired
	private TaskEmailService taskEmailService;
	@Autowired
	private NotificationDao notificationDao;
	@Autowired
	private ListEmailService listEmailService;
	@Autowired
	private NotificationArchiveDao notificationArchiveDao;
	@Autowired
	private HolidayDao holidayDao;
	@Autowired
	private LeavePolicyDao leavePolicyDao;
	@Autowired
	private SalarySlipEmailService salarySlipEmailService;
	@Autowired
	private SalarySlipDao salarySlipDao;
	@Autowired
	private UploadHistoryReportDao uploadHistoryReportDao;
	@Autowired
	private GoogleDriveService googleDriveService;

	@Transactional
	public stage_user register(stage_user user, String ipaddress, String location) throws Exception {
		try {
			System.out.println("User DOB: " + user.getDob());
			System.out.println("User Email: " + user.getUsername());

			if (isValidDOB(user.getDob())) {
				System.out.println("Valid DOB");
			} else {
				System.out.println("Employee must be at least 18 years old.");
				throw new Exception("Employee must be at least 18 years old.");
			}

			String admin_id = user.getAddwhoAdminId();
			System.out.println("AD ID " + admin_id);
			int admin_cast_to_int = Integer.parseInt(admin_id);
			Optional<Admin> get_admin = adminDao.findByAdminId(admin_cast_to_int);
			Admin get_admin_data = get_admin.get();
			if (!get_admin_data.getCompany_id().equals(user.getCompanyId())
					&& !get_admin_data.isAllowMultipleCompany()) {
				throw new Exception("Registration failed: The provided company ID (" + user.getCompanyId()
						+ ") does not match the assigned company ID (" + get_admin_data.getCompany_id() + ").");
			}

			// Calendar calendar = Calendar.getInstance();
//			int currentYear = calendar.get(Calendar.YEAR);

			// Check for existing user by email and phone
			Optional<stage_user> existingUser = stageUserDao.findByUserNameAndPhone(user.getEmail(), user.getPhone());
			Optional<Admin> adminUser = adminDao.findByUserName(user.getEmail());

			if (existingUser.isPresent()) {
				throw new Exception("Email and Phone Number already exist");
			} else if (adminUser.isPresent()) {
				throw new Exception(user.getUsername() + " - Issue encountered. Contact Administrator.");
			}

			// Generate random password for the user
			String generateRandomPassword = generatePassword(12).trim();
			String encodedPassword = passwordEncoder.encode(generateRandomPassword);
			long count = stageUserDao.count();
			// Set user properties
			if (count > 0) {
				int maxSno = stageUserDao.maxSno();
				user.setSno(++maxSno); // Ensure this is not set in the Java code
			} else {
				user.setSno(1); // Ensure this is not set in the Java code
			}
			user.setAccountNonLocked(true);
			user.setPhone(user.getPhone().trim().replaceAll("\\s", ""));
			user.setPassword(encodedPassword);
			user.setRepassword(encodedPassword); // Use the same encoded password
			user.setUsername(user.getUsername().toUpperCase());
			user.setEnabled(true);
			user.setBaseLocation("No Record Found");
			user.setEditwho("No Record Found");
			user.setState("No Record Found");
			user.setStatus("ACTIVE");
			user.setAdddate(new Date());
			user.setAddwho(user.getAddwhoAdminId());
			user.setIpAddress(ipaddress);
			user.setLocation(location);
			String subject = "www.ems.com : Your Crendential Created";
			String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>" + "    <meta charset='UTF-8'>"
					+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
					+ "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + "    <style>"
					+ "        body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; }"
					+ "        .wrapper { width: 100%; padding: 40px 0; background-color: #f9f9f9; }"
					+ "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }"
					+ "        .header { background-color: #007BFF; padding: 20px; text-align: center; color: #ffffff; border-top-left-radius: 8px; border-top-right-radius: 8px; }"
					+ "        .header h1 { margin: 0; font-size: 24px; font-weight: normal; }"
					+ "        .content { padding: 30px; text-align: left; color: #333333; }"
					+ "        .content p { font-size: 16px; line-height: 1.6; }"
					+ "        .content .password { font-weight: bold; color: #007BFF; }"
					+ "        .footer { padding: 20px; text-align: center; font-size: 12px; color: #888; background-color: #f1f1f1; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; }"
					+ "        .footer a { color: #007BFF; text-decoration: none; }" + "    </style>" + "</head>"
					+ "<body>" + "    <div class='wrapper'>" + "        <table class='container' align='center'>"
					+ "            <tr>" + "                <td class='header'>"
					+ "                    <h1>Password Reset Request</h1>" + "                </td>"
					+ "            </tr>" + "            <tr>" + "                <td class='content'>"
					+ "                    <p>Dear " + user.getUsername() + ",</p>"
					+ "                    <p>Your default password is: <span class='password'>"
					+ generateRandomPassword + "</span></p>"
					+ "                    <p>We kindly request that you reset this password at your earliest convenience to ensure the security of your account.</p>"
					+ "                    <p>If you need any help, feel free to contact our support team.</p>"
					+ "                </td>" + "            </tr>" + "            <tr>"
					+ "                <td class='footer'>"
					+ "                    <p>If you didn’t request this password reset, please ignore this email. Need help? <a href='#'>Contact Support</a>.</p>"
					+ "                </td>" + "            </tr>" + "        </table>" + "    </div>" + "</body>"
					+ "</html>";

//			boolean flag = false;

			if (user.getRole() != null) {
				if (user.getRole().equals("ROLE_ADMIN")) {
					String to = user.getEmail();
					Optional<CompanyInfo> companyOptional = company_dao.findByCompanyIdOptional(user.getCompanyId());
					if (companyOptional.isPresent()) {
						CompanyInfo companyInfo = companyOptional.get();
						user.setCompany(companyInfo.getCompany_name());
						System.out.println("EMAIL " + user.getEmail());
						Optional<stage_user> result3 = stageUserDao.findByUserNameAndPhone(to, user.getPhone());
						if (!result3.isPresent()) {
							stageUserDao.save(user); // Save user to database first
							stageUserDao.flush();

							CompletableFuture<Boolean> flagFuture = emailService.sendEmail(message, subject,
									user.getEmail());
							if (flagFuture.get()) { // Check email sending success
								user.setDefaultPasswordSent(true);
								stageUserDao.save(user); // Update user with email sent status
								stageUserDao.flush();
							} else {
								user.setDefaultPasswordSent(false);
								stageUserDao.save(user); // Update user with email sent status
								stageUserDao.flush();
							}
						} else {
							throw new Exception("User Already Present !!");
						}
					} else {
						throw new Exception(user.getCompanyId() + " Company Is Not Registered With EMS INDIA PVT LTD");
					}
				} else if (user.getRole().equals("ROLE_USER") || user.getRole().equals("ROLE_MANAGER")
						|| user.getRole().equals("ROLE_HR") || user.getRole().equals("ROLE_IT")) {

					String to = user.getEmail();
					Optional<CompanyInfo> companyOptional = company_dao.findByCompanyIdOptional(user.getCompanyId());
					if (companyOptional.isPresent()) {
						CompanyInfo companyInfo = companyOptional.get();
						user.setCompany(companyInfo.getCompany_name());
						System.out.println("EMAIL " + user.getEmail());
						Optional<stage_user> result3 = stageUserDao.findByUserNameAndPhone(to, user.getPhone());
						if (!result3.isPresent()) {
							stageUserDao.save(user); // Save user to database first
							stageUserDao.flush();
							CompletableFuture<Boolean> flagFuture = emailService.sendEmail(message, subject,
									user.getEmail());
							if (flagFuture.get()) { // Check email sending success
								user.setDefaultPasswordSent(true);
								stageUserDao.save(user); // Update user with email sent status
								stageUserDao.flush();

							} else {
								user.setDefaultPasswordSent(false);
								stageUserDao.save(user); // Update user with email sent status
								stageUserDao.flush();
							}
//							stageUserDao.save(user);
						} else {
							throw new Exception("User plready Present !!");
						}
					} else {
						throw new Exception(user.getCompanyId() + " Company Is Not Registered With EMS INDIA PVT LTD");
					}
				} else {
					throw new Exception("Something Went Wrong !!");
				}
			} else {
				throw new Exception(user.getCompanyId() + " Company Is Not Registered With EMS INDIA PVT LTD");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return user;
	}

	public static boolean isValidDOB(String dobStr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Matching input format
		LocalDate dob = LocalDate.parse(dobStr, formatter); // Convert String to LocalDate
		return Period.between(dob, LocalDate.now()).getYears() >= 18;
	}

	// Characters to include in the password
	private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
	private static final String DIGITS = "0123456789";
	private static final String SPECIAL_CHARACTERS = "@#-";
	// SecureRandom for cryptographically secure random generator
	private static final SecureRandom random = new SecureRandom();

	public static String generatePassword(int length) {
		if (length < 8 || length > 16) {
			throw new IllegalArgumentException("Password length should be between 8 and 16 characters.");
		}

		// Use a simple and balanced structure: Word + Digits + Special Character
		StringBuilder password = new StringBuilder();

		// Add random readable word-like sequence
		password.append(generateWord());

		// Add random digits
		for (int i = 0; i < 2; i++) { // Add 2 digits
			password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
		}

		// Add one special character
		password.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

		// Shuffle the characters for randomness
		return shuffleString(password.toString());
	}

	private static String generateWord() {
		StringBuilder word = new StringBuilder();
		word.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length()))); // Capital letter
		for (int i = 0; i < 4; i++) { // Add 4 lowercase letters
			word.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
		}
		return word.toString();
	}

	private static String shuffleString(String input) {
		StringBuilder shuffled = new StringBuilder(input.length());
		char[] chars = input.toCharArray();

		for (int i = chars.length; i > 0; i--) {
			int index = random.nextInt(i);
			shuffled.append(chars[index]);
			chars[index] = chars[i - 1];
		}
		return shuffled.toString();
	}

	public static Captcha createCaptcha(int width, int height) {
		return new Captcha.Builder(width, height).addBackground(new GradiatedBackgroundProducer())
				.addText(new DefaultTextProducer()).addNoise(new StraightLineNoiseProducer()).build();

	}

	public  String encodeCaptcha(Captcha captcha) {
		String imageData = null;

		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(captcha.getImage(), "png", os);
			byte[] arr = Base64.getEncoder().encode(os.toByteArray());
			imageData = new String(arr);
			System.out.println("image created" + imageData);
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
		return imageData;
	}

	public String generateString() {
		try {
			String uuid = UUID.randomUUID().toString();
			return uuid;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return null;
	    }
	}

	public boolean saveNewPassword(String NewPassword, String Email) {
		try {
			Optional<User> user = userdao.findByEmail(Email);
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
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
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
		user.setSeperationDate(null);

	}
	@Transactional
	public void getAllUsersByAccount_Non_LockedAndFailed_Attempts() {
	    try {
	        // Perform bulk updates directly
	        userdao.unlockUsers();
	        userDetailDao.unlockUsers();

	        System.out.println("Bulk unlock operation completed successfully.");
	    } catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
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
			List<UserLoginDateTime> login = userLoginDao.findAll();
			LoginDataArchive loginDataArchive = new LoginDataArchive();
			Date currentDate = new Date();
			for (UserLoginDateTime login_date_time : login) {
				Date login_date = login_date_time.getLoginDateAndTime();
				// Calculate the difference in days between the login date and current date
				long diffInMillis = currentDate.getTime() - login_date.getTime();
				long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

				int count = archiveLoginDao.getArchiveLoginCount();
				// If the login date is older than 30 days
				if (diffInDays > 30) {
					if (count > 0) {
						int last_sno = archiveLoginDao.getLastId();
						loginDataArchive.setSno(++last_sno);
						loginDataArchive.setId(login_date_time.getId());
						loginDataArchive.setEmail(login_date_time.getEmail());
						loginDataArchive.setIpAddress(login_date_time.getIpAddress());
						loginDataArchive.setUser_status(login_date_time.isUser_status());
						loginDataArchive.setLocation(login_date_time.getLocation());
						loginDataArchive.setLoginDateAndTime(login_date_time.getLoginDateAndTime());
						loginDataArchive.setLogoutDateAndTime(login_date_time.getLogoutDateAndTime());
						loginDataArchive.set_session_interrupted(login_date_time.is_session_interrupted());
						loginDataArchive.setUsername(login_date_time.getUsername());
						loginDataArchive.setSession_Id(login_date_time.getSession_Id());
						loginDataArchive.setAdddate(new Date());
						loginDataArchive.setAddwho("Login Archive Job");
						userLoginDao.delete(login_date_time);
					} else {
						loginDataArchive.setSno(1);
						loginDataArchive.setId(login_date_time.getId());
						loginDataArchive.setEmail(login_date_time.getEmail());
						loginDataArchive.setIpAddress(login_date_time.getIpAddress());
						loginDataArchive.setUser_status(login_date_time.isUser_status());
						loginDataArchive.setLocation(login_date_time.getLocation());
						loginDataArchive.setLoginDateAndTime(login_date_time.getLoginDateAndTime());
						loginDataArchive.setLogoutDateAndTime(login_date_time.getLogoutDateAndTime());
						loginDataArchive.set_session_interrupted(login_date_time.is_session_interrupted());
						loginDataArchive.setUsername(login_date_time.getUsername());
						loginDataArchive.setSession_Id(login_date_time.getSession_Id());
						loginDataArchive.setAdddate(new Date());
						loginDataArchive.setAddwho("Login Archive Job");
						userLoginDao.delete(login_date_time);
					}
					archiveLoginDao.save(loginDataArchive);
				}
			}
			jobDao.getJobRunningTime("Login_Archive_Job");
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	@Transactional
	public void getAllOrdersAdddate() {
		try {
			List<Payment_Order_Info> AllOrders = orderDao.findAll();
			ArchiveOldOrders archiveOldOrders = new ArchiveOldOrders();
			Date currentDate = new Date();
			for (Payment_Order_Info Orders : AllOrders) {
				Date orders_date = Orders.getSystem_date_and_time();
				// Calculate the difference in days between the login date and current date
				long diffInMillis = currentDate.getTime() - orders_date.getTime();
				long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

				int count = archiveOldOrdersDao.getArchiveOrdersCount();
				// If the login date is older than 30 days
				if (diffInDays > 30) {
					if (count > 0) {
						int last_sno = archiveOldOrdersDao.getLastId();
						archiveOldOrders.setSno(++last_sno);
						archiveOldOrders.setAmount(Orders.getAmount());
						archiveOldOrders.setAmount_without_gst(Orders.getAmount_without_gst());
						archiveOldOrders.setCompany(Orders.getCompany());
						archiveOldOrders.setCompany_id(Orders.getCompany_id());
						archiveOldOrders.setDiscount(Orders.getDiscount());
						archiveOldOrders.setEmail(Orders.getEmail());
						archiveOldOrders.setGst_amount(Orders.getGst_amount());
						archiveOldOrders.setLicense_number(Orders.getLicense_number());
						archiveOldOrders.setLicense_status(Orders.getLicense_status());
						archiveOldOrders.setSystem_date_and_time(Orders.getSystem_date_and_time());
						archiveOldOrders.setSubscription_expiry_date(Orders.getSubscription_expiry_date());
						archiveOldOrders.setSubscription_start_date(Orders.getSubscription_start_date());
						archiveOldOrders.setReceipt(Orders.getReceipt());
						archiveOldOrders.setPaymentId(Orders.getPaymentId());
						archiveOldOrders.setOrderId(Orders.getOrderId());
						archiveOldOrders.setTax(Orders.getTax());
						archiveOldOrders.setInvoice_sent_or_not(Orders.isInvoice_sent_or_not());
						archiveOldOrders.setGst_no(Orders.getGst_no());
						archiveOldOrders.setStatus(Orders.getStatus());
						archiveOldOrders.setPhone(Orders.getPhone());
						archiveOldOrdersDao.save(archiveOldOrders);
						orderDao.delete(Orders);
					} else {
						archiveOldOrders.setSno(1);
						archiveOldOrders.setAmount(Orders.getAmount());
						archiveOldOrders.setAmount_without_gst(Orders.getAmount_without_gst());
						archiveOldOrders.setCompany(Orders.getCompany());
						archiveOldOrders.setCompany_id(Orders.getCompany_id());
						archiveOldOrders.setDiscount(Orders.getDiscount());
						archiveOldOrders.setEmail(Orders.getEmail());
						archiveOldOrders.setGst_amount(Orders.getGst_amount());
						archiveOldOrders.setLicense_number(Orders.getLicense_number());
						archiveOldOrders.setLicense_status(Orders.getLicense_status());
						archiveOldOrders.setSystem_date_and_time(Orders.getSystem_date_and_time());
						archiveOldOrders.setSubscription_expiry_date(Orders.getSubscription_expiry_date());
						archiveOldOrders.setSubscription_start_date(Orders.getSubscription_start_date());
						archiveOldOrders.setReceipt(Orders.getReceipt());
						archiveOldOrders.setPaymentId(Orders.getPaymentId());
						archiveOldOrders.setOrderId(Orders.getOrderId());
						archiveOldOrders.setTax(Orders.getTax());
						archiveOldOrders.setInvoice_sent_or_not(Orders.isInvoice_sent_or_not());
						archiveOldOrders.setGst_no(Orders.getGst_no());
						archiveOldOrders.setStatus(Orders.getStatus());
						archiveOldOrders.setPhone(Orders.getPhone());
						archiveOldOrdersDao.save(archiveOldOrders);
						orderDao.delete(Orders);
					}
				}
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	@Transactional
	public String seperationLogic(Integer id, User user) {
		try {
			// 1. Set Separation Info
			user.setSeperationDate(new Date());
			Instant i = Instant.now();
			Instant i1 = i.plus(Duration.ofDays(2));
			Date myDate = Date.from(i1);
			user.setLastWorkingDay(myDate);
			user.setResignationRequestApplied(true);
			userdao.save(user);

			// 2. Update UserDetail
			Optional<UserDetail> userDetail = userDetailDao.findByIdField(id);
			if (userDetail.isPresent()) {
				UserDetail userDetail2 = userDetail.get();
				userDetail2.setSeperationDate(user.getSeperationDate());
				userDetail2.setResignationRequestApplied(true);
				userDetail2.setLastWorkingDay(myDate);
				userDetailDao.save(userDetail2);
			}

			// 3. Email details
			String to = user.getEmail();
			String adminId = user.getAdmin_id();
			int typeCastAdminId = Integer.parseInt(adminId);
			Optional<Admin> admin = adminDao.findByAdminId(typeCastAdminId);
			if (admin.isEmpty()) {
				throw new Exception("Something Went Wrong");
			}
			Admin admin1 = admin.get();
			String cc = admin1.getEmail();
			String subject = "Separation Request EMPID: " + user.getId();

			String message = "" + "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>"
					+ "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
					+ "<meta http-equiv='X-UA-Compatible' content='IE=edge'>" + "<style>"
					+ "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }"
					+ ".email-container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }"
					+ ".header { background-color: #007BFF; padding: 20px; text-align: center; color: #ffffff; border-top-left-radius: 8px; border-top-right-radius: 8px; }"
					+ ".header h1 { margin: 0; font-size: 24px; }"
					+ ".content { padding: 30px; color: #333333; line-height: 1.6; }"
					+ ".content p { font-size: 16px; margin: 0 0 20px 0; }"
					+ ".content .highlight { font-weight: bold; color: #007BFF; }"
					+ ".footer { padding: 20px; text-align: center; font-size: 12px; color: #888888; background-color: #f1f1f1; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; }"
					+ "</style></head><body>" + "<div class='email-container'>"
					+ "<div class='header'><h1>Separation Request Received</h1></div>" + "<div class='content'>"
					+ "<p>Dear " + user.getUsername() + ",</p>"
					+ "<p>We have received your separation request and are currently reviewing it.</p>"
					+ "<p>We will get back to you shortly with further details or next steps.</p>"
					+ "<p>Thank you for bringing this to our attention.</p>" + "<p>Sincerely,</p>"
					+ "<p><strong>HR Team</strong></p>" + "</div><div class='footer'>"
					+ "<p>If you have any questions, feel free to reach out to us at any time.</p>"
					+ "</div></div></body></html>";

			if (userDetail.get().getManagerId() == 0) {
				List<String> Emails = userDetailDao.findEmailsByBaseLocationAndRoles(
						userDetail.get().getBase_location(), userDetail.get().getCompany_id(),
						userDetail.get().getId());

				// Sending email to HR (assuming you're passing HR emails as cc)
				CompletableFuture<Boolean> flagFuture = listEmailService.sendEmail(message, subject, to, cc, Emails);

				List<UserDetail> getInfo = userDetailDao.findIdsByBaseLocationAndRoles(
						userDetail.get().getBase_location(), userDetail.get().getCompany_id(),
						userDetail.get().getId());

				if (flagFuture.get()) {
					for (UserDetail detail : getInfo) {
						System.out.println("NOTIFICATION SENDING TO EMP ID: " + detail.getId());
						Notification notification = new Notification();
						notification.setMessage_id(generateMessageId());
						notification.setMessage("Separation Request Withdrawn By EMPID: " + userDetail.get().getId());
						notification.setUserId(detail.getId());
						notification.setTimestamp(new Date());
						notification.setAdddate(new Date());
						notification.setAddwho("SYSTEM");
						notification.setEditdate(new Date());
						notification.setReceiverEmail(detail.getEmail());
						notification.setSenderEmail(userDetail.get().getEmail());
						notification.setEditwho(detail.getEditwho());
						notificationDao.save(notification);
						NotificationListener.send(detail.getEmail(), notification);
						System.out.println("Sending notification to: " + detail.getEmail());

					}
				} else {
					System.out.println("Email failed to send.");
				}

			} else {
				List<String> Emails = userDetailDao.findEmailsByBaseLocationAndRoles(
						userDetail.get().getBase_location(), userDetail.get().getCompany_id(),
						userDetail.get().getId());

				// Sending email to HR (assuming you're passing HR emails as cc)
				@SuppressWarnings("unused")
				CompletableFuture<Boolean> flagFuture = listEmailService.sendEmail(message, subject, to, cc, Emails);

				List<UserDetail> Ids = userDetailDao.findIdsByBaseLocationAndRoles(userDetail.get().getBase_location(),
						userDetail.get().getCompany_id(), userDetail.get().getId());
				System.out.println("WITHDRAWN SIZE " + Ids.size());

				for (UserDetail detail : Ids) {
					System.out.println("NOTIFICATION SENDING TO EMP ID: " + detail.getId());
					Notification notification = new Notification();
					notification.setMessage_id(generateMessageId());
					notification.setMessage("Separation Request Withdrawn By EMPID: " + userDetail.get().getId());
					notification.setUserId(detail.getId());
					notification.setTimestamp(new Date());
					notification.setAdddate(new Date());
					notification.setAddwho("SYSTEM");
					notification.setEditdate(new Date());
					notification.setReceiverEmail(detail.getEmail());
					notification.setSenderEmail(userDetail.get().getEmail());
					notification.setEditwho(detail.getEditwho());
					notificationDao.save(notification);
					NotificationListener.send(detail.getEmail(), notification);
					System.out.println("Sending notification to: " + detail.getEmail());

				}
			}

			return "Your last working day is " + myDate;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return null;
	    }
	}

	@Transactional
	public void seperationWithdrawnLogic(Integer id, User user) {
		try {
			user.setLastWorkingDay(null);
			user.setSeperationDate(null);
			user.setResignationRequestApplied(false);
			userdao.save(user);
			Optional<UserDetail> userOptional = userDetailDao.findByIdField(id);
			UserDetail userDetail = userOptional.get();
			userDetail.setLastWorkingDay(null);
			userDetail.setSeperationDate(null);
			userDetail.setResignationRequestApplied(false);
			userDetailDao.save(userDetail);

		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }

	}

	@Transactional
	public boolean processing_seperation_by_manager(Integer id, User user) {
		try {
			user.setSeperation_manager_approved(true);
			userdao.save(user);
			Optional<UserDetail> userOptional = userDetailDao.findByIdField(id);
			UserDetail userDetail = userOptional.get();
			userDetail.setSeperation_manager_approved(true);
			userDetailDao.save(userDetail);
			return true;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return false;
	    }
	}

	@Transactional
	public boolean processSeparationByManager(Integer id, String currentLoggedInUser)
			throws InterruptedException, ExecutionException {
		Optional<User> result2 = userdao.findByIdField(id);
		if (!result2.isPresent())
			return false;

		User user = result2.get();

		// Validate resignation status
		if (!user.isResignationRequestApplied() || user.isSeperation_manager_approved())
			return false;

		// Fetch admin
		int adminId = Integer.parseInt(user.getAdmin_id());
		Optional<Admin> adminOpt = adminDao.findByAdminId(adminId);
		if (!adminOpt.isPresent())
			return false;

		Admin admin = adminOpt.get();

		// Update user separation status
		boolean updated = processing_seperation_by_manager(id, user);
		if (!updated)
			return false;

		// Build email
		String to = user.getEmail();
		String cc = admin.getEmail();
		String subject = "Resignation Accepted - EMPID: " + user.getId();

		String message = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>"
				+ "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
				+ "<meta http-equiv='X-UA-Compatible' content='IE=edge'><style>"
				+ "body { font-family: Arial, sans-serif; background-color: #f9f9f9; }"
				+ ".email-container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px;"
				+ " box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }"
				+ ".header { background-color: #007BFF; color: #ffffff; padding: 20px; text-align: center;"
				+ " border-top-left-radius: 8px; border-top-right-radius: 8px; }"
				+ ".header h1 { margin: 0; font-size: 22px; }"
				+ ".content { padding: 20px; color: #333333; line-height: 1.5; }"
				+ ".content p { margin: 10px 0; font-size: 16px; }"
				+ ".highlight { font-weight: bold; color: #007BFF; }"
				+ ".footer { background-color: #f1f1f1; padding: 10px 20px; text-align: center;"
				+ " color: #888888; font-size: 12px; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; }"
				+ "</style></head><body><div class='email-container'>"
				+ "<div class='header'><h1>Resignation Accepted</h1></div>" + "<div class='content'><p>Dear "
				+ user.getUsername() + ",</p>"
				+ "<p>This email is to formally acknowledge the acceptance of your resignation request. "
				+ "Your last working day has been confirmed as <span class='highlight'>" + user.getLastWorkingDay()
				+ "</span>.</p>"
				+ "<p>We would like to express our deepest gratitude for the contributions you have made during your tenure with the organization. "
				+ "Your efforts and dedication have left a significant impact, and you will be greatly missed.</p>"
				+ "<p>We wish you continued success in all your future endeavors.</p>"
				+ "<p>Best regards,<br><strong>HR Team</strong></p></div>"
				+ "<div class='footer'><p>If you have any questions or need further assistance, feel free to contact the HR department.</p></div>"
				+ "</div></body></html>";

		// Fetch HR emails
		List<String> hrEmails = userDetailDao.findEmailsByBaseLocationAndRoles(user.getBase_location(),
				user.getCompany_id(), user.getId());

		// Send email
		CompletableFuture<Boolean> flagFuture = listEmailService.sendEmail(message, subject, to, cc, hrEmails);

		// Prepare recipients for notifications
		Set<UserDetail> recipients = new HashSet<>(userDetailDao.findIdsByBaseLocationAndRoles(user.getBase_location(),
				user.getCompany_id(), user.getId()));
		recipients.removeIf(detail -> detail.getEmail().equals(admin.getEmail())); // remove duplicate

		// Wait for email to finish sending
		if (flagFuture.get()) {
			for (UserDetail detail : recipients) {
				Notification notification = new Notification();
				notification.setMessage_id(generateMessageId());
				notification.setMessage("Separation Request Approved EMPID: " + user.getId());
				notification.setUserId(detail.getId());
				notification.setTimestamp(new Date());
				notification.setAdddate(new Date());
				notification.setAddwho("SYSTEM");
				notification.setEditdate(new Date());
				notification.setSenderEmail(user.getEmail());
				notification.setReceiverEmail(detail.getEmail());
				notification.setEditwho(user.getEditwho());
				notificationDao.save(notification);

				NotificationListener.send(detail.getEmail(), notification);
			}
		} else {
			for (UserDetail detail : recipients) {
				Notification notification = new Notification();
				notification.setMessage_id(generateMessageId());
				notification.setMessage("Separation Request Approved EMPID: " + user.getId());
				notification.setUserId(detail.getId());
				notification.setTimestamp(new Date());
				notification.setAdddate(new Date());
				notification.setAddwho("SYSTEM");
				notification.setEditdate(new Date());
				notification.setSenderEmail(user.getEmail());
				notification.setReceiverEmail(detail.getEmail());
				notification.setEditwho(user.getEditwho());
				notificationDao.save(notification);

				NotificationListener.send(detail.getEmail(), notification);
			}
		}

		return true;
	}

	public String withdrawResignationRequestWithEmail(Integer id) {
		try {
			Optional<User> result2 = userdao.findByIdField(id);
			if (!result2.isPresent()) {
				throw new NoSuchElementException("User not found with ID: " + id);
			}

			User user1 = result2.get();
			String adminId = user1.getAdmin_id();
			int typeCastAdminId = Integer.parseInt(adminId);

			Optional<Admin> admin = adminDao.findByAdminId(typeCastAdminId);
			if (!admin.isPresent()) {
				throw new NoSuchElementException("Admin not found with ID: " + typeCastAdminId);
			}

			Admin admin1 = admin.get();
			String to = user1.getEmail();
			String cc = admin1.getEmail();

			if (user1.isResignationRequestApplied() && !user1.isSeperation_manager_approved()) {
				// Business logic to withdraw resignation
				seperationWithdrawnLogic(id, user1);

				// Email template + sending
				String subject = "Withdraw Separation Request EMPID: " + user1.getId();
				String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>" + "    <meta charset='UTF-8'>"
						+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" + "    <style>"
						+ "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; }"
						+ "        .email-container { max-width: 600px; margin: 40px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }"
						+ "        .header { background-color: #28a745; color: white; padding: 20px; border-top-left-radius: 8px; border-top-right-radius: 8px; text-align: center; }"
						+ "        .content { padding: 30px; color: #333; line-height: 1.6; }"
						+ "        .footer { background-color: #f1f1f1; color: #888; text-align: center; padding: 20px; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; font-size: 12px; }"
						+ "    </style>" + "</head>" + "<body>" + "<div class='email-container'>"
						+ "    <div class='header'><h1>Resignation Withdrawn</h1></div>" + "    <div class='content'>"
						+ "        <p>Dear " + user1.getUsername() + ",</p>"
						+ "        <p>Your resignation request has been successfully withdrawn.</p>"
						+ "        <p>We’re happy to have you continue with us. Thank you for your contributions.</p>"
						+ "        <p>Sincerely,<br><strong>HR Team</strong></p>" + "    </div>"
						+ "    <div class='footer'>"
						+ "        <p>If you have any questions, feel free to reach out to us at any time.</p>"
						+ "    </div>" + "</div>" + "</body>" + "</html>";

				List<String> Emails = userDetailDao.findEmailsByBaseLocationAndRoles(user1.getBase_location(),
						user1.getCompany_id(), user1.getId());

				// Sending email to HR (assuming you're passing HR emails as cc)
				CompletableFuture<Boolean> flagFuture = listEmailService.sendEmail(message, subject, to, cc, Emails);

				List<UserDetail> Ids = userDetailDao.findIdsByBaseLocationAndRoles(user1.getBase_location(),
						user1.getCompany_id(), user1.getId());
				System.out.println("WITHDRAWN SIZE " + Ids.size());

				for (UserDetail detail : Ids) {
					Notification notification = new Notification();
					notification.setMessage_id(generateMessageId());
					notification.setMessage("Separation Request Withdrawn By EMPID: " + user1.getId());
					notification.setUserId(detail.getId());
					notification.setTimestamp(new Date());
					notification.setAdddate(new Date());
					notification.setAddwho("SYSTEM");
					notification.setEditdate(new Date());
					notification.setEditwho(detail.getEditwho());
					notification.setSenderEmail(user1.getEmail());
					notification.setReceiverEmail(detail.getEmail());
					notificationDao.save(notification);
					NotificationListener.send(detail.getEmail(), notification);
					System.out.println("Sending notification to: " + detail.getEmail());

				}

				boolean sent = flagFuture.get(); // Blocking call
				return sent ? "success" : "failure";
			} else {
				return "failure";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "failure";
		}
	}

	@Transactional
	public void DisabledSeparationUser() {
		try {
			List<Integer> userIds = userdao.getLastWorkingDay_Records();

			for (Integer userId : userIds) {
				Optional<User> userOptional = userdao.findByIdField(userId);
				Optional<UserDetail> userDetailOptional = userDetailDao.findByIdField(userId);

				// Ensure the user exists before proceeding
				if (userOptional.isPresent() && userDetailOptional.isPresent()) {
					User user = userOptional.get();
					UserDetail userDetail = userDetailOptional.get();
					Date lastDateGet = user.getLastWorkingDay();

					if (lastDateGet != null && lastDateGet.equals(userDetail.getLastWorkingDay())) {
						userdao.disableUsersByLastWorkingDay(lastDateGet);
						userDetailDao.disableUsersByLastWorkingDay(lastDateGet);
					}
				} else {
					System.out.println("User or UserDetail not found for ID: " + userId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//		Map<Integer,Date> map=daolayer.getLastWorkingDay_Records();
//		Iterator<Entry<Integer, Date>> itr=map.entrySet().iterator();
//		while(itr.hasNext())
//		{
//			Map.Entry<Integer, Date> GetMap=(Map.Entry<Integer,Date>)itr.next();
//			Date getDateRes=GetMap.getValue();
//			if(getDateRes!=null)
//
//			daolayer.getEnableFalse(getDateRes);
//			Optional<UserDetail> userDetail=  userDetailDao.findById(GetMap.getKey());
//			UserDetail userdetail1=userDetail.get();
//			userdetail1.setEnabled(false);
//			userDetailDao.save(userdetail1);
//			}
//		}
//	}

	public List<UserDetail> findUserByTeam(int id) {
		try {
			Optional<UserDetail> userDetailOptional = userDetailDao.findByIdField(id);

			if (!userDetailOptional.isPresent()) {
				return List.of(); // Return an empty list if no user is found by ID
			}

			UserDetail userDetail = userDetailOptional.get();
			String teamId = userDetail.getTeam();
			String companyId = userDetail.getCompany_id();

			if ("No Record Found".equals(teamId) || "0".equals(teamId)) {
				return List.of(); // Return an empty list if team_id is invalid
			} else {
				return userDetailDao.getUserByTeam(teamId, companyId); // Return the list of users by team
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);

			return List.of(); // Return an empty list in case of an exception
		}
	}

	@Transactional
	public void jobtime(String name) {
		try {
			jobDao.getJobRunningTimeInterrupted(name);
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	public String getjob_active_or_not(String name) {
		try {
			System.out.println("JOB NAME " + name);
			String result = jobDao.getJobStatus(name);
			return result;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return null;
	    }
	}

	@Transactional
	public void jobnotrunning(String name) {
		try {
			jobDao.getJobNotRunning(name);
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	@Transactional
	public void jobrunning(String name) {
		try {
			jobDao.getJobRunningTime(name);
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

//	@Transactional
//	public void InactiveUserDisabled() {
//		List<User> list = userdao.findAll();
//		ListIterator<User> listIterator = list.listIterator();
//		while (listIterator.hasNext()) {
//			User user = listIterator.next();
//			if (user.isNewUserActiveOrInactive() == false) {
//				System.out.println("USER ID IS DISABLED " + user);
//				userdao.disableuserbyid(user.getId());
//			}
//		}
//		jobrunning("Is_Disabled_Inactive_User_Job");
//	}

	@Transactional
	public void reset_failed_attempts_password() {
		try
		{
		jobrunning("Password_FailedAttempt_Reset");
		userdao.reset_failed_attempt_job();
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

//	@Transactional
//	public void user_inactive() {
//		jobrunning("Update_User_Inactive_Status");
//		userLoginDao.Update_Inactive_user_Status();
//	}

//	@Transactional
//	public void update_interrupt_user_status() {
//		try {
//			jobrunning("get_user_status");
//			userLoginDao.updateuserstatus();
//		} catch (Exception e) {
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = Servicelayer.class;
//			jobDao.getJobRunningTimeInterrupted("get_user_status");
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			e.printStackTrace();
//			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//		}
//	}

	@Transactional
	public void delete_old_error_log() {
		try {
			jobrunning("delete_old_error_log");
			error_log_dao.deleteOldErrorLog();
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	
	
	 @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW)
	    public  void insert_error_log(String errorDescription, String javaFileName, String errorMessage,
	                                 String methodName, int lineNumber, String fullStackTrace) {

	        System.out.println("[DEBUG] Starting insertErrorLog...");

	        try {
	            long sno = (error_log_dao.getCount() > 0)
	                    ? error_log_dao.getLastId() + 1
	                    : 1;

	            Error_Log errorLog = new Error_Log();
	            errorLog.setSno(sno);
	            errorLog.setError_description(errorDescription);
	            errorLog.setErrorDate(new Date());
	            errorLog.setJava_class_Name(javaFileName);
	            errorLog.setError_message(errorMessage);
	            errorLog.setMethod_name(methodName);
	            errorLog.setError_line_number(lineNumber);
	            errorLog.setFull_stack_trace(fullStackTrace);

	            error_log_dao.save(errorLog);
	            System.out.println("[DEBUG] Error log inserted into DB successfully.");

	        } catch (Exception logError) {
	            System.err.println("[ERROR] Failed to insert error log into DB.");
	            logError.printStackTrace();
	            fallbackToFile(errorDescription, javaFileName, errorMessage, methodName, lineNumber, fullStackTrace);
	        }
	    }

	    private void fallbackToFile(String errorDescription, String javaFileName, String errorMessage,
	                                 String methodName, int lineNumber, String fullStackTrace) {
	        try (FileWriter fw = new FileWriter("error_fallback.log", true)) {
	            fw.write("==== ERROR LOG ENTRY ====\n");
	            fw.write("Date: " + new Date() + "\n");
	            fw.write("Java Class: " + javaFileName + "\n");
	            fw.write("Method: " + methodName + "\n");
	            fw.write("Line: " + lineNumber + "\n");
	            fw.write("Message: " + errorMessage + "\n");
	            fw.write("Description: " + errorDescription + "\n");
	            fw.write("Stack Trace: \n" + fullStackTrace + "\n\n");
	            fw.flush();
	            System.out.println("[DEBUG] Error log saved to fallback file.");
	        } catch (IOException e) {
	            System.err.println("[CRITICAL] Failed to write error log to fallback file.");
	            e.printStackTrace();
	        }
	    }


	public String generateExcel() throws IOException {
		try
		{
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
		String[] columns = { "Sno", "Employee ID", "Name", "Email", "Login Time", "Logout Time", "IP Address",
				"Is Session Interrupted", "Location" };

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
		}catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        // Return fallback value
	        return "Error generating Excel. Check error logs for details.";
	    }
	}

	public ByteArrayOutputStream exportUserLoginData(String generatedByEmail, String phone) throws IOException {
		try
		{
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
		sheet.setColumnWidth(8, 7000);

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

		CellStyle infoStyle = workbook.createCellStyle();
		infoStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
		Font infoFont = workbook.createFont();
		infoFont.setFontHeightInPoints((short) 10);
		infoStyle.setFont(infoFont);

		CellStyle titleStyle = workbook.createCellStyle();
		titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		Font titleFont = workbook.createFont();
		titleFont.setFontHeightInPoints((short) 14);
		titleFont.setFontName("Arial");
		titleFont.setBold(true);
		titleStyle.setFont(titleFont);

		// Add title row
		Row titleRow = sheet.createRow(0);
		org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("Login History Report");
		titleCell.setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 8)); // Merge title across all columns

		// Add Excel generation metadata
		Row dateRow = sheet.createRow(2);
		org.apache.poi.ss.usermodel.Cell dateCell = dateRow.createCell(0);
		String generatedDateTime = "Generated on: "
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		dateCell.setCellValue(generatedDateTime);
		dateCell.setCellStyle(infoStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 8)); // Merged across columns

		Row emailRow = sheet.createRow(3);
		org.apache.poi.ss.usermodel.Cell emailCell = emailRow.createCell(0);
		emailCell.setCellValue("Generated by: " + generatedByEmail);
		emailCell.setCellStyle(infoStyle);
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 8)); // Merged across columns

		// Create header row
		Row headerRow = sheet.createRow(5); // Start header row after metadata
		String[] columnHeaders = { "Employee ID", "LOGIN DATE TIME", "LOGOUT DATE TIME", "EMAIL", "IP ADDRESS",
				"IS SESSION EXPIRED", "USERNAME", "USER STATUS", "Location" };

		for (int i = 0; i < columnHeaders.length; i++) {
			org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
			cell.setCellValue(columnHeaders[i]);
			cell.setCellStyle(headerStyle);
		}

		// Populate data rows
		int rowNum = 6; // Start data rows after header row
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
			row.createCell(8).setCellValue(userLogin.getLocation());
		}

		// Validate generatedByEmail and phone
		String firstFourEmailCharacters = generatedByEmail.length() >= 4 ? generatedByEmail.substring(0, 4) : "GEN";
		String lastFourDigitPhoneNumber = phone.length() >= 4 ? phone.substring(phone.length() - 4) : "0000";

		// Protect the sheet
		sheet.protectSheet(firstFourEmailCharacters + lastFourDigitPhoneNumber); // Set the password for sheet
																					// protection
		workbook.write(out);
		workbook.close();
		return out;
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return new ByteArrayOutputStream(); // Return empty Excel stream on failure
	    }
	}

	@Transactional
	public boolean update_profile(User user, MultipartFile file, MultipartFile resumeFile) {
	    try {
	        Optional<UserDetail> userDetailOpt = userDetailDao.findByIdField(user.getId());
	        UserDetail userDetail;

	        if (userDetailOpt.isPresent()) {
	            userDetail = userDetailOpt.get();
	        } else {
	            // Naya record agar UserDetail nahi mila
	            userDetail = new UserDetail();
	            userDetail.setId(user.getId());
	        }

	        // 🔹 Bank info
	        userDetail.setBank_account_holder_name(
	            user.getBank_account_holder_name() != null ? user.getBank_account_holder_name() : "NA"
	        );
	        userDetail.setBank_account_number(user.getBank_account_number() != 0 ? user.getBank_account_number() : 0);
	        userDetail.setBank_name(user.getBank_name() != null ? user.getBank_name() : "NA");
	        userDetail.setIfsc_code(user.getIfsc_code() != null ? user.getIfsc_code() : "NA");

	        // 🔹 Laptop info
	        userDetail.setLaptop_brand(user.getLaptop_brand() != null ? user.getLaptop_brand() : "NA");
	        userDetail.setLaptop_serial_number(user.getLaptop_serial_number() != null ? user.getLaptop_serial_number() : "NA");
	        userDetail.setLaptop_id(user.getLaptop_id() != null ? user.getLaptop_id() : "NA");

	        // 🔹 Profile Image
	        if (file != null && !file.isEmpty()) {
	            userDetail.setImage_Name(file.getOriginalFilename());
	            user.setImage_Name(file.getOriginalFilename());
	        } else {
	            // Agar new file nahi hai toh existing hi rakho
	            if (user.getImage_Name() == null || user.getImage_Name().equals("default.jpg")) {
	                userDetail.setImage_Name("default.jpg");
	                user.setImage_Name("default.jpg");
	            } else {
	                userDetail.setImage_Name(user.getImage_Name());
	            }
	        }

	        // 🔹 Resume
	        if (resumeFile != null && !resumeFile.isEmpty()) {
	            userDetail.setResume_file_name(resumeFile.getOriginalFilename());
	            user.setResume_file_name(resumeFile.getOriginalFilename());
	        } else {
	            if (user.getResume_file_name() == null || user.getResume_file_name().equals("NA")) {
	                userDetail.setResume_file_name("NA");
	                user.setResume_file_name("NA");
	            } else {
	                userDetail.setResume_file_name(user.getResume_file_name());
	            }
	        }

	        // 🔹 Save both
	        userdao.save(user);          // updates User (2FA, etc.)
	        userDetailDao.save(userDetail); // updates UserDetail (profile + bank etc.)

	        return true;
	    } catch (Exception e) {
	        insert_error_log(
	            e.toString(),
	            Servicelayer.class.getName(),
	            e.getMessage(),
	            e.getStackTrace()[0].getMethodName(),
	            e.getStackTrace()[0].getLineNumber(),
	            Arrays.toString(e.getStackTrace())
	        );
	        return false;
	    }
	}


	@Transactional
	public void emp_bank_profile_update(UserDetail userDetail, String current_user) {
		try
		{
		System.out.println(userDetail.getBank_account_holder_name());
		System.out.println(userDetail.getBank_account_number());
		System.out.println(userDetail.getBank_name());
		Optional<UserDetail> userDetail2 = userDetailDao.findByIdField(userDetail.getId());
		if (userDetail2.isPresent()) {
			if (userDetail.getBank_account_holder_name().trim().isEmpty()) {
				UserDetail userDetail3 = userDetail2.get();
				userDetail3.setBank_name("NA");
				userDetail3.setBank_account_number(0);
				userDetail3.setBank_account_holder_name("NA");
				userDetail3.setIfsc_code("NA");
				Optional<User> user = userdao.findByIdField(userDetail3.getId());
				User user1 = user.get();
				user1.setBank_name("NA");
				user1.setBank_account_number(0);
				user1.setBank_account_holder_name("NA");
				user1.setIfsc_code("NA");
				userDetailDao.save(userDetail3);
				userdao.save(user1);
			} else {
				UserDetail userDetail3 = userDetail2.get();
				userDetail3.setBank_name(userDetail.getBank_name());
				userDetail3.setBank_account_number(userDetail.getBank_account_number());
				userDetail3.setBank_account_holder_name(userDetail.getBank_account_holder_name());
				userDetail3.setIfsc_code(userDetail.getIfsc_code());
				Optional<User> user = userdao.findByIdField(userDetail3.getId());
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
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

//	@Transactional
//	public boolean emp_update_profile(UserDetail userDetail, String CurrentUser) throws Exception {
//		try {
//			int found = 0;
//			System.out.println(userDetail.getId());
//			System.out.println(userDetail.getLaptop_brand());
//			System.out.println(userDetail.getLaptop_id());
//			System.out.println(userDetail.getLaptop_serial_number());
//			Optional<UserDetail> userDetail2 = userDetailDao.findByIdField(userDetail.getId());
//			String laptopProductId = userDetail.getLaptop_id();
//			String laptopSerialNumber = userDetail.getLaptop_serial_number();
//			String laptopBrand = userDetail.getLaptop_brand();
//			found = laptopDao.existsByProductIdAndSerialNumberAndBrand(laptopProductId, laptopSerialNumber, laptopBrand);
//			if(found == 1)
//			{
//			if (userDetail2.isPresent()) {
//				System.out.println("USERDETAIL INPUT GET " + userDetail.getLaptop_brand() + " <<<<<<< "
//						+ userDetail.getBank_account_holder_name() + ">>>");
////				if (userDetail.getLaptop_brand().trim().isEmpty()) {
//				if (userDetail.getLaptop_brand().equals("NA")) {
//					UserDetail userDetail3 = userDetail2.get();
//					Optional<User> user = userdao.findByIdField(userDetail3.getId());
//					User user1 = user.get();
//					userDetail3.setLaptop_assign_or_not(false);
//					userDetail3.setLaptop_brand("NA");
//					userDetail3.setLaptop_id("NA");
//					userDetail3.setLaptop_serial_number("NA");
//					userDetail3.setLaptop_assign_date(new Date());
//					userDetail3.setWho_assign_laptop(CurrentUser);
//					userDetail3.setLaptop_status("NOT ASSIGNED");
//					userDetail3.setWho_assign_laptop_employee_id(user1.getId());
//					user1.setLaptop_brand(userDetail3.getLaptop_brand());
//					user1.setLaptop_id(userDetail3.getLaptop_id());
//					user1.setLaptop_serial_number(userDetail3.getLaptop_serial_number());
//					user1.setLaptop_assign_date(userDetail3.getLaptop_assign_date());
//					userDetailDao.save(userDetail3);
//					userdao.save(user1);
//				} else {
//					UserDetail userDetail3 = userDetail2.get();
//					Optional<User> user = userdao.findByIdField(userDetail3.getId());
//					User user1 = user.get();
//					userDetail3.setLaptop_assign_or_not(true);
//					userDetail3.setLaptop_brand(userDetail.getLaptop_brand());
//					userDetail3.setLaptop_id(userDetail.getLaptop_id());
//					userDetail3.setLaptop_serial_number(userDetail.getLaptop_serial_number());
//					userDetail3.setLaptop_assign_date(new Date());
//					userDetail3.setWho_assign_laptop(CurrentUser);
//					userDetail3.setWho_assign_laptop_employee_id(user1.getId());
//					userDetail3.setLaptop_status("ASSIGNED");
//					user1.setLaptop_brand(userDetail3.getLaptop_brand());
//					user1.setLaptop_id(userDetail3.getLaptop_id());
//					user1.setLaptop_serial_number(userDetail3.getLaptop_serial_number());
//					user1.setLaptop_assign_date(userDetail3.getLaptop_assign_date());
//					userDetailDao.save(userDetail3);
//					userdao.save(user1);
//				}
//			}
//			return true;
//			}
//			else
//			{
//				System.out.println("EXCEPTION");
////				throw new Exception("Laptop Not Exist In Inventory !!");
//				return false;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

	@Transactional
	public boolean emp_update_profile(UserDetail userDetail, final String currentUser) throws Exception {
		try {
			Optional<User> userOptional = userdao.findByEmail(currentUser);
			if (!userOptional.isPresent()) {
				throw new Exception("Current user not found: " + currentUser);
			}
			User user = userOptional.get();

			Optional<UserDetail> userDetailOptional = userDetailDao.findByIdField(userDetail.getId());
			if (!userDetailOptional.isPresent()) {
				throw new Exception("User not found with ID: " + userDetail.getId());
			}
			UserDetail existingDetail = userDetailOptional.get();
			// Validate if the laptop is already assigned
			Optional<UserDetail> assignedUser = userDetailDao.findByLaptopDetails(userDetail.getLaptop_id(),
					userDetail.getLaptop_serial_number(), userDetail.getLaptop_brand(), userDetail.getId());

			if (assignedUser.isPresent() && !"REMOVED".equalsIgnoreCase(assignedUser.get().getLaptop_brand())
					&& userDetail.getLaptop_brand().equals(assignedUser.get().getLaptop_brand())) {
				throw new Exception(
						"Laptop is already assigned to another employee (User ID: " + assignedUser.get().getId() + ")");
			}

			System.out.println("MODEL" + userDetail.getLaptop_model() + " ,PRODUCT ID " + userDetail.getLaptop_id()
					+ " ,SERIAL NUMBER " + userDetail.getLaptop_serial_number() + " ,BRAND "
					+ userDetail.getLaptop_brand() + " ,COMPANY ID " + existingDetail.getCompany_id());
			// Ensure laptop exists in inventory before assignment
			int laptopExists = laptopDao.existsByProductIdAndSerialNumberAndBrand(userDetail.getLaptop_model(),
					userDetail.getLaptop_id(), userDetail.getLaptop_serial_number(), userDetail.getLaptop_brand(),
					existingDetail.getCompany_id());
			System.out.println("PRODUCT ID " + userDetail.getLaptop_id() + " ,SERIAL NUMBER "
					+ userDetail.getLaptop_serial_number() + " ,BRAND " + userDetail.getLaptop_brand() + " ,COMPANY ID "
					+ existingDetail.getCompany_id() + ",ASSIGNMENT " + userDetail.getLaptop_assignment_status()
					+ " ,OPERATIONAL " + userDetail.getLaptop_operational_status());
			if (laptopExists == 0) {
				throw new Exception("Laptop not found in inventory! Please verify the details.");
			}
			System.out.println("RUNNED ! " + existingDetail.getLaptop_brand());
//			if (assignedUser.get().getLaptop_brand().equals("REMOVED") && assignedUser.get().getLaptop_model().equals("REMOVED")
//					&& assignedUser.get().getLaptop_id().equals("REMOVED")) {
//				throw new Exception("The laptop assigned to this employee (User ID: " + userDetail.getId()
//						+ ") has already been marked as REMOVED.");
//			}
			System.out.println("RUNNED");
			// Laptop Removal
			if ("REMOVED".equalsIgnoreCase(userDetail.getLaptop_brand())) {
				String laptop_assignment = userDetail.getLaptop_assignment_status();
				String laptop_assignment_uppercase = laptop_assignment.toUpperCase();
				String cleanedText_laptop_assignment_uppercase = laptop_assignment_uppercase.replaceAll(",$", "");

				if (cleanedText_laptop_assignment_uppercase.endsWith(",")) {
					cleanedText_laptop_assignment_uppercase = cleanedText_laptop_assignment_uppercase.substring(0,
							cleanedText_laptop_assignment_uppercase.length() - 1);
				}

				String laptop_operational = userDetail.getLaptop_operational_status();
				String laptop_operational_uppercase = laptop_operational.toUpperCase();
				String cleanText_laptop_operational_uppercase = laptop_operational_uppercase.replaceAll(",$", "");

				if (cleanText_laptop_operational_uppercase.endsWith(",")) {
					cleanText_laptop_operational_uppercase = cleanText_laptop_operational_uppercase.substring(0,
							cleanText_laptop_operational_uppercase.length() - 1);
				}

				if (cleanedText_laptop_assignment_uppercase == null && cleanText_laptop_operational_uppercase == null) {
					throw new Exception("No data found for the user. Laptop Assignment Status: "
							+ userDetail.getLaptop_assignment_status() + ", Laptop Operational Status: "
							+ userDetail.getLaptop_operational_status());
				}

				if (existingDetail.getLaptop_brand().equals("REMOVED")
						&& existingDetail.getLaptop_model().equals("REMOVED")
						&& existingDetail.getLaptop_serial_number().equals("REMOVED")) {
					throw new Exception("The laptop assigned to this employee (User ID: " + userDetail.getId()
							+ ") has already been marked as REMOVED.");
				}

				System.out.println("Assignment Status: " + cleanedText_laptop_assignment_uppercase);
				System.out.println("Operational Status: " + cleanText_laptop_operational_uppercase);

				existingDetail.setLaptop_assign_or_not(false);
				existingDetail.setLaptop_status("NOT ASSIGNED");
				existingDetail.setLaptop_brand(userDetail.getLaptop_brand());
				existingDetail.setLaptop_id(userDetail.getLaptop_id());
				existingDetail.setLaptop_serial_number(userDetail.getLaptop_serial_number());
				existingDetail.setLaptop_model(userDetail.getLaptop_model());
				existingDetail.setLaptop_color(userDetail.getLaptop_color());
				existingDetail.setDevice_id(userDetail.getDevice_id());
				existingDetail.setLaptop_assignment_status(cleanedText_laptop_assignment_uppercase);
				existingDetail.setLaptop_operational_status(cleanText_laptop_operational_uppercase);
				existingDetail.setLaptop_assign_date(new Date());
				existingDetail.setWho_assign_laptop(currentUser);
				existingDetail.setWho_assign_laptop_employee_id(user.getId());
				user.setLaptop_brand(existingDetail.getLaptop_brand());
				user.setLaptop_id(existingDetail.getLaptop_id());
				user.setLaptop_serial_number(existingDetail.getLaptop_serial_number());
				user.setLaptop_assign_date(existingDetail.getLaptop_assign_date());

				GatePassGenerator gatePass = new GatePassGenerator();
				gatePass.setGatePassId(generateGatePassId());
				gatePass.setUserId(existingDetail.getId());
				gatePass.setLaptopBrand(existingDetail.getLaptop_brand());
				gatePass.setLaptopId(existingDetail.getLaptop_id());
				gatePass.setLaptopSerialNumber(existingDetail.getLaptop_serial_number());
				gatePass.setLaptop_assignment_status(existingDetail.getLaptop_assignment_status());
				gatePass.setLaptop_operational_status(existingDetail.getLaptop_operational_status());
				gatePass.setIssueDate(new Date());
				gatePass.setActionType("NOT ASSIGNED");
				gatePass.setIssuedBy(user.getEmail());
				gatePass.setStatus("ACTIVE");
				gatePass.setAdddate(new Date());
				gatePass.setAddwho(user.getUsername());
				gatePass.setEmployee_email(existingDetail.getEmail());

				gatePassDao.save(gatePass);
				existingDetail.setGate_pass(generateGatePassId());

				// Save changes
				userDetailDao.save(existingDetail);
				userdao.save(user);
				generateGatePassPDF(existingDetail, user, "Laptop Removed");

				Notification notification = new Notification();
				notification.setMessage_id(generateMessageId());
				notification.setMessage(
						"A laptop has been successfully removed from Employee ID: " + existingDetail.getId() + ".");
				notification.setUserId(existingDetail.getId());
				notification.setTimestamp(new Date());
				notification.setAdddate(new Date());
				notification.setAddwho("SYSTEM");
				notification.setEditdate(new Date());
				notification.setSenderEmail("guptaayush12418@gmail.com");
				notification.setReceiverEmail(existingDetail.getEmail());
				notification.setEditwho(existingDetail.getEditwho());
				notificationDao.save(notification);

				System.out.println("Sending notification to: " + existingDetail.getEmail());
				System.out.println("Notification: " + notification.getMessage());

				NotificationListener.send(existingDetail.getEmail(), notification); // ✅ Pass Notification object

				return true;
			} else {
				String laptop_assignment = userDetail.getLaptop_assignment_status();
				String laptop_assignment_uppercase = laptop_assignment.toUpperCase();
				String cleanedText_laptop_assignment_uppercase = laptop_assignment_uppercase.replaceAll(",$", "");

				if (cleanedText_laptop_assignment_uppercase.endsWith(",")) {
					cleanedText_laptop_assignment_uppercase = cleanedText_laptop_assignment_uppercase.substring(0,
							cleanedText_laptop_assignment_uppercase.length() - 1);
				}

				String laptop_operational = userDetail.getLaptop_operational_status();
				String laptop_operational_uppercase = laptop_operational.toUpperCase();
				String cleanText_laptop_operational_uppercase = laptop_operational_uppercase.replaceAll(",$", "");

				if (cleanText_laptop_operational_uppercase.endsWith(",")) {
					cleanText_laptop_operational_uppercase = cleanText_laptop_operational_uppercase.substring(0,
							cleanText_laptop_operational_uppercase.length() - 1);
				}

				if (cleanedText_laptop_assignment_uppercase == null && cleanText_laptop_operational_uppercase == null) {
					throw new Exception("No data found for the user. Laptop Assignment Status: "
							+ userDetail.getLaptop_assignment_status() + ", Laptop Operational Status: "
							+ userDetail.getLaptop_operational_status());
				}

				System.out.println("Assignment Status: " + cleanedText_laptop_assignment_uppercase);
				System.out.println("Operational Status: " + cleanText_laptop_operational_uppercase);

				String status = "ASSIGNED";
				// Laptop Assignment
				existingDetail.setLaptop_assign_or_not(true);
				existingDetail.setLaptop_status(status);
				existingDetail.setLaptop_brand(userDetail.getLaptop_brand());
				existingDetail.setLaptop_id(userDetail.getLaptop_id());
				existingDetail.setLaptop_serial_number(userDetail.getLaptop_serial_number());
				existingDetail.setLaptop_color(userDetail.getLaptop_color());
				existingDetail.setDevice_id(userDetail.getDevice_id());
				existingDetail.setLaptop_assignment_status(cleanedText_laptop_assignment_uppercase);
				existingDetail.setLaptop_operational_status(cleanText_laptop_operational_uppercase);
				existingDetail.setLaptop_assign_date(new Date());
				existingDetail.setWho_assign_laptop(currentUser);
				existingDetail.setWho_assign_laptop_employee_id(user.getId());
				existingDetail.setLaptop_model(userDetail.getLaptop_model());
				System.out.println("PRODUCT ID " + userDetail.getLaptop_id() + " ,SERIAL NUMBER "
						+ userDetail.getLaptop_serial_number() + " ,BRAND " + userDetail.getLaptop_brand()
						+ " ,COMPANY ID " + existingDetail.getCompany_id() + " ,STATUS "
						+ existingDetail.getLaptop_status());
				int count = laptopDao.existsByProductIdAndSerialNumberAndBrand(userDetail.getLaptop_model(),
						userDetail.getLaptop_id(), userDetail.getLaptop_serial_number(), userDetail.getLaptop_brand(),
						existingDetail.getCompany_id());
				System.out.println("PRODUCT ID " + userDetail.getLaptop_id() + " ,SERIAL NUMBER "
						+ userDetail.getLaptop_serial_number() + " ,BRAND " + userDetail.getLaptop_brand()
						+ " ,COMPANY ID " + existingDetail.getCompany_id() + " ,COUNT " + count);
				if (count == 1) {
					String laptop_model = laptopDao.getfindByBrandORModelORSerialNumberORProductId(
							existingDetail.getLaptop_brand(), existingDetail.getLaptop_model(),
							existingDetail.getLaptop_serial_number(), existingDetail.getCompany_id());

					user.setLaptop_brand(existingDetail.getLaptop_brand());
					user.setLaptop_id(existingDetail.getLaptop_id());
					user.setLaptop_serial_number(existingDetail.getLaptop_serial_number());
					user.setLaptop_assign_date(existingDetail.getLaptop_assign_date());

					GatePassGenerator gatePass = new GatePassGenerator();
					gatePass.setGatePassId(generateGatePassId());
					gatePass.setUserId(existingDetail.getId());
					gatePass.setLaptopBrand(existingDetail.getLaptop_brand());
					gatePass.setLaptopId(existingDetail.getLaptop_id());
					gatePass.setLaptop_assignment_status(existingDetail.getLaptop_assignment_status());
					gatePass.setLaptop_operational_status(existingDetail.getLaptop_operational_status());
					gatePass.setLaptopSerialNumber(existingDetail.getLaptop_serial_number());
					gatePass.setIssueDate(new Date());
					gatePass.setActionType("ASSIGNED");
					gatePass.setIssuedBy(user.getEmail());
					gatePass.setStatus("ACTIVE");
					gatePass.setAdddate(new Date());
					gatePass.setAddwho(user.getUsername());
					gatePass.setEmployee_email(existingDetail.getEmail());

					gatePassDao.save(gatePass);
					existingDetail.setGate_pass(generateGatePassId());
					existingDetail.setLaptop_model(laptop_model);

					System.out.println("LAPTOP STATUS " + existingDetail.getLaptop_status());
					// Save all changes
					userDetailDao.save(existingDetail);
					userdao.save(user);

					generateGatePassPDF(existingDetail, user, "Laptop Assigned");

					Notification notification = new Notification();
					notification.setMessage_id(generateMessageId());
					notification.setMessage(
							"A laptop has been successfully assigned to Employee ID: " + existingDetail.getId() + ".");
					notification.setUserId(existingDetail.getId());
					notification.setTimestamp(new Date());
					notification.setAdddate(new Date());
					notification.setAddwho("SYSTEM");
					notification.setEditdate(new Date());
					notification.setSenderEmail("guptaayush12418@gmail.com");
					notification.setReceiverEmail(existingDetail.getEmail());
					notification.setEditwho(existingDetail.getEditwho());
					notificationDao.save(notification);

					System.out.println("Sending notification to: " + existingDetail.getEmail());
					System.out.println("Notification: " + notification.getMessage());

					NotificationListener.send(existingDetail.getEmail(), notification); // ✅ Pass Notification object

					return true;
				} else {
					throw new Exception("No matching laptop found in the inventory.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	public String generateGatePassId() {
		Pageable pageable = PageRequest.of(0, 1);
		List<String> latestIdList = gatePassDao.findLatestGatePassId(pageable);

		if (latestIdList.isEmpty()) {
			return "GP-1001"; // First ID if table is empty
		}

		String latestId = latestIdList.get(0); // Get last saved ID
		int lastNumber = Integer.parseInt(latestId.replace("GP-", "")); // Extract number
		return "GP-" + (lastNumber + 1); // Increment ID
	}

	public String generateMessageId() {
		Pageable pageable = PageRequest.of(0, 1);
		List<String> latestIdList = notificationDao.findLatestMessageId(pageable); // update your DAO method accordingly

		if (latestIdList.isEmpty()) {
			return "MSG-1001"; // First ID if table is empty
		}

		String latestId = latestIdList.get(0); // e.g., MSG-1010
		int lastNumber = Integer.parseInt(latestId.replace("MSG-", "")); // Extract numeric part
		return "MSG-" + (lastNumber + 1); // Increment and return
	}

	private String getUpperCase(String value) {
		return (value != null) ? value.toUpperCase() : "";
	}

	public void generateGatePassPDF(UserDetail userDetail, User user, String actionType) {
		if (userDetail == null) {
			System.err.println("Error: User details are NULL.");
			return;
		}
		System.out.println("USER DETAIL " + userDetail.getUsername());

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String fileName = "GatePass_" + userDetail.getId() + "_" + actionType + "_" + timeStamp + ".pdf";
		String filePath = "C:\\FakePath\\Documents\\" + fileName;

		Document document = null;
		try {
			PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
			PdfDocument pdf = new PdfDocument(writer);
			document = new Document(pdf);

			// **Company Header & Logo**
			String logoPath = "C:\\FakePath\\Documents\\company_logo.png"; // Change to your logo path
			java.io.File logoFile = new java.io.File(logoPath);
			if (logoFile.exists()) {
				ImageData logoData = ImageDataFactory.create(logoPath);
				Image logo = new Image(logoData).setWidth(80).setHeight(80)
						.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.LEFT);
				document.add(logo);
			}
			Optional<CompanyInfo> companyInfo = company_dao.findByCompanyIdOptional(userDetail.getCompany_id());

			Paragraph headerr = new Paragraph(companyInfo.get().getCompany_name()).setBold().setFontSize(20)
					.setTextAlignment(TextAlignment.CENTER).setUnderline();
			document.add(headerr);
			Paragraph header = new Paragraph("GATE PASS").setBold().setFontSize(20)
					.setTextAlignment(TextAlignment.CENTER).setUnderline();
			document.add(header);

			String laptop_assignment = userDetail.getLaptop_assignment_status();
			String laptop_assignment_uppercase = laptop_assignment.toUpperCase();
			String cleanedText_laptop_assignment_uppercase = laptop_assignment_uppercase.replaceAll(",$", "");

			if (cleanedText_laptop_assignment_uppercase.endsWith(",")) {
				cleanedText_laptop_assignment_uppercase = cleanedText_laptop_assignment_uppercase.substring(0,
						cleanedText_laptop_assignment_uppercase.length() - 1);
			}

			String laptop_operational = userDetail.getLaptop_operational_status();
			String laptop_operational_uppercase = laptop_operational.toUpperCase();
			String cleanText_laptop_operational_uppercase = laptop_operational_uppercase.replaceAll(",$", "");

			if (cleanText_laptop_operational_uppercase.endsWith(",")) {
				cleanText_laptop_operational_uppercase = cleanText_laptop_operational_uppercase.substring(0,
						cleanText_laptop_operational_uppercase.length() - 1);
			}

			System.out.println("Assignment Status: " + cleanedText_laptop_assignment_uppercase);
			System.out.println("Operational Status: " + cleanText_laptop_operational_uppercase);

			String laptopDetails = "GATE PASS ID: " + getUpperCase(userDetail.getGate_pass()) + "\nEMPLOYEE ID: "
					+ getUpperCase(String.valueOf(userDetail.getId())) + "\nEMPLOYEE NAME: "
					+ getUpperCase(userDetail.getUsername()) + "\nEMPLOYEE EMAIL: "
					+ getUpperCase(userDetail.getEmail()) + "\nSERIAL NUMBER: "
					+ getUpperCase(userDetail.getLaptop_serial_number()) + "\nBRAND: "
					+ getUpperCase(userDetail.getLaptop_brand()) + "\nLAPTOP MODEL: "
					+ getUpperCase(userDetail.getLaptop_model()) + "\nLAPTOP ID: "
					+ getUpperCase(userDetail.getLaptop_id()) + "\nASSIGNMENT STATUS: "
					+ getUpperCase(cleanedText_laptop_assignment_uppercase) + "\nOPERATIONAL STATUS: "
					+ getUpperCase(cleanText_laptop_operational_uppercase) + "\nLAPTOP STATUS: "
					+ getUpperCase(userDetail.getLaptop_status()) + "\nDEVICE ID: "
					+ getUpperCase(userDetail.getDevice_id()) + "\nLAPTOP COLOR: "
					+ getUpperCase(userDetail.getLaptop_color()) + "\nISSUED BY: "
					+ getUpperCase(userDetail.getWho_assign_laptop()) + "\nASSIGNED DATE: "
					+ userDetail.getLaptop_assign_date();

			String safeDetails = laptopDetails.replaceAll("[:]", "_"); // Replace colons with underscores
			if (laptopDetails != null && !laptopDetails.isEmpty()) {
				BufferedImage barcodeImage = generateBarcode(safeDetails);
				if (barcodeImage != null) {
					Image pdfBarcodeImage = convertToITextImage(barcodeImage);
					document.add(pdfBarcodeImage);
				}
			}

			// **Employee & Laptop Details Table**
			document.add(new Paragraph("\nEmployee & Laptop Details").setBold().setFontSize(14));

			Table table = new Table(new float[] { 4, 8 }).setWidth(UnitValue.createPercentValue(100));
			table.setBackgroundColor(ColorConstants.WHITE);

			// Define the date format
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:MM:SS"); // or "yyyy-MM-dd"

			addStyledRow(table, "GATE PASS ID", getUpperCase(userDetail.getGate_pass()));
			addStyledRow(table, "EMPLOYEE ID", String.valueOf(userDetail.getId()).toUpperCase());
			addStyledRow(table, "EMPLOYEE NAME", getUpperCase(userDetail.getUsername()));
			addStyledRow(table, "EMPLOYEE EMAIL", getUpperCase(userDetail.getEmail()));
			addStyledRow(table, "LAPTOP MODEL", getUpperCase(userDetail.getLaptop_model()));
			addStyledRow(table, "LAPTOP BRAND", getUpperCase(userDetail.getLaptop_brand()));
			addStyledRow(table, "LAPTOP COLOR", getUpperCase(userDetail.getLaptop_color()));
			addStyledRow(table, "LAPTOP ASSIGNMENT STATUS", getUpperCase(cleanedText_laptop_assignment_uppercase));
			addStyledRow(table, "LAPTOP OPERATIONAL STATUS", getUpperCase(cleanText_laptop_operational_uppercase));
			addStyledRow(table, "LAPTOP STATUS", getUpperCase(userDetail.getLaptop_status()));
			addStyledRow(table, "PRODUCT ID", getUpperCase(userDetail.getLaptop_id()));
			addStyledRow(table, "SERIAL NUMBER", getUpperCase(userDetail.getLaptop_serial_number()));
			addStyledRow(table, "ISSUED BY", getUpperCase(userDetail.getWho_assign_laptop()));

			// Convert Date to String before passing
			String formattedDate = userDetail.getLaptop_assign_date() != null
					? dateFormat.format(userDetail.getLaptop_assign_date())
					: "N/A";
			addStyledRow(table, "ISSUED DATE", formattedDate);

			addStyledRow(table, "COMPANY NAME", companyInfo.get().getCompany_name());

			document.add(table);

			// **Signature & Date Section**
			document.add(new Paragraph("\nAuthorized Signature: _______________").setFontSize(12)
					.setTextAlignment(TextAlignment.LEFT));
			document.add(new Paragraph("Date: " + java.time.LocalDate.now()).setFontSize(12)
					.setTextAlignment(TextAlignment.LEFT));

			// **Terms & Conditions Section**
			document.add(new Paragraph("\nTERMS & CONDITIONS").setBold().setFontSize(14).setUnderline());

			com.itextpdf.layout.element.List termsList = new com.itextpdf.layout.element.List()
					.add("1. The bearer is responsible for the laptop mentioned in this pass.")
					.add("2. This gate pass is valid only for the date of issue.")
					.add("3. The laptop must be returned in the same condition as issued.")
					.add("4. Any damage or loss will be the responsibility of the employee.")
					.add("5. In case of theft or loss, an FIR must be lodged immediately and reported to IT/Admin.")
					.add("6. Unauthorized software installations or modifications are strictly prohibited.")
					.add("7. The employee must ensure that no confidential company data is stored on the laptop.")
					.add("8. The laptop should not be used for personal work or unauthorized activities.")
					.add("9. The laptop must be secured with a password-protected login at all times.")
					.add("10. The laptop should not be left unattended in public or unsecured locations.")
					.add("11. IT/Admin reserves the right to audit or inspect the laptop at any time.")
					.add("12. Loss or theft of company data may result in legal action.")
					.add("13. Upon resignation, transfer, or termination, the laptop must be returned immediately.")
					.add("14. Physical damage due to negligence will result in penalty or replacement costs.")
					.add("15. Misuse of the laptop or violation of these terms may lead to disciplinary action, including termination.");

			document.add(termsList);

			if (document != null) {
				document.close();
			}
			System.out.println("Professional Gate Pass PDF generated successfully: " + filePath);
			if (actionType.equals("Laptop Removed")) {
				String subject = "Laptop Removal Confirmation & Gate Pass";
				String message = "<html>" + "<head>" + "<style>"
						+ "body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }"
						+ ".container { padding: 20px; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; background-color: #f9f9f9; }"
						+ "h2 { color: #d9534f; }" + "p { margin: 10px 0; }"
						+ ".details { background-color: #fff; padding: 15px; border-radius: 8px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); }"
						+ ".footer { margin-top: 20px; font-size: 14px; color: #555; }" + "</style>" + "</head>"
						+ "<body>" + "<div class='container'>" + "<h2>Laptop Removal Confirmation</h2>"
						+ "<p>Dear <strong>" + userDetail.getUsername() + "</strong>,</p>"
						+ "<p>This is to inform you that the laptop previously assigned to you has been successfully removed from our system. Below are the details:</p>"
						+ "<div class='details'>" + "<p><strong>Laptop Brand:</strong> " + userDetail.getLaptop_brand()
						+ "</p>" + "<p><strong>Laptop ID:</strong> " + userDetail.getLaptop_id() + "</p>"
						+ "<p><strong>Serial Number:</strong> " + userDetail.getLaptop_serial_number() + "</p>"
						+ "<p><strong>Assigned Date:</strong> " + userDetail.getLaptop_assign_date() + "</p>"
						+ "<p><strong>Removed By:</strong> " + userDetail.getWho_assign_laptop() + "</p>"
						+ "<p><strong>IT Admin ID:</strong> " + userDetail.getWho_assign_laptop_employee_id() + "</p>"
						+ "</div>"
						+ "<p>A <strong>Gate Pass</strong> has been generated and is attached to this email. Please ensure you present it when required.</p>"
						+ "<p>If you have any questions or require further assistance, please contact the IT department.</p>"
						+ "<div class='footer'>" + "<p><strong>Best Regards,</strong><br>"
						+ companyInfo.get().getCompany_name() + " IT Support Team<br>"
						+ companyInfo.get().getCompany_phone() + "</p>" + "</div>" + "</div>" + "</body>" + "</html>";

				String to = userDetail.getEmail();
				String cc = user.getEmail();
				gatePassEmailService.sendEmail(filePath, message, subject, to, cc);
			}
			if (actionType.equals("Laptop Assigned")) {
				String subject = "Laptop Assignment Confirmation & Gate Pass";
				String message = "<html>" + "<head>" + "<style>"
						+ "body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }"
						+ ".container { padding: 20px; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; background-color: #f9f9f9; }"
						+ "h2 { color: #007bff; }" + "p { margin: 10px 0; }"
						+ ".details { background-color: #fff; padding: 15px; border-radius: 8px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); }"
						+ ".footer { margin-top: 20px; font-size: 14px; color: #555; }" + "</style>" + "</head>"
						+ "<body>" + "<div class='container'>" + "<h2>Laptop Assignment Confirmation</h2>"
						+ "<p>Dear <strong>" + userDetail.getUsername() + "</strong>,</p>"
						+ "<p>We are pleased to inform you that a laptop has been successfully assigned to you. Please find the complete details below:</p>"
						+ "<div class='details'>" + "<p><strong>Laptop Brand:</strong> " + userDetail.getLaptop_brand()
						+ "</p>" + "<p><strong>Laptop ID:</strong> " + userDetail.getLaptop_id() + "</p>"
						+ "<p><strong>Serial Number:</strong> " + userDetail.getLaptop_serial_number() + "</p>"
						+ "<p><strong>Assignment Date:</strong> " + userDetail.getLaptop_assign_date() + "</p>"
						+ "<p><strong>Assigned By:</strong> " + userDetail.getWho_assign_laptop() + "</p>"
						+ "<p><strong>IT Admin ID:</strong> " + userDetail.getWho_assign_laptop_employee_id() + "</p>"
						+ "</div>"
						+ "<p>A <strong>Gate Pass</strong> has been generated and is attached to this email. Please keep it for verification purposes.</p>"
						+ "<p>If you have any questions or require further assistance, please contact the IT department.</p>"
						+ "<div class='footer'>" + "<p><strong>Best Regards,</strong><br>"
						+ companyInfo.get().getCompany_name() + " IT Support Team<br>"
						+ companyInfo.get().getCompany_phone() + "</p>" + "</div>" + "</div>" + "</body>" + "</html>";

				// Send email with the attachment
				String to = userDetail.getEmail();
				String cc = user.getEmail();
				gatePassEmailService.sendEmail(filePath, message, subject, to, cc);
				java.io.File pdfFile = new java.io.File(filePath);

				// 2. Convert into MultipartFile using your custom class
				MultipartFile multipartFile = new FileToMultipartFile(pdfFile);

				// 3. Upload to Google Drive
				uploadFileToGoogleDrive(multipartFile, user, "ITGATEPASS");
			}
		} catch (Exception e) {
			System.err.println("Error generating PDF: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static Image convertToITextImage(BufferedImage barcodeImage) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(barcodeImage, "png", baos);
			byte[] imageBytes = baos.toByteArray();
			ImageData barcodeData = ImageDataFactory.create(imageBytes);
			return new Image(barcodeData).setWidth(120).setHeight(120)
					.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
		} catch (Exception e) {
			System.err.println("Error converting image: " + e.getMessage());
			return null;
		}
	}

	private static void addStyledRow(Table table, String label, String value) {
		table.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(12)));
		table.addCell(new Cell().add(new Paragraph(value != null ? value : "N/A").setFontSize(12)));
	}

	public static BufferedImage generateBarcode(String data) {
		try {
			int width = 500;
			int height = 500;

			QRCodeWriter barcodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = barcodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

			// Convert to BufferedImage instead of saving to file
			return MatrixToImageWriter.toBufferedImage(bitMatrix);
		} catch (WriterException e) {
			System.err.println("Error generating barcode: " + e.getMessage());
			return null;
		}
	}

	public List<Double> performance(User user, HttpSession httpSession) {
	    try {
	        Optional<Performance> performanceOpt = performancedao.findByIdField(user.getId());

	        if (!performanceOpt.isPresent()) {
	            System.err.println("No performance data found for user ID: " + user.getId());
	            return Collections.emptyList(); // Return empty list if no data found
	        }

	        Performance performance = performanceOpt.get();
	        List<Double> chartData = new ArrayList<>();

	        // Store year in session
	        httpSession.setAttribute("year", performance.getYear());

	        // Collect monthly performance data
	        chartData.add(performance.getJanuary());
	        chartData.add(performance.getFebruary());
	        chartData.add(performance.getMarch());
	        chartData.add(performance.getApril());
	        chartData.add(performance.getMay());
	        chartData.add(performance.getJune());
	        chartData.add(performance.getJuly());
	        chartData.add(performance.getAugust());
	        chartData.add(performance.getSeptember());
	        chartData.add(performance.getOctober());
	        chartData.add(performance.getNovember());
	        chartData.add(performance.getDecember());

	        return chartData;
	    } catch (Exception e) {
	        // Capture basic details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);

	        return Collections.emptyList(); // Return empty list in case of error
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
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	@Transactional
	public void login_record_save(User user, HttpSession session, String Ip_address, String location)
			throws InterruptedException {
		try {
			UserLoginDateTime userLoginDateTime = new UserLoginDateTime();
			int employee_login_record_count = userLoginDao.getLoginCount();
			if (employee_login_record_count > 0) {
				int last_sno = userLoginDao.getLastId();
				userLoginDateTime.setSno(++last_sno);
				userLoginDateTime.setId(user.getId());
				userLoginDateTime.setEmail(user.getEmail());
				userLoginDateTime.setIpAddress(Ip_address);
				userLoginDateTime.setLocation(location);
				userLoginDateTime.setUser_status(true);
				userLoginDateTime.setCompany_id(user.getCompany_id());
				userLoginDateTime.setTeam_id(user.getTeam());
				user.setUser_status(true);
				Optional<UserDetail> userDetail1 = userDetailDao.findByIdField(user.getId());
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
				System.out.println(
						"----------------------------------------INDIA ASIA/KOLKATA-------------------------------- "
								+ formattedDate);

				// Convert formattedDate back to Date object with IST time zone
				Date loginDate = parseDateInIST(formattedDate);
				userLoginDateTime.setLoginDateAndTime(loginDate);
				userLoginDateTime.setUsername(user.getUsername());
				userLoginDao.save(userLoginDateTime);
				System.out.println("FAIL ATTEMPT MSC " + user.getFailedAttempt());
				userdao.save(user);
//		Thread.sleep(1000);
				userDetailDao.save(userDetail2);
			} else {
				userLoginDateTime.setSno(1);
				userLoginDateTime.setId(user.getId());
				userLoginDateTime.setEmail(user.getEmail());
				userLoginDateTime.setIpAddress(Ip_address);
				userLoginDateTime.setLocation(location);
				userLoginDateTime.setUser_status(true);
				userLoginDateTime.setCompany_id(user.getCompany_id());
				userLoginDateTime.setTeam_id(user.getTeam());
				user.setUser_status(true);
				Optional<UserDetail> userDetail1 = userDetailDao.findByIdField(user.getId());
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
				System.out.println(
						"----------------------------------------INDIA ASIA/KOLKATA-------------------------------- "
								+ formattedDate);

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
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	private Date parseDateInIST(String dateString) throws ParseException {
//		try
//		{
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
		return sdf.parse(dateString);
//		}
	}

//	public String downtime_satus(String server) {
//		try {
//			String status = downtime_Maintaince_Dao.server_status_check(server);
//			return status;
//		} catch (Exception e) {
//	        // Basic exception details
//	        String exceptionAsString = e.toString();
//	        String className = Servicelayer.class.getName();
//	        String errorMessage = e.getMessage();
//
//	        // Capture full stack trace as a string
//	        StringWriter sw = new StringWriter();
//	        e.printStackTrace(new PrintWriter(sw));
//	        String fullStackTrace = sw.toString();
//
//	        // Extract top stack trace element (if available)
//	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
//	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
//	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;
//
//	        // Log error to database
//	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);
//
//	        // Console log for quick debugging
//	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
//	        System.err.println(fullStackTrace);
//	        return null;
//	    }
//	}

//	@Transactional
//	public void disabled_server_down_permitted(String server_name) {
//		try {
//		downtime_Maintaince_Dao.update_server_status_down(server_name);
//		} catch (Exception e) {
//	        // Basic exception details
//	        String exceptionAsString = e.toString();
//	        String className = Servicelayer.class.getName();
//	        String errorMessage = e.getMessage();
//
//	        // Capture full stack trace as a string
//	        StringWriter sw = new StringWriter();
//	        e.printStackTrace(new PrintWriter(sw));
//	        String fullStackTrace = sw.toString();
//
//	        // Extract top stack trace element (if available)
//	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
//	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
//	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;
//
//	        // Log error to database
//	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);
//
//	        // Console log for quick debugging
//	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
//	        System.err.println(fullStackTrace);
//	    }
//	}

	public boolean check_server_status(String server) {
		try {
			boolean result = downtime_Maintaince_Dao.server_status_check_active_or_not(server);
			return result;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
			return false;
		}
	}

	@Transactional
	public void correct_login_record_table() {
		try {
			userLoginDao.updateUserStatusReset();
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

//	@Transactional
//	public void enabled_server_up_permitted(String server_name) {
//		try {
//			downtime_Maintaince_Dao.update_server_status_up(server_name);
//		} catch (Exception e) {
//	        // Basic exception details
//	        String exceptionAsString = e.toString();
//	        String className = Servicelayer.class.getName();
//	        String errorMessage = e.getMessage();
//
//	        // Capture full stack trace as a string
//	        StringWriter sw = new StringWriter();
//	        e.printStackTrace(new PrintWriter(sw));
//	        String fullStackTrace = sw.toString();
//
//	        // Extract top stack trace element (if available)
//	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
//	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
//	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;
//
//	        // Log error to database
//	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);
//
//	        // Console log for quick debugging
//	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
//	        System.err.println(fullStackTrace);
//	    }
//	}

	@Transactional
	public void syncEmployeeAndEmployeeDetailTable() {
		try {
			// Fetch all User and UserDetail data
			List<User> users = userdao.findAll();
			List<UserDetail> userDetails = userDetailDao.findAll();

			// Create a map of UserDetail by id (Integer) for quick access
			Map<Integer, UserDetail> userDetailMap = userDetails.stream()
					.collect(Collectors.toMap(UserDetail::getId, userDetail -> userDetail));

			// Loop through all users and update UserDetail if needed
			for (User user : users) {
				UserDetail userDetail = userDetailMap.get(user.getId());

				if (userDetail != null && !user.getUsername().equals(userDetail.getUsername())) {
					// Update only if the usernames are different
					userDetail.setUsername(user.getUsername());
					userDetailDao.save(userDetail); // Save the updated UserDetail
				}
				if (userDetail != null && !user.getEmail().equals(userDetail.getEmail())) {
					// Update only if the usernames are different
					userDetail.setEmail(user.getEmail());
					userDetailDao.save(userDetail); // Save the updated UserDetail
				}
				if (userDetail != null && !user.getDesignation().equals(userDetail.getDesignation())) {
					// Update only if the usernames are different
					userDetail.setDesignation(user.getDesignation());
					userDetailDao.save(userDetail); // Save the updated UserDetail
				}
				if (userDetail != null
						&& !user.getBank_account_holder_name().equals(userDetail.getBank_account_holder_name())) {
					// Update only if the usernames are different
					userDetail.setDesignation(user.getBank_account_holder_name());
					userDetailDao.save(userDetail); // Save the updated UserDetail
				}
			}

			// Log that the job has finished running
			jobrunning("sync_employee_and_employeedetail_table");

		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

    // Helper Methods
	
	public User get_user(String email) {
		try {
			User user = userdao.getUserByUserName(email);
			return user;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
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
			Optional<User> OptionalUser = userdao.findByIdField(userDetail.getId());
			User GetOptionalUser = OptionalUser.get();
			GetOptionalUser.setTeam(team_id);
			userdao.save(GetOptionalUser);
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	@Transactional
	public void processing_payment(Order order, Principal principal) {
		org.slf4j.Logger logger = LoggerFactory.getLogger(Servicelayer.class);

		try {
			Payment_Order_Info order_Info = new Payment_Order_Info();
			Optional<User> user = userdao.findByEmail(principal.getName());

			if (user.isEmpty()) {
				throw new Exception("User not found for email: " + principal.getName());
			}

			User user1 = user.get();

			// Get the amount in paise as a BigDecimal for precise calculations
			BigDecimal amtInPaise = new BigDecimal(order.get("amount").toString());

			// Convert paise to rupees with decimal precision
			BigDecimal paiseToRupee = amtInPaise.divide(BigDecimal.valueOf(100));

			// Populate the Payment_Order_Info object
			order_Info.setAmount(paiseToRupee.floatValue()); // Save as float
			order_Info.setId(user1.getId());
			order_Info.setOrderId(order.get("id").toString()); // Ensure the ID is a string
			order_Info.setPaymentId(null);
			order_Info.setStatus("created");
			order_Info.setReceipt(order.get("receipt").toString());
			order_Info.setEmail(user1.getEmail());
			order_Info.setPhone(user1.getPhone());
			order_Info.setCompany(user1.getCompany());
			order_Info.setSystem_date_and_time(new Date());
			order_Info.setCompany_id(user1.getCompany_id());
			order_Info.setLicense_number("No Record Found");
			order_Info.setGst_no("No Record Found");
			order_Info.setLicense_status("No Record Found");
			order_Info.setPaymentId("No Record Found");
			order_Info.setTax("No Record Found");
			order_Info.setQty("0");
			order_Info.setBase_location(user1.getBase_location());
			order_Info.setUsername(user1.getUsername());

			// Save to the database
			orderDao.save(order_Info);

			// Log success
			logger.info("Payment processing completed successfully for order ID: {}", order.get("id").toString());

		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	public String generateLicenseNumber() {
		try
		{
		UUID uuid = UUID.randomUUID();
		String uuidStr = uuid.toString().replace("-", ""); // Remove hyphens
		String formattedLicense = uuidStr.substring(0, 4) + "-" + uuidStr.substring(4, 8) + "-"
				+ uuidStr.substring(8, 12) + "-" + uuidStr.substring(12, 16); // Extract first 16 characters
		return formattedLicense.toUpperCase(); // Convert to uppercase (optional)
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	       insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return null;
	    }
	}

	@Transactional
	public boolean update_payment(User user, @RequestBody Map<String, Object> data) {
		try {
			// Fetch and update payment information
			Payment_Order_Info payment_Order_Info = orderDao.findByOrderId(data.get("order_id").toString());
			if (!data.get("status").toString().equals("pending")) {
				payment_Order_Info.setPaymentId(data.get("payment_id").toString());
				payment_Order_Info.setStatus(data.get("status").toString());

				// Generate a new license number
				String licenseNumber = generateLicenseNumber();
				payment_Order_Info.setLicense_number(licenseNumber);

				// Fetch subscription plans
				SubscriptionPlans subscriptionPlans = findSubscriptionPlans();
				float gst = subscriptionPlans.getGst() * 100;
				String gst_no = Float.toString(gst);
				payment_Order_Info.setDiscount(subscriptionPlans.getDiscount());
				payment_Order_Info.setTax(gst_no + '%');

				// Calculate GST amount
				Optional<SubscriptionPlans> subscriptionPlansOptional = subscriptionPlansDao.getAllPlans();
				SubscriptionPlans subscriptionPlans2 = subscriptionPlansOptional.get();
				payment_Order_Info.setGst_amount(payment_Order_Info.getAmount() * subscriptionPlans2.getGst());

				// Extract validity days from plan description
				String validity = subscriptionPlans2.getPlan_description();
				String[] extractValidity = validity.trim().split("\\s+");
				int validityDays = Integer.parseInt(extractValidity[1]);

				Instant now = Instant.now();
				Instant subscriptionStartDate;
				Instant subscriptionExpiryDate;

				// Fetch the last active or waiting plan
				Payment_Order_Info lastPlan = orderDao.findLastPlanByCompanyId(user.getCompany_id());

				if (lastPlan == null) {
					// No active or waiting plan, activate immediately
					subscriptionStartDate = now;
					subscriptionExpiryDate = now.plus(Duration.ofDays(validityDays));
					payment_Order_Info.setLicense_status("ACTIVE");
					update_enable_user_after_success_payment(user.getCompany_id());
				} else {
					// If an active or waiting plan exists, queue the new plan
					subscriptionStartDate = lastPlan.getSubscription_expiry_date().toInstant();
					subscriptionExpiryDate = subscriptionStartDate.plus(Duration.ofDays(validityDays));
					payment_Order_Info.setLicense_status("WAITING");
				}

				payment_Order_Info.setSubscription_start_date(Date.from(subscriptionStartDate));
				payment_Order_Info.setSubscription_expiry_date(Date.from(subscriptionExpiryDate));
				payment_Order_Info.setValidity(validityDays);

				// Calculate amount without GST
				float withoutGstAmount = payment_Order_Info.getAmount() - payment_Order_Info.getGst_amount();
				payment_Order_Info.setAmount_without_gst(withoutGstAmount);

				// Fetch company info
				CompanyInfo companyInfo = findCompanyInfo();
				payment_Order_Info.setGst_no(companyInfo.getGst_no());
				payment_Order_Info.setQty(subscriptionPlans.getQty());

				// Save the updated payment information
				orderDao.save(payment_Order_Info);

				// Send invoice after successful update
				try {
					generateAndSendInvoice(payment_Order_Info, subscriptionPlans, companyInfo, user, licenseNumber);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Failed to send invoice email. Payment info updated successfully.");
				}
			} else {
				payment_Order_Info.setStatus(data.get("status").toString());
				orderDao.save(payment_Order_Info);
			}
			return true;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
			return false;
		}
	}

	public CompanyInfo fetch_date_from_company_records(String company_id) {
		try {
			CompanyInfo companyInfo = new CompanyInfo();
			companyInfo = company_dao.getCompanyByCompanyId(company_id);
			return companyInfo;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return null;
	    }
	}

	@Transactional
	public void update_enable_user_after_success_payment(String company_id) {
		try {
			userdao.update_user_enabled_after_success_payment(company_id);
			userDetailDao.update_user_enabled_after_success_payment(company_id);
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

//	@Transactional
//	public void disbaled_expired_plan_users(String jobname) {
//		try {
//			List<Payment_Order_Info> order = orderDao.findAll();
//			ListIterator<Payment_Order_Info> orders_iterate = order.listIterator();
//			while (orders_iterate.hasNext()) {
//				Payment_Order_Info payment_Order_Info = orders_iterate.next();
//				if (payment_Order_Info.getStatus().equalsIgnoreCase("paid")) {
//					int get_output = orderDao.check_users_subscription_plan(payment_Order_Info.getCompany_id());
//					System.out.println("BEFORE WHILE LOOP " + get_output + " " + payment_Order_Info.getCompany_id());
//					if (get_output > 0) {
//						System.out.println("AFTER WHILE LOOP " + get_output + " " + payment_Order_Info.getCompany_id());
//						userdao.disbaled_expired_plan_users(payment_Order_Info.getCompany_id());
//						orderDao.expired_license_status(payment_Order_Info.getCompany_id());
//					} else {
//						continue;
//					}
//				} else {
//					continue;
//				}
//			}
//			jobrunning("disbaled_expired_plan_users");
//		}
//
//		catch (Exception e) {
//			jobDao.getJobRunningTimeInterrupted("disbaled_expired_plan_users");
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = Servicelayer.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			e.printStackTrace();
//			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//
//		}
//	}

//	@Transactional
//	public void disbaled_expired_plan_users() {
//		try {
//			List<Payment_Order_Info> order = orderDao.findAllByActive();
//			ListIterator<Payment_Order_Info> orders_iterate = order.listIterator();
//			while (orders_iterate.hasNext()) {
//				Payment_Order_Info payment_Order_Info = orders_iterate.next();
//
//				// Check if the payment status is "paid"
//				if (payment_Order_Info.getStatus().equalsIgnoreCase("paid")) {
//
//					int validityDays = payment_Order_Info.getValidity(); // e.g., 30 days
//					Date expiryDate = payment_Order_Info.getSubscription_expiry_date();
//					String company_id = payment_Order_Info.getCompany_id();
//
//					// Get the current date
//					LocalDate currentDate = LocalDate.now();
//
//					// Convert expiryDate to LocalDate
//					LocalDate expiryLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//
//					// Calculate the remaining days between the current date and the expiry date
//					long remainingDays = Duration.between(currentDate.atStartOfDay(), expiryLocalDate.atStartOfDay())
//							.toDays();
//					System.out.println("REMAINING DAYS: " + remainingDays + " | VALIDITY DAYS: " + validityDays);
//
//					// Trigger the expiry process only if remaining days are less than or equal to 0
//					if (remainingDays < 0 && payment_Order_Info.getLicense_status().equals("ACTIVE")) {
//						System.out.println("Expiring license for company: " + company_id);
//
//						// Call your methods to handle expired licenses and disable users with expired
//						// plans
//						orderDao.expired_license_status(company_id);
//						userdao.disbaled_expired_plan_users(company_id);
//						userDetailDao.disbaled_expired_plan_users(company_id);
//						String subject = "Your Subscription Has Expired – Renew to Stay Connected";
//						String to = payment_Order_Info.getEmail();
//						String message = "" +
//						        "<!DOCTYPE html>" +
//						        "<html lang='en'>" +
//						        "<head>" +
//						        "    <meta charset='UTF-8'>" +
//						        "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
//						        "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" +
//						        "    <style>" +
//						        "        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }" +
//						        "        .email-wrapper { width: 100%; padding: 40px 0; display: flex; justify-content: center; align-items: center; background-color: #f4f6f9; }" +
//						        "        .email-container { max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1); text-align: center; margin: 0 auto; }" +
//						        "        .email-header { background-color: #dc3545; color: white; text-align: center; padding: 20px; font-size: 24px; font-weight: bold; }" +
//						        "        .email-body { padding: 30px; text-align: left; }" +
//						        "        .email-body p { margin: 0 0 20px 0; line-height: 1.6; font-size: 16px; }" +
//						        "        .email-body .warning-box { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 20px; margin-bottom: 30px; border-radius: 5px; }" +
//						        "        .email-body .warning-box h2 { margin: 0; color: #856404; font-size: 22px; }" +
//						        "        .email-body .details-box { padding: 20px; border-radius: 5px; border: 1px solid #ddd; background-color: #fafafa; margin-bottom: 30px; }" +
//						        "        .email-body .details-box p { font-size: 15px; margin: 8px 0; }" +
//						        "        .email-footer { background-color: #f7f9fc; padding: 20px; text-align: center; font-size: 14px; color: #555555; }" +
//						        "        .email-footer a { color: #007bff; text-decoration: none; font-weight: bold; }" +
//						        "    </style>" +
//						        "</head>" +
//						        "<body>" +
//						        "    <div class='email-wrapper'>" +
//						        "        <div class='email-container'>" +
//						        "            <div class='email-header'>Subscription Expired</div>" +
//						        "            <div class='email-body'>" +
//						        "                <div class='warning-box'>" +
//						        "                    <h2>Your Subscription Has Ended</h2>" +
//						        "                </div>" +
//						        "                <p>Dear " + payment_Order_Info.getUsername() + ",</p>" +
//						        "                <p>Your subscription to <strong>[Pro Plus]</strong> expired on <strong>" + payment_Order_Info.getSystem_date_and_time() + "</strong>. To continue enjoying uninterrupted service, please renew your subscription.</p>" +
//						        "                <div class='details-box'>" +
//						        "                    <p><strong>Username:</strong> " + payment_Order_Info.getUsername() + "</p>" +
//						        "                    <p><strong>Email:</strong> " + payment_Order_Info.getEmail() + "</p>" +
//						        "                    <p><strong>License Number:</strong> " + payment_Order_Info.getLicense_number() + "</p>" +
//						        "                    <p><strong>Last Active Date:</strong> " + payment_Order_Info.getSystem_date_and_time() + "</p>" +
//						        "                    <p><strong>License Status:</strong> <span style='color: red;'>INACTIVE</span></p>" +
//						        "                </div>" +
//						        "                <p>Renew now to avoid losing access to your benefits.</p>" +
//						        "                <p style='text-align: center;'><a href='https://wwwemscom-production.up.railway.app/signin' style='background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; font-weight: bold; border-radius: 5px; display: inline-block;'>Renew Now</a></p>" +
//						        "                <p>If you have any questions, feel free to contact our support team at [Support Contact Info].</p>" +
//						        "                <p>Best regards,<br>Subscription Team</p>" +
//						        "            </div>" +
//						        "            <div class='email-footer'>" +
//						        "                <p>Need help? <a href='#'>Contact Support</a> or visit our <a href='#'>Help Center</a>.</p>" +
//						        "            </div>" +
//						        "        </div>" +
//						        "    </div>" +
//						        "</body>" +
//						        "</html>";
//
//						emailService.sendEmail(message, subject, to);
//
//					}
//				}
//			}
////			jobrunning("disbaled_expired_plan_users");
//		} catch (Exception e) {
//			jobDao.getJobRunningTimeInterrupted("Disabled Expired Plan users");
//
//			e.printStackTrace();
//		}
//	}

	@Transactional
	public void disbaled_expired_plan_users() {
		try {
			List<Payment_Order_Info> orderList = orderDao.findAllByActive();
			ListIterator<Payment_Order_Info> orders_iterate = orderList.listIterator();

			while (orders_iterate.hasNext()) {
				Payment_Order_Info currentPlan = orders_iterate.next();

				// Check if the payment status is "PAID"
				if (currentPlan.getStatus().equalsIgnoreCase("PAID")) {

					int validityDays = currentPlan.getValidity(); // e.g., 30 days
					Date expiryDate = currentPlan.getSubscription_expiry_date();
					String companyId = currentPlan.getCompany_id();

					// Get the current date
					LocalDate currentDate = LocalDate.now();

					// Convert expiryDate to LocalDate
					LocalDate expiryLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

					// Calculate the remaining days between the current date and the expiry date
					long remainingDays = Duration.between(currentDate.atStartOfDay(), expiryLocalDate.atStartOfDay())
							.toDays();
					System.out.println("REMAINING DAYS: " + remainingDays + " | VALIDITY DAYS: " + validityDays
							+ " | TRANSACTION ID: " + currentPlan.getPaymentId());

					// If the subscription has expired and is still active, process the renewal
					// check
					if (remainingDays < 0 && currentPlan.getLicense_status().equals("ACTIVE")) {
						System.out.println("Checking for renewal for company: " + companyId);

						// Check if there's an upcoming recharge in "WAIT" status
						Payment_Order_Info upcomingPlan = orderDao.findUpcomingPlanByCompanyId(companyId);
//	                    System.out.println("UPCOMING REMAINING DAYS: " + remainingDays + " | UPCOMING VALIDITY DAYS: " + validityDays + " | UPCOMING TRANSACTION ID: "+upcomingPlan.getPaymentId()+" | LICENSE STATUS: "+upcomingPlan.getLicense_status()+" | UPCOMING STATUS: "+upcomingPlan.getStatus()+" EMAIL: "+upcomingPlan.getEmail());
						if (upcomingPlan != null && "WAITING".equalsIgnoreCase(upcomingPlan.getLicense_status())
								&& "PAID".equalsIgnoreCase(upcomingPlan.getStatus())) {
							System.out.println("UPCOMING REMAINING DAYS: " + remainingDays
									+ " | UPCOMING VALIDITY DAYS: " + validityDays + " | UPCOMING TRANSACTION ID: "
									+ upcomingPlan.getPaymentId() + " | LICENSE STATUS: "
									+ upcomingPlan.getLicense_status() + " | UPCOMING STATUS: "
									+ upcomingPlan.getStatus() + " EMAIL: " + upcomingPlan.getEmail());
							// Auto-renew the upcoming plan
//	                        LocalDate newExpiryDate = currentDate.plusDays(upcomingPlan.getValidity());
//	                        Date newExpiry = Date.from(newExpiryDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
							orderDao.expired_license_status(companyId);
							orderDao.updatePlanToActive(companyId);
							System.out.println("New plan activated for company: " + companyId);

							// Send renewal confirmation email
							String to = upcomingPlan.getEmail();
							String cc = " ";
							String subject = "Your Subscription Plan Has Been Successfully Activated";
							String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>"
									+ "    <meta charset='UTF-8'>"
									+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
									+ "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + "    <style>"
									+ "        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }"
									+ "        .email-wrapper { width: 100%; padding: 40px 0; display: flex; justify-content: center; align-items: center; background-color: #f4f6f9; }"
									+ "        .email-container { max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1); text-align: center; margin: 0 auto; }"
									+ "        .email-header { background-color: #007bff; color: white; text-align: center; padding: 20px; font-size: 24px; font-weight: bold; }"
									+ "        .email-body { padding: 30px; text-align: left; }"
									+ "        .email-body p { margin: 0 0 20px 0; line-height: 1.6; font-size: 16px; }"
									+ "        .email-body .info-box { padding: 20px; border-radius: 5px; border: 1px solid #ddd; background-color: #fafafa; margin-bottom: 30px; }"
									+ "        .email-body .info-box p { font-size: 15px; margin: 8px 0; }"
									+ "        .email-footer { background-color: #f7f9fc; padding: 20px; text-align: center; font-size: 14px; color: #555555; }"
									+ "        .email-footer a { color: #007bff; text-decoration: none; font-weight: bold; }"
									+ "    </style>" + "</head>" + "<body>" + "    <div class='email-wrapper'>"
									+ "        <div class='email-container'>"
									+ "            <div class='email-header'>Subscription Activated</div>"
									+ "            <div class='email-body'>" + "                <p>Dear "
									+ upcomingPlan.getUsername() + ",</p>"
									+ "                <p>We are pleased to inform you that your new subscription plan has been activated automatically following your previous plan's expiry.</p>"
									+ "                <div class='info-box'>"
									+ "                    <p><strong>Plan:</strong> [Pro Plus]</p>"
									+ "                    <p><strong>Activation Date:</strong> "
									+ upcomingPlan.getSubscription_start_date() + "</p>"
									+ "                    <p><strong>Expiration Date:</strong> "
									+ upcomingPlan.getSubscription_expiry_date() + "</p>"
									+ "                    <p><strong>License Number:</strong> "
									+ upcomingPlan.getLicense_number() + "</p>"
									+ "                    <p><strong>Status:</strong> <span style='color: green;'>ACTIVE</span></p>"
									+ "                </div>"
									+ "                <p>You can access your subscription details anytime by logging into your account.</p>"
									+ "                <p style='text-align: center;'><a href='https://wwwemscom-production.up.railway.app/signin' style='background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; font-weight: bold; border-radius: 5px; display: inline-block;'>Go to Dashboard</a></p>"
									+ "                <p>If you have any questions or need assistance, feel free to contact our support team.</p>"
									+ "                <p>Best regards,<br>Subscription Team</p>" + "            </div>"
									+ "            <div class='email-footer'>"
									+ "                <p>Need help? <a href='#'>Contact Support</a> or visit our <a href='#'>Help Center</a>.</p>"
									+ "            </div>" + "        </div>" + "    </div>" + "</body>" + "</html>";

							System.out.println("UPCOMING BASE LOCATION : " + upcomingPlan.getBase_location());
							System.out.println("UPCOMING COMPANY ID : " + upcomingPlan.getBase_location());
							System.out.println("UPCOMING COMPANY ID : " + upcomingPlan.getId());
							List<String> Emails = userDetailDao.findEmailsByBaseLocationAndRoles(
									upcomingPlan.getBase_location(), upcomingPlan.getCompany_id(),
									upcomingPlan.getId());

							// Sending email to HR (assuming you're passing HR emails as cc)
							CompletableFuture<Boolean> flagFuture = listEmailService.sendEmail(message, subject, to, cc,
									Emails);

							try {
								Boolean emailSent = flagFuture.get(); // Await the result of the CompletableFuture

								List<UserDetail> ids = userDetailDao.findIdsByBaseLocationAndRoles(
										upcomingPlan.getBase_location(), upcomingPlan.getCompany_id(),
										upcomingPlan.getId());

								System.out.println("WITHDRAWN SIZE " + ids.size());

								if (emailSent != null && emailSent) {
									for (UserDetail detail : ids) {
										Notification notification = new Notification();
										notification.setMessage_id(generateMessageId());
										notification.setMessage(
												"Your Subscription Plan Has Been Successfully Activated With License ID "
														+ upcomingPlan.getLicense_number());
										notification.setUserId(detail.getId());
										notification.setTimestamp(new Date());
										notification.setAdddate(new Date());
										notification.setAddwho("SYSTEM");
										notification.setEditdate(new Date());
										notification.setEditwho(detail.getEditwho());
										notification.setSenderEmail(upcomingPlan.getEmail());
										notification.setReceiverEmail(detail.getEmail());

										notificationDao.save(notification);
										NotificationListener.send(detail.getEmail(), notification);

										System.out.println("Sending notification to: " + detail.getEmail());
									}
								} else {
									System.out.println("Email not sent to HR. Skipping notifications.");
								}
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("Failed to send email or process notifications.");
							}

						} else {
							// No upcoming recharge found, expire the plan
							System.out.println("Expiring license for company: " + companyId);

							orderDao.expired_license_status(companyId);
							userdao.disbaled_expired_plan_users(companyId);
							userDetailDao.disbaled_expired_plan_users(companyId);

							// Send expiration email
							String subject = "Your Subscription Has Expired – Renew to Stay Connected";
							String to = currentPlan.getEmail();
							String cc = " ";
							String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>"
									+ "    <meta charset='UTF-8'>"
									+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
									+ "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + "    <style>"
									+ "        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }"
									+ "        .email-wrapper { width: 100%; padding: 40px 0; display: flex; justify-content: center; align-items: center; background-color: #f4f6f9; }"
									+ "        .email-container { max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1); text-align: center; margin: 0 auto; }"
									+ "        .email-header { background-color: #dc3545; color: white; text-align: center; padding: 20px; font-size: 24px; font-weight: bold; }"
									+ "        .email-body { padding: 30px; text-align: left; }"
									+ "        .email-body p { margin: 0 0 20px 0; line-height: 1.6; font-size: 16px; }"
									+ "        .email-body .warning-box { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 20px; margin-bottom: 30px; border-radius: 5px; }"
									+ "        .email-body .warning-box h2 { margin: 0; color: #856404; font-size: 22px; }"
									+ "        .email-body .details-box { padding: 20px; border-radius: 5px; border: 1px solid #ddd; background-color: #fafafa; margin-bottom: 30px; }"
									+ "        .email-body .details-box p { font-size: 15px; margin: 8px 0; }"
									+ "        .email-footer { background-color: #f7f9fc; padding: 20px; text-align: center; font-size: 14px; color: #555555; }"
									+ "        .email-footer a { color: #007bff; text-decoration: none; font-weight: bold; }"
									+ "    </style>" + "</head>" + "<body>" + "    <div class='email-wrapper'>"
									+ "        <div class='email-container'>"
									+ "            <div class='email-header'>Subscription Expired</div>"
									+ "            <div class='email-body'>"
									+ "                <div class='warning-box'>"
									+ "                    <h2>Your Subscription Has Ended</h2>"
									+ "                </div>" + "                <p>Dear " + currentPlan.getUsername()
									+ ",</p>"
									+ "                <p>Your subscription to <strong>[Pro Plus]</strong> expired on <strong>"
									+ currentPlan.getSubscription_expiry_date()
									+ "</strong>. To continue enjoying uninterrupted service, please renew your subscription.</p>"
									+ "                <div class='details-box'>"
									+ "                    <p><strong>Username:</strong> " + currentPlan.getUsername()
									+ "</p>" + "                    <p><strong>Email:</strong> "
									+ currentPlan.getEmail() + "</p>"
									+ "                    <p><strong>License Number:</strong> "
									+ currentPlan.getLicense_number() + "</p>"
									+ "                    <p><strong>Last Active Date:</strong> "
									+ currentPlan.getSystem_date_and_time() + "</p>"
									+ "                    <p><strong>License Status:</strong> <span style='color: red;'>INACTIVE</span></p>"
									+ "                </div>"
									+ "                <p>Renew now to avoid losing access to your benefits.</p>"
									+ "                <p style='text-align: center;'><a href='https://wwwemscom-production.up.railway.app/signin' style='background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; font-weight: bold; border-radius: 5px; display: inline-block;'>Renew Now</a></p>"
									+ "                <p>If you have any questions, feel free to contact our support team at [Support Contact Info].</p>"
									+ "                <p>Best regards,<br>Subscription Team</p>" + "            </div>"
									+ "            <div class='email-footer'>"
									+ "                <p>Need help? <a href='#'>Contact Support</a> or visit our <a href='#'>Help Center</a>.</p>"
									+ "            </div>" + "        </div>" + "    </div>" + "</body>" + "</html>";

							List<String> Emails = userDetailDao.findEmailsByBaseLocationAndRoles(
									currentPlan.getBase_location(), currentPlan.getCompany_id(), currentPlan.getId());
							System.out.println("CURREN PLAN TO: " + to);
							System.out.println("CURRENT PLAN CC: " + cc);
							System.out.println("CURRENT PLAN EMAILS LIST: " + Emails);
							// Sending email to HR (assuming you're passing HR emails as cc)
							CompletableFuture<Boolean> flagFuture = listEmailService.sendEmail(message, subject, to, cc,
									Emails);

							try {
								// Wait for the email sending task to complete
								Boolean emailSent = flagFuture.get(); // blocking call

								List<UserDetail> ids = userDetailDao.findIdsByBaseLocationAndRoles(
										currentPlan.getBase_location(), currentPlan.getCompany_id(),
										currentPlan.getId());

								System.out.println("CURRENT PLAN SIZE " + ids.size());
								System.out.println("CURRENT BASE LOCATION : " + currentPlan.getBase_location());
								System.out.println("CURRENT COMPANY ID : " + currentPlan.getCompany_id());
								System.out.println("CURRENT ID : " + currentPlan.getId());

								if (Boolean.TRUE.equals(emailSent)) {
									for (UserDetail detail : ids) {
										Notification notification = new Notification();
										notification.setMessage_id(generateMessageId());
										notification
												.setMessage("Your Subscription Plan Has Been Expired With License ID "
														+ currentPlan.getLicense_number());
										notification.setUserId(detail.getId());
										notification.setTimestamp(new Date());
										notification.setAdddate(new Date());
										notification.setAddwho("SYSTEM");
										notification.setEditdate(new Date());
										notification.setEditwho(detail.getEditwho());
										notification.setSenderEmail(currentPlan.getEmail());
										notification.setReceiverEmail(detail.getEmail());

										notificationDao.save(notification);
										NotificationListener.send(detail.getEmail(), notification);
										System.out.println("Sending notification to: " + detail.getEmail());
									}
								} else {
									System.out.println("Email not sent to HR. Notifications skipped.");
								}
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("Failed to send email or generate notifications.");
							}

						}
					}
				}
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	public User findByUsername(String email) {
		try {
			Optional<User> user = userdao.findByEmail(email);
			if (user.isPresent()) {
				User user1 = user.get();
				return user1;
			} else {
				// Log or handle the case when user is not found
				System.out.println("No user found with email: " + email);
				return null; // or throw a custom exception
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
			return null;
		}
	}

	public Payment_Order_Info findOrderByCompanyId(String transaction_id) {
		try {
			Optional<Payment_Order_Info> payment_Order_Info = orderDao.findOrderByTransactionId(transaction_id);
			Payment_Order_Info payment_Order_Info2 = payment_Order_Info.get();
			return payment_Order_Info2;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
			return null;
		}
	}

	public CompanyInfo findCompanyInfo() {
		try {
			CompanyInfo companyInfo = company_dao.getCompany();
			return companyInfo;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
			return null;
		}
	}

	public SubscriptionPlans findSubscriptionPlans() {
		try {
			Optional<SubscriptionPlans> subscriptionPlans = subscriptionPlansDao.getAllPlans();
			SubscriptionPlans subscriptionPlans2 = subscriptionPlans.get();
			return subscriptionPlans2;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
			return null;
		}
	}

	public List<Payment_Order_Info> transaction_history(String company_id) {
		try {
			List<Payment_Order_Info> payment_Order_Infos = orderDao.transactionHistoryFindByCompanyId(company_id);
			return payment_Order_Infos;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
			return null;
		}
	}

	public SubscriptionPlans getAllPlans() {
		try {
			Optional<SubscriptionPlans> subscriptionPlans = subscriptionPlansDao.getAllPlans();
			SubscriptionPlans subscriptionPlans2 = subscriptionPlans.get();
			return subscriptionPlans2;
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
			return null;
		}
	}

//	@Transactional
//	public void expired_license_status() {
//		try {
//			int order_count = orderDao.countt();
//			if (order_count > 0) {
//				List<Payment_Order_Info> info = orderDao.findAll();
//				for (Payment_Order_Info order_Info : info) {
//					int validityDays = order_Info.getValidity();
//					Date startDate = order_Info.getSubscription_start_date();
//					Date ExpiryDate = order_Info.getSubscription_expiry_date();
//					String company_id = order_Info.getCompany_id();
//					int expire_order_count = orderDao.check_users_subscription_plan(company_id);
//					if (expire_order_count > 0) {
//						orderDao.expired_license_status(company_id);
//						userdao.disbaled_expired_plan_users(company_id);
//					}
//				}
//				jobrunning("expired_license_status");
//			}
//		} catch (Exception e) {
//			jobDao.getJobRunningTimeInterrupted("expired_license_status");
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = Servicelayer.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			e.printStackTrace();
//			insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//
//		}
//	}

//	@Transactional
//	public void expired_license_status() {
//	    try {
//	        int order_count = orderDao.countt();
//	        if (order_count > 0) {
//	            List<Payment_Order_Info> info = orderDao.findAll();
//	            for (Payment_Order_Info order_Info : info) {
//	                int validityDays = order_Info.getValidity();
//	                Date startDate = order_Info.getSubscription_start_date();
//	                Date expiryDate = order_Info.getSubscription_expiry_date();
//	                String company_id = order_Info.getCompany_id();
//
//	                // Convert Date to LocalDate
//	                LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//	                LocalDate expiryLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//
//	                // Calculate the number of days between startDate and expiryDate
//	                long subscriptionDays = Duration.between(startLocalDate.atStartOfDay(), expiryLocalDate.atStartOfDay()).toDays();
//
//	                // If validityDays is greater than subscriptionDays, enter the if condition
//	                if (validityDays > subscriptionDays) {
////	                    int expire_order_count = orderDao.check_users_subscription_plan(company_id);
////	                    if (expire_order_count > 0) {
//	                        orderDao.expired_license_status(company_id);
//	                        userdao.disbaled_expired_plan_users(company_id);
////	                    }
//	                }
//	            }
//	            jobrunning("expired_license_status");
//	        }
//	    } catch (Exception e) {
//	        jobDao.getJobRunningTimeInterrupted("expired_license_status");
//	        String exceptionAsString = e.toString();
//
//	        // Get the current class
//	        Class<?> currentClass = Servicelayer.class;
//
//	        // Get the name of the class
//	        String className = currentClass.getName();
//	        String errorMessage = e.getMessage();
//	        StackTraceElement[] stackTrace = e.getStackTrace();
//	        String methodName = stackTrace[0].getMethodName();
//	        int lineNumber = stackTrace[0].getLineNumber();
//	        e.printStackTrace();
//	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//	    }
//	}

//	private static final String APPLICATION_NAME = "Employee Management System";
//    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//    private static final String SERVICE_ACCOUNT_KEY_PATH = "src/main/resources/credentials.json"; 
//    // 👆 Service account JSON key ka path
//
//    // ✅ Initialize Google Drive Service
//    public static Drive getDriveService() throws IOException, GeneralSecurityException {
//        GoogleCredentials credentials = GoogleCredentials
//                .fromStream(new FileInputStream(SERVICE_ACCOUNT_KEY_PATH))
//                .createScoped(Collections.singleton(DriveScopes.DRIVE));
//
//        return new Drive.Builder(
//                GoogleNetHttpTransport.newTrustedTransport(),
//                JSON_FACTORY,
//                new HttpCredentialsAdapter(credentials))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//    }
//
//    // ✅ Upload file to Google Drive
//    public static String uploadFile(String filePath, String fileName, String folderId)
//            throws IOException, GeneralSecurityException {
//
//        Drive driveService = getDriveService();
//
//        File fileMetadata = new File();
//        fileMetadata.setName(fileName);
//
//        if (folderId != null && !folderId.isEmpty()) {
//            fileMetadata.setParents(Collections.singletonList(folderId));
//        }
//
//        java.io.File filePathObj = new java.io.File(filePath);
//
//        String mimeType = getMimeType(filePathObj);
//        FileContent mediaContent = new FileContent(mimeType, filePathObj);
//
//        File uploadedFile = driveService.files()
//                .create(fileMetadata, mediaContent)
//                .setFields("id, name")
//                .execute();
//
//        System.out.println("File uploaded: " + uploadedFile.getName() + " (ID: " + uploadedFile.getId() + ")");
//        return uploadedFile.getId();
//    }
//
//    // ✅ Detect MIME type based on file extension
//    private static String getMimeType(java.io.File file) {
//        String fileName = file.getName().toLowerCase();
//        if (fileName.endsWith(".txt")) return "text/plain";
//        if (fileName.endsWith(".pdf")) return "application/pdf";
//        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
//        if (fileName.endsWith(".png")) return "image/png";
//        if (fileName.endsWith(".doc") || fileName.endsWith(".docx"))
//            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
//        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))
//            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//        return "application/octet-stream"; // default
//    }
		
	public void generateAndSendInvoice(Payment_Order_Info payment, SubscriptionPlans subscriptionPlans,
			CompanyInfo companyInfo, User user, String formattedLicenseNumber)
			throws Exception {
		String invoicePath = subscriptionPlans.getInvoicePath() + payment.getPaymentId() + ".pdf";
//		String invoicePath = "C:\\Users\\ayush.gupta\\Documents\\Invoice Records\\invoice_" + payment.getPaymentId()+ ".pdf";
		generatePdfInvoice(invoicePath, payment, subscriptionPlans, companyInfo, user);
//	        sendInvoiceEmail("customer@example.com", "Your Invoice", "Please find attached your invoice.", invoicePath);
		String subject = "Subscription Confirmation: Welcome to [Pro Plus]!";
		String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>" + "    <meta charset='UTF-8'>"
				+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
				+ "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + "    <style>"
				+ "        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }"
				+ "        .email-wrapper { width: 100%; padding: 40px 0; display: flex; justify-content: center; align-items: center; background-color: #f4f6f9; }"
				+ "        .email-container { max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1); text-align: center; margin: 0 auto; }"
				+ "        .email-header { background-color: #007bff; color: white; text-align: center; padding: 20px; font-size: 24px; font-weight: bold; }"
				+ "        .email-body { padding: 30px; text-align: left; }"
				+ "        .email-body p { margin: 0 0 20px 0; line-height: 1.6; font-size: 16px; }"
				+ "        .email-body .success-box { background-color: #e8f4f8; border-left: 4px solid #28a745; padding: 20px; margin-bottom: 30px; border-radius: 5px; }"
				+ "        .email-body .success-box h2 { margin: 0; color: #28a745; font-size: 22px; }"
				+ "        .email-body .details-box { padding: 20px; border-radius: 5px; border: 1px solid #ddd; background-color: #fafafa; margin-bottom: 30px; }"
				+ "        .email-body .details-box p { font-size: 15px; margin: 8px 0; }"
				+ "        .email-footer { background-color: #f7f9fc; padding: 20px; text-align: center; font-size: 14px; color: #555555; }"
				+ "        .email-footer a { color: #007bff; text-decoration: none; font-weight: bold; }"
				+ "        .email-footer img { max-width: 100px; margin-top: 10px; }" + "    </style>" + "</head>"
				+ "<body>" + "    <div class='email-wrapper'>" + "        <div class='email-container'>"
				+ "            <div class='email-header'>Subscription Confirmation</div>"
				+ "            <div class='email-body'>" + "                <div class='success-box'>"
				+ "                    <h2>Payment Successful!</h2>" + "                </div>"
				+ "                <p>Dear " + user.getUsername() + ",</p>"
				+ "                <p>Thank you for subscribing to <strong>[Pro Plus]</strong>! We're thrilled to have you on board.</p>"
				+ "                <div class='details-box'>" + "                    <p><strong>Username:</strong> "
				+ user.getUsername() + "</p>" + "                    <p><strong>Email:</strong> " + payment.getEmail()
				+ "</p>" + "                    <p><strong>Payment Time:</strong> " + payment.getSystem_date_and_time()
				+ "</p>" + "                    <p><strong>License Number:</strong> " + payment.getLicense_number()
				+ "</p>" + "                    <p><strong>License Status:</strong> <span style='color: green;'>"
				+ payment.getLicense_status() + "</span></p>"
				+ "                    <p><strong>Payment Status:</strong> <span style='color: green; text-transform: uppercase;'>"
				+ payment.getStatus() + "</span></p>" + "                </div>"
				+ "                <p>We have attached the invoice for your subscription. If you have any questions, feel free to contact our support team at [Support Contact Info].</p>"
				+ "                <p>Thank you for choosing <strong>[WWW EMS COM]</strong>. We look forward to serving you!</p>"
				+ "                <p>Best regards,<br>Payment Team</p>" + "            </div>"
				+ "            <div class='email-footer'>"
				+ "                <p>Need help? <a href='#'>Contact Support</a> or visit our <a href='#'>Help Center</a>.</p>"
				+ "                <div style='font-size: 24px;'>" // Add this line for logo text
				+ "                    <span class='colored-char' style='color: rgb(66, 133, 244);'>w</span><span class='colored-char' style='color: rgb(255, 0, 0);'>w</span><span class='colored-char' style='color: rgb(255, 165, 0);'>w</span><span class='colored-char' style='color: rgb(0, 0, 255);'>.</span><span class='colored-char' style='color: rgb(60, 179, 113);'>e</span><span class='colored-char' style='color: rgb(255, 0, 0);'>m</span><span class='colored-char' style='color: rgb(0, 0, 255);'>s</span><span class='colored-char' style='color: rgb(255, 0, 0);'>.</span><span class='colored-char' style='color: rgb(255, 165, 0);'>c</span><span class='colored-char' style='color: rgb(0, 0, 255);'>o</span><span class='colored-char' style='color: rgb(255, 0, 0);'>m</span>"
				+ "                </div>" // Close the div for logo text
				+ "            </div>" + "        </div>" + "    </div>" + "</body>" + "</html>";

		List<String> Emails = userDetailDao.findEmailsByBaseLocationAndRolesPayment(user.getBase_location(),
				user.getCompany_id(), user.getId());
		CompletableFuture<Boolean> flagFuture = paymentSucessEmailService.sendEmail(invoicePath, message, subject,
				Emails);
		String fileName = payment.getPaymentId();
		String folderId = subscriptionPlans.getGoogleFolderId();
		String fileField = googleDriveService.uploadFile(invoicePath, fileName, folderId);
		System.out.println("GOOGLE DRIVE FILE PATH " + fileField);
		try {
			Boolean flag = flagFuture.get(); // Blocking call to get the result
			if (flag) {
				payment.setInvoice_sent_or_not(true);
				System.out.println(true);
				List<UserDetail> getInfo = userDetailDao.findIdsByBaseLocationAndRolesPayment(user.getBase_location(),
						user.getCompany_id(), user.getId());
				for (UserDetail detail : getInfo) {
					System.out.println(
							"NOTIFICATION SENDING TO EMP ID: " + detail.getId() + " ,EMAIL: " + detail.getEmail());
					Notification notification = new Notification();
					notification.setMessage_id(generateMessageId());
					notification.setMessage("Subscription Payment Successfull By EMP ID: " + user.getId());
					notification.setUserId(detail.getId());
					notification.setTimestamp(new Date());
					notification.setAdddate(new Date());
					notification.setAddwho("SYSTEM");
					notification.setEditdate(new Date());
					notification.setEditwho(detail.getEditwho());
					notification.setSenderEmail(user.getEmail());
					notification.setReceiverEmail(detail.getEmail());
					notificationDao.save(notification);
					NotificationListener.send(detail.getEmail(), notification);
					System.out.println("Sending notification to: " + detail.getEmail());

				}
			} else {
				payment.setInvoice_sent_or_not(false);
				System.out.println(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			user.setDefaultPasswordSent(false);
		}
	}

	private void generatePdfInvoice(String filePath, Payment_Order_Info payment, SubscriptionPlans subscriptionPlans,
			CompanyInfo company_Info, User user) throws IOException {
		try {
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
			Color[] colors = { new DeviceRgb(66, 133, 244), // Blue
					new DeviceRgb(219, 68, 55), // Red
					new DeviceRgb(244, 180, 0), // Yellow
					new DeviceRgb(66, 133, 244), // Blue
					new DeviceRgb(15, 157, 88), // Green
					new DeviceRgb(219, 68, 55), // Red
					new DeviceRgb(66, 133, 244), // Blue
					new DeviceRgb(219, 68, 55), // Red
					new DeviceRgb(244, 180, 0) // Yellow
			};
			Paragraph title4 = new Paragraph();
			// Add each character with its corresponding color
			for (int i = 0; i < textChars.length; i++) {
				// Create a Text object for each character
				Text coloredText = new Text(String.valueOf(textChars[i])).setFontColor(colors[i % colors.length]); // Rotate
																													// through
																													// the
																													// colors

				// Add the Text object to the Paragraph
				title4.add(coloredText);
			}
			title4.setFont(boldFont).setFontSize(26).setTextAlignment(TextAlignment.LEFT).setMarginBottom(10);
			document.add(title4);

			// Company Info
			Paragraph title1 = new Paragraph("Billed From").setFont(boldFont).setFontSize(12)
					.setTextAlignment(TextAlignment.LEFT).setMarginBottom(20);
			document.add(title1);
			Paragraph BilledFrom = new Paragraph(user.getId() + "\n" + payment.getCompany() + "\n"
					+ user.getBase_location() + "\n" + payment.getEmail() + "\n" + payment.getPhone()).setFont(font)
					.setFontSize(12).setTextAlignment(TextAlignment.LEFT).setMarginBottom(12);
			document.add(BilledFrom);

			Paragraph title2 = new Paragraph("Billed To").setFont(boldFont).setFontSize(12)
					.setTextAlignment(TextAlignment.LEFT).setMarginBottom(9);
			document.add(title2);
			Paragraph BilledTo = new Paragraph(company_Info.getCompany_name() + "\n" + company_Info.getCompany_address()
					+ "\n" + company_Info.getCompany_phone() + "\n" + company_Info.getCompany_email() + "\n"
					+ payment.getGst_no()).setFont(font).setFontSize(12).setTextAlignment(TextAlignment.LEFT)
					.setMarginBottom(12);
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
			invoiceTable
					.addCell(new Cell().add(new Paragraph(payment.getLicense_number()).setFont(font).setFontSize(12))
							.setBorder(Border.NO_BORDER));

			invoiceTable.addCell(new Cell().add(new Paragraph("License Status:").setFont(boldFont).setFontSize(12))
					.setBorder(Border.NO_BORDER));
			invoiceTable
					.addCell(new Cell().add(new Paragraph(payment.getLicense_status()).setFont(font).setFontSize(12))
							.setBorder(Border.NO_BORDER));

			invoiceTable.addCell(new Cell().add(new Paragraph("Receipt:").setFont(boldFont).setFontSize(12))
					.setBorder(Border.NO_BORDER));
			invoiceTable.addCell(new Cell().add(new Paragraph(payment.getReceipt()).setFont(font).setFontSize(12))
					.setBorder(Border.NO_BORDER));

			// Status with uppercase and green text
			invoiceTable.addCell(new Cell().add(new Paragraph("Status:").setFont(boldFont).setFontSize(12))
					.setBorder(Border.NO_BORDER));
			invoiceTable.addCell(new Cell().add(new Paragraph(payment.getStatus().toUpperCase()).setFont(font)
					.setFontSize(12).setFontColor(ColorConstants.GREEN)).setBorder(Border.NO_BORDER));

			invoiceTable.addCell(new Cell().add(new Paragraph("Payment Date/Time:").setFont(boldFont).setFontSize(12))
					.setBorder(Border.NO_BORDER));
			invoiceTable.addCell(new Cell().add(new Paragraph(formattedDate).setFont(font).setFontSize(12))
					.setBorder(Border.NO_BORDER));

			// Generate QR Code containing payment details
			String qrData = "Order ID: " + payment.getId() + "\n" + "Payment ID: " + payment.getPaymentId() + "\n"
					+ "Order ID: " + payment.getOrderId() + "\n" + "Receipt ID: " + payment.getReceipt() + "\n"
					+ "Amount: ₹" + payment.getAmount() + "\n" + "Billed From Phone Number: " + payment.getPhone()
					+ "\n" + "Billed License Number: " + payment.getLicense_number() + "\n" + "Billed From Company Id: "
					+ payment.getCompany_id() + "\n" + "Billed From Company Name: " + payment.getCompany() + "\n"
					+ "Billed From Phone Number: " + payment.getPhone() + "\n" + "Billed Subscription Start Date: "
					+ payment.getSubscription_start_date() + "\n" + "Billed Subscription End Date: "
					+ payment.getSubscription_expiry_date() + "\n" + "Billed Transaction Date: "
					+ payment.getSystem_date_and_time() + "\n" + "Billed  Validity: " + payment.getValidity() + "\n"
					+ "Billed Qty: " + payment.getQty() + "\n" + "GST Number: " + payment.getGst_no() + "\n"
					+ "Discount: " + payment.getDiscount() + "\n" + "Amount Without GST: "
					+ payment.getAmount_without_gst() + "\n" + "Tax: " + payment.getTax() + "\n" + "Billed To: "
					+ company_Info.getCompany_id() + "\n" + "Billed Email To: " + company_Info.getCompany_email() + "\n"
					+ "Billed To Phone Number: " + company_Info.getCompany_phone() + "\n" + "Status: "
					+ payment.getStatus();
			ImageData qrCodeData = generateQRCodeImage(qrData);
			Image qrCode = new Image(qrCodeData).setWidth(180).setHeight(180).setMarginBottom(2);
			// Add QR Code to PDF
			document.add(new Paragraph("Scan QR Code for Payment Details:").setFont(boldFont).setFontSize(12));
			document.add(qrCode);
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
			orderSummaryTable
					.addHeaderCell(new Cell().add(new Paragraph("Price (INR)").setFont(boldFont).setFontSize(12))
							.setBackgroundColor(ColorConstants.LIGHT_GRAY));
			orderSummaryTable
					.addHeaderCell(new Cell().add(new Paragraph("Discount (INR)").setFont(boldFont).setFontSize(12))
							.setBackgroundColor(ColorConstants.LIGHT_GRAY));
			orderSummaryTable.addHeaderCell(new Cell().add(new Paragraph("Tax (GST)").setFont(boldFont).setFontSize(12))
					.setBackgroundColor(ColorConstants.LIGHT_GRAY));
//		orderSummaryTable.addHeaderCell(new Cell().add(new Paragraph("Quantity").setFont(boldFont).setFontSize(12))
//				.setBackgroundColor(ColorConstants.LIGHT_GRAY));
			orderSummaryTable.addHeaderCell(new Cell().add(new Paragraph("Total").setFont(boldFont).setFontSize(12))
					.setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.RIGHT));

			orderSummaryTable.addCell(new Cell().add(new Paragraph("01").setFont(font).setFontSize(12)));
			orderSummaryTable.addCell(new Cell().add(new Paragraph("EMS SUBSCRIPTION\nValidity: "
					+ subscriptionPlans.getPlan_description() + "\nLicense Number: " + payment.getLicense_number()
					+ "\nLic. Issue Date: " + payment.getSubscription_start_date() + "\nLic. End Date: "
					+ payment.getSubscription_expiry_date() + "" + "\nQuantity: " + subscriptionPlans.getQty())
					.setFont(font).setFontSize(12)));
			float amt_without_gst = payment.getAmount() - payment.getGst_amount();
			float discount = payment.getDiscount();
			float gst_amount = payment.getGst_amount();
			// Formatted values
			String formattedAmtWithoutDiscount = String.format("%.2f", amt_without_gst);
			String formattedDiscount = String.format("%.2f", discount);
			String formattedGstAmount = String.format("%.2f", gst_amount);
			orderSummaryTable.addCell(
					new Cell().add(new Paragraph("₹" + formattedAmtWithoutDiscount).setFont(font).setFontSize(12)));
			orderSummaryTable
					.addCell(new Cell().add(new Paragraph("₹ -" + formattedDiscount)).setFont(font).setFontSize(12));
			orderSummaryTable.addCell(new Cell().add(new Paragraph("₹" + payment.getTax() + "   " + formattedGstAmount))
					.setFont(font).setFontSize(12));
//		orderSummaryTable.addCell(new Cell().add(new Paragraph("1").setFont(font).setFontSize(12)));
			orderSummaryTable
					.addCell(new Cell().add(new Paragraph("₹" + payment.getAmount()).setFont(font).setFontSize(12))
							.setTextAlignment(TextAlignment.RIGHT));

			orderSummaryTable.addCell(new Cell(1, 5).add(new Paragraph("Sub Total").setFont(boldFont).setFontSize(12)
					.setTextAlignment(TextAlignment.RIGHT)));
			orderSummaryTable
					.addCell(new Cell().add(new Paragraph("₹" + payment.getAmount()).setFont(font).setFontSize(12))
							.setTextAlignment(TextAlignment.RIGHT));

			orderSummaryTable.addCell(new Cell(1, 5).add(
					new Paragraph("Discount").setFont(boldFont).setFontSize(12).setTextAlignment(TextAlignment.RIGHT)));
			if (subscriptionPlans.getDiscount() == 0) {
				orderSummaryTable.addCell(new Cell().add(new Paragraph("₹ NA ").setFont(font).setFontSize(12))
						.setTextAlignment(TextAlignment.RIGHT));
			} else {
				orderSummaryTable.addCell(new Cell()
						.add(new Paragraph("₹" + subscriptionPlans.getDiscount()).setFont(font).setFontSize(12))
						.setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
			}
			orderSummaryTable.addCell(new Cell(1, 5).add(
					new Paragraph("Total").setFont(boldFont).setFontSize(12).setTextAlignment(TextAlignment.RIGHT)));
			orderSummaryTable
					.addCell(new Cell().add(new Paragraph("₹" + payment.getAmount()).setFont(font).setFontSize(12))
							.setTextAlignment(TextAlignment.RIGHT));

			document.add(orderSummaryTable);

			// Divider
			document.add(new Paragraph("\n"));

			// Terms and Conditions
			Paragraph termsTitle = new Paragraph("Terms and Conditions").setFont(boldFont).setFontSize(14)
					.setTextAlignment(TextAlignment.LEFT).setMarginBottom(10);
			document.add(termsTitle);

			Div termsDiv = new Div();
			termsDiv.add(new Paragraph("1. Payment Invoice valid for " + subscriptionPlans.getPlan_description())
					.setFont(font).setFontSize(12));
			termsDiv.add(new Paragraph("2. Late payments may be subject to additional charges.").setFont(font)
					.setFontSize(12));
			termsDiv.add(new Paragraph("3. Please contact support for any discrepancies in your invoice.").setFont(font)
					.setFontSize(12));
			termsDiv.add(new Paragraph("4. All sales are final. No refunds.").setFont(font).setFontSize(12));
			termsDiv.add(
					new Paragraph("5. This invoice is subject to the terms and conditions of our service agreement.")
							.setFont(font).setFontSize(12));
			document.add(termsDiv);

			document.close();
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	private static ImageData generateQRCodeImage(String data) throws Exception {
		int width = 150; // QR code width
		int height = 150; // QR code height
		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

		BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints);
		BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "PNG", baos);

		return ImageDataFactory.create(baos.toByteArray());
	}

	public void save_null_value_laptop_assign_by_IT(UserDetail detail) {
		userDetailDao.save(detail);
	}

	public void insert_Job_First_Time() {
		List<Job> job = jobDao.findAll();
		List<String> job_list = new ArrayList<>();
		if (job == null) {
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
			for (String c : job_list) {
				Job job1 = new Job();
				int count = jobDao.getJobCount();
				if (count == 0) {
					job1.setId(1);
					job1.setJob_description(c);
					job1.setJob_active_or_not("Y");
					jobDao.save(job1);
				} else {
					int lastid = jobDao.getJobLastId();
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

	public List<UserDetail> searchEmployees(String term) {
		return userDetailDao.findByNameContainingOrEmailContainingOrIdContaining(term);
	}

	@Transactional
	public void Archive_Disabled_Old_User_Job() {
		try {
			List<User> get_user = userdao.Get_ALL_Disabled_Old_User_Job();
			ListIterator<User> Get_ALL_Disabled_Old_User_Iterate = get_user.listIterator();
			while (Get_ALL_Disabled_Old_User_Iterate.hasNext()) {
				ArchiveDisabledUser archiveDisabledUser = new ArchiveDisabledUser();
				int count = archiveDisabledUserDao.getArchiveUserCount();
				if (count > 0) {
					User user_info_get = Get_ALL_Disabled_Old_User_Iterate.next();
					int getLastSno = archiveDisabledUserDao.getLastSno();
					archiveDisabledUser.setSno(++getLastSno);
					archiveDisabledUser.setId(user_info_get.getId());
					archiveDisabledUser.setUsername(user_info_get.getUsername());
					archiveDisabledUser.setState(user_info_get.getState());
					archiveDisabledUser.setEmail(user_info_get.getEmail());
					archiveDisabledUser.setPassword(user_info_get.getPassword());
					archiveDisabledUser.setRepassword(user_info_get.getRepassword());
					archiveDisabledUser.setPhone(user_info_get.getPhone());
					archiveDisabledUser.setUser_status(user_info_get.isUser_status());
					archiveDisabledUser.setGender(user_info_get.getGender());
					archiveDisabledUser.setDob(user_info_get.getDob());
					archiveDisabledUser.setEnabled(user_info_get.isEnabled());
					archiveDisabledUser.setAddress(user_info_get.getAddress());
					archiveDisabledUser.setCountry(user_info_get.getCountry());
					archiveDisabledUser.setImage_Url(user_info_get.getImage_Name());
					archiveDisabledUser.setExperience(user_info_get.getExperience());
					archiveDisabledUser.setSkills(user_info_get.getSkills());
					archiveDisabledUser.setSperationDate(user_info_get.getSeperationDate());
					archiveDisabledUser.setLastWorkingDay(user_info_get.getLastWorkingDay());
					archiveDisabledUser.setEditdate(user_info_get.getEditdate());
					archiveDisabledUser.setEditwho(user_info_get.getEditwho());
					archiveDisabledUser.setNewUserActiveOrInactive(user_info_get.isNewUserActiveOrInactive());
					archiveDisabledUser.setStatus(user_info_get.getStatus());
					archiveDisabledUser.setLast_failed_attempt(user_info_get.getLast_failed_attempt());
					archiveDisabledUser.setAlert_message_sent(user_info_get.getAlert_message_sent());
					archiveDisabledUser.setSystemDateAndTime(user_info_get.getSystemDateAndTime());
					archiveDisabledUser.setAdmin_id(user_info_get.getAdmin_id());
					archiveDisabledUser.setRole(user_info_get.getRole());
					archiveDisabledUser.setIpAddress(user_info_get.getIpAddress());
					archiveDisabledUser.setAccountNonLocked(user_info_get.isAccountNonLocked());
					archiveDisabledUser.setFailedAttempt(user_info_get.getFailedAttempt());
					archiveDisabledUser.setLockDateAndTime(user_info_get.getLockDateAndTime());
					archiveDisabledUser.setExpirelockDateAndTime(user_info_get.getExpirelockDateAndTime());
					archiveDisabledUser.setDefaultPasswordSent(user_info_get.isDefaultPasswordSent());
					archiveDisabledUser.setSession_Id(user_info_get.getSession_Id());
					archiveDisabledUser.setExcel_Download(user_info_get.isExcel_Download());
					archiveDisabledUser.setDownload_count(user_info_get.getDownload_count());
					archiveDisabledUser.setLaptop_id(user_info_get.getLaptop_id());
					archiveDisabledUser.setLaptop_brand(user_info_get.getLaptop_brand());
					archiveDisabledUser.setLaptop_assign_date(user_info_get.getLaptop_assign_date());
					archiveDisabledUser.setLaptop_serial_number(user_info_get.getLaptop_serial_number());
					archiveDisabledUser.setBank_account_holder_name(user_info_get.getBank_account_holder_name());
					archiveDisabledUser.setBank_account_number(user_info_get.getBank_account_number());
					archiveDisabledUser.setIfsc_code(user_info_get.getIfsc_code());
					archiveDisabledUser.setBank_name(user_info_get.getBank_name());
					archiveDisabledUser.setResume_file_url(user_info_get.getResume_file_name());
					archiveDisabledUser.setDesignation(user_info_get.getDesignation());
					archiveDisabledUser.setBase_location(user_info_get.getBase_location());
					archiveDisabledUser.setManager_or_not(user_info_get.isManager_or_not());
					archiveDisabledUser.setTeam(user_info_get.getTeam());
					archiveDisabledUser.setCompany(user_info_get.getCompany());
					archiveDisabledUser.setCompany_id(user_info_get.getCompany_id());
					archiveDisabledUserDao.save(archiveDisabledUser);
					userdao.deleteByEmpId(user_info_get.getId());
				} else {
					User user_info_get = Get_ALL_Disabled_Old_User_Iterate.next();
					archiveDisabledUser.setSno(1);
					archiveDisabledUser.setId(user_info_get.getId());
					archiveDisabledUser.setUsername(user_info_get.getUsername());
					archiveDisabledUser.setState(user_info_get.getState());
					archiveDisabledUser.setEmail(user_info_get.getEmail());
					archiveDisabledUser.setPassword(user_info_get.getPassword());
					archiveDisabledUser.setRepassword(user_info_get.getRepassword());
					archiveDisabledUser.setPhone(user_info_get.getPhone());
					archiveDisabledUser.setUser_status(user_info_get.isUser_status());
					archiveDisabledUser.setGender(user_info_get.getGender());
					archiveDisabledUser.setDob(user_info_get.getDob());
					archiveDisabledUser.setEnabled(user_info_get.isEnabled());
					archiveDisabledUser.setAddress(user_info_get.getAddress());
					archiveDisabledUser.setCountry(user_info_get.getCountry());
					archiveDisabledUser.setImage_Url(user_info_get.getImage_Name());
					archiveDisabledUser.setExperience(user_info_get.getExperience());
					archiveDisabledUser.setSkills(user_info_get.getSkills());
					archiveDisabledUser.setSperationDate(user_info_get.getSeperationDate());
					archiveDisabledUser.setLastWorkingDay(user_info_get.getLastWorkingDay());
					archiveDisabledUser.setEditdate(user_info_get.getEditdate());
					archiveDisabledUser.setEditwho(user_info_get.getEditwho());
					archiveDisabledUser.setNewUserActiveOrInactive(user_info_get.isNewUserActiveOrInactive());
					archiveDisabledUser.setStatus(user_info_get.getStatus());
					archiveDisabledUser.setLast_failed_attempt(user_info_get.getLast_failed_attempt());
					archiveDisabledUser.setAlert_message_sent(user_info_get.getAlert_message_sent());
					archiveDisabledUser.setSystemDateAndTime(user_info_get.getSystemDateAndTime());
					archiveDisabledUser.setAdmin_id(user_info_get.getAdmin_id());
					archiveDisabledUser.setRole(user_info_get.getRole());
					archiveDisabledUser.setIpAddress(user_info_get.getIpAddress());
					archiveDisabledUser.setAccountNonLocked(user_info_get.isAccountNonLocked());
					archiveDisabledUser.setFailedAttempt(user_info_get.getFailedAttempt());
					archiveDisabledUser.setLockDateAndTime(user_info_get.getLockDateAndTime());
					archiveDisabledUser.setExpirelockDateAndTime(user_info_get.getExpirelockDateAndTime());
					archiveDisabledUser.setDefaultPasswordSent(user_info_get.isDefaultPasswordSent());
					archiveDisabledUser.setSession_Id(user_info_get.getSession_Id());
					archiveDisabledUser.setExcel_Download(user_info_get.isExcel_Download());
					archiveDisabledUser.setDownload_count(user_info_get.getDownload_count());
					archiveDisabledUser.setLaptop_id(user_info_get.getLaptop_id());
					archiveDisabledUser.setLaptop_brand(user_info_get.getLaptop_brand());
					archiveDisabledUser.setLaptop_assign_date(user_info_get.getLaptop_assign_date());
					archiveDisabledUser.setLaptop_serial_number(user_info_get.getLaptop_serial_number());
					archiveDisabledUser.setBank_account_holder_name(user_info_get.getBank_account_holder_name());
					archiveDisabledUser.setBank_account_number(user_info_get.getBank_account_number());
					archiveDisabledUser.setIfsc_code(user_info_get.getIfsc_code());
					archiveDisabledUser.setBank_name(user_info_get.getBank_name());
					archiveDisabledUser.setResume_file_url(user_info_get.getResume_file_name());
					archiveDisabledUser.setDesignation(user_info_get.getDesignation());
					archiveDisabledUser.setBase_location(user_info_get.getBase_location());
					archiveDisabledUser.setManager_or_not(user_info_get.isManager_or_not());
					archiveDisabledUser.setTeam(user_info_get.getTeam());
					archiveDisabledUser.setCompany(user_info_get.getCompany());
					archiveDisabledUser.setCompany_id(user_info_get.getCompany_id());
					archiveDisabledUserDao.save(archiveDisabledUser);
					userdao.deleteByEmpId(user_info_get.getId());
				}
			}
			jobrunning("Archive_Disabled_Old_User_Job");
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	@Transactional
	public void Archive_Disabled_Old_UserDetail_Job() {
		try {
			List<UserDetail> get_user = userDetailDao.Get_ALL_Disabled_Old_UserDetail_Job();
			ListIterator<UserDetail> Get_ALL_Disabled_Old_User_Iterate = get_user.listIterator();
			while (Get_ALL_Disabled_Old_User_Iterate.hasNext()) {
				int count = archiveDisabledUserDetailDao.getArchiveUserCount();
				ArchiveDisabledUserDetail archiveDisabledUserDetail = new ArchiveDisabledUserDetail();
				if (count > 0) {
					UserDetail user_info_get = Get_ALL_Disabled_Old_User_Iterate.next();
					int getLastSno = archiveDisabledUserDetailDao.getLastSno();
					archiveDisabledUserDetail.setSno(++getLastSno);
					archiveDisabledUserDetail.setId(user_info_get.getId());
					archiveDisabledUserDetail.setUsername(user_info_get.getUsername());
//			archiveDisabledUserDetail.setState(user_info_get.getState());
					archiveDisabledUserDetail.setEmail(user_info_get.getEmail());
					archiveDisabledUserDetail.setPassword(user_info_get.getPassword());
					archiveDisabledUserDetail.setRepassword(user_info_get.getRepassword());
					archiveDisabledUserDetail.setPhone(user_info_get.getPhone());
					archiveDisabledUserDetail.setUser_status(user_info_get.isUser_status());
					archiveDisabledUserDetail.setGender(user_info_get.getGender());
					archiveDisabledUserDetail.setDob(user_info_get.getDob());
					archiveDisabledUserDetail.setEnabled(user_info_get.isEnabled());
					archiveDisabledUserDetail.setAddress(user_info_get.getAddress());
					archiveDisabledUserDetail.setCountry(user_info_get.getCountry());
					archiveDisabledUserDetail.setImage_Url(user_info_get.getImage_Name());
					archiveDisabledUserDetail.setExperience(user_info_get.getExperience());
					archiveDisabledUserDetail.setSkills(user_info_get.getSkills());
//		    archiveDisabledUserDetail.setSperationDate(user_info_get.getSperationDate());
					archiveDisabledUserDetail.setLastWorkingDay(user_info_get.getLastWorkingDay());
					archiveDisabledUserDetail.setEditdate(user_info_get.getEditdate());
					archiveDisabledUserDetail.setEditwho(user_info_get.getEditwho());
//			archiveDisabledUserDetail.setNewUserActiveOrInactive(user_info_get.isNewUserActiveOrInactive());
					archiveDisabledUserDetail.setStatus(user_info_get.getStatus());
//			archiveDisabledUserDetail.setLast_failed_attempt(user_info_get.getLast_failed_attempt());
					archiveDisabledUserDetail.setAlert_message_sent(user_info_get.getAlert_message_sent());
					archiveDisabledUserDetail.setSystemDateAndTime(user_info_get.getAdddate());
					archiveDisabledUserDetail.setAddwho(user_info_get.getAddwho());
					archiveDisabledUserDetail.setRole(user_info_get.getRole());
					archiveDisabledUserDetail.setIpAddress(user_info_get.getIpAddress());
					archiveDisabledUserDetail.setAccountNonLocked(user_info_get.isAccountNonLocked());
					archiveDisabledUserDetail.setFailedAttempt(user_info_get.getFailedAttempt());
					archiveDisabledUserDetail.setLockDateAndTime(user_info_get.getLockDateAndTime());
					archiveDisabledUserDetail.setExpirelockDateAndTime(user_info_get.getExpirelockDateAndTime());
//			archiveDisabledUserDetail.setDefaultPasswordSent(user_info_get.isDefaultPasswordSent());
//			archiveDisabledUserDetail.setSession_Id(user_info_get.getSession_Id());
//			archiveDisabledUserDetail.setExcel_Download(user_info_get.isExcel_Download());
//			archiveDisabledUserDetail.setDownload_count(user_info_get.getDownload_count());
					archiveDisabledUserDetail.setLaptop_id(user_info_get.getLaptop_id());
					archiveDisabledUserDetail.setLaptop_brand(user_info_get.getLaptop_brand());
					archiveDisabledUserDetail.setLaptop_assign_date(user_info_get.getLaptop_assign_date());
					archiveDisabledUserDetail.setLaptop_serial_number(user_info_get.getLaptop_serial_number());
					archiveDisabledUserDetail.setBank_account_holder_name(user_info_get.getBank_account_holder_name());
					archiveDisabledUserDetail.setBank_account_number(user_info_get.getBank_account_number());
					archiveDisabledUserDetail.setIfsc_code(user_info_get.getIfsc_code());
					archiveDisabledUserDetail.setBank_name(user_info_get.getBank_name());
					archiveDisabledUserDetail.setResume_file_url(user_info_get.getResume_file_name());
					archiveDisabledUserDetail.setDesignation(user_info_get.getDesignation());
					archiveDisabledUserDetail.setBase_location(user_info_get.getBase_location());
					archiveDisabledUserDetail.setManager_or_not(user_info_get.isManager_or_not());
					archiveDisabledUserDetail.setTeam(user_info_get.getTeam());
//			archiveDisabledUserDetail.setCompany(user_info_get.getCompany());
//			archiveDisabledUserDetail.setCompany_id(user_info_get.getCompany_id());
					archiveDisabledUserDetailDao.save(archiveDisabledUserDetail);
					userDetailDao.deleteByEmpId(user_info_get.getId());
				} else {
					UserDetail user_info_get = Get_ALL_Disabled_Old_User_Iterate.next();
					archiveDisabledUserDetail.setSno(1);
					archiveDisabledUserDetail.setId(user_info_get.getId());
					archiveDisabledUserDetail.setUsername(user_info_get.getUsername());
//			archiveDisabledUserDetail.setState(user_info_get.getState());
					archiveDisabledUserDetail.setEmail(user_info_get.getEmail());
					archiveDisabledUserDetail.setPassword(user_info_get.getPassword());
					archiveDisabledUserDetail.setRepassword(user_info_get.getRepassword());
					archiveDisabledUserDetail.setPhone(user_info_get.getPhone());
					archiveDisabledUserDetail.setUser_status(user_info_get.isUser_status());
					archiveDisabledUserDetail.setGender(user_info_get.getGender());
					archiveDisabledUserDetail.setDob(user_info_get.getDob());
					archiveDisabledUserDetail.setEnabled(user_info_get.isEnabled());
					archiveDisabledUserDetail.setAddress(user_info_get.getAddress());
					archiveDisabledUserDetail.setCountry(user_info_get.getCountry());
					archiveDisabledUserDetail.setImage_Url(user_info_get.getImage_Name());
					archiveDisabledUserDetail.setExperience(user_info_get.getExperience());
					archiveDisabledUserDetail.setSkills(user_info_get.getSkills());
//		    archiveDisabledUserDetail.setSperationDate(user_info_get.getSperationDate());
					archiveDisabledUserDetail.setLastWorkingDay(user_info_get.getLastWorkingDay());
					archiveDisabledUserDetail.setEditdate(user_info_get.getEditdate());
					archiveDisabledUserDetail.setEditwho(user_info_get.getEditwho());
//			archiveDisabledUserDetail.setNewUserActiveOrInactive(user_info_get.isNewUserActiveOrInactive());
					archiveDisabledUserDetail.setStatus(user_info_get.getStatus());
//			archiveDisabledUserDetail.setLast_failed_attempt(user_info_get.getLast_failed_attempt());
					archiveDisabledUserDetail.setAlert_message_sent(user_info_get.getAlert_message_sent());
					archiveDisabledUserDetail.setSystemDateAndTime(user_info_get.getAdddate());
					archiveDisabledUserDetail.setAddwho(user_info_get.getAddwho());
					archiveDisabledUserDetail.setRole(user_info_get.getRole());
					archiveDisabledUserDetail.setIpAddress(user_info_get.getIpAddress());
					archiveDisabledUserDetail.setAccountNonLocked(user_info_get.isAccountNonLocked());
					archiveDisabledUserDetail.setFailedAttempt(user_info_get.getFailedAttempt());
					archiveDisabledUserDetail.setLockDateAndTime(user_info_get.getLockDateAndTime());
					archiveDisabledUserDetail.setExpirelockDateAndTime(user_info_get.getExpirelockDateAndTime());
//			archiveDisabledUserDetail.setDefaultPasswordSent(user_info_get.isDefaultPasswordSent());
//			archiveDisabledUserDetail.setSession_Id(user_info_get.getSession_Id());
//			archiveDisabledUserDetail.setExcel_Download(user_info_get.isExcel_Download());
//			archiveDisabledUserDetail.setDownload_count(user_info_get.getDownload_count());
					archiveDisabledUserDetail.setLaptop_id(user_info_get.getLaptop_id());
					archiveDisabledUserDetail.setLaptop_brand(user_info_get.getLaptop_brand());
					archiveDisabledUserDetail.setLaptop_assign_date(user_info_get.getLaptop_assign_date());
					archiveDisabledUserDetail.setLaptop_serial_number(user_info_get.getLaptop_serial_number());
					archiveDisabledUserDetail.setBank_account_holder_name(user_info_get.getBank_account_holder_name());
					archiveDisabledUserDetail.setBank_account_number(user_info_get.getBank_account_number());
					archiveDisabledUserDetail.setIfsc_code(user_info_get.getIfsc_code());
					archiveDisabledUserDetail.setBank_name(user_info_get.getBank_name());
					archiveDisabledUserDetail.setResume_file_url(user_info_get.getResume_file_name());
					archiveDisabledUserDetail.setDesignation(user_info_get.getDesignation());
					archiveDisabledUserDetail.setBase_location(user_info_get.getBase_location());
					archiveDisabledUserDetail.setManager_or_not(user_info_get.isManager_or_not());
					archiveDisabledUserDetail.setTeam(user_info_get.getTeam());
//			archiveDisabledUserDetail.setCompany(user_info_get.getCompany());
//			archiveDisabledUserDetail.setCompany_id(user_info_get.getCompany_id());
					archiveDisabledUserDetailDao.save(archiveDisabledUserDetail);
					userDetailDao.deleteByEmpId(user_info_get.getId());
				}
			}
			jobrunning("Archive_Disabled_Old_UserDetail_Job");
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

//	public Page<UserDetail> findPaginated(int page, int size) {
//	    Pageable pageable = PageRequest.of(page, size);
//	    Page<UserDetail> page2= userDetailDao.findAllEnabledUser(pageable);
//	    return page2;
//	}

	public Page<UserDetail> findPaginated(List<UserDetail> sortedList, int page, int pageSize) {
		try {
			int startItem = page * pageSize;
			List<UserDetail> paginatedList;

			if (sortedList.size() < startItem) {
				paginatedList = Collections.emptyList();
			} else {
				int toIndex = Math.min(startItem + pageSize, sortedList.size());
				paginatedList = sortedList.subList(startItem, toIndex);
			}

			return new PageImpl<>(paginatedList, PageRequest.of(page, pageSize), sortedList.size());
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return null;
	    }
	}

	@Transactional
	public void update_login_dao(User user) {
		try {
			userLoginDao.updateSessionInterruptedStatus(user.getId());
//		userLoginDao.setDefaultLogoutTime(user.getId());
			userDetailDao.update_user_status(user.getId());
			userdao.update_user_status(user.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// In your Servicelayer
	public Page<User> fetchUsersByEmail(String email, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return userdao.findByUsername(email, pageable); // Ensure 'findByEmail' method is working
	}

	public List<UserLoginDateTime> findAllByCompanyIdOrTeamId(User user) {
		System.out.println("COMPANY " + user.getCompany_id() + ", TEAM " + user.getTeam());
		return userLoginDao.findAllByCompanyIdOrTeamId(user.getCompany_id(), user.getTeam());
	}

	public void saveRecordActivity(String company_id, String email, int employeeId, String employeeName,
			String ipAddress, String functionality, String addWho) {
		// Create a new RecordActivity instance
		RecordActivity record = new RecordActivity();
		record.setEmail(email);
		record.setEmployee_id(employeeId);
		record.setEmployee_name(employeeName);
		record.setDate(new Date()); // Current date/time
		record.setIpAddress(ipAddress);
		record.setCompany_id(company_id);
		record.setFunctionality(functionality);

		// Save the record to the database
		record_activity_dao.save(record);
	}

	public void RechargeUpcomingAlert() {
		List<Payment_Order_Info> payment_Order_Info = orderDao.findExpiredPlansWithoutUpcomingRecharges();
		for (Payment_Order_Info user : payment_Order_Info) {
			String NSQLVALUE = NsqlConfig("RENEWPLANREMINDER"); // Get reminder days from NSqlConfig
			int reminderDays = Integer.parseInt(NSQLVALUE); // Convert reminder days to an integer

			// Get the current date
			LocalDate currentDate = LocalDate.now();

			// Convert expiry date to LocalDate
			LocalDate expiryDate = user.getSubscription_expiry_date().toInstant().atZone(ZoneId.systemDefault())
					.toLocalDate();

			// Calculate remaining days
			long remainingDays = Duration.between(currentDate.atStartOfDay(), expiryDate.atStartOfDay()).toDays();

			// Send email only if remaining days are less than or equal to the reminder days
			if (remainingDays <= reminderDays) {
				String to = user.getEmail();
				String subject = " Urgent: Your Subscription is Expiring Soon – Renew Now!";
				String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>" + "    <meta charset='UTF-8'>"
						+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
						+ "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + "    <style>"
						+ "        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }"
						+ "        .email-wrapper { width: 100%; padding: 40px 0; display: flex; justify-content: center; align-items: center; background-color: #f4f6f9; }"
						+ "        .email-container { max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1); text-align: center; margin: 0 auto; }"
						+ "        .email-header { background-color: #ff9800; color: white; text-align: center; padding: 20px; font-size: 24px; font-weight: bold; }"
						+ "        .email-body { padding: 30px; text-align: left; }"
						+ "        .email-body p { margin: 0 0 20px 0; line-height: 1.6; font-size: 16px; }"
						+ "        .email-body .alert-box { background-color: #fff3cd; border-left: 4px solid #ff9800; padding: 20px; margin-bottom: 30px; border-radius: 5px; }"
						+ "        .email-body .alert-box h2 { margin: 0; color: #ff9800; font-size: 22px; }"
						+ "        .email-footer { background-color: #f7f9fc; padding: 20px; text-align: center; font-size: 14px; color: #555555; }"
						+ "        .email-footer a { color: #007bff; text-decoration: none; font-weight: bold; }"
						+ "    </style>" + "</head>" + "<body>" + "    <div class='email-wrapper'>"
						+ "        <div class='email-container'>"
						+ "            <div class='email-header'>Recharge Alert</div>"
						+ "            <div class='email-body'>" + "                <div class='alert-box'>"
						+ "                    <h2>Action Required: Recharge Your Account</h2>"
						+ "                </div>" + "                <p>Dear " + user.getUsername() + ",</p>"
						+ "                <p>Your account balance is low, and your subscription will expire soon.</p>"
						+ "                <p>To avoid any service disruption, please recharge your account before <strong>"
						+ user.getSubscription_expiry_date() + "</strong>.</p>"
						+ "                <p>Please log in to your account to complete the renewal process.</p>"
						+ "                <p>If you have already recharged, kindly ignore this message.</p>"
						+ "                <p>Best regards,<br>Billing Team</p>" + "            </div>"
						+ "            <div class='email-footer'>"
						+ "                <p>Need help? <a href='#'>Contact Support</a></p>" + "            </div>"
						+ "        </div>" + "    </div>" + "</body>" + "</html>";

				emailService.sendEmail(message, subject, to);
			}
		}
	}

	public String NsqlConfig(String config) {
		String NSQLVALUE = "NOTFOUND";
		Optional<NSqlConfig> optional = nSqlConfigDao.findByConfigkey(config);
		if (optional.isPresent()) {
			NSqlConfig nSqlConfig = optional.get();
			NSQLVALUE = nSqlConfig.getNSqlValue();
		} else {
			NSQLVALUE = "NOTFOUND";
		}
		return NSQLVALUE;

	}

	public void sendBirthdayEmail() {
		List<User> users = userdao.findAll();
		LocalDate today = LocalDate.now(); // Get today's date
		String NSQLVALUE = NsqlConfig("BIRTHDAYEMAILALERT");
		for (User user : users) {
			Optional<UserDetail> optional = userDetailDao.findByIdField(user.getId());
			UserDetail userDetail = optional.get();
			if (user.getDob() != null && isBirthdayToday(user.getDob(), today) && NSQLVALUE.equals("1")
					&& user.getDob().equals(userDetail.getDob())) { // Ensure DOB is not null

				Optional<CompanyInfo> birthdayGifUrl = company_dao.findByCompanyIdOptional(user.getCompany_id()); // Fetch
																													// GIF
																													// URL
																													// from
																													// the
																													// company
																													// entity
				CompanyInfo birthdayGif = birthdayGifUrl.get();
				String subject = "🎉 Happy Birthday, " + user.getUsername() + "!";
				String to = user.getEmail();
				String message = "<!DOCTYPE html>" + "<html lang='en'>" + "<head>" + "    <meta charset='UTF-8'>"
						+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
						+ "    <title>Happy Birthday!</title>" + "    <style>"
						+ "        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; text-align: center; }"
						+ "        .email-wrapper { width: 100%; padding: 40px 0; background-color: #f4f6f9; }"
						+ "        .email-container { max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; "
						+ "            box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1); text-align: center; margin: 0 auto; padding: 20px; }"
						+ "        .email-header { background-color: #ffcc00; color: #333; padding: 20px; font-size: 26px; font-weight: bold; }"
						+ "        .email-body { padding: 30px; text-align: center; }"
						+ "        .email-body p { font-size: 18px; color: #555; line-height: 1.6; margin-bottom: 20px; }"
						+ "        .email-footer { background-color: #f7f9fc; padding: 15px; font-size: 14px; color: #555555; }"
						+ "        .email-footer a { color: #007bff; text-decoration: none; font-weight: bold; }"
						+ "        .birthday-gif { width: 100%; max-width: 500px; border-radius: 10px; }"
						+ "    </style>" + "</head>" + "<body>" + "    <div class='email-wrapper'>"
						+ "        <div class='email-container'>"
						+ "            <div class='email-header'>🎂 Happy Birthday, " + user.getUsername()
						+ "! 🎉</div>" + "            <div class='email-body'>"
						+ "                <img class='birthday-gif' src='" + birthdayGif.getBirthdayGif()
						+ "' alt='Happy Birthday' />" + "                <p>Dear " + user.getUsername() + ",</p>"
						+ "                <p>Wishing you a day filled with love, joy, and all the things that make you happy!</p>"
						+ "                <p>May this year bring you success, good health, and wonderful memories.</p>"
						+ "                <p>Enjoy your special day! 🎈</p>"
						+ "                <p>Best Wishes,<br>The Team</p>" + "            </div>"
						+ "            <div class='email-footer'>"
						+ "                <p>Need assistance? <a href='#'>Contact Support</a></p>"
						+ "            </div>" + "        </div>" + "    </div>" + "</body>" + "</html>";
				emailService.sendEmail(message, subject, to);
			}
		}
	}

	/**
	 * Converts a String DOB to LocalDate and checks if today is the birthday.
	 */
	private boolean isBirthdayToday(String dobString, LocalDate today) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Adjust format if needed
			LocalDate dob = LocalDate.parse(dobString, formatter);
			return dob.getMonth() == today.getMonth() && dob.getDayOfMonth() == today.getDayOfMonth();
		} catch (DateTimeParseException e) {
			System.out.println("Invalid DOB format for: " + dobString);
			return false;
		}
	}

	@Transactional
	public Contact register1(Contact contact, String ipaddress, String location) throws Exception {
		try {
			Optional<Contact> optional = contactDao.findByEmailAndPhone(contact.getEmail(), contact.getPhone());

			if (optional.isEmpty()) {
				// Fetch last ID safely
				Long lastId = contactDao.getContactLastId();
				long id = (lastId == null || lastId <= 0) ? 1L : lastId + 1; // ✅ Proper handling of null values

				contact.setId(id);
				contact.setClient_ip(ipaddress);
				contact.setClient_location(location);

				// Set default metadata
				contact.setAdddate(new Date());
				contact.setAddwho("ROOT USER");
				contact.setEditdate(new Date());
				contact.setEditwho("ROOT USER");

				String to = contact.getEmail();
				String subject = "Thank You for Reaching Out to Us!";

				String message = "<html>" + "<body style='font-family: Arial, sans-serif; color: #333;'>"
						+ "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; background-color: #f9f9f9;'>"
						+ "<h2 style='color: #0056b3; text-align: center;'>Thank You for Getting in Touch!</h2>"
						+ "<p>Dear " + contact.getName() + ",</p>"
						+ "<p>We appreciate you reaching out to us. Our team has received your request, and we will review it shortly.</p>"
						+ "<p><strong>Details of Your Request:</strong></p>" + "<ul>" + "<li><strong>Name:</strong> "
						+ contact.getName() + "</li>" + "<li><strong>Email:</strong> " + contact.getEmail() + "</li>"
						+ "<li><strong>Phone:</strong> " + contact.getPhone() + "</li>"
						+ "<li><strong>Message:</strong> " + contact.getMessage() + "</li>"
						+ "<li><strong>Submitted On:</strong> "
						+ new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date()) + "</li>" + "</ul>"
						+ "<p>We strive to respond as quickly as possible. If your request is urgent, feel free to contact our support team.</p>"
						+ "<p>Best Regards,<br>" + "<strong>" + "<span style='color: rgb(66, 133, 244);'>W</span>"
						+ "<span style='color: rgb(255, 0, 0);'>W</span>"
						+ "<span style='color: rgb(255, 165, 0);'>W</span>"
						+ "<span style='color: rgb(0, 0, 255);'>.</span>"
						+ "<span style='color: rgb(60, 179, 113);'>E</span>"
						+ "<span style='color: rgb(255, 0, 0);'>M</span>"
						+ "<span style='color: rgb(0, 0, 255);'>S</span>"
						+ "<span style='color: rgb(255, 0, 0);'>.</span>"
						+ "<span style='color: rgb(255, 165, 0);'>C</span>"
						+ "<span style='color: rgb(0, 0, 255);'>O</span>"
						+ "<span style='color: rgb(255, 0, 0);'>M</span>" + "</strong></p>" + "<hr>"
						+ "<p style='text-align: center; font-size: 12px; color: #666;'>"
						+ "This is an automated email. Please do not reply.</p>" + "</div>" + "</body>" + "</html>";

				emailService.sendEmail(message, subject, to);
				return contactDao.save(contact); // Save and return the saved object
			} else {
				throw new Exception("You cannot submit another request right now. Please try again later.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e; // Rethrow exception to ensure transaction rollback
		}
	}

	public List<Attendance> getAllAttendanceById(int UserId) {
		return attendanceDao.findDistinctStatusByEmployeeId(UserId);
	}

	public List<Holiday> getAllHolidaysByCompanyInfo(String CompanyId) {
		return holidayDao.findAllHolidaysByCompanyInfo(CompanyId);
	}

	public List<String> getAllLaptopBrands(String companyId) {
		return laptopDao.findAllDistinctBrands(companyId); 
	} 

	public List<Laptop> findModelsByBrand(String brand, String companyId) {
		return laptopDao.findByBrand(brand, companyId);
	}

	public List<Laptop> findSerialNumbersByModel(String brand, String model, String companyId) {
		return laptopDao.findByBrandAndModel(brand, model, companyId);
	}
 
	public List<Laptop> findProductIdAndColorBySerialNumber(String brand, String model, String serialnumber,
			String companyId) {
		return laptopDao.findByBrandAndModelAndSerialNumber(brand, model, serialnumber, companyId);
	}

//	public List<laptop> getLaptopsByModel(String brand, String companyId) {
//		return laptopDao.findByBrandORModel(brand, companyId);
//	}

	public UserDetail getUserDetailById(Integer id) throws Exception {
		Optional<UserDetail> userOptional = userDetailDao.findByIdField(id);
		if (userOptional.isPresent()) {
			return userOptional.get();
		} else {
			throw new Exception("User not found with ID: " + id);
		}
	}

	public Map<String, Object> getUserDetailAndTeams(Integer id) throws Exception {
		Optional<UserDetail> userOptional = userDetailDao.findByIdField(id);
		if (userOptional.isPresent()) {
			UserDetail userDetail = userOptional.get();
			List<Team> teams = teamDao.findAllByCompanyId(userDetail.getCompany_id());

			// Store data in map to pass both objects
			Map<String, Object> data = new HashMap<>();
			data.put("userDetail", userDetail);
			data.put("teams", teams);

			return data;
		} else {
			throw new Exception("User not found with ID: " + id);
		}
	}

	public boolean updateUserTeam(int id, UserDetail userDetail, String email) throws Exception {
		// Fetch UserDetail by ID
		Optional<UserDetail> optionalUserdetail = userDetailDao.findByIdField(id);
		if (optionalUserdetail.isEmpty()) {
			throw new RuntimeException("User not found.");
		}

		// Fetch User by ID
		Optional<User> optionalUser = userdao.findByIdField(id);
		if (optionalUser.isEmpty()) {
			throw new RuntimeException("User not found.");
		}

		// Log Manager email
		System.out.println("MANAGER EMAIL: " + email + ".");

		// Fetch the logged-in user (assumed to be the manager) by email
		Optional<User> optionalLoggedInUser = userdao.findByEmail(email);
		if (optionalLoggedInUser.isEmpty()) {
			throw new RuntimeException("Manager user not found.");
		}

		// Existing user detail and user
		UserDetail existingUserDetail = optionalUserdetail.get();
		User existingUser = optionalUser.get();

		// Log manager ID (could be null)
		System.out.println("MANAGER ID: " + existingUserDetail.getManagerId() + ".");

		// Handle default for manager ID if null
		if (existingUserDetail.getManagerId() == null) {
			existingUserDetail.setManagerId(0); // You can set default here if necessary
		}

		// If manager ID is 0, assign the currently logged-in user's ID as the manager
		if (existingUserDetail.getManagerId() == 0) {
			existingUserDetail.setManagerId(optionalLoggedInUser.get().getId());
		}

		// Prepare data
		String inputTeam = userDetail.getTeam().trim();
		String teamData = teamDao.getAllDataFromTeamDescription(inputTeam, existingUser.getCompany_id());

		if (teamData == null) {
			return false; // Invalid team
		}

		// Parse team data
		String[] teamArray = teamData.split(",");
		String teamId = teamArray[0];
		String teamDesc = teamArray[1];
		String baseLocation = userDetail.getBase_location();
		String teamName = teamArray[1];
		String team_unique_id = userDetail.getTeam_id();

		// Prevent assigning the same team again
		if (inputTeam.equals(existingUser.getTeam())) {
			throw new RuntimeException("Same Team Cannot Be Reassigned.");
		}

		if (teamId.equals("0")) {
			existingUserDetail.setManagerId(0); // You can set default here if necessary
		}

		// Update User entity
		existingUser.setTeam(teamId);
		existingUser.setBase_location(baseLocation);
		existingUser.setTeam_name(teamName);
		existingUser.setTeam_id(team_unique_id);
		userdao.save(existingUser);

		// Update UserDetail entity
		existingUserDetail.setTeam(teamId);
		existingUserDetail.setBase_location(baseLocation);
		existingUserDetail.setTeam_name(teamName);
		existingUserDetail.setTeam_id(team_unique_id);
		userDetailDao.save(existingUserDetail);

		// Send team update email
		sendTeamUpdateEmail(existingUserDetail, teamId, teamDesc);

		return true;
	}

	private void sendTeamUpdateEmail(UserDetail user, String teamId, String teamDesc) throws Exception {
		String message = "<div style='font-family: Arial, sans-serif; margin: 0; padding: 0;'>"
				+ "<table width='100%' cellspacing='0' cellpadding='0' style='background-color: #f4f4f4; padding: 20px;'>"
				+ "<tr><td align='center'>"
				+ "<table width='600' cellspacing='0' cellpadding='20' style='background-color: #ffffff; border-radius: 8px;'>"
				+ "<tr><td>" + "<h2 style='font-size: 24px; color: #333;'>Dear " + user.getUsername() + ",</h2>"
				+ "<p style='font-size: 16px; color: #555;'>Your team has been updated to <b>" + teamId + " -> "
				+ teamDesc + "</b>.</p>" + "<p style='font-size: 16px; color: #555;'>Base Location: <b>"
				+ user.getBase_location() + "</b></p>"
				+ "<br><p style='font-size: 16px; color: #555;'>If you have any questions, feel free to reach out.</p>"
				+ "<br><p style='font-size: 16px; color: #555;'>Best regards,<br>Resource Management Team</p>"
				+ "</td></tr></table></td></tr></table></div>";

		String subject = "Team Assignment Notification - " + user.getId();
		teamEmailService.sendEmail(message, subject, user.getEmail()).get();
	}

	public Map<String, String> getTeamDetails(String teamId, String companyId) {
		Team team = teamDao.findByTeamIdAndCompanyId(teamId, companyId);

		Map<String, String> response = new HashMap<>();
		response.put("teamName", team != null ? team.getTeam_description() : "No Record Found");
		response.put("teamUniqueId", team != null ? String.valueOf(team.getId()) : "No Record Found");
		response.put("baseLocation", team != null ? team.getBase_location() : "No Record Found");

		return response;
	}

//	public void logError(Exception e, String methodName) {
//		StackTraceElement[] stackTrace = e.getStackTrace();
//		int lineNumber = stackTrace[0].getLineNumber();
//		String exceptionAsString = e.toString();
//		String className = this.getClass().getName();
//		String errorMessage = e.getMessage();
//
//		// Insert error log into DB
//		insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//	}

	public void updatePaymentStatus(String paymentId, String orderId, String status) {
		try {
			Payment_Order_Info paymentOrderInfo = orderDao.findByOrderId(orderId);

			if (paymentOrderInfo != null) {
				paymentOrderInfo.setPaymentId(paymentId);
				paymentOrderInfo.setStatus(status);

				if ("captured".equals(status)) {
					// Generate license number
					String licenseNumber = generateLicenseNumber();
					paymentOrderInfo.setLicense_number(licenseNumber);
					paymentOrderInfo.setLicense_status("ACTIVE");
					paymentOrderInfo.setStatus("paid");

					// Fetch subscription plans
					SubscriptionPlans subscriptionPlans = findSubscriptionPlans();
					float gst = subscriptionPlans.getGst() * 100;
					paymentOrderInfo.setDiscount(subscriptionPlans.getDiscount());
					paymentOrderInfo.setTax(gst + "%");

					// Calculate GST amount
					subscriptionPlansDao.getAllPlans().ifPresent(plan -> {
						paymentOrderInfo.setGst_amount(paymentOrderInfo.getAmount() * plan.getGst());

						// Extract validity
						String[] validity = plan.getPlan_description().trim().split("\\s+");
						int validityDays = Integer.parseInt(validity[1]);

						// Use AtomicReference to handle mutable Instant values
						AtomicReference<Instant> subscriptionStartDate = new AtomicReference<>(Instant.now());
						AtomicReference<Instant> subscriptionExpiryDate = new AtomicReference<>(
								Instant.now().plus(Duration.ofDays(validityDays)));

						Optional<User> userOptional = userdao.findByIdField(paymentOrderInfo.getId());
						userOptional.ifPresent(user -> {
							Payment_Order_Info lastPlan = orderDao.findLastPlanByCompanyId(user.getCompany_id());

							if (lastPlan != null) {
								subscriptionStartDate.set(lastPlan.getSubscription_expiry_date().toInstant());
								subscriptionExpiryDate
										.set(subscriptionStartDate.get().plus(Duration.ofDays(validityDays)));
								paymentOrderInfo.setLicense_status("WAITING");
							} else {
								paymentOrderInfo.setLicense_status("ACTIVE");
								update_enable_user_after_success_payment(user.getCompany_id());
							}

							paymentOrderInfo.setSubscription_start_date(Date.from(subscriptionStartDate.get()));
							paymentOrderInfo.setSubscription_expiry_date(Date.from(subscriptionExpiryDate.get()));
							paymentOrderInfo.setValidity(validityDays);

							// Calculate amount without GST
							float withoutGstAmount = paymentOrderInfo.getAmount() - paymentOrderInfo.getGst_amount();
							paymentOrderInfo.setAmount_without_gst(withoutGstAmount);
							paymentOrderInfo.setDiscount(plan.getDiscount());

							// Fetch company info and send invoice
							CompanyInfo companyInfo = findCompanyInfo();
							paymentOrderInfo.setGst_no(companyInfo.getGst_no());
							paymentOrderInfo.setQty(subscriptionPlans.getQty());

							try {
								generateAndSendInvoice(paymentOrderInfo, subscriptionPlans, companyInfo, user,
										licenseNumber);
							} catch (Exception e) {
								e.printStackTrace();
								System.err.println("Failed to send invoice email. Payment info updated successfully.");
							}
						});
					});
				} else if ("failed".equals(status)) {
					paymentOrderInfo.setLicense_status("NOT_ASSIGNED");
					paymentOrderInfo.setStatus("failed");
				} else if ("pending".equals(status)) {
					paymentOrderInfo.setLicense_status("NOT_ASSIGNED");
					paymentOrderInfo.setStatus("pending");
				}

				orderDao.save(paymentOrderInfo);
			} else {
				System.out.println("Order not found.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error updating payment status.");
		}
	}

	public String checkPaymentStatus(String paymentId) {
		try {
			RazorpayClient razorpayClient = new RazorpayClient("rzp_test_icIfOJXJUlRjph", "L90mE03ZqQXO5rgWxRnn8JCn");

			// Fetch the payment details using paymentId
			Payment payment = razorpayClient.Payments.fetch(paymentId);
			JSONObject paymentDetails = new JSONObject(payment.toString());

			String status = paymentDetails.getString("status"); // "captured", "failed", "pending"
			return status;

		} catch (Exception e) {
			if (e.getMessage().contains("BAD_REQUEST_ERROR")) {
				return "Invalid Payment ID: " + paymentId;
			} else {
				return "An error occurred while fetching payment status: " + e.getMessage();
			}
		}
	}

	public void PendingPaymentStatus() {
		int count1 = '0';
		// Fetch all pending payments from the database
		List<Payment_Order_Info> pendingPayments = orderDao.findPendingPayments();

		for (Payment_Order_Info paymentOrderInfo : pendingPayments) {
			String paymentId = paymentOrderInfo.getPaymentId();
			String status = checkPaymentStatus(paymentId);
			count1 = count1 + 1;
			System.out.println(count1 + " PENDING PAYMENT STATUS " + status);
			if ("captured".equals(status)) {
				updatePaymentStatus(paymentId, paymentOrderInfo.getOrderId(), status);
			}
		}
	}

	public List<String> GetAllCompanyID() {
		List<String> companyInfos = company_dao.getAllCompanyId();
		return companyInfos;
	}

	@Transactional
	public boolean getUserDetailByIdAndSaveTask(UserDetail userDetail, User user, String taskStatus) {
		try {
			System.out.println("USERDETAIL ID: " + userDetail.getId());

//	         // Fetch existing user details
//	         UserDetail userDetail1 = getUserDetailById(userDetail.getId());
//	         System.out.println("TASK ASSIGNED USER: " + userDetail1);

			// Generate Unique Task ID
			String taskId = generateTaskId(); // Call generateTaskId method

			// Create and save a new Task entry
			Tasks task = new Tasks();
			task.setTaskId(taskId); // Assign generated task ID
			task.setId(userDetail.getId()); // Assign user ID
			task.setEmail(userDetail.getEmail());
			task.setTaskAssignedDate(userDetail.getTaskAssignedDate()); // Assign Date directly
			task.setTaskDescription(userDetail.getTaskDescription());
			task.setTaskEndDate(userDetail.getTaskEndDate()); // Assign Date directly
			task.setWhoAssignedTaskEmail(user.getEmail());
			if (taskStatus.equals("Pending")) {
				task.setTaskPending(true);
				task.setTaskStatus("Pending");
			} else if (taskStatus.equals("In Progress")) {
				task.setTaskInProgress(true);
				task.setTaskStatus("In Progress");
			} else if (taskStatus.equals("Completed")) {
				task.setTaskInProgress(true);
				task.setTaskStatus("Completed");
			} else {
				task.setTaskOverdue(true);
				task.setTaskStatus("Overdue");
			}
			System.out.println("CURRENT USER " + user.getId());
			task.setWhoAssignedTask(String.valueOf(user.getId()));
			task.setAddwho(String.valueOf(user.getId()));
			task.setEditwho(String.valueOf(user.getId()));
			task.setEditdate(new Date());
			task.setAdddate(new Date()); // Set current date for add date

			// Save task in database
			taskDao.save(task);
			String to = userDetail.getEmail();
			String cc = user.getEmail();
			String message = "";

			String subject = "";

			if (taskStatus.equals("Pending")) {
				subject = "\uD83D\uDCC5 New Task Assigned – [Task ID: " + taskId + "] - Action Required"; // 📅
																											// (Calendar)
			} else if (taskStatus.equals("In Progress")) {
				subject = "\uD83D\uDCC8 Task In Progress – [Task ID: " + taskId + "] - Keep Up the Good Work!"; // 📈
																												// (Chart
																												// Increasing)
			} else if (taskStatus.equals("Completed")) {
				subject = "\u2705 Task Completed – [Task ID: " + taskId + "] - Well Done!"; // ✅ (Check Mark)
			} else if (taskStatus.equals("Overdue")) {
				subject = "\u26A0 Task Overdue – [Task ID: " + taskId + "] - Immediate Action Required"; // ⚠️ (Warning)
			}

			if (taskStatus.equals("Pending")) {
				message = "<html><body>" + "<p>Dear " + userDetail.getUsername() + ",</p>"
						+ "<p><b>Action Required:</b> A new task has been assigned to you. Please review and start working on it as soon as possible.</p>"
						+ "<h3>Task Details:</h3>" + "<ul>" + "<li><b>Task ID:</b> " + taskId + "</li>"
						+ "<li><b>Description:</b> " + userDetail.getTaskDescription() + "</li>"
						+ "<li><b>Assigned Date:</b> " + userDetail.getTaskAssignedDate() + "</li>"
						+ "<li><b>Due Date:</b> " + userDetail.getTaskEndDate() + "</li>" + "<li><b>Assigned By:</b> "
						+ user.getUsername() + "</li>" + "</ul>"
						+ "<p>Please ensure that the task is completed within the specified timeline.</p>"
						+ "<p>If you have any questions, feel free to reach out.</p>" + "<p><b>Best regards,</b><br/>"
						+ user.getUsername() + "<br/>" + "Your Company Name</p>" + "</body></html>";
				taskEmailService.sendEmail(message, subject, to, cc);

			} else if (taskStatus.equals("In Progress")) {
				message = "<html><body>" + "<p>Dear " + userDetail.getUsername() + ",</p>"
						+ "<p><b>Task In Progress:</b> You have started working on the assigned task. Please ensure timely completion.</p>"
						+ "<h3>Task Details:</h3>" + "<ul>" + "<li><b>Task ID:</b> " + taskId + "</li>"
						+ "<li><b>Description:</b> " + userDetail.getTaskDescription() + "</li>"
						+ "<li><b>Assigned Date:</b> " + userDetail.getTaskAssignedDate() + "</li>"
						+ "<li><b>Due Date:</b> " + userDetail.getTaskEndDate() + "</li>" + "<li><b>Assigned By:</b> "
						+ user.getUsername() + "</li>" + "</ul>"
						+ "<p>Keep up the great work! If you need any assistance, feel free to reach out.</p>"
						+ "<p><b>Best regards,</b><br/>" + user.getUsername() + "<br/>" + "Your Company Name</p>"
						+ "</body></html>";
				taskEmailService.sendEmail(message, subject, to, cc);

			} else if (taskStatus.equals("Completed")) {
				message = "<html><body>" + "<p>Dear " + userDetail.getUsername() + ",</p>"
						+ "<p><b>Task Completed:</b> Your assigned task has been successfully marked as completed. Great job!</p>"
						+ "<h3>Task Details:</h3>" + "<ul>" + "<li><b>Task ID:</b> " + taskId + "</li>"
						+ "<li><b>Description:</b> " + userDetail.getTaskDescription() + "</li>"
						+ "<li><b>Assigned Date:</b> " + userDetail.getTaskAssignedDate() + "</li>"
						+ "<li><b>Due Date:</b> " + userDetail.getTaskEndDate() + "</li>" + "<li><b>Assigned By:</b> "
						+ user.getUsername() + "</li>" + "</ul>"
						+ "<p>Thank you for your dedication and effort in completing the task.</p>"
						+ "<p><b>Best regards,</b><br/>" + user.getUsername() + "<br/>" + "Your Company Name</p>"
						+ "</body></html>";
				taskEmailService.sendEmail(message, subject, to, cc);

			} else if (taskStatus.equals("Overdue")) {
				message = "<html><body>" + "<p>Dear " + userDetail.getUsername() + ",</p>"
						+ "<p><b>Task Overdue:</b> The following task has exceeded its due date. Immediate action is required.</p>"
						+ "<h3>Task Details:</h3>" + "<ul>" + "<li><b>Task ID:</b> " + taskId + "</li>"
						+ "<li><b>Description:</b> " + userDetail.getTaskDescription() + "</li>"
						+ "<li><b>Assigned Date:</b> " + userDetail.getTaskAssignedDate() + "</li>"
						+ "<li><b>Due Date:</b> " + userDetail.getTaskEndDate() + "</li>" + "<li><b>Assigned By:</b> "
						+ user.getUsername() + "</li>" + "</ul>"
						+ "<p>Please take the necessary steps to complete the task as soon as possible.</p>"
						+ "<p>If you require any assistance, do not hesitate to reach out.</p>"
						+ "<p><b>Best regards,</b><br/>" + user.getUsername() + "<br/>" + "Your Company Name</p>"
						+ "</body></html>";
				taskEmailService.sendEmail(message, subject, to, cc);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Transactional
	public String generateTaskId() {
		// Step 1: Increment the last task ID in the database
		String updateQuery = "UPDATE task_id_tracker SET last_task_id = last_task_id + 1";
		jdbcTemplate.update(updateQuery);

		// Step 2: Retrieve the updated task ID
		String selectQuery = "SELECT last_task_id FROM task_id_tracker";
		int lastTaskId = jdbcTemplate.queryForObject(selectQuery, Integer.class);

		// Step 3: Format it as "TASK-1001"
		return "TASK-" + lastTaskId;
	}

	public Map<String, Long> getTaskCounts(int id) {
		List<Object[]> results = taskDao.fetchTaskCounts(id); // Fetch as a List

		if (results == null || results.isEmpty() || results.get(0) == null) {
			return Map.of("pending", 0L, "completed", 0L, "overdue", 0L, "inProgress", 0L, "deleted", 0L);
		}

		Object[] row = results.get(0); // Get the first (and only) row
		System.out.println("DEBUG: Raw Query Result = " + Arrays.toString(row)); // Debugging output

		return Map.of("pending", convertToLong(row[0]), "completed", convertToLong(row[1]), "overdue",
				convertToLong(row[2]), "inProgress", convertToLong(row[3]), "deleted", convertToLong(row[4]));
	}

	private long convertToLong(Object obj) {
		if (obj instanceof Number) {
			return ((Number) obj).longValue();
		}
		return 0L;
	}

	public List<Tasks> getTasksByUserId(int userId) {
		List<Tasks> tasklist = taskDao.findTasksByUserId(userId);
		return tasklist;
	}

	public boolean deleteTaskById(String taskId) {
		try {
			taskDao.deleteById(taskId);
			return true; // Task deleted successfully
		} catch (Exception e) {
			e.printStackTrace(); // Log the error for debugging
			return false; // Deletion failed
		}
	}

	public Optional<Tasks> updateTaskStatusById(String taskId) {
		return taskDao.findById(taskId);
	}

	public Optional<Tasks> editTask(String taskId) {
		return taskDao.findById(taskId);
	}

	public Tasks getTaskById(String taskId) {
		Optional<Tasks> task = taskDao.findById(taskId);
		return task.orElse(null);
	}

	// Update task status
	public void updateTaskStatus(String taskId, String taskStatus) throws Exception {
		Tasks task = getTaskById(taskId);
		if (task != null) {
			Optional<Tasks> getTask = taskDao.findTasksByTaskId(taskId);
			if (getTask.isPresent()) {
				switch (taskStatus) {
				case "Pending":
					task.setTaskPending(true);
					task.setTaskInProgress(false);
					task.setTaskCompleted(false);
					task.setTaskOverdue(false);
					task.setTaskCompletedDate(null);
					task.setTaskStatus("Pending");
					break;
				case "In Progress":
					task.setTaskPending(false);
					task.setTaskInProgress(true);
					task.setTaskCompleted(false);
					task.setTaskOverdue(false);
					task.setTaskCompletedDate(null);
					task.setTaskStatus("In Progress");
					break;
				case "Completed":
					task.setTaskPending(false);
					task.setTaskInProgress(false);
					task.setTaskCompleted(true);
					task.setTaskOverdue(false);
					task.setTaskCompletedDate(new Date());
					task.setTaskStatus("Completed");
					break;
				case "Overdue":
					task.setTaskPending(false);
					task.setTaskInProgress(false);
					task.setTaskCompleted(false);
					task.setTaskOverdue(true);
					task.setTaskCompletedDate(null);
					task.setTaskStatus("Overdue");
					break;
				}
				taskDao.save(task); // Save updated task status

				System.out.println("TASK SENDER EMAIL " + task.getEmail());
				System.out.println("TASK RECEIVER EMAIL " + task.getWhoAssignedTaskEmail());

				if (!task.getEmail().equals(task.getWhoAssignedTaskEmail())) {
					Notification notification = new Notification();
					notification.setMessage_id(generateMessageId());
					notification.setMessage(
							"Task ID: " + taskId + " has been updated. The task status has been modified by EMP ID: "
									+ task.getId() + " with status: " + task.getTaskStatus() + ".");
					notification.setUserId(Integer.parseInt(task.getWhoAssignedTask()));
					notification.setTimestamp(new Date());
					notification.setAdddate(new Date());
					notification.setAddwho("SYSTEM");
					notification.setSenderEmail("guptaayush12418@gmail.com");
					notification.setReceiverEmail(task.getWhoAssignedTaskEmail());
					notification.setEditdate(new Date());
					notification.setEditwho(task.getEditwho());

					notificationDao.save(notification);

					System.out.println("Sending notification to: " + task.getWhoAssignedTaskEmail());
					System.out.println("Notification:::: " + notification.getMessage());

					NotificationListener.send(task.getWhoAssignedTaskEmail(), notification); // ✅ Send only to receiver
				} else {
					System.out.println("Skipping notification: sender and receiver are the same.");
				}
			} else {
				throw new Exception(
						" Task with ID '" + taskId + "' has been closed by the manager and cannot be modified.");
			}

		}
	}

	// Update task status
	public void updateTaskStatuss(String taskId, String taskStatus) throws Exception {
		Tasks task = getTaskById(taskId);
		if (task != null) {
			Optional<Tasks> getTask = taskDao.findTasksByTaskId(taskId);
			if (getTask.isPresent()) {
				switch (taskStatus) {
				case "Pending":
					task.setTaskPending(true);
					task.setTaskInProgress(false);
					task.setTaskCompleted(false);
					task.setTaskOverdue(false);
					task.setTaskCompletedDate(null);
					task.setTaskStatus("Pending");
					break;
				case "In Progress":
					task.setTaskPending(false);
					task.setTaskInProgress(true);
					task.setTaskCompleted(false);
					task.setTaskOverdue(false);
					task.setTaskCompletedDate(null);
					task.setTaskStatus("In Progress");
					break;
				case "Completed":
					task.setTaskPending(false);
					task.setTaskInProgress(false);
					task.setTaskCompleted(true);
					task.setTaskOverdue(false);
					task.setTaskCompletedDate(new Date());
					task.setTaskStatus("Completed");
					break;
				case "Overdue":
					task.setTaskPending(false);
					task.setTaskInProgress(false);
					task.setTaskCompleted(false);
					task.setTaskOverdue(true);
					task.setTaskCompletedDate(null);
					task.setTaskStatus("Overdue");
					break;
				}
				taskDao.save(task); // Save updated task status
			} else {
				throw new Exception(
						" Task with ID '" + taskId + "' has been closed by the manager and cannot be modified.");
			}

		}
	}

	public List<Tasks> getPendingTasksByUserId(Integer userId) {
		return taskDao.findByUserIdAndTaskPendingTrue(userId);
	}

	public List<Tasks> getInProgressTasksByUserId(Integer userId) {
		return taskDao.findByUserIdAndTaskInProgressTrue(userId);
	}

//    public List<Tasks> searchTasksByTaskId(int id,String company_id) {
//        return taskDao.findTasksByEmpIdAndCompanyId(id,company_id);
//    }
//    
//    public List<Tasks> searchManagerTasksByTaskId(int id,String company_id) {
//        return taskDao.managerFindTasksByEmpIdAndCompanyId(id,company_id);
//    }
//    
	public List<Tasks> getCompletedTasksByUserId(Integer userId) {
		return taskDao.findByUserIdAndTaskCompletedTrue(userId);
	}

	public List<Tasks> getOverdueTasksByUserId(Integer userId) {
		return taskDao.findByUserIdAndTaskOverdueTrue(userId);
	}

//    public List<Tasks> searchTasksByTaskId(String query) {
//        return taskDao.findByTaskIdContainingIgnoreCase(query);
//    }

	public List<Tasks> searchTasksByTaskId(String query, int id, String role) {
		if (role.equals("ROLE_MANAGER")) {
			return taskDao.managerSearchByTaskIdAndUserId(query, id);
		} else {
			return taskDao.searchByTaskIdAndUserId(query, id);
		}
	}

	public Integer getTotalTaskCount(Integer userId) {
		return taskDao.countTotalTasksByUserId(userId);
	}

	public Integer getTasksInProgressCount(Integer userId) {
		return taskDao.countTasksInProgress(userId);
	}

	public Integer getTasksPendingCount(Integer userId) {
		return taskDao.countTasksPending(userId);
	}

	public Integer getTasksCompletedCount(Integer userId) {
		return taskDao.countTasksCompleted(userId);
	}

	public Integer getTasksOverdueCount(Integer userId) {
		return taskDao.countTasksOverdue(userId);
	}

//    public Map<String, Integer> getAllTaskCounts(Integer userId) {
//        Map<String, Integer> taskCounts = new HashMap<>();
//        taskCounts.put("totalTasks", getTotalTaskCount(userId));
//        taskCounts.put("tasksInProgress", getTasksInProgressCount(userId));
//        taskCounts.put("tasksPending", getTasksPendingCount(userId));
//        taskCounts.put("tasksCompleted", getTasksCompletedCount(userId));
//        taskCounts.put("tasksOverdue", getTasksOverdueCount(userId));
//        taskCounts.put("efficiency", getEfficiency(userId)); // Include efficiency
//        return taskCounts;
//    }
//    
//    public Integer getEfficiency(Integer userId) {
//        Integer totalTasks = getTotalTaskCount(userId);
//        Integer completedTasks = getTasksCompletedCount(userId);
//
//        if (totalTasks == 0) {
//            return 0; // Avoid division by zero
//        }
//        return (completedTasks * 100) / totalTasks;
//    }

	// Get performance data over time (last 6 months)
	public Map<String, Integer> getTaskPerformanceData(Integer userId) {
		List<Object[]> results = taskDao.findTaskCompletionTrends(userId);
		Map<String, Integer> performanceData = new LinkedHashMap<>();

		for (Object[] result : results) {
			String month = result[0].toString();
			Integer count = ((Number) result[1]).intValue();
			performanceData.put(month, count);
		}
		return performanceData;
	}

//    public Map<String, Map<String, Integer>> getTaskPerformanceData(Integer userId) {
//        List<Object[]> results = taskDao.getTaskPerformanceData(userId);
//        Map<String, Map<String, Integer>> performanceData = new LinkedHashMap<>();
//
//        for (Object[] row : results) {
//            String month = row[0].toString();
//            Map<String, Integer> data = new HashMap<>();
//            data.put("total_tasks", ((Number) row[1]).intValue());
//            data.put("completed_tasks", ((Number) row[2]).intValue());
//            data.put("pending_tasks", ((Number) row[3]).intValue());
//            data.put("in_progress_tasks", ((Number) row[4]).intValue());
//            data.put("overdue_tasks", ((Number) row[5]).intValue());
//            data.put("late_completed_tasks", ((Number) row[6]).intValue());
//            data.put("on_time_completed_tasks", ((Number) row[7]).intValue());
//            performanceData.put(month, data);
//        }
//
//        return performanceData;
//    }

	public List<TaskPerformance> getTaskPerformanceDataForGraph(int userId) {
		List<Object[]> results = taskDao.getTaskPerformanceData(userId);

		// Generate a list of all months in 'YYYY-MM' format
		Calendar calendar = Calendar.getInstance();
		int currentYear = calendar.get(Calendar.YEAR);
		List<String> allMonths = new ArrayList<>();

		for (int month = 1; month <= 12; month++) {
			allMonths.add(String.format("%d-%02d", currentYear, month));
		}

		// Map to store user-wise task performance data
		Map<String, Map<String, TaskPerformance>> userPerformanceMap = new HashMap<>();

		// Populate map with actual data from the query
		for (Object[] row : results) {
			if (row.length < 15) { // Ensure row has expected number of columns
				continue;
			}

			String month = row[0].toString();
			String username = row[8].toString(); // Extract username from the result set

			// Ensure a nested map exists for each username
			userPerformanceMap.putIfAbsent(username, new HashMap<>());
			Map<String, TaskPerformance> performanceDataMap = userPerformanceMap.get(username);

			// Create and store task performance data
			TaskPerformance data = new TaskPerformance();
			data.setMonth(month);
			data.setUsername(username);
			data.setTotalTasks(((Number) row[1]).intValue());
			data.setCompletedTasks(((Number) row[2]).intValue());
			data.setPendingTasks(((Number) row[3]).intValue());
			data.setInProgressTasks(((Number) row[4]).intValue());
			data.setOverdueTasks(((Number) row[5]).intValue());
			data.setLateCompletedTasks(((Number) row[6]).intValue());
			data.setOnTimeCompletedTasks(((Number) row[7]).intValue());

			data.setCompletedTaskList(
					row[9] != null ? Arrays.asList(row[9].toString().split(",")) : Collections.emptyList());
			data.setPendingTaskList(
					row[10] != null ? Arrays.asList(row[10].toString().split(",")) : Collections.emptyList());
			data.setInProgressTaskList(
					row[11] != null ? Arrays.asList(row[11].toString().split(",")) : Collections.emptyList());
			data.setOverdueTaskList(
					row[12] != null ? Arrays.asList(row[12].toString().split(",")) : Collections.emptyList());
			data.setLateCompletedTaskList(
					row[13] != null ? Arrays.asList(row[13].toString().split(",")) : Collections.emptyList());
			data.setOnTimeCompletedTaskList(
					row[14] != null ? Arrays.asList(row[14].toString().split(",")) : Collections.emptyList());
			data.setTotalTasksList(
					row[15] != null ? Arrays.asList(row[15].toString().split(",")) : Collections.emptyList());

			performanceDataMap.put(month, data);
		}

		// Ensure that all months are included for each user
		List<TaskPerformance> finalPerformanceData = new ArrayList<>();

		for (String username : userPerformanceMap.keySet()) {
			Map<String, TaskPerformance> performanceDataMap = userPerformanceMap.get(username);

			for (String month : allMonths) {
				TaskPerformance data = performanceDataMap.get(month);

				if (data == null) {
					// Create a default TaskPerformance with 0 values but with a username
					data = new TaskPerformance();
					data.setMonth(month);
					data.setUsername(username);
					data.setTotalTasks(0);
					data.setCompletedTasks(0);
					data.setPendingTasks(0);
					data.setInProgressTasks(0);
					data.setOverdueTasks(0);
					data.setLateCompletedTasks(0);
					data.setOnTimeCompletedTasks(0);
					data.setCompletedTaskList(Collections.emptyList());
					data.setPendingTaskList(Collections.emptyList());
					data.setInProgressTaskList(Collections.emptyList());
					data.setOverdueTaskList(Collections.emptyList());
					data.setLateCompletedTaskList(Collections.emptyList());
					data.setOnTimeCompletedTaskList(Collections.emptyList());
					data.setTotalTasksList(Collections.emptyList());
				}

				finalPerformanceData.add(data);
			}
		}

		return finalPerformanceData;
	}

	public Map<String, Integer> getAllTaskCounts(Integer userId) {
		Map<String, Integer> taskCounts = new HashMap<>();
		taskCounts.put("totalTasks", taskDao.countTotalTasksByUserId(userId));
		taskCounts.put("tasksInProgress", taskDao.countTasksInProgress(userId));
		taskCounts.put("tasksPending", taskDao.countTasksPending(userId));
		taskCounts.put("tasksCompleted", taskDao.countTasksCompleted(userId));
		taskCounts.put("tasksOverdue", taskDao.countTasksOverdue(userId));
		taskCounts.put("onTimeCompletedTasks", taskDao.getOnTimeCompletedCount(userId));
		taskCounts.put("lateCompletedTasks", taskDao.getLateCompletedCount(userId));
		taskCounts.put("efficiency", calculateEfficiency(userId)); // Efficiency calculation
		return taskCounts;
	}

	public Integer calculateEfficiency(Integer userId) {
		Integer totalTasks = taskDao.countTotalTasksByUserId(userId);
		Integer onTimeCompleted = taskDao.getOnTimeCompletedCount(userId);
		Integer lateCompleted = taskDao.getLateCompletedCount(userId);

		if (totalTasks == 0) {
			return 0; // Avoid division by zero
		}

		return (int) (((onTimeCompleted + (lateCompleted * 0.5)) / (double) totalTasks) * 100);
	}

	public long countNotification(int userId) {
		long count = notificationDao.countByUserIdAndReadFalse(userId);
		return count;
	}

	public List<Notification> getUnreadNotifications(String email) {
		List<Notification> messagesById = notificationDao.findNewMessgaesById(email);
		return messagesById;
	}

	public List<Notification> getReadNotifications(String email) {
		List<Notification> messagesById = notificationDao.findOldMessgaesById(email);
		return messagesById;
	}

	@Transactional
	public void markAllAsRead(String username) {
		// Assuming you store user_id in the Notification entity
		Integer userId = userdao.getUserByUserName(username).getId();

		// Mark all unread notifications as read for this user
		notificationDao.markAllAsReadByUserId(userId, new Date());
	}

	public Notification findMessageByMessageId(String messageId) {
		Optional<Notification> notification = notificationDao.findByMessageId(messageId);
		return notification.get();
	}

	@Transactional
	public void updateTaskStatusAndClose(String taskId, String newStatus, String currentUserEmail) throws Exception {
		Optional<Tasks> optionalTask = taskDao.findTasksByTaskIdByManager(taskId);

		if (optionalTask.isPresent()) {
			Tasks task = optionalTask.get();

			// Check and update status if different
			if (!task.getTaskStatus().equalsIgnoreCase(newStatus)) {
				updateTaskStatuss(taskId, newStatus);
				task.setTaskStatus(newStatus);
				System.out.println("TASK STATUS UPDATED TO: " + newStatus);
			}

			// Close the task
			task.setTaskClosed(true);
			task.setTaskReopen(false); // Optional: set reopen to false if needed

			taskDao.save(task);
			System.out.println("TASK CLOSED: " + task.isTaskClosed());

			Notification notification = new Notification();
			notification.setMessage_id(generateMessageId());
			notification.setMessage("Task ID: " + taskId + " Closed By THe Manager " + task.getId() + ".");
			notification.setUserId(task.getId());
			notification.setTimestamp(new Date());
			notification.setAdddate(new Date());
			notification.setAddwho("SYSTEM");
			notification.setEditdate(new Date());
			notification.setSenderEmail("guptaayush12418@gmail.com");
			notification.setReceiverEmail(task.getEmail());
			notification.setEditwho(task.getEditwho());
			notificationDao.save(notification);

			System.out.println("Sending notification to: " + task.getEmail());
			System.out.println("Notification: " + notification.getMessage());

			NotificationListener.send(task.getEmail(), notification); // ✅ Pass Notification object
		} else {
			throw new RuntimeException("Task not found with ID: " + taskId);
		}
	}

	@Transactional
	public void updateTaskStatusReopen(String taskId, String newStatus, String currentUserEmail) throws Exception {
		Optional<Tasks> optionalTask = taskDao.findTasksByTaskIdByManager(taskId);

		if (optionalTask.isPresent()) {
			Tasks task = optionalTask.get();
			System.out.println(task.getTaskStatus() + " TASK STATUS " + newStatus + " - > "
					+ !task.getTaskStatus().equalsIgnoreCase(newStatus));
			// Update status only if it's different
			if (!task.getTaskStatus().equalsIgnoreCase(newStatus)) {
				updateTaskStatuss(taskId, newStatus);
				task.setTaskStatus(newStatus);
				System.out.println("TASK STATUS UPDATED TO: " + newStatus);
			}

			// Reopen the task
			task.setTaskClosed(false);
			task.setTaskReopen(true);

			taskDao.save(task);
			System.out.println(
					"TASK REOPENED: TaskClosed=" + task.isTaskClosed() + ", TaskReopen=" + task.isTaskReopen());

			Notification notification = new Notification();
			notification.setMessage_id(generateMessageId());
			notification.setMessage("Task ID: " + taskId + " Reopen By The Manager ID " + task.getId() + ".");
			notification.setUserId(task.getId());
			notification.setTimestamp(new Date());
			notification.setAdddate(new Date());
			notification.setAddwho("SYSTEM");
			notification.setSenderEmail("guptaayush12418@gmail.com");
			notification.setReceiverEmail(task.getEmail());
			notification.setEditdate(new Date());
			notification.setEditwho(task.getEditwho());
			notificationDao.save(notification);

			NotificationListener.send(task.getEmail(), notification); // ✅ Pass Notification object
		} else {
			throw new RuntimeException("Task not found with ID: " + taskId);
		}
	}

	@Transactional
	public void archive_old_notification() {
		try {
			List<Notification> notifications = notificationDao.listOfNotification();
			for (Notification notification : notifications) {
				NotificationArchive notificationArchive = new NotificationArchive();
				notificationArchive.setMessage_id(notification.getMessage_id());
				notificationArchive.setMessage(notification.getMessage());
				notificationArchive.setUserId(notification.getUserId());
				notificationArchive.setMarkAsRead(notification.isMarkAsRead());
				notificationArchive.setMarkAsReadDate(notification.getMarkAsReadDate());
				notificationArchive.setReceiverEmail(notificationArchive.getReceiverEmail());
				notificationArchive.setSenderEmail(notification.getSenderEmail());
				notificationArchive.setRead(notification.isRead());
				notificationArchive.setTimestamp(notification.getTimestamp());
				notificationArchive.setEditdate(new Date());
				notificationArchive.setEditwho("SYSTEM ARCHIVE EDIT");
				notificationArchive.setAdddate(new Date());
				notificationArchive.setAddwho("SYSTEM ARCHIVE ADD");
				// Save the notification archive and check if it's not null (indicating success)
				NotificationArchive savedNotificationArchive = notificationArchiveDao.save(notificationArchive);

				if (savedNotificationArchive != null) {
					notificationDao.deleteById(notification.getMessage_id());
				}
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = Servicelayer.class.getName();
	        String errorMessage = e.getMessage();

	        // Capture full stack trace as a string
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        // Extract top stack trace element (if available)
	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        // Log error to database
	        insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	    }
	}

	public Map<String, Object> getTodayStatus(String email) {
		Map<String, Object> response = new HashMap<>();
		try {
			Optional<User> userOpt = userdao.findByEmail(email);
			if (userOpt.isEmpty()) {
				response.put("checkedIn", false);
				response.put("isAbsent", false);
				return response;
			}

			int empId = userOpt.get().getId();
			Optional<Attendance> latest = attendanceDao.findLatestAttendanceToday(empId);

			boolean checkedIn = latest.isPresent() && latest.get().getCheckInTime() != null
					&& latest.get().getCheckOutTime() == null;
			boolean isAbsent = latest.isPresent() && "absent".equalsIgnoreCase(latest.get().getStatus());

			response.put("checkedIn", checkedIn);
			response.put("isAbsent", isAbsent);

		} catch (Exception e) {
			e.printStackTrace();
			response.put("checkedIn", false);
			response.put("isAbsent", false);
		}
		return response;
	}

	public ResponseEntity<String> handleCheckIn(String email) {
		try {
			Optional<User> userOpt = userdao.findByEmail(email);
			if (userOpt.isEmpty())
				return ResponseEntity.badRequest().body("❌ User not found.");
			User user = userOpt.get();

			// ✅ Check if today is a holiday
			List<Holiday> holidays = holidayDao.findAllHolidaysByCompanyInfo(user.getCompany_id());
			boolean isHoliday = holidays.stream()
					.anyMatch(h -> (h.getType().equalsIgnoreCase("Public Holiday")
							|| h.getType().equalsIgnoreCase("Restricted Holiday")
							|| h.getType().equalsIgnoreCase("Festival")) && h.getDate().equals(LocalDate.now()));

			if (isHoliday) {
				return ResponseEntity.badRequest().body("❌ Attendance not allowed on holidays.");
			}

			// ✅ Check config for multi-attendance
			String configkey = "MULTIATTENDANCEALLOWED";
			String multiAllowed = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id(), configkey);

			if (multiAllowed.equals("0")) {
				Optional<Attendance> existing = attendanceDao.findByEmployeeIdAndDate(user.getId(), LocalDate.now());
				if (existing.isPresent())
					return ResponseEntity.badRequest().body("❌ Already checked in today.");
			}

			String configkey1 = "OFFICETIMINGS";
			String get_officeTimings = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id(), configkey1);

			// ✅ New Attendance Entry
			long count = attendanceDao.findAttendanceCount();
			Attendance att = new Attendance();
			  // 2️⃣ On approval, suspend any old “active” records for the same date range
            List<Attendance> oldOnes = attendanceDao.findActiveAttendance(user.getId(), LocalDate.now());
            for (Attendance oldRec : oldOnes) {
                oldRec.setStatus("Suspended");
                oldRec.setApproved_or_reject_or_pending("Suspended");
                oldRec.setEditdate(new Date());
                oldRec.setEditwho("User-SuspendOld-Status");
//                oldRec.setManager_id(user.getId());
//                oldRec.setEditTime(LocalTime.now());
                attendanceDao.save(oldRec);
            }
			att.setSno(count > 0 ? attendanceDao.findLastSno() + 1 : 1);
			att.setEmployee_id(user.getId());
			att.setDate(LocalDate.now());
			att.setStatus("Present");
			att.setAdddate(new Date());
			att.setAddwho("SYSTEM");
			att.setEditdate(new Date());
			att.setEditwho("SYSTEM");
			att.setCompany_id(user.getCompany_id());
			att.setCheckInTime(LocalTime.now());
			att.setShift(get_officeTimings);
			att.setApproved_or_reject_or_pending("No Record Found");
			att.setEmployee_name(user.getUsername());
			att.setEmail(user.getEmail());
			att.setTeam_id(user.getTeam());
			String request_id = generateRequestId();
			att.setRequest_id(request_id);
			attendanceDao.save(att);

			return ResponseEntity.ok("✅ Checked in successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Check-in failed.");
		}
	}

	@Transactional
	public void markAbsenteesForAllCompanies(String companyId) {
	    try {
	        System.out.println("🔁 Marking absentees for company: " + companyId);

	        LocalDate today = LocalDate.now();
	        LocalTime now = LocalTime.now();
	        DayOfWeek todayDay = today.getDayOfWeek();

	        // Step 1: Skip if today is a weekend for this company
	        List<String> weekendDays = holidayDao.getCompanyWeekendDays(companyId);
	        if (weekendDays.stream().anyMatch(d -> d.equalsIgnoreCase(todayDay.name()))) {
	            System.out.println("⛔ Skipped absent marking for " + companyId + " (Weekend: " + todayDay + ")");
	            return;
	        }

	        // Step 2: Get office timings
	        String officeTimeRange = nSqlConfigDao.findbyConfigKeyAndCompanyId(companyId, "OFFICETIMINGS");
	        System.out.println(companyId+"| OFFICE TIME |"+officeTimeRange);
	        if (officeTimeRange == null || !officeTimeRange.contains("-")) {
	            System.out.println("⚠️ Office timing not configured for " + companyId);
	            return;
	        }

	        String endTimeStr = officeTimeRange.split("-")[1].trim().toUpperCase()
	                .replaceAll("\\.", "").replaceAll("\\s+", "");

	        LocalTime officeEndTime;
	        try {
	            if (endTimeStr.contains("AM") || endTimeStr.contains("PM")) {
	                DateTimeFormatter formatter12 = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
	                officeEndTime = LocalTime.parse(endTimeStr, formatter12);
	            } else {
	                DateTimeFormatter formatter24 = DateTimeFormatter.ofPattern("H:mm");
	                officeEndTime = LocalTime.parse(endTimeStr, formatter24);
	            }
	        } catch (Exception e) {
	            System.out.println("⚠️ Failed to parse office time. Defaulting to 18:30.");
	            officeEndTime = LocalTime.of(18, 30);
	        }

	        // Step 3: Skip if current time is before office end time
	        if (now.isBefore(officeEndTime)) {
	            System.out.println("⏳ Too early to mark absent for " + companyId + " (Office ends at " + officeEndTime + ")");
	            return;
	        }

	        // Step 4: Start processing absentees
	        long sno = attendanceDao.findAttendanceCount() > 0
	                ? attendanceDao.findLastSno() + 1
	                : 1;

	        List<Object[]> absentUsers = attendanceDao.findEmployeesWithoutValidAttendanceToday(companyId);
	        List<Attendance> toInsert = new ArrayList<>();
	        List<Attendance> toUpdate = new ArrayList<>();

	        for (Object[] obj : absentUsers) {
	            Integer userId = (Integer) obj[0];
	            String empCompanyId = (String) obj[1];
	            String empName = (String) obj[2];
	            String teamId = (String) obj[3];

	            if (!companyId.equalsIgnoreCase(empCompanyId)) continue;

	            Optional<Attendance> existingOpt = attendanceDao.findByEmployeeIdAndDate(userId, today);
	            if (existingOpt.isPresent()) {
	                Attendance old = existingOpt.get();
	                if ("Withdrawn".equalsIgnoreCase(old.getStatus())) {
	                    old.setStatus("Suspended");
	                    old.setEditdate(new Date());
	                    old.setEditwho("SYSTEM_ABSENT_SUSPEND");
	                    toUpdate.add(old);

	                    Attendance absent = new Attendance();
	                    absent.setSno(sno++);
	                    absent.setEmployee_id(userId);
	                    absent.setCompany_id(companyId);
	                    absent.setStatus("Absent");
	                    absent.setDate(today);
	                    absent.setAdddate(new Date());
	                    absent.setAddwho("SYSTEM_ABSENT");
	                    absent.setEditdate(new Date());
	                    absent.setEditwho("SYSTEM_ABSENT");
	                    absent.setCheckInTime(LocalTime.MIDNIGHT);
	                    absent.setCheckOutTime(LocalTime.MIDNIGHT);
	                    absent.setUpdatedCheckInTime(LocalTime.MIDNIGHT);
	                    absent.setUpdatedcheckOutTime(LocalTime.MIDNIGHT);
	                    absent.setShift(officeTimeRange);
	                    absent.setHoursWorked("00:00:00");
	                    absent.setReason("SYSTEM MARKED ABSENT");
	                    absent.setLeave_request(false);
	                    absent.setAttendance_request(false);
	                    absent.setWfh_request(false);
	                    absent.setWithdrawnRequest(false);
	                    absent.setTeam_id(teamId);
	                    absent.setEmployee_name(empName);
	                    absent.setEmail("No Record Found");
	                    absent.setApproved_or_reject_or_pending("No Record Found");
	                    absent.setFile_name("No Record Found");
	                    absent.setFileDownloadUrl("No Record Found");
	                    absent.setFileViewUrl("No Record Found");
			    String request_id = generateRequestId();
			    absent.setRequest_id(request_id);
	                    // absent.setRequest_id(UUID.randomUUID().toString());
	                    absent.setRequestType("MARKED ABSENT");
	                    absent.setNumOfRequests(0);
	                    toInsert.add(absent);
	                }
	                continue;
	            }

	            // No record exists → insert absent
	            Attendance absent = new Attendance();
	            absent.setSno(sno++);
	            absent.setEmployee_id(userId);
	            absent.setCompany_id(companyId);
	            absent.setStatus("Absent");
	            absent.setDate(today);
	            absent.setAdddate(new Date());
	            absent.setAddwho("SYSTEM_ABSENT");
	            absent.setEditdate(new Date());
	            absent.setEditwho("SYSTEM_ABSENT");
	            absent.setCheckInTime(LocalTime.MIDNIGHT);
	            absent.setCheckOutTime(LocalTime.MIDNIGHT);
	            absent.setUpdatedCheckInTime(LocalTime.MIDNIGHT);
	            absent.setUpdatedcheckOutTime(LocalTime.MIDNIGHT);
	            absent.setShift(officeTimeRange);
	            absent.setHoursWorked("00:00:00");
	            absent.setReason("SYSTEM MARKED ABSENT");
	            absent.setLeave_request(false);
	            absent.setAttendance_request(false);
	            absent.setWfh_request(false);
	            absent.setWithdrawnRequest(false);
	            absent.setTeam_id(teamId);
	            absent.setEmployee_name(empName);
	            absent.setEmail("No Record Found");
	            absent.setApproved_or_reject_or_pending("No Record Found");
	            absent.setFile_name("No Record Found");
	            absent.setFileDownloadUrl("No Record Found");
	            absent.setFileViewUrl("No Record Found");
		    String request_id = generateRequestId();
		    absent.setRequest_id(request_id);
	            // absent.setRequest_id(UUID.randomUUID().toString());
	            absent.setRequestType("MARKED ABSENT");
	            absent.setNumOfRequests(0);
	            toInsert.add(absent);
	        }

	        // Step 5: Save updates and inserts
	        if (!toUpdate.isEmpty()) attendanceDao.saveAll(toUpdate);
	        if (!toInsert.isEmpty()) {
	            attendanceDao.saveAll(toInsert);
	            System.out.println("✅ Marked " + toInsert.size() + " employees absent for company " + companyId);
	        }

	    } catch (Exception e) {
	        System.err.println("❌ Error while marking absentees for " + companyId);
	        e.printStackTrace();
	    }
	}



	public ResponseEntity<String> handleCheckOut(String email) {
		try {
			Optional<User> userOpt = userdao.findByEmail(email);
			if (userOpt.isEmpty())
				return ResponseEntity.badRequest().body("❌ User not found.");
			User user = userOpt.get();

			Optional<Attendance> latestUnclosed = attendanceDao.findByEmployeeIdAndDate(user.getId(), LocalDate.now());

			if (latestUnclosed.isEmpty())
				return ResponseEntity.badRequest().body("❌ No active check-in found.");

			Attendance att = latestUnclosed.get();

			LocalTime checkIn = att.getCheckInTime();
			LocalTime now = LocalTime.now();

			if (checkIn != null) {
				Duration duration = Duration.between(checkIn, now);
				long hours = duration.toHours();
				long minutes = duration.toMinutes() % 60;
				String formatted = String.format("%02d:%02d", hours, minutes); // e.g., "03:45"
				att.setHoursWorked(formatted);
				att.setTotalBreak(0);
			}

			att.setCheckOutTime(now);
			attendanceDao.save(att);

			return ResponseEntity.ok("✅ Checked out successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Check-out failed.");
		}
	}

	public Attendance submitLeave(String reason, LocalDate leaveDate, String status, User user) throws Exception {
		String configkey = "OFFICETIMINGS";
		String getOfficeTimings = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id(), configkey);

		boolean flag = canApplyForUpdateOrLeave(user.getId(), leaveDate);

		if (flag) {
			Attendance attendance = new Attendance();
			long count = attendanceDao.findAttendanceCount();

//			 List<Attendance> oldOnes = attendanceDao.findActiveAttendance(user.getId(), LocalDate.now());
//             for (Attendance oldRec : oldOnes) {
//                 oldRec.setStatus("Suspended");
//                 oldRec.setEditdate(new Date());
//                 oldRec.setEditwho("User-SuspendOld");
//                 oldRec.setManager_id(user.getId());
////                 oldRec.setEditTime(LocalTime.now());
//                 attendanceDao.save(oldRec);
//             }
             
			if (count > 0) {
				long serialKey = attendanceDao.findLastSno();
				String request_id = generateRequestId();
				attendance.setSno(++serialKey);
				attendance.setRequest_id(request_id);
			} else {
				attendance.setSno(1);
			}

			attendance.setEmployee_id(user.getId());
			attendance.setDate(leaveDate);
			attendance.setStatus("Leave Update");
			attendance.setReason(reason);
			attendance.setLeave_request(true);
			attendance.setAdddate(new Date());
			attendance.setAddwho("LEAVE REQUEST");
			attendance.setEditdate(new Date());
			attendance.setEditwho("LEAVE REQUEST");
			attendance.setCompany_id(user.getCompany_id());
			attendance.setTeam_id(user.getTeam());
			attendance.setEmployee_name(user.getUsername());
			attendance.setEmail(user.getEmail());
			attendance.setApproved_or_reject_or_pending("Pending");
			attendance.setHoursWorked("00:00");
			attendance.setShift(getOfficeTimings);
//			attendance.setAddTime(LocalTime.now());
//			attendance.setEditTime(LocalTime.now());

			Attendance savedAttendance = attendanceDao.save(attendance);

			// ✉️ Prepare Email After Leave Submission
			// ✉️ Prepare Professional Email After Leave Submission
			String message = "<div style='background-color: #f5f7fb; padding: 40px 0; font-family: Arial, sans-serif;'>"
					+ "<table align='center' width='600' cellpadding='0' cellspacing='0' style='background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"

					// Top Bar (Optional light blue)
					+ "<tr><td style='background-color: #007bff; height: 8px;'></td></tr>"

					+ "<tr><td style='padding: 40px 30px;'>"

					// Heading
					+ "<h2 style='margin: 0; font-size: 26px; color: #333333; text-align: center;'>Leave Request Submitted ✅</h2>"

					// Greeting
					+ "<p style='font-size: 16px; color: #777777; margin-top: 20px;'>Hello <b>" + user.getUsername()
					+ "</b>,</p>"

					// Message
					+ "<p style='font-size: 16px; color: #555555;'>We have received your leave request for the following date:</p>"

					// Leave Request Details box
					+ "<div style='background: #f0f4f8; border-radius: 8px; padding: 20px; margin: 30px 0;'>"
					+ "<table width='100%' cellpadding='8' cellspacing='0' style='font-size: 16px; color: #555;'>"

					+ "<tr><td><b>Request ID:</b></td><td style='text-align:right;'>"
					+ (savedAttendance.getSno() != 0 ? savedAttendance.getSno() : "N/A") + "</td></tr>"
					+ "<tr><td><b>Leave Date:</b></td><td style='text-align:right;'>" + leaveDate + "</td></tr>"
					+ "<tr><td><b>Reason:</b></td><td style='text-align:right;'>"
					+ (reason != null ? reason : "Not Provided") + "</td></tr>"
					+ "<tr><td><b>Status:</b></td><td style='text-align:right; color: #ff9800; font-weight: bold;'>Pending Approval</td></tr>"

					+ "</table>" + "</div>"

					// Follow up message
					+ "<p style='font-size: 16px; color: #555555;'>Our HR/Admin team will review your request shortly and update you via email.</p>"

					// View Requests Button
					+ "<div style='margin: 30px 0; text-align: center;'>"
					+ "<a href='https://wwwemscom-production.up.railway.app/signin'"
					+ "style='background-color: #007bff; color: #ffffff; padding: 12px 25px; font-size: 16px; text-decoration: none; border-radius: 6px; display: inline-block;'>\"\n"
					+ "View Your Requests </a>" + "</div>"

					// Divider
					+ "<hr style='border: none; border-top: 1px solid #dddddd; margin: 40px 0;'>"

					// Footer
					+ "<p style='font-size: 13px; color: #999999; text-align: center;'>Thank you for helping us maintain accurate attendance records.<br>– Attendance Management System</p>"

					+ "</td></tr>" + "</table>" + "</div>";

			String subject = "Leave Request Submitted - " + user.getId();

			// ✅ Send Email Notification
			teamEmailService.sendEmail(message, subject, user.getEmail()).get();

			return savedAttendance;
		} else {
			throw new Exception("❌ You have already applied for a Leave Request on this date.");
		}
	}

	public Attendance updateAttendance(LocalDate date, LocalTime inTime, LocalTime outTime, String status, User user)
			throws Exception {
		String configkey = "OFFICETIMINGS";
		String getOfficeTimings = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id(), configkey);

		boolean flag = canApplyForUpdateOrLeave(user.getId(), date);

		if (flag) {
			Attendance attendance = new Attendance();
			long count = attendanceDao.findAttendanceCount();

//			  List<Attendance> oldOnes = attendanceDao.findActiveAttendance(user.getId(), LocalDate.now());
//              for (Attendance oldRec : oldOnes) {
//                  oldRec.setStatus("Suspended");
//                  oldRec.setEditdate(new Date());
//                  oldRec.setEditwho("User-SuspendOld");
//                  oldRec.setManager_id(user.getId());
////                  oldRec.setEditTime(LocalTime.now());
//                  attendanceDao.save(oldRec);
//              }

			if (count > 0) {
				long serialkey = attendanceDao.findLastSno();
				String request_id = generateRequestId();
				attendance.setSno(++serialkey);
				attendance.setRequest_id(request_id);
			} else {
				attendance.setSno(1);
			}
			System.out.println("UPDATED");
			attendance.setEmployee_id(user.getId());
			attendance.setUpdatedCheckInTime(inTime);
			attendance.setUpdatedcheckOutTime(outTime);
			attendance.setDate(date);
			attendance.setAttendance_request(true);
			attendance.setStatus("Attendance Update");
			attendance.setAdddate(new Date());
			attendance.setAddwho("UPDATE ATTENDANCE");
			attendance.setEditdate(new Date());
			attendance.setEditwho("UPDATE ATTENDANCE");
			attendance.setCompany_id(user.getCompany_id());
			attendance.setTeam_id(user.getTeam());
			attendance.setEmployee_name(user.getUsername());
			attendance.setEmail(user.getEmail());
			attendance.setApproved_or_reject_or_pending("Pending");
			attendance.setShift(getOfficeTimings);
//			attendance.setAddTime(LocalTime.now());
//			attendance.setEditTime(LocalTime.now());

			if (inTime != null && outTime != null) {
				Duration duration = Duration.between(inTime, outTime);
				long hours = duration.toHours();
				long minutes = duration.toMinutes() % 60;
				String formatted = String.format("%02d:%02d", hours, minutes);
				attendance.setHoursWorked(formatted);
				attendance.setTotalBreak(0);
			}
			Attendance savedAttendance = attendanceDao.save(attendance);
			// After attendanceDao.save(attendance);

			// 📩 Prepare Email Notification for Attendance Update
			// 📩 Prepare Professional Email Notification for Attendance Update
			String message = "<div style='background-color:#f5f7fb; padding: 40px 0; font-family: Arial, sans-serif;'>"
					+ "<table align='center' width='600' cellpadding='0' cellspacing='0' style='background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"

					// Top Green Line
					+ "<tr><td style='background-color: #007bff; height: 8px;'></td></tr>"

					// Main Content
					+ "<tr><td style='padding: 40px 30px;'>"

					// Heading
					+ "<h2 style='margin: 0; font-size: 26px; color: #333333; text-align: center;'>Attendance Update Request Received ✅</h2>"

					// Greeting
					+ "<p style='color: #777777; font-size: 16px; margin-top: 20px;'>Hello <b>" + user.getUsername()
					+ "</b>,</p>"

					// Thank you text
					+ "<p style='color: #555555; font-size: 16px; margin-top: 20px;'>Thank you for submitting your attendance update request. Here are the details:</p>"

					// Summary box
					+ "<div style='background: #f0f4f8; border-radius: 8px; padding: 20px; margin: 30px 0;'>"
					+ "<table width='100%' cellpadding='8' cellspacing='0' style='font-size: 16px; color: #555;'>"
					+ "<tr><td><b>Request ID:</b></td><td style='text-align:right;'>"
					+ (savedAttendance.getSno() != 0 ? savedAttendance.getSno() : "N/A") + "</td></tr>"
					+ "<tr><td><b>Date:</b></td><td style='text-align:right;'>" + date + "</td></tr>"
					+ "<tr><td><b>Updated Check-In:</b></td><td style='text-align:right;'>"
					+ (inTime != null ? inTime.toString() : "N/A") + "</td></tr>"
					+ "<tr><td><b>Updated Check-Out:</b></td><td style='text-align:right;'>"
					+ (outTime != null ? outTime.toString() : "N/A") + "</td></tr>"
					+ "<tr><td><b>Working Hours:</b></td><td style='text-align:right;'>"
					+ (inTime != null && outTime != null
							? String.format("%02d:%02d", Duration.between(inTime, outTime).toHours(),
									Duration.between(inTime, outTime).toMinutes() % 60)
							: "N/A")
					+ "</td></tr>"
					+ "<tr><td><b>Status:</b></td><td style='text-align:right; color: #ff9800; font-weight: bold;'>Pending Approval</td></tr>"
					+ "</table>" + "</div>"

					// Info Message
					+ "<p style='color: #555555; font-size: 16px;'>Our team will review your request shortly. You will be notified once a decision is made.</p>"

					// View Requests Button
					+ "<div style='margin: 30px 0; text-align: center;'>"
					+ "<a href='https://wwwemscom-production.up.railway.app/signin'"
					+ "style='background-color: #007bff; color: #ffffff; padding: 12px 25px; font-size: 16px; text-decoration: none; border-radius: 6px; display: inline-block;'>\"\n"
					+ "View Your Requests </a>" + "</div>"
					// Divider Line
					+ "<hr style='border: none; border-top: 1px solid #dddddd; margin: 40px 0;'>"

					// Footer
					+ "<p style='color: #999999; font-size: 14px; text-align: center;'>This is an automated message. Please do not reply directly.<br>"
					+ "For any queries, contact your HR/Admin team.<br><br>" + "– Attendance Management System</p>"

					+ "</td></tr>" + "</table>" + "</div>";

			String subject = "Attendance Update Request Submitted - " + user.getId();

			// ✅ Sending the email
			teamEmailService.sendEmail(message, subject, user.getEmail()).get();

			return savedAttendance;
		} else {
			throw new Exception("❌ You have already submitted an Attendance Update Request for this date.");
		}
	}

	public List<Attendance> findPendingRequests(User user) throws Exception {
	    List<Attendance> pendingRequests;

	    // Step 1: Fetch pending requests based on user's role
	    if ("ROLE_MANAGER".equalsIgnoreCase(user.getRole())) {
	        User manager = userdao.findByIdField(user.getId())
	                .orElseThrow(() -> new RuntimeException("Manager not found"));

	        String team = manager.getTeam();
	        System.out.println("🔍 Manager Team: " + (team != null ? team : "Not Assigned"));

	        pendingRequests = (team == null || team.trim().isEmpty())
	                ? convertToAttendanceList(attendanceDao.findRequestsForNoTeamManager())
	                : convertToAttendanceList(attendanceDao.findPendingRequestsByTeam(team));
	    } else {
	        // Employee role
	        pendingRequests = convertToAttendanceList(attendanceDao.findEmployeeAttendanceWithoutTeam());
	    }

	    // Step 2: Get folder ID and NOTIFY flag from DB config
	    List<Object[]> result = nSqlConfigDao.findNsqlValueAndIsNotifybyConfigKeyAndCompanyId(
	            user.getCompany_id().trim(), "REQUESTGOOGLEDRIVEFOLDER");

	    String folderId = null;
	    String configurable = null;
	    boolean isNotify = false;

	    if (result != null && !result.isEmpty()) {
	        Object[] row = result.get(0);
	        if (row.length >= 1) {
	            folderId = row[0] != null ? row[0].toString() : null;
	        }
	        if (row.length >= 2) {
	            configurable = row[1] != null ? row[1].toString() : null;
	            isNotify = "NOTIFY".equalsIgnoreCase(configurable);
	        }
	    }

	    System.out.printf("📁 FOLDER ID: %s | CONFIGURABLE: %s | COMPANY ID: %s%n",
	            folderId, configurable, user.getCompany_id());

	    // Step 3: Attach Drive URLs to each attendance request
	    for (Attendance request : pendingRequests) {
	        String fileName = request.getFile_name();
	        String fileId = request.getFileId();

	        try {
	            Map<String, String> links = null;

	            // 3-a: Prefer direct fileId access
	            if (fileId != null && !fileId.trim().isEmpty()) {
	                System.out.println("🔍 Getting Drive links by file ID: " + fileId);
	                links = getDriveLinks(fileId);
	                grantAccessIfNotShared(fileId, user.getEmail(), isNotify);

	            // 3-b: Fallback to file name search inside folder
	            } else if (folderId != null && !folderId.trim().isEmpty()
	                    && fileName != null && !fileName.trim().isEmpty()
	                    && !"no record found".equalsIgnoreCase(fileName.trim())) {

	                System.out.printf("🔍 Searching Drive by name: [%s] in folder [%s], REQUEST_ID: %s%n",
	                        fileName, folderId, request.getRequest_id());

	                String foundFileId = findFileIdFromFolder(folderId, fileName);

	                if (foundFileId != null) {
	                    request.setFileId(foundFileId); // optional caching
	                    links = getDriveLinks(foundFileId);
	                    grantAccessIfNotShared(foundFileId, user.getEmail(), isNotify);
	                }
	            }

	            // 3-c: Set file links or fallback values
	            request.setFileViewUrl(links != null ? links.getOrDefault("view", "File not found") : "File not found");
	            request.setFileDownloadUrl(links != null ? links.getOrDefault("download", "File not found") : "File not found");

	        } catch (IOException | GeneralSecurityException e) {
	            System.err.println("⚠️ Error accessing Drive: " + e.getMessage());
	            request.setFileViewUrl("Error accessing Drive");
	            request.setFileDownloadUrl("Error accessing Drive");
	        }
	    }

	    return pendingRequests;
	}

	
	public Map<String, String> getDriveLinks(String fileId) throws Exception {
	    Drive driveService = googleDriveService.getDriveService();

	    File file = driveService.files().get(fileId)
	            .setFields("id, webViewLink, webContentLink")
	            .setSupportsAllDrives(true)
	            .execute();

	    Map<String, String> links = new HashMap<>();
	    links.put("view", file.getWebViewLink());
	    links.put("download", file.getWebContentLink());
	    return links;
	}

	
	public void grantAccessIfNotShared(String fileId, String userEmail, boolean notifyUser)
	        throws Exception {

	    Drive driveService = googleDriveService.getDriveService();

	    // Step 1: Check current permissions
	    PermissionList permissions = driveService.permissions().list(fileId)
	            .setFields("permissions(emailAddress, role)")
	            .execute();

	    boolean alreadyShared = permissions.getPermissions().stream()
	            .anyMatch(p -> userEmail.equalsIgnoreCase(p.getEmailAddress()));

	    // Step 2: Grant reader access if not shared
	    if (!alreadyShared) {
	        Permission permission = new Permission()
	                .setType("user")
	                .setRole("reader")
	                .setEmailAddress(userEmail);

	        driveService.permissions()
	                .create(fileId, permission)
	                .setSendNotificationEmail(notifyUser)
	                .execute();

	        System.out.println("✅ Access granted to: " + userEmail);
	    } else {
	        System.out.println("🔁 Already shared with: " + userEmail);
	    }
	}


	
 // Helper method to convert java.sql.Date to java.time.LocalDate
    private LocalDate convertSqlDateToLocalDate(Object sqlDateObj) {
        if (sqlDateObj instanceof java.sql.Date) {
            return ((java.sql.Date) sqlDateObj).toLocalDate();  // Convert to LocalDate
        }
        return null;  // Return null if not a valid SQL Date
    }

    // Method to convert query results to Attendance objects
    private List<Attendance> convertToAttendanceList(List<Object[]> queryResults) {
        List<Attendance> attendanceList = new ArrayList<>();

        for (Object[] row : queryResults) {
            Attendance attendance = new Attendance();

            // Set the request_id, employee_name, email, status, reason, etc.
            attendance.setRequest_id((String) row[0]);  // request_id
            attendance.setEmployee_name((String) row[1]);  // employee_name
            attendance.setEmail((String) row[2]);  // email
            attendance.setStatus((String) row[3]);  // status
            attendance.setReason((String) row[4]);  // reason

            // Handling COUNT(*) result which could be an Integer or BigInteger
            Object totalDaysObj = row[5];
            if (totalDaysObj instanceof BigInteger) {
                attendance.setTotalDays(((BigInteger) totalDaysObj).longValue());  // BigInteger -> long
            } else if (totalDaysObj instanceof Integer) {
                attendance.setTotalDays(((Integer) totalDaysObj).longValue());  // Integer -> long
            } else {
                attendance.setTotalDays(0L);  // Default to 0 if it's neither BigInteger nor Integer
            }

            // Convert from java.sql.Date to java.time.LocalDate for 'from_date' and 'to_date'
            attendance.setFromDate(convertSqlDateToLocalDate(row[6]));  // from_date
            attendance.setToDate(convertSqlDateToLocalDate(row[7]));    // to_date

            if (row[8] != null) {
                attendance.setAdddate(new java.util.Date(((java.sql.Timestamp) row[8]).getTime()));
            }
            if (row[9] != null) {
                attendance.setEditdate(new java.util.Date(((java.sql.Timestamp) row[9]).getTime()));
            }

            // Convert employee_id safely from Integer or String as needed
            Object employeeIdObj = row[10];
            if (employeeIdObj instanceof String) {
                try {
                    attendance.setEmployee_id(Integer.parseInt((String) employeeIdObj));  // String to Integer
                } catch (NumberFormatException e) {
                    attendance.setEmployee_id(0);  // Default value in case of error
                }
            } else if (employeeIdObj instanceof Integer) {
                attendance.setEmployee_id((Integer) employeeIdObj);  // Integer -> Integer
            } else {
                attendance.setEmployee_id(0);  // Default value if it's neither String nor Integer
            }

            attendance.setApproved_or_reject_or_pending((String) row[11]);  // ArrovedOr Reject
            attendance.setRequestType((String) row[12]);  // requesttype
            attendance.setFile_name((String) row[13]);  // filename
            attendance.setFileId((String) row[14]);  // filename

            attendanceList.add(attendance);
        }

        return attendanceList;
    }


  
//    // Helper method to convert java.sql.Timestamp to java.util.Date
//    private Date convertSqlTimestampToDate(Object timestampObj) {
//        if (timestampObj instanceof java.sql.Timestamp) {
//            return new Date(((java.sql.Timestamp) timestampObj).getTime());  // Convert to java.util.Date
//        } else if (timestampObj instanceof java.sql.Date) {
//            return new Date(((java.sql.Date) timestampObj).getTime());  // If it's java.sql.Date, convert to java.util.Date
//        }
//        return null;  // Return null if not a valid Timestamp or Date
//    }

    public boolean canApplyForUpdateOrLeave(int employeeId, LocalDate date) {
		Optional<Attendance> optional = attendanceDao.findPendingUpdateOrLeaveRequest(employeeId, date);

		if (optional.isPresent()) {
			String lastStatus = optional.get().getStatus();
			if ("Leave Rejected".equalsIgnoreCase(lastStatus) || "Attendance Rejected".equalsIgnoreCase(lastStatus)) {
				return true; // Rejected previously -> allow new request
			} else {
				return false; // Already applied and still pending
			}
		} else {
			return true; // No previous request -> allow
		}
	}

    @Transactional
    public void processApprovalOrRejection(String request_id, boolean approved, User manager)
            throws InterruptedException, ExecutionException {
        try {
            // 1️⃣ Fetch all requests with the same request_id
            List<Attendance> requests = attendanceDao.findByRequestId(request_id);
            
            // If no request found, throw exception
            if (requests.isEmpty()) {
                throw new RuntimeException("❌ Request not found for ID: " + request_id);
            }

            String decisionStatus = null; // "approved ✅" or "rejected ❌"
            String requestLabel = null; // "Leave Request", "WFH Request", etc.
            String statusColor = null; // green/red
            String statusText = null; // "Approved" / "Rejected"
            
            boolean isLeave = false;
            boolean isWFH = false;
            boolean isUpdate = false;
            boolean isWithdrawn = false;
            
            String email = null;
            String employee_name = null;
            Integer employee_id = 0;
            
            LocalDate StartDate = null;
            LocalDate EndDate = null;
             
            
            // Loop through each attendance record
            for (Attendance req : requests) {
                 isLeave = req.isLeave_request();
                 isWFH = req.isWfh_request();
                 isUpdate = req.isAttendance_request();
                 isWithdrawn = req.isWithdrawnRequest();
                 email = req.getEmail();
                 employee_id = req.getEmployee_id();
                 employee_name = req.getEmployee_name();
                 StartDate = req.getFromDate();
                 EndDate = req.getToDate();
                 

                // Only handle one of those four types
                if (!(isLeave || isWFH || isUpdate || isWithdrawn)) {
                    throw new IllegalStateException("⚠️ Request ID " + request_id + " is not actionable");
                }

//                String decisionStatus; // "approved ✅" or "rejected ❌"
//                String requestLabel; // "Leave Request", "WFH Request", etc.
//                String statusColor; // green/red
//                String statusText; // "Approved" / "Rejected"

                if (approved) {
                    // 2️⃣ On approval, suspend any old “active” records for the same date range
                    List<Attendance> oldOnes = attendanceDao.findActiveAttendance(req.getEmployee_id(), req.getDate());
                    for (Attendance oldRec : oldOnes) {
                        oldRec.setStatus("Suspended");
                        oldRec.setEditdate(new Date());
                        oldRec.setEditwho("MANAGER APPROVAL -SUSPEND OLD ATTENDACE");
                        oldRec.setManager_id(manager.getId());
//                        oldRec.setEditTime(LocalTime.now());
                        attendanceDao.save(oldRec);
                    }

                    // 3️⃣ Update this record to “Approved”
                    if (isLeave) {
                        req.setStatus("Leave");
                    } else if (isWFH) {
                        req.setStatus("WFH");
                    } else if (isWithdrawn) {
                        req.setStatus("Withdrawn");
                    } else {
                        // Attendance update
                        req.setStatus("Present");
                        req.setCheckInTime(req.getUpdatedCheckInTime());
                        req.setCheckOutTime(req.getUpdatedcheckOutTime());
                    }
                    req.setApproved_or_reject_or_pending("Approved");
//                    req.setEditTime(LocalTime.now());

                    requestLabel = isLeave ? "Leave Request"
                            : isWFH ? "WFH Request" : isWithdrawn ? "Withdrawal Request" : "Attendance Update";
                    decisionStatus = "approved ✅";
                    statusColor = "#28a745";
                    statusText = "Approved";

                } else {
                    // ❌ On rejection, no suspension of old — just mark this one rejected
                    req.setStatus(isLeave ? "Leave Rejected"
                            : isWFH ? "WFH Rejected" : isWithdrawn ? "Withdrawal Rejected" : "Attendance Rejected");
                    req.setApproved_or_reject_or_pending("Rejected");

                    requestLabel = isLeave ? "Leave Request"
                            : isWFH ? "WFH Request" : isWithdrawn ? "Withdrawal Request" : "Attendance Update";
                    decisionStatus = "rejected ❌";
                    statusColor = "#dc3545";
                    statusText = "Rejected";
                }

                // 4️⃣ Finalize metadata & save
                req.setManager_id(manager.getId());
                req.setEditdate(new Date());
                req.setEditwho("MANAGER APPROVAL");
                attendanceDao.save(req);
            }
            
            // 5️⃣ Build and send the HTML email
            String heading = approved ? "✅ Request Approved" : "❌ Request Rejected";
            String subject = requestLabel + " " + decisionStatus + " - ID " + request_id;

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>")
                    .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>").append("<title>")
                    .append(heading).append("</title></head>")
                    .append("<body style='background:#f5f7fb;font-family:Arial,sans-serif;'>")
                    // card wrapper
                    .append("<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center'>")
                    .append("<table width='600' style='background:#fff;border-radius:10px;overflow:hidden;'>")
                    // top bar color
                    .append("<tr><td style='height:6px;background:").append(statusColor).append("'></td></tr>")

                    // content
                    .append("<tr><td style='padding:30px;'>").append("<h2 style='text-align:center;color:#333;'>")
                    .append(heading).append("</h2>").append("<p>Hello <strong>").append(employee_name)
                    .append("</strong>,</p>").append("<p>Your <strong>").append(requestLabel).append("</strong> ")
                    .append("for <strong>").append(StartDate)
                    .append(isLeave || isWFH ? " to " + EndDate : "").append("</strong> has been ")
                    .append("<strong style='color:").append(statusColor).append(";'>").append(statusText)
                    .append("</strong>.</p>")
                    // details block
                    .append("<div style='background:#f0f4f8;padding:15px;border-radius:6px;'>")
                    .append("<p><strong>Request ID:</strong> ").append(request_id).append("</p>")
                    .append("<p><strong>Status:</strong> <span style='color:").append(statusColor).append(";'>")
                    .append(statusText).append("</span></p>").append("</div>")
                    .append("<p style='margin-top:20px;color:#666;'>Questions? Contact your manager or HR.</p>")
                    .append("</td></tr>")
                    // footer
                    .append("<tr><td style='padding:10px;text-align:center;font-size:12px;color:#aaa;'>")
                    .append("Attendance Management System").append("</td></tr>").append("</table></td></tr></table>")
                    .append("</body></html>");

            teamEmailService.sendEmail(html.toString(), subject, email).get();

            // 6️⃣ Persist a notification record
            Notification note = new Notification();
            note.setMessage_id(generateMessageId());
            note.setUserId(employee_id);
            note.setMessage(requestLabel + " ID " + request_id + " has been " + (approved ? "approved" : "rejected")
                    + " by Manager #" + manager.getId());
            note.setTimestamp(new Date());
            note.setAdddate(new Date());
            note.setAddwho("SYSTEM");
            note.setSenderEmail("noreply@ems.com");
            note.setReceiverEmail(email);
            notificationDao.save(note);
            NotificationListener.send(email, note);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<Attendance> getRequestHistory(User user) throws Exception {
        // Fetch all processed requests
        List<Attendance> allProcessedRequests = convertToAttendanceList(attendanceDao.findRequestHistory(user.getId()));

        // Get folder ID and configurable flag from the DB config
        String getFolderId1 = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id().trim(), "REQUESTGOOGLEDRIVEFOLDER");
        System.out.println("FOLDER ID " + getFolderId1 + " ,COMPANY ID " + user.getCompany_id());

        // Step 2: Get folder ID and NOTIFY flag from DB config
        List<Object[]> result = nSqlConfigDao.findNsqlValueAndIsNotifybyConfigKeyAndCompanyId(
                user.getCompany_id().trim(), "REQUESTGOOGLEDRIVEFOLDER");

        String folderId = null;
        String configurable = null;
        boolean isNotify = false;

        if (result != null && !result.isEmpty()) {
            Object[] row = result.get(0);
            if (row.length >= 1) {
                folderId = row[0] != null ? row[0].toString() : null;
            }
            if (row.length >= 2) {
                configurable = row[1] != null ? row[1].toString() : null;
                isNotify = "NOTIFY".equalsIgnoreCase(configurable);
            }
        }

        System.out.printf("📁 FOLDER ID: %s | CONFIGURABLE: %s | COMPANY ID: %s%n",
                folderId, configurable, user.getCompany_id());

        // Step 3: Attach Drive URLs to each processed request
        for (Attendance request : allProcessedRequests) {
            String fileName = request.getFile_name();
            String fileId = request.getFileId();

            try {
                Map<String, String> links = null;

                // 3-a: Prefer direct fileId access
                if (fileId != null && !fileId.trim().isEmpty()) {
                    System.out.println("🔍 Getting Drive links by file ID: " + fileId);
                    links = getDriveLinks(fileId);
                    grantAccessIfNotShared(fileId, user.getEmail(), isNotify);

                // 3-b: Fallback to file name search inside folder
                } else if (folderId != null && !folderId.trim().isEmpty()
                        && fileName != null && !fileName.trim().isEmpty()
                        && !"no record found".equalsIgnoreCase(fileName.trim())) {

                    System.out.printf("🔍 Searching Drive by name: [%s] in folder [%s], REQUEST_ID: %s%n",
                            fileName, folderId, request.getRequest_id());

                    String foundFileId = findFileIdFromFolder(folderId, fileName);

                    if (foundFileId != null) {
                        request.setFileId(foundFileId); // optional caching
                        links = getDriveLinks(foundFileId);
                        grantAccessIfNotShared(foundFileId, user.getEmail(), isNotify);
                    }
                }

                // 3-c: Set file links or fallback values
                request.setFileViewUrl(links != null ? links.getOrDefault("view", "File not found") : "File not found");
                request.setFileDownloadUrl(links != null ? links.getOrDefault("download", "File not found") : "File not found");

            } catch (IOException | GeneralSecurityException e) {
                System.err.println("⚠️ Error accessing Drive: " + e.getMessage());
                request.setFileViewUrl("Error accessing Drive");
                request.setFileDownloadUrl("Error accessing Drive");
            }
        }

        return allProcessedRequests;
    }

    public String getDownloadLinkIfExists(String folderId, String fileName, String loggedInUserEmail)
            throws Exception {

        Drive driveService = googleDriveService.getDriveService();

        String query = "'" + folderId + "' in parents";
        FileList fileList = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name)")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .execute();

        List<File> files = fileList.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("❌ No files found in folder.");
            return null;
        }

        String inputName = fileName.trim().replaceAll("\\s+", " ").toLowerCase();

        for (File file : files) {
            String driveName = file.getName().trim().replaceAll("\\s+", " ").toLowerCase();

            // Match on exact or partial name
            if (driveName.equals(inputName) || 
                (inputName.length() >= 5 && driveName.contains(inputName.substring(0, 5)))) {
                
                System.out.println("✅ Found match: " + file.getName());

                // --- Check existing permissions properly ---
                PermissionList permissions = driveService.permissions()
                        .list(file.getId())
                        .setFields("permissions(id,emailAddress,role,type)") // ✅ Ensure emailAddress included
                        .execute();

                boolean isAlreadyShared = false;
                for (Permission permission : permissions.getPermissions()) {
                    if (permission.getEmailAddress() != null &&
                        permission.getEmailAddress().equalsIgnoreCase(loggedInUserEmail)) {
                        isAlreadyShared = true;
                        break;
                    }
                }

                // Grant permission only if not already shared
                if (!isAlreadyShared) {
                    try {
                        Permission permission = new Permission();
                        permission.setType("user");
                        permission.setRole("reader");
                        permission.setEmailAddress(loggedInUserEmail);

                        driveService.permissions().create(file.getId(), permission)
                                .setSendNotificationEmail(true) // ⚠️ change to false if you don’t want emails
                                .execute();

                        System.out.println("✅ Permission granted to: " + loggedInUserEmail);
                    } catch (IOException e) {
                        System.err.println("⚠️ Error while granting permission: " + e.getMessage());
                    }
                } else {
                    System.out.println("ℹ️ Already shared with " + loggedInUserEmail);
                }

                // ✅ Return a Drive link
                return "https://drive.google.com/file/d/" + file.getId() + "/view";
            }
        }

        System.out.println("❌ No matching file name found in folder.");
        return null;
    }
    
    public byte[] downloadImageByUserId(Integer userId) throws Exception {
        // Fetch user from DB
        User user = userdao.findByIdField(userId).orElseThrow(() -> new RuntimeException("User not found"));

        String folderId = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id(), "PROFILEIMAGEGOOGLEDRIVEFOLDER");
        String imageName = user.getImage_Name();

        // 🔍 Find file ID from folder by image name
        String fileId = findFileIdFromFolder(folderId, imageName); // Write this helper

        // 🧲 Download the file content using Drive API
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        googleDriveService.getDriveService().files().get(fileId).executeMediaAndDownloadTo(outputStream);
//       System.out.println("FILE ID IMG "+fileId+" OUTPUTSTREAM "+outputStream);
        return outputStream.toByteArray();
    }
    
    public String findFileIdFromFolder(String folderId, String fileName) throws Exception {
        Drive driveService = googleDriveService.getDriveService(); // Ensure this returns an authenticated Drive client

        String query = "'" + folderId + "' in parents and trashed = false";
        FileList fileList = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name)")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .execute();

        List<File> files = fileList.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("❌ No files found in the folder.");
            return null;
        }

        String inputName = fileName.trim().replaceAll("\\s+", " ").toLowerCase();

        for (File file : files) {
            String driveName = file.getName().trim().replaceAll("\\s+", " ").toLowerCase();
            System.out.println("🔍 Comparing: " + inputName + " <==> " + driveName);

            if (driveName.equals(inputName)) {
                System.out.println("✅ Exact match found: " + file.getName());
                return file.getId();
            }

            if (inputName.length() >= 5 && driveName.contains(inputName.substring(0, 5))) {
                System.out.println("✅ Partial match found: " + file.getName());
                return file.getId();
            }
        }

        System.out.println("❌ No matching file name found.");
        return null;
    }

    
    public User loadProfile(Integer userId, Model model) {
        Optional<User> userOptional = userdao.findByIdField(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found");
        }

        User user = userOptional.get();

        try {
            String resumeFolderId = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id(), "RESUMEGOOGLEDRIVEFOLDER");
            String imageFolderId = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id(), "PROFILEIMAGEGOOGLEDRIVEFOLDER");

            if (resumeFolderId != null && user.getResume_file_name() != null && !user.getResume_file_name().equalsIgnoreCase("NA")) {
                String resumeUrl = getDownloadLinkIfExists(resumeFolderId, user.getResume_file_name(), user.getEmail());
                user.setResume_file_url(resumeUrl);
            }

            if (imageFolderId != null && user.getImage_Name() != null && !user.getImage_Name().equalsIgnoreCase("default.jpg")) {
                String imageUrl = getDownloadLinkIfExists(imageFolderId, user.getImage_Name(), user.getEmail());
                user.setImage_File_Url(imageUrl);
                System.out.println("IMAGE URL "+imageUrl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }


    
    public String getFileUrlByFileId(String fileId) throws Exception {
        Drive driveService = googleDriveService.getDriveService();

        File file = driveService.files().get(fileId)
                .setFields("id, name, webViewLink")
                .setSupportsAllDrives(true)
                .execute();

        System.out.println("GOOGLE DRIVE LINK "+file);
        return file.getWebViewLink(); // returns a preview/download link
    }

    
	public Map<String, Integer> getLeaveSummary(int employeeId, String requestType) {
		int year = LocalDate.now().getYear();

		Map<String, Integer> summary = new HashMap<>();
		String type = requestType.trim().toLowerCase();

		if (type.equals("leave")) {
			int assigned = leavePolicyDao.getAssignedLeaveQuota(employeeId, year);
			int consumed = attendanceDao.countConsumedLeaves(employeeId, year);
			int pending = attendanceDao.countPendingLeaves(employeeId);
			// subtract both consumed and pending
			int remaining = Math.max(0, assigned - (consumed + pending));

			summary.put("assignedLeaves", assigned);
			summary.put("consumedLeaves", consumed);
			summary.put("pendingLeaves", pending);
			summary.put("remainingLeaves", remaining);
		} else if (type.equals("wfh")) {
			int assigned = leavePolicyDao.getAssignedWfhQuota(employeeId, year);
			int consumed = attendanceDao.countConsumedWfh(employeeId, year);
			int pending = attendanceDao.countPendingWfh(employeeId);
			// subtract both consumed and pending
			int remaining = Math.max(0, assigned - (consumed + pending));

			System.out.println("ASSIGNED " + assigned + " ,CONSUMED " + consumed + " ,PENDING " + pending
					+ " ,REMAINING " + remaining);

			summary.put("assignedWfh", assigned);
			summary.put("consumedWfh", consumed);
			summary.put("pendingWfh", pending);
			summary.put("remainingWfh", remaining);
		}

		return summary;
	}

//	// Add this method in your service class
//	public boolean isHolidayOrWeekend(LocalDate date, String companyId) {
//		DayOfWeek day = date.getDayOfWeek();
//		if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
//			return true;
//		}
//
//		List<String> typesToSkip = Arrays.asList("Festival", "Public Holiday", "Restricted Holiday");
//		return holidayDao.existsByCompanyIdAndDateAndTypeIn(companyId, date, typesToSkip);
//	}

	@Transactional
	public String processWfhRequest(String dateRange, String reason, String requestType, MultipartFile file, User user) throws Exception {
	    int year = LocalDate.now().getYear();

	    if (dateRange == null || dateRange.trim().isEmpty()) {
	        throw new IllegalArgumentException("❌ Please provide a valid date range.");
	    }

	    String[] dates = dateRange.split(" to ");
	    LocalDate startDate, endDate;

	    if (dates.length == 2) {
	        startDate = LocalDate.parse(dates[0].trim());
	        endDate = LocalDate.parse(dates[1].trim());
	    } else if (dates.length == 1 && !dates[0].trim().isEmpty()) {
	        startDate = endDate = LocalDate.parse(dates[0].trim());
	    } else {
	        throw new IllegalArgumentException("❌ The date range format is incorrect. Please use 'start date to end date'.");
	    }

	    if (endDate.isBefore(startDate)) {
	        throw new IllegalArgumentException("❌ The end date cannot be earlier than the start date.");
	    }

	    // Weekend and holiday list
	    List<String> weekendDays = holidayDao.getCompanyWeekendDays(user.getCompany_id());
	    List<Holiday> holidays = holidayDao.getCompanyHolidays(user.getCompany_id(), startDate, endDate);

	    // Conflict check for existing pending requests
	    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
	        if (attendanceDao.existsAnyPendingRequestOnDate(user.getId(), date) > 0) {
	            throw new IllegalArgumentException("⚠️ A pending request already exists for " + date + ". Please resolve it before submitting a new request.");
	        }
	    }

	    long nextSno = attendanceDao.findAttendanceCount() > 0 ? attendanceDao.findLastSno() + 1 : 1;
	    String requestId = generateRequestId();

	    // Calculate working days
	    int workingDays = calculateWorkingDays(startDate, endDate, weekendDays, holidays);

	    // Validate WFH quota
	    int assigned = leavePolicyDao.getAssignedLeaveQuota(user.getId(), year);
	    int consumed = attendanceDao.countConsumedLeaves(user.getId(), year);
	    int pending = attendanceDao.countPendingLeaves(user.getId());
	    int remaining = Math.max(0, assigned - (consumed + pending));

	    String nsql_value = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id(), "OVERWFHALLOWED");

	    // Check if WFH limit exceeds remaining days
	    if (("0").equals(nsql_value) && remaining < workingDays) {
	        throw new IllegalArgumentException("⚠️ Your Work From Home request exceeds the remaining available days. Please adjust your request.");
	    }

	    int validDays = 0;
	    int numberOfRequests = workingDays;
	    
	    // File upload logic (if file is provided)
	    String fileId = null;
	    if (file != null && !file.isEmpty()) {
	        fileId = uploadFileToGoogleDrive(file, user,"REQUESTGOOGLEDRIVEFOLDER");  // Upload the file to Google Drive
	    }

	    // Create the attendance records for each valid day
	    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
	        String dayOfWeekUpperCase = capitalize(date.getDayOfWeek().name());
	        System.out.println("FOR LOOP ENTER" +weekendDays+" -> "+dayOfWeekUpperCase);
	        if (weekendDays.contains(dayOfWeekUpperCase)) {
	            continue;
	        }

	        System.out.println("SET");
	        // Create the leave (attendance) record
	        Attendance wfh = new Attendance();
	        wfh.setSno(nextSno++);
	        wfh.setRequest_id(requestId);
	        wfh.setEmployee_id(user.getId());
	        wfh.setStatus("WFH Request");
	        wfh.setWfh_request(true);
	        wfh.setLeave_request(false);
	        wfh.setAttendance_request(false);
	        wfh.setFromDate(date);
	        wfh.setToDate(date);
	        wfh.setApproved_or_reject_or_pending("Pending");
	        wfh.setCompany_id(user.getCompany_id());
	        wfh.setTeam_id(user.getTeam());
	        wfh.setEmployee_name(user.getUsername());
	        wfh.setEmail(user.getEmail());
	        wfh.setReason(reason);
	        wfh.setAdddate(new Date());
	        wfh.setDate(date);
	        wfh.setAddwho("USER");
	        wfh.setEditdate(new Date());
	        wfh.setEditwho("USER");
	        wfh.setTotalDays(1);
	        wfh.setFile_name((file.getOriginalFilename() != null && !file.getOriginalFilename().trim().isEmpty()) ? file.getOriginalFilename() : "No Record Found");
	        wfh.setNumOfRequests(numberOfRequests);
	        wfh.setRequestType(requestType);  // Setting request type (Leave, WFH, etc.)

	        // Save the file ID if it was uploaded
	        if (fileId != null) {
	            wfh.setFileId(fileId);  // Save the Google Drive file ID in the WFH record
	        }
	        else
	        {
	        	 wfh.setFileId("No Record Found"); 
	        }

	        attendanceDao.save(wfh);
	        validDays++;
	    }

	    if (validDays == 0) {
	        throw new IllegalArgumentException("⚠️ All selected dates fall on holidays or weekends. Please choose valid working days.");
	    }

	    return "✅ Your Work From Home request for " + validDays + " working day(s) has been successfully submitted.\n📌 Request ID: " + requestId;
	}

	@Transactional
	public String processLeaveRequest(String dateRange, String reason, String requestType, MultipartFile file, User user) throws Exception  {
	    int year = LocalDate.now().getYear();

	    if (dateRange == null || dateRange.trim().isEmpty()) {
	        throw new IllegalArgumentException("❌ Please provide a valid date range.");
	    }

	    String[] dates = dateRange.split(" to ");
	    LocalDate startDate, endDate;

	    if (dates.length == 2) {
	        startDate = LocalDate.parse(dates[0].trim());
	        endDate = LocalDate.parse(dates[1].trim());
	    } else if (dates.length == 1 && !dates[0].trim().isEmpty()) {
	        startDate = endDate = LocalDate.parse(dates[0].trim());
	    } else {
	        throw new IllegalArgumentException("❌ The date range format is incorrect. Please use 'start date to end date'.");
	    }

	    if (endDate.isBefore(startDate)) {
	        throw new IllegalArgumentException("❌ The end date cannot be earlier than the start date.");
	    }

	    List<String> weekendDays = holidayDao.getCompanyWeekendDays(user.getCompany_id());
	    List<Holiday> holidays = holidayDao.getCompanyHolidays(user.getCompany_id(), startDate, endDate);

	    // Conflict check
	    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
	        if (attendanceDao.existsAnyPendingRequestOnDate(user.getId(), date) > 0) {
	            throw new IllegalArgumentException("⚠️ A pending request already exists for " + date + ". Please resolve it before submitting a new request.");
	        }
	    }

	    long nextSno = attendanceDao.findAttendanceCount() > 0 ? attendanceDao.findLastSno() + 1 : 1;
	    String requestId = generateRequestId();

	    int workingDays = calculateWorkingDays(startDate, endDate, weekendDays, holidays);

	    int assigned = leavePolicyDao.getAssignedLeaveQuota(user.getId(), year);
	    int consumed = attendanceDao.countConsumedLeaves(user.getId(), year);
	    int pending = attendanceDao.countPendingLeaves(user.getId());
	    int remaining = Math.max(0, assigned - (consumed + pending));

	    String nsql_value = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id(), "OVERLEAVEALLOWED");

	    // Check if the remaining days are less than the applied working days and if exceeding the limit is not allowed
	    if (("0").equals(nsql_value) && remaining < workingDays) {
	        throw new IllegalArgumentException("⚠️ You cannot apply for more leave days than your remaining balance. Please adjust your request.");
	    }

	    int validDays = 0;
	    int numberOfRequests = workingDays;

	    // If a file is uploaded, upload it to Google Drive
	    String fileId = null;
	    if (file != null && !file.isEmpty()) {
	        fileId = uploadFileToGoogleDrive(file, user, "REQUESTGOOGLEDRIVEFOLDER");  // Upload the file to Google Drive
	    }

	    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
	        String dayOfWeekUpperCase = capitalize(date.getDayOfWeek().name());
	        if (weekendDays.contains(dayOfWeekUpperCase)) {
	            continue;
	        }

	        // Create the attendance record
	        Attendance leave = new Attendance();
	        leave.setSno(nextSno++);
	        leave.setRequest_id(requestId);
	        leave.setEmployee_id(user.getId());
	        leave.setStatus("Leave Request");
	        leave.setLeave_request(true);
	        leave.setAttendance_request(false);
	        leave.setWfh_request(false);
	        leave.setFromDate(date);
	        leave.setToDate(date);
	        leave.setApproved_or_reject_or_pending("Pending");
	        leave.setCompany_id(user.getCompany_id());
	        leave.setTeam_id(user.getTeam());
	        leave.setEmployee_name(user.getUsername());
	        leave.setEmail(user.getEmail());
	        leave.setReason(reason);
	        leave.setAdddate(new Date());
	        leave.setDate(date);
	        leave.setAddwho("USER");
	        leave.setEditdate(new Date());
	        leave.setEditwho("USER");
	        leave.setTotalDays(1);
	        leave.setFile_name((file.getOriginalFilename() != null && !file.getOriginalFilename().trim().isEmpty()) ? file.getOriginalFilename() : "No Record Found");
	        leave.setNumOfRequests(numberOfRequests);
	        leave.setRequestType(requestType);

	        // Save the file ID if it was uploaded
	        if (fileId != null) {
	            leave.setFileId(fileId);  // Save the Google Drive file ID in the leave record
	        }
	        else
	        {
	        	 leave.setFileId("No Record Found"); 
	        }
	        attendanceDao.save(leave);
	        validDays++;
	    }

	    if (validDays == 0) {
	        throw new IllegalArgumentException("⚠️ All selected dates fall on holidays or weekends. Please choose valid working days.");
	    }

	    return "✅ Your leave request for " + validDays + " working day(s) has been successfully submitted.\n📌 Request ID: " + requestId;
	}

	// Upload file to Google Drive using config-based folder. 
	public String uploadFileToGoogleDrive(MultipartFile file, User user, String configKey)
	        throws Exception {

	    System.out.println("FILE " + file.isEmpty() + " ,FILE ORIGINAL NAME " + file.getOriginalFilename());

	    // ✅ Validate uploaded file
	    if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
	        throw new IOException("❌ Uploaded file is empty or invalid.");
	    }

	    // ✅ Get Drive config (main folder ID)
	    String folderId = nSqlConfigDao.findbyConfigKeyAndCompanyId(user.getCompany_id(), configKey);

	    // ✅ Additional config to check NOTIFY flag
	    List<Object[]> folderInfoList = nSqlConfigDao.findNsqlValueAndIsNotifybyConfigKeyAndCompanyId(
	            user.getCompany_id(), configKey);

	    String notifyFlag = null;
	    if (folderInfoList != null && !folderInfoList.isEmpty()) {
	        Object[] folderInfo = folderInfoList.get(0);  // LIMIT 1 expected
	        folderId = folderInfo[0] != null ? folderInfo[0].toString() : folderId; // override if found
	        notifyFlag = folderInfo[1] != null ? folderInfo[1].toString() : null;

	        System.out.println("📂 Folder ID from config: " + folderId);
	        System.out.println("🔔 Notify flag: " + notifyFlag);
	    } else {
	        System.out.println("⚠️ No folder info found for companyId: " + user.getCompany_id());
	    }

	    if (folderId == null || folderId.trim().isEmpty()) {
	        throw new IllegalArgumentException("❌ Google Drive folder ID not found in configuration.");
	    }

	    System.out.println("📁 Using Google Drive folder ID: " + folderId);
	    boolean isNotify = "NOTIFY".equalsIgnoreCase(notifyFlag);

	    // ✅ Save file temporarily to disk
	    String tempFilePath = saveFileToTempLocation(file);

	    // ✅ Upload to Google Drive
	    String fileId = googleDriveService.uploadFile(tempFilePath, file.getOriginalFilename(), folderId);

	    // ✅ Check if already shared
	    Drive driveService = googleDriveService.getDriveService();
	    PermissionList permissions = driveService.permissions().list(fileId).execute();
	    boolean isAlreadyShared = false;

	    for (Permission permission : permissions.getPermissions()) {
	        if (permission.getEmailAddress() != null && permission.getEmailAddress().equals(user.getEmail())) {
	            isAlreadyShared = true;
	            break;
	        }
	    }

	    // ✅ Only if NOTIFY flag is true → share + send email
	    if (!isAlreadyShared && isNotify) {
	        Permission permission = new Permission();
	        permission.setType("user");
	        permission.setRole("reader");
	        permission.setEmailAddress(user.getEmail());

	        driveService.permissions().create(fileId, permission)
	                .setSendNotificationEmail(true) // ✅ send email only when NOTIFY
	                .execute();

	        System.out.println("✅ Permission granted to: " + user.getEmail() + " (NOTIFY enabled)");
	    } else if (!isAlreadyShared) {
	        // ✅ Permission without notification
	        Permission permission = new Permission();
	        permission.setType("user");
	        permission.setRole("reader");
	        permission.setEmailAddress(user.getEmail());

	        driveService.permissions().create(fileId, permission)
	                .setSendNotificationEmail(false) // ❌ don't send email
	                .execute();

	        System.out.println("✅ Permission granted silently to: " + user.getEmail() + " (NOTIFY disabled)");
	    }

	    return fileId;
	}

	
	private String saveFileToTempLocation(MultipartFile file) throws IOException {
	    // ✅ Define temp file path
	    String tempDir = System.getProperty("java.io.tmpdir");
	    java.io.File tempFile = new java.io.File(tempDir, file.getOriginalFilename());

	    // ✅ Write using safe InputStream/OutputStream (fixes Tomcat write issues)
	    try (InputStream in = file.getInputStream();
	         OutputStream out = new FileOutputStream(tempFile)) {

	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = in.read(buffer)) != -1) {
	            out.write(buffer, 0, bytesRead);
	        }
	    }

	    System.out.println("✅ Temp file saved at: " + tempFile.getAbsolutePath());
	    return tempFile.getAbsolutePath();
	}


	public LocalDate getDefaultDateFromConfig(String configkey) {
		Optional<NSqlConfig> configValue = nSqlConfigDao.findByConfigkey(configkey);

		if (configValue.isPresent()) {
			String rawDate = configValue.get().getNSqlValue().trim();

			try {
				// Recommended format: yyyy-MM-dd (e.g., 1800-01-01)
				return LocalDate.parse(rawDate);
			} catch (DateTimeParseException e) {
				System.out.println("⚠️ Invalid date format in config: " + rawDate);
			}
		} else {
			System.out.println("⚠️ Config key 'DATEDEFAULTVALUE' not found.");
		}

		// 🔁 Fallback default value if config is missing or incorrect
		return LocalDate.of(1800, 1, 1);
	}

//	public List<Attendance> getRequestHistory(String email) throws Exception {
//		Optional<User> user = userdao.findByEmail(email);
//		if (user.isEmpty())
//			throw new Exception("User not found");
//		return attendanceDao.findByEmployeeIdOrderByDateDesc(user.get().getId());
//	}

	public void withdrawRequest(String request_id, int sno, String email) throws Exception {
		Optional<User> user = userdao.findByEmail(email);
		if (user.isEmpty())
			throw new Exception("User not found");

		Optional<Attendance> attendanceOpt = attendanceDao.findByRequestIdAndSno(request_id, sno);
		if (attendanceOpt.isEmpty())
			throw new Exception("Request not found");

		Attendance attendance = attendanceOpt.get();
		if (!attendance.getEmployee_id().equals(user.get().getId())) {
			throw new Exception("Unauthorized to withdraw this request");
		}

		if (!"Pending".equalsIgnoreCase(attendance.getApproved_or_reject_or_pending())) {
			throw new Exception("Only pending requests can be withdrawn");
		}

		attendanceDao.deleteByRequestId(request_id, sno);
	}

	public Attendance getRequestSummaryByRequestId(String requestId, User user) throws Exception {
	    // 1. Fetch attendance records
	    List<Attendance> requests = attendanceDao.findByRequestIdAndEmployeeId(requestId, user.getId());

	    if (requests.isEmpty()) {
	        throw new RuntimeException("No attendance records found for request " + requestId);
	    }

	    // 2. Calculate date span
	    LocalDate startDate = requests.stream()
	            .map(Attendance::getFromDate)
	            .min(LocalDate::compareTo)
	            .orElseThrow(() -> new RuntimeException("Unable to find earliest start date"));

	    LocalDate endDate = requests.stream()
	            .map(Attendance::getFromDate)
	            .max(LocalDate::compareTo)
	            .orElseThrow(() -> new RuntimeException("Unable to find latest end date"));

	    // 3. Prepare summary object
	    Attendance summary = requests.get(0); // Use the first request as base for the summary
	    summary.setFromDate(startDate);
	    summary.setToDate(endDate);
	    summary.setTotalDays(requests.size());

	    // 4. File Handling - Fetch folder info from configuration
	    List<Object[]> folderInfoList = nSqlConfigDao.findNsqlValueAndIsNotifybyConfigKeyAndCompanyId(
	            user.getCompany_id(), "REQUESTGOOGLEDRIVEFOLDER");

	    if (folderInfoList != null && !folderInfoList.isEmpty()) {
	        Object[] folderInfo = folderInfoList.get(0);  // Only take the first element, since we have LIMIT 1 in the query

	        String folderId = folderInfo[0] != null ? folderInfo[0].toString() : null;
	        String configurable = folderInfo[1] != null ? folderInfo[1].toString() : null;

	        boolean isNotify = "NOTIFY".equalsIgnoreCase(configurable);

	        // Log the values for debugging
	        System.out.printf("📁 FOLDER ID: %s | CONFIGURABLE: %s | COMPANY ID: %s%n",
	                folderId, configurable, user.getCompany_id());

	        // File ID and file name for the current attendance request
	        String fileId = summary.getFileId();
	        String fileName = summary.getFile_name();

	        try {
	            Map<String, String> links = null;

	            // 4-a: Try fileId directly
	            if (fileId != null && !fileId.isBlank()) {
	                links = getDriveLinks(fileId);
	                grantAccessIfNotShared(fileId, user.getEmail(), isNotify);

	            // 4-b: Fallback to search by fileName inside folder
	            } else if (folderId != null && !folderId.isBlank()
	                    && fileName != null && !fileName.isBlank()
	                    && !"no record found".equalsIgnoreCase(fileName.trim())) {

	                String foundId = findFileIdFromFolder(folderId, fileName);

	                if (foundId != null) {
	                    summary.setFileId(foundId);  // Cache new ID
	                    links = getDriveLinks(foundId);
	                    grantAccessIfNotShared(foundId, user.getEmail(), isNotify);
	                }
	            }

	            // 4-c: Populate URLs
	            if (links != null) {
	                summary.setFileViewUrl(links.getOrDefault("view", "File not found"));
	                summary.setFileDownloadUrl(links.getOrDefault("download", "File not found"));
	            } else {
	                summary.setFileViewUrl("File not found");
	                summary.setFileDownloadUrl("File not found");
	            }

	        } catch (IOException | GeneralSecurityException ex) {
	            System.err.println("⚠️ Error accessing Drive: " + ex.getMessage());
	            summary.setFileViewUrl("Error accessing Drive");
	            summary.setFileDownloadUrl("Error accessing Drive");
	        }
	    } else {
	        // Handle the case where folderInfo is not returned correctly
	        throw new RuntimeException("Invalid configuration data for Google Drive folder.");
	    }

	    return summary;
	}




	@Transactional
	public void withdrawAttendanceRequest(String request_id, String email) throws Exception {
		Optional<User> userOpt = userdao.findByEmail(email);
		if (userOpt.isEmpty())
			throw new Exception("❌ User not found");

		User user = userOpt.get();

		List<Attendance> requests = attendanceDao.findByRequestIdAndEmployeeId(request_id, user.getId());

		if (requests.isEmpty()) {
			throw new Exception("❌ Request not found or unauthorized access.");
		}

		// Ensure at least one entry is pending
		boolean hasPending = requests.stream()
				.anyMatch(req -> "Pending".equalsIgnoreCase(req.getApproved_or_reject_or_pending()));

		if (!hasPending) {
			throw new Exception("⚠️ Only pending requests can be withdrawn.");
		}

		// Mark all as withdrawn
		for (Attendance req : requests) {
			req.setStatus("Withdrawn");
			req.setApproved_or_reject_or_pending("Withdrawn");
			req.setEditdate(new Date());
			req.setWithdrawnRequest(true);
			req.setEditwho("USER-WITHDRAW");
//			req.setEditTime(LocalTime.now());
			attendanceDao.save(req);
		}
	}

	@Transactional
	public String generateRequestId() {
		// Step 1: Fetch the last request ID from the database
		String selectQuery = "SELECT last_request_id FROM request_id_tracker";
		String lastRequestId = jdbcTemplate.queryForObject(selectQuery, String.class);

		// Step 2: Extract the numeric part from the current last_request_id
		int numericPart = Integer.parseInt(lastRequestId.replaceAll("[^0-9]", ""));

		// Step 3: Increment the numeric part by 1
		numericPart++;

		// Step 4: Format the new request ID in the desired format "RQID1001"
		String newRequestId = "RQID" + String.format("%04d", numericPart); // Format to always have 4 digits

		// Step 5: Update the last_request_id in the database with the new request ID
		String updateQuery = "UPDATE request_id_tracker SET last_request_id = ?";
		jdbcTemplate.update(updateQuery, newRequestId);

		// Step 6: Return the new request ID
		return newRequestId;
	}

	public List<Attendance> getGroupedRequestsByEmployee(int employeeId) {
	    try {
	        int year = LocalDate.now().getYear();
	        List<Attendance> allRequests = attendanceDao.findByEmployeeId(employeeId, year);
	        System.out.println("LIST OF ALL REQUESTS : " + allRequests);

	        // ✅ Remove null request_id before sorting/grouping
	        List<Attendance> filteredRequests = allRequests.stream()
	            .filter(a -> a.getRequest_id() != null)
	            .collect(Collectors.toList());

	        // Sort by request_id descending
	        filteredRequests.sort(Comparator.comparing(Attendance::getRequest_id).reversed());

	        // Group by request_id
	        Map<String, List<Attendance>> groupedByRequestId = filteredRequests.stream()
	                .collect(Collectors.groupingBy(Attendance::getRequest_id));

	        List<Attendance> summarizedRequests = new ArrayList<>();

	        // Summarize each group
	        for (Map.Entry<String, List<Attendance>> entry : groupedByRequestId.entrySet()) {
	            List<Attendance> group = entry.getValue();

	            LocalDate minDate = group.stream().map(Attendance::getFromDate).filter(Objects::nonNull).min(LocalDate::compareTo).orElse(null);
	            LocalDate maxDate = group.stream().map(Attendance::getToDate).filter(Objects::nonNull).max(LocalDate::compareTo).orElse(null);

	            Attendance summary = group.get(0);
	            summary.setFromDate(minDate);
	            summary.setToDate(maxDate);
	            summary.setTotalDays(group.size());

	            summarizedRequests.add(summary);
	        }

	        // Final sort
	        summarizedRequests.sort(Comparator.comparing(Attendance::getRequest_id).reversed());

	        return summarizedRequests;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}




	public int calculateWorkingDays(LocalDate startDate, LocalDate endDate, List<String> weekendDays, List<Holiday> holidays) {
	    int totalWorkingDays = 0;
	    LocalDate currentDate = startDate;
	    System.out.println("CURRENT DATE "+currentDate);
	    System.out.println("END DATE DATE "+endDate);

	    // Loop through each day in the date range
	    while (!currentDate.isAfter(endDate)) {
	        // Extract the day of the week (e.g., "SATURDAY", "SUNDAY")
	        String dayOfWeek = currentDate.getDayOfWeek().name(); // Returns "SATURDAY", "SUNDAY", etc.

	        // Convert both weekendDays list and dayOfWeek to title case for comparison
	        String dayOfWeekTitleCase = capitalize(dayOfWeek); // Capitalize first letter to match DB format
            System.out.println("WEEKEND DAYS "+!weekendDays.contains(dayOfWeekTitleCase));
            System.out.println("HOLIDAY "+!isHoliday(currentDate, holidays));
	        // Check if the current day is not a weekend or holiday
	        if (!weekendDays.contains(dayOfWeekTitleCase) && !isHoliday(currentDate, holidays)) {
	            totalWorkingDays++;
	        }

	        // Move to the next day
	        currentDate = currentDate.plusDays(1);
	    }

	    return totalWorkingDays;
	}

	// Helper method to capitalize the first letter of the day name
	private String capitalize(String input) {
	    if (input == null || input.isEmpty()) {
	        return input;
	    }
	    return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
	}

	private boolean isHoliday(LocalDate date, List<Holiday> holidays) {
	    for (Holiday holiday : holidays) {
	        if (holiday.getDate().equals(date)) {
	            return true;
	        }
	    }
	    return false;
	}

	 // Fetch holidays for the company within the specified date range
    public List<Holiday> getCompanyHolidays(String companyId, LocalDate startDate, LocalDate endDate) {
        return holidayDao.getCompanyHolidays(companyId, startDate, endDate);
    }

    // Fetch weekend days for the company
    public List<String> getCompanyWeekendDays(String companyId) {
        return holidayDao.getCompanyWeekendDays(companyId);
    }

    public void generateSalarySlipsForCurrentMonth(String companyId) throws IOException {
        String currentMonth = getCurrentMonth();  // e.g. "June"
        String currentYear = getCurrentYear();    // e.g. "2025"

        LocalDate today = LocalDate.now();
        int todayDay = today.getDayOfMonth();
        int currentMonthMaxDay = today.lengthOfMonth(); // dynamic based on month/leap year

        List<Object[]> salarySlipConfig = nSqlConfigDao.findNsqlValueAndIsSalaryDatebyConfigKeyAndCompanyId(companyId, "SALARYSLIPGENERATEDATE");

        for (Object[] result : salarySlipConfig) {
            if ("1".equals(result[0])) {
                String configuredDateString = (String) result[1]; // e.g., "2025-06-07"

                try {
                    LocalDate configuredDate = LocalDate.parse(configuredDateString);
                    int configuredDayOfMonth = configuredDate.getDayOfMonth();

                    System.out.println("configuredDayOfMonth: " + configuredDayOfMonth);
                    System.out.println("todayDay: " + todayDay);
                    System.out.println("currentMonthMaxDay: " + currentMonthMaxDay);

                    if (configuredDayOfMonth == todayDay && configuredDayOfMonth <= currentMonthMaxDay) {
                        List<UserDetail> employees = fetchEmployeeDetails(companyId);
                        Optional<CompanyInfo> companyInfoOpt = company_dao.findByCompanyIdOptional(companyId);
                        System.out.println("USERDETAIL LIST "+employees.size());
                        System.out.println("COMPANY INFO LIST "+companyInfoOpt.isPresent());
                        if (!companyInfoOpt.isPresent()) {
                            System.out.println("⚠️ Company info not found for ID: " + companyId);
                            return;
                        }

                        CompanyInfo companyInfo = companyInfoOpt.get();

                        Iterator<UserDetail> iterator = employees.iterator();
                        while (iterator.hasNext()) {
                            UserDetail employee = iterator.next();
                            String subject = "Salary Slip for " + currentMonth + " " + currentYear;
                            String message = "Dear " + employee.getUsername() + ",<br><br>Please find your salary slip attached.";

                            ByteArrayOutputStream pdfStream = generateSalarySlipPdf(employee, companyInfo);
                            Path tempFile = Files.createTempFile("SalarySlip_" + employee.getId(), ".pdf");
                            Files.write(tempFile, pdfStream.toByteArray());

                            try {
                                boolean emailSent = salarySlipEmailService.sendEmail(tempFile.toString(), message, subject, employee.getEmail()).get();

                                if (emailSent) {
                                    iterator.remove();
                                    System.out.println("✅ Email sent successfully to " + employee.getEmail());

                                    SalarySlip salarySlip = new SalarySlip();
                                    salarySlip.setBasicSalary(employee.getBasicSalary());
                                    salarySlip.setBonus(employee.getBonus());
                                    salarySlip.setDeductions(employee.getDeductions());
                                    salarySlip.setDepartment(employee.getDepartment());
                                    salarySlip.setDesignation(employee.getDesignation());
                                    salarySlip.setEmployeeId(employee.getId());
                                    salarySlip.setEmployeeName(employee.getUsername());
                                    salarySlip.setArrear(employee.getArrear());
                                    salarySlip.setBasicArrear(employee.getArrear());
                                    salarySlip.setBonusArrear(employee.getBonusArrear());
                                    salarySlip.setHraArrear(employee.getHraArrear());
                                    salarySlip.setLtaArrear(employee.getLtaArrear());
                                    salarySlip.setRetentionArrear(employee.getRetentionArrear());
                                    salarySlip.setSpecialArrear(employee.getSpecialArrear());
                                    salarySlip.setTransportArrear(employee.getTransportArrear());
                                    salarySlip.setFileDownloadUrl("");
                                    salarySlip.setFileViewUrl("");
                                    salarySlip.setGrossSalary(employee.getGrossSalary());
                                    salarySlip.setNetSalary(employee.getNetSalary());
                                    salarySlip.setSalarySlipDate(today.toString());
                                    salarySlip.setAdddate(new Date());
                                    salarySlip.setAddwho("SYSTEM");
                                    salarySlip.setEditdate(new Date());
                                    salarySlip.setEditwho("SYSTEM");
                                    salarySlip.setPaymentDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
                                    salarySlip.setSalarySlipMonth(today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
                                    salarySlip.setSalarySlipYear(String.valueOf(today.getYear()));
                                    salarySlip.setEmailSent(emailSent);
                                    salarySlipDao.save(salarySlip);
                                } else {
                                    System.out.println("❌ Failed to send email to " + employee.getEmail());
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                Files.deleteIfExists(tempFile);
                            }
                        }
                    } else {
                        System.out.println("⏭️ Skipping: Today is " + todayDay + ", but configured salary day is " + configuredDayOfMonth);
                    }

                } catch (DateTimeParseException e) {
                    System.err.println("⚠️ Invalid configured date format in DB for company " + companyId + ": " + configuredDateString);
                }
            } else {
                System.out.println("🚫 Salary slip generation disabled for company " + companyId);
            }
        }
    }


    public ByteArrayOutputStream generateSalarySlipPdf(UserDetail detail, CompanyInfo companyInfo) throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc, PageSize.A4);
            doc.setMargins(20, 20, 20, 20);

            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            doc.add(new Paragraph(companyInfo.getCompany_name()).setFont(bold).setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(companyInfo.getCompany_address()).setFont(regular).setFontSize(10).setTextAlignment(TextAlignment.CENTER));

            String salaryMonth = detail.getSalarySlipMonth();
            String salaryYear = detail.getSalarySlipYear();
            if (salaryMonth == null || salaryYear == null) {
                LocalDate today = LocalDate.now();
                salaryMonth = today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                salaryYear = String.valueOf(today.getYear());
            }
            String monthYear = salaryMonth + " " + salaryYear;

            doc.add(new Paragraph("Pay Slip for the month of " + monthYear).setFont(bold).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginTop(10));

            int daysInMonth = 30;
            try {
                YearMonth ym = YearMonth.of(Integer.parseInt(salaryYear), MonthStringToNumber(salaryMonth));
                daysInMonth = ym.lengthOfMonth();
            } catch (Exception e) {}

            int payableDays = detail.getPayableDays() != null ? detail.getPayableDays() : daysInMonth;

            float[] colWidths = {1, 1};
            Table infoTable = new Table(colWidths).setWidth(UnitValue.createPercentValue(100));

            infoTable.addCell(infoCell("Emp. Code: " + detail.getId(), regular));
            infoTable.addCell(infoCell("Work Location: " + safeString(detail.getBase_location()), regular));
            infoTable.addCell(infoCell("Name: " + safeString(detail.getUsername()), regular));
            infoTable.addCell(infoCell("Division: " + safeString(detail.getDivision()), regular));
            infoTable.addCell(infoCell("Department: " + safeString(detail.getDepartment()), regular));
            infoTable.addCell(infoCell("PAN: " + safeString(detail.getPanNumber()), regular));
            infoTable.addCell(infoCell("Designation: " + safeString(detail.getDesignation()), regular));
            infoTable.addCell(infoCell("Grade: " + safeString(detail.getGrade()), regular));
            infoTable.addCell(infoCell("MOP: Bank Transfer", regular));
            infoTable.addCell(infoCell("DOB: " + safeString(detail.getDob()), regular));
            infoTable.addCell(infoCell("DOJ: " + safeString(detail.getDateOfJoining()), regular));
            infoTable.addCell(infoCell("UAN No.: " + safeString(detail.getUanNumber()), regular));
            infoTable.addCell(infoCell("PF Number: " + safeString(detail.getPfNumber()), regular));
            infoTable.addCell(infoCell("Bank Account No: " + safeString(detail.getBank_account_number()), regular));
            infoTable.addCell(infoCell("Payable Days: " + payableDays, regular));

            doc.add(infoTable);
            doc.add(new Paragraph("\n"));

            BigDecimal basic = getOrZero(detail.getBasicSalary());
            BigDecimal hra = getOrZero(detail.getHra());
            BigDecimal bonus = getOrZero(detail.getStatutoryBonus());
            BigDecimal lta = getOrZero(detail.getLeaveTravelAllowance());
            BigDecimal retention = getOrZero(detail.getRetentionBonus());
            BigDecimal special = getOrZero(detail.getSpecialAllowance());
            BigDecimal transport = getOrZero(detail.getTransportAllowance());

            BigDecimal pf = getOrZero(detail.getPfEmployee());
            BigDecimal tds = getOrZero(detail.getTds());

            BigDecimal basicArrear = getOrZero(detail.getBasicArrear());
            BigDecimal hraArrear = getOrZero(detail.getHraArrear());
            BigDecimal bonusArrear = getOrZero(detail.getBonusArrear());
            BigDecimal ltaArrear = getOrZero(detail.getLtaArrear());
            BigDecimal retentionArrear = getOrZero(detail.getRetentionArrear());
            BigDecimal specialArrear = getOrZero(detail.getSpecialArrear());
            BigDecimal transportArrear = getOrZero(detail.getTransportArrear());

            BigDecimal gross = basic.add(basicArrear).add(hra).add(hraArrear).add(bonus).add(bonusArrear)
                .add(lta).add(ltaArrear).add(retention).add(retentionArrear).add(special).add(specialArrear)
                .add(transport).add(transportArrear);

            BigDecimal totalDeductions = pf.add(tds);
            BigDecimal net = gross.subtract(totalDeductions);

            float[] salaryColWidths = {2, 1, 1, 1, 1, 2, 1};
            Table salaryTable = new Table(salaryColWidths).setWidth(UnitValue.createPercentValue(100));

            salaryTable.addHeaderCell(new Cell(1, 5).add(new Paragraph("Earnings").setFont(bold)).setBackgroundColor(new DeviceRgb(211, 211, 211)));
            salaryTable.addHeaderCell(new Cell(1, 2).add(new Paragraph("Deductions").setFont(bold)).setBackgroundColor(new DeviceRgb(211, 211, 211)));

            salaryTable.addCell(subHeader("Description", bold));
            salaryTable.addCell(subHeader("Rate", bold));
            salaryTable.addCell(subHeader("Monthly", bold));
            salaryTable.addCell(subHeader("Arrear", bold));
            salaryTable.addCell(subHeader("Total", bold));
            salaryTable.addCell(subHeader("Description", bold));
            salaryTable.addCell(subHeader("Amount", bold));

            addSalaryRow(salaryTable, "Basic", format(basic), format(basic), format(basicArrear), format(basic.add(basicArrear)), "PF", format(pf));
            addSalaryRow(salaryTable, "HRA", format(hra), format(hra), format(hraArrear), format(hra.add(hraArrear)), "TDS", format(tds));
            addSalaryRow(salaryTable, "Bonus", format(bonus), format(bonus), format(bonusArrear), format(bonus.add(bonusArrear)), "", "");
            addSalaryRow(salaryTable, "LTA", format(lta), format(lta), format(ltaArrear), format(lta.add(ltaArrear)), "", "");
            addSalaryRow(salaryTable, "Retention", format(retention), format(retention), format(retentionArrear), format(retention.add(retentionArrear)), "", "");
            addSalaryRow(salaryTable, "Special Allowance", format(special), format(special), format(specialArrear), format(special.add(specialArrear)), "", "");
            addSalaryRow(salaryTable, "Transport", format(transport), format(transport), format(transportArrear), format(transport.add(transportArrear)), "", "");

            doc.add(salaryTable);

            doc.add(new Paragraph("Net Salary: Rs. " + format(net) + " (in words: " + NumberToWordsConverter.convert(net) + " only)")
                    .setFont(bold).setFontSize(11).setMarginTop(10));

            String qrText = String.format("Employee Name: %s\nEmployee ID: %s\nDepartment: %s\nDesignation: %s\nBasic Salary: Rs. %s\nHRA: Rs. %s\nBonus: Rs. %s\nGross Salary: Rs. %s\nDeductions: Rs. %s\nNet Salary: Rs. %s\nPayment Date: %s\nSalary Month/Year: %s",
                    safeString(detail.getUsername()),
                    safeString(detail.getId()),
                    safeString(detail.getDepartment()),
                    safeString(detail.getDesignation()),
                    format(detail.getBasicSalary()),
                    format(detail.getHra()),
                    format(detail.getBonus()),
                    format(detail.getGrossSalary()),
                    format(detail.getDeductions()),
                    format(detail.getNetSalary()),
                    detail.getPaymentDate() != null ? detail.getPaymentDate().toString() : "N/A",
                    monthYear);

            BarcodeQRCode qrCode = new BarcodeQRCode(qrText);
            Image qrImg = new Image(qrCode.createFormXObject(pdfDoc)).scaleToFit(100, 100);
            doc.add(qrImg.setHorizontalAlignment(HorizontalAlignment.CENTER));

            generateTaxWorksheetSection(pdfDoc, doc, detail);

            doc.add(new Paragraph("This is a computer generated payslip and does not require any signature.")
                    .setFont(regular).setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginTop(20));

            doc.close();
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void generateTaxWorksheetSection(PdfDocument pdfDoc, Document doc, UserDetail detail) {
        doc.add(new Paragraph("Income Tax Worksheet for the Period April 2025 - March 2026")
                .setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(11).setMarginTop(10));

        float[] columnWidths = {200F, 180F, 200F};
        Table taxTable = new Table(UnitValue.createPointArray(columnWidths)).setWidth(UnitValue.createPercentValue(100));

        taxTable.addHeaderCell(getHeaderCell("Salary u/s 17(1)"));
        taxTable.addHeaderCell(getHeaderCell("Other Exemptions"));
        taxTable.addHeaderCell(getHeaderCell("Tax Calculation"));

        taxTable.addCell(getCell("Basic\nHRA\nLeave Travel Allowance\nRetention Bonus\nSpecial Allowance\nStatutory Bonus\nTransport Allowance\n\nTotal", TextAlignment.LEFT));
        taxTable.addCell(getCell("Deductions under Chapter VI-A\n80C\n\nDeductions u/s 16\nStandard Deduction u/s 16 (ia)\n\nTotal", TextAlignment.LEFT));
        taxTable.addCell(getCell("Total Taxable Income\nTax on Total Income\nTax Rebate u/s 87A\nNet Tax Payable\nSurcharge\nCess\nTax Deducted\nRemaining Tax\nTax Deduction This Month", TextAlignment.LEFT));

        taxTable.addCell(getCell("220423\n110211\n18370\n99999\n126262\n15000\n19200\n\n609465", TextAlignment.RIGHT));
        taxTable.addCell(getCell(" \n \n\n \n75000\n\n75000", TextAlignment.RIGHT));
        taxTable.addCell(getCell("534470\n6724\n6724\n0\n0\n0\n0\n0\n0", TextAlignment.RIGHT));

        doc.add(taxTable);

        doc.add(new Paragraph("\nHRA Exemption Calculation").setBold().setFontSize(10).setMarginTop(10));
        Table hraTable = new Table(new float[]{300, 150}).setWidth(UnitValue.createPercentValue(60));
        hraTable.addCell(getHeaderCell("Component"));
        hraTable.addCell(getHeaderCell("Amount"));
        hraTable.addCell("1. HRA Received");
        hraTable.addCell("110211");
        hraTable.addCell("2. 40% or 50% of Basic");
        hraTable.addCell("");
        hraTable.addCell("3. Rent > 10% Basic");
        hraTable.addCell("");
        hraTable.addCell("Exempt HRA");
        hraTable.addCell("Not Applicable");
        hraTable.addCell("Taxable HRA");
        hraTable.addCell("110211");
        doc.add(hraTable);

        doc.add(new Paragraph("\nLeave Balances as on 01-Jun-2025").setBold().setFontSize(10).setMarginTop(10));
        Table leaveTable = new Table(new float[]{300, 100}).setWidth(UnitValue.createPercentValue(50));
        leaveTable.addCell(getHeaderCell("Leave Type"));
        leaveTable.addCell(getHeaderCell("Balance"));
        leaveTable.addCell("Employee Engagement Leave (EEL)");
        leaveTable.addCell("8.00");
        doc.add(leaveTable);
    }

    private Cell getHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell getCell(String text, TextAlignment alignment) {
        return new Cell().add(new Paragraph(text)).setTextAlignment(alignment);
    }
    // Helper method to avoid NullPointerException
    private BigDecimal getOrZero(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }

    // Simple cell with no border for info table
    private Cell infoCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(10))
                .setBorder(null);
    }

    // Subheader cell for salary table
    private Cell subHeader(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(10))
                .setBackgroundColor(new DeviceRgb(230, 230, 230));
    }

    // Add one salary row for earnings and deductions
    private void addSalaryRow(Table table,
                              String earnDesc, String earnRate, String earnMonthly, String earnArrear, String earnTotal,
                              String dedDesc, String dedAmount) {
        table.addCell(new Cell().add(new Paragraph(earnDesc)));
        table.addCell(new Cell().add(new Paragraph(earnRate)).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(earnMonthly)).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(earnArrear)).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(earnTotal)).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(dedDesc)));
        table.addCell(new Cell().add(new Paragraph(dedAmount)).setTextAlignment(TextAlignment.RIGHT));
    }

    public String format(BigDecimal val) {
        if (val == null) {
            return "0.00";
        }
        return val.setScale(2, RoundingMode.HALF_UP).toString();
    }


    // Convert month name to month number for YearMonth parsing
    private int MonthStringToNumber(String monthName) {
        try {
            return java.time.Month.valueOf(monthName.toUpperCase(Locale.ENGLISH)).getValue();
        } catch (Exception e) {
            return 1; // fallback January
        }
    }

    // Safe String to avoid null
    private String safeString(Object obj) {
        return obj == null ? "" : obj.toString();
    }
    
    // Get current month name
    private String getCurrentMonth() {
        Month currentMonth = LocalDate.now().getMonth();
        return currentMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    // Get current year as string
    private String getCurrentYear() {
        return String.valueOf(LocalDate.now().getYear());
    }

    
    // Helper to fetch employee details by company ID
    private List<UserDetail> fetchEmployeeDetails(String companyId) {
        return userDetailDao.findActiveEmployeesWithoutSalarySlipForCurrentMonth(companyId);
    }
    
    public void processSalaryExcel(MultipartFile file, User user1) {
        String filename = file.getOriginalFilename();

        try (InputStream is = file.getInputStream();
             Workbook workbook = getWorkbook(is, filename)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            String[] expectedHeaders = {
                "emp_id", "name",
                "basic_salary", "hra", "bonus", "pf_employee", "tds",
                "payable_days", "deductions", "gross_salary", "net_salary", "payment_date",
                "date_of_joining", "pan_number", "uan_number", "division", "grade",
                "pf_number", "leave_travel_allowance", "retention_bonus",
                "special_allowance", "statutory_bonus", "transport_allowance", "Arrear",
                "basic_arrear", "hra_arrear", "bonus_arrear", "lta_arrear",
                "retention_arrear", "special_arrear", "transport_arrear"
            };

            Row headerRow = null;

            while (rowIterator.hasNext()) {
                Row potentialHeader = rowIterator.next();
                if (isHeaderRow(potentialHeader, expectedHeaders)) {
                    headerRow = potentialHeader;
                    break;
                }
            }

            if (headerRow == null) {
                throw new RuntimeException("❌ Valid header row not found. Please ensure the file contains the correct header.");
            }

            System.out.println("✅ Header row detected at row number: " + headerRow.getRowNum());

            Set<Integer> processedEmpIds = new HashSet<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                try {
                    Integer empId = getCellValueAsInt(row.getCell(0));
                    String name = getCellValueAsString(row.getCell(1));
                    BigDecimal basic = getCellValueAsDecimal(row.getCell(2));
                    BigDecimal hra = getCellValueAsDecimal(row.getCell(3));
                    BigDecimal bonus = getCellValueAsDecimal(row.getCell(4));
                    BigDecimal pf = getCellValueAsDecimal(row.getCell(5));
                    BigDecimal tds = getCellValueAsDecimal(row.getCell(6));
                    Integer payableDays = getCellValueAsInt(row.getCell(7));

                    BigDecimal basicArrear = getCellValueAsDecimal(row.getCell(24));
                    BigDecimal hraArrear = getCellValueAsDecimal(row.getCell(25));
                    BigDecimal bonusArrear = getCellValueAsDecimal(row.getCell(26));
                    BigDecimal ltaArrear = getCellValueAsDecimal(row.getCell(27));
                    BigDecimal retentionArrear = getCellValueAsDecimal(row.getCell(28));
                    BigDecimal specialArrear = getCellValueAsDecimal(row.getCell(29));
                    BigDecimal transportArrear = getCellValueAsDecimal(row.getCell(30));

                    // 🔁 Check for duplicate emp_id in Excel
                    if (processedEmpIds.contains(empId)) {
                        throw new IllegalArgumentException("Duplicate entry found for Employee ID: " + empId + " at row " + row.getRowNum());
                    }
                    processedEmpIds.add(empId);

                    Optional<UserDetail> optionalUser = userDetailDao.findByIdField(empId);

                    // ✅ VALIDATE ALL INPUTS
                    validateRowData(empId, name, basic, hra, bonus, pf, tds, payableDays, optionalUser, user1);

                    UserDetail user = optionalUser.get();

                    user.setId(empId);
                    user.setBasicSalary(safe(basic));
                    user.setHra(safe(hra));
                    user.setStatutoryBonus(safe(bonus));
                    user.setPfEmployee(safe(pf));
                    user.setTds(safe(tds));
                    user.setPayableDays(payableDays);

                    user.setBasicArrear(safe(basicArrear));
                    user.setHraArrear(safe(hraArrear));
                    user.setBonusArrear(safe(bonusArrear));
                    user.setLtaArrear(safe(ltaArrear));
                    user.setRetentionArrear(safe(retentionArrear));
                    user.setSpecialArrear(safe(specialArrear));
                    user.setTransportArrear(safe(transportArrear));

                    BigDecimal gross = safe(basic).add(safe(hra)).add(safe(bonus));
                    BigDecimal deductions = safe(pf).add(safe(tds));
                    BigDecimal net = gross.subtract(deductions);

                    user.setGrossSalary(gross);
                    user.setDeductions(deductions);
                    user.setNetSalary(net);

                    user.setAdddate(new Date());
                    user.setAddwho("ADMIN_" + user1.getId());
                    user.setEditdate(new Date());
                    user.setEditwho("ADMIN_" + user1.getId());

                    userDetailDao.save(user);

                } catch (Exception e) {
                    System.err.println("❌ Skipping row " + row.getRowNum() + ": " + e.getMessage());
                    throw e; // Stop processing on any invalid record
                }
            }

            // ✅ Save upload success log
            UploadHistory history = new UploadHistory();
            history.setFileName(filename);
            history.setCompanyId(user1.getCompany_id());
            history.setEmployeeId(user1.getId());
            history.setUploadedBy("ADMIN_" + user1.getId());
            history.setUploadTime(LocalDateTime.now());
            history.setStatus("Success");
            history.setRemarks("File uploaded successfully");
            history.setAdddate(new Date());
            history.setAddwho("ADMIN_" + user1.getId());
            history.setEditdate(new Date());
            history.setEditwho("ADMIN_" + user1.getId());
            uploadHistoryReportDao.save(history);

        } catch (Exception e) {
            e.printStackTrace();

            // ❌ Save failure log
            UploadHistory history = new UploadHistory();
            history.setFileName(filename);
            history.setCompanyId(user1.getCompany_id());
            history.setEmployeeId(user1.getId());
            history.setUploadedBy("ADMIN_" + user1.getId());
            history.setUploadTime(LocalDateTime.now());
            history.setStatus("Failed");
            history.setRemarks("File upload failed: " + e.getMessage());
            history.setAdddate(new Date());
            history.setAddwho("ADMIN_" + user1.getId());
            history.setEditdate(new Date());
            history.setEditwho("ADMIN_" + user1.getId());
            uploadHistoryReportDao.save(history);

            throw new RuntimeException("❌ Failed to process Excel file: " + filename, e);
        }
    }

    
    private void validateRowData(Integer empId, String name, BigDecimal basic, BigDecimal hra, BigDecimal bonus,
            BigDecimal pf, BigDecimal tds, Integer payableDays,
            Optional<UserDetail> optionalUser, User user1) {
        
        if (empId == null) throw new IllegalArgumentException("Employee ID is missing.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Employee name is missing.");
        if (bonus == null) throw new IllegalArgumentException("Bonus cannot be null.");
        if (basic == null) throw new IllegalArgumentException("Basic salary cannot be null.");
        if (hra == null) throw new IllegalArgumentException("HRA cannot be null.");
        if (pf == null) throw new IllegalArgumentException("PF cannot be null.");
        if (tds == null) throw new IllegalArgumentException("TDS cannot be null.");
        if (payableDays == null || payableDays < 0 || payableDays > 31)
            throw new IllegalArgumentException("Payable days must be between 0 and 31.");

        if (optionalUser.isEmpty())
            throw new IllegalArgumentException("Employee ID " + empId + " not found.");

        UserDetail dbUser = optionalUser.get();
        if (!dbUser.getCompany_id().equals(user1.getCompany_id())) {
            throw new IllegalArgumentException("Access Denied: Employee ID " + empId + " does not belong to your company.");
        }

        if (!dbUser.getUsername().equalsIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("Employee name mismatch for ID " + empId +
                    ": expected " + dbUser.getUsername() + ", found " + name);
        }
    }



    private boolean isHeaderRow(Row row, String[] expectedHeaders) {
        for (int i = 0; i < expectedHeaders.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
            if (cell == null) return false;
            String value = getCellValueAsString(cell).trim();
            if (!value.equalsIgnoreCase(expectedHeaders[i])) {
                System.err.println("❌ Invalid header at column " + (i + 1) + ": Expected '" + expectedHeaders[i] + "', Found '" + value + "'");
                return false;
            }
        }
        return true;
    }

    
    private Workbook getWorkbook(InputStream is, String filename) throws IOException {
        if (filename.endsWith(".xlsx")) {
            return new XSSFWorkbook(is);
        } else if (filename.endsWith(".xls")) {
            return new HSSFWorkbook(is);
        } else {
            throw new IllegalArgumentException("Invalid Excel file format");
        }
    }


    // Helpers to parse Excel values safely
    private BigDecimal getCellValueAsDecimal(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                return new BigDecimal(cell.getStringCellValue().trim());
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private Integer getCellValueAsInt(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return 0;
        try {
            return (int) cell.getNumericCellValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private BigDecimal safe(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }
    
    public List<UploadHistory> findAllByCompanyId(String companyId) {
        return uploadHistoryReportDao.findByCompanyIdOrderByUploadTimeDesc(companyId);
    }
    
    public Optional<Job> findByCompanyAndDescription(String companyId, String description) {
        return jobDao.findByCompanyIdAndJobDescription(companyId, description);
    }

    @Transactional
    public void createOrUpdateJob(Job inputJob) {
        try {
            String companyId = inputJob.getCompany_id();
            String configKey = "SALARYSLIPGENERATEDATE";

            // Format date as yyyy-MM-dd
            String formattedDate = inputJob.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toString(); // e.g., "2025-05-09"
            String nsqlValue ="0";
            if(inputJob.getJob_active_or_not().equalsIgnoreCase("Y"))
            {
            	 nsqlValue="1";
            }

            // Step 1: Try updating nsql_config
            int updated = nSqlConfigDao.updateSalarySlipConfig(companyId, configKey, nsqlValue, formattedDate);

            // Step 2: If no rows updated, insert new config
            if (updated == 0) {
                nSqlConfigDao.insertSalarySlipConfig(companyId, configKey, nsqlValue, formattedDate);
            }

            // Step 3: Create or update job
            Optional<Job> existingOpt = jobDao.findByCompanyIdAndJobDescription(
                companyId, inputJob.getJob_description());

            if (existingOpt.isPresent()) {
                Job existing = existingOpt.get();
                existing.setJob_active_or_not(inputJob.getJob_active_or_not());
                existing.setStartDate(inputJob.getStartDate());
                existing.setJobFrequency(inputJob.getJobFrequency());
                existing.setNextRun(calculateNextRun(inputJob.getStartDate(), inputJob.getJobFrequency()));
                existing.setEditdate(new Date());
                existing.setEditwho("SYSTEM_UPDATE_JOB");
                jobDao.save(existing);
            } else {
                inputJob.setSno(jobDao.getNextSno());
                inputJob.setLastRun(null);
                inputJob.setNextRun(calculateNextRun(inputJob.getStartDate(), inputJob.getJobFrequency()));
                inputJob.setAdddate(new Date());
                inputJob.setAddwho("SYSTEM_CREATE_JOB");
                inputJob.setEditdate(new Date());
                inputJob.setEditwho("SYSTEM_CREATE_JOB");
                jobDao.save(inputJob);
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to update job/config.");
            e.printStackTrace();
        }
    }

    public List<Job> findAllJobs() {
        return jobDao.findAll();
    }

    public Date calculateNextRun(Date from, String frequency) {
        if (from == null || frequency == null) return null;
        LocalDateTime next = LocalDateTime.ofInstant(from.toInstant(), ZoneId.systemDefault());

        switch (frequency.toUpperCase()) {
            case "MINUTE": next = next.plusMinutes(1); break;
            case "HOURLY": next = next.plusHours(1); break;
            case "DAILY": next = next.plusDays(1); break;
            case "WEEKLY": next = next.plusWeeks(1); break;
            case "MONTHLY": next = next.plusMonths(1); break;
            case "YEARLY": next = next.plusYears(1); break;
            default: next = next.plusMinutes(1); break;
        }

        return Date.from(next.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    @Transactional
    public void updateTwoStepEnabled(int userId, boolean enabled) {
        userdao.updateTwoStepEnabled(userId, enabled);
    }
    
    public void syncUserAndUserDetail() {
        List<User> users = userdao.findAll();
        List<UserDetail> userDetails = userDetailDao.findAll();

        Map<Integer, UserDetail> userDetailById = userDetails.stream()
                .filter(ud -> ud.getId() != null)
                .collect(Collectors.toMap(UserDetail::getId, ud -> ud));

        for (User user : users) {
            if (user.getId() == null) continue;

            UserDetail detail = userDetailById.get(user.getId());
            if (detail == null) {
                detail = new UserDetail();
                detail.setId(user.getId());
                copyCommonFields(user, detail);
                userDetailDao.save(detail);
            } else {
                boolean updated = copyCommonFields(user, detail);
                if (updated) {
                    userDetailDao.save(detail);
                }
            }
        }
    }

    private boolean copyCommonFields(User user, UserDetail detail) {
        boolean updated = false;

        updated |= updateField(detail::getUsername, detail::setUsername, user.getUsername());
        updated |= updateField(detail::getEmail, detail::setEmail, user.getEmail());
        updated |= updateField(detail::getPhone, detail::setPhone, user.getPhone());
        updated |= updateField(detail::getGender, detail::setGender, user.getGender());
        updated |= updateField(detail::getDob, detail::setDob, user.getDob());
        updated |= updateField(detail::isEnabled, detail::setEnabled, user.isEnabled());
        updated |= updateField(detail::getAddress, detail::setAddress, user.getAddress());
        updated |= updateField(detail::getCountry, detail::setCountry, user.getCountry());
        updated |= updateField(detail::getImage_Name, detail::setImage_Name, user.getImage_Name());
        updated |= updateField(detail::getExperience, detail::setExperience, user.getExperience());
        updated |= updateField(detail::getSkills, detail::setSkills, user.getSkills());
        updated |= updateField(detail::getRole, detail::setRole, user.getRole());
        updated |= updateField(detail::getIpAddress, detail::setIpAddress, user.getIpAddress());
        updated |= updateField(detail::isAccountNonLocked, detail::setAccountNonLocked, user.isAccountNonLocked());
        updated |= updateField(detail::getFailedAttempt, detail::setFailedAttempt, user.getFailedAttempt());
        updated |= updateField(detail::getLockDateAndTime, detail::setLockDateAndTime, user.getLockDateAndTime());
        updated |= updateField(detail::getExpirelockDateAndTime, detail::setExpirelockDateAndTime, user.getExpirelockDateAndTime());
        updated |= updateField(detail::getStatus, detail::setStatus, user.getStatus());
        updated |= updateField(detail::isSeperation_manager_approved, detail::setSeperation_manager_approved, user.isSeperation_manager_approved());
        updated |= updateField(detail::isResignationRequestApplied, detail::setResignationRequestApplied, user.isResignationRequestApplied());
        updated |= updateField(detail::getEditdate, detail::setEditdate, user.getEditdate());
        updated |= updateField(detail::getEditwho, detail::setEditwho, user.getEditwho());
        updated |= updateField(detail::getLastWorkingDay, detail::setLastWorkingDay, user.getLastWorkingDay());
        updated |= updateField(detail::getSeperationDate, detail::setSeperationDate, user.getSeperationDate());
        updated |= updateField(detail::getCompany_id, detail::setCompany_id, user.getCompany_id());
        updated |= updateField(detail::isManager_or_not, detail::setManager_or_not, user.isManager_or_not());
        updated |= updateField(detail::getDesignation, detail::setDesignation, user.getDesignation());
        updated |= updateField(detail::getBase_location, detail::setBase_location, user.getBase_location());
        updated |= updateField(detail::getLaptop_id, detail::setLaptop_id, user.getLaptop_id());
        updated |= updateField(detail::getLaptop_brand, detail::setLaptop_brand, user.getLaptop_brand());
        updated |= updateField(detail::getLaptop_assign_date, detail::setLaptop_assign_date, user.getLaptop_assign_date());
        updated |= updateField(detail::getLaptop_serial_number, detail::setLaptop_serial_number, user.getLaptop_serial_number());
        updated |= updateField(detail::getBank_account_holder_name, detail::setBank_account_holder_name, user.getBank_account_holder_name());
        updated |= updateField(detail::getBank_account_number, detail::setBank_account_number, user.getBank_account_number());
        updated |= updateField(detail::getIfsc_code, detail::setIfsc_code, user.getIfsc_code());
        updated |= updateField(detail::getBank_name, detail::setBank_name, user.getBank_name());
        updated |= updateField(detail::getResume_file_name, detail::setResume_file_name, user.getResume_file_name());
        updated |= updateField(detail::getLinkdin_url, detail::setLinkdin_url, user.getLinkdin_url());
        updated |= updateField(detail::getGithub_url, detail::setGithub_url, user.getGithub_url());
        updated |= updateField(detail::getInstagram_url, detail::setInstagram_url, user.getInstagram_url());
        updated |= updateField(detail::getFacebook_url, detail::setFacebook_url, user.getFacebook_url());
        updated |= updateField(detail::getX_url, detail::setX_url, user.getX_url());

        return updated;
    }

    private <T> boolean updateField(Supplier<T> getter, Consumer<T> setter, T newValue) {
        T currentValue = getter.get();
        if (!Objects.equals(currentValue, newValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }
    
    public void initializeTaskTracker() {

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS task_id_tracker (
                SNO INT AUTO_INCREMENT PRIMARY KEY,
                LAST_TASK_ID INT NOT NULL,
                ADDDATE DATETIME,
                ADDWHO VARCHAR(255),
                EDITDATE DATETIME,
                EDITWHO VARCHAR(255)
            )
        """);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM task_id_tracker",
                Integer.class);

        if (count == null || count == 0) {
            jdbcTemplate.update("""
                INSERT INTO task_id_tracker
                (LAST_TASK_ID, ADDDATE, ADDWHO, EDITDATE, EDITWHO)
                VALUES (1000, NOW(), 'SYSTEM', NOW(), 'SYSTEM')
            """);

            System.out.println("✅ task_id_tracker initialized");
        }
    }
}