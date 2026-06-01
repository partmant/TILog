package com.tilog.repository;

import com.tilog.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 게시글 이미지 Repository

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    // fileUrl 목록에 해당하는 이미지 조회
    List<PostImage> findByFileUrlIn(List<String> fileUrls);
}
