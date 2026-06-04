package com.tilog.domain.payback.entity;

import com.tilog.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "payback_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaybackPolicy extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payback_policy_id")
    private Long paybackPolicyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "required_write_days", nullable = false)
    private int requiredWriteDays;

    @Column(name = "subscription_fee", nullable = false)
    private int subscriptionFee;

    @Column(name = "refund_amount", nullable = false)
    private int refundAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
