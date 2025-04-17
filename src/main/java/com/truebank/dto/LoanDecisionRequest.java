package com.truebank.dto;

import jakarta.validation.constraints.NotBlank;

public class LoanDecisionRequest {
    private boolean approved;
    
    @NotBlank
    private String remarks;
    
    public LoanDecisionRequest() {
    }
    
    public LoanDecisionRequest(boolean approved, String remarks) {
        this.approved = approved;
        this.remarks = remarks;
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
