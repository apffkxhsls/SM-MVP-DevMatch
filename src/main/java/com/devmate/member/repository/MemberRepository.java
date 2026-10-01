package com.devmate.member.repository;

import com.devmate.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 이메일로 회원을 조회한다.
     */
    Optional<Member> findByEmail(String email);

    /**
     * 동일한 이메일의 회원이 존재하는지 확인한다.
     */
    boolean existsByEmail(String email);

}
