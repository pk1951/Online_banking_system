package com.truebank.dto;

public class AccountCreationRequest {
    private Long userId;
    private Long branchId;
    private String accountType;
    private String initialDeposit;
    private String branchCode; // For user-initiated account creation
    private String tenure; // For fixed deposit accounts
    
    public AccountCreationRequest() {
    }
    
    public AccountCreationRequest(Long userId, Long branchId, String accountType, String initialDeposit,
                                 String branchCode, String tenure) {
        this.userId = userId;
        this.branchId = branchId;
        this.accountType = accountType;
        this.initialDeposit = initialDeposit;
        this.branchCode = branchCode;
        this.tenure = tenure;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getBranchId() {
        return branchId;
    }
    
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public String getInitialDeposit() {
        return initialDeposit;
    }
    
    public void setInitialDeposit(String initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
    
    public String getBranchCode() {
        return branchCode;
    }
    
    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }
    
    public String getTenure() {
        return tenure;
    }
    
    public void setTenure(String tenure) {
        this.tenure = tenure;
    }
}
