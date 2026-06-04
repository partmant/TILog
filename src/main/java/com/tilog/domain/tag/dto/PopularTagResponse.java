package com.tilog.domain.tag.dto;

import lombok.Getter;

// 인기 태그 응답 DTO
@Getter
public class PopularTagResponse {

    private final String tagName;
    private final long count;

    public PopularTagResponse(String tagName, Long count) {
        this.tagName = tagName;
        this.count = count == null ? 0 : count;
    }
}
