package com.devmate.project.repository;

import com.devmate.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** 특정 회원이 작성한 모집글을 페이지 단위로 조회한다. */
    Page<Project> findByAuthor_Id(Long authorId, Pageable pageable);
}