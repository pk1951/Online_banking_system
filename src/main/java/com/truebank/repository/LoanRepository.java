package com.truebank.repository;

import com.truebank.model.Branch;
import com.truebank.model.Loan;
import com.truebank.model.LoanStatus;
import com.truebank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    List<Loan> findByUser(User user);
    
    List<Loan> findByBranch(Branch branch);
    
    List<Loan> findByLoanStatus(LoanStatus status);
    
    List<Loan> findByBranchAndLoanStatus(Branch branch, LoanStatus status);
    
    List<Loan> findByUserAndLoanStatus(User user, LoanStatus status);
    
    List<Loan> findByApprovedBy(User manager);
    
    long countByLoanStatus(LoanStatus status);
}
