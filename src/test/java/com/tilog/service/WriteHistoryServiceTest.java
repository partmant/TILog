package com.tilog.service;

import com.tilog.dto.history.WriteHistoryRequest;
import com.tilog.entity.WriteHistory;
import com.tilog.repository.WriteHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WriteHistoryServiceTest {
    @Mock
    private WriteHistoryRepository writeHistoryRepository;
    @InjectMocks
    private WriteHistoryService writeHistoryService;

    // 공통 변수 추출
    private final Long memberId = 1L;
    private final WriteHistoryRequest request = new WriteHistoryRequest(memberId);
    private final LocalDate today = LocalDate.now();

    @Test
    @DisplayName("오늘 작성 이력이 없으면 새 작성 이력을 생성한다")
    void recordWriteHistory_create() {
        // Given
        given(writeHistoryRepository.findByMemberIdAndWrittenDate(memberId, today))
                .willReturn(Optional.empty());
        given(writeHistoryRepository.save(any())).willReturn(WriteHistory.create(memberId, today));

        // When
        writeHistoryService.recordWriteHistory(request);

        // Then
        verify(writeHistoryRepository).save(any());
    }

    @Test
    @DisplayName("이미 있으면 횟수를 증가시킨다")
    void recordWriteHistory_increaseCount() {
        // Given
        WriteHistory history = WriteHistory.create(memberId, today);
        given(writeHistoryRepository.findByMemberIdAndWrittenDate(memberId, today))
                .willReturn(Optional.of(history));

        // When
        writeHistoryService.recordWriteHistory(request);

        // Then
        assertThat(history.getWriteCount()).isEqualTo(2);
        verify(writeHistoryRepository, never()).save(any());
    }
}
