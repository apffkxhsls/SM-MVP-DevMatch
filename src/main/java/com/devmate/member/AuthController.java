package com.devmate.member;

import com.devmate.member.exception.DuplicateEmailException;
import com.devmate.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final MemberService memberService;

    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 회원가입 화면을 표시한다.
     */
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "member/signup";
    }

    /**
     * 입력값을 검증하고 회원가입을 처리한다.
     */
    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute("signupForm") SignupForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            form.setPassword(null);
            return "member/signup";
        }

        try {
            memberService.signup(form);
        } catch (DuplicateEmailException e) {
            return duplicateEmail(form, bindingResult);
        } catch (DataIntegrityViolationException e) {
            // 동시에 들어온 가입 요청이 DB의 중복 제약에 걸린 경우
            if (memberService.isEmailRegistered(form.getEmail())) {
                return duplicateEmail(form, bindingResult);
            }

            // 다른 DB 오류를 이메일 중복으로 처리하지 않는다.
            throw e;
        }

        return "redirect:/login";
    }

    private String duplicateEmail(
            SignupForm form,
            BindingResult bindingResult
    ) {
        bindingResult.rejectValue(
                "email",
                "duplicate",
                "이미 사용 중인 이메일입니다."
        );

        form.setPassword(null);
        return "member/signup";
    }
}
