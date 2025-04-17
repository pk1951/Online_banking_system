package com.truebank.controller;

import com.truebank.dto.LoanApplicationRequest;
import com.truebank.model.*;
import com.truebank.security.services.UserDetailsImpl;
import com.truebank.service.AccountService;
import com.truebank.service.LoanService;
import com.truebank.service.TransactionService;
import com.truebank.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {

    private static final Logger logger = LoggerFactory.getLogger(UserApiController.class);
    
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;

    /**
     * Helper method to get the authenticated user
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userService.getUserById(userDetails.getId());
    }

    /**
     * Get account statement
     */
    @GetMapping("/accounts/{accountId}/statement")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAccountStatement(
            @PathVariable Long accountId,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        try {
            // Get authenticated user
            User user = getAuthenticatedUser();
            
            // Get account
            Account account = accountService.getAccountById(accountId);
            
            // Check if account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You are not authorized to access this account's statements"
                ));
            }
            
            // Parse dates
            LocalDateTime startDateTime = LocalDate.parse(fromDate).atStartOfDay();
            LocalDateTime endDateTime = LocalDate.parse(toDate).atTime(LocalTime.MAX);
            
            // Get transactions for date range
            List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDateTime, endDateTime);
            
            // Filter transactions for this account
            List<Transaction> accountTransactions = transactions.stream()
                .filter(t -> (t.getSourceAccount() != null && t.getSourceAccount().getId().equals(accountId)) 
                          || (t.getDestinationAccount() != null && t.getDestinationAccount().getId().equals(accountId)))
                .toList();
            
            // Calculate total amount and ending balance
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal endingBalance = account.getBalance();
            
            // Process transactions for statement
            List<Map<String, Object>> statementTransactions = new ArrayList<>();
            BigDecimal runningBalance = account.getBalance();
            
            for (Transaction transaction : accountTransactions) {
                boolean isCredit = transaction.getDestinationAccount() != null 
                                  && transaction.getDestinationAccount().getId().equals(accountId);
                
                BigDecimal amount = transaction.getAmount();
                
                // Update running balance - this should be more accurate
                if (isCredit) {
                    totalAmount = totalAmount.add(amount);
                    runningBalance = runningBalance.add(amount);
                } else {
                    totalAmount = totalAmount.subtract(amount);
                    runningBalance = runningBalance.subtract(amount);
                }
                
                // Create transaction record for statement
                Map<String, Object> transactionRecord = new HashMap<>();
                transactionRecord.put("id", transaction.getId());
                transactionRecord.put("createdAt", transaction.getTransactionDate());
                transactionRecord.put("description", transaction.getDescription());
                transactionRecord.put("type", transaction.getTransactionType().toString());
                transactionRecord.put("amount", amount);
                transactionRecord.put("isCredit", isCredit);
                
                // Add source and destination account info if available
                if (transaction.getSourceAccount() != null) {
                    Map<String, Object> sourceAccount = new HashMap<>();
                    sourceAccount.put("id", transaction.getSourceAccount().getId());
                    sourceAccount.put("number", transaction.getSourceAccount().getAccountNumber());
                    transactionRecord.put("sourceAccount", sourceAccount);
                }
                
                if (transaction.getDestinationAccount() != null) {
                    Map<String, Object> destAccount = new HashMap<>();
                    destAccount.put("id", transaction.getDestinationAccount().getId());
                    destAccount.put("number", transaction.getDestinationAccount().getAccountNumber());
                    transactionRecord.put("destinationAccount", destAccount);
                }
                
                // Add running balance
                transactionRecord.put("runningBalance", runningBalance);
                
                statementTransactions.add(transactionRecord);
            }
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accountId", accountId);
            response.put("accountNumber", account.getAccountNumber());
            response.put("fromDate", fromDate);
            response.put("toDate", toDate);
            response.put("transactions", statementTransactions);
            response.put("totalAmount", totalAmount);
            response.put("endingBalance", endingBalance);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating account statement: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to generate account statement: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get account statement as PDF
     */
    @GetMapping("/accounts/{accountId}/statement/pdf")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAccountStatementPdf(
            @PathVariable Long accountId,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        try {
            // Get authenticated user
            User user = getAuthenticatedUser();
            
            // Get account
            Account account = accountService.getAccountById(accountId);
            
            // Check if account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You are not authorized to access this account's statements"
                ));
            }
            
            // Parse dates
            LocalDateTime startDateTime = LocalDate.parse(fromDate).atStartOfDay();
            LocalDateTime endDateTime = LocalDate.parse(toDate).atTime(LocalTime.MAX);
            
            // Create filename
            String fileName = "statement_" + account.getAccountNumber() + "_" + fromDate + "_to_" + toDate + ".pdf";
            
            // In a real application, you would generate a PDF here
            // For now, we'll just return a text placeholder as a response
            String pdfContent = "PDF Statement for Account: " + account.getAccountNumber() + 
                                "\nPeriod: " + fromDate + " to " + toDate +
                                "\nThis is a placeholder for a real PDF statement.";
                                
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            
            return new ResponseEntity<>(pdfContent.getBytes(), headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating PDF statement: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to generate PDF statement: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get account statement as CSV
     */
    @GetMapping("/accounts/{accountId}/statement/csv")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAccountStatementCsv(
            @PathVariable Long accountId,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        try {
            // Get authenticated user
            User user = getAuthenticatedUser();
            
            // Get account
            Account account = accountService.getAccountById(accountId);
            
            // Check if account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You are not authorized to access this account's statements"
                ));
            }
            
            // Parse dates
            LocalDateTime startDateTime = LocalDate.parse(fromDate).atStartOfDay();
            LocalDateTime endDateTime = LocalDate.parse(toDate).atTime(LocalTime.MAX);
            
            // Get transactions for date range
            List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDateTime, endDateTime);
            
            // Filter transactions for this account
            List<Transaction> accountTransactions = transactions.stream()
                .filter(t -> (t.getSourceAccount() != null && t.getSourceAccount().getId().equals(accountId)) 
                          || (t.getDestinationAccount() != null && t.getDestinationAccount().getId().equals(accountId)))
                .toList();
            
            // Create CSV content
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Date,Transaction ID,Description,Type,Amount,Running Balance\n");
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            BigDecimal runningBalance = account.getBalance();
            
            for (Transaction transaction : accountTransactions) {
                boolean isCredit = transaction.getDestinationAccount() != null 
                                  && transaction.getDestinationAccount().getId().equals(accountId);
                
                BigDecimal amount = transaction.getAmount();
                
                // Update running balance
                if (isCredit) {
                    runningBalance = runningBalance.add(amount);
                } else {
                    runningBalance = runningBalance.subtract(amount);
                }
                
                // Format type
                String type = transaction.getTransactionType().toString();
                if (transaction.getTransactionType() == TransactionType.TRANSFER) {
                    type = isCredit ? "CREDIT" : "DEBIT";
                }
                
                // Add CSV row
                csvContent.append(transaction.getTransactionDate().format(dateFormatter)).append(",")
                          .append(transaction.getId()).append(",")
                          .append("\"").append(transaction.getDescription()).append("\"").append(",")
                          .append(type).append(",")
                          .append(isCredit ? "+" : "-").append(amount).append(",")
                          .append(runningBalance).append("\n");
            }
            
            // Create filename
            String fileName = "statement_" + account.getAccountNumber() + "_" + fromDate + "_to_" + toDate + ".csv";
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", fileName);
            
            return new ResponseEntity<>(csvContent.toString().getBytes(), headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating CSV statement: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to generate CSV statement: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint to get account statement for a specific account and date range
     */
    @GetMapping("/account-statement")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAccountStatement(
            @RequestParam Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        try {
            // Get authenticated user
            User user = getAuthenticatedUser();
            
            // Get account
            Account account = accountService.getAccountById(accountId);
            
            // Verify account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Account does not belong to authenticated user"
                ));
            }
            
            // Convert dates to LocalDateTime (start of fromDate to end of toDate)
            LocalDateTime fromDateTime = fromDate.atStartOfDay();
            LocalDateTime toDateTime = toDate.plusDays(1).atStartOfDay().minusNanos(1);
            
            // Get transactions for the date range
            List<Transaction> transactions = transactionService.getTransactionsByDateRange(
                    account, fromDateTime, toDateTime);
            
            // Calculate balance before the from date
            BigDecimal openingBalance = accountService.getBalanceAsOfDate(account, fromDateTime);
            
            // Map transactions to response format
            List<Map<String, Object>> transactionsList = new ArrayList<>();
            BigDecimal runningBalance = openingBalance;
            
            for (Transaction transaction : transactions) {
                Map<String, Object> transactionMap = new LinkedHashMap<>();
                transactionMap.put("id", transaction.getId());
                transactionMap.put("date", transaction.getTransactionDate());
                transactionMap.put("referenceNumber", transaction.getReferenceNumber());
                transactionMap.put("description", transaction.getDescription());
                transactionMap.put("type", transaction.getTransactionType().toString());
                
                BigDecimal amount = transaction.getAmount();
                transactionMap.put("amount", amount);
                
                // Calculate running balance
                if (transaction.getTransactionType() == TransactionType.CREDIT || 
                    transaction.getTransactionType() == TransactionType.DEPOSIT) {
                    runningBalance = runningBalance.add(amount);
                } else if (transaction.getTransactionType() == TransactionType.DEBIT || 
                          transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
                    runningBalance = runningBalance.subtract(amount);
                }
                
                transactionMap.put("runningBalance", runningBalance);
                transactionsList.add(transactionMap);
            }
            
            // Calculate totals
            BigDecimal totalCredit = transactions.stream()
                    .filter(t -> t.getTransactionType() == TransactionType.CREDIT || 
                               t.getTransactionType() == TransactionType.DEPOSIT)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalDebit = transactions.stream()
                    .filter(t -> t.getTransactionType() == TransactionType.DEBIT || 
                               t.getTransactionType() == TransactionType.WITHDRAWAL)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Create response object
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("accountNumber", account.getAccountNumber());
            response.put("accountName", user.getFirstName() + " " + user.getLastName());
            response.put("accountType", account.getAccountType().toString());
            response.put("fromDate", fromDate);
            response.put("toDate", toDate);
            response.put("openingBalance", openingBalance);
            response.put("closingBalance", runningBalance);
            response.put("totalCredit", totalCredit);
            response.put("totalDebit", totalDebit);
            response.put("transactions", transactionsList);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating account statement: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to generate statement: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Apply for loan
     */
    @PostMapping("/loans/apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> applyForLoan(@Valid @RequestBody LoanApplicationRequest request) {
        try {
            // Get authenticated user
            User user = getAuthenticatedUser();
            
            // Get account
            Account account = accountService.getAccountById(request.getAccountId());
            
            // Verify account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Account does not belong to authenticated user"
                ));
            }
            
            // Convert loan type string to enum
            LoanType loanType;
            try {
                switch (request.getLoanType()) {
                    case "PERSONAL":
                        loanType = LoanType.PERSONAL_LOAN;
                        break;
                    case "HOME":
                        loanType = LoanType.HOME_LOAN;
                        break;
                    case "CAR":
                        loanType = LoanType.VEHICLE_LOAN;
                        break;
                    case "EDUCATION":
                        loanType = LoanType.EDUCATION_LOAN;
                        break;
                    case "BUSINESS":
                        loanType = LoanType.BUSINESS_LOAN;
                        break;
                    default:
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Invalid loan type"
                        ));
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid loan type: " + e.getMessage()
                ));
            }
            
            // Map interest rates based on loan type
            BigDecimal interestRate;
            switch (loanType) {
                case PERSONAL_LOAN:
                    interestRate = new BigDecimal("12.5");
                    break;
                case HOME_LOAN:
                    interestRate = new BigDecimal("8.5");
                    break;
                case VEHICLE_LOAN:
                    interestRate = new BigDecimal("9.5");
                    break;
                case EDUCATION_LOAN:
                    interestRate = new BigDecimal("7.5");
                    break;
                case BUSINESS_LOAN:
                    interestRate = new BigDecimal("14.0");
                    break;
                default:
                    interestRate = new BigDecimal("10.0");
            }
            
            // Generate a random CIBIL score between 650 and 900
            int cibilScore = new Random().nextInt(251) + 650;
            
            // Create new loan
            Loan loan = new Loan();
            loan.setUser(user);
            loan.setLoanType(loanType);
            loan.setAmount(request.getAmount());
            loan.setInterestRate(interestRate);
            loan.setTermMonths(request.getTenure());
            loan.setCibilScore(cibilScore);
            loan.setLoanStatus(LoanStatus.PENDING);
            loan.setBranch(account.getBranch());
            
            // Add remarks if provided
            if (request.getAdditionalDetails() != null && !request.getAdditionalDetails().isEmpty()) {
                loan.setRemarks("Purpose: " + request.getPurpose() + ", " + request.getAdditionalDetails());
            } else {
                loan.setRemarks("Purpose: " + request.getPurpose());
            }
            
            // Save loan
            Loan savedLoan = loanService.saveLoan(loan);
            
            // Create success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan application submitted successfully");
            response.put("loanId", savedLoan.getId());
            response.put("status", savedLoan.getLoanStatus().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error applying for loan: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to process loan application: " + e.getMessage()
            ));
        }
    }
} 