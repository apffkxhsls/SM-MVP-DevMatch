package com.devmate.member.security;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MemberPrincipal implements UserDetails, CredentialsContainer {

    private static final long serialVersionUID = 1L;

    private final Long memberId;
    private final String email;
    private String passwordHash;

    public MemberPrincipal(
            Long memberId,
            String email,
            String passwordHash
    ) {
        this.memberId = memberId;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // 이후 본인의 프로필을 조회·저장할 때 사용할 회원 ID
    public Long getMemberId() {
        return memberId;
    }

    // 로그인 식별자로 이메일 반환
    @Override
    public String getUsername() {
        return email;
    }

    // 비밀번호 비교에 사용할 DB의 해시 반환
    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));  // 일반 회원 권한
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // 인증 완료 후 객체에서 비밀번호 해시 제거
    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }
}