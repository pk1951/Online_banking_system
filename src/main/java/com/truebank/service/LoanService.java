package com.truebank.service;

import com.truebank.model.*;
import com.truebank.repository.LoanRepository;
import com.truebank.dto.LoanRepaymentResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final TransactionService transactionService;
    private final AccountService accountService;

    /**
     * Get all loans
     */
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    /**
     * Get loan by ID
     */
    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found with id: " + id));
    }

    /**
     * Get loans by user
     */
    public List<Loan> getLoansByUser(User user) {
        return loanRepository.findByUser(user);
    }

    /**
     * Get loans by branch
     */
    public List<Loan> getLoansByBranch(Branch branch) {
        return loanRepository.findByBranch(branch);
    }

    /**
     * Get loans by status
     */
    public List<Loan> getLoansByStatus(LoanStatus status) {
        return loanRepository.findByLoanStatus(status);
    }

    /**
     * Get pending loans for a branch
     */
    public List<Loan> getPendingLoansByBranch(Branch branch) {
        return loanRepository.findByBranchAndLoanStatus(branch, LoanStatus.PENDING);
    }

    /**
     * Count pending loan applications
     */
    public int countPendingLoanApplications() {
        return (int) loanRepository.countByLoanStatus(LoanStatus.PENDING);
    }

    /**
     * Apply for a loan
     */
    public Loan applyForLoan(User user, Branch branch, LoanType loanType, BigDecimal amount, 
                           Integer termMonths, Integer cibilScore) {
        // Set a default CIBIL score if not provided
        if (cibilScore == null) {
            cibilScore = 750; // Default to excellent credit score
        }
        
        // Create loan application
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBranch(branch);
        loan.setLoanType(loanType);
        loan.setAmount(amount);
        loan.setTermMonths(termMonths);
        loan.setCibilScore(cibilScore);
        
        // Set interest rate based on loan type only
        loan.setInterestRate(calculateInterestRate(loanType, cibilScore));
        
        // Set initial status as PENDING
        loan.setLoanStatus(LoanStatus.PENDING);
        
        return loanRepository.save(loan);
    }

    /**
     * Calculate interest rate based on loan type and CIBIL score
     */
    private BigDecimal calculateInterestRate(LoanType loanType, Integer cibilScore) {
        // Base interest rates for different loan types
        BigDecimal baseRate;
        
        switch (loanType) {
            case HOME_LOAN:
                baseRate = new BigDecimal("8.50");
                break;
            case PERSONAL_LOAN:
                baseRate = new BigDecimal("12.00");
                break;
            case EDUCATION_LOAN:
                baseRate = new BigDecimal("7.50");
                break;
            case VEHICLE_LOAN:
                baseRate = new BigDecimal("9.50");
                break;
            case BUSINESS_LOAN:
                baseRate = new BigDecimal("11.00");
                break;
            default:
                baseRate = new BigDecimal("10.00");
        }
        
        // Adjust rate based on CIBIL score
        if (cibilScore >= 750) {
            // Excellent credit - reduce rate by 1%
            return baseRate.subtract(new BigDecimal("1.00"));
        } else if (cibilScore >= 650) {
            // Good credit - reduce rate by 0.5%
            return baseRate.subtract(new BigDecimal("0.50"));
        } else if (cibilScore >= 550) {
            // Fair credit - standard rate
            return baseRate;
        } else if (cibilScore >= 450) {
            // Poor credit - increase rate by 1%
            return baseRate.add(new BigDecimal("1.00"));
        } else {
            // Very poor credit - increase rate by 2%
            return baseRate.add(new BigDecimal("2.00"));
        }
    }

    /**
     * Approve a loan
     */
    @Transactional
    public Loan approveLoan(Long loanId, User manager, String remarks) {
        Loan loan = getLoanById(loanId);
        
        // Check if loan is in PENDING status
        if (loan.getLoanStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Cannot approve loan that is not in PENDING status");
        }
        
        // Update loan status
        loan.setLoanStatus(LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDateTime.now());
        loan.setApprovedBy(manager);
        loan.setRemarks(remarks);
        
        return loanRepository.save(loan);
    }

    /**
     * Reject a loan
     */
    public Loan rejectLoan(Long loanId, User manager, String remarks) {
        Loan loan = getLoanById(loanId);
        
        // Check if loan is in PENDING status
        if (loan.getLoanStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Cannot reject loan that is not in PENDING status");
        }
        
        // Update loan status
        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setApprovalDate(LocalDateTime.now());
        loan.setApprovedBy(manager);
        loan.setRemarks(remarks);
        
        return loanRepository.save(loan);
    }

    /**
     * Disburse a loan
     */
    @Transactional
    public Loan disburseLoan(Long loanId, String accountNumber) {
        Loan loan = getLoanById(loanId);
        
        // Check if loan is in APPROVED status
        if (loan.getLoanStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Cannot disburse loan that is not in APPROVED status");
        }
        
        // Create loan disbursement transaction
        transactionService.createLoanDisbursementTransaction(
            accountNumber, 
            loan.getAmount(), 
            "Loan disbursement for " + loan.getLoanType() + " - Loan ID: " + loan.getId()
        );
        
        // Update loan status
        loan.setLoanStatus(LoanStatus.DISBURSED);
        
        return loanRepository.save(loan);
    }

    /**
     * Make loan repayment
     */
    @Transactional
    public LoanRepaymentResponse makeLoanRepayment(Long loanId, String accountNumber, BigDecimal amount) {
        Loan loan = getLoanById(loanId);
        
        // Check if loan is in DISBURSED status
        if (loan.getLoanStatus() != LoanStatus.DISBURSED) {
            throw new IllegalStateException("Cannot make repayment for loan that is not in DISBURSED status");
        }
        
        // Create loan repayment transaction
        transactionService.createLoanRepaymentTransaction(
            accountNumber, 
            amount, 
            "Loan repayment for " + loan.getLoanType() + " - Loan ID: " + loan.getId()
        );
        
        // Update repaid amount
        BigDecimal newRepaidAmount = loan.getRepaidAmount().add(amount);
        loan.setRepaidAmount(newRepaidAmount);
        
        // Check if loan is fully repaid
        if (newRepaidAmount.compareTo(loan.getAmount()) >= 0) {
            loan.setLoanStatus(LoanStatus.CLOSED);
        }
        
        loan = loanRepository.save(loan);
        
        // Calculate remaining amount
        BigDecimal remainingAmount = loan.getAmount().subtract(loan.getRepaidAmount());
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }
        
        // Create response
        return new LoanRepaymentResponse(
            "Loan repayment successful",
            loan.getId(),
            loan.getAmount(),
            loan.getRepaidAmount(),
            remainingAmount,
            loan.getLoanStatus()
        );
    }

    /**
     * Save loan
     */
    @Transactional
    public Loan saveLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    /**
     * Update loan status
     */
    @Transactional
    public Loan updateLoanStatus(Long id, LoanStatus status) {
        Loan loan = getLoanById(id);
        loan.setLoanStatus(status);
        return loanRepository.save(loan);
    }

    /**
     * Delete loan
     */
    @Transactional
    public void deleteLoan(Long id) {
        loanRepository.deleteById(id);
    }

    /**
     * Get pending loans count
     */
    public long getPendingLoansCount() {
        return loanRepository.countByLoanStatus(LoanStatus.PENDING);
    }

    /**
     * Get approved loans count
     */
    public long getApprovedLoansCount() {
        return loanRepository.countByLoanStatus(LoanStatus.APPROVED);
    }

    /**
     * Get disbursed loans count
     */
    public long getDisbursedLoansCount() {
        return loanRepository.countByLoanStatus(LoanStatus.DISBURSED);
    }
}
