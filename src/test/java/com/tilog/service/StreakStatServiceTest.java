package com.tilog.service;

import com.tilog.domain.streak.dto.StreakStatResponse;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.streak.entity.StreakStat;
import com.tilog.domain.streak.repository.StreakStatRepository;
import com.tilog.domain.streak.service.StreakStatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StreakStatServiceTest {
    private StreakStatRepository streakStatRepository;
    private StreakStatService streakStatService;

    @BeforeEach
    void setUp() {
        streakStatRepository = mock(StreakStatRepository.class);
        streakStatService = new StreakStatService(streakStatRepository);
    }

    @Test
    @DisplayName("스트릭 통계가 없으면 새로 생성한다")
    void updateStreakWhenStreakStatDoesNotExist() {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId);
        LocalDate writtenDate = LocalDate.of(2026, 5, 31);

        when(streakStatRepository.findById(memberId))
                .thenReturn(Optional.empty());

        // when
        streakStatService.updateStreak(member, writtenDate);

        // then
        ArgumentCaptor<StreakStat> captor = ArgumentCaptor.forClass(StreakStat.class);

        verify(streakStatRepository).findById(memberId);
        verify(streakStatRepository).save(captor.capture());

        StreakStat savedStreakStat = captor.getValue();

        assertThat(savedStreakStat.getMember()).isEqualTo(member);
        assertThat(savedStreakStat.getCurrentStreak()).isEqualTo(1);
        assertThat(savedStreakStat.getLongestStreak()).isEqualTo(1);
        assertThat(savedStreakStat.getTotalWrittenDays()).isEqualTo(1);
        assertThat(savedStreakStat.getLastWrittenDate()).isEqualTo(writtenDate);
    }

    @Test
    @DisplayName("마지막 작성일 다음 날 작성하면 현재 스트릭이 증가한다")
    void updateStreakWhenWrittenNextDay() {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId);
        LocalDate lastWrittenDate = LocalDate.of(2026, 5, 30);
        LocalDate writtenDate = LocalDate.of(2026, 5, 31);

        StreakStat streakStat = StreakStat.create(member, lastWrittenDate);

        when(streakStatRepository.findById(memberId))
                .thenReturn(Optional.of(streakStat));

        // when
        streakStatService.updateStreak(member, writtenDate);

        // then
        verify(streakStatRepository).findById(memberId);
        verify(streakStatRepository, never()).save(any());

        assertThat(streakStat.getCurrentStreak()).isEqualTo(2);
        assertThat(streakStat.getLongestStreak()).isEqualTo(2);
        assertThat(streakStat.getTotalWrittenDays()).isEqualTo(2);
        assertThat(streakStat.getLastWrittenDate()).isEqualTo(writtenDate);
    }

    @Test
    @DisplayName("같은 날짜에 다시 작성하면 스트릭 통계는 증가하지 않는다")
    void updateStreakWhenWrittenSameDay() {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId);
        LocalDate writtenDate = LocalDate.of(2026, 5, 31);

        StreakStat streakStat = StreakStat.create(member, writtenDate);

        when(streakStatRepository.findById(memberId))
                .thenReturn(Optional.of(streakStat));

        // when
        streakStatService.updateStreak(member, writtenDate);

        // then
        verify(streakStatRepository).findById(memberId);
        verify(streakStatRepository, never()).save(any());

        assertThat(streakStat.getCurrentStreak()).isEqualTo(1);
        assertThat(streakStat.getLongestStreak()).isEqualTo(1);
        assertThat(streakStat.getTotalWrittenDays()).isEqualTo(1);
        assertThat(streakStat.getLastWrittenDate()).isEqualTo(writtenDate);
    }

    @Test
    @DisplayName("연속되지 않은 날짜에 작성하면 현재 스트릭은 1로 초기화된다")
    void resetCurrentStreakWhenWrittenDateIsNotContinuous() {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId);
        LocalDate lastWrittenDate = LocalDate.of(2026, 5, 28);
        LocalDate writtenDate = LocalDate.of(2026, 5, 31);

        StreakStat streakStat = StreakStat.create(member, lastWrittenDate);

        when(streakStatRepository.findById(memberId))
                .thenReturn(Optional.of(streakStat));

        // when
        streakStatService.updateStreak(member, writtenDate);

        // then
        verify(streakStatRepository).findById(memberId);
        verify(streakStatRepository, never()).save(any());

        assertThat(streakStat.getCurrentStreak()).isEqualTo(1);
        assertThat(streakStat.getLongestStreak()).isEqualTo(1);
        assertThat(streakStat.getTotalWrittenDays()).isEqualTo(2);
        assertThat(streakStat.getLastWrittenDate()).isEqualTo(writtenDate);
    }

    @Test
    @DisplayName("현재 스트릭이 초기화되어도 기존 최장 스트릭은 유지된다")
    void keepLongestStreakWhenCurrentStreakIsReset() {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId);

        StreakStat streakStat = StreakStat.create(member, LocalDate.of(2026, 5, 1));
        streakStat.update(LocalDate.of(2026, 5, 2));
        streakStat.update(LocalDate.of(2026, 5, 3));

        when(streakStatRepository.findById(memberId))
                .thenReturn(Optional.of(streakStat));

        // when
        streakStatService.updateStreak(member, LocalDate.of(2026, 5, 10));

        // then
        verify(streakStatRepository).findById(memberId);
        verify(streakStatRepository, never()).save(any());

        assertThat(streakStat.getCurrentStreak()).isEqualTo(1);
        assertThat(streakStat.getLongestStreak()).isEqualTo(3);
        assertThat(streakStat.getTotalWrittenDays()).isEqualTo(4);
        assertThat(streakStat.getLastWrittenDate()).isEqualTo(LocalDate.of(2026, 5, 10));
    }

    @Test
    @DisplayName("스트릭 통계가 존재하면 스트릭 조회 응답을 반환한다")
    void getStreakWhenStreakStatExists() {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId);
        LocalDate writtenDate = LocalDate.of(2026, 5, 31);

        StreakStat streakStat = StreakStat.create(member, writtenDate);
        setMemberId(streakStat, memberId);

        when(streakStatRepository.findById(memberId))
                .thenReturn(Optional.of(streakStat));

        // when
        StreakStatResponse response = streakStatService.getStreak(memberId);

        // then
        verify(streakStatRepository).findById(memberId);

        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.currentStreak()).isEqualTo(1);
        assertThat(response.longestStreak()).isEqualTo(1);
        assertThat(response.totalWrittenDays()).isEqualTo(1);
        assertThat(response.lastWrittenDate()).isEqualTo(writtenDate);
    }

    @Test
    @DisplayName("스트릭 통계가 없으면 빈 스트릭 조회 응답을 반환한다")
    void getStreakWhenStreakStatDoesNotExist() {
        // given
        Long memberId = 1L;

        when(streakStatRepository.findById(memberId))
                .thenReturn(Optional.empty());

        // when
        StreakStatResponse response = streakStatService.getStreak(memberId);

        // then
        verify(streakStatRepository).findById(memberId);

        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.currentStreak()).isZero();
        assertThat(response.longestStreak()).isZero();
        assertThat(response.totalWrittenDays()).isZero();
        assertThat(response.lastWrittenDate()).isNull();
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

    private void setMemberId(StreakStat streakStat, Long memberId) {
        try {
            Field memberIdField = StreakStat.class.getDeclaredField("memberId");
            memberIdField.setAccessible(true);
            memberIdField.set(streakStat, memberId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
