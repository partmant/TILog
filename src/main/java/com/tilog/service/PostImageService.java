package com.tilog.service;

import com.tilog.dto.PostImageDto;
import com.tilog.entity.PostImage;
import com.tilog.repository.PostImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// 게시글 이미지 서비스

@Service
@RequiredArgsConstructor
public class PostImageService {
    private final PostImageRepository postImageRepository;

    @Value("${file.upload-path}")
    private String uploadPath;

    // 게시글 이미지 업로드
    public PostImageDto.Upload upload(MultipartFile image) {
        try {
            // 원본 파일명
            String originalName = image.getOriginalFilename();

            if (originalName == null || originalName.isBlank()) {
                throw new IllegalArgumentException("파일명이 존재하지 않습니다.");
            }

            // 확장자 추출
            String extension = extractExtension(originalName);

            // 저장 파일명 생성
            String storedName = UUID.randomUUID() + extension;

            // 저장 경로 생성
            Path directory = Paths.get(uploadPath);

            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 실제 저장 경로
            Path savePath = directory.resolve(storedName);

            // 파일 저장
            image.transferTo(savePath);

            // 접근 URL
            String fileUrl = "/uploads/post/" + storedName;

            // 이미지 엔티티 생성
            PostImage postImage = new PostImage(
                    originalName,
                    storedName,
                    fileUrl,
                    image.getSize(),
                    image.getContentType()
            );

            // DB 저장
            postImageRepository.save(postImage);

            // 응답 반환
            return new PostImageDto.Upload(fileUrl);

        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    // 파일 확장자 추출
    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf("."));
    }
}