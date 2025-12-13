/* ========== FORGOT PASSWORD PAGE SCRIPTS ========== */

let resetEmail = '';
let resetOTP = '';

/**
 * Handle email submission
 * @param {Event} event 
 */
async function handleEmailSubmit(event) {
    event.preventDefault();

    clearAlerts();

    const email = document.getElementById('resetEmail').value.trim();

    if (!email || !isValidEmail(email)) {
        showAlert('Email không hợp lệ', 'error');
        return;
    }

    try {
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang gửi...';

        // Call API to request OTP
        const response = await apiPost('/api/auth/request-otp', {
            email: email
        });

        resetEmail = email;

        // Show OTP step
        document.getElementById('emailStep').classList.add('hidden');
        document.getElementById('otpStep').classList.remove('hidden');

        showAlert('Mã OTP đã được gửi đến email của bạn', 'success');

    } catch (error) {
        showAlert(error.message || 'Email không tồn tại trong hệ thống', 'error');

        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Gửi Mã OTP';
    }
}

/**
 * Handle OTP verification
 * @param {Event} event 
 */
async function handleOTPSubmit(event) {
    event.preventDefault();

    clearAlerts();

    const otpCode = document.getElementById('otpCode').value.trim();

    if (!otpCode || otpCode.length !== 6) {
        showAlert('Vui lòng nhập mã OTP 6 chữ số', 'error');
        return;
    }

    try {
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang xác minh...';

        // Call API to verify OTP
        const response = await apiPost('/api/auth/verify-otp', {
            email: resetEmail,
            otp: otpCode
        });

        resetOTP = otpCode;

        // Show reset password step
        document.getElementById('otpStep').classList.add('hidden');
        document.getElementById('resetPasswordStep').classList.remove('hidden');

        showAlert('OTP xác minh thành công', 'success');

    } catch (error) {
        showAlert(error.message || 'Mã OTP không chính xác hoặc hết hạn', 'error');

        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Xác Minh';
    }
}

/**
 * Handle password reset
 * @param {Event} event 
 */
async function handleResetPassword(event) {
    event.preventDefault();

    clearAlerts();

    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmNewPassword').value;

    // Validation
    if (newPassword.length < 6) {
        showAlert('Mật khẩu phải có ít nhất 6 ký tự', 'error');
        return;
    }

    if (!passwordsMatch(newPassword, confirmPassword)) {
        showAlert('Mật khẩu xác nhận không khớp', 'error');
        return;
    }

    try {
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang cập nhật...';

        // Call API to reset password
        const response = await apiPost('/api/auth/reset-password', {
            email: resetEmail,
            otp: resetOTP,
            newPassword: newPassword
        });

        showAlert('Mật khẩu đã được cập nhật thành công!', 'success', 'alertContainer', 2000);

        // Redirect to login after delay
        setTimeout(() => {
            navigateTo('/templates/login.html');
        }, 2000);

    } catch (error) {
        showAlert(error.message || 'Đã xảy ra lỗi khi cập nhật mật khẩu', 'error');

        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Cập Nhật Mật Khẩu';
    }
}

/**
 * Go back to email step
 */
function backToEmail() {
    clearAlerts();
    document.getElementById('otpStep').classList.add('hidden');
    document.getElementById('emailStep').classList.remove('hidden');
    document.getElementById('otpCode').value = '';
}

/**
 * Check password strength
 */
function checkPasswordStrength() {
    const password = document.getElementById('newPassword').value;
    const strengthFill = document.getElementById('strengthFill');
    const strengthText = document.getElementById('strengthText');

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

    validateConfirmPassword();
}

/**
 * Validate password match
 */
function validateConfirmPassword() {
    const password = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmNewPassword').value;
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

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    console.log('Forgot password page initialized');
});
