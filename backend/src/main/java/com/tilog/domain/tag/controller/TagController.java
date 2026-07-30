package com.tilog.domain.tag.controller;

import com.tilog.domain.tag.dto.PopularTagResponse;
import com.tilog.domain.tag.entity.Tag;
import com.tilog.domain.tag.repository.TagRepository;
import com.tilog.domain.tag.service.TagService;
import com.tilog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

// 태그 요청 처리 컨트롤러
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final TagRepository tagRepository;

    // 인기 태그 조회
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PopularTagResponse>>> getPopularTags(
            @RequestParam(required = false) Integer limit
    ) {
        List<PopularTagResponse> response = tagService.getPopularTags(limit);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 상세 검색 드롭다운 태그 목록 조회
    @GetMapping
    public ResponseEntity<List<String>> getTags() {
        List<String> tagNames = tagRepository.findAll().stream()
            .map(Tag::getName)
            .sorted(Comparator.naturalOrder())
            .toList();
        return ResponseEntity.ok(tagNames);
    }
}