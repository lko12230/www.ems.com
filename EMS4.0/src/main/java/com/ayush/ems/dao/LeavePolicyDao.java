
package com.ayush.ems.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ayush.ems.entities.LeavePolicy;

public interface LeavePolicyDao extends JpaRepository<LeavePolicy, Integer> {
    @Query(value = "SELECT l.leave_quota FROM leave_policy l WHERE l.employee_id = :employeeId AND l.year = :year",nativeQuery = true)
    int getAssignedLeaveQuota(@Param("employeeId") int employeeId, @Param("year") int year);

    @Query(value = "SELECT l.wfh_quota FROM leave_policy l WHERE l.employee_id = :employeeId AND l.year = :year",nativeQuery = true)
    int getAssignedWfhQuota(@Param("employeeId") int employeeId, @Param("year") int year);
}
