package com.devmate.project;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectController {

    /**
     * 팀 찾기 화면을 반환한다.
     * 현재는 하드코딩된 모집글 카드 목록을 표시한다.
     *
     * @return 모집글 목록 템플릿 경로
     */
    @GetMapping("/projects")
    public String list() {
        return "project/list";
    }

    /**
     * 모집글 작성 화면을 반환한다.
     * 실제 등록 및 DB 저장은 아직 연결하지 않는다.
     */
    @GetMapping("/projects/new")
    public String createForm() {
        return "project/form";
    }
}
