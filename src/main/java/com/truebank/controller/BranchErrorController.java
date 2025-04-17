package com.truebank.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice(assignableTypes = {BankerViewController.class})
public class BranchErrorController {

    private static final Logger logger = LoggerFactory.getLogger(BranchErrorController.class);
    
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericBranchError(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        logger.error("Branch operation error for URI {}: {}", requestURI, e.getMessage(), e);
        
        ModelAndView modelAndView = new ModelAndView();
        if (requestURI.contains("/branches/create")) {
            modelAndView.setViewName("banker/create-branch");
            modelAndView.addObject("branchRequest", new com.truebank.dto.BranchCreationRequest());
            modelAndView.addObject("errorMessage", "Error creating branch: " + e.getMessage());
        } else {
            modelAndView.setViewName("banker/branches");
            modelAndView.addObject("errorMessage", "Branch operation failed: " + e.getMessage());
        }
        
        return modelAndView;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleBranchNotFound(EntityNotFoundException e) {
        logger.error("Branch not found: {}", e.getMessage());
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("banker/branches");
        modelAndView.addObject("errorMessage", "Branch not found: " + e.getMessage());
        
        return modelAndView;
    }
    
    /**
     * Handle validation errors specifically for branch creation
     */
    @ExceptionHandler(BindException.class)
    public ModelAndView handleBindException(BindException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        logger.error("Validation error for URI {}: {}", requestURI, e.getMessage());
        
        ModelAndView modelAndView = new ModelAndView();
        if (requestURI.contains("/branches/create")) {
            modelAndView.setViewName("banker/create-branch");
            // Keep the submitted data
            modelAndView.addObject("branchRequest", e.getTarget());
            modelAndView.addObject("errorMessage", "Please fix the validation errors.");
            // Add binding result for field errors
            modelAndView.addObject("org.springframework.validation.BindingResult.branchRequest", e.getBindingResult());
        } else {
            modelAndView.setViewName("banker/branches");
            modelAndView.addObject("errorMessage", "Validation error: " + e.getMessage());
        }
        
        return modelAndView;
    }
    
    @GetMapping("/banker/branches/error")
    public String branchErrorPage(Model model, HttpServletRequest request) {
        String errorMessage = request.getParameter("message");
        model.addAttribute("errorMessage", errorMessage != null ? errorMessage : "An error occurred with branch operations");
        return "banker/branch-error";
    }
} 