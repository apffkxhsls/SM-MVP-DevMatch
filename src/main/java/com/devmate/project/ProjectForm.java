package com.devmate.project;

import com.devmate.common.enums.MeetingType;
import com.devmate.common.enums.ProjectGoal;
import com.devmate.common.enums.Role;
import com.devmate.common.enums.Skill;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ProjectForm {

    @NotBlank(message = "프로젝트 제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "상세 설명을 입력해주세요.")
    @Size(max = 5000, message = "상세 설명은 5000자 이하로 입력해주세요.")
    private String description;

    @NotNull(message = "모집 역할을 선택해주세요.")
    private Role role;

    // 필수 기술을 지정하지 않는 모집글도 허용한다.
    private Set<@NotNull Skill> requiredSkills = new HashSet<>();

    private Set<@NotNull Skill> preferredSkills = new HashSet<>();

    @NotNull(message = "요구 주당 시간을 입력해주세요.")
    @Min(value = 1, message = "요구 주당 시간은 1시간 이상이어야 합니다.")
    @Max(value = 100, message = "요구 주당 시간은 100시간 이하여야 합니다.")
    private Integer requiredWeeklyHours;

    @NotNull(message = "최소 공통 시간을 입력해주세요.")
    @Min(value = 1, message = "최소 공통 시간은 1시간 이상이어야 합니다.")
    @Max(value = 168, message = "최소 공통 시간은 168시간 이하여야 합니다.")
    private Integer minimumCommonHours;

    @NotNull(message = "희망 공통 시간을 입력해주세요.")
    @Min(value = 1, message = "희망 공통 시간은 1시간 이상이어야 합니다.")
    @Max(value = 168, message = "희망 공통 시간은 168시간 이하여야 합니다.")
    private Integer desiredCommonHours;

    @NotEmpty(message = "팀 활동 가능 시간대를 선택해주세요.")
    private Set<
            @NotNull(message = "시간대 값이 누락되었습니다.")
            @Min(value = 0, message = "유효하지 않은 시간대입니다.")
            @Max(value = 167, message = "유효하지 않은 시간대입니다.")
                    Integer> availableSlots = new HashSet<>();

    @NotNull(message = "프로젝트 목표를 선택해주세요.")
    private ProjectGoal goal;

    @NotNull(message = "협업 방식을 선택해주세요.")
    private MeetingType meetingType;

    @Size(max = 100, message = "지역은 100자 이하로 입력해주세요.")
    private String region;

    @NotNull(message = "모집 마감 일시를 입력해주세요.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime deadline;

    @AssertTrue(message = "동일한 기술을 필수와 우대에 중복 선태할 수 없습니다.")
    public boolean isSkillsDisjoint() {
        if (requiredSkills == null || preferredSkills == null) {
            return true;
        }

        return Collections.disjoint(requiredSkills, preferredSkills);
    }

    @AssertTrue(message = "최소 공통 시간 ≤ 희망 공통 시간 ≤ 요구 주당 시간이어야 합니다.")
    public boolean isHoursOrdered() {
        // 누락은 각 필드의 @NotNull에서 처리한다.
        if (minimumCommonHours == null
                || desiredCommonHours == null
                || requiredWeeklyHours == null) {
            return true;
        }

        return minimumCommonHours <= desiredCommonHours
                && desiredCommonHours <= requiredWeeklyHours;
    }

    @AssertTrue(message = "팀 활동 가능 시간대를 희망 공통 시간 이상 선택해주세요.")
    public boolean isAvailabilitySufficient() {
        if (availableSlots == null || desiredCommonHours == null) {
            return true;
        }

        return availableSlots.size() >= desiredCommonHours;
    }

    @AssertTrue(message = "대면 프로젝트는 진행 지역을 입력해주세요.")
    public boolean isOfflineRegionPresent() {
        return meetingType != MeetingType.OFFLINE
                || (region != null && !region.isBlank());
    }

    @AssertTrue(message = "모집 마감 일시는 현재보다 이후여야 합니다.")
    public boolean isDeadlineFuture() {
        if (deadline == null) {
            return true;
        }

        return deadline.isAfter(
                LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        );
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(Set<Skill> requiredSkills) {
        this.requiredSkills = requiredSkills == null
                ? new HashSet<>()
                : new HashSet<>(requiredSkills);
    }

    public Set<Skill> getPreferredSkills() {
        return preferredSkills;
    }

    public void setPreferredSkills(Set<Skill> preferredSkills) {
        this.preferredSkills = preferredSkills == null
                ? new HashSet<>()
                : new HashSet<>(preferredSkills);
    }

    public Integer getRequiredWeeklyHours() {
        return requiredWeeklyHours;
    }

    public void setRequiredWeeklyHours(Integer requiredWeeklyHours) {
        this.requiredWeeklyHours = requiredWeeklyHours;
    }

    public Integer getMinimumCommonHours() {
        return minimumCommonHours;
    }

    public void setMinimumCommonHours(Integer minimumCommonHours) {
        this.minimumCommonHours = minimumCommonHours;
    }

    public Integer getDesiredCommonHours() {
        return desiredCommonHours;
    }

    public void setDesiredCommonHours(Integer desiredCommonHours) {
        this.desiredCommonHours = desiredCommonHours;
    }

    public Set<Integer> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(Set<Integer> availableSlots) {
        this.availableSlots = availableSlots == null
                ? new HashSet<>()
                : new HashSet<>(availableSlots);
    }

    public ProjectGoal getGoal() {
        return goal;
    }

    public void setGoal(ProjectGoal goal) {
        this.goal = goal;
    }

    public MeetingType getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(MeetingType meetingType) {
        this.meetingType = meetingType;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
