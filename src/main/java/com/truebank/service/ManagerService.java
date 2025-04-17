package com.truebank.service;

import com.truebank.model.Manager;
import com.truebank.dto.ManagerCreationRequest;

import java.util.List;

public interface ManagerService {
    
    /**
     * Get all managers
     * @return List of all managers
     */
    List<Manager> getAllManagers();
    
    /**
     * Get manager by ID
     * @param id Manager ID
     * @return Manager entity
     */
    Manager getManagerById(Long id);
    
    /**
     * Create a new manager
     * @param managerRequest Manager creation request data
     * @return Created manager entity
     */
    Manager createManager(ManagerCreationRequest managerRequest);
    
    /**
     * Update manager details
     * @param id Manager ID
     * @param managerRequest Updated manager data
     * @return Updated manager entity
     */
    Manager updateManager(Long id, ManagerCreationRequest managerRequest);
    
    /**
     * Delete manager
     * @param id Manager ID
     */
    void deleteManager(Long id);
} 