package com.truebank.controller;

import com.truebank.dto.AccountCreationRequest;
import com.truebank.dto.LoanApplicationRequest;
import com.truebank.dto.LoanRepaymentRequest;
import com.truebank.dto.LoanRepaymentResponse;
import com.truebank.dto.MessageResponse;
import com.truebank.dto.ProfileUpdateRequest;
import com.truebank.dto.TransactionRequest;
import com.truebank.model.*;
import com.truebank.service.AccountService;
import com.truebank.service.LoanService;
import com.truebank.service.TransactionService;
import com.truebank.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Update user details
            user.setFirstName(profileUpdateRequest.getFirstName());
            user.setLastName(profileUpdateRequest.getLastName());
            user.setAge(profileUpdateRequest.getAge());
            user.setAddress(profileUpdateRequest.getAddress());
            
            // Update phone number if changed and available
            if (!user.getPhoneNumber().equals(profileUpdateRequest.getPhoneNumber())) {
                if (!userService.isPhoneNumberAvailable(profileUpdateRequest.getPhoneNumber())) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Error: Phone number is already in use!"));
                }
                user.setPhoneNumber(profileUpdateRequest.getPhoneNumber());
            }
            
            User updatedUser = userService.updateUser(user);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get user accounts
     */
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserAccounts() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            List<Account> accounts = accountService.getAccountsByUser(user);
            
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Create a new account
     */
    @PostMapping("/accounts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountCreationRequest accountCreationRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Create account
            Account account = accountService.createAccount(
                user, 
                accountCreationRequest.getBranchCode(), 
                AccountType.valueOf(accountCreationRequest.getAccountType())
            );
            
            // Handle initial deposit if provided
            if (accountCreationRequest.getInitialDeposit() != null && !accountCreationRequest.getInitialDeposit().isEmpty()) {
                try {
                    BigDecimal amount = new BigDecimal(accountCreationRequest.getInitialDeposit());
                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        // Create deposit transaction
                        transactionService.createDepositTransaction(
                            account.getAccountNumber(),
                            amount,
                            "Initial deposit"
                        );
                    }
                } catch (NumberFormatException e) {
                    logger.error("Failed to parse initial deposit amount: {}", e.getMessage());
                    // Continue without creating initial deposit transaction
                }
            }
            
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get account transactions
     */
    @GetMapping("/accounts/{accountNumber}/transactions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAccountTransactions(@PathVariable String accountNumber) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            Account account = accountService.getAccountByNumber(accountNumber);
            
            // Check if account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to user!"));
            }
            
            List<Transaction> transactions = transactionService.getTransactionsByAccount(account);
            
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Deposit money
     */
    @PostMapping("/accounts/{accountNumber}/deposit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deposit(@PathVariable String accountNumber, @Valid @RequestBody TransactionRequest transactionRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            Account account = accountService.getAccountByNumber(accountNumber);
            
            // Check if account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to user!"));
            }
            
            Transaction transaction = transactionService.createDepositTransaction(
                accountNumber, 
                transactionRequest.getAmount(), 
                transactionRequest.getDescription()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("transaction", transaction);
            response.put("newBalance", account.getBalance());
            response.put("message", "Deposit successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Withdraw money
     */
    @PostMapping("/accounts/{accountNumber}/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> withdraw(@PathVariable String accountNumber, @Valid @RequestBody TransactionRequest transactionRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            Account account = accountService.getAccountByNumber(accountNumber);
            
            // Check if account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to user!"));
            }
            
            Transaction transaction = transactionService.createWithdrawalTransaction(
                accountNumber, 
                transactionRequest.getAmount(), 
                transactionRequest.getDescription()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("transaction", transaction);
            response.put("newBalance", account.getBalance());
            response.put("message", "Withdrawal successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Transfer money
     */
    @PostMapping("/accounts/{accountNumber}/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> transfer(
            @PathVariable String accountNumber, 
            @Valid @RequestBody TransactionRequest transactionRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            Account account = accountService.getAccountByNumber(accountNumber);
            
            // Check if account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to user!"));
            }
            
            // Check if destination account exists
            accountService.getAccountByNumber(transactionRequest.getDestinationAccountNumber());
            
            Transaction transaction = transactionService.createTransferTransaction(
                accountNumber, 
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
     * Apply for a loan
     */
    @PostMapping("/loans/apply-internal")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> applyForLoan(@Valid @RequestBody LoanApplicationRequest loanApplicationRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Get account to verify branch
            Account account = accountService.getAccountById(loanApplicationRequest.getAccountId());
            
            // Check if account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to user!"));
            }
            
            Loan loan = loanService.applyForLoan(
                user, 
                account.getBranch(), 
                LoanType.valueOf(loanApplicationRequest.getLoanType()), 
                loanApplicationRequest.getAmount(), 
                loanApplicationRequest.getTenure(), 
                700 // Default CIBIL score if not provided
            );
            
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get user loans
     */
    @GetMapping("/loans")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserLoans() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            List<Loan> loans = loanService.getLoansByUser(user);
            
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get specific loan by ID
     */
    @GetMapping("/loans/{loanId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getLoanById(@PathVariable Long loanId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Get the loan
            Loan loan = loanService.getLoanById(loanId);
            
            // Check if loan belongs to user
            if (!loan.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Loan does not belong to user!"));
            }
            
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Make loan repayment
     */
    @PostMapping("/loans/{loanId}/repay")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> repayLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanRepaymentRequest repaymentRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Get the loan
            Loan loan = loanService.getLoanById(loanId);
            
            // Check if loan belongs to user
            if (!loan.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Loan does not belong to user!"));
            }
            
            // Check if loan is in DISBURSED status
            if (loan.getLoanStatus() != LoanStatus.DISBURSED) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Loan is not in DISBURSED status!"));
            }
            
            // Get account to verify ownership
            Account account = accountService.getAccountByNumber(repaymentRequest.getAccountNumber());
            
            // Check if account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Account does not belong to user!"));
            }
            
            // Make loan repayment
            LoanRepaymentResponse response = loanService.makeLoanRepayment(
                loanId, 
                repaymentRequest.getAccountNumber(), 
                repaymentRequest.getAmount()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Handle form submission for fund transfer
     */
    @PostMapping("/api/transactions/transfer")
    @PreAuthorize("hasRole('USER')")
    public String processTransferForm(@Valid @ModelAttribute("transferRequest") TransactionRequest transferRequest,
                                     BindingResult result,
                                     Model model) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            // Basic validation
            if (result.hasErrors()) {
                prepareTransferModel(model, user);
                return "user/transfer";
            }
            
            // Verify source account belongs to user
            Account sourceAccount = accountService.getAccountByNumber(transferRequest.getSourceAccountNumber());
            if (!sourceAccount.getUser().getId().equals(user.getId())) {
                result.rejectValue("sourceAccountNumber", "error.transferRequest", "Source account does not belong to you");
                prepareTransferModel(model, user);
                return "user/transfer";
            }
            
            // Process transfer
            Transaction transaction = transactionService.createTransferTransaction(
                transferRequest.getSourceAccountNumber(),
                transferRequest.getDestinationAccountNumber(),
                transferRequest.getAmount(),
                transferRequest.getDescription()
            );
            
            // Add success message directly to the model
            model.addAttribute("successMessage", 
                "Transfer of ₹" + transferRequest.getAmount() + " to account " + 
                transferRequest.getDestinationAccountNumber() + " was successful. Reference: " + 
                transaction.getReferenceNumber());
            
            // Add transaction details to the model
            model.addAttribute("transaction", transaction);
            model.addAttribute("newBalance", sourceAccount.getBalance());
            
            // Reload accounts for display
            prepareTransferModel(model, user);
            
            // Return to the same page with success message
            return "user/transfer";
        } catch (Exception e) {
            result.reject("error.global", "Error: " + e.getMessage());
            User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
            prepareTransferModel(model, user);
            return "user/transfer";
        }
    }
    
    private void prepareTransferModel(Model model, User user) {
        List<Account> accounts = accountService.getAccountsByUser(user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("hasAccounts", !accounts.isEmpty());
        if (!model.containsAttribute("transferRequest")) {
            model.addAttribute("transferRequest", new TransactionRequest());
        }
    }

    /**
     * Global exception handler for this controller
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleException(Exception e) {
        if (e instanceof ResponseStatusException) {
            return ResponseEntity
                .status(((ResponseStatusException) e).getStatusCode())
                .body(new MessageResponse("Error: " + e.getMessage()));
        }
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new MessageResponse("Error: " + e.getMessage()));
    }
    
    /**
     * Handle entity not found exceptions
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<MessageResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new MessageResponse("Error: " + e.getMessage()));
    }
}
