package com.truebank.service.impl;

import com.truebank.model.Manager;
import com.truebank.model.Branch;
import com.truebank.model.User;
import com.truebank.dto.ManagerCreationRequest;
import com.truebank.repository.ManagerRepository;
import com.truebank.repository.BranchRepository;
import com.truebank.service.ManagerService;
import com.truebank.service.UserService;
import com.truebank.service.BranchService;
import com.truebank.service.SupabaseAuthService;
import com.truebank.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerServiceImpl implements ManagerService {

    private static final Logger logger = LoggerFactory.getLogger(ManagerServiceImpl.class);
    
    private final ManagerRepository managerRepository;
    private final BranchRepository branchRepository;
    private final UserService userService;
    private final BranchService branchService;
    private final SupabaseAuthService supabaseAuthService;
    private final EmailService emailService;
    
    @Override
    public List<Manager> getAllManagers() {
        return managerRepository.findAll();
    }
    
    @Override
    public Manager getManagerById(Long id) {
        return managerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manager not found with ID: " + id));
    }
    
    @Override
    @Transactional
    public Manager createManager(ManagerCreationRequest managerRequest) {
        try {
            // Check if branch exists
            Branch branch = branchService.getBranchById(managerRequest.getBranchId());
            
            // Check if branch already has a manager
            if (branch.getManager() != null) {
                throw new RuntimeException("Branch already has a manager assigned");
            }
            
            // Create a user object for manager authentication
            User user = new User();
            user.setFirstName(managerRequest.getFirstName());
            user.setLastName(managerRequest.getLastName());
            user.setEmail(managerRequest.getEmail());
            user.setPhoneNumber(managerRequest.getPhoneNumber());
            user.setAadharNumber(managerRequest.getAadharNumber());
            user.setAddress(managerRequest.getAddress());
            user.setAge(managerRequest.getAge());
            
            // Generate username and password
            String username = managerRequest.getFirstName().toLowerCase() + branch.getBranchCode();
            String password = "manager@123";
            
            // Check if username already exists
            if (!userService.isUsernameAvailable(username)) {
                // Add random number to username
                Random random = new Random();
                username = username + random.nextInt(100);
            }
            
            user.setUsername(username);
            
            // Create user account in authentication system
            User createdUser = supabaseAuthService.createManager(user, password);
            
            // Map user properties to manager
            Manager manager = new Manager();
            manager.setId(createdUser.getId());
            manager.setFirstName(createdUser.getFirstName());
            manager.setLastName(createdUser.getLastName());
            manager.setUsername(createdUser.getUsername());
            manager.setEmail(createdUser.getEmail());
            manager.setPhoneNumber(createdUser.getPhoneNumber());
            manager.setAadharNumber(createdUser.getAadharNumber());
            manager.setAddress(createdUser.getAddress());
            manager.setAge(createdUser.getAge());
            manager.setBranch(branch);
            manager.setIsActive(true);
            
            // Save manager to database
            Manager savedManager = managerRepository.save(manager);
            
            // Assign manager to branch
            branch.setManager(createdUser);
            branchRepository.save(branch);
            
            try {
                // Update the branch reference in the manager
                savedManager.setBranch(branchRepository.findById(branch.getId()).orElse(branch));
                managerRepository.save(savedManager);
            } catch (Exception e) {
                logger.warn("Error updating manager branch reference: {}", e.getMessage());
                // Continue even if this update fails
            }
            
            // Attempt to send credentials email
            try {
                emailService.sendManagerCredentialsEmail(
                    savedManager.getEmail(),
                    username,
                    password,
                    branch.getName()
                );
            } catch (Exception e) {
                logger.warn("Failed to send credentials email: {}", e.getMessage());
                // Continue despite email failure
            }
            
            return savedManager;
        } catch (Exception e) {
            logger.error("Error creating manager: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create manager: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public Manager updateManager(Long id, ManagerCreationRequest managerRequest) {
        try {
            Manager existingManager = getManagerById(id);
            
            // Update manager details
            existingManager.setFirstName(managerRequest.getFirstName());
            existingManager.setLastName(managerRequest.getLastName());
            existingManager.setEmail(managerRequest.getEmail());
            existingManager.setPhoneNumber(managerRequest.getPhoneNumber());
            existingManager.setAadharNumber(managerRequest.getAadharNumber());
            existingManager.setAddress(managerRequest.getAddress());
            existingManager.setAge(managerRequest.getAge());
            
            // If branch assignment has changed
            if (!existingManager.getBranch().getId().equals(managerRequest.getBranchId())) {
                // Check if new branch exists
                Branch newBranch = branchService.getBranchById(managerRequest.getBranchId());
                
                // Check if new branch already has a manager
                if (newBranch.getManager() != null) {
                    throw new RuntimeException("New branch already has a manager assigned");
                }
                
                // Remove manager from old branch
                Branch oldBranch = existingManager.getBranch();
                oldBranch.setManager(null);
                branchRepository.save(oldBranch);
                
                // Assign manager to new branch
                // Get the User object for the manager to assign to branch
                User managerUser = userService.getUserById(existingManager.getId());
                newBranch.setManager(managerUser);
                branchRepository.save(newBranch);
                
                existingManager.setBranch(newBranch);
            }
            
            return managerRepository.save(existingManager);
        } catch (Exception e) {
            logger.error("Error updating manager: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update manager: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void deleteManager(Long id) {
        try {
            Manager manager = getManagerById(id);
            
            // Remove manager from branch
            if (manager.getBranch() != null) {
                Branch branch = manager.getBranch();
                branch.setManager(null);
                branchRepository.save(branch);
            }
            
            // Delete manager
            managerRepository.deleteById(id);
            
            // Delete user account from authentication system
            supabaseAuthService.deleteUserById(id);
        } catch (Exception e) {
            logger.error("Error deleting manager: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete manager: " + e.getMessage(), e);
        }
    }
} 