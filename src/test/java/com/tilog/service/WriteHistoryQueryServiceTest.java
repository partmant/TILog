package com.tilog.service;

import com.tilog.dto.history.WriteHistoryDailyCountResponse;
import com.tilog.dto.history.WriteHistoryDailyCountSummaryResponse;
import com.tilog.dto.history.WriteHistoryHeatmapItemResponse;
import com.tilog.dto.history.WriteHistoryHeatmapResponse;
import com.tilog.entity.WriteHistory;
import com.tilog.repository.WriteHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class WriteHistoryQueryServiceTest {
    @Mock
    private WriteHistoryRepository writeHistoryRepository;

    @InjectMocks
    private WriteHistoryQueryService writeHistoryQueryService;

    private final Long memberId = 1L;
    private final LocalDate startDate = LocalDate.of(2026, 5, 1);
    private final LocalDate endDate = LocalDate.of(2026, 5, 5);

    @Test
    @DisplayName("기간별 작성 개수를 조회하면 기록이 없는 날짜는 0으로 채워진다")
    void getDailyCounts_fillMissingDatesWithZero() {
        // Given
        BDDMockito.given(writeHistoryRepository.findAllByMemberIdAndWrittenDateBetweenOrderByWrittenDateAsc(
                memberId,
                startDate,
                endDate
        )).willReturn(List.of(
                createHistory(startDate, 1),
                createHistory(startDate.plusDays(2), 2)
        ));

        // When
        WriteHistoryDailyCountSummaryResponse response =
                writeHistoryQueryService.getDailyCounts(memberId, startDate, endDate);

        // Then
        assertThat(response.items())
                .extracting(WriteHistoryDailyCountResponse::writeCount)
                .containsExactly(1, 0, 2, 0, 0);
    }

    @Test
    @DisplayName("잔디 히트맵을 조회하면 기록이 없는 날짜는 writeCount 0과 level 0으로 채워진다")
    void getHeatmap_fillMissingDatesWithZeroLevel() {
        // Given
        BDDMockito.given(writeHistoryRepository.findAllByMemberIdAndWrittenDateBetweenOrderByWrittenDateAsc(
                memberId,
                startDate,
                endDate
        )).willReturn(List.of(
                createHistory(startDate, 1),
                createHistory(startDate.plusDays(2), 2)
        ));

        // When
        WriteHistoryHeatmapResponse response =
                writeHistoryQueryService.getHeatmap(memberId, startDate, endDate);

        // Then
        assertThat(response.items())
                .extracting(WriteHistoryHeatmapItemResponse::writeCount)
                .containsExactly(1, 0, 2, 0, 0);

        assertThat(response.items())
                .extracting(WriteHistoryHeatmapItemResponse::level)
                .containsExactly(1, 0, 2, 0, 0);
    }

    @Test
    @DisplayName("잔디 히트맵 level은 작성 횟수가 4 이상이면 4로 제한된다")
    void getHeatmap_calculateLevelMaxFour() {
        // Given
        BDDMockito.given(writeHistoryRepository.findAllByMemberIdAndWrittenDateBetweenOrderByWrittenDateAsc(
                memberId,
                startDate,
                endDate
        )).willReturn(List.of(
                createHistory(startDate, 0),
                createHistory(startDate.plusDays(1), 1),
                createHistory(startDate.plusDays(2), 2),
                createHistory(startDate.plusDays(3), 3),
                createHistory(startDate.plusDays(4), 5)
        ));

        // When
        WriteHistoryHeatmapResponse response =
                writeHistoryQueryService.getHeatmap(memberId, startDate, endDate);

        // Then
        assertThat(response.items())
                .extracting(WriteHistoryHeatmapItemResponse::writeCount)
                .containsExactly(0, 1, 2, 3, 5);

        assertThat(response.items())
                .extracting(WriteHistoryHeatmapItemResponse::level)
                .containsExactly(0, 1, 2, 3, 4);
    }

    @Test
    @DisplayName("날짜 유효성 검증 실패 시 예외가 발생한다")
    void validateDateRange_throwException() {
        LocalDate invalidStart = LocalDate.of(2026, 5, 10);
        LocalDate invalidEnd = LocalDate.of(2026, 5, 1);

        assertThatThrownBy(() ->
                writeHistoryQueryService.getDailyCounts(memberId, invalidStart, invalidEnd)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작일은 종료일보다 늦을 수 없습니다.");
    }

    @Test
    @DisplayName("잔디 히트맵 조회 시 시작일이 종료일보다 늦으면 예외가 발생한다")
    void getHeatmap_throwExceptionWhenStartDateAfterEndDate() {
        LocalDate invalidStart = LocalDate.of(2026, 5, 10);
        LocalDate invalidEnd = LocalDate.of(2026, 5, 1);

        assertThatThrownBy(() ->
                writeHistoryQueryService.getHeatmap(memberId, invalidStart, invalidEnd)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작일은 종료일보다 늦을 수 없습니다.");
    }

    private WriteHistory createHistory(LocalDate date, int count) {
        return WriteHistory.builder()
                .writtenDate(date)
                .writeCount(count)
                .build();
    }
}
