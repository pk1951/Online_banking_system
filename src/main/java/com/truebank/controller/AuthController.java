package com.truebank.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.truebank.config.JwtUtils;
import com.truebank.dto.JwtResponse;
import com.truebank.dto.LoginRequest;
import com.truebank.dto.MessageResponse;
import com.truebank.dto.SignupRequest;
import com.truebank.model.ERole;
import com.truebank.model.Role;
import com.truebank.model.User;
import com.truebank.repository.RoleRepository;
import com.truebank.repository.UserRepository;
import com.truebank.service.SupabaseAuthService;
import com.truebank.security.services.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SupabaseAuthService supabaseAuthService;
    private final JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Try to authenticate with Supabase only if email is provided
            String supabaseToken = null;
            if (loginRequest.getEmail() != null && !loginRequest.getEmail().isEmpty()) {
                try {
                    supabaseToken = supabaseAuthService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
                } catch (Exception e) {
                    // Log the error but continue with local authentication
                    System.out.println("Supabase authentication failed: " + e.getMessage());
                }
            }
            
            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Determine dashboard URL based on role
            String dashboardUrl = "/user/dashboard";
            if (roles.contains("ROLE_BANKER")) {
                dashboardUrl = "/banker/dashboard";
            } else if (roles.contains("ROLE_MANAGER")) {
                dashboardUrl = "/manager/dashboard";
            }

            return ResponseEntity.ok(new JwtResponse(
                    jwt, 
                    supabaseToken,
                    userDetails.getId(), 
                    userDetails.getUsername(), 
                    userDetails.getEmail(), 
                    roles,
                    dashboardUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // Check if email is already in use
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        
        // Check if phone number is already in use
        if (userRepository.existsByPhoneNumber(signUpRequest.getPhoneNumber())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Phone number is already in use!"));
        }
        
        // Check if Aadhar number is already in use
        if (userRepository.existsByAadharNumber(signUpRequest.getAadharNumber())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Aadhar number is already in use!"));
        }

        // Validate Aadhar number is 12 digits
        if (!signUpRequest.getAadharNumber().matches("\\d{12}")) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Aadhar number must be 12 digits!"));
        }

        // Create new user
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setAge(signUpRequest.getAge());
        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user.setAadharNumber(signUpRequest.getAadharNumber());
        user.setAddress(signUpRequest.getAddress());

        try {
            // Try to register with Supabase, but continue even if it fails
            User registeredUser = null;
            try {
                registeredUser = supabaseAuthService.registerUser(user, signUpRequest.getPassword());
            } catch (Exception e) {
                // Log the error but continue with local registration
                System.out.println("Supabase registration failed: " + e.getMessage());
                
                // Set password and roles manually
                user.setPassword(new BCryptPasswordEncoder().encode(signUpRequest.getPassword()));
                
                // Set default role as USER
                Set<Role> roles = new HashSet<>();
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
                roles.add(userRole);
                user.setRoles(roles);
                
                // Save user to our database
                registeredUser = userRepository.save(user);
            }
            
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam String username) {
        boolean exists = userRepository.existsByUsername(username);
        return ResponseEntity.ok(new MessageResponse(exists ? "Username is already taken" : "Username is available"));
    }
    
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(new MessageResponse(exists ? "Email is already in use" : "Email is available"));
    }
    
    @GetMapping("/check-phone")
    public ResponseEntity<?> checkPhoneAvailability(@RequestParam String phoneNumber) {
        boolean exists = userRepository.existsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(new MessageResponse(exists ? "Phone number is already in use" : "Phone number is available"));
    }
    
    @GetMapping("/check-aadhar")
    public ResponseEntity<?> checkAadharAvailability(@RequestParam String aadharNumber) {
        boolean exists = userRepository.existsByAadharNumber(aadharNumber);
        return ResponseEntity.ok(new MessageResponse(exists ? "Aadhar number is already in use" : "Aadhar number is available"));
    }
}
