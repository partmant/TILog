package com.tilog.domain.writeHistory.repository;

import com.tilog.domain.writeHistory.entity.WriteHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WriteHistoryRepository extends JpaRepository<WriteHistory, Long> {
    Optional<WriteHistory> findByMember_IdAndWrittenDate(Long memberId, LocalDate writtenDate);

    List<WriteHistory> findAllByMember_IdAndWrittenDateBetweenOrderByWrittenDateAsc(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    int countByMember_IdAndWrittenDateBetweenAndWriteCountGreaterThan(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            int writeCount
    );
}
