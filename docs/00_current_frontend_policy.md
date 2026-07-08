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
/team-analysis-clips/new
/team-analysis-clips/:teamClipId/edit

/player-analysis-clips
/player-analysis-clips/new
/player-analysis-clips/:playerClipId/edit

/player-records
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

단, 선수 기록 관련 기능에서는 기존 최종 정책상 ANALYST도 기록 삭제 UI를 볼 수 있다.

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

클립 생성/수정 요청처럼 즉시 결과 확인이 필요한 작업은 `alert()`로 안내할 수 있다.

---

## 8. React useEffect 작성 정책

React Compiler 경고를 방지하기 위해 `useEffect` 안에서 동기적으로 `setState`를 반복 호출하지 않는다.

API 호출이 필요한 effect는 다음 기준을 따른다.

- effect 내부에 async 함수를 따로 정의한다.
- API 응답 이후에만 상태를 갱신한다.
- 컴포넌트 언마운트 후 상태 변경을 막기 위해 `ignore` 또는 `isMounted` 플래그를 사용한다.
- dependency에는 객체 전체가 아니라 필요한 원시값을 넣는다.
- effect 안에서 즉시 `setState`를 호출해 렌더링 연쇄를 만들지 않는다.

---

## 9. 전체 코드 제공 규칙

사용자가 에러를 보고하거나 특정 파일 수정이 필요한 경우, 수정된 파일의 전체 코드를 제공한다.

특히 프론트 작업에서는 부분 코드만 제공하지 않는다.

대상 예시는 다음과 같다.

```text
MatchVideoPage.tsx
PlayerRecordPage.tsx
PlayerAnalysisClipPage.tsx
PlayerAnalysisClipEditorPage.tsx
TeamAnalysisClipPage.tsx
TeamAnalysisClipEditorPage.tsx
```

---

## 10. 사용하지 않는 변수 금지

TypeScript/ESLint 경고를 방지하기 위해 사용하지 않는 변수는 만들지 않는다.

권한 분기가 필요한 경우 실제로 사용하는 값만 만든다.

---

## 11. 파일 상단 주석 정책

프론트 파일 상단에는 해당 파일 역할을 설명하는 한 줄 주석을 작성한다.

예시:

```ts
// 경기 영상 기반 분석 작업 화면을 구성하는 페이지 컴포넌트
```

---

## 12. 영상 URL 처리 정책

현재 프론트는 백엔드가 상대 경로를 내려주면 API base URL과 결합해 재생 URL을 만든다.

예시:

```text
/uploads/match-videos/{storedFileName}
/uploads/team-analysis-clips/{storedFileName}
/uploads/player-analysis-clips/{storedFileName}
```

운영 전에는 반드시 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.

현재 MVP에서 `/uploads/**`를 직접 재생하는 구조는 개발 편의용이다.

---

## 13. 팀 분석 클립 프론트 정책

팀 분석 클립 화면은 목록/상세 페이지와 등록/수정 편집기 페이지로 분리되어 있다.

경로는 다음과 같다.

```text
/team-analysis-clips
/team-analysis-clips/new
/team-analysis-clips/:teamClipId/edit
```

다만 신규 등록의 주 흐름은 추후 `/match-videos` 경기 영상 상세 화면으로 이동한다.

`/team-analysis-clips/new`는 당장 제거하지 않고, 안정화 후 제거 여부를 결정한다.

---

## 14. 선수 개인 분석 클립 프론트 정책

선수 개인 분석 클립 화면은 목록/상세 페이지와 등록/수정 편집기 페이지로 분리되어 있다.

경로는 다음과 같다.

```text
/player-analysis-clips
/player-analysis-clips/new
/player-analysis-clips/:playerClipId/edit
```

상세 재생은 `playerClipUrl` 기준으로 처리한다.

드로잉 시간 기준은 생성된 선수 개인 분석 클립 영상 기준 초다.

신규 등록의 주 흐름은 추후 `/match-videos` 경기 영상 상세 화면으로 이동한다.

`/player-analysis-clips/new`는 당장 제거하지 않고, 안정화 후 제거 여부를 결정한다.

---

## 15. 선수 개인 분석 클립 조회 기록 프론트 정책

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

## 16. 경기 영상 기반 분석 작업 프론트 방향

향후 프론트 작업의 핵심 화면은 다음이다.

```text
frontend/src/pages/MatchVideoPage.tsx
```

`MatchVideoPage`는 경기 목록/상세/원본 영상 조회뿐 아니라, 경기 영상 기반 분석 작업의 진입점이 된다.

추가될 주요 기능은 다음과 같다.

- 경기 상세 영상 하단 또는 우측에 `분석 작업 시작` 버튼 추가
- 팀 분석 클립 / 선수 개인 분석 클립 선택
- 클립 시작/종료 시간 설정
- 드로잉 작성
- 클립 저장 정보 입력
- 클립 생성 API 호출
- 클립 생성 성공 후 선수 기록 이벤트 연결 창 표시
- 기록 대상 선수 선택
- 기록 이벤트 타입 선택
- 이벤트 시간 설정
- 이벤트 메모 입력
- 선수 기록 이벤트 + 클립 연결 API 호출
- 영상 하단에 클립 없이 선수 기록 이벤트만 빠르게 등록하는 폼 추가

---

## 17. 선수 기록 프론트 방향

선수 기록 조회 화면은 다음 경로를 사용한다.

```text
/player-records
```

`/player-records`의 역할은 선수 본인 기록 조회 중심이다.

역할은 다음과 같다.

- 선수 본인 경기별 기록 목록 조회
- 기록 상세 조회
- 기록 이벤트 목록 조회
- 기록 이벤트별 연결 클립 조회
- 기록 타입 클릭 시 연결 클립 재생
- 클립 없는 기록 이벤트는 `클립 없음`으로 표시

COACH/ANALYST의 선수 기록 직접 등록 화면은 우선순위에서 제외한다.

지도자/분석관의 기록 등록 주 흐름은 `/match-videos` 경기 영상 상세 화면이다.

---

## 18. CORS 관련 프론트 주의사항

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

## 19. 기능 완료 후 문서 갱신 규칙

프론트 기능 작업이 완료되면 사용자가 따로 요청하지 않아도 다음을 제공한다.

1. 변경 내용이 반영된 최종 프론트 요구사항 문서
2. 생성/수정 파일 기준 커밋 메시지
3. GitHub PR 제목과 내용
4. 다음 작업 GitHub 이슈
5. 다음 새 채팅 시작 프롬프트
6. 다음 작업 기준으로 갱신된 `00_project_context_for_chatgpt.md`
7. 필요하면 `00_current_backend_policy.md`, `00_current_frontend_policy.md` 갱신 내용

---

## 20. 다음 프론트 작업

다음 프론트 작업은 아직 바로 시작하지 않는다.

먼저 백엔드에서 다음 작업을 완료해야 한다.

```text
경기 영상 기반 선수 기록 이벤트와 클립 연결 API 구현
```

백엔드 완료 후 진행할 프론트 작업은 다음이다.

```text
경기 영상 기반 클립 생성·선수 기록 이벤트 연결 프론트 연동
```

우선 확인할 파일은 다음과 같다.

```text
frontend/src/pages/MatchVideoPage.tsx
frontend/src/api/matchVideoApi.ts
frontend/src/types/matchVideo.ts
frontend/src/api/teamAnalysisClipApi.ts
frontend/src/types/teamAnalysisClip.ts
frontend/src/api/playerAnalysisClipApi.ts
frontend/src/types/playerAnalysisClip.ts
frontend/src/components/TeamAnalysisDrawingCanvas.tsx
frontend/src/components/PlayerAnalysisDrawingCanvas.tsx
frontend/src/constants/routes.ts
frontend/src/App.tsx
```