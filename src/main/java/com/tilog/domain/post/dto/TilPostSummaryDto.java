package com.tilog.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tilog.domain.post.entity.Difficulty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TilPostSummaryDto {

    private final Long postId;
    private final String title;
    private final String authorNickname;
    private final Difficulty difficulty;
    private final LocalDateTime createdAt;
    private final long likeCount;
    private final long commentCount;
    private final Integer viewCount;
    private final Integer studyTime;

    /** 태그 목록은 별도 배치 쿼리 후 주입 */
    @Setter
    private List<String> tags = new ArrayList<>();

    /**
     * 즐겨찾기 여부는 로그인 사용자 기준으로 별도 주입 (비로그인 시 false)
     * @JsonProperty: boolean 필드의 is 접두사를 Jackson이 제거하지 않도록 강제 지정
     */
    @Setter
    @JsonProperty("isBookmarked")
    private boolean isBookmarked = false;

    /** Querydsl Projections.constructor 용 생성자 */
    public TilPostSummaryDto(Long postId, String title, String authorNickname,
                              Difficulty difficulty, LocalDateTime createdAt,
                              Long likeCount, Long commentCount,
                              Integer viewCount, Integer studyTime) {
        this.postId = postId;
        this.title = title;
        this.authorNickname = authorNickname;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
        this.likeCount = likeCount != null ? likeCount : 0L;
        this.commentCount = commentCount != null ? commentCount : 0L;
        this.viewCount = viewCount != null ? viewCount : 0;
        this.studyTime = studyTime;
    }
}