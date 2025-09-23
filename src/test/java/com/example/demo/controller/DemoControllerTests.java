package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for DemoController
 */
@WebMvcTest(DemoController.class)
@ActiveProfiles("test")
class DemoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthEndpoint_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void infoEndpoint_ShouldReturnApplicationInfo() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.features").isArray());
    }

    @Test
    void getAllUsers_ShouldReturnUserList() throws Exception {
        User user1 = new User("Alice", "alice@example.com");
        User user2 = new User("Bob", "bob@example.com");
        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        User user = new User("Alice", "alice@example.com");
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        User inputUser = new User("Charlie", "charlie@example.com");
        User savedUser = new User("Charlie", "charlie@example.com");
        savedUser.setId(3L);

        when(userService.saveUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Charlie"))
                .andExpect(jsonPath("$.email").value("charlie@example.com"));
    }

    @Test
    void deleteUser_ShouldReturnOk_WhenUserExists() throws Exception {
        User user = new User("Alice", "alice@example.com");
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void environmentEndpoint_ShouldReturnEnvironmentInfo() throws Exception {
        mockMvc.perform(get("/api/environment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.current_environment").exists())
                .andExpect(jsonPath("$.message").exists());
    }
}