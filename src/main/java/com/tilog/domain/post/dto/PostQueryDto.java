package com.tilog.domain.post.dto;

import com.tilog.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 게시글 로직 처리 서비스

public class PostQueryDto {

    // 게시글 목록 조회
    @Getter
    @Builder
    public static class ListResponse {
        private Long postId;
        private String title;
        private String difficulty;
        private String visibility;
        private Integer viewCount;
        private Integer studyTime;
        private String nickname;
        private List<String> tagNames;

        public static ListResponse from(Post post, List<String> tagNames) {
            return ListResponse.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .difficulty(post.getDifficulty() == null ? null : post.getDifficulty().name())
                    .visibility(post.getVisibility().name())
                    .viewCount(post.getViewCount())
                    .studyTime(post.getStudyTime())
                    .nickname(post.getMember().getNickname())
                    .tagNames(tagNames)
                    .build();
        }
    }

    // 게시글 상세 조회
    @Getter
    @Builder
    public static class DetailResponse {
        private Long postId;
        private String title;
        private String content;
        private String difficulty;
        private String visibility;
        private Integer viewCount;
        private Integer studyTime;
        private String nickname;
        private LocalDateTime createdAt;
        private List<String> tagNames;

        public static DetailResponse from(Post post, List<String> tagNames) {
            return DetailResponse.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .difficulty(post.getDifficulty() == null ? null : post.getDifficulty().name())
                    .visibility(post.getVisibility().name())
                    .nickname(post.getMember().getNickname())
                    .createdAt(post.getCreatedAt())
                    .viewCount(post.getViewCount())
                    .studyTime(post.getStudyTime())
                    .tagNames(tagNames)
                    .build();
        }
    }
}
