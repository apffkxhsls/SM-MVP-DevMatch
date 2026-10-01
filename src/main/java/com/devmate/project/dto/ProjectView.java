package com.devmate.project.dto;

import com.devmate.common.enums.*;
import com.devmate.project.Project;

import java.time.LocalDateTime;
import java.util.Set;

public record ProjectView(
        Long id,
        Long authorId,
        String authorName,
        String title,
        String description,
        Role role,
        Set<Skill> requiredSkills,
        Set<Skill> preferredSkills,
        Integer requiredWeeklyHours,
        Integer minimumCommonHours,
        Integer desiredCommonHours,
        Set<Integer> availableSlots,
        ProjectGoal goal,
        MeetingType meetingType,
        String region,
        LocalDateTime deadline,
        ProjectStatus status,
        boolean recruiting,
        LocalDateTime createdAt
) {

    public static ProjectView from(Project project, LocalDateTime now) {
        return new ProjectView(
                project.getId(),
                project.getAuthor().getId(),
                project.getAuthor().getName(),
                project.getTitle(),
                project.getDescription(),
                project.getRole(),
                Set.copyOf(project.getRequiredSkills()),
                Set.copyOf(project.getPreferredSkills()),
                project.getRequiredWeeklyHours(),
                project.getMinimumCommonHours(),
                project.getDesiredCommonHours(),
                Set.copyOf(project.getAvailableSlots()),
                project.getGoal(),
                project.getMeetingType(),
                project.getRegion(),
                project.getDeadline(),
                project.getStatus(),
                project.isRecruiting(now),
                project.getCreatedAt()
        );
    }
}
