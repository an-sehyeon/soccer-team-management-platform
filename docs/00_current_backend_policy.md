# 00. 현재 백엔드 정책

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 백엔드 최신 정책을 요약한다.

새 채팅에서 백엔드 기능을 이어서 구현할 때 이 문서를 우선 참고한다.

상세 내용은 기능별 요구사항 md 문서와 실제 소스 코드를 기준으로 확인한다.

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
- FFmpeg 기반 클립 파일 생성 예정

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

초기 MVP에서는 실제 영상 파일은 삭제하지 않는다.

---

## 9. 팀 분석 클립 정책

현재 팀 분석 클립은 원본 경기 영상 기준 `startTimeSec`, `endTimeSec` 메타데이터로 관리한다.

현재 구조는 다음과 같다.

```text
team_video_clip.upload_id = 원본 경기 영상 ID
team_video_clip.start_time_sec = 원본 경기 영상 기준 시작 시간
team_video_clip.end_time_sec = 원본 경기 영상 기준 종료 시간
team_video_clip.url = 현재는 null 또는 추후 클립 URL
```

팀 분석 클립도 추후 실제 mp4 클립 파일 생성 구조로 전환할 수 있다.

다만 현재 우선순위는 선수 개인 분석 클립 파일 생성 구조다.

---

## 10. 선수 개인 분석 클립 기존 정책

기존 선수 개인 분석 클립은 실제 파일 없이 다음 구조로 동작했다.

```text
원본 경기 영상 URL + startTimeSec + endTimeSec
```

기존 `PlayerAnalysisClipService`는 클립 생성 시 `player_video_clip.url`을 null로 저장했다.

이 구조는 MVP로는 빠르지만 실제 운영에서는 다음 문제가 있다.

- 원본 영상 삭제 상태와 클립 재생 정책이 충돌한다.
- 선수 개인 클립이 독립 자료가 아니다.
- 추후 Signed URL, CDN, 스트리밍 API 전환 시 권한 정책이 복잡해진다.
- 선수 피드백 자료를 장기 보관하기 어렵다.

따라서 다음 작업에서 구조를 변경한다.

---

## 11. 선수 개인 분석 클립 신규 정책

다음 백엔드 작업의 핵심 정책은 다음과 같다.

```text
선수 개인 분석 클립 생성 시 실제 mp4 파일을 비동기로 생성한다.
```

신규 생성 흐름은 다음과 같다.

1. `COACH` 또는 `ANALYST`가 통합 생성 API를 호출한다.
2. 백엔드는 원본 경기 영상, 대상 선수, 클립 시간, 드로잉 목록을 검증한다.
3. `player_video_clip` row를 `PROCESSING` 상태로 저장한다.
4. `player_video_clip_drawing` row를 함께 저장한다.
5. 비동기 작업을 등록한다.
6. 백그라운드 작업에서 FFmpeg로 원본 경기 영상 구간을 잘라낸다.
7. 성공 시 `player_video_clip.url`에 생성된 클립 파일 URL을 저장한다.
8. 성공 시 `status = READY`로 변경한다.
9. 실패 시 `status = FAILED`로 변경한다.

신규 API 후보는 다음과 같다.

```http
POST /api/management/player-analysis-clips/with-drawings
```

---

## 12. 선수 개인 분석 클립 상태 정책

선수 개인 분석 클립 파일 생성 구조에서는 다음 상태를 사용한다.

| 상태 | 의미 |
|---|---|
| `PROCESSING` | 클립 파일 생성 중 |
| `READY` | 클립 파일 생성 완료, 재생 가능 |
| `FAILED` | 클립 파일 생성 실패 |

기존 `UPLOADING` 상태는 선수 개인 분석 클립 파일 생성 흐름에는 적합하지 않다.

기존 Enum에 `PROCESSING`이 없으면 추가 여부를 실제 코드 기준으로 확인한다.

---

## 13. 드로잉 정책

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

---

## 14. 드로잉 시간 기준 변경 정책

기존 선수 개인 분석 클립 드로잉은 원본 경기 영상 기준 시간 범위 안에서 검증했다.

신규 선수 개인 분석 클립 파일 생성 구조에서는 드로잉 시간 기준을 다음처럼 변경한다.

```text
player_video_clip.start_time_sec = 원본 경기 영상 기준 시작 시간
player_video_clip.end_time_sec = 원본 경기 영상 기준 종료 시간

player_video_clip_drawing.start_time_sec = 생성된 클립 영상 기준 시작 시간
player_video_clip_drawing.end_time_sec = 생성된 클립 영상 기준 종료 시간
```

예시:

```text
원본 영상 755초~790초를 잘라 35초 클립 생성
드로잉은 3초~8초처럼 클립 내부 시간 기준으로 저장
```

---

## 15. 비동기 작업 정책

초기 운영형 구조는 Spring `@Async` 기반으로 시작할 수 있다.

단, 추후 확장을 위해 비동기 작업 서비스는 분리한다.

추후 확장 후보는 다음과 같다.

- Redis Queue
- RabbitMQ
- Kafka
- 별도 인코딩 워커 서버
- 재시도 API
- 실패 사유 DB 저장
- 썸네일 생성
- CDN 업로드

파일 생성 작업과 DB 업데이트는 실패 처리를 명확히 분리한다.

FFmpeg 실패 시 API 요청 자체를 되돌리지 않고, 클립 상태를 `FAILED`로 변경한다.

---

## 16. 예외 정책

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

## 17. 코드 작성 규칙

코드는 짧게 쓰는 것보다 가독성을 우선한다.

클래스와 메서드 이름은 역할이 드러나게 작성한다.

예시:

```java
createPlayerAnalysisClipWithDrawings()
requestPlayerClipFileGeneration()
generatePlayerAnalysisClipFile()
markPlayerClipGenerationFailed()
```

피해야 할 이름:

```java
doSave()
makeFile()
chk()
```

파일 생성, 저장, 검증, 비동기 실행 책임은 가능한 분리한다.

---

## 18. 다음 백엔드 작업 시작 시 필요한 파일

다음 작업을 시작할 때 우선 확인할 파일은 다음과 같다.

```text
PlayerAnalysisClipController.java
PlayerAnalysisClipService.java
PlayerAnalysisClipValidator.java
PlayerVideoClipEntity.java
PlayerVideoClipRepository.java
PlayerClipDrawingService.java
PlayerClipDrawingValidator.java
PlayerVideoClipDrawingEntity.java
PlayerVideoClipDrawingRepository.java
GameVideoUploadEntity.java
MatchVideoStorageService.java
LocalMatchVideoStorageService.java
FfprobeMatchVideoMetadataExtractor.java
VideoUploadStatusEnum.java
ErrorCode.java
application.properties
```

파일이 없거나 최신 코드가 불확실하면 사용자에게 먼저 요청한다.
