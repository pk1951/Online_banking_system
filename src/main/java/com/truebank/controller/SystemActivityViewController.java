package com.truebank.controller;

import com.truebank.model.SystemActivity;
import com.truebank.service.SystemActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/banker/activities")
@RequiredArgsConstructor
public class SystemActivityViewController {

    private final SystemActivityService systemActivityService;

    /**
     * View all system activities
     */
    @GetMapping
    @PreAuthorize("hasRole('BANKER')")
    public String viewActivities(Model model) {
        List<SystemActivity> activities = systemActivityService.getAllActivities();
        model.addAttribute("activities", activities);
        return "banker/activities";
    }
} 