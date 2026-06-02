package com.tilog.domain.writeHistory.entity;

import com.tilog.domain.member.entity.Member;
import com.tilog.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "til_write_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_til_write_history",
                        columnNames = {"member_id", "written_date"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WriteHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "written_date", nullable = false)
    private LocalDate writtenDate;

    @Column(name = "write_count", nullable = false)
    private int writeCount;

    @Builder
    private WriteHistory(Member member, LocalDate writtenDate, int writeCount) {
        this.member = member;
        this.writtenDate = writtenDate;
        this.writeCount = writeCount;
    }

    public static WriteHistory create(Member member, LocalDate writtenDate) {
        return WriteHistory.builder()
                .member(member)
                .writtenDate(writtenDate)
                .writeCount(1)
                .build();
    }

    public void increaseCount() {
        this.writeCount += 1;
    }

    public Long getMemberId() {
        return member.getId();
    }
}
