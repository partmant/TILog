package com.tilog.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 엔티티 자동 시간 기록을 위한 추상 클래스
 * 직접 구현한 @PrePersist를 통해 서비스 로직의 개입 없이 생성 시간을 관리
 */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 엔티티가 생성되어 저장될 때 시간이 자동 저장

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;    // 엔티티의 값을 변경할 때 시간이 자동 업데이트

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
