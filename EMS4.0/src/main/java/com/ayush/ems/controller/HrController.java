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
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ayush.ems.config.NotificationListener;
import com.ayush.ems.dao.NSqlConfigDao;
import com.ayush.ems.dao.NotificationDao;
import com.ayush.ems.dao.TeamDao;
import com.ayush.ems.dao.UserDao;
import com.ayush.ems.dao.UserDetailDao;
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
import com.ayush.ems.service.Servicelayer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Controller
@RequestMapping("/hr")
//@Scope(value = WebApplicationContext.SCOPE_SESSION)
@Scope
public class HrController {

	@Autowired
	private UserDao userdao;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private Servicelayer servicelayer;
	@Autowired
	private UserDetailDao userDetailDao;
	@Autowired
	private NotificationDao notificationDao;
	@Autowired
	private TeamDao teamDao;
	@Autowired
	private NSqlConfigDao nSqlConfig;
	@ModelAttribute
	public void commonData(Model model, Principal principal, HttpServletRequest request) {
	    try {
	        String uri = request.getRequestURI();
	        
	        // 🔁 Skip commonData for SSE endpoints to avoid principal issues
	        if (uri.contains("/hr/notifications/stream")) {
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
	
	  @GetMapping("/access-denied")
	    public String accessDenied(Model model, @ModelAttribute("errorMessage") String errorMessage) {
	        // If no message was passed, fallback message
	        if (errorMessage == null || errorMessage.isEmpty()) {
	            errorMessage = "🚫 Access Denied! You do not have permission to view this page.";
	        }
	        model.addAttribute("errorMessage", errorMessage);
	        return "HrError403"; // this points to a Thymeleaf file: access-denied.html
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

	      return "HrMessageDetails";
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
	      return "redirect:/hr/notifications/" + messageId + "?success=true";
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
	          return "HrHome";

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
				return "Unknown Location";
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
//				return "redirect:/hr/hr_profile_edit_1/" + user.getId();
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
//						return "redirect:/hr/hr_profile_edit_1/" + user.getId();
//					} else {
//						session.setAttribute("message",
//								new Message(
//										"Alert !! Profile Not Updated Because Resume Extension Should Be in PDF/WORD",
//										"alert-danger"));
//						return "redirect:/hr/hr_profile_edit_1/" + user.getId();
//					}
//				} else {
//					session.setAttribute("message",
//							new Message("Alert !! Profile Not Updated Because Resume size Should Be Less Than 3MB",
//									"alert-danger"));
//					return "redirect:/hr/hr_profile_edit_1/" + user.getId();
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
//			return "redirect:/hr/hr_profile_edit_1/" + user.getId();
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



	@GetMapping("/hr_profile_edit_1/{id}")
	public String yourProfileee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findByIdField(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "HrViewProfile";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = HrController.class.getName();
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

	@GetMapping("/hr_profile_edit_2/{id}")
	public String yourProfileeee(@PathVariable("id") Integer id, Model model, Principal principal) {
	    try {
	        if (principal == null) throw new Exception("Unauthenticated access");

	        User user = servicelayer.loadProfile(id, model);  // ✅ get the updated user
	        model.addAttribute("user", user);                 // ✅ add it to the model again
	        model.addAttribute("title", "Update Form - " + user.getUsername());

	        return "HrEditProfile";

	    } catch (Exception e) {  // Basic exception details
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
	        System.err.println(fullStackTrace); e.printStackTrace();
	        return "redirect:/error";
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
				return "HrViewAllEmployees";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = HrController.class.getName();
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
//        return "ViewMembers4";
//    }

	@GetMapping("/emp_profile_edit_1/{id}")
	public String profile1(@PathVariable("id") Integer id, Model model, Principal principal) {
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
				return "HrEmpProfile";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = HrController.class.getName();
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

	    return "HrTeamViewProfile";
	}

	@GetMapping("/team_emp_profile_edit_2/{id}")
	public String edit_emp_team_profile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
				UserDetail userDetail = userOptional.get();
				List<Team> teams = teamDao.findAll();
				model.addAttribute("teams", teams);
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "HrTeamEditProfile";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = HrController.class.getName();
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
	    Map<String, String> teamDetails = servicelayer.getTeamDetails(teamId, companyId);

	    return ResponseEntity.ok(teamDetails);
	}  
	
	
	@PostMapping("/update_emp_Profile/{id}")
	public String assignTeam(@PathVariable int id, UserDetail userDetail, HttpSession session, Principal principal) {
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
	    return "redirect:/hr/emp_profile_edit_1/" + id;
	}
	
	
	@GetMapping("/emp_profile_edit_2/{id}")
	public String profilee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				Map<String, Object> data = servicelayer.getUserDetailAndTeams(id);
				model.addAttribute("userdetail", data.get("userDetail"));
				model.addAttribute("teams", data.get("teams"));
				model.addAttribute("title", "update form - " + ((UserDetail) data.get("userDetail")).getUsername());
				return "HrEditEmpProfile";
			} else {
				throw new Exception("Principal is null");
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = HrController.class.getName();
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
			return "HrPerformance";
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = HrController.class.getName();
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
	@GetMapping("/emp_profile_edit_21/{id}")
	public String profileee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile4.1";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = HrController.class.getName();
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

	@GetMapping("/profile/{id}")
	public String profile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile3";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
		        // Basic exception details
			e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = HrController.class.getName();
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
//			return "SomethingWentWrong";
			return "redirect:/signin?expiredsession=true";
		}
	}

	@GetMapping("/swrr")
	public String swr() {
//		return "SomethingWentWrong";
		return "redirect:/signin?expiredsession=true";
	}

	@GetMapping("/ChangeCurrentPassword")
	public String changepassword(Principal principal) {
		if (principal != null) {
			return "HrChangeCurrentPassword";
		}
//		return "SomethingWentWrong";
		return "redirect:/signin?expiredsession=true";
	}

	@PostMapping("/ChangeCurrentPassword")
	public String ChangePasswordRequest(User user, @RequestParam("currentPassword") String currentPassword,
			@RequestParam("newpassword") String newPassword,
			@RequestParam("newconfirmpassword") String newconfirmpassword, HttpSession session) {
		try {
			String oldpassword = user.getPassword();
			System.out.println(oldpassword);
			if (this.bCryptPasswordEncoder.matches(currentPassword, oldpassword)) {
				if (newPassword.equals(newconfirmpassword)) {
					if (this.bCryptPasswordEncoder.matches(newconfirmpassword, oldpassword)) {
						session.setAttribute("message",
								new Message("OldPassword And NewPassword Cannot Be Same!!", "alert-danger"));
						return "redirect:/hr/ChangeCurrentPassword";
					} else {
						String emaill = user.getEmail();
						boolean res = servicelayer.saveNewPassword(newconfirmpassword, emaill);
						if (res == true) {
							session.setAttribute("message",
									new Message("Password Successfully Updated", "alert-success"));
							return "redirect:/hr/ChangeCurrentPassword";
						} else {
							session.setAttribute("message",
									new Message("Password Not Updated Due To Something Went Wrong!!", "alert-danger"));
							return "redirect:/hr/ChangeCurrentPassword";
						}
					}
				} else {
					session.setAttribute("message",
							new Message("NewPassword And NewConfirmPassword Not Match!!", "alert-danger"));
					return "redirect:/hr/ChangeCurrentPassword";
				}

			} else {
				session.setAttribute("message",
						new Message("Current password Not Match As Per Your Entered Input!!", "alert-danger"));
				return "redirect:/hr/ChangeCurrentPassword";
			}
		} catch (Exception e) {
		        // Basic exception details
			e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = HrController.class.getName();
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
//			return "SomrthingWentWrong";
			return "redirect:/signin?expiredsession=true";
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
//				String username = userDetail.getUsername();
//				String to = userDetail.getEmail();
//				String team_iid = userDetail.getTeam();
//
//				if (team_iid.equals("0") && initialvalue.length() == 1) {
//					session.setAttribute("message", new Message("Employee Is Already On Bench", "alert-danger"));
//				} else if (initialvalue.equals(team_iid)) {
//					session.setAttribute("message", new Message(" Same Team Cannot Be Reassigned!!", "alert-danger"));
//				} else if (initialvalue.length() > 0 && team_iid != "0" && team_iid.length() == 8) {
//					session.setAttribute("message",
//							new Message("Employee Details Successfully Updated !!", "alert-success"));
//					String subject = "Google : Employee GOOGLEIN" + userDetail.getId() + " Team Assigned";
//					servicelayer.sentMessage1(to, subject, team_iid, username);
//					userDetail.setEmployeeOnBench(false);
//					userDetailDao.save(userDetail);
//				} else if (initialvalue.length() == 8 && team_iid.equals("0")) {
//					String subject = "Google : Your Team Removed";
//					servicelayer.sentMessage3(to, subject, initialvalue, username);
//					userDetail.setEmployeeOnBench(true);
//					userDetailDao.save(userDetail);
//					session.setAttribute("message",
//							new Message("Employee Details Successfully Updated !! Team Changed", "alert-success"));
//				} else {
//					session.setAttribute("message",
//							new Message(" Something Went Wrong , Please try again !!", "alert-danger"));
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
//		return "redirect:/hr/profile/" + userDetail.getId();
//	}

	@GetMapping("/seperation")
	public String Seperation(Principal principal) {
		if (principal != null) {
			return "HrSeperation";
		}
//		return "SomethingWentWrong";
		return "redirect:/signin?expiredsession=true";
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
	    return "HrSeperation";
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
	        return "HrSeperation";
	    } catch (NoSuchElementException e) {
	        session.setAttribute("message", new Message("Something Went Wrong !! " + e.getMessage(), "alert-danger"));
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
	        return "HrSeperation";
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
	        session.setAttribute("message", new Message("Unexpected Error: " + e.getMessage(), "alert-danger"));
	        return "HrSeperation";
	    }
	}
	@GetMapping("/assetpolicy")
	public String AssetPolicy(Principal principal) {
		try {
			if (principal != null) {
				return "assetpolicy4";
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
//			return "SomethingWentWrong";
			return "redirect:/signin?expiredsession=true";
		}
	}

	@GetMapping("/profile3/{id}")
	public String yourProfile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findByIdField(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile3";
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
//			return "SomethingWentWrong";
			return "redirect:/signin?expiredsession=true";
		}
	}

	@GetMapping("/viewTeamMembersOnly")
	public String ViewTeamMembers(User user, Model model) {
		try {
			// Get user ID
						int id = user.getId();

						// Fetch user details by team
						List<UserDetail> allDetails = servicelayer.findUserByTeam(id);

						// Handle the case where no details are found
						if (allDetails.isEmpty()) {
							// Process and add details to the model
							model.addAttribute("allDetails", allDetails);
							return "HrTeamMembers"; // Redirect to a view that shows no team members found
						}

						// Process and add details to the model
						model.addAttribute("allDetails", allDetails);

			return "HrTeamMembers";
		} catch (Exception e) {
//			return "SomethingWentWrong";
			return "redirect:/signin?expiredsession=true";
		}
	}

	@GetMapping("/employee_leave_policy")
	public String Employee_Leave_Policy() {
		return "employeeleavepolicy1";
	}

	@RequestMapping("/teamprofile/{id}")
	public String teamprofile(@PathVariable("id") Integer id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
		try {
			if (principal != null) {
				try
				{
				System.out.println("IN");
				Optional<User> get_user = this.userdao.findByEmail(principal.getName());
				User get_user1 = get_user.get();
//				Optional<UserDetail> userOptional = this.userDetailDao.findByIdField(id);
				  // 1. Get the logged-in username
		        String loggedInUsername = principal.getName();
				 // 2. Fetch the logged-in user from DB
		        User loggedInUser = servicelayer.findByUsername(loggedInUsername); // or fetch by email if applicable
				Optional<UserDetail> userOptional = this.userDetailDao.findIfSameTeam(id, loggedInUser.getId());
				  if (userOptional.isEmpty()) {
		            redirectAttributes.addFlashAttribute("errorMessage", "🚫 Unauthorized access! You are not allowed to view this content.");
		            return "redirect:/hr/access-denied";
		        }
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("get_user", get_user1);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				}
				catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
//		if(userDetail.getRole().equals("ROLE_hr"))
//		{
				return "HrTeamViewProfile";
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
//			return "SomethingWentWrong";
			return "redirect:/signin?expiredsession=true";
		}
//		}
//		else
//		{
//			return "hrNormalRoleViewProfile";
//		}
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
						return "redirect:/hr/teamprofile/" + userDetail.getId(); // Redirect to a relevant page
					}

					// Fetch teams for the dropdown if required
					List<Team> teams = teamDao.findAll();
					model.addAttribute("teams", teams);

					// Add user and employee details to the model
					model.addAttribute("userdetail", userDetail);
					model.addAttribute("user", loggedInUser);
					model.addAttribute("title", "Update Employee Info - " + userDetail.getUsername());

					// Return the approval page
					return "HrTeamSeperationRequestIApproved";
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
	        String className = HrController.class.getName();
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
			return "redirect:/hr/teamprofile/" + user1.getId();
		} else {
			lastdate = user1.getLastWorkingDay();
			session.setAttribute("message", new Message(
					"Sorry!! You have already applied speration request and your last working day is " + lastdate,
					"alert-danger"));
			return "redirect:/hr/teamprofile/" + user1.getId();
		}
		}
		catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = HrController.class.getName();
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

	@GetMapping("/careers")
	public String careers() {
		try
		{
		return "hcareers";
		}
		catch (Exception e) {
	        // Basic exception details
			e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = HrController.class.getName();
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

	            return "HrTaskList";
	        } else {
	            throw new Exception("Principal is null");
	        }
	    } catch (Exception e) {
	        // Basic exception details
	    	e.printStackTrace();
	        String exceptionAsString = e.toString();
	        String className = HrController.class.getName();
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
	        return "HrTaskList";
	    	}
	    	catch (Exception e) {
		        // Basic exception details
	    		e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = HrController.class.getName();
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
	        return "HrTaskList";
	    	}
	    	catch (Exception e) {
		        // Basic exception details
	    		e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = HrController.class.getName();
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
	        return "HrTaskList";
	    	}
	    	catch (Exception e) {
		        // Basic exception details
	    		e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = HrController.class.getName();
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
	        return "HrTaskList";
	    	}
	    	catch (Exception e) {
		        // Basic exception details
	    		e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = HrController.class.getName();
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
	        return "HrTaskList";
	    	}
	    	catch (Exception e) {
		        // Basic exception details
	    		e.printStackTrace();
		        String exceptionAsString = e.toString();
		        String className = HrController.class.getName();
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
	                    return "HrEditTaskStatus";
	                }

	                Tasks task = taskOptional.get();
	                UserDetail userDetail = servicelayer.getUserDetailById(task.getId());

	                model.addAttribute("task", task);
	                model.addAttribute("userdetail", userDetail);

	                return "HrEditTaskStatus";
	            } else {
	                throw new Exception("User session expired. Principal is null.");
	            }
	        } catch (Exception e) {
	    	        // Basic exception details
	        	e.printStackTrace();
	    	        String exceptionAsString = e.toString();
	    	        String className = HrController.class.getName();
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
	            return "HrEditTaskStatus";
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
	  		            return "HrViewTask"; 
	  		        } else {
	  		            throw new Exception("Principal is null");
	  		        }
	  		    } catch (Exception e) {
	  		        // Basic exception details
	  		        String exceptionAsString = e.toString();
	  		        String className = HrController.class.getName();
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
	        List<Tasks> tasks = servicelayer.searchTasksByTaskId(query,user.getId(),"ROLE_HR");
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
	    
	    
	    // Handle Task Status Update
	    @PostMapping("/updateTaskStatus/{taskId}")
	    public String updateTaskStatus(@PathVariable("taskId") String taskId
	    		,@RequestParam("id") int id, @RequestParam("taskStatus") String taskStatus, HttpSession session) {
	    	try
	    	{
	        servicelayer.updateTaskStatus(taskId, taskStatus);
	        session.setAttribute("message",
	                new Message("Task ID: " + taskId + " has been updated successfully.", "alert-success"));
	        return "redirect:/hr/ViewTaskList/"+id; // Redirect to task list after update
	    	}
	    	catch (Exception e) {
	    		   session.setAttribute("message", new Message("Task Update Failed!!: " + e.getMessage(), "alert-danger"));
	   	        return "redirect:/hr/ViewTaskList/"+id; // Redirect to task list after update
			}
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
	        return "HrAttendanceCalender";
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
	            return "Hr-leave-summary"; // Thymeleaf view for Leave
	        } else if (requestType.equalsIgnoreCase("WFH")) {
	            return "Hr-wfh-summary"; // Thymeleaf view for WFH
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

	        return "redirect:/hr/summary/WFH";
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

	        return "redirect:/hr/summary/Leave";
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
	        return "Hr-Attendance-Request-History";
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
	        return "redirect:/hr/request-history";
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
	        return "Hr-Request-Details";
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
	        return "redirect:/hr/view-request/" + request_id;
	    }
}
