package com.devmate.member.service;

import com.devmate.member.Member;
import com.devmate.member.SignupForm;
import com.devmate.member.exception.DuplicateEmailException;
import com.devmate.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 검증된 회원가입 정보로 회원을 등록한다.
     *
     * @return 생성된 회원 ID
     */
    @Transactional
    public Long signup(SignupForm form) {
        String email = form.getEmail();

        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        String passwordHash = passwordEncoder.encode(form.getPassword());

        Member member = new Member(
                email,
                passwordHash,
                form.getName()
        );

        Member savedMember = memberRepository.saveAndFlush(member);

        return savedMember.getId();
    }

    @Transactional(readOnly = true)
    public boolean isEmailRegistered(String email) {
        return memberRepository.existsByEmail(email);
    }
}
