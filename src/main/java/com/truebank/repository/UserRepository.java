package com.truebank.repository;

import com.truebank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    Optional<User> findByAadharNumber(String aadharNumber);
    
    Optional<User> findBySupabaseUid(String supabaseUid);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    Boolean existsByPhoneNumber(String phoneNumber);
    
    Boolean existsByAadharNumber(String aadharNumber);
    
    List<User> findByRoles_Name(String roleName);
    
    Long countByRoles_Name(String roleName);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_MANAGER' AND u.id NOT IN (SELECT b.manager.id FROM Branch b WHERE b.manager IS NOT NULL)")
    List<User> findManagersWithoutBranch();
}
