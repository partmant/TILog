package com.tilog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "til_post_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "member_id"}))
@Getter
@NoArgsConstructor
public class TilPostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id")
    private Long postLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private TilPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TilPostLike(TilPost post, Member member) {
        this.post = post;
        this.member = member;
        this.createdAt = LocalDateTime.now();
    }
}