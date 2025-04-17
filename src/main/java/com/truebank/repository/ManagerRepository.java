package com.truebank.repository;

import com.truebank.model.Manager;
import com.truebank.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {
    
    List<Manager> findByBranch(Branch branch);
    
    Optional<Manager> findByEmail(String email);
    
    Optional<Manager> findByUsername(String username);
    
    Optional<Manager> findByAadharNumber(String aadharNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByAadharNumber(String aadharNumber);
} 