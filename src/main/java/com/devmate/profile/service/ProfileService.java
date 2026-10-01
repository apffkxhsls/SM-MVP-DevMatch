package com.devmate.profile.service;

import com.devmate.member.Member;
import com.devmate.member.repository.MemberRepository;
import com.devmate.profile.Profile;
import com.devmate.profile.ProfileForm;
import com.devmate.profile.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final MemberRepository memberRepository;

    public ProfileService(
            ProfileRepository profileRepository,
            MemberRepository memberRepository
    ) {
        this.profileRepository = profileRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * 기존 프로필을 입력 폼으로 반환한다.
     * 프로필이 없는 회원에게는 빈 폼을 반환한다.
     */
    public ProfileForm getProfile(Long memberId) {
        findMember(memberId);

        return profileRepository.findByMember_Id(memberId)
                .map(this::toForm)
                .orElseGet(ProfileForm::new);
    }

    /**
     * 프로필이 없으면 생성하고, 있으면 수정한다.
     * memberId는 컨트롤러에서 인증된 회원 기준으로 전달해야 한다.
     */
    @Transactional
    public Long saveProfile(
            Long memberId,
            @NotNull @Valid ProfileForm form
    ) {
        Member member = findMember(memberId);

        Profile profile = profileRepository.findByMember_Id(memberId)
                .orElseGet(() -> new Profile(member));

        profile.update(
                form.getRole(),
                form.getSkills(),
                form.getWeeklyHours(),
                form.getAvailableSlots(),
                form.getGoal(),
                form.getRegion(),
                form.getPortfolioUrl(),
                form.getIntroduction()
        );

        return profileRepository.saveAndFlush(profile).getId();
    }

    private Member findMember(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID가 필요합니다.");
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new EntityNotFoundException("회원을 찾을 수 없습니다.")
                );
    }

    private ProfileForm toForm(Profile profile) {
        ProfileForm form = new ProfileForm();

        form.setRole(profile.getRole());
        form.setSkills(profile.getSkills());
        form.setWeeklyHours(profile.getWeeklyHours());
        form.setAvailableSlots(profile.getAvailableSlots());
        form.setGoal(profile.getGoal());
        form.setRegion(profile.getRegion());
        form.setPortfolioUrl(profile.getPortfolioUrl());
        form.setIntroduction(profile.getIntroduction());

        return form;
    }
}