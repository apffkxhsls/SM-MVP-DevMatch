/* 이메일 형식 검사 정규식 */
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/* 입력 필드 아래에 에러 메시지를 표시하고 aria-invalid 설정 */
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

/* 에러 메시지를 지우고 aria-invalid 해제 */
function clearError(input) {
    const errorEl = input.parentElement.querySelector('.field-error');
    if (errorEl) errorEl.textContent = '';
    input.removeAttribute('aria-invalid');
}

/* 이메일: 필수, 형식, 최대 254자 */
function validateEmail(input) {
    const value = input.value.trim();
    if (!value) return showError(input, '이메일을 입력해주세요.'), false;
    if (value.length > 254) return showError(input, '이메일은 254자 이하여야 합니다.'), false;
    if (!EMAIL_REGEX.test(value)) return showError(input, '올바른 이메일 형식이 아닙니다.'), false;
    clearError(input);
    return true;
}

/* 로그인 비밀번호: 비어있는지만 확인 (서버에서 복잡도 조건 없이 가입 가능하므로 기존 계정 로그인이 막히지 않도록) */
function validatePasswordLogin(input) {
    const value = input.value;
    if (!value) return showError(input, '비밀번호를 입력해주세요.'), false;
    clearError(input);
    return true;
}

/* 회원가입 비밀번호: 8~20자, 영문·숫자·특수문자 포함 */
function validatePasswordSignup(input) {
    const value = input.value;
    if (!value) return showError(input, '비밀번호를 입력해주세요.'), false;
    if (value.length < 8 || value.length > 20) return showError(input, '비밀번호는 8~20자여야 합니다.'), false;
    const hasLetter  = /[a-zA-Z]/.test(value);
    const hasNumber  = /[0-9]/.test(value);
    const hasSpecial = /[^a-zA-Z0-9]/.test(value);
    if (!hasLetter || !hasNumber || !hasSpecial) return showError(input, '영문, 숫자, 특수문자를 포함해서 비밀번호를 설정해주세요.'), false;
    clearError(input);
    return true;
}

/* 닉네임: 필수, 최대 50자 */
function validateNickname(input) {
    const value = input.value.trim();
    if (!value) return showError(input, '닉네임을 입력해주세요.'), false;
    if (value.length > 50) return showError(input, '닉네임은 50자 이하여야 합니다.'), false;
    clearError(input);
    return true;
}

/* 비밀번호 확인: 비밀번호 필드와 일치 여부 검사 */
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

    // blur 시 실시간 검사
    emailInput.addEventListener('blur', () => validateEmail(emailInput));
    passwordInput.addEventListener('blur', () => validatePasswordLogin(passwordInput));

    // 제출 시 전체 검사 후 실패하면 서버 요청 차단
    loginForm.addEventListener('submit', (e) => {
        const ok = [
            validateEmail(emailInput),
            validatePasswordLogin(passwordInput),
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

    // blur 시 실시간 검사
    emailInput.addEventListener('blur', () => validateEmail(emailInput));
    passwordInput.addEventListener('blur', () => validatePasswordSignup(passwordInput));
    confirmInput.addEventListener('blur', () => validatePasswordConfirm(passwordInput, confirmInput));
    nicknameInput.addEventListener('blur', () => validateNickname(nicknameInput));

    // 제출 시 전체 검사 후 실패하면 서버 요청 차단
    signupForm.addEventListener('submit', (e) => {
        const ok = [
            validateEmail(emailInput),
            validatePasswordSignup(passwordInput),
            validatePasswordConfirm(passwordInput, confirmInput),
            validateNickname(nicknameInput),
        ].every(Boolean);
        if (!ok) e.preventDefault();
    });
}
