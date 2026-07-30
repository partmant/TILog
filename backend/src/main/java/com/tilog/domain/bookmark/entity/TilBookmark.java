package com.tilog.domain.bookmark.entity;

import com.tilog.domain.member.entity.Member;
import com.tilog.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "til_bookmark",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_til_bookmark",
        columnNames = {"member_id", "post_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TilBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static TilBookmark create(Member member, Post post) {
        TilBookmark bookmark = new TilBookmark();
        bookmark.member = member;
        bookmark.post = post;
        return bookmark;
    }
}
