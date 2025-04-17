package com.truebank.controller;

import com.truebank.model.Branch;
import com.truebank.service.BranchService;
import com.truebank.dto.BranchCreationRequest;
import com.truebank.dto.ApiResponse;
import com.truebank.model.User;
import com.truebank.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;
    private final UserService userService;

    /**
     * Create a new branch
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> createBranch(@RequestBody BranchCreationRequest branchRequest) {
        try {
            Branch branch = new Branch();
            branch.setName(branchRequest.getName());
            branch.setBranchCode(branchRequest.getBranchCode());
            branch.setAddress(branchRequest.getAddress());
            branch.setCity(branchRequest.getCity());
            branch.setState(branchRequest.getState());
            branch.setZipCode(branchRequest.getZipCode());
            branch.setPhoneNumber(branchRequest.getPhoneNumber());
            branch.setActive(true);
            
            Branch createdBranch = branchService.createBranch(branch);
            
            return ResponseEntity.ok(new ApiResponse(true, "Branch created successfully", createdBranch));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Get all branches
     */
    @GetMapping
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<List<Branch>> getAllBranches() {
        List<Branch> branches = branchService.getAllBranches();
        return ResponseEntity.ok(branches);
    }

    /**
     * Get branch by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> getBranchById(@PathVariable Long id) {
        try {
            Branch branch = branchService.getBranchById(id);
            return ResponseEntity.ok(branch);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Update branch
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> updateBranch(@PathVariable Long id, @RequestBody BranchCreationRequest branchRequest) {
        try {
            Branch updatedBranch = branchService.updateBranch(id, branchRequest);
            return ResponseEntity.ok(new ApiResponse(true, "Branch updated successfully", updatedBranch));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Activate/deactivate branch
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> updateBranchStatus(@PathVariable Long id, @RequestParam boolean active) {
        try {
            Branch branch = branchService.getBranchById(id);
            branch.setActive(active);
            
            Branch updatedBranch = branchService.updateBranch(branch);
            
            String message = active ? "Branch activated successfully" : "Branch deactivated successfully";
            return ResponseEntity.ok(new ApiResponse(true, message, updatedBranch));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Assign manager to branch
     */
    @PutMapping("/{branchId}/assign-manager/{managerId}")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> assignManager(@PathVariable Long branchId, @PathVariable Long managerId) {
        try {
            User manager = userService.getUserById(managerId);
            Branch updatedBranch = branchService.assignManager(branchId, manager);
            return ResponseEntity.ok(new ApiResponse(true, "Manager assigned to branch successfully", updatedBranch));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Unassign manager from branch
     */
    @PutMapping("/{branchId}/unassign-manager")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> unassignManager(@PathVariable Long branchId) {
        try {
            Branch branch = branchService.getBranchById(branchId);
            
            // Check if branch has a manager assigned
            if (branch.getManager() == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Branch does not have a manager assigned", null));
            }
            
            // Set manager to null
            branch.setManager(null);
            Branch updatedBranch = branchService.updateBranch(branch);
            
            return ResponseEntity.ok(new ApiResponse(true, "Manager unassigned from branch successfully", updatedBranch));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
