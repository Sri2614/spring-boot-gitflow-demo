package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @GetMapping("/stats")
    public String getStats() {
        return "Dashboard Statistics: Users=100, Orders=50";
    }
    
    @GetMapping("/summary")
    public String getSummary() {
        return "Dashboard Summary for today";
    }
}
