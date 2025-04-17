package com.truebank.repository;

import com.truebank.model.Branch;
import com.truebank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    
    Optional<Branch> findByBranchCode(String branchCode);
    
    List<Branch> findByActive(boolean active);
    
    Optional<Branch> findByManager(User manager);
    
    Boolean existsByBranchCode(String branchCode);
    
    @Query(value = "SELECT * FROM branches ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<Branch> findRecentBranches(@Param("limit") int limit);

    List<Branch> findByActiveTrue();
    
    List<Branch> findByManagerIsNull();
}
