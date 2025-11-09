package com.saxion.proj.tfms.planner.services.routeServices;

import com.saxion.proj.tfms.commons.model.DriverDao;
import com.saxion.proj.tfms.commons.model.RouteDao;
import org.springframework.stereotype.Component;

@Component
public class NotificationService {

    /**
     * Sends a notification to a driver when a route is assigned.
     * (Stub implementation; extend later for email, push, etc.)
     */
    public void sendDriverAssignmentNotification(DriverDao driver, RouteDao route) {
        if (driver == null || route == null) {
            return;
        }

        String message = String.format(
                "Driver '%s' has been assigned to Route #%d for Truck '%s'.",
                driver.getUser().getUsername(),
                route.getId(),
                route.getTruck() != null ? route.getTruck().getPlateNumber() : "N/A"
        );
    }
}
