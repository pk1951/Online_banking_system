package com.truebank.controller;

import com.truebank.model.*;
import com.truebank.service.*;
import com.truebank.dto.*;
import com.truebank.security.services.UserDetailsImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserViewController {

    private static final Logger logger = LoggerFactory.getLogger(UserViewController.class);
    
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;

    /**
     * Helper method to get the authenticated user
     */
    private User getAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
                logger.error("Authentication error: Principal is not UserDetailsImpl");
                throw new RuntimeException("Authentication error: Invalid principal type");
            }
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userService.getUserById(userDetails.getId());
        } catch (Exception e) {
            logger.error("Error getting authenticated user: {}", e.getMessage());
            throw new RuntimeException("Failed to get authenticated user", e);
        }
    }

    /**
     * User dashboard page
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String dashboard(Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            // Get user accounts
            List<Account> accounts = accountService.getAccountsByUser(user);
            model.addAttribute("accounts", accounts);
            model.addAttribute("hasAccounts", !accounts.isEmpty());
            
            // Get recent transactions
            List<Transaction> recentTransactions = transactionService.getRecentTransactionsByUser(user, 5);
            model.addAttribute("recentTransactions", recentTransactions);
            
            // Calculate CIBIL score - this is a simplified example
            // In a real application, this would involve complex calculations based on credit history
            int cibilScore = calculateUserCibilScore(user);
            model.addAttribute("cibilScore", cibilScore);
            
            return "user/dashboard";
        } catch (Exception e) {
            // Log the full error with stack trace
            logger.error("Error loading dashboard: {}", e.getMessage(), e);
            
            // Add error message and empty data to prevent template errors
            model.addAttribute("errorMessage", "Failed to load dashboard data. Please try again later.");
            model.addAttribute("user", new User());
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("hasAccounts", false);
            model.addAttribute("recentTransactions", new ArrayList<>());
            model.addAttribute("cibilScore", 0);
            
            return "user/dashboard";
        }
    }

    /**
     * Calculate a user's CIBIL score based on various factors
     * This is a simplified example - real CIBIL scores involve complex algorithms
     */
    private int calculateUserCibilScore(User user) {
        // Default base score
        int baseScore = 650;
        
        try {
            // Get recent transactions to calculate financial activity
            List<Transaction> transactions = transactionService.getTransactionsByUser(user);
            
            // Get user loans
            List<Loan> loans = loanService.getLoansByUser(user);
            
            // If user has active loans
            if (!loans.isEmpty()) {
                // Check for existing CIBIL score in one of the loans
                for (Loan loan : loans) {
                    if (loan.getCibilScore() != null) {
                        return loan.getCibilScore(); // Return existing CIBIL score if available
                    }
                }
            }
            
            // Get user accounts
            List<Account> accounts = accountService.getAccountsByUser(user);
            
            // Factors that can increase CIBIL score:
            // 1. Account balance (higher balances = better score)
            int balanceScore = 0;
            for (Account account : accounts) {
                if (account.getBalance().compareTo(new BigDecimal("10000")) > 0) {
                    balanceScore += 20;
                } else if (account.getBalance().compareTo(new BigDecimal("5000")) > 0) {
                    balanceScore += 10;
                }
            }
            // Cap the balance score
            balanceScore = Math.min(balanceScore, 50);
            
            // 2. Account age (older accounts = better score)
            int accountAgeScore = 0;
            for (Account account : accounts) {
                LocalDateTime createdAt = account.getCreatedAt();
                if (createdAt != null) {
                    long ageInDays = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
                    accountAgeScore += Math.min(ageInDays / 30, 50); // 30 days = 1 month, max 50 points
                }
            }
            accountAgeScore = Math.min(accountAgeScore, 50);
            
            // 3. Transaction history (more transactions = better score)
            int transactionScore = Math.min(transactions.size(), 50);
            
            // Calculate final score (base + factors)
            int finalScore = baseScore + balanceScore + accountAgeScore + transactionScore;
            
            // Ensure score is between 300 and 900 (typical CIBIL score range)
            return Math.max(300, Math.min(finalScore, 900));
            
        } catch (Exception e) {
            logger.error("Error calculating CIBIL score: {}", e.getMessage(), e);
            return baseScore; // Return base score if calculation fails
        }
    }

    /**
     * User accounts page
     */
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('USER')")
    public String accounts(Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            // Get user accounts
            List<Account> accounts = accountService.getAccountsByUser(user);
            model.addAttribute("accounts", accounts);
            model.addAttribute("hasAccounts", !accounts.isEmpty());
            
            return "user/accounts";
        } catch (Exception e) {
            logger.error("Error loading accounts: {}", e.getMessage(), e);
            
            // Add error message and empty data to prevent template errors
            model.addAttribute("errorMessage", "Failed to load accounts data. Please try again later.");
            model.addAttribute("user", new User());
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("hasAccounts", false);
            
            return "user/accounts";
        }
    }

    /**
     * Create new account page
     */
    @GetMapping("/accounts/new")
    @PreAuthorize("hasRole('USER')")
    public String createAccountForm(Model model) {
        try {
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            model.addAttribute("accountRequest", new AccountCreationRequest());
            return "user/create-account";
        } catch (Exception e) {
            logger.error("Error loading create account form: {}", e.getMessage(), e);
            return "redirect:/user/accounts?error=Failed to load account creation form";
        }
    }

    /**
     * Account details page
     */
    @GetMapping("/accounts/{id}")
    @PreAuthorize("hasRole('USER')")
    public String accountDetails(@PathVariable Long id, Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            // Get account details
            Account account = accountService.getAccountById(id);
            
            // Security check - ensure the account belongs to the current user
            if (!account.getUser().getId().equals(user.getId())) {
                return "redirect:/user/accounts";
            }
            
            model.addAttribute("account", account);
            
            // Get account transactions
            List<Transaction> transactions = transactionService.getTransactionsByAccount(account);
            model.addAttribute("transactions", transactions);
            
            return "user/account-details";
        } catch (Exception e) {
            logger.error("Error loading account details: {}", e.getMessage(), e);
            return "redirect:/user/accounts?error=Failed to load account details";
        }
    }

    /**
     * Transactions page
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('USER')")
    public String transactions(Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            // Get user transactions
            List<Transaction> transactions = transactionService.getTransactionsByUser(user);
            model.addAttribute("transactions", transactions);
            
            return "user/transactions";
        } catch (Exception e) {
            logger.error("Error loading transactions: {}", e.getMessage(), e);
            
            // Add error message and empty data to prevent template errors
            model.addAttribute("errorMessage", "Failed to load transactions data. Please try again later.");
            model.addAttribute("user", new User());
            model.addAttribute("transactions", new ArrayList<>());
            
            return "user/transactions";
        }
    }

    /**
     * Fund transfer page
     */
    @GetMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public String transferForm(Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            // Get user accounts
            List<Account> accounts = accountService.getAccountsByUser(user);
            model.addAttribute("accounts", accounts);
            model.addAttribute("hasAccounts", !accounts.isEmpty());
            
            // Transfer request
            model.addAttribute("transferRequest", new TransferRequest());
            
            return "user/transfer";
        } catch (Exception e) {
            logger.error("Error loading transfer form: {}", e.getMessage(), e);
            
            // Add error message and empty data to prevent template errors
            model.addAttribute("errorMessage", "Failed to load transfer form data. Please try again later.");
            model.addAttribute("user", new User());
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("hasAccounts", false);
            model.addAttribute("transferRequest", new TransferRequest());
            
            return "user/transfer";
        }
    }

    /**
     * Loans page
     */
    @GetMapping("/loans")
    @PreAuthorize("hasRole('USER')")
    public String loans(Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            // Get user loans
            List<Loan> loans = loanService.getLoansByUser(user);
            model.addAttribute("loans", loans);
            
            return "user/loans";
        } catch (Exception e) {
            logger.error("Error loading loans: {}", e.getMessage(), e);
            
            // Add error message and empty data to prevent template errors
            model.addAttribute("errorMessage", "Failed to load loans data. Please try again later.");
            model.addAttribute("user", new User());
            model.addAttribute("loans", new ArrayList<>());
            
            return "user/loans";
        }
    }

    /**
     * Loan details page
     */
    @GetMapping("/loans/{id}")
    @PreAuthorize("hasRole('USER')")
    public String loanDetails(@PathVariable Long id, Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            // Get loan details
            Loan loan = loanService.getLoanById(id);
            
            // Security check - ensure the loan belongs to the current user
            if (!loan.getUser().getId().equals(user.getId())) {
                return "redirect:/user/loans";
            }
            
            model.addAttribute("loan", loan);
            
            // Calculate remaining amount to be paid
            BigDecimal remainingAmount = loan.getAmount().subtract(loan.getRepaidAmount());
            model.addAttribute("remainingAmount", remainingAmount);
            
            // Get user accounts for repayment options
            List<Account> accounts = accountService.getAccountsByUser(user);
            model.addAttribute("accounts", accounts);
            
            return "user/loan-details";
        } catch (Exception e) {
            logger.error("Error loading loan details: {}", e.getMessage(), e);
            return "redirect:/user/loans?error=Failed to load loan details";
        }
    }

    /**
     * Apply for loan page
     */
    @GetMapping("/loans/apply")
    @PreAuthorize("hasRole('USER')")
    public String applyLoanForm(Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            // Get user accounts
            List<Account> accounts = accountService.getAccountsByUser(user);
            model.addAttribute("accounts", accounts);
            model.addAttribute("hasAccounts", !accounts.isEmpty());
            
            // Loan application request
            model.addAttribute("loanRequest", new LoanApplicationRequest());
            
            return "user/apply-loan";
        } catch (Exception e) {
            logger.error("Error loading loan application form: {}", e.getMessage(), e);
            
            // Add error message and empty data to prevent template errors
            model.addAttribute("errorMessage", "Failed to load loan application form. Please try again later.");
            model.addAttribute("user", new User());
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("hasAccounts", false);
            model.addAttribute("loanRequest", new LoanApplicationRequest());
            
            return "user/apply-loan";
        }
    }

    /**
     * Account statements page
     */
    @GetMapping("/statements")
    @PreAuthorize("hasRole('USER')")
    public String statements(Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            // Get user accounts
            List<Account> accounts = accountService.getAccountsByUser(user);
            model.addAttribute("accounts", accounts);
            model.addAttribute("hasAccounts", !accounts.isEmpty());
            
            return "user/statements";
        } catch (Exception e) {
            logger.error("Error loading statements: {}", e.getMessage(), e);
            
            // Add error message and empty data to prevent template errors
            model.addAttribute("errorMessage", "Failed to load statements data. Please try again later.");
            model.addAttribute("user", new User());
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("hasAccounts", false);
            
            return "user/statements";
        }
    }

    /**
     * Profile page
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public String profile(Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            return "user/profile";
        } catch (Exception e) {
            logger.error("Error loading profile: {}", e.getMessage(), e);
            
            // Add error message and empty data to prevent template errors
            model.addAttribute("errorMessage", "Failed to load profile data. Please try again later.");
            model.addAttribute("user", new User());
            
            return "user/profile";
        }
    }
    
    /**
     * Settings page
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('USER')")
    public String settings(Model model) {
        try {
            // Get current user
            User user = getAuthenticatedUser();
            model.addAttribute("user", user);
            
            return "user/settings";
        } catch (Exception e) {
            logger.error("Error loading settings: {}", e.getMessage(), e);
            
            // Add error message and empty data to prevent template errors
            model.addAttribute("errorMessage", "Failed to load settings data. Please try again later.");
            model.addAttribute("user", new User());
            
            return "user/settings";
        }
    }

    @GetMapping("/withdraw")
    public String showWithdrawPage(Model model, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        List<Account> accounts = accountService.getAccountsByUser(user);
        
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        
        return "user/withdraw";
    }

    @PostMapping("/withdraw")
    public String processWithdrawal(@RequestParam Long accountId,
                                  @RequestParam BigDecimal amount,
                                  @RequestParam String withdrawalMethod,
                                  @RequestParam(required = false) String description,
                                  Model model,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(principal.getName());
            Account account = accountService.getAccountById(accountId);
            
            // Validate account ownership
            if (!account.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to withdraw from this account.");
                return "redirect:/user/withdraw";
            }
            
            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid amount greater than zero.");
                return "redirect:/user/withdraw";
            }
            
            // Validate balance
            if (amount.compareTo(account.getBalance()) > 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Insufficient balance for this withdrawal.");
                return "redirect:/user/withdraw";
            }
            
            // Create transaction details
            Map<String, String> transactionDetails = new HashMap<>();
            transactionDetails.put("withdrawalMethod", withdrawalMethod);
            
            if (description != null && !description.isEmpty()) {
                transactionDetails.put("description", description);
            }
            
            // Process withdrawal
            transactionService.createWithdrawal(account, amount, transactionDetails);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Successfully withdrawn ₹" + amount + " from your account. Your new balance is ₹" + account.getBalance());
            return "redirect:/user/transactions";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing withdrawal: " + e.getMessage());
            return "redirect:/user/withdraw";
        }
    }

    @GetMapping("/deposit")
    public String showDepositPage(Model model, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        List<Account> accounts = accountService.getAccountsByUser(user);
        
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        
        return "user/deposit";
    }

    @PostMapping("/deposit")
    public String processDeposit(@RequestParam Long accountId,
                               @RequestParam BigDecimal amount,
                               @RequestParam String depositMethod,
                               @RequestParam(required = false) String description,
                               Model model,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(principal.getName());
            Account account = accountService.getAccountById(accountId);
            
            // Validate account ownership
            if (!account.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to deposit to this account.");
                return "redirect:/user/deposit";
            }
            
            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid amount greater than zero.");
                return "redirect:/user/deposit";
            }
            
            // Create transaction details
            Map<String, String> transactionDetails = new HashMap<>();
            transactionDetails.put("depositMethod", depositMethod);
            
            if (description != null && !description.isEmpty()) {
                transactionDetails.put("description", description);
            }
            
            // Process deposit
            transactionService.createDeposit(account, amount, transactionDetails);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Successfully deposited ₹" + amount + " to your account. Your new balance is ₹" + account.getBalance());
            return "redirect:/user/transactions";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing deposit: " + e.getMessage());
            return "redirect:/user/deposit";
        }
    }
}
