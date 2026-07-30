package com.tilog.domain.tag.service;

import com.tilog.domain.post.entity.Visibility;
import com.tilog.domain.tag.dto.PopularTagResponse;
import com.tilog.domain.tag.repository.PostTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 태그 로직 처리 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private static final int DEFAULT_POPULAR_TAG_LIMIT = 10;

    private final PostTagRepository postTagRepository;

    // 공개 게시글 기준 인기 태그 조회
    public List<PopularTagResponse> getPopularTags(Integer limit) {
        int resolvedLimit = resolveLimit(limit);

        return postTagRepository.findPopularTags(
                Visibility.PUBLIC,
                PageRequest.of(0, resolvedLimit)
        );
    }

    // 인기 태그 조회 개수 보정
    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_POPULAR_TAG_LIMIT;
        }

        return Math.min(limit, DEFAULT_POPULAR_TAG_LIMIT);
    }
}
