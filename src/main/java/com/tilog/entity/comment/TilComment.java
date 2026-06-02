package com.tilog.entity.comment;

import com.tilog.entity.member.Member;
import com.tilog.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "til_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TilComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    // 2번 담당자가 정의한 TilPost 엔티티 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private TilComment parentComment;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 생성 (일반 댓글)
    public static TilComment create(Post post, Member member, String content) {
        TilComment comment = new TilComment();
        comment.post = post;
        comment.member = member;
        comment.content = content;
        comment.isDeleted = false;
        return comment;
    }

    // 생성 (대댓글)
    public static TilComment createReply(Post post, Member member, String content, TilComment parentComment) {
        TilComment comment = new TilComment();
        comment.post = post;
        comment.member = member;
        comment.content = content;
        comment.parentComment = parentComment;
        comment.isDeleted = false;
        return comment;
    }

    public void update(String content) {
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public boolean isOwner(Long memberId) {
        return this.member.getId().equals(memberId);
    }
}