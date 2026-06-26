package com.ayush.ems.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ayush.ems.dao.DowntimeMaintaince_Dao;
import com.ayush.ems.entities.Downtime_Maintaince;

@Service
public class DowntimeMaintainceService {

    @Autowired
    private DowntimeMaintaince_Dao repo;

    // Return Optional from DAO
    public Optional<Downtime_Maintaince> getDowntimeDetails(String description) {
        return repo.findByDescription(description);
    }

    /**
     * Returns true if server is UP, false if server is currently DOWN.
     */
    public boolean isServerUp(String description) {
        Optional<Downtime_Maintaince> downtimeOpt = getDowntimeDetails(description);

        if (!downtimeOpt.isPresent()) {
            return true; // No record => server is UP
        }

        Downtime_Maintaince downtime = downtimeOpt.get();

        if (!downtime.isServerDownOrNot()) {
            return true; // Flagged as UP
        }

        Date now = new Date();

        if (downtime.isServerDownManually()) {
            // Manual downtime check
            Date manualStart = downtime.getManualDowntimeStart();
            Date manualEnd = downtime.getManualDowntimeEnd();

            if (manualStart != null && manualEnd != null) {
                if (now.after(manualStart) && now.before(manualEnd)) {
                    return false; // In manual downtime window => DOWN
                } else {
                    // Outside manual downtime window but flagged down manually
                    return true; // Treat as UP outside window
                }
            } else {
                // No manual window set but flagged down manually
                return false; // Treat as DOWN if no time set but flagged
            }
        } else {
            // Scheduled downtime check
            Date scheduledStart = downtime.getServerDowntimeStart();
            Date scheduledEnd = downtime.getServerDowntimeEnd();

            if (scheduledStart != null && scheduledEnd != null) {
                if (now.after(scheduledStart) && now.before(scheduledEnd)) {
                    return false; // In scheduled downtime window => DOWN
                } else {
                    // Outside scheduled downtime window but flagged down
                    return true; // Treat as UP outside window
                }
            } else {
                return false; // No scheduled window but flagged down
            }
        }
    }

//    /**
//     * Mark server as DOWN for given description.
//     */
//    public void markServerDown(String description) {
//        // Example implementation: find record, update flag, save
//        Optional<Downtime_Maintaince> downtimeOpt = repo.findByDescription(description);
//        if (downtimeOpt.isPresent()) {
//            Downtime_Maintaince downtime = downtimeOpt.get();
//            downtime.setServerDownOrNot(true);
//            // You may want to update manual/scheduled times here as needed
//            repo.save(downtime);
//        }
//    }
//
//    /**
//     * Mark server as UP for given description.
//     */
//    public void markServerUp(String description) {
//        Optional<Downtime_Maintaince> downtimeOpt = repo.findByDescription(description);
//        if (downtimeOpt.isPresent()) {
//            Downtime_Maintaince downtime = downtimeOpt.get();
//            downtime.setServerDownOrNot(false);
//            repo.save(downtime);
//        }
//    }
}
