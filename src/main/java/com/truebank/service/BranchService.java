package com.truebank.service;

import com.truebank.model.Branch;
import com.truebank.model.User;
import com.truebank.repository.BranchRepository;
import com.truebank.dto.BranchCreationRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing branches
 */
@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private static final Logger logger = LoggerFactory.getLogger(BranchService.class);

    /**
     * Get all branches
     */
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    /**
     * Get all active branches
     */
    public List<Branch> getAllActiveBranches() {
        return branchRepository.findByActive(true);
    }

    /**
     * Get branch by ID
     */
    public Branch getBranchById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found with id: " + id));
    }

    /**
     * Get branch by branch code
     */
    public Branch getBranchByCode(String branchCode) {
        return branchRepository.findByBranchCode(branchCode)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found with code: " + branchCode));
    }

    /**
     * Create a new branch
     */
    public Branch createBranch(Branch branch) {
        // Validate branch code (must be unique)
        if (branchRepository.existsByBranchCode(branch.getBranchCode())) {
            throw new IllegalArgumentException("Branch code already exists: " + branch.getBranchCode());
        }
        
        // Validate phone number format
        if (branch.getPhoneNumber() == null || !branch.getPhoneNumber().matches("\\d{10,12}")) {
            throw new IllegalArgumentException("Phone number must be between 10 and 12 digits");
        }
        
        // Enforce required fields
        if (branch.getName() == null || branch.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch name is required");
        }
        
        if (branch.getAddress() == null || branch.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch address is required");
        }
        
        if (branch.getCity() == null || branch.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch city is required");
        }
        
        if (branch.getState() == null || branch.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch state is required");
        }
        
        if (branch.getZipCode() == null || branch.getZipCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch ZIP code is required");
        }
        
        // Set created timestamp if not set
        if (branch.getCreatedAt() == null) {
            branch.setCreatedAt(java.time.LocalDateTime.now());
        }
        
        // Save the branch
        return branchRepository.save(branch);
    }

    /**
     * Update branch 
     */
    public Branch updateBranch(Branch branch) {
        // Check if branch exists
        getBranchById(branch.getId());
        
        // Save and return the updated branch
        return branchRepository.save(branch);
    }

    /**
     * Update branch using BranchCreationRequest
     */
    public Branch updateBranch(Long id, BranchCreationRequest branchRequest) {
        // Check if branch exists
        Branch existingBranch = getBranchById(id);
        
        // Ensure the branch code isn't being changed
        if (!existingBranch.getBranchCode().equals(branchRequest.getBranchCode())) {
            throw new IllegalArgumentException("Branch code cannot be changed");
        }
        
        // Update branch details
        existingBranch.setName(branchRequest.getName());
        existingBranch.setBranchCode(branchRequest.getBranchCode());
        existingBranch.setAddress(branchRequest.getAddress());
        existingBranch.setCity(branchRequest.getCity());
        existingBranch.setState(branchRequest.getState());
        existingBranch.setZipCode(branchRequest.getZipCode());
        existingBranch.setPhoneNumber(branchRequest.getPhoneNumber());
        
        return branchRepository.save(existingBranch);
    }

    /**
     * Assign a manager to a branch
     *
     * @param branchId branch id
     * @param manager manager to assign
     * @return updated branch
     */
    public Branch assignManager(Long branchId, User manager) {
        Branch branch = getBranchById(branchId);
        branch.setManager(manager);
        return branchRepository.save(branch);
    }

    /**
     * Get branch by manager
     */
    public Branch getBranchByManager(User manager) {
        return branchRepository.findByManager(manager)
                .orElseThrow(() -> new EntityNotFoundException("No branch found for manager: " + manager.getUsername()));
    }

    /**
     * Deactivate branch
     */
    public void deactivateBranch(Long id) {
        Branch branch = getBranchById(id);
        branch.setActive(false);
        branchRepository.save(branch);
    }

    /**
     * Activate branch
     */
    public void activateBranch(Long id) {
        Branch branch = getBranchById(id);
        branch.setActive(true);
        branchRepository.save(branch);
    }

    /**
     * Check if branch code exists
     */
    public boolean branchCodeExists(String branchCode) {
        return branchRepository.existsByBranchCode(branchCode);
    }

    /**
     * Get branch count
     */
    public long getBranchCount() {
        return branchRepository.count();
    }
    
    /**
     * Get recent branches with limit
     */
    public List<Branch> getRecentBranches(int limit) {
        return branchRepository.findRecentBranches(limit);
    }

    /**
     * Get branches without manager
     */
    public List<Branch> getBranchesWithoutManager() {
        try {
            return branchRepository.findByManagerIsNull();
        } catch (Exception e) {
            logger.error("Error fetching branches without manager: {}", e.getMessage(), e);
            // Fallback to get all branches and filter them
            return getAllBranches().stream()
                    .filter(branch -> branch.getManager() == null)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Create a new branch from BranchRequest
     */
    public Branch createBranch(com.truebank.dto.BranchRequest branchRequest) {
        Branch branch = new Branch();
        branch.setName(branchRequest.getName());
        branch.setBranchCode(branchRequest.getBranchCode());
        branch.setAddress(branchRequest.getAddress());
        branch.setCity(branchRequest.getCity());
        branch.setState(branchRequest.getState());
        branch.setZipCode(branchRequest.getZipCode());
        branch.setPhoneNumber(branchRequest.getPhoneNumber());
        branch.setActive(true);
        branch.setCreatedAt(java.time.LocalDateTime.now());
        
        return createBranch(branch);
    }

    /**
     * Delete branch
     */
    public void deleteBranch(Long id) {
        Branch branch = getBranchById(id);
        
        // Check if branch has any associated accounts
        if (!branch.getAccounts().isEmpty()) {
            throw new IllegalStateException("Cannot delete branch with active accounts");
        }
        
        // Check if branch has a manager
        if (branch.getManager() != null) {
            throw new IllegalStateException("Cannot delete branch with an assigned manager");
        }
        
        branchRepository.delete(branch);
    }
}
