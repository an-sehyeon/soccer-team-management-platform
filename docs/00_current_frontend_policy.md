# 00. 현재 프론트엔드 정책

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 프론트엔드 최신 정책을 요약한다.

새 채팅에서 프론트 작업을 이어서 진행할 때 이 문서를 우선 참고한다.

상세 내용은 기능별 프론트 연동 요구사항 문서와 실제 소스 코드를 기준으로 확인한다.

---

## 2. 프론트 기술 스택

- React
- TypeScript
- Vite
- Axios
- VS Code
- 반응형 UI

선수 화면은 모바일 사용성을 우선한다.

지도자와 분석관의 영상 업로드/편집 화면은 PC 사용성을 우선한다.

---

## 3. 인증 정책

프론트 인증은 기존 로그인/JWT 구조를 사용한다.

기본 정책은 다음과 같다.

- 로그인 성공 시 Access Token을 저장한다.
- Access Token은 `localStorage`에 저장한다.
- API 요청은 `axiosInstance`를 사용한다.
- `axiosInstance`에서 Authorization 헤더를 자동으로 붙인다.
- 새로고침 시 `GET /api/auth/me`로 로그인 상태를 복원한다.
- 401 응답이 발생하면 저장된 토큰을 제거하고 로그인 화면으로 이동한다.

Authorization 헤더 형식은 다음과 같다.

```http
Authorization: Bearer {accessToken}
```

---

## 4. 라우트 정책

라우트 경로는 문자열로 직접 작성하지 않고 `ROUTES` 상수를 사용한다.

현재 주요 경로는 다음과 같다.

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

팀 분석 클립과 선수 개인 분석 클립의 등록/수정은 더 이상 독립 라우트를 사용하지 않는다.

제거된 기존 경로는 다음과 같다.

```text
/team-analysis-clips/new
/team-analysis-clips/:teamClipId/edit
/player-analysis-clips/new
/player-analysis-clips/:playerClipId/edit
```

분석 작업은 `/match-videos`에서 `analysisMode` 쿼리 파라미터로 처리한다.

```text
/match-videos?analysisMode=team-clip-create
/match-videos?analysisMode=team-clip-edit&teamClipId={teamClipId}
/match-videos?analysisMode=player-clip-create
/match-videos?analysisMode=player-clip-edit&playerClipId={playerClipId}
/match-videos?analysisMode=player-record-event
```

관리자/지도자/분석관 PC 화면은 대시보드 중심으로 진입한다.

선수 PC 화면은 선수 홈 중심으로 진입한다.

모바일/태블릿은 공통 모바일 홈을 사용한다.

---

## 5. 권한별 UI 노출 정책

프론트 권한 처리는 사용자 경험을 위한 버튼/폼 노출 제어다.

실제 권한 검증은 반드시 백엔드에서 처리한다.

공통 기준은 다음과 같다.

```text
COACH: 등록/수정/삭제 UI 노출
ANALYST: 등록/수정 UI 노출
PLAYER: 조회 UI만 노출
```

단, 선수 기록 관련 기능에서는 기존 최종 정책상 `ANALYST`도 기록 삭제 UI를 볼 수 있다.

영상/분석 클립 삭제 UI는 `COACH`에게만 노출한다.

프론트에서 버튼이 보이지 않아도 사용자가 API를 직접 호출할 수 있으므로, 프론트 권한 분기를 보안으로 간주하지 않는다.

---

## 6. API 타입 정책

프론트 타입은 백엔드 DTO와 정확히 맞춘다.

다음은 임의로 추측하지 않는다.

- API 주소
- 요청 필드명
- 응답 필드명
- 응답 배열 래핑 여부
- Enum 값
- 페이지 응답 필드명

백엔드 Controller, Request DTO, Response DTO를 확인한 뒤 타입을 작성한다.

사용자가 현재 로컬 파일과 GitHub `main`이 동일하다고 명확히 말한 경우에는 GitHub `main` 기준으로 실제 코드 구조를 확인한 뒤 타입과 API를 작성할 수 있다.

---

## 7. 에러 메시지 표시 정책

사용자 화면에는 HTTP status, error code, path를 그대로 노출하지 않는다.

백엔드가 내려준 `message`가 있으면 fallback 메시지와 함께 보여준다.

클립 생성/수정 요청처럼 즉시 결과 확인이 필요한 작업은 `alert()` 또는 성공/실패 메시지 영역으로 안내할 수 있다.

---

## 8. React useEffect 작성 정책

React Compiler 경고를 방지하기 위해 `useEffect` 안에서 동기적으로 `setState`를 반복 호출하지 않는다.

API 호출이 필요한 effect는 다음 기준을 따른다.

- effect 내부에 async 함수를 따로 정의한다.
- API 응답 이후에만 상태를 갱신한다.
- 컴포넌트 언마운트 후 상태 변경을 막기 위해 `ignore` 또는 `isMounted` 플래그를 사용한다.
- dependency에는 객체 전체가 아니라 필요한 원시값을 넣는다.
- effect 안에서 즉시 `setState`를 호출해 렌더링 연쇄를 만들지 않는다.
- 폼 초기값은 가능하면 lazy `useState(() => initialState)`로 만든다.
- 선택 경기 영상 변경에 따른 폼 초기화는 부모에서 `key`를 변경해 remount하는 방식을 우선한다.

---

## 9. TypeScript import 정책

타입 전용 import는 반드시 `import type`을 사용한다.

예시는 다음과 같다.

```ts
import { useEffect, useState } from "react";
import type { FormEvent } from "react";
```

일반 import에 타입을 섞어 ESLint 또는 빌드 경고가 생기지 않도록 한다.

---

## 10. 전체 코드 제공 규칙

사용자가 에러를 보고하거나 특정 파일 수정이 필요한 경우, 수정된 파일의 전체 코드를 제공한다.

특히 프론트 작업에서는 부분 코드만 제공하지 않는다.

대상 예시는 다음과 같다.

```text
MatchVideoPage.tsx
PlayerRecordPage.tsx
PlayerAnalysisClipPage.tsx
TeamAnalysisClipPage.tsx
DashboardHomePage.tsx
MobileHomePage.tsx
PlayerHomePage.tsx
```

---

## 11. 사용하지 않는 변수 금지

TypeScript/ESLint 경고를 방지하기 위해 사용하지 않는 변수는 만들지 않는다.

권한 분기가 필요한 경우 실제로 사용하는 값만 만든다.

---

## 12. 파일 상단 주석 정책

프론트 파일 상단에는 해당 파일 역할을 설명하는 한 줄 주석을 작성한다.

예시:

```ts
// 경기 영상 기반 분석 작업 화면을 구성하는 페이지 컴포넌트
```

---

## 13. 영상 URL 처리 정책

현재 프론트는 백엔드가 상대 경로를 내려주면 API base URL과 결합해 재생 URL을 만든다.

예시는 다음과 같다.

```text
/uploads/match-videos/{storedFileName}
/uploads/team-analysis-clips/{storedFileName}
/uploads/player-analysis-clips/{storedFileName}
```

운영 전에는 반드시 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.

현재 MVP에서 `/uploads/**`를 직접 재생하는 구조는 개발 편의용이다.

---

## 14. 경기 영상 화면 정책

핵심 화면은 다음이다.

```text
frontend/src/pages/MatchVideoPage.tsx
```

`MatchVideoPage`는 다음 역할을 가진다.

- 경기 영상 목록 조회
- 경기 영상 상세 조회
- 경기 영상 메타데이터 수정
- 경기 영상 삭제
- 팀 분석 클립 등록
- 팀 분석 클립 수정
- 선수 개인 분석 클립 등록
- 선수 개인 분석 클립 수정
- 선수 기록 이벤트 등록
- 선수 기록 이벤트와 분석 클립 연결

분석 모드가 아닌 경우 화면 구조는 다음과 같다.

```text
경기 영상 목록 + 경기 영상 상세
```

분석 모드인 경우 화면 구조는 다음과 같다.

```text
경기 영상 목록 + 분석 편집 패널
```

분석 모드에서는 경기 영상 상세 영역을 숨기고, 같은 위치에 분석 편집 패널을 표시한다.

한 페이지에 원본 영상과 편집 패널 영상이 동시에 2개 표시되지 않도록 한다.

수정 모드에서 사용자가 상단 경기 영상 목록의 다른 경기를 클릭하면 수정 모드를 종료하고 `/match-videos` 일반 상세 모드로 전환한다.

---

## 15. 팀 분석 클립 프론트 정책

팀 분석 클립 화면은 목록/상세/삭제 중심 화면이다.

경로는 다음만 사용한다.

```text
/team-analysis-clips
```

팀 분석 클립 등록은 다음 경로로 이동한다.

```text
/match-videos?analysisMode=team-clip-create
```

팀 분석 클립 수정은 다음 경로로 이동한다.

```text
/match-videos?analysisMode=team-clip-edit&teamClipId={teamClipId}
```

수정 모드에서는 `teamClipId`로 팀 분석 클립 상세를 조회한 뒤, 해당 클립의 `matchVideoId`로 원본 경기 영상을 자동 선택한다.

팀 분석 클립 등록/수정 패널은 다음 파일에서 관리한다.

```text
frontend/src/components/analysis/TeamAnalysisClipEditorPanel.tsx
```

기존 독립 편집 페이지는 제거되었다.

```text
frontend/src/pages/TeamAnalysisClipEditorPage.tsx
```

---

## 16. 선수 개인 분석 클립 프론트 정책

선수 개인 분석 클립 화면은 목록/상세/삭제 중심 화면이다.

경로는 다음만 사용한다.

```text
/player-analysis-clips
```

선수 개인 분석 클립 등록은 다음 경로로 이동한다.

```text
/match-videos?analysisMode=player-clip-create
```

선수 개인 분석 클립 수정은 다음 경로로 이동한다.

```text
/match-videos?analysisMode=player-clip-edit&playerClipId={playerClipId}
```

수정 모드에서는 `playerClipId`로 선수 개인 분석 클립 상세를 조회한 뒤, 해당 클립의 `matchVideoId`로 원본 경기 영상을 자동 선택한다.

선수 개인 분석 클립 등록/수정 패널은 다음 파일에서 관리한다.

```text
frontend/src/components/analysis/PlayerAnalysisClipEditorPanel.tsx
```

기존 독립 편집 페이지는 제거되었다.

```text
frontend/src/pages/PlayerAnalysisClipEditorPage.tsx
```

상세 재생은 `playerClipUrl` 기준으로 처리한다.

드로잉 시간 기준은 생성된 선수 개인 분석 클립 영상 기준 초다.

---

## 17. 드로잉 캔버스 정책

팀 분석 클립과 선수 개인 분석 클립의 드로잉 캔버스는 영상 표시 영역 위에 정확히 겹쳐야 한다.

대상 파일은 다음과 같다.

```text
frontend/src/components/TeamAnalysisDrawingCanvas.tsx
frontend/src/components/PlayerAnalysisDrawingCanvas.tsx
```

정책은 다음과 같다.

- canvas는 영상과 같은 부모 영역 안에서 absolute로 배치한다.
- `ResizeObserver`로 부모 영역 크기를 감지한다.
- canvas 내부 `width`, `height`는 실제 표시 크기와 동기화한다.
- 드로잉 좌표는 0~1 정규화 좌표로 저장한다.
- 저장된 좌표는 현재 canvas 크기에 맞춰 다시 렌더링한다.
- 드로잉 모드가 아닐 때는 canvas pointer event를 막는다.
- 드로잉 모드일 때만 pointer event를 활성화한다.

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

## 18. 선수 개인 분석 클립 조회 기록 프론트 정책

선수 개인 분석 클립 조회 기록은 `/player-analysis-clips` 상세 영역에서 표시한다.

별도 조회 기록 페이지는 현재 만들지 않는다.

조회 기록 UI는 다음 사용자에게만 노출한다.

```text
COACH
ANALYST
```

PLAYER에게는 조회 기록 관리 UI를 노출하지 않는다.

조회 기록에는 다음 정보를 표시한다.

- 조회 선수
- 최초 확인 시간
- 마지막 확인 시간
- 대상 선수 조회 횟수

---

## 19. 선수 기록 프론트 정책

선수 기록 조회 화면은 다음 경로를 사용한다.

```text
/player-records
```

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

선수 기록 이벤트 생성의 주 흐름은 `/match-videos?analysisMode=player-record-event`다.

선수 기록 이벤트 시간은 원본 경기 영상 기준으로 처리한다.

```text
eventStartTimeSec
eventEndTimeSec
```

---

## 20. 선수 기록 이벤트와 분석 클립 연결 프론트 정책

선수 기록 이벤트는 클립 없이도 등록할 수 있다.

선택적으로 연결 가능한 클립 유형은 다음과 같다.

```text
TEAM_ANALYSIS
PLAYER_ANALYSIS
```

팀 분석 클립 연결 시에는 같은 경기 영상의 팀 분석 클립만 선택 가능해야 한다.

선수 개인 분석 클립 연결 시에는 같은 경기 영상이면서 대상 선수가 같은 클립만 선택 가능해야 한다.

최종 검증은 백엔드에서 처리한다.

프론트는 사용자 실수 방지를 위해 선택 가능한 클립 목록을 필터링한다.

---

## 21. CORS 관련 프론트 주의사항

`PUT`, `PATCH`, `DELETE` 요청은 브라우저에서 preflight `OPTIONS` 요청이 발생한다.

프론트에서 CORS 에러가 나면 백엔드 CORS 설정을 먼저 확인한다.

백엔드에서는 다음 설정이 필요하다.

```text
allowedOrigins: http://localhost:5173
allowedMethods: GET, POST, PUT, PATCH, DELETE, OPTIONS
allowedHeaders: Authorization, Content-Type
OPTIONS /** permitAll
```

프론트에서는 실제 요청에 Authorization 헤더가 정상 포함되는지 확인한다.

---

## 22. 기능 완료 후 문서 갱신 규칙

프론트 기능 작업이 완료되면 사용자가 따로 요청하지 않아도 다음을 제공한다.

1. 변경 내용이 반영된 최종 프론트 요구사항 문서
2. 생성/수정/삭제 파일 기준 커밋 메시지
3. GitHub PR 제목과 내용
4. 다음 작업 GitHub 이슈
5. 다음 새 채팅 시작 프롬프트
6. 다음 작업 기준으로 갱신된 `00_project_context_for_chatgpt.md`
7. 필요하면 `00_current_backend_policy.md`, `00_current_frontend_policy.md` 갱신 내용

---

## 23. 다음 프론트 작업

다음 프론트 작업 후보는 다음이다.

```text
경기 영상 북마크 기능 구현
```

이 기능은 경기 장면을 빠르게 찾기 위한 편집 편의 기능이다.

우선 확인할 파일은 다음과 같다.

```text
frontend/src/pages/MatchVideoPage.tsx
frontend/src/api/matchVideoApi.ts
frontend/src/types/matchVideo.ts
frontend/src/constants/routes.ts
frontend/src/App.tsx

backend/src/main/java/com/soccer/platform/controller/*
backend/src/main/java/com/soccer/platform/entity/*
backend/src/main/java/com/soccer/platform/repository/*
backend/src/main/java/com/soccer/platform/service/*
```