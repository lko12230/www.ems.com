package com.ayush.ems.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ayush.ems.entities.Contact;

public interface ContactDao extends JpaRepository<Contact, Integer> {
	
	@Query("SELECT COALESCE(MAX(c.id), 0) FROM Contact c")
	long getContactLastId();  // ✅ Ensures 0 is returned instead of null

	
	@Query(value = "select * from contact_requests u where (u.email = :email or u.phone = :phone) and u.adddate >= (NOW() - INTERVAL (SELECT nsql_value FROM nsql_config where configkey='CONTACTAGAINREGISTER') DAY) order by adddate desc limit 1",nativeQuery = true)
	public Optional<Contact> findByEmailAndPhone(@Param("email") String email, @Param("phone") String phone);

}
