// 1. 필수·우대 기술 중복 선택 방지
const requiredSkills = document.querySelectorAll(
    'input[name="requiredSkills"]'
);

const preferredSkills = document.querySelectorAll(
    'input[name="preferredSkills"]'
);

function updateSkillOptions() {
    const selectedRequired = new Set(
        [...requiredSkills]
            .filter(input => input.checked)
            .map(input => input.value)
    );

    const selectedPreferred = new Set(
        [...preferredSkills]
            .filter(input => input.checked)
            .map(input => input.value)
    );

    // 우대에서 선택한 기술은 필수에서 선택 불가
    requiredSkills.forEach(input => {
        input.disabled = selectedPreferred.has(input.value);
    });

    // 필수에서 선택한 기술은 우대에서 선택 불가
    preferredSkills.forEach(input => {
        input.disabled = selectedRequired.has(input.value);
    });
}

[...requiredSkills, ...preferredSkills].forEach(input => {
    input.addEventListener("change", updateSkillOptions);
});

updateSkillOptions();


// 2. 대면 선택 시에만 지역 입력
const meetingType = document.getElementById("meetingType");
const regionField = document.getElementById("regionField");
const regionInput = document.getElementById("region");

function updateRegionField() {
    const isOffline = meetingType.value === "OFFLINE";

    regionField.hidden = !isOffline;
    regionInput.disabled = !isOffline;
    regionInput.required = isOffline;
}

meetingType.addEventListener("change", updateRegionField);

updateRegionField();


// 3. 시간 조건 검증 (최소 공통 ≤ 희망 공통 ≤ 요구 주당)
const requiredWeeklyHoursInput = document.getElementById("requiredWeeklyHours");
const minimumCommonHoursInput  = document.getElementById("minimumCommonHours");
const desiredCommonHoursInput  = document.getElementById("desiredCommonHours");
const hoursOrderError          = document.getElementById("hoursOrderError");

function validateTimeOrder() {
    // 세 필드 모두 입력된 경우에만 조건 검사 (빈 값 검사는 서버에서 처리)
    if (!minimumCommonHoursInput.value || !desiredCommonHoursInput.value || !requiredWeeklyHoursInput.value) {
        hoursOrderError.textContent = "";
        return true;
    }

    const min     = Number(minimumCommonHoursInput.value);
    const desired = Number(desiredCommonHoursInput.value);
    const weekly  = Number(requiredWeeklyHoursInput.value);

    if (min <= desired && desired <= weekly) {
        hoursOrderError.textContent = "";
        return true;
    }

    hoursOrderError.textContent = "최소 공통 시간 ≤ 희망 공통 시간 ≤ 요구 주당 시간이어야 합니다.";
    return false;
}

// blur 시 실시간 검사
[requiredWeeklyHoursInput, minimumCommonHoursInput, desiredCommonHoursInput].forEach(input => {
    input.addEventListener("blur", validateTimeOrder);
});

// 제출 시 조건 위반이면 서버 요청 차단
const projectForm = document.querySelector(".project-form");
if (projectForm) {
    projectForm.addEventListener("submit", (e) => {
        if (!validateTimeOrder()) {
            e.preventDefault();
        }
    });
}