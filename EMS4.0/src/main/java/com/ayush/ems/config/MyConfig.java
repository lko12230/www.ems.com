package com.ayush.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class MyConfig {

    @Autowired private LoginSuccessHandler loginSuccessHandler;
    @Autowired private CustomLoginFailureHandler customLoginFailureHandler;
    @Autowired private CustomLogoutSuccessHandler customLogoutSuccessHandler;
    @Autowired private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(requests -> requests
                .antMatchers(
                    "/signin", "/oauth2/**", "/css/**", "/js/**", "/img/**",
                    "/", "/signup/**", "/forgot/**", "/verify_admin_get", "/verify_admin",
                    "/verify-otp2/**", "/do-register", "/send-otp", "/verify-otp",
                    "/change-password", "/resendotp", "/log-activity", "/about", "/contact_us",
                    "/do-submit", "/swr", "/error",
                    "/do-verify-otp2fa", "/verify-otp2fa", "/2fa/**","/server-down"
                ).permitAll()
                .antMatchers("/user/**").hasRole("USER")
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/hr/**").hasRole("HR")
                .antMatchers("/IT/**").hasRole("IT")
                .antMatchers("/manager/**").hasRole("MANAGER")
                .antMatchers("/super_admin/**").hasRole("SUPER_ADMIN")
                .antMatchers("/support/**").hasRole("SUPPORT")
                .antMatchers("/actuator", "/actuator/health", "/actuator/info", "/actuator/metrics", "/actuator/env", "/actuator/beans", "/actuator/mappings", "/actuator/loggers").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/signin")
                .loginProcessingUrl("/dologin")
                .successHandler(loginSuccessHandler)
                .failureHandler(customLoginFailureHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .oauth2Login(oauth -> oauth
                .loginPage("/signin")
                .userInfoEndpoint().userService(customOAuth2UserService)
                .and()
                .successHandler(loginSuccessHandler)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().newSession()
                .invalidSessionUrl("/signin?expiredsession=true")
                .maximumSessions(1)
                .expiredUrl("/signin?expiredsession=true")
                .sessionRegistry(sessionRegistry())
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public FilterRegistrationBean<TwoFactorAuthenticationFilter> twoFactorFilter() {
        FilterRegistrationBean<TwoFactorAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TwoFactorAuthenticationFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2); // Ensures this runs after Spring Security filter
        return registrationBean;
    }
    
    @Bean
    public FilterRegistrationBean<MaintenanceFilter> maintenanceFilterRegistration(MaintenanceFilter maintenanceFilter) {
        FilterRegistrationBean<MaintenanceFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(maintenanceFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // Run before Spring Security
        return registrationBean;
    }

}
