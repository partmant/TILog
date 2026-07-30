# TILog (틸로그)

매일의 학습 기록(TIL, Today I Learned)을 습관으로 만들고, 스트릭·잔디 히트맵과 AI 주간 리포트로
성장 과정을 시각화하며, 구독 기반 페이백으로 꾸준한 기록을 장려하는 개발자용 학습 기록 플랫폼입니다.

- **프로젝트 기간**: 2026.05.28 ~ 2026.06.07
- **팀 구성**: 6인 (백엔드·프론트엔드 통합)
- **담당**: 마이페이지 전반(작성 이력·스트릭·잔디 히트맵, 구독·페이백, 프로필) · 로그인 연동 · 프로젝트 초기 세팅
- **시연 영상**: _(유튜브 업로드 후 링크 추가 예정)_

> 본 저장소는 6인 팀 프로젝트의 백엔드/프론트엔드 코드를 개인 포트폴리오 용도로 하나의 저장소에
> 옮겨온 사본이며, 각 폴더(`backend/`, `frontend/`)의 원본 커밋 이력을 그대로 유지했습니다.
> - 팀 원본 백엔드 저장소: https://github.com/TILOGER/tilog-backend
> - 팀 원본 프론트엔드 저장소: https://github.com/TILOGER/tilog-frontend
> - Figma 디자인: https://www.figma.com/design/rXPQm12x7OslfOKFnxrxfh/TILog-%EC%99%80%EC%9D%B4%EC%96%B4%ED%94%84%EB%A0%88%EC%9E%84?node-id=0-1&p=f&t=Ifjv43hipmFSS8Vu-0

---

## 문제 정의

TIL(오늘 배운 것) 기록 서비스는 이미 여러 곳에 있지만, 대부분 단순 텍스트 기록에 그쳐 있어
다음과 같은 한계가 있었습니다.

- 기록 자체는 쉬워도 습관으로 이어지지 않아 며칠 못 가 중단되는 경우가 많음
- 얼마나 꾸준히, 얼마나 다양한 주제로 학습했는지 스스로 확인하기 어려움
- 기록만 쌓일 뿐 개선 방향에 대한 피드백이나 동기부여 장치가 없음

TILog는 작성 이력을 스트릭과 잔디 히트맵으로 시각화해 습관 형성을 돕고, Gemini 기반 주간
리포트로 한 주간의 학습 흐름을 요약하며, 멘토 피드백과 구독 기반 페이백으로 꾸준한 기록에
대한 동기를 부여합니다.

---

## 담당 영역

- **프로젝트 초기 세팅**: Spring Boot 프로젝트 초기화, DB·JPA 환경 설정, CORS 설정,
  Spring Security 초기 설정(`SecurityConfig`), JWT 인증 필터 적용
- **작성 이력 · 스트릭 · 잔디 히트맵**: 날짜별 작성 이력 기록, 회원 연관관계 기반으로 이력·스트릭
  로직 재설계, 연속 작성일수(스트릭) 계산, 잔디 히트맵 데이터 조회 (`WriteHistoryController`,
  `WriteHistoryQueryController`, `StreakStatController`)
- **구독 · 페이백**: Mock 구독 신청/취소 예약(`CANCEL_RESERVED`)/기간 연장 및 자동 갱신 스케줄러
  구현, 구독 기간 기준 페이백 정책 조회·참여 기능 (`SubscriptionController`, `SubscriptionService`,
  `PaybackPolicyController`, `PaybackParticipationController`)
- **로그인 연동 · 마이페이지**: 로그인 연동 및 마이페이지 실사용자 데이터 연결, 프로필 이미지
  업로드·조회, 회원 상태(`currentStatus`)·목표 직무(`targetJob`) 수정 기능
- **TIL 검색 · 즐겨찾기**: 내가 작성한 TIL 검색 API, TIL 즐겨찾기 API
- **프론트엔드**: 마이페이지 구현(작성 이력·스트릭·구독/페이백 현황 UI), 로직을 커스텀 훅으로
  분리하는 리팩터링, 공통 헤더·푸터 레이아웃 분리, JWT 디코딩 및 로그인 연동, 마이페이지 내
  TIL 검색/필터/정렬, 즐겨찾기 화면

아래 "주요 기능"은 팀 전체 기능이며, 담당자는 "팀원 역할 분담"에 별도로 표시했습니다.

---

## 주요 기능

### 1. 계정 · 인증

- 회원가입, 로그인/로그아웃, JWT(Access) 발급 및 인증
- 역할 기반 권한 관리(`USER` / `PREMIUM` / 멘토 / 관리자)
- Mock 구독 신청 — 프리미엄(`PREMIUM`) 권한 부여

### 2. TIL 작성

- TIL 게시글 CRUD
- 기술 스택 태그, 난이도·소요시간 입력
- 게시글 상세 조회, Gemini 기반 게시글 AI 요약

### 3. 소셜

- 댓글 작성·수정·삭제, 좋아요
- 팔로우, 팔로잉/팔로워 목록
- 전체 피드 조회

### 4. 검색

- 통합 검색, 페이징
- QueryDSL 기반 동적 검색·정렬, 태그 전체 조회(상세검색용)

### 5. 작성 이력 · 성장 관리

- 날짜별 작성 이력 기록, 연속 작성일수(스트릭) 계산, 잔디 히트맵
- Mock 구독 신청/취소 예약/자동 갱신
- 구독 기간 기준 페이백 정책 조회 및 참여
- 내가 작성한 TIL 검색, TIL 즐겨찾기
- 마이페이지 프로필(이미지, 현재 상태, 목표 직무) 관리

### 6. AI 주간 리포트

- Gemini 연동, 주간 활동 기반 리포트 자동 생성(캐시 저장, 완료된 주만 생성 가능)
- 작성 TIL 0개 등 경계 상황 예외 처리
- 게시글 상세 페이지에 AI 핵심 요약 노출

### 7. 관리자

- 회원 관리, 게시글 관리
- 멘토 권한 승격, 멘토 피드백(점수·코멘트)
- 신고 접수 및 제재 로직

### 8. 알림

- 인앱 알림, SSE(Server-Sent Events) 기반 실시간 알림(피드백 등)

---

## 🖼 주요 화면

### 로그인 · 회원가입

<p>
  <img src="images/login.png" width="45%">
  <img src="images/signup.png" width="45%">
</p>

### 메인 피드 · TIL 상세

<p>
  <img src="images/main-feed.png" width="45%">
  <img src="images/til-detail.png" width="45%">
</p>

- TIL 상세 화면에서는 멘토 피드백 요청·확인을 사이드 패널로 함께 제공합니다.

### 마이페이지 [담당]

<p>
  <img src="images/mypage.png" width="45%">
  <img src="images/mypage-report-favorites.png" width="45%">
</p>

- 작성 이력·연속 작성일수(스트릭)·잔디 히트맵, 구독/페이백 현황을 한 화면에서 확인합니다.
- AI 주간 리포트 요약, 최근 작성한 TIL, 즐겨찾기한 TIL도 마이페이지에서 바로 확인할 수 있습니다.

### AI 주간 성장 리포트

![AI 주간 성장 리포트 상세](images/weekly-report-detail.png)

- 난이도/카테고리 분포, 기술 스택 TOP3, 규칙 기반 코멘트와 Gemini 기반 심층 분석을 함께 제공합니다.

### 멘토 피드백

![멘토 피드백 목록](images/feedback-list.png)

### 관리자

<p>
  <img src="images/admin-dashboard.png" width="45%">
  <img src="images/admin-member-management.png" width="45%">
</p>

추가 화면과 전체 사용 흐름은 시연 영상에서 확인할 수 있습니다. _(링크 추가 예정)_

---

## 기술 스택

### Backend

- Java 21, Spring Boot 4.0.6
- Spring Data JPA, QueryDSL 5.x(Jakarta), MySQL
- Spring Security, JWT(jjwt 0.12.6)
- Google Gemini API(`gemini-2.5-flash`) — AI 주간 리포트, 게시글 요약
- Maven

### Frontend

- React 19, Vite 8, React Router 7
- Zustand(상태 관리), Axios
- Tailwind CSS 4
- Toast UI Editor(마크다운 에디터), react-markdown / remark-gfm / rehype-highlight
- Chart.js / react-chartjs-2(통계 차트)
- event-source-polyfill(SSE)

### 시스템 구성

| 구분 | 기술 |
|---|---|
| Frontend | React, Vite, React Router (`frontend/`) |
| Backend | Spring Boot REST API (`backend/`) |
| DB | MySQL |
| AI | Google Gemini API |
| 실시간 알림 | SSE |

### 아키텍처

![TILog 시스템 아키텍처](images/architecture.png)

React 프론트엔드와 Spring Boot API 서버가 REST API(JWT 인증)로 통신하고, 활동 데이터는 MySQL에
저장합니다. 저장된 활동 기록을 바탕으로 Gemini API가 주간 성장 리포트를 생성하며, 멘토 피드백·
팔로우·좋아요 알림은 SSE로 실시간 전송됩니다.

### ERD

![TILog ERD](images/erd.png)

---

## 핵심 설계 의사결정

**구독 취소는 즉시 만료가 아니라 예약(`CANCEL_RESERVED`) 방식으로 처리**
구독을 취소해도 이미 결제(Mock)한 기간이 남아 있다면 그 혜택을 바로 빼앗는 것은 사용자 경험상
바람직하지 않다고 판단했습니다. `cancel()` 호출 시 구독 상태만 `ACTIVE → CANCEL_RESERVED`로
바꾸고 `endedAt`은 그대로 유지해, 만료 시점까지는 PREMIUM 권한과 페이백 참여 자격을 유지합니다.
스케줄러(`SubscriptionService.processExpiredSubscriptions`)가 만료된 구독을 주기적으로 스캔해
`CANCEL_RESERVED`는 `EXPIRED` 처리 후 역할을 `USER`로 되돌리고, 정상 만료된 `ACTIVE` 구독은
자동으로 신규 구독을 생성해 갱신합니다.

**작성 이력·스트릭을 회원 연관관계 기반으로 재설계**
초기에는 작성 이력을 게시글 단위로 조회했지만, 스트릭 계산과 잔디 히트맵 조회가 잦아지면서
회원 기준 연관관계로 조회 구조를 바꾸고, 생성 시각만 필요한 이력성 엔티티를 위해
`BaseCreateTimeEntity`를 별도로 분리했습니다. 덕분에 스트릭 계산과 히트맵 데이터 조회 로직을
단순화할 수 있었습니다.

---

## 실행 방법

### 1. 저장소 클론

```bash
git clone <이 저장소 URL>
cd tilog
```

### 2. Backend 환경 변수

`backend` 폴더에 `application-local.yml` 대신 아래 환경 변수를 설정하거나 `.env`/실행 환경에
주입합니다.

```env
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Base64로 인코딩된 256bit 이상의 키 (운영에서는 반드시 환경변수로 주입)
JWT_SECRET=your_jwt_secret

# Gemini 기반 AI 주간 리포트 / 게시글 요약에 사용
GOOGLE_GEMINI_API_KEY=your_gemini_api_key
```

### 3. Backend 실행

```bash
cd backend
./mvnw spring-boot:run
```

서버는 기본적으로 `8080` 포트에서 실행됩니다. (DB: `jdbc:mysql://localhost:3306/tilog`)

### 4. Frontend 환경 변수

`frontend` 폴더에 `.env` 파일을 생성합니다.

```env
VITE_API_BASE_URL=http://localhost:8080
```

### 5. Frontend 실행

```bash
cd frontend
npm install
npm run dev
```

---

## 패키지 구조

```
backend/src/main/java/com/tilog/
├── domain/
│   ├── auth/            # 회원가입·로그인·JWT [담당: 박규태]
│   ├── member/          # 회원 정보·프로필
│   ├── post/            # TIL 게시글 CRUD·상세·피드·검색 [담당: 김진수]
│   ├── comment/         # 댓글
│   ├── like/            # 좋아요
│   ├── follow/          # 팔로우
│   ├── tag/              # 기술 스택 태그
│   ├── writeHistory/    # 작성 이력 [담당]
│   ├── streak/          # 스트릭·잔디 히트맵 [담당]
│   ├── subscription/    # Mock 구독·취소예약·자동갱신 [담당]
│   ├── payback/         # 페이백 정책·참여 [담당]
│   ├── report/          # AI 주간 리포트(Gemini)
│   ├── feedback/        # 멘토 피드백
│   ├── notification/    # 인앱 알림·SSE
│   └── admin/           # 관리자 회원·게시글 관리
└── global/
    ├── security/        # JWT 인증·인가 [담당 일부]
    ├── client/           # Gemini 클라이언트
    ├── config/, exception/, response/, entity/

frontend/src/
├── api/                 # 도메인별 API 모듈
├── pages/
│   ├── admin/           # 관리자 화면
│   ├── feed/            # 전체 피드
│   ├── feedbacks/       # 멘토 피드백
│   ├── mypage/          # 마이페이지 [담당]
│   └── post/            # TIL 작성·상세
├── components/
│   ├── mypage/          # 작성 이력·구독/페이백 UI [담당]
│   ├── report/          # 주간 리포트 차트
│   └── search/          # 검색·페이징
├── hooks/
│   ├── mypage/, post/, report/, sse/, common/
├── layouts/, router/, styles/, utils/
```

---

## 팀원 역할 분담

| 이름 | 담당 |
|---|---|
| 박규태 | 회원가입, 로그인/로그아웃, JWT 발급·인증, 권한 관리, Mock 구독 |
| 김진수 | TIL 게시글 CRUD, 기술 스택 태그, 난이도/소요시간 입력, 상세 조회 |
| 배재욱 | 댓글, 좋아요, 팔로우, 전체 피드 |
| 김기전 | 검색, 페이징, QueryDSL 동적 검색 |
| 홍창희 | 작성 이력 기록, 스트릭 계산, 잔디 히트맵, 구독·페이백 |
| 김민규 | 관리자 회원 관리, 게시글 관리, 멘토 권한 승격, 멘토 피드백 |

---

🔗 [Figma 디자인 바로가기](https://www.figma.com/design/rXPQm12x7OslfOKFnxrxfh/TILog-%EC%99%80%EC%9D%B4%EC%96%B4%ED%94%84%EB%A0%88%EC%9E%84?node-id=0-1&p=f&t=Ifjv43hipmFSS8Vu-0)
