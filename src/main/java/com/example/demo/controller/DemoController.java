package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo REST Controller showcasing various endpoints for testing workflows
 */
@RestController
@RequestMapping("/api")
public class DemoController {

    @Autowired
    private UserService userService;

    @Value("${spring.application.name:spring-boot-gitflow-demo}")
    private String applicationName;

    @Value("${demo.environment:unknown}")
    private String environment;

    @Value("${demo.version:unknown}")
    private String version;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", applicationName);
        response.put("environment", environment);
        response.put("version", version);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Get application information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", applicationName);
        response.put("environment", environment);
        response.put("version", version);
        response.put("description", "Spring Boot GitFlow Demo Application");
        response.put("features", List.of(
            "GitHub Workflows",
            "GitFlow Strategy", 
            "Multi-Environment Deployments",
            "Automated Release Management",
            "Docker Support",
            "Comprehensive Testing"
        ));
        return ResponseEntity.ok(response);
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new user
     */
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * Update existing user
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.getUserById(id)
                .map(existingUser -> {
                    user.setId(id);
                    return ResponseEntity.ok(userService.saveUser(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> {
                    userService.deleteUser(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search users by department
     */
    @GetMapping("/users/search/department/{department}")
    public ResponseEntity<List<User>> getUsersByDepartment(@PathVariable String department) {
        List<User> users = userService.getUsersByDepartment(department);
        return ResponseEntity.ok(users);
    }

    /**
     * Search active users
     */
    @GetMapping("/users/search/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> activeUsers = userService.getActiveUsers();
        return ResponseEntity.ok(activeUsers);
    }

    /**
     * Update user status (activate/deactivate)
     */
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        return userService.getUserById(id)
                .map(user -> {
                    user.setIsActive(isActive);
                    User updatedUser = userService.saveUser(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to test different environments
     */
    @GetMapping("/environment")
    public ResponseEntity<Map<String, String>> getEnvironmentInfo() {
        Map<String, String> envInfo = new HashMap<>();
        envInfo.put("current_environment", environment);
        envInfo.put("message", getEnvironmentMessage());
        return ResponseEntity.ok(envInfo);
    }

    private String getEnvironmentMessage() {
        return switch (environment.toLowerCase()) {
            case "dev" -> "Development environment - for active development and testing";
            case "tst", "test" -> "Test environment - for integration testing";
            case "uat" -> "User Acceptance Testing environment - for business validation";
            case "preprod", "pre-prod" -> "Pre-production environment - final testing before production";
            case "prod", "production" -> "Production environment - live system";
            default -> "Unknown environment";
        };
    }
}