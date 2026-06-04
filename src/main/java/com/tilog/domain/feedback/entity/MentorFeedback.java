package com.tilog.domain.feedback.entity;

import com.tilog.domain.member.entity.Member;
import com.tilog.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentorFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "til_id", nullable = false)
    private Post til;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requestor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private Member mentor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "technical_score", nullable = true)
    private Integer technicalScore;

    @Column(name = "flow_score", nullable = true)
    private Integer flowScore;

    @Column(name = "design_score", nullable = true)
    private Integer designScore;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public MentorFeedback(Post til, Member requestor, Member mentor) {
        this.til = til;
        this.requestor = requestor;
        this.mentor = mentor;
        this.status = Status.REQUESTED;
        this.requestedAt = LocalDateTime.now();
    }

    public void updateFeedback(int tech, int flow, int design, String comment) {
        this.technicalScore = tech;
        this.flowScore = flow;
        this.designScore = design;
        this.comment = comment;
        this.status = Status.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
