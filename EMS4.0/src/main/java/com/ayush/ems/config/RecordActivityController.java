package com.ayush.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ayush.ems.entities.User;
import com.ayush.ems.service.Servicelayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // Allow requests from any origin
public class RecordActivityController {

    private static final Logger logger = LoggerFactory.getLogger(RecordActivityController.class); // Logger setup

    @Autowired
    private Servicelayer servicelayer;

    @PostMapping("/log-activity")
    public String logActivity(@RequestBody Map<String, String> requestData) {
        System.out.println("HELLO API HIT");
        String email = requestData.get("email");
        String ipAddress = requestData.get("ipAddress");
        String functionality = requestData.get("functionality");
        String addWho = "System"; // Dynamic value or hardcoded
        System.out.println("HELLO WORLD");

        // Log the email and IP address received in the request
        logger.info("EMAIL = {} , IPADDRESS = {}", email, ipAddress);

        // Dummy logic for employeeId and employeeName
        int employeeId;
        User user = servicelayer.findByUsername(email); // Replace with logic to fetch ID
        String get_email = email;
        String employeeName = null;
        String company_id=null;
        if(email==null || user == null)
        {
        	employeeName = "USER"; // Replace with logic to fetch name
        	email="user@gmail.com";
        }
        else
        {
        	employeeName = user.getUsername();
        	get_email=email;
        }
        if(user==null)
        {
        	employeeId=1221;
        	company_id="ABC110092";
        }
        else
        {
        	employeeId=user.getId();
        	company_id=user.getCompany_id();
        }
        

        // Save the record activity to the database
        servicelayer.saveRecordActivity(company_id,get_email,employeeId, employeeName, ipAddress, functionality, addWho);

        // Log confirmation
        logger.info("Activity logged successfully for EMAIL = {}", email);

        return "Activity logged successfully!";
    }
}
