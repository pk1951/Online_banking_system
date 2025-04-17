package com.truebank.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthViewController {

    private static final Logger logger = LoggerFactory.getLogger(AuthViewController.class);

    /**
     * Login page with custom messages
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String required,
            @RequestParam(required = false) String redirect,
            Model model) {
        
        logger.info("Login page accessed with params - error: {}, logout: {}, required: {}, redirect: {}", 
                error, logout, required, redirect);
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        
        if (required != null) {
            model.addAttribute("required", "You must log in to access this feature");
            if (redirect != null) {
                model.addAttribute("redirect", redirect);
            }
        }
        
        return "login";
    }
} 