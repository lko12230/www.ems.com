package com.example.demo.controller;

import java.io.BufferedReader;
import java.io.File;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.EMSMAIN;
import com.example.demo.dao.UserDetailDao;
import com.example.demo.dao.Userdao;
import com.example.demo.dao.adminDao;
import com.example.demo.entities.Admin;
import com.example.demo.entities.Error_Log;
import com.example.demo.entities.Performance;
import com.example.demo.entities.User;
import com.example.demo.entities.UserDetail;
import com.example.demo.helper.Message;
import com.example.demo.service.servicelayer;

@Controller
@RequestMapping("/hr")
//@Scope(value = WebApplicationContext.SCOPE_SESSION)
@Scope
public class HrController {

	@Autowired
	private Userdao userdao;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private servicelayer servicelayer;
	@Autowired
	private UserDetailDao userDetailDao;
	@Autowired
	private adminDao adminDao;

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
			Redirect("/hr/swrr");
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
	            Optional<User> currentUser = this.userdao.findByUserName(username);
	            User user1 = currentUser.get();
	            servicelayer.login_record_save(user1, session, clientIp, location);
	            count++;
	        }
	        return "home3";
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

	
	@GetMapping("/hr_profile_edit_1/{id}")
	public String yourProfileee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findById(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile4";
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

	@GetMapping("/hr_profile_edit_2/{id}")
	public String yourProfileeee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findById(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile4.0";
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

	List<UserDetail> all_users = new ArrayList<>();

	@GetMapping("/ViewMembers")
	public String Employee(Model model, User user) {
		try {
			all_users = userDetailDao.findAll();
			if (all_users != null && user.getUsername() != null) {
				System.out.println("find all " + all_users);
				model.addAttribute("all_users", all_users);
				System.out.println("IN");
				return "ViewMembers4";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//			return "SomethingWentWrong";
			return "redirect:/signin?expiredsession=true";
		}
	}

	
	@GetMapping("/emp_profile_edit_1/{id}")
	public String profile1(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile4.0";
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
	public String profile2(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile4";
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
			return "hperformance";
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
	
	@GetMapping("/emp_profile_edit_21/{id}")
	public String profileee(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile4.1";
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


	
	@GetMapping("/profile/{id}")
	public String profile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "profile3";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
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
			return "ChangeCurrentPasswordhr";
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
			return "Seperation4";
		}
//		return "SomethingWentWrong";
		return "redirect:/signin?expiredsession=true";
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
			return "Seperation4";
		} else {
			lastdate = user1.getLastWorkingDay();
			session.setAttribute("message", new Message(
					"Sorry!! You have already applied speration request and your last working day is " + lastdate,
					"alert-danger"));
			return "Seperation4";
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
//			return "SomethingWentWrong";
			return "redirect:/signin?expiredsession=true";
		}
	}

	@GetMapping("/profile3/{id}")
	public String yourProfile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<User> userOptional = this.userdao.findById(id);
				User userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
				return "emp_profile3";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//			return "SomethingWentWrong";
			return "redirect:/signin?expiredsession=true";
		}
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
			return "hrTeamMembers2";
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
	public String teamprofile(@PathVariable("id") Integer id, Model model, Principal principal) {
		try {
			if (principal != null) {
				System.out.println("IN");
				Optional<UserDetail> userOptional = this.userDetailDao.findById(id);
				UserDetail userDetail = userOptional.get();
				model.addAttribute("userdetail", userDetail);
				model.addAttribute("title", "update form - " + userDetail.getUsername());
//		if(userDetail.getRole().equals("ROLE_hr"))
//		{
				return "hrViewProfile2";
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
//			return "SomethingWentWrong";
			return "redirect:/signin?expiredsession=true";
		}
//		}
//		else
//		{
//			return "hrNormalRoleViewProfile";
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
			String subject = "Google : Seperation Request EMPID: GOOGLEIN" + user1.getId();
			String username = user1.getUsername();
			String to = user1.getEmail();
			int find = user1.getAaid();
			Optional<Admin> admin = adminDao.findById(find);
			Admin admin1 = admin.get();
			String cc = admin1.getEmail();
			servicelayer.sentMessage4(to, subject, username, lastdate, cc);
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
	
	@GetMapping("/careers")
	public String careers()
	{
		return "hcareers";
	}
}
