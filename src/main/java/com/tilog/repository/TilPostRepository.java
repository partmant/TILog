package com.tilog.repository;

import com.tilog.entity.TilPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TilPostRepository extends JpaRepository<TilPost, Long>, TilPostRepositoryCustom {
}