package com.truebank.dto;

import java.util.List;

public class JwtResponse {
    private String token;
    private String supabaseToken;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private String dashboardUrl;

    // Default constructor
    public JwtResponse() {
    }

    // Constructor with all fields
    public JwtResponse(String token, String supabaseToken, String type, Long id, String username, String email, List<String> roles, String dashboardUrl) {
        this.token = token;
        this.supabaseToken = supabaseToken;
        this.type = type;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.dashboardUrl = dashboardUrl;
    }

    public JwtResponse(String token, String supabaseToken, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.supabaseToken = supabaseToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.dashboardUrl = "/user/dashboard"; // Default dashboard URL
    }
    
    public JwtResponse(String token, String supabaseToken, Long id, String username, String email, List<String> roles, String dashboardUrl) {
        this.token = token;
        this.supabaseToken = supabaseToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.dashboardUrl = dashboardUrl;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSupabaseToken() {
        return supabaseToken;
    }

    public void setSupabaseToken(String supabaseToken) {
        this.supabaseToken = supabaseToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }
}
