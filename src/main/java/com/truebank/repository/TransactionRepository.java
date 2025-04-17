package com.truebank.repository;

import com.truebank.model.Account;
import com.truebank.model.Transaction;
import com.truebank.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
    
    List<Transaction> findBySourceAccount(Account account);
    
    List<Transaction> findByDestinationAccount(Account account);
    
    List<Transaction> findByTransactionType(TransactionType transactionType);
    
    List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount = ?1 OR t.destinationAccount = ?1 ORDER BY t.transactionDate DESC")
    List<Transaction> findAllTransactionsByAccount(Account account);
    
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.branch.id = ?1 OR t.destinationAccount.branch.id = ?1 ORDER BY t.transactionDate DESC")
    List<Transaction> findAllTransactionsByBranchId(Long branchId);
    
    @Query(value = "SELECT * FROM transactions ORDER BY transaction_date DESC LIMIT ?1", nativeQuery = true)
    List<Transaction> findRecentTransactions(int limit);
}
