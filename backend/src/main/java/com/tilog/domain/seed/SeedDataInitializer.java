package com.tilog.domain.seed;

import com.tilog.domain.member.entity.CurrentStatus;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.TargetJob;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.post.entity.Difficulty;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.post.entity.Visibility;
import com.tilog.domain.post.repository.PostRepository;
import com.tilog.domain.tag.entity.PostTag;
import com.tilog.domain.tag.entity.Tag;
import com.tilog.domain.tag.repository.PostTagRepository;
import com.tilog.domain.tag.repository.TagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 공개 포트폴리오 데모의 피드가 텅 비어 보이지 않도록, 서버 최초 기동 시점에
 * 가상의 작성자 계정과 TIL 게시글을 미리 채워 넣는다.
 *
 * <p>demo.account.email(단일 체험 계정)과는 완전히 분리된 별도 회원들이다.
 * DemoDataResetService는 demo.account.email로 조회한 회원만 초기화 대상으로 삼기
 * 때문에, 이 시더가 만든 계정/게시글은 매일 자정 초기화에 영향을 받지 않고 영구적으로
 * 남아 공개 피드용 샘플 콘텐츠 역할을 한다.
 *
 * <p>이미 시더 계정(AUTHORS 목록의 첫 번째 이메일)이 존재하면 재실행하지 않아
 * 배포/재시작마다 게시글이 중복 생성되지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "seed.enabled", havingValue = "true", matchIfMissing = true)
public class SeedDataInitializer implements ApplicationRunner {

    private static final Random RANDOM = new Random();

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (memberRepository.existsByEmail(AUTHORS.get(0).email())) {
            log.info("샘플(seed) 게시글이 이미 존재하여 초기화를 건너뜁니다.");
            return;
        }

        int totalPosts = 0;

        for (SeedAuthor author : AUTHORS) {
            Member member = Member.create(
                    author.email(),
                    passwordEncoder.encode(UUID.randomUUID().toString()),
                    author.nickname(),
                    author.currentStatus(),
                    author.targetJob()
            );
            memberRepository.save(member);

            // 가장 오래된 글이 오늘로부터 대략 (게시글 수 * 4~7일) 이전이 되도록 시작점을 잡고,
            // 글마다 2~6일씩 간격을 두어 실제로 꾸준히 글을 써온 것처럼 보이게 한다.
            LocalDateTime cursor = LocalDateTime.now().minusDays(author.posts().size() * 5L);

            for (SeedPost seedPost : author.posts()) {
                Post post = Post.create(
                        member,
                        seedPost.title(),
                        seedPost.content(),
                        seedPost.difficulty(),
                        Visibility.PUBLIC,
                        seedPost.studyTime()
                );
                Post saved = postRepository.save(post);

                for (String tagName : seedPost.tags()) {
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.create(tagName)));
                    postTagRepository.save(PostTag.create(saved, tag));
                }

                cursor = cursor.plusDays(2 + RANDOM.nextInt(5))
                        .withHour(9 + RANDOM.nextInt(13))
                        .withMinute(RANDOM.nextInt(60))
                        .withSecond(0)
                        .withNano(0);
                LocalDateTime backdatedAt = cursor.isAfter(LocalDateTime.now())
                        ? LocalDateTime.now().minusHours(RANDOM.nextInt(48))
                        : cursor;

                // Post.createdAt은 @CreationTimestamp가 insert 시점에 강제로 현재 시각을
                // 채우므로, save 이후 네이티브 UPDATE로 과거 시점으로 되돌려야 한다.
                entityManager.flush();
                entityManager.createNativeQuery(
                                "UPDATE til_post SET created_at = :ts, updated_at = :ts WHERE post_id = :id")
                        .setParameter("ts", backdatedAt)
                        .setParameter("id", saved.getId())
                        .executeUpdate();

                totalPosts++;
            }
        }

        log.info("샘플(seed) 회원 {}명, 게시글 {}건 생성 완료", AUTHORS.size(), totalPosts);
    }

    private record SeedPost(String title, String content, Difficulty difficulty, Integer studyTime,
                             List<String> tags) {
    }

    private record SeedAuthor(String email, String nickname, CurrentStatus currentStatus, TargetJob targetJob,
                               List<SeedPost> posts) {
    }

    private static final List<SeedAuthor> AUTHORS = List.of(
            new SeedAuthor(
                    "doyoon@seed.tilog.kr", "backend_doyoon",
                    CurrentStatus.JOB_SEEKER, TargetJob.BACKEND,
                    List.of(
                            new SeedPost(
                                    "Spring Bean 생명주기와 초기화 콜백 정리",
                                    """
                                    오늘은 Spring Bean의 생명주기를 정리했다.

                                    - 컨테이너 생성 → 의존관계 주입 → 초기화 콜백(`@PostConstruct`) → 사용 → 소멸 콜백(`@PreDestroy`) 순서로 진행된다.
                                    - `InitializingBean`/`DisposableBean` 인터페이스를 구현하는 방식도 있지만, 스프링 코드에 강하게 결합되기 때문에 요즘은 `@PostConstruct`/`@PreDestroy` 애노테이션을 더 많이 쓴다는 걸 알게 됐다.
                                    - 생성자 주입을 쓰면 필드에 값이 다 채워진 뒤 빈이 완전한 상태로 만들어지기 때문에, 생성자 안에서는 아직 다른 빈 주입이 끝나지 않았을 수 있는 초기화 로직(다른 빈을 참조하는 로직)은 `@PostConstruct`로 분리하는 게 안전하다는 점이 인상적이었다.

                                    ```java
                                    @Component
                                    public class MyService {
                                        @PostConstruct
                                        public void init() {
                                            log.info("초기화 완료");
                                        }
                                    }
                                    ```

                                    다음엔 `BeanPostProcessor`가 이 생명주기에 어떻게 개입하는지 더 깊게 파볼 예정이다.
                                    """,
                                    Difficulty.EASY, 45,
                                    List.of("Spring", "Java")
                            ),
                            new SeedPost(
                                    "JPA N+1 문제, Fetch Join으로 해결하기",
                                    """
                                    N+1 문제를 직접 재현해보고 해결까지 해봤다.

                                    1. 연관관계가 지연 로딩(LAZY)으로 걸린 엔티티를 리스트로 조회하면, 첫 조회 쿼리 1번 + 각 엔티티마다 연관 엔티티를 조회하는 쿼리 N번이 추가로 나간다는 걸 로그로 직접 확인했다.
                                    2. `fetch join`을 사용하면 연관 엔티티까지 한 번의 쿼리로 함께 가져올 수 있어서 N번의 추가 쿼리가 사라졌다.
                                    3. 다만 컬렉션을 fetch join 하면 페이징이 애플리케이션 메모리에서 처리돼 위험하다는 것도 알게 됐다. 이럴 땐 `@BatchSize`로 IN 절 쿼리를 묶어서 처리하는 방법을 대안으로 정리했다.

                                    ```java
                                    @Query("select p from Post p join fetch p.member where p.isDeleted = false")
                                    List<Post> findAllWithMember();
                                    ```

                                    실무에서는 fetch join과 batch size를 상황에 맞게 섞어 써야 한다는 결론을 내렸다.
                                    """,
                                    Difficulty.HARD, 120,
                                    List.of("JPA", "Spring", "DB")
                            ),
                            new SeedPost(
                                    "트랜잭션 전파 옵션(Propagation) 완전 정리",
                                    """
                                    스프링 트랜잭션 전파 옵션을 표로 정리하며 공부했다.

                                    - `REQUIRED`(기본값): 진행 중인 트랜잭션이 있으면 참여하고, 없으면 새로 시작한다.
                                    - `REQUIRES_NEW`: 항상 새로운 트랜잭션을 시작하고, 기존 트랜잭션은 잠시 보류한다. 알림 발송처럼 실패해도 메인 로직에 영향을 주면 안 되는 부가 작업에 유용하다는 걸 예제로 확인했다.
                                    - `NESTED`: 부모 트랜잭션 안에 저장 지점(savepoint)을 만들어, 실패 시 그 지점까지만 롤백한다.

                                    실습으로 `REQUIRES_NEW`를 걸어둔 로깅 서비스가 예외를 던져도 메인 트랜잭션은 정상 커밋되는 걸 직접 확인해보니 개념이 훨씬 명확해졌다.
                                    """,
                                    Difficulty.NORMAL, 90,
                                    List.of("Spring", "DB")
                            ),
                            new SeedPost(
                                    "Redis로 캐시 적용해서 조회 성능 개선하기",
                                    """
                                    조회가 잦은 API에 Redis 캐시를 붙여봤다.

                                    - `@Cacheable`을 붙이는 것만으로 캐시 적용이 가능하다는 게 신기했지만, 캐시 키 설계를 대충 하면 다른 파라미터 조회 결과가 뒤섞일 수 있다는 걸 삽질하면서 배웠다.
                                    - TTL을 너무 길게 잡으면 데이터 정합성이 깨지고, 너무 짧게 잡으면 캐시 효과가 없어서 도메인 특성에 맞게 조율해야 한다는 걸 체감했다.
                                    - 캐시 적중률을 로그로 남겨보니, 자주 바뀌지 않는 목록 조회 API에서 응답 시간이 눈에 띄게 줄어드는 걸 확인할 수 있었다.

                                    다음엔 캐시 무효화(eviction) 전략까지 다뤄볼 예정이다.
                                    """,
                                    Difficulty.NORMAL, 100,
                                    List.of("Redis", "성능최적화")
                            ),
                            new SeedPost(
                                    "JWT 인증 구조와 Refresh Token 전략",
                                    """
                                    JWT 기반 인증에서 Access Token / Refresh Token을 나누는 이유를 정리했다.

                                    - Access Token은 만료 시간을 짧게 잡아 탈취당해도 피해를 최소화하고, Refresh Token은 별도 저장소(DB/Redis)에 보관해 탈취 시 즉시 폐기할 수 있게 한다는 구조를 배웠다.
                                    - Refresh Token Rotation(재발급마다 새 토큰으로 교체) 전략을 적용하면 하나의 Refresh Token이 여러 번 재사용되는 걸 탐지해 탈취를 더 쉽게 막을 수 있다는 점이 흥미로웠다.
                                    - 프론트에서 Access Token 만료 시 자동으로 재발급 요청을 보내는 인터셉터 흐름도 함께 이해했다.

                                    보안은 한 번에 완성되는 게 아니라 계속 다듬어가야 하는 영역이라는 걸 다시 느꼈다.
                                    """,
                                    Difficulty.NORMAL, 80,
                                    List.of("Spring Security", "인증")
                            ),
                            new SeedPost(
                                    "MySQL 인덱스 튜닝, EXPLAIN으로 실행계획 읽기",
                                    """
                                    느린 쿼리를 `EXPLAIN`으로 분석해봤다.

                                    - `type` 컬럼이 `ALL`(풀스캔)로 나오는 쿼리에 조건절 컬럼 기준 인덱스를 추가하니 `ref`/`range`로 바뀌면서 조회 행 수가 크게 줄었다.
                                    - 복합 인덱스는 컬럼 순서가 중요하다는 것도 실습으로 확인했다. WHERE 절에서 등호 조건으로 자주 쓰이는 컬럼을 앞에 두는 게 핵심이었다.
                                    - 인덱스가 무조건 좋은 건 아니고, 쓰기 성능(INSERT/UPDATE)에는 오버헤드가 생긴다는 트레이드오프도 함께 정리했다.

                                    ```sql
                                    EXPLAIN SELECT * FROM til_post WHERE member_id = 1 AND is_deleted = false;
                                    ```

                                    실행계획을 읽는 눈이 조금씩 생기는 것 같다.
                                    """,
                                    Difficulty.HARD, 110,
                                    List.of("MySQL", "DB")
                            )
                    )
            ),
            new SeedAuthor(
                    "seoyeon@seed.tilog.kr", "frontend_seoyeon",
                    CurrentStatus.STUDENT, TargetJob.FRONTEND,
                    List.of(
                            new SeedPost(
                                    "React useEffect 의존성 배열 제대로 이해하기",
                                    """
                                    `useEffect`의 의존성 배열을 헷갈려 하다가 오늘 확실히 정리했다.

                                    - 의존성 배열을 생략하면 렌더링마다 매번 실행되고, 빈 배열이면 마운트 시 딱 한 번만 실행된다.
                                    - 배열 안에 값을 넣으면 그 값이 바뀔 때만 실행되는데, 객체나 함수를 그대로 넣으면 렌더링마다 참조가 달라져서 의도치 않게 계속 실행될 수 있다는 걸 콘솔 로그로 직접 확인했다.
                                    - ESLint의 `react-hooks/exhaustive-deps` 규칙이 왜 있는지도 이해가 됐다. 의존성을 빠뜨리면 클로저가 오래된 값을 참조하는 버그(stale closure)가 생긴다.

                                    작은 카운터 컴포넌트로 직접 실험해보니 개념이 훨씬 잘 와닿았다.
                                    """,
                                    Difficulty.EASY, 40,
                                    List.of("React", "JavaScript")
                            ),
                            new SeedPost(
                                    "리렌더링 최적화 - useMemo와 useCallback 차이",
                                    """
                                    비슷해 보이는 두 훅의 차이를 명확히 구분해봤다.

                                    - `useMemo`는 계산된 '값'을 메모이제이션하고, `useCallback`은 '함수 자체'를 메모이제이션한다. 사실 `useCallback(fn, deps)`는 `useMemo(() => fn, deps)`와 동일하다는 걸 알고 나니 훨씬 이해가 쉬워졌다.
                                    - 자식 컴포넌트에 콜백을 props로 넘길 때, 부모가 리렌더링될 때마다 새 함수가 생성되면 `React.memo`로 감싼 자식도 같이 리렌더링된다는 걸 직접 확인했다.
                                    - 다만 무조건 남발하면 오히려 메모이제이션 비용이 더 클 수 있어서, 실제로 리렌더링이 문제가 되는 지점에만 선택적으로 적용하는 게 맞다는 결론을 내렸다.

                                    React DevTools Profiler로 리렌더링 횟수를 직접 눈으로 확인하니 확실히 감이 왔다.
                                    """,
                                    Difficulty.NORMAL, 70,
                                    List.of("React", "성능최적화")
                            ),
                            new SeedPost(
                                    "CSS Grid vs Flexbox, 언제 뭘 써야할까",
                                    """
                                    레이아웃 잡을 때마다 헷갈리던 Grid와 Flexbox 기준을 정리했다.

                                    - Flexbox는 1차원(가로 또는 세로) 정렬에 강하고, Grid는 2차원(행과 열 모두)을 동시에 다룰 때 강력하다는 게 핵심 차이였다.
                                    - 카드 목록처럼 아이템 개수가 유동적인 경우엔 Flexbox의 `flex-wrap`이 편하고, 대시보드처럼 영역이 미리 정해진 레이아웃엔 Grid의 `grid-template-areas`가 훨씬 직관적이었다.
                                    - 실제로 같은 레이아웃을 두 방식으로 각각 만들어보니, "정해진 틀 안에 배치"는 Grid, "내용에 따라 흘러가는 배치"는 Flexbox라는 감이 잡혔다.

                                    다음엔 `grid-template-areas`로 반응형 대시보드를 만들어볼 예정이다.
                                    """,
                                    Difficulty.EASY, 35,
                                    List.of("CSS")
                            ),
                            new SeedPost(
                                    "React Query로 서버 상태 관리 입문",
                                    """
                                    클라이언트 상태와 서버 상태를 분리해서 관리해야 하는 이유를 배우며 React Query(TanStack Query)를 처음 써봤다.

                                    - `useQuery` 하나로 로딩/에러/캐싱 상태를 다 관리해주니, 기존에 `useEffect` + `useState`로 직접 짜던 보일러플레이트가 확 줄었다.
                                    - 같은 쿼리 키로 여러 컴포넌트에서 요청해도 중복 호출 없이 캐시를 공유한다는 점이 인상 깊었다.
                                    - `staleTime`과 `cacheTime`의 차이를 헷갈렸는데, staleTime은 "다시 fetch할지 여부", cacheTime은 "캐시를 메모리에서 유지할 시간"이라는 걸 문서를 보며 확실히 구분했다.

                                    서버 상태를 클라이언트 상태처럼 다루던 예전 코드가 왜 복잡했는지 이해가 됐다.
                                    """,
                                    Difficulty.NORMAL, 85,
                                    List.of("React", "TanStackQuery")
                            ),
                            new SeedPost(
                                    "웹 접근성(a11y) 기본기 정리",
                                    """
                                    처음으로 웹 접근성을 신경 써서 컴포넌트를 만들어봤다.

                                    - 의미 없는 `<div onClick>` 대신 `<button>`을 쓰는 것만으로 키보드 포커스와 스크린리더 지원이 기본으로 딸려온다는 걸 알게 됐다.
                                    - 이미지에는 `alt` 속성을, 폼 요소에는 `label`을 반드시 연결해야 스크린리더 사용자가 맥락을 이해할 수 있다는 점을 실습했다.
                                    - `aria-label`, `aria-live` 같은 속성이 동적으로 바뀌는 알림 UI에서 왜 필요한지도 함께 정리했다.

                                    접근성을 "나중에 추가하는 것"이 아니라 "처음부터 고려하는 것"으로 습관을 바꿔야겠다고 느꼈다.
                                    """,
                                    Difficulty.EASY, 50,
                                    List.of("웹접근성", "HTML")
                            ),
                            new SeedPost(
                                    "Vite로 빌드 속도 개선한 경험",
                                    """
                                    CRA(Create React App) 프로젝트를 Vite로 옮겨본 경험을 기록한다.

                                    - 개발 서버 시작 속도가 번들 전체를 미리 묶지 않고 ESM 기반으로 필요한 모듈만 즉시 서빙해서 체감상 훨씬 빨라졌다.
                                    - HMR(Hot Module Replacement) 속도도 눈에 띄게 개선돼서, 코드를 수정하고 화면에 반영되기까지 기다리는 시간이 크게 줄었다.
                                    - 마이그레이션 과정에서 `process.env` 대신 `import.meta.env`를 써야 하는 부분과, 환경변수 접두어를 `VITE_`로 맞춰야 하는 점을 놓쳐서 한참 헤맸다.

                                    빌드 도구 하나 바꾼 것뿐인데 개발 경험이 이렇게 달라질 수 있다는 걸 체감한 하루였다.
                                    """,
                                    Difficulty.NORMAL, 60,
                                    List.of("Vite", "번들러")
                            )
                    )
            ),
            new SeedAuthor(
                    "jihoon@seed.tilog.kr", "devops_jihoon",
                    CurrentStatus.CAREER_CHANGE, TargetJob.INFRA_DEVOPS,
                    List.of(
                            new SeedPost(
                                    "Docker 멀티스테이지 빌드로 이미지 용량 줄이기",
                                    """
                                    Spring Boot 애플리케이션 이미지 용량을 줄이기 위해 멀티스테이지 빌드를 적용했다.

                                    - 빌드 스테이지에서는 JDK 이미지로 `mvnw package`를 실행하고, 런타임 스테이지는 JRE 이미지에 빌드 결과물(jar)만 복사하는 구조로 바꿨다.
                                    - 그 결과 최종 이미지 용량이 눈에 띄게 줄었고, 컨테이너 시작 속도와 배포 시간도 함께 개선됐다.
                                    - 컨테이너를 root가 아닌 별도 사용자로 실행하도록 `USER` 지시어를 추가해 보안까지 챙겼다.

                                    ```dockerfile
                                    FROM eclipse-temurin:21-jdk-jammy AS build
                                    ...
                                    FROM eclipse-temurin:21-jre-jammy
                                    COPY --from=build /app/target/*.jar app.jar
                                    ```
                                    """,
                                    Difficulty.NORMAL, 75,
                                    List.of("Docker", "인프라")
                            ),
                            new SeedPost(
                                    "GitHub Actions로 CI/CD 파이프라인 구축하기",
                                    """
                                    푸시하면 자동으로 빌드 → 이미지 푸시 → 서버 배포까지 이어지는 파이프라인을 처음부터 구성해봤다.

                                    - `on: push: branches: [main]` 트리거로 시작해서, 빌드 job과 배포 job을 분리하니 실패 지점을 훨씬 명확하게 파악할 수 있었다.
                                    - GHCR(GitHub Container Registry)에 이미지를 푸시하고, `appleboy/ssh-action`으로 배포 서버에 접속해 `docker compose pull && up -d`를 실행하는 흐름을 완성했다.
                                    - 워크플로 파일을 하위 디렉터리(`backend/`)가 아니라 반드시 저장소 루트의 `.github/workflows/`에 둬야 트리거가 인식된다는 걸 삽질 끝에 알게 됐다.

                                    파이프라인이 처음으로 초록불로 끝났을 때의 뿌듯함이 컸다.
                                    """,
                                    Difficulty.NORMAL, 95,
                                    List.of("CI/CD", "GithubActions")
                            ),
                            new SeedPost(
                                    "Nginx 리버스 프록시 설정 삽질기",
                                    """
                                    프론트 정적 파일과 백엔드 API를 하나의 도메인에서 서빙하기 위해 Nginx 설정을 손봤다.

                                    - `location /api/` 블록으로 API 요청만 백엔드 컨테이너로 프록시하고, 나머지는 정적 파일과 SPA 라우팅(`try_files`)으로 처리하도록 구성했다.
                                    - 정규식 location과 prefix location의 우선순위 때문에 `/uploads/` 경로가 자꾸 정적 자산 캐시 규칙에 걸려서, `^~` 수식어로 우선순위를 강제 지정해 해결했다.
                                    - SSE(Server-Sent Events) 알림 엔드포인트는 기본 프록시 버퍼링 때문에 실시간으로 전달되지 않아서, `proxy_buffering off`와 긴 `proxy_read_timeout`을 별도로 설정해야 했다.

                                    설정 파일 한 줄 순서 때문에 몇 시간을 날렸지만 그만큼 확실히 이해하게 됐다.
                                    """,
                                    Difficulty.NORMAL, 65,
                                    List.of("Nginx", "인프라")
                            ),
                            new SeedPost(
                                    "AWS EC2 프리티어로 서버 운영하며 배운 것들",
                                    """
                                    개인 프로젝트를 EC2 프리티어(t3.small) 위에서 운영하며 겪은 것들을 정리했다.

                                    - 여러 프로젝트 컨테이너를 한 인스턴스에 같이 띄우다 보니 메모리가 금방 부족해져서, 스왑 파일을 설정해 OOM으로 프로세스가 죽는 걸 방지했다.
                                    - Caddy 하나로 여러 도메인을 호스트 기반(Host header/SNI) 라우팅하니, 인스턴스 하나로도 여러 서비스를 각자의 도메인으로 서빙할 수 있다는 걸 알게 됐다.
                                    - 프리티어라도 크레딧 소진 방식(신규 가입 크레딧 vs 750시간 무료)이 계정 생성 시점에 따라 다르다는 걸 비용 대시보드를 보며 새로 알았다.

                                    작은 서버 하나 운영하는 것도 생각보다 신경 쓸 게 많다는 걸 체감했다.
                                    """,
                                    Difficulty.EASY, 55,
                                    List.of("AWS", "클라우드")
                            ),
                            new SeedPost(
                                    "Linux 스왑 메모리 설정으로 OOM 방지하기",
                                    """
                                    메모리가 넉넉하지 않은 소형 인스턴스에서 컨테이너가 죽는 문제를 스왑으로 해결했다.

                                    ```bash
                                    sudo fallocate -l 1G /swapfile
                                    sudo chmod 600 /swapfile
                                    sudo mkswap /swapfile
                                    sudo swapon /swapfile
                                    ```

                                    - 스왑은 디스크를 메모리처럼 쓰는 것이라 물리 메모리보다는 느리지만, 순간적인 메모리 스파이크로 프로세스가 강제 종료(OOM Killer)되는 걸 막아주는 안전망 역할을 한다는 걸 이해했다.
                                    - 컨테이너별로 `mem_limit`을 지정해서 한 컨테이너가 메모리를 독차지하지 않도록 상한을 걸어두는 것도 함께 적용했다.
                                    - `free -h`로 스왑 사용량을 주기적으로 확인하는 습관도 새로 생겼다.
                                    """,
                                    Difficulty.NORMAL, 50,
                                    List.of("Linux", "인프라")
                            ),
                            new SeedPost(
                                    "Let's Encrypt로 HTTPS 무료 인증서 발급받기",
                                    """
                                    새 서브도메인에 HTTPS를 적용하며 자동 인증서 발급 과정을 정리했다.

                                    - Caddy는 Caddyfile에 도메인만 적어주면 ACME(Let's Encrypt) 프로토콜로 인증서 발급과 갱신을 알아서 처리해준다는 게 가장 편했다.
                                    - 다만 DNS가 아직 새 IP로 전파되지 않은 상태에서 시도하면 `ERR_SSL_PROTOCOL_ERROR`가 발생한다는 걸 직접 겪었다. `nslookup`으로 도메인이 올바른 IP를 가리키는지 먼저 확인하는 습관이 생겼다.
                                    - 인증서 발급/갱신 실패 로그는 Caddy 컨테이너 로그에서 바로 확인할 수 있어서 디버깅이 어렵지 않았다.

                                    DNS 전파를 기다리는 몇 분이 오늘 가장 길게 느껴졌다.
                                    """,
                                    Difficulty.EASY, 40,
                                    List.of("SSL", "인프라")
                            )
                    )
            ),
            new SeedAuthor(
                    "minseo@seed.tilog.kr", "data_minseo",
                    CurrentStatus.EMPLOYED, TargetJob.DATA_ENGINEER,
                    List.of(
                            new SeedPost(
                                    "Python Pandas로 대용량 CSV 처리 최적화",
                                    """
                                    수백만 행짜리 CSV를 다루다 메모리 문제를 겪고 최적화 방법을 정리했다.

                                    - `read_csv`에 `dtype`을 명시해주는 것만으로 판다스가 자동으로 추론하며 낭비하던 메모리를 크게 줄일 수 있었다.
                                    - 전체를 한 번에 읽는 대신 `chunksize` 옵션으로 나눠 읽어 처리하니 메모리 사용량이 안정적으로 유지됐다.
                                    - 반복문으로 행을 순회하며 값을 바꾸던 코드를 벡터 연산(`apply` 대신 벡터화된 연산)으로 바꾸니 처리 속도가 눈에 띄게 빨라졌다.

                                    "동작하는 코드"에서 "효율적인 코드"로 넘어가는 감각을 조금 잡은 것 같다.
                                    """,
                                    Difficulty.NORMAL, 90,
                                    List.of("Python", "Pandas")
                            ),
                            new SeedPost(
                                    "SQL 윈도우 함수 제대로 활용하기",
                                    """
                                    GROUP BY로는 표현하기 까다로웠던 집계를 윈도우 함수로 깔끔하게 풀어봤다.

                                    ```sql
                                    SELECT member_id, written_date,
                                           RANK() OVER (PARTITION BY member_id ORDER BY written_date DESC) AS rnk
                                    FROM til_write_history;
                                    ```

                                    - `PARTITION BY`로 그룹을 나누고 `ORDER BY`로 그룹 내 순서를 매기는 개념을 이해하니, "회원별 최근 게시글 3개" 같은 쿼리를 서브쿼리 없이 한 번에 작성할 수 있었다.
                                    - `RANK`, `DENSE_RANK`, `ROW_NUMBER`의 차이(동점 처리 방식)를 실제 데이터로 비교하며 확실히 구분했다.
                                    - 집계 함수와 다르게 윈도우 함수는 원본 행을 그대로 유지하면서 계산된 값을 추가한다는 점이 GROUP BY와의 가장 큰 차이였다.
                                    """,
                                    Difficulty.NORMAL, 70,
                                    List.of("SQL", "DB")
                            ),
                            new SeedPost(
                                    "Airflow로 배치 파이프라인 스케줄링하기",
                                    """
                                    매일 새벽 실행되는 배치 작업을 Airflow DAG로 옮겨봤다.

                                    - Task 간 의존관계를 `>>` 연산자로 명시적으로 표현하니, 크론잡을 여러 개 늘어놓던 예전 방식보다 전체 흐름을 한눈에 파악하기 훨씬 쉬워졌다.
                                    - 특정 Task가 실패했을 때 그 지점부터만 재실행할 수 있다는 게 크론잡 대비 가장 큰 장점으로 느껴졌다.
                                    - `retries`와 `retry_delay`로 일시적인 네트워크 오류에는 자동 재시도가 되도록 설정하고, Slack 알림 연동으로 실패를 바로 인지할 수 있게 구성했다.

                                    처음엔 개념이 복잡하게 느껴졌는데, 직접 작은 DAG를 하나 완성해보니 구조가 이해됐다.
                                    """,
                                    Difficulty.HARD, 130,
                                    List.of("Airflow", "데이터엔지니어링")
                            ),
                            new SeedPost(
                                    "정규화와 반정규화, 언제 무엇을 선택할까",
                                    """
                                    스키마 설계 시 정규화 수준을 어디까지 가져갈지 고민하며 기준을 정리했다.

                                    - 정규화는 데이터 중복을 줄이고 정합성을 지키는 데 유리하지만, 조회 시 여러 테이블을 조인해야 해서 읽기 성능이 떨어질 수 있다.
                                    - 반정규화는 일부 데이터를 중복 저장해서 조인을 줄이는 대신, 데이터가 여러 곳에 흩어져 있어 갱신 시 정합성 관리가 까다로워진다.
                                    - 실제로는 "쓰기가 많은 테이블은 정규화, 읽기가 압도적으로 많은 통계/리포트성 테이블은 반정규화"라는 경험 규칙을 팀 선배에게 배웠다.

                                    무조건 정답이 있는 게 아니라 트레이드오프를 이해하고 상황에 맞게 선택하는 문제라는 걸 다시 느꼈다.
                                    """,
                                    Difficulty.NORMAL, 60,
                                    List.of("DB", "데이터모델링")
                            ),
                            new SeedPost(
                                    "Kafka 기본 개념과 메시지 큐 이해하기",
                                    """
                                    이벤트 기반 아키텍처를 이해하기 위해 Kafka의 기본 개념을 정리했다.

                                    - Producer가 메시지를 Topic에 발행하고, Consumer가 구독해서 읽는 구조인데, 다른 메시지 큐와 달리 메시지를 즉시 삭제하지 않고 일정 기간 보관한다는 점이 특징적이었다.
                                    - Topic은 여러 Partition으로 나뉘어 병렬 처리가 가능하고, Consumer Group 단위로 Partition을 나눠 가져가며 처리량을 늘릴 수 있다는 걸 이해했다.
                                    - 동기 HTTP 호출로 서비스를 직접 연결하던 방식과 비교해, Kafka를 중간에 두면 서비스 간 결합도를 낮추고 장애가 전파되는 걸 막을 수 있다는 장점이 크게 와닿았다.

                                    개념은 익혔으니 다음엔 직접 Producer/Consumer 코드를 짜볼 예정이다.
                                    """,
                                    Difficulty.HARD, 100,
                                    List.of("Kafka", "메시징")
                            ),
                            new SeedPost(
                                    "데이터 파이프라인 로깅과 모니터링 전략",
                                    """
                                    배치 파이프라인이 조용히 실패하는 문제를 겪고 나서 모니터링 체계를 다시 잡았다.

                                    - 각 Task 단계마다 처리 건수, 소요 시간, 실패 여부를 구조화된 로그(JSON)로 남기니 나중에 문제를 추적하기가 훨씬 수월해졌다.
                                    - 파이프라인이 "실행은 됐지만 결과가 이상한" 경우를 잡기 위해, 처리 건수가 평소 대비 급격히 줄어들면 알림이 가도록 임계치 기반 체크를 추가했다.
                                    - 실패를 사람이 대시보드를 보고 알아채는 대신, Slack/이메일로 즉시 알림이 오도록 자동화한 이후로 장애 인지 시간이 눈에 띄게 줄었다.

                                    "돌아가는 파이프라인"과 "믿을 수 있는 파이프라인"은 다르다는 걸 깨달은 하루였다.
                                    """,
                                    Difficulty.NORMAL, 75,
                                    List.of("모니터링", "데이터엔지니어링")
                            )
                    )
            ),
            new SeedAuthor(
                    "haneul@seed.tilog.kr", "fullstack_haneul",
                    CurrentStatus.FREELANCER, TargetJob.FULLSTACK,
                    List.of(
                            new SeedPost(
                                    "REST API 설계 원칙과 네이밍 컨벤션",
                                    """
                                    프리랜서로 여러 프로젝트를 거치며 정리해온 REST API 설계 기준을 다시 정리했다.

                                    - URI는 동사가 아닌 명사(자원)로 표현하고, 행위는 HTTP 메서드(GET/POST/PATCH/DELETE)로 나타내는 게 기본 원칙이다.
                                    - 컬렉션은 복수형(`/posts`), 특정 자원은 `/posts/{id}`처럼 계층 구조로 표현하면 일관성이 생겨 API 문서 없이도 어느 정도 유추가 가능해진다.
                                    - 응답 포맷을 `{ success, data, message }` 형태로 통일해두면 프론트에서 공통 에러 처리 로직을 훨씬 간단하게 짤 수 있다는 걸 여러 프로젝트를 거치며 체감했다.

                                    작은 컨벤션 하나가 협업 속도를 크게 좌우한다는 걸 다시 느꼈다.
                                    """,
                                    Difficulty.EASY, 45,
                                    List.of("API설계", "백엔드")
                            ),
                            new SeedPost(
                                    "TypeScript 제네릭 기초부터 실전까지",
                                    """
                                    막연하게 쓰던 제네릭을 제대로 이해하려고 기초부터 다시 훑었다.

                                    ```typescript
                                    function wrapResponse<T>(data: T): { success: boolean; data: T } {
                                      return { success: true, data };
                                    }
                                    ```

                                    - 제네릭은 "타입을 함수의 매개변수처럼 다루는 것"이라는 설명이 가장 와닿았다. 호출 시점에 실제 타입이 결정된다는 점이 핵심이었다.
                                    - `extends`로 제네릭에 제약을 거는 방법을 익히니, 아무 타입이나 허용하지 않고 특정 속성을 가진 타입만 받도록 안전하게 제한할 수 있었다.
                                    - API 응답 타입을 제네릭으로 감싸두니 엔드포인트마다 반복하던 타입 선언이 크게 줄어들었다.

                                    타입을 값처럼 다루는 감각이 조금씩 생기는 것 같다.
                                    """,
                                    Difficulty.NORMAL, 80,
                                    List.of("TypeScript")
                            ),
                            new SeedPost(
                                    "모노레포로 프론트/백엔드 관리하기",
                                    """
                                    별도 저장소로 나뉘어 있던 프론트/백엔드를 하나의 모노레포로 합쳐본 경험을 기록한다.

                                    - `git subtree`로 기존 두 저장소의 커밋 히스토리를 보존하면서 하나의 저장소로 합쳤다. 히스토리가 남아있어서 나중에 특정 변경을 추적하기 훨씬 편했다.
                                    - CI 워크플로 파일을 반드시 저장소 루트의 `.github/workflows/`에 둬야 하며, 하위 디렉터리에 두면 트리거가 아예 인식되지 않는다는 걸 직접 겪고 알게 됐다.
                                    - 프론트와 백엔드를 같은 저장소에서 관리하니 하나의 기능 변경이 양쪽 모두에 걸쳐 있을 때 PR을 하나로 묶어 리뷰할 수 있어 편리했다.

                                    저장소를 나누는 것과 합치는 것 모두 장단점이 있다는 걸 균형 있게 이해하게 됐다.
                                    """,
                                    Difficulty.NORMAL, 70,
                                    List.of("모노레포", "프로젝트관리")
                            ),
                            new SeedPost(
                                    "WebSocket으로 실시간 알림 기능 구현하기",
                                    """
                                    폴링 방식으로 구현했던 알림 기능을 실시간 방식으로 개선하며 SSE와 WebSocket을 비교해봤다.

                                    - WebSocket은 양방향 통신이 가능해 채팅처럼 클라이언트도 서버에 자주 메시지를 보내야 하는 경우에 적합하고, SSE(Server-Sent Events)는 서버 → 클라이언트 단방향 알림에 더 가볍게 쓸 수 있다는 차이를 이해했다.
                                    - 이번 프로젝트는 서버에서 클라이언트로 알림만 보내면 됐기 때문에 별도 프로토콜 업그레이드가 필요 없는 SSE로 먼저 구현했다.
                                    - 다만 SSE는 기본적으로 프록시 서버(Nginx)의 응답 버퍼링 설정 때문에 실시간으로 전달되지 않을 수 있어서, 버퍼링을 끄는 설정이 반드시 필요하다는 걸 배웠다.

                                    실시간 기능은 프론트-백엔드-인프라 세 영역을 모두 이해해야 제대로 구현할 수 있다는 걸 느꼈다.
                                    """,
                                    Difficulty.HARD, 110,
                                    List.of("WebSocket", "실시간통신")
                            ),
                            new SeedPost(
                                    "OAuth2 소셜 로그인 연동 정리",
                                    """
                                    구글/깃허브 소셜 로그인을 연동하며 OAuth2 인가 코드 흐름을 정리했다.

                                    - 클라이언트가 인가 서버로 리다이렉트되어 사용자 동의를 받고, 인가 코드를 발급받아 서버가 그 코드로 액세스 토큰을 교환하는 흐름(Authorization Code Grant)을 직접 구현하며 이해했다.
                                    - 소셜 로그인으로 받은 이메일이 이미 일반 회원가입으로 존재하는 경우 계정을 어떻게 연동할지(신규 생성 vs 기존 계정 병합) 정책을 미리 정해둬야 한다는 걸 실제로 겪으며 깨달았다.
                                    - Redirect URI는 인가 서버 콘솔에 등록된 값과 정확히 일치해야 하는데, 로컬 개발/운영 환경의 URI가 달라 등록을 빠뜨려 한참 헤맸다.

                                    OAuth2는 개념보다 실제 콘솔 설정과 리다이렉트 흐름을 맞추는 게 더 까다로웠다.
                                    """,
                                    Difficulty.NORMAL, 85,
                                    List.of("OAuth2", "인증")
                            ),
                            new SeedPost(
                                    "사이드 프로젝트 배포 자동화 삽질기",
                                    """
                                    수동으로 SSH 접속해서 배포하던 사이드 프로젝트를 CI/CD로 자동화한 과정을 기록한다.

                                    - 커밋을 푸시하면 GitHub Actions가 Docker 이미지를 빌드해 레지스트리에 올리고, 배포 서버에 SSH로 접속해 최신 이미지를 받아 컨테이너를 재시작하는 흐름을 구성했다.
                                    - 이미지 레지스트리 패키지가 기본적으로 Private로 생성돼서, 배포 서버가 익명으로 pull하려다 `unauthorized` 오류가 나는 걸 겪고 나서야 Public으로 바꿔야 한다는 걸 알게 됐다.
                                    - 배포 직후 헬스체크 없이 바로 트래픽을 흘려보내던 걸, 컨테이너에 `HEALTHCHECK`를 추가해 준비가 끝난 뒤에만 정상 취급되도록 개선했다.

                                    수동 배포 몇 번의 실수를 겪고 나서야 자동화의 필요성을 몸으로 느꼈다.
                                    """,
                                    Difficulty.NORMAL, 65,
                                    List.of("배포", "DevOps")
                            )
                    )
            )
    );
}
