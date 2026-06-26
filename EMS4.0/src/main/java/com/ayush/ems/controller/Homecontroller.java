package com.ayush.ems.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ayush.ems.EMSMAIN;
import com.ayush.ems.EMSMAIN.OTPInfo;
import com.ayush.ems.dao.AdminDao;
import com.ayush.ems.dao.TaskDao;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.dao.UserDetailDao;
import com.ayush.ems.entities.Admin;
import com.ayush.ems.entities.Contact;
import com.ayush.ems.entities.User;
import com.ayush.ems.entities.UserDetail;
import com.ayush.ems.entities.UserLoginDateTime;
import com.ayush.ems.entities.stage_user;
import com.ayush.ems.globlaexceptionhandler.InvalidCaptchaException;
import com.ayush.ems.globlaexceptionhandler.TermsNotAgreedException;
import com.ayush.ems.globlaexceptionhandler.UserAlreadyExistsException;
import com.ayush.ems.helper.Message;
import com.ayush.ems.service.EmailService;
import com.ayush.ems.service.ForgotOTPEmailService;
import com.ayush.ems.service.Servicelayer;

import cn.apiclub.captcha.Captcha;

@Controller
public class Homecontroller {

    @Autowired
	private Servicelayer servicelayer;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserDao userdao;
	@Autowired
	private UserDetailDao userDetailDao;
	@Autowired
	private EmailService emailService;
	@Autowired
	private ForgotOTPEmailService forgotOTPEmailService;


    Homecontroller(TaskDao taskDao) {
    }


	@GetMapping("/")
	public String defaultpage(@ModelAttribute User user, Model model) {

		System.out.println("hi");
//		model.addAttribute("title", "Microsoft");
//		getCaptcha(user);
//		String Captcha_Created = user.getHidden();
//		EMSMAIN.captchaValidateMap.put(Captcha_Created, new Date());
//		int otp = (int) (Math.random() * 900000000) + 100000000;
//		System.out.println("MATH RANDOM  "+otp);
		model.addAttribute(user);
		return "index";
	}

	@GetMapping("/signup/{id}")
	public String homeee(@PathVariable("id") Integer id, Model model, stage_user user, HttpSession session) {
		try
		{
	    Optional<Admin> admin = adminDao.findByAdminId(id);
	    if (admin.isPresent()) {
	        String admin_id = String.valueOf(admin.get().getAid());
	        user.setAddwhoAdminId(admin_id);
	    }

	    // Fetch company IDs and add to the model
	    List<String> companyIds = servicelayer.GetAllCompanyID(); // Call your method to fetch company IDs
	    model.addAttribute("companyIds", companyIds);

	    getCaptcha(user);
	    EMSMAIN.captchaValidateMap.put(user.getHidden(), new Date());
	    session.setAttribute("hiddenCaptcha", user.getHidden());

	    return "signup";
		}
		catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
	        String className = Homecontroller.class.getName();
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
	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return "redirect:/swr";
	    }
	}


	public int otp(String email) {
	    int otp = (int) (Math.random() * 9000) + 1000; // Generates 4-digit OTP

	    // Store OTP against email in OTPInfo object
	    EMSMAIN.otpValidateMap.put(email, new EMSMAIN.OTPInfo(String.valueOf(otp), new Date()));

	    System.out.println("🔐 OTP generated for " + email + ": " + otp);

	    return otp;
	}


	@PostMapping("/verify_admin")
	public String homeee(@Valid Admin admin, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, String password,
			String Captcha, String email, HttpSession session, HttpServletRequest request) {

		try {
			if (!agreement) {
				System.out.println("you have not agreed terms and conditions");
				throw new Exception("you have not agreed terms and conditions");
			}
			if (result.hasErrors()) {
				System.out.println(result);
			} else {
				Optional<Admin> adminn = adminDao.findByUserName(email);
				if(adminn.isPresent())
				{
				admin = adminn.get();

				if (email.equals("ayush.gupta@trangile.com")) {
					System.out.println("aaid " + admin.getAid());
					System.out.println("admin " + admin);
					System.out.println("hi " + Captcha);
					String hiddenCaptcha = (String) session.getAttribute("hiddenCaptcha");
					System.out.println("hi2 " + hiddenCaptcha);
					System.out.println("hi1 " + adminn.get());
					if (email.equals(admin.getEmail()) && password.equals("admin")) {
//					servicelayer.validate_home_captcha();
						boolean found = false;
						Set<Map.Entry<String, Date>> entrySet_data = EMSMAIN.captchaValidateMap.entrySet();
						for (Map.Entry<String, Date> entry : entrySet_data) {
							String hidden_captcha = entry.getKey();
							if (Captcha.equals(hidden_captcha)) {
								// Match found, do something
								found = true;
								break; // Exit the loop once a match is found
							}
						}

						if (found) {
							int adminId = admin.getAid();
							session.setAttribute("adminId", adminId);
							System.out.println("email " + email);
//			            model.addAttribute("title","Send OTP");
							int otp = otp(admin.getEmail());
							String subject = "Admin Verification";
							String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>"
									+ "    <meta charset='UTF-8'>"
									+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
									+ "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + "    <style>"
									+ "        body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; }"
									+ "        .wrapper { width: 100%; background-color: #f9f9f9; padding: 40px 0; }"
									+ "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }"
									+ "        .header { background-color: #4CAF50; padding: 20px; text-align: center; color: #ffffff; }"
									+ "        .header h1 { margin: 0; font-size: 24px; }"
									+ "        .content { padding: 30px; text-align: center; }"
									+ "        .content h2 { font-size: 20px; color: #333; margin-bottom: 10px; }"
									+ "        .otp { font-size: 36px; font-weight: bold; letter-spacing: 10px; color: #4CAF50; margin: 20px 0; }"
									+ "        .info { font-size: 16px; color: #666; }"
									+ "        .footer { padding: 20px; text-align: center; font-size: 12px; color: #888; background-color: #f1f1f1; }"
									+ "        .footer a { color: #4CAF50; text-decoration: none; }" + "    </style>"
									+ "</head>" + "<body>" + "    <div class='wrapper'>"
									+ "        <table class='container' align='center'>" + "            <tr>"
									+ "                <td class='header'>"
									+ "                    <h1>Your OTP Code</h1>" + "                </td>"
									+ "            </tr>" + "            <tr>" + "                <td class='content'>"
									+ "                    <h2>Use the following OTP to complete your action:</h2>"
									+ "                    <div class='otp'>" + otp + "</div>" + // Dynamic OTP inserted
																									// here
									"                    <p class='info'>This OTP is valid for the next 05 minutes. Please do not share it with anyone.</p>"
									+ "                </td>" + "            </tr>" + "            <tr>"
									+ "                <td class='footer'>"
									+ "                    <p>If you didn’t request this, please ignore this email. Need help? <a href='#'>Contact Support</a>.</p>"
									+ "                </td>" + "            </tr>" + "        </table>" + "    </div>"
									+ "</body>" + "</html>";

							String to = email;
							CompletableFuture<Boolean> flagFuture = this.forgotOTPEmailService.sendEmail(message,
									subject, to);
							Boolean flag = flagFuture.get(); // Blocking call to get the result
							System.out.println(flag);
							if (flag) {
								session.setAttribute("myotp", otp);
								session.setAttribute("email", email);
								System.out.println("email is   " + email);
								System.out.println("aid " + admin.getAid() + " otp is   " + otp);
								session.setAttribute("message", new Message("OTP sent successfully", "alert-success"));
							} else {
								session.setAttribute("message",
										new Message("OTP not sent successfully", "alert-danger"));
							}
							return "redirect:/verify-otp2/" + admin.getAid();
//						} else {
//							session.setAttribute("message", "check your email id");
//							return "redirect:/verify_admin_get";
//						}
						} else {
							session.setAttribute("message", new Message("Wrong Captcha", "alert-danger"));
							return "redirect:/verify_admin_get";
						}
					} else {
						System.out.println("invalid credentials");
						session.setAttribute("message",
								new Message("Please Enter Correct Admin Credentials", "alert-danger"));

						return "redirect:/verify_admin_get";
					}
				} else {
					Optional<User> userFindByEmail = userdao.findByEmail(email);
					User GetUser = userFindByEmail.get();
					if (GetUser.isEnabled()) {
						System.out.println("aaid " + admin.getAid());
						System.out.println("admin " + admin);
						System.out.println("hi " + Captcha);
						String hiddenCaptcha = (String) session.getAttribute("hiddenCaptcha");
						System.out.println("hi2 " + hiddenCaptcha);
						System.out.println("hi1 " + adminn.get());
						if (email.equals(admin.getEmail()) && password.equals("admin")) {
//						servicelayer.validate_home_captcha();
							boolean found = false;
							Set<Map.Entry<String, Date>> entrySet_data = EMSMAIN.captchaValidateMap.entrySet();
							for (Map.Entry<String, Date> entry : entrySet_data) {
								String hidden_captcha = entry.getKey();
								if (Captcha.equals(hidden_captcha)) {
									// Match found, do something
									found = true;
									break; // Exit the loop once a match is found
								}
							}

							if (found) {
								int adminId = admin.getAid();
								session.setAttribute("adminId", adminId);
								System.out.println("email " + email);
								int otp = otp(admin.getEmail());
//							EMSMAIN.admin_send_otp.put(email, otp);
								System.out.println("OTP " + otp);
								String subject = "Admin Verification";
								String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>"
										+ "    <meta charset='UTF-8'>"
										+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
										+ "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + "    <style>"
										+ "        body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; }"
										+ "        .wrapper { width: 100%; background-color: #f9f9f9; padding: 40px 0; }"
										+ "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }"
										+ "        .header { background-color: #4CAF50; padding: 20px; text-align: center; color: #ffffff; }"
										+ "        .header h1 { margin: 0; font-size: 24px; }"
										+ "        .content { padding: 30px; text-align: center; }"
										+ "        .content h2 { font-size: 20px; color: #333; margin-bottom: 10px; }"
										+ "        .otp { font-size: 36px; font-weight: bold; letter-spacing: 10px; color: #4CAF50; margin: 20px 0; }"
										+ "        .info { font-size: 16px; color: #666; }"
										+ "        .footer { padding: 20px; text-align: center; font-size: 12px; color: #888; background-color: #f1f1f1; }"
										+ "        .footer a { color: #4CAF50; text-decoration: none; }"
										+ "    </style>" + "</head>" + "<body>" + "    <div class='wrapper'>"
										+ "        <table class='container' align='center'>" + "            <tr>"
										+ "                <td class='header'>"
										+ "                    <h1>Your OTP Code</h1>" + "                </td>"
										+ "            </tr>" + "            <tr>"
										+ "                <td class='content'>"
										+ "                    <h2>Use the following OTP to complete your action:</h2>"
										+ "                    <div class='otp'>" + otp + "</div>" + // Dynamic OTP
																										// inserted here
										"                    <p class='info'>This OTP is valid for the next 05 minutes. Please do not share it with anyone.</p>"
										+ "                </td>" + "            </tr>" + "            <tr>"
										+ "                <td class='footer'>"
										+ "                    <p>If you didn’t request this, please ignore this email. Need help? <a href='#'>Contact Support</a>.</p>"
										+ "                </td>" + "            </tr>" + "        </table>"
										+ "    </div>" + "</body>" + "</html>";

								String to = email;
								CompletableFuture<Boolean> flagFuture = this.forgotOTPEmailService.sendEmail(message,
										subject, to);
								Boolean flag = flagFuture.get(); // Blocking call to get the result
								System.out.println(flag);
								if (flag) {
									session.setAttribute("myotp", otp);
									session.setAttribute("email", email);
									System.out.println("email is   " + email);
									System.out.println("otp is   " + otp);
									session.setAttribute("message",
											new Message("OTP sent successfully", "alert-success"));
								} else {
									session.setAttribute("message",
											new Message("OTP not sent successfully", "alert-danger"));
								}
								return "redirect:/verify-otp2/" + admin.getAid();
//							} else {
//								session.setAttribute("message", "check your email id");
//								return "redirect:/verify_admin_get";
//							}
							} else {
								session.setAttribute("message", new Message("Wrong Captcha", "alert-danger"));
								return "redirect:/verify_admin_get";
							}
						} else {
							System.out.println("invalid credentials");
							session.setAttribute("message",
									new Message("Please Enter Correct Admin Credentials", "alert-danger"));

							return "redirect:/verify_admin_get";
						}
					} else {
						session.setAttribute("message", new Message(
								"Account Blocked/Disabled, For more information, Please contact administrator !!",
								"alert-danger"));
						return "redirect:/verify_admin_get";
					}
				}
				}
				else
				{
					session.setAttribute("message",
							new Message("Something went wrong !! : Admin Not Registered", "alert-danger"));
				}
				getCaptchaa(admin);
				EMSMAIN.captchaValidateMap.put(admin.getHidden(), new Date());
				session.setAttribute("hiddenCaptcha", admin.getHidden());
				System.out.println(admin.getHidden());
				return "redirect:/verify_admin_get";
			}
		} catch (Exception e) {
			e.printStackTrace();
			getCaptchaa(admin);
//			System.out.println(hiddenCaptcha);
			EMSMAIN.captchaValidateMap.put(admin.getCaptcha(), new Date());
			if (e.getMessage().equals("No value present")) {
				session.setAttribute("message",
						new Message("Something went wrong !! : "+e, "alert-danger"));
		        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//		        String exceptionAsString = e.toString();
		        String className = Homecontroller.class.getName();
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
		        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

		        // Console log for quick debugging
		        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
		        System.err.println(fullStackTrace);
			} else {
//				String exceptionAsString = e.toString();
//				// Get the current class
//				Class<?> currentClass = Homecontroller.class;
//
//				// Get the name of the class
//				String className = currentClass.getName();
//				String errorMessage = e.getMessage();
//				StackTraceElement[] stackTrace = e.getStackTrace();
//				String methodName = stackTrace[0].getMethodName();
//				int lineNumber = stackTrace[0].getLineNumber();
//				System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//				servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
				e.printStackTrace();

				session.setAttribute("message",
						new Message("Something went wrong !! : " + e.getMessage(), "alert-danger"));
			}
		}
		getCaptchaa(admin);
		EMSMAIN.captchaValidateMap.put(admin.getHidden(), new Date());
		session.setAttribute("hiddenCaptcha", admin.getHidden());
		System.out.println(admin.getHidden());
		return "redirect:/verify_admin_get";
	}

	private void getCaptcha(stage_user user) {
		Captcha captcha = com.ayush.ems.service.Servicelayer.createCaptcha(250, 80);
		user.setHidden(captcha.getAnswer());
		user.setCaptcha("");
		user.setImageCaptcha(servicelayer.encodeCaptcha(captcha));
		System.out.println("impoted" + user.getImageCaptcha());
	}

	private void getCaptcha(User user) {
		Captcha captcha = com.ayush.ems.service.Servicelayer.createCaptcha(250, 80);
		user.setHidden(captcha.getAnswer());
		user.setCaptcha("");
		user.setImageCaptcha(servicelayer.encodeCaptcha(captcha));
		System.out.println("impoted" + user.getImageCaptcha());
	}

	private String getCaptchaa(Admin admin) {
		try
		{
		Captcha captcha = com.ayush.ems.service.Servicelayer.createCaptcha(250, 80);
		admin.setHidden(captcha.getAnswer());
		admin.setCaptcha("");
		admin.setImageCaptcha(servicelayer.encodeCaptcha(captcha));
		String result = admin.getImageCaptcha();
		System.out.println("impoted" + admin.getImageCaptcha());
		return result;
		}
		catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
	        String className = Homecontroller.class.getName();
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
	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return "redirect:/swr";
	    }
	}

	@GetMapping("/verify-otp2/{id}")
	public String verify_otp(Model model, @PathVariable("id") Integer id, Admin admin, HttpSession session) {
		try {
			Optional<Admin> admin_get = adminDao.findByAdminId(id);
			if (admin_get != null) {
				Admin admin2 = admin_get.get();
				System.out.println("OTP ADMIN " + admin2.getAid());
				// Retrieve the message from the session
				Message message = (Message) session.getAttribute("message");
				if (message != null) {
					// Add the message to the model to pass it to the view
					session.setAttribute("message", message);
					System.out.println("Message: " + message);
					
				}
				System.out.println("Message: " + message);

				model.addAttribute("admin", admin2);
			}
			return "verify_otp2";
		} catch (Exception e) {
		        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//		        String exceptionAsString = e.toString();
		        String className = Homecontroller.class.getName();
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
		        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

		        // Console log for quick debugging
		        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
		        System.err.println(fullStackTrace);
			return "verify_otp2";
		}
	}

	@PostMapping("/verify-otp2/{id}")
	public String verifyOtpp(@PathVariable("id") Integer id, @RequestParam("otp") String otp,
	                         HttpSession session, Model model) {
	    try {
	        model.addAttribute("title", "Verify OTP");

	        String email = (String) session.getAttribute("email");
	        if (email == null) {
	            session.setAttribute("message", new Message("Session expired. Please start again.", "alert-danger"));
	            return "redirect:/admin_authenticate";
	        }

	        OTPInfo otpInfo = EMSMAIN.otpValidateMap.get(email);

	        if (otpInfo != null) {
	            long minutes = Duration.between(
	                otpInfo.getGeneratedAt().toInstant(),
	                new Date().toInstant()
	            ).toMinutes();

	            if (minutes <= 5) {
	                if (otpInfo.getOtp().equals(otp)) {
	                    Optional<Admin> user = this.adminDao.findByUserName(email);
	                    if (user.isPresent()) {
	                        EMSMAIN.otpValidateMap.remove(email); // Optional cleanup
	                        return "redirect:/signup/" + id;
	                    } else {
	                        session.setAttribute("message", new Message("User does not exist!", "alert-danger"));
	                        return "redirect:/admin_authenticate";
	                    }
	                } else {
	                    session.setAttribute("message", new Message("You have entered the wrong OTP", "alert-danger"));
	                    return "redirect:/verify-otp2/" + id;
	                }
	            } else {
	                EMSMAIN.otpValidateMap.remove(email); // Cleanup expired
	                session.setAttribute("message", new Message("OTP has expired", "alert-danger"));
	                return "redirect:/verify-otp2/" + id;
	            }
	        } else {
	            session.setAttribute("message", new Message("No OTP found for verification", "alert-danger"));
	            return "redirect:/verify-otp2/" + id;
	        }
	    } catch (Exception e) {
		        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//		        String exceptionAsString = e.toString();
		        String className = Homecontroller.class.getName();
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
		        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

		        // Console log for quick debugging
		        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
		        System.err.println(fullStackTrace);
	        return "redirect:/swr";
	    }
	}


	@PostMapping("/do-register")
	public String home(@Valid stage_user user, BindingResult result,
	                   @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
	                   @RequestParam("profileImage") MultipartFile file, 
	                   HttpSession session, HttpServletRequest request, Model model) {
	    
	    System.out.println("Account locked/not locked: " + user.isAccountNonLocked());

	    // ✅ Step 1: Check validation errors first
	    if (result.hasErrors()) {
	        session.setAttribute("message", new Message("Invalid fields. Please check your input.", "alert-danger"));
	        System.out.println(result);
	        // Fetch company IDs and add to the model
		    List<String> companyIds = servicelayer.GetAllCompanyID(); // Call your method to fetch company IDs
		    model.addAttribute("companyIds", companyIds);

	        // Reset captcha and return "signup" page
	        getCaptcha(user);
	        EMSMAIN.captchaValidateMap.put(user.getHidden(), new Date());
		    session.setAttribute("hiddenCaptcha", user.getHidden());
	        model.addAttribute("hiddenCaptcha", user.getHidden());
	        return "signup";  // ✅ Directly return signup so errors show up
	    }

	    try {
	        if (!agreement) {
	            throw new TermsNotAgreedException("You have not agreed to the terms and conditions");
	        }

	        // ✅ Step 2: Handle file upload
	        if (file.isEmpty()) {
	            user.setImageUrl("default.jpg");
	        } else {
	            user.setImageUrl(file.getOriginalFilename());
	            // Implement file save logic here
	        }

	        // ✅ Step 3: Validate Captcha
	        boolean isCaptchaValid = EMSMAIN.captchaValidateMap.containsKey(user.getCaptcha());
	        System.out.println("CAPTCHA "+user.getCaptcha());
	        if (!isCaptchaValid) {
	            throw new InvalidCaptchaException("Invalid Captcha");
	        }

	        // ✅ Step 4: Get client IP and location
	        String clientIp = getClientIpAddress(request);
	        String location = getLocationFromIp(clientIp);

	        // ✅ Step 5: Register user
	        servicelayer.register(user, clientIp, location);
	        session.setAttribute("message", new Message("Successfully Registered", "alert-success"));

	        return "redirect:/signup/" + user.getAddwhoAdminId();  // ✅ Redirect after successful registration

	    } catch (UserAlreadyExistsException e) {
	        session.setAttribute("message", new Message("Registration failed: " + e.getMessage(), "alert-danger"));
	        e.printStackTrace();
	    } catch (InvalidCaptchaException e) {
	        session.setAttribute("message", new Message("Captcha validation failed: " + e.getMessage(), "alert-danger"));
	        e.printStackTrace();
	    } catch (TermsNotAgreedException e) {
	        session.setAttribute("message", new Message("Terms not agreed: " + e.getMessage(), "alert-danger"));
	        e.printStackTrace();
	    } catch (Exception e) {
	        session.setAttribute("message", new Message("An error occurred: " + e.getMessage(), "alert-danger"));
	        e.printStackTrace();
	    }

	    // ✅ Step 6: If an exception occurs, reset captcha and return "signup"
	    getCaptcha(user);
	    EMSMAIN.captchaValidateMap.put(user.getHidden(), new Date());
	    session.setAttribute("hiddenCaptcha", user.getHidden());
	    model.addAttribute("hiddenCaptcha", user.getHidden());
	    // Fetch company IDs and add to the model
	    List<String> companyIds = servicelayer.GetAllCompanyID(); // Call your method to fetch company IDs
	    model.addAttribute("companyIds", companyIds);


	    return "signup";  // ✅ RETURN "signup" instead of redirecting
	}


	public String getClientIpAddress(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-Forwarded-For");
		if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
		}
		// Handle cases where multiple IPs are in X-Forwarded-For
		if (ipAddress != null && ipAddress.contains(",")) {
			ipAddress = ipAddress.split(",")[0];
		}
		return ipAddress;
	}

	/*
	 * Get location information from IP address using a simple API. Replace this
	 * method with your API call.
	 */
	private String getLocationFromIp(String ip) {
		try {
			// Use a simple public API to get location data
			String url = "https://ipapi.co/" + ip + "/city/";
			HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
			urlConnection.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// Return city name
			return response.toString().isEmpty() ? "Unknown Location" : response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "Unknown Location";
		}
	}

	@GetMapping("/signin")
	public String homee(@ModelAttribute User user, Model model, HttpSession session,
			UserLoginDateTime userLoginDateTime,
			@RequestParam(value = "expiredsession", defaultValue = "false") boolean expiredsession,
			@RequestParam(value = "notactivated", defaultValue = "false") boolean notactivated,
			HttpServletResponse response, HttpServletRequest request) {
		try {
			System.out.println("hi" + user.getId());
			session = request.getSession();
//			EMSMAIN.session_map_data.put(session.getId(), new Date());
			System.out.println("NOT IF ACTIVATED "+notactivated);
			if(notactivated)
			{
				System.out.println("IF NOT ACTIVATED ");
				session.setAttribute("message", new Message("Account Is Not Activated, Please Contact Administrator", "alert-danger"));	
			}
			if (expiredsession) {
//				servicelayer.update_login_dao(user);
//				System.out.println(new Date() + " Expired Time "+user.getId()+" Email "+user.getEmail());
//				session.setAttribute("message", new Message("Your session has expired. Please log in again.", "alert-warning"));
				return "redirect:/signin?expired=true";
			}
			System.out.println(")))))) " + session.getAttribute("messge"));
			getCaptcha(user);
			EMSMAIN.captchaValidateMap.put(user.getHidden(), new Date());
			return "signin";
		} catch (Exception e) {
			e.printStackTrace();
			return "signin";
//			}

		}

	}

	@GetMapping("/verify-otp2fa")
	public String showOtpPage(HttpSession session, Model model) {
	    String email = (String) session.getAttribute("2fa_user");
	    if (email != null) model.addAttribute("email", email);

	    // ✅ NEW: Show OTP sent message if available
	    String otpMsg = (String) session.getAttribute("otp_success_message");
	    if (otpMsg != null) {
	        model.addAttribute("otp_success_message", otpMsg);
	        session.removeAttribute("otp_success_message");
	    }

	    return "OtpVerification2FA";
	}

	@PostMapping("/do-verify-otp2fa")
	public String verifyOtp(@RequestParam("otp") String otp, HttpSession session, RedirectAttributes redirectAttributes) {
	    try {
	        String email = (String) session.getAttribute("2fa_user");
	        if (email == null) {
	            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
	            return "redirect:/signin";
	        }

	        OTPInfo otpInfo = EMSMAIN.otpValidateMap.get(email);
	        if (otpInfo == null) {
	            redirectAttributes.addFlashAttribute("error", "No OTP found for your session.");
	            return "redirect:/verify-otp2fa";
	        }

	        long minutes = Duration.between(otpInfo.getGeneratedAt().toInstant(), new Date().toInstant()).toMinutes();
	        if (minutes > 5) {
	            EMSMAIN.otpValidateMap.remove(email);
	            redirectAttributes.addFlashAttribute("error", "❌ OTP has expired.");
	            return "redirect:/verify-otp2fa";
	        }

	        if (!otpInfo.getOtp().equals(otp)) {
	            redirectAttributes.addFlashAttribute("error", "❌ Invalid OTP entered.");
	            return "redirect:/verify-otp2fa";
	        }

	        Optional<User> userOpt = userdao.findByEmail(email);
	        if (userOpt.isEmpty()) {
	            redirectAttributes.addFlashAttribute("error", "User not found.");
	            return "redirect:/signin";
	        }

	        // ✅ OTP success
	        session.setAttribute("2fa_verified", true);
	        session.removeAttribute("2fa_user");
	        session.removeAttribute("2fa_otp");
	        EMSMAIN.otpValidateMap.remove(email);

	        // ✅ Redirect to target stored during login
	        String redirectUrl = (String) session.getAttribute("post2fa_redirect_url");
	        session.removeAttribute("post2fa_redirect_url");
	        return "redirect:" + (redirectUrl != null ? redirectUrl : "/default");

	    } catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
	        String className = Homecontroller.class.getName();
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
	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        redirectAttributes.addFlashAttribute("error", "Something went wrong. Please try again.");
	        return "redirect:/swr";
	    }
	}




	@GetMapping("/forgot")
	public String openEmailForm(Model model) {
//		model.addAttribute("title","Forgot Password");
		return "forgot_email_form";

	}

	@GetMapping("/swr")
	public String somethingWentWrong() {
		return "SomethingWentWrong";
	}

	@GetMapping("/verify_admin_get")
	public String homeeee(Model model, Admin admin, HttpSession session, HttpServletRequest httpServletRequest)
			throws UnknownHostException {

		try {

//			boolean res1=VPNChecker.vpnchecker();
//			 if(res1)
//	            {
//	            	session.setAttribute("message", "VPN is Connected ,Please Disconnect Your VPN");
//	            	return "redirect:/swr";
//	            }
			System.out.println("hi");
			getCaptchaa(admin);
//			String str1 = httpServletRequest.getSession().getId();
			EMSMAIN.captchaValidateMap.put(admin.getHidden(), new Date());
			session.setAttribute("hiddenCaptcha", admin.getHidden());
//			allCaptcha.put(str1, res);
//			System.out.println(" " + allCaptcha.size());
////		System.out.println(emsmain.allCaptcha.isEmpty());
//			System.out.println(allCaptcha);
			String res = admin.getHidden();
			System.out.println(res);
			EMSMAIN.captchaValidateMap.put(res, new Date());
			System.out.println("CAPTCHA MAP-> " + EMSMAIN.captchaValidateMap);
			System.out.println("CAPTCHA MAP SIZE -> " + EMSMAIN.captchaValidateMap.size());
//			System.out.println(admin.getHidden() + " " + str1 + " " + res);
			return "AuthenticateAdmin";
		} catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
	        String className = Homecontroller.class.getName();
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
	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return "redirect:/swr";
	    }
	}

	@GetMapping("/swrr")
	public String swr() {
		return "SomethingWentWrong";
	}

	@PostMapping("/send-otp")
	public String sendotp(@RequestParam("email") String email, HttpSession session, Model model) {
	    Optional<User> user = userdao.findByEmail(email);

	    try {
	        if (user.isPresent()) {
	            model.addAttribute("title", "Send OTP");

	            String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000)); // 6-digit OTP
	            EMSMAIN.otpValidateMap.put(email, new EMSMAIN.OTPInfo(otp, new Date())); // Bind OTP to email

	            // Email content
	            String subject = "Forgot Password - OTP Verification";
	            String message = "<!DOCTYPE html>" +
	                    "<html lang='en'><head><meta charset='UTF-8'>" +
	                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
	                    "<meta http-equiv='X-UA-Compatible' content='IE=edge'>" +
	                    "<style>" +
	                    "body { font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif; background-color: #f9f9f9; }" +
	                    ".wrapper { width: 100%; padding: 40px 0; }" +
	                    ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px;" +
	                    "box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
	                    ".header { background-color: #4CAF50; padding: 20px; text-align: center; color: #ffffff; }" +
	                    ".header h1 { margin: 0; font-size: 26px; }" +
	                    ".content { padding: 30px; text-align: center; }" +
	                    ".content h2 { font-size: 22px; color: #333; margin-bottom: 10px; }" +
	                    ".otp { font-size: 40px; font-weight: bold; letter-spacing: 8px; color: #4CAF50;" +
	                    "background-color: #f0f8f0; padding: 15px; border-radius: 8px; margin: 20px 0; display: inline-block; }" +
	                    ".info { font-size: 16px; color: #666; margin-top: 10px; }" +
	                    ".footer { padding: 20px; text-align: center; font-size: 12px; color: #888; background-color: #f1f1f1; }" +
	                    ".footer a { color: #4CAF50; text-decoration: none; }" +
	                    "</style></head><body>" +
	                    "<div class='wrapper'><table class='container' align='center'>" +
	                    "<tr><td class='header'><h1>Your OTP Code For Forgot Password</h1></td></tr>" +
	                    "<tr><td class='content'>" +
	                    "<h2>Use the following OTP to complete your action:</h2>" +
	                    "<div class='otp'>" + otp + "</div>" +
	                    "<p class='info'>This OTP is valid for the next <strong>10 minutes</strong>. Please do not share it with anyone.</p>" +
	                    "</td></tr>" +
	                    "<tr><td class='footer'>" +
	                    "<p>If you didn’t request this, please ignore this email. Need help? <a href='[Support Link]'>Contact Support</a>.</p>" +
	                    "</td></tr></table></div></body></html>";

	            CompletableFuture<Boolean> flagFuture = emailService.sendEmail(message, subject, email);

	            try {
	                Boolean flag = flagFuture.get(); // Wait for email result
	                System.out.println("OTP Email sent: " + flag);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }

	            // Store in session to show in frontend if needed
	            session.setAttribute("email", email);
	            return "verify_otp"; // Forward to OTP verification page
	        } else {
	            session.setAttribute("message", "User is Not Registered, Please Sign Up.");
	            return "forgot_email_form";
	        }

	    } catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
	        String className = Homecontroller.class.getName();
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
	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return "redirect:/swr";
	    }
	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session, Model model) {
	    try {
	        model.addAttribute("title", "Verify OTP");

	        String email = (String) session.getAttribute("email");
	        System.out.println("Email: " + email);
	        System.out.println("User OTP: " + otp);

	        if (email == null || !EMSMAIN.otpValidateMap.containsKey(email)) {
	            session.setAttribute("message", "Session expired or OTP not found.");
	            return "verify_otp";
	        }

	        EMSMAIN.OTPInfo otpInfo = EMSMAIN.otpValidateMap.get(email);

	        // Check OTP match
	        if (!String.valueOf(otp).equals(otpInfo.getOtp())) {
	            session.setAttribute("message", "You have entered wrong OTP.");
	            return "verify_otp";
	        }

	        // Check OTP validity (5 minutes)
	        long minutesElapsed = Duration.between(
	            otpInfo.getGeneratedAt().toInstant(),
	            new Date().toInstant()
	        ).toMinutes();

	        if (minutesElapsed > 5) {
	            session.setAttribute("message", "OTP has expired.");
	            return "verify_otp";
	        }

	        // Verify user exists
	        Optional<User> user = this.userdao.findByEmail(email);
	        if (!user.isPresent()) {
	            session.setAttribute("message", "User does not exist !!");
	            return "forgot_email_form";
	        }

	        // Valid OTP
	        EMSMAIN.otpValidateMap.remove(email); // Optional: cleanup
	        return "password_change_form";

	    } catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
	        String className = Homecontroller.class.getName();
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
	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return "redirect:/swr";
	    }
	}

	@PostMapping("/change-password")
	@Transactional(rollbackOn = Exception.class)
	public String changePassword(
	        @RequestParam("newpassword") String newpassword,
	        @RequestParam("newconfirmpassword") String newconfirmpassword,
	        HttpSession session) {

	    try {
	        String email = (String) session.getAttribute("email");
	        System.out.println("Session Email: " + email);

	        Optional<User> userOpt = userdao.findByEmail(email);
	        if (!userOpt.isPresent()) {
	            session.setAttribute("message", "User not found");
	            return "password_change_form";
	        }

	        User user = userOpt.get();
	        Optional<UserDetail> userDetailOpt = userDetailDao.findByIdField(user.getId());

	        if (!userDetailOpt.isPresent()) {
	            session.setAttribute("message", "User details not found");
	            return "password_change_form";
	        }

	        if (!newpassword.equals(newconfirmpassword)) {
	            session.setAttribute("message", "Password Mismatch, Please Enter Same Password In Both Fields");
	            return "password_change_form";
	        }

	        if (bCryptPasswordEncoder.matches(newpassword, user.getPassword())) {
	            session.setAttribute("message", "Old Password and New Password are the Same");
	            return "password_change_form";
	        }

	        // Update User
	        user.setPassword(bCryptPasswordEncoder.encode(newpassword));
	        user.setFailedAttempt(0);
	        user.setAccountNonLocked(true);
	        user.setExpirelockDateAndTime(null);
	        user.setLockDateAndTime(null);

	        // Update UserDetail
	        UserDetail userDetail = userDetailOpt.get();
	        userDetail.setAccountNonLocked(true);
	        userDetail.setFailedAttempt(0);
	        userDetail.setExpirelockDateAndTime(null);
	        userDetail.setLockDateAndTime(null);

	        // Save both
	        userdao.save(user); // Save User
	        userDetailDao.save(userDetail); // Save UserDetail

	        session.setAttribute("message", new Message("Password Changed Successfully", "alert-success"));
	        return "signin";

	    } catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
	        String className = Homecontroller.class.getName();
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
	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
	        return "redirect:/swr";
	    }
	}


//	@PostMapping("/create_order")
//	@ResponseBody
//	public String create_order(@RequestBody Map<String, Object> data) throws Exception {
//		try {
//			int amount = Integer.parseInt(data.get("amount").toString());
//			RazorpayClient razorpay = new RazorpayClient("rzp_test_icIfOJXJUlRjph", "L90mE03ZqQXO5rgWxRnn8JCn");
//			System.out.println("RAZORPAY " + razorpay);
//			JSONObject orderRequest = new JSONObject();
//			orderRequest.put("amount", amount * 100);
//			orderRequest.put("currency", "INR");
//			orderRequest.put("receipt", "txn_235425");
////		JSONObject notes = new JSONObject();
////		notes.put("notes_key_1","Tea, Earl Grey, Hot");
////		orderRequest.put("notes",notes);
//
//			Order order = razorpay.Orders.create(orderRequest);
//			System.out.println(order);
//			return order.toString();
//		} catch (Exception e) {
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = AdminController.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//
//			return "redirect:/swr";
//		}
//	}
//
//	@GetMapping("/payment")
//	public String payment() {
//		return "payment";
//	}
//	
//	    @GetMapping("/api/authenticated")
//	    @ResponseBody
//	    public Map<String, Boolean> isAuthenticated(HttpServletRequest request) {
//	        Map<String, Boolean> response = new HashMap<>();
//	        // Check authentication status
//	        response.put("authenticated", request.getUserPrincipal() != null);
//	        System.out.println(">>>>>>>>>>>>>>>> "+response);
//	        return response;
//	    }

	@RequestMapping("/resendotp")
	public String resendOTP(@RequestParam("admin_email") String email, HttpSession session)
			throws InterruptedException, ExecutionException {

		int otp = otp(email);
		Optional<Admin> admin = adminDao.findByUserName(email);
		Admin get_admin = admin.get();
		String subject = "Admin Verification";
		String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>" + "    <meta charset='UTF-8'>"
				+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
				+ "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + "    <style>"
				+ "        body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; }"
				+ "        .wrapper { width: 100%; background-color: #f9f9f9; padding: 40px 0; }"
				+ "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }"
				+ "        .header { background-color: #4CAF50; padding: 20px; text-align: center; color: #ffffff; }"
				+ "        .header h1 { margin: 0; font-size: 24px; }"
				+ "        .content { padding: 30px; text-align: center; }"
				+ "        .content h2 { font-size: 20px; color: #333; margin-bottom: 10px; }"
				+ "        .otp { font-size: 36px; font-weight: bold; letter-spacing: 10px; color: #4CAF50; margin: 20px 0; }"
				+ "        .info { font-size: 16px; color: #666; }"
				+ "        .footer { padding: 20px; text-align: center; font-size: 12px; color: #888; background-color: #f1f1f1; }"
				+ "        .footer a { color: #4CAF50; text-decoration: none; }" + "    </style>" + "</head>" + "<body>"
				+ "    <div class='wrapper'>" + "        <table class='container' align='center'>" + "            <tr>"
				+ "                <td class='header'>" + "                    <h1>Your OTP Code</h1>"
				+ "                </td>" + "            </tr>" + "            <tr>"
				+ "                <td class='content'>"
				+ "                    <h2>Use the following OTP to complete your action:</h2>"
				+ "                    <div class='otp'>" + otp + "</div>" + // Dynamic OTP inserted here
				"                    <p class='info'>This OTP is valid for the next 05 minutes. Please do not share it with anyone.</p>"
				+ "                </td>" + "            </tr>" + "            <tr>"
				+ "                <td class='footer'>"
				+ "                    <p>If you didn’t request this, please ignore this email. Need help? <a href='#'>Contact Support</a>.</p>"
				+ "                </td>" + "            </tr>" + "        </table>" + "    </div>" + "</body>"
				+ "</html>";

		String to = email;
		CompletableFuture<Boolean> flagFuture = this.forgotOTPEmailService.sendEmail(message, subject, to);
		Boolean flag = flagFuture.get(); // Blocking call to get the result
		System.out.println(flag);
		if (flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			System.out.println("email is   " + email);
			System.out.println("aid " + get_admin.getAid() + " otp is   " + otp);
			session.setAttribute("message", new Message("Resend OTP Sent Successfully", "alert-success"));
		} else {
			session.setAttribute("message", new Message("Resend OTP not Sent Successfully", "alert-danger"));
		}
		return "redirect:/verify-otp2/" + get_admin.getAid();
	}
	
	@GetMapping("/about")
	public String About()
	{
		return "About";
	}
	
	@GetMapping("/contact_us")
	public String ContactUs(Contact contact, Model model)
	{
		return "ContactUs";
	}
	
	@PostMapping("/do-submit")
	public String submitContactForm(
	        @Valid Contact user, 
	        BindingResult result,
	        @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
	        HttpSession session, 
	        HttpServletRequest request) {

	    try {
	        // Check if the user agreed to the terms and conditions
	        if (!agreement) {
	            throw new TermsNotAgreedException("You must agree to the terms and conditions.");
	        }

	        // Handle validation errors
	        if (result.hasErrors()) {
	            session.setAttribute("message", new Message("Validation failed. Please check your input.", "alert-danger"));
	            return "ContactUs";
	        }

	        // Capture client's IP address
	        String clientIp = getClientIpAddress(request);

	        // Fetch location based on IP address
	        String location = getLocationFromIp(clientIp);

	        // Register the contact request
	        servicelayer.register1(user, clientIp, location);

	        // Set success message
	        session.setAttribute("message", new Message("Successfully Registered!", "alert-success"));
	        return "ContactUs";

	    } catch (TermsNotAgreedException e) {
	        session.setAttribute("message", new Message("Terms not agreed: " + e.getMessage(), "alert-danger"));
	    } catch (Exception e) {
	        session.setAttribute("message", new Message("An error occurred: " + e.getMessage(), "alert-danger"));
	        e.printStackTrace();
	    }

	    // If an exception occurs, return the same page with error message
	    return "ContactUs";
	}
	
	@GetMapping("/resend-otp")
	public String resendOtp(@RequestParam("email") String email,
	                        HttpSession session,
	                        RedirectAttributes redirectAttributes) {
	    try {
	        Optional<User> optionalUser = userdao.findByEmail(email);
	        if (!optionalUser.isPresent()) {
	            redirectAttributes.addFlashAttribute("error", "❌ User not found with email: " + email);
	            return "redirect:/verify-otp2fa";
	        }

	        User user = optionalUser.get();
	        int otp = ThreadLocalRandom.current().nextInt(100000, 1000000); // 6-digit OTP

	        // Store OTP in session and map
	        session.setAttribute("2fa_user", email);
	        session.setAttribute("2fa_otp", String.valueOf(otp));
	        EMSMAIN.otpValidateMap.put(email, new EMSMAIN.OTPInfo(String.valueOf(otp), new Date()));

	        // Email content
	        String subject = "🔐 Your EMS OTP Code";
	        String content = String.format(
	            "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
	            "<style>" +
	            "body { font-family: 'Segoe UI', sans-serif; background-color: #f9f9f9; margin: 0; padding: 0; }" +
	            ".container { max-width: 600px; margin: 40px auto; background: #ffffff; padding: 30px; border-radius: 8px;" +
	            "box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
	            ".header { font-size: 20px; font-weight: 600; color: #2C3E50; margin-bottom: 20px; }" +
	            ".otp-box { font-size: 28px; color: #2E86C1; font-weight: bold; background: #f1f8ff; padding: 15px;" +
	            "border-radius: 6px; text-align: center; margin: 20px 0; }" +
	            ".content { font-size: 15px; color: #333333; line-height: 1.6; }" +
	            ".footer { font-size: 12px; color: #888888; text-align: center; margin-top: 30px; }" +
	            "</style></head><body><div class='container'>" +
	            "<div class='header'>🔐 EMS OTP Verification</div>" +
	            "<div class='content'>" +
	            "<p>Dear <b>%s</b>,</p>" +
	            "<p>You have requested a new One-Time Password (OTP) to verify your login. Use the OTP below:</p>" +
	            "<div class='otp-box'>%06d</div>" +
	            "<p>This OTP is valid for <b>5 minutes</b>. Please do not share it with anyone.</p>" +
	            "<p>If this was not initiated by you, please reset your password immediately and contact EMS support.</p>" +
	            "<p>Regards,<br><b>EMS Security Team</b></p>" +
	            "</div><div class='footer'>© %d EMS. All rights reserved.</div>" +
	            "</div></body></html>",
	            user.getUsername(), otp, Calendar.getInstance().get(Calendar.YEAR)
	        );

	        emailService.sendEmail(content, subject, email);

	        redirectAttributes.addFlashAttribute("success", "✅ A new OTP has been sent to your email: " + email);
	        return "redirect:/verify-otp2fa";

	    } catch (Exception ex) {
	        redirectAttributes.addFlashAttribute("error", "⚠️ An unexpected error occurred while resending OTP: " + ex.getMessage());
	        return "redirect:/verify-otp2fa";
	    }
	}

    // 🔹 Step 1: 2FA landing page
    @GetMapping("/2fa")
    public String show2faChoice() {
        return "2fa-choice";
    }

    // 🔹 Step 2: Email entry page
    @GetMapping("/2fa/email")
    public String showEmailEntryPage() {
        return "2fa-email";
    }

    // 🔹 Step 3: Process email and send OTP
    @PostMapping("/2fa/email")
    public String handleEmail(@RequestParam("email") String email,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
try
{
        Optional<User> optionalUser = userdao.findByEmail(email);
        if (!optionalUser.isPresent()) {
            redirectAttributes.addFlashAttribute("error",
                    "❌ The email address is not registered. Please contact the administrator.");
            return "redirect:/2fa/email";
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));

        // Store in session
        session.setAttribute("2fa_user", email);
        session.setAttribute("2fa_otp", otp);

        // Send OTP
        sendOtpEmail(email, otp);
      System.out.println("EMAL "+email+", otp "+otp);
        redirectAttributes.addFlashAttribute("success",
                "✅ OTP sent successfully to " + email + ". Please check your inbox.");

        return "redirect:/2fa/setup";
    }
catch (Exception e) {
    // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//    String exceptionAsString = e.toString();
    String className = Homecontroller.class.getName();
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
    servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

    // Console log for quick debugging
    System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
    System.err.println(fullStackTrace);
	 return "redirect:/2fa/setup";
}
    }
    

    // 🔹 Step 4: Show OTP page with employee info
    @GetMapping("/2fa/setup")
    public String showOtpPage2FA(HttpSession session, Model model) {
        String email = (String) session.getAttribute("2fa_user");
        System.out.println("GET EMAIL FROM  SESSION "+email);
        if (email == null) {
            return "redirect:/2fa/email";
        }

        Optional<User> employee = userdao.findByEmail(email);
        System.out.println("user found "+employee.isPresent());
        if (!employee.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "redirect:/2fa/email";
        }

        model.addAttribute("employee", employee.get());
        return "2fa-verify-page";
    }
    @PostMapping("/2fa/verify")
    public String verifyOtp(@RequestParam("otp") String enteredOtp,
                            @RequestParam("action") String action,
                            HttpSession session,
                            Model model) {

    	try
    	{
        String email = (String) session.getAttribute("2fa_user");
        String sessionOtp = (String) session.getAttribute("2fa_otp");

        if (email == null || sessionOtp == null) {
            model.addAttribute("error", "Session expired. Please start again.");
            return "redirect:/2fa/email";
        }

        Optional<User> optionalUser = userdao.findByEmail(email);
        System.out.println("USER NOT FOUND => "+optionalUser.isPresent());
        if (!optionalUser.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "redirect:/2fa/email";
        }

        User user = optionalUser.get();
      
        if (enteredOtp.equals(sessionOtp)) {
            session.removeAttribute("2fa_otp");

            if ("enable".equalsIgnoreCase(action)) {
                servicelayer.updateTwoStepEnabled(user.getId(), true);
                model.addAttribute("success", "✅ 2FA enabled successfully for " + email);
            } else if ("disable".equalsIgnoreCase(action)) {
                servicelayer.updateTwoStepEnabled(user.getId(), false);
                model.addAttribute("success", "❌ 2FA disabled for " + email);
            } else {
                model.addAttribute("success", "✅ OTP verified.");
            }

            // 🔄 Refresh user data after update
            User refreshedUser = userdao.findByEmail(email).orElse(user);
            model.addAttribute("employee", refreshedUser);
            model.addAttribute("showLogin", true);
        } else {
        	 System.out.println("USER NOT FOUND <=> "+optionalUser.isPresent());
            model.addAttribute("employee", user);
            model.addAttribute("error", "❌ Invalid OTP. Please try again.");
            model.addAttribute("showLogin", false);
        }

        return "2fa-verify-page";
    }
    	catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
	        String className = Homecontroller.class.getName();
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
	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        // Console log for quick debugging
	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);
    		 return "2fa-verify-page";
    	}
    }



    // 🔹 Step 6: Resend OTP
    @GetMapping("/2fa/setup-resend-otp")
    public String resendOtp(@RequestParam("email") String email,
                            HttpSession session,
                            Model model) {

        String otp = String.format("%06d", new SecureRandom().nextInt(999999));
        session.setAttribute("2fa_otp", otp);
        session.setAttribute("2fa_user", email);

        sendOtpEmail(email, otp);
        model.addAttribute("success", "🔄 OTP resent to " + email);

        Optional<User> employee = userdao.findByEmail(email);
        employee.ifPresent(user -> model.addAttribute("employee", user));

        return "2fa-verify-page";
    }

    // 🔹 Send OTP Email (HTML)
    public void sendOtpEmail(String to, String otp) {
        String subject = "🔐 Your OTP for 2-Step Verification";
        String email = to;
        String content = "<html>" +
                "<head><style>" +
                ".email-body {font-family:'Segoe UI',sans-serif;background-color:#f4f4f4;padding:40px 0;text-align:center;}" +
                ".card {background:#fff;max-width:500px;margin:auto;padding:30px;border-radius:10px;box-shadow:0 4px 12px rgba(0,0,0,0.1);text-align:left;}" +
                ".otp-box {font-size:24px;font-weight:bold;color:#007bff;letter-spacing:4px;text-align:center;margin:20px 0;}" +
                ".footer {font-size:12px;color:#888;margin-top:30px;text-align:center;}" +
                "</style></head>" +
                "<body><div class='email-body'><div class='card'>" +
                "<h2>🔐 2-Step Verification</h2>" +
                "<p>Hello,</p><p>Your One-Time Password (OTP) for 2FA is:</p>" +
                "<div class='otp-box'>" + otp + "</div>" +
                "<p>This OTP is valid for the next <b>5 minutes</b>.</p>" +
                "<p>If you didn’t request this, please ignore this email.</p>" +
                "<br/><p>Thank you,<br/>EMS Team</p></div>" +
                "<div class='footer'>© EMS Portal | Please do not reply to this email.</div>" +
                "</div></body></html>";

        emailService.sendEmail(content, subject, email); // should support HTML
    }


}