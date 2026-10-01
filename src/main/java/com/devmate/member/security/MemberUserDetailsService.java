package com.devmate.member.security;

import com.devmate.member.Member;
import com.devmate.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public MemberUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException(
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        String email = username.strip().toLowerCase(Locale.ROOT);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "이메일 또는 비밀번호가 올바르지 않습니다."
                ));

        return new MemberPrincipal(
                member.getId(),
                member.getEmail(),
                member.getPasswordHash()
        );
    }
}