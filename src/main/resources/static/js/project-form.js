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