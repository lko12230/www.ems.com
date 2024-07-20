package com.example.demo.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.EMSMAIN;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
		// Get the role of logged in user
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String role = auth.getAuthorities().toString();
		if (role == null) {
			throw new UsernameNotFoundException("hi");
		} else {
			System.out.println(auth.getName()+" :::::::");
			InetAddress localHost = null;
			try {
				localHost = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String str1 = localHost.toString();
			String osName = System.getProperty("os.name");
			String osVersion = System.getProperty("os.version");
			String osArchitecture = System.getProperty("os.arch");
			EMSMAIN.success_login_Attempt.put(auth.getName(), str1);
			EMSMAIN.device_os.put(auth.getName(), osName);
			EMSMAIN.device_version.put(auth.getName(), osVersion);
			EMSMAIN.device_Architecture.put(auth.getName(), osArchitecture);
			EMSMAIN.login_date_time.put(auth.getName(), new  Date());
			String targetUrl = "";
			if (role.contains("ROLE_USER")) {
				targetUrl = "/user/new";
			} else if (role.contains("ROLE_ADMIN")) {
				targetUrl = "/admin/new";
			} else if (role.contains("ROLE_MANAGER")) {
				targetUrl = "/manager/new";
			} else if (role.contains("ROLE_HR")) {
				targetUrl = "/hr/new";
			} else if (role.contains("ROLE_IT")) {
				targetUrl = "/IT/new";
			}
			return targetUrl;
		}
	}
}
