package com.ayush.ems.controller;

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
import org.springframework.data.domain.Page;
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

import com.ayush.ems.dao.AdminDao;
import com.ayush.ems.dao.TeamDao;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.dao.UserDetailDao;
import com.ayush.ems.entities.Admin;
import com.ayush.ems.entities.Error_Log;
import com.ayush.ems.entities.Performance;
import com.ayush.ems.entities.Team;
import com.ayush.ems.entities.User;
import com.ayush.ems.entities.UserDetail;
import com.ayush.ems.helper.Message;
import com.ayush.ems.service.EmailService;
import com.ayush.ems.service.SeperationEmailService;
import com.ayush.ems.service.Servicelayer;
import com.ayush.ems.service.TeamEmailService;

@Controller
@RequestMapping("/manager")
//@Scope(value = WebApplicationContext.SCOPE_SESSION)
@SessionScope
public class ManagerController {
	@Autowired
	private UserDao userdao;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private Servicelayer servicelayer;
	@Autowired
	private UserDetailDao userDetailDao;
	@Autowired
	private TeamDao teamdao;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private EmailService emailService;
	@Autowired
	private SeperationEmailService emailService1;
	@Autowired
	private TeamEmailService teamEmailService;

	@ModelAttribute
	public void commonData(Model model, Principal principal) {
		try {
			if (principal.equals(null)) {
				throw new Exception();
			} else {
				System.out.println(">>>>>>>>>>>>> " + principal);
				String userName = principal.getName();
				System.out.println("username " + userName);
				Optional<User> user = userdao.findByEmail(userName);
				User user1 = user.get();
				System.out.println("user " + user1);
				model.addAttribute("user", user1);
			}
		} catch (Exception e) {
			Redirect("/manager/swrr");
		}

	}

	private void Redirect(String string) {
		// TODO Auto-generated method stub

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
	            Optional<User> currentUser = this.userdao.findByEmail(username);
	            User user1 = currentUser.get();
	            servicelayer.login_record_save(user1, session, clientIp, location);
	            count++;
	        }
	        return "home2";
	    } catch (Exception e) {
	        System.out.println("ERRRRRRRRRRRRR " + e + " " + count);

	        String exceptionAsString = e.toString();
	        Class<?> currentClass = ManagerController.class;
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
//	private String getClientIpAddress(HttpServletRequest request) {
//	    String xfHeader = request.getHeader("X-Forwarded-For");
//	    if (xfHeader == null || xfHeader.isEmpty()) {
//	        return request.getRemoteAddr();
//	    }
//	    return xfHeader.split(",")[0];
//	}
	

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


	@GetMapping("/swrr")
	public String swr() {
		return "SomethingWentWrong";
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
		return "mperformance";
	}

	@GetMapping("/employee_leave_policy")
	public String Employee_Leave_Policy() {
		return "employeeleavepolicy4";
	}

//	List<UserDetail> all_users = new ArrayList<>();

//	@GetMapping("/viewMembers")
//	public String Employee(Model model, User user) {
//		try {
//			if (all_users != null && user.getUsername() != null) {
//				all_users = userDetailDao.findAllEnabledUser();
//				System.out.println("find all " + all_users);
//				model.addAttribute("all_users", all_users);
//				System.out.println("IN");
//				System.out.println("IN");
//				return "ViewMembers3";
//			} else {
//				throw new Exception();
//			}
//		}catch (Exception e) {
////			return "SomethingWentWrong";
////			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
//			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
////			String exString=e.toString();
////			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////			{
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = ManagerController.class;
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
//	@GetMapping("/employees")
//    public String getAllEmployees(@RequestParam(name = "sort", required = false) String sort, Model model) {
//        
//		all_users = userDetailDao.findAll();
//        if ("az".equals(sort)) {
//        	all_users.sort((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()));
//        } else if ("za".equals(sort)) {
//        	all_users.sort((a, b) -> b.getUsername().compareToIgnoreCase(a.getUsername()));
//        }
//
//        model.addAttribute("all_users", all_users);
//        return "ViewMembers3";
//    }
	
	List<UserDetail> all_users = new ArrayList<>();

	@GetMapping("/viewMembers/{page}")
	public String viewTeamMembers(
	        Model model, 
	        User user, 
	        Principal principal,
	        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
	        @RequestParam(name = "sort", required = false) String sort, // Get sorting param here
	        @PathVariable("page") int page) {
	    
	    try {
	        System.out.println(" all_users_size " + all_users.size());

	        // Assuming all_users gets populated here by fetching enabled users
	        all_users = userDetailDao.findAllEnabledUser(user.getCompany_id());
	        
	        System.out.println(" all_users_size " + all_users.size());

	        if (all_users != null && user.getUsername() != null) {
	            
	            // Sort the list based on user input
	        	if ("az".equals(sort)) {
	        	    all_users.sort((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()));
	        	} else if ("za".equals(sort)) {
	        	    all_users.sort((a, b) -> b.getUsername().compareToIgnoreCase(a.getUsername()));
	        	}


	            // Create a paginated version of the sorted list
	            Page<UserDetail> user_page = servicelayer.findPaginated(all_users, page, pageSize);

	            // Add the sorted and paginated data to the model
	            model.addAttribute("all_inbound_records", user_page);
	            model.addAttribute("currentPage", page);
	            model.addAttribute("totalPages", user_page.getTotalPages());
	            model.addAttribute("selectedPageSize", pageSize); // For Thymeleaf page size selection
	            model.addAttribute("pageSizes", List.of(10, 20, 50, 100, 500)); // Possible page sizes
	            model.addAttribute("sort", sort); // Pass sorting parameter to maintain state on frontend

	            System.out.println("IN");
	            return "ViewMembers3";
	        } else {
	            throw new Exception();
	        }
	    } catch (Exception e) {
	        System.out.println("ERRRRRRRRRRRRR " + e);
	        
	        // Exception handling and logging
	        String exceptionAsString = e.toString();
	        Class<?> currentClass = AdminController.class;
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

	
	@GetMapping("/emp_profile_edit_1/{id}")
	public String profile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile3.0";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = ManagerController.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
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
				List<Team> teams= teamdao.findAll();
				model.addAttribute("teams", teams);
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile3";
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
			Class<?> currentClass = ManagerController.class;

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

//	@GetMapping("/profile/{id}")
//	public String profile(@PathVariable("id") Integer id, Model model) {
//		System.out.println("IN");
//		Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
//		UserDetail userDetail = userOptional.get();
//		model.addAttribute("userdetail", userDetail);
//		model.addAttribute("title", "update form - " + userDetail.getUsername());
//		return "profile1";
//	}

	@GetMapping("/manager_profile_edit_1/{id}")
	public String yourProfileeeee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findById(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile3";
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
			Class<?> currentClass = ManagerController.class;

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

	@GetMapping("/manager_profile_edit_2/{id}")
	public String yourProfileee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findById(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile3.0";
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
			Class<?> currentClass = ManagerController.class;

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

	@GetMapping("/ChangeCurrentPassword")
	public String changepassword() {
		return "ChangeCurrentPasswordManager";
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
					return "redirect:/manager/ChangeCurrentPassword";
				} else {
					String emaill = user.getEmail();
					boolean res = servicelayer.saveNewPassword(newconfirmpassword, emaill);
					if (res == true) {
						session.setAttribute("message", new Message("Password Successfully Updated", "alert-success"));
						return "redirect:/manager/ChangeCurrentPassword";
					} else {
						session.setAttribute("message",
								new Message("Password Not Updated Due To Something Went Wrong!!", "alert-danger"));
						return "redirect:/manager/ChangeCurrentPassword";
					}
				}
			} else {
				session.setAttribute("message",
						new Message("NewPassword And NewConfirmPassword Not Match!!", "alert-danger"));
				return "redirect:/manager/ChangeCurrentPassword";
			}

		} else {
			session.setAttribute("message",
					new Message("Current password Not Match As Per Your Entered Input!!", "alert-danger"));
			return "redirect:/manager/ChangeCurrentPassword";
		}

	}

	@PostMapping("/update_emp_Profile/{id}")
	public String AssignTeam(UserDetail userDetail, Model model, HttpSession session) {
		try {
			int id = userDetail.getId();
			String email = null;
			String username = null;
			String team_desc = null;
			String team_id = null;
			String input_team_by_manager = userDetail.getTeam();
			System.out.println("USERDETAIL " + input_team_by_manager);
			Optional<UserDetail> userDetail2 = userDetailDao.findById(userDetail.getId());
			UserDetail userDetail3 = userDetail2.get();
			String user_team_check = userDetail3.getTeam();
			String team_validate_by_db = teamdao.getAllDataFromTeamDescription(input_team_by_manager);
			if (team_validate_by_db != null) {
				System.out.println(" team is valid or not " + team_validate_by_db);
				String[] string_array = team_validate_by_db.split(",");
				team_id = string_array[0];
				team_desc = string_array[1];
				System.out.println(team_id + " TEAM INFO " + team_desc);
				if (input_team_by_manager.equals(team_id) && input_team_by_manager.equals(user_team_check)) {
					session.setAttribute("message", new Message(" Same Team Cannot Be Reassigned!!", "alert-danger"));
				}
				if(input_team_by_manager.equals("0") && input_team_by_manager.equals(team_id))
				{
					username = userDetail3.getUsername();
					email = userDetail3.getEmail();
					System.out.println("EMAIL " + email);
					System.out.println("TEAMID " + team_id);
					System.out.println("USERNAME " + username);
					System.out.println("TEAMDESC " + team_desc);
//					EMSMAIN.id_with_email.put(id, email);
//					EMSMAIN.id_with_team_id.put(id, team_id);
//					EMSMAIN.id_with_username.put(id, username);
//					EMSMAIN.id_with_team_desc.put(id, team_desc);
					servicelayer.update_team_by_team_id(userDetail3, team_id, team_desc);
//					String teamDescwithid = teamdao.getAllDataFromTeamDescription(team_id);
					String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear " + username
							+ "<br>" + "<br>" + "You have removed from team , " + "<b>" + team_id + " -> " + team_desc
							+ "</b>" + " on "+ "<b>"+ new Date() +"</b>" + "<br><br>"
							+ "Resource Management Team" + "</p>" + "</div>";
					String subject = "Employee EMPID" + id + " Team Assigned";
					  CompletableFuture<Boolean> flagFuture = this.teamEmailService.sendEmail(message, subject, email);
					  Boolean flag = flagFuture.get(); // Blocking call to get the result
				        if (flag) {
				           System.out.println(true);
				        } else {
				            System.out.println(false);
				        }
				}
				if (input_team_by_manager.startsWith("EMS") && input_team_by_manager.length() == 9
						&& input_team_by_manager.equals(team_id)) {
					username = userDetail3.getUsername();
					email = userDetail3.getEmail();
					System.out.println("EMAIL " + email);
					System.out.println("TEAMID " + team_id);
					System.out.println("USERNAME " + username);
					System.out.println("TEAMDESC " + team_desc);
//					EMSMAIN.id_with_email.put(id, email);
//					EMSMAIN.id_with_team_id.put(id, team_id);
//					EMSMAIN.id_with_username.put(id, username);
//					EMSMAIN.id_with_team_desc.put(id, team_desc);
					servicelayer.update_team_by_team_id(userDetail3, team_id, team_desc);
//					String teamDescwithid = teamdao.getAllDataFromTeamDescription(team_id);
					String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<p>" + "Dear " + username
							+ "<br>" + "<br>" + "Your Team Changed and your new team is " + "<b>" + team_id + " -> " + team_desc
							+ "</b>" + ",You will get mail from your manager within 2 working days." + "<br><br>"
							+ "Resource Management Team" + "</p>" + "</div>";
					String subject = "Employee EMPID" + id + " Team Assigned";
					  CompletableFuture<Boolean> flagFuture = this.emailService.sendEmail(message, subject, email);
					  Boolean flag = flagFuture.get(); // Blocking call to get the result
				        if (flag) {
				           System.out.println(true);
				        } else {
				            System.out.println(false);
				        }
					session.setAttribute("message",
							new Message("Employee Details Successfully Updated !!", "alert-success"));
				}
			} else {
				session.setAttribute("message", new Message("Team ID is not valid !!", "alert-danger"));
			}
		} catch (Exception e) {
			if (e.getMessage().equals("No value present")) {
				session.setAttribute("message",
						new Message("Something went wrong !! : Admin Not Registered", "alert-danger"));
			} else {
				session.setAttribute("message",
						new Message("Something went wrong !! : " + e.getMessage(), "alert-danger"));
			}
		}
		return "redirect:/manager/emp_profile_edit_1/" + userDetail.getId();
	}

	@GetMapping("/seperation")
	public String Seperation() {
		return "Seperation";
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
			String adminId = user1.getAdmin_id();
			int typeCastAdminId = Integer.parseInt(adminId);
			Optional<Admin> admin = adminDao.findById(typeCastAdminId);
			Admin admin1 = admin.get();
			String cc = admin1.getEmail();
//			EMSMAIN.id_with_email.put(user1.getId(), to);
//			EMSMAIN.id_with_cc.put(user1.getId(), cc);
//			EMSMAIN.id_with_last_working_day_date.put(user1.getId(), lastdate);
//			EMSMAIN.id_with_username.put(user1.getId(), username);
//			servicelayer.sentMessage2(to, subject, username, lastdate, cc,id);
			String subject = "Seperation Request EMPID: EMPID" + user1.getId();
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
			return "Seperation";
		} else {
			lastdate = user1.getLastWorkingDay();
			session.setAttribute("message", new Message(
					"Sorry!! You have already applied speration request and your last working day is " + lastdate,
					"alert-danger"));
			return "Seperation";
		}
	}

	@GetMapping("/assetpolicy")
	public String AssetPolicy() {
		return "assetpolicy";
	}

	@GetMapping("/profile1/{id}")
	public String yourProfile(@PathVariable("id") Integer id, Model model) {
		System.out.println("IN");
		Optional<User> userOptional = this.userdao.findById(id);
		User userDetail = userOptional.get();
		model.addAttribute("userdetail", userDetail);
		model.addAttribute("title", "update form - " + userDetail.getUsername());
		return "emp_profile2";
	}

	@GetMapping("/viewTeamMembersOnly")
	public String ViewTeamMembers(User user, Model model) {
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
			return "ManagerTeamMembers";
		} catch (Exception e) {
//			return "redirect: /manager/swr";
			return "redirect:/signin?expiredsession=true";
		}
	}

	@RequestMapping("/teamprofile/{id}")
	public String teamprofile(@PathVariable("id") Integer id, Model model,Principal principal) {
		System.out.println("IN");
		Optional<User> get_user=this.userdao.findByEmail(principal.getName());
		 User get_user1=get_user.get();
		Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
		UserDetail userDetail = userOptional.get();
		model.addAttribute("userdetail", userDetail);
		model.addAttribute("get_user",get_user1);
		model.addAttribute("title", "update form - " + userDetail.getUsername());
//		if(userDetail.getRole().equals("ROLE_MANAGER"))
//		{
		return "ManagerTeamViewProfile";
//		}
//		else
//		{
//			return "ManagerNormalRoleViewProfile";
//		}
	}

	@RequestMapping("/termination/{id}")
	public String Termination(@PathVariable("id") Integer id, HttpSession session, Principal principal) {
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
//			String subject = "Google : Seperation Request EMPID: GOOGLEIN" + user1.getId();
//			String username = user1.getUsername();
//			String to = user1.getEmail();
//			int find = user1.getAaid();
//			Optional<Admin> admin = adminDao.findById(find);
//			Admin admin1 = admin.get();
//			String cc = admin1.getEmail();
//			servicelayer.sentMessage4(to, subject, username, lastdate, cc);
			System.out.println("?????????????" + user1.getId());
			return "redirect:/manager/teamprofile/" + user1.getId();
		} else {
			lastdate = user1.getLastWorkingDay();
			session.setAttribute("message", new Message(
					"Sorry!! You have already applied speration request and your last working day is " + lastdate,
					"alert-danger"));
			return "redirect:/manager/teamprofile/" + user1.getId();
		}
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
//					return "redirect:/manager/manager_profile_edit_1/" + user.getId();
//				}
//			}
//			if (file1.isEmpty()) {
//				user.setResume_file_url("NA");
//				servicelayer.update_profile(user);
//				session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
////            return "File uploaded successfully";
//				return "redirect:/manager/manager_profile_edit_1/" + user.getId();
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
//						return "redirect:/manager/manager_profile_edit_1/" + user.getId();
//					} else {
//						session.setAttribute("message",
//								new Message(
//										"Alert !! Profile Not Updated Because Resume Extension Should Be in PDF/WORD",
//										"alert-danger"));
//						return "redirect:/manager/manager_profile_edit_1/" + user.getId();
//					}
//				} else {
//					session.setAttribute("message",
//							new Message("Alert !! Profile Not Updated Because Resume size Should Be Less Than 3MB",
//									"alert-danger"));
//					return "redirect:/manager/manager_profile_edit_1/" + user.getId();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = ITcontroller.class;
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
//			return "redirect:/manager/manager_profile_edit_1/" + user.getId();
//		}
//	}
//}
	
	
	
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
	                return "redirect:/manger/manager_profile_edit_1/" + user.getId();
	            }
	        }

	        // Handle resume upload
	        if (file1.isEmpty()) {
	            user.setResume_file_url("NA");
	            user.setEditdate(new Date());
	            servicelayer.update_profile(user);
	            session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
	            return "redirect:/manager/manager_profile_edit_1/" + user.getId();
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
	                    return "redirect:/manager/manager_profile_edit_1/" + user.getId();
	                } else {
	                    session.setAttribute("message",
	                            new Message(
	                                    "Alert !! Profile Not Updated Because Resume Extension Should Be in PDF/WORD",
	                                    "alert-danger"));
	                    return "redirect:/manager/manager_profile_edit_1/" + user.getId();
	                }
	            } else {
	                session.setAttribute("message",
	                        new Message("Alert !! Profile Not Updated Because Resume size Should Be Less Than 3MB",
	                                "alert-danger"));
	                return "redirect:/manager/manager_profile_edit_1/" + user.getId();
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
	        return "redirect:/manager/manager_profile_edit_1/" + user.getId();
	    }
	}
}
