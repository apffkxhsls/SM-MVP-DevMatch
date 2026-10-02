const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function showError(input, message) {
    let errorEl = input.parentElement.querySelector('.field-error');
    if (!errorEl) {
        errorEl = document.createElement('p');
        errorEl.className = 'field-error';
        input.after(errorEl);
    }
    errorEl.textContent = message;
    input.setAttribute('aria-invalid', 'true');
}

function clearError(input) {
    const errorEl = input.parentElement.querySelector('.field-error');
    if (errorEl) errorEl.textContent = '';
    input.removeAttribute('aria-invalid');
}

function validateEmail(input) {
    const value = input.value.trim();
    if (!value) return showError(input, '이메일을 입력해주세요.'), false;
    if (value.length > 254) return showError(input, '이메일은 254자 이하여야 합니다.'), false;
    if (!EMAIL_REGEX.test(value)) return showError(input, '올바른 이메일 형식이 아닙니다.'), false;
    clearError(input);
    return true;
}

function validatePassword(input) {
    const value = input.value;
    if (!value) return showError(input, '비밀번호를 입력해주세요.'), false;
    if (value.length < 8 || value.length > 20) return showError(input, '비밀번호는 8~20자여야 합니다.'), false;
    clearError(input);
    return true;
}

function validateNickname(input) {
    const value = input.value.trim();
    if (!value) return showError(input, '닉네임을 입력해주세요.'), false;
    if (value.length > 50) return showError(input, '닉네임은 50자 이하여야 합니다.'), false;
    clearError(input);
    return true;
}

function validatePasswordConfirm(passwordInput, confirmInput) {
    if (!confirmInput.value) return showError(confirmInput, '비밀번호를 확인해주세요.'), false;
    if (passwordInput.value !== confirmInput.value) return showError(confirmInput, '비밀번호가 일치하지 않습니다.'), false;
    clearError(confirmInput);
    return true;
}

/* ===== 로그인 ===== */
const loginForm = document.querySelector('.login-form');
if (loginForm) {
    const emailInput    = loginForm.querySelector('#email');
    const passwordInput = loginForm.querySelector('#password');

    emailInput.addEventListener('blur', () => validateEmail(emailInput));
    passwordInput.addEventListener('blur', () => validatePassword(passwordInput));

    loginForm.addEventListener('submit', (e) => {
        const ok = [
            validateEmail(emailInput),
            validatePassword(passwordInput),
        ].every(Boolean);
        if (!ok) e.preventDefault();
    });
}

/* ===== 회원가입 ===== */
const signupForm = document.querySelector('.signup-form');
if (signupForm) {
    const emailInput    = signupForm.querySelector('#email');
    const passwordInput = signupForm.querySelector('#password');
    const confirmInput  = signupForm.querySelector('#passwordConfirm');
    const nicknameInput = signupForm.querySelector('#name');

    emailInput.addEventListener('blur', () => validateEmail(emailInput));
    passwordInput.addEventListener('blur', () => validatePassword(passwordInput));
    confirmInput.addEventListener('blur', () => validatePasswordConfirm(passwordInput, confirmInput));
    nicknameInput.addEventListener('blur', () => validateNickname(nicknameInput));

    signupForm.addEventListener('submit', (e) => {
        const ok = [
            validateEmail(emailInput),
            validatePassword(passwordInput),
            validatePasswordConfirm(passwordInput, confirmInput),
            validateNickname(nicknameInput),
        ].every(Boolean);
        if (!ok) e.preventDefault();
    });
}
