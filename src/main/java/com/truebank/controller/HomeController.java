package com.truebank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Return the homepage (index) instead of redirecting to banker dashboard
        return "index";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    @GetMapping("/services")
    public String services() {
        return "services";
    }
    
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
    
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    @GetMapping("/banker")
    public String bankerRedirect() {
        return "redirect:/banker/dashboard";
    }
    
    // Removed the conflicting dashboard mappings since they are handled by specific controllers
    // These were causing ambiguous mapping errors
}
