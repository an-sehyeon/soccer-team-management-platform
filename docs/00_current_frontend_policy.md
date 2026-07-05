# 00. 현재 프론트엔드 정책

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 프론트엔드 최신 정책을 요약한다.

새 채팅에서 프론트 작업을 이어서 진행할 때 이 문서를 우선 참고한다.

상세 내용은 기능별 프론트 연동 요구사항 문서와 실제 소스 코드를 기준으로 확인한다.

기능별 상세 요구사항 md 문서는 GitHub `docs/` 경로를 기준으로 확인한다.

```text
https://github.com/an-sehyeon/soccer-team-management-platform/tree/main/docs
```

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
/player-analysis-clips
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
ANALYST: 등록/수정 UI 노출, 삭제 UI 미노출
PLAYER: 조회 UI만 노출
```

단, 기능별 정책이 다를 수 있으므로 상세 요구사항 문서를 우선 확인한다.

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

과거에 백엔드 DTO와 프론트 타입이 달라 에러가 발생한 적이 있으므로, 기능 구현 전 애매한 부분은 반드시 사용자에게 파일을 요청한다.

---

## 7. 에러 메시지 표시 정책

사용자 화면에는 HTTP status, error code, path를 그대로 노출하지 않는다.

백엔드가 내려준 `message`가 있으면 fallback 메시지와 함께 보여준다.

권장 함수는 다음과 같다.

```ts
function getApiErrorMessage(error: unknown, fallbackMessage: string) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const message = error.response?.data?.message;

    if (message) {
      return `${fallbackMessage} / ${message}`;
    }

    if (error.message) {
      return `${fallbackMessage} / ${error.message}`;
    }
  }

  if (error instanceof Error && error.message) {
    return `${fallbackMessage} / ${error.message}`;
  }

  return fallbackMessage;
}
```

예시는 다음과 같다.

```text
선수 개인 분석 클립 상세 정보를 불러오지 못했습니다. / 경기 영상을 찾을 수 없습니다.
```

---

## 8. React useEffect 작성 정책

React Compiler 경고를 방지하기 위해 `useEffect` 안에서 동기적으로 `setState`를 반복 호출하지 않는다.

API 호출이 필요한 effect는 다음 기준을 따른다.

- effect 내부에 async 함수를 따로 정의한다.
- API 응답 이후에만 상태를 갱신한다.
- 컴포넌트 언마운트 후 상태 변경을 막기 위해 `ignore` 또는 `isMounted` 플래그를 사용한다.
- dependency에는 객체 전체가 아니라 필요한 원시값을 넣는다.

예시는 다음과 같다.

```ts
useEffect(() => {
  let ignore = false;

  async function fetchData() {
    try {
      const response = await getData();

      if (!ignore) {
        setData(response);
      }
    } catch (error) {
      if (!ignore) {
        setErrorMessage(getApiErrorMessage(error, "데이터를 불러오지 못했습니다."));
      }
    }
  }

  fetchData();

  return () => {
    ignore = true;
  };
}, []);
```

---

## 9. 전체 코드 제공 규칙

사용자가 에러를 보고하거나 특정 파일 수정이 필요한 경우, 수정된 파일의 전체 코드를 제공한다.

특히 프론트 작업에서는 부분 코드만 제공하지 않는다.

대상 예시는 다음과 같다.

```text
PlayerAnalysisClipPage.tsx
TeamAnalysisClipPage.tsx
MatchVideoPage.tsx
SchedulePage.tsx
NoticePage.tsx
```

단, 코드가 너무 길면 sandbox 파일로 만들어 링크를 제공할 수 있다.

---

## 10. 사용하지 않는 변수 금지

TypeScript/ESLint 경고를 방지하기 위해 사용하지 않는 변수는 만들지 않는다.

예를 들어 `isPlayer`를 선언했지만 사용하지 않으면 안 된다.

권한 분기가 필요한 경우 실제로 사용하는 값만 만든다.

좋은 예시는 다음과 같다.

```ts
const canManageDrawing = member?.memberRole === "COACH" || member?.memberRole === "ANALYST";
const canDeleteDrawing = member?.memberRole === "COACH";
```

피해야 할 예시는 다음과 같다.

```ts
const isPlayer = member?.memberRole === "PLAYER";
```

단, 실제 JSX나 로직에서 사용한다면 선언해도 된다.

---

## 11. 파일 상단 주석 정책

프론트 파일 상단에는 해당 파일 역할을 설명하는 한 줄 주석을 작성한다.

예시는 다음과 같다.

```ts
// 선수 개인 분석 클립 관련 백엔드 API 호출 함수를 관리하는 파일
```

```tsx
// 선수 개인 분석 클립 화면을 구성하는 페이지 컴포넌트
```

---

## 12. 현재 프론트 완료 상태

현재 완료된 주요 프론트 연동은 다음과 같다.

- 로그인/회원가입 프론트 연동
- 관리자 회원 승인 프론트 연동
- 스케줄 프론트 연동
- 공지사항 프론트 연동
- 경기 영상 업로드/조회 프론트 연동
- 팀 분석 클립 프론트 연동
- 팀 분석 클립 드로잉 프론트 기본 연동
- 선수 목록 조회 API 프론트 연동
- 선수 개인 분석 클립 프론트 연동
- 선수 개인 분석 클립 드로잉 프론트 연동

현재 중단된 프론트 기능은 없다.

---

## 13. 선수 개인 분석 클립 프론트 정책

선수 개인 분석 클립 상세 재생은 원본 경기 영상 URL + `startTimeSec/endTimeSec` 구간 재생 방식이 아니다.

상세 재생은 다음 필드를 기준으로 한다.

```text
playerClipUrl
```

`playerClipUrl`은 백엔드에서 생성된 실제 선수 개인 분석 클립 mp4 파일 URL이다.

상세 응답에는 `startTimeSec`, `endTimeSec`가 포함된다.

단, 이 값은 상세 재생용이 아니라 수정 폼 초기값과 파일 재생성 요청용이다.

목록 응답에는 `startTimeSec`, `endTimeSec`를 사용하지 않는다.

상태별 화면 처리는 다음 기준을 따른다.

```text
PROCESSING 또는 UPLOADING
→ 클립 파일 생성 중 메시지 표시
→ 영상 재생 막기
→ 드로잉 표시 막기

FAILED
→ 클립 파일 생성 실패 메시지 표시
→ 영상 재생 막기
→ 드로잉 표시 막기

READY
→ playerClipUrl 기준 영상 재생
→ 저장된 드로잉 오버레이 표시
```

선수 개인 분석 클립 수정은 기존 `PATCH /api/management/player-analysis-clips/{playerClipId}` API를 사용한다.

수정 후에는 백엔드가 새 mp4 파일을 비동기로 생성하므로, 프론트는 `PROCESSING`, `READY`, `FAILED` 상태를 반영한다.

---

## 14. 선수 개인 분석 클립 + 드로잉 통합 등록 정책

선수 개인 분석 클립 등록 화면은 다음 흐름을 따른다.

1. 원본 경기 영상 선택
2. 대상 선수 선택
3. 클립 유형, 제목, 코멘트 입력
4. 원본 경기 영상 기준 시작/종료 구간 선택
5. 드로잉 유형 선택
6. 드로잉 시작/종료 시간 입력
7. 드로잉 모드 켜기
8. 영상 위에 드로잉 작성
9. 작성 즉시 화면에 미리보기 표시
10. 필요 시 저장 전 드로잉 제거
11. 최종 등록 버튼 클릭
12. 클립 메타데이터와 드로잉 JSON 목록을 통합 생성 API로 전송

통합 생성 API는 다음과 같다.

```http
POST /api/management/player-analysis-clips/with-drawings
```

프론트 요청에는 다음 데이터를 포함한다.

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

클립의 `startTimeSec`, `endTimeSec`는 원본 경기 영상 기준이다.

드로잉의 `startTimeSec`, `endTimeSec`는 생성될 선수 개인 분석 클립 영상 기준이다.

---

## 15. 드로잉 프론트 정책

드로잉은 영상 파일에 합성하지 않는다.

프론트 캔버스에서 생성한 좌표, 텍스트, 도형 데이터를 JSON으로 저장한다.

조회 시 저장된 JSON을 다시 캔버스 오버레이로 렌더링한다.

드로잉은 영상 재생 시간이 `startTimeSec`, `endTimeSec` 범위 안에 있을 때만 표시한다.

선수 개인 분석 클립 드로잉 시간은 생성된 선수 개인 분석 클립 기준 초다.

등록 화면에서는 드로잉을 작성한 즉시 `createDrawings`에 추가하고 화면에 미리보기로 표시한다.

저장 전 드로잉은 목록에서 제거할 수 있다.

저장 전 드로잉 수정, 저장된 드로잉 수정/삭제, 색상/두께/글자 크기 변경, 타임라인 기반 조절은 추후 영상 편집기 UX 고도화 단계에서 처리한다.

---

## 16. 영상 URL 처리 정책

현재 프론트는 백엔드가 상대 경로를 내려주면 API base URL과 결합해 재생 URL을 만든다.

예시는 다음과 같다.

```text
/uploads/match-videos/{storedFileName}
/uploads/player-analysis-clips/{storedFileName}
```

운영 전에는 반드시 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.

현재 MVP에서 `/uploads/**`를 직접 재생하는 구조는 개발 편의용이다.

---

## 17. 다음 프론트 작업

다음 프론트 작업은 다음이다.

```text
팀 분석 클립/드로잉을 선수 개인 분석 클립/드로잉과 동일한 UX와 파일 재생 구조로 통일
```

핵심 방향은 다음과 같다.

- 팀 분석 클립 상세 재생을 원본 경기 영상 URL + `startTimeSec/endTimeSec`에서 `teamClipUrl` 기준으로 변경한다.
- `PROCESSING`, `READY`, `FAILED` 상태를 목록/상세 화면에 표시한다.
- `READY` 상태에서만 영상 재생과 드로잉 오버레이를 활성화한다.
- 팀 분석 드로잉 시간은 생성된 팀 분석 클립 기준 초로 처리한다.
- 팀 분석 클립 등록 화면에서도 드로잉 즉시 미리보기와 유지 시간 설정을 제공한다.
- 저장 전 드로잉 제거 기능을 유지한다.
- 상세 재생 시 저장된 팀 분석 드로잉을 해당 시간대에 표시한다.

다음 작업을 시작할 때 우선 확인할 프론트 파일은 다음과 같다.

```text
frontend/src/types/teamAnalysisClip.ts
frontend/src/api/teamAnalysisClipApi.ts
frontend/src/types/teamAnalysisClipDrawing.ts
frontend/src/api/teamAnalysisClipDrawingApi.ts
frontend/src/pages/TeamAnalysisClipPage.tsx
frontend/src/components/TeamAnalysisDrawingCanvas.tsx
frontend/src/utils/videoUrl.ts
```

필요하면 관련 백엔드 Controller/DTO 파일도 함께 확인한다.

---

## 18. 기능 완료 후 문서 갱신 규칙

프론트 기능 작업이 완료되면 사용자가 따로 요청하지 않아도 다음을 제공한다.

1. 변경 내용이 반영된 최종 프론트 요구사항 문서
2. 생성/수정 파일 기준 커밋 메시지
3. GitHub PR 제목과 내용
4. 다음 작업 GitHub 이슈
5. 다음 새 채팅 시작 프롬프트
6. 다음 작업 기준으로 갱신된 `00_project_context_for_chatgpt.md`
7. 필요 시 `00_current_frontend_policy.md` 갱신본

이 규칙은 모든 프론트 작업에 적용한다.
