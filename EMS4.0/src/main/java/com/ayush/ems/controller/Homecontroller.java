package com.ayush.ems.controller;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ayush.ems.EMSMAIN;
import com.ayush.ems.dao.AdminDao;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.entities.Admin;
import com.ayush.ems.entities.User;
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
	private EmailService emailService;
	@Autowired
	private ForgotOTPEmailService forgotOTPEmailService;

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
//		int adminID1 = (int) session.getAttribute("adminId");

//		System.out.println("adminid1 " + adminID1);
//		user.setAaid(adminID1);
		Optional<Admin> admin = adminDao.findById(id);
		if (admin != null) {
			Admin admin1 = admin.get();
			user.setAddwho(admin1.getAid());
		}
//		int var=user.getAaid();
		System.out.println("hi");
		getCaptcha(user);
		String hiddenCaptcha = user.getHidden();
		EMSMAIN.captchaValidateMap.put(hiddenCaptcha, new Date());
		session.setAttribute("hiddenCaptcha", user.getHidden());
		System.out.println(user.getHidden());
		return "signup";
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
				admin = adminn.get();
			
				if(email.equals("ayush.gupta@trangile.com"))
				{
				System.out.println("aaid " + admin.getAid());
				System.out.println("admin " + admin);
				System.out.println("hi " + Captcha);
				String hiddenCaptcha = (String) session.getAttribute("hiddenCaptcha");
				System.out.println("hi2 " + hiddenCaptcha);
				System.out.println("hi1 " + adminn.get());
				if (email.equals(admin.getEmail()) && password.equals("admin")) {
					servicelayer.validate_home_captcha();
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
						int otp = (int) (Math.random() * 9000) + 1000;
						EMSMAIN.otpValidateMap.put(otp, new Date());
						EMSMAIN.admin_send_otp.put(email, otp);
						System.out.println("OTP ?????????????????????///////////...... " + otp);
						String subject = "Admin Verification";
						String message = "" +
							    "<!DOCTYPE html>" +
							    "<html lang='en'>" +
							    "<head>" +
							    "    <meta charset='UTF-8'>" +
							    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
							    "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" +
							    "    <style>" +
							    "        body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; }" +
							    "        .wrapper { width: 100%; background-color: #f9f9f9; padding: 40px 0; }" +
							    "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }" +
							    "        .header { background-color: #4CAF50; padding: 20px; text-align: center; color: #ffffff; }" +
							    "        .header h1 { margin: 0; font-size: 24px; }" +
							    "        .content { padding: 30px; text-align: center; }" +
							    "        .content h2 { font-size: 20px; color: #333; margin-bottom: 10px; }" +
							    "        .otp { font-size: 36px; font-weight: bold; letter-spacing: 10px; color: #4CAF50; margin: 20px 0; }" +
							    "        .info { font-size: 16px; color: #666; }" +
							    "        .footer { padding: 20px; text-align: center; font-size: 12px; color: #888; background-color: #f1f1f1; }" +
							    "        .footer a { color: #4CAF50; text-decoration: none; }" +
							    "    </style>" +
							    "</head>" +
							    "<body>" +
							    "    <div class='wrapper'>" +
							    "        <table class='container' align='center'>" +
							    "            <tr>" +
							    "                <td class='header'>" +
							    "                    <h1>Your OTP Code</h1>" +
							    "                </td>" +
							    "            </tr>" +
							    "            <tr>" +
							    "                <td class='content'>" +
							    "                    <h2>Use the following OTP to complete your action:</h2>" +
							    "                    <div class='otp'>" + otp + "</div>" +  // Dynamic OTP inserted here
							    "                    <p class='info'>This OTP is valid for the next 05 minutes. Please do not share it with anyone.</p>" +
							    "                </td>" +
							    "            </tr>" +
							    "            <tr>" +
							    "                <td class='footer'>" +
							    "                    <p>If you didn’t request this, please ignore this email. Need help? <a href='#'>Contact Support</a>.</p>" +
							    "                </td>" +
							    "            </tr>" +
							    "        </table>" +
							    "    </div>" +
							    "</body>" +
							    "</html>";

						String to = email;
						 CompletableFuture<Boolean> flagFuture = this.forgotOTPEmailService.sendEmail(message, subject, to);
						   Boolean flag = flagFuture.get(); // Blocking call to get the result
						System.out.println(flag);
						if (flag) {
							session.setAttribute("myotp", otp);
							session.setAttribute("email", email);
							System.out.println("email is   " + email);
							System.out.println("otp is   " + otp);
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
				}
				else
				{
					Optional<User> userFindByEmail = userdao.findByEmail(email);
					User GetUser = userFindByEmail.get();
					if(GetUser.isEnabled())
					{
					System.out.println("aaid " + admin.getAid());
					System.out.println("admin " + admin);
					System.out.println("hi " + Captcha);
					String hiddenCaptcha = (String) session.getAttribute("hiddenCaptcha");
					System.out.println("hi2 " + hiddenCaptcha);
					System.out.println("hi1 " + adminn.get());
					if (email.equals(admin.getEmail()) && password.equals("admin")) {
						servicelayer.validate_home_captcha();
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
//				            model.addAttribute("title","Send OTP");
							int otp = (int) (Math.random() * 9000) + 1000;
							EMSMAIN.otpValidateMap.put(otp, new Date());
							EMSMAIN.admin_send_otp.put(email, otp);
							System.out.println("OTP " + otp);
							String subject = "Admin Verification";
							String message = "" +
								    "<!DOCTYPE html>" +
								    "<html lang='en'>" +
								    "<head>" +
								    "    <meta charset='UTF-8'>" +
								    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
								    "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" +
								    "    <style>" +
								    "        body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; }" +
								    "        .wrapper { width: 100%; background-color: #f9f9f9; padding: 40px 0; }" +
								    "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }" +
								    "        .header { background-color: #4CAF50; padding: 20px; text-align: center; color: #ffffff; }" +
								    "        .header h1 { margin: 0; font-size: 24px; }" +
								    "        .content { padding: 30px; text-align: center; }" +
								    "        .content h2 { font-size: 20px; color: #333; margin-bottom: 10px; }" +
								    "        .otp { font-size: 36px; font-weight: bold; letter-spacing: 10px; color: #4CAF50; margin: 20px 0; }" +
								    "        .info { font-size: 16px; color: #666; }" +
								    "        .footer { padding: 20px; text-align: center; font-size: 12px; color: #888; background-color: #f1f1f1; }" +
								    "        .footer a { color: #4CAF50; text-decoration: none; }" +
								    "    </style>" +
								    "</head>" +
								    "<body>" +
								    "    <div class='wrapper'>" +
								    "        <table class='container' align='center'>" +
								    "            <tr>" +
								    "                <td class='header'>" +
								    "                    <h1>Your OTP Code</h1>" +
								    "                </td>" +
								    "            </tr>" +
								    "            <tr>" +
								    "                <td class='content'>" +
								    "                    <h2>Use the following OTP to complete your action:</h2>" +
								    "                    <div class='otp'>" + otp + "</div>" +  // Dynamic OTP inserted here
								    "                    <p class='info'>This OTP is valid for the next 05 minutes. Please do not share it with anyone.</p>" +
								    "                </td>" +
								    "            </tr>" +
								    "            <tr>" +
								    "                <td class='footer'>" +
								    "                    <p>If you didn’t request this, please ignore this email. Need help? <a href='#'>Contact Support</a>.</p>" +
								    "                </td>" +
								    "            </tr>" +
								    "        </table>" +
								    "    </div>" +
								    "</body>" +
								    "</html>";

							String to = email;
							 CompletableFuture<Boolean> flagFuture = this.forgotOTPEmailService.sendEmail(message, subject, to);
							   Boolean flag = flagFuture.get(); // Blocking call to get the result
							System.out.println(flag);
							if (flag) {
								session.setAttribute("myotp", otp);
								session.setAttribute("email", email);
								System.out.println("email is   " + email);
								System.out.println("otp is   " + otp);
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
					}
					else
					{
					session.setAttribute("message", new Message("Account Blocked/Disabled, For more information, Please contact administrator !!", "alert-danger"));
					return "redirect:/verify_admin_get";
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			getCaptchaa(admin);
//			System.out.println(hiddenCaptcha);
			EMSMAIN.captchaValidateMap.put(admin.getCaptcha(), new Date());
			if (e.getMessage().equals("No value present")) {
				session.setAttribute("message",
						new Message("Something went wrong !! : Admin Not Registered", "alert-danger"));
				String exceptionAsString = e.toString();
				// Get the current class
				Class<?> currentClass = Homecontroller.class;

				// Get the name of the class
				String className = currentClass.getName();
				String errorMessage = e.getMessage();
				StackTraceElement[] stackTrace = e.getStackTrace();
				String methodName = stackTrace[0].getMethodName();
				int lineNumber = stackTrace[0].getLineNumber();
				System.out.println("METHOD NAME " + methodName + " " + lineNumber);
				servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

			} else {
				String exceptionAsString = e.toString();
				// Get the current class
				Class<?> currentClass = Homecontroller.class;

				// Get the name of the class
				String className = currentClass.getName();
				String errorMessage = e.getMessage();
				StackTraceElement[] stackTrace = e.getStackTrace();
				String methodName = stackTrace[0].getMethodName();
				int lineNumber = stackTrace[0].getLineNumber();
				System.out.println("METHOD NAME " + methodName + " " + lineNumber);
				servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

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
		user.setImageCaptcha(com.ayush.ems.service.Servicelayer.encodeCaptcha(captcha));
		System.out.println("impoted" + user.getImageCaptcha());
	}

	
	private void getCaptcha(User user) {
		Captcha captcha = com.ayush.ems.service.Servicelayer.createCaptcha(250, 80);
		user.setHidden(captcha.getAnswer());
		user.setCaptcha("");
		user.setImageCaptcha(com.ayush.ems.service.Servicelayer.encodeCaptcha(captcha));
		System.out.println("impoted" + user.getImageCaptcha());
	}
	
	private String getCaptchaa(Admin admin) {
		Captcha captcha = com.ayush.ems.service.Servicelayer.createCaptcha(250, 80);
		admin.setHidden(captcha.getAnswer());
		admin.setCaptcha("");
		admin.setImageCaptcha(com.ayush.ems.service.Servicelayer.encodeCaptcha(captcha));
		String result = admin.getImageCaptcha();
		System.out.println("impoted" + admin.getImageCaptcha());
		return result;
	}

	@GetMapping("/verify-otp2/{id}")
	public String verify_otp(Model model, @PathVariable("id") Integer id, Admin admin) {
		try {
			Optional<Admin> admin_get = adminDao.findById(id);
			if (admin_get != null) {
				Admin admin2 = admin_get.get();
				System.out.println("OTP ADMIN " + admin2.getAid());
				model.addAttribute("admin", admin2);
			}
			return "verify_otp2";
		} catch (Exception e) {
			return "redirect:/swr";
		}
	}

	@PostMapping("/verify-otp2/{id}")
	public String verifyOtpp(@PathVariable("id") Integer id, @RequestParam("otp") int otp, HttpSession session,
			Model model) {
		try {
			model.addAttribute("title", "Verify OTP");
//		Integer myOtp = (Integer) session.getAttribute("myotp");
//		System.out.println(" user otp" + otp);
//		System.out.println(" our otp" + myOtp);
			String email = (String) session.getAttribute("email");
//		System.out.println("emailll " + email);
			boolean flag = false;
			Set<Map.Entry<Integer, Date>> myOtp = EMSMAIN.otpValidateMap.entrySet();
			for (Map.Entry<Integer, Date> entry : myOtp) {
				Integer myotp1 = entry.getKey();
				if (myotp1 == otp) {
					flag = true;
					break;
				}
			}
			if (flag) {
				Optional<Admin> user = this.adminDao.findByUserName(email);
				if (user == null) {
					session.setAttribute("message", "User does not exist !!");
					return "redirect:/admin_authenticate";
				}
				return "redirect:/signup/" + id;
			} else {
				session.setAttribute("message", "you have entered wrong otp");
				return "redirect:/verify-otp2/" + id;
			}
		} catch (Exception e) {
			return "redirect:/swr";
		}
	}

	@PostMapping("/do-register")
	public String home(@Valid stage_user user, BindingResult result,
	                   @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
	                   @RequestParam("profileImage") MultipartFile file, HttpSession session) {
	    System.out.println("Account locked/not locked: " + user.isAccountNonLocked());
	    
	    try {
	        if (!agreement) {
	            throw new TermsNotAgreedException("You have not agreed to the terms and conditions");
	        }

	        // File upload logic
	        if (file.isEmpty()) {
	            user.setImage_Url("default.jpg");
	        } else {
	            user.setImage_Url(file.getOriginalFilename());
	            // Implement file save logic here
	        }

	        // Validate captcha
	        boolean isCaptchaValid = EMSMAIN.captchaValidateMap.entrySet().stream()
	                .anyMatch(entry -> entry.getKey().equals(user.getCaptcha()));
	        if (!isCaptchaValid) {
	            throw new InvalidCaptchaException("Invalid Captcha");
	        }

	        // Call the register method and handle any thrown exceptions
	        servicelayer.register(user);
	        session.setAttribute("message", new Message("Successfully Registered", "alert-success"));
	        return "signup";

	    } catch (UserAlreadyExistsException e) {
	        session.setAttribute("message", new Message("Registration failed: " + e.getMessage(), "alert-danger"));
	    } catch (InvalidCaptchaException e) {
	        session.setAttribute("message", new Message("Captcha validation failed: " + e.getMessage(), "alert-danger"));
	    } catch (TermsNotAgreedException e) {
	        session.setAttribute("message", new Message("Terms not agreed: " + e.getMessage(), "alert-danger"));
	    } catch (Exception e) {
	        session.setAttribute("message", new Message("An error occurred: " + e.getMessage(), "alert-danger"));
	        e.printStackTrace();
	    }
	    // Reset the captcha and return to the signup page
	    getCaptcha(user);
	    String Captcha_Created = user.getHidden();
	    EMSMAIN.captchaValidateMap.put(Captcha_Created, new Date());
	    session.setAttribute("hiddenCaptcha", user.getHidden());
	    return "signup";
	}


	@GetMapping("/signin")
	public String homee(@ModelAttribute User user, Model model, HttpSession session,
			UserLoginDateTime userLoginDateTime,
			@RequestParam(value = "expiredsession", defaultValue = "false") boolean expiredsession,
			HttpServletResponse response, HttpServletRequest request) {
		try {
			System.out.println("hi"+user.getId());
			session = request.getSession();
//			EMSMAIN.session_map_data.put(session.getId(), new Date());
			if (expiredsession) {
				 servicelayer.update_login_dao(user);
//				System.out.println(new Date() + " Expired Time");
//				session.setAttribute("message", new Message("Your session has expired. Please log in again.", "alert-warning"));
				   return "redirect:/signin?expired=true";
			}
			System.out.println(")))))) " + session.getAttribute("messge"));
			getCaptcha(user);
			EMSMAIN.loginCaptcha.put(user.getHidden(), new Date());
			return "signin";
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = AdminController.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//			session.setAttribute("message", new Message("Please Diconnect VPN Because VPN may Interrupt application !!", "alert-danger"));
			return "signin";
//			}

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
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = Homecontroller.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

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
			System.out.println("---------" + user);
			if (user.isPresent()) {
				System.out.println("email " + email);
				model.addAttribute("title", "Send OTP");
				int otp = (int) (Math.random() * 9000) + 1000;
//			EMSMAIN.captchaValidateMap.put(otp, new Date());
				System.out.println("OTP " + otp);
//            boolean res= VPNChecker.vpnchecker();
//            if(res)
//            {
//            	throw new Exception();
//            }
				EMSMAIN.forgot_password_email_sent.put(email, otp);
				EMSMAIN.otpValidateMap.put(otp, new Date());
				String subject = "Forgot Email OTP Verification";
				String message = "" +
					    "<!DOCTYPE html>" +
					    "<html lang='en'>" +
					    "<head>" +
					    "    <meta charset='UTF-8'>" +
					    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
					    "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" +
					    "    <style>" +
					    "        body { font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; }" +
					    "        .wrapper { width: 100%; background-color: #f9f9f9; padding: 40px 0; }" +
					    "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
					    "        .header { background-color: #4CAF50; padding: 20px; text-align: center; color: #ffffff; border-top-left-radius: 12px; border-top-right-radius: 12px; }" +
					    "        .header h1 { margin: 0; font-size: 26px; }" +
					    "        .content { padding: 30px; text-align: center; }" +
					    "        .content h2 { font-size: 22px; color: #333; margin-bottom: 10px; }" +
					    "        .otp { font-size: 40px; font-weight: bold; letter-spacing: 8px; color: #4CAF50; background-color: #f0f8f0; padding: 15px; border-radius: 8px; margin: 20px 0; display: inline-block; }" +
					    "        .info { font-size: 16px; color: #666; margin-top: 10px; }" +
					    "        .footer { padding: 20px; text-align: center; font-size: 12px; color: #888; background-color: #f1f1f1; border-bottom-left-radius: 12px; border-bottom-right-radius: 12px; }" +
					    "        .footer a { color: #4CAF50; text-decoration: none; }" +
					    "    </style>" +
					    "</head>" +
					    "<body>" +
					    "    <div class='wrapper'>" +
					    "        <table class='container' align='center'>" +
					    "            <tr>" +
					    "                <td class='header'>" +
					    "                    <h1>Your OTP Code For Forgot Password</h1>" +
					    "                </td>" +
					    "            </tr>" +
					    "            <tr>" +
					    "                <td class='content'>" +
					    "                    <h2>Use the following OTP to complete your action:</h2>" +
					    "                    <div class='otp'>" + otp + "</div>" +  // Dynamic OTP inserted here
					    "                    <p class='info'>This OTP is valid for the next <strong>10 minutes</strong>. Please do not share it with anyone.</p>" +
					    "                </td>" +
					    "            </tr>" +
					    "            <tr>" +
					    "                <td class='footer'>" +
					    "                    <p>If you didn’t request this, please ignore this email. Need help? <a href='[Support Link]'>Contact Support</a>.</p>" +
					    "                </td>" +
					    "            </tr>" +
					    "        </table>" +
					    "    </div>" +
					    "</body>" +
					    "</html>";


				 CompletableFuture<Boolean> flagFuture = this.emailService.sendEmail(message, subject, email);
				// This will block until the result is available
				    try {
				        Boolean flag = flagFuture.get(); // Blocking call to get the result
				        if (flag) {
				            System.out.println(true);
				        } else {
				           System.out.println(false);
				        }
				    } catch (Exception e) {
				        e.printStackTrace();
				    }
					session.setAttribute("myotp", otp);
					session.setAttribute("email", email);
					System.out.println("email is   " + email);
					System.out.println("otp is   " + otp);
					return "verify_otp";
			} else {
				session.setAttribute("message", "User is Not Registered , Please Signup User Details");
				return "forgot_email_form";
			}
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = Homecontroller.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

			return "redirect:/swr";
		}

	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session, Model model) {
		try {
			model.addAttribute("title", "Verify OTP");
			Integer myOtp = (Integer) session.getAttribute("myotp");
			System.out.println(" user otp" + otp);
			System.out.println(" our otp" + myOtp);
			String email = (String) session.getAttribute("email");
			System.out.println("emailll " + email);
			if (myOtp == otp) {
				Optional<User> user = this.userdao.findByEmail(email);
				if (user == null) {
					session.setAttribute("message", "User does not exist !!");
					return "forgot_email_form";
				} 
				return "password_change_form";
			} else {
				session.setAttribute("message", "You Have Entered Wrong OTP");
				return "verify_otp";
			}
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = Homecontroller.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

			return "redirect:/swr";
		}
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,
			@RequestParam("newconfirmpassword") String newconfirmpassword, HttpSession session) {
		try {
			String email = (String) session.getAttribute("email");
			System.out.println(email);
			Optional<User> user = userdao.findByEmail(email);

			if (newpassword.equals(newconfirmpassword)) {
				User user2 = user.get();
				System.out.println(user2.getPassword());
				if (this.bCryptPasswordEncoder.matches(newpassword, user2.getPassword())) {

					session.setAttribute("message", "Old Password And New Password Same");
					return "password_change_form";

				} else {

					System.out.println(user2.getPassword());
					user2.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
					this.userdao.save(user2);
					session.setAttribute("message",new Message("Password Changed Successfully",	"alert-success"));
					return "signin";

				}
			} else {
				session.setAttribute("message", "Password Mismatch , Please Enter Same Password In Both Field");
				return "password_change_form";
			}
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = Homecontroller.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

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
}
