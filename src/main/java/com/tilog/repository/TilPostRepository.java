package com.tilog.repository;

import com.tilog.entity.TilPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TilPostRepository extends JpaRepository<TilPost, Long> {
}
