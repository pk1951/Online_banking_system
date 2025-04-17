package com.truebank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ProfileUpdateRequest {
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    private int age;
    
    @NotBlank
    @Pattern(regexp = "\\d{10,12}", message = "Phone number must be between 10 and 12 digits")
    private String phoneNumber;
    
    @NotBlank
    private String address;
    
    public ProfileUpdateRequest() {
    }
    
    public ProfileUpdateRequest(String firstName, String lastName, int age, String phoneNumber, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
}
