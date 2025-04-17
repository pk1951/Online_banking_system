package com.truebank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;

    @Column(nullable = false)
    @NotNull(message = "Amount is required")
    @Positive
    private BigDecimal amount;

    @Column(name = "interest_rate", nullable = false)
    @NotNull(message = "Interest rate is required")
    @Positive
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    @ColumnDefault("0")
    private Integer termMonths;

    @Column(name = "cibil_score", nullable = false)
    private Integer cibilScore;

    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_status", nullable = false)
    private LoanStatus loanStatus = LoanStatus.PENDING;

    @Column(nullable = true)
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ColumnDefault("0.00")
    @Column(precision = 19, scale = 2)
    private BigDecimal repaidAmount = BigDecimal.ZERO;

    public Loan() {
    }

    public Loan(Long id, LoanType loanType, BigDecimal amount, BigDecimal interestRate, Integer termMonths,
               Integer cibilScore, LocalDateTime applicationDate, LocalDateTime approvalDate, LoanStatus loanStatus,
               String remarks, User user, Branch branch, User approvedBy, BigDecimal repaidAmount) {
        this.id = id;
        this.loanType = loanType;
        this.amount = amount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.cibilScore = cibilScore;
        this.applicationDate = applicationDate;
        this.approvalDate = approvalDate;
        this.loanStatus = loanStatus;
        this.remarks = remarks;
        this.user = user;
        this.branch = branch;
        this.approvedBy = approvedBy;
        this.repaidAmount = repaidAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public Integer getCibilScore() {
        return cibilScore;
    }

    public void setCibilScore(Integer cibilScore) {
        this.cibilScore = cibilScore;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public LoanStatus getLoanStatus() {
        return loanStatus;
    }

    public void setLoanStatus(LoanStatus loanStatus) {
        this.loanStatus = loanStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public BigDecimal getRepaidAmount() {
        return repaidAmount;
    }

    public void setRepaidAmount(BigDecimal repaidAmount) {
        this.repaidAmount = repaidAmount;
    }

    @PrePersist
    protected void onCreate() {
        this.applicationDate = LocalDateTime.now();
    }
}
