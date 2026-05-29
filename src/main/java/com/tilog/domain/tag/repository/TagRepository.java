package com.tilog.domain.tag.repository;

import com.tilog.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 태그 DB 접근 Repository

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // 태그 이름으로 태그 조회
    Optional<Tag> findByName(String name);
}
