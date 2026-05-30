package com.tilog.service;

import com.tilog.dto.history.WriteHistoryDailyCountResponse;
import com.tilog.dto.history.WriteHistoryDailyCountSummaryResponse;
import com.tilog.entity.WriteHistory;
import com.tilog.repository.WriteHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    @Test
    @DisplayName("기간별 작성 개수를 조회하면 기록이 없는 날짜는 0으로 채워진다")
    void getDailyCounts_fillMissingDatesWithZero() {
        // Given
        Long memberId = 1L;
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 5);

        WriteHistory day1 = WriteHistory.builder().writtenDate(startDate).writeCount(1).build();
        WriteHistory day3 = WriteHistory.builder().writtenDate(startDate.plusDays(2)).writeCount(2).build();

        BDDMockito.given(writeHistoryRepository.findAllByMemberIdAndWrittenDateBetweenOrderByWrittenDateAsc(memberId, startDate, endDate))
                .willReturn(List.of(day1, day3));

        // When
        WriteHistoryDailyCountSummaryResponse response = writeHistoryQueryService.getDailyCounts(memberId, startDate, endDate);

        // Then
        assertThat(response.items()).hasSize(5);

        assertDailyCount(response.items().get(0), "2026-05-01", 1);
        assertDailyCount(response.items().get(1), "2026-05-02", 0);
        assertDailyCount(response.items().get(2), "2026-05-03", 2);
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
    void getDailyCounts_throwExceptionWhenStartDateAfterEndDate() {
        Long memberId = 1L;
        LocalDate startDate = LocalDate.of(2026, 5, 10);
        LocalDate endDate = LocalDate.of(2026, 5, 1);

        assertThatThrownBy(() ->
                writeHistoryQueryService.getDailyCounts(memberId, startDate, endDate)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작일은 종료일보다 늦을 수 없습니다.");
    }

    @Test
    @DisplayName("조회 기간이 1년을 초과하면 예외가 발생한다")
    void getDailyCounts_throwExceptionWhenDateRangeOverOneYear() {
        Long memberId = 1L;
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2027, 1, 3);

        assertThatThrownBy(() ->
                writeHistoryQueryService.getDailyCounts(memberId, startDate, endDate)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조회 기간은 최대 1년까지 가능합니다.");
    }

    // 검증용 헬퍼 메서드
    private void assertDailyCount(WriteHistoryDailyCountResponse item, String expectedDate, int expectedCount) {
        assertThat(item.date()).isEqualTo(LocalDate.parse(expectedDate));
        assertThat(item.writeCount()).isEqualTo(expectedCount);
    }
}
