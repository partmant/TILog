package com.tilog.domain.post.dto;

import com.tilog.domain.post.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter  // @ModelAttribute 바인딩에 필요 — 없으면 모든 파라미터가 null로 무시됨
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilPostSearchCondition {

    /** 제목/본문 키워드 검색 */
    private String keyword;

    /** 작성자 닉네임 검색 */
    private String nickname;

    /** 기술 스택 태그 이름 검색 */
    private String tagName;

    /** 난이도 필터 (EASY / NORMAL / HARD) */
    private Difficulty difficulty;

    /** 작성 기간 시작일 */
    private LocalDate from;

    /** 작성 기간 종료일 */
    private LocalDate to;

    /** 정렬 기준 (기본: LATEST) */
    @Builder.Default
    private TilSortType sort = TilSortType.LATEST;
}