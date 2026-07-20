# 00. 현재 프론트엔드 정책

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 최신 프론트엔드 정책을 요약한다.

새 채팅에서 프론트 작업을 이어서 진행할 때 이 문서를 우선 참고한다.

상세 내용은 기능별 요구사항 문서와 실제 프론트·백엔드 소스 코드를 기준으로 확인한다.

선수 기록 관련 최신 상세 문서는 다음과 같다.

```text
docs/15_player_record_requirements_final.md
docs/30_player_record_registration_frontend_integration_requirements.md
```

경기 영상 북마크 관련 최신 상세 문서는 다음과 같다.

```text
docs/29_match_video_bookmark_requirements.md
```

선수 기록 백엔드 API 개편과 경기 영상 기반 선수 기록 등록 프론트 연동은 모두 완료됐다. 현재 다음 작업은 확정되지 않았다.

---

## 2. 기술 스택

* React
* TypeScript
* Vite
* Axios
* VS Code
* 반응형 UI

선수 화면은 모바일 우선으로 설계한다.

지도자와 분석관의 영상 업로드·편집 화면은 PC 사용성을 우선한다.

---

## 3. 인증 정책

* 로그인 성공 시 JWT Access Token을 저장한다.
* Access Token은 현재 `localStorage`에 저장한다.
* API 요청은 `axiosInstance`를 사용한다.
* `axiosInstance`가 Authorization 헤더를 자동으로 추가한다.
* 새로고침 시 `GET /api/auth/me`로 로그인 상태를 복원한다.
* 401 응답 시 토큰을 제거하고 로그인 화면으로 이동한다.

```http
Authorization: Bearer {accessToken}
```

---

## 4. 라우트 정책

라우트 경로는 문자열로 직접 작성하지 않고 `ROUTES` 상수를 사용한다.

주요 경로는 다음과 같다.

```text
/login
/sign-up
/dashboard
/player
/mobile
/schedules
/notices
/match-videos
/match-videos/new
/team-analysis-clips
/player-analysis-clips
/player-records
```

팀 분석 클립과 선수 개인 분석 클립의 등록·수정은 독립 편집 라우트를 사용하지 않는다.

분석 작업은 `/match-videos`의 `analysisMode` 쿼리 파라미터로 처리한다.

현재 분석 모드는 다음과 같다.

```text
team-clip-create
team-clip-edit
player-clip-create
player-clip-edit
player-record-create
```

선수 기록 등록 모드는 다음 값으로 확정됐다.

```text
player-record-create
```

기존 `player-record-event` 값은 `routes.ts`, `MatchVideoPage.tsx`, `DashboardHomePage.tsx`, `MobileHomePage.tsx`와 북마크 진입 경로에서 모두 제거됐다.

---

## 5. 권한별 UI 정책

```text
COACH
→ 등록·수정·삭제 UI

ANALYST
→ 등록·수정 UI

PLAYER
→ 조회 UI
```

단, 북마크는 개인 임시 데이터이므로 `ANALYST`도 본인 북마크 삭제 UI를 사용한다.

선수 기록 등록 UI는 `COACH`, `ANALYST`에게만 노출한다.

`PLAYER`에게는 선수 기록 등록, 수정, 클립 연결 UI를 표시하지 않는다.

프론트 권한 분기는 사용자 경험용이며 실제 보안은 백엔드 검증을 기준으로 한다.

`isAdmin`만으로 선수 기록 관리 UI를 노출하지 않는다.

---

## 6. API 타입 정책

프론트 타입은 백엔드 DTO와 정확히 맞춘다.

다음 항목을 추측하지 않는다.

* API 주소
* 요청 필드명
* 응답 필드명
* 배열 래핑 여부
* 페이지 응답 구조
* Enum 값
* nullable 여부

백엔드 Controller, Request DTO, Response DTO를 확인한 뒤 타입을 작성한다.

선수 기록 프론트 타입과 API 함수는 최종 백엔드 DTO와 Controller 구조에 맞게 동기화됐다.

---

## 7. 에러 표시 정책

* HTTP status, error code, path를 사용자에게 그대로 노출하지 않는다.
* 백엔드 `message`가 있으면 사용자용 안내와 함께 표시한다.
* 저장 중에는 버튼을 비활성화한다.
* 중복 선수 기록 클립 연결은 다음 취지로 표시한다.

```text
선택한 클립에는 해당 선수 기록 유형이 이미 연결되어 있습니다.
```

READY 상태가 아닌 클립, 경기 영상 불일치, 개인 클립 대상 선수 불일치 등의 오류도 백엔드 메시지를 기준으로 사용자에게 표시한다.

---

## 8. React `useEffect` 정책

* effect 안에 async 함수를 따로 정의한다.
* API 응답 이후 상태를 갱신한다.
* 언마운트 후 상태 변경 방지를 위해 `ignore` 또는 `isMounted`를 사용한다.
* dependency에는 필요한 원시값을 사용한다.
* 폼 초기값은 lazy `useState`를 우선한다.
* 선택 대상 변경에 따른 폼 초기화는 부모 `key` 변경 remount 방식을 우선 검토한다.
* async 함수 호출은 필요하면 `void`를 붙여 ESLint 경고를 방지한다.
* 대상 선수나 등록 방식이 변경되면 기존 비동기 응답이 새 폼 상태를 덮어쓰지 않도록 처리한다.

---

## 9. TypeScript 정책

타입 전용 import는 `import type`을 사용한다.

```ts
import { useEffect, useState } from "react";
import type { FormEvent } from "react";
```

사용하지 않는 변수와 Props를 만들지 않는다.

특정 컴포넌트에 새 Props를 전달할 때는 해당 컴포넌트의 Props 타입과 구조 분해를 함께 수정한다.

백엔드에서 삭제된 필드는 프론트 타입과 요청 생성 코드에서도 함께 제거한다.

---

## 10. 전체 코드 제공 정책

프론트 파일 수정 요청 시 부분 코드보다 수정된 전체 파일을 제공한다.

특히 다음 파일은 전체 코드 기준으로 관리한다.

```text
MatchVideoPage.tsx
PlayerRecordPage.tsx
PlayerRecordEditorPanel.tsx
PlayerAnalysisClipEditorPanel.tsx
TeamAnalysisClipEditorPanel.tsx
VideoBookmarkSidebar.tsx
```

실제 파일명이 다르면 현재 `main` 소스 기준으로 맞춘다.

---

## 11. 파일 상단 주석 정책

프론트 파일 상단에는 파일 역할을 설명하는 한 줄 주석을 작성한다.

```ts
// 경기 영상 기준 선수 기록 등록 화면을 구성하는 패널 컴포넌트
```

---

## 12. 영상 URL 정책

백엔드 상대 URL을 API base URL과 결합해 재생 URL을 만든다.

```text
/uploads/match-videos/{storedFileName}
/uploads/team-analysis-clips/{storedFileName}
/uploads/player-analysis-clips/{storedFileName}
```

운영 전에는 권한 검증 스트리밍 API 또는 Signed URL 방식으로 전환한다.

---

## 13. 경기 영상 화면 정책

핵심 파일은 다음이다.

```text
frontend/src/pages/MatchVideoPage.tsx
```

`MatchVideoPage` 역할은 다음과 같다.

* 경기 영상 목록 조회
* 경기 영상 상세 조회
* 경기 영상 재생
* 경기 영상 메타데이터 수정
* 경기 영상 삭제
* 팀 분석 클립 등록·수정
* 선수 개인 분석 클립 등록·수정
* 영상 북마크 관리
* 선수 기록 등록
* 팀 분석 클립과 선수 기록 이벤트 연결
* 선수 개인 분석 클립과 선수 기록 이벤트 연결

분석 모드에서는 해당 작업 패널 내부에 필요한 영상 플레이어를 표시한다.

---

## 14. 영상 북마크 화면 정책

### 버튼과 권한

* `COACH`, `ANALYST`에게 `북마크 목록` 버튼을 표시한다.
* `PLAYER`에게 표시하지 않는다.
* `READY` 영상에서만 버튼을 활성화한다.

### 사이드바

* 화면 오른쪽 고정 사이드바로 표시한다.
* 페이지 아래 일반 영역에 렌더링하지 않는다.
* PC에서는 반투명 배경을 표시한다.
* 외부 클릭, 닫기 버튼, Esc 키로 닫는다.
* 모바일에서는 전체 화면 너비를 사용한다.

### 북마크 선택

북마크 선택 시 다음과 같이 처리한다.

1. 영상을 일시 정지한다.
2. 북마크 시간으로 이동한다.
3. 자동 재생하지 않는다.
4. 사이드바를 닫는다.
5. 영상 위치로 화면을 스크롤한다.
6. 선택 북마크 정보를 영상 아래에 표시한다.

### 분석 작업 연결

선택 북마크 영역에는 다음 버튼을 표시한다.

```text
팀 분석 클립 등록
선수 개인 분석 클립 등록
선수 기록 등록
```

북마크 시간은 팀·선수 분석 클립 생성 초기 시작 시간으로 사용할 수 있다.

북마크 시간은 선수 기록 이벤트 시간으로 사용하지 않는다.

선수 기록 이벤트 시간은 백엔드가 선택한 분석 클립에서 자동으로 조회한다.

---

## 15. 팀 분석 클립 편집 정책

* `MatchVideoPage` 안의 팀 분석 패널을 사용한다.
* 일반 등록은 기본 시작 0초, 종료 1초다.
* 원본 영상 북마크에서 진입하면 시작 시간은 북마크 시간이다.
* 종료 시간은 원본 경기 영상 길이로 초기화한다.
* 사용자가 구간을 다시 조정할 수 있다.
* 수정 모드에서는 기존 클립 저장 시간을 사용한다.
* 북마크 초기값이 수정 모드 값을 덮어쓰지 않는다.

---

## 16. 선수 개인 분석 클립 편집 정책

* `MatchVideoPage` 안의 선수 개인 분석 패널을 사용한다.
* 일반 등록은 기본 시작 0초, 종료 1초다.
* 원본 영상 북마크에서 진입하면 시작 시간은 북마크 시간이다.
* 종료 시간은 원본 경기 영상 길이로 초기화한다.
* 사용자가 구간을 다시 조정할 수 있다.
* 수정 모드에서는 기존 클립 저장 시간을 사용한다.
* 북마크 초기값이 수정 모드 값을 덮어쓰지 않는다.

---

## 17. `PlayerRecordPage` 최종 정책

`PlayerRecordPage`는 조회 전용으로 변경됐다.

유지 기능은 다음과 같다.

* 검색
* 필터
* 목록 조회
* 상세 조회
* 연결 이벤트 목록 조회
* 이벤트 상세 조회
* 연결 클립 정보 조회

제거 기능은 다음과 같다.

* 선수 기록 등록 폼
* 선수 기록 수정 폼
* 선수 기록 삭제 UI
* 독립 이벤트 등록 폼
* 독립 이벤트 수정 폼
* 독립 이벤트 삭제 UI

선수 기록 쓰기 기능은 `MatchVideoPage`에서만 제공한다.

백엔드에서 독립 이벤트 등록·수정·삭제 API가 제거됐으므로 프론트에서도 관련 API 호출과 UI를 모두 제거한다.

---

## 18. 선수 기록 등록 화면 정책

`MatchVideoPage`의 버튼명은 다음을 사용한다.

```text
선수 기록 등록
```

등록 방식은 다음 세 가지다.

```text
클립 없이 등록
팀 분석 클립 연결
선수 개인 분석 클립 연결
```

핵심 컴포넌트는 다음이다.

```text
frontend/src/components/analysis/PlayerRecordEditorPanel.tsx
```

기존 `PlayerRecordEventEditorPanel.tsx`는 삭제됐다.

---

## 19. 클립 없이 선수 기록 등록 정책

화면 흐름은 다음과 같다.

1. 대상 선수 선택
2. 현재 경기와 선수의 기존 기록 조회
3. 기존 기록이 있으면 기존 값 표시
4. 기존 기록이 없으면 모든 값 0 표시
5. 모든 기록 항목을 화면에 나열
6. 각 항목에 `-`, 현재 값, `+` 버튼 표시
7. 여러 항목을 한 번에 조정
8. 최종 저장 버튼으로 한 번에 저장

표시 항목은 다음과 같다.

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
경기 전체 메모
```

* 최소값은 0
* 최대값은 255
* 숫자 직접 입력은 사용하지 않는다.
* 이번 단계에서는 성공 기록과 전체 시도 수 사이의 추가 검증을 적용하지 않는다.
* 기존 `recordId`가 있으면 수정 API를 호출한다.
* 기존 기록이 없으면 생성 API를 호출한다.
* 저장 중에는 카운터와 저장 버튼을 비활성화한다.
* 대상 선수가 변경되면 기존 기록을 다시 조회하고 폼을 재구성한다.

---

## 20. 선수 기록 카운터 UI 정책

카운터는 다음 형태를 사용한다.

```text
[-] 3 [+]
```

* `+` 클릭 시 1 증가
* `-` 클릭 시 1 감소
* 최소 0
* 최대 255
* 저장 전에는 로컬 상태만 변경
* 최종 저장 버튼 클릭 시 전체 기록값을 한 번에 요청
* 저장 중 버튼 비활성화
* 터치 환경에서도 누르기 쉬운 크기로 구현

---

## 21. 선수 기록 기본 API 연동 정책

기존 기록 조회:

```http
GET /api/management/player-records?uploadId={uploadId}&playerId={playerId}
```

기록이 없는 경우:

```http
POST /api/management/player-records
```

기록이 있는 경우:

```http
PATCH /api/management/player-records/{recordId}
```

프론트는 조회 결과의 `recordId` 존재 여부에 따라 생성과 수정을 분기한다.

동일 경기·동일 선수 중복 생성 오류가 발생하면 기존 기록을 다시 조회하도록 처리할 수 있다.

---

## 22. 팀 분석 클립 연결 정책

화면 흐름은 다음과 같다.

1. 현재 경기의 `READY` 팀 분석 클립 목록 표시
2. 팀 분석 클립 하나 선택
3. 대상 선수 하나 선택
4. 이벤트 유형 하나 선택
5. 이벤트 메모 선택 입력
6. 최종 저장

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

사용자에게 다음 값을 보여주거나 입력받지 않는다.

```text
eventStartTimeSec
eventEndTimeSec
value
```

---

## 23. 선수 개인 분석 클립 연결 정책

화면 흐름은 다음과 같다.

1. 현재 경기의 `READY` 선수 개인 분석 클립 목록 표시
2. 선수 개인 분석 클립 하나 선택
3. 클립 대상 선수를 읽기 전용으로 표시
4. 이벤트 유형 하나 선택
5. 이벤트 메모 선택 입력
6. 최종 저장

요청 형식은 다음과 같다.

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

프론트에서 클립 대상 선수를 기록 대상 선수로 사용한다.

백엔드도 개인 클립 대상 선수와 요청 `playerId`의 일치 여부를 검증한다.

---

## 24. 선수 기록 이벤트 유형 정책

클립 연결 등록은 한 번에 이벤트 유형 하나만 선택한다.

같은 클립에 다른 이벤트 유형을 별도 요청으로 등록할 수 있다.

같은 클립에 같은 이벤트 유형을 다시 등록하면 백엔드 오류 메시지를 표시하고 저장하지 않는다.

주요 이벤트 유형은 다음과 같다.

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

---

## 25. 선수 기록 이벤트 API 정책

유지하는 등록 API:

```http
POST /api/management/player-record-events/with-clip-link
```

유지하는 관리용 조회 API:

```http
GET /api/management/player-records/{recordId}/events
GET /api/management/player-record-events/{eventId}
```

유지하는 선수 본인 조회 API:

```http
GET /api/player/me/player-records/{recordId}/events
GET /api/player/me/player-record-events/{eventId}
```

제거된 API:

```http
POST   /api/management/player-record-events
PATCH  /api/management/player-record-events/{eventId}
DELETE /api/management/player-record-events/{eventId}
```

프론트의 독립 이벤트 생성·수정·삭제 API 함수와 타입은 제거한다.

---

## 26. 클립 연결 요청 타입 정책

클립 연결 요청 타입에는 다음 필드만 포함한다.

```text
uploadId
playerId
eventType
eventMemo
clipSourceType
teamClipId
playerClipId
```

다음 필드는 제거한다.

```text
eventStartTimeSec
eventEndTimeSec
value
```

응답 타입에서는 백엔드가 저장한 이벤트 시간과 `value`를 조회용으로 유지할 수 있다.

---

## 27. 중복 오류 처리 정책

같은 클립과 같은 이벤트 유형 중복 시 백엔드는 다음 취지의 `409 Conflict` 응답을 반환한다.

```text
선택한 클립에는 해당 선수 기록 유형이 이미 연결되어 있습니다.
```

프론트는 다음과 같이 처리한다.

* 저장 성공 상태로 변경하지 않는다.
* 입력한 이벤트 메모와 선택값을 임의로 초기화하지 않는다.
* 백엔드 메시지를 사용자에게 표시한다.
* 중복으로 인해 요약 기록이 증가한 것처럼 로컬 상태를 변경하지 않는다.
* 필요하면 이벤트 목록과 선수 기록 상세를 다시 조회한다.

---

## 28. 반응형 정책

* 선수 조회 화면은 모바일 우선이다.
* 지도자와 분석관의 선수 기록 입력은 PC 우선이다.
* 기록 카운터는 터치 환경에서도 누르기 쉬운 크기를 사용한다.
* 클립 선택 목록은 좁은 화면에서 한 열로 배치한다.
* 영상과 입력 폼이 동시에 너무 작아지지 않도록 세로 흐름을 우선한다.
* 선수 개인 클립 대상 선수 정보는 작은 화면에서도 명확히 표시한다.

---

## 29. 백엔드 선수 기록 API 개편 완료 내용

백엔드에서 다음 작업이 완료됐다.

1. 클립 연결 요청에서 `eventStartTimeSec` 제거
2. `eventEndTimeSec` 제거
3. `value` 제거
4. 클립 시간 자동 스냅샷 저장
5. `value = 1` 고정
6. READY 상태 검증
7. 경기 영상 일치 검증
8. 개인 클립 대상 선수 일치 검증
9. 동일 클립·동일 이벤트 유형 중복 차단
10. 비관적 잠금으로 동시 중복 요청 방지
11. 기존 선수 기록이 없을 때 자동 생성
12. 선수 요약 기록 자동 증가
13. 독립 이벤트 등록·수정·삭제 API 제거
14. 관리용·선수 본인 이벤트 조회 API 유지
15. 신규 DB 테이블과 컬럼 추가 없음

---


## 30. 선수 기록 프론트 연동 완료 상태

경기 영상 기반 선수 기록 등록 프론트 연동은 완료됐다.

상세 완료 문서는 다음과 같다.

```text
docs/30_player_record_registration_frontend_integration_requirements.md
```

완료된 주요 항목은 다음과 같다.

1. `PlayerRecordPage` 조회 전용화
2. 선수 기록 등록·수정·삭제 UI 제거
3. 독립 이벤트 등록·수정·삭제 UI와 API 함수 제거
4. `MatchVideoPage`에 `PlayerRecordEditorPanel` 연결
5. 분석 모드 `player-record-create` 적용
6. 기존 선수 기록 조회 후 POST·PATCH 분기
7. 모든 기록 항목의 0~255 카운터 UI 구현
8. 팀 분석 클립 연결 등록
9. 선수 개인 분석 클립 연결 등록
10. 개인 클립 대상 선수 읽기 전용 표시
11. 클립 연결 요청에서 이벤트 시간과 `value` 제거
12. 동일 클립·동일 유형 중복 `409 Conflict` 메시지 처리
13. COACH·ANALYST 관리 UI 표시와 PLAYER 미표시
14. PC·모바일 반응형 스타일 적용
15. 대시보드·모바일 홈·북마크 진입 경로 동기화

사용자가 실제 정상 확인한 테스트는 다음과 같다.

* 프론트 빌드 정상 완료
* COACH·ANALYST 선수 기록 등록 정상
* PLAYER 등록 UI 미표시 및 관리 API 차단
* 신규 기록 POST 저장
* 기존 기록 PATCH 수정
* 선수 재선택 시 기존 값과 메모 재조회
* 기록값 최소 0, 최대 255 제한
* 클립 없이 등록 시 이벤트·클립 연결 데이터 미생성
* 팀 분석 클립 정상 연결과 요약 기록 증가
* 팀 클립 동일 유형 중복 차단 및 다른 유형 허용
* 다른 선수 대상 동일 팀 클립·동일 유형 중복 차단
* 선수 개인 분석 클립 정상 연결
* 개인 클립 대상 선수와 요청 선수 일치
* 개인 클립 동일 유형 중복 차단 및 다른 유형 허용
* 기존 기록이 없는 선수의 기록 자동 생성
* `PlayerRecordPage` 목록·상세·이벤트·연결 클립 조회
* 경기 영상 북마크 회귀 테스트
* 팀 분석 클립 회귀 테스트
* 선수 개인 분석 클립 회귀 테스트
* 브라우저와 개발 서버 콘솔 오류 없음

현재 다음 작업은 확정되지 않았다.
