package com.tilog.service;

import com.tilog.dto.history.WriteHistoryDailyCountResponse;
import com.tilog.dto.history.WriteHistoryDailyCountSummaryResponse;
import com.tilog.entity.WriteHistory;
import com.tilog.repository.WriteHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WriteHistoryQueryService {
    private final WriteHistoryRepository writeHistoryRepository;

    public WriteHistoryDailyCountSummaryResponse getDailyCounts(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);

        List<WriteHistory> histories =
                writeHistoryRepository.findAllByMemberIdAndWrittenDateBetweenOrderByWrittenDateAsc(
                        memberId,
                        startDate,
                        endDate
                );

        Map<LocalDate, Integer> writeCountMap = histories.stream()
                .collect(Collectors.toMap(
                        WriteHistory::getWrittenDate,
                        WriteHistory::getWriteCount
                ));

        List<WriteHistoryDailyCountResponse> items = LongStream
                .rangeClosed(0, ChronoUnit.DAYS.between(startDate, endDate))
                .mapToObj(startDate::plusDays)
                .map(date -> new WriteHistoryDailyCountResponse(
                        date,
                        writeCountMap.getOrDefault(date, 0)
                ))
                .toList();

        return new WriteHistoryDailyCountSummaryResponse(
                startDate,
                endDate,
                items
        );
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작일과 종료일은 필수입니다.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);

        if (days > 366) {
            throw new IllegalArgumentException("조회 기간은 최대 1년까지 가능합니다.");
        }
    }
}
