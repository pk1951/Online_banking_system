package com.truebank.controller.api;

import com.truebank.dto.ApiResponse;
import com.truebank.model.SystemActivity;
import com.truebank.model.User;
import com.truebank.service.SystemActivityService;
import com.truebank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class SystemActivityController {

    private final SystemActivityService systemActivityService;
    private final UserService userService;

    /**
     * Get all system activities
     */
    @GetMapping
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<ApiResponse> getAllActivities() {
        try {
            List<SystemActivity> activities = systemActivityService.getAllActivities();
            return ResponseEntity.ok(new ApiResponse(true, "Activities retrieved successfully", activities));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Get recent system activities with limit
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<ApiResponse> getRecentActivities(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<SystemActivity> activities = systemActivityService.getRecentActivities(limit);
            return ResponseEntity.ok(new ApiResponse(true, "Recent activities retrieved successfully", activities));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Get activities by user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<ApiResponse> getActivitiesByUser(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            List<SystemActivity> activities = systemActivityService.getActivitiesByUser(user);
            return ResponseEntity.ok(new ApiResponse(true, "User activities retrieved successfully", activities));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Get activities by type
     */
    @GetMapping("/type/{activityType}")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<ApiResponse> getActivitiesByType(@PathVariable String activityType) {
        try {
            SystemActivity.ActivityType type = SystemActivity.ActivityType.valueOf(activityType.toUpperCase());
            List<SystemActivity> activities = systemActivityService.getActivitiesByType(type);
            return ResponseEntity.ok(new ApiResponse(true, "Activities filtered by type retrieved successfully", activities));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid activity type", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Get activities by date range
     */
    @GetMapping("/dateRange")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<ApiResponse> getActivitiesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<SystemActivity> activities = systemActivityService.getActivitiesBetweenDates(startDate, endDate);
            return ResponseEntity.ok(new ApiResponse(true, "Activities filtered by date range retrieved successfully", activities));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Get activities with combined filters
     */
    @GetMapping("/filter")
    @PreAuthorize("hasRole('BANKER')")
    public ResponseEntity<ApiResponse> getActivitiesWithFilters(
            @RequestParam(required = false) String activityType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<SystemActivity> activities;
            
            // Apply filters using repository methods where possible
            if (activityType != null && !activityType.isEmpty() && userId != null) {
                // Filter by both user and activity type
                User user = userService.getUserById(userId);
                SystemActivity.ActivityType type = SystemActivity.ActivityType.valueOf(activityType.toUpperCase());
                activities = systemActivityService.getActivitiesByUserAndType(user, type);
            } else if (activityType != null && !activityType.isEmpty()) {
                // Filter by activity type only
                SystemActivity.ActivityType type = SystemActivity.ActivityType.valueOf(activityType.toUpperCase());
                activities = systemActivityService.getActivitiesByType(type);
            } else if (userId != null) {
                // Filter by user only
                User user = userService.getUserById(userId);
                activities = systemActivityService.getActivitiesByUser(user);
            } else {
                // No user or activity type filter
                activities = systemActivityService.getAllActivities();
            }
            
            // Apply date filters
            if (startDate != null && endDate != null) {
                // Both start and end date provided
                List<SystemActivity> dateFilteredActivities = systemActivityService.getActivitiesBetweenDates(startDate, endDate);
                // Retain only activities that pass both filters
                activities.retainAll(dateFilteredActivities);
            } else if (startDate != null) {
                // Only start date provided
                List<SystemActivity> dateFilteredActivities = systemActivityService.getActivitiesAfterDate(startDate);
                activities.retainAll(dateFilteredActivities);
            } else if (endDate != null) {
                // Only end date provided
                List<SystemActivity> dateFilteredActivities = systemActivityService.getActivitiesBeforeDate(endDate);
                activities.retainAll(dateFilteredActivities);
            }
            
            return ResponseEntity.ok(new ApiResponse(true, "Filtered activities retrieved successfully", activities));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid filter parameter", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
} 