package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

/**
 * Service class for User operations
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Save user
     */
    public User saveUser(User user) {
        // Check if email already exists (for new users)
        if (user.getId() == null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Search users by name
     */
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get count of users created today
     */
    public long getUsersCreatedToday() {
        return userRepository.countUsersCreatedToday();
    }

    /**
     * Initialize demo data
     */
    @PostConstruct
    public void initDemoData() {
        if (userRepository.count() == 0) {
            // Add some demo users
            userRepository.save(new User("Alice Johnson", "alice@example.com"));
            userRepository.save(new User("Bob Smith", "bob@example.com"));
            userRepository.save(new User("Carol Davis", "carol@example.com"));
            userRepository.save(new User("David Wilson", "david@example.com"));
            
            System.out.println("Demo users created successfully!");
        }
    }
}