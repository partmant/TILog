package com.tilog.domain.post.repository;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tilog.domain.comment.entity.QTilComment;
import com.tilog.domain.like.entity.QTilPostLike;
import com.tilog.domain.member.entity.QMember;
import com.tilog.domain.post.dto.TilPostSearchCondition;
import com.tilog.domain.post.dto.TilPostSummaryDto;
import com.tilog.domain.post.dto.TilSortType;
import com.tilog.domain.post.entity.QPost;
import com.tilog.domain.post.entity.Difficulty;
import com.tilog.domain.post.entity.Visibility;
import com.tilog.domain.tag.entity.QPostTag;
import com.tilog.domain.tag.entity.QTag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // Q클래스 — mvnw compile 후 target/generated-sources 에 생성됨
    // Post.id (PK 컬럼명 post_id, 필드명 id), Post.isDeleted 주의
    private static final QPost qPost = QPost.post;
    private static final QMember qMember = QMember.member;
    private static final QPostTag qPostTag = QPostTag.postTag;
    private static final QTag qTag = QTag.tag;
    private static final QTilPostLike qLike = QTilPostLike.tilPostLike;
    private static final QTilComment qComment = QTilComment.tilComment;

    // ===== 공개 API =====

    @Override
    public Page<TilPostSummaryDto> searchPosts(TilPostSearchCondition cond, Pageable pageable) {
        List<TilPostSummaryDto> content = fetchContent(cond, pageable);
        long total = fetchCount(cond);

        if (!content.isEmpty()) {
            List<Long> postIds = content.stream().map(TilPostSummaryDto::getPostId).toList();
            Map<Long, List<String>> tagMap = fetchTagsByPostIds(postIds);
            content.forEach(dto -> dto.setTags(tagMap.getOrDefault(dto.getPostId(), List.of())));
        }

        return new PageImpl<>(content, pageable, total);
    }

    // ===== 본문 조회 =====

    private List<TilPostSummaryDto> fetchContent(TilPostSearchCondition cond, Pageable pageable) {

        JPQLQuery<Long> likeSubQ = JPAExpressions
                .select(qLike.postLikeId.count())
                .from(qLike)
                .where(qLike.post.id.eq(qPost.id));   // Post.id (PK 필드명)

        JPQLQuery<Long> commentSubQ = JPAExpressions
                .select(qComment.commentId.count())
                .from(qComment)
                .where(qComment.post.id.eq(qPost.id),
                        qComment.isDeleted.isFalse());  // TilComment.isDeleted

        return queryFactory
                .select(Projections.constructor(TilPostSummaryDto.class,
                        qPost.id,               // Post.id → TilPostSummaryDto.postId
                        qPost.title,
                        qMember.nickname,
                        qPost.difficulty,
                        qPost.createdAt,
                        likeSubQ,
                        commentSubQ,
                        qPost.viewCount,
                        qPost.studyTime
                ))
                .from(qPost)
                .join(qPost.member, qMember)
                .where(buildConditions(cond))
                .orderBy(resolveOrderSpecifiers(cond.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    // ===== count 쿼리 (페이지네이션용) =====

    private long fetchCount(TilPostSearchCondition cond) {
        Long count = queryFactory
                .select(qPost.id.count())       // Post.id
                .from(qPost)
                .join(qPost.member, qMember)
                .where(buildConditions(cond))
                .fetchOne();
        return count != null ? count : 0L;
    }

    // ===== 태그 배치 조회 (N+1 방지) =====

    private Map<Long, List<String>> fetchTagsByPostIds(List<Long> postIds) {
        return queryFactory
                .select(qPostTag.post.id, qTag.name)    // PostTag.post.id
                .from(qPostTag)
                .join(qPostTag.tag, qTag)
                .where(qPostTag.post.id.in(postIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        t -> t.get(qPostTag.post.id),
                        Collectors.mapping(t -> t.get(qTag.name), Collectors.toList())
                ));
    }

    // ===== WHERE 조건 조합 =====

    private Predicate[] buildConditions(TilPostSearchCondition cond) {
        // memberId가 있으면 내 TIL 검색 — PUBLIC 필터 우회, DRAFT만 제외
        if (cond.getMemberId() != null) {
            return new Predicate[]{
                    qPost.member.id.eq(cond.getMemberId()),
                    qPost.visibility.ne(Visibility.DRAFT),
                    qPost.isDeleted.isFalse(),
                    keywordContains(cond.getKeyword()),
                    tagNameExists(cond.getTagName()),
                    difficultyEq(cond.getDifficulty()),
                    createdAtBetween(cond.getFrom(), cond.getTo())
            };
        }
        // 일반 공개 검색
        return new Predicate[]{
                qPost.visibility.eq(Visibility.PUBLIC),
                qPost.isDeleted.isFalse(),
                keywordContains(cond.getKeyword()),
                nicknameEq(cond.getNickname()),
                tagNameExists(cond.getTagName()),
                difficultyEq(cond.getDifficulty()),
                createdAtBetween(cond.getFrom(), cond.getTo())
        };
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return qPost.title.containsIgnoreCase(keyword)
                .or(qPost.content.containsIgnoreCase(keyword));
    }

    private BooleanExpression nicknameEq(String nickname) {
        if (!StringUtils.hasText(nickname)) return null;
        return qMember.nickname.eq(nickname);
    }

    private BooleanExpression tagNameExists(String tagName) {
        if (!StringUtils.hasText(tagName)) return null;
        return JPAExpressions
                .selectOne()
                .from(qPostTag)
                .join(qPostTag.tag, qTag)
                .where(qPostTag.post.id.eq(qPost.id),   // PostTag.post.id
                        qTag.name.eq(tagName))
                .exists();
    }

    private BooleanExpression difficultyEq(Difficulty difficulty) {
        return difficulty != null ? qPost.difficulty.eq(difficulty) : null;
    }

    private BooleanExpression createdAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;
        if (from == null) return qPost.createdAt.loe(to.atTime(23, 59, 59));
        if (to == null) return qPost.createdAt.goe(from.atStartOfDay());
        return qPost.createdAt.between(from.atStartOfDay(), to.atTime(23, 59, 59));
    }

    // ===== 정렬 =====

    @SuppressWarnings({"rawtypes", "unchecked"})
    private OrderSpecifier<?>[] resolveOrderSpecifiers(TilSortType sort) {
        TilSortType resolved = sort != null ? sort : TilSortType.LATEST;
        return switch (resolved) {
            case LIKES -> new OrderSpecifier[]{
                    new OrderSpecifier<>(Order.DESC,
                            (Expression<Long>) JPAExpressions
                                    .select(qLike.postLikeId.count())
                                    .from(qLike)
                                    .where(qLike.post.id.eq(qPost.id))),  // Post.id
                    qPost.createdAt.desc()
            };
            case COMMENTS -> new OrderSpecifier[]{
                    new OrderSpecifier<>(Order.DESC,
                            (Expression<Long>) JPAExpressions
                                    .select(qComment.commentId.count())
                                    .from(qComment)
                                    .where(qComment.post.id.eq(qPost.id),  // Post.id
                                            qComment.isDeleted.isFalse())),  // TilComment.isDeleted
                    qPost.createdAt.desc()
            };
            case VIEWS -> new OrderSpecifier[]{qPost.viewCount.desc(), qPost.createdAt.desc()};
            default -> new OrderSpecifier[]{qPost.createdAt.desc()};
        };
    }
}
