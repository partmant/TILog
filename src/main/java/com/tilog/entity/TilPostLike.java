package com.tilog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "til_post_like",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_til_post_like",
        columnNames = {"post_id", "member_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TilPostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id")
    private Long postLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TilPostLike(Post post, Member member) {
        this.post = post;
        this.member = member;
        this.createdAt = LocalDateTime.now();
    }

    public static TilPostLike create(Post post, Member member) {
        TilPostLike like = new TilPostLike();
        like.post = post;
        like.member = member;
        like.createdAt = LocalDateTime.now();
        return like;
    }
}