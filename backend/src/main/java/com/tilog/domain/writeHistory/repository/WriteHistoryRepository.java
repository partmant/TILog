package com.tilog.domain.writeHistory.repository;

import com.tilog.domain.writeHistory.entity.WriteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 스트릭/잔디 히트맵 초기화용 (데모 계정 데이터 초기화)
    @Modifying
    @Query("DELETE FROM WriteHistory w WHERE w.member.id = :memberId")
    void deleteByMember_Id(@Param("memberId") Long memberId);
}
