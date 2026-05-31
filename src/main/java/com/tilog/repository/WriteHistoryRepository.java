package com.tilog.repository;

import com.tilog.entity.WriteHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WriteHistoryRepository extends JpaRepository<WriteHistory, Long> {
    Optional<WriteHistory> findByMemberIdAndWrittenDate(Long memberId, LocalDate writtenDate);

    List<WriteHistory> findAllByMemberIdAndWrittenDateBetweenOrderByWrittenDateAsc(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );
}
