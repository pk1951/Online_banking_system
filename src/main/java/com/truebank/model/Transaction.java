package com.truebank.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @Column(nullable = true)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;

    @ManyToOne
    @JoinColumn(name = "source_account_id")
    @JsonBackReference(value = "account-outgoing")
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    @JsonBackReference(value = "account-incoming")
    private Account destinationAccount;

    @Column(nullable = false)
    private boolean successful = true;

    public Transaction() {
    }

    public Transaction(Long id, TransactionType transactionType, BigDecimal amount, String description,
                      LocalDateTime transactionDate, String referenceNumber, Account sourceAccount,
                      Account destinationAccount, boolean successful) {
        this.id = id;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.referenceNumber = referenceNumber;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.successful = successful;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
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

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(Account sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public Account getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(Account destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    @PrePersist
    protected void onCreate() {
        this.transactionDate = LocalDateTime.now();
        // Generate unique reference number
        this.referenceNumber = "TXN" + System.currentTimeMillis();
    }
}
