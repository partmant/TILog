package com.tilog.repository;

import com.tilog.entity.TilPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TilPostLikeRepository extends JpaRepository<TilPostLike, Long> {

    boolean existsByPost_IdAndMember_Id(Long postId, Long memberId);

    Optional<TilPostLike> findByPost_IdAndMember_Id(Long postId, Long memberId);

    long countByPost_Id(Long postId);
}
