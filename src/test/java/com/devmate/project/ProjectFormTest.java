package com.devmate.project;

import com.devmate.common.enums.MeetingType;
import com.devmate.common.enums.ProjectGoal;
import com.devmate.common.enums.Role;
import com.devmate.common.enums.Skill;
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

    @Test
    @DisplayName("필수 기술과 우대 기술이 서로 다르면 허용한다")
    void differentSkillsAllowed() {
        ProjectForm form = validForm();
        form.setRequiredSkills(Set.of(Skill.JAVA));
        form.setPreferredSkills(Set.of(Skill.SPRING_BOOT));

        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    @DisplayName("필수·우대 기술에 일부라도 같은 기술이 있으면 거부한다")
    void overlappingSkillsRejected() {
        ProjectForm form = validForm();
        form.setRequiredSkills(Set.of(Skill.JAVA, Skill.KOTLIN));
        form.setPreferredSkills(Set.of(Skill.KOTLIN, Skill.SPRING_BOOT));

        assertFieldError(form, "skillsDisjoint");
    }

    @Test
    @DisplayName("필수 기술 없이 우대 기술만 지정할 수 있다")
    void preferredSkillsOnly() {
        ProjectForm form = validForm();
        form.setRequiredSkills(Set.of());
        form.setPreferredSkills(Set.of(Skill.JAVA));

        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    @DisplayName("우대 기술 없이 필수 기술만 지정할 수 있다")
    void requiredSkillsOnly() {
        ProjectForm form = validForm();
        form.setRequiredSkills(Set.of(Skill.JAVA));
        form.setPreferredSkills(Set.of());

        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    @DisplayName("대면 프로젝트에 지역을 입력하면 허용한다")
    void offlineWithRegion() {
        ProjectForm form = validForm();
        form.setMeetingType(MeetingType.OFFLINE);
        form.setRegion("서울특별시 용산구");

        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    @DisplayName("대면 프로젝트에 지역이 없거나 공백뿐이면 거부한다")
    void offlineWithoutRegion() {
        for (String region : new String[]{null, "", "   "}) {
            ProjectForm form = validForm();
            form.setMeetingType(MeetingType.OFFLINE);
            form.setRegion(region);

            assertFieldError(form, "offlineRegionPresent");
        }
    }

    @Test
    @DisplayName("비대면 프로젝트는 지역을 입력하지 않아도 된다")
    void onlineWithoutRegion() {
        ProjectForm form = validForm();
        form.setMeetingType(MeetingType.ONLINE);
        form.setRegion(null);

        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    @DisplayName("진행 지역이 100자를 초과하면 거부한다")
    void regionTooLong() {
        ProjectForm form = validForm();
        form.setMeetingType(MeetingType.OFFLINE);
        form.setRegion("가".repeat(101));

        assertFieldError(form, "region");
    }

    @Test
    @DisplayName("미래의 모집 마감 일시는 허용한다")
    void futureDeadline() {
        ProjectForm form = validForm();
        form.setDeadline(
                LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(1)
        );

        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    @DisplayName("과거의 모집 마감 일시는 거부한다")
    void pastDeadline() {
        ProjectForm form = validForm();
        form.setDeadline(
                LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(1)
        );

        assertFieldError(form, "deadlineFuture");
    }

    @Test
    @DisplayName("모집 마감 일시를 입력하지 않으면 거부한다")
    void missingDeadline() {
        ProjectForm form = validForm();
        form.setDeadline(null);

        assertFieldError(form, "deadline");
    }
}