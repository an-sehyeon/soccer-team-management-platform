# 28. 선수 기록 프론트 연동 및 경기 영상 기반 분석 편집 통합 요구사항

## 1. 기능 목적

이 문서는 선수 기록 프론트 연동과 경기 영상 기반 분석 편집 화면 통합 작업의 최종 요구사항을 정리한다.

이번 작업의 목적은 다음과 같다.

- 지도자와 분석관이 선수 기록을 프론트에서 조회, 등록, 수정, 삭제할 수 있게 한다.
- 선수 기록 이벤트를 조회, 수정, 삭제할 수 있게 한다.
- 경기 영상 화면에서 팀 분석 클립, 선수 개인 분석 클립, 선수 기록 이벤트를 한 흐름으로 생성할 수 있게 한다.
- 기존 팀/선수 분석 클립 독립 편집 페이지를 제거하고 `/match-videos` 화면으로 등록/수정 흐름을 통합한다.
- 팀/선수 분석 클립 수정 시 기존 클립의 원본 경기 영상을 자동으로 선택하고 바로 수정할 수 있게 한다.
- 드로잉 캔버스 위치를 영상 표시 영역과 정확히 맞춘다.

---

## 2. 사용자 역할

### COACH

지도자는 다음 작업을 수행할 수 있다.

- 경기 영상 조회, 등록, 수정, 삭제
- 팀 분석 클립 조회, 등록, 수정, 삭제
- 선수 개인 분석 클립 조회, 등록, 수정, 삭제
- 선수 기록 조회, 등록, 수정, 삭제
- 선수 기록 이벤트 조회, 등록, 수정, 삭제
- 선수 기록 이벤트와 팀 분석 클립 연결
- 선수 기록 이벤트와 선수 개인 분석 클립 연결

### ANALYST

분석관은 다음 작업을 수행할 수 있다.

- 경기 영상 조회, 등록, 수정
- 팀 분석 클립 조회, 등록, 수정
- 선수 개인 분석 클립 조회, 등록, 수정
- 선수 기록 조회, 등록, 수정, 삭제
- 선수 기록 이벤트 조회, 등록, 수정, 삭제
- 선수 기록 이벤트와 팀/선수 개인 분석 클립 연결

단, 분석관은 기본적으로 영상/클립 삭제 권한은 가지지 않는다.

### PLAYER

선수는 다음 작업을 수행할 수 있다.

- 경기 영상 조회
- 팀 분석 클립 조회
- 본인 선수 개인 분석 클립 조회
- 본인 선수 기록 조회
- 본인 선수 기록 이벤트 조회
- 본인 선수 개인 분석 클립 상세 조회 시 조회 기록 생성 또는 갱신

선수에게는 등록, 수정, 삭제 UI를 노출하지 않는다.

---

## 3. 권한 정책

프론트의 권한 분기는 사용자 경험을 위한 UI 제어다.

실제 권한 검증은 반드시 백엔드에서 처리한다.

프론트 권한 노출 기준은 다음과 같다.

```text
COACH
- 등록/수정/삭제 UI 노출

ANALYST
- 등록/수정 UI 노출
- 선수 기록 기능에서는 삭제 UI 노출
- 영상/분석 클립 삭제 UI는 미노출

PLAYER
- 조회 UI만 노출
- 본인 데이터만 조회
```

---

## 4. 화면 흐름

## 4-1. 관리 대시보드

`DashboardHomePage`에서는 다음 메뉴를 제공한다.

- 스케줄 관리
- 공지사항 관리
- 경기 영상 관리
- 팀 분석 클립 관리
- 선수 개인 분석 클립 관리
- 선수 기록 관리
- 회원 승인 관리

경기 영상 영역에서는 관리자가 바로 다음 작업으로 이동할 수 있다.

```text
팀 클립 등록
→ /match-videos?analysisMode=team-clip-create

선수 클립 등록
→ /match-videos?analysisMode=player-clip-create

선수 기록 이벤트 등록
→ /match-videos?analysisMode=player-record-event
```

---

## 4-2. 모바일 홈

`MobileHomePage`에서도 주요 기능을 조회하거나 관리 화면으로 이동할 수 있다.

관리자 역할인 `COACH`, `ANALYST`는 모바일에서도 다음 분석 작업으로 이동할 수 있다.

```text
팀 클립 등록
선수 클립 등록
기록 이벤트 등록
```

선수는 조회 중심 메뉴만 사용한다.

---

## 4-3. 경기 영상 화면

핵심 화면은 다음이다.

```text
frontend/src/pages/MatchVideoPage.tsx
```

`MatchVideoPage`는 기존 경기 영상 목록/상세 화면에서 경기 영상 기반 분석 편집 허브로 확장되었다.

지원하는 분석 모드는 다음과 같다.

```text
team-clip-create
team-clip-edit
player-clip-create
player-clip-edit
player-record-event
```

URL 예시는 다음과 같다.

```text
/match-videos?analysisMode=team-clip-create
/match-videos?analysisMode=team-clip-edit&teamClipId=1
/match-videos?analysisMode=player-clip-create
/match-videos?analysisMode=player-clip-edit&playerClipId=1
/match-videos?analysisMode=player-record-event
```

일반 조회 상태에서는 다음 구조를 사용한다.

```text
경기 영상 목록 + 경기 영상 상세
```

분석 모드 상태에서는 다음 구조를 사용한다.

```text
경기 영상 목록 + 분석 편집 패널
```

분석 모드에서는 경기 영상 상세 영역을 숨기고, 같은 위치에 분석 편집 패널을 표시한다.

한 화면에 영상이 2개 표시되지 않아야 한다.

---

## 4-4. 팀 분석 클립 목록 화면

`TeamAnalysisClipPage`는 목록/상세/삭제 중심 화면이다.

등록과 수정은 더 이상 기존 독립 편집 페이지에서 처리하지 않는다.

등록 버튼은 다음 경로로 이동한다.

```text
/match-videos?analysisMode=team-clip-create
```

수정 버튼은 다음 경로로 이동한다.

```text
/match-videos?analysisMode=team-clip-edit&teamClipId={teamClipId}
```

팀 분석 클립 수정 모드에서는 `teamClipId`로 기존 클립 상세를 조회하고, 해당 클립의 `matchVideoId`로 원본 경기 영상을 자동 선택한다.

---

## 4-5. 선수 개인 분석 클립 목록 화면

`PlayerAnalysisClipPage`는 목록/상세/삭제 중심 화면이다.

등록과 수정은 더 이상 기존 독립 편집 페이지에서 처리하지 않는다.

등록 버튼은 다음 경로로 이동한다.

```text
/match-videos?analysisMode=player-clip-create
```

수정 버튼은 다음 경로로 이동한다.

```text
/match-videos?analysisMode=player-clip-edit&playerClipId={playerClipId}
```

선수 개인 분석 클립 수정 모드에서는 `playerClipId`로 기존 클립 상세를 조회하고, 해당 클립의 `matchVideoId`로 원본 경기 영상을 자동 선택한다.

---

## 4-6. 선수 기록 화면

`PlayerRecordPage`는 다음 역할을 가진다.

관리자 역할:

- 선수 기록 목록 조회
- 선수 기록 상세 조회
- 선수 기록 등록
- 선수 기록 수정
- 선수 기록 삭제
- 선수 기록 이벤트 목록 조회
- 선수 기록 이벤트 상세 조회
- 선수 기록 이벤트 수정
- 선수 기록 이벤트 삭제

선수 역할:

- 본인 선수 기록 목록 조회
- 본인 선수 기록 상세 조회
- 본인 선수 기록 이벤트 조회

선수 기록 이벤트 생성은 주로 `/match-videos?analysisMode=player-record-event`에서 수행한다.

---

## 5. API 흐름

## 5-1. 경기 영상 API

경기 영상 화면에서는 다음 API를 사용한다.

```http
GET    /api/match-videos
GET    /api/match-videos/{matchVideoId}
PATCH  /api/management/match-videos/{matchVideoId}
DELETE /api/coach/match-videos/{matchVideoId}
```

---

## 5-2. 팀 분석 클립 API

팀 분석 클립 등록/수정은 경기 영상 화면의 패널에서 수행한다.

```http
GET  /api/team-analysis-clips
GET  /api/team-analysis-clips/{teamClipId}
GET  /api/team-analysis-clips/{teamClipId}/drawings
POST /api/management/team-analysis-clips/with-drawings
PUT  /api/management/team-analysis-clips/{teamClipId}/with-drawings
```

프론트에서는 다음 API 함수를 사용한다.

```text
getTeamAnalysisClipDetail()
createTeamAnalysisClipWithDrawings()
updateTeamAnalysisClipWithDrawings()
getTeamAnalysisClipDrawings()
```

---

## 5-3. 선수 개인 분석 클립 API

선수 개인 분석 클립 등록/수정은 경기 영상 화면의 패널에서 수행한다.

```http
GET  /api/management/player-analysis-clips/{playerClipId}
GET  /api/player-analysis-clips/{playerClipId}/drawings
POST /api/management/player-analysis-clips/with-drawings
PUT  /api/management/player-analysis-clips/{playerClipId}/with-drawings
```

프론트에서는 다음 API 함수를 사용한다.

```text
getManagementPlayerAnalysisClipDetail()
createPlayerAnalysisClipWithDrawings()
updatePlayerAnalysisClipWithDrawings()
getPlayerAnalysisClipDrawings()
getManagementPlayers()
```

---

## 5-4. 선수 기록 API

선수 기록 화면에서는 다음 API를 사용한다.

```http
GET    /api/management/player-records
GET    /api/management/player-records/{recordId}
POST   /api/management/player-records
PATCH  /api/management/player-records/{recordId}
DELETE /api/management/player-records/{recordId}

GET    /api/player/me/player-records
GET    /api/player/me/player-records/{recordId}
```

---

## 5-5. 선수 기록 이벤트 API

선수 기록 이벤트에서는 다음 API를 사용한다.

```http
POST   /api/management/player-record-events
POST   /api/management/player-record-events/with-clip-link
GET    /api/management/player-records/{recordId}/events
GET    /api/management/player-record-events/{eventId}
PATCH  /api/management/player-record-events/{eventId}
DELETE /api/management/player-record-events/{eventId}

GET    /api/player/me/player-records/{recordId}/events
GET    /api/player/me/player-record-events/{eventId}
```

---

## 6. 시간 기준 정책

## 6-1. 클립 시간

팀 분석 클립과 선수 개인 분석 클립의 `startTimeSec`, `endTimeSec`는 원본 경기 영상 기준 초다.

검증 기준은 다음과 같다.

```text
startTimeSec >= 0
endTimeSec > startTimeSec
endTimeSec <= matchVideo.durationSec
```

---

## 6-2. 드로잉 시간

드로잉 시간은 생성될 클립 내부 기준 초다.

예시는 다음과 같다.

```text
원본 경기 영상 100초 ~ 115초를 잘라 15초 클립 생성
드로잉은 2초 ~ 6초처럼 클립 내부 시간 기준으로 저장
```

검증 기준은 다음과 같다.

```text
drawingStartTimeSec >= 0
drawingEndTimeSec > drawingStartTimeSec
drawingEndTimeSec <= clipDurationSec
```

---

## 6-3. 선수 기록 이벤트 시간

선수 기록 이벤트 시간은 원본 경기 영상 기준 초다.

사용 필드는 다음과 같다.

```text
eventStartTimeSec
eventEndTimeSec
```

검증 기준은 다음과 같다.

```text
eventStartTimeSec >= 0
eventEndTimeSec > eventStartTimeSec
eventEndTimeSec <= matchVideo.durationSec
```

---

## 7. 드로잉 캔버스 정책

팀 분석 드로잉과 선수 개인 분석 드로잉은 영상 위에 canvas를 absolute로 덮어 표시한다.

적용 파일은 다음과 같다.

```text
frontend/src/components/TeamAnalysisDrawingCanvas.tsx
frontend/src/components/PlayerAnalysisDrawingCanvas.tsx
```

드로잉 위치 보정을 위해 다음 기준을 적용한다.

- `ResizeObserver`로 부모 영역 크기를 감지한다.
- canvas 내부 width/height를 실제 표시 영역과 동기화한다.
- 마우스 좌표는 canvas 표시 영역 기준으로 0~1 정규화 좌표로 저장한다.
- 저장된 좌표는 다시 canvas 표시 크기에 맞춰 렌더링한다.
- `.video-canvas-wrap`은 `position: relative`를 사용한다.
- `.analysis-drawing-canvas`는 `position: absolute`, `inset: 0`을 사용한다.

필수 CSS는 다음과 같다.

```css
.video-canvas-wrap {
  position: relative;
  width: 100%;
}

.video-canvas-wrap video {
  display: block;
  width: 100%;
  height: auto;
}

.analysis-drawing-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.analysis-drawing-canvas.is-drawing {
  pointer-events: auto;
  cursor: crosshair;
}

.analysis-panel-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
```

---

## 8. 라우트 정리 정책

기존 독립 편집 페이지 라우트는 제거한다.

제거된 경로는 다음과 같다.

```text
/team-analysis-clips/new
/team-analysis-clips/:teamClipId/edit
/player-analysis-clips/new
/player-analysis-clips/:playerClipId/edit
```

제거된 파일은 다음과 같다.

```text
frontend/src/pages/TeamAnalysisClipEditorPage.tsx
frontend/src/pages/PlayerAnalysisClipEditorPage.tsx
```

등록/수정은 모두 `/match-videos`의 `analysisMode` 쿼리 파라미터로 처리한다.

---

## 9. 예외 상황

프론트에서 처리해야 하는 주요 예외는 다음과 같다.

```text
경기 영상 길이 정보가 없는 경우
→ 클립 생성/수정 불가 메시지 표시

수정하려는 클립의 원본 경기와 현재 선택한 경기 영상이 다른 경우
→ 저장 불가 메시지 표시

수정 모드에서 사용자가 다른 경기 영상을 클릭한 경우
→ 수정 모드를 종료하고 /match-videos 일반 상세 모드로 전환

드로잉 종료 시간이 클립 길이를 초과한 경우
→ 저장 전 검증 메시지 표시

선수 개인 분석 클립 연결 대상 선수와 기록 대상 선수가 다른 경우
→ 백엔드 에러 메시지 표시

PLAYER가 관리 화면 접근 또는 등록/수정/삭제 시도
→ 백엔드 권한 검증 기준으로 차단
```

---

## 10. 구현 파일

이번 작업에서 생성 또는 수정한 주요 파일은 다음과 같다.

```text
frontend/src/api/playerRecordApi.ts
frontend/src/api/playerRecordEventApi.ts
frontend/src/types/playerRecord.ts
frontend/src/types/playerRecordEvent.ts
frontend/src/pages/PlayerRecordPage.tsx

frontend/src/components/analysis/TeamAnalysisClipEditorPanel.tsx
frontend/src/components/analysis/PlayerAnalysisClipEditorPanel.tsx
frontend/src/components/analysis/PlayerRecordEventEditorPanel.tsx

frontend/src/pages/MatchVideoPage.tsx
frontend/src/pages/TeamAnalysisClipPage.tsx
frontend/src/pages/PlayerAnalysisClipPage.tsx
frontend/src/pages/DashboardHomePage.tsx
frontend/src/pages/MobileHomePage.tsx
frontend/src/pages/PlayerHomePage.tsx

frontend/src/components/TeamAnalysisDrawingCanvas.tsx
frontend/src/components/PlayerAnalysisDrawingCanvas.tsx

frontend/src/constants/routes.ts
frontend/src/App.tsx
frontend/src/index.css
frontend/src/App.css
```

삭제한 파일은 다음과 같다.

```text
frontend/src/pages/TeamAnalysisClipEditorPage.tsx
frontend/src/pages/PlayerAnalysisClipEditorPage.tsx
```

---

## 11. 테스트 기준

최종 확인해야 하는 테스트는 다음과 같다.

```text
npm run build 정상 통과

COACH
- 경기 영상 조회/수정/삭제
- 팀 분석 클립 등록/수정/삭제
- 선수 개인 분석 클립 등록/수정/삭제
- 선수 기록 조회/등록/수정/삭제
- 선수 기록 이벤트 조회/수정/삭제

ANALYST
- 경기 영상 조회/수정
- 팀 분석 클립 등록/수정
- 선수 개인 분석 클립 등록/수정
- 선수 기록 조회/등록/수정/삭제
- 영상/클립 삭제 버튼 미노출

PLAYER
- 본인 기록 조회
- 본인 기록 이벤트 조회
- 본인 개인 분석 클립 조회
- 등록/수정/삭제 UI 미노출
```

---

## 12. 추후 확장 가능성

이번 작업은 경기 영상 기반 분석 편집 흐름을 `/match-videos`에 모으는 구조다.

추후 확장 가능한 기능은 다음과 같다.

- 경기 영상 북마크
- 특정 시점 코멘트
- 선수 기록 이벤트 클릭 시 원본 경기 영상 해당 구간으로 이동
- 분석 클립과 선수 기록 이벤트 양방향 이동
- 클립 생성 실패/처리 중 상태 관리 화면
- 영상 접근 권한이 포함된 스트리밍 API 또는 Signed URL 전환
- AI 이벤트 탐지 결과를 선수 기록 이벤트로 변환
- AI 자동 클립 생성 결과를 팀/선수 분석 클립으로 저장

---

## 13. 최종 변경 사항

초기 설계 대비 변경된 내용은 다음과 같다.

- 팀 분석 클립 등록/수정 페이지를 독립 페이지에서 `/match-videos` 분석 패널로 통합했다.
- 선수 개인 분석 클립 등록/수정 페이지를 독립 페이지에서 `/match-videos` 분석 패널로 통합했다.
- 기존 `TeamAnalysisClipEditorPage.tsx`, `PlayerAnalysisClipEditorPage.tsx`를 제거했다.
- 팀/선수 클립 수정 시 클립 ID 기준으로 원본 경기 영상을 자동 선택하도록 변경했다.
- 분석 모드에서는 경기 영상 상세 영역을 분석 패널이 대체하도록 변경했다.
- 한 화면에 영상이 2개 표시되지 않도록 수정했다.
- 수정 모드에서 다른 경기 영상을 클릭하면 기존 수정 모드를 종료하도록 변경했다.
- 팀/선수 드로잉 캔버스를 `ResizeObserver` 기반으로 영상 표시 영역과 동기화했다.
- 선수 기록 이벤트 시간은 `eventStartTimeSec`, `eventEndTimeSec` 기준으로 연동한다.