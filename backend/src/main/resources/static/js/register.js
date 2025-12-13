/* ========== REGISTER PAGE SCRIPTS ========== */

/**
 * Check password strength and update UI
 */
function checkPasswordStrength() {
    const password = document.getElementById('regPassword').value;
    const strengthFill = document.getElementById('strengthFill');
    const strengthText = document.getElementById('strengthText');

    // Clear previous classes
    strengthFill.className = 'strength-fill';

    if (!password) {
        strengthText.textContent = 'Độ mạnh: Yếu';
        return;
    }

    const { level, score } = checkPasswordStrengthLevel(password);

    strengthFill.classList.add(level);

    let strengthLabel = '';
    switch(level) {
        case 'weak':
            strengthLabel = 'Yếu';
            break;
        case 'fair':
            strengthLabel = 'Trung bình';
            break;
        case 'strong':
            strengthLabel = 'Mạnh';
            break;
    }

    strengthText.textContent = `Độ mạnh: ${strengthLabel} (${score}%)`;

    // Update confirm password validation
    validateConfirmPassword();
}

/**
 * Validate password match in real-time
 */
function validateConfirmPassword() {
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const confirmText = document.getElementById('confirmText');

    if (!password || !confirmPassword) {
        confirmText.textContent = '';
        return;
    }

    if (passwordsMatch(password, confirmPassword)) {
        confirmText.textContent = '✓ Mật khẩu khớp';
        confirmText.style.color = 'var(--color-success)';
    } else {
        confirmText.textContent = '✗ Mật khẩu không khớp';
        confirmText.style.color = 'var(--color-error)';
    }
}

/**
 * Handle registration form submission
 * @param {Event} event 
 */
async function handleRegister(event) {
    event.preventDefault();

    clearAlerts();

    // Get form values
    const fullName = document.getElementById('fullName').value.trim();
    const dob = document.getElementById('dob').value;
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const username = document.getElementById('regUsername').value.trim();
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Validation
    const errors = [];

    if (!isValidFullName(fullName)) {
        errors.push('Họ và tên phải có từ 2-100 ký tự');
    }

    if (!dob) {
        errors.push('Vui lòng chọn ngày sinh');
    } else if (!isAtLeast16(dob)) {
        errors.push('Bạn phải từ 16 tuổi trở lên');
    }

    if (!email || !isValidEmail(email)) {
        errors.push('Email không hợp lệ (phải chứa @ và dấu chấm)');
    }

    if (!phone || !isValidPhone(phone)) {
        errors.push('Số điện thoại phải có 10-11 chữ số');
    }

    if (!isValidUsername(username)) {
        errors.push('Tên đăng nhập không hợp lệ (3+ ký tự, không dấu cách, không bắt đầu bằng số)');
    }

    if (password.length < 6) {
        errors.push('Mật khẩu phải có ít nhất 6 ký tự');
    }

    if (!passwordsMatch(password, confirmPassword)) {
        errors.push('Mật khẩu xác nhận không khớp');
    }

    if (errors.length > 0) {
        errors.forEach(error => showAlert(error, 'error'));
        return;
    }

    try {
        // Disable submit button
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang tạo tài khoản...';

        // Call API to register user
        // Backend sẽ:
        // 1. Lưu tất cả thông tin vào database
        // 2. Gán balance = 1,000,000 VNĐ cho tài khoản mới
        const response = await apiPost('/api/auth/register', {
            fullName: fullName,
            dateOfBirth: dob,
            email: email,
            phoneNumber: phone,
            username: username,
            password: password
        });

        // Store user data from registration (sẽ dùng khi login)
        if (response.user) {
            saveUserData(response.user);
        }

        showAlert('Đăng ký thành công! Vui lòng đăng nhập.', 'success', 'alertContainer', 2000);

        // Redirect to login after delay
        setTimeout(() => {
            navigateTo('/templates/login.html');
        }, 2000);

    } catch (error) {
        showAlert(error.message || 'Đã xảy ra lỗi khi đăng ký tài khoản', 'error');

        // Re-enable submit button
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Tạo Tài Khoản';
    }
}

// Listen for input events on password confirm field
document.addEventListener('DOMContentLoaded', () => {
    const confirmPasswordField = document.getElementById('confirmPassword');
    if (confirmPasswordField) {
        confirmPasswordField.addEventListener('input', validateConfirmPassword);
    }

    const passwordField = document.getElementById('regPassword');
    if (passwordField) {
        passwordField.addEventListener('input', checkPasswordStrength);
    }
});
