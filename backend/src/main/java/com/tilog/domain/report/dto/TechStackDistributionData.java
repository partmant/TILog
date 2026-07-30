package com.tilog.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/** tech_stack_distribution_data 컬럼에 JSON으로 저장되는 기술 스택 분포 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechStackDistributionData {

    /** key: "BACKEND" | "FRONTEND" | "SECURITY" | "CS" | "OTHER", value: 비율(%) */
    private Map<String, Integer> categories;

    /** key: 태그 이름 (ex. "Spring"), value: 게시글 수 */
    private Map<String, Integer> tags;
}