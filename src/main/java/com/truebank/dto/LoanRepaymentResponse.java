package com.truebank.dto;

import com.truebank.model.LoanStatus;
import java.math.BigDecimal;

public class LoanRepaymentResponse {
    private String message;
    private Long loanId;
    private BigDecimal totalAmount;
    private BigDecimal repaidAmount;
    private BigDecimal remainingAmount;
    private LoanStatus status;
    
    public LoanRepaymentResponse() {
    }
    
    public LoanRepaymentResponse(String message, Long loanId, BigDecimal totalAmount, 
                               BigDecimal repaidAmount, BigDecimal remainingAmount, LoanStatus status) {
        this.message = message;
        this.loanId = loanId;
        this.totalAmount = totalAmount;
        this.repaidAmount = repaidAmount;
        this.remainingAmount = remainingAmount;
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getLoanId() {
        return loanId;
    }
    
    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getRepaidAmount() {
        return repaidAmount;
    }
    
    public void setRepaidAmount(BigDecimal repaidAmount) {
        this.repaidAmount = repaidAmount;
    }
    
    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }
    
    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
    
    public LoanStatus getStatus() {
        return status;
    }
    
    public void setStatus(LoanStatus status) {
        this.status = status;
    }
} 