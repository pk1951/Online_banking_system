package com.truebank.controller;

import com.truebank.dto.LoanDecisionRequest;
import com.truebank.dto.MessageResponse;
import com.truebank.model.*;
import com.truebank.service.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final UserService userService;
    private final BranchService branchService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;
    private final EmailService emailService;

    /**
     * Get manager's branch
     */
    @GetMapping("/branch")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getManagerBranch() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User manager = userService.getUserByUsername(username);
            
            Branch branch = branchService.getBranchByManager(manager);
            
            return ResponseEntity.ok(branch);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all accounts in manager's branch
     */
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getBranchAccounts() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User manager = userService.getUserByUsername(username);
            
            Branch branch = branchService.getBranchByManager(manager);
            List<Account> accounts = accountService.getAccountsByBranch(branch);
            
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all transactions in manager's branch
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getBranchTransactions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User manager = userService.getUserByUsername(username);
            
            Branch branch = branchService.getBranchByManager(manager);
            List<Transaction> transactions = transactionService.getTransactionsByBranchId(branch.getId());
            
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all pending loans in manager's branch
     */
    @GetMapping("/loans/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getPendingLoans() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User manager = userService.getUserByUsername(username);
            
            Branch branch = branchService.getBranchByManager(manager);
            List<Loan> pendingLoans = loanService.getPendingLoansByBranch(branch);
            
            return ResponseEntity.ok(pendingLoans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all loans in manager's branch
     */
    @GetMapping("/loans")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getAllLoans() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User manager = userService.getUserByUsername(username);
            
            Branch branch = branchService.getBranchByManager(manager);
            List<Loan> loans = loanService.getLoansByBranch(branch);
            
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Approve or reject a loan
     */
    @PostMapping("/loans/{loanId}/decision")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> decideLoan(
            @PathVariable Long loanId, 
            @Valid @RequestBody LoanDecisionRequest loanDecisionRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User manager = userService.getUserByUsername(username);
            
            // Get the loan
            Loan loan = loanService.getLoanById(loanId);
            
            // Check if loan belongs to manager's branch
            Branch branch = branchService.getBranchByManager(manager);
            if (!loan.getBranch().getId().equals(branch.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Loan does not belong to manager's branch!"));
            }
            
            // Process loan decision
            if (loanDecisionRequest.isApproved()) {
                loan = loanService.approveLoan(loanId, manager, loanDecisionRequest.getRemarks());
                
                // Send approval email to user
                emailService.sendLoanApprovalEmail(
                    loan.getUser().getEmail(),
                    loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                    loan.getLoanType().toString(),
                    loan.getAmount().toString(),
                    loan.getInterestRate().toString()
                );
            } else {
                loan = loanService.rejectLoan(loanId, manager, loanDecisionRequest.getRemarks());
                
                // Send rejection email to user
                emailService.sendLoanRejectionEmail(
                    loan.getUser().getEmail(),
                    loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                    loan.getLoanType().toString(),
                    loan.getAmount().toString(),
                    loanDecisionRequest.getRemarks()
                );
            }
            
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Disburse an approved loan
     */
    @PostMapping("/loans/{loanId}/disburse")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> disburseLoan(@PathVariable Long loanId, @RequestParam String accountNumber) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User manager = userService.getUserByUsername(username);
            
            // Get the loan
            Loan loan = loanService.getLoanById(loanId);
            
            // Check if loan belongs to manager's branch
            Branch branch = branchService.getBranchByManager(manager);
            if (!loan.getBranch().getId().equals(branch.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Loan does not belong to manager's branch!"));
            }
            
            // Check if loan is approved
            if (loan.getLoanStatus() != LoanStatus.APPROVED) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Loan is not in APPROVED status!"));
            }
            
            // Check if account exists and belongs to the loan applicant
            Account account = accountService.getAccountByNumber(accountNumber);
            if (!account.getUser().getId().equals(loan.getUser().getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to loan applicant!"));
            }
            
            // Disburse the loan
            loan = loanService.disburseLoan(loanId, accountNumber);
            
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get user details for a specific account
     */
    @GetMapping("/accounts/{accountNumber}/user")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getAccountUser(@PathVariable String accountNumber) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User manager = userService.getUserByUsername(username);
            
            // Get the account
            Account account = accountService.getAccountByNumber(accountNumber);
            
            // Check if account belongs to manager's branch
            Branch branch = branchService.getBranchByManager(manager);
            if (!account.getBranch().getId().equals(branch.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to manager's branch!"));
            }
            
            return ResponseEntity.ok(account.getUser());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get transactions for a specific account
     */
    @GetMapping("/accounts/{accountNumber}/transactions")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getAccountTransactions(@PathVariable String accountNumber) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User manager = userService.getUserByUsername(username);
            
            // Get the account
            Account account = accountService.getAccountByNumber(accountNumber);
            
            // Check if account belongs to manager's branch
            Branch branch = branchService.getBranchByManager(manager);
            if (!account.getBranch().getId().equals(branch.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to manager's branch!"));
            }
            
            List<Transaction> transactions = transactionService.getTransactionsByAccount(account);
            
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
