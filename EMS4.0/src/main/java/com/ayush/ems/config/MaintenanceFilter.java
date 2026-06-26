package com.ayush.ems.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ayush.ems.service.DowntimeMaintainceService;

@Component
public class MaintenanceFilter extends OncePerRequestFilter {

    @Autowired
    private DowntimeMaintainceService downtimeService;

    private static final String MAINTENANCE_DESC = "Server Maintenance";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        boolean serverUp;
        try {
            serverUp = downtimeService.isServerUp(MAINTENANCE_DESC);
            System.out.println("IS SERVER UP "+serverUp);
        } catch (Exception e) {
            serverUp = true; // Fail open: if DB check fails, keep server UP
        }

        if (!serverUp &&
            !path.startsWith("/server-down") &&
            !path.startsWith("/css/") &&
            !path.startsWith("/js/") &&
            !path.startsWith("/img/")) {

            response.sendRedirect("/server-down");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
