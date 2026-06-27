package com.ayush.ems.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ayush.ems.entities.UserDetail;

@Repository
public interface UserDetailDao extends JpaRepository<UserDetail, Integer> {
	@Query("select u from UserDetail u where u.team = :team and length(u.team) > 1  and u.company_id= :companyId and ((u.resignationRequestApplied='false' AND u.seperation_manager_approved='false')  OR (u.resignationRequestApplied='true' AND u.seperation_manager_approved='false')) and u.Status='ACTIVE' and (u.team <> 'NA' OR u.team <> 'No Record Found')")
	public List<UserDetail> getUserByTeam(String team, String companyId);

	@Query(value = "update employeedetail u set u.user_status='0' where u.email= ?1 and u.status='ACTIVE' and  ((u.resignation_request_applied='0' AND u.seperation_manager_approved='0')  OR (u.resignation_request_applied='1' AND u.seperation_manager_approved='0'))", nativeQuery = true)
	@Modifying
	public void update_user_status(String username);

	@Query(value = "update employeedetail u set u.user_status='1' where u.email= ?1and ((u.resignation_request_applied='0' AND u.seperation_manager_approved='0')  OR (u.resignation_request_applied='1' AND u.seperation_manager_approved='0'))", nativeQuery = true)
	@Modifying
	public void update_user_status_online(String username);

	@Query("SELECT u FROM UserDetail u WHERE u.username = :input OR u.email = :input OR u.id = :input  and u.Status='ACTIVE' and ((u.resignationRequestApplied='false' AND u.seperation_manager_approved='false')  OR (u.resignationRequestApplied='true' AND u.seperation_manager_approved='false'))")
	public List<UserDetail> findByNameContainingOrEmailContainingOrIdContaining(@Param("input") String input);

	@Query("select u from UserDetail u where u.enabled=1 and u.company_id=?1 and u.Status='ACTIVE' and ((u.resignationRequestApplied='false' AND u.seperation_manager_approved='false')  OR (u.resignationRequestApplied='true' AND u.seperation_manager_approved='false'))")
	public List<UserDetail> findAllEnabledUser(String company_id);

	@Query(value = "select * from employeedetail u where u.enabled='0' and u.last_working_day <= (NOW() - INTERVAL 30 DAY)", nativeQuery = true)
	public List<UserDetail> Get_ALL_Disabled_Old_UserDetail_Job();

	@Query(value = "select count(1) from employeedetail", nativeQuery = true)
	public int getUserDetailCount();

	@Query(value = "select u.sno from employeedetail u order by u.sno desc limit 1", nativeQuery = true)
	public int getLastSno();

	@Query("SELECT u FROM UserDetail u WHERE u.id = :id")
	Optional<UserDetail> findByIdField(@Param("id") Integer id);
	
	@Query(value = "SELECT * FROM employeedetail e " +
		       "WHERE e.id = :targetId  and e.status='ACTIVE' and ((e.resignation_request_applied='false' AND e.seperation_manager_approved='false')  OR (e.resignation_request_applied='true' AND e.seperation_manager_approved='false'))" +
		       "AND e.team_id IN (SELECT e2.team_id FROM employeedetail e2 WHERE e2.id = :loggedInId)",nativeQuery = true)
		Optional<UserDetail> findIfSameTeam(@Param("targetId") Integer targetId,
		                                        @Param("loggedInId") Integer loggedInId);

	
	@Query(value = "update employeedetail u set u.user_status='0' where u.id= ?1 and u.status='ACTIVE' and ((u.resignation_request_applied='0' AND u.seperation_manager_approved='0')  OR (u.resignation_request_applied='1' AND u.seperation_manager_approved='0'))", nativeQuery = true)
	@Modifying
	@Transactional
	public void update_user_status(Integer id);
	
	@Query(value = "update employee u set u.enabled='0' where u.company_id= ?1 and u.status='ACTIVE' and ((u.resignation_request_applied='0' AND u.seperation_manager_approved='0')  OR (u.resignation_request_applied='1' AND u.seperation_manager_approved='0')) and u.role in ('ROLE_MANAGER','ROLE_HR','ROLE_USER','ROLE_IT')", nativeQuery = true)
	@Modifying
	public void disbaled_expired_plan_users(String company_id);
	
	@Query(value = "update employeedetail u set u.enabled ='0',u.status='INACTIVE' where u.seperation_date <= (NOW() - INTERVAL (SELECT nsql_value FROM nsql_config where configkey='SEPERATIONUSERDISABLED') DAY)", nativeQuery = true)
	@Modifying
	public void disableUsersByLastWorkingDay(Date lastWorkingDay);
	
	@Query(value = "update employeedetail u set u.enabled='1' where u.company_id= ?1 and status='ACTIVE' and ((u.resignation_request_applied='0' AND u.seperation_manager_approved='0')  OR (u.resignation_request_applied='1' AND u.seperation_manager_approved='0'))", nativeQuery = true)
	@Modifying
	public void update_user_enabled_after_success_payment(String company_id);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE employee u " +
	               "JOIN nsql_config c ON c.company_id = u.company_id " +
	               "                  AND c.configkey = 'UNLOCKUSER' " +
	               "SET u.lock_date_and_time = NULL, " +
	               "    u.expire_lock_date_and_time = NULL, " +
	               "    u.account_non_locked = 1, " +
	               "    u.failed_attempts = 0 " +
	               "WHERE u.lock_date_and_time <= (NOW() - INTERVAL c.nsql_value DAY) " +
	               "  AND ((u.resignation_request_applied = 0 AND u.seperation_manager_approved = 0) " +
	               "    OR (u.resignation_request_applied = 1 AND u.seperation_manager_approved = 0)) " +
	               "  AND u.status = 'ACTIVE'",
	       nativeQuery = true)
	void unlockUsers();



	 @Query(value = "SELECT * FROM employeedetail u WHERE u.laptop_id = :laptopId AND u.laptop_serial_number = :serialNumber AND u.laptop_brand = :brand and u.id=:id  and u.status='ACTIVE' and ((u.resignation_request_applied='false' AND u.seperation_manager_approved='false')  OR (u.resignation_request_applied='true' AND u.seperation_manager_approved='false'))", nativeQuery = true)
	    Optional<UserDetail> findByLaptopDetails(@Param("laptopId") String laptopId, 
	                                             @Param("serialNumber") String serialNumber, 
	                                             @Param("brand") String brand,
	                                             @Param("id") int id);
	 
	 @Query(value = "SELECT count(1) FROM employeedetail u WHERE u.laptop_id = :laptopId AND u.laptop_serial_number = :serialNumber AND u.laptop_brand = :brand and u.id=:id  and u.status='ACTIVE' and ((u.resignation_request_applied='false' AND u.seperation_manager_approved='false')  OR (u.resignation_request_applied='true' AND u.seperation_manager_approved='false'))",nativeQuery = true)
	    int countByLaptopDetails(@Param("laptopId") String laptopId, 
	                                             @Param("serialNumber") String serialNumber, 
	                                             @Param("brand") String brand,
	                                             @Param("id") int id);
	 
	 @Query(value = "SELECT email FROM employeedetail " +
             "WHERE (base_location = :baseLocation AND company_id = :company_id AND role IN ('ROLE_HR','ROLE_ADMIN','ROLE_MANAGER')) " +
             "OR id = :user_id  and status='ACTIVE' and ((resignation_request_applied='false' AND seperation_manager_approved='false')  OR (resignation_request_applied='true' AND seperation_manager_approved='false'))", nativeQuery = true)
List<String> findEmailsByBaseLocationAndRoles(@Param("baseLocation") String baseLocation,
                                            @Param("company_id") String company_id,
                                            @Param("user_id") int user_id);

@Query(value = "SELECT * FROM employeedetail " +
             "WHERE (base_location = :baseLocation AND company_id = :company_id AND role IN ('ROLE_HR','ROLE_ADMIN','ROLE_MANAGER')) " +
             "OR id = :user_id and status='ACTIVE' and ((resignation_request_applied='false' AND seperation_manager_approved='false')  OR (resignation_request_applied='true' AND seperation_manager_approved='false'))", nativeQuery = true)
List<UserDetail> findIdsByBaseLocationAndRoles(@Param("baseLocation") String baseLocation,
                                             @Param("company_id") String company_id,
                                             @Param("user_id") int user_id);


@Query(value = "SELECT email FROM employeedetail " +
        "WHERE " +
        "(" +
        "(base_location = :baseLocation AND company_id = :company_id AND role IN ('ROLE_ADMIN','ROLE_IT')) " +
        "OR id = :user_id" +
        ") " +
        "AND status = 'ACTIVE' " +
        "AND enabled = true " +
        "AND (" +
        "(resignation_request_applied='false' AND seperation_manager_approved='false') " +
        "OR " +
        "(resignation_request_applied='true' AND seperation_manager_approved='false')" +
        ")",
        nativeQuery = true)
List<String> findEmailsByBaseLocationAndRolesPayment(
        @Param("baseLocation") String baseLocation,
        @Param("company_id") String company_id,
        @Param("user_id") int user_id);


@Query(value = "SELECT * FROM employeedetail " +
	       "WHERE (status='ACTIVE' " +
	       "AND ((resignation_request_applied='false' AND seperation_manager_approved='false')  OR (resignation_request_applied='true' AND seperation_manager_approved='false')) " +
	       "AND (base_location = :baseLocation AND company_id = :company_id AND role IN ('ROLE_ADMIN','ROLE_IT'))) " +
	       "OR id = :user_id", nativeQuery = true)
	List<UserDetail> findIdsByBaseLocationAndRolesPayment(@Param("baseLocation") String baseLocation,
	                                                      @Param("company_id") String company_id,
	                                                      @Param("user_id") int user_id);

@Query(value = "SELECT * FROM employeedetail WHERE team NOT LIKE 'T%' AND company_id = :companyId and status='ACTIVE' and ((resignation_request_applied='false' AND seperation_manager_approved='false')  OR (resignation_request_applied='true' AND seperation_manager_approved='false'))", nativeQuery = true)
List<UserDetail> findEmployeesWithoutTeamStartingWithT(@Param("companyId") String companyId);

@Query(value = "SELECT e.* FROM employeedetail e " +
        "WHERE e.status = 'ACTIVE' and ((e.resignation_request_applied='false' AND e.seperation_manager_approved='false')  OR (e.resignation_request_applied='true' AND e.seperation_manager_approved='false'))" +
        "AND e.company_id = :companyId " +
        "AND NOT EXISTS ( " +
        "    SELECT 1 FROM salary_slip ss " +
        "    WHERE ss.employee_id = e.id " +
        "    AND ss.payment_date >= DATE_FORMAT(CURRENT_DATE, '%Y-%m-01') " +
        "    AND ss.payment_date < DATE_FORMAT(DATE_ADD(CURRENT_DATE, INTERVAL 1 MONTH), '%Y-%m-01') " +
        ")", nativeQuery = true)
List<UserDetail> findActiveEmployeesWithoutSalarySlipForCurrentMonth(@Param("companyId") String companyId);

@Modifying
@Transactional
@Query(value = "DELETE FROM employeedetail u WHERE u.id = :empId",nativeQuery = true)
void deleteByEmpId(@Param("empId") Integer empId);

}
