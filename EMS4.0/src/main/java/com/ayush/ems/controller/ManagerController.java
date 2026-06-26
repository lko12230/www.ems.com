package com.ayush.ems.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
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
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.ayush.ems.dao.AdminDao;
import com.ayush.ems.dao.NSqlConfigDao;
import com.ayush.ems.dao.NotificationDao;
import com.ayush.ems.dao.TeamDao;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.dao.UserDetailDao;
import com.ayush.ems.entities.Admin;
import com.ayush.ems.entities.Attendance;
import com.ayush.ems.entities.Error_Log;
import com.ayush.ems.entities.Holiday;
import com.ayush.ems.entities.Notification;
import com.ayush.ems.entities.Performance;
import com.ayush.ems.entities.TaskPerformance;
import com.ayush.ems.entities.Tasks;
import com.ayush.ems.entities.Team;
import com.ayush.ems.entities.User;
import com.ayush.ems.entities.UserDetail;
import com.ayush.ems.entities.WorkingDaysAndHolidaysResponse;
import com.ayush.ems.helper.Message;
import com.ayush.ems.service.ListEmailService;
import com.ayush.ems.service.Servicelayer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
	private NSqlConfigDao nSqlConfig;
	@Autowired
	private ListEmailService listEmailService;

	@Autowired
	private NotificationDao notificationDao;

	@ModelAttribute
	public void commonData(Model model, Principal principal, HttpServletRequest request) {
	    try {
	        String uri = request.getRequestURI();
	        
	        // 🔁 Skip commonData for SSE endpoints to avoid principal issues
	        if (uri.contains("/manager/notifications/stream")) {
	            return;
	        }

	        if (principal != null && principal.getName() != null) {
	            Optional<User> userOptional = this.userdao.findByEmail(principal.getName());
	            userOptional.ifPresent(user -> model.addAttribute("user", user));
	        }

	    } catch (Exception e) {
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

	      return "ManagerMessageDetails";
		  }
		  catch (Exception e) {
				// TODO: handle exception
		    	  e.printStackTrace();
		    	  return "error";
			}
	  }
	  
	  
	  @PostMapping("/notifications/mark-as-read")
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
	      return "redirect:/manager/notifications/" + messageId + "?success=true";
	  }

	  
	  @PostMapping("/notifications/each-mark-as-read/{message_id}")
	  @ResponseBody
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
	          e.printStackTrace();
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
	          for (Holiday h : holidays) {
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
	          return "ManagerHome";

	      } catch (Exception e) {
	          e.printStackTrace();
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

	@GetMapping("/swrr")
	public String swr() {
		return "SomethingWentWrong";
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
			return "ManagerPerformance";
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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

	@GetMapping("/employee_leave_policy")
	public String Employee_Leave_Policy() {
		{
			try
			{
		return "employeeleavepolicy4";
			}
			catch (Exception e) {
		        // Basic exception details
		        String exceptionAsString = e.toString();
		        String className = ManagerController.class.getName();
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
//				}

			}
		}
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
				model.addAttribute("all_inbound_records", user_page);
				model.addAttribute("currentPage", page);
				model.addAttribute("totalPages", user_page.getTotalPages());
				model.addAttribute("selectedPageSize", pageSize); // For Thymeleaf page size selection
				model.addAttribute("pageSizes", List.of(10, 20, 50, 100, 500)); // Possible page sizes
				model.addAttribute("sort", sort); // Pass sorting parameter to maintain state on frontend

				System.out.println("IN");
				return "ManagerViewAllEmployees";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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

	@GetMapping("/emp_profile_edit_1/{id}")
	public String profile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				UserDetail userDetail = servicelayer.getUserDetailById(id); // Service layer call
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
				return "ManagerViewEmpProfile";
			} else {
				throw new Exception("Principal is null");
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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
				Map<String, Object> data = servicelayer.getUserDetailAndTeams(id);
				model.addAttribute("userdetail", data.get("userDetail"));
				model.addAttribute("teams", data.get("teams"));
				model.addAttribute("title", "update form - " + ((UserDetail) data.get("userDetail")).getUsername());
				return "ManagerEditEmpProfie";
			} else {
				throw new Exception("Principal is null");
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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

	@GetMapping("/team_emp_profile_edit_2/{id}")
	public String edit_emp_team_profile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
				UserDetail userDetail = userOptional.get();
				List<Team> teams = teamdao.findAll();
				model.addAttribute("teams", teams);
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "ManagerTeamEditProfile";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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

//	@GetMapping("/profile/{id}")
//	public String profile(@PathVariable("id") Integer id, Model model) {
//		System.out.println("IN");
//		Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
//		UserDetail userDetail = userOptional.get();
//		model.addAttribute("userdetail", userDetail);
//		model.addAttribute("title", "update form - " + userDetail.getUsername());
//		return "profile1";
//	}

	@GetMapping("/user-image/{id}")
	public ResponseEntity<byte[]> getUserImage(@PathVariable Integer id) throws Exception {
		System.out.println("ENTERED IMAGE METHOD");
	    byte[] image = servicelayer.downloadImageByUserId(id);
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.IMAGE_JPEG); // or detect type dynamically
	    return new ResponseEntity<>(image, headers, HttpStatus.OK);
	}
	
	@GetMapping("/manager_profile_edit_1/{id}")
	public String yourProfileeeee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findByIdField(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "ManagerViewProfile";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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

	@GetMapping("/manager_profile_edit_2/{id}")
	public String yourProfileee(@PathVariable("id") Integer id, Model model, Principal principal) {
	    try {
	        if (principal == null) throw new Exception("Unauthenticated access");

	        User user = servicelayer.loadProfile(id, model);  // ✅ get the updated user
	        model.addAttribute("user", user);                 // ✅ add it to the model again
	        model.addAttribute("title", "Update Form - " + user.getUsername());

	        return "ManagerEditProfile";

	    } catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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

	@GetMapping("/fetchTeamDetails")
	public ResponseEntity<Map<String, String>> fetchTeamDetails(@RequestParam("teamId") String teamId,
	                                                            @RequestParam("companyId") String companyId) {
	    try {
	        Map<String, String> teamDetails = servicelayer.getTeamDetails(teamId, companyId);
	        return ResponseEntity.ok(teamDetails);
	    } catch (Exception e) {
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
	        String errorMessage = e.getMessage();

	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        String fullStackTrace = sw.toString();

	        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
	        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
	        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

	        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber, fullStackTrace);

	        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
	        System.err.println(fullStackTrace);

	        // Option 1: Redirect user to logout (only works if client handles 302 properly)
	        return ResponseEntity.status(HttpStatus.FOUND)
	                             .header("Location", "/logout")
	                             .build();

	        // OR

	        // Option 2: Send JSON error
	        /*
	        Map<String, String> error = new HashMap<>();
	        error.put("error", "Session expired or error occurred.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	        */
	    }
	}



	@GetMapping("/ChangeCurrentPassword")
	public String changepassword() {
		try
		{
		return "ManagerChangeCurrentPassword";
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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
			@RequestParam("newconfirmpassword") String newconfirmpassword, HttpSession session) {
		try
		{
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
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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

	@PostMapping("/update_emp_Profile/{id}")
	public String assignTeam(@PathVariable int id, UserDetail userDetail, HttpSession session, Principal principal) {
		try
		{
	    try {
	        boolean isUpdated = servicelayer.updateUserTeam(id, userDetail, principal.getName());
	        if (isUpdated) {
	            session.setAttribute("message", new Message("Employee Details Successfully Updated !!", "alert-success"));
	        } else {
	            session.setAttribute("message", new Message("Team ID is not valid !!", "alert-danger"));
	        }
	    } catch (Exception e) {
	        session.setAttribute("message", new Message("Something went wrong !! : " + e.getMessage(), "alert-danger"));
	    }
	    return "redirect:/manager/emp_profile_edit_1/" + id;
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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


	@GetMapping("/seperation")
	public String Seperation() {
		try
		{
		return "ManagerSeperation";
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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

		}	}

	@PostMapping("/withdrawn_request/{id}")
	public String Withdrawn_Request(@PathVariable("id") Integer id, HttpSession session) {
		try
		{
	    try {
	        String result = servicelayer.withdrawResignationRequestWithEmail(id);
	        if (result.equals("success")) {
	            session.setAttribute("message", new Message("Your Resignation Request Has Been Withdrawn Successfully ", "alert-success"));
	        } else {
	            session.setAttribute("message", new Message("Something Went Wrong !! Resignation Cannot Be Withdrawn", "alert-danger"));
	        }
	        return "ManagerSeperation";
	    } catch (NoSuchElementException e) {
	        session.setAttribute("message", new Message("Something Went Wrong !! " + e.getMessage(), "alert-danger"));
	        e.printStackTrace();
	        return "AdminSeperation";
	    } catch (Exception e) {
	        session.setAttribute("message", new Message("Unexpected Error: " + e.getMessage(), "alert-danger"));
	        return "ManagerSeperation";
	    }
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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

	@PostMapping("/processing_seperation_by_manager/{id}")
	public String processing_seperation_by_manager(@PathVariable("id") Integer id, Model model, Principal principal,
			HttpSession session) throws InterruptedException, ExecutionException {
		Optional<User> result2 = userdao.findByIdField(id);
		User user1 = result2.get();
		System.out.println("Admin FindById 1 " + user1);
		String adminId = user1.getAdmin_id();
		System.out.println("Admin FindById 2 " + adminId);
		int typeCastAdminId = Integer.parseInt(adminId);
		System.out.println("Admin FindById 3 " + typeCastAdminId);
		Optional<Admin> admin = adminDao.findByAdminId(typeCastAdminId);
		Admin admin1 = admin.get();
		System.out.println("Admin FindById 4 " + typeCastAdminId);
		String to = user1.getEmail();
		String cc = admin1.getEmail();
		String cc1 = principal.getName();
		System.out.println("CC1 " + cc1 + " CC " + cc);
		if (user1.isResignationRequestApplied() == true && user1.isSeperation_manager_approved() == false) {
			boolean isUpdate = servicelayer.processing_seperation_by_manager(id, user1);
			if (isUpdate) {

				String subject = "Resignation Accepted - EMPID: " + user1.getId();
				String message = "" + "<!DOCTYPE html>" + "<html lang='en'>" + "<head>" + "    <meta charset='UTF-8'>"
						+ "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
						+ "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + "    <style>"
						+ "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; }"
						+ "        .email-container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }"
						+ "        .header { background-color: #007BFF; color: #ffffff; padding: 20px; text-align: center; border-top-left-radius: 8px; border-top-right-radius: 8px; }"
						+ "        .header h1 { margin: 0; font-size: 22px; }"
						+ "        .content { padding: 20px; color: #333333; line-height: 1.5; }"
						+ "        .content p { margin: 10px 0; font-size: 16px; }"
						+ "        .content .highlight { font-weight: bold; color: #007BFF; }"
						+ "        .footer { background-color: #f1f1f1; padding: 10px 20px; text-align: center; color: #888888; font-size: 12px; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; }"
						+ "    </style>" + "</head>" + "<body>" + "    <div class='email-container'>"
						+ "        <div class='header'>" + "            <h1>Resignation Accepted</h1>"
						+ "        </div>" + "        <div class='content'>" + "            <p>Dear "
						+ user1.getUsername() + ",</p>"
						+ "            <p>This email is to formally acknowledge the acceptance of your resignation request. Your last working day has been confirmed as <span class='highlight'>"
						+ user1.getLastWorkingDay() + "</span>.</p>"
						+ "            <p>We would like to express our deepest gratitude for the contributions you have made during your tenure with the organization. Your efforts and dedication have left a significant impact, and you will be greatly missed.</p>"
						+ "            <p>We wish you continued success in all your future endeavors. If there is anything we can assist you with during your transition, please do not hesitate to let us know.</p>"
						+ "            <p>Best regards,</p>" + "            <p><strong>HR Team</strong></p>"
						+ "        </div>" + "        <div class='footer'>"
						+ "            <p>If you have any questions or need further assistance, feel free to contact the HR department.</p>"
						+ "        </div>" + "    </div>" + "</body>" + "</html>";
				 List<String> Emails = userDetailDao.findEmailsByBaseLocationAndRoles(user1.getBase_location(), user1.getCompany_id(), user1.getId());

		            // Sending email to HR (assuming you're passing HR emails as cc)
		            CompletableFuture<Boolean> flagFuture = listEmailService.sendEmail(message, subject, to, cc, Emails);
		            
		            List<UserDetail> getInfo = userDetailDao.findIdsByBaseLocationAndRoles(user1.getBase_location(), user1.getCompany_id(), user1.getId());

		            if (flagFuture.get()) {
		            	  for(UserDetail detail : getInfo)
		                  {
		            		 	System.out.println("NOTIFICATION SENDING TO EMP ID: "+detail.getId());
			                Notification notification = new Notification();
			                notification.setMessage_id(servicelayer.generateMessageId());
			                notification.setMessage("Separation Request Withdrawn By EMPID: " + user1.getId());
			                notification.setUserId(detail.getId());
			                notification.setTimestamp(new Date());
			                notification.setAdddate(new Date());
			                notification.setAddwho("SYSTEM");
			                notification.setEditdate(new Date());
			                notification.setEditwho(detail.getEditwho());
			                notificationDao.save(notification);
			                NotificationListener.send(detail.getEmail(), notification);
			                System.out.println("Sending notification to: " + detail.getEmail());

		                  }
		            } else {
		                System.out.println("Email failed to send.");
		            }


			}
		}
		session.setAttribute("message", new Message("Success", "alert-danger"));
		Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
		UserDetail userDetail = userOptional.get();
		model.addAttribute("userdetail", userDetail);
		return "emp_profile3.0";
	}

	@PostMapping("/team_processing_seperation_by_manager/{id}")
	public String teamProcessingSeperation(@PathVariable("id") Integer id, Model model,
	                                       Principal principal, HttpSession session) throws InterruptedException, ExecutionException {

	    boolean result = servicelayer.processSeparationByManager(id, principal.getName());

	    Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
	    userOptional.ifPresent(userDetail -> model.addAttribute("userdetail", userDetail));

	    if (result) {
	        session.setAttribute("message",
	                new Message("EMP ID " + id + " Seperation Request Has Been Approved", "alert-success"));
	    } else {
	        session.setAttribute("message",
	                new Message("EMP ID " + id + " Seperation Request Has Not Been Approved", "alert-danger"));
	    }

	    return "ManagerTeamViewProfile";
	}

	
	@GetMapping("/team_employee_seperation_details/{id}")
	public String team_emp_seperation_details(@PathVariable("id") Integer id, Model model, Principal principal,
			HttpSession session) {
		try {
			// Ensure the user is logged in
			if (principal != null) {
				System.out.println("Accessing employee details... " + id);

				// Fetch the employee details for the given ID
				Optional<UserDetail> userDetailOptional = this.userDetailDao.findByIdField(id);

				// Fetch the logged-in user's details
				Optional<User> loggedInUserOptional = userdao.findByEmail(principal.getName());

				// Ensure both userDetail and logged-in user exist
				if (userDetailOptional.isPresent() && loggedInUserOptional.isPresent()) {
					UserDetail userDetail = userDetailOptional.get();
					User loggedInUser = loggedInUserOptional.get();

					System.out.println(
							"Logged-in User ID: " + loggedInUser.getId() + ", Employee ID: " + userDetail.getId());

					// Restrict self-approval
					if (loggedInUser.getId().equals(userDetail.getId()) && userDetail.isResignationRequestApplied()
							&& userDetail.isSeperation_manager_approved() == false) {
						session.setAttribute("message",
								new Message(
										"You cannot approve your own separation request. Please contact HR/Admin Team.",
										"alert-danger"));
						return "redirect:/manager/teamprofile/" + userDetail.getId(); // Redirect to a relevant page
					}

					// Fetch teams for the dropdown if required
					List<Team> teams = teamdao.findAll();
					model.addAttribute("teams", teams);

					// Add user and employee details to the model
					model.addAttribute("userdetail", userDetail);
					model.addAttribute("user", loggedInUser);
					model.addAttribute("title", "Update Employee Info - " + userDetail.getUsername());

					// Return the approval page
					return "ManagerTeamSeperationRequestIApproved";
				} else {
					session.setAttribute("message", new Message("Employee details not found.", "alert-danger"));
					return "redirect:/error-page"; // Redirect if the user or employee is not found
				}
			} else {
				throw new Exception("Unauthorized access.");
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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
		try
		{
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
	    return "ManagerSeperation";
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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
	
	@GetMapping("/assetpolicy")
	public String AssetPolicy() {
		try
		{
		return "assetpolicy";
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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

	@GetMapping("/profile1/{id}")
	public String yourProfile(@PathVariable("id") Integer id, Model model) {
		try
		{
		System.out.println("IN");
		Optional<User> userOptional = this.userdao.findByIdField(id);
		User userDetail = userOptional.get();
		model.addAttribute("userdetail", userDetail);
		model.addAttribute("title", "update form - " + userDetail.getUsername());
		return "emp_profile2";
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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
	public String ViewTeamMembers(User user, Model model) {
		try
		{
		try {
			// Get user ID
						int id = user.getId();

						// Fetch user details by team
						List<UserDetail> allDetails = servicelayer.findUserByTeam(id);

						// Handle the case where no details are found
						if (allDetails.isEmpty()) {
							// Process and add details to the model
							model.addAttribute("allDetails", allDetails);
							return "ManagerTeamMembers"; // Redirect to a view that shows no team members found
						}

						// Process and add details to the model
						model.addAttribute("allDetails", allDetails);

			return "ManagerTeamMembers";
		} catch (Exception e) {
//			return "redirect: /manager/swr";
			return "redirect:/signin?expiredsession=true";
		}
		}
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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
		  try
		  {
	        // If no message was passed, fallback message
	        if (errorMessage == null || errorMessage.isEmpty()) {
	            errorMessage = "🚫 Access Denied! You do not have permission to view this page.";
	        }
	        model.addAttribute("errorMessage", errorMessage);
	        return "ManagerError403"; // this points to a Thymeleaf file: access-denied.html
		  }
		  catch (Exception e) {
		        // Basic exception details
		        String exceptionAsString = e.toString();
		        String className = ManagerController.class.getName();
		        String errorMessage1 = e.getMessage();

		        // Capture full stack trace as a string
		        StringWriter sw = new StringWriter();
		        e.printStackTrace(new PrintWriter(sw));
		        String fullStackTrace = sw.toString();

		        // Extract top stack trace element (if available)
		        StackTraceElement topElement = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
		        String methodName = topElement != null ? topElement.getMethodName() : "N/A";
		        int lineNumber = topElement != null ? topElement.getLineNumber() : -1;

		        // Log error to database
		        servicelayer.insert_error_log(exceptionAsString, className, errorMessage1, methodName, lineNumber, fullStackTrace);

		        // Console log for quick debugging
		        System.err.printf("Error in %s.%s at line %d%n", className, methodName, lineNumber);
		        System.err.println(fullStackTrace);
				return "redirect:/logout";
//				}

			}
	    }
	  
	@RequestMapping("/teamprofile/{id}")
	public String teamprofile(@PathVariable("id") Integer id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
		try
		{
		System.out.println("IN");
		Optional<User> get_user = this.userdao.findByEmail(principal.getName());
		User get_user1 = get_user.get();
//		Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
		  // 1. Get the logged-in username
        String loggedInUsername = principal.getName();
		 // 2. Fetch the logged-in user from DB
        User loggedInUser = servicelayer.findByUsername(loggedInUsername); // or fetch by email if applicable
		Optional<UserDetail> userOptional = this.userDetailDao.findIfSameTeam(id, loggedInUser.getId());
		  if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "🚫 Unauthorized access! You are not allowed to view this content.");
            return "redirect:/manager/access-denied";
        }
		UserDetail userDetail = userOptional.get();
		model.addAttribute("userdetail", userDetail);
		model.addAttribute("get_user", get_user1);
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
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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

	@RequestMapping("/termination/{id}")
	public String Termination(@PathVariable("id") Integer id, HttpSession session, Principal principal) {
		try
		{
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
		catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = UserController.class.getName();
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
	            return "redirect:/manager/manager_profile_edit_1/" + user.getId();
	        }

	        // Validate resume size
	        if (resumeFile != null && !resumeFile.isEmpty() && resumeFile.getSize() > maxResumeBytes) {
	            session.setAttribute("message", new Message(
	                    String.format("❌ Resume %.2f MB exceeds allowed %.2f MB",
	                            resumeFile.getSize() / (1024.0 * 1024.0),
	                            maxResumeBytes / (1024.0 * 1024.0)),
	                    "alert-danger"));
	            return "redirect:/manager/manager_profile_edit_1/" + user.getId();
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
	        return "redirect:/manager/manager_profile_edit_1/" + user.getId();

	    } catch (Exception e) {
	        e.printStackTrace();
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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
	        return "redirect:/manager/manager_profile_edit_1/" + user.getId();
	    }
	}




	@GetMapping("/assignTeamMembersOnly")
	public String AssignTeamMembersOnly(User user, Model model) {
		try {
			// Get user ID
						int id = user.getId();

						// Fetch user details by team
						List<UserDetail> allDetails = servicelayer.findUserByTeam(id);

						// Handle the case where no details are found
						if (allDetails.isEmpty()) {
							// Process and add details to the model
							model.addAttribute("allDetails", allDetails);
							return "ManagerAssignTask"; // Redirect to a view that shows no team members found
						}

						// Process and add details to the model
						model.addAttribute("allDetails", allDetails);

			return "ManagerAssignTask";
		} catch (Exception e) {
		        // Basic exception details
		        String exceptionAsString = e.toString();
		        String className = ManagerController.class.getName();
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
			return "redirect:/signin?expiredsession=true";
		}
	}
	
	@GetMapping("/managerViewTask/{id}")
	public String ManagerViewTask(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				UserDetail userDetail = servicelayer.getUserDetailById(id); // Service layer call
				  List<Tasks> taskList = servicelayer.getTasksByUserId(id);
				    System.out.println("TASK LIST SIZE "+taskList.size());
				    model.addAttribute("taskList", taskList);
				model.addAttribute("userdetail", userDetail);
				  model.addAttribute("taskCounts", servicelayer.getTaskCounts(userDetail.getId()));
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "ManagerViewTask";
			} else {
				throw new Exception("Principal is null");
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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
	
	@GetMapping("/managerEditAssignTask/{id}")
	public String ManagerEditAssignTask(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				UserDetail userDetail = servicelayer.getUserDetailById(id); // Service layer call
				  // Fetch tasks assigned to this user
			    List<Tasks> taskList = servicelayer.getTasksByUserId(id);
			    System.out.println("TASK LIST SIZE "+taskList.size());
			    model.addAttribute("taskList", taskList);
				model.addAttribute("userdetail", userDetail);
				  model.addAttribute("taskCounts", servicelayer.getTaskCounts(userDetail.getId()));
				  System.out.println(servicelayer.getTaskCounts(userDetail.getId())+" TASK COUNT");
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "ManagerEditAssignTask";
			} else {
				throw new Exception("Principal is null");
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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
	
	
	@PostMapping("/managerSaveAssignTask/{id}")
	public String ManagerSaveAssignTask(
	        @PathVariable("id") Integer id, 
	        @ModelAttribute UserDetail userDetail, 
	        Model model, 
	        Principal principal,
	        HttpSession session,  
	        @RequestParam("taskAssignedDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date taskAssignedDate,
	        @RequestParam("taskEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date taskEndDate,
	        @RequestParam("taskStatus") String taskStatus
	        ) {
	    try {
	        if (principal == null) {
	            throw new Exception("Principal is null");
	        }

	        System.out.println("Received ID: " + id);
	        System.out.println(taskStatus+" TASk, USERDETAIL OBJECT: " + userDetail);
	        
	        // Ensure userDetail has valid data before proceeding
	        if (userDetail == null || userDetail.getId() == null) {
	            session.setAttribute("message",
	                    new Message("Error: UserDetail is missing or incomplete", "alert-danger"));
	            return "redirect:/manager/assignTask"; // Redirect to a safe page
	        }

	        System.out.println("USERDETAIL ID: " + userDetail.getId());
            User currentUser = servicelayer.findByUsername(principal.getName());
            System.out.println("CURRENT LOGIN "+currentUser.getId());
	        boolean flag = servicelayer.getUserDetailByIdAndSaveTask(userDetail,currentUser,taskStatus);

	        if (flag) {
	            UserDetail userDetail1 = servicelayer.getUserDetailById(id);
	            if (userDetail1 == null) {
	                session.setAttribute("message",
	                        new Message("Error: Could not fetch user details", "alert-danger"));
	                return "redirect:/manager/assignTask"; // Redirect safely
	            }
	            model.addAttribute("userdetail", userDetail1);
	            model.addAttribute("title", "update form - " + userDetail1.getUsername());
	            session.setAttribute("message", new Message("Success !! Task Assigned !!", "alert-success"));
	        } else {
	            session.setAttribute("message",
	                    new Message("Alert !! Something Went Wrong During Task Assigned",
	                            "alert-danger"));
	        }
            
	        return "redirect:/manager/ViewTaskList/" + userDetail.getId();
	    } catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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

	            // ✅ Pass message from session to model (and remove it so it's not repeated)
	            Message message = (Message) session.getAttribute("message");
	            if (message != null) {
	                model.addAttribute("message", message);
	                session.removeAttribute("message");
	            }

	            return "ManagerTaskList";
	        } else {
	            throw new Exception("Principal is null");
	        }
	    } catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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

	
	
	@PostMapping("/deleteTask/{taskId}")
	public String deleteTask(@PathVariable("taskId") String taskId,Tasks tasks,
			RedirectAttributes redirectAttributes, HttpSession httpSession) {
	    try {
	    	System.out.println("DELETE TASK "+taskId);
	    	 // Attempt to delete task
	        boolean isDeleted = servicelayer.deleteTaskById(taskId);
	        if (isDeleted) {
//	            redirectAttributes.addFlashAttribute("success", "The task has been successfully deleted.");
	        	httpSession.setAttribute("message", new Message("The task has been successfully deleted.", "alert-success"));
	        } else {
//	            redirectAttributes.addFlashAttribute("error", "Task deletion failed. It may not exist or has already been removed.");
	        	httpSession.setAttribute("message", new Message("Task deletion failed. It may not exist or has already been removed.", "alert-danger"));
	        }
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Failed to delete task.");
	    }
	    return "redirect:/manager/ViewTaskList/"+tasks.getId(); // Redirect to the task list page
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
        return "redirect:/manager/ViewTaskList/"+id; // Redirect to task list after update
    	}
    	catch (Exception e) {
    		   session.setAttribute("message", new Message("Task Update Failed!!: " + e.getMessage(), "alert-danger"));
   	        return "redirect:/manager/ViewTaskList/"+id; // Redirect to task list after update
		}
    }
    
	 // Handle Task Status Update
    @PostMapping("/updateTaskStatuss/{taskId}")
    public String updateTaskStatuss(@PathVariable("taskId") String taskId
    		,@RequestParam("id") int id, @RequestParam("taskStatus") String taskStatus, HttpSession session) {
    	try
    	{
        servicelayer.updateTaskStatus(taskId, taskStatus);
        return "redirect:/manager/ViewTaskLists/"+id; // Redirect to task list after update
    	}
    	catch (Exception e) {
    		   session.setAttribute("message", new Message("Task Update Failed!!: " + e.getMessage(), "alert-danger"));
   	        return "redirect:/manager/ViewTaskLists/"+id; // Redirect to task list after update
		}
    }
    
    
	
    @GetMapping("/editViewTaskList/{id}")
    public String EditTaskStatus(@PathVariable("id") String id, Model model, Principal principal, HttpSession session) {
        try {
            if (principal != null) {
                Optional<Tasks> taskOptional = servicelayer.editTask(id);
                
                if (taskOptional.isEmpty()) {
                    session.setAttribute("message",
                            new Message("Unable to locate the task for update. Please try again or contact admin.", "alert-warning"));
                    return "ManagerEditTaskStatus";
                }

                Tasks task = taskOptional.get();
                UserDetail userDetail = servicelayer.getUserDetailById(task.getId());

                model.addAttribute("task", task);
                model.addAttribute("userdetail", userDetail);
                
                return "ManagerEditTaskStatus";
            } else {
                throw new Exception("User session expired. Principal is null.");
            }
        } catch (Exception e) {
		        // Basic exception details
		        String exceptionAsString = e.toString();
		        String className = ManagerController.class.getName();
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
            return "ManagerEditTaskStatus";
        }
    }
	
	
	  // Show Pending Tasks
    @GetMapping("/tasks_pending/{userId}")
    public String showPendingTasks(@PathVariable("userId") Integer userId, Model model, Principal principal) throws Exception {
        List<Tasks> pendingTasks = servicelayer.getPendingTasksByUserId(userId);
        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
        model.addAttribute("taskList", pendingTasks);
        model.addAttribute("userdetail", userDetail);
        return "ManagerTaskList";
    }
    
    

    // Show Completed Tasks
    @GetMapping("/tasks_completed/{userId}")
    public String showCompletedTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
        List<Tasks> completedTasks = servicelayer.getCompletedTasksByUserId(userId);
        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
        model.addAttribute("taskList", completedTasks);
        model.addAttribute("userdetail", userDetail);
        return "ManagerTaskList";
    }

    // Show Overdue Tasks
    @GetMapping("/tasks_overdue/{userId}")
    public String showOverdueTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
        List<Tasks> overdueTasks = servicelayer.getOverdueTasksByUserId(userId);
        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
        model.addAttribute("taskList", overdueTasks);
        model.addAttribute("userdetail", userDetail);
        return "ManagerTaskList";
    }
    
    // Show All Tasks
    @GetMapping("/tasks_all/{userId}")
    public String showAllTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
        List<Tasks> overdueTasks = servicelayer.getTasksByUserId(userId);
        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
        model.addAttribute("taskList", overdueTasks);
        model.addAttribute("userdetail", userDetail);
        return "ManagerTaskList";
    }
    
    // Show Pending Tasks
    @GetMapping("/tasks_pendingg/{userId}")
    public String showPendingTaskss(@PathVariable("userId") Integer userId, Model model, Principal principal) throws Exception {
        List<Tasks> pendingTasks = servicelayer.getPendingTasksByUserId(userId);
        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
        model.addAttribute("taskList", pendingTasks);
        model.addAttribute("userdetail", userDetail);
        return "ManagerTaskLists";
    }
    
    

    // Show Completed Tasks
    @GetMapping("/tasks_completedd/{userId}")
    public String showCompletedTaskss(@PathVariable("userId") Integer userId, Model model) throws Exception {
        List<Tasks> completedTasks = servicelayer.getCompletedTasksByUserId(userId);
        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
        model.addAttribute("taskList", completedTasks);
        model.addAttribute("userdetail", userDetail);
        return "ManagerTaskLists";
    }

    // Show Overdue Tasks
    @GetMapping("/tasks_overduee/{userId}")
    public String showOverdueTaskss(@PathVariable("userId") Integer userId, Model model) throws Exception {
        List<Tasks> overdueTasks = servicelayer.getOverdueTasksByUserId(userId);
        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
        model.addAttribute("taskList", overdueTasks);
        model.addAttribute("userdetail", userDetail);
        return "ManagerTaskLists";
    }
    
    // Show All Tasks
    @GetMapping("/tasks_alll/{userId}")
    public String showAllTaskss(@PathVariable("userId") Integer userId, Model model) throws Exception {
        List<Tasks> overdueTasks = servicelayer.getTasksByUserId(userId);
        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
        model.addAttribute("taskList", overdueTasks);
        model.addAttribute("userdetail", userDetail);
        return "ManagerTaskLists";
    }
    
    
    // Show Overdue Tasks
    @GetMapping("/tasks_inprogresss/{userId}")
    public String showInProgressTaskss(@PathVariable("userId") Integer userId, Model model) throws Exception {
        List<Tasks> overdueTasks = servicelayer.getInProgressTasksByUserId(userId);
        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
        model.addAttribute("taskList", overdueTasks);
        model.addAttribute("userdetail", userDetail);
        return "ManagerTaskLists";
    }
    
    
    @GetMapping("/searchTasks")
    public ResponseEntity<List<Map<String, Object>>> searchTasks(@RequestParam("query") String query,Tasks tasks, User user) {
    	System.out.println("TASK WHO ASSIGNED "+tasks.getWhoAssignedTask());
        List<Tasks> get_tasks = servicelayer.searchTasksByTaskId(query,user.getId(),"ROLE_MANAGER");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
System.out.println("get_tasks "+get_tasks.size());
        List<Map<String, Object>> taskSuggestions = new ArrayList<>();
        for (Tasks task : get_tasks) {
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

    
    
    @GetMapping("/searchTaskss")
    public ResponseEntity<List<Map<String, Object>>> searchTaskss(@RequestParam("query") String query, User user) {
        List<Tasks> tasks = servicelayer.searchTasksByTaskId(query,user.getId(),"ROLE_MANAGER_1");
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
    
    
    @GetMapping("/ViewTaskInfo/{id}")
		public String ViewTaskInfo(@PathVariable("id") String id, Model model, Principal principal) {
		    try {
		        if (principal != null) {
		          Optional<Tasks> getOptional = servicelayer.editTask(id);
	              Tasks task = getOptional.get();
	             UserDetail userDetail = servicelayer.getUserDetailById(task.getId());
		            model.addAttribute("task",task);
		            model.addAttribute("userdetail",userDetail);
		            return "ManagerViewTask"; 
		        } else {
		            throw new Exception("Principal is null");
		        }
		    } catch (Exception e) {
		        // Basic exception details
		        String exceptionAsString = e.toString();
		        String className = ManagerController.class.getName();
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
//				}

			}
		}
    
    
    @GetMapping("/ViewTaskInfoo/{id}")
		public String ViewTaskInfoo(@PathVariable("id") String id, Model model, Principal principal) {
		    try {
		        if (principal != null) {
		          Optional<Tasks> getOptional = servicelayer.editTask(id);
	              Tasks task = getOptional.get();
	             UserDetail userDetail = servicelayer.getUserDetailById(task.getId());
		            model.addAttribute("task",task);
		            model.addAttribute("userdetail",userDetail);
		            return "ManagerViewTasks"; 
		        } else {
		            throw new Exception("Principal is null");
		        }
		    } catch (Exception e) {
		        // Basic exception details
		        String exceptionAsString = e.toString();
		        String className = ManagerController.class.getName();
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
//				}

			}
		}
    
    // Show Progress Tasks
    @GetMapping("/tasks_inprogress/{userId}")
    public String showInProgressTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
        List<Tasks> overdueTasks = servicelayer.getInProgressTasksByUserId(userId);
        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
        model.addAttribute("taskList", overdueTasks);
        model.addAttribute("userdetail", userDetail);
        return "ManagerTaskList";
    }
    
    
    @GetMapping("/ViewTaskLists/{id}")
	public String ViewTaskLists(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				UserDetail userDetail = servicelayer.getUserDetailById(id); // Service layer call
				  List<Tasks> taskList = servicelayer.getTasksByUserId(id);
				    System.out.println("TASK LIST SIZE "+taskList.size());
				    model.addAttribute("taskList", taskList);
				model.addAttribute("userdetail", userDetail);
				  model.addAttribute("taskCounts", servicelayer.getTaskCounts(userDetail.getId()));
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "ManagerTaskLists";
			} else {
				throw new Exception("Principal is null");
			}
		} catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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
    
    
    @GetMapping("/editViewTaskLists/{id}")
	public String EditTaskStatuss(@PathVariable("id") String id, Model model, Principal principal) {
	    try {
	        if (principal != null) {
	          Optional<Tasks> getOptional = servicelayer.editTask(id);
              Tasks task = getOptional.get();
             UserDetail userDetail = servicelayer.getUserDetailById(task.getId());
	            model.addAttribute("task",task);
	            model.addAttribute("userdetail",userDetail);
	            return "ManagerEditTaskStatuss"; 
	        } else {
	            throw new Exception("Principal is null");
	        }
	    } catch (Exception e) {
	        // Basic exception details
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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
    
    @PostMapping("/closeTask/{taskId}")
    public String updateTaskStatusClosed(@PathVariable("taskId") String taskId,
                                         @RequestParam("id") int id,
                                         @RequestParam("taskStatus") String taskStatus,
                                         HttpSession session, Principal principal) {
        try {
            System.out.println("TASK CLOSE MANAGER CONTROLLER");

            servicelayer.updateTaskStatusAndClose(taskId, taskStatus,principal.getName());

            session.setAttribute("message", new Message(
                    "Task ID: " + taskId + " has been closed successfully.", "alert-success"));
        } catch (Exception e) {
		        // Basic exception details
		        String exceptionAsString = e.toString();
		        String className = ManagerController.class.getName();
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
            
            session.setAttribute("message", new Message(
                    "Unable to close Task ID: " + taskId + ". Please try again later.", "alert-danger"));
        }

        return "redirect:/manager/ViewTaskList/" + id;
    }

    @PostMapping("/re-openTask/{taskId}")
    public String updateTaskStatusReopen(@PathVariable("taskId") String taskId,
                                         @RequestParam("id") int id,
                                         @RequestParam("taskStatus") String taskStatus,
                                         HttpSession session, Principal principal) {
        try {
        	System.out.println("taskStatus value:" + taskStatus+"||");
//        	System.out.println("TASK REOPEN MANAGER CONTROLLER " + (taskStatus ? "VALUED GET" : "VALUE NOT GET") + " ||");


            servicelayer.updateTaskStatusReopen(taskId, taskStatus, principal.getName());

            session.setAttribute("message", new Message(
                    "Task ID: " + taskId + " has been reopened successfully.", "alert-success"));
        } catch (Exception e) {
        	  // Basic exception details
        	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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
            session.setAttribute("message", new Message(
                    "Unable to reopen Task ID: " + taskId + ". Please try again later.", "alert-danger"));
        }

        return "redirect:/manager/ViewTaskList/" + id;
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
        return "ManagerAttendanceCalender";
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
	        String className = ManagerController.class.getName();
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
	        String className = ManagerController.class.getName();
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
        	  // Basic exception details
        	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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
            return ResponseEntity
                   .status(HttpStatus.BAD_REQUEST)
                   .body(e.getMessage());  // ✅ Send real message to JS
        }
    }



    @GetMapping("/approval-requests")
    public String viewApprovalRequests(Principal principal, Model model) throws Exception {
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

        return "ManagerAttendanceReview";  // Return the view name
    }
    
    
    // ✅ Approve or Reject a Request
    @PostMapping("/approve-request")
    public String approveOrRejectRequest(@RequestParam("request_id") String request_id,
                                         @RequestParam("approved") boolean approved, Principal principal, User user) {
    	try
    	{
    		System.out.println("ATTENDANCE ID : "+request_id+" , APPROVED : "+approved);
        servicelayer.processApprovalOrRejection(request_id, approved, user);
        return "redirect:/manager/approval-requests";
    	}
    	catch (Exception e) {
    		  // Basic exception details
        	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = ManagerController.class.getName();
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
    
    // 🚀 Request History page
    @GetMapping("/request-history")
    public String viewRequestHistory(Model model, Principal principal, User user) throws Exception {
        List<Attendance> allProcessedRequests = servicelayer.getRequestHistory(user);
        System.out.println("HISTORY : "+allProcessedRequests);
        model.addAttribute("requests", allProcessedRequests);
        return "Manager_Request_History"; // another HTML page showing full approval history
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
            return "Manager-leave-summary"; // Thymeleaf view for Leave
        } else if (requestType.equalsIgnoreCase("WFH")) {
            return "Manager-wfh-summary"; // Thymeleaf view for WFH
        } else {
            throw new Exception("Invalid request type");
        }
    }
    
    @GetMapping("/calculate-working-days")
    public ResponseEntity<?> calculateWorkingDays(@RequestParam String startDate,
                                                  @RequestParam String endDate,
                                                  @RequestParam String companyId) {
        try {
            // Convert the start and end dates from String to LocalDate
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
            LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);

            // Fetch holidays for the company in the specified date range
            List<Holiday> holidays = servicelayer.getCompanyHolidays(companyId, parsedStartDate, parsedEndDate);

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
	        String className = ManagerController.class.getName();
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
	        String className = ManagerController.class.getName();
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

        return "redirect:/manager/summary/WFH";
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
	        String className = ManagerController.class.getName();
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

        return "redirect:/manager/summary/Leave";
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
	        String className = ManagerController.class.getName();
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
        return "Manager-Attendance-Request-History";
    }
    
    @GetMapping("/withdraw-request/{request_id}")
    public String withdrawRequest(@PathVariable("request_id") String request_id,@RequestParam("sno") int sno,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            servicelayer.withdrawRequest(request_id, sno, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Request withdrawn successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/Manager/request-history";
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
	        String className = ManagerController.class.getName();
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
            e.printStackTrace();
        }
        return "Manager-Request-Details";
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
	        String className = ManagerController.class.getName();
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
            e.printStackTrace();
        }
        return "redirect:/manager/view-request/" + request_id;
    }
}
