package com.ayush.ems.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ayush.ems.entities.Downtime_Maintaince;

@Repository
public interface DowntimeMaintaince_Dao extends JpaRepository<Downtime_Maintaince, Integer> {

    @Query("SELECT u FROM Downtime_Maintaince u WHERE u.downtimeDescription = ?1")
    Optional<Downtime_Maintaince> findByDescription(String description);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM Downtime_Maintaince u " +
           "WHERE u.downtimeDescription = ?1 AND u.serverDownOrNot = true")
    boolean server_status_check_active_or_not(String description);

    @Query("UPDATE Downtime_Maintaince u SET u.serverDownOrNot = true WHERE u.downtimeDescription = ?1")
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void markServerDown(String description);

    @Query("UPDATE Downtime_Maintaince u SET u.serverDownOrNot = false WHERE u.downtimeDescription = ?1")
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void markServerUp(String description);
}
