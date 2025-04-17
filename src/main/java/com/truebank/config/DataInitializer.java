package com.truebank.config;

import com.truebank.model.ERole;
import com.truebank.model.Role;
import com.truebank.model.User;
import com.truebank.repository.RoleRepository;
import com.truebank.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.truebank.model.Branch;
import com.truebank.repository.BranchRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private BranchRepository branchRepository;

    @PostConstruct
    @Transactional
    public void init() {
        // Create roles if they don't exist
        createRoleIfNotFound(ERole.ROLE_USER);
        createRoleIfNotFound(ERole.ROLE_MANAGER);
        createRoleIfNotFound(ERole.ROLE_BANKER);

        // Create default banker account if it doesn't exist
        if (!userRepository.existsByUsername("banker")) {
            createBankerAccount();
        } else {
            // Reset password for existing banker account
            resetUserPassword("banker", "banker123");
        }
        
        // Create a test manager account if it doesn't exist
        if (!userRepository.existsByUsername("manager")) {
            createManagerAccount();
        } else {
            // Reset password for existing manager account
            resetUserPassword("manager", "manager123");
        }
        
        // Create a test user account if it doesn't exist
        if (!userRepository.existsByUsername("user")) {
            createUserAccount();
        } else {
            // Reset password for existing user account
            resetUserPassword("user", "user123");
        }
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Add the 5 branches specified if they don't exist
        addBranchIfNotExists("002", "Adugodi", "Adugodi Bangalore", "Bangalore", "Karnataka", "560030", "9087654002");
        addBranchIfNotExists("003", "Madivala", "Madivala Bangalore", "Bangalore", "Karnataka", "560032", "9087654003");
        addBranchIfNotExists("004", "PES University", "Konapa Agrahara Bangalore", "Bangalore", "Karnataka", "560031", "9087654004");
        addBranchIfNotExists("005", "Jayanagar", "Jayanagar 4th Block Bangalore", "Bangalore", "Karnataka", "560041", "9087654005");
        addBranchIfNotExists("006", "Electronic City", "Electronic City Phase 1 Bangalore", "Bangalore", "Karnataka", "560100", "9087654006");
    }

    private void createBankerAccount() {
        User banker = new User();
        banker.setUsername("banker");
        banker.setPassword(passwordEncoder.encode("banker123"));
        banker.setEmail("banker@truebank.com");
        banker.setFirstName("TRUE");
        banker.setLastName("Banker");
        banker.setPhoneNumber("9087654321");
        banker.setAadharNumber("123456789012");
        banker.setAddress("PES University EC Campus");
        banker.setAge(30);
        banker.setActive(true);
        banker.setCreatedAt(LocalDateTime.now());

        // Set BANKER role (owner status)
        Set<Role> roles = new HashSet<>();
        Role bankerRole = roleRepository.findByName(ERole.ROLE_BANKER)
                .orElseThrow(() -> new RuntimeException("Error: Role BANKER is not found."));
        roles.add(bankerRole);
        banker.setRoles(roles);

        userRepository.save(banker);
        System.out.println("Default banker account created with username: banker and password: banker123");
    }

    private void createManagerAccount() {
        User manager = new User();
        manager.setUsername("manager");
        manager.setPassword(passwordEncoder.encode("manager123"));
        manager.setEmail("manager@truebank.com");
        manager.setFirstName("Branch");
        manager.setLastName("Manager");
        manager.setPhoneNumber("9876543210");
        manager.setAadharNumber("987654321012");
        manager.setAddress("PES University EC Campus");
        manager.setAge(28);
        manager.setActive(true);
        manager.setCreatedAt(LocalDateTime.now());

        // Set MANAGER role
        Set<Role> roles = new HashSet<>();
        Role managerRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                .orElseThrow(() -> new RuntimeException("Error: Role MANAGER is not found."));
        roles.add(managerRole);
        manager.setRoles(roles);

        userRepository.save(manager);
        System.out.println("Test manager account created with username: manager and password: manager123");
    }

    private void createUserAccount() {
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setEmail("user@truebank.com");
        user.setFirstName("Regular");
        user.setLastName("User");
        user.setPhoneNumber("8765432109");
        user.setAadharNumber("876543210987");
        user.setAddress("PES University EC Campus");
        user.setAge(25);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        // Set USER role
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);
        System.out.println("Test user account created with username: user and password: user123");
    }

    private void resetUserPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found: " + username));
        
        // Check if the password is already BCrypt encoded
        if (!user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            System.out.println("Password reset for user: " + username);
        }
    }

    private void createRoleIfNotFound(ERole name) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role(name);
            roleRepository.save(role);
            System.out.println("Created role: " + name);
        }
    }

    private void addBranchIfNotExists(String branchCode, String name, String address, String city, String state, String zipCode, String phoneNumber) {
        // Check if branch with this code already exists
        if (!branchRepository.existsByBranchCode(branchCode)) {
            Branch branch = new Branch();
            branch.setBranchCode(branchCode);
            branch.setName(name);
            branch.setAddress(address);
            branch.setCity(city);
            branch.setState(state);
            branch.setZipCode(zipCode);
            branch.setPhoneNumber(phoneNumber);
            branch.setActive(true);
            branch.setCreatedAt(LocalDateTime.now());
            
            branchRepository.save(branch);
            log.info("Added branch: {}", name);
        } else {
            log.info("Branch with code {} already exists, skipping", branchCode);
        }
    }
}
