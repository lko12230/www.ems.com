package com.ayush.ems.entities;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Downtime_Maintaince {

    @Id
    private int sno;

    private String downtimeDescription;

    // Scheduled downtime times
    private Date serverDowntimeStart;
    private Date serverDowntimeEnd;

    // Manual downtime times
    private Date manualDowntimeStart;
    private Date manualDowntimeEnd;

    private String status;             // e.g., "Scheduled", "Completed"
    private boolean serverDownOrNot;   // True if server is currently down
    private boolean serverDownManually; // True if downtime is manual

    private Date adddate;
    private String addwho;
    private Date editdate;
    private String editwho;
}
