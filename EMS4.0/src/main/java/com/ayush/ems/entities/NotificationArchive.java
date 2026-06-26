package com.ayush.ems.entities;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class NotificationArchive {
	  @Id
	    private String message_id; // manually set primary key

	    @Column(name = "user_id")
	    private int userId;

	    private String message;

	    @Column(name = "is_read")
	    private boolean read;

	    @Temporal(TemporalType.TIMESTAMP)
	    @Column(name = "notified_at")
	    private Date timestamp;
	    
	    private String senderEmail;
	    private String receiverEmail;
	    
	    private boolean markAsRead;
	    
	    private Date markAsReadDate;
	    
	    private Date adddate;
		private String addwho;
		private Date editdate;
		private String editwho;
}
