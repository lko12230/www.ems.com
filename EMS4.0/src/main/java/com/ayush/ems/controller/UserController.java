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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.ayush.ems.dao.NSqlConfigDao;
import com.ayush.ems.dao.NotificationDao;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.dao.UserDetailDao;
import com.ayush.ems.entities.Attendance;
import com.ayush.ems.entities.Error_Log;
import com.ayush.ems.entities.Holiday;
import com.ayush.ems.entities.Notification;
import com.ayush.ems.entities.Performance;
import com.ayush.ems.entities.TaskPerformance;
import com.ayush.ems.entities.Tasks;
import com.ayush.ems.entities.User;
import com.ayush.ems.entities.UserDetail;
import com.ayush.ems.entities.WorkingDaysAndHolidaysResponse;
import com.ayush.ems.helper.Message;
import com.ayush.ems.service.Servicelayer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Controller
@RequestMapping("/user")
@SessionScope
public class UserController {
	@Autowired
	private UserDao userdao;
	@Autowired
	private Servicelayer servicelayer;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserDetailDao userDetailDao;
	@Autowired
	private NSqlConfigDao nSqlConfig;
//	@ModelAttribute
//	public void commonData(Model model, Principal principal) {
//		String userName = principal.getName();
//		System.out.println("username " + userName);
//		User user = userdao.findByEmail(userName);
//		System.out.println("user " + user);
//		model.addAttribute("user", user);
//
//	}

	@GetMapping("/ChangeCurrentPassword")
	public String changepassword() {
		return "UserChangeCurrentPassword";
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

	@Autowired
	private NotificationDao notificationDao;

	@ModelAttribute
	public void commonData(Model model, Principal principal, HttpServletRequest request) {
	    try {
	        String uri = request.getRequestURI();
	        
	        // 🔁 Skip commonData for SSE endpoints to avoid principal issues
	        if (uri.contains("/user/notifications/stream")) {
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

	  @GetMapping("/access-denied")
	    public String accessDenied(Model model, @ModelAttribute("errorMessage") String errorMessage) {
	        // If no message was passed, fallback message
	        if (errorMessage == null || errorMessage.isEmpty()) {
	            errorMessage = "🚫 Access Denied! You do not have permission to view this page.";
	        }
	        model.addAttribute("errorMessage", errorMessage);
	        return "UserError403"; // this points to a Thymeleaf file: access-denied.html
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

	      return "UserMessageDetails";
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
	      return "redirect:/user/notifications/" + messageId + "?success=true";
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
	          return "UserHome";

	      } catch (Exception e) {
	          e.printStackTrace();
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

	@GetMapping("/user_profile_edit_1/{id}")
	public String yourProfileeeee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findByIdField(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "UserViewProfile";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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

//	@GetMapping("/admin_profile_edit_2/{id}")
//	public String yourProfileee(@PathVariable("id") Integer id, Model model, Principal principal) {
//		try {
//			if (principal != null) {
//				System.out.println("IN");
//				Optional<User> userOptional = this.userdao.findByIdField(id);
//				User userDetail = userOptional.get();
//				model.addAttribute("userdetail", userDetail);
//				model.addAttribute("title", "update form - " + userDetail.getUsername());
//				return "profile1.0";
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

	@GetMapping("/performance")
	public String performance(Model model, User user, Performance performance, HttpSession httpSession,
			Principal principal) {
		try
		{
			Calendar calendar = Calendar.getInstance();
			int currentYear = calendar.get(Calendar.YEAR);
		if (principal.equals(null)) {
//				throw new Exception("session_invalid_exception");
		}
//		List<Double> chartData = servicelayer.performance(user, httpSession);
//		List<String> chartLabels = Arrays.asList("January", "February", "March", "April", "May", "June", "July",
//				"August", "September", "October", "November", "December"); // Example labels
//		int year = (int) httpSession.getAttribute("year");
//		model.addAttribute("chartData", chartData);
//		model.addAttribute("chartLabels", chartLabels);
//		model.addAttribute("year", year);
		 Map<String, Integer> taskCounts = servicelayer.getAllTaskCounts(user.getId());
	        model.addAttribute("taskCounts", taskCounts);
	        List<TaskPerformance> performanceData = servicelayer.getTaskPerformanceDataForGraph(user.getId());
		    
		    model.addAttribute("performanceData", performanceData);
	    	model.addAttribute("year", currentYear);
		return "UserPerformance";
		}
		catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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
//				}

			}
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

//	List<UserDetail> all_users = new ArrayList<>();
//
//	@GetMapping("/viewMembers")
//	public String viewTeamMembers(Model model, User user, Principal principal) {
//		try {
//			all_users = userDetailDao.findAllEnabledUser();
//			if (all_users != null && user.getUsername() != null) {
//				System.out.println("find all " + all_users);
//				model.addAttribute("all_users", all_users);
//				System.out.println("IN");
//				return "ViewMembers";
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
				return "UserViewAllEmployees";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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
//        return "ViewMembers";
//    }

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
				return "UserViewEmpProfile";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile1";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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


	@GetMapping("/user-image/{id}")
	public ResponseEntity<byte[]> getUserImage(@PathVariable Integer id) throws Exception {
		System.out.println("ENTERED IMAGE METHOD");
	    byte[] image = servicelayer.downloadImageByUserId(id);
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.IMAGE_JPEG); // or detect type dynamically
	    return new ResponseEntity<>(image, headers, HttpStatus.OK);
	}


	@GetMapping("/user_profile_edit_2/{id}")
	public String yourProfileeee(@PathVariable("id") Integer id, Model model, Principal principal) {
	    try {
	        if (principal == null) throw new Exception("Unauthenticated access");

	        User user = servicelayer.loadProfile(id, model);  // ✅ get the updated user
	        model.addAttribute("user", user);                 // ✅ add it to the model again
	        model.addAttribute("title", "Update Form - " + user.getUsername());

	        return "UserEditProfile";

	    } catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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
		try
		{
		return "UserSeperation";
		}
		catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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
	    return "UserSeperation";
		}
		catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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
	        return "UserSeperation";
	    } catch (NoSuchElementException e) {
	        session.setAttribute("message", new Message("Something Went Wrong !! " + e.getMessage(), "alert-danger"));
	        e.printStackTrace();
	        return "UserSeperation";
	    } catch (Exception e) {
	        session.setAttribute("message", new Message("Unexpected Error: " + e.getMessage(), "alert-danger"));
	        return "UserSeperation";
	    }
		}
		catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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

	@RequestMapping("/emp_profile_view/{id}")
	public String profileee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile1.0";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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
							return "UserTeamMembers"; // Redirect to a view that shows no team members found
						}

						// Process and add details to the model
						model.addAttribute("allDetails", allDetails);

			return "UserTeamMembers";
		} catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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
            return "redirect:/user/access-denied";
        }
		UserDetail userDetail = userOptional.get();
		model.addAttribute("get_user", get_user1);
		model.addAttribute("userdetail", userDetail);
		model.addAttribute("title", "update form - " + userDetail.getUsername());
//		if (userDetail.getRole().equals("ROLE_IT")) {
		return "UserTeamViewProfile";
//		} else {
//			return "ManagerNormalRoleViewProfile";
//		}
		}
		catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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



	@GetMapping("/assetpolicy")
	public String AssetPolicy() {
		try
		{
		return "assetpolicy3";
		}
		catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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

	            return "UserTaskList";
	        } else {
	            throw new Exception("Principal is null");
	        }
	    } catch (Exception e) {
	        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//	        String exceptionAsString = e.toString();
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
		
		
		  // Show Pending Tasks
	    @GetMapping("/tasks_pending/{userId}")
	    public String showPendingTasks(@PathVariable("userId") Integer userId, Model model, Principal principal) throws Exception {
	    	try
	    	{
	        List<Tasks> pendingTasks = servicelayer.getPendingTasksByUserId(userId);
	        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
	        model.addAttribute("taskList", pendingTasks);
	        model.addAttribute("userdetail", userDetail);
	        return "UserTaskList";
	    	}
	    	catch (Exception e) {
		        // Basic exception details
        	    e.printStackTrace();
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
//				}

			}
	    }
	    
	    // Show Overdue Tasks
	    @GetMapping("/tasks_inprogress/{userId}")
	    public String showInProgressTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
	    	try
	    	{
	        List<Tasks> overdueTasks = servicelayer.getInProgressTasksByUserId(userId);
	        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
	        model.addAttribute("taskList", overdueTasks);
	        model.addAttribute("userdetail", userDetail);
	        return "UserTaskList";
	    	}
	    	catch (Exception e) {
		        // Basic exception details
        	    e.printStackTrace();
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
//				}

			}
	    }
	    
	    

	    // Show Completed Tasks
	    @GetMapping("/tasks_completed/{userId}")
	    public String showCompletedTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
	    	try
	    	{
	        List<Tasks> completedTasks = servicelayer.getCompletedTasksByUserId(userId);
	        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
	        model.addAttribute("taskList", completedTasks);
	        model.addAttribute("userdetail", userDetail);
	        return "UserTaskList";
	    	}
	    	catch (Exception e) {
		        // Basic exception details
        	    e.printStackTrace();
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
//				}

			}
	    }

	    // Show Overdue Tasks
	    @GetMapping("/tasks_overdue/{userId}")
	    public String showOverdueTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
	    	try
	    	{
	        List<Tasks> overdueTasks = servicelayer.getOverdueTasksByUserId(userId);
	        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
	        model.addAttribute("taskList", overdueTasks);
	        model.addAttribute("userdetail", userDetail);
	        return "UserTaskList";
	    	}
	    	catch (Exception e) {
		        // Basic exception details
        	    e.printStackTrace();
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
//				}

			}
	    }
	    
	    // Show Overdue Tasks
	    @GetMapping("/tasks_all/{userId}")
	    public String showAllTasks(@PathVariable("userId") Integer userId, Model model) throws Exception {
	    	try
	    	{
	        List<Tasks> overdueTasks = servicelayer.getTasksByUserId(userId);
	        UserDetail userDetail = servicelayer.getUserDetailById(userId); // Service layer call
	        model.addAttribute("taskList", overdueTasks);
	        model.addAttribute("userdetail", userDetail);
	        return "UserTaskList";
	    	}
	    	catch (Exception e) {
		        // Basic exception details
        	    e.printStackTrace();
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
//				}

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
	                    return "UserEditTaskStatus";
	                }

	                Tasks task = taskOptional.get();
	                UserDetail userDetail = servicelayer.getUserDetailById(task.getId());

	                model.addAttribute("task", task);
	                model.addAttribute("userdetail", userDetail);
	                return "UserEditTaskStatus";
	            } else {
	                throw new Exception("User session expired. Principal is null.");
	            }
	        } catch (Exception e) {
	        	  // Basic exception details
        	    e.printStackTrace();
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
	            session.setAttribute("message",
	                    new Message("An error occurred while fetching the task details. Please try again.", "alert-danger"));
	            return "UserEditTaskStatus";
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
	        return "redirect:/user/ViewTaskList/"+id; // Redirect to task list after update
	    	}
	    	catch (Exception e) {
			        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//			        String exceptionAsString = e.toString();
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
	    		   session.setAttribute("message", new Message("Task Update Failed!!: " + e.getMessage(), "alert-danger"));
	   	        return "redirect:/user/ViewTaskList/"+id; // Redirect to task list after update
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
	  		            return "UserViewTask"; 
	  		        } else {
	  		            throw new Exception("Principal is null");
	  		        }
	  		    } catch (Exception e) {
			        // Basic exception details
        	    e.printStackTrace();
		        String exceptionAsString = e.toString();
//			        String exceptionAsString = e.toString();
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
//					}

				}
	  		}
	  	    
	    
	    @GetMapping("/searchTasks")
	    public ResponseEntity<List<Map<String, Object>>> searchTasks(@RequestParam("query") String query, User user) {
	        List<Tasks> tasks = servicelayer.searchTasksByTaskId(query,user.getId(),"ROLE_USER");
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
	        return "UserAttendanceCalender";
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
			e.printStackTrace();
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
	            e.printStackTrace();
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
	    // 🚀 Request History page
	    @GetMapping("/request-history")
	    public String viewRequestHistory(Model model, Principal principal, User user) throws Exception {
	        List<Attendance> allProcessedRequests = servicelayer.getRequestHistory(user);
	        System.out.println("HISTORY : "+allProcessedRequests);
	        model.addAttribute("requests", allProcessedRequests);
	        return "User_Request_History"; // another HTML page showing full approval history
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
	            return "User-leave-summary"; // Thymeleaf view for Leave
	        } else if (requestType.equalsIgnoreCase("WFH")) {
	            return "User-wfh-summary"; // Thymeleaf view for WFH
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

	        } catch (IllegalArgumentException ex) {
	            // Add error message to redirect if exception occurs
	            redirectAttributes.addFlashAttribute("error", ex.getMessage());
	        }

	        return "redirect:/user/summary/WFH";
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

	        } catch (IllegalArgumentException ex) {
	            // Add error message to redirect if exception occurs
	            redirectAttributes.addFlashAttribute("error", ex.getMessage());
	        }

	        return "redirect:/user/summary/Leave";
	    }

	    @GetMapping("/attendance-request-history")
	    public String viewAttendanceRequestHistory(Model model, Principal principal) {
	        try {
	            User user = userdao.findByEmail(principal.getName()).orElseThrow(() -> new Exception("User not found"));
	            List<Attendance> requests = servicelayer.getGroupedRequestsByEmployee(user.getId());
	            System.out.println("CONTROLLER LISTS REQUESTS : "+ requests);
	            model.addAttribute("requests", requests);
	        } catch (Exception e) {
	            model.addAttribute("error", e.getMessage());
	        }
	        return "User-Attendance-Request-History";
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
	        return "redirect:/user/request-history";
	    }

	    @GetMapping("/view-request/{request_id}")
	    public String viewRequestDetails(@PathVariable String request_id, Model model, Principal principal) {
	        try {
	            User user = userdao.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));
	            Attendance request = servicelayer.getRequestSummaryByRequestId(request_id, user);
	            model.addAttribute("request", request);
	            System.out.println("REQ "+request);
	        } catch (Exception e) {
	            model.addAttribute("error", e.getMessage());
	        }
	        return "User-Request-Details";
	    }

	    
	    @PostMapping("/withdraw-request")
	    public String attendanceWithdrawRequest(@RequestParam("request_id") String request_id, Principal principal, RedirectAttributes redirectAttrs) {
	        try {
	            servicelayer.withdrawAttendanceRequest(request_id, principal.getName());
	            redirectAttrs.addFlashAttribute("success", "✅ Request ID " + request_id + " has been successfully withdrawn.");
	        } catch (Exception e) {
	            redirectAttrs.addFlashAttribute("error", "❌ Unable to withdraw the request. Reason: " + e.getMessage());
	        }
	        return "redirect:/user/view-request/" + request_id;
	    }
}
