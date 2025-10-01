package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @GetMapping
    public String getNotifications() {
        return "You have 5 new notifications";
    }
    
    @PostMapping("/send")
    public String sendNotification(@RequestBody String message) {
        return "Notification sent: " + message;
    }
}
