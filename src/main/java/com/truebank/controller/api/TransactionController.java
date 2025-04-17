package com.truebank.controller.api;

import com.truebank.dto.MessageResponse;
import com.truebank.dto.TransactionRequest;
import com.truebank.model.Account;
import com.truebank.model.Transaction;
import com.truebank.model.User;
import com.truebank.service.AccountService;
import com.truebank.service.TransactionService;
import com.truebank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final AccountService accountService;

    /**
     * Get transaction by reference number
     */
    @GetMapping("/{referenceNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getTransactionByReferenceNumber(@PathVariable String referenceNumber) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Get transaction
            Transaction transaction = transactionService.getTransactionByReferenceNumber(referenceNumber);
            
            // Security check - ensure the transaction is related to one of user's accounts
            List<Account> userAccounts = accountService.getAccountsByUser(user);
            boolean authorized = userAccounts.stream().anyMatch(account -> 
                (transaction.getSourceAccount() != null && transaction.getSourceAccount().getId().equals(account.getId())) ||
                (transaction.getDestinationAccount() != null && transaction.getDestinationAccount().getId().equals(account.getId()))
            );
            
            if (!authorized) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not authorized to view this transaction"));
            }
            
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all transactions for authenticated user
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserTransactions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            List<Transaction> transactions = transactionService.getTransactionsByUser(user);
            
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Deposit money
     */
    @PostMapping("/deposit/{accountNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransactionRequest transactionRequest) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Verify account belongs to user
            Account account = accountService.getAccountByNumber(accountNumber);
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to you"));
            }
            
            // Process deposit
            Transaction transaction = transactionService.createDepositTransaction(
                accountNumber, 
                transactionRequest.getAmount(), 
                transactionRequest.getDescription()
            );
            
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Withdraw money
     */
    @PostMapping("/withdraw/{accountNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransactionRequest transactionRequest) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Verify account belongs to user
            Account account = accountService.getAccountByNumber(accountNumber);
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to you"));
            }
            
            // Process withdrawal
            Transaction transaction = transactionService.createWithdrawalTransaction(
                accountNumber, 
                transactionRequest.getAmount(), 
                transactionRequest.getDescription()
            );
            
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Transfer money (form submission endpoint)
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> transferForm(@Valid @ModelAttribute TransactionRequest transactionRequest) {
        try {
            if (transactionRequest.getSourceAccountNumber() == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Source account is required"));
            }
            
            if (transactionRequest.getDestinationAccountNumber() == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Destination account is required"));
            }
            
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Verify source account belongs to user
            Account sourceAccount = accountService.getAccountByNumber(transactionRequest.getSourceAccountNumber());
            if (!sourceAccount.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Source account does not belong to you"));
            }
            
            // Process transfer
            Transaction transaction = transactionService.createTransferTransaction(
                transactionRequest.getSourceAccountNumber(),
                transactionRequest.getDestinationAccountNumber(),
                transactionRequest.getAmount(),
                transactionRequest.getDescription()
            );
            
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get all transactions for an account
     */
    @GetMapping("/account/{accountNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAccountTransactions(@PathVariable String accountNumber) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Verify account belongs to user
            Account account = accountService.getAccountByNumber(accountNumber);
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to you"));
            }
            
            List<Transaction> transactions = transactionService.getTransactionsByAccount(account);
            
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
} 