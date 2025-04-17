/**
 * Authentication utilities for TRUE Bank
 */

// Check if user is logged in
function isLoggedIn() {
    return localStorage.getItem('token') !== null;
}

// Get the current user from localStorage
function getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
}

// Get the JWT token
function getToken() {
    return localStorage.getItem('token');
}

// Add authorization header to fetch requests
function authHeader() {
    const token = getToken();
    return token ? { 'Authorization': 'Bearer ' + token } : {};
}

// Safely parse JSON - returns null if not JSON
function safeParseJSON(response) {
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }
    return Promise.resolve(null);
}

// Handle API response with improved error handling
function handleApiResponse(response) {
    if (response.ok) {
        return safeParseJSON(response);
    } else {
        return safeParseJSON(response)
            .then(data => {
                // If we got JSON data with an error message, use it
                if (data && data.message) {
                    throw new Error(data.message);
                } else {
                    // Otherwise use a generic error with the status
                    throw new Error(`Request failed with status ${response.status}`);
                }
            });
    }
}

// Logout user
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    
    // Clear the token cookie
    document.cookie = 'token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    
    // Redirect to login page
    window.location.href = '/login';
}

// Check user role
function hasRole(role) {
    const user = getCurrentUser();
    return user && user.roles && user.roles.includes(role);
}

// Redirect to appropriate dashboard based on role
function redirectToDashboard() {
    if (!isLoggedIn()) {
        window.location.href = '/login';
        return;
    }
    
    const user = getCurrentUser();
    if (user && user.roles) {
        if (user.roles.includes('ROLE_BANKER')) {
            window.location.href = '/banker/dashboard';
        } else if (user.roles.includes('ROLE_MANAGER')) {
            window.location.href = '/manager/dashboard';
        } else {
            window.location.href = '/user/dashboard';
        }
    }
}

// Add event listener to execute when DOM is fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Add logout event listener to logout buttons
    const logoutButtons = document.querySelectorAll('.logout-btn');
    logoutButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            logout();
        });
    });
    
    // Add token to all fetch requests
    const originalFetch = window.fetch;
    window.fetch = function(url, options = {}) {
        // Add authorization header if token exists
        if (isLoggedIn()) {
            options.headers = options.headers || {};
            options.headers = {
                ...options.headers,
                ...authHeader()
            };
        }
        return originalFetch(url, options);
    };
});
