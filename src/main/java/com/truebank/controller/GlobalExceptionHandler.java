package com.truebank.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        logger.error("Unhandled exception occurred: {}", e.getMessage(), e);
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error");
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + e.getMessage());
        modelAndView.addObject("stackTrace", e.getStackTrace());
        
        return modelAndView;
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException e) {
        logger.error("Access denied: {}", e.getMessage(), e);
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("errorMessage", "You need to log in as a Banker to access this page");
        
        return modelAndView;
    }
    
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ModelAndView handleAuthenticationException(AuthenticationCredentialsNotFoundException e) {
        logger.error("Authentication credentials not found: {}", e.getMessage(), e);
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("errorMessage", "Please log in to access this feature");
        
        return modelAndView;
    }
} 