package com.tilog.domain.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BookmarkResponse {

    @JsonProperty("isBookmarked")
    private final boolean isBookmarked;

    private BookmarkResponse(boolean isBookmarked) {
        this.isBookmarked = isBookmarked;
    }

    public static BookmarkResponse of(boolean isBookmarked) {
        return new BookmarkResponse(isBookmarked);
    }
}
