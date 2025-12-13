/* ========== COMMON APP UTILITIES ========== */

/**
 * Go back to previous page
 */
function goBack() {
    window.history.back();
}

/**
 * Navigate to page
 * @param {string} path 
 */
function navigateTo(path) {
    window.location.href = path;
}

/**
 * Go to profile page
 */
function goToProfile() {
    // This will be implemented when profile page is created
    console.log('Navigating to profile...');
    // navigateTo('/templates/profile.html');
}

/**
 * Logout user
 */
function logout() {
    if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
        // Clear session/localStorage
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        // Redirect to home
        navigateTo('/');
    }
}

/**
 * Store user data in localStorage
 * @param {object} userData 
 */
function saveUserData(userData) {
    localStorage.setItem('userData', JSON.stringify(userData));
}

/**
 * Get user data from localStorage
 * @returns {object|null}
 */
function getUserData() {
    const data = localStorage.getItem('userData');
    return data ? JSON.parse(data) : null;
}

/**
 * Save auth token
 * @param {string} token 
 */
function saveAuthToken(token) {
    localStorage.setItem('authToken', token);
}

/**
 * Get auth token
 * @returns {string|null}
 */
function getAuthToken() {
    return localStorage.getItem('authToken');
}

/**
 * Check if user is authenticated
 * @returns {boolean}
 */
function isAuthenticated() {
    return !!getAuthToken();
}

/**
 * API helper - GET request
 * @param {string} url 
 * @returns {Promise}
 */
async function apiGet(url) {
    try {
        const headers = {
            'Content-Type': 'application/json'
        };
        
        const token = getAuthToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(url, {
            method: 'GET',
            headers: headers
        });

        if (!response.ok) {
            throw new Error(`API Error: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API GET Error:', error);
        throw error;
    }
}

/**
 * API helper - POST request
 * @param {string} url 
 * @param {object} data 
 * @returns {Promise}
 */
async function apiPost(url, data) {
    try {
        const headers = {
            'Content-Type': 'application/json'
        };

        const token = getAuthToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(url, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(data)
        });

        const responseData = await response.json();

        if (!response.ok) {
            throw new Error(responseData.message || `API Error: ${response.status}`);
        }

        return responseData;
    } catch (error) {
        console.error('API POST Error:', error);
        throw error;
    }
}

/**
 * API helper - PUT request
 * @param {string} url 
 * @param {object} data 
 * @returns {Promise}
 */
async function apiPut(url, data) {
    try {
        const headers = {
            'Content-Type': 'application/json'
        };

        const token = getAuthToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(url, {
            method: 'PUT',
            headers: headers,
            body: JSON.stringify(data)
        });

        const responseData = await response.json();

        if (!response.ok) {
            throw new Error(responseData.message || `API Error: ${response.status}`);
        }

        return responseData;
    } catch (error) {
        console.error('API PUT Error:', error);
        throw error;
    }
}

/**
 * Debounce helper
 * @param {function} func 
 * @param {number} wait 
 * @returns {function}
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * Throttle helper
 * @param {function} func 
 * @param {number} limit 
 * @returns {function}
 */
function throttle(func, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

/**
 * Get query parameter
 * @param {string} name 
 * @returns {string|null}
 */
function getQueryParam(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

/**
 * Local storage with expiration
 */
const StorageWithExpiry = {
    setItem(key, value, expirationMinutes = null) {
        const item = {
            value: value,
            expiration: expirationMinutes ? Date.now() + (expirationMinutes * 60 * 1000) : null
        };
        localStorage.setItem(key, JSON.stringify(item));
    },

    getItem(key) {
        const item = localStorage.getItem(key);
        if (!item) return null;

        const parsed = JSON.parse(item);

        if (parsed.expiration && Date.now() > parsed.expiration) {
            localStorage.removeItem(key);
            return null;
        }

        return parsed.value;
    },

    removeItem(key) {
        localStorage.removeItem(key);
    }
};

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    // Add any global initialization here
    console.log('App initialized');
});
