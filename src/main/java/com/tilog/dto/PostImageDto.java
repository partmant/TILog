package com.tilog.dto;

// 게시글 이미지 DTO

import lombok.AllArgsConstructor;
import lombok.Getter;

public class PostImageDto {

    // 게시글 이미지 업로드 응답 DTO
    @Getter
    @AllArgsConstructor
    public static class Upload {
        private String imageUrl;
    }
}