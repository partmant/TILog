package com.tilog.domain.post.service;

import com.tilog.domain.bookmark.repository.TilBookmarkRepository;
import com.tilog.domain.post.dto.TilPostSearchCondition;
import com.tilog.domain.post.dto.TilPostSummaryDto;
import com.tilog.domain.post.repository.PostRepository;
import com.tilog.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TilSearchService {

    private final PostRepository postRepository;
    private final TilBookmarkRepository bookmarkRepository;

    public Page<TilPostSummaryDto> search(TilPostSearchCondition condition, Pageable pageable) {
        Page<TilPostSummaryDto> page = postRepository.searchPosts(condition, pageable);

        // 로그인 사용자의 즐겨찾기 여부 주입 (비로그인이면 false 유지)
        try {
            Long memberId = SecurityUtil.getCurrentMemberId();
            if (memberId != null && page.hasContent()) {
                List<Long> postIds = page.getContent().stream()
                        .map(TilPostSummaryDto::getPostId)
                        .toList();
                Set<Long> bookmarkedIds = bookmarkRepository
                        .findBookmarkedPostIdsByMemberIdAndPostIds(memberId, postIds);
                page.getContent().forEach(dto ->
                        dto.setBookmarked(bookmarkedIds.contains(dto.getPostId()))
                );
            }
        } catch (Exception ignored) {
            // 비로그인 상태에서는 isBookmarked = false (기본값) 유지
        }

        return page;
    }
}
