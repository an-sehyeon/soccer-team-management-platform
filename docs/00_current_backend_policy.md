# 00. 현재 백엔드 정책

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 최신 백엔드 정책을 요약한다.

새 채팅에서 백엔드 기능을 이어서 구현하거나 프론트 연동 전에 백엔드 API를 확인할 때 이 문서를 우선 참고한다.

상세 정책은 기능별 요구사항 문서와 실제 소스 코드를 기준으로 확인한다.

선수 기록 관련 최신 상세 문서는 다음과 같다.

```text
docs/15_player_record_requirements_final.md
```

경기 영상 북마크 관련 최신 상세 문서는 다음과 같다.

```text
docs/29_match_video_bookmark_requirements.md
```

---

## 2. 기술 스택

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

* 회원가입과 로그인 기능은 구현되어 있다.
* 로그인 성공 시 JWT Access Token을 발급한다.
* 인증 후 `CustomUserPrincipal`에서 다음 값을 사용한다.

```text
memberId
memberRole
isAdmin
```

* 주요 API는 인증된 사용자만 접근할 수 있다.
* 회원가입과 로그인 API만 `permitAll` 대상이다.
* 권한 검증은 프론트가 아니라 반드시 백엔드에서 처리한다.
* `isAdmin = true`만으로 선수 기록이나 영상 관리 권한을 부여하지 않는다.

---

## 4. 역할 정책

### COACH

* 스케줄 등록·조회·수정·삭제
* 공지사항 등록·조회·수정·삭제
* 경기 영상 업로드·조회·수정·삭제
* 팀 분석 클립 등록·조회·수정·삭제
* 선수 개인 분석 클립 등록·조회·수정·삭제
* 드로잉 등록·조회·수정·삭제
* 선수 개인 분석 클립 조회 기록 확인
* 경기 영상 기준 선수 기록 등록·갱신
* 분석 클립 연결 선수 기록 이벤트 등록
* 전체 선수 기록과 이벤트 조회
* 본인 영상 북마크 등록·조회·수정·삭제

### ANALYST

* 스케줄과 공지사항 조회
* 경기 영상 업로드·조회·수정
* 팀 분석 클립 등록·조회·수정
* 선수 개인 분석 클립 등록·조회·수정
* 드로잉 등록·조회·수정
* 선수 개인 분석 클립 조회 기록 확인
* 경기 영상 기준 선수 기록 등록·갱신
* 분석 클립 연결 선수 기록 이벤트 등록
* 전체 선수 기록과 이벤트 조회
* 본인 영상 북마크 등록·조회·수정·삭제

영상과 분석 클립의 삭제 권한은 기본적으로 없다.

북마크는 개인 임시 작업 데이터이므로 본인 북마크 삭제를 허용한다.

### PLAYER

* 스케줄 조회
* 공지사항 조회
* 경기 원본 영상 조회
* READY 팀 분석 클립 조회
* 본인 선수 개인 분석 클립 조회
* 본인 드로잉 조회
* 본인 선수 개인 분석 클립 조회 기록 생성·갱신
* 본인 선수 기록 조회
* 본인 선수 기록 이벤트 조회

선수 기록 등록·수정과 영상 북마크 관리 API에는 접근할 수 없다.

---

## 5. 권한 검증 원칙

* 권한 검증은 Service 또는 Validator 계층에서 처리한다.
* 프론트에서 버튼을 숨겨도 백엔드 검증은 유지한다.
* `isAdmin = true`만으로 영상, 클립, 선수 기록 관리 권한을 부여하지 않는다.
* 선수 본인 조회는 로그인한 `memberId`를 기준으로 제한한다.
* 사용자 소유 데이터는 로그인 사용자 ID를 함께 검증한다.
* 선수 기록 관리 권한은 `COACH`, `ANALYST` 역할에만 부여한다.
* `memberRole = PLAYER`, `isAdmin = true`인 사용자도 선수 기록 관리 API에 접근할 수 없다.

---

## 6. API 주소 정책

역할과 목적이 API 주소에 드러나야 한다.

```text
공통 조회: /api/{resources}
관리용: /api/management/{resources}
지도자 전용: /api/coach/{resources}
선수 본인: /api/player/me/{resources}
관리자: /api/admin/{resources}
```

주요 예시는 다음과 같다.

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

GET    /api/management/player-records
GET    /api/management/player-records/{recordId}
POST   /api/management/player-records
PATCH  /api/management/player-records/{recordId}

POST   /api/management/player-record-events/with-clip-link
GET    /api/management/player-records/{recordId}/events
GET    /api/management/player-record-events/{eventId}
GET    /api/player/me/player-records/{recordId}/events
GET    /api/player/me/player-record-events/{eventId}

POST   /api/management/video-bookmarks
GET    /api/management/video-bookmarks
PATCH  /api/management/video-bookmarks/{bookmarkId}
DELETE /api/management/video-bookmarks/{bookmarkId}
```

다음 독립 선수 기록 이벤트 쓰기 API는 제거됐다.

```http
POST   /api/management/player-record-events
PATCH  /api/management/player-record-events/{eventId}
DELETE /api/management/player-record-events/{eventId}
```

선수 기록 이벤트는 분석 클립 연결 API를 통해서만 신규 생성한다.

새 API를 만들기 전 기존 Controller와 주소 체계를 먼저 확인한다.

---

## 7. DB 기본 정책

현재 서비스는 단일 팀 기준이다.

초기 구조에서는 별도 `team` 테이블을 사용하지 않는다.

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

삭제는 기본적으로 소프트 삭제를 사용한다.

```text
is_deleted = false
→ 활성 데이터

is_deleted = true
→ 삭제 데이터
```

일반 목록과 상세 조회에서는 삭제 데이터를 제외한다.

선수 기록 API 개편에서는 신규 테이블과 신규 컬럼을 추가하지 않았다.

---

## 8. 경기 영상 정책

* 경기 원본 영상은 실제 `.mp4` 업로드 방식으로 관리한다.
* 로컬 저장 경로는 `backend/uploads/match-videos`를 사용한다.
* DB에는 파일 자체가 아니라 URL과 메타데이터를 저장한다.
* 영상 길이는 `ffprobe`로 추출한다.
* 영상 길이는 `game_video_upload.duration_sec`에 저장한다.
* 경기 영상 삭제는 소프트 삭제다.
* 초기 MVP에서는 실제 영상 파일을 바로 삭제하지 않는다.

경기 영상은 다음 기능의 기준 데이터다.

* 팀 분석 클립 생성
* 선수 개인 분석 클립 생성
* 선수 기록 등록
* 분석 클립 연결 기록 검증
* 영상 북마크
* 영상 편집기

---

## 9. 팀 분석 클립 정책

* 팀 분석 클립은 실제 mp4 파일을 비동기로 생성한다.
* 생성 직후 `PROCESSING` 상태로 저장한다.
* FFmpeg 성공 시 URL을 저장하고 `READY`로 변경한다.
* 실패 시 `FAILED`로 변경한다.
* 일반 목록 조회는 `READY`만 반환한다.
* 팀 분석 클립과 드로잉은 통합 생성·수정 API를 사용한다.
* 시작·종료 시간이 변경되거나 삭제되면 해당 클립의 활성 북마크를 일괄 소프트 삭제한다.
* 제목, 코멘트, 유형, 드로잉만 변경된 경우 북마크를 유지한다.
* 선수 기록 이벤트에는 `READY` 상태의 팀 분석 클립만 연결할 수 있다.

---

## 10. 선수 개인 분석 클립 정책

* 선수 개인 분석 클립은 실제 mp4 파일을 비동기로 생성한다.
* 생성 직후 `PROCESSING` 상태로 저장한다.
* FFmpeg 성공 시 URL을 저장하고 `READY`로 변경한다.
* 실패 시 `FAILED`로 변경한다.
* 선수는 본인 클립만 조회할 수 있다.
* 관리자는 관리용 API로 전체 선수 클립을 조회한다.
* 선수 개인 분석 클립과 드로잉은 통합 생성·수정 API를 사용한다.
* 시작·종료 시간, 원본 경기 영상이 변경되거나 삭제되면 해당 클립의 활성 북마크를 일괄 소프트 삭제한다.
* 제목, 코멘트, 유형, 대상 선수, 드로잉만 변경된 경우 북마크를 유지한다.
* 선수 기록 이벤트에는 `READY` 상태의 선수 개인 분석 클립만 연결할 수 있다.
* 선수 개인 분석 클립 연결 시 요청 `playerId`만 신뢰하지 않고 실제 클립 대상 선수와 일치하는지 검증한다.

---

## 11. 영상 북마크 최신 정책

북마크는 `COACH`, `ANALYST` 개인의 임시 분석 작업 데이터다.

* `PLAYER` 접근 불가
* 사용자 본인 북마크만 조회·수정·삭제
* 같은 시간 중복 북마크 허용
* 정수 초 저장
* 제목 필수, 메모 선택
* 경기 원본 영상, 팀 분석 클립, 선수 개인 분석 클립 지원
* `READY` 소스만 사용
* 삭제는 소프트 삭제

소스 조합은 다음과 같다.

```text
원본 영상
upload_id 있음, 두 clip_id null

팀 분석 클립
upload_id 있음, team_clip_id 있음, player_clip_id null

선수 개인 분석 클립
upload_id 있음, team_clip_id null, player_clip_id 있음
```

---

## 12. 선수 기록 화면 책임 정책

* `PlayerRecordPage`는 검색·목록·상세 조회 전용으로 사용한다.
* 선수 기록 등록과 갱신은 `MatchVideoPage`에서 진행한다.
* 독립 선수 기록 이벤트 등록·수정·삭제 기능은 제공하지 않는다.
* 선수 기록 이벤트는 분석 클립 연결 등록을 통해서만 생성한다.

---

## 13. 클립 없이 선수 기록 등록 정책

클립 없이 선수 기록을 저장할 때는 기존 선수 기록 API를 조합해 사용한다.

```http
GET   /api/management/player-records?uploadId={uploadId}&playerId={playerId}
POST  /api/management/player-records
PATCH /api/management/player-records/{recordId}
```

처리 방식은 다음과 같다.

1. 현재 경기와 선수의 활성 `player_record`를 조회한다.
2. 기존 기록이 없으면 생성 API를 호출한다.
3. 기존 기록이 있으면 조회된 `recordId`로 수정 API를 호출한다.
4. 한 요청으로 전체 요약 기록을 저장한다.
5. `player_record_event`, `player_record_event_clip`은 생성하지 않는다.

백엔드는 동일 경기·동일 선수의 활성 기록 중복 생성을 계속 차단한다.

### 수치 검증

각 선수 기록 수치는 다음 범위만 검증한다.

```text
최소값: 0
최대값: 255
```

현재는 다음 추가 정합성 검증을 적용하지 않는다.

```text
shotsOnTarget <= shots
successfulPasses <= passes
successfulDribbles <= dribbles
```

---

## 14. 분석 클립 연결 선수 기록 정책

클립 연결 이벤트 등록 API는 다음과 같다.

```http
POST /api/management/player-record-events/with-clip-link
```

한 요청에서 다음을 하나씩 선택한다.

* 대상 선수 한 명
* 이벤트 유형 한 개
* 팀 분석 클립 또는 선수 개인 분석 클립 한 개
* 이벤트 메모 선택 입력

클립 연결 요청 DTO에는 다음 필드를 사용한다.

```text
uploadId
playerId
eventType
eventMemo
clipSourceType
teamClipId
playerClipId
```

다음 필드는 요청 DTO에서 제거됐다.

```text
eventStartTimeSec
eventEndTimeSec
value
```

삭제된 독립 요청 DTO는 다음과 같다.

```text
CreatePlayerRecordEventRequestDTO
UpdatePlayerRecordEventRequestDTO
```

---

## 15. 클립 연결 조합 정책

### 팀 분석 클립

```text
clipSourceType = TEAM_ANALYSIS
teamClipId 필수
playerClipId = null
```

### 선수 개인 분석 클립

```text
clipSourceType = PLAYER_ANALYSIS
teamClipId = null
playerClipId 필수
```

두 클립 ID가 모두 존재하거나 모두 없는 요청은 차단한다.

---

## 16. 이벤트 시간과 value 정책

클립 연결 요청에서는 이벤트 시간과 `value`를 받지 않는다.

백엔드가 선택한 클립에서 다음 값을 조회해 저장한다.

```text
eventStartTimeSec = clip.startTimeSec
eventEndTimeSec = clip.endTimeSec
value = 1
```

다음 DB 컬럼은 유지한다.

```text
player_record_event.event_start_time_sec
player_record_event.event_end_time_sec
player_record_event.value
```

이벤트 시간은 등록 당시 선택한 클립 구간의 스냅샷이다.

클립 시간이 나중에 수정돼도 기존 이벤트의 시간 스냅샷은 변경하지 않는다.

실제 영상 재생은 연결된 현재 클립을 기준으로 한다.

---

## 17. 선수 기록 이벤트 중복 정책

중복 기준은 다음과 같다.

```text
clip_source_type
+ 실제 clip_id
+ event_type
+ 활성 player_record_event
+ 활성 player_record_event_clip
```

허용 예시는 다음과 같다.

```text
클립 10 + SHOT
클립 10 + PASS
```

차단 예시는 다음과 같다.

```text
클립 10 + SHOT
클립 10 + SHOT
```

중복 기준에는 기록 대상 선수를 포함하지 않는다.

같은 팀 분석 클립에 같은 이벤트 유형이 이미 연결돼 있다면 다른 선수 대상으로 다시 연결하는 요청도 차단한다.

중복 시 다음 응답을 사용한다.

```text
HTTP 409 Conflict
선택한 클립에는 해당 선수 기록 유형이 이미 연결되어 있습니다.
```

---

## 18. 동시 중복 요청 방지 정책

같은 클립과 같은 이벤트 유형의 동시 요청을 막기 위해 선택한 분석 클립을 비관적 쓰기 잠금으로 조회한다.

```text
PESSIMISTIC_WRITE
```

처리 순서는 다음과 같다.

1. 선택한 클립 잠금 조회
2. 클립 상태와 관계 검증
3. 동일 클립·동일 이벤트 유형 중복 조회
4. 중복이 없으면 이벤트 저장
5. 클립 연결 저장
6. 선수 요약 기록 갱신
7. 트랜잭션 종료 후 잠금 해제

동일 요청이 동시에 들어오면 한 요청만 성공하고 다른 요청은 `409 Conflict`를 반환해야 한다.

---

## 19. 기존 player_record 자동 생성 정책

클립 연결 이벤트 등록 시 현재 경기와 선수의 활성 `player_record`를 조회한다.

기존 기록이 있으면 해당 기록을 사용한다.

기존 기록이 없으면 모든 수치를 0으로 생성한다.

```text
minutesPlayed = 0
goals = 0
assists = 0
shots = 0
shotsOnTarget = 0
passes = 0
successfulPasses = 0
dribbles = 0
successfulDribbles = 0
tackles = 0
interceptions = 0
clearances = 0
saves = 0
yellowCards = 0
redCards = 0
memo = null
```

이후 선택한 이벤트 유형에 해당하는 선수 요약 수치를 반영한다.

---

## 20. 선수 기록 요약 반영 정책

클립 연결 이벤트 등록 시 기본 반영은 다음과 같다.

```text
GOAL → goals +1
ASSIST → assists +1
SHOT → shots +1
SHOT_ON_TARGET → shots +1, shots_on_target +1
PASS → passes +1
SUCCESSFUL_PASS → passes +1, successful_passes +1
DRIBBLE → dribbles +1
SUCCESSFUL_DRIBBLE → dribbles +1, successful_dribbles +1
TACKLE → tackles +1
INTERCEPTION → interceptions +1
CLEARANCE → clearances +1
SAVE → saves +1
YELLOW_CARD → yellow_cards +1
RED_CARD → red_cards +1
ETC → 요약 수치 변경 없음
```

요약 기록 수치가 255를 초과하면 저장하지 않고 전체 트랜잭션을 롤백한다.

---

## 21. 트랜잭션 정책

다음 작업은 하나의 트랜잭션으로 처리한다.

```text
클립 잠금 조회
클립 검증
중복 검증
player_record 조회 또는 생성
player_record_event 저장
player_record_event_clip 저장
player_record 요약 수치 갱신
```

중간에 예외가 발생하면 다음 데이터가 모두 저장 이전 상태로 롤백돼야 한다.

```text
player_record
player_record_event
player_record_event_clip
```

이벤트와 연결을 먼저 저장한 뒤 요약 수치 반영에서 예외가 발생하더라도 이벤트와 연결 데이터는 DB에 남지 않는다.

---

## 22. 선수 기록 이벤트 조회 정책

### 관리용

```http
GET /api/management/player-records/{recordId}/events
GET /api/management/player-record-events/{eventId}
```

`COACH`, `ANALYST`만 접근할 수 있다.

### 선수 본인

```http
GET /api/player/me/player-records/{recordId}/events
GET /api/player/me/player-record-events/{eventId}
```

로그인 선수와 기록 대상 선수가 일치할 때만 조회할 수 있다.

다른 선수의 기록 또는 이벤트 조회 요청은 차단한다.

---

## 23. 주요 예외 정책

### READY가 아닌 클립

```text
HTTP 400 Bad Request
READY 상태의 분석 클립만 선수 기록에 연결할 수 있습니다.
```

### 동일 클립·동일 이벤트 유형 중복

```text
HTTP 409 Conflict
선택한 클립에는 해당 선수 기록 유형이 이미 연결되어 있습니다.
```

### 경기 영상 불일치

```text
HTTP 400 Bad Request
선수 기록 이벤트와 연결할 클립의 경기 영상이 일치하지 않습니다.
```

### 개인 클립 대상 선수 불일치

```text
HTTP 400 Bad Request
선수 기록 대상 선수와 선수 개인 분석 클립 대상 선수가 일치하지 않습니다.
```

### 잘못된 클립 출처 조합

```text
HTTP 400 Bad Request
선수 기록 이벤트 클립 출처 유형이 올바르지 않습니다.
```

### 선수 기록 관리 권한 없음

```text
HTTP 403 Forbidden
선수 기록 이벤트 관리 권한이 없습니다.
```

---

## 24. 조회 기록 정책

선수가 본인 선수 개인 분석 클립 상세를 조회하면 조회 기록을 생성하거나 갱신한다.

```text
최초 조회
firstViewedAt 저장
lastViewedAt 저장
viewCount = 1

재조회
lastViewedAt 갱신
viewCount + 1
```

관리용 상세 조회는 선수 조회 기록에 반영하지 않는다.

---

## 25. 영상 파일 접근 정책

현재 MVP는 `/uploads/**` 직접 접근을 사용한다.

운영 전에는 다음 중 하나로 전환해야 한다.

* 권한 검증 스트리밍 API
* Signed URL
* CDN과 서명 URL

개인 분석 클립은 반드시 백엔드 권한 검증을 거쳐야 한다.

---

## 26. 예외 처리 정책

* 공통 예외는 `ErrorCode`와 `CustomException` 구조를 사용한다.
* 사용자 메시지는 구체적이어야 한다.
* HTTP status, path, 내부 스택은 프론트에 그대로 노출하지 않는다.
* 검증 실패는 가능한 한 저장 전에 처리한다.
* 연관 데이터 저장은 하나의 트랜잭션으로 묶는다.
* DB 저장 범위 초과 등의 저장 중 예외도 전체 트랜잭션을 롤백한다.

---

## 27. 선수 기록 API 개편 완료 상태

경기 영상 기반 선수 기록 등록 및 클립 연결 API 구조 개편은 완료됐다.

완료된 항목은 다음과 같다.

1. 클립 연결 요청 DTO에서 시간과 `value` 제거
2. 팀·선수 클립 시간 자동 조회
3. `value = 1` 고정
4. READY 상태 검증
5. 경기 영상 일치 검증
6. 선수 개인 클립 대상 선수 일치 검증
7. 동일 클립·동일 이벤트 유형 중복 차단
8. 비관적 잠금으로 동시 중복 요청 방지
9. 기존 `player_record`가 없을 때 자동 생성
10. 선택 유형의 선수 요약 수치 반영
11. 독립 이벤트 등록·수정·삭제 API 제거
12. 관리용·선수 본인 이벤트 조회 API 유지
13. 트랜잭션 롤백 검증
14. 클립 시간 스냅샷 유지 검증
15. 신규 DB 테이블과 컬럼 추가 없음

사용자가 백엔드 서버 실행과 주요 API 테스트를 모두 정상 확인했다.

---

## 28. 다음 작업

다음 작업은 경기 영상 기반 선수 기록 등록 프론트 연동이다.

백엔드 PR이 `main`에 병합된 후 최신 `main`을 기준으로 진행한다.

주요 프론트 구현 범위는 다음과 같다.

1. `PlayerRecordPage`를 검색·목록·상세 조회 전용으로 변경
2. 선수 기록과 이벤트 등록·수정·삭제 UI 제거
3. `MatchVideoPage` 버튼명을 `선수 기록 등록`으로 변경
4. 클립 없이 등록 카운터 UI 구현
5. 기존 기록 조회 후 생성·수정 API 분기
6. 팀 분석 클립 연결 등록
7. 선수 개인 분석 클립 연결 등록
8. 이벤트 시간과 `value` 입력 UI 제거
9. 동일 클립·동일 유형 중복 오류 표시
10. 기존 북마크와 분석 클립 기능 회귀 테스트
