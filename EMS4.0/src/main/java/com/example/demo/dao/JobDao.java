package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entities.Job;

public interface JobDao extends JpaRepository<Job, Integer> {
	@Query(value = "update job u set u.job_running_time= CURRENT_TIMESTAMP ,u.job_Status= 'COMPLETED SUCCESSFULLY' where u.job_description=?1 ", nativeQuery = true)
	@Modifying
	public void getJobRunningTime(String job_description);

	@Query(value = "update job u set u.job_running_time= CURRENT_TIMESTAMP ,u.job_Status= 'COMPLETED UNSUCCESSFULLY' where u.job_description=?1 ", nativeQuery = true)
	@Modifying
	public void getJobRunningTimeInterrupted(String job_description);

	@Query("select u.job_active_or_not from Job u where u.job_description=?1 ")
	public String getJobStatus(String job_description);

	@Query("update Job u set u.job_Status= 'JOB NOT RUNNING' where u.job_description=?1 ")
	@Modifying
	public void getJobNotRunning(String job_description);
	
	@Query(value = "select u.id from database_ems.Job u order by id desc limit 1",nativeQuery = true)
	public int getJobLastId();
	
	@Query(value="select count(1) from database_ems.Job",nativeQuery = true)
	public int getJobCount();

}
