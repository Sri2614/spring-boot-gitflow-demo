package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for the Demo Application
 */
@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures the Spring context loads correctly
    }

    @Test
    void mainMethodTest() {
        // Test that main method doesn't throw exceptions
        DemoApplication.main(new String[]{});
    }
}