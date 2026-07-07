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

예시:

```text
선수 개인 분석 클립 상세 정보를 불러오지 못했습니다. / 경기 영상을 찾을 수 없습니다.
```

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
- 목록 조회 함수가 effect 밖에 있고 내부에서 바로 `setState`를 여러 번 호출하는 패턴은 피한다.

예시:

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
TeamAnalysisClipEditorPage.tsx
MatchVideoPage.tsx
SchedulePage.tsx
NoticePage.tsx
```

단, 사용자가 “필요한 코드만”이라고 명시하면 수정 부분만 제공한다.

---

## 10. 사용하지 않는 변수 금지

TypeScript/ESLint 경고를 방지하기 위해 사용하지 않는 변수는 만들지 않는다.

예를 들어 `isPlayer`를 선언했지만 사용하지 않으면 안 된다.

권한 분기가 필요한 경우 실제로 사용하는 값만 만든다.

좋은 예:

```ts
const canManageDrawing = member?.memberRole === "COACH" || member?.memberRole === "ANALYST";
const canDeleteDrawing = member?.memberRole === "COACH";
```

피해야 할 예:

```ts
const isPlayer = member?.memberRole === "PLAYER";
```

단, 실제 JSX나 로직에서 사용한다면 선언해도 된다.

---

## 11. 파일 상단 주석 정책

프론트 파일 상단에는 해당 파일 역할을 설명하는 한 줄 주석을 작성한다.

예시:

```ts
// 선수 개인 분석 클립 관련 백엔드 API 호출 함수를 관리하는 파일
```

```tsx
// 선수 개인 분석 클립 화면을 구성하는 페이지 컴포넌트
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

## 13. 팀 분석 클립 프론트 최신 정책

팀 분석 클립 화면은 목록/상세 페이지와 등록/수정 편집기 페이지를 분리한다.

경로는 다음과 같다.

```text
/team-analysis-clips
/team-analysis-clips/new
/team-analysis-clips/:teamClipId/edit
```

### 13-1. 목록/상세 페이지

`/team-analysis-clips`의 역할은 다음과 같다.

- READY 상태 팀 분석 클립 목록 조회
- 경기 영상 조건 조회
- 클립 유형 조건 조회
- 팀 분석 클립 상세 조회
- 생성된 `teamClipUrl` 기준 영상 재생
- 드로잉 오버레이 표시
- 등록 페이지 이동
- 수정 페이지 이동
- COACH 삭제 처리

PLAYER는 조회만 가능하다.

COACH와 ANALYST는 등록/수정 버튼을 볼 수 있다.

COACH만 삭제 버튼을 볼 수 있다.

### 13-2. 등록 편집기 페이지

`/team-analysis-clips/new`의 역할은 다음과 같다.

- 원본 경기 영상 선택
- 영상 선택 후 원본 영상 재생 영역 표시
- 클립 시작/종료 시간 버튼 설정
- 클립 시간 초기화
- 선택 구간 재생
- 드로잉 시작/종료 시간 버튼 설정
- 드로잉 시간 초기화
- 클립 정보 입력
- 드로잉 작성
- 저장 전 드로잉 목록 관리
- 최종 저장

등록 페이지는 원본 경기 영상 선택 전에는 원본 영상 선택 영역만 표시한다.

원본 영상 선택 전에는 아래 영역을 숨긴다.

```text
클립 정보
드로잉 작성
저장 전 드로잉 목록
최종 저장
```

### 13-3. 수정 편집기 페이지

`/team-analysis-clips/:teamClipId/edit`의 역할은 다음과 같다.

- 기존 클립 정보 조회
- 기존 드로잉 조회
- 기존 원본 영상 기준 시간 유지
- 수정 페이지에서는 전체 편집 영역을 바로 표시
- 수정 저장 시 통합 수정 API 호출

수정 페이지에서는 제목/코멘트/드로잉만 바꾸면 기존 mp4 파일을 유지한다.

클립 시작/종료 시간이 바뀌면 백엔드에서 mp4 파일 재생성을 요청한다.

---

## 14. 팀 분석 클립 드로잉 프론트 정책

드로잉은 영상 파일에 합성하지 않는다.

프론트 캔버스에서 생성한 좌표, 텍스트, 도형 데이터를 JSON으로 저장한다.

조회 시 저장된 JSON을 다시 캔버스 오버레이로 렌더링한다.

드로잉은 영상 재생 시간이 `startTimeSec`, `endTimeSec` 범위 안에 있을 때만 표시한다.

팀 분석 클립 드로잉 시간 기준은 생성된 팀 분석 클립 영상 기준 초다.

예시는 다음과 같다.

```text
원본 영상 100초 ~ 115초를 잘라 15초 팀 분석 클립 생성
드로잉은 2초 ~ 6초처럼 생성된 클립 영상 기준으로 저장
```

---

## 15. 선수 개인 분석 클립 현재 프론트 상태

선수 개인 분석 클립 화면은 팀 분석 클립과 동일하게 목록/상세 페이지와 등록/수정 편집기 페이지로 분리되어 있다.

경로는 다음과 같다.

- /player-analysis-clips
- /player-analysis-clips/new
- /player-analysis-clips/:playerClipId/edit

/player-analysis-clips의 역할은 다음과 같다.

- 선수 개인 분석 클립 목록 조회
- 선수 개인 분석 클립 상세 조회
- 생성된 playerClipUrl 기준 영상 재생
- 드로잉 오버레이 표시
- 등록 페이지 이동
- 수정 페이지 이동
- COACH 삭제 처리

/player-analysis-clips/new의 역할은 다음과 같다.

- 원본 경기 영상 선택
- 대상 선수 선택
- 원본 경기 영상과 대상 선수 선택 전 편집 영역 숨김
- 클립 시작/종료 시간 버튼 설정
- 클립 시간 초기화
- 선택 구간 재생
- 드로잉 시작/종료 시간 버튼 설정
- 드로잉 시간 초기화
- 드로잉 작성
- 저장 전 드로잉 목록 관리
- 최종 저장

/player-analysis-clips/:playerClipId/edit의 역할은 다음과 같다.

- 기존 클립 정보 조회
- 기존 드로잉 조회
- 기존 원본 영상 기준 시간 유지
- 기존 대상 선수 유지
- 전체 편집 영역 바로 표시
- 통합 수정 API 호출

상세 재생은 playerClipUrl 기준으로 처리한다.

클립 시작/종료 시간은 원본 경기 영상 기준 초다.

드로잉 시작/종료 시간은 생성된 선수 개인 분석 클립 영상 기준 초다.

클립/드로잉 시간은 직접 입력하지 않고 영상 현재 시간 기준 버튼으로 설정한다.

NaN이 사용자 화면에 표시되지 않도록 안전 처리한다.

---

## 16. CORS 관련 프론트 주의사항

`PUT`, `PATCH`, `DELETE` 요청은 브라우저에서 preflight `OPTIONS` 요청이 발생한다.

프론트에서 다음과 같은 에러가 나면 백엔드 CORS 설정을 먼저 확인한다.

```text
Response to preflight request doesn't pass access control check
No 'Access-Control-Allow-Origin' header is present on the requested resource
```

백엔드에서는 다음 설정이 필요하다.

```text
allowedOrigins: http://localhost:5173
allowedMethods: GET, POST, PUT, PATCH, DELETE, OPTIONS
allowedHeaders: Authorization, Content-Type
OPTIONS /** permitAll
```

프론트에서는 실제 요청에 Authorization 헤더가 정상 포함되는지 확인한다.

---

## 17. 기능 완료 후 문서 갱신 규칙

프론트 기능 작업이 완료되면 사용자가 따로 요청하지 않아도 다음을 제공한다.

1. 변경 내용이 반영된 최종 프론트 요구사항 문서
2. 생성/수정 파일 기준 커밋 메시지
3. GitHub PR 제목과 내용
4. 다음 작업 GitHub 이슈
5. 다음 새 채팅 시작 프롬프트
6. 다음 작업 기준으로 갱신된 `00_project_context_for_chatgpt.md`

이 규칙은 모든 프론트 작업에 적용한다.