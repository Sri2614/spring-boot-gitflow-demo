package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for user profile enhancement features
 */
@ExtendWith(MockitoExtension.class)
class UserProfileEnhancementTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", "+1-555-0100", "Engineering", "Software Engineer");
        testUser.setId(1L);
        testUser.setIsActive(true);
    }

    @Test
    void testGetUsersByDepartment() {
        // Given
        List<User> engineeringUsers = Arrays.asList(testUser);
        when(userRepository.findByDepartmentIgnoreCase("Engineering")).thenReturn(engineeringUsers);

        // When
        List<User> result = userService.getUsersByDepartment("Engineering");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getDepartment());
        verify(userRepository).findByDepartmentIgnoreCase("Engineering");
    }

    @Test
    void testGetActiveUsers() {
        // Given
        List<User> activeUsers = Arrays.asList(testUser);
        when(userRepository.findByIsActiveTrue()).thenReturn(activeUsers);

        // When
        List<User> result = userService.getActiveUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
        verify(userRepository).findByIsActiveTrue();
    }

    @Test
    void testGetInactiveUsers() {
        // Given
        User inactiveUser = new User("Jane Doe", "jane@example.com", "+1-555-0101", "HR", "HR Manager");
        inactiveUser.setIsActive(false);
        List<User> inactiveUsers = Arrays.asList(inactiveUser);
        when(userRepository.findByIsActiveFalse()).thenReturn(inactiveUsers);

        // When
        List<User> result = userService.getInactiveUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsActive());
        verify(userRepository).findByIsActiveFalse();
    }

    @Test
    void testGetUsersByJobTitle() {
        // Given
        List<User> engineers = Arrays.asList(testUser);
        when(userRepository.findByJobTitleContainingIgnoreCase("Engineer")).thenReturn(engineers);

        // When
        List<User> result = userService.getUsersByJobTitle("Engineer");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getJobTitle().contains("Engineer"));
        verify(userRepository).findByJobTitleContainingIgnoreCase("Engineer");
    }

    @Test
    void testUserProfileFields() {
        // Test all new profile fields
        assertNotNull(testUser.getPhoneNumber());
        assertNotNull(testUser.getDepartment());
        assertNotNull(testUser.getJobTitle());
        assertNotNull(testUser.getIsActive());

        assertEquals("+1-555-0100", testUser.getPhoneNumber());
        assertEquals("Engineering", testUser.getDepartment());
        assertEquals("Software Engineer", testUser.getJobTitle());
        assertTrue(testUser.getIsActive());
    }

    @Test
    void testUserStatusToggle() {
        // Test status activation/deactivation
        assertTrue(testUser.getIsActive());

        testUser.setIsActive(false);
        assertFalse(testUser.getIsActive());

        testUser.setIsActive(true);
        assertTrue(testUser.getIsActive());
    }

    @Test
    void testUserToString() {
        // Test enhanced toString method includes new fields
        String userString = testUser.toString();
        assertTrue(userString.contains("phoneNumber"));
        assertTrue(userString.contains("department"));
        assertTrue(userString.contains("jobTitle"));
        assertTrue(userString.contains("isActive"));
    }
}