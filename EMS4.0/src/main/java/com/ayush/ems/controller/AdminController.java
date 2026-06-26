package com.ayush.ems.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
//import javax.transaction.Transactional;
import javax.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ayush.ems.config.NotificationListener;
import com.ayush.ems.dao.NSqlConfigDao;
import com.ayush.ems.dao.NotificationDao;
import com.ayush.ems.dao.OrderDao;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.dao.UserDetailDao;
import com.ayush.ems.entities.Attendance;
import com.ayush.ems.entities.CompanyInfo;
import com.ayush.ems.entities.EmployeeSuggestion;
import com.ayush.ems.entities.Error_Log;
import com.ayush.ems.entities.Holiday;
import com.ayush.ems.entities.Job;
import com.ayush.ems.entities.Payment_Order_Info;
import com.ayush.ems.entities.Performance;
import com.ayush.ems.entities.SubscriptionPlans;
import com.ayush.ems.entities.TaskPerformance;
import com.ayush.ems.entities.Tasks;
import com.ayush.ems.entities.UploadHistory;
import com.ayush.ems.entities.User;
import com.ayush.ems.entities.UserDetail;
import com.ayush.ems.entities.UserLoginDateTime;
import com.ayush.ems.entities.WorkingDaysAndHolidaysResponse;
import com.ayush.ems.entities.Laptop;
import com.ayush.ems.entities.Notification;
import com.ayush.ems.helper.Message;
import com.ayush.ems.service.LoginHistoryExportEmail;
import com.ayush.ems.service.Servicelayer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import cn.apiclub.captcha.Captcha;

@Controller
@RequestMapping("/admin")
@SessionScope
public class AdminController {
	@Autowired
	private UserDao userdao;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private Servicelayer servicelayer;
	@Autowired
	private UserDetailDao userDetailDao;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private LoginHistoryExportEmail loginHistoryExportEmail;
	@Autowired
	private NotificationDao notificationDao;
	@Autowired
	private NSqlConfigDao nSqlConfig;

	@ModelAttribute
	public void commonData(Model model, Principal principal, HttpServletRequest request) {
	    try {
	        String uri = request.getRequestURI();
	        
	        // 🔁 Skip commonData for SSE endpoints to avoid principal issues
	        if (uri.contains("/admin/notifications/stream")) {
	            return;
	        }

	        if (principal != null && principal.getName() != null) {
	            Optional<User> userOptional = this.userdao.findByEmail(principal.getName());
	            userOptional.ifPresent(user -> model.addAttribute("user", user));
	        }

	    } catch (Exception e) {
	    	 // Basic exception details
        	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
	        System.out.println("⚠️ Exception in commonData: " + e.getMessage());
	    }
	}


	
	@GetMapping("/notifications/stream")
	public SseEmitter streamNotifications(Authentication authentication) {
	    String username = authentication.getName();
	    System.out.println("🔌 SSE connection established for: " + username);

	    SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1 hour timeout
	    NotificationListener.register(username, emitter);

	    emitter.onCompletion(() -> NotificationListener.remove(username));
	    emitter.onTimeout(() -> NotificationListener.remove(username));
	    emitter.onError((ex) -> {
	        System.out.println("⚠️ Error for SSE user: " + username);
	        NotificationListener.remove(username);
	    });

	    return emitter;
	}

	  @PostMapping("/notifications/mark-all-read")
	    public ResponseEntity<Map<String, Object>> markAllAsRead(Principal principal) {
	        String username = principal.getName(); // or fetch userId

	        servicelayer.markAllAsRead(username);

	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("message", "All notifications marked as read");
	        response.put("notificationCount", 0); // updated count

	        return ResponseEntity.ok(response);
	    }
	  
	  
	  @GetMapping("/notifications/{id}")
	  @Transactional
	  public String viewNotificationDetails(@PathVariable("id") String messageId,
	                                        @RequestParam(value = "success", required = false) String success,
	                                        Model model, Principal principal) {
		  try
		  {
	      Notification notification = servicelayer.findMessageByMessageId(messageId);
System.out.println("BEFORE IF BLOCK");
	      if (notification != null && !notification.isRead()) {
	    	  System.out.println("AFTER IF BLOCK");
	          notification.setRead(true);
	          notificationDao.save(notification);
	      }
	      
	      if (notification == null) {
	          model.addAttribute("error", "Notification not found");
	          return "error";
	      }

	      // ✅ Already marked as read during POST, so no need again unless you want auto-mark here
	      model.addAttribute("notification", notification);

	      // ✅ Pass success message to the view if present
	      if (success != null && success.equals("true")) {
	          model.addAttribute("successMessage", "✅ Notification marked as read successfully!");
	      }

	      return "AdminMessageDetails";
		  }
		  catch (Exception e) {
			  // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
		    	  return "error";
			}
	  }
	  
	  
	  @PostMapping("/notifications/mark-as-read")
	  @Transactional
	  public String markAsRead(@RequestParam("id") String messageId) {
	      Notification notification = servicelayer.findMessageByMessageId(messageId);

	      if (notification != null && !notification.isMarkAsRead()) {
	    	  System.out.println("HELLO MARK AS READ");
	    	  notification.setMarkAsRead(true);
	    	  notification.setMarkAsReadDate(new Date());
	          notification.setRead(true);
	          notification.setEditdate(new Date());
	          notification.setEditwho("SYSTEM");
	          notificationDao.save(notification);
	      }

	      // ✅ Redirect to GET with success flag
	      return "redirect:/admin/notifications/" + messageId + "?success=true";
	  }

	  
	  @PostMapping("/notifications/each-mark-as-read/{message_id}")
	  @ResponseBody
	  @Transactional
	  public ResponseEntity<?> markAsRead(@PathVariable("message_id") String messageId, Principal principal) {
	      try {
	          if (principal == null) {
	              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("⚠️ You are not logged in");
	          }

	          System.out.println("Marking message as read: " + messageId);
	          Notification notification = servicelayer.findMessageByMessageId(messageId);

	          if (notification != null && !notification.isMarkAsRead()) {
	              notification.setMarkAsRead(true);
	              notification.setMarkAsReadDate(new Date());
	              notification.setRead(true);
	              notification.setEditdate(new Date());
	              notification.setEditwho("SYSTEM");
	              System.out.println("BEFORE SAVE");
	              notificationDao.save(notification);
	              System.out.println("AFTER SAVE");
	              return ResponseEntity.ok("✅ Marked as read");
	          } else {
	              return ResponseEntity.ok("⚠️ Already read or not found");
	          }

	      } catch (Exception e) {
	    	  // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
	          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Failed to mark as read");
	      }
	  }
	  
	  
	  @GetMapping("/notifications/fetch")
	  @ResponseBody
	  public Map<String, List<Notification>> fetchNotifications(Principal principal) {
	      String username = principal.getName();
	      List<Notification> unread = servicelayer.getUnreadNotifications(username);
	      List<Notification> read = servicelayer.getReadNotifications(username);

	      Map<String, List<Notification>> map = new HashMap<>();
	      map.put("unread", unread);
	      map.put("read", read);
	      return map;
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

	    private int count = 0;


@GetMapping("/new")
public String homeee(User user, UserDetail userDetail, Error_Log error_Log, Principal principal, Model model,
                     HttpSession session, HttpServletResponse response, HttpServletRequest request) {
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
            int page = 0;
            int batchSize = 100;
            String clientIp = getClientIpAddress(request);
            String location = getLocationFromIp(clientIp);
            String username = principal.getName();

            Page<User> currentUser = servicelayer.fetchUsersByEmail(username, page, batchSize);
            if (currentUser != null && !currentUser.getContent().isEmpty()) {
                User user1 = currentUser.getContent().get(0);
                servicelayer.login_record_save(user1, session, clientIp, location);
            }
            count++;
        }

        List<TaskPerformance> performanceData = servicelayer.getTaskPerformanceDataForGraph(user.getId());
        model.addAttribute("performanceData", performanceData);

        Map<String, Integer> taskCounts = servicelayer.getAllTaskCounts(user.getId());
        model.addAttribute("taskCounts", taskCounts);

        model.addAttribute("year", currentYear);

        // ✅ Collect attendance and holiday events
        List<Map<String, Object>> events = new ArrayList<>();

        @SuppressWarnings("unused")
		int sum_total_break = 0;
        List<Attendance> attendances = servicelayer.getAllAttendanceById(user.getId());
        for (Attendance att : attendances) {
            Map<String, Object> event = new HashMap<>();
            sum_total_break = att.getTotalBreak() + 1;
            event.put("date", att.getDate().toString());
            event.put("status", att.getStatus());
            event.put("name", null);
            event.put("inTime", att.getCheckInTime() != null ? att.getCheckInTime().toString() : "--");
            event.put("outTime", att.getCheckOutTime() != null ? att.getCheckOutTime().toString() : "--");
            event.put("totalBreak", att.getTotalBreak() != 0 ? att.getTotalBreak() : "0");
            event.put("hoursWorked", att.getHoursWorked() != null ? att.getHoursWorked().toString() : "--");
            event.put("shift", att.getShift() != null ? att.getShift() : "--");
            event.put("wfh_request", att.isWfh_request());
            event.put("leave_request", att.isLeave_request());
            event.put("withdrawn_request", att.isWithdrawnRequest());
            events.add(event);
        }

        List<Holiday> holidays = servicelayer.getAllHolidaysByCompanyInfo(user.getCompany_id());
        System.out.println("HOLIDAY SIZE "+holidays.size());
        for (Holiday h : holidays) {
        	 if (h.getDate() == null) {
        	        System.out.println("Holiday Date is NULL. Holiday Name: " + h.getName());
        	        continue;   // Skip invalid record
        	    }
            Map<String, Object> event = new HashMap<>();
            event.put("date", h.getDate().toString());
            event.put("status", h.getType());
            event.put("name", h.getName());
            events.add(event);
        }

        // ✅ Register JavaTimeModule with ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String eventsJson = objectMapper.writeValueAsString(events);
        model.addAttribute("eventsJson", eventsJson);

        System.out.println("EVENTS : " + events);
        return "AdminHome";

    } catch (Exception e) {
    	 // Basic exception details
    	e.printStackTrace();
        String exceptionAsString = e.toString();
        String className = AdminController.class.getName();
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
        return "redirect:/logout";
    }
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

//	private String getLocationFromIp(String ip) {
//	    try {
//	        // Use a simple public API to get location data
//	        String url = "https://ipapi.co/" + ip + "/city/";
//	        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
//	        urlConnection.setRequestMethod("GET");
//
//	        int responseCode = urlConnection.getResponseCode();
//
//	        // If rate-limited (HTTP 429), wait and retry
//	        if (responseCode == 429) {
//	            System.out.println("Rate limit exceeded. Retrying after a delay...");
//	            Thread.sleep(5000); // Delay for 5 seconds (or adjust based on the API's limit)
//	            return getLocationFromIp(ip); // Retry the request
//	        }
//
//	        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//	        String inputLine;
//	        StringBuilder response = new StringBuilder();
//	        while ((inputLine = in.readLine()) != null) {
//	            response.append(inputLine);
//	        }
//	        in.close();
//
//	        // Return city name
//	        return response.toString().isEmpty() ? "Unknown Location" : response.toString();
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        return "Unknown Location";
//	    }
//	}

	/**
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

	@GetMapping("/employee_leave_policy")
	public String Employee_Leave_Policy() {
		return "employeeleavepolicy2";
	}

	@GetMapping("/performance")
	public String performance(Model model, User user, Performance performance, HttpSession httpSession,
			Principal principal) {
		try {
			Calendar calendar = Calendar.getInstance();
			int currentYear = calendar.get(Calendar.YEAR);
			if (principal.equals(null)) {
				throw new Exception("session_invalid_exception");
			}
//			List<Double> chartData = servicelayer.performance(user, httpSession);
//			List<String> chartLabels = Arrays.asList("January", "February", "March", "April", "May", "June", "July",
//					"August", "September", "October", "November", "December"); // Example labels
//			int year = (int) httpSession.getAttribute("year");
			 Map<String, Integer> taskCounts = servicelayer.getAllTaskCounts(user.getId());
		        model.addAttribute("taskCounts", taskCounts);
List<TaskPerformance> performanceData = servicelayer.getTaskPerformanceDataForGraph(user.getId());
			    
			    model.addAttribute("performanceData", performanceData);
//			model.addAttribute("chartData", chartData);
//			model.addAttribute("chartLabels", chartLabels);
			model.addAttribute("year", currentYear);
			return "AdminPerformance";
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

	@GetMapping("/cal")
	public String cal() {
		return "calender";
	}

//	List<UserDetail> all_users = new ArrayList<>();
//
//	@GetMapping("/viewMembers/{page}")
//	public String viewTeamMembers(Model model, User user, Principal principal,@RequestParam(name = "pageSize", defaultValue = "10")
//	int pageSize,
//			@PathVariable("page") int page) {
//		try {
//			System.out.println(" all_users_size "+all_users.size());
////			all_users = userDetailDao.findAllEnabledUser();
//			System.out.println(" all_users_size "+all_users.size());
//			if (all_users != null && user.getUsername() != null) {
//				Page<UserDetail> user_page=servicelayer.findPaginated(page, pageSize);
//			    model.addAttribute("all_inbound_records", user_page);
//			    model.addAttribute("currentPage", page);
//			    model.addAttribute("totalPages", user_page.getTotalPages());
//			    model.addAttribute("selectedPageSize", pageSize); // Add this attribute for Thymeleaf to select the current page size
//			    model.addAttribute("pageSizes", List.of(10, 20, 50, 100,500)); // Provide possible page sizes
//				System.out.println("IN");
//				return "ViewMembers2";
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
////        return "ViewMembers2";
//        return "redirect:/admin/viewMembers/0";
//    }
//	

	List<UserDetail> all_users = new ArrayList<>();

	@GetMapping("/viewMembers/{page}")
	public String viewTeamMembers(Model model, User user, Principal principal,
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
				

	            // ✅ Drive Permission Check for each user
				for (UserDetail ud : all_users) {
				    String fileId = ud.getImage_file_id();
				    String fileName = ud.getImage_Name();

				    // Skip if default.jpg or No Record Found
				    if (fileId != null 
				        && !fileId.isBlank() 
				        && !fileName.equalsIgnoreCase("default.jpg") 
				        && !fileId.equalsIgnoreCase("No Record Found")) {

				        try {
				        	System.out.println("USERDETAIL PERMSIION "+ud.getImage_Name()+" "+ud.getImage_File_Url());
				            // Grant access if not already shared
				            servicelayer.grantAccessIfNotShared(fileId, user.getEmail(), true);
				            ud.setHas_drive_access(true);
				        } catch (Exception ex) {
				            ud.setHas_drive_access(false);
				            System.err.println("⚠️ Permission issue for: " 
				                                + ud.getEmail() + " | " + ex.getMessage());
				        }

				    } else {
				        // Skip invalid/default image
				        ud.setHas_drive_access(false);
				    }
				}


				// Create a paginated version of the sorted list
				Page<UserDetail> user_page = servicelayer.findPaginated(all_users, page, pageSize);

				// Add the sorted and paginated data to the model
				model.addAttribute("all_emp_records", user_page);
				model.addAttribute("currentPage", page);
				model.addAttribute("totalPages", user_page.getTotalPages());
				model.addAttribute("selectedPageSize", pageSize); // For Thymeleaf page size selection
				model.addAttribute("pageSizes", List.of(10, 20, 50, 100, 500)); // Possible page sizes
				model.addAttribute("sort", sort); // Pass sorting parameter to maintain state on frontend

				System.out.println("IN");
				return "AdminViewAllEmployees";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

	@GetMapping("/employeeSuggestions")
	@ResponseBody
	public List<EmployeeSuggestion> getEmployeeSuggestions(@RequestParam("term") String term) {
		List<UserDetail> employees = servicelayer.searchEmployees(term);
		return employees.stream().map(userdetail -> new EmployeeSuggestion(userdetail.getId(), userdetail.getUsername(),
				userdetail.getEmail())).collect(Collectors.toList());
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
				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
				UserDetail userDetail = userOptional.get();
				  String fileId = userDetail.getImage_file_id();
				    String fileName = userDetail.getImage_Name();

				    // Skip if default.jpg or No Record Found
				    if (fileId != null 
				        && !fileId.isBlank() 
				        && !fileName.equalsIgnoreCase("default.jpg") 
				        && !fileId.equalsIgnoreCase("No Record Found")) {

				        try {
				        	System.out.println("USERDETAIL PERMSIION "+userDetail.getImage_Name()+" "+userDetail.getImage_File_Url());
				            // Grant access if not already shared
				            servicelayer.grantAccessIfNotShared(fileId, principal.getName(), true);
				            userDetail.setHas_drive_access(true);
				        } catch (Exception ex) {
				            userDetail.setHas_drive_access(false);
				            System.err.println("⚠️ Permission issue for: " 
				                                + principal.getName() + " | " + ex.getMessage());
				        }

				    } else {
				        // Skip invalid/default image
				        userDetail.setHas_drive_access(false);
				    }
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "AdminViewEmpProfile";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

	@GetMapping("/emp_profile_edit_2/{id}")
	public String profilee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
				UserDetail userDetail = userOptional.get();
				 String fileId = userDetail.getImage_file_id();
				    String fileName = userDetail.getImage_Name();

				    // Skip if default.jpg or No Record Found
				    if (fileId != null 
				        && !fileId.isBlank() 
				        && !fileName.equalsIgnoreCase("default.jpg") 
				        && !fileId.equalsIgnoreCase("No Record Found")) {

				        try {
				        	System.out.println("USERDETAIL PERMSIION "+userDetail.getImage_Name()+" "+userDetail.getImage_File_Url());
				            // Grant access if not already shared
				            servicelayer.grantAccessIfNotShared(fileId, principal.getName(), true);
				            userDetail.setHas_drive_access(true);
				        } catch (Exception ex) {
				            userDetail.setHas_drive_access(false);
				            System.err.println("⚠️ Permission issue for: " 
				                                + principal.getName() + " | " + ex.getMessage());
				        }

				    } else {
				        // Skip invalid/default image
				        userDetail.setHas_drive_access(false);
				    }
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "AdminEmpProfile";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

//	@GetMapping("/emp_profile_edit_21/{id}")
//	public String profileee(@PathVariable("id") Integer id, Model model, Principal principal) {
//		try {
//			if (principal != null) {
//				System.out.println("IN");
//				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
//				UserDetail userDetail = userOptional.get();
//				model.addAttribute("userdetail", userDetail);
//				model.addAttribute("title", "update form - " + userDetail.getUsername());
//				return "AdminEmpProfileEdit1";
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

	@GetMapping("/emp_profile_edit_21/{id}")
	public String profileee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
				UserDetail userDetail = userOptional.get();
				List<String> laptopBrands = servicelayer.getAllLaptopBrands(userDetail.getCompany_id());
				System.out.println("LIST LAPTOP BY COMPANY ID " + laptopBrands.size());
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("laptopBrands", laptopBrands);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "AdminEmpProfileEdit1";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

	@PostMapping("/getModelsByBrand")
	@ResponseBody
	public List<String> getModelsByBrand(@RequestBody Laptop laptop) {
		System.out.println(
				"Fetching models for brand: " + laptop.getLaptop_brand() + ", Company: " + laptop.getCompany_id());

		List<Laptop> laptops = servicelayer.findModelsByBrand(laptop.getLaptop_brand(), laptop.getCompany_id());

		List<String> models = laptops.stream().map(Laptop::getLaptop_model).distinct().collect(Collectors.toList());

		System.out.println("Models found: " + models.size());
		return models;
	}

	@PostMapping("/getSerialNumbersByModel")
	@ResponseBody
	public List<String> getSerialNumbersByModel(@RequestBody Laptop laptop) {
		System.out.println("Fetching serial numbers for brand: " + laptop.getLaptop_brand() + ", Model: "
				+ laptop.getLaptop_model() + ", Company: " + laptop.getCompany_id());

		List<Laptop> laptops = servicelayer.findSerialNumbersByModel(laptop.getLaptop_brand(), laptop.getLaptop_model(),
				laptop.getCompany_id());

		List<String> serialNumbers = laptops.stream().map(Laptop::getSerial_number).distinct()
				.collect(Collectors.toList());

		System.out.println("Serial Numbers found: " + serialNumbers.size());
		return serialNumbers;
	}

	@PostMapping("/getProductIdAndColorBySerialNumber")
	@ResponseBody
	public Map<String, String> getProductIdAndColorBySerialNumber(@RequestBody Laptop laptop) {
		System.out.println("Fetching Product ID and Color for Brand: " + laptop.getLaptop_brand() + ", Model: "
				+ laptop.getLaptop_model() + ", Serial Number: " + laptop.getSerial_number() + ", Company: "
				+ laptop.getCompany_id());

		List<Laptop> laptops = servicelayer.findProductIdAndColorBySerialNumber(laptop.getLaptop_brand(),
				laptop.getLaptop_model(), laptop.getSerial_number(), laptop.getCompany_id());

		Map<String, String> response = new HashMap<>();
		if (!laptops.isEmpty()) {
			response.put("product_id", laptops.get(0).getProduct_ID());
			response.put("laptop_color", laptops.get(0).getLaptop_color());
			response.put("device_id", laptops.get(0).getDevice_id());
			System.out.println("Product ID: " + laptops.get(0).getProduct_ID() + ", Laptop Color: "
					+ laptops.get(0).getLaptop_color() + ", Device ID: " + laptops.get(0).getDevice_id());
		} else {
			response.put("product_id", "Not Found");
			response.put("laptop_color", "Not Found");
			response.put("device_id", "Not Found");
			System.out.println("Product ID and Laptop Color Not Found");
		}
		return response;
	}

	@GetMapping("/admin_profile_edit_1/{id}")
	public String yourProfile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findByIdField(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "AdminViewProfile";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

	@GetMapping("/admin_profile_edit_2/{id}")
	public String yourProfileee(@PathVariable("id") Integer id, Model model, Principal principal) {
	    try {
	        if (principal == null) throw new Exception("Unauthenticated access");

	        User user = servicelayer.loadProfile(id, model);  // ✅ get the updated user
	        model.addAttribute("user", user);                 // ✅ add it to the model again
	        model.addAttribute("title", "Update Form - " + user.getUsername());

	        return "AdminEditProfile";

	    } catch (Exception e) {
	        // Basic exception details
	    	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}
	
	@GetMapping("/user-image/{id}")
	public ResponseEntity<byte[]> getUserImage(@PathVariable Integer id) throws Exception {
		System.out.println("ENTERED IMAGE METHOD");
	    byte[] image = servicelayer.downloadImageByUserId(id);
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.IMAGE_JPEG); // or detect type dynamically
	    return new ResponseEntity<>(image, headers, HttpStatus.OK);
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
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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

			session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
			return "redirect:/admin/emp_profile_edit_1/" + userDetail.getId();
		}
	}

	@PostMapping("/emp_processing_profile/{id}")
	public String empProfileUpdate(@PathVariable("id") int getid, @ModelAttribute("userdetail") UserDetail userDetail,
			Principal principal, HttpSession session) throws Exception {

		try
		{
		String CurrentUser = null;
		if (principal != null) {
			CurrentUser = principal.getName();
		}
		System.out.println(userDetail.getLaptop_brand());
		System.out.println(userDetail.getLaptop_id());
		System.out.println(userDetail.getLaptop_serial_number());
		System.out.println(userDetail.getLaptop_assignment_status());
		System.out.println("OPERATIONAL FROM USER " + userDetail.getLaptop_operational_status());
		servicelayer.emp_update_profile(userDetail, CurrentUser);
		System.out.println("USERDETAIL ID ");
		session.setAttribute("message", new Message("Profile Updated !!", "alert-success"));
		return "redirect:/admin/emp_profile_edit_1/" + userDetail.getId();
		}
		catch (Exception e) {
			// TODO: handle exception
		        // Basic exception details
			e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
	        @RequestParam("resume") MultipartFile resumeFile,
	        @RequestParam(name = "twoStepEnabled", required = false) String twoStepEnabledParam,
	        HttpSession session) {

	    try {
	        // 2FA toggle
	        user.setTwoStepEnabled(twoStepEnabledParam != null);

	        // Size limits from DB
	        String maxImageStr = nSqlConfig.findMaxImageSizeByCompanyId(user.getCompany_id());
	        long maxImageBytes = (maxImageStr != null)
	                ? org.springframework.util.unit.DataSize.parse(maxImageStr).toBytes()
	                : org.springframework.util.unit.DataSize.ofMegabytes(5).toBytes();

	        String maxResumeStr = nSqlConfig.findMaxFileSizeByCompanyId(user.getCompany_id());
	        long maxResumeBytes = (maxResumeStr != null)
	                ? org.springframework.util.unit.DataSize.parse(maxResumeStr).toBytes()
	                : org.springframework.util.unit.DataSize.ofMegabytes(10).toBytes();

	        // Validate profile image size
	        if (file != null && !file.isEmpty() && file.getSize() > maxImageBytes) {
	            session.setAttribute("message", new Message(
	                    String.format("❌ Profile image %.2f MB exceeds allowed %.2f MB",
	                            file.getSize() / (1024.0 * 1024.0),
	                            maxImageBytes / (1024.0 * 1024.0)),
	                    "alert-danger"));
	            return "redirect:/admin/admin_profile_edit_1/" + user.getId();
	        }

	        // Validate resume size
	        if (resumeFile != null && !resumeFile.isEmpty() && resumeFile.getSize() > maxResumeBytes) {
	            session.setAttribute("message", new Message(
	                    String.format("❌ Resume %.2f MB exceeds allowed %.2f MB",
	                            resumeFile.getSize() / (1024.0 * 1024.0),
	                            maxResumeBytes / (1024.0 * 1024.0)),
	                    "alert-danger"));
	            return "redirect:/admin/admin_profile_edit_1/" + user.getId();
	        }

	        // Upload to Drive if new files uploaded
	        if (file != null && !file.isEmpty()) {
	            String image_file_id = servicelayer.uploadFileToGoogleDrive(file, user, "PROFILEIMAGEGOOGLEDRIVEFOLDER");
	            user.setImage_file_id(image_file_id);
	        }

	        if (resumeFile != null && !resumeFile.isEmpty()) {
	            String resume_file_id = servicelayer.uploadFileToGoogleDrive(resumeFile, user, "RESUMEGOOGLEDRIVEFOLDER");
	            user.setResume_file_id(resume_file_id);
	        }

	        // Final DB update
	        servicelayer.update_profile(user, file, resumeFile);

	        session.setAttribute("message", new Message("Profile updated successfully ✅", "alert-success"));
	        return "redirect:/admin/admin_profile_edit_1/" + user.getId();

	    } catch (Exception e) {
	        e.printStackTrace();
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
	        session.setAttribute("message", new Message("Something went wrong!!", "alert-danger"));
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
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

	private void getCaptcha(User user) {
		Captcha captcha = com.ayush.ems.service.Servicelayer.createCaptcha(250, 80);
		user.setHidden(captcha.getAnswer());
		user.setCaptcha("");
		user.setImageCaptcha(servicelayer.encodeCaptcha(captcha));
		System.out.println("impoted " + user.getImageCaptcha());
	}

	@GetMapping("/show_all_employees")
	public String getAllEmployees(Principal principal) {
		try {
			return "show_employees";
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

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
			return "AdminChangeCurrentPassword";
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

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
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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

			return "AdminSeperation";
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

	@PostMapping("/seperation/{id}")
	public String Seperation(@PathVariable("id") Integer id, HttpSession session) {
	    Optional<User> result2 = userdao.findByIdField(id);
	    User user1 = result2.get();

	    if (user1.getSeperationDate() == null && user1.getLastWorkingDay() == null) {
	        String message = servicelayer.seperationLogic(id, user1);
	        session.setAttribute("message", new Message(message, "alert-success"));
	    } else {
	        Date lastdate = user1.getLastWorkingDay();
	        session.setAttribute("message", new Message(
	                "Sorry!! You have already applied speration request and your last working day is " + lastdate,
	                "alert-danger"));
	    }
	    return "AdminSeperation";
	}


	@PostMapping("/withdrawn_request/{id}")
	public String Withdrawn_Request(@PathVariable("id") Integer id, HttpSession session) {
	    try {
	        String result = servicelayer.withdrawResignationRequestWithEmail(id);
	        if (result.equals("success")) {
	            session.setAttribute("message", new Message("Your Resignation Request Has Been Withdrawn Successfully ", "alert-success"));
	        } else {
	            session.setAttribute("message", new Message("Something Went Wrong !! Resignation Cannot Be Withdrawn", "alert-danger"));
	        }
	        return "AdminSeperation";
	    } catch (NoSuchElementException e) {
	        session.setAttribute("message", new Message("Something Went Wrong !! " + e.getMessage(), "alert-danger"));
	        e.printStackTrace();
	        return "AdminSeperation";
	    } catch (Exception e) {
	        session.setAttribute("message", new Message("Unexpected Error: " + e.getMessage(), "alert-danger"));
	        return "ManagerSeperation";
	    }
	}

	@GetMapping("/assetpolicy")
	public String AssetPolicy(Principal principal) {
		try {
			if (principal != null) {
				return "AdminAssetPolicy";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

	@GetMapping("/viewTeamMembersOnly")
	public String ViewTeamMembers(User user, Model model, Principal principal) {
		try {
			// Get user ID
			int id = user.getId();

			// Fetch user details by team
			List<UserDetail> allDetails = servicelayer.findUserByTeam(id);

			// Handle the case where no details are found
			if (allDetails.isEmpty()) {
				// Process and add details to the model
				model.addAttribute("allDetails", allDetails);
				return "AdminTeamMembers"; // Redirect to a view that shows no team members found
			}

			// Process and add details to the model
			model.addAttribute("allDetails", allDetails);

			return "AdminTeamMembers"; // Proceed with normal flow if details are present
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

	  @GetMapping("/access-denied")
	    public String accessDenied(Model model, @ModelAttribute("errorMessage") String errorMessage) {
	        // If no message was passed, fallback message
	        if (errorMessage == null || errorMessage.isEmpty()) {
	            errorMessage = "🚫 Access Denied! You do not have permission to view this page.";
	        }
	        model.addAttribute("errorMessage", errorMessage);
	        return "AdminError403"; // this points to a Thymeleaf file: access-denied.html
	    }
	  
	@RequestMapping("/teamprofile/{id}")
	public String teamprofile(@PathVariable("id") Integer id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
		System.out.println("IN");
		Optional<User> get_user = this.userdao.findByEmail(principal.getName());
		User get_user1 = get_user.get();
		  // 1. Get the logged-in username
        String loggedInUsername = principal.getName();
		 // 2. Fetch the logged-in user from DB
        User loggedInUser = servicelayer.findByUsername(loggedInUsername); // or fetch by email if applicable
    	Optional<UserDetail> userOptional = this.userDetailDao.findIfSameTeam(id, loggedInUser.getId());
		  if (userOptional.isEmpty()) {
              redirectAttributes.addFlashAttribute("errorMessage", "🚫 Unauthorized access! You are not allowed to view this content.");
              return "redirect:/admin/access-denied";
          }
		  UserDetail userDetail = userOptional.get();
		model.addAttribute("userdetail", userDetail);
		model.addAttribute("get_user", get_user1);
		model.addAttribute("title", "update form - " + userDetail.getUsername());
		return "AdminTeamViewProfile";

	}

	@RequestMapping("/termination/{id}")
	public String Termination(@PathVariable("id") Integer id, HttpSession session, Principal principal) {
		try {
			Optional<User> result2 = userdao.findByIdField(id);
			User user1 = result2.get();
			System.out.println("{{{{{{{{{{{{{{{ " + user1);
			Date lastdate = user1.getLastWorkingDay();
			System.out.println("}}}}}}}}}}}}}}} " + lastdate);
			if (user1.getSeperationDate() == null && user1.getLastWorkingDay() == null) {
				servicelayer.seperationLogic(user1.getId(), user1);
				lastdate = user1.getLastWorkingDay();
				System.out.println("}}}}}}}}}}}}}}} " + lastdate);
				session.setAttribute("message", new Message("Your last working day is " + lastdate, "alert-success"));
//				String subject = "Google : Seperation Request EMPID: GOOGLEIN" + user1.getId();
//				String username = user1.getUsername();
//				String to = user1.getEmail();
//				int find = user1.getAaid();
//				Optional<Admin> admin = adminDao.findById(find);
//				Admin admin1 = admin.get();
//				String cc = admin1.getEmail();
//				servicelayer.sentMessage4(to, subject, username, lastdate, cc);
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
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

	List<UserLoginDateTime> all_users_login_records = new ArrayList<>();

	@GetMapping("/getloginrecords")
	public String get_login_records(User user, UserLoginDateTime userLoginDateTime, Model model, Principal principal) {
		try {
			all_users_login_records = servicelayer.findAllByCompanyIdOrTeamId(user);
			if (all_users_login_records != null && principal != null) {
				System.out.println("find all " + all_users_login_records);
				model.addAttribute("all_users_login_records", all_users_login_records);
				System.out.println("IN");
				return "AdminGetLoginRecords";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			 // Basic exception details
        	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

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
	public ResponseEntity<byte[]> exportExcel(User user) throws GeneralSecurityException {
		ByteArrayOutputStream out;
		try {
			System.out.println("EXPORT EXCEL " + user.getPhone());
			out = servicelayer.exportUserLoginData(user.getEmail(), user.getPhone());
		} catch (IOException e) {
			return ResponseEntity.internalServerError().build();
		}

		byte[] bytes = out.toByteArray();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(
				MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
		headers.setContentDispositionFormData("attachment", "USER_LOGIN_DATA.xlsx");

		return ResponseEntity.ok().headers(headers).body(bytes);
	}

	@PostMapping("/export_email_excel")
	public String emailExcel(Principal principal, HttpSession httpSession) {
		try {

			String generatedExcelPath = servicelayer.generateExcel();
			String subject = "Login History Report";
			String to = principal.getName();
			Optional<User> findUserByEmail = userdao.findByEmail(to);
			User getUserByEmail = findUserByEmail.get();
			String username = getUserByEmail.getUsername();
			String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>" + "    <meta charset='UTF-8'>"
					+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" + "    <style>"
					+ "        body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }"
					+ "        .email-wrapper { width: 100%; height: 100vh; display: flex; justify-content: center; align-items: center; }"
					+ "        .email-container { max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1); text-align: center; padding: 20px; }"
					+ "        .email-header { background-color: #007bff; color: white; text-align: center; padding: 20px; font-size: 24px; font-weight: bold; border-radius: 8px 8px 0 0; }"
					+ "        .email-body { padding: 20px; text-align: left; }"
					+ "        .email-body p { margin: 0 0 20px; line-height: 1.6; font-size: 16px; color: #333; }"
					+ "        .highlight { background-color: #e8f4f8; border-left: 4px solid #007bff; padding: 15px; margin-bottom: 30px; border-radius: 5px; }"
					+ "        .footer { margin-top: 20px; text-align: center; font-size: 14px; color: #555555; }"
					+ "        .footer a { color: #007bff; text-decoration: none; font-weight: bold; }"
					+ "        .colored-logo { font-size: 16px; }" + "    </style>" + "</head>" + "<body>"
					+ "    <div class='email-wrapper'>" + "        <div class='email-container'>"
					+ "            <div class='email-header'>" + "                <strong>Login History Report</strong>"
					+ "            </div>" + "            <div class='email-body'>"
					+ "                <div class='highlight'>" + "                    <p>Dear " + username + ",</p>"
					+ "                    <p>Please find attached the login history report.</p>"
					+ "                </div>"
					+ "                <p>We appreciate your engagement with our platform. Your security and activity are important to us.</p>"
					+ "                <p>In this report, you will find detailed information about your login attempts, including dates, times, and IP addresses.</p>"
					+ "                <p>If you notice any unusual activity, please contact us immediately.</p>"
					+ "                <p>For more information, visit our website:</p>"
					+ "                <p><a href='https://wwwemscom-production.up.railway.app/' style='color: #007bff;'>https://wwwemscom-production.up.railway.app/</a></p>"
					+ "            </div>" + "            <div class='footer'>"
					+ "                <p>Thank you for being a valued member of our community!</p>"
					+ "                <p class='colored-logo'>"
					+ "                    <span class='colored-char' style='color: rgb(66, 133, 244);'>w</span><span class='colored-char' style='color: rgb(255, 0, 0);'>w</span><span class='colored-char' style='color: rgb(255, 165, 0);'>w</span><span class='colored-char' style='color: rgb(0, 0, 255);'>.</span><span class='colored-char' style='color: rgb(60, 179, 113);'>e</span><span class='colored-char' style='color: rgb(255, 0, 0);'>m</span><span class='colored-char' style='color: rgb(0, 0, 255);'>s</span><span class='colored-char' style='color: rgb(255, 0, 0);'>.</span><span class='colored-char' style='color: rgb(255, 165, 0);'>c</span><span class='colored-char' style='color: rgb(0, 0, 255);'>o</span><span class='colored-char' style='color: rgb(255, 0, 0);'>m</span>"
					+ "                </p>" + "                <p>Need assistance? <a href='#'>Contact Support</a></p>"
					+ "            </div>" + "        </div>" + "    </div>" + "</body>" + "</html>";

			CompletableFuture<Boolean> flagFuture = loginHistoryExportEmail.sendEmail(generatedExcelPath, message,
					subject, to);
			Boolean isSent = flagFuture.get(); // Blocking call to get the result
			if (isSent) {
				httpSession.setAttribute("message", new Message("File sent successfully to " + to, "alert-success"));
				return "redirect:/admin/getloginrecords";
			} else {
				httpSession.setAttribute("message", new Message("File not sent successfully to " + to, "alert-danger"));
				return "redirect:/admin/getloginrecords";
			}

		} catch (Exception e) {
			 // Basic exception details
        	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
//	public String create_order(@RequestBody Map<String, Object> data, Principal principal,
//			SubscriptionPlans subscriptionPlans) throws Exception {
//		try {
//			String receiptnumber_string = null;
//			float amount = Float.parseFloat(data.get("amount").toString());
//			RazorpayClient razorpay = new RazorpayClient("rzp_test_icIfOJXJUlRjph", "L90mE03ZqQXO5rgWxRnn8JCn");
//			List<Payment_Order_Info> payment_Order_Infos = orderDao.findAll();
//			if (payment_Order_Infos.size() == 0) {
//				System.out.println("RAZORPAY " + razorpay);
//				JSONObject orderRequest = new JSONObject();
//				orderRequest.put("amount", amount * 100);
//				orderRequest.put("currency", "INR");
//				orderRequest.put("receipt", "TXNIN110092");
////			JSONObject notes = new JSONObject();
////			notes.put("notes_key_1","Tea, Earl Grey, Hot");
////			orderRequest.put("notes",notes);
//
//				Order order = razorpay.Orders.create(orderRequest);
//				System.out.println(order);
//
//				// save order info
//				servicelayer.processing_payment(order, principal);
//				return order.toString();
//
//			} else {
//				String getlastreceipt = orderDao.getLastReceiptNumber();
//				// Regular expression to match the numeric part in the string
//				String numericPart = getlastreceipt.replaceAll("\\D", ""); // Remove all non-digit characters
//				String prefix = "TXNIN";
//				System.out.println(numericPart); // Output: 110092
//				int receiptnumbers_int = Integer.parseInt(numericPart);
//				++receiptnumbers_int;
//				receiptnumber_string = prefix + Integer.toString(receiptnumbers_int);
//				System.out.println("RAZORPAY " + razorpay);
//				JSONObject orderRequest = new JSONObject();
//				orderRequest.put("amount", amount * 100);
//				orderRequest.put("currency", "INR");
//				orderRequest.put("receipt", receiptnumber_string);
////		JSONObject notes = new JSONObject();
////		notes.put("notes_key_1","Tea, Earl Grey, Hot");
////		orderRequest.put("notes",notes);
//
//				Order order = razorpay.Orders.create(orderRequest);
//				System.out.println(order);
//
//				// save order info
//				servicelayer.processing_payment(order, principal);
//				return order.toString();
//			}
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

	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) {
		try {
			// Parse the amount and convert it to paise
			double amount = Double.parseDouble(data.get("amount").toString());
			int amountInPaise = (int) (amount * 100); // Convert to paise

			// Initialize Razorpay Client
			RazorpayClient razorpay = new RazorpayClient("rzp_test_T6FzWD6ntCjZdb", "jF6rNGBdnVN8v3hnead6fZQe");

			// Generate a new receipt number
			String receiptNumber = generateReceiptNumber();

			// Create a Razorpay order request
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", amountInPaise);
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", receiptNumber);

			// Create order via Razorpay API
			Order order = razorpay.Orders.create(orderRequest);
			System.out.println("Order created: " + order);

			// Process payment and save the order info
			servicelayer.processing_payment(order, principal);

			// Return the order as a response
			return order.toString();

		} catch (Exception e) {
			 // Basic exception details
        	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Generates the next receipt number based on the last receipt in the database.
	 * 
	 * @return A new receipt number in the format "TXNINXXXXX".
	 */
//	private String generateReceiptNumber() {
//	    String prefix = "TXNIN";
//	    String lastReceipt = orderDao.getLastReceiptNumber();
//
//	    // Extract numeric part and increment
//	    int nextNumber = (lastReceipt != null && !lastReceipt.isEmpty()) 
//	        ? Integer.parseInt(lastReceipt.replaceAll("\\D", "")) + 1 
//	        : 110092; // Default starting number
//
//	    return prefix + nextNumber;
//	}

	private static final Lock lock = new ReentrantLock();

	private String generateReceiptNumber() {
		lock.lock(); // Acquire the lock
		try {
			String prefix = "TXNIN";
			Integer getLastReceipt = getNextReceiptNumber();
			String lastReceipt = String.valueOf(getLastReceipt);

			int nextNumber = (lastReceipt != null && !lastReceipt.isEmpty())
					? Integer.parseInt(lastReceipt.replaceAll("\\D", "")) + 1
					: 110092;

			return prefix + nextNumber;
		} finally {
			lock.unlock(); // Always release the lock
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public int getNextReceiptNumber() {
		// Update and fetch the next receipt number atomically
		String updateQuery = "UPDATE receipt_tracker SET last_receipt_number = last_receipt_number + 1";
		String selectQuery = "SELECT last_receipt_number FROM receipt_tracker";

		jdbcTemplate.update(updateQuery);
		return jdbcTemplate.queryForObject(selectQuery, Integer.class);
	}

	@GetMapping("/payment")
	public String payment(Principal principal, Model model) {
		try {
			Optional<User> user = userdao.findByEmail(principal.getName());
			User user1 = user.get();
			Optional<Payment_Order_Info> payment_Order_Info = orderDao.findbycompany(user1.getCompany_id());
			List<Payment_Order_Info> payment_Order_Info_upcoming_recharge = orderDao
					.findbycompanyUpcomingRecharges(user1.getCompany_id());
			if (payment_Order_Info.isPresent()) {
				SubscriptionPlans subscriptionPlans = servicelayer.getAllPlans();
				String NSQLVALUE = servicelayer.NsqlConfig("RENEWPLANREMINDER");
				System.out.println(" LIST PLANS " + subscriptionPlans);
				Payment_Order_Info orders = payment_Order_Info.orElse(null);
				if (orders.getStatus().equals("created")) {
					orders.setPaymentId("NA");
					orders.setLicense_number("NA");
					orders.setLicense_status("NA");
				}
				Date expiryDate = orders.getSubscription_expiry_date();

				// Get the current date
				LocalDate currentDate = LocalDate.now();

				// Convert expiryDate to LocalDate
				LocalDate expiryLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				// Calculate the remaining days between the current date and the expiry date
				long remainingDays = Duration.between(currentDate.atStartOfDay(), expiryLocalDate.atStartOfDay())
						.toDays();
				System.out.println("LICENSE STATUS REMAINING DAYS " + remainingDays);
				model.addAttribute("remaingDays", remainingDays);
				model.addAttribute("all_plans", subscriptionPlans);
				model.addAttribute("orders", orders);
				model.addAttribute("NSQLVALUE", Integer.parseInt(NSQLVALUE));
				model.addAttribute("payment_Order_Info_upcoming_recharge", payment_Order_Info_upcoming_recharge);
				System.out.println("ORDERS ROW " + orders);
				return "AdminSubscriptionPayment";
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
				model.addAttribute("payment_Order_Info_upcoming_recharge", payment_Order_Info_upcoming_recharge);
				model.addAttribute("orders", order_Info);
				System.out.println("ORDERS ROW " + order_Info);
				return "AdminSubscriptionPayment";
			}
		} catch (Exception e) {
			 // Basic exception details
        	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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

	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data, User user)
			throws IOException, MessagingException {
		boolean response = servicelayer.update_payment(user, data);
		return ResponseEntity.ok(Map.of("msg", response));
	}

	@GetMapping("/receipt/{orderId}")
	public String receipt(Principal principal, Model model, @PathVariable("orderId") String orderId) {
		String username = principal.getName();
		User user = servicelayer.findByUsername(username);
		Payment_Order_Info payment_Order_Info = servicelayer.findOrderByCompanyId(orderId);
		SubscriptionPlans subscriptionPlans = servicelayer.findSubscriptionPlans();
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
	public String careers() {
		return "careers";
	}
	
	@GetMapping("/ViewTaskList/{id}")
	public String ViewTaskList(@PathVariable("id") Integer id, Model model, Principal principal, HttpSession session) {
	    try {
	        if (principal != null) {
	            UserDetail userDetail = servicelayer.getUserDetailById(id);
	            List<Tasks> taskList = servicelayer.getTasksByUserId(id);
	            System.out.println("TASK LIST SIZE " + taskList.size());
	            
	            model.addAttribute("taskList", taskList);
	            model.addAttribute("userdetail", userDetail);
	            model.addAttribute("taskCounts", servicelayer.getTaskCounts(userDetail.getId()));
	            model.addAttribute("title", "update form - " + userDetail.getUsername());

	            // 🔥 Add this to pass session message to model
	            Object message = session.getAttribute("message");
	            if (message != null) {
	                model.addAttribute("message", message);
	                session.removeAttribute("message"); // Clear it so it doesn't persist
	            }

	            return "AdminTaskList";
	        } else {
	            throw new Exception("Principal is null");
	        }
	    } catch (Exception e) {
	        // Basic exception details
	    	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = AdminController.class.getName();
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
			return "redirect:/logout";
//			}

		}
	}

		
		  // Show Pending Tasks
	    @GetMapping("/tasks_pending/{userId}")
	    public String showPendingTasks(@PathVariable("userId") Integer userId, Model model, Principal principal) throws Exception {
	        List<Tasks> pendingTasks = servicelayer.getPendingTasksByUserId(userId);
	        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
	        model.addAttribute("taskList", pendingTasks);
	        model.addAttribute("userdetail", userDetail);
	        return "AdminTaskList";
	    }
	    
	    // Show Overdue Tasks
	    @GetMapping("/tasks_inprogress/{userId}")
	    public String showInProgressTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
	        List<Tasks> overdueTasks = servicelayer.getInProgressTasksByUserId(userId);
	        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
	        model.addAttribute("taskList", overdueTasks);
	        model.addAttribute("userdetail", userDetail);
	        return "AdminTaskList";
	    }
	    
	    

	    // Show Completed Tasks
	    @GetMapping("/tasks_completed/{userId}")
	    public String showCompletedTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
	        List<Tasks> completedTasks = servicelayer.getCompletedTasksByUserId(userId);
	        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
	        model.addAttribute("taskList", completedTasks);
	        model.addAttribute("userdetail", userDetail);
	        return "AdminTaskList";
	    }

	    // Show Overdue Tasks
	    @GetMapping("/tasks_overdue/{userId}")
	    public String showOverdueTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
	        List<Tasks> overdueTasks = servicelayer.getOverdueTasksByUserId(userId);
	        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
	        model.addAttribute("taskList", overdueTasks);
	        model.addAttribute("userdetail", userDetail);
	        return "AdminTaskList";
	    }
	    
	    // Show Overdue Tasks
	    @GetMapping("/tasks_all/{userId}")
	    public String showAllTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
	        List<Tasks> overdueTasks = servicelayer.getTasksByUserId(userId);
	        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
	        model.addAttribute("taskList", overdueTasks);
	        model.addAttribute("userdetail", userDetail);
	        return "AdminTaskList";
	    }
	    
	    
	    @GetMapping("/editViewTaskList/{id}")
	    public String EditTaskStatus(@PathVariable("id") String id, Model model, Principal principal, HttpSession session) {
	        try {
	            if (principal != null) {
	                Optional<Tasks> taskOptional = servicelayer.editTask(id);
	                
	                if (taskOptional.isEmpty()) {
	                    session.setAttribute("message",
	                            new Message("Unable to locate the task for update. Please try again or contact admin.", "alert-warning"));
	                    return "AdminEditTaskStatus";
	                }

	                Tasks task = taskOptional.get();
	                UserDetail userDetail = servicelayer.getUserDetailById(task.getId());

	                model.addAttribute("task", task);
	                model.addAttribute("userdetail", userDetail);

	                return "AdminEditTaskStatus";
	            } else {
	                throw new Exception("User session expired. Principal is null.");
	            }
	        } catch (Exception e) {
		        // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            session.setAttribute("message",
	                    new Message("An error occurred while fetching the task details. Please try again.", "alert-danger"));
	            return "AdminEditTaskStatus";
	        }
	    }
	    
		 // Handle Task Status Update
	    @PostMapping("/updateTaskStatus/{taskId}")
	    public String updateTaskStatus(@PathVariable("taskId") String taskId
	    		,@RequestParam("id") int id, @RequestParam("taskStatus") String taskStatus, HttpSession session) {
	    	try
	    	{
	        servicelayer.updateTaskStatus(taskId, taskStatus);
	        session.setAttribute("message",
                    new Message("Task ID: " + taskId + " has been updated successfully.", "alert-success"));
	        return "redirect:/admin/ViewTaskList/"+id; // Redirect to task list after update
	    	}
	    	catch (Exception e) {
	    		 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
	    		   session.setAttribute("message", new Message("Task Update Failed!!: " + e.getMessage(), "alert-danger"));
	   	        return "redirect:/admin/ViewTaskList/"+id; // Redirect to task list after update
			}
	    }
	    
	    @GetMapping("/ViewTaskInfo/{id}")
	  		public String ViewTaskInfo(@PathVariable("id") String id, Model model, Principal principal) {
	  		    try {
	  		        if (principal != null) {
	  		          Optional<Tasks> getOptional = servicelayer.editTask(id);
	  	              Tasks task = getOptional.get();
	  	             UserDetail userDetail = servicelayer.getUserDetailById(task.getId());
	  		            model.addAttribute("task",task);
	  		            model.addAttribute("userdetail",userDetail);
	  		            return "AdminViewTask"; 
	  		        } else {
	  		            throw new Exception("Principal is null");
	  		        }
	  		    } catch (Exception e) {
	  		        // Basic exception details
	  		        String exceptionAsString = e.toString();
	  		        String className = AdminController.class.getName();
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
	  				return "redirect:/logout";
//	  				}

	  			}
	  		}
	  	    
	    
	    @GetMapping("/searchTasks")
	    public ResponseEntity<List<Map<String, Object>>> searchTasks(@RequestParam("query") String query,User user) {
	        List<Tasks> tasks = servicelayer.searchTasksByTaskId(query,user.getId(),"ROLE_ADMIN");
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

	        List<Map<String, Object>> taskSuggestions = new ArrayList<>();
	        for (Tasks task : tasks) {
	            Map<String, Object> taskData = new HashMap<>();
	            taskData.put("taskId", task.getTaskId());
	            taskData.put("taskDescription", task.getTaskDescription());
	            taskData.put("whoAssignedTask", task.getWhoAssignedTask());

	            // 🔹 Format Dates Correctly
	            taskData.put("taskAssignedDate", formatDate(task.getTaskAssignedDate(), formatter));
	            taskData.put("taskEndDate", formatDate(task.getTaskEndDate(), formatter));

	            taskData.put("taskPending", task.isTaskPending());
	            taskData.put("taskOverdue", task.isTaskOverdue());
	            taskData.put("taskCompleted", task.isTaskCompleted());
	            taskData.put("taskInProgress", task.isTaskInProgress());

	            taskSuggestions.add(taskData);
	        }

	        return ResponseEntity.ok(taskSuggestions);
	    }


	    // 🔹 Utility Method for Date Formatting
	    private String formatDate(Date date, DateTimeFormatter formatter) {
	        if (date == null) return "";
	        LocalDateTime localDateTime = date.toInstant()
	                .atZone(ZoneId.systemDefault())
	                .toLocalDateTime();
	        return localDateTime.format(formatter);
	    }
	    
	    @GetMapping("/calendar")
	    public String calendarPage(Principal principal, Model model) throws Exception {
	    	if(principal == null)
	    	{
	    		throw new Exception("Something Went Wrong");
	    	}
	    	User user=servicelayer.findByUsername(principal.getName());
	    	System.out.println("CALENDER GET");
	    	model.addAttribute("user",user);
	        return "AdminAttendanceCalender";
	    }

	    @GetMapping("/events")
	    @ResponseBody
	    public List<Map<String, Object>> getAttendanceEvents(Principal principal) throws Exception {
	    	System.out.println("getAttendanceEvents");
	    	if(principal == null)
	    	{
	    		throw new Exception("Something Went Wrong");
	    	}
	    	User user=servicelayer.findByUsername(principal.getName());
	        List<Map<String, Object>> events = new ArrayList<>();

	        List<Attendance> attendances = servicelayer.getAllAttendanceById(user.getId());
	        for (Attendance att : attendances) {
	            Map<String, Object> event = new HashMap<>();
	            event.put("date", att.getDate().toString());
	            event.put("status", att.getStatus());
	            event.put("name", null);
	            event.put("inTime", att.getCheckInTime() != null ? att.getCheckInTime().toString() : "--");
	            event.put("outTime", att.getCheckOutTime() != null ? att.getCheckOutTime().toString() : "--");
	            event.put("totalBreak", att.getTotalBreak() != 0 ? att.getTotalBreak() : "0");
	            event.put("hoursWorked", att.getHoursWorked() != null ? att.getHoursWorked().toString() : "--");
	            event.put("shift", att.getShift() != null ? att.getShift() : "--");
	            event.put("wfh_request", att.isWfh_request());
	            event.put("leave_request", att.isLeave_request());
	            event.put("withdrawn_request", att.isWithdrawnRequest());
	            events.add(event);
	            System.out.println("getAllAttendanceById : "+event);
	        }

	        List<Holiday> holidays = servicelayer.getAllHolidaysByCompanyInfo(user.getCompany_id());
	        for (Holiday h : holidays) {
	            Map<String, Object> event = new HashMap<>();
	            event.put("date", h.getDate().toString());
	            event.put("status", h.getType());
	            event.put("name", h.getName());
	            events.add(event);
	            System.out.println("getAllHolidaysByCompanyInfo : "+events);
	        }

	        return events;
	    }
	    
	    @GetMapping("/attendance/status")
	    @ResponseBody
	    public Map<String, Object> checkAttendanceStatus(Principal principal) {
	        return servicelayer.getTodayStatus(principal.getName());
	    }

	    @PostMapping("/checkin")
	    @ResponseBody
	    public ResponseEntity<String> checkIn(Principal principal) {
	    	try
	    	{
	        return servicelayer.handleCheckIn(principal.getName());
	    	}
	    	catch (Exception e) {
	    		 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
			return null;
			}
	    }

	    @PostMapping("/checkout")
	    @ResponseBody
	    public ResponseEntity<String> checkOut(Principal principal) {
	        return servicelayer.handleCheckOut(principal.getName());
	    }
	    
	    @PostMapping("/update-attendance")
	    public ResponseEntity<String> updateAttendance(
	        @RequestBody Map<String, String> payload,
	        User user
	    ) {
	        try {
	            String date = payload.get("date");
	            String status = payload.get("status");
	            String inTime = payload.get("inTime");
	            String outTime = payload.get("outTime");

	            LocalDate parsedDate = LocalDate.parse(date);
	            LocalTime parsedInTime = LocalTime.parse(inTime);
	            LocalTime parsedOutTime = LocalTime.parse(outTime);

	            servicelayer.updateAttendance(parsedDate, parsedInTime, parsedOutTime, status, user);

	            return ResponseEntity.ok("✅ Attendance updated successfully!");
	        } catch (Exception e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
	            
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // ✅ Show actual error
	        }
	    }

	    @PostMapping("/submit-leave")
	    public ResponseEntity<String> submitLeave(
	        @RequestBody Map<String, String> payload,
	        User user
	    ) {
	        try {
	            String reason = payload.get("reason");
	            String dateStr = payload.get("leaveDate");
	            String status = payload.get("status");

	            LocalDate leaveDate = LocalDate.parse(dateStr);
	          servicelayer.submitLeave(reason, leaveDate, status, user);

	            return ResponseEntity.ok("✅ Leave submitted successfully!");
	        } catch (Exception e) {
	            e.printStackTrace(); // shows in console
	            return ResponseEntity
	                   .status(HttpStatus.BAD_REQUEST)
	                   .body(e.getMessage());  // ✅ Send real message to JS
	        }
	    }
	    

	    @GetMapping("/approval-requests")
	    public String viewApprovalRequests(Principal principal, Model model) throws Exception {
	    	try
	    	{
	        if (principal == null) {
	            throw new Exception("Something Went Wrong");
	        }

	        // Fetch the user (manager or employee) based on the principal (user)
	        User user = userdao.findByEmail(principal.getName())
	                .orElseThrow(() -> new RuntimeException("User not found"));

	        // Get pending requests based on the user role (manager or employee)
	        List<Attendance> pendingRequests = servicelayer.findPendingRequests(user);

	        // Add the pending requests to the model
	        model.addAttribute("requests", pendingRequests);  // Pass the pending requests to the view
	        System.out.println("REQUESTS "+pendingRequests);

	        return "AdminAttendanceReview";  // Return the view name
	    	}
	    	catch (Exception e) {
	    		 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	    		return "AdminAttendanceReview";
			}
	    }
	    
	    
	    // ✅ Approve or Reject a Request
	    @PostMapping("/approve-request")
	    public String approveOrRejectRequest(@RequestParam("request_id") String request_id,
	                                         @RequestParam("approved") boolean approved, Principal principal, User user) {
	    	try
	    	{
	    		System.out.println("ATTENDANCE ID : "+request_id+" , APPROVED : "+approved);
	        servicelayer.processApprovalOrRejection(request_id, approved, user);
	        return "redirect:/admin/approval-requests";
	    	}
	    	catch (Exception e) {
	    		 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	    		return "redirect:/logout";
			}
	    }
	     
	    // 🚀 Request History page
	    @GetMapping("/request-history")
	    public String viewRequestHistory(Model model, Principal principal, User user) throws Exception {
	        List<Attendance> allProcessedRequests = servicelayer.getRequestHistory(user);
	        System.out.println("HISTORY : "+allProcessedRequests);
	        model.addAttribute("requests", allProcessedRequests);
	        return "Admin_Request_History"; // another HTML page showing full approval history
	    }
	    
	    @GetMapping("/summary/{request_type}")
	    public String showLeaveSummary(@PathVariable("request_type") String requestType,
	                                   Model model,
	                                   Principal principal) throws Exception {
	        if (principal == null) {
	            throw new Exception("Unauthorized access");
	        }

	        Optional<User> user = userdao.findByEmail(principal.getName());
	        if (user.isEmpty()) {
	            throw new Exception("User not found");
	        }

	        System.out.println("REQUEST TYPE "+requestType);
	        int employeeId = user.get().getId();
	        Map<String, Integer> summary = servicelayer.getLeaveSummary(employeeId, requestType);

	        model.addAttribute("leaveSummary", summary);
	        model.addAttribute("requestType", requestType);

	        if (requestType.equalsIgnoreCase("Leave")) {
	            return "Admin-leave-summary"; // Thymeleaf view for Leave
	        } else if (requestType.equalsIgnoreCase("WFH")) {
	            return "Admin-wfh-summary"; // Thymeleaf view for WFH
	        } else {
	            throw new Exception("Invalid request type");
	        }
	    }
	    
	    @GetMapping("/calculate-working-days")
	    public ResponseEntity<?> calculateWorkingDays(@RequestParam String startDate,
	                                                  @RequestParam String endDate,
	                                                  @RequestParam String companyId) {
	        try {
	        	System.out.println("ENTERED CALCULATE WORKING DAY");
	            // Convert the start and end dates from String to LocalDate
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	            LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
	            LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);

	            // Fetch holidays for the company in the specified date range
	            List<Holiday> holidays = servicelayer.getCompanyHolidays(companyId, parsedStartDate, parsedEndDate);
	            System.out.println("HOLIDAYS "+holidays);

	            // Fetch the company's weekend days
	            List<String> weekendDays = servicelayer.getCompanyWeekendDays(companyId);
                System.out.println("WEEKENDS "+weekendDays);
	            // Calculate the working days by excluding weekends and holidays
	            int workingDays = servicelayer.calculateWorkingDays(parsedStartDate, parsedEndDate, weekendDays, holidays);

	            return ResponseEntity.ok().body(new WorkingDaysAndHolidaysResponse(workingDays, holidays));
	        } catch (Exception e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            return ResponseEntity.status(500).body("Error calculating working days: " + e.getMessage());
	        }
	    }
	    
	    @PostMapping("/submit-wfh-request")
	    public String submitWfhRequest(@RequestParam("dateRange") String dateRange,
	                                     @RequestParam("reason") String reason, @RequestParam("requestType") String requestType,
	                                     @RequestParam(value = "fileUpload", required = false) MultipartFile file, User user,
	                                     RedirectAttributes redirectAttributes) throws Exception {

	        try {
	        	System.out.println("WFH REQUEST TYPE "+requestType);
	            // Call service layer to process the leave request
	            String result = servicelayer.processWfhRequest(dateRange, reason, requestType, file, user);

	            // Add success message to redirect
	            redirectAttributes.addFlashAttribute("success", result);

	        } catch (IllegalArgumentException e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            // Add error message to redirect if exception occurs
	            redirectAttributes.addFlashAttribute("error", e.getMessage());
	        }

	        return "redirect:/admin/summary/WFH";
	    }
	    
	    @PostMapping("/submit-leave-request")
	    public String submitLeaveRequest(@RequestParam("dateRange") String dateRange,
	                                     @RequestParam("reason") String reason, @RequestParam("requestType") String requestType,
	                                     @RequestParam(value = "fileUpload", required = false) MultipartFile file, User user,
	                                     RedirectAttributes redirectAttributes) throws Exception {

	        try {
	            // Call service layer to process the leave request
	            String result = servicelayer.processLeaveRequest(dateRange, reason, requestType, file, user);

	            // Add success message to redirect
	            redirectAttributes.addFlashAttribute("success", result);

	        } catch (IllegalArgumentException e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            // Add error message to redirect if exception occurs
	            redirectAttributes.addFlashAttribute("error", e.getMessage());
	        }

	        return "redirect:/admin/summary/Leave";
	    }

	    @GetMapping("/attendance-request-history")
	    public String viewAttendanceRequestHistory(Model model, Principal principal) {
	        try {
	            User user = userdao.findByEmail(principal.getName()).orElseThrow(() -> new Exception("User not found"));
	            List<Attendance> requests = servicelayer.getGroupedRequestsByEmployee(user.getId());
	            System.out.println("CONTROLLER LISTS REQUESTS : "+ requests);
	            model.addAttribute("requests", requests);
	        } catch (Exception e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            model.addAttribute("error", e.getMessage());
	        }
	        return "Admin-Attendance-Request-History";
	    }


	    @GetMapping("/withdraw-request/{request_id}")
	    public String withdrawRequest(@PathVariable("request_id") String request_id,@RequestParam("sno") int sno,
	                                   Principal principal,
	                                   RedirectAttributes redirectAttributes) {
	        try {
	            servicelayer.withdrawRequest(request_id, sno, principal.getName());
	            redirectAttributes.addFlashAttribute("success", "Request withdrawn successfully.");
	        } catch (Exception e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            redirectAttributes.addFlashAttribute("error", e.getMessage());
	        }
	        return "redirect:/admin/request-history";
	    }

	    @GetMapping("/view-request/{request_id}")
	    public String viewRequestDetails(@PathVariable String request_id, Model model, Principal principal) {
	        try {
	            User user = userdao.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));
	            Attendance request = servicelayer.getRequestSummaryByRequestId(request_id, user);
	            model.addAttribute("request", request);
	            System.out.println("REQ "+request);
	        } catch (Exception e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            model.addAttribute("error", e.getMessage());
	        }
	        return "Admin-Request-Details";
	    }

	    
	    @PostMapping("/withdraw-request")
	    public String attendanceWithdrawRequest(@RequestParam("request_id") String request_id, Principal principal, RedirectAttributes redirectAttrs) {
	        try {
	            servicelayer.withdrawAttendanceRequest(request_id, principal.getName());
	            redirectAttrs.addFlashAttribute("success", "✅ Request ID " + request_id + " has been successfully withdrawn.");
	        } catch (Exception e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            redirectAttrs.addFlashAttribute("error", "❌ Unable to withdraw the request. Reason: " + e.getMessage());
	        }
	        return "redirect:/admin/view-request/" + request_id;
	    }
	    
	    @PostMapping("/upload-salary-data")
	    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, User user) {
	        try {
	            servicelayer.processSalaryExcel(file, user);
	            redirectAttributes.addFlashAttribute("status", "success");
	            redirectAttributes.addFlashAttribute("message", "✅ Uploaded and processed successfully.");
	        } catch (RuntimeException e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            System.err.println("❌ Error: " + e.getMessage());
	            redirectAttributes.addFlashAttribute("status", "fail");
	            redirectAttributes.addFlashAttribute("message", "❌ Failed to process file: " + e.getMessage());
	        } catch (Exception e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            redirectAttributes.addFlashAttribute("status", "fail");
	            redirectAttributes.addFlashAttribute("message", "❌ An unexpected error occurred: " + e.getMessage());
	        }

	        return "redirect:/admin/admin_payroll"; // Redirect with flash attributes
	    }


	    @GetMapping("/admin_payroll")
	    public String payRollPage(Principal principal, Model model) throws Exception {
	        if (principal == null) {
	            throw new Exception("Something Went Wrong");
	        }

	        User user = servicelayer.findByUsername(principal.getName());
	        model.addAttribute("user", user);

	        // ✅ Fetch upload history and add to model
	        List<UploadHistory> history = servicelayer.findAllByCompanyId(user.getCompany_id()); // implement this service
	        model.addAttribute("uploadHistory", history);

	        System.out.println("CALENDER GET");

	        return "AdminPayroll"; // this should match your Thymeleaf template file name
	    }
	   
	 // 🟢 GET: Show configuration page
	    @GetMapping("/configurable_job")
	    public String showConfigurableJobPage(Principal principal, Model model, RedirectAttributes redirectAttributes) {
	        try {
	            if (principal == null) {
	                redirectAttributes.addFlashAttribute("error", "Unauthorized access.");
	                return "redirect:/login"; // fallback
	            }

	            String email = principal.getName();
	            User user = userdao.findByEmail(email)
	                    .orElseThrow(() -> new RuntimeException("User not found"));

	            String companyId = user.getCompany_id();
	            String jobDescription = "Generate Salary Slip";

	            Optional<Job> jobOpt = servicelayer.findByCompanyAndDescription(companyId, jobDescription);
	            Job job = jobOpt.orElse(new Job());

	            model.addAttribute("job", job);
	            model.addAttribute("isNew", jobOpt.isEmpty());
	            model.addAttribute("companyId", companyId);
	            model.addAttribute("jobDescription", jobDescription);

	            return "AdminConfigurableJob";

	        } catch (Exception e) {
	        	 // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            redirectAttributes.addFlashAttribute("error", "⚠️ Failed to load job config.");
	            return "redirect:/dashboard"; // fallback route
	        }
	    }

	 // 🟡 POST: Handle job config form submission
	    @PostMapping("/configurable_job")
	    public String handleConfigurableJobSubmit(@ModelAttribute Job job,
	                                              Principal principal,
	                                              RedirectAttributes redirectAttributes) {
	        try {
	            if (principal == null) {
	                redirectAttributes.addFlashAttribute("error", "Unauthorized.");
	                return "redirect:/login";
	            }

	            String email = principal.getName();
	            User user = userdao.findByEmail(email)
	                    .orElseThrow(() -> new RuntimeException("User not found"));

	            job.setCompany_id(user.getCompany_id());
	            job.setJob_description("Generate Salary Slip");

	            servicelayer.createOrUpdateJob(job);

	            redirectAttributes.addFlashAttribute("message", "✅ Job configuration saved successfully.");
	            return "redirect:/admin/configurable_job";

	        } catch (Exception e) {
	            // Basic exception details
	        	e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = AdminController.class.getName();
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
				
	            redirectAttributes.addFlashAttribute("error", "❌ Failed to save job configuration.");
	            return "redirect:/admin/configurable_job";
	        }
	    }

}