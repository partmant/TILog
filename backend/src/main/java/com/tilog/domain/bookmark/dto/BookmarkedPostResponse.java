package com.tilog.domain.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tilog.domain.bookmark.entity.TilBookmark;
import com.tilog.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BookmarkedPostResponse {

    private Long postId;
    private String title;
    private String nickname;
    private String difficulty;
    private Integer studyTime;
    private Integer viewCount;
    private long likeCount;
    private long commentCount;
    private List<String> tagNames;
    private LocalDateTime createdAt;
    @JsonProperty("isBookmarked")
    private boolean isBookmarked;

    public static BookmarkedPostResponse from(TilBookmark bookmark, List<String> tagNames,
                                               long likeCount, long commentCount) {
        Post post = bookmark.getPost();
        return BookmarkedPostResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .nickname(post.getMember().getNickname())
                .difficulty(post.getDifficulty() == null ? null : post.getDifficulty().name())
                .studyTime(post.getStudyTime())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .tagNames(tagNames)
                .createdAt(post.getCreatedAt())
                .isBookmarked(true)
                .build();
    }
}
