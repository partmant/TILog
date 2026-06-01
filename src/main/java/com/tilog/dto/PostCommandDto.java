package com.tilog.dto;

import com.tilog.entity.Difficulty;
import com.tilog.entity.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 게시글 요청 DTO

public class PostCommandDto {

    // 게시글 작성 요청 DTO
    @Getter
    @NoArgsConstructor
    public static class Create {
        private String title;
        private String content;
        private Difficulty difficulty;
        private Visibility visibility;
        private Integer studyTime;
    }

    // 게시글 수정 요청 DTO
    @Getter
    @NoArgsConstructor
    public static class Update {
        private String title;
        private String content;
        private Difficulty difficulty;
        private Visibility visibility;
        private Integer studyTime;
    }
}