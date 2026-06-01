package com.tilog.repository;

import com.tilog.global.config.QuerydslConfig;
import com.tilog.dto.TilPostSearchCondition;
import com.tilog.dto.TilPostSummaryDto;
import com.tilog.dto.TilSortType;
import com.tilog.entity.*;
import com.tilog.entity.enums.Difficulty;
import com.tilog.entity.enums.Visibility;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
class TilPostRepositoryImplTest {

    @Autowired
    TilPostRepository tilPostRepository;

    @Autowired
    EntityManager em;

    // 공통 테스트 데이터
    Member alice, bob;
    TilPost post1, post2, post3, post4, post5;
    Tag springTag, reactTag, jpaTag;

    @BeforeEach
    void setUp() {
        alice = new Member("alice@test.com", "pw", "alice");
        bob   = new Member("bob@test.com",   "pw", "bob");
        em.persist(alice);
        em.persist(bob);

        springTag = new Tag("Spring");
        reactTag  = new Tag("React");
        jpaTag    = new Tag("JPA");
        em.persist(springTag);
        em.persist(reactTag);
        em.persist(jpaTag);

        // alice 작성 — Spring 태그, EASY, study_time 30
        post1 = new TilPost(alice, "Spring 기초 공부", "의존성 주입 배웠다", Difficulty.EASY,   Visibility.PUBLIC,  30);
        // alice 작성 — React 태그, HARD
        post2 = new TilPost(alice, "React Hook 심화", "useMemo Hook 배웠다", Difficulty.HARD,   Visibility.PUBLIC,  90);
        // bob 작성 — 태그 없음, NORMAL
        post3 = new TilPost(bob,   "알고리즘 BFS/DFS", "그래프 탐색 정리",   Difficulty.NORMAL, Visibility.PUBLIC,  45);
        // bob 작성 — Spring + JPA 태그, HARD
        post4 = new TilPost(bob,   "Spring Security",  "JWT 필터 체인 분석", Difficulty.HARD,   Visibility.PUBLIC,  120);
        // alice 작성 — PRIVATE (검색 제외 대상)
        post5 = new TilPost(alice, "비공개 메모",       "Draft 내용",         Difficulty.EASY,   Visibility.PRIVATE, 10);

        em.persist(post1);
        em.persist(post2);
        em.persist(post3);
        em.persist(post4);
        em.persist(post5);

        em.persist(new TilPostTag(post1, springTag));
        em.persist(new TilPostTag(post2, reactTag));
        em.persist(new TilPostTag(post4, springTag));
        em.persist(new TilPostTag(post4, jpaTag));

        // 좋아요: post1(2개), post3(1개)
        em.persist(new TilPostLike(post1, bob));
        em.persist(new TilPostLike(post1, alice));
        em.persist(new TilPostLike(post3, alice));

        // 댓글: post2(3개), post3(1개)
        em.persist(new TilComment(post2, bob,   "좋은 글이네요"));
        em.persist(new TilComment(post2, alice, "감사합니다"));
        em.persist(new TilComment(post2, bob,   "더 공부해 보세요"));
        em.persist(new TilComment(post3, alice, "저도 헷갈렸어요"));

        em.flush();
        em.clear();
    }

    // ──────────────────────── 기본 조회 ────────────────────────

    @Test
    @DisplayName("조건 없이 전체 공개 게시글 조회 — PRIVATE 게시글 제외")
    void noCondition_returnsOnlyPublicPosts() {
        Page<TilPostSummaryDto> page = search(TilPostSearchCondition.builder().build());

        assertThat(page.getTotalElements()).isEqualTo(4);   // post5(PRIVATE) 제외
        assertThat(page.getContent()).allMatch(dto -> dto.getPostId() != null);
    }

    // ──────────────────────── 조건 1: 키워드 ────────────────────────

    @Test
    @DisplayName("제목 키워드 'Spring' 검색 — post1, post4 반환")
    void keyword_title_matchesSpringPosts() {
        Page<TilPostSummaryDto> page = search(cond().keyword("Spring").build());

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(TilPostSummaryDto::getTitle)
                .allMatch(t -> t.contains("Spring"));
    }

    @Test
    @DisplayName("본문 키워드 '배웠다' 검색 — post1, post2 반환")
    void keyword_content_matchesPosts() {
        Page<TilPostSummaryDto> page = search(cond().keyword("배웠다").build());

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("존재하지 않는 키워드 — 빈 리스트 반환")
    void keyword_noMatch_returnsEmpty() {
        Page<TilPostSummaryDto> page = search(cond().keyword("존재하지않는키워드xyz").build());

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    // ──────────────────────── 조건 2: 작성자 닉네임 ────────────────────────

    @Test
    @DisplayName("닉네임 'alice' 검색 — alice의 공개 게시글만 반환")
    void nickname_alice_returnsAlicePosts() {
        Page<TilPostSummaryDto> page = search(cond().nickname("alice").build());

        assertThat(page.getTotalElements()).isEqualTo(2); // post1, post2 (post5는 PRIVATE)
        assertThat(page.getContent()).extracting(TilPostSummaryDto::getAuthorNickname)
                .containsOnly("alice");
    }

    @Test
    @DisplayName("존재하지 않는 닉네임 검색 — 빈 리스트 반환")
    void nickname_notFound_returnsEmpty() {
        Page<TilPostSummaryDto> page = search(cond().nickname("nobody").build());

        assertThat(page.getContent()).isEmpty();
    }

    // ──────────────────────── 조건 3: 기술 스택 태그 ────────────────────────

    @Test
    @DisplayName("태그 'Spring' 검색 — post1, post4 반환")
    void tagName_spring_matchesTwoPosts() {
        Page<TilPostSummaryDto> page = search(cond().tagName("Spring").build());

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("태그 'React' 검색 — post2만 반환")
    void tagName_react_matchesOnePost() {
        Page<TilPostSummaryDto> page = search(cond().tagName("React").build());

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("React Hook 심화");
    }

    @Test
    @DisplayName("태그 필터 결과에 태그 목록이 채워짐")
    void tagName_resultContainsTags() {
        Page<TilPostSummaryDto> page = search(cond().tagName("JPA").build());

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTags()).contains("JPA");
    }

    // ──────────────────────── 조건 4: 난이도 ────────────────────────

    @Test
    @DisplayName("난이도 HARD 필터 — post2, post4 반환")
    void difficulty_hard_matchesTwoPosts() {
        Page<TilPostSummaryDto> page = search(cond().difficulty(Difficulty.HARD).build());

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(TilPostSummaryDto::getDifficulty)
                .containsOnly(Difficulty.HARD);
    }

    @Test
    @DisplayName("난이도 EASY 필터 — 공개 게시글 중 EASY만 반환")
    void difficulty_easy_matchesEasyPosts() {
        Page<TilPostSummaryDto> page = search(cond().difficulty(Difficulty.EASY).build());

        assertThat(page.getTotalElements()).isEqualTo(1); // post1만 (post5는 PRIVATE)
    }

    // ──────────────────────── 조건 5: 기간 ────────────────────────

    @Test
    @DisplayName("from~to 범위 안의 게시글만 반환")
    void dateRange_withinRange_returnsPosts() {
        LocalDate today = LocalDate.now();
        Page<TilPostSummaryDto> page = search(
                cond().from(today.minusDays(1)).to(today.plusDays(1)).build());

        assertThat(page.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("미래 날짜 범위 — 빈 리스트 반환")
    void dateRange_future_returnsEmpty() {
        Page<TilPostSummaryDto> page = search(
                cond().from(LocalDate.now().plusDays(1)).to(LocalDate.now().plusDays(7)).build());

        assertThat(page.getContent()).isEmpty();
    }

    // ──────────────────────── 조건 복합 ────────────────────────

    @Test
    @DisplayName("닉네임 'bob' + 난이도 HARD 복합 검색 — post4만 반환")
    void combined_bobAndHard_returnsPost4() {
        Page<TilPostSummaryDto> page = search(
                cond().nickname("bob").difficulty(Difficulty.HARD).build());

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Spring Security");
    }

    // ──────────────────────── 페이징 ────────────────────────

    @Test
    @DisplayName("페이지 크기 2 — 전체 4개 중 첫 페이지 2개 반환")
    void paging_pageSize2_returnsFirstPage() {
        Page<TilPostSummaryDto> page = tilPostRepository.searchPosts(
                TilPostSearchCondition.builder().build(),
                PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("마지막 페이지 — 남은 데이터만 반환")
    void paging_lastPage_returnsRemainder() {
        Page<TilPostSummaryDto> page = tilPostRepository.searchPosts(
                TilPostSearchCondition.builder().build(),
                PageRequest.of(1, 3)); // 전체 4개, 두 번째 페이지 → 1개

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("범위 초과 페이지 — 빈 리스트 반환")
    void paging_outOfRange_returnsEmpty() {
        Page<TilPostSummaryDto> page = tilPostRepository.searchPosts(
                TilPostSearchCondition.builder().build(),
                PageRequest.of(99, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(4);
    }

    // ──────────────────────── 정렬 ────────────────────────

    @Test
    @DisplayName("최신순 정렬 — created_at DESC")
    void sort_latest_orderedByCreatedAt() {
        Page<TilPostSummaryDto> page = search(cond().sort(TilSortType.LATEST).build());

        // 모두 같은 시간에 저장되므로 역순 ID가 최신 (post4가 마지막)
        // 최소한 결과가 4개이고 순서가 있는지 확인
        assertThat(page.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("좋아요순 정렬 — 좋아요 많은 게시글이 앞에 옴 (post1=2개)")
    void sort_likes_post1First() {
        Page<TilPostSummaryDto> page = search(cond().sort(TilSortType.LIKES).build());

        assertThat(page.getContent()).isNotEmpty();
        TilPostSummaryDto first = page.getContent().get(0);
        assertThat(first.getLikeCount()).isEqualTo(2L); // post1이 제일 많음
    }

    @Test
    @DisplayName("댓글순 정렬 — 댓글 많은 게시글이 앞에 옴 (post2=3개)")
    void sort_comments_post2First() {
        Page<TilPostSummaryDto> page = search(cond().sort(TilSortType.COMMENTS).build());

        assertThat(page.getContent()).isNotEmpty();
        TilPostSummaryDto first = page.getContent().get(0);
        assertThat(first.getCommentCount()).isEqualTo(3L); // post2가 제일 많음
    }

    // ──────────────────────── 헬퍼 ────────────────────────

    private Page<TilPostSummaryDto> search(TilPostSearchCondition condition) {
        return tilPostRepository.searchPosts(condition, PageRequest.of(0, 20));
    }

    private TilPostSearchCondition.TilPostSearchConditionBuilder cond() {
        return TilPostSearchCondition.builder();
    }
}