package com.tilog.domain.post.dto;

import com.tilog.domain.post.entity.Difficulty;
import com.tilog.domain.post.entity.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 게시글 요청 DTO

public class PostCommandDto {

    @Getter
    @NoArgsConstructor
    // 게시글 작성 요청 DTO
    public static class Create {
        private String title;
        private String content;
        private Difficulty difficulty;
        private Visibility visibility;
        private Integer studyTime;
    }

    @Getter
    @NoArgsConstructor
    // 게시글 수정 요청 DTO
    public static class Update {
        private String title;
        private String content;
        private Difficulty difficulty;
        private Visibility visibility;
        private Integer studyTime;
    }
}