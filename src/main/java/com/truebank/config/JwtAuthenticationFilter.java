package com.truebank.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.truebank.security.services.UserDetailsServiceImpl;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        
        // Skip JWT authentication for public endpoints
        if (requestURI.equals("/") || 
            requestURI.startsWith("/css/") || 
            requestURI.startsWith("/js/") || 
            requestURI.startsWith("/images/") || 
            requestURI.startsWith("/webjars/") || 
            requestURI.startsWith("/login") || 
            requestURI.startsWith("/register") || 
            requestURI.startsWith("/about") || 
            requestURI.startsWith("/contact") || 
            requestURI.startsWith("/services") || 
            requestURI.startsWith("/error") || 
            requestURI.startsWith("/api/auth/") || 
            requestURI.startsWith("/h2-console") || 
            requestURI.startsWith("/banker/branches")) {
            
            logger.debug("Skipping JWT authentication for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);
            
            logger.debug("Processing request: {}", requestURI);
            if (jwt != null && jwt.length() > 0) {
                logger.debug("Found JWT token, attempting to validate");
            } else {
                logger.debug("No JWT token found for request: {}", requestURI);
            }
            
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.debug("Username from JWT: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("User authenticated via JWT: {}", username);
                
                filterChain.doFilter(request, response);
            } else {
                // For debugging - log unauthenticated requests
                logger.debug("Unauthenticated request to: {}", requestURI);
                
                // DON'T redirect for AJAX or API requests
                if (isAjaxRequest(request) || requestURI.startsWith("/api/")) {
                    logger.debug("AJAX or API request without authentication, returning 401");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                    return;
                }
                
                // Only redirect for UI paths that require authentication
                // But DON'T redirect if already on login-related pages
                if ((requestURI.startsWith("/banker") || requestURI.startsWith("/manager") || requestURI.startsWith("/user")) && 
                    !requestURI.equals("/login") && 
                    !requestURI.contains("/error")) {
                    logger.debug("UI request without authentication, redirecting to login page");
                    response.sendRedirect("/login?required=true&redirect=" + requestURI);
                    return;
                }
                
                // Let Spring Security handle other cases
                logger.debug("Letting filter chain continue for request: {}", requestURI);
                filterChain.doFilter(request, response);
            }
        } catch (Exception e) {
            logger.error("Authentication error for request {}: {}", requestURI, e.getMessage());
            
            // Don't send error response for favicon or static resources
            if (requestURI.contains("favicon.ico") || 
                requestURI.startsWith("/css") || 
                requestURI.startsWith("/js") || 
                requestURI.startsWith("/images") || 
                requestURI.startsWith("/webjars")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // For banker routes, provide a more specific error
            if (requestURI.startsWith("/banker")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.sendRedirect("/login?error=session_expired&redirect=" + requestURI);
                return;
            }
            
            // Continue the chain on error for other cases
            filterChain.doFilter(request, response);
        }
    }

    private String parseJwt(HttpServletRequest request) {
        // Try to get token from Authorization header
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        
        // If not in header, try to get from cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // If not in cookies, try to get from request parameter
        String paramToken = request.getParameter("token");
        if (StringUtils.hasText(paramToken)) {
            return paramToken;
        }
        
        return null;
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}
