# 00. 현재 백엔드 정책

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 백엔드 최신 정책을 요약한다.

새 채팅에서 백엔드 기능을 이어서 구현할 때 이 문서를 우선 참고한다.

상세 내용은 기능별 요구사항 md 문서와 실제 소스 코드를 기준으로 확인한다.

기능별 상세 요구사항 md 문서는 GitHub `docs/` 경로를 기준으로 확인한다.

```text
https://github.com/an-sehyeon/soccer-team-management-platform/tree/main/docs
```

---

## 2. 백엔드 기술 스택

- Java
- Spring Boot
- Gradle
- Spring Security
- JPA
- MySQL
- IntelliJ IDEA
- JWT 인증
- 로컬 파일 저장소
- ffprobe 기반 영상 길이 추출
- FFmpeg 기반 선수 개인 분석 클립 파일 생성
- Spring `@Async` 기반 비동기 파일 생성

---

## 3. 인증 정책

- 회원가입/로그인 기능은 구현되어 있다.
- 로그인 성공 시 JWT Access Token을 발급한다.
- 인증 후 `CustomUserPrincipal`에서 다음 값을 사용한다.

```text
memberId
memberRole
isAdmin
```

- 모든 주요 API는 인증된 사용자만 접근할 수 있다.
- 회원가입과 로그인 API만 `permitAll` 대상이다.
- 권한 검증은 프론트가 아니라 반드시 백엔드에서 처리한다.

---

## 4. 역할 정책

### COACH

지도자는 팀 운영과 분석 기능의 주 관리자다.

기본적으로 등록, 조회, 수정, 삭제 권한을 가진다.

### ANALYST

분석관은 영상 분석 실무자다.

기본적으로 등록, 조회, 수정 권한을 가진다.

삭제 권한은 기본적으로 없다.

단, 선수 기록 기능에서는 기존 최종 정책상 분석관도 삭제 가능하다.

### PLAYER

선수는 조회 중심 사용자다.

본인 데이터만 조회할 수 있는 기능에서는 반드시 로그인한 `memberId` 기준으로 접근 범위를 제한한다.

---

## 5. 권한 검증 원칙

권한 검증은 Service 또는 Validator 계층에서 처리한다.

프론트에서 버튼을 숨겨도 API 직접 호출이 가능하므로 백엔드 검증은 반드시 유지한다.

공통 기준은 다음과 같다.

```text
COACH: 관리 권한
ANALYST: 관리 보조 권한, 삭제 제한
PLAYER: 조회 권한, 본인 데이터 제한
```

`isAdmin = true`만으로 영상/스케줄/공지/클립 관리 권한을 주지 않는다.

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

GET    /api/management/player-analysis-clips
GET    /api/player/me/player-analysis-clips
DELETE /api/coach/player-analysis-clips/{playerClipId}
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
video_bookmark
```

삭제는 기본적으로 실제 삭제가 아니라 소프트 삭제를 사용한다.

```text
is_deleted = false: 활성 데이터
is_deleted = true: 삭제 처리된 데이터
```

일반 목록/상세 조회에서는 삭제 데이터를 제외한다.

단, 영상 파일과 연결되는 클립 정책은 기능별 요구사항을 따른다.

---

## 8. 경기 영상 정책

경기 원본 영상은 실제 `.mp4` 파일 업로드 방식으로 관리한다.

초기 로컬 저장 경로는 다음을 사용한다.

```text
backend/uploads/match-videos
```

DB의 `game_video_upload.url`에는 재생 가능한 접근 URL을 저장한다.

영상 길이는 백엔드에서 `ffprobe`로 추출한다.

경기 영상은 다음 기능의 기준 데이터다.

- 팀 분석 클립 생성
- 선수 개인 분석 클립 생성
- 선수 기록 연결
- 영상 편집기 기준 영상

경기 영상 삭제는 소프트 삭제다.

초기 MVP에서는 실제 경기 영상 파일은 삭제하지 않는다.

운영 전에는 `/uploads/match-videos/**` 직접 접근 구조를 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.

---

## 9. 팀 분석 클립 현재 정책

현재 팀 분석 클립은 원본 경기 영상 기준 `startTimeSec`, `endTimeSec` 메타데이터로 관리한다.

현재 구조는 다음과 같다.

```text
team_video_clip.upload_id = 원본 경기 영상 ID
team_video_clip.start_time_sec = 원본 경기 영상 기준 시작 시간
team_video_clip.end_time_sec = 원본 경기 영상 기준 종료 시간
team_video_clip.url = 현재는 null 또는 추후 클립 URL
```

팀 분석 클립도 다음 작업에서 선수 개인 분석 클립과 동일하게 실제 mp4 클립 파일 생성 구조로 전환한다.

전환 전 반드시 확인해야 할 항목은 다음과 같다.

```text
team_video_clip 테이블에 url/status 컬럼이 실제로 존재하는지
TeamVideoClipEntity에 url/status 필드가 있는지
TeamAnalysisClipDetailResponseDTO가 teamClipUrl/status/startTimeSec/endTimeSec를 내려줄 수 있는지
팀 분석 클립 수정 시 기존 파일 삭제/새 파일 생성 정책이 필요한지
팀 분석 클립 드로잉 시간이 현재 원본 기준인지 클립 기준인지
팀 분석 클립 + 드로잉 통합 등록 API가 이미 있는지
```

---

## 10. 선수 개인 분석 클립 파일 생성 정책

선수 개인 분석 클립은 이제 원본 경기 영상 URL과 시간 메타데이터로 구간 재생하지 않는다.

생성 시 실제 mp4 파일을 비동기로 생성한다.

생성 흐름은 다음과 같다.

1. `COACH` 또는 `ANALYST`가 생성 API를 호출한다.
2. 백엔드는 원본 경기 영상, 대상 선수, 클립 시간, 드로잉 목록을 검증한다.
3. `player_video_clip` row를 `PROCESSING` 상태로 저장한다.
4. 통합 등록인 경우 `player_video_clip_drawing` row를 함께 저장한다.
5. 트랜잭션 커밋 후 FFmpeg 비동기 작업을 실행한다.
6. 성공 시 `player_video_clip.url`에 생성된 클립 파일 URL을 저장한다.
7. 성공 시 `status = READY`로 변경한다.
8. 실패 시 `status = FAILED`로 변경한다.

생성된 선수 개인 분석 클립 파일은 로컬 개발 기준 다음 경로에 저장한다.

```text
backend/uploads/player-analysis-clips
```

DB의 `player_video_clip.url`에는 브라우저 접근용 URL을 저장한다.

예시는 다음과 같다.

```text
/uploads/player-analysis-clips/player-clip-{id}-{uuid}.mp4
```

상세 재생 기준은 다음 필드다.

```text
playerClipUrl
```

`playerClipUrl`은 DB의 `player_video_clip.url`에서 내려온다.

`player_video_clip.start_time_sec`, `player_video_clip.end_time_sec`는 DB와 Entity에 유지한다.

이 값은 원본 경기 영상에서 어떤 구간을 잘라냈는지 기록하는 생성 이력, 수정 폼 초기값, 파일 재생성 요청용 메타데이터다.

프론트 상세 재생에는 사용하지 않는다.

---

## 11. 선수 개인 분석 클립 상세 응답 정책

선수 개인 분석 클립 상세 응답에는 다음 필드를 포함한다.

```text
playerClipId
matchVideoId
matchVideoTitle
playerClipUrl
playerId
playerName
clipType
title
comment
startTimeSec
endTimeSec
status
editorId
editorName
createdAt
updatedAt
```

주의할 점은 다음과 같다.

```text
startTimeSec/endTimeSec는 상세 재생용이 아니다.
startTimeSec/endTimeSec는 수정 폼 초기값과 파일 재생성 요청용이다.
상세 재생은 playerClipUrl 기준으로 처리한다.
목록 응답에는 startTimeSec/endTimeSec를 포함하지 않는다.
```

현재 `PlayerAnalysisClipDetailResponseDTO`는 `PlayerVideoClipEntity`를 받아 `from()` 정적 팩토리 메서드로 생성하는 구조를 사용한다.

Service에서는 생성자를 직접 호출하지 않고 다음 구조를 사용한다.

```java
return PlayerAnalysisClipDetailResponseDTO.from(playerVideoClip);
```

---

## 12. 선수 개인 분석 클립 API 정책

관리자성 목록 조회는 다음 API를 사용한다.

```http
GET /api/management/player-analysis-clips
```

선수 본인 목록 조회는 다음 API를 사용한다.

```http
GET /api/player/me/player-analysis-clips
```

관리자성 상세 조회는 다음 API를 사용한다.

```http
GET /api/management/player-analysis-clips/{playerClipId}
```

선수 본인 상세 조회는 다음 API를 사용한다.

```http
GET /api/player/me/player-analysis-clips/{playerClipId}
```

선수 개인 분석 클립과 드로잉 통합 생성 API는 다음과 같다.

```http
POST /api/management/player-analysis-clips/with-drawings
```

요청 데이터는 다음을 포함한다.

```text
matchVideoId
playerId
clipType
title
comment
startTimeSec
endTimeSec
drawings[]
```

`drawings[]` 항목은 다음을 포함한다.

```text
drawingType
startTimeSec
endTimeSec
drawingData
```

수정 API는 다음과 같다.

```http
PATCH /api/management/player-analysis-clips/{playerClipId}
```

수정 시 새 mp4 파일을 비동기로 재생성한다.

기존 파일은 수정 요청 시점에 삭제하지 않고, 새 파일 생성 성공 후 삭제한다.

삭제 API는 다음과 같다.

```http
DELETE /api/coach/player-analysis-clips/{playerClipId}
```

삭제는 `COACH`만 가능하다.

---

## 13. 선수 개인 분석 클립 상태 정책

선수 개인 분석 클립 파일 생성 구조에서는 다음 상태를 사용한다.

| 상태 | 의미 |
|---|---|
| `PROCESSING` | 클립 파일 생성 중 |
| `READY` | 클립 파일 생성 완료, 재생 가능 |
| `FAILED` | 클립 파일 생성 실패 |

기존 `UPLOADING` 상태는 기존 경기 영상 업로드 또는 기존 코드 호환용으로 유지할 수 있다.

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
→ playerClipUrl 기준으로 영상 재생
→ 드로잉 오버레이 렌더링 가능
```

---

## 14. 드로잉 정책

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

선수 개인 분석 클립 드로잉은 생성된 클립 영상 기준 시간으로 저장한다.

```text
player_video_clip.start_time_sec = 원본 경기 영상 기준 시작 시간
player_video_clip.end_time_sec = 원본 경기 영상 기준 종료 시간
player_video_clip_drawing.start_time_sec = 생성된 클립 영상 기준 시작 시간
player_video_clip_drawing.end_time_sec = 생성된 클립 영상 기준 종료 시간
```

예시는 다음과 같다.

```text
원본 영상 755초~790초를 잘라 35초 클립 생성
드로잉은 3초~8초처럼 클립 내부 시간 기준으로 저장
```

검증 기준은 다음과 같다.

```text
drawingStartTimeSec >= 0
drawingEndTimeSec <= clipDurationSec
drawingStartTimeSec < drawingEndTimeSec
```

`clipDurationSec`는 다음으로 계산한다.

```text
player_video_clip.end_time_sec - player_video_clip.start_time_sec
```

---

## 15. 정적 리소스 접근 정책

로컬 개발 환경에서는 업로드 파일을 브라우저에서 확인하기 위해 정적 리소스 접근을 허용한다.

현재 개발용 접근 경로는 다음과 같다.

```text
/uploads/match-videos/**
/uploads/player-analysis-clips/**
```

`SecurityConfig`와 `WebConfig`에서 위 경로를 매핑한다.

단, 이 방식은 개발 편의용이다.

운영 전에는 반드시 다음 중 하나로 전환해야 한다.

```text
인증 기반 스트리밍 API
Signed URL
CDN Signed URL
권한 검증이 포함된 파일 다운로드/재생 API
```

선수 개인 분석 클립은 개인 접근 제어가 중요하므로 운영 배포 전 정적 파일 직접 접근 구조를 제거하거나 보호해야 한다.

---

## 16. 비동기 작업 정책

현재 선수 개인 분석 클립 파일 생성은 Spring `@Async` 기반으로 시작한다.

비동기 작업은 파일 생성 책임과 DB 상태 업데이트 책임을 분리한다.

FFmpeg 실패 시 API 요청 자체를 되돌리지 않고, 클립 상태를 `FAILED`로 변경한다.

추후 확장 후보는 다음과 같다.

- Redis Queue
- RabbitMQ
- Kafka
- 별도 인코딩 워커 서버
- 재시도 API
- 실패 사유 DB 저장
- 썸네일 생성
- CDN 업로드

---

## 17. 예외 정책

공통 예외 응답은 `ErrorResponse`와 `ErrorCode`를 사용한다.

주요 예외 후보는 다음과 같다.

```text
MATCH_VIDEO_NOT_FOUND
MATCH_VIDEO_DURATION_NOT_READY
INVALID_CLIP_TIME_RANGE
PLAYER_NOT_FOUND
INVALID_PLAYER_ROLE
PLAYER_ANALYSIS_CLIP_NOT_FOUND
INVALID_DRAWING_TIME_RANGE
FORBIDDEN
```

새 예외가 필요하면 `ErrorCode`에 추가하고 요구사항 문서에 반영한다.

---

## 18. 코드 작성 규칙

코드는 짧게 쓰는 것보다 가독성을 우선한다.

클래스와 메서드 이름은 역할이 드러나게 작성한다.

예시는 다음과 같다.

```java
createPlayerAnalysisClipWithDrawings()
requestPlayerClipFileGeneration()
generatePlayerAnalysisClipFile()
markPlayerClipGenerationFailed()
```

피해야 할 이름은 다음과 같다.

```java
doSave()
makeFile()
chk()
```

파일 생성, 저장, 검증, 비동기 실행 책임은 가능한 분리한다.

---

## 19. 다음 백엔드 작업

다음 백엔드 작업은 다음이다.

```text
팀 분석 클립/드로잉 실제 파일 재생 구조로 통일
```

핵심 방향은 다음과 같다.

- 팀 분석 클립도 실제 mp4 파일을 비동기로 생성한다.
- 상세 재생은 원본 영상 URL + `startTimeSec/endTimeSec`가 아니라 `teamClipUrl` 기준으로 처리한다.
- `PROCESSING`, `READY`, `FAILED` 상태를 반영한다.
- 팀 분석 드로잉 시간은 생성된 팀 분석 클립 기준 초로 저장하고 표시한다.
- 팀 분석 클립 등록 시 드로잉을 함께 저장하는 통합 API 필요 여부를 확인한다.
- `COACH`는 등록/조회/수정/삭제 가능, `ANALYST`는 등록/조회/수정 가능, `PLAYER`는 조회만 가능하게 유지한다.

다음 작업을 시작할 때 우선 확인할 파일은 다음과 같다.

```text
TeamAnalysisClipController.java
TeamAnalysisClipDrawingController.java
TeamAnalysisClipService.java
TeamVideoClipEntity.java
TeamVideoClipDrawingEntity.java
TeamVideoClipRepository.java
TeamVideoClipDrawingRepository.java
teamclip 관련 Request DTO
teamclip 관련 Response DTO
teamclipdrawing 관련 Request DTO
teamclipdrawing 관련 Response DTO
GameVideoUploadEntity.java
VideoUploadStatusEnum.java
ErrorCode.java
SecurityConfig.java
WebConfig.java
application.properties
```

파일이 없거나 최신 코드가 불확실하면 사용자에게 먼저 요청한다.
