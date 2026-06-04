package com.tilog.domain.post.controller;

import com.tilog.domain.tag.entity.Tag;
import com.tilog.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * GET /api/tags
 * 검색 드롭다운용 태그 목록 조회 (검색 기능 전용, 프론트 tagOptions.js 대체)
 * NOTE: SecurityConfig에서 permitAll() 처리 필요
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository tagRepository;

    @GetMapping
    public ResponseEntity<List<String>> getTags() {
        List<String> tagNames = tagRepository.findAll().stream()
                .map(Tag::getName)
                .sorted(Comparator.naturalOrder())
                .toList();
        return ResponseEntity.ok(tagNames);
    }
}