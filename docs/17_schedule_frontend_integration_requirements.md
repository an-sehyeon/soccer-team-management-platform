# 17. 스케줄 프론트 연동 요구사항

## 1. 결론

스케줄 프론트 연동 기능은 백엔드에 이미 구현된 스케줄 API를 React 화면과 연결해, 모든 로그인 사용자가 스케줄을 조회하고 지도자만 스케줄을 등록, 수정, 삭제할 수 있게 하는 기능이다.

이번 구현 기준은 다음과 같다.

- 프론트는 React + TypeScript + Vite를 사용한다.
- 인증은 기존 로그인/회원가입 프론트 구조를 사용한다.
- Access Token은 `localStorage`에 저장하고, `axiosInstance`에서 Authorization 헤더에 자동으로 추가한다.
- 스케줄 화면 경로는 `/schedules`를 사용한다.
- 라우트 경로는 문자열로 직접 작성하지 않고 `ROUTES.SCHEDULE` 상수를 사용한다.
- 모든 로그인 사용자는 스케줄 목록과 상세를 조회할 수 있다.
- `COACH`만 스케줄 등록, 수정, 삭제 UI를 볼 수 있다.
- `ANALYST`, `PLAYER`에게는 등록, 수정, 삭제 버튼을 노출하지 않는다.
- 버튼 숨김은 사용자 경험을 위한 처리이며, 실제 권한 검증은 백엔드에서 처리한다.
- 스케줄 요청/응답 JSON 필드명은 `scheduleDateTime`으로 통일한다.
- 스케줄 목록 조회 파라미터는 `startDate`, `endDate`를 사용한다.
- 프론트 파일 상단에는 해당 파일 역할을 설명하는 한 줄 주석을 작성한다.

---

## 2. 기능 목적

스케줄 프론트 연동의 목적은 팀 훈련, 경기, 미팅, 행사 일정을 사용자가 화면에서 확인하고, 지도자가 화면에서 직접 관리할 수 있게 하는 것이다.

이 기능은 선수들이 모바일에서 자주 확인할 가능성이 높은 운영 기능이다.

따라서 초기 프론트 구현에서는 다음 기준을 우선한다.

- 선수는 스케줄을 빠르게 확인할 수 있어야 한다.
- 지도자는 스케줄을 쉽게 등록, 수정, 삭제할 수 있어야 한다.
- 분석관은 팀 일정을 확인하되 변경 권한은 가지지 않아야 한다.
- 같은 `/schedules` 화면을 역할에 따라 다르게 보여준다.
- 프론트 권한 처리는 버튼 노출 제어에 한정하고, 실제 차단은 백엔드 권한 검증에 맡긴다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

지도자는 팀 운영 책임자이므로 스케줄을 화면에서 관리할 수 있다.

가능한 기능은 다음과 같다.

- 스케줄 목록 조회
- 스케줄 상세 조회
- 스케줄 등록
- 스케줄 수정
- 스케줄 삭제

화면에서는 다음 UI가 노출된다.

- 조회 기간 선택 영역
- 스케줄 목록
- 스케줄 상세 정보
- 스케줄 등록 폼
- 수정 버튼
- 삭제 버튼

### 3.2 분석관 `ANALYST`

분석관은 팀 일정을 확인할 수 있지만 운영 일정을 변경하지 않는다.

가능한 기능은 다음과 같다.

- 스케줄 목록 조회
- 스케줄 상세 조회

노출하지 않는 UI는 다음과 같다.

- 스케줄 등록 폼
- 수정 버튼
- 삭제 버튼

### 3.3 선수 `PLAYER`

선수는 본인 팀 일정을 확인하는 사용자이다.

가능한 기능은 다음과 같다.

- 스케줄 목록 조회
- 스케줄 상세 조회

노출하지 않는 UI는 다음과 같다.

- 스케줄 등록 폼
- 수정 버튼
- 삭제 버튼

---

## 4. 권한 정책

| 역할 | 목록 조회 | 상세 조회 | 등록 UI | 수정 UI | 삭제 UI |
|---|---:|---:|---:|---:|---:|
| `COACH` | 가능 | 가능 | 노출 | 노출 | 노출 |
| `ANALYST` | 가능 | 가능 | 미노출 | 미노출 | 미노출 |
| `PLAYER` | 가능 | 가능 | 미노출 | 미노출 | 미노출 |

권한 처리 기준은 다음과 같다.

- `/schedules` 라우트는 로그인 사용자라면 접근 가능하다.
- `/schedules` 라우트를 `COACH` 전용 `RoleRoute`로 막지 않는다.
- 화면 내부에서 로그인 사용자의 `memberRole`이 `COACH`인지 확인한다.
- `COACH`인 경우에만 등록, 수정, 삭제 관련 UI를 렌더링한다.
- `ANALYST`, `PLAYER`는 조회 화면만 사용한다.
- 프론트에서 버튼이 보이지 않더라도 API 직접 호출은 가능할 수 있으므로 백엔드에서 다시 권한을 검증한다.

사용 기준 예시는 다음과 같다.

```tsx
const isCoach = member?.memberRole === "COACH";
```

---

## 5. 화면 흐름

### 5.1 공통 조회 흐름

1. 사용자가 로그인한다.
2. 대시보드, 선수 홈, 모바일 홈에서 스케줄 메뉴를 클릭한다.
3. `ROUTES.SCHEDULE` 경로로 이동한다.
4. 스케줄 화면 진입 시 현재 월 기준 조회 기간을 설정한다.
5. 프론트는 `GET /api/schedules?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd`를 호출한다.
6. 응답받은 스케줄 목록을 화면에 표시한다.
7. 사용자가 스케줄 항목을 클릭하면 상세 조회 API를 호출한다.
8. 상세 정보를 화면 하단에 표시한다.

### 5.2 지도자 관리 흐름

1. `COACH`가 `/schedules`에 진입한다.
2. 스케줄 등록 폼이 노출된다.
3. 일정 날짜와 시간, 장소, 일정 유형, 운동 강도, 상세 내용을 입력한다.
4. 등록 버튼을 누르면 `POST /api/coach/schedules`를 호출한다.
5. 등록 성공 후 목록을 다시 조회한다.
6. 기존 스케줄의 수정 버튼을 누르면 폼에 기존 값이 채워진다.
7. 수정 버튼을 누르면 `PATCH /api/coach/schedules/{scheduleId}`를 호출한다.
8. 삭제 버튼을 누르면 확인창을 띄운 뒤 `DELETE /api/coach/schedules/{scheduleId}`를 호출한다.
9. 수정 또는 삭제 성공 후 목록을 다시 조회한다.

### 5.3 선수/분석관 조회 흐름

1. `PLAYER` 또는 `ANALYST`가 `/schedules`에 진입한다.
2. 조회 기간과 목록만 확인한다.
3. 등록 폼, 수정 버튼, 삭제 버튼은 화면에 보이지 않는다.
4. 스케줄 항목을 클릭해 상세 정보를 확인한다.

---

## 6. API 흐름

### 6.1 스케줄 목록 조회

```http
GET /api/schedules?startDate=2026-06-01&endDate=2026-06-30
```

프론트 요청 파라미터는 다음과 같다.

| 파라미터 | 형식 | 설명 |
|---|---|---|
| `startDate` | `yyyy-MM-dd` | 조회 시작일 |
| `endDate` | `yyyy-MM-dd` | 조회 종료일 |

프론트 처리 기준은 다음과 같다.

- 화면 진입 시 현재 월의 첫날과 마지막 날을 기본 조회 기간으로 사용한다.
- JavaScript `toISOString()`은 UTC 변환으로 날짜가 밀릴 수 있으므로 로컬 날짜 포맷 함수를 사용한다.
- 응답은 배열 형태의 `ScheduleResponse[]`로 처리한다.

응답 예시는 다음과 같다.

```json
[
  {
    "scheduleId": 1,
    "writerMemberId": 5,
    "writerName": "이승원",
    "scheduleDateTime": "2026-06-20T15:00:00",
    "place": "운동장",
    "scheduleType": "TRAINING",
    "comment": "전술 훈련",
    "intensity": "HIGH",
    "createdAt": "2026-06-16T13:16:35.962399",
    "updatedAt": "2026-06-16T13:16:35.962399"
  }
]
```

### 6.2 스케줄 상세 조회

```http
GET /api/schedules/{scheduleId}
```

사용 목적은 목록에서 선택한 스케줄의 상세 정보를 확인하는 것이다.

### 6.3 스케줄 등록

```http
POST /api/coach/schedules
```

사용 가능 역할은 다음과 같다.

- `COACH`

요청 필드 기준은 다음과 같다.

```json
{
  "scheduleDateTime": "2026-06-25T14:20:00",
  "place": "대운동장",
  "scheduleType": "MATCH",
  "intensity": "MEDIUM",
  "comment": "금호고등학교와 연습경기"
}
```

### 6.4 스케줄 수정

```http
PATCH /api/coach/schedules/{scheduleId}
```

사용 가능 역할은 다음과 같다.

- `COACH`

요청 필드 기준은 등록 요청과 동일하다.

### 6.5 스케줄 삭제

```http
DELETE /api/coach/schedules/{scheduleId}
```

사용 가능 역할은 다음과 같다.

- `COACH`

삭제 성공 후 프론트는 목록을 다시 조회한다.

---

## 7. 타입 설계 방향

스케줄 프론트 타입 파일은 다음 위치에 둔다.

```text
frontend/src/types/schedule.ts
```

주요 타입은 다음과 같다.

```ts
export type ScheduleType =
  | "TRAINING"
  | "MATCH"
  | "MEETING"
  | "EVENT"
  | "EXTERNAL"
  | "ETC";

export type ScheduleIntensity = "HIGH" | "MEDIUM" | "LOW";
```

스케줄 응답 타입은 백엔드 응답과 맞춘다.

```ts
export type ScheduleResponse = {
  scheduleId: number;
  writerMemberId: number;
  writerName: string;
  scheduleDateTime: string;
  place: string;
  scheduleType: ScheduleType;
  comment: string | null;
  intensity: ScheduleIntensity | null;
  createdAt: string;
  updatedAt: string;
};
```

스케줄 요청 타입은 다음과 같다.

```ts
export type ScheduleRequest = {
  scheduleDateTime: string;
  place: string;
  scheduleType: ScheduleType;
  intensity: ScheduleIntensity | null;
  comment: string | null;
};
```

---

## 8. 라우팅 및 메뉴 연결

스케줄 경로는 `frontend/src/constants/routes.ts`에서 관리한다.

```ts
// 프론트에서 사용하는 주요 페이지 경로를 한 곳에서 관리하는 파일

export const ROUTES = {
  LOGIN: "/login",
  SIGN_UP: "/sign-up",
  DASHBOARD: "/dashboard",
  PLAYER: "/player",
  MOBILE: "/mobile",
  MEMBER_APPROVAL: "/dashboard/member-approval",
  SCHEDULE: "/schedules",
} as const;
```

라우트 등록 시 문자열 `"/schedules"`를 직접 사용하지 않고 `ROUTES.SCHEDULE`을 사용한다.

```tsx
<Route
  path={ROUTES.SCHEDULE}
  element={
    <ProtectedRoute>
      <SchedulePage />
    </ProtectedRoute>
  }
/>
```

홈 화면 메뉴 이동도 `ROUTES.SCHEDULE`을 사용한다.

```tsx
<button type="button" onClick={() => navigate(ROUTES.SCHEDULE)}>
  스케줄 관리
</button>
```

---

## 9. DB 설계 방향

이번 작업은 프론트 연동 작업이므로 신규 DB 테이블은 추가하지 않는다.

기존 백엔드 스케줄 기능에서 사용하는 테이블은 다음과 같다.

```text
schedule
member
```

DB 컬럼명과 API 필드명은 다음 기준으로 관리한다.

```text
DB 컬럼명: schedule_datetime
DTO 필드명: scheduleDateTime
프론트 타입 필드명: scheduleDateTime
JSON 필드명: scheduleDateTime
```

스케줄 조회 기간은 백엔드에서 다음 기준으로 변환한다.

```text
startDate.atStartOfDay()
endDate.plusDays(1).atStartOfDay()
```

조회 조건은 다음 기준을 사용한다.

```text
scheduleDateTime >= startDateTime
scheduleDateTime < endDateTime
```

이 방식은 종료일 당일 오후, 저녁 일정이 조회에서 누락되지 않게 하기 위한 처리이다.

---

## 10. 예외 상황

### 10.1 스케줄 조회 실패

상황:

- Access Token이 없거나 만료됨
- 조회 기간이 잘못됨
- 백엔드 서버 오류

처리:

- 백엔드 에러 메시지를 화면에 표시한다.
- 목록은 빈 배열로 처리한다.
- 인증 실패 시 기존 `axiosInstance` 정책에 따라 Access Token을 제거한다.

### 10.2 등록/수정 실패

상황:

- 필수값 누락
- 잘못된 일정 유형
- 잘못된 운동 강도
- `COACH`가 아닌 사용자가 직접 API 호출

처리:

- 백엔드 에러 메시지를 화면에 표시한다.
- 실패 시 목록을 임의로 갱신하지 않는다.
- 실제 권한 실패는 백엔드 응답을 기준으로 처리한다.

### 10.3 삭제 실패

상황:

- 존재하지 않는 스케줄 ID
- 이미 삭제된 스케줄
- `COACH`가 아닌 사용자가 직접 API 호출

처리:

- 백엔드 에러 메시지를 화면에 표시한다.
- 실패 시 선택된 상세 정보와 목록 상태를 유지한다.

---

## 11. 구현 순서

1. `frontend/src/types/schedule.ts` 작성
2. `frontend/src/api/scheduleApi.ts` 작성
3. `frontend/src/pages/SchedulePage.tsx` 작성
4. `frontend/src/constants/routes.ts`에 `SCHEDULE` 추가
5. `frontend/src/App.tsx`에 `/schedules` 라우트 추가
6. `DashboardHomePage.tsx`에서 스케줄 관리 메뉴 연결
7. `PlayerHomePage.tsx`에서 오늘 일정 메뉴 연결
8. `MobileHomePage.tsx`에서 오늘 일정 메뉴 연결
9. `index.css` 또는 공통 CSS에 스케줄 화면 스타일 추가
10. 백엔드 DTO 필드명을 `scheduleDateTime`으로 정리
11. 백엔드 조회 기간을 `startDate`, `endDate` 기준으로 정리
12. `COACH` 계정으로 조회, 등록, 수정, 삭제 테스트
13. `PLAYER`, `ANALYST` 계정에서 조회 전용 화면 확인
14. 빌드 확인

---

## 12. 구현 파일

이번 기능에서 생성 또는 수정한 파일은 다음과 같다.

```text
docs/16_frontend_auth_requirements.md
frontend/src/App.tsx
backend/src/main/java/com/soccer/platform/dto/schedule/CreateScheduleRequestDTO.java
frontend/src/pages/DashboardHomePage.tsx
frontend/src/index.css
frontend/src/pages/MobileHomePage.tsx
frontend/src/pages/PlayerHomePage.tsx
frontend/src/constants/routes.ts
frontend/src/types/schedule.ts
frontend/src/api/scheduleApi.ts
backend/src/main/java/com/soccer/platform/controller/ScheduleController.java
frontend/src/pages/SchedulePage.tsx
backend/src/main/java/com/soccer/platform/repository/ScheduleRepository.java
backend/src/main/java/com/soccer/platform/dto/schedule/ScheduleResponseDTO.java
backend/src/main/java/com/soccer/platform/service/schedule/ScheduleService.java
backend/src/main/java/com/soccer/platform/service/schedule/ScheduleValidator.java
backend/src/main/java/com/soccer/platform/dto/schedule/UpdateScheduleRequestDTO.java
```

---

## 13. 테스트 결과

사용자가 실제 확인한 테스트 결과는 다음과 같다.

- 스케줄 목록 조회 정상 확인
- 스케줄 등록 정상 확인
- 스케줄 수정 정상 확인
- 스케줄 삭제 정상 확인

추가 확인이 필요한 항목은 다음과 같다.

- `PLAYER` 계정에서 등록, 수정, 삭제 버튼 미노출 확인
- `ANALYST` 계정에서 등록, 수정, 삭제 버튼 미노출 확인
- `PLAYER`, `ANALYST` 토큰으로 등록, 수정, 삭제 API 직접 호출 시 백엔드 차단 확인

---

## 14. 추후 확장 가능성

스케줄 프론트 기능은 추후 다음 기능으로 확장할 수 있다.

- 오늘 일정 우선 표시
- 주간 캘린더 UI
- 월간 캘린더 UI
- 일정 유형별 필터
- 운동 강도별 필터
- 반복 일정
- 일정 알림
- 출석 체크 연동
- 경기 영상과 경기 일정 연결
- 모바일 하단 네비게이션과 스케줄 화면 연결
