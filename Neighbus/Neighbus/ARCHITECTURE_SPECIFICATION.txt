================================================================================
                   NEIGHBUS 프로젝트 아키텍처 및 기능정의서
================================================================================

프로젝트명: NEIGHBUS (네이버스 - Neighborhood + Bus)
프로젝트 유형: 지역 기반 커뮤니티 플랫폼
개발 기간: 2024.10 ~ 2024.12
개발 인원: 4명
버전: 0.0.1-SNAPSHOT

================================================================================
1. 프로젝트 개요
================================================================================

1.1 프로젝트 목적
---------------------------------------------------------------------------
NEIGHBUS는 지역 주민들을 위한 통합 커뮤니티 플랫폼으로, 이웃 간 소통과
취미 활동 공유를 통해 지역 사회의 연결성을 강화하는 것을 목표로 합니다.

1.2 주요 특징
---------------------------------------------------------------------------
- 지역 기반 동아리 활동 지원
- 실시간 채팅 및 알림 시스템
- AI 챗봇을 통한 사용자 지원
- 관리자 대시보드를 통한 통합 관리
- OAuth2 소셜 로그인 지원
- 반응형 웹 디자인

1.3 기대 효과
---------------------------------------------------------------------------
- 지역 주민 간 유대감 형성
- 취미 활동 활성화를 통한 삶의 질 향상
- 오프라인 모임 촉진을 통한 실질적 커뮤니티 형성


================================================================================
2. 시스템 아키텍처
================================================================================

2.1 기술 스택
---------------------------------------------------------------------------

【백엔드】
- Framework: Spring Boot 3.5.8
- Language: Java 17
- ORM: MyBatis 3.0.3
- Security: Spring Security 6
- Authentication: OAuth2 Client
- Validation: Spring Validation
- Scheduler: Spring Scheduling
- WebSocket: Spring WebSocket
- Email: Spring Mail
- SMS: Nurigo SDK 4.3.0

【프론트엔드】
- Template Engine: Thymeleaf
- JavaScript: Vanilla JS (ES6+)
- CSS: Custom CSS + Bootstrap
- Icons: Font Awesome
- Charts: Chart.js

【데이터베이스】
- DBMS: MySQL 8.0+
- Connection Pool: HikariCP (Spring Boot Default)

【AI/ML】
- AI Platform: Spring AI 1.1.0
- Model: OpenAI GPT

【개발 도구】
- Build Tool: Gradle
- IDE Support: Spring Boot DevTools
- Testing: JUnit 5, Spring Test, MyBatis Test
- Lombok: 코드 간소화

【인프라】
- Mac M1/M2/M3 (Apple Silicon) 최적화
- Netty DNS Resolver for macOS


2.2 시스템 아키텍처 구조
---------------------------------------------------------------------------

┌─────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Thymeleaf   │  │  REST API    │  │  WebSocket   │          │
│  │  Templates   │  │  Controller  │  │  Handler     │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         Security Layer                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Spring Security + OAuth2 + CSRF Protection              │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         Business Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Service    │  │   Scheduler  │  │   Validator  │          │
│  │   Layer      │  │   Tasks      │  │   Layer      │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Persistence Layer                           │
│  ┌──────────────┐  ┌──────────────┐                            │
│  │   MyBatis    │  │     DTO      │                            │
│  │   Mapper     │  │   Objects    │                            │
│  └──────────────┘  └──────────────┘                            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         Data Layer                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    MySQL Database                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      External Services                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   OpenAI     │  │  OAuth2      │  │   Nurigo     │          │
│  │   API        │  │  Providers   │  │   SMS API    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘


2.3 패키지 구조
---------------------------------------------------------------------------

com.neighbus
├── about              # 서비스 소개
├── account            # 회원 인증/인가
│   ├── Controller     # 로그인, 회원가입, OAuth2
│   ├── Service        # 사용자 인증 처리
│   └── DTO            # 사용자 데이터 전송
├── admin              # 관리자 기능
│   ├── Controller     # 관리자 페이지 라우팅
│   ├── RestController # 관리자 API
│   ├── Service        # 관리자 비즈니스 로직
│   └── Mapper         # 통합 관리 쿼리
├── alarm              # 알림 시스템
│   ├── Controller     # 알림 API
│   ├── Service        # 알림 처리 로직
│   └── DTO            # 알림 데이터
├── chat               # 실시간 채팅
│   ├── WebSocket      # WebSocket 핸들러
│   ├── Service        # 채팅 메시지 처리
│   └── DTO            # 채팅 메시지 구조
├── chatbot            # AI 챗봇
│   ├── Controller     # 챗봇 API
│   ├── Service        # OpenAI 연동
│   └── DTO            # 대화 데이터
├── club               # 동아리 관리
│   ├── Controller     # 동아리 CRUD
│   ├── Service        # 동아리 비즈니스 로직
│   ├── Mapper         # 동아리 쿼리
│   └── DTO            # 동아리 데이터
├── config             # 설정
│   ├── SecurityConfig # Spring Security 설정
│   ├── WebSocketConfig# WebSocket 설정
│   └── MyBatisConfig  # MyBatis 설정
├── freeboard          # 자유게시판
│   ├── Controller     # 게시글 CRUD
│   ├── Service        # 게시글 처리
│   ├── Mapper         # 게시글 쿼리
│   └── DTO            # 게시글/댓글 데이터
├── friend             # 친구 관리
│   ├── Service        # 친구 추가/삭제
│   └── DTO            # 친구 관계 데이터
├── gallery            # 갤러리
│   ├── Controller     # 갤러리 CRUD
│   ├── Service        # 이미지 처리
│   └── DTO            # 갤러리 데이터
├── inquiry            # 문의하기
│   ├── Controller     # 문의 접수
│   ├── Service        # 문의 처리
│   └── DTO            # 문의 데이터
├── main               # 메인 페이지
│   ├── Controller     # 홈 화면
│   └── Service        # 메인 데이터 통합
├── mypage             # 마이페이지
│   ├── Controller     # 개인정보 관리
│   └── Service        # 프로필 수정
├── notice             # 공지사항
│   ├── Controller     # 공지 CRUD
│   ├── Service        # 공지 관리
│   └── DTO            # 공지 데이터
├── recruitment        # 모임 모집
│   ├── Controller     # 모임 CRUD
│   ├── JobController  # 스케줄러 트리거
│   ├── Service        # 모임 처리
│   ├── Scheduler      # 자동 마감
│   ├── Mapper         # 모임 쿼리
│   └── DTO            # 모임 데이터
├── util               # 유틸리티
│   └── PasswordEncoder# 비밀번호 암호화
└── weather            # 날씨 정보
    ├── Controller     # 날씨 API
    └── Service        # 외부 날씨 API 연동


2.4 데이터베이스 설계
---------------------------------------------------------------------------

【주요 테이블】

1. users (회원)
   - id (PK), username, password, email, name, nickname
   - phone, birth_date, gender, address, profile_image
   - created_at, role, status

2. clubs (동아리)
   - id (PK), club_name, description, category
   - location, max_members, current_members
   - created_at, leader_id (FK -> users)

3. club_members (동아리 회원)
   - id (PK), club_id (FK), user_id (FK)
   - joined_at, role

4. recruitments (모임)
   - id (PK), club_id (FK), title, content
   - writer (FK -> users), address, max_user
   - meeting_date, latitude, longitude
   - status (OPEN/CLOSED), created_at

5. recruitment_member (모임 참여자)
   - recruitment_id (FK), user_id (FK)

6. freeboards (자유게시판)
   - id (PK), title, content, writer (FK -> users)
   - view_count, created_at, updated_at

7. freeboard_comments (댓글)
   - id (PK), freeboard (FK), writer (FK -> users)
   - content, created_at, parent_comment_id

8. galleries (갤러리)
   - id (PK), title, content, writer (FK -> users)
   - club_id (FK), image_url, view_count
   - created_at

9. gallery_comments (갤러리 댓글)
   - id (PK), gallery_id (FK), writer (FK -> users)
   - content, created_at

10. reports (신고)
    - id (PK), type (POST/COMMENT/USER/CLUB/GALLERY/GATHERING)
    - reporter_id (FK -> users), target_id
    - reason, status (PROCESSING/COMPLETED/REJECTED)
    - created_at

11. notices (공지사항)
    - id (PK), title, content, writer (FK -> users)
    - created_at, updated_at, is_pinned

12. alarms (알림)
    - id (PK), user_id (FK -> users), type
    - content, is_read, created_at, related_url

13. chat_messages (채팅)
    - id (PK), room_id, sender_id (FK -> users)
    - message, sent_at

14. friends (친구)
    - id (PK), user_id (FK), friend_id (FK)
    - status (PENDING/ACCEPTED/BLOCKED)
    - created_at

15. inquiries (문의)
    - id (PK), user_id (FK -> users), title
    - content, answer, status, created_at


2.5 보안 설계
---------------------------------------------------------------------------

【인증 (Authentication)】
- Spring Security 기반 세션 인증
- OAuth2 소셜 로그인 (Google, Naver, Kakao)
- BCrypt 패스워드 암호화
- Remember-Me 기능

【인가 (Authorization)】
- Role 기반 접근 제어 (ROLE_USER, ROLE_ADMIN)
- 메서드 레벨 보안 (@PreAuthorize)
- URL 패턴 기반 접근 제어

【보안 조치】
- CSRF 토큰 검증 (모든 POST/PUT/DELETE 요청)
- XSS 방지 (Thymeleaf 자동 이스케이프)
- SQL Injection 방지 (MyBatis PreparedStatement)
- 세션 고정 공격 방지
- 클릭재킹 방지 (X-Frame-Options)


================================================================================
3. 핵심 기능 명세
================================================================================

3.1 회원 관리 (Account)
---------------------------------------------------------------------------

【회원가입】
- 기능: 일반 회원가입 및 소셜 로그인
- 구현:
  * 이메일 중복 검증
  * 비밀번호 강도 검증 (8자 이상, 영문+숫자+특수문자)
  * 휴대폰 인증 (Nurigo SMS API)
  * 프로필 이미지 업로드
- 엔드포인트: POST /account/signup

【로그인/로그아웃】
- 기능: 일반 로그인, 소셜 로그인, 자동 로그인
- 구현:
  * Spring Security Form Login
  * OAuth2 Client (Google, Naver, Kakao)
  * Remember-Me 토큰
- 엔드포인트:
  * POST /account/login
  * GET /account/logout
  * GET /oauth2/authorization/{provider}

【비밀번호 찾기】
- 기능: 이메일 인증을 통한 비밀번호 재설정
- 구현:
  * 임시 비밀번호 이메일 발송
  * 비밀번호 재설정 링크 생성
- 엔드포인트: POST /account/reset-password


3.2 동아리 관리 (Club)
---------------------------------------------------------------------------

【동아리 생성】
- 기능: 사용자가 새로운 동아리 개설
- 구현:
  * 카테고리 선택 (운동, 문화, 학습, 봉사 등)
  * 위치 정보 입력 (지역 기반)
  * 최대 인원 설정
  * 동아리 소개 및 이미지 업로드
- 검증:
  * 중복 동아리명 방지
  * 최소/최대 인원 제한
- 엔드포인트: POST /club/create

【동아리 가입/탈퇴】
- 기능: 동아리 회원 관리
- 구현:
  * 가입 신청 및 승인
  * 회원 역할 관리 (리더, 부리더, 일반)
  * 회원 강제 탈퇴 (리더 권한)
- 엔드포인트:
  * POST /club/{clubId}/join
  * POST /club/{clubId}/leave
  * POST /club/{clubId}/kick

【동아리 목록 조회】
- 기능: 동아리 검색 및 필터링
- 구현:
  * 카테고리별 필터
  * 지역별 필터
  * 키워드 검색
  * 페이지네이션
- 엔드포인트: GET /club/list


3.3 모임 관리 (Recruitment)
---------------------------------------------------------------------------

【모임 생성】
- 기능: 동아리 회원이 오프라인 모임 개설
- 구현:
  * 모임 일시 및 장소 설정
  * 최대 참여 인원 설정
  * 지도 API 연동 (위도/경도)
  * 모임 상세 설명 작성
- 엔드포인트: POST /recruitment/create

【모임 참여/취소】
- 기능: 모임 참여 관리
- 구현:
  * 실시간 참여 인원 표시
  * 마감 시 참여 불가
  * 참여 취소 기능
- 엔드포인트:
  * POST /recruitment/{id}/join
  * POST /recruitment/{id}/cancel

【자동 마감 처리】
- 기능: 스케줄러를 통한 자동 마감
- 구현:
  * @Scheduled 어노테이션 활용
  * 매시간 정각 실행 (Cron: 0 0 * * * *)
  * meeting_date < NOW() 조건으로 status를 CLOSED로 변경
  * 처리 건수 로깅
- 클래스: RecruitmentScheduler.java
- 쿼리: updateExpiredRecruitments()

【모임 통계】
- 기능: 관리자 대시보드 통계 제공
- 구현:
  * 진행 중/종료된 모임 개수
  * 카테고리별 모임 분포 (Top 5)
  * 월별 모임 추이


3.4 게시판 (Freeboard)
---------------------------------------------------------------------------

【게시글 CRUD】
- 기능: 자유게시판 글 작성, 조회, 수정, 삭제
- 구현:
  * 제목/내용 입력
  * 조회수 자동 증가
  * 작성자 본인만 수정/삭제 가능
  * 관리자 삭제 가능
- 엔드포인트:
  * POST /freeboard/write
  * GET /freeboard/{id}
  * PUT /freeboard/{id}/edit
  * DELETE /freeboard/{id}/delete

【댓글 시스템】
- 기능: 댓글 및 대댓글 작성
- 구현:
  * 댓글 작성/삭제
  * 대댓글 (parent_comment_id)
  * 실시간 댓글 업데이트
- 엔드포인트:
  * POST /freeboard/{id}/comment
  * DELETE /freeboard/comment/{commentId}

【검색 및 정렬】
- 기능: 게시글 검색 및 정렬
- 구현:
  * 제목/내용/작성자 검색
  * 최신순/조회수순 정렬
  * 페이지네이션


3.5 갤러리 (Gallery)
---------------------------------------------------------------------------

【이미지 업로드】
- 기능: 동아리 활동 사진 공유
- 구현:
  * 이미지 파일 업로드
  * 썸네일 자동 생성
  * 동아리별 갤러리 분류
- 엔드포인트: POST /gallery/upload

【갤러리 조회】
- 기능: 갤러리 목록 및 상세 조회
- 구현:
  * 그리드 레이아웃
  * 이미지 확대 보기 (모달)
  * 조회수 카운팅
- 엔드포인트:
  * GET /gallery/list
  * GET /gallery/{id}

【댓글 기능】
- 기능: 갤러리 댓글 작성
- 엔드포인트: POST /gallery/{id}/comment


3.6 실시간 채팅 (Chat)
---------------------------------------------------------------------------

【1:1 채팅】
- 기능: 사용자 간 실시간 메시지 전송
- 구현:
  * WebSocket 프로토콜
  * STOMP 메시징
  * 채팅방 자동 생성
- 엔드포인트: ws://localhost:8080/chat

【그룹 채팅】
- 기능: 동아리별 단체 채팅방
- 구현:
  * 동아리 생성 시 자동 채팅방 생성
  * 실시간 메시지 브로드캐스트
  * 읽음/안읽음 표시

【채팅 기록】
- 기능: 채팅 히스토리 저장 및 조회
- 구현:
  * 메시지 DB 저장
  * 페이지네이션된 히스토리 로드


3.7 AI 챗봇 (Chatbot)
---------------------------------------------------------------------------

【챗봇 대화】
- 기능: OpenAI GPT를 활용한 FAQ 자동 응답
- 구현:
  * Spring AI + OpenAI API 연동
  * 서비스 안내 및 질의응답
  * 컨텍스트 유지 대화
- 엔드포인트: POST /chatbot/ask

【학습 데이터】
- 기능: 챗봇 학습 및 개선
- 구현:
  * 사용자 질문 로깅
  * 자주 묻는 질문 분석


3.8 알림 시스템 (Alarm)
---------------------------------------------------------------------------

【알림 생성】
- 기능: 이벤트 발생 시 자동 알림
- 트리거:
  * 동아리 가입 승인/거절
  * 모임 참여 승인
  * 댓글/대댓글 작성
  * 친구 요청
  * 공지사항 등록
- 구현:
  * 비동기 알림 생성
  * 알림 유형별 분류

【알림 조회】
- 기능: 사용자별 알림 목록
- 구현:
  * 읽음/안읽음 상태 관리
  * 최신 알림 상단 고정
  * 실시간 알림 개수 표시 (Badge)
- 엔드포인트:
  * GET /alarm/list
  * POST /alarm/{id}/read


3.9 신고 관리 (Report)
---------------------------------------------------------------------------

【신고 접수】
- 기능: 부적절한 콘텐츠 신고
- 신고 대상:
  * 게시글 (POST)
  * 댓글 (COMMENT)
  * 사용자 (USER)
  * 동아리 (CLUB)
  * 갤러리 (GALLERY)
  * 모임 (GATHERING)
- 엔드포인트: POST /report/submit

【신고 처리】
- 기능: 관리자의 신고 검토 및 조치
- 구현:
  * 신고 상태 관리 (PROCESSING/COMPLETED/REJECTED)
  * 대상별 처리 방식:
    - USER: 계정 정지/차단
    - POST/COMMENT: 게시물 삭제
    - CLUB/GATHERING: 폐쇄/삭제
  * 신고 처리 시 자동 COMPLETED 상태 변경


3.10 관리자 대시보드 (Admin)
---------------------------------------------------------------------------

【통계 대시보드】
- 기능: 전체 시스템 현황 모니터링
- 통계 항목:
  * 월별 회원 가입 추이 (6개월)
  * 카테고리별 모임 분포 (Top 5)
  * 일일 활성 사용자 수
  * 신고 처리 현황 (처리중/완료/기각)
- 시각화: Chart.js 라인/바/도넛 차트

【회원 관리】
- 기능: 전체 회원 조회 및 관리
- 구현:
  * 회원 검색 (이름/이메일/아이디)
  * 회원 상태 변경 (활성/정지/탈퇴)
  * 휴대폰 번호 포맷팅 (010-1234-5678)
  * ID 기준 정렬 (오름차순/내림차순)
  * 페이지네이션 (10개씩)
- 엔드포인트: GET /api/admin/users

【게시글 관리】
- 기능: 전체 게시글 통합 관리
- 구현:
  * 자유게시판 게시글 목록
  * 제목/작성자 검색
  * 게시글 삭제 (관리자 권한)
  * ID 정렬 토글
- 엔드포인트:
  * GET /api/admin/posts
  * POST /api/admin/posts/delete

【동아리 관리】
- 기능: 동아리 승인 및 관리
- 구현:
  * 동아리 목록 조회
  * 동아리 강제 폐쇄
  * 카테고리별 필터
  * ID 정렬 기능
- 엔드포인트:
  * GET /api/admin/clubs
  * POST /api/admin/clubs/delete

【모임 관리】
- 기능: 모임 모니터링 및 관리
- 구현:
  * 진행 중/종료됨 통계
  * 상태별 필터 (OPEN/CLOSED)
  * 모임명 검색
  * ID 정렬 (기본: 오름차순)
  * 모임 삭제
- 통계:
  * 총 모임 수
  * 진행 중인 모임 (status = OPEN)
  * 종료된 모임 (status = CLOSED)
- 엔드포인트:
  * GET /api/admin/gatherings?sortOrder=asc
  * POST /api/admin/gatherings/delete

【갤러리 관리】
- 기능: 갤러리 콘텐츠 관리
- 구현:
  * 작성자 표시 (본명 + 아이디)
  * 통계 카드 (총 조회수, 오늘 등록된 갤러리)
  * 갤러리 삭제
  * ID 정렬
- 엔드포인트:
  * GET /api/admin/galleries
  * GET /api/admin/galleries/stats
  * POST /api/admin/galleries/delete

【신고 관리】
- 기능: 신고 내역 검토 및 처리
- 구현:
  * 신고 대상자 이름 표시
  * 신고 유형별 필터 (6가지)
  * 상태별 필터 (처리중/완료/기각)
  * 신고 처리 모달:
    - USER: 정지 기간 선택 + 차단
    - CLUB/GATHERING: 폐쇄/기각
    - POST/COMMENT/GALLERY: 삭제/기각
  * 처리 시 자동 COMPLETED 상태 변경
- 통계:
  * 전체 신고 건수
  * 처리중 건수
  * 완료 건수
  * 기각 건수
- 엔드포인트:
  * GET /api/admin/reports
  * POST /api/admin/reports/process
  * POST /api/admin/reports/reject

【공지사항 관리】
- 기능: 전체 공지사항 작성 및 관리
- 구현:
  * 공지 작성/수정/삭제
  * 상단 고정 기능
  * 공지 우선순위 설정
- 엔드포인트:
  * POST /api/admin/notices/create
  * PUT /api/admin/notices/update
  * POST /api/admin/notices/delete


3.11 마이페이지 (Mypage)
---------------------------------------------------------------------------

【프로필 관리】
- 기능: 개인정보 수정
- 구현:
  * 프로필 이미지 변경
  * 닉네임/전화번호/주소 수정
  * 비밀번호 변경
- 엔드포인트: PUT /mypage/profile

【활동 내역】
- 기능: 사용자 활동 조회
- 구현:
  * 작성한 게시글 목록
  * 작성한 댓글 목록
  * 가입한 동아리 목록
  * 참여한 모임 목록

【회원 탈퇴】
- 기능: 계정 삭제
- 구현:
  * 본인 확인 (비밀번호 재입력)
  * 탈퇴 사유 수집
  * 연관 데이터 처리


3.12 기타 기능
---------------------------------------------------------------------------

【날씨 정보】
- 기능: 실시간 날씨 표시
- 구현:
  * 외부 날씨 API 연동
  * 지역별 날씨 정보
- 엔드포인트: GET /weather/current

【친구 관리】
- 기능: 친구 추가/삭제
- 구현:
  * 친구 요청/승인/거절
  * 친구 목록 조회
  * 친구 차단

【서비스 소개】
- 기능: 서비스 안내 페이지
- 구현:
  * 서비스 소개
  * 이용 가이드
  * FAQ


================================================================================
4. API 명세
================================================================================

4.1 회원 관리 API
---------------------------------------------------------------------------
POST   /account/signup          회원가입
POST   /account/login           로그인
GET    /account/logout          로그아웃
POST   /account/reset-password  비밀번호 재설정
GET    /oauth2/authorization/google   구글 로그인
GET    /oauth2/authorization/naver    네이버 로그인
GET    /oauth2/authorization/kakao    카카오 로그인


4.2 동아리 API
---------------------------------------------------------------------------
GET    /club/list               동아리 목록
GET    /club/{id}               동아리 상세
POST   /club/create             동아리 생성
PUT    /club/{id}/edit          동아리 수정
DELETE /club/{id}/delete        동아리 삭제
POST   /club/{id}/join          동아리 가입
POST   /club/{id}/leave         동아리 탈퇴
POST   /club/{id}/kick          회원 강퇴


4.3 모임 API
---------------------------------------------------------------------------
GET    /recruitment/list        모임 목록
GET    /recruitment/{id}        모임 상세
POST   /recruitment/create      모임 생성
DELETE /recruitment/{id}/delete 모임 삭제
POST   /recruitment/{id}/join   모임 참여
POST   /recruitment/{id}/cancel 모임 취소
GET    /jobs/close-gatherings   자동 마감 실행 (스케줄러)


4.4 게시판 API
---------------------------------------------------------------------------
GET    /freeboard/list          게시글 목록
GET    /freeboard/{id}          게시글 상세
POST   /freeboard/write         게시글 작성
PUT    /freeboard/{id}/edit     게시글 수정
DELETE /freeboard/{id}/delete   게시글 삭제
POST   /freeboard/{id}/comment  댓글 작성
DELETE /freeboard/comment/{id}  댓글 삭제


4.5 갤러리 API
---------------------------------------------------------------------------
GET    /gallery/list            갤러리 목록
GET    /gallery/{id}            갤러리 상세
POST   /gallery/upload          갤러리 업로드
DELETE /gallery/{id}/delete     갤러리 삭제
POST   /gallery/{id}/comment    갤러리 댓글 작성


4.6 알림 API
---------------------------------------------------------------------------
GET    /alarm/list              알림 목록
POST   /alarm/{id}/read         알림 읽음 처리
GET    /alarm/unread-count      읽지 않은 알림 개수


4.7 신고 API
---------------------------------------------------------------------------
POST   /report/submit           신고 접수


4.8 챗봇 API
---------------------------------------------------------------------------
POST   /chatbot/ask             챗봇 질문


4.9 관리자 API
---------------------------------------------------------------------------
GET    /api/admin/dashboard/stats         대시보드 통계
GET    /api/admin/dashboard/monthly-signups  월별 가입자
GET    /api/admin/dashboard/gatherings-by-category  카테고리별 모임

GET    /api/admin/users                   회원 목록
POST   /api/admin/users/block             회원 정지

GET    /api/admin/posts                   게시글 목록
POST   /api/admin/posts/delete            게시글 삭제

GET    /api/admin/clubs                   동아리 목록
POST   /api/admin/clubs/delete            동아리 삭제

GET    /api/admin/gatherings              모임 목록
POST   /api/admin/gatherings/delete       모임 삭제

GET    /api/admin/galleries               갤러리 목록
GET    /api/admin/galleries/stats         갤러리 통계
POST   /api/admin/galleries/delete        갤러리 삭제

GET    /api/admin/reports                 신고 목록
GET    /api/admin/reports/rejectedCount   기각 건수
POST   /api/admin/reports/process         신고 처리
POST   /api/admin/reports/reject          신고 기각

POST   /api/admin/notices/create          공지 작성
PUT    /api/admin/notices/update          공지 수정
POST   /api/admin/notices/delete          공지 삭제


4.10 마이페이지 API
---------------------------------------------------------------------------
GET    /mypage                  마이페이지 조회
PUT    /mypage/profile          프로필 수정
GET    /mypage/posts            내가 쓴 글
GET    /mypage/clubs            가입 동아리
POST   /mypage/withdraw         회원 탈퇴


================================================================================
5. 주요 구현 기술
================================================================================

5.1 스케줄러 (Scheduler)
---------------------------------------------------------------------------

【모임 자동 마감】
- 기술: Spring @Scheduled
- 실행 주기: 매시간 정각 (Cron: 0 0 * * * *)
- 로직:
  1. 현재 시간 기준 meeting_date가 지난 모임 조회
  2. status를 'OPEN'에서 'CLOSED'로 일괄 변경
  3. 처리된 건수 로깅

클래스: RecruitmentScheduler.java
```java
@Scheduled(cron = "0 0 * * * *")
public void closeExpiredGatherings() {
    int count = recruitmentService.autoCloseExpiredGatherings();
    log.info("Auto-closed {} gatherings", count);
}
```

SQL: recruitmentMapper.xml
```sql
UPDATE recruitments
SET status = 'CLOSED'
WHERE status = 'OPEN'
  AND meeting_date IS NOT NULL
  AND meeting_date < NOW()
```


5.2 실시간 통신 (WebSocket)
---------------------------------------------------------------------------

【WebSocket 설정】
- 프로토콜: STOMP over WebSocket
- 엔드포인트: /chat
- 메시지 브로커: SimpleBroker

【메시지 흐름】
Client → /app/chat.sendMessage → MessageMapping
        → SimpleBroker → /topic/public
        → Subscribers


5.3 페이지네이션
---------------------------------------------------------------------------

【서버 사이드 페이징】
- 방식: MyBatis LIMIT/OFFSET
- 파라미터:
  * page: 현재 페이지 (1부터 시작)
  * size: 페이지당 개수 (기본 10)
  * sortOrder: 정렬 순서 (asc/desc)

【응답 구조】
```json
{
  "status": 1,
  "data": {
    "content": [...],
    "totalPages": 10,
    "totalElements": 95,
    "number": 1,
    "size": 10,
    "activeGatherings": 50,
    "endedGatherings": 45
  }
}
```


5.4 동적 쿼리 (MyBatis)
---------------------------------------------------------------------------

【조건부 쿼리】
```xml
<select id="selectGatheringsPaginated">
    SELECT * FROM recruitments
    <where>
        <if test="keyword != null and keyword != ''">
            AND title LIKE CONCAT('%', #{keyword}, '%')
        </if>
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
    </where>
    <if test="sortOrder != null and (sortOrder == 'asc' or sortOrder == 'desc')">
        ORDER BY id ${sortOrder}
    </if>
    <if test="sortOrder == null">
        ORDER BY id ASC
    </if>
    LIMIT #{limit} OFFSET #{offset}
</select>
```


5.5 보안 토큰
---------------------------------------------------------------------------

【CSRF 토큰】
- 모든 POST/PUT/DELETE 요청에 필수
- Thymeleaf에서 자동 주입
```html
<meta name="_csrf" content="${_csrf.token}"/>
<meta name="_csrf_header" content="${_csrf.headerName}"/>
```

JavaScript에서 사용:
```javascript
const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
headers[csrfHeader] = csrfToken;
```


5.6 파일 업로드
---------------------------------------------------------------------------

【이미지 업로드】
- 지원 형식: JPG, PNG, GIF
- 최대 크기: 10MB
- 저장 위치: /uploads/images/
- 파일명: UUID + 원본 확장자


5.7 데이터 검증
---------------------------------------------------------------------------

【Validation】
- @NotNull: 필수 값
- @NotBlank: 빈 문자열 불가
- @Email: 이메일 형식
- @Size: 길이 제한
- @Pattern: 정규식 검증


================================================================================
6. 성능 최적화
================================================================================

6.1 데이터베이스 최적화
---------------------------------------------------------------------------
- 인덱스: PK, FK, 검색 컬럼 (username, email)
- 연결 풀: HikariCP (기본 10개)
- 쿼리 최적화: JOIN 최소화, 서브쿼리 개선


6.2 캐싱
---------------------------------------------------------------------------
- 정적 리소스: Spring Boot Static Resource Caching
- 세션: Server Session (In-Memory)


6.3 비동기 처리
---------------------------------------------------------------------------
- 알림 생성: @Async
- 이메일 발송: @Async


================================================================================
7. 테스트 전략
================================================================================

7.1 단위 테스트
---------------------------------------------------------------------------
- JUnit 5
- Service Layer 테스트
- Mapper 테스트 (MyBatis Test)


7.2 통합 테스트
---------------------------------------------------------------------------
- Spring Boot Test
- MockMvc를 통한 Controller 테스트
- Security Test


================================================================================
8. 배포 및 운영
================================================================================

8.1 빌드
---------------------------------------------------------------------------
- 빌드 도구: Gradle
- 빌드 명령: ./gradlew build
- 산출물: JAR (Spring Boot Executable)


8.2 실행
---------------------------------------------------------------------------
- 실행 명령: java -jar neighbus.jar
- 포트: 8080 (기본)
- 프로필: dev, prod


8.3 모니터링
---------------------------------------------------------------------------
- 로깅: Logback
- 로그 레벨: INFO (prod), DEBUG (dev)


================================================================================
9. 향후 개선 계획
================================================================================

9.1 기능 확장
---------------------------------------------------------------------------
- 모바일 앱 개발 (React Native / Flutter)
- 결제 시스템 연동 (유료 동아리)
- 동아리 일정 관리 (캘린더)
- 출석 체크 기능
- 포인트/리워드 시스템


9.2 기술 개선
---------------------------------------------------------------------------
- Redis 캐싱 도입
- Elasticsearch 검색 엔진
- AWS S3 파일 저장소
- CDN 적용
- Docker 컨테이너화
- CI/CD 파이프라인 구축


9.3 성능 개선
---------------------------------------------------------------------------
- 쿼리 최적화 및 인덱스 튜닝
- 이미지 압축 및 최적화
- Lazy Loading 적용
- API 응답 캐싱


================================================================================
10. 팀 구성 및 역할
================================================================================

본 프로젝트는 4명의 팀원이 협업하여 개발한 포트폴리오 프로젝트입니다.

【역할 분담 예시】
- 팀원 1: 회원 관리, 인증/인가, 보안
- 팀원 2: 동아리 관리, 모임 관리, 스케줄러
- 팀원 3: 게시판, 갤러리, 댓글 시스템
- 팀원 4: 관리자 페이지, 신고 처리, 통계 대시보드

【공통 작업】
- 데이터베이스 설계
- API 명세 작성
- 코드 리뷰
- 통합 테스트


================================================================================
11. 라이센스 및 저작권
================================================================================

본 프로젝트는 교육 및 포트폴리오 목적으로 개발되었습니다.
모든 소스 코드는 MIT License를 따릅니다.

Copyright (c) 2024 NEIGHBUS Team


================================================================================
12. 참고 자료
================================================================================

【공식 문서】
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- MyBatis: https://mybatis.org/mybatis-3/
- Thymeleaf: https://www.thymeleaf.org/

【사용 API】
- OpenAI API: https://platform.openai.com/docs
- Nurigo SMS API: https://www.coolsms.co.kr/

【개발 도구】
- IntelliJ IDEA
- MySQL Workbench
- Postman (API 테스트)
- Git/GitHub (버전 관리)


================================================================================
문서 끝
================================================================================

작성일: 2024년 12월
버전: 1.0
작성자: NEIGHBUS 개발팀
