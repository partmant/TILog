package com.tilog.domain.writeHistory.service;

import com.tilog.domain.writeHistory.dto.WriteHistoryDailyCountResponse;
import com.tilog.domain.writeHistory.dto.WriteHistoryDailyCountSummaryResponse;
import com.tilog.domain.writeHistory.dto.WriteHistoryHeatmapItemResponse;
import com.tilog.domain.writeHistory.dto.WriteHistoryHeatmapResponse;
import com.tilog.domain.writeHistory.entity.WriteHistory;
import com.tilog.domain.writeHistory.repository.WriteHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

// 날짜별 작성 개수 조회
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
                writeHistoryRepository.findAllByMember_IdAndWrittenDateBetweenOrderByWrittenDateAsc(
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

    public WriteHistoryHeatmapResponse getHeatmap(Long memberId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        List<WriteHistory> histories = writeHistoryRepository
                .findAllByMember_IdAndWrittenDateBetweenOrderByWrittenDateAsc(memberId, startDate, endDate);

        Map<LocalDate, Integer> writeCountMap = histories.stream()
                .collect(Collectors.toMap(
                        WriteHistory::getWrittenDate,
                        WriteHistory::getWriteCount
                ));

        List<WriteHistoryHeatmapItemResponse> items = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    int writeCount = writeCountMap.getOrDefault(date, 0);
                    return new WriteHistoryHeatmapItemResponse(
                            date,
                            writeCount,
                            calculateLevel(writeCount)
                    );
                })
                .toList();

        // 조회 기간 동안 작성한 전체 TIL 수
        int totalWriteCount = items.stream()
                .mapToInt(WriteHistoryHeatmapItemResponse::writeCount)
                .sum();

        // 조회 기간 중 실제로 작성한 날짜 수
        int activeDays = (int) items.stream()
                .filter(item -> item.writeCount() > 0)
                .count();

        // 조회 기간 중 하루에 가장 많이 작성한 횟수
        int maxWriteCount = items.stream()
                .mapToInt(WriteHistoryHeatmapItemResponse::writeCount)
                .max()
                .orElse(0);

        return new WriteHistoryHeatmapResponse(
                startDate,
                endDate,
                totalWriteCount,
                activeDays,
                maxWriteCount,
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

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (days > 366) {
            throw new IllegalArgumentException("조회 기간은 최대 1년까지 가능합니다.");
        }
    }

    // 잔디 색상 레벨 계산
    private int calculateLevel(int writeCount) {
        if (writeCount <= 0) {
            return 0;
        }

        if (writeCount >= 4) {
            return 4;
        }

        return writeCount;
    }
}
