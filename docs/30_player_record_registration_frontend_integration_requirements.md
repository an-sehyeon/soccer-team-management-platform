# 30. 경기 영상 기반 선수 기록 등록 프론트 연동 요구사항 최종본

## 1. 문서 목적

이 문서는 경기 영상 화면에서 선수 요약 기록을 등록·수정하고, 팀 분석 클립 또는 선수 개인 분석 클립을 선수 기록 이벤트와 연결하는 프론트엔드 기능의 최종 구현 결과를 정리한다.

이번 작업은 경기 영상 기반 선수 기록 등록 및 클립 연결 백엔드 API 구조 개편이 `main` 브랜치에 병합된 이후 진행했다.

구현과 테스트가 완료된 범위는 다음과 같다.

* 선수 기록 조회 화면의 조회 전용 전환
* 경기 영상 화면의 선수 기록 등록 기능
* 기존 선수 기록 조회 및 생성·수정 분기
* 기록 항목별 카운터 UI
* 팀 분석 클립과 선수 기록 이벤트 연결
* 선수 개인 분석 클립과 선수 기록 이벤트 연결
* 제거된 독립 이벤트 쓰기 API와 프론트 코드 정리
* 권한 및 기존 기능 회귀 테스트

이번 작업에서는 DB, 백엔드 Entity, Repository와 백엔드 API를 변경하지 않았다.

---

## 2. 기능 목적

지도자와 분석관이 경기 영상을 확인하면서 다음 작업을 하나의 분석 화면에서 수행할 수 있도록 한다.

1. 경기와 선수를 기준으로 기존 선수 기록 조회
2. 기존 기록이 없으면 신규 선수 기록 생성
3. 기존 기록이 있으면 전체 기록값 수정
4. 팀 분석 클립을 선수 기록 이벤트에 연결
5. 선수 개인 분석 클립을 선수 기록 이벤트에 연결
6. 선수 기록과 연결 이벤트를 별도 조회 화면에서 확인

선수 기록 쓰기 기능은 `MatchVideoPage`에서만 제공한다.

`PlayerRecordPage`는 선수 기록과 연결 이벤트의 검색·목록·상세 조회 전용 화면으로 사용한다.

---

## 3. 사용자 역할

### 3.1 COACH

다음 기능을 사용할 수 있다.

* 전체 선수 기록 목록 및 상세 조회
* 경기 영상 기준 선수 기록 신규 등록
* 기존 선수 기록 수정
* 팀 분석 클립과 선수 기록 이벤트 연결
* 선수 개인 분석 클립과 선수 기록 이벤트 연결
* 선수 기록 이벤트 목록 및 상세 조회

### 3.2 ANALYST

다음 기능을 사용할 수 있다.

* 전체 선수 기록 목록 및 상세 조회
* 경기 영상 기준 선수 기록 신규 등록
* 기존 선수 기록 수정
* 팀 분석 클립과 선수 기록 이벤트 연결
* 선수 개인 분석 클립과 선수 기록 이벤트 연결
* 선수 기록 이벤트 목록 및 상세 조회

### 3.3 PLAYER

다음 기능만 사용할 수 있다.

* 본인 선수 기록 목록 및 상세 조회
* 본인 선수 기록 이벤트 목록 및 상세 조회

다음 UI는 표시하지 않는다.

* 선수 기록 등록
* 선수 기록 수정
* 팀 분석 클립 연결
* 선수 개인 분석 클립 연결
* 관리용 검색 필터

---

## 4. 권한 정책

선수 기록 등록 UI는 다음 역할에게만 표시한다.

```text
COACH
ANALYST
```

다음 역할에는 표시하지 않는다.

```text
PLAYER
```

`isAdmin = true`만으로 선수 기록 관리 UI를 표시하지 않는다.

PLAYER가 다음 주소를 직접 입력해도 등록 패널을 표시하지 않는다.

```text
/match-videos?analysisMode=player-record-create
```

프론트 권한 분기는 사용자 경험을 위한 처리다.

실제 보안은 백엔드의 역할 및 사용자 검증 결과를 기준으로 한다.

---

## 5. 화면 책임

### 5.1 PlayerRecordPage

`PlayerRecordPage`는 조회 전용 화면으로 변경했다.

유지 기능은 다음과 같다.

* 경기 영상 ID 검색
* 선수 ID 검색
* 검색 조건 초기화
* 선수 기록 목록 조회
* 페이지 이동
* 선수 기록 상세 조회
* 선수 기록 이벤트 목록 조회
* 선수 기록 이벤트 상세 조회
* 연결된 팀 분석 클립 정보 조회
* 연결된 선수 개인 분석 클립 정보 조회
* PLAYER 본인 기록 조회

제거한 기능은 다음과 같다.

* 선수 기록 등록 폼
* 선수 기록 수정 폼
* 선수 기록 삭제 UI
* 독립 선수 기록 이벤트 등록 폼
* 독립 선수 기록 이벤트 수정 폼
* 독립 선수 기록 이벤트 삭제 UI
* 이벤트 시작 시간 입력
* 이벤트 종료 시간 입력
* 이벤트 `value` 입력
* 유효 슈팅·성공 패스·성공 드리블 추가 상호 검증

### 5.2 MatchVideoPage

`MatchVideoPage`는 경기 영상 기반 분석 작업 허브 역할을 유지한다.

선수 기록 관련 기능은 다음과 같다.

* 선수 기록 등록 버튼 표시
* 클립 없이 선수 기록 등록
* 팀 분석 클립 연결 등록
* 선수 개인 분석 클립 연결 등록
* 현재 경기 기준 기존 선수 기록 조회
* 현재 경기 기준 READY 분석 클립 조회

버튼명은 다음으로 통일했다.

```text
선수 기록 등록
```

기존 문구는 제거했다.

```text
선수 기록 이벤트 등록
```

### 5.3 선수 기록 편집 패널

신규 파일은 다음과 같다.

```text
frontend/src/components/analysis/PlayerRecordEditorPanel.tsx
```

기존 독립 이벤트 중심 패널은 삭제했다.

```text
frontend/src/components/analysis/PlayerRecordEventEditorPanel.tsx
```

경기 영상이 변경되면 `matchVideoId`를 `key`로 사용하는 내부 컴포넌트가 새로 마운트되도록 구현했다.

이를 통해 `useEffect` 내부에서 다수의 상태를 동기적으로 초기화하지 않고도 이전 경기의 선수, 기록값과 선택 클립 상태가 새 경기로 유지되지 않도록 처리했다.

---

## 6. 분석 모드 정책

기존 분석 모드는 제거했다.

```text
player-record-event
```

최종 분석 모드는 다음 값을 사용한다.

```text
player-record-create
```

다음 화면 진입점을 함께 변경했다.

* `MatchVideoPage`
* `DashboardHomePage`
* `MobileHomePage`
* 경기 영상 북마크의 선수 기록 등록 버튼
* `MatchVideoAnalysisMode` 타입

선수 기록 등록은 북마크에서 진입할 수 있지만, 북마크 시간은 선수 기록 이벤트 시간으로 사용하지 않는다.

---

## 7. 등록 방식

선수 기록 편집 패널은 다음 세 가지 등록 방식을 제공한다.

```text
클립 없이 등록
팀 분석 클립 연결
선수 개인 분석 클립 연결
```

등록 방식을 변경하면 이전 방식에서 사용하던 다음 상태를 초기화한다.

* 대상 선수
* 선택 분석 클립
* 이벤트 유형
* 이벤트 메모
* 기존 기록 ID
* 기록 카운터
* 경기 전체 메모
* 오류 메시지
* 성공 메시지

저장 중에는 등록 방식 변경을 차단한다.

---

## 8. 클립 없이 선수 기록 등록

### 8.1 화면 흐름

1. 대상 선수 선택
2. 현재 경기와 대상 선수의 기존 기록 조회
3. 기존 기록이 없으면 모든 값을 0으로 표시
4. 기존 기록이 있으면 상세 기록값 표시
5. 기록 카운터를 로컬 상태에서 조정
6. 경기 전체 메모 입력
7. 최종 저장 버튼 클릭
8. 기존 기록 유무에 따라 POST 또는 PATCH 호출
9. 저장 후 같은 선수를 재선택하면 저장값 재조회

### 8.2 기존 기록 조회

다음 조건으로 선수 기록을 조회한다.

```text
uploadId = 현재 선택 경기 영상 ID
playerId = 선택한 선수 ID
```

사용 API는 다음과 같다.

```http
GET /api/management/player-records?uploadId={uploadId}&playerId={playerId}
```

조회 결과가 한 건이면 반환된 `recordId`로 상세 조회한다.

```http
GET /api/management/player-records/{recordId}
```

조회 결과가 없으면 신규 기록으로 판단한다.

동일 경기와 선수의 활성 기록이 여러 건 반환되면 신규 저장을 진행하지 않고 관리자 확인 메시지를 표시한다.

### 8.3 신규 생성

기존 기록이 없으면 다음 API를 호출한다.

```http
POST /api/management/player-records
```

### 8.4 기존 기록 수정

기존 기록이 있으면 다음 API를 호출한다.

```http
PATCH /api/management/player-records/{recordId}
```

수정 요청은 화면에 표시되는 전체 기록값을 전달한다.

---

## 9. 선수 기록 카운터

화면에 표시하는 기록 항목은 다음과 같다.

```text
출전 시간
득점
도움
슈팅
유효 슈팅
패스
성공 패스
드리블
성공 드리블
태클
인터셉트
클리어링
세이브
경고
퇴장
```

경기 전체 메모는 별도 텍스트 영역으로 제공한다.

카운터 UI는 다음 형식을 사용한다.

```text
[-] 현재 값 [+]
```

처리 규칙은 다음과 같다.

* 숫자 직접 입력을 제공하지 않는다.
* `+` 클릭 시 1 증가한다.
* `-` 클릭 시 1 감소한다.
* 최소값은 0이다.
* 최대값은 255다.
* 여러 값을 변경한 후 최종 저장 버튼으로 한 번에 저장한다.
* 저장 중에는 카운터와 저장 버튼을 비활성화한다.
* 출전 시간도 현재 단계에서는 1분 단위로 증감한다.

이번 단계에서는 다음 추가 정합성 검증을 적용하지 않는다.

```text
유효 슈팅 <= 슈팅
성공 패스 <= 패스
성공 드리블 <= 드리블
```

---

## 10. 팀 분석 클립 연결

### 10.1 화면 흐름

1. 현재 경기의 READY 팀 분석 클립 목록 조회
2. 대상 선수 선택
3. 팀 분석 클립 하나 선택
4. 이벤트 유형 하나 선택
5. 이벤트 메모 선택 입력
6. 저장 버튼 클릭
7. 클립 연결 API 호출
8. 선수 요약 기록과 이벤트 조회 화면에서 결과 확인

### 10.2 요청 API

```http
POST /api/management/player-record-events/with-clip-link
```

요청 형식은 다음과 같다.

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

다음 필드는 요청하지 않는다.

```text
eventStartTimeSec
eventEndTimeSec
value
```

현재 경기의 READY 팀 분석 클립만 프론트 선택 목록에 표시한다.

최종 경기, 상태와 권한 검증은 백엔드 결과를 기준으로 한다.

---

## 11. 선수 개인 분석 클립 연결

### 11.1 화면 흐름

1. 현재 경기의 READY 선수 개인 분석 클립 목록 조회
2. 선수 개인 분석 클립 하나 선택
3. 클립 대상 선수 읽기 전용 표시
4. 이벤트 유형 하나 선택
5. 이벤트 메모 선택 입력
6. 저장 버튼 클릭
7. 클립 연결 API 호출
8. 선수 요약 기록과 이벤트 조회 화면에서 결과 확인

### 11.2 대상 선수 정책

개인 클립 연결 방식에서는 별도의 대상 선수 드롭다운을 제공하지 않는다.

선택한 개인 분석 클립의 다음 값을 사용한다.

```text
playerId
playerName
```

`playerName`은 읽기 전용으로 표시한다.

`playerId`는 클립 연결 요청의 기록 대상 선수로 사용한다.

백엔드는 요청 선수와 실제 개인 클립 대상 선수의 일치 여부를 다시 검증한다.

### 11.3 요청 형식

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

다음 필드는 요청하지 않는다.

```text
eventStartTimeSec
eventEndTimeSec
value
```

---

## 12. 선수 기록 이벤트 유형

한 번의 클립 연결 요청에서 이벤트 유형 하나만 선택한다.

지원 유형은 다음과 같다.

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

프론트 표시 문구는 다음과 같다.

```text
GOAL → 득점
ASSIST → 도움
SHOT → 슈팅
SHOT_ON_TARGET → 유효 슈팅
PASS → 패스
SUCCESSFUL_PASS → 성공 패스
DRIBBLE → 드리블
SUCCESSFUL_DRIBBLE → 성공 드리블
TACKLE → 태클
INTERCEPTION → 인터셉트
CLEARANCE → 클리어링
SAVE → 세이브
YELLOW_CARD → 경고
RED_CARD → 퇴장
ETC → 기타
```

---

## 13. 이벤트 시간과 value 정책

다음 값은 이벤트 조회 응답에서 유지한다.

```text
eventStartTimeSec
eventEndTimeSec
value
```

해당 값은 백엔드가 저장한 이벤트 스냅샷을 조회하기 위한 용도다.

프론트 등록 요청에서는 제거했다.

```text
eventStartTimeSec → 요청하지 않음
eventEndTimeSec → 요청하지 않음
value → 요청하지 않음
```

백엔드는 선택한 클립의 시간과 `value = 1`을 저장한다.

---

## 14. 중복 연결 정책

같은 분석 클립과 같은 이벤트 유형은 다시 연결할 수 없다.

허용:

```text
클립 10 + SHOT
클립 10 + PASS
```

차단:

```text
클립 10 + SHOT
클립 10 + SHOT
```

중복 기준에는 대상 선수가 포함되지 않는다.

따라서 같은 팀 분석 클립과 같은 유형을 다른 선수에게 연결하는 요청도 차단된다.

중복 시 백엔드 `409 Conflict` 메시지를 사용자에게 표시한다.

```text
선택한 클립에는 해당 선수 기록 유형이 이미 연결되어 있습니다.
```

중복 오류가 발생하면 다음 상태를 유지한다.

* 선택 클립
* 이벤트 유형
* 이벤트 메모

중복 오류 후 다음 처리는 하지 않는다.

* 성공 메시지 표시
* 로컬 요약 기록 임의 증가
* 선택값 자동 초기화

---

## 15. API 정책

### 15.1 선수 기록 관리

```http
GET   /api/management/player-records
GET   /api/management/player-records/{recordId}
POST  /api/management/player-records
PATCH /api/management/player-records/{recordId}
```

### 15.2 클립 연결 등록

```http
POST /api/management/player-record-events/with-clip-link
```

### 15.3 관리용 이벤트 조회

```http
GET /api/management/player-records/{recordId}/events
GET /api/management/player-record-events/{eventId}
```

### 15.4 선수 본인 이벤트 조회

```http
GET /api/player/me/player-records/{recordId}/events
GET /api/player/me/player-record-events/{eventId}
```

### 15.5 제거된 독립 이벤트 쓰기 API

프론트에서 다음 API 호출을 제거했다.

```http
POST   /api/management/player-record-events
PATCH  /api/management/player-record-events/{eventId}
DELETE /api/management/player-record-events/{eventId}
```

---

## 16. 프론트 타입 및 API 정리

`playerRecordEvent.ts`의 클립 연결 요청 타입은 다음 필드만 사용한다.

```text
uploadId
playerId
eventType
eventMemo
clipSourceType
teamClipId
playerClipId
```

다음 독립 이벤트 요청 타입을 제거했다.

```text
CreatePlayerRecordEventRequest
UpdatePlayerRecordEventRequest
```

조회 응답 타입의 다음 필드는 유지했다.

```text
eventStartTimeSec
eventEndTimeSec
value
```

`playerRecordEventApi.ts`에서는 클립 연결 등록과 조회 API만 유지했다.

---

## 17. 에러 처리

백엔드 공통 에러 응답의 사용자 메시지를 화면에 표시한다.

사용자에게 다음 내부 값은 노출하지 않는다.

```text
HTTP status 숫자
내부 error code
요청 path
stack trace
내부 예외 클래스명
```

주요 오류는 다음과 같다.

### 동일 클립·동일 이벤트 유형 중복

```text
선택한 클립에는 해당 선수 기록 유형이 이미 연결되어 있습니다.
```

### READY가 아닌 클립

```text
READY 상태의 분석 클립만 선수 기록에 연결할 수 있습니다.
```

### 경기 영상 불일치

```text
선수 기록 이벤트와 연결할 클립의 경기 영상이 일치하지 않습니다.
```

### 개인 클립 대상 선수 불일치

```text
선수 기록 대상 선수와 선수 개인 분석 클립 대상 선수가 일치하지 않습니다.
```

### 잘못된 클립 출처 조합

```text
선수 기록 이벤트 클립 출처 유형이 올바르지 않습니다.
```

저장 실패 시 선택값과 메모를 유지하여 사용자가 원인을 확인하고 다시 시도할 수 있도록 처리했다.

---

## 18. 비동기 및 상태 정책

다음 비동기 상태를 분리해 관리한다.

```text
선수 목록 조회
분석 클립 목록 조회
기존 선수 기록 조회
선수 기록 저장
클립 연결 저장
```

저장 중에는 다음 동작을 차단한다.

* 등록 방식 변경
* 대상 선수 변경
* 카운터 조작
* 분석 클립 변경
* 이벤트 유형 변경
* 저장 버튼 재클릭

`useEffect`의 비동기 요청은 `ignore` 변수를 사용해 컴포넌트 언마운트 후 상태 변경과 이전 응답의 상태 덮어쓰기를 방지한다.

경기 영상 변경에 따른 전체 폼 초기화는 부모 `key` 변경 방식으로 처리한다.

---

## 19. 반응형 UI

선수 기록 입력은 지도자와 분석관의 PC 사용성을 우선한다.

다음 스타일을 `frontend/src/index.css`에 추가했다.

* 등록 방식 선택 버튼
* 활성 등록 방식 표시
* 선수 기록 카운터 그리드
* 최소 44~46px 크기의 증감 버튼
* 읽기 전용 개인 클립 대상 선수 영역
* 저장 성공·실패 메시지
* 모바일 한 열 또는 두 열 배치
* 라이트·다크 테마 공통 CSS 변수 사용

모바일에서는 영상과 입력 폼을 과도하게 가로 축소하지 않고 세로 흐름을 우선한다.

---

## 20. 주요 DB 테이블

이번 기능과 관련된 주요 테이블은 다음과 같다.

```text
player_record
player_record_event
player_record_event_clip
game_video_upload
team_video_clip
player_video_clip
member
```

이번 작업에서는 DB 스키마를 변경하지 않았다.

기존 `is_deleted` 기반 소프트 삭제 정책을 유지한다.

---

## 21. 최종 수정 및 생성 파일

### 문서

```text
docs/30_player_record_registration_frontend_integration_requirements.md
```

### 생성

```text
frontend/src/components/analysis/PlayerRecordEditorPanel.tsx
```

### 삭제

```text
frontend/src/components/analysis/PlayerRecordEventEditorPanel.tsx
```

### 수정

```text
frontend/src/api/playerRecordEventApi.ts
frontend/src/constants/routes.ts
frontend/src/index.css
frontend/src/pages/DashboardHomePage.tsx
frontend/src/pages/MatchVideoPage.tsx
frontend/src/pages/MobileHomePage.tsx
frontend/src/pages/PlayerRecordPage.tsx
frontend/src/types/playerRecordEvent.ts
```

### 변경하지 않은 파일

기존 API와 타입이 최종 백엔드 구조에 이미 맞아 추가 변경하지 않았다.

```text
frontend/src/api/playerRecordApi.ts
frontend/src/types/playerRecord.ts
frontend/src/App.tsx
frontend/src/App.css
```

---

## 22. 초기 요구사항 대비 최종 반영 내용

초기 요구사항의 핵심 정책은 변경 없이 구현했다.

구현 과정에서 실제 소스 구조를 확인해 다음 내용을 추가 반영했다.

1. 선수 기록 편집 패널의 실제 경로를 `components/analysis` 기준으로 적용
2. 기존 `PlayerRecordEventEditorPanel`을 삭제하고 `PlayerRecordEditorPanel` 신규 생성
3. 분석 모드 변경에 따라 `DashboardHomePage`와 `MobileHomePage` 진입 링크도 함께 수정
4. 실제 전역 스타일 파일인 `index.css`에 반응형 스타일 추가
5. 경기 영상 변경 시 effect 안의 동기 상태 초기화 대신 `key` 기반 remount 적용
6. `PlayerRecordPage`에 이벤트 상세 조회를 연결해 이벤트 목록과 상세를 분리 표시
7. 기존 기록 조회 결과가 여러 건인 비정상 상황에서는 저장을 차단

DB, 백엔드 API 주소와 DTO 필드는 초기 요구사항에서 변경하지 않았다.

---

## 23. 테스트 완료 결과

사용자가 다음 테스트를 실제로 정상 확인했다.

### 23.1 빌드

* `npm run build` 정상 완료
* TypeScript 오류 없음
* 사용하지 않는 import 및 변수 오류 없음
* 삭제된 API와 타입 참조 없음
* `player-record-event` 잔여 참조 없음

### 23.2 클립 없이 선수 기록 등록

* COACH 선수 기록 등록 버튼 표시
* ANALYST 선수 기록 등록 버튼 표시
* 기존 기록이 없는 선수의 모든 값 0 표시
* 신규 선수 기록 POST 저장
* 저장 후 수정 모드 전환
* 기존 선수 기록 PATCH 수정
* 선수 재선택 시 저장값 재조회
* 기록 카운터 최소 0 처리
* 기록 카운터 최대 255 처리
* 경기 전체 메모 저장 및 재조회
* `PlayerRecordPage` 기록 상세 조회
* 클립 없는 저장에서 이벤트 및 클립 연결 데이터 미생성

### 23.3 팀 분석 클립 연결

* 현재 경기 READY 팀 분석 클립 목록 표시
* 팀 분석 클립 정상 연결
* 요청 Payload에서 시간과 `value` 제거 확인
* 선택 이벤트 유형에 따른 선수 요약 기록 증가
* 이벤트 시간 스냅샷과 `value = 1` 조회
* 연결 팀 클립 제목과 유형 조회
* 같은 클립·같은 유형 `409 Conflict` 처리
* 중복 오류 메시지 표시
* 중복 후 선택 클립, 이벤트 유형과 메모 유지
* 같은 클립·다른 유형 등록 허용
* 다른 선수 대상의 같은 클립·같은 유형 중복 차단

### 23.4 선수 개인 분석 클립 연결

* 현재 경기 READY 개인 분석 클립 목록 표시
* 개인 분석 클립 정상 연결
* 개인 클립 대상 선수 읽기 전용 표시
* 요청 `playerId`와 개인 클립 대상 선수 일치
* 요청 Payload에서 시간과 `value` 제거 확인
* 선택 이벤트 유형에 따른 선수 요약 기록 증가
* 이벤트와 개인 클립 상세 조회
* 같은 클립·같은 유형 `409 Conflict` 처리
* 같은 클립·다른 유형 등록 허용
* 기존 선수 기록이 없는 선수의 자동 생성 확인

### 23.5 권한

* PLAYER 선수 기록 등록 버튼 미표시
* PLAYER 직접 분석 모드 진입 시 등록 패널 미표시
* PLAYER 관리 API 접근 백엔드 차단
* COACH 등록 가능
* ANALYST 등록 가능
* `PlayerRecordPage` PLAYER 본인 기록 조회
* 관리용 검색 UI PLAYER 미표시

### 23.6 회귀 테스트

* 경기 영상 목록 및 상세 조회
* 경기 영상 재생
* 경기 영상 북마크 목록
* 북마크 등록·수정·삭제
* 북마크 시간 이동
* 북마크 기반 팀 분석 클립 등록 진입
* 북마크 기반 선수 개인 분석 클립 등록 진입
* 북마크 기반 선수 기록 등록 진입
* 팀 분석 클립 등록·수정·조회·재생
* 선수 개인 분석 클립 등록·수정·조회·재생
* `PlayerRecordPage` 검색·목록·상세 조회
* 선수 기록 이벤트 목록·상세 조회
* 브라우저 콘솔 오류 없음
* 개발 서버 오류 없음

---

## 24. 완료 기준

다음 조건을 모두 만족해 이번 기능을 완료한 것으로 판단한다.

1. `PlayerRecordPage` 조회 전용 전환 완료
2. 독립 이벤트 등록·수정·삭제 UI 제거 완료
3. 제거된 독립 이벤트 API 호출 및 타입 제거 완료
4. 선수 기록 등록을 `MatchVideoPage`로 통합 완료
5. 기존 기록 유무에 따른 POST/PATCH 분기 완료
6. 15개 선수 기록 카운터 구현 완료
7. 팀 분석 클립 연결 완료
8. 선수 개인 분석 클립 연결 완료
9. 이벤트 시간과 `value` 요청 제거 완료
10. 동일 클립·동일 유형 중복 메시지 처리 완료
11. PLAYER 등록 UI 차단 완료
12. PC·모바일 반응형 스타일 적용 완료
13. 프론트 빌드 정상 확인 완료
14. 권한 및 주요 기능 브라우저 테스트 완료
15. 기존 북마크와 분석 클립 회귀 테스트 완료

