package com.truebank.service;

import com.truebank.model.*;
import com.truebank.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final BranchService branchService;

    /**
     * Get all accounts
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Get all active accounts
     */
    public List<Account> getAllActiveAccounts() {
        return accountRepository.findByActive(true);
    }

    /**
     * Get account by ID
     */
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + id));
    }

    /**
     * Get account by account number
     */
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with number: " + accountNumber));
    }

    /**
     * Get accounts by user
     */
    public List<Account> getAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }

    /**
     * Get accounts by branch
     */
    public List<Account> getAccountsByBranch(Branch branch) {
        return accountRepository.findByBranch(branch);
    }

    /**
     * Get account count
     */
    public long getAccountCount() {
        return accountRepository.count();
    }
    
    /**
     * Count accounts created today
     */
    public int countTodayNewAccounts() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        return accountRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

    /**
     * Create a new account
     */
    @Transactional
    public Account createAccount(User user, String branchCode, AccountType accountType) {
        // Get branch by code
        Branch branch = branchService.getBranchByCode(branchCode);
        
        // Generate account number (TRUE + 3-digit branch code + 4-digit random number)
        String accountNumber = generateAccountNumber(branch.getBranchCode());
        
        // Create new account
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setBranch(branch);
        account.setActive(true);
        
        return accountRepository.save(account);
    }
    
    /**
     * Save an account (used for banker creating accounts)
     */
    @Transactional
    public Account saveAccount(Account account) {
        // Generate account number if it doesn't exist
        if (account.getAccountNumber() == null || account.getAccountNumber().isEmpty()) {
            String accountNumber = generateAccountNumber(account.getBranch().getBranchCode());
            account.setAccountNumber(accountNumber);
        }
        
        return accountRepository.save(account);
    }

    /**
     * Generate unique account number
     */
    private String generateAccountNumber(String branchCode) {
        String prefix = "TRUE";
        Random random = new Random();
        String randomDigits;
        String accountNumber;
        
        do {
            // Generate 4 random digits
            randomDigits = String.format("%04d", random.nextInt(10000));
            accountNumber = prefix + branchCode + randomDigits;
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }

    /**
     * Deposit money into account
     */
    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        Account account = getAccountByNumber(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        
        return accountRepository.save(account);
    }

    /**
     * Withdraw money from account
     */
    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        Account account = getAccountByNumber(accountNumber);
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        
        account.setBalance(account.getBalance().subtract(amount));
        
        return accountRepository.save(account);
    }

    /**
     * Transfer money between accounts
     */
    @Transactional
    public void transfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        Account sourceAccount = getAccountByNumber(sourceAccountNumber);
        Account destinationAccount = getAccountByNumber(destinationAccountNumber);
        
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance in source account");
        }
        
        // Withdraw from source account
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        accountRepository.save(sourceAccount);
        
        // Deposit to destination account
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
        accountRepository.save(destinationAccount);
    }

    /**
     * Calculate account balance as of a specific date/time
     * Used for statement generation
     */
    public BigDecimal getBalanceAsOfDate(Account account, LocalDateTime dateTime) {
        // Get the current balance
        BigDecimal currentBalance = account.getBalance();
        
        // Get all transactions after the specified date
        List<Transaction> laterTransactions = accountRepository.findTransactionsAfterDate(account.getId(), dateTime);
        
        // Adjust current balance by reverting all transactions that happened after the specified date
        for (Transaction transaction : laterTransactions) {
            if (transaction.getTransactionType() == TransactionType.CREDIT 
                    || transaction.getTransactionType() == TransactionType.DEPOSIT) {
                // Subtract credits that happened after the date
                currentBalance = currentBalance.subtract(transaction.getAmount());
            } else if (transaction.getTransactionType() == TransactionType.DEBIT 
                    || transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
                // Add back debits that happened after the date
                currentBalance = currentBalance.add(transaction.getAmount());
            }
        }
        
        return currentBalance;
    }

    /**
     * Deactivate account
     */
    public void deactivateAccount(Long id) {
        Account account = getAccountById(id);
        account.setActive(false);
        accountRepository.save(account);
    }

    /**
     * Activate account
     */
    public void activateAccount(Long id) {
        Account account = getAccountById(id);
        account.setActive(true);
        accountRepository.save(account);
    }

    /**
     * Update an account
     */
    @Transactional
    public Account updateAccount(Account account) {
        if (account.getId() == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        
        // Ensure account exists
        Account existingAccount = accountRepository.findById(account.getId())
            .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + account.getId()));
        
        return accountRepository.save(account);
    }
}
