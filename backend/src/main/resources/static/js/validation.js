/* ========== VALIDATION UTILITIES ========== */

/**
 * Validate email format
 * @param {string} email 
 * @returns {boolean}
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Validate phone number (10-11 digits)
 * @param {string} phone 
 * @returns {boolean}
 */
function isValidPhone(phone) {
    const phoneRegex = /^\d{10,11}$/;
    return phoneRegex.test(phone.replace(/\D/g, ''));
}

/**
 * Validate username (no spaces, no special chars, doesn't start with number)
 * @param {string} username 
 * @returns {boolean}
 */
function isValidUsername(username) {
    if (!username || username.length < 3) return false;
    if (/^\d/.test(username)) return false; // starts with number
    if (/[^a-zA-Z0-9_]/.test(username)) return false; // contains special chars except _
    return true;
}

/**
 * Check password strength
 * @param {string} password 
 * @returns {object} { level: 'weak'|'fair'|'strong', score: 0-100 }
 */
function checkPasswordStrengthLevel(password) {
    let score = 0;

    if (password.length >= 8) score += 25;
    if (password.length >= 12) score += 10;
    
    if (/[a-z]/.test(password)) score += 15;
    if (/[A-Z]/.test(password)) score += 15;
    if (/[0-9]/.test(password)) score += 15;
    if (/[^a-zA-Z0-9]/.test(password)) score += 20;

    let level = 'weak';
    if (score >= 60) level = 'strong';
    else if (score >= 40) level = 'fair';

    return { level, score };
}

/**
 * Validate date of birth (must be 16 or older)
 * @param {string} dobString ISO date string (YYYY-MM-DD)
 * @returns {boolean}
 */
function isAtLeast16(dobString) {
    const dob = new Date(dobString);
    const today = new Date();
    let age = today.getFullYear() - dob.getFullYear();
    const monthDiff = today.getMonth() - dob.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
        age--;
    }
    
    return age >= 16;
}

/**
 * Show alert message
 * @param {string} message 
 * @param {string} type 'success'|'error'|'warning'|'info'
 * @param {string} containerId 
 * @param {number} duration auto-dismiss time in ms (0 = no auto dismiss)
 */
function showAlert(message, type = 'info', containerId = 'alertContainer', duration = 5000) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.innerHTML = `
        <span>${message}</span>
        <button class="alert-close" onclick="this.parentElement.remove()">×</button>
    `;

    container.appendChild(alertDiv);

    if (duration > 0) {
        setTimeout(() => {
            if (alertDiv.parentElement) {
                alertDiv.remove();
            }
        }, duration);
    }
}

/**
 * Clear all alerts
 * @param {string} containerId 
 */
function clearAlerts(containerId = 'alertContainer') {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = '';
    }
}

/**
 * Check password match
 * @param {string} password 
 * @param {string} confirmPassword 
 * @returns {boolean}
 */
function passwordsMatch(password, confirmPassword) {
    return password === confirmPassword && password.length > 0;
}

/**
 * Format phone number for display
 * @param {string} phone 
 * @returns {string}
 */
function formatPhoneDisplay(phone) {
    const cleaned = phone.replace(/\D/g, '');
    if (cleaned.length <= 3) return cleaned;
    if (cleaned.length <= 6) return `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 6)}-${cleaned.slice(6)}`;
}

/**
 * Format currency for display
 * @param {number} amount 
 * @returns {string}
 */
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(amount);
}

/**
 * Validate full name (non-empty, reasonable length)
 * @param {string} name 
 * @returns {boolean}
 */
function isValidFullName(name) {
    return name && name.trim().length >= 2 && name.trim().length <= 100;
}
