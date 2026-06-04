package com.tilog.domain.post.repository;

import com.tilog.domain.post.dto.TilPostSearchCondition;
import com.tilog.domain.post.dto.TilPostSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

    Page<TilPostSummaryDto> searchPosts(TilPostSearchCondition condition, Pageable pageable);
}