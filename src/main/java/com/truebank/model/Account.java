package com.truebank.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false)
    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    @NotNull(message = "Balance is required")
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-accounts")
    private User user;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    @JsonBackReference(value = "branch-accounts")
    private Branch branch;

    @OneToMany(mappedBy = "sourceAccount", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "account-outgoing")
    private Set<Transaction> outgoingTransactions = new HashSet<>();

    @OneToMany(mappedBy = "destinationAccount", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "account-incoming")
    private Set<Transaction> incomingTransactions = new HashSet<>();

    public Account() {
    }

    public Account(Long id, String accountNumber, AccountType accountType, BigDecimal balance,
                  LocalDateTime createdAt, LocalDateTime updatedAt, boolean active, User user,
                  Branch branch, Set<Transaction> outgoingTransactions, Set<Transaction> incomingTransactions) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
        this.user = user;
        this.branch = branch;
        this.outgoingTransactions = outgoingTransactions;
        this.incomingTransactions = incomingTransactions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public Set<Transaction> getOutgoingTransactions() {
        return outgoingTransactions;
    }

    public void setOutgoingTransactions(Set<Transaction> outgoingTransactions) {
        this.outgoingTransactions = outgoingTransactions;
    }

    public Set<Transaction> getIncomingTransactions() {
        return incomingTransactions;
    }

    public void setIncomingTransactions(Set<Transaction> incomingTransactions) {
        this.incomingTransactions = incomingTransactions;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
