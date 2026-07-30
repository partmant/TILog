package com.tilog.domain.bookmark.controller;

import com.tilog.domain.bookmark.dto.BookmarkResponse;
import com.tilog.domain.bookmark.dto.BookmarkedPostResponse;
import com.tilog.domain.bookmark.service.BookmarkService;
import com.tilog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /** 즐겨찾기 등록 */
    @PostMapping("/api/posts/{postId}/bookmarks")
    public ResponseEntity<ApiResponse<BookmarkResponse>> addBookmark(@PathVariable Long postId) {
        BookmarkResponse response = bookmarkService.addBookmark(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 즐겨찾기 해제 */
    @DeleteMapping("/api/posts/{postId}/bookmarks")
    public ResponseEntity<ApiResponse<BookmarkResponse>> removeBookmark(@PathVariable Long postId) {
        BookmarkResponse response = bookmarkService.removeBookmark(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 내가 즐겨찾기한 TIL 목록 조회 (검색/난이도/정렬 지원) */
    @GetMapping("/api/posts/bookmarks/me")
    public ResponseEntity<Page<BookmarkedPostResponse>> getMyBookmarkedPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "LATEST") String sortType,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<BookmarkedPostResponse> result =
                bookmarkService.getMyBookmarkedPosts(keyword, difficulty, sortType, pageable);
        return ResponseEntity.ok(result);
    }
}
