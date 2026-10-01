package com.devmate.member.service;

import com.devmate.member.Member;
import com.devmate.member.SignupForm;
import com.devmate.member.exception.DuplicateEmailException;
import com.devmate.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:member-service-test",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({
        MemberService.class,
        MemberServiceTest.PasswordTestConfig.class
})
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @TestConfiguration(proxyBeanMethods = false)
    static class PasswordTestConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Test
    @DisplayName("회원가입 시 정규화된 이메일과 BCrypt 비밀번호를 저장한다")
    void signupSuccess() {
        SignupForm form = createForm("  User@Example.com  ");
        form.setName("  DevMate  ");
        String rawPassword = form.getPassword();

        Long memberId = memberService.signup(form);

        // 메모리의 객체 대신 DB에서 다시 조회한다.
        entityManager.clear();

        Member member = memberRepository.findById(memberId).orElseThrow();

        assertThat(member.getEmail()).isEqualTo("user@example.com");
        assertThat(member.getName()).isEqualTo("DevMate");
        assertThat(member.getCreatedAt()).isNotNull();

        assertThat(member.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(
                rawPassword, member.getPasswordHash()
        )).isTrue();
        assertThat(passwordEncoder.matches(
                "wrong-password", member.getPasswordHash()
        )).isFalse();

        assertThat(memberRepository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이미 가입한 이메일이면 회원가입을 거부한다")
    void rejectDuplicateEmail() {
        memberService.signup(createForm("user@example.com"));
        SignupForm duplicate = createForm("user@example.com");

        assertThatThrownBy(() -> memberService.signup(duplicate))
                .isInstanceOf(DuplicateEmailException.class);

        assertThat(memberRepository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("대소문자와 앞뒤 공백이 달라도 같은 이메일이면 거부한다")
    void rejectDuplicateEmailIgnoringCase() {
        memberService.signup(createForm("User@Example.com"));
        SignupForm duplicate = createForm("  USER@example.COM  ");

        assertThatThrownBy(() -> memberService.signup(duplicate))
                .isInstanceOf(DuplicateEmailException.class);

        assertThat(memberRepository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이메일 등록 여부를 확인할 수 있다")
    void checkRegisteredEmail() {
        memberService.signup(createForm("user@example.com"));

        assertThat(memberService.isEmailRegistered("user@example.com"))
                .isTrue();
        assertThat(memberService.isEmailRegistered("other@example.com"))
                .isFalse();
    }

    private SignupForm createForm(String email) {
        SignupForm form = new SignupForm();
        form.setEmail(email);
        form.setPassword("TestPass123!");
        form.setName("테스트회원");
        return form;
    }
}