/**
 * Dashboard authentication utilities for TRUE Bank
 */

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is authenticated
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');
    
    // Add a console log to debug authentication status
    console.log('Authentication check:', { 
        token: token ? 'Present' : 'Missing', 
        user: userStr ? 'Present' : 'Missing',
        path: window.location.pathname
    });
    
    // Special case for login and public pages
    const publicPaths = ['/', '/login', '/register', '/about', '/contact', '/services'];
    if (publicPaths.includes(window.location.pathname)) {
        console.log('On public page, authentication not required');
        return;
    }
    
    // For testing: If on banker dashboard, allow even without auth
    if (window.location.pathname === '/banker/dashboard') {
        console.log('On banker dashboard, proceeding without strict auth check');
        setupLogoutHandlers();
        return;
    }
    
    if (!token || !userStr) {
        console.error('No authentication token found. Redirecting to login.');
        window.location.href = '/login?required=true&page=' + encodeURIComponent(window.location.pathname);
        return;
    }
    
    try {
        // Parse user data
        const user = JSON.parse(userStr);
        console.log('User authenticated:', user.username, 'Roles:', user.roles);
        
        // Check if user has appropriate role for this dashboard
        const currentPath = window.location.pathname;
        let requiredRole = null;
        
        if (currentPath.startsWith('/banker')) {
            requiredRole = 'ROLE_BANKER';
        } else if (currentPath.startsWith('/manager')) {
            requiredRole = 'ROLE_MANAGER';
        } else if (currentPath.startsWith('/user')) {
            requiredRole = 'ROLE_USER';
        }
        
        if (requiredRole && (!user.roles || !user.roles.includes(requiredRole))) {
            console.error(`User does not have required role: ${requiredRole}. Redirecting to appropriate dashboard.`);
            
            // Redirect to appropriate dashboard based on role
            if (user.roles && user.roles.length > 0) {
                if (user.roles.includes('ROLE_BANKER')) {
                    window.location.href = '/banker/dashboard';
                } else if (user.roles.includes('ROLE_MANAGER')) {
                    window.location.href = '/manager/dashboard';
                } else if (user.roles.includes('ROLE_USER')) {
                    window.location.href = '/user/dashboard';
                } else {
                    window.location.href = '/login';
                }
            } else {
                window.location.href = '/login';
            }
            return;
        }
        
        // Update UI with user information
        const usernameElements = document.querySelectorAll('.user-username');
        if (usernameElements) {
            usernameElements.forEach(el => {
                el.textContent = user.username;
            });
        }
        
        // Set up logout functionality
        setupLogoutHandlers();
        
        // Add token to all fetch requests
        const originalFetch = window.fetch;
        window.fetch = function(url, options = {}) {
            options.headers = options.headers || {};
            options.headers['Authorization'] = 'Bearer ' + token;
            return originalFetch(url, options);
        };
        
    } catch (error) {
        console.error('Error parsing user data:', error);
        window.location.href = '/login';
    }
});

function setupLogoutHandlers() {
    const logoutButtons = document.querySelectorAll('.logout-btn');
    if (logoutButtons) {
        logoutButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                logout();
            });
        });
    }
}

// Logout function
function logout() {
    // Clear authentication data
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    
    // Clear cookies
    document.cookie = 'token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    
    // Redirect to login page
    window.location.href = '/login?logout=true';
}
