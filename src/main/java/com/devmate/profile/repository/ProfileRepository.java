package com.devmate.profile.repository;

import com.devmate.profile.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /** 회원 ID로 해당 회원의 프로필을 조회한다. */
    Optional<Profile> findByMember_Id(Long memberId);
}