package com.tilog.domain.tag.entity;

import com.tilog.domain.post.entity.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// [게시글---태그] 매핑 엔티티

@Entity
@Table(
        name = "til_post_tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_til_post_tag",
                columnNames = {"post_id", "tag_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    // 게시글-태그 매핑 생성 메서드
    public static PostTag create(Post post, Tag tag) {
        PostTag postTag = new PostTag();
        postTag.post = post;
        postTag.tag = tag;

        return postTag;
    }
}
