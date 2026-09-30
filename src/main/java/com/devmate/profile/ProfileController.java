package com.devmate.profile;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    /**
     * 프로필 입력 화면을 반환한다.
     * 저장 기능은 아직 연결하지 않는다.
     */
    @GetMapping("/profile")
    public String form() {
        return "profile/form";
    }
}