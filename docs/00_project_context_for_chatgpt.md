# 00. ChatGPT 프로젝트 컨텍스트

## 1. 문서 목적

이 문서는 ChatGPT 새 채팅 또는 기능별 작업 시작 시 업로드할 핵심 요약 문서다.

기능별 상세 요구사항 문서를 모두 ChatGPT 프로젝트 소스에 업로드하지 못하는 상황을 대비해, 프로젝트의 최신 기준과 작업 규칙을 한 곳에 요약한다.

이 문서는 상세 요구사항 문서를 대체하지 않는다.

상세 요구사항 문서는 GitHub 프로젝트의 `docs/` 디렉터리에 기능별로 계속 유지하고, 이 문서는 ChatGPT가 작업을 빠르게 이어갈 수 있게 하는 업로드용 요약 문서로 사용한다.

상세 요구사항 md 참고가 필요하면 아래 GitHub docs 경로를 우선 기준으로 확인한다.

```text
https://github.com/an-sehyeon/soccer-team-management-platform/tree/main/docs
```

---

## 2. 문서 보관 기준

문서 보관 위치는 다음처럼 구분한다.

```text
GitHub 프로젝트 소스 docs/
→ 기능별 상세 md 문서를 전부 보관한다.
→ 요구사항 변경 이력, 커밋, PR 리뷰를 위해 삭제하지 않는다.

ChatGPT 프로젝트 소스 업로드
→ 모든 상세 md를 올리지 않는다.
→ 핵심 요약 파일 5개만 기본으로 유지한다.
→ 현재 작업에 꼭 필요한 상세 md나 코드 파일만 추가로 올린다.
```

ChatGPT 프로젝트 소스에 기본으로 유지할 파일은 다음 5개다.

```text
00_project_context_for_chatgpt.md
00_current_backend_policy.md
00_current_frontend_policy.md
soccer_platform.sql
README.md
```

기능별 상세 md는 GitHub에 남아 있으므로 삭제되는 것이 아니라, ChatGPT 프로젝트 업로드 대상에서만 제외되는 것이다.

코드 구현에 필요한 실제 Controller, DTO, Entity, Repository, Service, 프론트 타입/API/페이지 파일은 GitHub 문서만으로 추측하지 않고 사용자에게 업로드를 요청하거나 최신 코드 기준으로 확인한다.

현재 사용자의 로컬 파일과 GitHub `main` 브랜치가 일치한다고 사용자가 명확히 말한 경우, 필요한 실제 코드 구조와 파일 내용은 GitHub `main`을 기준으로 확인하고 진행한다.

모든 md 파일을 작성해서 사용자에게 제공할 때는 사용자가 바로 복사해 붙여넣을 수 있도록 마크다운 코드블록 형태로 전체 내용을 제공한다.

---

## 3. 기본 원칙

이 프로젝트는 포트폴리오용 예제가 아니라 실제 축구팀에 판매하고 운영할 목적의 플랫폼이다.

모든 설계와 구현은 다음 기준을 따른다.

* 실제 서비스 운영 가능성
* 권한과 보안
* 유지보수성
* 영상 저장 비용과 서버 부하
* 지도자/분석관의 PC 편집 사용성
* 선수의 모바일 조회 사용성
* 추후 AI 분석 확장 가능성

답변과 작업은 항상 결론부터 시작한다.

기본 답변 순서는 다음을 따른다.

1. 결론
2. 이유
3. 적용 방법
4. 필요하면 코드 또는 예시
5. 주의사항

---

## 4. ChatGPT 업로드 운영 방식

새 채팅 또는 기능 작업 시작 시 기본 업로드 파일은 다음 5개를 우선 사용한다.

```text
00_project_context_for_chatgpt.md
00_current_backend_policy.md
00_current_frontend_policy.md
soccer_platform.sql
README.md
```

기능별 작업을 시작할 때는 위 5개에 더해서 현재 작업에 꼭 필요한 상세 요구사항 문서와 실제 코드 파일만 추가로 업로드한다.

상세 요구사항 md 파일이 ChatGPT 프로젝트 소스에 업로드되어 있지 않은 경우에는 아래 GitHub docs 경로를 참고한다.

```text
https://github.com/an-sehyeon/soccer-team-management-platform/tree/main/docs
```

새 채팅에서 작업을 시작할 때 필요한 문서나 코드 파일이 불명확하면, ChatGPT는 바로 구현하지 말고 먼저 사용자에게 필요한 파일 목록을 요청해야 한다.

ChatGPT는 DTO 필드명, API 주소, Entity 컬럼명, Repository 메서드명을 추측해서 구현하지 않는다.

단, 사용자가 현재 로컬 파일과 GitHub `main`이 동일하다고 명확히 말한 경우에는 GitHub `main`을 기준으로 실제 코드 구조를 확인한 뒤 작업할 수 있다.

---

## 5. 기능 완료 후 자동 갱신 규칙

기능 작업이 끝나면 사용자가 따로 요청하지 않아도 마지막 정리 단계에서 다음을 제공한다.

1. 변경 내용이 반영된 최종 요구사항 md 문서
2. 생성/수정 파일 기준 기능별 커밋 메시지
3. GitHub PR 제목과 내용
4. 다음 작업 GitHub 이슈 제목과 10줄 이내 내용
5. 다음 기능을 새 채팅에서 시작할 수 있는 프롬프트
6. 다음 작업 기준으로 갱신된 `00_project_context_for_chatgpt.md` 내용
7. 필요하면 `00_current_backend_policy.md`, `00_current_frontend_policy.md` 갱신 내용
8. DB 테이블이 변경된 경우 `soccer_platform.sql` 갱신 내용

다음 내용이 바뀌면 반드시 요약 문서에 반영한다.

* 현재까지 완료된 기능
* 현재 중단된 기능
* 다음 작업명
* 새로 확정된 백엔드 정책
* 새로 확정된 프론트 정책
* API 주소 변경
* DB 테이블/컬럼 변경
* 영상 저장 정책 변경
* 반복 발생한 에러와 예방 규칙
* 다음 채팅에서 반드시 요청해야 할 문서 또는 코드 파일 목록

---

## 6. 다음 채팅 프롬프트 작성 규칙

기능 완료 후 다음 작업용 프롬프트를 작성할 때는 반드시 아래 내용을 포함한다.

* 시작할 기능명
* 참고할 기본 업로드 파일 5개
* 현재까지 완료된 기능
* 현재 중단된 기능
* 사용할 DB 테이블
* 초기 구현 방식
* 권한 정책
* 삭제 정책
* 프론트 구현 여부
* 관련 상세 요구사항 md 파일명
* 다음 채팅에서 ChatGPT가 먼저 확인하거나 요청해야 할 문서/코드 파일 목록

다음 채팅 프롬프트에는 반드시 아래 문장을 포함한다.

```text
코드 작성 전, 현재 작업에 필요한 상세 요구사항 md와 실제 백엔드/프론트 코드 파일이 충분히 제공되었는지 먼저 확인해줘. 부족한 파일이 있으면 바로 구현하지 말고 필요한 파일 목록을 먼저 요청해줘. DTO 필드명, API 주소, Entity 컬럼명, Repository 메서드명은 추측하지 말고 실제 파일 기준으로 맞춰줘.
```

---

## 7. 현재 기술 스택

### Backend

* Java
* Spring Boot
* Gradle
* Spring Security
* JPA
* MySQL
* IntelliJ IDEA
* JWT 인증
* 로컬 파일 저장소
* ffprobe 기반 영상 길이 추출
* FFmpeg 기반 클립 파일 생성
* Spring `@Async` 기반 비동기 파일 생성

### Frontend

* React
* TypeScript
* Vite
* Axios
* VS Code
* 반응형 UI

### 영상 처리

* 경기 원본 영상 업로드는 실제 `.mp4` 파일 업로드 방식
* 로컬 개발 기준 업로드 경로는 `backend/uploads` 하위 경로 사용
* 경기 영상 길이 추출은 `ffprobe` 기반
* 팀 분석 클립 파일 생성은 `FFmpeg` 기반
* 선수 개인 분석 클립 파일 생성은 `FFmpeg` 기반
* 초기 MVP에서는 `/uploads/**` 직접 접근 구조를 사용
* 운영 전에는 권한 검증 스트리밍 API 또는 Signed URL 방식으로 전환 필요

---

## 8. 사용자 역할

### COACH

지도자는 팀 운영과 영상 분석을 관리한다.

주요 권한은 다음과 같다.

* 스케줄 등록/수정/삭제/조회
* 공지사항 등록/수정/삭제/조회
* 경기 영상 업로드/조회/수정/삭제
* 팀 분석 클립 등록/조회/수정/삭제
* 선수 개인 분석 클립 등록/조회/수정/삭제
* 드로잉 등록/조회/수정/삭제
* 선수 개인 분석 클립 조회 기록 확인
* 선수 기록 조회/관리
* 선수 기록 이벤트 등록/조회/수정/삭제
* 선수 기록 이벤트와 분석 클립 연결 관리

### ANALYST

분석관은 영상 분석 실무를 담당한다.

주요 권한은 다음과 같다.

* 스케줄/공지사항 조회
* 경기 영상 업로드/조회/수정
* 팀 분석 클립 등록/조회/수정
* 선수 개인 분석 클립 등록/조회/수정
* 드로잉 등록/조회/수정
* 선수 개인 분석 클립 조회 기록 확인
* 기본적으로 삭제 권한 없음

단, 선수 기록 관리 기능에서는 기존 최종 정책상 분석관도 기록 삭제가 가능하다.

분석관은 선수 기록 이벤트 등록/조회/수정/삭제와 선수 기록 이벤트-클립 연결도 가능하다.

### PLAYER

선수는 조회 중심 사용자다.

주요 권한은 다음과 같다.

* 스케줄 조회
* 공지사항 조회
* 경기 원본 영상 조회
* READY 상태 팀 분석 클립 조회
* 본인 선수 개인 분석 클립 조회
* 본인 선수 개인 분석 클립 드로잉 조회
* 본인 선수 개인 분석 클립 상세 조회 시 조회 기록 자동 생성/갱신
* 본인 기록 조회
* 본인 선수 기록 이벤트 조회

---

## 9. 현재까지 완료된 기능

현재까지 구현 또는 연동이 완료된 기능은 다음과 같다.

* 회원가입/로그인/JWT 인증
* 역할 기반 접근 제어
* 관리자 회원 승인
* 스케줄 백엔드 구현
* 스케줄 프론트 연동
* 공지사항 백엔드 구현
* 공지사항 프론트 연동
* 경기 영상 업로드 백엔드 구현
* 경기 영상 길이 `ffprobe` 추출
* 경기 영상 프론트 연동
* 팀 분석 클립 백엔드 구현
* 팀 분석 클립 프론트 연동
* 팀 분석 클립 드로잉 백엔드 구현
* 팀 분석 클립 드로잉 프론트 연동
* 팀 분석 클립 실제 mp4 비동기 파일 생성 구조 구현
* 팀 분석 클립 + 드로잉 통합 생성 API 구현
* 팀 분석 클립 + 드로잉 통합 수정 API 구현
* 팀 분석 클립 목록/상세 페이지와 등록/수정 편집기 페이지 분리
* 팀 분석 클립 드로잉 시간 기준을 생성된 팀 분석 클립 영상 기준으로 정리
* 팀 분석 클립 PUT 수정 요청 CORS 문제 해결
* 선수 개인 분석 클립 백엔드 구현
* 선수 개인 분석 클립 조회 기록 백엔드 구현
* 선수 개인 분석 클립 드로잉 백엔드 구현
* 선수 목록 조회 API 백엔드 구현
* 선수 개인 분석 클립 프론트 연동
* 선수 개인 분석 클립 비동기 파일 생성 구조 구현
* 선수 개인 분석 클립 + 드로잉 통합 수정 API 구현
* 선수 개인 분석 클립 목록/상세 페이지와 등록/수정 편집기 페이지 분리
* 선수 개인 분석 클립 상세 재생을 `playerClipUrl` 기준으로 정리
* 선수 개인 분석 클립 드로잉 시간 기준을 생성된 선수 개인 분석 클립 영상 기준으로 정리
* 선수 개인 분석 클립 편집기 프론트 구조 정리
* 선수 개인 분석 클립 조회 기록 Controller 추가
* 선수 개인 분석 클립 조회 기록 프론트 연동 완료
* 선수 기록 관리 백엔드 기본 CRUD 구현
* 선수 기록 이벤트 백엔드 구현
* 선수 기록 이벤트와 팀 분석 클립 연결 API 구현
* 선수 기록 이벤트와 선수 개인 분석 클립 연결 API 구현
* 선수 기록 이벤트 생성/수정/삭제 시 player_record 요약 수치 자동 보정 구현
* 선수 기록 이벤트 시간 구간을 `eventStartTimeSec`, `eventEndTimeSec` 기준으로 정리
* 선수 기록 이벤트 시간이 원본 경기 영상 `durationSec`을 초과하지 않도록 검증 구현

---

## 10. 현재 중단된 기능

현재 별도로 중단된 기능은 없다.

다음 작업으로 선수 기록 이벤트-클립 연결 API의 프론트 연동을 진행하는 것이 적절하다.

---

## 11. 최근 완료된 작업

최근 완료된 작업명은 다음과 같다.

```text
경기 영상 기반 선수 기록 이벤트와 클립 연결 API 백엔드 구현
```

완료된 핵심 정책은 다음과 같다.

* 기존 `player_record`는 경기별 선수 요약 기록으로 유지한다.
* 같은 `upload_id`, `player_id` 기준 활성 `player_record`는 1개만 유지한다.
* 기록 이벤트 등록 시 기존 `player_record`가 있으면 새로 만들지 않고 재사용한다.
* 기존 `player_record`가 없으면 기본값 0으로 자동 생성한다.
* `player_record_event`는 기록 이벤트가 등록될 때마다 새로 저장한다.
* `player_record_event`에는 `upload_id`, `player_id`를 직접 저장하지 않는다.
* `upload_id`, `player_id`는 `player_record_id`를 통해 `player_record`에서 확인한다.
* 초기 `eventTimeSec` 단일 시점 구조는 폐기했다.
* 이벤트 시간은 `eventStartTimeSec`, `eventEndTimeSec` 구간으로 저장한다.
* 이벤트 시간 구간은 원본 경기 영상 기준 초다.
* 이벤트 종료 시간은 `game_video_upload.duration_sec`을 초과할 수 없다.
* `player_record_event.memo`는 이벤트 단위 메모이며, `player_record.memo`와 별도로 유지한다.
* DTO에서는 이벤트 단위 메모를 `eventMemo`로 표현한다.
* `player_record_event_clip`은 선택 연결 테이블이다.
* 기록 이벤트는 클립 없이도 존재할 수 있다.
* 팀 분석 클립과 선수 개인 분석 클립 모두 기록 이벤트와 연결할 수 있다.
* 선수 개인 분석 클립 연결 시 기록 대상 선수와 클립 대상 선수가 일치해야 한다.
* 팀 분석 클립 연결 시 기록 대상 선수를 별도로 선택한다.
* 이벤트 생성/수정/삭제 시 `player_record` 요약 수치를 갱신 또는 보정한다.
* 삭제는 소프트 삭제를 사용한다.
* 권한 검증은 백엔드에서 처리한다.

완료된 주요 API는 다음과 같다.

```http
POST /api/management/player-record-events
POST /api/management/player-record-events/with-clip-link
GET /api/management/player-records/{recordId}/events
GET /api/management/player-record-events/{eventId}
PATCH /api/management/player-record-events/{eventId}
DELETE /api/management/player-record-events/{eventId}
GET /api/player/me/player-records/{recordId}/events
GET /api/player/me/player-record-events/{eventId}
```

완료된 주요 테이블은 다음과 같다.

```text
player_record_event
player_record_event_clip
```

---

## 12. 팀 분석 클립 파일 생성 정책

팀 분석 클립은 원본 영상 URL과 시간 메타데이터로 구간 재생하지 않고 실제 mp4 파일 생성 구조를 사용한다.

상세 재생 기준은 다음 필드다.

```text
teamClipUrl
```

`teamClipUrl`은 DB의 `team_video_clip.url`에서 내려온다.

`team_video_clip.start_time_sec`, `team_video_clip.end_time_sec`는 DB와 Entity에 유지한다.

이 값은 원본 경기 영상에서 어떤 구간을 잘라냈는지 기록하는 생성 이력, 재생성, 디버깅용 메타데이터다.

프론트 목록/상세 화면에서는 READY 상태 클립만 기본 조회한다.

---

## 13. 팀 분석 클립 상태 정책

팀 분석 클립 파일 생성 구조에서는 다음 상태를 사용한다.

| 상태 | 의미 |
| --- | --- |
| `PROCESSING` | 클립 파일 생성 중 |
| `READY` | 클립 파일 생성 완료, 재생 가능 |
| `FAILED` | 클립 파일 생성 실패 |

프론트에서는 다음 기준으로 처리한다.

```text
PROCESSING
→ 일반 목록에서 미노출
→ 필요 시 별도 실패/처리 관리 화면에서 표시

FAILED
→ 일반 목록에서 미노출
→ 필요 시 별도 실패/처리 관리 화면에서 표시

READY
→ teamClipUrl 기준으로 영상 재생
→ 드로잉 오버레이 렌더링 가능
```

---

## 14. 팀 분석 클립 드로잉 정책

팀 분석 클립 드로잉은 영상 파일에 합성하지 않는다.

드로잉은 `team_video_clip_drawing` 테이블에 JSON 문자열로 저장한다.

드로잉 시간 기준은 생성된 클립 영상 기준 초다.

예시는 다음과 같다.

```text
원본 경기 영상 100초 ~ 115초를 잘라 15초 팀 분석 클립 생성
드로잉은 2초 ~ 6초처럼 클립 내부 시간 기준으로 저장
```

검증 기준은 다음과 같다.

```text
drawingStartTimeSec >= 0
drawingEndTimeSec <= clipDurationSec
drawingStartTimeSec < drawingEndTimeSec
```

`clipDurationSec`는 다음으로 계산한다.

```text
team_video_clip.end_time_sec - team_video_clip.start_time_sec
```

---

## 15. 선수 개인 분석 클립 파일 생성 정책

선수 개인 분석 클립은 실제 mp4 파일 비동기 생성 구조를 사용한다.

상세 재생 기준은 다음 필드다.

```text
playerClipUrl
```

`playerClipUrl`은 DB의 `player_video_clip.url`에서 내려온다.

`player_video_clip.start_time_sec`, `player_video_clip.end_time_sec`는 DB와 Entity에 유지한다.

이 값은 원본 경기 영상에서 어떤 구간을 잘라냈는지 기록하는 생성 이력, 재생성, 디버깅용 메타데이터다.

드로잉은 영상 파일에 합성하지 않고 `player_video_clip_drawing`에 JSON으로 저장한다.

드로잉 시간 기준은 생성된 선수 개인 분석 클립 영상 기준 초다.

선수 개인 분석 클립은 목록/상세 페이지와 등록/수정 편집기 페이지가 분리되어 있다.

경로는 다음과 같다.

```text
/player-analysis-clips
/player-analysis-clips/new
/player-analysis-clips/:playerClipId/edit
```

등록 페이지에서는 원본 경기 영상과 대상 선수를 선택하기 전까지 편집 영역을 숨긴다.

수정 페이지에서는 기존 클립 정보와 드로잉을 유지한 상태로 전체 편집 영역을 바로 표시한다.

선수 개인 분석 클립 + 드로잉 통합 수정 API는 다음을 사용한다.

```http
PUT /api/management/player-analysis-clips/{playerClipId}/with-drawings
```

통합 수정 시 드로잉은 전체 교체 방식으로 처리한다.

제목/코멘트/드로잉만 변경하면 기존 mp4 파일을 유지한다.

클립 시작/종료 시간이 변경되면 mp4 파일을 재생성한다.

---

## 16. 선수 개인 분석 클립 조회 기록 정책

선수 개인 분석 클립 조회 기록은 `player_video_clip_view` 테이블을 사용한다.

조회 기록은 선수가 본인 개인 분석 클립 상세를 조회할 때만 생성 또는 갱신된다.

지도자나 분석관이 관리용 상세 화면에서 개인 분석 클립을 조회해도 조회 기록은 증가하지 않는다.

조회 기록 기준은 다음과 같다.

```text
COACH 관리용 상세 조회
→ 조회 기록 증가 없음

ANALYST 관리용 상세 조회
→ 조회 기록 증가 없음

해당 클립의 대상 PLAYER가 본인 상세 조회
→ 최초 조회 시 row 생성
→ 재조회 시 lastViewedAt 갱신 및 viewCount 증가
```

프론트에서는 선수 개인 분석 클립 상세 화면에 COACH, ANALYST 전용 조회 기록 영역을 표시한다.

표시 정보는 다음과 같다.

* 조회 선수
* 최초 확인 시간
* 마지막 확인 시간
* 대상 선수 조회 횟수

PLAYER에게는 조회 기록 관리 UI를 표시하지 않는다.

조회 기록 삭제 기능은 현재 구현하지 않는다.

---

## 17. 선수 기록 이벤트 정책

선수 기록 이벤트는 `player_record_event` 테이블을 사용한다.

선수 기록 이벤트는 특정 선수의 경기 요약 기록인 `player_record`에 연결된다.

이벤트 시간은 원본 경기 영상 기준 구간으로 관리한다.

```text
eventStartTimeSec
eventEndTimeSec
```

검증 기준은 다음과 같다.

```text
eventStartTimeSec >= 0
eventEndTimeSec > eventStartTimeSec
eventEndTimeSec <= game_video_upload.duration_sec
```

`player_record_event`에는 `upload_id`, `player_id`를 직접 저장하지 않는다.

경기 영상과 선수 정보는 `player_record_id`를 통해 확인한다.

선수 기록 이벤트는 클립 없이 존재할 수 있다.

선수 기록 이벤트에 클립을 연결하는 경우 `player_record_event_clip` 테이블을 사용한다.

연결 가능한 클립 유형은 다음과 같다.

```text
TEAM_ANALYSIS
PLAYER_ANALYSIS
```

팀 분석 클립 연결 시 `player_record.upload_id`와 `team_video_clip.upload_id`가 일치해야 한다.

선수 개인 분석 클립 연결 시 다음 조건을 모두 만족해야 한다.

```text
player_record.upload_id = player_video_clip.upload_id
player_record.player_id = player_video_clip.player_id
```

이벤트 생성/수정/삭제 시 `player_record` 요약 수치를 자동 보정한다.

---

## 18. 파일 저장 정책

로컬 개발 기준 클립 파일 저장 경로는 다음과 같다.

```text
backend/uploads/match-videos
backend/uploads/team-analysis-clips
backend/uploads/player-analysis-clips
```

DB의 클립 `url`에는 브라우저 접근용 URL을 저장한다.

예시는 다음과 같다.

```text
/uploads/match-videos/{storedFileName}
/uploads/team-analysis-clips/team-clip-{id}-{uuid}.mp4
/uploads/player-analysis-clips/player-clip-{id}-{uuid}.mp4
```

운영 전에는 `/uploads/**` 직접 접근 구조를 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.

---

## 19. CORS 정책

프론트 개발 서버는 다음 Origin을 사용한다.

```text
http://localhost:5173
```

팀 분석 클립 통합 수정 API와 선수 개인 분석 클립 통합 수정 API는 PUT 요청을 사용한다.

브라우저는 PUT 요청 전에 preflight OPTIONS 요청을 보낸다.

백엔드 CORS 정책은 다음을 허용해야 한다.

```text
allowedOrigins:
- http://localhost:5173

allowedMethods:
- GET
- POST
- PUT
- PATCH
- DELETE
- OPTIONS

allowedHeaders:
- Authorization
- Content-Type

exposedHeaders:
- Authorization
```

SecurityConfig에서는 다음을 허용한다.

```text
OPTIONS /** permitAll
```

실제 관리 API 요청은 JWT 인증을 유지한다.

---

## 20. 다음 작업

다음 작업명은 다음과 같다.

```text
선수 기록 이벤트-클립 연결 프론트 연동
```

다음 작업의 핵심 방향은 다음과 같다.

* 선수 기록 관리 프론트 화면에 선수 기록 이벤트 기능을 연결한다.
* 지도자/분석관이 선수 기록 상세에서 이벤트를 등록, 조회, 수정, 삭제할 수 있게 한다.
* 이벤트 등록 시 원본 경기 영상의 현재 재생 구간을 `eventStartTimeSec`, `eventEndTimeSec`로 저장할 수 있게 한다.
* 이벤트를 팀 분석 클립 또는 선수 개인 분석 클립과 선택적으로 연결할 수 있게 한다.
* 선수는 본인 기록 이벤트만 조회할 수 있게 한다.
* 프론트 타입은 백엔드 DTO 필드명과 정확히 맞춘다.
* API 주소는 백엔드 Controller 기준으로 작성한다.
* 권한 검증은 반드시 백엔드에서 처리하고, 프론트는 사용자 경험을 위해 버튼과 영역만 숨긴다.

다음 작업에서 우선 요청하거나 확인해야 할 파일은 다음과 같다.

```text
frontend/src/App.tsx
frontend/src/constants/routes.ts
frontend/src/pages/DashboardHomePage.tsx
frontend/src/pages/MobileHomePage.tsx
frontend/src/pages/PlayerHomePage.tsx
frontend/src/pages/PlayerRecordPage.tsx 또는 선수 기록 페이지 파일
frontend/src/types/playerRecord.ts 또는 선수 기록 타입 파일
frontend/src/api/playerRecordApi.ts 또는 선수 기록 API 파일

backend/src/main/java/com/soccer/platform/controller/PlayerRecordEventController.java
backend/src/main/java/com/soccer/platform/dto/playerrecordevent/CreatePlayerRecordEventRequestDTO.java
backend/src/main/java/com/soccer/platform/dto/playerrecordevent/CreatePlayerRecordEventWithClipLinkRequestDTO.java
backend/src/main/java/com/soccer/platform/dto/playerrecordevent/UpdatePlayerRecordEventRequestDTO.java
backend/src/main/java/com/soccer/platform/dto/playerrecordevent/CreatePlayerRecordEventResponseDTO.java
backend/src/main/java/com/soccer/platform/dto/playerrecordevent/PlayerRecordEventResponseDTO.java
backend/src/main/java/com/soccer/platform/dto/playerrecordevent/PlayerRecordEventClipResponseDTO.java
backend/src/main/java/com/soccer/platform/dto/playerrecordevent/PlayerRecordEventListResponseDTO.java
```

---

## 21. 기능 개발 규칙

새 기능을 시작할 때는 바로 코드부터 작성하지 않는다.

순서는 다음을 따른다.

1. 기본 업로드 파일 5개 확인
2. 관련 상세 요구사항 md 문서 확인
3. 현재 작업에 필요한 코드 파일이 충분한지 확인
4. 부족한 문서나 코드 파일 요청
5. Controller, DTO, Entity, Repository, Service 실제 코드 확인
6. 요구사항 문서 작성 또는 갱신
7. GitHub 이슈 제목과 내용 작성
8. 구현 순서 제안
9. 사용자가 진행하면 코드 구현

DTO 필드명, API 주소, Entity 컬럼명, Repository 메서드명은 임의로 확정하지 않는다.

확실하지 않으면 사용자에게 실제 파일을 요청하거나 GitHub 최신 코드 기준으로 확인한다.

사용자가 현재 로컬 파일과 GitHub `main`이 동일하다고 명확히 말한 경우에는 GitHub `main` 기준으로 실제 코드 구조를 확인한 뒤 진행한다.

---

## 22. 코드 제공 규칙

백엔드 코드는 가독성을 우선한다.

클래스와 메서드 이름은 역할이 드러나게 작성한다.

프론트 작업 중 에러가 나거나 수정이 필요한 경우에는 수정된 파일의 전체 코드를 제공한다.

프론트 파일 수정 시 다음을 지킨다.

* 사용하지 않는 변수 생성 금지
* React `useEffect` 안에서 동기적인 `setState` 반복 금지
* effect 내부 async 함수와 `ignore` 또는 `isMounted` 플래그 사용
* API 응답 구조는 백엔드 DTO 확인 후 반영
* 에러 메시지는 사용자에게 필요한 message 중심으로 표시
* NaN이 사용자 화면에 표시되지 않도록 숫자 값을 안전하게 처리

---

## 23. 상세 문서 보관 기준

상세 요구사항 문서는 기능별로 계속 분리해서 GitHub `docs/`에 유지한다.

단, ChatGPT 프로젝트 소스에는 모든 상세 문서를 올리지 않는다.

ChatGPT 프로젝트 소스에는 기본 5개 파일만 상시 유지하고, 기능별 상세 문서는 현재 작업에 필요한 것만 임시로 업로드한다.

ChatGPT 프로젝트 업로드 한도에 도달하면 다음 순서로 정리한다.

1. 오래된 기능별 상세 md를 ChatGPT 프로젝트 소스에서 제거한다.
2. 현재 작업과 무관한 코드 파일을 ChatGPT 프로젝트 소스에서 제거한다.
3. 기본 5개 파일은 유지한다.
4. 현재 작업에 필요한 상세 md와 코드 파일만 추가한다.

모든 md를 하나로 합치지 않는다.

GitHub에는 기능별 상세 md를 계속 유지하고, ChatGPT에는 요약 문서와 현재 작업에 필요한 문서만 올린다.