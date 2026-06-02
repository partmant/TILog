package com.tilog.domain.post.dto;

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

    /** 태그 목록은 별도 배치 쿼리 후 주입 */
    @Setter
    private List<String> tags = new ArrayList<>();

    /** Querydsl Projections.constructor 용 생성자 */
    public TilPostSummaryDto(Long postId, String title, String authorNickname,
                              Difficulty difficulty, LocalDateTime createdAt,
                              Long likeCount, Long commentCount) {
        this.postId = postId;
        this.title = title;
        this.authorNickname = authorNickname;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
        this.likeCount = likeCount != null ? likeCount : 0L;
        this.commentCount = commentCount != null ? commentCount : 0L;
    }
}