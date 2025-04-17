package com.truebank.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "branches")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Branch name is required")
    private String name;

    @Column(name = "branch_code", unique = true, nullable = false)
    @NotBlank(message = "Branch code is required")
    @Pattern(regexp = "\\d{3}", message = "Branch code must be 3 digits")
    private String branchCode;

    @Column(nullable = false)
    @NotBlank(message = "Address is required")
    private String address;

    @Column(nullable = false)
    @NotBlank(message = "City is required")
    private String city;

    @Column(nullable = false)
    @NotBlank(message = "State is required")
    private String state;

    @Column(name = "zip_code", nullable = false)
    @NotBlank(message = "Zip code is required")
    private String zipCode;

    @Column(nullable = false)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10,12}", message = "Phone number must be between 10 and 12 digits")
    private String phoneNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "branch-accounts")
    private Set<Account> accounts = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "manager_id")
    @JsonBackReference(value = "manager-branch")
    private User manager;
    
    // Transient field for Manager entity compatibility
    @Transient
    private Manager managerEntity;
    
    public Branch() {
    }
    
    public Branch(Long id, String name, String branchCode, String address, String city, String state,
                 String zipCode, String phoneNumber, LocalDateTime createdAt, LocalDateTime updatedAt,
                 boolean active, Set<Account> accounts, User manager, Manager managerEntity) {
        this.id = id;
        this.name = name;
        this.branchCode = branchCode;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
        this.accounts = accounts;
        this.manager = manager;
        this.managerEntity = managerEntity;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBranchCode() {
        return branchCode;
    }
    
    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
    
    public Set<Account> getAccounts() {
        return accounts;
    }
    
    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }
    
    public User getManager() {
        return manager;
    }
    
    public void setManager(User manager) {
        this.manager = manager;
    }
    
    public Manager getManagerEntity() {
        return managerEntity;
    }
    
    public void setManagerEntity(Manager managerEntity) {
        this.managerEntity = managerEntity;
    }
    
    // Helper method to get manager as Manager entity if needed
    public Manager getManagerAsEntity() {
        if (managerEntity != null) {
            return managerEntity;
        }
        // Only attempt conversion if manager exists
        return null;
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
