//package com.ayush.ems.service;
//
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import javax.servlet.http.HttpServletRequest;
//
//@Service
//public class LoginInfoService {
//
//    private final HttpServletRequest request;
//
//    public LoginInfoService(HttpServletRequest request) {
//        this.request = request;
//    }
//
//    // Get the user's IP address from the request
//    public String getClientIpAddress() {
//        String ipAddress = request.getHeader("X-FORWARDED-FOR");
//        if (ipAddress == null || ipAddress.isEmpty()) {
//            ipAddress = request.getRemoteAddr();
//        }
//        return ipAddress;
//    }
//
//    // Get the logged-in user's username
//    public String getLoggedInUsername() {
//        return SecurityContextHolder.getContext().getAuthentication().getName();
//    }
//}



