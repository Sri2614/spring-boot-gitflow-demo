package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    @GetMapping("/{userId}")
    public String getUserProfile(@PathVariable String userId) {
        return "Profile for user: " + userId;
    }
    
    @PutMapping("/{userId}")
    public String updateProfile(@PathVariable String userId, @RequestBody String data) {
        return "Updated profile for user: " + userId;
    }
}
