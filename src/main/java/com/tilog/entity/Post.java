package com.tilog.entity;

import com.tilog.entity.enums.Difficulty;
import com.tilog.entity.enums.Visibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// TIL 게시글 엔티티

@Entity
@Table(name = "til_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty = Difficulty.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "study_time")
    private Integer studyTime;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 게시글 생성 메서드 - (Setter 사용 대신 수정 책임을 엔티티에 위임)
    public static Post create(Member member, String title, String content, Difficulty difficulty, Visibility visibility, Integer studyTime) {
        Post post = new Post();

        post.member = member;
        post.title = title;
        post.content = content;
        post.difficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        post.visibility = visibility == null ? Visibility.PUBLIC : visibility;
        post.studyTime = studyTime;

        return post;
    }

    // 게시글 수정 메서드 - (Setter 사용 대신 수정 책임을 엔티티에 위임)
    public void update(String title, String content, Difficulty difficulty, Visibility visibility, Integer studyTime) {
        this.title = title;
        this.content = content;
        this.difficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        this.visibility = visibility == null ? Visibility.PUBLIC : visibility;
        this.studyTime = studyTime;
    }

    // 게시글 조회수 증가 메서드
    public void increaseViewCount() {
        this.viewCount = this.viewCount == null ? 1 : this.viewCount + 1;
    }

    // 게시글 삭제 메서드 - (Setter 사용 대신 수정 책임을 엔티티에 위임)
    public void delete() {
        this.isDeleted = true;
    }
}
