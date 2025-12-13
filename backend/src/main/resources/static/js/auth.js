/* ========== AUTH PAGE SCRIPTS ========== */

/**
 * Handle login form submission
 * @param {Event} event 
 */
async function handleLogin(event) {
    event.preventDefault();
    
    clearAlerts();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    // Validation
    if (!username || !password) {
        showAlert('Vui lòng điền tất cả các trường', 'error');
        return;
    }

    try {
        // Disable submit button
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang xử lý...';

        // Call API
        const response = await apiPost('/api/auth/login', {
            username: username,
            password: password
        });

        // Save auth token and user data from login response
        if (response.token) {
            saveAuthToken(response.token);
            
            // Ensure user data is stored with fullName and current balance
            if (response.user) {
                // Structure user data with fullName from registration and balance from database
                const userData = {
                    id: response.user.id,
                    username: response.user.username,
                    fullName: response.user.fullName, // Lấy từ thông tin đăng ký
                    email: response.user.email,
                    balance: response.user.balance || 1000000, // Mặc định 1 triệu cho tài khoản mới
                    avatarUrl: response.user.avatarUrl || null,
                    rankPoints: response.user.rankPoints || 0
                };
                saveUserData(userData);
            }

            showAlert('Đăng nhập thành công!', 'success', 'alertContainer', 1500);

            // Redirect after short delay
            setTimeout(() => {
                navigateTo('/templates/home.html');
            }, 1500);
        }
    } catch (error) {
        showAlert(error.message || 'Tên đăng nhập hoặc mật khẩu không đúng', 'error');
        
        // Re-enable submit button
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Đăng Nhập';

        // Clear password field
        document.getElementById('password').value = '';
    }
}

/**
 * Load user data into header (called on pages with header)
 * Retrieves fullName and balance from localStorage after login
 * fullName: Từ thông tin đăng ký
 * balance: Số dư hiện có (mặc định 1 triệu VNĐ cho tài khoản mới)
 */
function loadUserDataToHeader() {
    const userData = getUserData();
    if (!userData) {
        console.warn('No user data found in localStorage');
        return;
    }

    const avatarEl = document.querySelector('.avatar');
    const usernameEl = document.querySelector('.username');
    const balanceEl = document.querySelector('.balance-amount');

    // Set avatar - first letter of full name
    if (avatarEl && userData.fullName) {
        avatarEl.textContent = userData.fullName.charAt(0).toUpperCase();
    }

    // Set username - display full name from registration
    if (usernameEl && userData.fullName) {
        usernameEl.textContent = userData.fullName;
    }

    // Set balance - display current balance from database
    // Default to 1,000,000 if not set (new account starting balance)
    if (balanceEl) {
        const balance = userData.balance || 1000000;
        balanceEl.textContent = formatCurrency(balance);
    }
}

/**
 * Verify authentication on protected pages
 */
function verifyAuth() {
    if (!isAuthenticated()) {
        showAlert('Vui lòng đăng nhập để tiếp tục', 'warning', 'alertContainer', 2000);
        setTimeout(() => {
            navigateTo('/templates/login.html');
        }, 2000);
        return false;
    }
    return true;
}

// Load user data when page loads
document.addEventListener('DOMContentLoaded', () => {
    loadUserDataToHeader();
});
