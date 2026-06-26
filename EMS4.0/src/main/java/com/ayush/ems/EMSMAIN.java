package com.ayush.ems;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ayush.ems.dao.DowntimeMaintaince_Dao;
import com.ayush.ems.dao.JobDao;
import com.ayush.ems.entities.Downtime_Maintaince;
import com.ayush.ems.entities.Job;
import com.ayush.ems.service.EmailService;
import com.ayush.ems.service.Servicelayer;

@SpringBootApplication
@EnableScheduling
public class EMSMAIN {

    @Autowired private Servicelayer servicelayer;
    @Autowired private EmailService emailService;
    @Autowired private JobDao jobDao;
    @Autowired private DowntimeMaintaince_Dao downtimeRepo;

    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean shuttingDown = false;
    
    @PreDestroy
    public void onShutdown() {
        shuttingDown = true;
        System.out.println("🛑 Application is shutting down...");
    }

    // Allowed jobs to run during Server Maintenance
    private static final Set<String> ALLOWED_DURING_MAINTENANCE = Set.of(
        "Server Maintenance",
        "Validate Captcha Job",
        "Validate Otp Job",
        "Email Retry",
        "Sync System Data",
        "Account Locked Job",
        "Disabled Expired Plan Users"
    );

    public static Map<String, Date> captchaValidateMap = new ConcurrentHashMap<>();
    public static Map<String, OTPInfo> otpValidateMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(EMSMAIN.class, args);
    }

    /**
     * Run missed jobs after server restart
     */
    @PostConstruct
    public void runMissedJobsOnStartup() {

        servicelayer.initializeTaskTracker();   // <-- add this line
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Wait for Spring context to fully load
                Date now = new Date();
                List<Job> jobs = jobDao.findAll();

                for (Job job : jobs) {
                    if ("Y".equalsIgnoreCase(job.getJob_active_or_not())
                            && job.getNextRun() != null
                            && !job.getNextRun().after(now)) {
                        System.out.println("🚀 Running missed job after restart: " + job.getJob_description());
                        executeJob(job, true);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Main scheduler loop (runs every 1 min)
     */
    @Scheduled(fixedRate = 60000)
    public void runAllDynamicJobs() {

        if (shuttingDown) {
            return;
        }

        if (!lock.tryLock()) {
            System.out.println("⚠️ Scheduler already running. Skipping this tick.");
            return;
        }

        try {

            if (shuttingDown) {
                return;
            }

            Date now = new Date();
            List<Job> jobs = jobDao.findAll();

            boolean isServerDown = checkAndUpdateServerDowntime();

            for (Job job : jobs) {

                if (shuttingDown) {
                    break;
                }

                if (!"Y".equalsIgnoreCase(job.getJob_active_or_not())
                        || job.getNextRun() == null
                        || job.getNextRun().after(now)) {
                    continue;
                }

                if (isServerDown) {

                    if (ALLOWED_DURING_MAINTENANCE.contains(job.getJob_description())) {
                        executeJob(job, false);
                    } else {
                        System.out.println("⏭️ Skipping job due to server downtime: " + job.getJob_description());
                    }

                } else {
                    executeJob(job, false);
                }
            }

        } finally {

            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

        }
    }
    /**
     * Checks downtime window and updates serverDownOrNot flag accordingly.
     */
    private boolean checkAndUpdateServerDowntime() {
        Date now = new Date();
        Optional<Downtime_Maintaince> downtimeOpt = downtimeRepo.findById(1);  // assuming sno=1

        if (downtimeOpt.isEmpty()) {
            return false;
        }

        Downtime_Maintaince downtime = downtimeOpt.get();

        Date start = downtime.isServerDownManually() ? downtime.getManualDowntimeStart() : downtime.getServerDowntimeStart();
        Date end   = downtime.isServerDownManually() ? downtime.getManualDowntimeEnd()   : downtime.getServerDowntimeEnd();

        if (start == null || end == null) {
            return downtime.isServerDownOrNot();
        }

        boolean currentlyDown = downtime.isServerDownOrNot();
        boolean inDowntimeWindow = !now.before(start) && !now.after(end);

        if (!currentlyDown && inDowntimeWindow) {
            downtime.setServerDownOrNot(true);
            downtime.setStatus("Running");
            downtime.setEditdate(now);
            downtime.setEditwho("SYSTEM_SCHEDULER");
            downtimeRepo.save(downtime);
            System.out.println("⚠️ Server downtime started as per schedule.");
            return true;
        }

        if (currentlyDown && !inDowntimeWindow) {
            downtime.setServerDownOrNot(false);
            downtime.setStatus("Completed");
            downtime.setEditdate(now);
            downtime.setEditwho("SYSTEM_SCHEDULER");
            if (downtime.isServerDownManually()) {
                downtime.setServerDownManually(false);
            }

            if (!downtime.isServerDownManually()) {
                updateDowntimeWindowForNextDay(downtime);
            }

            downtimeRepo.save(downtime);
            System.out.println("✅ Server downtime ended as per schedule.");
            return false;
        }

        return currentlyDown;
    }

    private void updateDowntimeWindowForNextDay(Downtime_Maintaince downtime) {
        Date start = downtime.getServerDowntimeStart();
        Date end   = downtime.getServerDowntimeEnd();

        if (start != null && end != null) {
            downtime.setServerDowntimeStart(addOneDayKeepTime(start));
            downtime.setServerDowntimeEnd(addOneDayKeepTime(end));
            downtime.setEditdate(new Date());
            downtime.setEditwho("SYSTEM_SCHEDULER");
            System.out.println("🔄 Downtime window moved to next day with same time.");
        }
    }

    private Date addOneDayKeepTime(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    private void executeJob(Job job, boolean isMissedJobRun) {
        long startMillis = System.currentTimeMillis();
        Date jobStartTime = new Date();

        try {
            job.setStartflag(true);
            job.setEndflag(false);
            job.setEditdate(jobStartTime);
            job.setEditwho("SYSTEM_SCHEDULER");
            jobDao.save(job);

            System.out.println("▶️ Running Job: " + job.getJob_description());

            switch (job.getJob_description()) {
                case "Generate Salary Slip": servicelayer.generateSalarySlipsForCurrentMonth(job.getCompany_id()); break;
                case "Validate Captcha Job": cleanupCaptchaMap(); break;
                case "Validate Otp Job": cleanupOtpMap(); break;
                case "Account Locked Job": servicelayer.getAllUsersByAccount_Non_LockedAndFailed_Attempts(); break;
                case "Login Archive Job": servicelayer.getAllLoginAdddate(); break;
                case "Disabled Expired Plan Users": servicelayer.disbaled_expired_plan_users(); break;
                case "Orders Archive Job": servicelayer.getAllOrdersAdddate(); break;
                case "Archive Disabled Old User Job":
                    servicelayer.Archive_Disabled_Old_UserDetail_Job();
                    servicelayer.Archive_Disabled_Old_User_Job(); break;
                case "Delete Old Error Log": servicelayer.delete_old_error_log(); break;
                case "Password Failed Attempt Reset": servicelayer.reset_failed_attempts_password(); break;
                case "Email Retry": emailService.retryFailedEmails(); break;
                case "Recharge Upcoming Alert": servicelayer.RechargeUpcomingAlert(); break;
                case "Disabled Separation User": servicelayer.DisabledSeparationUser(); break;
                case "Send Birthday Email": servicelayer.sendBirthdayEmail(); break;
                case "Archive Old Notification": servicelayer.archive_old_notification(); break;
                case "Pending Payment Status": servicelayer.PendingPaymentStatus(); break;
                case "Absent Marking Job": servicelayer.markAbsenteesForAllCompanies(job.getCompany_id()); break;
                case "Server Maintenance": servicelayer.syncUserAndUserDetail(); break;
                default: System.out.println("⚠️ Unknown job: " + job.getJob_description());
            }

            long duration = System.currentTimeMillis() - startMillis;
            job.setLastRun(jobStartTime);
            job.setLastRunDurationInMs(duration);
            job.setTotalRunCount(job.getTotalRunCount() + 1);
            job.setJob_running_time(formatDuration(duration));
            job.setJob_Status("COMPLETED");

            Date nextRunBase = isMissedJobRun ? job.getNextRun() : jobStartTime;
            job.setNextRun(calculateNextRun(nextRunBase, job.getJobFrequency()));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startMillis;
            e.printStackTrace();
            job.setJob_Status("FAILED");
            job.setLastRun(jobStartTime);
            job.setLastRunDurationInMs(duration);
            job.setJob_running_time(formatDuration(duration));

            servicelayer.insert_error_log(
                e.toString(),
                this.getClass().getName(),
                e.getMessage(),
                "executeJob",
                e.getStackTrace()[0].getLineNumber(),
                Arrays.toString(e.getStackTrace())
            );

        } finally {
            job.setStartflag(false);
            job.setEndflag(true);
            job.setEditdate(new Date());
            job.setEditwho("SYSTEM_SCHEDULER");
            jobDao.save(job);
        }
    }

    private void cleanupCaptchaMap() {
        LocalDateTime now = LocalDateTime.now();
        captchaValidateMap.entrySet().removeIf(entry ->
            Duration.between(entry.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), now).toMinutes() >= 5);
        System.out.println("🧹 CAPTCHA MAP CLEANED: " + captchaValidateMap.size());
    }

    private void cleanupOtpMap() {
        LocalDateTime now = LocalDateTime.now();
        otpValidateMap.entrySet().removeIf(entry ->
            Duration.between(entry.getValue().getGeneratedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), now).toMinutes() >= 5);
        System.out.println("🧹 OTP MAP CLEANED: " + otpValidateMap.size());
    }

    public Date calculateNextRun(Date from, String jobFrequency) {
        if (from == null || jobFrequency == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(from);

        switch (jobFrequency.toUpperCase()) {
            case "MINUTE":  calendar.add(Calendar.MINUTE, 1); break;
            case "HOURLY":  calendar.add(Calendar.HOUR_OF_DAY, 1); break;
            case "DAILY":   calendar.add(Calendar.DATE, 1); break;
            case "WEEKLY":  calendar.add(Calendar.DATE, 7); break;
            case "MONTHLY": calendar.add(Calendar.MONTH, 1); break;
            case "YEARLY":  calendar.add(Calendar.YEAR, 1); break;
            default:        calendar.add(Calendar.DATE, 1);
        }
        return calendar.getTime();
    }

    private String formatDuration(long durationMs) {
        long totalSeconds = durationMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static class OTPInfo {
        private String otp;
        private Date generatedAt;

        public OTPInfo(String otp, Date generatedAt) {
            this.otp = otp;
            this.generatedAt = generatedAt;
        }

        public String getOtp() { return otp; }
        public Date getGeneratedAt() { return generatedAt; }
    }
}
