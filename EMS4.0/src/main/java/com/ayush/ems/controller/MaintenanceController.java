package com.ayush.ems.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ayush.ems.entities.Downtime_Maintaince;
import com.ayush.ems.service.DowntimeMaintainceService;

@Controller
public class MaintenanceController {

    @Autowired
    private DowntimeMaintainceService downtimeService;

    private static final String DOWNTIME_DESC = "Server Maintenance";

    @GetMapping("/server-down")
    public String maintenancePage(Model model) {

        Optional<Downtime_Maintaince> downtimeOpt = downtimeService.getDowntimeDetails(DOWNTIME_DESC);

        if (downtimeOpt.isPresent()) {
            Downtime_Maintaince dt = downtimeOpt.get();

            model.addAttribute("serverDown", dt.isServerDownOrNot());
            model.addAttribute("serverDownManually", dt.isServerDownManually());

            // Pass scheduled start/end as ISO strings or null
            model.addAttribute("scheduledStart", dt.getServerDowntimeStart() != null ? dt.getServerDowntimeStart().getTime() : null);
            model.addAttribute("scheduledEnd", dt.getServerDowntimeEnd() != null ? dt.getServerDowntimeEnd().getTime() : null);

            // Pass manual downtime start/end as ISO or null
            model.addAttribute("manualStart", dt.getManualDowntimeStart() != null ? dt.getManualDowntimeStart().getTime() : null);
            model.addAttribute("manualEnd", dt.getManualDowntimeEnd() != null ? dt.getManualDowntimeEnd().getTime() : null);
        } else {
            // Default if no record
            model.addAttribute("serverDown", false);
            model.addAttribute("serverDownManually", false);
            model.addAttribute("scheduledStart", null);
            model.addAttribute("scheduledEnd", null);
            model.addAttribute("manualStart", null);
            model.addAttribute("manualEnd", null);
        }

        return "server-down";  // Thymeleaf template: maintenance.html
    }
}
