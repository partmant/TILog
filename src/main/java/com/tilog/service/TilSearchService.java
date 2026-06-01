package com.tilog.service;

import com.tilog.dto.TilPostSearchCondition;
import com.tilog.dto.TilPostSummaryDto;
import com.tilog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TilSearchService {

    private final PostRepository postRepository;

    public Page<TilPostSummaryDto> search(TilPostSearchCondition condition, Pageable pageable) {
        return postRepository.searchPosts(condition, pageable);
    }
}