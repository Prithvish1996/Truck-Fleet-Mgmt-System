package com.saxion.proj.tfms.notification.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private Map<Long, Map<String, Object>> notifications = new HashMap<>();
    private Long idCounter = 1L;

    @GetMapping
    public List<Map<String, Object>> getAllNotifications() {
        return new ArrayList<>(notifications.values());
    }

    @PostMapping
    public Map<String, Object> sendNotification(@RequestBody Map<String, Object> notificationRequest) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", idCounter);
        notification.put("recipient", notificationRequest.get("recipient"));
        notification.put("message", notificationRequest.get("message"));
        notification.put("type", notificationRequest.getOrDefault("type", "INFO"));
        notification.put("sentAt", LocalDateTime.now());
        notification.put("status", "SENT");

        notifications.put(idCounter, notification);
        idCounter++;

        return notification;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getNotification(@PathVariable Long id) {
        return notifications.get(id);
    }

    @GetMapping("/types")
    public Map<String, Object> getNotificationTypes() {
        return Map.of("types", Arrays.asList("INFO", "WARNING", "ERROR", "SUCCESS"));
    }
}
