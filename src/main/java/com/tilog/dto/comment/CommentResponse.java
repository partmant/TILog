package com.tilog.dto.comment;

import com.tilog.entity.TilComment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {

    private final Long commentId;
    private final Long postId;
    private final Long memberId;
    private final String nickname;
    private final Long parentCommentId;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private CommentResponse(TilComment comment) {
        this.commentId = comment.getCommentId();
        this.postId = comment.getPost().getId();
        this.memberId = comment.getMember().getId();
        this.nickname = comment.getMember().getNickname();
        this.parentCommentId = comment.getParentComment() != null
                ? comment.getParentComment().getCommentId() : null;
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }

    public static CommentResponse from(TilComment comment) {
        return new CommentResponse(comment);
    }
}
