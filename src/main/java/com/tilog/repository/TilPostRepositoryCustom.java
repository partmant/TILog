package com.tilog.repository;

import com.tilog.dto.TilPostSearchCondition;
import com.tilog.dto.TilPostSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TilPostRepositoryCustom {

    Page<TilPostSummaryDto> searchPosts(TilPostSearchCondition condition, Pageable pageable);
}