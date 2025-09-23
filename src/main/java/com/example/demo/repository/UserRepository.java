package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find users by name containing the given string (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Count users created after a certain date
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= CURRENT_DATE")
    long countUsersCreatedToday();
}