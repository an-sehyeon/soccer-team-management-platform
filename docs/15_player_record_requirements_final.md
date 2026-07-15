# 15. 경기 영상 기반 선수 기록 등록 및 클립 연결 최종 요구사항

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 경기 영상 기반 선수 기록 등록과 분석 클립 연결 기능에 대한 최종 요구사항을 정의한다.

이번 개편의 목적은 선수 기록 쓰기 기능을 경기 영상 화면으로 통합하고, 분석 클립과 선수 기록 이벤트를 안전하게 연결하는 것이다.

이 문서는 다음 기능의 최종 기준이다.

* 경기 영상 기준 선수 요약 기록 등록
* 기존 선수 요약 기록 갱신
* 팀 분석 클립과 선수 기록 이벤트 연결
* 선수 개인 분석 클립과 선수 기록 이벤트 연결
* 선수 기록 이벤트 조회
* 선수 본인 기록 이벤트 조회
* 선수 기록 요약 수치 자동 반영
* 동일 클립·동일 이벤트 유형 중복 방지

---

## 2. 사용자 역할

### COACH

* 선수 요약 기록 등록
* 선수 요약 기록 갱신
* 팀 분석 클립 연결 이벤트 등록
* 선수 개인 분석 클립 연결 이벤트 등록
* 전체 선수 기록 조회
* 전체 선수 기록 이벤트 조회

### ANALYST

* 선수 요약 기록 등록
* 선수 요약 기록 갱신
* 팀 분석 클립 연결 이벤트 등록
* 선수 개인 분석 클립 연결 이벤트 등록
* 전체 선수 기록 조회
* 전체 선수 기록 이벤트 조회

### PLAYER

* 본인 선수 기록 조회
* 본인 선수 기록 이벤트 목록 조회
* 본인 선수 기록 이벤트 상세 조회
* 선수 기록 등록 및 갱신 불가
* 클립 연결 이벤트 등록 불가

권한은 `memberRole`을 기준으로 검증한다.

`isAdmin = true`만으로 선수 기록 관리 권한을 부여하지 않는다.

프론트에서 버튼을 숨기더라도 권한 검증은 반드시 백엔드에서 처리한다.

---

## 3. 화면 책임

### PlayerRecordPage

`PlayerRecordPage`는 조회 전용으로 변경한다.

유지 기능:

* 검색
* 필터
* 목록 조회
* 상세 조회
* 연결 이벤트 목록 조회
* 이벤트 상세 조회
* 연결 클립 정보 조회

제거 기능:

* 선수 기록 등록 UI
* 선수 기록 수정 UI
* 선수 기록 삭제 UI
* 독립 이벤트 등록 UI
* 독립 이벤트 수정 UI
* 독립 이벤트 삭제 UI

### MatchVideoPage

선수 기록 쓰기 기능은 `MatchVideoPage`에서 제공한다.

버튼명은 다음을 사용한다.

```text
선수 기록 등록
```

등록 방식은 다음 세 가지다.

```text
클립 없이 등록
팀 분석 클립 연결
선수 개인 분석 클립 연결
```

---

## 4. 주요 테이블

```text
player_record
player_record_event
player_record_event_clip
game_video_upload
team_video_clip
player_video_clip
member
```

신규 테이블과 신규 컬럼은 추가하지 않는다.

기존 컬럼을 그대로 사용한다.

```text
player_record_event.event_start_time_sec
player_record_event.event_end_time_sec
player_record_event.value
```

삭제는 기존 `is_deleted` 기반 소프트 삭제 정책을 유지한다.

---

## 5. 선수 요약 기록 API

기존 선수 기록 API 구조를 유지한다.

```http
GET   /api/management/player-records
GET   /api/management/player-records/{recordId}
POST  /api/management/player-records
PATCH /api/management/player-records/{recordId}
```

프론트는 현재 경기와 대상 선수 기준으로 기존 기록을 먼저 조회한다.

```http
GET /api/management/player-records?uploadId={uploadId}&playerId={playerId}
```

기존 기록이 없으면 생성 API를 호출한다.

```http
POST /api/management/player-records
```

기존 기록이 있으면 조회된 `recordId`로 수정 API를 호출한다.

```http
PATCH /api/management/player-records/{recordId}
```

백엔드는 동일 경기와 동일 선수의 활성 기록 중복 생성을 계속 차단한다.

---

## 6. 클립 없이 선수 기록 등록

클립 없이 등록할 때는 `player_record`만 생성하거나 갱신한다.

다음 데이터는 생성하지 않는다.

```text
player_record_event
player_record_event_clip
```

### 신규 기록

현재 경기와 선수의 활성 `player_record`가 없으면 사용자가 입력한 전체 요약 기록으로 신규 생성한다.

초기 화면에서는 모든 수치를 0으로 표시한다.

### 기존 기록

현재 경기와 선수의 활성 `player_record`가 있으면 기존 값을 조회해 화면에 표시한다.

사용자가 여러 기록 항목을 수정한 뒤 최종 저장 버튼을 누르면 전체 값을 한 번에 갱신한다.

### 입력 항목

```text
minutesPlayed
goals
assists
shots
shotsOnTarget
passes
successfulPasses
dribbles
successfulDribbles
tackles
interceptions
clearances
saves
yellowCards
redCards
memo
```

### 수치 검증

각 수치는 다음 범위만 검증한다.

```text
최소값: 0
최대값: 255
```

현재 단계에서는 다음 추가 정합성 검증을 적용하지 않는다.

```text
shotsOnTarget <= shots
successfulPasses <= passes
successfulDribbles <= dribbles
```

---

## 7. 클립 연결 이벤트 등록 API

클립 연결 이벤트 등록은 다음 API만 사용한다.

```http
POST /api/management/player-record-events/with-clip-link
```

한 요청에서 다음을 각각 하나씩 선택한다.

* 대상 선수 한 명
* 이벤트 유형 한 개
* 팀 분석 클립 또는 선수 개인 분석 클립 한 개

하나의 요청에서 여러 이벤트 유형을 등록하지 않는다.

---

## 8. 클립 연결 요청 DTO

### 공통 필드

```text
uploadId
playerId
eventType
eventMemo
clipSourceType
teamClipId
playerClipId
```

다음 필드는 요청 DTO에서 제거한다.

```text
eventStartTimeSec
eventEndTimeSec
value
```

### 팀 분석 클립 연결 요청

```json
{
  "uploadId": 8,
  "playerId": 8,
  "eventType": "SHOT",
  "eventMemo": "팀 분석 클립 연결",
  "clipSourceType": "TEAM_ANALYSIS",
  "teamClipId": 10,
  "playerClipId": null
}
```

### 선수 개인 분석 클립 연결 요청

```json
{
  "uploadId": 8,
  "playerId": 8,
  "eventType": "SUCCESSFUL_PASS",
  "eventMemo": "선수 개인 분석 클립 연결",
  "clipSourceType": "PLAYER_ANALYSIS",
  "teamClipId": null,
  "playerClipId": 15
}
```

---

## 9. 클립 출처 조합

### TEAM_ANALYSIS

```text
clipSourceType = TEAM_ANALYSIS
teamClipId 필수
playerClipId = null
```

### PLAYER_ANALYSIS

```text
clipSourceType = PLAYER_ANALYSIS
teamClipId = null
playerClipId 필수
```

두 클립 ID가 모두 존재하거나 모두 없는 요청은 차단한다.

---

## 10. 백엔드 필수 검증

클립 연결 이벤트 등록 시 백엔드는 다음 항목을 검증한다.

1. 로그인 사용자가 `COACH` 또는 `ANALYST`인지
2. `isAdmin`만으로 관리 권한을 부여하지 않는지
3. 요청 경기 영상이 존재하는지
4. 요청 경기 영상이 소프트 삭제되지 않았는지
5. 기록 대상 회원이 존재하는지
6. 기록 대상 회원의 역할이 `PLAYER`인지
7. 선택한 클립이 존재하는지
8. 선택한 클립이 소프트 삭제되지 않았는지
9. 선택한 클립의 상태가 `READY`인지
10. 요청 경기 영상과 클립 원본 경기 영상이 일치하는지
11. `clipSourceType`과 두 클립 ID 조합이 올바른지
12. 선수 개인 분석 클립 대상 선수와 기록 대상 선수가 일치하는지
13. 같은 클립과 같은 이벤트 유형의 활성 연결이 이미 존재하는지
14. 클립 시간이 원본 경기 영상 범위를 벗어나지 않는지
15. 이벤트 메모가 DB 최대 길이를 초과하지 않는지

---

## 11. 이벤트 시간과 value 결정

클립 연결 요청에서는 이벤트 시간과 `value`를 받지 않는다.

백엔드가 선택한 클립에서 다음 값을 조회한다.

```text
eventStartTimeSec = clip.startTimeSec
eventEndTimeSec = clip.endTimeSec
value = 1
```

이 값은 `player_record_event`에 저장한다.

응답 DTO에서는 저장된 이벤트 시간과 `value`를 반환할 수 있다.

---

## 12. 이벤트 시간 스냅샷 정책

이벤트 시작·종료 시간은 이벤트 등록 당시 클립 구간의 스냅샷이다.

클립 시간이 나중에 수정되더라도 기존 이벤트의 다음 값은 변경하지 않는다.

```text
player_record_event.event_start_time_sec
player_record_event.event_end_time_sec
```

실제 영상 재생은 이벤트에 연결된 현재 팀 분석 클립 또는 선수 개인 분석 클립을 기준으로 한다.

따라서 이벤트에 저장된 시간과 현재 클립의 시간이 이후 서로 달라질 수 있다.

---

## 13. 동일 클립·동일 이벤트 유형 중복 정책

중복 기준은 다음과 같다.

```text
clipSourceType
+ 실제 teamClipId 또는 playerClipId
+ eventType
+ 활성 player_record_event
+ 활성 player_record_event_clip
```

### 허용

```text
클립 10 + SHOT
클립 10 + PASS
```

같은 클립에 서로 다른 이벤트 유형을 별도 요청으로 등록할 수 있다.

### 차단

```text
클립 10 + SHOT
클립 10 + SHOT
```

같은 클립에 같은 이벤트 유형을 다시 등록할 수 없다.

중복 기준에는 기록 대상 선수를 추가하지 않는다.

팀 분석 클립 하나에 이미 `SHOT`이 연결되어 있다면 다른 선수 대상으로 같은 팀 클립과 `SHOT`을 다시 등록하는 요청도 차단한다.

중복 시 HTTP 상태는 `409 Conflict`를 사용한다.

사용자 메시지는 다음 취지를 사용한다.

```text
선택한 클립에는 해당 선수 기록 유형이 이미 연결되어 있습니다.
```

---

## 14. 동시 중복 요청 방지

단순 중복 조회만으로는 동시에 들어온 두 요청을 모두 막을 수 없다.

선택한 팀 분석 클립 또는 선수 개인 분석 클립을 비관적 쓰기 잠금으로 조회한다.

```text
PESSIMISTIC_WRITE
```

처리 순서는 다음과 같다.

1. 클립 잠금 조회
2. 클립 상태와 관계 검증
3. 동일 클립·동일 이벤트 유형 중복 조회
4. 중복이 없을 때 이벤트 저장
5. 클립 연결 저장
6. 선수 요약 기록 갱신
7. 트랜잭션 종료 후 잠금 해제

동시에 동일한 요청이 들어오면 한 요청만 성공하고 다른 요청은 중복 오류를 반환해야 한다.

---

## 15. 기존 player_record 자동 생성

클립 연결 요청 시 현재 경기와 선수의 활성 `player_record`를 조회한다.

기존 기록이 있으면 해당 기록을 사용한다.

기존 기록이 없으면 모든 요약 수치를 0으로 생성한다.

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

이후 선택한 이벤트 유형에 해당하는 요약 수치를 반영한다.

최초 기록자는 현재 로그인한 `COACH` 또는 `ANALYST`다.

---

## 16. 이벤트 유형별 요약 기록 반영

클립 연결 이벤트의 `value`는 항상 1이다.

```text
GOAL
→ goals +1

ASSIST
→ assists +1

SHOT
→ shots +1

SHOT_ON_TARGET
→ shots +1
→ shotsOnTarget +1

PASS
→ passes +1

SUCCESSFUL_PASS
→ passes +1
→ successfulPasses +1

DRIBBLE
→ dribbles +1

SUCCESSFUL_DRIBBLE
→ dribbles +1
→ successfulDribbles +1

TACKLE
→ tackles +1

INTERCEPTION
→ interceptions +1

CLEARANCE
→ clearances +1

SAVE
→ saves +1

YELLOW_CARD
→ yellowCards +1

RED_CARD
→ redCards +1

ETC
→ 이벤트와 클립 연결은 생성
→ player_record 요약 수치는 변경하지 않음
```

요약 기록 수치가 255를 초과하면 저장하지 않고 전체 트랜잭션을 롤백한다.

---

## 17. 트랜잭션 정책

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

중간에 예외가 발생하면 다음 데이터가 모두 저장 이전 상태로 롤백되어야 한다.

```text
player_record
player_record_event
player_record_event_clip
```

요약 기록 증가 과정에서 예외가 발생해도 앞서 저장한 이벤트와 클립 연결은 DB에 남지 않아야 한다.

---

## 18. 독립 선수 기록 이벤트 쓰기 API 제거

다음 독립 쓰기 API를 제거한다.

```http
POST   /api/management/player-record-events
PATCH  /api/management/player-record-events/{eventId}
DELETE /api/management/player-record-events/{eventId}
```

다음 요청 DTO도 제거한다.

```text
CreatePlayerRecordEventRequestDTO
UpdatePlayerRecordEventRequestDTO
```

선수 기록 이벤트는 분석 클립 연결 등록 API를 통해서만 새로 생성한다.

이벤트 시간과 `value`를 임의로 수정하는 기능은 제공하지 않는다.

---

## 19. 유지하는 이벤트 API

### 관리용

```http
POST /api/management/player-record-events/with-clip-link
GET  /api/management/player-records/{recordId}/events
GET  /api/management/player-record-events/{eventId}
```

### 선수 본인

```http
GET /api/player/me/player-records/{recordId}/events
GET /api/player/me/player-record-events/{eventId}
```

선수 본인 조회는 로그인 사용자 ID와 이벤트 대상 선수 ID를 백엔드에서 비교한다.

다른 선수의 기록이나 이벤트를 조회하려는 요청은 차단한다.

---

## 20. 주요 예외

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

### 선수 개인 분석 클립 대상 선수 불일치

```text
HTTP 400 Bad Request
선수 기록 대상 선수와 선수 개인 분석 클립 대상 선수가 일치하지 않습니다.
```

### 잘못된 클립 출처 조합

```text
HTTP 400 Bad Request
선수 기록 이벤트 클립 출처 유형이 올바르지 않습니다.
```

### 권한 없음

```text
HTTP 403 Forbidden
선수 기록 이벤트 관리 권한이 없습니다.
```

---

## 21. 소프트 삭제 정책

기존 소프트 삭제 구조를 유지한다.

```text
is_deleted = false
→ 활성 데이터

is_deleted = true
→ 삭제 데이터
```

동일 클립·동일 이벤트 유형 중복 검사는 이벤트와 클립 연결이 모두 활성 상태인 경우만 중복으로 판단한다.

```text
player_record_event.is_deleted = false
player_record_event_clip.is_deleted = false
```

이번 개편에서는 독립 이벤트 삭제 API를 제거한다.

---

## 22. 사용자 확인 테스트

다음 항목을 사용자가 실제로 정상 확인했다.

### 선수 요약 기록

* 경기와 선수 기준 기존 기록 조회
* 기존 기록 신규 생성
* 기존 기록 전체 값 갱신
* 추가 상호 정합성 검증 제거
* 음수 값 차단
* 255 초과 값 차단
* 동일 경기·동일 선수 중복 생성 차단
* 클립 없이 등록 시 이벤트와 연결 데이터 미생성

### 팀 분석 클립 연결

* 정상 등록
* 클립 시간 자동 저장
* `value = 1` 자동 저장
* 선수 요약 기록 증가
* 같은 클립·같은 유형 중복 차단
* 같은 클립·다른 유형 등록 허용

### 선수 개인 분석 클립 연결

* 정상 등록
* 클립 시간 자동 저장
* `value = 1` 자동 저장
* 선수 요약 기록 증가
* 같은 클립·같은 유형 중복 차단
* 같은 클립·다른 유형 등록 허용
* 개인 클립 대상 선수 불일치 차단
* 잘못된 클립 ID 조합 차단

### 권한 및 예외

* 기존 player_record가 없을 때 자동 생성
* READY가 아닌 팀 클립 차단
* READY가 아닌 선수 개인 클립 차단
* 경기 영상 불일치 차단
* COACH 등록 허용
* ANALYST 등록 허용
* PLAYER 등록 차단
* `isAdmin = true`인 PLAYER 등록 차단
* 실패 요청 시 이벤트·연결·요약 기록 미변경

### 회귀 및 트랜잭션

* 독립 이벤트 등록 API 제거
* 독립 이벤트 수정 API 제거
* 독립 이벤트 삭제 API 제거
* 관리용 이벤트 목록·상세 조회
* 선수 본인 이벤트 목록·상세 조회
* 다른 선수 기록 접근 차단
* `SHOT_ON_TARGET` 복합 요약 반영
* `SUCCESSFUL_PASS` 복합 요약 반영
* `SUCCESSFUL_DRIBBLE` 복합 요약 반영
* `ETC` 요약 미반영
* 클립 수정 후 이벤트 시간 스냅샷 유지
* 요약 수치 최대값 초과 시 전체 롤백
* 동일 요청 동시 전송 시 한 건만 생성

---

## 23. 초기 요구사항에서 변경된 내용

초기 요구사항 이후 다음 정책이 최종 변경됐다.

1. 독립 선수 기록 이벤트 등록 API를 제거했다.
2. 독립 선수 기록 이벤트 수정 API를 제거했다.
3. 독립 선수 기록 이벤트 삭제 API를 제거했다.
4. 선수 기록 이벤트는 클립 연결 API로만 생성한다.
5. 독립 이벤트 등록·수정 요청 DTO를 삭제한다.
6. 동일 클립·동일 이벤트 유형 동시 중복을 막기 위해 클립 비관적 잠금을 적용한다.
7. 클립 없이 선수 기록 저장은 신규 upsert API를 만들지 않고 기존 조회·생성·수정 API를 조합해 처리한다.
8. 선수 기록 수치 간 추가 상호 정합성 검증을 제거하고 각 값의 0~255 범위만 검증한다.
9. DB 테이블과 컬럼은 변경하지 않는다.

---

## 24. 다음 프론트 구현 범위

백엔드 구현과 테스트가 완료됐으므로 다음 작업은 프론트 연동이다.

주요 범위:

1. `PlayerRecordPage`를 검색·목록·상세 조회 전용으로 변경
2. 등록·수정·삭제 UI 제거
3. 독립 이벤트 등록·수정·삭제 UI 제거
4. `MatchVideoPage` 버튼명을 `선수 기록 등록`으로 변경
5. 클립 없이 등록 시 기존 기록 조회
6. 기존 기록이 없으면 모든 값을 0으로 표시
7. 기록별 `-`, 현재 값, `+` 카운터 UI
8. 여러 기록을 조정한 뒤 최종 저장
9. 기존 기록 유무에 따라 `POST` 또는 `PATCH` 호출
10. 팀 분석 클립 하나와 이벤트 유형 하나 연결
11. 선수 개인 분석 클립 하나와 이벤트 유형 하나 연결
12. 이벤트 시간과 `value` 입력 UI 제거
13. 동일 클립·동일 유형 중복 메시지 표시
14. 변경된 백엔드 DTO와 프론트 타입 동기화
15. 기존 경기 영상 북마크 기능 회귀 테스트
