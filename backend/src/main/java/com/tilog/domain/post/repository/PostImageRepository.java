package com.tilog.domain.post.repository;

import com.tilog.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 게시글 이미지 Repository

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    // fileUrl 목록에 해당하는 이미지 조회
    List<PostImage> findByFileUrlIn(List<String> fileUrls);

    // 게시글 하드 삭제 전 이미지 일괄 삭제 (데모 계정 데이터 초기화용)
    void deleteByPostId(Long postId);
}
