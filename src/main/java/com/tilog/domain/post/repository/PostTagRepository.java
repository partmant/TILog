package com.tilog.domain.post.repository;

import com.tilog.domain.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// [게시글---태그] DB 접근 Repository

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {
}