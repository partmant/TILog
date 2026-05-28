package com.tilog.domain.post.repository;

import com.tilog.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 게시글 DB 접근 Repository

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    //  삭제 안된 게시글 목록 조회
    List<Post> findByIsDeletedFalse();
}