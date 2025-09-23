package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot GitFlow Demo Application
 * 
 * This application demonstrates:
 * - GitHub workflows and CI/CD pipelines
 * - GitFlow branching strategy
 * - Environment-based deployments (DEV, TST, UAT, PREPROD, PROD)
 * - Automated release management
 * - Docker containerization
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}