package com.devmate.member;

import com.devmate.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:member-test",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("회원을 저장하면 회원 정보와 생성 시간이 DB에 저장된다")
    void saveMember() {
        Member member = new Member(
                "test@example.com",
                "test-hash",
                "테스트회원"
        );

        Member saved = memberRepository.saveAndFlush(member);
        Long savedId = saved.getId();

        // 메모리에 남아 있는 객체 대신 DB에서 다시 조회한다.
        entityManager.clear();

        Member found = memberRepository.findById(savedId).orElseThrow();

        assertThat(found.getEmail()).isEqualTo("test@example.com");
        assertThat(found.getPasswordHash()).isEqualTo("test-hash");
        assertThat(found.getName()).isEqualTo("테스트회원");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("등록된 이메일로 회원을 조회할 수 있다")
    void findByEmail() {
        memberRepository.saveAndFlush(
                new Member("test@example.com", "test-hash", "테스트회원")
        );
        entityManager.clear();

        var result = memberRepository.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getName()).isEqualTo("테스트회원");
    }

    @Test
    @DisplayName("등록되지 않은 이메일로 조회하면 결과가 없다")
    void findByEmailNotFound() {
        var result = memberRepository.findByEmail("unknown@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일 등록 여부를 확인할 수 있다")
    void existsByEmail() {
        memberRepository.saveAndFlush(
                new Member("test@example.com", "test-hash", "테스트회원")
        );

        assertThat(memberRepository.existsByEmail("test@example.com"))
                .isTrue();
        assertThat(memberRepository.existsByEmail("unknown@example.com"))
                .isFalse();
    }

    @Test
    @DisplayName("동일한 이메일로 두 명의 회원을 저장할 수 없다")
    void rejectDuplicateEmail() {
        memberRepository.saveAndFlush(
                new Member("test@example.com", "test-hash", "첫번째회원")
        );

        Member duplicate = new Member(
                "test@example.com",
                "another-test-hash",
                "두번째회원"
        );

        assertThatThrownBy(() -> memberRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}