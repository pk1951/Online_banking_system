package com.truebank.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.truebank.model.ERole;
import com.truebank.model.Role;
import com.truebank.model.User;
import com.truebank.repository.RoleRepository;
import com.truebank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SupabaseAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.key}")
    private String supabaseKey;
    
    private WebClient webClient;
    
    /**
     * Initialize the WebClient for Supabase API calls
     */
    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(supabaseUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("apikey", supabaseKey)
                    .defaultHeader("Authorization", "Bearer " + supabaseKey)
                    .build();
        }
        return webClient;
    }
    
    /**
     * Register a new user in Supabase and our database
     */
    public User registerUser(User user, String password) throws Exception {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", user.getEmail());
            requestBody.put("password", password);
            
            // Register user in Supabase
            JsonNode response = getWebClient().post()
                    .uri("/auth/v1/signup")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null && response.has("id")) {
                // Set Supabase UID
                user.setSupabaseUid(response.get("id").asText());
                
                // Encode password for our database
                user.setPassword(passwordEncoder.encode(password));
                
                // Set default role as USER
                Set<Role> roles = new HashSet<>();
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
                roles.add(userRole);
                user.setRoles(roles);
                
                // Save user to our database
                return userRepository.save(user);
            } else {
                throw new Exception("Failed to register user in Supabase");
            }
        } catch (Exception e) {
            // Log the error but allow caller to handle it
            System.out.println("Supabase registration error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Login a user with Supabase
     */
    public String loginUser(String email, String password) throws Exception {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("password", password);
            
            // Login with Supabase
            JsonNode response = getWebClient().post()
                    .uri("/auth/v1/token?grant_type=password")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null && response.has("access_token")) {
                return response.get("access_token").asText();
            } else {
                throw new Exception("Invalid credentials");
            }
        } catch (Exception e) {
            // Log the error but allow caller to handle it
            System.out.println("Supabase login error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Create a manager account
     */
    public User createManager(User manager, String password) throws Exception {
        try {
            User newManager = registerUser(manager, password);
            
            // Add MANAGER role
            Set<Role> roles = newManager.getRoles();
            Role managerRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                    .orElseThrow(() -> new RuntimeException("Error: Role MANAGER is not found."));
            roles.add(managerRole);
            newManager.setRoles(roles);
            
            return userRepository.save(newManager);
        } catch (Exception e) {
            // If Supabase registration fails, create manager locally
            System.out.println("Supabase manager creation failed, creating locally: " + e.getMessage());
            
            // Set password and roles manually
            manager.setPassword(passwordEncoder.encode(password));
            
            // Set roles (USER + MANAGER)
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
            Role managerRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                    .orElseThrow(() -> new RuntimeException("Error: Role MANAGER is not found."));
            roles.add(userRole);
            roles.add(managerRole);
            manager.setRoles(roles);
            
            // Save manager to our database
            return userRepository.save(manager);
        }
    }
    
    /**
     * Create a banker account
     */
    public User createBanker(User banker, String password) throws Exception {
        try {
            User newBanker = registerUser(banker, password);
            
            // Add BANKER role
            Set<Role> roles = newBanker.getRoles();
            Role bankerRole = roleRepository.findByName(ERole.ROLE_BANKER)
                    .orElseThrow(() -> new RuntimeException("Error: Role BANKER is not found."));
            roles.add(bankerRole);
            newBanker.setRoles(roles);
            
            return userRepository.save(newBanker);
        } catch (Exception e) {
            // If Supabase registration fails, create banker locally
            System.out.println("Supabase banker creation failed, creating locally: " + e.getMessage());
            
            // Set password and roles manually
            banker.setPassword(passwordEncoder.encode(password));
            
            // Set roles (USER + BANKER)
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
            Role bankerRole = roleRepository.findByName(ERole.ROLE_BANKER)
                    .orElseThrow(() -> new RuntimeException("Error: Role BANKER is not found."));
            roles.add(userRole);
            roles.add(bankerRole);
            banker.setRoles(roles);
            
            // Save banker to our database
            return userRepository.save(banker);
        }
    }
    
    /**
     * Logout a user
     */
    public void logoutUser(String accessToken) {
        try {
            getWebClient().post()
                    .uri("/auth/v1/logout")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            // Just log the error and continue
            System.out.println("Supabase logout error: " + e.getMessage());
        }
    }
    
    /**
     * Delete a user by ID from both Supabase and local database
     */
    public void deleteUserById(Long userId) {
        try {
            // First get the user to retrieve the Supabase UID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
            // Only attempt to delete from Supabase if the user has a Supabase UID
            if (user.getSupabaseUid() != null && !user.getSupabaseUid().isEmpty()) {
                try {
                    // Delete from Supabase
                    getWebClient().delete()
                            .uri("/auth/v1/admin/users/" + user.getSupabaseUid())
                            .retrieve()
                            .bodyToMono(Void.class)
                            .block();
                } catch (Exception e) {
                    // Log error but continue to delete from local database
                    System.out.println("Supabase user deletion error: " + e.getMessage());
                }
            }
            
            // Delete from our database
            userRepository.deleteById(userId);
        } catch (Exception e) {
            System.out.println("User deletion error: " + e.getMessage());
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }
}
