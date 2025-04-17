package com.truebank.repository;

import com.truebank.model.Account;
import com.truebank.model.AccountType;
import com.truebank.model.Branch;
import com.truebank.model.Transaction;
import com.truebank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    List<Account> findByUser(User user);
    
    List<Account> findByBranch(Branch branch);
    
    List<Account> findByUserAndAccountType(User user, AccountType accountType);
    
    List<Account> findByBranchAndAccountType(Branch branch, AccountType accountType);
    
    List<Account> findByActive(boolean active);
    
    Boolean existsByAccountNumber(String accountNumber);
    
    int countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) AND t.transactionDate > :dateTime ORDER BY t.transactionDate ASC")
    List<Transaction> findTransactionsAfterDate(@Param("accountId") Long accountId, @Param("dateTime") LocalDateTime dateTime);
}
