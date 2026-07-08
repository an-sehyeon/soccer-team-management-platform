# 00. 현재 백엔드 정책

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 백엔드 최신 정책을 요약한다.

새 채팅에서 백엔드 기능을 이어서 구현할 때 이 문서를 우선 참고한다.

상세 내용은 기능별 요구사항 md 문서와 실제 소스 코드를 기준으로 확인한다.

---

## 2. 백엔드 기술 스택

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

---

## 3. 인증 정책

* 회원가입/로그인 기능은 구현되어 있다.
* 로그인 성공 시 JWT Access Token을 발급한다.
* 인증 후 `CustomUserPrincipal`에서 다음 값을 사용한다.

```text
memberId
memberRole
isAdmin
```

* 모든 주요 API는 인증된 사용자만 접근할 수 있다.
* 회원가입과 로그인 API만 `permitAll` 대상이다.
* 권한 검증은 프론트가 아니라 반드시 백엔드에서 처리한다.

---

## 4. 역할 정책

### COACH

지도자는 팀 운영과 분석 기능의 주 관리자다.

기본적으로 등록, 조회, 수정, 삭제 권한을 가진다.

선수 개인 분석 클립 조회 기록도 확인할 수 있다.

선수 기록과 선수 기록 이벤트도 관리할 수 있다.

### ANALYST

분석관은 영상 분석 실무자다.

기본적으로 등록, 조회, 수정 권한을 가진다.

삭제 권한은 기본적으로 없다.

선수 개인 분석 클립 조회 기록도 확인할 수 있다.

단, 선수 기록 기능에서는 기존 최종 정책상 분석관도 삭제 가능하다.

선수 기록 이벤트 기능에서도 분석관은 등록, 조회, 수정, 삭제가 가능하다.

### PLAYER

선수는 조회 중심 사용자다.

본인 데이터만 조회할 수 있는 기능에서는 반드시 로그인한 `memberId` 기준으로 접근 범위를 제한한다.

선수가 본인 개인 분석 클립 상세를 조회하면 조회 기록이 자동 생성 또는 갱신된다.

선수는 본인 선수 기록과 본인 선수 기록 이벤트만 조회할 수 있다.

---

## 5. 권한 검증 원칙

권한 검증은 Service 또는 Validator 계층에서 처리한다.

프론트에서 버튼을 숨겨도 API 직접 호출이 가능하므로 백엔드 검증은 반드시 유지한다.

공통 기준은 다음과 같다.

```text
COACH: 관리 권한
ANALYST: 관리 보조 권한, 선수 기록 기능에서는 삭제 가능
PLAYER: 조회 권한, 본인 데이터 제한
```

`isAdmin = true`만으로 영상/스케줄/공지/클립/선수 기록 관리 권한을 주지 않는다.

`isAdmin`은 가입 승인 같은 운영자 권한에 가깝게 본다.

---

## 6. API 주소 정책

역할과 목적이 API 주소에서 드러나야 한다.

기본 규칙은 다음과 같다.

```text
공통 조회 API: /api/{resources}
관리용 API: /api/management/{resources}
지도자 전용 API: /api/coach/{resources}
선수 본인 API: /api/player/me/{resources}
관리자 API: /api/admin/{resources}
```

예시는 다음과 같다.

```http
GET    /api/match-videos
POST   /api/management/match-videos
DELETE /api/coach/match-videos/{matchVideoId}

GET    /api/team-analysis-clips
POST   /api/management/team-analysis-clips/with-drawings
PUT    /api/management/team-analysis-clips/{teamClipId}/with-drawings
DELETE /api/coach/team-analysis-clips/{teamClipId}

GET    /api/management/player-analysis-clips
GET    /api/player/me/player-analysis-clips
DELETE /api/coach/player-analysis-clips/{playerClipId}

GET    /api/management/players/{playerId}/player-analysis-clip-views
GET    /api/player/me/player-analysis-clip-views

POST   /api/management/player-record-events
GET    /api/player/me/player-record-events/{eventId}
```

API 주소를 새로 만들 때는 기존 주소 체계를 우선 따른다.

---

## 7. DB 기본 정책

현재 서비스는 단일 팀 기준이다.

따라서 초기 구조에서는 별도 `team` 테이블을 사용하지 않는다.

주요 테이블은 다음과 같다.

```text
member
schedule
notice
game_video_upload
team_video_clip
player_video_clip
team_video_clip_drawing
player_video_clip_drawing
player_video_clip_view
player_record
player_record_event
player_record_event_clip
video_bookmark
```

삭제는 기본적으로 실제 삭제가 아니라 소프트 삭제를 사용한다.

```text
is_deleted = false: 활성 데이터
is_deleted = true: 삭제 처리된 데이터
```

일반 목록/상세 조회에서는 삭제 데이터를 제외한다.

---

## 8. 경기 영상 정책

경기 원본 영상은 실제 `.mp4` 파일 업로드 방식으로 관리한다.

초기 로컬 저장 경로는 다음을 사용한다.

```text
backend/uploads/match-videos
```

DB의 `game_video_upload.url`에는 재생 가능한 접근 URL을 저장한다.

영상 길이는 백엔드에서 `ffprobe`로 추출한다.

영상 길이는 `game_video_upload.duration_sec`에 저장한다.

경기 영상은 다음 기능의 기준 데이터다.

* 팀 분석 클립 생성
* 선수 개인 분석 클립 생성
* 선수 기록 연결
* 선수 기록 이벤트 시간 검증
* 영상 편집기 기준 영상

경기 영상 삭제는 소프트 삭제다.

초기 MVP에서는 실제 영상 파일은 삭제하지 않는다.

---

## 9. 팀 분석 클립 최신 정책

팀 분석 클립은 원본 경기 영상 URL + 시간 메타데이터 구간 재생 구조가 아니라 실제 mp4 파일 생성 구조를 사용한다.

기본 흐름은 다음과 같다.

1. `COACH` 또는 `ANALYST`가 통합 생성 API를 호출한다.
2. 백엔드는 원본 경기 영상, 클립 시간, 드로잉 목록을 검증한다.
3. `team_video_clip` row를 `PROCESSING` 상태로 저장한다.
4. `team_video_clip_drawing` row를 함께 저장한다.
5. 트랜잭션 커밋 후 FFmpeg 비동기 작업을 실행한다.
6. FFmpeg로 원본 경기 영상 구간을 잘라낸다.
7. 성공 시 `team_video_clip.url`에 생성된 클립 파일 URL을 저장한다.
8. 성공 시 `status = READY`로 변경한다.
9. 실패 시 `status = FAILED`로 변경한다.

주요 API는 다음과 같다.

```http
GET  /api/team-analysis-clips
GET  /api/team-analysis-clips/{teamClipId}
GET  /api/team-analysis-clips/{teamClipId}/drawings
POST /api/management/team-analysis-clips/with-drawings
PUT  /api/management/team-analysis-clips/{teamClipId}/with-drawings
```

목록 조회 정책은 다음과 같다.

* 목록 조회는 `READY` 상태만 반환한다.
* `PROCESSING`, `FAILED`는 일반 목록에서 노출하지 않는다.
* 실패 클립 관리는 추후 별도 관리자용 화면에서 검토한다.
* `matchVideoId` 조건 조회를 지원한다.
* `clipType` 조건 조회를 지원한다.
* `clipType`은 Controller에서 String으로 받고 Validator에서 Enum으로 변환한다.
* 잘못된 `clipType`은 400 에러로 처리한다.

상세 조회 응답에는 다음 필드가 포함되어야 한다.

```text
teamClipId
matchVideoId
matchVideoTitle
clipType
title
comment
startTimeSec
endTimeSec
teamClipUrl
status
editorId
editorName
createdAt
updatedAt
```

---

## 10. 팀 분석 클립 DB 정책

`team_video_clip`의 주요 필드는 다음과 같다.

```text
id
upload_id
member_id
clip_type
title
comment
start_time_sec
end_time_sec
url
status
is_deleted
created_at
updated_at
```

정책은 다음과 같다.

* `start_time_sec`, `end_time_sec`는 원본 경기 영상 기준 초다.
* `url`은 생성된 팀 분석 클립 mp4 접근 URL이다.
* `status`는 PROCESSING, READY, FAILED를 사용한다.
* 삭제는 소프트 삭제다.

`team_video_clip_drawing`의 주요 필드는 다음과 같다.

```text
id
team_video_clip_id
member_id
drawing_type
start_time_sec
end_time_sec
drawing_data
is_deleted
created_at
updated_at
```

드로잉 시간 기준은 생성된 팀 분석 클립 영상 기준 초다.

---

## 11. 팀 분석 클립 수정 정책

팀 분석 클립과 드로잉은 통합 수정 API로 처리한다.

```http
PUT /api/management/team-analysis-clips/{teamClipId}/with-drawings
```

정책은 다음과 같다.

* `COACH`, `ANALYST`만 호출 가능하다.
* `PLAYER`는 호출 불가다.
* `PROCESSING` 상태에서는 수정할 수 없다.
* `FAILED` 상태는 수정 저장을 통해 재생성을 요청할 수 있다.
* 드로잉은 전체 교체 방식으로 처리한다.
* 기존 드로잉은 소프트 삭제한다.
* 요청 드로잉을 새로 저장한다.
* 제목/코멘트/드로잉만 변경하면 기존 mp4 파일을 유지한다.
* 시작/종료 시간이 변경되면 mp4 파일을 재생성한다.

---

## 12. 선수 개인 분석 클립 정책

선수 개인 분석 클립도 실제 mp4 파일 비동기 생성 구조가 구현되어 있다.

기본 흐름은 다음과 같다.

1. `COACH` 또는 `ANALYST`가 생성 API를 호출한다.
2. 백엔드는 원본 경기 영상, 대상 선수, 클립 시간, 드로잉 목록을 검증한다.
3. `player_video_clip` row를 `PROCESSING` 상태로 저장한다.
4. 필요 시 `player_video_clip_drawing` row를 함께 저장한다.
5. 트랜잭션 커밋 후 FFmpeg 비동기 작업을 실행한다.
6. 성공 시 `player_video_clip.url`에 생성된 클립 파일 URL을 저장한다.
7. 성공 시 `status = READY`로 변경한다.
8. 실패 시 `status = FAILED`로 변경한다.

주요 API는 다음과 같다.

```http
GET    /api/management/player-analysis-clips
GET    /api/management/player-analysis-clips/{playerClipId}
GET    /api/player/me/player-analysis-clips
GET    /api/player/me/player-analysis-clips/{playerClipId}
GET    /api/player-analysis-clips/{playerClipId}/drawings
POST   /api/management/player-analysis-clips/with-drawings
PUT    /api/management/player-analysis-clips/{playerClipId}/with-drawings
DELETE /api/coach/player-analysis-clips/{playerClipId}
```

선수 개인 분석 클립의 상세 재생 기준은 다음 필드다.

```text
playerClipUrl
```

`player_video_clip.start_time_sec`, `player_video_clip.end_time_sec`는 DB와 Entity에는 유지한다.

이 값은 원본 경기 영상에서 어떤 구간을 잘라냈는지 기록하는 생성 이력, 재생성, 디버깅용 메타데이터다.

선수 개인 분석 클립과 드로잉은 통합 수정 API를 사용한다.

```http
PUT /api/management/player-analysis-clips/{playerClipId}/with-drawings
```

정책은 다음과 같다.

* `COACH`, `ANALYST`만 호출 가능하다.
* `PLAYER`는 호출 불가다.
* 드로잉은 전체 교체 방식으로 처리한다.
* 기존 드로잉은 소프트 삭제한다.
* 요청 드로잉을 새로 저장한다.
* `PROCESSING` 상태에서는 수정할 수 없다.
* `FAILED` 상태는 수정 저장을 통해 재생성을 요청할 수 있다.
* 제목/코멘트/드로잉만 변경하면 기존 mp4 파일을 유지한다.
* 클립 시작/종료 시간이 변경되면 mp4 파일을 재생성한다.
* 기존 URL이 없으면 mp4 파일을 재생성한다.

---

## 13. 선수 개인 분석 클립 조회 기록 정책

선수 개인 분석 클립 조회 기록은 `player_video_clip_view` 테이블을 사용한다.

조회 기록은 다음 기준으로 저장한다.

```text
해당 클립의 대상 PLAYER가 본인 개인 분석 클립 상세 조회
→ 최초 조회 시 조회 기록 생성
→ 재조회 시 lastViewedAt 갱신 및 viewCount 증가

COACH가 관리용 상세 조회
→ 조회 기록 증가 없음

ANALYST가 관리용 상세 조회
→ 조회 기록 증가 없음
```

조회 기록 저장/갱신은 백엔드에서 자동 처리한다.

프론트가 조회 기록 생성 API를 따로 호출하지 않는다.

관리용 조회 기록 API는 다음과 같다.

```http
GET /api/management/players/{playerId}/player-analysis-clip-views?page=0&size=50
```

선수 본인 조회 기록 API는 다음과 같다.

```http
GET /api/player/me/player-analysis-clip-views?page=0&size=20
```

현재 프론트에서는 선수 본인 조회 기록 목록 화면은 노출하지 않는다.

조회 기록 응답 DTO는 다음 필드를 가진다.

```text
viewId
playerClipId
playerClipTitle
playerId
playerName
firstViewedAt
lastViewedAt
viewCount
createdAt
updatedAt
```

페이지 응답 DTO는 다음 필드를 가진다.

```text
views
page
size
totalElements
totalPages
```

조회 기록 삭제 기능은 현재 구현하지 않는다.

---

## 14. 선수 기록 정책

선수 기록은 `player_record` 테이블을 사용한다.

`player_record`는 경기별 선수 요약 기록이다.

기본 정책은 다음과 같다.

```text
한 경기 영상 + 한 선수 기준 활성 player_record는 1개만 유지
upload_id + player_id + is_deleted = false 기준
```

선수 기록 기능에서는 기존 최종 정책상 `COACH`, `ANALYST`가 등록, 조회, 수정, 삭제할 수 있다.

`PLAYER`는 본인 기록만 조회할 수 있다.

`player_record.memo`는 경기 전체 선수 기록 메모다.

이벤트 단위 메모는 `player_record_event.memo`에 따로 저장한다.

---

## 15. 선수 기록 이벤트 정책

선수 기록 이벤트는 `player_record_event` 테이블을 사용한다.

선수 기록 이벤트는 경기 영상 구간 기반의 개별 기록이다.

`player_record_event`는 `player_record`에 연결된다.

`player_record_event`에는 `upload_id`, `player_id`를 직접 저장하지 않는다.

경기 영상과 선수 정보는 다음 경로로 확인한다.

```text
player_record_event.player_record_id
→ player_record.upload_id
→ player_record.player_id
```

이벤트 시간은 원본 경기 영상 기준 구간으로 저장한다.

```text
eventStartTimeSec
eventEndTimeSec
```

DB 컬럼은 다음을 사용한다.

```text
event_start_time_sec
event_end_time_sec
```

검증 기준은 다음과 같다.

```text
eventStartTimeSec >= 0
eventEndTimeSec > eventStartTimeSec
eventEndTimeSec <= game_video_upload.duration_sec
```

`game_video_upload.duration_sec`가 없으면 선수 기록 이벤트를 생성하거나 수정할 수 없다.

이벤트 단위 메모는 `player_record_event.memo`에 저장한다.

DTO에서는 `eventMemo`로 표현한다.

---

## 16. 선수 기록 이벤트 유형 정책

선수 기록 이벤트 유형은 다음 값을 사용한다.

```text
GOAL
ASSIST
SHOT
SHOT_ON_TARGET
PASS
SUCCESSFUL_PASS
DRIBBLE
SUCCESSFUL_DRIBBLE
TACKLE
INTERCEPTION
CLEARANCE
SAVE
YELLOW_CARD
RED_CARD
ETC
```

이벤트 생성 시 `player_record` 요약 수치를 증가시킨다.

이벤트 수정 시 기존 이벤트 수치를 차감하고 수정된 이벤트 수치를 다시 반영한다.

이벤트 삭제 시 기존 이벤트 수치를 차감한다.

반영 기준은 다음과 같다.

```text
GOAL → goals
ASSIST → assists
SHOT → shots
SHOT_ON_TARGET → shots, shotsOnTarget
PASS → passes
SUCCESSFUL_PASS → passes, successfulPasses
DRIBBLE → dribbles
SUCCESSFUL_DRIBBLE → dribbles, successfulDribbles
TACKLE → tackles
INTERCEPTION → interceptions
CLEARANCE → clearances
SAVE → saves
YELLOW_CARD → yellowCards
RED_CARD → redCards
ETC → 요약 수치 반영 없음
```

초기 MVP에서는 `GOAL` 등록 시 `shots`, `shotsOnTarget`까지 자동 증가시키지 않는다.

---

## 17. 선수 기록 이벤트-클립 연결 정책

선수 기록 이벤트와 클립 연결은 `player_record_event_clip` 테이블을 사용한다.

연결은 선택 사항이다.

기록 이벤트는 클립 없이도 존재할 수 있다.

연결 가능한 클립 출처는 다음과 같다.

```text
TEAM_ANALYSIS
PLAYER_ANALYSIS
```

### TEAM_ANALYSIS

팀 분석 클립 연결 시 다음 조건을 만족해야 한다.

```text
player_record.upload_id = team_video_clip.upload_id
```

팀 분석 클립은 팀 전체 장면이므로 기록 대상 선수는 요청에서 별도로 선택한다.

### PLAYER_ANALYSIS

선수 개인 분석 클립 연결 시 다음 조건을 모두 만족해야 한다.

```text
player_record.upload_id = player_video_clip.upload_id
player_record.player_id = player_video_clip.player_id
```

다른 경기 영상의 개인 분석 클립이나 다른 선수의 개인 분석 클립은 연결할 수 없다.

---

## 18. 선수 기록 이벤트 API 정책

관리용 API는 다음과 같다.

```http
POST /api/management/player-record-events
POST /api/management/player-record-events/with-clip-link
GET /api/management/player-records/{recordId}/events
GET /api/management/player-record-events/{eventId}
PATCH /api/management/player-record-events/{eventId}
DELETE /api/management/player-record-events/{eventId}
```

선수 본인 조회 API는 다음과 같다.

```http
GET /api/player/me/player-records/{recordId}/events
GET /api/player/me/player-record-events/{eventId}
```

관리 API는 `COACH`, `ANALYST`만 호출할 수 있다.

선수 본인 API는 `PLAYER`만 호출할 수 있고, 본인 기록 이벤트만 조회할 수 있다.

---

## 19. 상태 정책

팀 분석 클립과 선수 개인 분석 클립 파일 생성 구조에서는 다음 상태를 사용한다.

| 상태 | 의미 |
| --- | --- |
| `PROCESSING` | 클립 파일 생성 중 |
| `READY` | 클립 파일 생성 완료, 재생 가능 |
| `FAILED` | 클립 파일 생성 실패 |
| `UPLOADING` | 기존 경기 영상 업로드 또는 기존 코드 호환용 |

프론트에서는 다음 기준으로 처리한다.

```text
PROCESSING
→ 생성 중 메시지 표시
→ 영상 재생 막기
→ 드로잉 렌더링 막기

FAILED
→ 생성 실패 메시지 표시
→ 영상 재생 막기
→ 드로잉 렌더링 막기

READY
→ 생성된 클립 URL 기준으로 영상 재생
→ 드로잉 오버레이 렌더링 가능
```

---

## 20. 드로잉 정책

드로잉은 영상 파일에 합성하지 않는다.

드로잉은 JSON 데이터로 별도 테이블에 저장한다.

팀 분석 클립 드로잉은 `team_video_clip_drawing` 테이블을 사용한다.

선수 개인 분석 클립 드로잉은 `player_video_clip_drawing` 테이블을 사용한다.

드로잉 유형은 다음을 기준으로 한다.

```text
LINE
ARROW
CIRCLE
BOX
AREA
TEXT
```

드로잉 데이터는 프론트 캔버스에서 생성한 좌표, 텍스트, 색상, 두께 등을 JSON으로 저장한다.

드로잉 시간 기준은 생성된 클립 영상 기준 초다.

검증 기준은 다음과 같다.

```text
drawingStartTimeSec >= 0
drawingEndTimeSec <= clipDurationSec
drawingStartTimeSec < drawingEndTimeSec
```

`clipDurationSec`는 다음으로 계산한다.

```text
clip.end_time_sec - clip.start_time_sec
```

---

## 21. 파일 저장 정책

로컬 개발 기준 저장 경로는 다음과 같다.

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

## 22. CORS 정책

프론트 개발 서버는 다음 Origin을 사용한다.

```text
http://localhost:5173
```

Spring Security 환경에서는 `SecurityConfig`의 CORS 설정이 중요하다.

개발 기준 CORS 허용 메서드는 다음과 같다.

```text
GET
POST
PUT
PATCH
DELETE
OPTIONS
```

요청 헤더는 다음을 허용한다.

```text
Authorization
Content-Type
```

노출 헤더는 다음을 허용한다.

```text
Authorization
```

`OPTIONS /**`는 `permitAll`로 허용한다.

실제 관리 API 요청은 기존처럼 JWT 인증을 타야 한다.

---

## 23. 예외 정책

공통 예외 응답은 `ErrorResponse`와 `ErrorCode`를 사용한다.

주요 예외는 다음과 같다.

```text
MATCH_VIDEO_NOT_FOUND
MATCH_VIDEO_DURATION_NOT_READY
INVALID_CLIP_TIME_RANGE
INVALID_TEAM_ANALYSIS_CLIP_TYPE
TEAM_ANALYSIS_CLIP_NOT_FOUND
TEAM_ANALYSIS_CLIP_PROCESSING_CANNOT_UPDATE
TEAM_ANALYSIS_CLIP_DIRECTORY_CREATE_FAILED
TEAM_ANALYSIS_CLIP_ORIGINAL_FILE_NOT_FOUND
TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED
PLAYER_NOT_FOUND
INVALID_PLAYER_ROLE
PLAYER_ANALYSIS_CLIP_NOT_FOUND
INVALID_DRAWING_TIME_RANGE
PLAYER_RECORD_NOT_FOUND
INVALID_PLAYER_RECORD_PLAYER
INVALID_PLAYER_RECORD_VALUE
PLAYER_RECORD_EVENT_NOT_FOUND
PLAYER_RECORD_EVENT_CLIP_NOT_FOUND
PLAYER_RECORD_EVENT_ACCESS_DENIED
PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED
INVALID_PLAYER_RECORD_EVENT_REQUEST
INVALID_PLAYER_RECORD_EVENT_TYPE
INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE
INVALID_PLAYER_RECORD_EVENT_TIME
INVALID_PLAYER_RECORD_EVENT_VALUE
PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH
PLAYER_RECORD_EVENT_PLAYER_MISMATCH
FORBIDDEN
```

새 예외가 필요하면 `ErrorCode`에 추가하고 요구사항 문서에 반영한다.

---

## 24. 비동기 작업 정책

초기 운영형 구조는 Spring `@Async` 기반으로 시작한다.

단, 추후 확장을 위해 비동기 작업 서비스는 분리한다.

추후 확장 후보는 다음과 같다.

* Redis Queue
* RabbitMQ
* Kafka
* 별도 인코딩 워커 서버
* 재시도 API
* 실패 사유 DB 저장
* 썸네일 생성
* CDN 업로드

파일 생성 작업과 DB 업데이트는 실패 처리를 명확히 분리한다.

FFmpeg 실패 시 API 요청 자체를 되돌리지 않고, 클립 상태를 `FAILED`로 변경한다.

---

## 25. 코드 작성 규칙

코드는 짧게 쓰는 것보다 가독성을 우선한다.

클래스와 메서드 이름은 역할이 드러나게 작성한다.

예시:

```java
createTeamAnalysisClipWithDrawings()
updateTeamAnalysisClipWithDrawings()
generateTeamAnalysisClipFile()
markTeamClipGenerationFailed()
createPlayerAnalysisClipWithDrawings()
generatePlayerAnalysisClipFile()
recordViewIfPlayer()
findPlayerViewHistoriesForManagement()
createPlayerRecordEvent()
createPlayerRecordEventWithClipLink()
findPlayerRecordEventsForManagement()
validateEventTimeRangeWithinMatchVideo()
```

피해야 할 이름:

```java
doSave()
makeFile()
chk()
```

파일 생성, 저장, 검증, 비동기 실행 책임은 가능한 분리한다.

선수 기록 이벤트 기능에서는 다음 책임을 분리한다.

```text
Controller
→ API 요청/응답 담당

Service
→ 트랜잭션, 이벤트 저장, 요약 수치 보정 담당

Validator
→ 요청값, 권한, 클립 연결 조건, 영상 길이 검증 담당

Repository
→ DB 조회 담당
```

---

## 26. 다음 백엔드 작업 시작 시 필요한 파일

다음 작업은 프론트 연동이므로 백엔드 작업은 당장 추가로 진행하지 않는다.

다만 선수 기록 이벤트 프론트 연동 시 백엔드 DTO와 API 주소를 확인하기 위해 다음 파일을 참고한다.

```text
PlayerRecordEventController.java
PlayerRecordEventService.java
PlayerRecordEventValidator.java
PlayerRecordEventEntity.java
PlayerRecordEventClipEntity.java
PlayerRecordEventRepository.java
PlayerRecordEventClipRepository.java
CreatePlayerRecordEventRequestDTO.java
CreatePlayerRecordEventWithClipLinkRequestDTO.java
UpdatePlayerRecordEventRequestDTO.java
CreatePlayerRecordEventResponseDTO.java
PlayerRecordEventResponseDTO.java
PlayerRecordEventClipResponseDTO.java
PlayerRecordEventListResponseDTO.java
ErrorCode.java
SecurityConfig.java
```

파일이 없거나 최신 코드가 불확실하면 사용자에게 먼저 요청한다.

사용자가 현재 로컬 파일과 GitHub `main`이 동일하다고 명확히 말한 경우에는 GitHub `main` 기준으로 실제 코드 구조를 확인한 뒤 진행할 수 있다.