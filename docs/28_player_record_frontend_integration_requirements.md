# 28. 선수 기록 관리 프론트 연동 및 선수 기록 이벤트-클립 연결 요구사항

## 1. 기능 목적

이번 기능의 목적은 기존 선수 기록 관리 기능을 확장하여, 단순 경기별 선수 요약 기록뿐 아니라 경기 영상의 특정 구간과 연결되는 개별 기록 이벤트를 관리할 수 있게 하는 것이다.

기존 `player_record`는 경기별 선수 요약 기록으로 유지한다.

새로 추가되는 `player_record_event`는 한 경기에서 특정 선수가 수행한 개별 장면 단위 기록이다.

새로 추가되는 `player_record_event_clip`은 선수 기록 이벤트와 팀 분석 클립 또는 선수 개인 분석 클립을 선택적으로 연결하는 테이블이다.

이번 작업은 백엔드 API 구현을 우선 완료하고, 프론트 구현은 백엔드 테스트 완료 후 별도 작업으로 진행한다.

---

## 2. 사용자 역할

### COACH

지도자는 선수 기록 이벤트를 등록, 조회, 수정, 삭제할 수 있다.

권한은 다음과 같다.

* 선수 기록 이벤트 등록
* 선수 기록 이벤트 + 클립 연결 등록
* 선수 기록 이벤트 목록 조회
* 선수 기록 이벤트 상세 조회
* 선수 기록 이벤트 수정
* 선수 기록 이벤트 삭제
* 선수 기록 요약 수치 자동 갱신 결과 확인

### ANALYST

분석관도 이번 선수 기록 이벤트 기능에서는 지도자와 동일하게 관리할 수 있다.

권한은 다음과 같다.

* 선수 기록 이벤트 등록
* 선수 기록 이벤트 + 클립 연결 등록
* 선수 기록 이벤트 목록 조회
* 선수 기록 이벤트 상세 조회
* 선수 기록 이벤트 수정
* 선수 기록 이벤트 삭제

분석관은 기존 영상 기능에서는 삭제 권한이 제한되는 경우가 있지만, 선수 기록 기능에서는 기존 최종 정책에 따라 삭제 가능하다.

### PLAYER

선수는 본인 기록 이벤트만 조회할 수 있다.

권한은 다음과 같다.

* 본인 선수 기록 이벤트 목록 조회
* 본인 선수 기록 이벤트 상세 조회

선수는 선수 기록 이벤트를 등록, 수정, 삭제할 수 없다.

---

## 3. 권한 정책

권한 검증은 반드시 백엔드에서 처리한다.

프론트에서 버튼을 숨기더라도 API 직접 호출이 가능하므로 Service 또는 Validator 계층에서 역할과 본인 여부를 검증한다.

기본 정책은 다음과 같다.

```text
COACH
→ 선수 기록 이벤트 등록/조회/수정/삭제 가능

ANALYST
→ 선수 기록 이벤트 등록/조회/수정/삭제 가능

PLAYER
→ 본인 player_record에 연결된 이벤트만 조회 가능
→ 등록/수정/삭제 불가
```

선수 본인 조회 기준은 다음과 같다.

```text
로그인한 PLAYER의 memberId = player_record.player_id
```

위 조건을 만족하지 않으면 접근을 차단한다.

---

## 4. 핵심 정책

### 4-1. player_record 유지 정책

기존 `player_record`는 경기별 선수 요약 기록으로 유지한다.

`player_record`는 다음 기준으로 하나의 활성 기록만 유지한다.

```text
upload_id + player_id + is_deleted = false
```

같은 경기 영상과 같은 선수에 대한 활성 `player_record`가 이미 있으면 새로 생성하지 않는다.

기록 이벤트 등록 시 기존 `player_record`가 있으면 해당 row를 재사용한다.

기존 `player_record`가 없으면 기본값 0으로 자동 생성한다.

자동 생성되는 `player_record`의 요약 수치 기본값은 다음과 같다.

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

---

### 4-2. player_record_event 저장 정책

`player_record_event`는 기록 이벤트가 등록될 때마다 새로 저장한다.

`player_record_event`에는 `upload_id`, `player_id`를 직접 저장하지 않는다.

경기 영상과 선수 정보는 다음 경로로 확인한다.

```text
player_record_event.player_record_id
→ player_record.id
→ player_record.upload_id
→ player_record.player_id
```

이벤트 단위 메모는 `player_record_event.memo`에 저장한다.

`player_record.memo`는 경기 전체 기록 메모이고, `player_record_event.memo`는 특정 장면/이벤트 메모이므로 서로 별도로 유지한다.

DTO에서는 이벤트 단위 메모를 다음 필드명으로 표현한다.

```text
eventMemo
```

---

### 4-3. 이벤트 시간 정책

초기 설계의 `eventTimeSec`는 폐기한다.

기록 이벤트는 한 순간이 아니라 영상 구간으로 관리한다.

따라서 다음 필드를 사용한다.

```text
eventStartTimeSec
eventEndTimeSec
```

DB 컬럼은 다음과 같다.

```text
event_start_time_sec
event_end_time_sec
```

의미는 다음과 같다.

```text
eventStartTimeSec
→ 원본 경기 영상 기준 이벤트 구간 시작 시간(초)

eventEndTimeSec
→ 원본 경기 영상 기준 이벤트 구간 종료 시간(초)
```

예시는 다음과 같다.

```json
{
  "eventType": "SHOT",
  "eventStartTimeSec": 125,
  "eventEndTimeSec": 135,
  "value": 1,
  "eventMemo": "페널티 박스 안 오른발 슈팅"
}
```

위 요청은 다음 의미다.

```text
원본 경기 영상 125초~135초 구간에 슈팅 장면이 있고,
선수 기록 요약 수치에는 슈팅 1개를 반영한다.
```

이벤트 시간 검증 기준은 다음과 같다.

```text
eventStartTimeSec >= 0
eventEndTimeSec > eventStartTimeSec
eventEndTimeSec <= game_video_upload.duration_sec
```

경기 영상 길이(`durationSec`)가 아직 추출되지 않은 경우 이벤트 등록/수정을 허용하지 않는다.

---

### 4-4. value 정책

`value`는 해당 이벤트를 선수 기록 요약 수치에 몇 개로 반영할지 나타내는 값이다.

초기 MVP에서는 기본적으로 `1`을 사용한다.

예시는 다음과 같다.

```json
{
  "eventType": "GOAL",
  "eventStartTimeSec": 240,
  "eventEndTimeSec": 250,
  "value": 1
}
```

의미는 다음과 같다.

```text
240초~250초 구간에 득점 이벤트가 있고,
player_record.goals를 1 증가시킨다.
```

나중에 특정 구간에서 패스 3회를 한 번에 등록해야 한다면 다음처럼 확장할 수 있다.

```json
{
  "eventType": "PASS",
  "eventStartTimeSec": 600,
  "eventEndTimeSec": 620,
  "value": 3
}
```

초기 프론트에서는 `value` 입력창을 숨기거나 기본값 1로 고정해도 된다.

---

## 5. 이벤트 유형 정책

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

잘못된 이벤트 유형이 전달되면 400 에러로 처리한다.

---

## 6. 이벤트 유형별 요약 수치 반영 정책

이벤트 생성 시 `player_record` 요약 수치를 증가시킨다.

이벤트 수정 시 기존 이벤트 수치를 차감한 뒤 수정된 이벤트 수치를 다시 반영한다.

이벤트 삭제 시 기존 이벤트 수치를 차감한다.

반영 기준은 다음과 같다.

```text
GOAL
→ goals 증가

ASSIST
→ assists 증가

SHOT
→ shots 증가

SHOT_ON_TARGET
→ shots 증가
→ shotsOnTarget 증가

PASS
→ passes 증가

SUCCESSFUL_PASS
→ passes 증가
→ successfulPasses 증가

DRIBBLE
→ dribbles 증가

SUCCESSFUL_DRIBBLE
→ dribbles 증가
→ successfulDribbles 증가

TACKLE
→ tackles 증가

INTERCEPTION
→ interceptions 증가

CLEARANCE
→ clearances 증가

SAVE
→ saves 증가

YELLOW_CARD
→ yellowCards 증가

RED_CARD
→ redCards 증가

ETC
→ 요약 수치에는 반영하지 않음
```

초기 MVP에서는 `GOAL`이 자동으로 `shots`, `shotsOnTarget`까지 증가하지 않는다.

자동 연쇄 증가는 추후 통계 정책이 확정되면 별도 기능으로 검토한다.

---

## 7. 클립 연결 정책

`player_record_event_clip`은 선택 연결 테이블이다.

기록 이벤트는 클립 없이도 존재할 수 있다.

기록 이벤트는 다음 클립과 연결할 수 있다.

```text
TEAM_ANALYSIS
→ team_video_clip 연결

PLAYER_ANALYSIS
→ player_video_clip 연결
```

클립 출처 유형은 다음 값을 사용한다.

```text
TEAM_ANALYSIS
PLAYER_ANALYSIS
```

---

### 7-1. 팀 분석 클립 연결 정책

팀 분석 클립 연결 시 기록 대상 선수는 요청에서 별도로 선택한다.

팀 분석 클립은 팀 전체 장면이므로 특정 선수와 직접 매핑되어 있지 않다.

검증 기준은 다음과 같다.

```text
player_record.upload_id = team_video_clip.upload_id
```

다른 경기 영상의 팀 분석 클립은 연결할 수 없다.

---

### 7-2. 선수 개인 분석 클립 연결 정책

선수 개인 분석 클립 연결 시 기록 대상 선수와 클립 대상 선수가 반드시 일치해야 한다.

검증 기준은 다음과 같다.

```text
player_record.upload_id = player_video_clip.upload_id
player_record.player_id = player_video_clip.player_id
```

다른 경기 영상의 선수 개인 분석 클립은 연결할 수 없다.

다른 선수의 개인 분석 클립도 연결할 수 없다.

---

## 8. 화면 흐름

이번 작업은 백엔드 API 작업만 진행한다.

프론트 구현은 백엔드 API 테스트 완료 후 별도 작업으로 진행한다.

향후 프론트 화면 흐름은 다음 방향으로 구현한다.

### 8-1. 관리 화면

지도자/분석관은 선수 기록 상세 화면에서 이벤트 목록을 확인한다.

관리 화면에서 제공할 기능은 다음과 같다.

* 선수 기록 요약 정보 조회
* 선수 기록 이벤트 목록 조회
* 이벤트 구간 확인
* 이벤트별 연결 클립 확인
* 이벤트 등록
* 이벤트 + 클립 연결 등록
* 이벤트 수정
* 이벤트 삭제

이벤트 등록 시 영상 현재 재생 시간을 기준으로 `eventStartTimeSec`, `eventEndTimeSec`를 설정할 수 있게 한다.

초기 프론트에서는 `value`를 기본값 1로 처리해도 된다.

### 8-2. 선수 화면

선수는 본인 기록 상세에서 본인 기록 이벤트만 조회한다.

선수 화면에서 제공할 기능은 다음과 같다.

* 본인 선수 기록 이벤트 목록 조회
* 본인 선수 기록 이벤트 상세 조회
* 연결 클립이 있는 경우 클립 정보 확인

선수는 이벤트 등록, 수정, 삭제를 할 수 없다.

---

## 9. API 흐름

### 9-1. 관리용 선수 기록 이벤트 등록

```http
POST /api/management/player-record-events
```

요청 예시:

```json
{
  "uploadId": 1,
  "playerId": 3,
  "eventType": "SHOT",
  "eventStartTimeSec": 125,
  "eventEndTimeSec": 135,
  "value": 1,
  "eventMemo": "페널티 박스 안 오른발 슈팅"
}
```

처리 흐름:

```text
1. COACH 또는 ANALYST 권한 확인
2. 요청값 검증
3. 경기 영상 조회
4. 경기 영상 durationSec 기준 이벤트 시간 검증
5. 대상 선수가 PLAYER인지 검증
6. 같은 uploadId, playerId의 활성 player_record 조회
7. 없으면 기본값 0으로 player_record 자동 생성
8. player_record_event 저장
9. player_record 요약 수치 증가
10. 응답 반환
```

---

### 9-2. 관리용 선수 기록 이벤트 + 클립 연결 등록

```http
POST /api/management/player-record-events/with-clip-link
```

팀 분석 클립 연결 요청 예시:

```json
{
  "uploadId": 1,
  "playerId": 3,
  "eventType": "TACKLE",
  "eventStartTimeSec": 300,
  "eventEndTimeSec": 312,
  "value": 1,
  "eventMemo": "중앙 압박 후 태클 성공",
  "clipSourceType": "TEAM_ANALYSIS",
  "teamClipId": 5,
  "playerClipId": null
}
```

선수 개인 분석 클립 연결 요청 예시:

```json
{
  "uploadId": 1,
  "playerId": 3,
  "eventType": "SUCCESSFUL_PASS",
  "eventStartTimeSec": 400,
  "eventEndTimeSec": 420,
  "value": 1,
  "eventMemo": "빌드업 구간 전진 패스 성공",
  "clipSourceType": "PLAYER_ANALYSIS",
  "teamClipId": null,
  "playerClipId": 8
}
```

처리 흐름:

```text
1. COACH 또는 ANALYST 권한 확인
2. 요청값 검증
3. 경기 영상 조회
4. 경기 영상 durationSec 기준 이벤트 시간 검증
5. 대상 선수가 PLAYER인지 검증
6. player_record 조회 또는 자동 생성
7. clipSourceType 검증
8. TEAM_ANALYSIS면 team_video_clip 조회 및 upload_id 일치 검증
9. PLAYER_ANALYSIS면 player_video_clip 조회 및 upload_id/player_id 일치 검증
10. player_record_event 저장
11. player_record_event_clip 저장
12. player_record 요약 수치 증가
13. 응답 반환
```

---

### 9-3. 관리용 선수 기록 이벤트 목록 조회

```http
GET /api/management/player-records/{recordId}/events
```

정렬 기준은 다음과 같다.

```text
eventStartTimeSec ASC
createdAt ASC
```

---

### 9-4. 관리용 선수 기록 이벤트 상세 조회

```http
GET /api/management/player-record-events/{eventId}
```

---

### 9-5. 관리용 선수 기록 이벤트 수정

```http
PATCH /api/management/player-record-events/{eventId}
```

요청 예시:

```json
{
  "eventType": "ASSIST",
  "eventStartTimeSec": 120,
  "eventEndTimeSec": 132,
  "value": 1,
  "eventMemo": "컷백 패스 도움으로 수정"
}
```

수정 정책은 다음과 같다.

```text
이벤트 유형 수정 가능
이벤트 시작/종료 시간 수정 가능
value 수정 가능
eventMemo 수정 가능
클립 연결 수정은 초기 버전에서 제외
```

수정 시 요약 수치 보정 흐름은 다음과 같다.

```text
1. 기존 이벤트 유형/value 기준으로 player_record 요약 수치 차감
2. 이벤트 내용 수정
3. 수정된 이벤트 유형/value 기준으로 player_record 요약 수치 증가
```

---

### 9-6. 관리용 선수 기록 이벤트 삭제

```http
DELETE /api/management/player-record-events/{eventId}
```

삭제 정책은 다음과 같다.

```text
player_record_event.is_deleted = true
연결된 player_record_event_clip.is_deleted = true
player_record 요약 수치 차감
```

실제 row는 삭제하지 않는다.

---

### 9-7. 선수 본인 기록 이벤트 목록 조회

```http
GET /api/player/me/player-records/{recordId}/events
```

로그인한 선수 본인의 `player_record`가 아니면 403으로 차단한다.

---

### 9-8. 선수 본인 기록 이벤트 상세 조회

```http
GET /api/player/me/player-record-events/{eventId}
```

로그인한 선수 본인의 이벤트가 아니면 403으로 차단한다.

---

## 10. 주요 요청 DTO

### CreatePlayerRecordEventRequestDTO

```text
uploadId
playerId
eventType
eventStartTimeSec
eventEndTimeSec
value
eventMemo
```

### CreatePlayerRecordEventWithClipLinkRequestDTO

```text
uploadId
playerId
eventType
eventStartTimeSec
eventEndTimeSec
value
eventMemo
clipSourceType
teamClipId
playerClipId
```

### UpdatePlayerRecordEventRequestDTO

```text
eventType
eventStartTimeSec
eventEndTimeSec
value
eventMemo
```

---

## 11. 주요 응답 DTO

### CreatePlayerRecordEventResponseDTO

```text
eventId
recordId
uploadId
matchVideoTitle
playerId
playerName
eventType
eventStartTimeSec
eventEndTimeSec
value
eventMemo
createdAt
```

### PlayerRecordEventResponseDTO

```text
eventId
recordId
uploadId
matchVideoTitle
playerId
playerName
createdById
createdByName
eventType
eventStartTimeSec
eventEndTimeSec
value
eventMemo
clips
createdAt
updatedAt
```

### PlayerRecordEventClipResponseDTO

```text
eventClipId
clipSourceType
teamClipId
teamClipTitle
playerClipId
playerClipTitle
```

### PlayerRecordEventListResponseDTO

```text
events
```

---

## 12. DB 설계 방향

### 12-1. player_record

기존 테이블을 유지한다.

역할은 경기별 선수 요약 기록이다.

`upload_id`, `player_id` 기준 활성 기록은 1개만 유지한다.

이번 기능에서 이벤트 생성/수정/삭제 시 요약 수치를 자동 보정한다.

---

### 12-2. player_record_event

신규 테이블이다.

역할은 경기 영상 구간 기준 개별 선수 기록 이벤트 저장이다.

주요 컬럼은 다음과 같다.

```text
id
player_record_id
event_type
event_start_time_sec
event_end_time_sec
value
memo
created_by
is_deleted
created_at
updated_at
```

`upload_id`, `player_id`는 직접 저장하지 않는다.

---

### 12-3. player_record_event_clip

신규 테이블이다.

역할은 선수 기록 이벤트와 팀 분석 클립 또는 선수 개인 분석 클립의 선택 연결이다.

주요 컬럼은 다음과 같다.

```text
id
player_record_event_id
clip_source_type
team_clip_id
player_clip_id
is_deleted
created_at
updated_at
```

`clip_source_type = TEAM_ANALYSIS`이면 `team_clip_id`를 사용한다.

`clip_source_type = PLAYER_ANALYSIS`이면 `player_clip_id`를 사용한다.

---

## 13. 예외 상황

추가 또는 사용하는 주요 예외는 다음과 같다.

```text
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
MATCH_VIDEO_DURATION_NOT_READY
```

예외 기준은 다음과 같다.

```text
이벤트가 없거나 삭제됨
→ PLAYER_RECORD_EVENT_NOT_FOUND

PLAYER가 타인 기록 이벤트 조회
→ PLAYER_RECORD_EVENT_ACCESS_DENIED

PLAYER가 관리 API 호출
→ PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED

eventType이 잘못됨
→ INVALID_PLAYER_RECORD_EVENT_TYPE

clipSourceType이 잘못됨
→ INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE

eventStartTimeSec/eventEndTimeSec가 잘못됨
→ INVALID_PLAYER_RECORD_EVENT_TIME

eventEndTimeSec가 경기 영상 durationSec을 초과함
→ INVALID_PLAYER_RECORD_EVENT_TIME

경기 영상 durationSec이 없음
→ MATCH_VIDEO_DURATION_NOT_READY

value가 null, 1 미만, 255 초과
→ INVALID_PLAYER_RECORD_EVENT_VALUE

클립의 경기 영상과 기록 대상 경기 영상이 다름
→ PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH

선수 개인 분석 클립 대상 선수와 기록 대상 선수가 다름
→ PLAYER_RECORD_EVENT_PLAYER_MISMATCH
```

---

## 14. 구현 완료 기준

이번 백엔드 구현 완료 기준은 다음과 같다.

```text
COACH가 클립 없는 기록 이벤트 등록 가능
ANALYST가 클립 없는 기록 이벤트 등록 가능
기존 player_record가 없으면 자동 생성
기존 player_record가 있으면 재사용
이벤트 등록 시 player_record 요약 수치 증가
팀 분석 클립 연결 이벤트 등록 가능
선수 개인 분석 클립 연결 이벤트 등록 가능
선수 개인 분석 클립 대상 선수 불일치 시 실패
클립 경기 영상 불일치 시 실패
원본 경기 영상 durationSec 초과 이벤트 시간 저장 실패
이벤트 목록 조회 가능
이벤트 상세 조회 가능
이벤트 수정 시 요약 수치 보정
이벤트 삭제 시 이벤트/연결 클립 소프트 삭제 및 요약 수치 차감
PLAYER는 본인 이벤트 목록/상세만 조회 가능
PLAYER는 타인 이벤트 조회 불가
PLAYER는 관리 API 호출 불가
```

---

## 15. 구현 순서

백엔드 구현 순서는 다음과 같다.

```text
1. PlayerRecordEventTypeEnum 추가
2. PlayerRecordClipSourceTypeEnum 추가
3. PlayerRecordEventEntity 추가
4. PlayerRecordEventClipEntity 추가
5. PlayerRecordEventRepository 추가
6. PlayerRecordEventClipRepository 추가
7. PlayerRecordRepository에 활성 기록 단건 조회 메서드 추가
8. 선수 기록 이벤트 DTO 추가
9. ErrorCode 추가
10. PlayerRecordEventValidator 추가
11. PlayerRecordEventService 추가
12. PlayerRecordEventController 추가
13. 서버 실행 및 JPA 테이블 생성 확인
14. Postman/API 테스트
15. soccer_platform.sql 반영
16. 최종 문서 갱신
```

---

## 16. 테스트 결과

백엔드 테스트 결과 정상 확인했다.

확인된 항목은 다음과 같다.

```text
서버 정상 실행
player_record_event 테이블 정상 생성
player_record_event_clip 테이블 정상 생성
클립 없는 이벤트 등록 정상
이벤트 + 팀 분석 클립 연결 등록 정상
이벤트 + 선수 개인 분석 클립 연결 등록 정상
관리용 이벤트 목록 조회 정상
관리용 이벤트 상세 조회 정상
이벤트 수정 정상
이벤트 삭제 정상
선수 본인 이벤트 조회 정상
원본 영상 길이 기준 이벤트 시간 검증 정상
```

---

## 17. 추후 확장 가능성

추후 확장 가능한 기능은 다음과 같다.

```text
프론트에서 영상 현재 시간 기준 이벤트 구간 선택
이벤트 구간 클릭 시 원본 경기 영상 해당 구간 재생
이벤트와 연결된 팀/개인 분석 클립 바로 재생
선수 기록 이벤트 타임라인 UI
이벤트 유형별 필터
경기별 선수 이벤트 히트맵
AI 이벤트 자동 생성
AI 이벤트와 수동 이벤트 비교
이벤트 기반 선수 기록 자동 통계화
클립 연결 수정 API
이벤트별 코치 피드백
선수 피드백 확인 여부
```

초기 버전에서는 AI 기능을 무리하게 구현하지 않고, 수동 기록 이벤트와 영상 구간 연결의 안정성을 우선한다.