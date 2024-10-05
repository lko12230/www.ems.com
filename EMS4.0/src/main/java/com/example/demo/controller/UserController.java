package com.example.demo.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.UserDetailDao;
import com.example.demo.dao.Userdao;
import com.example.demo.dao.adminDao;
import com.example.demo.entities.Admin;
import com.example.demo.entities.Error_Log;
import com.example.demo.entities.Performance;
import com.example.demo.entities.User;
import com.example.demo.entities.UserDetail;
import com.example.demo.helper.Message;
import com.example.demo.service.SeperationEmailService;
import com.example.demo.service.servicelayer;

@Controller
@RequestMapping("/user")
@SessionScope
public class UserController {
	@Autowired
	private Userdao userdao;
	@Autowired
	private servicelayer servicelayer;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserDetailDao userDetailDao;
	@Autowired
	private adminDao adminDao;
	@Autowired
	private SeperationEmailService emailService1;
//	@ModelAttribute
//	public void commonData(Model model, Principal principal) {
//		String userName = principal.getName();
//		System.out.println("username " + userName);
//		User user = userdao.findByUserName(userName);
//		System.out.println("user " + user);
//		model.addAttribute("user", user);
//
//	}

	@GetMapping("/ChangeCurrentPassword")
	public String changepassword() {
		return "ChangeCurrentPassword";
	}

//	@GetMapping("/logout")
//	public String logout() {
//		// Perform logout logic if needed
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		if (auth != null) {
//			SecurityContextHolder.getContext().setAuthentication(null);
//		}
//		return "redirect:/signin?logout";
//	}

	@GetMapping("/employee_leave_policy")
	public String Employee_Leave_Policy() {
		return "employeeleavepolicy";
	}

	@ModelAttribute
	public void commonData(Model model, Principal principal) {
		try {
			if (principal.equals(null)) {
				throw new Exception();
			} else {
				System.out.println(">>>>>>>>>>>>> " + principal);
				String userName = principal.getName();
				System.out.println("username " + userName);
				Optional<User> user = userdao.findByUserName(userName);
				User user1 = user.get();
				System.out.println("user " + user1);
				model.addAttribute("user", user1);
			}
		} catch (Exception e) {
			Redirect("/user/swrr");
		}

	}
	
	int count = 0;

	@GetMapping("/new")
	public String homeee(User user, UserDetail userDetail, Error_Log error_Log, Principal principal,
	        Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
	    Calendar calendar = Calendar.getInstance();
	    int currentYear = calendar.get(Calendar.YEAR);
	    System.out.println("++++++++++++++ " + currentYear);
	    try {
	        if (principal == null) {
	            throw new Exception("session_invalid_exception");
	        }
	        if (user.getFailedAttempt() > 0) {
	            user.setFailedAttempt(0);
	        }
	        if (count == 0) {
	            // Capture client IP address
	            String clientIp = getClientIpAddress(request);

	            // Fetch location based on IP address
	            String location = getLocationFromIp(clientIp);

	            String username = principal.getName();
	            System.out.println(user.getFailedAttempt() + " USER EMAIL " + user.getEmail());
	            Optional<User> currentUser = this.userdao.findByUserName(username);
	            User user1 = currentUser.get();
	            servicelayer.login_record_save(user1, session, clientIp, location);
	            count++;
	        }
	        return "home";
	    } catch (Exception e) {
	        System.out.println("ERRRRRRRRRRRRR " + e + " " + count);

	        String exceptionAsString = e.toString();
	        Class<?> currentClass = UserController.class;
	        String className = currentClass.getName();
	        String errorMessage = e.getMessage();
	        StackTraceElement[] stackTrace = e.getStackTrace();
	        String methodName = stackTrace[0].getMethodName();
	        int lineNumber = stackTrace[0].getLineNumber();
	        System.out.println("METHOD NAME " + methodName + " " + lineNumber);
	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

	        return "redirect:/logout";
	    }
	}
	
	/**
	 * Get the client IP address from the request.
	 */
	private String getClientIpAddress(HttpServletRequest request) {
	    String xfHeader = request.getHeader("X-Forwarded-For");
	    if (xfHeader == null || xfHeader.isEmpty()) {
	        return request.getRemoteAddr();
	    }
	    return xfHeader.split(",")[0];
	}

	/**
	 * Get location information from IP address using a simple API.
	 * Replace this method with your API call.
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


	@GetMapping("/user_profile_edit_1/{id}")
	public String yourProfileeeee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findById(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile1";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//			return "SomethingWentWrong";
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = UserController.class;

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
			return "redirect:/logout";
//			}

		}
	}

	@GetMapping("/admin_profile_edit_2/{id}")
	public String yourProfileee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findById(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile1.0";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//			return "SomethingWentWrong";
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = UserController.class;

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
			return "redirect:/logout";
//			}

		}
	}

	@GetMapping("/performance")
	public String performance(Model model, User user, Performance performance, HttpSession httpSession,
			Principal principal) {
//		try
//		{
		if (principal.equals(null)) {
//				throw new Exception("session_invalid_exception");
		}
		List<Double> chartData = servicelayer.performance(user, httpSession);
		List<String> chartLabels = Arrays.asList("January", "February", "March", "April", "May", "June", "July",
				"August", "September", "October", "November", "December"); // Example labels
		int year = (int) httpSession.getAttribute("year");
		model.addAttribute("chartData", chartData);
		model.addAttribute("chartLabels", chartLabels);
		model.addAttribute("year", year);
		return "uperformance";
//		}
//		catch (Exception e) {
////			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
//			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
////			String exString=e.toString();
////			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////			{
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = UserController.class;
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
//			return "redirect:/logout";
////			}
//
//		}
	}

	private void Redirect(String string) {
		// TODO Auto-generated method stub

	}

	@PostMapping("/ChangeCurrentPassword")
	public String ChangePasswordRequest(User user, @RequestParam("currentPassword") String currentPassword,
			@RequestParam("newpassword") String newPassword,
			@RequestParam("newconfirmpassword") String newconfirmpassword, HttpSession session) {
		String oldpassword = user.getPassword();
		System.out.println(oldpassword);
		if (this.bCryptPasswordEncoder.matches(currentPassword, oldpassword)) {
			if (newPassword.equals(newconfirmpassword)) {
				if (this.bCryptPasswordEncoder.matches(newconfirmpassword, oldpassword)) {
					session.setAttribute("message",
							new Message("OldPassword And NewPassword Cannot Be Same!!", "alert-danger"));
					return "redirect:/user/ChangeCurrentPassword";
				} else {
					String emaill = user.getEmail();
					boolean res = servicelayer.saveNewPassword(newconfirmpassword, emaill);
					if (res == true) {
						session.setAttribute("message", new Message("Password Successfully Updated", "alert-success"));
						return "redirect:/user/ChangeCurrentPassword";
					} else {
						session.setAttribute("message",
								new Message("Password Not Updated Due To Something Went Wrong!!", "alert-danger"));
						return "redirect:/user/ChangeCurrentPassword";
					}
				}
			} else {
				session.setAttribute("message",
						new Message("NewPassword And NewConfirmPassword Not Match!!", "alert-danger"));
				return "redirect:/user/ChangeCurrentPassword";
			}

		} else {
			session.setAttribute("message",
					new Message("Current password Not Match As Per Your Entered Input!!", "alert-danger"));
			return "redirect:/user/ChangeCurrentPassword";
		}

	}

	
	List<UserDetail> all_users = new ArrayList<>();

	@GetMapping("/viewMembers")
	public String viewTeamMembers(Model model, User user, Principal principal) {
		try {
			all_users = userDetailDao.findAllEnabledUser();
			if (all_users != null && user.getUsername() != null) {
				System.out.println("find all " + all_users);
				model.addAttribute("all_users", all_users);
				System.out.println("IN");
				return "ViewMembers";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//			return "SomethingWentWrong";
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = UserController.class;

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
			return "redirect:/logout";
//			}

		}
	}
	
	
	@GetMapping("/employees")
    public String getAllEmployees(@RequestParam(name = "sort", required = false) String sort, Model model) {
        
		all_users = userDetailDao.findAll();
        if ("az".equals(sort)) {
        	all_users.sort((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()));
        } else if ("za".equals(sort)) {
        	all_users.sort((a, b) -> b.getUsername().compareToIgnoreCase(a.getUsername()));
        }

        model.addAttribute("all_users", all_users);
        return "ViewMembers";
    }
	
	@GetMapping("/emp_profile_edit_1/{id}")
	public String profile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile1.0";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//			return "SomethingWentWrong";
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = UserController.class;

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
			return "redirect:/logout";
//			}

		}
	}

	@GetMapping("/emp_profile_edit_2/{id}")
	public String profilee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile1";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//			return "SomethingWentWrong";
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = UserController.class;

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
			return "redirect:/logout";
//			}

		}
	}

//	@GetMapping("/user_profile_edit_1/{id}")
//	public String yourProfile( @PathVariable("id") Integer id, Model model, Principal principal) {
//		try {
//			if (principal != null) {
//				System.out.println("IN");
//				Optional<User> userOptional = this.userdao.findById(id);
//				User userDetail = userOptional.get();
//				model.addAttribute("userdetail", userDetail);
//				model.addAttribute("title", "update form - " + userDetail.getUsername());
//				return "profile1";
//			} else {
//				throw new Exception();
//			}
//		} catch (Exception e) {
////			return "SomethingWentWrong";
////			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
//			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
////			String exString=e.toString();
////			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////			{
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = UserController.class;
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
//			return "redirect:/logout";
////			}
//
//		}
//	}
//	

	@GetMapping("/user_profile_edit_2/{id}")
	public String yourProfilee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findById(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile1.0";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//			return "SomethingWentWrong";
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = UserController.class;

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
			return "redirect:/logout";
//			}

		}
	}

//	@PostMapping("/updateProfile/{id}")
//	public String AssignTeam(UserDetail userDetail, @RequestParam("assignteam") String assignteam, Model model,
//			HttpSession session) {
//		try {
//			boolean teamValidation = false;
//			String s = null;
//			Optional<UserDetail> userOptional = this.userDetailDao.findById(userDetail.getId());
//			List<String> team = teamdao.getAllDataFromTeam();
//			userDetail = userOptional.get();
//			String initialvalue=userDetail.getTeam();
//			userDetail.setTeam(assignteam);
//			for (int i = 0; i < team.size(); i++) {
//				s = team.get(i);
//				System.out.println(" ********** " + s);
//				System.out.println("userdetail ---><>< " + userDetail.getTeam());
//				if (s.equals(userDetail.getTeam())) {
//					teamValidation = true;
//					break;
//				}
//
//			}
//			if (teamValidation == true) {
//				System.out.println("^^^^^^^^^^^^^^^^^^^^^ " + userOptional);
//				userDetailDao.save(userDetail);
//				String username = userDetail.getUsername();
//				String to = userDetail.getEmail();
//				String team_iid = userDetail.getTeam();
//				if(initialvalue.length()>1)
//				{
//				
//					String subject = "Google : Your Team Changed";
//					servicelayer.sentMessage1(to, subject, team_iid, username);
//					session.setAttribute("message",
//							new Message("Employee Details Successfully Updated !! Team Changed", "alert-success"));
//				}
//				else
//				{
//					
//					String subject = "Google : Onboard New Team";
//					servicelayer.sentMessage(to, subject, team_iid, username);
//					session.setAttribute("message",
//							new Message("Employee Details Successfully Updated !!", "alert-success"));
//				}
//			} else {
//				throw new Exception("Team Id Not Valid");
//			}
//		} catch (Exception e) {
//			if (e.getMessage().equals("No value present")) {
//				session.setAttribute("message",
//						new Message("Something went wrong !! : Admin Not Registered", "alert-danger"));
//			} else {
//				session.setAttribute("message",
//						new Message("Something went wrong !! : " + e.getMessage(), "alert-danger"));
//			}
//		}
//		return "redirect:/admin/profile/" + userDetail.getId();
//	}

	@GetMapping("/seperation")
	public String Seperation() {
		return "Seperation2";
	}

	@PostMapping("/seperation/{id}")
	public String Seperation(@PathVariable("id") Integer id, HttpSession session) {
		Optional<User> result2 = userdao.findById(id);
		User user1 = result2.get();
		System.out.println("{{{{{{{{{{{{{{{ " + user1);
		Date lastdate = user1.getLastWorkingDay();
		System.out.println("}}}}}}}}}}}}}}} " + lastdate);
		if (user1.getSperationDate() == null && user1.getLastWorkingDay() == null) {
			servicelayer.seperationLogic(user1.getId(), user1);
			lastdate = user1.getLastWorkingDay();
			System.out.println("}}}}}}}}}}}}}}} " + lastdate);
			session.setAttribute("message", new Message("Your last working day is " + lastdate, "alert-success"));
//			String username = user1.getUsername();
			String to = user1.getEmail();
			int find = user1.getAaid();
			Optional<Admin> admin = adminDao.findById(find);
			Admin admin1 = admin.get();
			String cc = admin1.getEmail();
//			EMSMAIN.id_with_email.put(user1.getId(), to);
//			EMSMAIN.id_with_cc.put(user1.getId(), cc);
//			EMSMAIN.id_with_last_working_day_date.put(user1.getId(), lastdate);
//			EMSMAIN.id_with_username.put(user1.getId(), username);
//			servicelayer.sentMessage2(to, subject, username, lastdate, cc);
			String subject = "Seperation Request EMPID: "+ user1.getId();
			String message = "" +
				    "<!DOCTYPE html>" +
				    "<html lang='en'>" +
				    "<head>" +
				    "    <meta charset='UTF-8'>" +
				    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
				    "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" +
				    "    <style>" +
				    "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }" +
				    "        .email-container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }" +
				    "        .header { background-color: #007BFF; padding: 20px; text-align: center; color: #ffffff; border-top-left-radius: 8px; border-top-right-radius: 8px; }" +
				    "        .header h1 { margin: 0; font-size: 24px; }" +
				    "        .content { padding: 30px; color: #333333; line-height: 1.6; }" +
				    "        .content p { font-size: 16px; margin: 0 0 20px 0; }" +
				    "        .content .highlight { font-weight: bold; color: #007BFF; }" +
				    "        .footer { padding: 20px; text-align: center; font-size: 12px; color: #888888; background-color: #f1f1f1; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; }" +
				    "    </style>" +
				    "</head>" +
				    "<body>" +
				    "    <div class='email-container'>" +
				    "        <div class='header'>" +
				    "            <h1>Resignation Accepted</h1>" +
				    "        </div>" +
				    "        <div class='content'>" +
				    "            <p>Dear " + user1.getUsername() + ",</p>" +
				    "            <p>Your resignation request has been accepted, and your last working day is <span class='highlight'>" + lastdate + "</span>.</p>" +
				    "            <p>We want to take this opportunity to thank you for your contributions and wish you all the best for your future endeavors.</p>" +
				    "            <p>Sincerely,</p>" +
				    "            <p><strong>HR Team</strong></p>" +
				    "        </div>" +
				    "        <div class='footer'>" +
				    "            <p>If you have any questions, feel free to reach out to us at any time.</p>" +
				    "        </div>" +
				    "    </div>" +
				    "</body>" +
				    "</html>";

			CompletableFuture<Boolean> flagFuture = this.emailService1.sendEmail(message, subject, to, cc);
		    
		    // This will block until the result is available
			try {
		        Boolean flag = flagFuture.get(); // Blocking call to get the result
		        if (flag) {
		           System.out.println(true);
		        } else {
		            System.out.println(false);
		        }
			}
			catch (Exception e) {
		        e.printStackTrace();
		       System.out.println("ERROR");
		    }
			return "Seperation2";
		} else {
			lastdate = user1.getLastWorkingDay();
			session.setAttribute("message", new Message(
					"Sorry!! You have already applied speration request and your last working day is " + lastdate,
					"alert-danger"));
			return "Seperation2";
		}
	}

	@RequestMapping("/emp_profile_view/{id}")
	public String profileee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile1.0";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//			return "SomethingWentWrong";
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = UserController.class;

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
			return "redirect:/logout";
//			}

		}
	}

	@GetMapping("/viewTeamMembersOnly")
	public String ViewTeamMembers(User user, Model model, Principal principal) {
		try {
			int id = user.getId();
			List<UserDetail> allDetails = servicelayer.findUserByTeam(id);
			for (int i = 0; i < allDetails.size(); i++) {
				System.out.println(allDetails.get(i));
			}
			System.out.println("find all " + allDetails);
			model.addAttribute("allDetails", allDetails);
			System.out.println("IN");
			System.out.println("IN Team");
			return "UserTeamMembers";
		} catch (Exception e) {
//		return "SomethingWentWrong";
//		String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//		String exString=e.toString();
//		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//		{
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = UserController.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//		return "SomethingWentWrong";)
//			return "redirect:/swr";
//		}
//		else
//		{
			return "redirect:/logout";
//		}

		}
	}

	@RequestMapping("/teamprofile/{id}")
	public String teamprofile(@PathVariable("id") Integer id, Model model,Principal principal) {
		System.out.println("IN");
		Optional<User> get_user=this.userdao.findByUserName(principal.getName());
		 User get_user1=get_user.get();
		Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
		UserDetail userDetail = userOptional.get();
		model.addAttribute("get_user",get_user1);
		model.addAttribute("userdetail", userDetail);
		model.addAttribute("title", "update form - " + userDetail.getUsername());
//		if (userDetail.getRole().equals("ROLE_IT")) {
		return "UserTeamViewProfile";
//		} else {
//			return "ManagerNormalRoleViewProfile";
//		}
	}

//	@PostMapping("/processing_profile/{id}")
//	public String yourProfileUpdate(@ModelAttribute("user") User user, @RequestParam("profileImage") MultipartFile file,
//			@RequestParam("resume") MultipartFile file1, HttpSession session) {
//		try {
//			System.out.println(" --------------- " + user.getDob() + " ---------- " + user.getBank_name());
//			servicelayer.update_profile(user);
//			if (file.isEmpty()) {
//				user.setImage_Url("default.jpg");
//			} else {
//				String contentType1 = file.getContentType();
//				System.out.println(file.getOriginalFilename());
//				if (contentType1.equals("image/jpeg") || contentType1.equals("image/jpg")
//						|| contentType1.equals("image/png")) {
//
//					user.setImage_Url(file.getOriginalFilename());
//					File savefile = new ClassPathResource("static/img").getFile();
//					System.out.println(savefile);
//					Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
//					System.out.println("PATH " + path);
//					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//					System.out.println(file.getOriginalFilename());
//					System.out.println("FILE UPLOAD SUCESS ");
//
//				} else {
//					session.setAttribute("message",
//							new Message(
//									"Alert !! Profile Not Updated Because Image Extension Should Be in JPG/JPEG/PNG",
//									"alert-danger"));
//					return "redirect:/user/user_profile_edit_1/" + user.getId();
//				}
//			}
//			if (file1.isEmpty()) {
//				user.setResume_file_url("NA");
//				servicelayer.update_profile(user);
//				session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
////            return "File uploaded successfully";
//				return "redirect:/user/user_profile_edit_1/" + user.getId();
//			} else {
//				System.out.println("FILE SIZE   " + file.getSize());
//				if (file1.getSize() < 3000000) {
//					// Check if file is PDF or Word document
//					String contentType = file1.getContentType();
//					if (contentType.equals("application/pdf") || contentType.equals("application/msword")) {
//						// File is either a PDF or Word document, process accordingly
//						// Your code here
//						System.out.println("File Is Uploaded And Custom Image Is Uploaded");
//						user.setResume_file_url(file1.getOriginalFilename());
//						File savefile = new ClassPathResource("static/resume").getFile();
//						Path path = Paths
//								.get(savefile.getAbsolutePath() + File.separator + file1.getOriginalFilename());
//						System.out.println("PATH " + path);
//						Files.copy(file1.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//						System.out.println(file1.getOriginalFilename());
//						System.out.println("FILE UPLOAD SUCESS ");
//
//						servicelayer.update_profile(user);
//						session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
////	                return "File uploaded successfully";
//						return "redirect:/user/user_profile_edit_1/" + user.getId();
//					} else {
//						session.setAttribute("message",
//								new Message(
//										"Alert !! Profile Not Updated Because Resume Extension Should Be in PDF/WORD",
//										"alert-danger"));
//						return "redirect:/user/user_profile_edit_1/" + user.getId();
//					}
//				} else {
//					session.setAttribute("message",
//							new Message("Alert !! Profile Not Updated Because Resume size Should Be Less Than 3MB",
//									"alert-danger"));
//					return "redirect:/user/user_profile_edit_1/" + user.getId();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = Homecontroller.class;
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
////			getCaptcha(user);
////			System.out.println(hiddenCaptcha);
////			String Captcha_Created=user.getHidden();
////			EMSMAIN.captcha_validate_map.put(Captcha_Created, new Date());
//			servicelayer.AllIntanceVariableClear(user);
//			session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
//			return "redirect:/user/user_profile_edit_1/" + user.getId();
//		}
//	}

	
	@PostMapping("/processing_profile/{id}")
	public String yourProfileUpdate(
	        @ModelAttribute("user") User user, 
	        @RequestParam("profileImage") MultipartFile file,
	        @RequestParam("resume") MultipartFile file1, 
	        HttpSession session) {
	    try {
	        System.out.println("BANK " + user.getBank_account_holder_name() + " --------------- " + user.getDob() + " ---------- " + user.getBank_name());
	        servicelayer.update_profile(user);
	        if (user.getBank_account_holder_name().trim().isEmpty()) {
	            user.setBank_account_holder_name("NA");
	            user.setBank_name("NA");
	            user.setIfsc_code("NA");
	            user.setBank_account_number(0);
	        }
	        
	        // Handle profile image upload
	        if (file.isEmpty()) {
	            user.setImage_Url("default.jpg");
	        } else {
	            String contentType1 = file.getContentType();
	            System.out.println(file.getOriginalFilename());
	            if (contentType1.equals("image/jpeg") || contentType1.equals("image/jpg") || contentType1.equals("image/png")) {
	                user.setImage_Url(file.getOriginalFilename());
	                // Save the file to the upload directory
	                Path uploadPath = Paths.get(System.getProperty("user.dir") + "/uploads/img");
	                if (!Files.exists(uploadPath)) {
	                    Files.createDirectories(uploadPath);
	                }
	                Path path = uploadPath.resolve(file.getOriginalFilename());
	                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	                System.out.println(file.getOriginalFilename());
	                System.out.println("FILE UPLOAD SUCCESS");
	            } else {
	                session.setAttribute("message",
	                        new Message(
	                                "Alert !! Profile Not Updated Because Image Extension Should Be in JPG/JPEG/PNG",
	                                "alert-danger"));
	                return "redirect:/user/user_profile_edit_1/" + user.getId();
	            }
	        }

	        // Handle resume upload
	        if (file1.isEmpty()) {
	            user.setResume_file_url("NA");
	            user.setEditdate(new Date());
	            servicelayer.update_profile(user);
	            session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
	            return "redirect:/user/user_profile_edit_1/" + user.getId();
	        } else {
	            System.out.println("FILE SIZE " + file.getSize());
	            if (file1.getSize() < 3000000) {
	                String contentType = file1.getContentType();
	                if (contentType.equals("application/pdf") || contentType.equals("application/msword")) {
	                    System.out.println("File Is Uploaded And Custom Image Is Uploaded");
	                    user.setResume_file_url(file1.getOriginalFilename());
	                    // Save the file to the upload directory
	                    Path uploadPath = Paths.get(System.getProperty("user.dir") + "/uploads/resume");
	                    if (!Files.exists(uploadPath)) {
	                        Files.createDirectories(uploadPath);
	                    }
	                    Path path = uploadPath.resolve(file1.getOriginalFilename());
	                    Files.copy(file1.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	                    System.out.println(file1.getOriginalFilename());
	                    System.out.println("FILE UPLOAD SUCCESS");
	                    user.setEditdate(new Date());
	                    servicelayer.update_profile(user);
	                    session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
	                    return "redirect:/user/user_profile_edit_1/" + user.getId();
	                } else {
	                    session.setAttribute("message",
	                            new Message(
	                                    "Alert !! Profile Not Updated Because Resume Extension Should Be in PDF/WORD",
	                                    "alert-danger"));
	                    return "redirect:/user/user_profile_edit_1/" + user.getId();
	                }
	            } else {
	                session.setAttribute("message",
	                        new Message("Alert !! Profile Not Updated Because Resume size Should Be Less Than 3MB",
	                                "alert-danger"));
	                return "redirect:/user/user_profile_edit_1/" + user.getId();
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        String exceptionAsString = e.toString();
	        Class<?> currentClass = Homecontroller.class;
	        String className = currentClass.getName();
	        String errorMessage = e.getMessage();
	        StackTraceElement[] stackTrace = e.getStackTrace();
	        String methodName = stackTrace[0].getMethodName();
	        int lineNumber = stackTrace[0].getLineNumber();
	        System.out.println("METHOD NAME " + methodName + " " + lineNumber);
	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
	        servicelayer.AllIntanceVariableClear(user);
	        session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
	        return "redirect:/user/user_profile_edit_1/" + user.getId();
	    }
	}

	
	@GetMapping("/assetpolicy")
	public String AssetPolicy() {
		return "assetpolicy3";
	}

}
