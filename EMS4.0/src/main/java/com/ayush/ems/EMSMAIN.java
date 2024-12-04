package com.ayush.ems;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ayush.ems.service.*;

@SpringBootApplication
@EnableScheduling
public class EMSMAIN {

    @Autowired
    private Servicelayer servicelayer;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final ReentrantLock lock = new ReentrantLock();

    // Shared Maps for Captcha and Email Retry Logic
    public static Map<String, Date> captchaValidateMap = new ConcurrentHashMap<>();
    public static Map<String, Integer> forgotPasswordEmailSent = new ConcurrentHashMap<>();
    public static Map<String, Integer> adminSendOtp = new ConcurrentHashMap<>();
    public static Map<Integer, Date> otpValidateMap = new ConcurrentHashMap<>();
    public static Map<String, Date> loginCaptcha = new ConcurrentHashMap<>();
    public static Map<String, String> licenseStatus = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(EMSMAIN.class, args);
    }

    // Scheduled Jobs

    @Scheduled(cron = "0 0/1 * * * *")
    public void accountLockedJob() {
        executeJob("Account Locked Job", () -> servicelayer.getAllUsersByAccount_Non_LockedAndFailed_Attempts());
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void loginOldDataArchiveJob() {
        executeJob("Login Archive Job", servicelayer::getAllLoginAdddate);
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void archiveDisabledOldUserJob() {
        executeJob("Archive Disabled Old User Job", () -> {
            servicelayer.Archive_Disabled_Old_UserDetail_Job();
            servicelayer.Archive_Disabled_Old_User_Job();
        });
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void deleteOldErrorLog() {
        executeJob("Delete Old Error Log", () -> {
            servicelayer.delete_old_error_log();
            cleanupCaptchaMap();
        });
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void passwordFailedAttemptReset() {
        executeJob("Password Failed Attempt Reset", servicelayer::reset_failed_attempts_password);
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void loginRetryEmails() {
        executeJob("Email Retry", emailService::retryFailedEmails);
    }

    // Helper Methods

    private void cleanupCaptchaMap() {
        LocalDateTime now = LocalDateTime.now();
        captchaValidateMap.entrySet().removeIf(entry ->
            Duration.between(entry.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), now)
                   .toMinutes() >= 5);
        System.out.println("MAP CAPTCHA SIZE AFTER CLEANUP: " + captchaValidateMap.size());
    }

    private void executeJob(String jobName, Runnable jobLogic) {
        if (!lock.tryLock()) {
            System.out.println("Job " + jobName + " is already running. Skipping execution.");
            return;
        }

        try {
            if (!entityManagerFactory.isOpen()) {
                System.out.println("EntityManagerFactory is closed. Skipping job execution for " + jobName);
                return;
            }

            String status = servicelayer.getjob_active_or_not(jobName);
            if ("Y".equalsIgnoreCase(status)) {
                System.out.println("Starting job: " + jobName);
                long start = System.currentTimeMillis();
                jobLogic.run();
                long duration = System.currentTimeMillis() - start;
                System.out.println(jobName + " completed in " + duration + " ms");
                servicelayer.jobrunning(jobName);
            } else {
                servicelayer.jobnotrunning(jobName);
            }
        } catch (Exception e) {
            logAndHandleException(e, jobName);
        } finally {
            lock.unlock();
        }
    }

    private void logAndHandleException(Exception e, String jobName) {
        String exceptionAsString = e.toString();
        String className = this.getClass().getName();
        String errorMessage = e.getMessage();
        StackTraceElement stackTrace = e.getStackTrace()[0];
        String methodName = stackTrace.getMethodName();
        int lineNumber = stackTrace.getLineNumber();

        System.err.println("Error in job " + jobName + " at " + methodName + " line " + lineNumber);
        servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
    }
}