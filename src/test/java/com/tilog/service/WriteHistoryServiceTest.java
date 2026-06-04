package com.tilog.service;

import com.tilog.domain.writeHistory.dto.WriteHistoryRequest;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.writeHistory.entity.WriteHistory;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.writeHistory.repository.WriteHistoryRepository;
import com.tilog.domain.writeHistory.service.WriteHistoryService;
import com.tilog.domain.streak.service.StreakStatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WriteHistoryServiceTest {
    @Mock
    private WriteHistoryRepository writeHistoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StreakStatService streakStatService;

    @InjectMocks
    private WriteHistoryService writeHistoryService;

    private final Long memberId = 1L;
    private final WriteHistoryRequest request = new WriteHistoryRequest(memberId);
    private final LocalDate today = LocalDate.now();

    @Test
    @DisplayName("오늘 작성 이력이 없으면 새 작성 이력을 생성한다")
    void recordWriteHistory_create() {
        // given
        Member member = createMember(memberId);

        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(member));

        given(writeHistoryRepository.findByMember_IdAndWrittenDate(memberId, today))
                .willReturn(Optional.empty());

        given(writeHistoryRepository.save(any(WriteHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        writeHistoryService.recordWriteHistory(request);

        // then
        verify(memberRepository).findById(memberId);
        verify(writeHistoryRepository).findByMember_IdAndWrittenDate(memberId, today);
        verify(writeHistoryRepository).save(any(WriteHistory.class));
        verify(streakStatService).updateStreak(member, today);
    }

    @Test
    @DisplayName("이미 있으면 횟수를 증가시킨다")
    void recordWriteHistory_increaseCount() {
        // given
        Member member = createMember(memberId);
        WriteHistory history = WriteHistory.create(member, today);

        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(member));

        given(writeHistoryRepository.findByMember_IdAndWrittenDate(memberId, today))
                .willReturn(Optional.of(history));

        // when
        writeHistoryService.recordWriteHistory(request);

        // then
        assertThat(history.getWriteCount()).isEqualTo(2);

        verify(memberRepository).findById(memberId);
        verify(writeHistoryRepository).findByMember_IdAndWrittenDate(memberId, today);
        verify(writeHistoryRepository, never()).save(any(WriteHistory.class));
        verify(streakStatService).updateStreak(member, today);
    }

    private Member createMember(Long memberId) {
        Member member = Member.create(
                "test" + memberId + "@example.com",
                "encoded-password",
                "테스트유저" + memberId
        );

        setMemberId(member, memberId);

        return member;
    }

    private void setMemberId(Member member, Long memberId) {
        try {
            Field idField = Member.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(member, memberId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
