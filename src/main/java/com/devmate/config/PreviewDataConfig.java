package com.devmate.config;

import com.devmate.member.Member;
import com.devmate.member.repository.MemberRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("preview")
public class PreviewDataConfig {

    @Bean
    public ApplicationRunner previewMemberInitializer(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            String email = "preview@example.com";

            if (!memberRepository.existsByEmail(email)) {
                Member member = new Member(
                        email,
                        passwordEncoder.encode("Preview123!"),
                        "미리보기회원"
                );

                memberRepository.saveAndFlush(member);
            }
        };
    }
}
