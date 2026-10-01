package com.devmate.project;

import com.devmate.common.enums.MeetingType;
import com.devmate.common.enums.ProjectGoal;
import com.devmate.common.enums.Role;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectFormTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    @DisplayName("유효한 시간 조건이면 검증을 통과한다")
    void validTimeConditions() {
        assertThat(validator.validate(validForm())).isEmpty();
    }

    @Test
    @DisplayName("최소·희망·요구 시간이 같아도 허용한다")
    void equalHoursAllowed() {
        ProjectForm form = validForm();
        form.setMinimumCommonHours(4);
        form.setDesiredCommonHours(4);
        form.setRequiredWeeklyHours(4);

        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    @DisplayName("최소 공통 시간이 희망 공통 시간보다 크면 거부한다")
    void minimumExceedsDesired() {
        ProjectForm form = validForm();
        form.setMinimumCommonHours(5);
        form.setDesiredCommonHours(4);

        assertFieldError(form, "hoursOrdered");
    }

    @Test
    @DisplayName("희망 공통 시간이 요구 주당 시간보다 크면 거부한다")
    void desiredExceedsWeekly() {
        ProjectForm form = validForm();
        form.setDesiredCommonHours(4);
        form.setRequiredWeeklyHours(3);

        assertFieldError(form, "hoursOrdered");
    }

    @Test
    @DisplayName("선택한 시간대 수가 희망 공통 시간보다 적으면 거부한다")
    void insufficientAvailability() {
        ProjectForm form = validForm();
        form.setAvailableSlots(Set.of(19, 20, 21));

        assertFieldError(form, "availabilitySufficient");
    }

    @Test
    @DisplayName("시간 항목이 누락되면 해당 필드 오류를 반환한다")
    void missingHours() {
        ProjectForm form = validForm();
        form.setRequiredWeeklyHours(null);
        form.setMinimumCommonHours(null);
        form.setDesiredCommonHours(null);

        assertThat(validator.validate(form))
                .extracting(v -> v.getPropertyPath().toString())
                .contains(
                        "requiredWeeklyHours",
                        "minimumCommonHours",
                        "desiredCommonHours"
                );
    }

    @Test
    @DisplayName("시간 항목은 0시간과 169시간을 허용하지 않는다")
    void hoursOutOfRange() {
        for (int hours : new int[]{0, 169}) {
            ProjectForm form = validForm();
            form.setRequiredWeeklyHours(hours);
            form.setMinimumCommonHours(hours);
            form.setDesiredCommonHours(hours);

            assertThat(validator.validate(form))
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains(
                            "requiredWeeklyHours",
                            "minimumCommonHours",
                            "desiredCommonHours"
                    );
        }
    }

    @Test
    @DisplayName("시간대 값이 0~167 범위를 벗어나면 거부한다")
    void invalidSlotValue() {
        for (int slot : new int[]{-1, 168}) {
            ProjectForm form = validForm();
            form.setAvailableSlots(Set.of(19, 20, 21, slot));

            assertThat(validator.validate(form))
                    .anySatisfy(violation ->
                            assertThat(violation.getPropertyPath().toString())
                                    .startsWith("availableSlots")
                    );
        }
    }

    private void assertFieldError(ProjectForm form, String field) {
        assertThat(validator.validate(form))
                .extracting(v -> v.getPropertyPath().toString())
                .contains(field);
    }

    private ProjectForm validForm() {
        ProjectForm form = new ProjectForm();
        form.setTitle("백엔드 팀원 모집");
        form.setDescription("함께 프로젝트를 진행할 팀원을 모집합니다.");
        form.setRole(Role.BACKEND);
        form.setRequiredWeeklyHours(8);
        form.setMinimumCommonHours(2);
        form.setDesiredCommonHours(4);
        form.setAvailableSlots(Set.of(19, 20, 21, 22));
        form.setGoal(ProjectGoal.PORTFOLIO);
        form.setMeetingType(MeetingType.ONLINE);
        form.setDeadline(
                LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(7)
        );
        return form;
    }
}