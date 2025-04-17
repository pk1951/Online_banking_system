package com.truebank.service;

import com.truebank.model.SystemActivity;
import com.truebank.model.User;
import com.truebank.repository.SystemActivityRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemActivityService {

    private final SystemActivityRepository activityRepository;
    private final HttpServletRequest request;

    /**
     * Log system activity with the current user
     */
    public SystemActivity logActivity(String description, SystemActivity.ActivityType activityType, User user) {
        SystemActivity activity = SystemActivity.builder()
                .description(description)
                .activityType(activityType)
                .user(user)
                .ipAddress(getClientIpAddress())
                .build();
        
        return activityRepository.save(activity);
    }
    
    /**
     * Log system activity without user (system generated)
     */
    public SystemActivity logSystemActivity(String description, SystemActivity.ActivityType activityType) {
        SystemActivity activity = SystemActivity.builder()
                .description(description)
                .activityType(activityType)
                .ipAddress(getClientIpAddress())
                .build();
        
        return activityRepository.save(activity);
    }
    
    /**
     * Get all activities
     */
    public List<SystemActivity> getAllActivities() {
        return activityRepository.findAllOrderByActivityTimestampDesc();
    }
    
    /**
     * Get activities by user
     */
    public List<SystemActivity> getActivitiesByUser(User user) {
        return activityRepository.findByUserId(user.getId());
    }
    
    /**
     * Get recent activities
     */
    public List<SystemActivity> getRecentActivities(int limit) {
        return activityRepository.findRecentActivities(limit);
    }
    
    /**
     * Get activities by type
     */
    public List<SystemActivity> getActivitiesByType(SystemActivity.ActivityType activityType) {
        return activityRepository.findByActivityType(activityType);
    }
    
    /**
     * Get activities created after specific date
     */
    public List<SystemActivity> getActivitiesAfterDate(LocalDateTime startDate) {
        return activityRepository.findByActivityTimestampAfter(startDate);
    }
    
    /**
     * Get activities created before specific date
     */
    public List<SystemActivity> getActivitiesBeforeDate(LocalDateTime endDate) {
        return activityRepository.findByActivityTimestampBefore(endDate);
    }
    
    /**
     * Get activities between dates
     */
    public List<SystemActivity> getActivitiesBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return activityRepository.findByActivityTimestampBetween(startDate, endDate);
    }
    
    /**
     * Get activities by user and type
     */
    public List<SystemActivity> getActivitiesByUserAndType(User user, SystemActivity.ActivityType activityType) {
        return activityRepository.findByUserIdAndActivityType(user.getId(), activityType);
    }
    
    /**
     * Get client IP address
     */
    private String getClientIpAddress() {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
} 