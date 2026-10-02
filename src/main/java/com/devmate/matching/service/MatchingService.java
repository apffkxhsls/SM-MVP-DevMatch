package com.devmate.matching.service;

import com.devmate.common.enums.MeetingType;
import com.devmate.common.enums.Skill;
import com.devmate.matching.MatchResult;
import com.devmate.matching.MatchStatus;
import com.devmate.profile.Profile;
import com.devmate.project.Project;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class MatchingService {

    /**
     * 유효한 모집글과 회원 프로필을 비교한다.
     * 모집 마감 여부와 본인 글 여부는 별도로 확인한다.
     */
    public MatchResult calculate(Profile profile, Project project) {
        Objects.requireNonNull(project, "모집글이 필요합니다.");

        if (profile == null) {
            return new MatchResult(
                    MatchStatus.UNKNOWN,
                    null,
                    List.of("프로필을 작성해주세요."),
                    Set.of()
            );
        }

        List<String> failures = new ArrayList<>();
        List<String> unknowns = new ArrayList<>();

        // 1. 역할
        if (profile.getRole() == null) {
            unknowns.add("희망 역할을 입력해주세요.");
        } else if (profile.getRole() != project.getRole()) {
            failures.add("모집 역할과 희망 역할이 다릅니다.");
        }

        // 2. 필수 기술
        Set<Skill> missingSkills =
                new HashSet<>(project.getRequiredSkills());
        missingSkills.removeAll(profile.getSkills());

        if (!missingSkills.isEmpty()) {
            String names = missingSkills.stream()
                    .map(Skill::name)
                    .sorted()
                    .collect(java.util.stream.Collectors.joining(", "));

            failures.add("필수 기술이 부족합니다: " + names);
        }

        // 3. 주당 투자 시간
        if (profile.getWeeklyHours() == null) {
            unknowns.add("주당 투자 가능 시간을 입력해주세요.");
        } else if (profile.getWeeklyHours()
                < project.getRequiredWeeklyHours()) {
            failures.add("주당 투자 가능 시간이 요구 시간보다 부족합니다.");
        }

        // 4. 공통 활동 시간
        Set<Integer> commonSlots =
                new HashSet<>(profile.getAvailableSlots());
        commonSlots.retainAll(project.getAvailableSlots());

        if (profile.getAvailableSlots().isEmpty()) {
            unknowns.add("활동 가능 시간대를 입력해주세요.");
        } else if (commonSlots.size() < project.getMinimumCommonHours()) {
            failures.add("공통 활동 시간이 최소 조건보다 부족합니다.");
        }

        // 5. 대면 지역
        if (project.getMeetingType() == MeetingType.OFFLINE) {
            String region = profile.getRegion();

            if (region == null || region.isBlank()) {
                unknowns.add("대면 가능 지역을 입력해주세요.");
            } else if (!region.strip().equals(project.getRegion().strip())) {
                failures.add("대면 가능 지역이 진행 지역과 다릅니다.");
            }
        }

        // 목표는 점수 계산에 필요하다.
        if (profile.getGoal() == null) {
            unknowns.add("희망 프로젝트 목표를 입력해주세요.");
        }

        // 정보가 부족해도, 이미 확인된 불일치가 있으면 FAIL이다.
        if (!failures.isEmpty()) {
            List<String> reasons = new ArrayList<>(failures);
            reasons.addAll(unknowns);

            return new MatchResult(
                    MatchStatus.FAIL,
                    null,
                    reasons,
                    commonSlots
            );
        }

        if (!unknowns.isEmpty()) {
            return new MatchResult(
                    MatchStatus.UNKNOWN,
                    null,
                    unknowns,
                    commonSlots
            );
        }

        // 여기부터는 PASS인 경우에만 실행된다.
        double timeScore = Math.min(
                (double) commonSlots.size() / project.getDesiredCommonHours(),
                1.0
        ) * 40;

        boolean goalMatches = profile.getGoal() == project.getGoal();
        double goalScore = goalMatches ? 20 : 0;

        Set<Skill> preferredSkills = project.getPreferredSkills();

        long matchedPreferredCount = preferredSkills.stream()
                .filter(profile.getSkills()::contains)
                .count();

        double totalScore;

        if (preferredSkills.isEmpty()) {
            // 공통 시간 : 목표 = 2 : 1 비율로 100점 환산
            totalScore = (timeScore + goalScore) / 60.0 * 100;
        } else {
            double skillScore =
                    (double) matchedPreferredCount / preferredSkills.size() * 40;

            totalScore = skillScore + timeScore + goalScore;
        }

        List<String> reasons = new ArrayList<>();
        reasons.add("필수 조건을 모두 충족합니다.");

        if (preferredSkills.isEmpty()) {
            reasons.add("우대 기술 조건 없음");
        } else {
            reasons.add(
                    "우대 기술 " + matchedPreferredCount
                            + "/" + preferredSkills.size() + "개 충족"
            );
        }

        reasons.add("공통 활동 가능 시간: 주 " + commonSlots.size() + "시간");
        reasons.add(goalMatches ? "프로젝트 목표 일치" : "프로젝트 목표 불일치");

        return new MatchResult(
                MatchStatus.PASS,
                (int) Math.round(totalScore),
                reasons,
                commonSlots
        );
    }
}
