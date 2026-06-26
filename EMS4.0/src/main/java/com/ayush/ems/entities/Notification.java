package com.ayush.ems.entities;

import javax.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Notification {

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
