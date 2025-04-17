package com.truebank.controller.api;

import com.truebank.dto.AccountCreationRequest;
import com.truebank.dto.ApiResponse;
import com.truebank.model.Account;
import com.truebank.model.AccountType;
import com.truebank.model.User;
import com.truebank.service.AccountService;
import com.truebank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * REST controller for managing user accounts via the new API
 */
@RestController
@RequestMapping("/api/v1/user/accounts")
public class UserAccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    /**
     * Create a new account for the authenticated user
     * 
     * @param request Account creation request
     * @param principal Authenticated user
     * @return ResponseEntity with created account
     */
    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountCreationRequest request, Principal principal) {
        try {
            User user = userService.getUserByUsername(principal.getName());
            
            // Create new account
            Account account = accountService.createAccount(
                user, 
                request.getBranchCode(), 
                AccountType.valueOf(request.getAccountType())
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", account));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error creating account: " + e.getMessage()));
        }
    }

    /**
     * Get all accounts for the authenticated user
     * 
     * @param principal Authenticated user
     * @return ResponseEntity with list of accounts
     */
    @GetMapping
    public ResponseEntity<?> getUserAccounts(Principal principal) {
        try {
            User user = userService.getUserByUsername(principal.getName());
            List<Account> accounts = accountService.getAccountsByUser(user);
            
            return ResponseEntity.ok(ApiResponse.success("User accounts retrieved successfully", accounts));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error retrieving accounts: " + e.getMessage()));
        }
    }

    /**
     * Get details of a specific account by account number
     * 
     * @param accountNumber Account number
     * @param principal Authenticated user
     * @return ResponseEntity with account details
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccountDetails(@PathVariable String accountNumber, Principal principal) {
        try {
            User user = userService.getUserByUsername(principal.getName());
            Account account = accountService.getAccountByNumber(accountNumber);
            
            // Check if account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Account does not belong to authenticated user"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("Account details retrieved successfully", account));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error retrieving account details: " + e.getMessage()));
        }
    }
} 