package com.tilog.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tilog.dto.TilPostSearchCondition;
import com.tilog.dto.TilPostSummaryDto;
import com.tilog.dto.TilSortType;
import com.tilog.entity.*;
import com.tilog.entity.enums.Difficulty;
import com.tilog.entity.enums.Visibility;
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
public class TilPostRepositoryImpl implements TilPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // Q클래스 static 인스턴스 (mvnw compile 후 target/generated-sources 에 생성됨)
    private static final QTilPost qPost     = QTilPost.tilPost;
    private static final QMember qMember   = QMember.member;
    private static final QTilPostTag qPostTag = QTilPostTag.tilPostTag;
    private static final QTag qTag         = QTag.tag;
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

        // SELECT 절에 포함할 좋아요/댓글 서브쿼리 (표시용)
        JPQLQuery<Long> likeSubQ = JPAExpressions
                .select(qLike.postLikeId.count())
                .from(qLike)
                .where(qLike.post.postId.eq(qPost.postId));

        JPQLQuery<Long> commentSubQ = JPAExpressions
                .select(qComment.commentId.count())
                .from(qComment)
                .where(qComment.post.postId.eq(qPost.postId),
                        qComment.deleted.isFalse());

        return queryFactory
                .select(Projections.constructor(TilPostSummaryDto.class,
                        qPost.postId,
                        qPost.title,
                        qMember.nickname,
                        qPost.difficulty,
                        qPost.createdAt,
                        likeSubQ,
                        commentSubQ
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
                .select(qPost.postId.count())
                .from(qPost)
                .join(qPost.member, qMember)
                .where(buildConditions(cond))
                .fetchOne();
        return count != null ? count : 0L;
    }

    // ===== 태그 배치 조회 (N+1 방지) =====

    private Map<Long, List<String>> fetchTagsByPostIds(List<Long> postIds) {
        return queryFactory
                .select(qPostTag.post.postId, qTag.name)
                .from(qPostTag)
                .join(qPostTag.tag, qTag)
                .where(qPostTag.post.postId.in(postIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        t -> t.get(qPostTag.post.postId),
                        Collectors.mapping(t -> t.get(qTag.name), Collectors.toList())
                ));
    }

    // ===== WHERE 조건 조합 =====

    private Predicate[] buildConditions(TilPostSearchCondition cond) {
        return new Predicate[] {
                qPost.visibility.eq(Visibility.PUBLIC),
                qPost.deleted.isFalse(),
                keywordContains(cond.getKeyword()),
                nicknameEq(cond.getNickname()),
                tagNameExists(cond.getTagName()),
                difficultyEq(cond.getDifficulty()),
                createdAtBetween(cond.getFrom(), cond.getTo())
        };
    }

    /** 1) 제목/본문 키워드 — LIKE %keyword% */
    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return qPost.title.containsIgnoreCase(keyword)
                .or(qPost.content.containsIgnoreCase(keyword));
    }

    /** 2) 작성자 닉네임 — 정확히 일치 */
    private BooleanExpression nicknameEq(String nickname) {
        if (!StringUtils.hasText(nickname)) return null;
        return qMember.nickname.eq(nickname);
    }

    /**
     * 3) 기술 스택 태그 — EXISTS 서브쿼리
     *    idx_tilposttag_tag_til (tag_id, post_id) 인덱스 활용
     */
    private BooleanExpression tagNameExists(String tagName) {
        if (!StringUtils.hasText(tagName)) return null;
        return JPAExpressions
                .selectOne()
                .from(qPostTag)
                .join(qPostTag.tag, qTag)
                .where(qPostTag.post.postId.eq(qPost.postId),
                        qTag.name.eq(tagName))
                .exists();
    }

    /** 4) 난이도 필터 — idx_til_post_difficulty_created_at 인덱스 활용 */
    private BooleanExpression difficultyEq(Difficulty difficulty) {
        return difficulty != null ? qPost.difficulty.eq(difficulty) : null;
    }

    /** 5) 작성 기간 필터 — idx_til_post_visibility_created_at 인덱스 활용 */
    private BooleanExpression createdAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;
        if (from == null) return qPost.createdAt.loe(to.atTime(23, 59, 59));
        if (to == null)   return qPost.createdAt.goe(from.atStartOfDay());
        return qPost.createdAt.between(from.atStartOfDay(), to.atTime(23, 59, 59));
    }

    // ===== 정렬 =====

    @SuppressWarnings({"rawtypes", "unchecked"})
    private OrderSpecifier<?>[] resolveOrderSpecifiers(TilSortType sort) {
        TilSortType resolved = sort != null ? sort : TilSortType.LATEST;
        return switch (resolved) {
            case LIKES -> new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC,
                            (Expression<Long>) JPAExpressions
                                    .select(qLike.postLikeId.count())
                                    .from(qLike)
                                    .where(qLike.post.postId.eq(qPost.postId))),
                    qPost.createdAt.desc()
            };
            case COMMENTS -> new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC,
                            (Expression<Long>) JPAExpressions
                                    .select(qComment.commentId.count())
                                    .from(qComment)
                                    .where(qComment.post.postId.eq(qPost.postId),
                                            qComment.deleted.isFalse())),
                    qPost.createdAt.desc()
            };
            default -> new OrderSpecifier[] { qPost.createdAt.desc() };
        };
    }
}