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
    private Long feedbackId;

    @ManyToOne
    @JoinColumn(name = "til_id")
    private Post til;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private Member requestor;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private Member mentor;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int technicalScore;
    private int flowScore;
    private int designScore;
    private String comment;

    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    public MentorFeedback(Post til, Member requestor, Member mentor){
        this.til = til;
        this.requestor = requestor;
        this.mentor = mentor;
        this.status = Status.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    public void updateFeedback(int tech, int flow, int design, String comment){
        this.technicalScore = tech;
        this.flowScore = flow;
        this.designScore = design;
        this.comment = comment;
        this.status = Status.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
