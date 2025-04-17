package com.truebank.controller;

import com.truebank.dto.*;
import com.truebank.model.*;
import com.truebank.service.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Random;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/banker")
@RequiredArgsConstructor
@Slf4j
public class BankerController {

    private static final Logger log = LoggerFactory.getLogger(BankerController.class);

    private final UserService userService;
    private final BranchService branchService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;
    private final EmailService emailService;
    private final SupabaseAuthService supabaseAuthService;

    /**
     * Get all branches
     */
    @GetMapping("/branches")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> getAllBranches() {
        try {
            List<Branch> branches = branchService.getAllBranches();
            return ResponseEntity.ok(branches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Create a new branch
     */
    @PostMapping("/branches")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> createBranch(@Valid @RequestBody BranchCreationRequest branchRequest) {
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
            
            return ResponseEntity.ok(createdBranch);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Update branch details
     */
    @PutMapping("/branches/{id}")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> updateBranch(@PathVariable Long id, 
                               @Valid @RequestBody BranchCreationRequest branchRequest) {
        try {
            Branch updatedBranch = branchService.updateBranch(id, branchRequest);
            return ResponseEntity.ok(new MessageResponse("Branch updated successfully"));
        } catch (Exception e) {
            log.error("Error updating branch: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Deactivate a branch
     */
    @PutMapping("/branches/{id}/deactivate")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> deactivateBranch(@PathVariable Long id) {
        try {
            branchService.deactivateBranch(id);
            return ResponseEntity.ok(new MessageResponse("Branch deactivated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Activate a branch
     */
    @PutMapping("/branches/{id}/activate")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> activateBranch(@PathVariable Long id) {
        try {
            branchService.activateBranch(id);
            return ResponseEntity.ok(new MessageResponse("Branch activated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Create a manager account and assign to branch
     */
    @PostMapping("/managers")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> createManager(@Valid @RequestBody ManagerCreationRequest managerRequest) {
        try {
            // Check if branch exists
            Branch branch = branchService.getBranchById(managerRequest.getBranchId());
            
            // Check if branch already has a manager
            if (branch.getManager() != null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Branch already has a manager!"));
            }
            
            // Create user object for manager
            User manager = new User();
            manager.setFirstName(managerRequest.getFirstName());
            manager.setLastName(managerRequest.getLastName());
            manager.setEmail(managerRequest.getEmail());
            manager.setPhoneNumber(managerRequest.getPhoneNumber());
            manager.setAadharNumber(managerRequest.getAadharNumber());
            manager.setAddress(managerRequest.getAddress());
            manager.setAge(managerRequest.getAge());
            
            // Generate username and password
            String username = managerRequest.getFirstName().toLowerCase() + branch.getBranchCode();
            String password = "manager@123";
            
            // Check if username already exists
            if (!userService.isUsernameAvailable(username)) {
                // Add random number to username
                Random random = new Random();
                username = username + random.nextInt(100);
            }
            
            manager.setUsername(username);
            
            // Create manager account with MANAGER role
            User createdManager = supabaseAuthService.createManager(manager, password);
            
            // Assign manager to branch
            branch = branchService.assignManager(branch.getId(), createdManager);
            
            // Send credentials email to manager
            emailService.sendManagerCredentialsEmail(
                createdManager.getEmail(),
                username,
                password,
                branch.getName()
            );
            
            return ResponseEntity.ok(new MessageResponse("Manager created and assigned to branch successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Deactivate a user
     */
    @PutMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok(new MessageResponse("User deactivated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Activate a user
     */
    @PutMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok(new MessageResponse("User activated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all accounts
     */
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> getAllAccounts() {
        try {
            List<Account> accounts = accountService.getAllAccounts();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Deactivate an account
     */
    @PutMapping("/accounts/{id}/deactivate")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> deactivateAccount(@PathVariable Long id) {
        try {
            accountService.deactivateAccount(id);
            return ResponseEntity.ok(new MessageResponse("Account deactivated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Activate an account
     */
    @PutMapping("/accounts/{id}/activate")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> activateAccount(@PathVariable Long id) {
        try {
            accountService.activateAccount(id);
            return ResponseEntity.ok(new MessageResponse("Account activated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all transactions
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> getAllTransactions() {
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all loans
     */
    @GetMapping("/loans")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> getAllLoans() {
        try {
            List<Loan> loans = loanService.getAllLoans();
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get branch statistics
     */
    @GetMapping("/branches/{id}/stats")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> getBranchStatistics(@PathVariable Long id) {
        try {
            Branch branch = branchService.getBranchById(id);
            List<Account> accounts = accountService.getAccountsByBranch(branch);
            List<Transaction> transactions = transactionService.getTransactionsByBranchId(id);
            List<Loan> loans = loanService.getLoansByBranch(branch);
            
            BranchStatisticsResponse stats = new BranchStatisticsResponse();
            stats.setBranchId(branch.getId());
            stats.setBranchName(branch.getName());
            stats.setBranchCode(branch.getBranchCode());
            stats.setTotalAccounts(accounts.size());
            stats.setTotalTransactions(transactions.size());
            stats.setTotalLoans(loans.size());
            
            // Calculate more statistics as needed
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Create a new account for a user
     */
    @PostMapping("/accounts/create")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> createAccount(@RequestBody AccountCreationRequest request) {
        try {
            // Validate request
            if (request.getUserId() == null || request.getBranchId() == null || 
                request.getAccountType() == null || request.getInitialDeposit() == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "All fields are required", null));
            }
            
            // Get user and branch
            User user = userService.getUserById(request.getUserId());
            Branch branch = branchService.getBranchById(request.getBranchId());
            
            // Create account
            Account account = new Account();
            account.setUser(user);
            account.setBranch(branch);
            account.setAccountType(AccountType.valueOf(request.getAccountType()));
            account.setBalance(new BigDecimal(request.getInitialDeposit()));
            account.setActive(true);
            
            Account createdAccount = accountService.saveAccount(account);
            
            return ResponseEntity.ok(new ApiResponse(true, "Account created successfully", createdAccount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    /**
     * Reset a user's password
     */
    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        try {
            // In a real application, this would generate a random password
            // and send it to the user's email
            return ResponseEntity.ok(new ApiResponse(true, "Password reset email sent", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
