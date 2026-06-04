package com.tilog.domain.post.dto;

import com.tilog.domain.post.entity.Difficulty;
import com.tilog.domain.post.entity.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
        private List<String> tagNames;
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
        private List<String> tagNames;
    }
}