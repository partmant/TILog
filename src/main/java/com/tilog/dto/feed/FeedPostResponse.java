package com.tilog.dto.feed;

import com.tilog.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FeedPostResponse {

    private final Long postId;
    private final Long memberId;
    private final String nickname;
    private final String title;
    private final String contentPreview;  // 본문 앞 100자
    private final LocalDateTime createdAt;

    private FeedPostResponse(Post post) {
        this.postId = post.getId();
        this.memberId = post.getMember().getId();
        this.nickname = post.getMember().getNickname();
        this.title = post.getTitle();
        this.contentPreview = post.getContent().length() > 100
                ? post.getContent().substring(0, 100) + "..."
                : post.getContent();
        this.createdAt = post.getCreatedAt();
    }

    public static FeedPostResponse from(Post post) {
        return new FeedPostResponse(post);
    }
}
