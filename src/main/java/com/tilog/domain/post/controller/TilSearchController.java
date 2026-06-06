package com.tilog.domain.post.controller;

import com.tilog.domain.post.dto.TilPostSearchCondition;
import com.tilog.domain.post.dto.TilPostSummaryDto;
import com.tilog.domain.post.service.TilSearchService;
import com.tilog.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/tils
 *
 * 쿼리 파라미터:
 *   keyword    - 제목/본문 키워드 (선택)
 *   nickname   - 작성자 닉네임 (선택)
 *   tagName    - 기술 스택 태그 이름 (선택)
 *   difficulty - EASY | NORMAL | HARD (선택)
 *   from       - 시작일 yyyy-MM-dd (선택)
 *   to         - 종료일 yyyy-MM-dd (선택)
 *   sort       - LATEST | LIKES | COMMENTS (기본: LATEST)
 *   page       - 페이지 번호 0-based (기본: 0)
 *   size       - 페이지 크기 (기본: 10)
 *
 * NOTE: SecurityConfig에서 이 경로를 permitAll() 처리해야 접근 가능
 */
@RestController
@RequestMapping("/api/tils")
@RequiredArgsConstructor
public class TilSearchController {

    private final TilSearchService tilSearchService;

    @GetMapping
    public ResponseEntity<Page<TilPostSummaryDto>> search(
            @ModelAttribute TilPostSearchCondition condition,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<TilPostSummaryDto> result = tilSearchService.search(condition, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/tils/me
     *
     * 로그인한 사용자의 TIL 검색 (PUBLIC + PRIVATE 포함, DRAFT 제외)
     * nickname 파라미터는 무시되고 JWT에서 추출한 memberId로 필터링
     */
    @GetMapping("/me")
    public ResponseEntity<Page<TilPostSummaryDto>> searchMyTils(
            @ModelAttribute TilPostSearchCondition condition,
            @PageableDefault(size = 10) Pageable pageable) {

        Long memberId = SecurityUtil.getCurrentMemberId();
        condition.setMemberId(memberId);

        Page<TilPostSummaryDto> result = tilSearchService.search(condition, pageable);
        return ResponseEntity.ok(result);
    }
}