package com.truebank.service;

import com.truebank.model.Account;
import com.truebank.model.Transaction;
import com.truebank.model.TransactionType;
import com.truebank.model.User;
import com.truebank.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    /**
     * Get all transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Get transaction by ID
     */
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
    }

    /**
     * Get transaction by reference number
     */
    public Transaction getTransactionByReferenceNumber(String referenceNumber) {
        return transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with reference number: " + referenceNumber));
    }

    /**
     * Get all transactions for an account
     */
    public List<Transaction> getTransactionsByAccount(Account account) {
        return transactionRepository.findAllTransactionsByAccount(account);
    }

    /**
     * Get all transactions for a branch
     */
    public List<Transaction> getTransactionsByBranchId(Long branchId) {
        return transactionRepository.findAllTransactionsByBranchId(branchId);
    }

    /**
     * Get transactions by type
     */
    public List<Transaction> getTransactionsByType(TransactionType transactionType) {
        return transactionRepository.findByTransactionType(transactionType);
    }

    /**
     * Get transactions by date range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }

    /**
     * Get transactions for a specific account by date range
     */
    public List<Transaction> getTransactionsByDateRange(Account account, LocalDateTime startDate, LocalDateTime endDate) {
        // Get all transactions for the account within the date range
        List<Transaction> allTransactions = getTransactionsByAccount(account);
        
        // Filter by date range
        return allTransactions.stream()
                .filter(t -> !t.getTransactionDate().isBefore(startDate) && !t.getTransactionDate().isAfter(endDate))
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .collect(Collectors.toList());
    }

    /**
     * Get transactions by user
     */
    public List<Transaction> getTransactionsByUser(User user) {
        List<Account> userAccounts = accountService.getAccountsByUser(user);
        return userAccounts.stream()
                .flatMap(account -> getTransactionsByAccount(account).stream())
                .collect(Collectors.toList());
    }

    /**
     * Get recent transactions by user with limit
     */
    public List<Transaction> getRecentTransactionsByUser(User user, int limit) {
        List<Transaction> allTransactions = getTransactionsByUser(user);
        return allTransactions.stream()
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get recent transactions with limit
     */
    public List<Transaction> getRecentTransactions(int limit) {
        return transactionRepository.findRecentTransactions(limit);
    }
    
    /**
     * Get transaction count
     */
    public long getTransactionCount() {
        return transactionRepository.count();
    }

    /**
     * Create a deposit transaction
     */
    @Transactional
    public Transaction createDepositTransaction(String accountNumber, BigDecimal amount, String description) {
        // Deposit to account
        Account account = accountService.deposit(accountNumber, amount);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setDestinationAccount(account);
        transaction.setSuccessful(true);
        
        return transactionRepository.save(transaction);
    }

    /**
     * Create a withdrawal transaction
     */
    @Transactional
    public Transaction createWithdrawalTransaction(String accountNumber, BigDecimal amount, String description) {
        // Withdraw from account
        Account account = accountService.withdraw(accountNumber, amount);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setSourceAccount(account);
        transaction.setSuccessful(true);
        
        return transactionRepository.save(transaction);
    }

    /**
     * Create a transfer transaction
     */
    @Transactional
    public Transaction createTransferTransaction(String sourceAccountNumber, String destinationAccountNumber, 
                                                BigDecimal amount, String description) {
        // Get accounts
        Account sourceAccount = accountService.getAccountByNumber(sourceAccountNumber);
        Account destinationAccount = accountService.getAccountByNumber(destinationAccountNumber);
        
        // Perform transfer
        accountService.transfer(sourceAccountNumber, destinationAccountNumber, amount);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setSuccessful(true);
        
        return transactionRepository.save(transaction);
    }

    /**
     * Create a loan disbursement transaction
     */
    @Transactional
    public Transaction createLoanDisbursementTransaction(String accountNumber, BigDecimal amount, String description) {
        // Deposit loan amount to account
        Account account = accountService.deposit(accountNumber, amount);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.LOAN_DISBURSEMENT);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setDestinationAccount(account);
        transaction.setSuccessful(true);
        
        return transactionRepository.save(transaction);
    }

    /**
     * Create a loan repayment transaction
     */
    @Transactional
    public Transaction createLoanRepaymentTransaction(String accountNumber, BigDecimal amount, String description) {
        // Withdraw repayment amount from account
        Account account = accountService.withdraw(accountNumber, amount);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.LOAN_REPAYMENT);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setSourceAccount(account);
        transaction.setSuccessful(true);
        
        return transactionRepository.save(transaction);
    }

    /**
     * Create deposit with transaction details
     */
    @Transactional
    public Transaction createDeposit(Account account, BigDecimal amount, Map<String, String> transactionDetails) {
        // Update account balance
        account.setBalance(account.getBalance().add(amount));
        accountService.updateAccount(account);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setDestinationAccount(account);
        transaction.setSuccessful(true);
        
        // Set description from transaction details if available
        if (transactionDetails.containsKey("description")) {
            transaction.setDescription(transactionDetails.get("description"));
        } else {
            String depositMethod = transactionDetails.getOrDefault("depositMethod", "CASH");
            transaction.setDescription("Deposit via " + depositMethod);
        }
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Create withdrawal with transaction details
     */
    @Transactional
    public Transaction createWithdrawal(Account account, BigDecimal amount, Map<String, String> transactionDetails) {
        // Validate sufficient balance
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for withdrawal");
        }
        
        // Update account balance
        account.setBalance(account.getBalance().subtract(amount));
        accountService.updateAccount(account);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setSourceAccount(account);
        transaction.setSuccessful(true);
        
        // Set description from transaction details if available
        if (transactionDetails.containsKey("description")) {
            transaction.setDescription(transactionDetails.get("description"));
        } else {
            String withdrawalMethod = transactionDetails.getOrDefault("withdrawalMethod", "CASH");
            transaction.setDescription("Withdrawal via " + withdrawalMethod);
        }
        
        return transactionRepository.save(transaction);
    }

    /**
     * Create a credit transaction
     */
    @Transactional
    public Transaction createCreditTransaction(String accountNumber, BigDecimal amount, String description) {
        // Deposit to account (credit increases balance)
        Account account = accountService.deposit(accountNumber, amount);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setDestinationAccount(account);
        transaction.setSuccessful(true);
        
        return transactionRepository.save(transaction);
    }

    /**
     * Create a debit transaction
     */
    @Transactional
    public Transaction createDebitTransaction(String accountNumber, BigDecimal amount, String description) {
        // Withdraw from account (debit decreases balance)
        Account account = accountService.withdraw(accountNumber, amount);
        
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setSourceAccount(account);
        transaction.setSuccessful(true);
        
        return transactionRepository.save(transaction);
    }
}
