//package com.ayush.ems.config;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ScheduledFuture;
//
//import org.springframework.scheduling.TaskScheduler;
//import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
//import org.springframework.scheduling.support.CronTrigger;
//import org.springframework.stereotype.Service;
//
//import com.ayush.ems.dao.JobDao;
//import com.ayush.ems.entities.Job;
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class DynamicSchedulerService {
//
//    private final TaskScheduler scheduler = new ConcurrentTaskScheduler();
//    private final JobDao jobDao;
//
//    // Use job ID (Integer) as key
//    private final Map<Integer, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
//
//    /**
//     * Save job in DB and schedule it
//     */
//    public void scheduleJob(String frequency, int hour, int minute, int second,
//                            LocalDateTime startDate, LocalDateTime endDate,
//                            String jobName, String companyId, String createdBy) {
//
//        Job job = new Job();
//        job.setJobName(jobName);
//        job.setFrequency(frequency.toUpperCase());
//        job.setHour(hour);
//        job.setMinute(minute);
//        job.setSecond(second);
//        job.setStartDate(startDate);
//        job.setEndDate(endDate);
//        job.setJobStatus("ACTIVE");
//        job.setIsActive("Y");
//        job.setCreatedAt(LocalDateTime.now());
//        job.setCreatedBy(createdBy);
//        job.setCompany_id(companyId);
//
//        Job savedJob = jobDao.save(job); // Save to DB and get SNO
//
//        String cron = buildCronExpression(frequency, hour, minute, second);
//
//        ScheduledFuture<?> future = scheduler.schedule(() -> runJob(savedJob), new CronTrigger(cron));
//        scheduledJobs.put(savedJob.getSno(), future); // ✅ use sno as Integer
//    }
//
//    /**
//     * Convert frequency into Cron expression
//     */
//    private String buildCronExpression(String frequency, int hour, int minute, int second) {
//        frequency = frequency.toUpperCase();
//
//        switch (frequency) {
//            case "SECONDLY": return String.format("*/%d * * * * *", second);
//            case "MINUTELY": return String.format("0 */%d * * * *", minute);
//            case "HOURLY":   return String.format("0 0 */%d * * *", hour);
//            case "DAILY":    return String.format("0 %d %d * * *", minute, hour);
//            case "WEEKLY":   return String.format("0 %d %d * * MON", minute, hour);
//            case "MONTHLY":  return String.format("0 %d %d 1 * *", minute, hour);
//            case "YEARLY":   return String.format("0 %d %d 1 1 *", minute, hour);
//            default:
//                throw new IllegalArgumentException("Unsupported frequency: " + frequency);
//        }
//    }
//
//    /**
//     * Job execution logic
//     */
//    private void runJob(Job job) {
//        LocalDateTime now = LocalDateTime.now();
//
//        if (job.getEndDate() != null && now.isAfter(job.getEndDate())) {
//            cancelJob(job.getSno());
//            return;
//        }
//
//        System.out.println("✅ Running job [" + job.getJobName() + "] for companyId [" + job.getCompany_id() + "] at " + now);
//        // Add actual business logic here
//    }
//
//    /**
//     * Cancel job by job ID
//     */
//    public void cancelJob(Integer jobId) {
//        ScheduledFuture<?> future = scheduledJobs.get(jobId);
//        if (future != null) {
//            future.cancel(false);
//            scheduledJobs.remove(jobId);
//        }
//
//        jobDao.findById(jobId).ifPresent(job -> {
//            job.setJobStatus("INACTIVE");
//            job.setIsActive("N");
//            job.setUpdatedAt(LocalDateTime.now());
//            jobDao.save(job);
//        });
//    }
//
//    /**
//     * Fetch and run all jobs for a company
//     */
//    public void loadCompanyJobs(String companyId) {
//        List<Job> jobs = jobDao.findByCompanyIdAndIsActive(companyId, "Y");
//
//        for (Job job : jobs) {
//            String cron = buildCronExpression(job.getFrequency(), job.getHour(), job.getMinute(), job.getSecond());
//            ScheduledFuture<?> future = scheduler.schedule(() -> runJob(job), new CronTrigger(cron));
//            scheduledJobs.put(job.getSno(), future); // ✅ store by Integer sno
//        }
//    }
//}
