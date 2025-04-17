package com.truebank.service;

import com.truebank.model.User;
import com.truebank.model.SystemActivity;
import com.truebank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SystemActivityService systemActivityService;
    private final BranchService branchService;

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    /**
     * Get user count
     */
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * Get manager count
     */
    public long getManagerCount() {
        return userRepository.countByRoles_Name("ROLE_MANAGER");
    }

    /**
     * Get all managers
     */
    public List<User> getAllManagers() {
        try {
            List<User> managers = userRepository.findByRoles_Name("ROLE_MANAGER");
            System.out.println("Found " + managers.size() + " managers");
            return managers;
        } catch (Exception e) {
            System.err.println("Error retrieving managers: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Return empty list on error
        }
    }

    /**
     * Get all managers without a branch assignment
     */
    public List<User> getAllManagersWithoutBranch() {
        try {
            // SQL to find managers not assigned to any branch
            return userRepository.findManagersWithoutBranch();
        } catch (Exception e) {
            System.err.println("Error retrieving available managers: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Return empty list on error
        }
    }

    /**
     * Update user profile
     */
    public User updateUser(User user) {
        // Check if user exists
        User existingUser = getUserById(user.getId());
        
        // Update user details
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setAge(user.getAge());
        existingUser.setAddress(user.getAddress());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        
        User updatedUser = userRepository.save(existingUser);
        
        // Log the activity
        systemActivityService.logActivity(
            "User profile updated: " + user.getUsername(),
            SystemActivity.ActivityType.UPDATE,
            user
        );
        
        return updatedUser;
    }

    /**
     * Check if username exists
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * Check if phone number exists
     */
    public boolean isPhoneNumberAvailable(String phoneNumber) {
        return !userRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * Check if Aadhar number exists
     */
    public boolean isAadharNumberAvailable(String aadharNumber) {
        return !userRepository.existsByAadharNumber(aadharNumber);
    }

    /**
     * Deactivate user
     */
    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
        
        // Log the activity
        systemActivityService.logActivity(
            "User deactivated: " + user.getUsername(),
            SystemActivity.ActivityType.UPDATE, 
            user
        );
    }

    /**
     * Activate user
     */
    public void activateUser(Long id) {
        User user = getUserById(id);
        user.setActive(true);
        userRepository.save(user);
        
        // Log the activity
        systemActivityService.logActivity(
            "User activated: " + user.getUsername(),
            SystemActivity.ActivityType.UPDATE,
            user
        );
    }
    
    /**
     * Get recent system activities
     */
    public List<SystemActivity> getRecentSystemActivities(int limit) {
        return systemActivityService.getRecentActivities(limit);
    }
}
