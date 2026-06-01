package com.tilog.dto;

import com.tilog.entity.enums.Difficulty;
import com.tilog.entity.enums.Visibility;
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