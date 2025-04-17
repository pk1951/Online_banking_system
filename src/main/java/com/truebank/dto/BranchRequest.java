package com.truebank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class BranchRequest {
    @NotBlank
    private String name;
    
    @NotBlank
    @Pattern(regexp = "\\d{3}", message = "Branch code must be 3 digits")
    private String branchCode;
    
    @NotBlank
    private String address;
    
    @NotBlank
    private String city;
    
    @NotBlank
    private String state;
    
    @NotBlank
    private String zipCode;
    
    @NotBlank
    @Pattern(regexp = "\\d{10,12}", message = "Phone number must be between 10 and 12 digits")
    private String phoneNumber;
    
    public BranchRequest() {
    }
    
    public BranchRequest(String name, String branchCode, String address, String city, 
                        String state, String zipCode, String phoneNumber) {
        this.name = name;
        this.branchCode = branchCode;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.phoneNumber = phoneNumber;
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
} 