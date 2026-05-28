package com.tilog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class WriteHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "written_date", nullable = false)
    private LocalDate writtenDate;

    @Column(name = "write_count", nullable = false)
    private int writeCount;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Builder
    private WriteHistory(Long memberId, LocalDate writtenDate, int writeCount) {
        this.memberId = memberId;
        this.writtenDate = writtenDate;
        this.writeCount = writeCount;
    }

    public static WriteHistory create(Long memberId, LocalDate writtenDate) {
        return WriteHistory.builder()
                .memberId(memberId)
                .writtenDate(writtenDate)
                .writeCount(1)
                .build();
    }

    public void increaseCount() {
        this.writeCount += 1;
    }
}
