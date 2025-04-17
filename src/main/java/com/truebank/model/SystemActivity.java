package com.truebank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_activities")
public class SystemActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "details")
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "activity_timestamp", nullable = false)
    private LocalDateTime activityTimestamp;

    public enum ActivityType {
        LOGIN, LOGOUT, CREATE, UPDATE, DELETE, VIEW, TRANSACTION, SYSTEM
    }

    public SystemActivity() {
    }

    public SystemActivity(Long id, ActivityType activityType, String description, String details, 
                         String ipAddress, String userAgent, String performedBy, Long userId, 
                         LocalDateTime activityTimestamp) {
        this.id = id;
        this.activityType = activityType;
        this.description = description;
        this.details = details;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.performedBy = performedBy;
        this.userId = userId;
        this.activityTimestamp = activityTimestamp;
    }

    public static SystemActivityBuilder builder() {
        return new SystemActivityBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getActivityTimestamp() {
        return activityTimestamp;
    }

    public void setActivityTimestamp(LocalDateTime activityTimestamp) {
        this.activityTimestamp = activityTimestamp;
    }

    public static class SystemActivityBuilder {
        private Long id;
        private ActivityType activityType;
        private String description;
        private String details;
        private String ipAddress;
        private String userAgent;
        private String performedBy;
        private Long userId;
        private LocalDateTime activityTimestamp = LocalDateTime.now();
        
        public SystemActivityBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public SystemActivityBuilder activityType(ActivityType activityType) {
            this.activityType = activityType;
            return this;
        }
        
        public SystemActivityBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public SystemActivityBuilder details(String details) {
            this.details = details;
            return this;
        }
        
        public SystemActivityBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public SystemActivityBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public SystemActivityBuilder performedBy(String performedBy) {
            this.performedBy = performedBy;
            return this;
        }
        
        public SystemActivityBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }
        
        public SystemActivityBuilder activityTimestamp(LocalDateTime activityTimestamp) {
            this.activityTimestamp = activityTimestamp;
            return this;
        }
        
        public SystemActivityBuilder user(User user) {
            if (user != null) {
                this.userId = user.getId();
                this.performedBy = user.getUsername();
            }
            return this;
        }
        
        public SystemActivity build() {
            return new SystemActivity(id, activityType, description, details, ipAddress, 
                                     userAgent, performedBy, userId, activityTimestamp);
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.activityTimestamp == null) {
            this.activityTimestamp = LocalDateTime.now();
        }
    }
} 