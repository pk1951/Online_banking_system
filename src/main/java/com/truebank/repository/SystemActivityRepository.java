package com.truebank.repository;

import com.truebank.model.SystemActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemActivityRepository extends JpaRepository<SystemActivity, Long> {

    List<SystemActivity> findByUserId(Long userId);
    
    List<SystemActivity> findByActivityType(SystemActivity.ActivityType activityType);
    
    List<SystemActivity> findByUserIdAndActivityType(Long userId, SystemActivity.ActivityType activityType);
    
    List<SystemActivity> findByActivityTimestampBefore(LocalDateTime endDate);
    
    List<SystemActivity> findByActivityTimestampAfter(LocalDateTime startDate);
    
    List<SystemActivity> findByActivityTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT a FROM SystemActivity a ORDER BY a.activityTimestamp DESC")
    List<SystemActivity> findAllOrderByActivityTimestampDesc();
    
    @Query(value = "SELECT * FROM system_activities ORDER BY activity_timestamp DESC LIMIT ?1", nativeQuery = true)
    List<SystemActivity> findRecentActivities(int limit);
    
    @Query(value = "SELECT * FROM system_activities WHERE user_id = ?1 ORDER BY activity_timestamp DESC LIMIT ?2", nativeQuery = true)
    List<SystemActivity> findRecentActivitiesByUser(Long userId, int limit);
    
    @Query(value = "SELECT * FROM system_activities WHERE activity_type = ?1 ORDER BY activity_timestamp DESC LIMIT ?2", nativeQuery = true)
    List<SystemActivity> findRecentActivitiesByType(String activityType, int limit);
}