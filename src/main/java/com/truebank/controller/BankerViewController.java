package com.truebank.controller;

import com.truebank.model.*;
import com.truebank.service.*;
import com.truebank.dto.*;
import com.truebank.exception.ResourceNotFoundException;
import com.truebank.security.services.UserDetailsImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/banker")
@RequiredArgsConstructor
@Slf4j
public class BankerViewController {

    private static final Logger logger = LoggerFactory.getLogger(BankerViewController.class);
    
    private final UserService userService;
    private final BranchService branchService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;
    private final SupabaseAuthService supabaseAuthService;
    private final EmailService emailService;
    private final ManagerService managerService;

    /**
     * Helper method to get the authenticated banker
     */
    private User getAuthenticatedBanker() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
                logger.error("Authentication error: Principal is not UserDetailsImpl");
                throw new RuntimeException("Authentication error: Invalid principal type");
            }
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userService.getUserById(userDetails.getId());
        } catch (Exception e) {
            logger.error("Error getting authenticated banker: {}", e.getMessage());
            throw new RuntimeException("Failed to get authenticated banker", e);
        }
    }

    /**
     * Banker dashboard page
     */
    @GetMapping({"/dashboard", "", "/"})
    @PreAuthorize("hasRole('BANKER')")
    public String dashboard(Model model) {
        try {
            // Try to get current banker if authenticated
            try {
            User banker = getAuthenticatedBanker();
            model.addAttribute("banker", banker);
            
                // Try to get branch if assigned
                try {
            Branch branch = branchService.getBranchByManager(banker);
            model.addAttribute("branch", branch);
            model.addAttribute("hasBranch", branch != null);
                } catch (Exception e) {
                    logger.warn("Error getting branch for banker: {}", e.getMessage());
                    model.addAttribute("branch", null);
                    model.addAttribute("hasBranch", false);
                }
            } catch (Exception e) {
                logger.warn("User not authenticated or error getting banker: {}", e.getMessage());
                model.addAttribute("banker", new User());
                model.addAttribute("branch", null);
                model.addAttribute("hasBranch", false);
            }
            
            // Get system activities
            try {
            List<SystemActivity> recentActivities = userService.getRecentSystemActivities(5);
            model.addAttribute("recentActivities", recentActivities);
            } catch (Exception e) {
                logger.warn("Error getting recent activities: {}", e.getMessage());
                model.addAttribute("recentActivities", new ArrayList<>());
            }
            
            // Get pending loan applications count
            try {
            int pendingLoans = loanService.countPendingLoanApplications();
            model.addAttribute("pendingLoans", pendingLoans);
            } catch (Exception e) {
                logger.warn("Error getting pending loans count: {}", e.getMessage());
                model.addAttribute("pendingLoans", 0);
            }
            
            // Get today's new accounts count
            try {
            int newAccounts = accountService.countTodayNewAccounts();
            model.addAttribute("newAccounts", newAccounts);
            } catch (Exception e) {
                logger.warn("Error getting new accounts count: {}", e.getMessage());
                model.addAttribute("newAccounts", 0);
            }
            
            // Get recent branches
            try {
                List<Branch> recentBranches = branchService.getRecentBranches(5);
                model.addAttribute("recentBranches", recentBranches);
            } catch (Exception e) {
                logger.warn("Error getting recent branches: {}", e.getMessage());
                model.addAttribute("recentBranches", new ArrayList<>());
            }
            
            return "banker/dashboard";
        } catch (Exception e) {
            logger.error("Error loading banker dashboard: {}", e.getMessage(), e);
            
            // Add error message and empty data to prevent template errors
            model.addAttribute("errorMessage", "Failed to load dashboard data. Please try again later.");
            model.addAttribute("banker", new User());
            model.addAttribute("branch", null);
            model.addAttribute("hasBranch", false);
            model.addAttribute("recentActivities", new ArrayList<>());
            model.addAttribute("pendingLoans", 0);
            model.addAttribute("newAccounts", 0);
            model.addAttribute("recentBranches", new ArrayList<>());
            
            return "banker/dashboard";
        }
    }

    /**
     * Branches management page - public access without security
     */
    @GetMapping("/branches")
    public String branches(Model model) {
        List<Branch> branches = branchService.getAllBranches();
        model.addAttribute("branches", branches);
        return "banker/branches";
    }

    /**
     * Add branch page
     */
    @GetMapping("/branches/create")
    public String createBranchForm(Model model) {
        model.addAttribute("branchRequest", new BranchCreationRequest());
        return "banker/create-branch";
    }

    /**
     * Handle branch addition form submission
     */
    @PostMapping("/branches/create")
    public String createBranch(@Valid @ModelAttribute("branchRequest") BranchCreationRequest branchRequest,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        logger.info("Received branch creation request for branch code: {}", branchRequest.getBranchCode());
        
        if (result.hasErrors()) {
            logger.warn("Validation errors in branch creation form: {}", result.getAllErrors());
            model.addAttribute("errorMessage", "Please correct the errors in the form.");
            return "banker/create-branch";
        }
        
        try {
            // Convert BranchCreationRequest to Branch
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
            logger.info("Branch created successfully with ID: {}", createdBranch.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Branch created successfully!");
            return "redirect:/banker/branches";
        } catch (Exception e) {
            logger.error("Error creating branch: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Failed to create branch: " + e.getMessage());
            return "banker/create-branch";
        }
    }

    /**
     * Displays details for a specific branch
     * 
     * @param id     The ID of the branch to display
     * @param model  The model to add attributes to
     * @return The view name for branch details
     */
    @GetMapping("/branches/{id}")
    public String viewBranchDetails(@PathVariable Long id, Model model) {
        try {
        Branch branch = branchService.getBranchById(id);
        model.addAttribute("branch", branch);
        
            // Add success message if available in flash attributes
            if (model.containsAttribute("successMessage")) {
                model.addAttribute("successMessage", model.asMap().get("successMessage"));
            }
        
        return "banker/branch-details";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "banker/branch-details";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while loading branch details: " + e.getMessage());
            return "banker/branch-details";
        }
    }

    /**
     * Managers management page
     */
    @GetMapping("/managers")
    @PreAuthorize("hasRole('BANKER')")
    public String managers(Model model) {
        try {
            List<Manager> managers = managerService.getAllManagers();
            model.addAttribute("managers", managers);
            return "banker/managers";
        } catch (Exception e) {
            logger.error("Error loading managers page: {}", e.getMessage());
            return "error";
        }
    }

    /**
     * Add manager page
     */
    @GetMapping("/managers/create")
    @PreAuthorize("hasRole('BANKER')")
    public String createManagerForm(Model model) {
        try {
            // Get branches without managers
            List<Branch> availableBranches = branchService.getBranchesWithoutManager();
            
            // Add the manager request object and available branches to the model
        model.addAttribute("managerRequest", new ManagerCreationRequest());
            model.addAttribute("availableBranches", availableBranches);
            
            return "banker/create-manager";
        } catch (Exception e) {
            logger.error("Error loading manager creation form: {}", e.getMessage());
            return "error";
        }
    }

    /**
     * Handle manager addition form submission
     */
    @PostMapping("/managers/create")
    @PreAuthorize("hasRole('BANKER')")
    public String createManager(@Valid @ModelAttribute("managerRequest") ManagerCreationRequest managerRequest,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            try {
                List<Branch> availableBranches = branchService.getBranchesWithoutManager();
                model.addAttribute("availableBranches", availableBranches);
            } catch (Exception e) {
                logger.error("Error loading branches for manager form: {}", e.getMessage());
            }
            return "banker/create-manager";
        }
        
        try {
            // Check if branch exists
            Branch branch = branchService.getBranchById(managerRequest.getBranchId());
            
            // Check if branch already has a manager
            if (branch.getManager() != null) {
                model.addAttribute("errorMessage", "Branch already has a manager assigned");
                List<Branch> availableBranches = branchService.getBranchesWithoutManager();
                model.addAttribute("availableBranches", availableBranches);
                return "banker/create-manager";
            }
            
            // Create manager using the managerService
            Manager manager = managerService.createManager(managerRequest);
            
            // Assign manager to branch if needed and if assignment successful
            if (manager != null) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Manager " + manager.getFirstName() + " " + manager.getLastName() + " created successfully");
            }
            
            return "redirect:/banker/managers";
        } catch (Exception e) {
            logger.error("Error creating manager: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error creating manager: " + e.getMessage());
            try {
                List<Branch> availableBranches = branchService.getBranchesWithoutManager();
                model.addAttribute("availableBranches", availableBranches);
            } catch (Exception ex) {
                logger.error("Error loading branches for manager form: {}", ex.getMessage());
            }
        return "banker/create-manager";
        }
    }

    /**
     * Manager details page
     */
    @GetMapping("/managers/{id}")
    @PreAuthorize("hasRole('BANKER')")
    public String managerDetails(@PathVariable Long id, Model model) {
        try {
            Manager manager = managerService.getManagerById(id);
            model.addAttribute("manager", manager);
            return "banker/manager-details";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Manager not found");
            return "redirect:/banker/managers";
        }
    }

    /**
     * Users management page
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('BANKER')")
    public String users(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "banker/users";
    }

    /**
     * User details page
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('BANKER')")
    public String userDetails(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        
        List<Account> accounts = accountService.getAccountsByUser(user);
        model.addAttribute("accounts", accounts);
        
        // Add all branches for account creation
        List<Branch> branches = branchService.getAllActiveBranches();
        model.addAttribute("branches", branches);
        
        return "banker/user-details";
    }

    /**
     * Reports page
     */
    @GetMapping("/reports")
    @PreAuthorize("hasRole('BANKER')")
    public String reports(Model model) {
        return "banker/reports";
    }

    /**
     * Generate report page
     */
    @GetMapping("/reports/generate")
    @PreAuthorize("hasRole('BANKER')")
    public String generateReport(Model model) {
        List<Branch> branches = branchService.getAllActiveBranches();
        model.addAttribute("branches", branches);
        return "banker/generate-report";
    }

    /**
     * Edit branch page
     */
    @GetMapping("/branches/{id}/edit")
    public String editBranchForm(@PathVariable Long id, Model model) {
        try {
            Branch branch = branchService.getBranchById(id);
            
            // Create branch request from existing branch
            BranchCreationRequest branchRequest = new BranchCreationRequest();
            branchRequest.setName(branch.getName());
            branchRequest.setBranchCode(branch.getBranchCode());
            branchRequest.setAddress(branch.getAddress());
            branchRequest.setCity(branch.getCity());
            branchRequest.setState(branch.getState());
            branchRequest.setZipCode(branch.getZipCode());
            branchRequest.setPhoneNumber(branch.getPhoneNumber());
            
            model.addAttribute("branchId", id);
            model.addAttribute("branchRequest", branchRequest);
            model.addAttribute("branch", branch);
            
            return "banker/edit-branch";
        } catch (Exception e) {
            logger.error("Error loading branch for edit: {}", e.getMessage());
            return "redirect:/banker/branches";
        }
    }

    /**
     * Handle branch update form submission
     */
    @PostMapping("/branches/{id}")
    public String updateBranch(@PathVariable Long id, 
                              @Valid @ModelAttribute("branchRequest") BranchCreationRequest branchRequest,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("branchId", id);
            model.addAttribute("branch", branchService.getBranchById(id));
            return "banker/edit-branch";
        }
        
        try {
            // Get the existing branch to compare branch code
            Branch existingBranch = branchService.getBranchById(id);
            if (!existingBranch.getBranchCode().equals(branchRequest.getBranchCode())) {
                model.addAttribute("branchId", id);
                model.addAttribute("branch", existingBranch);
                model.addAttribute("errorMessage", "Branch code cannot be modified");
                return "banker/edit-branch";
            }
            
            branchService.updateBranch(id, branchRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Branch updated successfully");
            return "redirect:/banker/branches?success";
        } catch (Exception e) {
            logger.error("Error updating branch: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating branch: " + e.getMessage());
            return "redirect:/banker/branches/" + id + "/edit?error";
        }
    }

    /**
     * Transaction reports page
     */
    @GetMapping("/reports/transactions")
    @PreAuthorize("hasRole('BANKER')")
    public String transactionReports(Model model) {
        List<Branch> branches = branchService.getAllActiveBranches();
        model.addAttribute("branches", branches);
        return "banker/transaction-reports";
    }

    /**
     * Account reports page
     */
    @GetMapping("/reports/accounts")
    @PreAuthorize("hasRole('BANKER')")
    public String accountReports(Model model) {
        List<Branch> branches = branchService.getAllActiveBranches();
        model.addAttribute("branches", branches);
        return "banker/account-reports";
    }

    /**
     * Loan reports page
     */
    @GetMapping("/reports/loans")
    @PreAuthorize("hasRole('BANKER')")
    public String loanReports(Model model) {
        List<Branch> branches = branchService.getAllActiveBranches();
        model.addAttribute("branches", branches);
        return "banker/loan-reports";
    }

    /**
     * Branch reports page
     */
    @GetMapping("/reports/branches")
    @PreAuthorize("hasRole('BANKER')")
    public String branchReports(Model model) {
        return "banker/branch-reports";
    }

    /**
     * User reports page
     */
    @GetMapping("/reports/users")
    @PreAuthorize("hasRole('BANKER')")
    public String userReports(Model model) {
        return "banker/user-reports";
    }

    /**
     * Summary reports page
     */
    @GetMapping("/reports/summary")
    @PreAuthorize("hasRole('BANKER')")
    public String summaryReports(Model model) {
        return "banker/summary-reports";
    }

    /**
     * Settings page
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('BANKER')")
    public String settings(Model model) {
        return "banker/settings";
    }

    /**
     * Profile page
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('BANKER')")
    public String profile(Model model) {
        return "banker/profile";
    }

    /**
     * Deactivate branch
     */
    @GetMapping("/branches/deactivate/{id}")
    public String deactivateBranch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            branchService.deactivateBranch(id);
            redirectAttributes.addFlashAttribute("successMessage", "Branch deactivated successfully");
            return "redirect:/banker/branches/" + id;
        } catch (Exception e) {
            logger.error("Error deactivating branch: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deactivating branch: " + e.getMessage());
            return "redirect:/banker/branches/" + id;
        }
    }

    /**
     * Activate branch
     */
    @GetMapping("/branches/activate/{id}")
    public String activateBranch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            branchService.activateBranch(id);
            redirectAttributes.addFlashAttribute("successMessage", "Branch activated successfully");
            return "redirect:/banker/branches/" + id;
        } catch (Exception e) {
            logger.error("Error activating branch: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error activating branch: " + e.getMessage());
            return "redirect:/banker/branches/" + id;
        }
    }

    /**
     * Delete branch
     */
    @GetMapping("/branches/delete/{id}")
    public String deleteBranch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            branchService.deleteBranch(id);
            redirectAttributes.addFlashAttribute("successMessage", "Branch deleted successfully");
            return "redirect:/banker/branches";
        } catch (Exception e) {
            logger.error("Error deleting branch: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting branch: " + e.getMessage());
            return "redirect:/banker/branches";
        }
    }
}
