package com.devmate.member;

import com.devmate.member.exception.DuplicateEmailException;
import com.devmate.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.view.AbstractView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;
    private MemberService memberService;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        // HTML 파일 없이 컨트롤러의 모델과 뷰 이름을 검증한다.
        AbstractView emptyView = new AbstractView() {
            @Override
            protected void renderMergedOutputModel(
                    Map<String, Object> model,
                    HttpServletRequest request,
                    HttpServletResponse response
            ) {
                // 화면 렌더링은 프론트 연동 후 확인한다.
            }
        };

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(memberService))
                .setValidator(validator)
                .setViewResolvers((viewName, locale) -> {
                    if (viewName.startsWith("redirect:")) {
                        return new org.springframework.web.servlet.view.RedirectView(
                                viewName.substring("redirect:".length())
                        );
                    }
                    return emptyView;
                })
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    @DisplayName("회원가입 화면에 빈 입력 객체를 전달한다")
    void signupPage() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/signup"))
                .andExpect(model().attributeExists("signupForm"));

        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("가입 성공 시 정규화된 입력값을 전달하고 로그인으로 이동한다")
    void signupSuccess() throws Exception {
        when(memberService.signup(any(SignupForm.class))).thenReturn(1L);

        mockMvc.perform(post("/signup")
                        .param("email", "  User@Example.com  ")
                        .param("password", "TestPass123!")
                        .param("name", "  DevMate  "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        var captor = org.mockito.ArgumentCaptor.forClass(SignupForm.class);
        verify(memberService).signup(captor.capture());

        SignupForm submitted = captor.getValue();
        assertThat(submitted.getEmail()).isEqualTo("user@example.com");
        assertThat(submitted.getName()).isEqualTo("DevMate");
        assertThat(submitted.getPassword()).isEqualTo("TestPass123!");
    }

    @Test
    @DisplayName("입력 오류 시 저장하지 않고 이메일과 이름만 유지한다")
    void invalidInput() throws Exception {
        MvcResult result = mockMvc.perform(post("/signup")
                        .param("email", "invalid-email")
                        .param("password", "short")
                        .param("name", "DevMate"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/signup"))
                .andExpect(model().attributeHasFieldErrors(
                        "signupForm", "email", "password"
                ))
                .andReturn();

        assertRetainedFields(result, "invalid-email", "DevMate");
        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("필수 항목이 누락되면 회원가입을 처리하지 않는다")
    void missingFields() throws Exception {
        mockMvc.perform(post("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/signup"))
                .andExpect(model().attributeHasFieldErrors(
                        "signupForm", "email", "password", "name"
                ));

        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("중복 이메일이면 이메일 오류를 표시하고 비밀번호를 제거한다")
    void duplicateEmail() throws Exception {
        when(memberService.signup(any(SignupForm.class)))
                .thenThrow(new DuplicateEmailException());

        MvcResult result = mockMvc.perform(post("/signup")
                        .param("email", "user@example.com")
                        .param("password", "TestPass123!")
                        .param("name", "DevMate"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/signup"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "signupForm", "email", "duplicate"
                ))
                .andReturn();

        assertRetainedFields(result, "user@example.com", "DevMate");
    }

    @Test
    @DisplayName("DB 중복 오류도 이메일 필드 오류로 처리한다")
    void databaseDuplicateEmail() throws Exception {
        when(memberService.signup(any(SignupForm.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        when(memberService.isEmailRegistered("user@example.com"))
                .thenReturn(true);

        MvcResult result = mockMvc.perform(post("/signup")
                        .param("email", "user@example.com")
                        .param("password", "TestPass123!")
                        .param("name", "DevMate"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/signup"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "signupForm", "email", "duplicate"
                ))
                .andReturn();

        assertRetainedFields(result, "user@example.com", "DevMate");
        verify(memberService).isEmailRegistered("user@example.com");
    }

    private void assertRetainedFields(
            MvcResult result,
            String expectedEmail,
            String expectedName
    ) {
        SignupForm form = (SignupForm) result.getModelAndView()
                .getModel()
                .get("signupForm");

        assertThat(form.getEmail()).isEqualTo(expectedEmail);
        assertThat(form.getName()).isEqualTo(expectedName);
        assertThat(form.getPassword()).isNull();
    }
}