package com.tilog.domain.follow.repository;

import com.tilog.domain.follow.entity.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    Optional<Follow> findByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    long countByFollowing_Id(Long memberId);  // 팔로워 수

    long countByFollower_Id(Long memberId);   // 팔로잉 수

    // 팔로워 목록 (나를 팔로우한 사람들)
    Slice<Follow> findByFollowing_IdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 팔로잉 목록 (내가 팔로우한 사람들)
    Slice<Follow> findByFollower_IdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 팔로잉 피드용 - 내가 팔로우한 사람들의 ID 목록
    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :memberId")
    List<Long> findFollowingIdsByFollowerId(@Param("memberId") Long memberId);
}
