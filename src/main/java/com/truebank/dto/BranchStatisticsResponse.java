package com.truebank.dto;

public class BranchStatisticsResponse {
    private Long branchId;
    private String branchName;
    private String branchCode;
    private int totalAccounts;
    private int totalTransactions;
    private int totalLoans;
    private int activeSavingsAccounts;
    private int activeCurrentAccounts;
    private int activeFixedDepositAccounts;
    private int pendingLoans;
    private int approvedLoans;
    private int rejectedLoans;
    private int disbursedLoans;
    private double totalDeposits;
    private double totalWithdrawals;
    private double totalTransfers;
    private double totalLoanAmount;
    
    public BranchStatisticsResponse() {
    }
    
    public Long getBranchId() {
        return branchId;
    }
    
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
    
    public String getBranchName() {
        return branchName;
    }
    
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
    
    public String getBranchCode() {
        return branchCode;
    }
    
    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }
    
    public int getTotalAccounts() {
        return totalAccounts;
    }
    
    public void setTotalAccounts(int totalAccounts) {
        this.totalAccounts = totalAccounts;
    }
    
    public int getTotalTransactions() {
        return totalTransactions;
    }
    
    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
    
    public int getTotalLoans() {
        return totalLoans;
    }
    
    public void setTotalLoans(int totalLoans) {
        this.totalLoans = totalLoans;
    }
    
    public int getActiveSavingsAccounts() {
        return activeSavingsAccounts;
    }
    
    public void setActiveSavingsAccounts(int activeSavingsAccounts) {
        this.activeSavingsAccounts = activeSavingsAccounts;
    }
    
    public int getActiveCurrentAccounts() {
        return activeCurrentAccounts;
    }
    
    public void setActiveCurrentAccounts(int activeCurrentAccounts) {
        this.activeCurrentAccounts = activeCurrentAccounts;
    }
    
    public int getActiveFixedDepositAccounts() {
        return activeFixedDepositAccounts;
    }
    
    public void setActiveFixedDepositAccounts(int activeFixedDepositAccounts) {
        this.activeFixedDepositAccounts = activeFixedDepositAccounts;
    }
    
    public int getPendingLoans() {
        return pendingLoans;
    }
    
    public void setPendingLoans(int pendingLoans) {
        this.pendingLoans = pendingLoans;
    }
    
    public int getApprovedLoans() {
        return approvedLoans;
    }
    
    public void setApprovedLoans(int approvedLoans) {
        this.approvedLoans = approvedLoans;
    }
    
    public int getRejectedLoans() {
        return rejectedLoans;
    }
    
    public void setRejectedLoans(int rejectedLoans) {
        this.rejectedLoans = rejectedLoans;
    }
    
    public int getDisbursedLoans() {
        return disbursedLoans;
    }
    
    public void setDisbursedLoans(int disbursedLoans) {
        this.disbursedLoans = disbursedLoans;
    }
    
    public double getTotalDeposits() {
        return totalDeposits;
    }
    
    public void setTotalDeposits(double totalDeposits) {
        this.totalDeposits = totalDeposits;
    }
    
    public double getTotalWithdrawals() {
        return totalWithdrawals;
    }
    
    public void setTotalWithdrawals(double totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }
    
    public double getTotalTransfers() {
        return totalTransfers;
    }
    
    public void setTotalTransfers(double totalTransfers) {
        this.totalTransfers = totalTransfers;
    }
    
    public double getTotalLoanAmount() {
        return totalLoanAmount;
    }
    
    public void setTotalLoanAmount(double totalLoanAmount) {
        this.totalLoanAmount = totalLoanAmount;
    }
}
