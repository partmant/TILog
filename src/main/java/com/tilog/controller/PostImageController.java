package com.tilog.controller;

import com.tilog.dto.PostImageDto;
import com.tilog.service.PostImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// 게시글 이미지 컨트롤러

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/images")
public class PostImageController {
    private final PostImageService postImageService;

    // 게시글 이미지 업로드
    @PostMapping
    public PostImageDto.Upload uploadImage(@RequestParam("image") MultipartFile image) {
        return postImageService.upload(image);
    }
}