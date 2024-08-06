package com.example.demo.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import java.util.Map;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.EMSMAIN;
import com.example.demo.dao.UserDetailDao;
import com.example.demo.dao.UserLoginDao;
import com.example.demo.dao.Userdao;
import com.example.demo.dao.adminDao;
import com.example.demo.dao.orderDao;
import com.example.demo.entities.Admin;
import com.example.demo.entities.CompanyInfo;
import com.example.demo.entities.Error_Log;
import com.example.demo.entities.Payment_Order_Info;
import com.example.demo.entities.Performance;
import com.example.demo.entities.SubscriptionPlans;
import com.example.demo.entities.User;
import com.example.demo.entities.UserDetail;
import com.example.demo.entities.UserLoginDateTime;
import com.example.demo.helper.Message;
import com.example.demo.service.LoginHistoryExportEmail;
import com.example.demo.service.PaymentSucessEmailService;
import com.example.demo.service.servicelayer;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import cn.apiclub.captcha.Captcha;

@Controller
@RequestMapping("/admin")
@SessionScope
public class AdminController {
	@Autowired
	private Userdao userdao;
	@Autowired
	private UserLoginDao userLoginDao;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private servicelayer servicelayer;
	@Autowired
	private UserDetailDao userDetailDao;
	@Autowired
	private adminDao adminDao;
	@Autowired
	private orderDao orderDao;
	@Autowired
	private LoginHistoryExportEmail loginHistoryExportEmail;

	@ModelAttribute
	public void commonData(Model model, Principal principal) {
		try {
			if (principal.equals(null)) {
				throw new Exception();
			} else {
				System.out.println(">>>>>>>>>>>>> " + principal);
				String userName = principal.getName();
				System.out.println("principal username " + userName);
				Optional<User> user = userdao.findByUserName(userName);
				User user1 = user.get();
				System.out.println("user " + user1);
				model.addAttribute("user", user1);
			}
		} catch (Exception e) {
			Redirect("/admin/swrr");
		}

	}

	private void Redirect(String string) {
		// TODO Auto-generated method stub

	}

//	@GetMapping("/logout")
//    public String logout() {
//        // Perform logout logic if needed
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null) {
//            SecurityContextHolder.getContext().setAuthentication(null);
//        }
//        return "redirect:/signin?logout";
//    }

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
	        
	        return "home1";
	    } catch (Exception e) {
	        System.out.println("ERRRRRRRRRRRRR " + e + " " + count);

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


	@GetMapping("/employee_leave_policy")
	public String Employee_Leave_Policy() {
		return "employeeleavepolicy2";
	}

	@GetMapping("/performance")
	public String performance(Model model, User user, Performance performance, HttpSession httpSession,
			Principal principal) {
		try {
			if (principal.equals(null)) {
				throw new Exception("session_invalid_exception");
			}
			List<Double> chartData = servicelayer.performance(user, httpSession);
			List<String> chartLabels = Arrays.asList("January", "February", "March", "April", "May", "June", "July",
					"August", "September", "October", "November", "December"); // Example labels
			int year = (int) httpSession.getAttribute("year");
			model.addAttribute("chartData", chartData);
			model.addAttribute("chartLabels", chartLabels);
			model.addAttribute("year", year);
			return "aperformance";
		} catch (Exception e) {
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
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
//			return "SomethingWentWrong";)
//				return "redirect:/swr";
//			}
//			else
//			{
			return "redirect:/logout";
//			}

		}
	}

	@GetMapping("/cal")
	public String cal() {
		return "calender";
	}

	List<UserDetail> all_users = new ArrayList<>();

	@GetMapping("/viewMembers")
	public String viewTeamMembers(Model model, User user, Principal principal) {
		try {
			all_users = userDetailDao.findAll();
			if (all_users != null && user.getUsername() != null) {
				System.out.println("find all " + all_users);
				model.addAttribute("all_users", all_users);
				System.out.println("IN");
				return "ViewMembers2";
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
			Class<?> currentClass = AdminController.class;

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
        return "ViewMembers2";
    }

	@GetMapping("/swrr")
	public String swr() {
		return "AdminSomethingWentWrong";
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
				return "emp_profile2.0";
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
			Class<?> currentClass = AdminController.class;

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
				return "emp_profile2";
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
			Class<?> currentClass = AdminController.class;

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

	@GetMapping("/emp_profile_edit_21/{id}")
	public String profileee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile2.1";
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
			Class<?> currentClass = AdminController.class;

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

	@GetMapping("/admin_profile_edit_1/{id}")
	public String yourProfile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findById(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile2";
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
			Class<?> currentClass = AdminController.class;

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
	public String yourProfilee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findById(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile2.0";
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
			Class<?> currentClass = AdminController.class;

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

	@PostMapping("/processing_profilee/{id}")
	public String yourProfileUpdatee(@PathVariable("id") int id, @ModelAttribute("userdetail") UserDetail userDetail,
	                                 Principal principal, HttpSession session) {
	    try {
	        String currentUser = null;
	        if (principal != null) {
	            currentUser = principal.getName();
	        }
	        System.out.println("TEST MODE");
	        System.out.println(userDetail.getLaptop_brand());
	        System.out.println(userDetail.getLaptop_id());
	        System.out.println(userDetail.getLaptop_serial_number());
	        System.out.println(userDetail.getBank_name());
	        System.out.println(userDetail.getBank_account_number());
	        System.out.println(userDetail.getBank_account_holder_name());
	        
	        servicelayer.emp_bank_profile_update(userDetail, currentUser);
	        
	        session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
	        return "redirect:/admin/emp_profile_edit_1/" + userDetail.getId();
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
	        
	        session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
	        return "redirect:/admin/emp_profile_edit_1/" + userDetail.getId();
	    }
	}
	
	
	@PostMapping("/emp_processing_profile/{id}")
	public String empProfileUpdate(@PathVariable("id") int getid, @ModelAttribute("userdetail") UserDetail userDetail,
			Principal principal, HttpSession session) throws Exception {
	
			String CurrentUser = null;
			if (principal != null) {
				CurrentUser = principal.getName();
			}
			System.out.println(userDetail.getLaptop_brand());
			System.out.println(userDetail.getLaptop_id());
			System.out.println(userDetail.getLaptop_serial_number());
			servicelayer.emp_update_profile(userDetail, CurrentUser);
			System.out.println("USERDETAIL ID ");
			session.setAttribute("message", new Message("Profile Updated !!", "alert-success"));
			 return "redirect:/admin/emp_profile_edit_1/" + userDetail.getId();
		} 
	
	
	@PostMapping("/processing_profileee/{id}")
	public String yourProfileUpdateee(@ModelAttribute("userdetail") UserDetail userDetail,
	                                 Principal principal, HttpSession session) {
	    try {
	        String currentUser = null;
	        if (principal != null) {
	            currentUser = principal.getName();
	        }
	        System.out.println("TEST MODE");
	        System.out.println(userDetail.getLaptop_brand());
	        System.out.println(userDetail.getLaptop_id());
	        System.out.println(userDetail.getLaptop_serial_number());
	        System.out.println(userDetail.getBank_name());
	        System.out.println(userDetail.getBank_account_number());
	        System.out.println(userDetail.getBank_account_holder_name());
	        
	        servicelayer.emp_update_profile(userDetail, currentUser);
	        
	        session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
	        return "redirect:/admin/emp_profile_edit_1/" + userDetail.getId();
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
	        
	        session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
	        return "redirect:/admin/emp_profile_edit_1/" + userDetail.getId();
	    }
	}
	
	
//	@PostMapping("/processing_profile/{id}")
//	public String yourProfileUpdate(@ModelAttribute("user") User user, @RequestParam("profileImage") MultipartFile file,
//			@RequestParam("resume") MultipartFile file1, HttpSession session) {
//		try {
//			System.out.println("BANK    "+user.getBank_account_holder_name()+" --------------- " + user.getDob() + " ---------- " + user.getBank_name());
//			servicelayer.update_profile(user);
//			if(user.getBank_account_holder_name().trim().isEmpty())
//			{
//	user.setBank_account_holder_name("NA");
//	user.setBank_name("NA");
//	user.setIfsc_code("NA");
//	user.setBank_account_number(0);
//	}
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
//					return "redirect:/admin/admin_profile_edit_1/" + user.getId();
//				}
//			}
//			if (file1.isEmpty()) {
//				user.setResume_file_url("NA");
//				user.setEditdate(new Date());
//				servicelayer.update_profile(user);
//				session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
////            return "File uploaded successfully";
//				return "redirect:/admin/admin_profile_edit_1/" + user.getId();
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
//						user.setEditdate(new Date());
//						servicelayer.update_profile(user);
//						session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
////	                return "File uploaded successfully";
//						return "redirect:/admin/admin_profile_edit_1/" + user.getId();
//					} else {
//						session.setAttribute("message",
//								new Message(
//										"Alert !! Profile Not Updated Because Resume Extension Should Be in PDF/WORD",
//										"alert-danger"));
//						return "redirect:/admin/admin_profile_edit_1/" + user.getId();
//					}
//				} else {
//					session.setAttribute("message",
//							new Message("Alert !! Profile Not Updated Because Resume size Should Be Less Than 3MB",
//									"alert-danger"));
//					return "redirect:/admin/admin_profile_edit_1/" + user.getId();
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
//			return "redirect:/admin/admin_profile_edit_1/" + user.getId();
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
	                return "redirect:/admin/admin_profile_edit_1/" + user.getId();
	            }
	        }

	        // Handle resume upload
	        if (file1.isEmpty()) {
	            user.setResume_file_url("NA");
	            user.setEditdate(new Date());
	            servicelayer.update_profile(user);
	            session.setAttribute("message", new Message("Success !! Profile Updated !!", "alert-success"));
	            return "redirect:/admin/admin_profile_edit_1/" + user.getId();
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
	                    return "redirect:/admin/admin_profile_edit_1/" + user.getId();
	                } else {
	                    session.setAttribute("message",
	                            new Message(
	                                    "Alert !! Profile Not Updated Because Resume Extension Should Be in PDF/WORD",
	                                    "alert-danger"));
	                    return "redirect:/admin/admin_profile_edit_1/" + user.getId();
	                }
	            } else {
	                session.setAttribute("message",
	                        new Message("Alert !! Profile Not Updated Because Resume size Should Be Less Than 3MB",
	                                "alert-danger"));
	                return "redirect:/admin/admin_profile_edit_1/" + user.getId();
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
	        return "redirect:/admin/admin_profile_edit_1/" + user.getId();
	    }
	}

	
	
	@PostMapping("/profile1/{id}")
	public String update_profile() {
		return null;
	}

	@GetMapping("/adminRegisterEmployee")
	public String adminRegisterEmp(Model model, User user, HttpSession session, Principal principal) {
		try {
			System.out.println("hi");
			model.addAttribute("title", "Microsoft Sign Up");
//		model.addAttribute("user",new User());
			getCaptcha(user);
			session.setAttribute("hiddenCaptcha", user.getHidden());
			System.out.println(user.getHidden());
			System.out.println("IN");
			return "EmployeeRegistration";
		} catch (Exception e) {
//			return "SomrthingWentWrong";
//			String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//			String exString=e.toString();
//			if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//			{
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
//			return "SomethingWentWrong";)
//				return "redirect:/swr";
//			}
//			else
//			{
			return "redirect:/logout";
//			}

		}
	}

	private void getCaptcha(User user) {
		Captcha captcha = com.example.demo.service.servicelayer.createCaptcha(250, 80);
		user.setHidden(captcha.getAnswer());
		user.setCaptcha("");
		user.setImageCaptcha(com.example.demo.service.servicelayer.encodeCaptcha(captcha));
		System.out.println("impoted " + user.getImageCaptcha());
	}

	@GetMapping("/show_all_employees")
	public String getAllEmployees(Principal principal) {
		try {
			return "show_employees";
		} catch (Exception e) {
//		return "SomrthingWentWrong";
//		String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//		String exString=e.toString();
//		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//		{
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
//		return "SomethingWentWrong";)
//			return "redirect:/swr";
//		}
//		else
//		{
			return "redirect:/logout";
//		}

		}
	}
//	@PostMapping("/admin-emp-register")
//	public String home(@Valid @ModelAttribute("user") User user,BindingResult result,@RequestParam(value="agreement",defaultValue = "false")boolean agreement,String password,String repassword ,String Captcha,String gender,String dob ,String email,String phone, HttpSession session)
//	{
//		System.out.println("hi "+Captcha);
//		String hiddenCaptcha=(String) session.getAttribute("hiddenCaptcha");
//		System.out.println("hi2 "+hiddenCaptcha);
//		System.out.println("hi1 "+user.getHidden());
//		System.out.println(user.getDob());
//		try
//		{
//			if(!agreement)
//			{
//				System.out.println("you have not agreed terms and conditions");
//			throw new Exception("you have not agreed terms and conditions");
//			}
//			
//		if(result.hasErrors())
//		{
//			
//		System.out.println(result);
//		}
//		if(Captcha.equals(hiddenCaptcha))
//		{
//		User result1=servicelayer.registerr(user);
//		System.out.println(result1);
//		if(result1==null)
//		{
//			throw new Exception("Password and Re-Password not match");
//		}
//		else
//		{
//		session.setAttribute("message",new Message("Successfully Registered", "alert-success"));
//		System.out.println("pass");
//		return "redirect:/admin/adminRegisterEmployee";
//		}
//		}
//		else
//		{
//			session.setAttribute("message",new Message("Wrong Captcha", "alert-danger"));
//
//			return "redirect:/admin/adminRegisterEmployee";
//		}
//		}
//catch (Exception e) {
//	e.printStackTrace();
//	getCaptcha(user);
//	System.out.println(hiddenCaptcha);
//	session.setAttribute("message", new Message("Something went wrong !! "+e.getMessage(), "alert-danger"));
//}
//		return "EmployeeRegistration";
//	}

	@GetMapping("/ChangeCurrentPassword")
	public String changepassword(Principal principal) {
		try {
			return "ChangeCurrentPasswordAdmin";
		} catch (Exception e) {
//		return "SomethingWentWrong";
//		String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//		String exString=e.toString();
//		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//		{
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
//		return "SomethingWentWrong";)
//			return "redirect:/swr";
//		}
//		else
//		{
			return "redirect:/logout";
//		}

		}
	}

	@PostMapping("/ChangeCurrentPassword")
	public String ChangePasswordRequest(User user, @RequestParam("currentPassword") String currentPassword,
			@RequestParam("newpassword") String newPassword,
			@RequestParam("newconfirmpassword") String newconfirmpassword, HttpSession session, Principal principal) {
		try {
			String oldpassword = user.getPassword();
			System.out.println(oldpassword);
			if (this.bCryptPasswordEncoder.matches(currentPassword, oldpassword)) {
				if (newPassword.equals(newconfirmpassword)) {
					if (this.bCryptPasswordEncoder.matches(newconfirmpassword, oldpassword)) {
						session.setAttribute("message",
								new Message("OldPassword And NewPassword Cannot Be Same!!", "alert-danger"));
						return "redirect:/admin/ChangeCurrentPassword";
					} else {
						String emaill = user.getEmail();
						boolean res = servicelayer.saveNewPassword(newconfirmpassword, emaill);
						if (res == true) {
							session.setAttribute("message",
									new Message("Password Successfully Updated", "alert-success"));
							return "redirect:/admin/ChangeCurrentPassword";
						} else {
							session.setAttribute("message",
									new Message("Password Not Updated Due To Something Went Wrong!!", "alert-danger"));
							return "redirect:/dadmin/ChangeCurrentPassword";
						}
					}
				} else {
					session.setAttribute("message",
							new Message("NewPassword And NewConfirmPassword Not Match!!", "alert-danger"));
					return "redirect:/admin/ChangeCurrentPassword";
				}

			} else {
				session.setAttribute("message",
						new Message("Current password Not Match As Per Your Entered Input!!", "alert-danger"));
				return "redirect:/admin/ChangeCurrentPassword";
			}
		} catch (Exception e) {
//		return "SomrthingWentWrong";
//		String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//		String exString=e.toString();
//		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//		{
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
//		return "SomethingWentWrong";)
//			return "redirect:/swr";
//		}
//		else
//		{
			return "redirect:/logout";
//		}

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
//			String initialvalue = userDetail.getTeam();
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
//				if (initialvalue.length() > 1) {
//
//					String subject = "Google : Your Team Changed";
//					servicelayer.sentMessage1(to, subject, team_iid, username);
//					session.setAttribute("message",
//							new Message("Employee Details Successfully Updated !! Team Changed", "alert-success"));
//				} else {
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
	public String Seperation(Principal principal) {
		try {

			return "Seperation3";
		} catch (Exception e) {
//	return "SomethingWentWrong";
//		String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//		String exString=e.toString();
//		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//		{
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
//		return "SomethingWentWrong";)
//			return "redirect:/swr";
//		}
//		else
//		{
			return "redirect:/logout";
//		}

		}
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
			String username = user1.getUsername();
			String to = user1.getEmail();
			int find = user1.getAaid();
			Optional<Admin> admin = adminDao.findById(find);
			Admin admin1 = admin.get();
			String cc = admin1.getEmail();
			EMSMAIN.id_with_email.put(user1.getId(), to);
			EMSMAIN.id_with_cc.put(user1.getId(), cc);
			EMSMAIN.id_with_last_working_day_date.put(user1.getId(), lastdate);
			EMSMAIN.id_with_username.put(user1.getId(), username);
//			servicelayer.sentMessage2(to, subject, username, lastdate, cc);
			return "Seperation3";
		} else {
			lastdate = user1.getLastWorkingDay();
			session.setAttribute("message", new Message(
					"Sorry!! You have already applied speration request and your last working day is " + lastdate,
					"alert-danger"));
			return "Seperation3";
		}
	}

	@GetMapping("/assetpolicy")
	public String AssetPolicy(Principal principal) {
		try {
			if (principal != null) {
				return "assetpolicy2";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//		return "SomethingWentWrong";
//		String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//		String exString=e.toString();
//		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//		{
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
//		return "SomethingWentWrong";)
//			return "redirect:/swr";
//		}
//		else
//		{
			return "redirect:/logout";
//		}

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
			return "AdminTeamMembers";
		} catch (Exception e) {
//		return "SomethingWentWrong";
//		String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//		String exString=e.toString();
//		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//		{
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
	public String teamprofile(@PathVariable("id") Integer id, Model model) {
		System.out.println("IN");
		Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
		UserDetail userDetail = userOptional.get();
		model.addAttribute("userdetail", userDetail);
		model.addAttribute("title", "update form - " + userDetail.getUsername());
		return "AdminTeamViewProfile";

	}

	@RequestMapping("/termination/{id}")
	public String Termination(@PathVariable("id") Integer id, HttpSession session, Principal principal) {
		try {
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
				String subject = "Google : Seperation Request EMPID: GOOGLEIN" + user1.getId();
				String username = user1.getUsername();
				String to = user1.getEmail();
				int find = user1.getAaid();
				Optional<Admin> admin = adminDao.findById(find);
				Admin admin1 = admin.get();
				String cc = admin1.getEmail();
				servicelayer.sentMessage4(to, subject, username, lastdate, cc);
				System.out.println("?????????????" + user1.getId());
				return "redirect:/admin/teamprofile/" + user1.getId();
			} else {
				lastdate = user1.getLastWorkingDay();
				session.setAttribute("message", new Message(
						"Sorry!! You have already applied speration request and your last working day is " + lastdate,
						"alert-danger"));
				return "redirect:/admin/teamprofile/" + user1.getId();
			}
		} catch (Exception e) {
//	return "SomethingWentWrong";
//		String error=" java.lang.NullPointerException: Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null";
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//		String exString=e.toString();
//		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//		{
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
//		return "SomethingWentWrong";)
//			return "redirect:/swr";
//		}
//		else
//		{
			return "redirect:/logout";
//		}

		}
	}

	List<UserLoginDateTime> all_users_login_records = new ArrayList<>();

	@GetMapping("/getloginrecords")
	public String get_login_records(UserLoginDateTime userLoginDateTime, Model model, Principal principal) {
		try {
			all_users_login_records = userLoginDao.findAll();
			if (all_users_login_records != null && principal != null) {
				System.out.println("find all " + all_users_login_records);
				model.addAttribute("all_users_login_records", all_users_login_records);
				System.out.println("IN");
				return "getloginrecords";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
//		String exString=e.toString();
//		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
//		{
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
//		return "SomethingWentWrong";)
//			return "redirect:/swr";
//		}
//		else
//		{
			return "redirect:/logout";
//		}

		}
	}

//  EXCEL download start ****   Added By Ayush Gupta 16 March 2024
//	@PostMapping("/export_excel")
//	public String export_excel(User user, HttpSession httpSession, Principal principal, Model model)
//			throws IOException, InvalidFormatException {
//		boolean result = servicelayer.data_insert_excel();
//		if (result) {
//			try {
//				if (all_users_login_records != null && principal != null) {
//					System.out.println("find all " + all_users_login_records);
//					model.addAttribute("all_users_login_records", all_users_login_records);
//					System.out.println("IN");
//					httpSession.setAttribute("message",
//							new Message("Users Login Data !! Download Successfully", "alert-success"));
//					user.setExcel_Download(true);
//					user.setExcel_Download_Date(new Date());
//					user.setDownload_count(user.getDownload_count() + 1);
//					userdao.save(user);
//					return "getloginrecords";
//				} else {
//					throw new Exception();
//				}
//			} catch (Exception e) {
//				System.out.println(e);
//				System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
////		String exString=e.toString();
////		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////		{
//				String exceptionAsString = e.toString();
//				// Get the current class
//				Class<?> currentClass = AdminController.class;
//
//				// Get the name of the class
//				String className = currentClass.getName();
//				String errorMessage = e.getMessage();
//				StackTraceElement[] stackTrace = e.getStackTrace();
//				String methodName = stackTrace[0].getMethodName();
//				int lineNumber = stackTrace[0].getLineNumber();
//				System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//				servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
////		return "SomethingWentWrong";)
////			return "redirect:/swr";
////		}
////		else
////		{
//				return "redirect:/logout";
////		}
//
//			}
//		} else {
//			try {
//				if (all_users_login_records != null && principal != null) {
//					System.out.println("find all " + all_users_login_records);
//					model.addAttribute("all_users_login_records", all_users_login_records);
//					System.out.println("IN");
////	httpSession.setAttribute("message", new Message("Users Login Data !! Download Successfully", "alert-success"));
//					System.out.println("no insert");
//					httpSession.setAttribute("message",
//							new Message("Something Went Wrong !! Download Failed", "alert-danger"));
//					return "getloginrecords";
//				} else {
//					throw new Exception();
//				}
//			} catch (Exception e) {
//				System.out.println(e);
//				System.out.println("ERRRRRRRRRRRRR " + e + " " + count);
////		String exString=e.toString();
////		if(exString.equals("Cannot invoke \"java.security.Principal.equals(Object)\" because \"principal\" is null") && count==1 || count==0)
////		{
//				String exceptionAsString = e.toString();
//				// Get the current class
//				Class<?> currentClass = AdminController.class;
//
//				// Get the name of the class
//				String className = currentClass.getName();
//				String errorMessage = e.getMessage();
//				StackTraceElement[] stackTrace = e.getStackTrace();
//				String methodName = stackTrace[0].getMethodName();
//				int lineNumber = stackTrace[0].getLineNumber();
//				System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//				servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
////		return "SomethingWentWrong";)
////			return "redirect:/swr";
////		}
////		else
////		{
//				return "redirect:/logout";
////		}
//
//			}
//		}
//	} // method end***
	
	 @PostMapping("/export_excel")
	 public ResponseEntity<byte[]> exportExcel() {
	        ByteArrayOutputStream out;
	        try {
	            out = servicelayer.exportUserLoginData();
	        } catch (IOException e) {
	            return ResponseEntity.internalServerError().build();
	        }

	        byte[] bytes = out.toByteArray();

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDispositionFormData("attachment", "USER_LOGIN_DATA.xlsx");

	        return ResponseEntity.ok()
	                .headers(headers)
	                .body(bytes);
	    }

	 
	 
	 
	 @PostMapping("/export_email_excel")
	    public String emailExcel(Principal principal,HttpSession httpSession) {
	        try {

	            String generatedExcelPath = servicelayer.generateExcel();
	            String subject = "Login History Report";
	            String to = principal.getName();
	            Optional<User> findUserByEmail=userdao.findByUserName(to);
	            User getUserByEmail = findUserByEmail.get();
	            String username = getUserByEmail.getUsername();
	            String message = "<div style='border:1px solid #e2e2e2;padding:20px'>" +
	                    "<p>Dear " + username +
	                    "<br><br>Please find attached the login history report." +
	                    "<br><br>EMS<br><b>For more information visit our website</b>" +
	                    "<br>https://wwwemscom-production.up.railway.app/" +
	                    "</p>" +
	                    "</div>";

	            boolean isSent = loginHistoryExportEmail.sendEmail(generatedExcelPath, message, subject, to);
                if(isSent)
                {
                	httpSession.setAttribute("message",
							new Message("File sent successfully to "+to, "alert-success"));
                	 return "redirect:/admin/getloginrecords";
                }
                else
                {
                	httpSession.setAttribute("message",
							new Message("File not sent successfully to "+to, "alert-danger"));
                	 return "redirect:/admin/getloginrecords";
                }
	          
	        } catch (Exception e) {
	            e.printStackTrace();
	            return "redirect:/swr";
	           
	        }
	    }

	 
	@PostMapping("/create_order")
	@ResponseBody
	public String create_order(@RequestBody Map<String, Object> data, Principal principal,SubscriptionPlans subscriptionPlans) throws Exception {
		try {
			String receiptnumber_string = null;
			float amount = Float.parseFloat(data.get("amount").toString());
			RazorpayClient razorpay = new RazorpayClient("rzp_test_icIfOJXJUlRjph", "L90mE03ZqQXO5rgWxRnn8JCn");
			List<Payment_Order_Info> payment_Order_Infos = orderDao.findAll();
			if (payment_Order_Infos.size() == 0) {
				System.out.println("RAZORPAY " + razorpay);
				JSONObject orderRequest = new JSONObject();
				orderRequest.put("amount", amount * 100);
				orderRequest.put("currency", "INR");
				orderRequest.put("receipt", "TXNIN110092");
//			JSONObject notes = new JSONObject();
//			notes.put("notes_key_1","Tea, Earl Grey, Hot");
//			orderRequest.put("notes",notes);

				Order order = razorpay.Orders.create(orderRequest);
				System.out.println(order);

				// save order info
				servicelayer.processing_payment(order, principal);
				return order.toString();

			} else {
				String getlastreceipt = orderDao.getLastReceiptNumber();
				// Regular expression to match the numeric part in the string
				String numericPart = getlastreceipt.replaceAll("\\D", ""); // Remove all non-digit characters
				String prefix = "TXNIN";
				System.out.println(numericPart); // Output: 110092
				int receiptnumbers_int = Integer.parseInt(numericPart);
				++receiptnumbers_int;
				receiptnumber_string = prefix + Integer.toString(receiptnumbers_int);
				System.out.println("RAZORPAY " + razorpay);
				JSONObject orderRequest = new JSONObject();
				orderRequest.put("amount", amount * 100);
				orderRequest.put("currency", "INR");
				orderRequest.put("receipt", receiptnumber_string);
//		JSONObject notes = new JSONObject();
//		notes.put("notes_key_1","Tea, Earl Grey, Hot");
//		orderRequest.put("notes",notes);

				Order order = razorpay.Orders.create(orderRequest);
				System.out.println(order);

				// save order info
				servicelayer.processing_payment(order, principal);
				return order.toString();
			}
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

			return "redirect:/swr";
		}
	}

	@GetMapping("/payment")
	public String payment(Principal principal, Model model) {
		try {
			Optional<User> user = userdao.findByUserName(principal.getName());
			User user1 = user.get();
			Optional<Payment_Order_Info> payment_Order_Info = orderDao.findbycompany(user1.getCompany_id());
			if (payment_Order_Info.isPresent()) {
				SubscriptionPlans subscriptionPlans = servicelayer.getAllPlans();
				System.out.println(" LIST PLANS " + subscriptionPlans);
				Payment_Order_Info orders = payment_Order_Info.orElse(null);
				if (orders.getStatus().equals("created")) {
					orders.setPaymentId("NA");
					orders.setLicense_number("NA");
					orders.setLicense_status("NA");
				}
				model.addAttribute("all_plans", subscriptionPlans);
				model.addAttribute("orders", orders);
				System.out.println("ORDERS ROW " + orders);
				return "payment";
			} else {
				SubscriptionPlans subscriptionPlans = servicelayer.getAllPlans();
				System.out.println(" LIST PLANS " + subscriptionPlans);
				Payment_Order_Info order_Info = new Payment_Order_Info();
				order_Info.setAmount(0);
				order_Info.setSubscription_start_date(null);
				order_Info.setSubscription_expiry_date(null);
				order_Info.setStatus("NA");
				order_Info.setReceipt("NA");
				order_Info.setOrderId("NA");
				order_Info.setPaymentId("NA");
				order_Info.setLicense_number("NA");
				order_Info.setLicense_status("NA");
				order_Info.setSystem_date_and_time(null);
//				order_Info.setLicense_status("ACTIVE");
				model.addAttribute("all_plans", subscriptionPlans);
				model.addAttribute("orders", order_Info);
				System.out.println("ORDERS ROW " + order_Info);
				return "payment";
			}
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

			return "redirect:/swr";
		}

	}

	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data, User user) throws IOException, MessagingException {
		boolean response=servicelayer.update_payment(user,data);
		return ResponseEntity.ok(Map.of("msg", response));
	}

	@GetMapping("/receipt/{orderId}")
	public String receipt(Principal principal, Model model, @PathVariable("orderId") String orderId) {
		String username = principal.getName();
		User user = servicelayer.findByUsername(username);
		Payment_Order_Info payment_Order_Info = servicelayer.findOrderByCompanyId(orderId);
		SubscriptionPlans subscriptionPlans=servicelayer.findSubscriptionPlans();
		CompanyInfo companyInfo = servicelayer.findCompanyInfo();
		model.addAttribute("payment_Order_Info", payment_Order_Info);
		model.addAttribute("user", user);
		model.addAttribute("companyinfo", companyInfo);
		model.addAttribute("subscriptionPlans", subscriptionPlans);
		return "receipt";
	}

	@GetMapping("/transaction_history")
	public String transaction_history(Principal principal, Model model) {
		String username = principal.getName();
		User user = servicelayer.findByUsername(username);
		List<Payment_Order_Info> payment_Order_Info = servicelayer.transaction_history(user.getCompany_id());
		model.addAttribute("payment_Order_Info", payment_Order_Info);
		return "transaction_history";
	}
	
	@GetMapping("/careers")
	public String careers()
	{
		return "careers";
	}
}