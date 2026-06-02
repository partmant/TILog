package com.tilog.repository;

import com.tilog.entity.Member;
import com.tilog.entity.Post;
import com.tilog.entity.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 게시글 DB 접근 Repository

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 삭제되지 않았고, 임시저장이 아닌 게시글 목록 조회
    List<Post> findByIsDeletedFalseAndVisibilityNot(Visibility visibility);

    // 특정 회원이 작성한 삭제되지 않은 게시글 목록 조회
    // PUBLIC, PRIVATE, DRAFT 모두 포함
    List<Post> findByMemberAndIsDeletedFalse(Member member);

    // 특정 회원의 임시저장 게시글 목록 조회
    List<Post> findByMemberAndVisibilityAndIsDeletedFalse(Member member, Visibility visibility);
}
