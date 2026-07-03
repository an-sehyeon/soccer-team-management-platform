# 24. 선수 개인 분석 클립 비동기 파일 생성 구조 요구사항

## 1. 기능 목적

선수 개인 분석 클립을 원본 경기 영상의 시간 메타데이터만으로 재생하던 기존 구조에서 벗어나, 실제 mp4 클립 파일을 생성하는 구조로 변경한다.

기존에는 `player_video_clip.url`이 `null`이고, 프론트에서 원본 경기 영상 URL과 `startTimeSec`, `endTimeSec`를 이용해 구간 재생했다.

신규 구조에서는 선수 개인 분석 클립 생성 시 원본 경기 영상에서 실제 mp4 파일을 비동기로 생성하고, 생성 완료 후 `player_video_clip.url`에 생성된 클립 파일 URL을 저장한다.

이 구조를 통해 원본 경기 영상이 소프트 삭제되어도 이미 생성된 선수 개인 분석 클립은 독립적으로 재생 가능하도록 한다.

---

## 2. 사용자 역할

### COACH

* 선수 개인 분석 클립 생성 가능
* 선수 개인 분석 클립과 드로잉 통합 생성 가능
* 선수 개인 분석 클립 조회 가능
* 선수 개인 분석 클립 수정 가능
* 선수 개인 분석 클립 삭제 가능

### ANALYST

* 선수 개인 분석 클립 생성 가능
* 선수 개인 분석 클립과 드로잉 통합 생성 가능
* 선수 개인 분석 클립 조회 가능
* 선수 개인 분석 클립 수정 가능
* 선수 개인 분석 클립 삭제 불가

### PLAYER

* 본인 선수 개인 분석 클립만 조회 가능
* 본인 선수 개인 분석 클립 드로잉 조회 가능
* 생성, 수정, 삭제 불가

---

## 3. 권한 정책

통합 생성 API는 `COACH`, `ANALYST`만 사용할 수 있다.

```http
POST /api/management/player-analysis-clips/with-drawings
```

삭제 API는 `COACH`만 사용할 수 있다.

```http
DELETE /api/coach/player-analysis-clips/{playerClipId}
```

선수는 본인 클립만 조회할 수 있다.

```http
GET /api/player/me/player-analysis-clips
GET /api/player/me/player-analysis-clips/{playerClipId}
```

권한 검증은 반드시 백엔드에서 처리한다.

---

## 4. 변경 전 구조

기존 선수 개인 분석 클립은 실제 파일 없이 다음 방식으로 재생했다.

```text
원본 경기 영상 URL + startTimeSec + endTimeSec
```

기존 구조의 특징은 다음과 같다.

* `player_video_clip.url`은 `null`
* `player_video_clip.start_time_sec`, `end_time_sec`는 원본 경기 영상 기준
* 프론트에서 원본 영상을 구간 재생
* 드로잉은 `player_video_clip_drawing` 테이블에 JSON으로 저장
* 드로잉 시간도 원본 경기 영상 기준으로 검증

---

## 5. 변경 후 구조

신규 구조에서는 선수 개인 분석 클립 생성 시 실제 mp4 클립 파일을 생성한다.

생성 흐름은 다음과 같다.

1. `COACH` 또는 `ANALYST`가 클립 생성 API를 호출한다.
2. 백엔드는 원본 경기 영상, 대상 선수, 클립 시간, 드로잉 목록을 검증한다.
3. `player_video_clip`을 `PROCESSING` 상태로 먼저 저장한다.
4. 드로잉 요청이 있으면 `player_video_clip_drawing`에 함께 저장한다.
5. 트랜잭션 커밋 후 FFmpeg 비동기 작업을 실행한다.
6. FFmpeg로 원본 경기 영상 구간을 mp4 파일로 생성한다.
7. 생성 성공 시 `player_video_clip.url`에 생성 파일 URL을 저장하고 `READY`로 변경한다.
8. 생성 실패 시 `FAILED`로 변경한다.

---

## 6. 상태 정책

`player_video_clip.status`는 다음 값을 사용한다.

| 상태           | 의미                 |
| ------------ | ------------------ |
| `PROCESSING` | 클립 파일 생성 중         |
| `READY`      | 클립 파일 생성 완료, 재생 가능 |
| `FAILED`     | 클립 파일 생성 실패        |

기존 `UPLOADING`은 기존 경기 영상 업로드 또는 기존 코드 호환을 위해 유지한다.

선수 개인 분석 클립 생성과 수정 흐름에서는 `PROCESSING`, `READY`, `FAILED`를 사용한다.

---

## 7. API 흐름

### 기존 개별 생성 API

```http
POST /api/management/player-analysis-clips
```

기존 API는 유지하되, 신규 구조에 맞춰 드로잉 없이 mp4 파일 생성 흐름을 탄다.

처리 흐름은 다음과 같다.

1. 요청 검증
2. 원본 경기 영상 조회
3. 대상 선수 검증
4. 클립 시간 검증
5. 원본 영상 실제 파일 경로 확인
6. `player_video_clip.status = PROCESSING` 저장
7. 트랜잭션 커밋 후 FFmpeg 비동기 생성 요청
8. 응답 반환

### 신규 통합 생성 API

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

클립의 `startTimeSec`, `endTimeSec`는 원본 경기 영상 기준이다.

드로잉의 `startTimeSec`, `endTimeSec`는 생성될 클립 영상 기준이다.

---

## 8. 수정 API 정책

선수 개인 분석 클립 수정 API는 기존처럼 유지한다.

```http
PATCH /api/management/player-analysis-clips/{playerClipId}
```

수정 요청에는 `matchVideoId`, `playerId`, `clipType`, `title`, `comment`, `startTimeSec`, `endTimeSec`가 포함된다.

수정 시 실제 mp4 파일도 다시 생성한다.

처리 흐름은 다음과 같다.

1. 수정 권한 검증
2. 기존 클립 조회
3. `PROCESSING` 상태 클립이면 수정 차단
4. 원본 경기 영상 조회
5. 대상 선수 검증
6. 클립 시간 검증
7. 기존 `player_video_clip.url`을 `previousClipUrl`로 보관
8. 메타데이터 수정
9. `url = null`
10. `status = PROCESSING`
11. 트랜잭션 커밋 후 FFmpeg 비동기 재생성 요청
12. 새 파일 생성 성공 시 새 URL 저장 및 `READY` 변경
13. 새 파일 생성 성공 후 기존 파일 삭제
14. 새 파일 생성 실패 시 `FAILED` 변경

기존 파일은 수정 요청 시점에 바로 삭제하지 않는다.

새 파일 생성 성공 후에만 기존 파일을 삭제한다.

---

## 9. 드로잉 정책

드로잉은 영상 파일에 합성하지 않는다.

드로잉은 `player_video_clip_drawing` 테이블에 JSON 문자열로 저장한다.

드로잉 시간 기준은 원본 경기 영상 기준이 아니라 생성된 클립 영상 기준이다.

예시:

```text
원본 영상 755초 ~ 790초를 잘라 35초 클립 생성
드로잉은 3초 ~ 8초처럼 클립 내부 시간 기준으로 저장
```

드로잉 시간 검증 기준은 다음과 같다.

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

## 10. 파일 저장 정책

선수 개인 분석 클립 파일은 로컬 개발 기준 다음 경로에 저장한다.

```text
backend/uploads/player-analysis-clips
```

DB에는 파일 자체가 아니라 브라우저 접근용 URL을 저장한다.

예시:

```text
/uploads/player-analysis-clips/player-clip-{id}-{uuid}.mp4
```

운영 전에는 `/uploads/**` 직접 접근 구조를 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.

---

## 11. FFmpeg 처리 정책

FFmpeg를 사용해 원본 경기 영상의 특정 구간을 mp4 파일로 생성한다.

기본 명령 정책은 다음과 같다.

```text
-ss {startTimeSec}
-i {originalVideoPath}
-t {durationSec}
-map 0:v:0
-map 0:a?
-c:v libx264
-preset veryfast
-c:a aac
-movflags +faststart
```

`durationSec`는 다음으로 계산한다.

```text
endTimeSec - startTimeSec
```

FFmpeg 실행 제한 시간은 초기 기준 30분으로 둔다.

---

## 12. 비동기 처리 정책

초기 구현은 Spring `@Async`를 사용한다.

비동기 작업은 별도 Bean에서 실행한다.

트랜잭션 커밋 전에 비동기 작업이 실행되면 DB row를 찾지 못할 수 있으므로, `TransactionSynchronization.afterCommit()` 이후 비동기 작업을 요청한다.

비동기 처리 흐름은 다음과 같다.

```text
Service 트랜잭션
→ player_video_clip PROCESSING 저장
→ player_video_clip_drawing 저장
→ 트랜잭션 커밋
→ @Async FFmpeg 실행
→ 성공 시 READY + url 저장
→ 성공 후 이전 파일 삭제
→ 실패 시 FAILED 저장
```

---

## 13. 조회 응답 정책

선수 개인 분석 클립 상세 응답은 생성된 클립 파일 기준으로 제공한다.

상세 응답에는 원본 경기 영상 URL을 내려주지 않는다.

상세 응답에는 원본 영상 기준 `startTimeSec`, `endTimeSec`도 내려주지 않는다.

상세 응답 주요 필드는 다음과 같다.

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
status
editorId
editorName
createdAt
updatedAt
```

목록 응답에도 원본 영상 기준 `startTimeSec`, `endTimeSec`를 내려주지 않는다.

목록 응답 주요 필드는 다음과 같다.

```text
playerClipId
matchVideoId
matchVideoTitle
playerId
playerName
clipType
title
status
editorId
editorName
createdAt
```

---

## 14. 원본 경기 영상 삭제 정책

삭제된 원본 경기 영상으로는 새 선수 개인 분석 클립을 생성할 수 없다.

하지만 이미 생성된 선수 개인 분석 클립은 원본 경기 영상이 소프트 삭제되어도 독립적으로 재생 가능해야 한다.

따라서 생성/수정 시에는 활성 원본 경기 영상을 검증한다.

상세 조회 시에는 원본 경기 영상 삭제 여부를 검증하지 않는다.

---

## 15. 예외 상황

주요 예외는 다음과 같다.

```text
MATCH_VIDEO_NOT_FOUND
MATCH_VIDEO_DURATION_NOT_READY
INVALID_CLIP_TIME_RANGE
INVALID_DRAWING_TIME_RANGE
DRAWING_TIME_OUT_OF_CLIP_RANGE
PLAYER_ANALYSIS_CLIP_NOT_FOUND
PLAYER_ANALYSIS_CLIP_PROCESSING_CANNOT_UPDATE
PLAYER_ANALYSIS_CLIP_DIRECTORY_CREATE_FAILED
PLAYER_ANALYSIS_CLIP_ORIGINAL_FILE_NOT_FOUND
PLAYER_ANALYSIS_CLIP_FILE_GENERATION_FAILED
INVALID_PLAYER_ROLE
ACCESS_DENIED
```

`PROCESSING` 상태의 클립은 수정할 수 없다.

드로잉 시간이 생성될 클립 영상 길이를 초과하면 실패한다.

원본 파일을 찾을 수 없으면 클립 생성 요청은 실패한다.

FFmpeg 작업 실패 시 API 요청 자체를 롤백하지 않고, 저장된 클립 상태를 `FAILED`로 변경한다.

---

## 16. DB 설계 방향

기존 테이블을 유지한다.

주요 테이블은 다음과 같다.

```text
player_video_clip
player_video_clip_drawing
game_video_upload
member
```

`player_video_clip.url`은 생성된 선수 개인 분석 클립 mp4 파일 URL을 저장한다.

`player_video_clip.start_time_sec`, `end_time_sec`는 원본 경기 영상 기준 생성 구간으로 DB에는 유지한다.

다만 조회 응답에서는 원본 구간 재생 혼동을 막기 위해 내려주지 않는다.

---

## 17. 구현 순서

구현 순서는 다음과 같다.

1. `@Async` 활성화 설정 추가
2. `VideoUploadStatusEnum`에 `PROCESSING` 추가
3. `application.properties`에 FFmpeg 및 선수 개인 분석 클립 저장 경로 추가
4. 통합 생성 요청/응답 DTO 추가
5. 상세/목록 응답 DTO 신규 구조에 맞게 수정
6. `PlayerVideoClipEntity` 상태 변경 메서드 추가
7. 드로잉 시간 검증 기준을 클립 영상 기준으로 수정
8. FFmpeg 파일 생성 서비스 추가
9. 로컬 파일 경로 관리 서비스 추가
10. 비동기 생성 서비스 추가
11. 생성 상태 변경 서비스 추가
12. 기존 생성 API를 비동기 파일 생성 구조로 변경
13. 통합 생성 API 추가
14. 수정 API를 파일 재생성 구조로 변경
15. 수정 성공 후 기존 파일 삭제 처리
16. API 테스트

---

## 18. 테스트 확인 항목

테스트 완료 항목은 다음과 같다.

* 서버 정상 실행
* 기존 개별 선수 개인 분석 클립 생성 API 정상 동작
* 통합 생성 API 정상 동작
* FFmpeg로 실제 mp4 파일 생성 확인
* `PROCESSING → READY` 상태 변경 확인
* 생성 실패 시 `FAILED` 처리 확인
* 드로잉 JSON 저장 확인
* 드로잉 시간 기준이 클립 영상 기준으로 검증되는지 확인
* 상세 응답에서 `playerClipUrl` 반환 확인
* 상세 응답에서 `matchVideoUrl`, `startTimeSec`, `endTimeSec` 제거 확인
* 목록 응답에서 `startTimeSec`, `endTimeSec` 제거 확인
* 수정 API 호출 시 새 파일 재생성 확인
* 수정 성공 후 기존 파일 삭제 확인
* 원본 경기 영상 삭제 여부와 상세 조회 정책 분리 확인

---

## 19. 추후 확장 가능성

추후 운영 단계에서는 다음 기능을 검토한다.

* Redis Queue 기반 비동기 작업 큐
* RabbitMQ 또는 Kafka 기반 인코딩 워커 분리
* FFmpeg 실패 사유 DB 저장
* 클립 파일 재시도 API
* 썸네일 생성
* CDN 업로드
* Signed URL 기반 재생
* 권한 검증 스트리밍 API
* 기존 파일 정리 배치
* 파일 저장소를 S3 같은 Object Storage로 전환
* 클립 생성 진행률 표시
* AI 이벤트 분석 결과와 클립 연결
