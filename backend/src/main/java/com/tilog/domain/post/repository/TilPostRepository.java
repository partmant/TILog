package com.tilog.domain.post.repository;

import com.tilog.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TilPostRepository extends JpaRepository<Post, Long> {
}
