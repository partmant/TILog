package com.tilog.repository;

import com.tilog.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    Optional<Follow> findByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    long countByFollowing_Id(Long memberId);  // 팔로워 수

    long countByFollower_Id(Long memberId);   // 팔로잉 수
}
