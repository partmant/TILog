package com.tilog.repository;

import com.tilog.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// [게시글---태그] DB 접근 Repository

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    // 게시글 ID로 태그 매핑 목록 조회
    List<PostTag> findByPost_Id(Long postId);

    @Modifying
    @Query("delete from PostTag pt where pt.post.id = :postId")
    // 게시글 ID로 기존 태그 매핑 삭제
    void deleteByPost_Id(Long postId);
}