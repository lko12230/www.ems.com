package com.ayush.ems.dao;

import com.ayush.ems.entities.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationDao extends JpaRepository<Notification, String> {

	@Query(value = "SELECT * FROM notification n,employee e WHERE n.user_id = e.id and n.is_read = false and e.email = :email ORDER BY notified_at DESC LIMIT 20", nativeQuery = true)
	List<Notification> findNewMessgaesById(String email);

	@Query(value = "SELECT * FROM notification n, employee e WHERE n.user_id = e.id and n.is_read = true and e.email = :email ORDER BY notified_at DESC LIMIT 20", nativeQuery = true)
	List<Notification> findOldMessgaesById(String email);

	long countByUserIdAndReadFalse(int userId);

	@Modifying
	@Query(value = "UPDATE notification SET is_read = true, notified_at = :readTime, editdate = NOW(), editwho = 'SYSTEM', mark_as_read = true, mark_as_read_date = NOW() WHERE user_id = :userId AND is_read = false", nativeQuery = true)
	void markAllAsReadByUserId(@Param("userId") int userId, @Param("readTime") Date readTime);

	@Query(value = "SELECT * FROM notification WHERE message_id = :message_id", nativeQuery = true)
	Optional<Notification> findByMessageId(String message_id);

	@Query("SELECT n.message_id FROM Notification n ORDER BY n.message_id DESC")
	List<String> findLatestMessageId(Pageable pageable);
	
	@Query(value = "SELECT * FROM notification " +
            "WHERE notified_at < DATE_SUB(NOW(), INTERVAL " +
            "(SELECT nsql_value FROM nsql_config WHERE configkey = 'NUMBEROFDAYSNOTIFICATIONARCHIVE' LIMIT 1) DAY)",
    nativeQuery = true)
List<Notification> listOfNotification();


}
