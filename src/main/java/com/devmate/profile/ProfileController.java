package com.devmate.profile;

import com.devmate.member.security.MemberPrincipal;
import com.devmate.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /** 로그인한 회원의 프로필 입력 화면을 표시한다. */
    @GetMapping("/profile")
    public String form(
            @AuthenticationPrincipal MemberPrincipal principal,
            Model model
    ) {
        ProfileForm form = profileService.getProfile(
                principal.getMemberId()
        );

        model.addAttribute("profileForm", form);

        return "profile/form";
    }

    /** 로그인한 회원의 프로필을 저장하거나 수정한다. */
    @PostMapping("/profile")
    public String save(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @ModelAttribute("profileForm") ProfileForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "profile/form";
        }

        profileService.saveProfile(principal.getMemberId(), form);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "프로필이 저장되었습니다."
        );

        return "redirect:/profile";
    }
}