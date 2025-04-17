package com.truebank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class TransactionRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String description;
    
    // Required for transfers initiated from form
    private String sourceAccountNumber;
    
    // Only required for transfers
    private String destinationAccountNumber;
    
    public TransactionRequest() {
    }
    
    public TransactionRequest(BigDecimal amount, String description, String sourceAccountNumber, String destinationAccountNumber) {
        this.amount = amount;
        this.description = description;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }
    
    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }
    
    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }
    
    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }
}
