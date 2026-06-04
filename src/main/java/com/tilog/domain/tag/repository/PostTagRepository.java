package com.tilog.domain.tag.repository;

import com.tilog.domain.post.entity.Visibility;
import com.tilog.domain.tag.dto.PopularTagResponse;
import com.tilog.domain.tag.entity.PostTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// [게시글---태그] DB 접근 Repository

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    // 게시글 ID로 태그 매핑 목록 조회
    List<PostTag> findByPost_Id(Long postId);

    @Modifying
    @Query("delete from PostTag pt where pt.post.id = :postId")
    // 게시글 ID로 기존 태그 매핑 삭제
    void deleteByPost_Id(Long postId);

    // 공개 게시글 기준 인기 태그 조회
    @Query("""
            select new com.tilog.domain.tag.dto.PopularTagResponse(pt.tag.name, count(pt))
            from PostTag pt
            where pt.post.isDeleted = false
              and pt.post.visibility = :visibility
            group by pt.tag.name
            order by count(pt) desc, pt.tag.name asc
            """)
    List<PopularTagResponse> findPopularTags(@Param("visibility") Visibility visibility, Pageable pageable);
}
