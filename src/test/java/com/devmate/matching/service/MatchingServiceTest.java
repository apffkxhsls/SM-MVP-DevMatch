package com.devmate.matching.service;

import com.devmate.common.enums.*;
import com.devmate.matching.MatchResult;
import com.devmate.matching.MatchStatus;
import com.devmate.member.Member;
import com.devmate.profile.Profile;
import com.devmate.project.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingServiceTest {

    private final MatchingService matchingService = new MatchingService();

    private static final Set<Integer> TEAM_SLOTS =
            Set.of(19, 20, 21, 22, 23, 43);

    @Test
    @DisplayName("모든 조건을 충족하면 PASS와 100점을 반환한다")
    void perfectMatch() {
        MatchResult result = matchingService.calculate(
                defaultProfile(),
                defaultProject()
        );

        assertThat(result.status()).isEqualTo(MatchStatus.PASS);
        assertThat(result.score()).isEqualTo(100);
        assertThat(result.commonSlots())
                .containsExactlyInAnyOrder(19, 20, 21, 22);
    }

    @Test
    @DisplayName("프로필이 없으면 UNKNOWN이며 점수가 없다")
    void missingProfile() {
        MatchResult result = matchingService.calculate(null, defaultProject());

        assertNoScore(result, MatchStatus.UNKNOWN);
        assertThat(result.commonSlots()).isEmpty();
        assertThat(result.reasons()).isNotEmpty();
    }

    @Test
    @DisplayName("모집 역할이 다르면 FAIL이다")
    void differentRole() {
        Profile profile = profile(
                Role.ANDROID,
                Set.of(Skill.JAVA, Skill.SPRING_BOOT),
                8,
                Set.of(19, 20, 21, 22),
                ProjectGoal.PORTFOLIO,
                null
        );

        assertNoScore(
                matchingService.calculate(profile, defaultProject()),
                MatchStatus.FAIL
        );
    }

    @Test
    @DisplayName("기술 미선택은 필수 기술 미충족으로 처리한다")
    void missingRequiredSkill() {
        Profile profile = profile(
                Role.BACKEND,
                Set.of(),
                8,
                Set.of(19, 20, 21, 22),
                ProjectGoal.PORTFOLIO,
                null
        );

        MatchResult result =
                matchingService.calculate(profile, defaultProject());

        assertNoScore(result, MatchStatus.FAIL);
        assertThat(result.reasons())
                .anySatisfy(reason -> assertThat(reason).contains("JAVA"));
    }

    @Test
    @DisplayName("주당 투자 시간이 부족하면 FAIL이다")
    void insufficientWeeklyHours() {
        Profile profile = profile(
                Role.BACKEND,
                Set.of(Skill.JAVA, Skill.SPRING_BOOT),
                7,
                Set.of(19, 20, 21, 22),
                ProjectGoal.PORTFOLIO,
                null
        );

        assertNoScore(
                matchingService.calculate(profile, defaultProject()),
                MatchStatus.FAIL
        );
    }

    @Test
    @DisplayName("공통 시간이 최소보다 부족하면 FAIL이다")
    void insufficientCommonHours() {
        Profile profile = profile(
                Role.BACKEND,
                Set.of(Skill.JAVA, Skill.SPRING_BOOT),
                8,
                Set.of(19, 100),
                ProjectGoal.PORTFOLIO,
                null
        );

        MatchResult result =
                matchingService.calculate(profile, defaultProject());

        assertNoScore(result, MatchStatus.FAIL);
        assertThat(result.commonSlots()).containsExactly(19);
    }

    @Test
    @DisplayName("공통 시간이 최소와 같으면 PASS이며 비례 점수를 계산한다")
    void minimumCommonHoursBoundary() {
        Profile profile = profile(
                Role.BACKEND,
                Set.of(Skill.JAVA, Skill.SPRING_BOOT),
                8,
                Set.of(19, 20),
                ProjectGoal.PORTFOLIO,
                null
        );

        MatchResult result =
                matchingService.calculate(profile, defaultProject());

        // 우대 40 + 시간 20 + 목표 20
        assertThat(result.status()).isEqualTo(MatchStatus.PASS);
        assertThat(result.score()).isEqualTo(80);
    }

    @Test
    @DisplayName("역할·투자 시간·시간대·목표 미입력은 UNKNOWN이다")
    void incompleteProfile() {
        Profile[] profiles = {
                profile(null, Set.of(Skill.JAVA), 8,
                        Set.of(19, 20), ProjectGoal.PORTFOLIO, null),
                profile(Role.BACKEND, Set.of(Skill.JAVA), null,
                        Set.of(19, 20), ProjectGoal.PORTFOLIO, null),
                profile(Role.BACKEND, Set.of(Skill.JAVA), 8,
                        Set.of(), ProjectGoal.PORTFOLIO, null),
                profile(Role.BACKEND, Set.of(Skill.JAVA), 8,
                        Set.of(19, 20), null, null)
        };

        for (Profile profile : profiles) {
            assertNoScore(
                    matchingService.calculate(profile, defaultProject()),
                    MatchStatus.UNKNOWN
            );
        }
    }

    @Test
    @DisplayName("정보 누락과 명확한 불일치가 함께 있으면 FAIL이 우선한다")
    void failBeforeUnknown() {
        Profile profile = profile(
                Role.ANDROID,
                Set.of(Skill.JAVA),
                null,
                Set.of(19, 20),
                ProjectGoal.PORTFOLIO,
                null
        );

        MatchResult result =
                matchingService.calculate(profile, defaultProject());

        assertNoScore(result, MatchStatus.FAIL);
        assertThat(result.reasons()).hasSize(2);
    }

    @Test
    @DisplayName("우대 기술 충족 비율에 따라 점수를 계산한다")
    void preferredSkillRatio() {
        Project project = project(
                Set.of(Skill.KOTLIN, Skill.PYTHON, Skill.SPRING_BOOT, Skill.REACT),
                MeetingType.ONLINE,
                null
        );

        Profile profile = profile(
                Role.BACKEND,
                Set.of(Skill.JAVA, Skill.KOTLIN, Skill.PYTHON, Skill.SPRING_BOOT),
                8,
                Set.of(19, 20, 21, 22),
                ProjectGoal.PORTFOLIO,
                null
        );

        MatchResult result = matchingService.calculate(profile, project);

        // 우대 30 + 시간 40 + 목표 20
        assertThat(result.status()).isEqualTo(MatchStatus.PASS);
        assertThat(result.score()).isEqualTo(90);
    }

    @Test
    @DisplayName("목표가 달라도 PASS이며 목표 점수만 제외한다")
    void differentGoal() {
        Profile profile = profile(
                Role.BACKEND,
                Set.of(Skill.JAVA, Skill.SPRING_BOOT),
                8,
                Set.of(19, 20, 21, 22),
                ProjectGoal.CONTEST,
                null
        );

        MatchResult result =
                matchingService.calculate(profile, defaultProject());

        assertThat(result.status()).isEqualTo(MatchStatus.PASS);
        assertThat(result.score()).isEqualTo(80);
    }

    @Test
    @DisplayName("희망 시간보다 많이 겹쳐도 시간 점수는 40점을 넘지 않는다")
    void timeScoreCap() {
        Profile profile = profile(
                Role.BACKEND,
                Set.of(Skill.JAVA, Skill.SPRING_BOOT),
                8,
                TEAM_SLOTS,
                ProjectGoal.PORTFOLIO,
                null
        );

        MatchResult result =
                matchingService.calculate(profile, defaultProject());

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.commonSlots()).hasSize(6);
    }

    @Test
    @DisplayName("우대 기술이 없으면 시간과 목표 점수를 100점으로 환산한다")
    void noPreferredSkillsPerfectScore() {
        Project project = project(Set.of(), MeetingType.ONLINE, null);

        MatchResult result =
                matchingService.calculate(defaultProfile(), project);

        assertThat(result.status()).isEqualTo(MatchStatus.PASS);
        assertThat(result.score()).isEqualTo(100);
    }

    @Test
    @DisplayName("우대 기술이 없을 때 환산한 최종 점수를 반올림한다")
    void noPreferredSkillsRounding() {
        Project project = project(Set.of(), MeetingType.ONLINE, null);

        Profile profile = profile(
                Role.BACKEND,
                Set.of(Skill.JAVA),
                8,
                Set.of(19, 20),
                ProjectGoal.PORTFOLIO,
                null
        );

        MatchResult result = matchingService.calculate(profile, project);

        // (시간 20 + 목표 20) / 60 × 100 = 66.666... → 67
        assertThat(result.status()).isEqualTo(MatchStatus.PASS);
        assertThat(result.score()).isEqualTo(67);
    }

    @Test
    @DisplayName("대면 지역은 앞뒤 공백을 제거한 뒤 비교한다")
    void matchingOfflineRegion() {
        Project project = project(
                Set.of(Skill.SPRING_BOOT),
                MeetingType.OFFLINE,
                "서울특별시 용산구"
        );

        Profile profile = profile(
                Role.BACKEND,
                Set.of(Skill.JAVA, Skill.SPRING_BOOT),
                8,
                Set.of(19, 20, 21, 22),
                ProjectGoal.PORTFOLIO,
                "  서울특별시 용산구  "
        );

        assertThat(matchingService.calculate(profile, project).status())
                .isEqualTo(MatchStatus.PASS);
    }

    @Test
    @DisplayName("대면 지역이 다르면 FAIL이다")
    void differentOfflineRegion() {
        Project project = project(
                Set.of(Skill.SPRING_BOOT),
                MeetingType.OFFLINE,
                "서울특별시 용산구"
        );

        Profile profile = profile(
                Role.BACKEND,
                Set.of(Skill.JAVA, Skill.SPRING_BOOT),
                8,
                Set.of(19, 20, 21, 22),
                ProjectGoal.PORTFOLIO,
                "서울특별시 강남구"
        );

        assertNoScore(
                matchingService.calculate(profile, project),
                MatchStatus.FAIL
        );
    }

    @Test
    @DisplayName("대면 지역이 누락되거나 공백뿐이면 UNKNOWN이다")
    void missingOfflineRegion() {
        Project project = project(
                Set.of(Skill.SPRING_BOOT),
                MeetingType.OFFLINE,
                "서울특별시 용산구"
        );

        for (String region : new String[]{null, "", "   "}) {
            Profile profile = profile(
                    Role.BACKEND,
                    Set.of(Skill.JAVA, Skill.SPRING_BOOT),
                    8,
                    Set.of(19, 20, 21, 22),
                    ProjectGoal.PORTFOLIO,
                    region
            );

            assertNoScore(
                    matchingService.calculate(profile, project),
                    MatchStatus.UNKNOWN
            );
        }
    }

    private void assertNoScore(MatchResult result, MatchStatus status) {
        assertThat(result.status()).isEqualTo(status);
        assertThat(result.score()).isNull();
        assertThat(result.reasons()).isNotEmpty();
    }

    private Profile defaultProfile() {
        return profile(
                Role.BACKEND,
                Set.of(Skill.JAVA, Skill.SPRING_BOOT),
                8,
                Set.of(19, 20, 21, 22),
                ProjectGoal.PORTFOLIO,
                null
        );
    }

    private Profile profile(
            Role role,
            Set<Skill> skills,
            Integer weeklyHours,
            Set<Integer> slots,
            ProjectGoal goal,
            String region
    ) {
        Member member = new Member(
                "member@example.com", "test-hash", "지원자"
        );

        Profile profile = new Profile(member);
        profile.update(
                role, skills, weeklyHours, slots,
                goal, region, null, null
        );

        return profile;
    }

    private Project defaultProject() {
        return project(
                Set.of(Skill.SPRING_BOOT),
                MeetingType.ONLINE,
                null
        );
    }

    private Project project(
            Set<Skill> preferredSkills,
            MeetingType meetingType,
            String region
    ) {
        Member author = new Member(
                "author@example.com", "test-hash", "작성자"
        );

        return new Project(
                author,
                "백엔드 팀원 모집",
                "테스트용 모집글입니다.",
                Role.BACKEND,
                Set.of(Skill.JAVA),
                preferredSkills,
                8,
                2,
                4,
                TEAM_SLOTS,
                ProjectGoal.PORTFOLIO,
                meetingType,
                region,
                LocalDateTime.of(2030, 1, 1, 0, 0)
        );
    }
}
