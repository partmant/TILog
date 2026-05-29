package com.tilog.entity;

import com.tilog.entity.enums.Difficulty;
import com.tilog.entity.enums.Visibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "til_post")
@Getter
@NoArgsConstructor
public class TilPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 10)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 10)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "study_time")
    private Integer studyTime;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TilPost(Member member, String title, String content, Difficulty difficulty,
                   Visibility visibility, Integer studyTime) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.difficulty = difficulty;
        this.visibility = visibility;
        this.studyTime = studyTime;
        this.deleted = false;
        this.viewCount = 0;
        this.createdAt = LocalDateTime.now();
    }
}