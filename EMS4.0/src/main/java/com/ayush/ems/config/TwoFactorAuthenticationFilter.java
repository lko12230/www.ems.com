package com.ayush.ems.config;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class TwoFactorAuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        String uri = request.getRequestURI();

        // Only apply this filter to protected paths
        boolean isProtected = uri.startsWith("/admin") || uri.startsWith("/hr") || uri.startsWith("/user")
                || uri.startsWith("/manager") || uri.startsWith("/IT");

        // Allow OTP and static resources without checks
        boolean isSafe = uri.startsWith("/verify-otp2fa") || uri.startsWith("/do-verify-otp2fa")
                || uri.startsWith("/logout") || uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/img");

        if (isProtected && !isSafe) {
            if (session != null) {
                Boolean verified = (Boolean) session.getAttribute("2fa_verified");
                if (verified == null || !verified) {
                    response.sendRedirect("/verify-otp2fa");
                    return;
                }
            } else {
                response.sendRedirect("/signin");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
