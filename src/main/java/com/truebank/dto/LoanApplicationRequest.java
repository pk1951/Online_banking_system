package com.truebank.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class LoanApplicationRequest {
    
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @NotBlank(message = "Loan type is required")
    private String loanType;
    
    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Loan tenure is required")
    @Min(value = 1, message = "Loan tenure must be at least 1 month")
    private Integer tenure; // Changed from termMonths to match frontend
    
    @NotBlank(message = "Purpose is required")
    private String purpose;
    
    @NotNull(message = "Income is required")
    @Positive(message = "Income must be positive")
    private BigDecimal income;
    
    private String additionalDetails;
    
    public LoanApplicationRequest() {
    }
    
    public LoanApplicationRequest(Long accountId, String loanType, BigDecimal amount, Integer tenure,
                                 String purpose, BigDecimal income, String additionalDetails) {
        this.accountId = accountId;
        this.loanType = loanType;
        this.amount = amount;
        this.tenure = tenure;
        this.purpose = purpose;
        this.income = income;
        this.additionalDetails = additionalDetails;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public String getLoanType() {
        return loanType;
    }
    
    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Integer getTenure() {
        return tenure;
    }
    
    public void setTenure(Integer tenure) {
        this.tenure = tenure;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public BigDecimal getIncome() {
        return income;
    }
    
    public void setIncome(BigDecimal income) {
        this.income = income;
    }
    
    public String getAdditionalDetails() {
        return additionalDetails;
    }
    
    public void setAdditionalDetails(String additionalDetails) {
        this.additionalDetails = additionalDetails;
    }
}
