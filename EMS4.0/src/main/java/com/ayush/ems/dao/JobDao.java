package com.ayush.ems.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ayush.ems.entities.Job;
@Repository
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
	
	@Query(value = "select u.id from Job u order by id desc limit 1",nativeQuery = true)
	public int getJobLastId();
	
	@Query(value="select count(1) from Job",nativeQuery = true)
	public int getJobCount();

	@Query("SELECT MAX(j.sno) FROM Job j")
	Optional<Long> getMaxSno();

	  @Query("SELECT j FROM Job j WHERE j.company_id = :companyId AND j.job_description = :description")
	    Optional<Job> findByCompanyIdAndJobDescription(@Param("companyId") String companyId, @Param("description") String description);

	    @Query("SELECT COALESCE(MAX(j.sno), 0) + 1 FROM Job j")
	    int getNextSno();
	    
	    @Query(value = "SELECT * FROM job WHERE job_description = :job_description AND job_active_or_not = :status", nativeQuery = true)
	    List<Job> findActiveJobsByDescription(@Param("job_description") String job_description, @Param("status") String status);

}
