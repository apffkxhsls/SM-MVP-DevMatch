package com.devmate.profile.service;

import com.devmate.common.enums.ProjectGoal;
import com.devmate.common.enums.Role;
import com.devmate.common.enums.Skill;
import com.devmate.member.Member;
import com.devmate.member.repository.MemberRepository;
import com.devmate.profile.Profile;
import com.devmate.profile.ProfileForm;
import com.devmate.profile.repository.ProfileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:profile-service-test",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({
        ProfileService.class,
        ProfileServiceTest.ValidationConfig.class
})
class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    // JPA 테스트에서도 서비스의 @Validated 검증을 활성화한다.
    @TestConfiguration(proxyBeanMethods = false)
    static class ValidationConfig {

        @Bean
        static MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }
    }

    @Test
    @DisplayName("프로필 최초 저장 시 회원과 연결된 프로필을 생성한다")
    void createProfile() {
        Long memberId = createMember("first@example.com");

        Long profileId = profileService.saveProfile(memberId, validForm());
        entityManager.clear();

        Profile saved = profileRepository.findById(profileId).orElseThrow();

        assertThat(saved.getMember().getId()).isEqualTo(memberId);
        assertThat(saved.getRole()).isEqualTo(Role.BACKEND);
        assertThat(saved.getSkills())
                .containsExactlyInAnyOrder(Skill.JAVA, Skill.SPRING_BOOT);
        assertThat(saved.getWeeklyHours()).isEqualTo(10);
        assertThat(saved.getAvailableSlots())
                .containsExactlyInAnyOrder(19, 20, 21);
        assertThat(saved.getGoal()).isEqualTo(ProjectGoal.PORTFOLIO);
        assertThat(saved.getRegion()).isEqualTo("서울특별시 용산구");
        assertThat(saved.getPortfolioUrl()).isEqualTo("https://example.com");
        assertThat(saved.getIntroduction()).isEqualTo("백엔드 개발자입니다.");
        assertThat(profileRepository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("기존 프로필을 수정하면 같은 ID를 유지하고 선택 목록을 교체한다")
    void updateProfile() {
        Long memberId = createMember("update@example.com");
        Long originalId = profileService.saveProfile(memberId, validForm());
        entityManager.clear();

        ProfileForm changed = validForm();
        changed.setRole(Role.ANDROID);
        changed.setSkills(Set.of(Skill.KOTLIN));
        changed.setWeeklyHours(5);
        changed.setAvailableSlots(Set.of(43, 44));
        changed.setGoal(ProjectGoal.CONTEST);
        changed.setRegion("");
        changed.setPortfolioUrl("");
        changed.setIntroduction("");

        Long updatedId = profileService.saveProfile(memberId, changed);
        entityManager.clear();

        Profile saved = profileRepository.findById(updatedId).orElseThrow();

        assertThat(updatedId).isEqualTo(originalId);
        assertThat(profileRepository.count()).isEqualTo(1L);
        assertThat(saved.getMember().getId()).isEqualTo(memberId);
        assertThat(saved.getRole()).isEqualTo(Role.ANDROID);
        assertThat(saved.getSkills()).containsExactly(Skill.KOTLIN);
        assertThat(saved.getWeeklyHours()).isEqualTo(5);
        assertThat(saved.getAvailableSlots())
                .containsExactlyInAnyOrder(43, 44);
        assertThat(saved.getGoal()).isEqualTo(ProjectGoal.CONTEST);
        assertThat(saved.getRegion()).isNull();
        assertThat(saved.getPortfolioUrl()).isNull();
        assertThat(saved.getIntroduction()).isNull();
    }

    @Test
    @DisplayName("저장된 프로필을 입력 폼으로 조회한다")
    void getExistingProfile() {
        Long memberId = createMember("read@example.com");
        profileService.saveProfile(memberId, validForm());
        entityManager.clear();

        ProfileForm form = profileService.getProfile(memberId);

        assertThat(form.getRole()).isEqualTo(Role.BACKEND);
        assertThat(form.getSkills())
                .containsExactlyInAnyOrder(Skill.JAVA, Skill.SPRING_BOOT);
        assertThat(form.getWeeklyHours()).isEqualTo(10);
        assertThat(form.getAvailableSlots())
                .containsExactlyInAnyOrder(19, 20, 21);
        assertThat(form.getGoal()).isEqualTo(ProjectGoal.PORTFOLIO);
        assertThat(form.getRegion()).isEqualTo("서울특별시 용산구");
        assertThat(form.getPortfolioUrl()).isEqualTo("https://example.com");
        assertThat(form.getIntroduction()).isEqualTo("백엔드 개발자입니다.");
    }

    @Test
    @DisplayName("프로필이 없는 회원에게 빈 폼을 반환하고 DB에는 저장하지 않는다")
    void getEmptyProfile() {
        Long memberId = createMember("empty@example.com");

        ProfileForm form = profileService.getProfile(memberId);

        assertThat(form.getRole()).isNull();
        assertThat(form.getWeeklyHours()).isNull();
        assertThat(form.getGoal()).isNull();
        assertThat(form.getSkills()).isEmpty();
        assertThat(form.getAvailableSlots()).isEmpty();
        assertThat(profileRepository.count()).isZero();
    }

    @Test
    @DisplayName("다른 회원의 프로필을 반환하지 않는다")
    void separateMembers() {
        Long firstMemberId = createMember("one@example.com");
        Long secondMemberId = createMember("two@example.com");

        profileService.saveProfile(firstMemberId, validForm());
        entityManager.clear();

        ProfileForm secondForm = profileService.getProfile(secondMemberId);

        assertThat(secondForm.getRole()).isNull();
        assertThat(secondForm.getSkills()).isEmpty();
        assertThat(profileRepository.findByMember_Id(secondMemberId)).isEmpty();
    }

    @Test
    @DisplayName("유효하지 않은 입력은 저장하지 않는다")
    void rejectInvalidForm() {
        Long memberId = createMember("invalid@example.com");
        ProfileForm form = validForm();
        form.setWeeklyHours(0);

        assertThatThrownBy(() ->
                profileService.saveProfile(memberId, form)
        ).isInstanceOf(ConstraintViolationException.class);

        assertThat(profileRepository.count()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 회원의 프로필 저장을 거부한다")
    void rejectMissingMember() {
        assertThatThrownBy(() ->
                profileService.saveProfile(-1L, validForm())
        ).isInstanceOf(EntityNotFoundException.class);

        assertThat(profileRepository.count()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 회원의 프로필 조회를 거부한다")
    void rejectMissingMemberLookup() {
        assertThatThrownBy(() ->
                profileService.getProfile(-1L)
        ).isInstanceOf(EntityNotFoundException.class);
    }

    private Long createMember(String email) {
        return memberRepository.saveAndFlush(
                new Member(email, "test-hash", "테스트회원")
        ).getId();
    }

    private ProfileForm validForm() {
        ProfileForm form = new ProfileForm();
        form.setRole(Role.BACKEND);
        form.setSkills(Set.of(Skill.JAVA, Skill.SPRING_BOOT));
        form.setWeeklyHours(10);
        form.setAvailableSlots(Set.of(19, 20, 21));
        form.setGoal(ProjectGoal.PORTFOLIO);
        form.setRegion("서울특별시 용산구");
        form.setPortfolioUrl("https://example.com");
        form.setIntroduction("백엔드 개발자입니다.");
        return form;
    }
}