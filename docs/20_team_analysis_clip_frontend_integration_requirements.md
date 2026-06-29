# 20. 팀 분석 클립 프론트 연동 요구사항

## 1. 결론

팀 분석 클립 프론트 연동 기능은 백엔드에 이미 구현된 팀 분석 클립 API를 React 화면과 연결해, 지도자와 분석관은 원본 경기 영상의 특정 구간을 팀 분석 클립으로 등록·수정할 수 있고, 모든 로그인 사용자는 팀 분석 클립을 조회·재생할 수 있게 하는 기능이다.

이번 구현 기준은 다음과 같다.

* 프론트는 React + TypeScript + Vite를 사용한다.
* 인증은 기존 로그인/회원가입 프론트 구조를 사용한다.
* Access Token은 `localStorage`에 저장하고, `axiosInstance`에서 Authorization 헤더에 자동으로 추가한다.
* 팀 분석 클립 화면 경로는 `/team-analysis-clips`를 사용한다.
* 라우트 경로는 문자열로 직접 작성하지 않고 `ROUTES.TEAM_ANALYSIS_CLIP` 상수를 사용한다.
* `ROUTES` 상수는 `frontend/src/constants/routes.ts`에서 관리한다.
* 모든 로그인 사용자는 팀 분석 클립 목록과 상세를 조회할 수 있다.
* `COACH`, `ANALYST`는 팀 분석 클립 등록과 수정 UI를 볼 수 있다.
* `COACH`만 팀 분석 클립 삭제 UI를 볼 수 있다.
* `PLAYER`는 조회와 재생 UI만 볼 수 있다.
* 버튼 숨김은 사용자 경험을 위한 처리이며, 실제 권한 검증은 백엔드에서 처리한다.
* 팀 분석 클립은 실제 영상 파일을 자르지 않고 원본 경기 영상 URL과 `startTimeSec`, `endTimeSec` 메타데이터를 기준으로 재생한다.
* 프론트는 클립 구간 재생을 위해 `<video>` 태그의 `currentTime`을 `startTimeSec`로 이동시키고, 재생 시간이 `endTimeSec`에 도달하면 일시정지한다.
* 클립 생성 시 원본 경기 영상은 기존 경기 영상 목록 API를 통해 선택한다.
* 클립 수정 시 기존 값을 폼에 채운 뒤 전체 수정 방식에 가깝게 처리한다.
* 프론트 파일 상단에는 해당 파일 역할을 설명하는 한 줄 주석을 작성한다.
* `FormEvent`는 사용하지 않고 form submit 이벤트는 `{ preventDefault: () => void }` 타입으로 작성한다.

---

## 2. 기능 목적

팀 분석 클립 프론트 연동의 목적은 지도자와 분석관이 경기 영상 중 팀 전체가 함께 봐야 하는 장면을 클립으로 저장하고, 선수들이 해당 장면을 모바일 또는 PC에서 쉽게 조회할 수 있게 하는 것이다.

이 기능은 경기 영상 편집 MVP의 핵심 기능이다.

초기 프론트 구현에서는 다음 기준을 우선한다.

* 지도자와 분석관은 원본 경기 영상을 보면서 필요한 구간을 클립으로 등록할 수 있어야 한다.
* 선수는 등록된 팀 분석 클립을 빠르게 확인하고 재생할 수 있어야 한다.
* 실제 클립 파일을 생성하지 않고 원본 경기 영상 기준 시간 메타데이터로 재생한다.
* 클립 제목, 코멘트, 유형을 통해 분석 의도가 명확히 전달되어야 한다.
* 권한별 버튼 노출은 프론트에서 처리하되, 실제 차단은 백엔드 권한 검증에 맡긴다.
* 추후 팀 분석 클립 드로잉, 북마크, 조회 기록 기능과 연결할 수 있게 구조를 유지한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

가능한 기능은 다음과 같다.

* 팀 분석 클립 목록 조회
* 팀 분석 클립 상세 조회
* 팀 분석 클립 구간 재생
* 팀 분석 클립 등록
* 팀 분석 클립 수정
* 팀 분석 클립 삭제

화면에서는 다음 UI가 노출된다.

* 팀 분석 클립 목록
* 팀 분석 클립 상세 정보
* 원본 경기 영상 재생 영역
* 경기 영상 선택 영역
* 클립 유형 선택
* 클립 제목 입력
* 클립 코멘트 입력
* 시작 시간 입력
* 종료 시간 입력
* 등록 버튼
* 수정 버튼
* 삭제 버튼

### 3.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

* 팀 분석 클립 목록 조회
* 팀 분석 클립 상세 조회
* 팀 분석 클립 구간 재생
* 팀 분석 클립 등록
* 팀 분석 클립 수정

노출하지 않는 UI는 다음과 같다.

* 삭제 버튼

분석관에게 삭제 버튼을 보여주지 않는 이유는 팀 분석 클립이 선수 전체에게 공유되는 분석 자료이고, 추후 드로잉이나 코멘트 데이터와 연결될 수 있기 때문이다.

### 3.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

* 팀 분석 클립 목록 조회
* 팀 분석 클립 상세 조회
* 팀 분석 클립 구간 재생

노출하지 않는 UI는 다음과 같다.

* 등록 폼
* 수정 버튼
* 삭제 버튼

선수는 팀 분석 클립을 조회하는 사용자이므로 관리 기능은 제공하지 않는다.

---

## 4. 프론트 권한 노출 정책

| 역할        | 목록 조회 | 상세 조회 | 구간 재생 | 등록 UI | 수정 UI | 삭제 UI |
| --------- | ----: | ----: | ----: | ----: | ----: | ----: |
| `COACH`   |    가능 |    가능 |    가능 |    노출 |    노출 |    노출 |
| `ANALYST` |    가능 |    가능 |    가능 |    노출 |    노출 |   미노출 |
| `PLAYER`  |    가능 |    가능 |    가능 |   미노출 |   미노출 |   미노출 |

권한 처리 기준은 다음과 같다.

* `/team-analysis-clips` 라우트는 로그인 사용자라면 접근 가능하다.
* `/team-analysis-clips` 라우트를 `COACH` 전용 라우트로 막지 않는다.
* 화면 내부에서 로그인 사용자의 `memberRole`을 확인한다.
* `COACH`, `ANALYST`인 경우에만 등록/수정 관련 UI를 렌더링한다.
* `COACH`인 경우에만 삭제 UI를 렌더링한다.
* `PLAYER`는 조회와 재생 화면만 사용한다.
* 프론트에서 버튼이 보이지 않더라도 API 직접 호출은 가능할 수 있으므로 백엔드에서 다시 권한을 검증한다.

사용 기준은 다음과 같다.

```ts
const canManageTeamClip =
  member?.memberRole === "COACH" || member?.memberRole === "ANALYST";

const canDeleteTeamClip = member?.memberRole === "COACH";
```

---

## 5. 화면 흐름

### 5.1 공통 조회 흐름

1. 사용자가 로그인한다.
2. 대시보드, 선수 홈, 모바일 홈에서 팀 분석 클립 메뉴를 클릭한다.
3. `/team-analysis-clips` 경로로 이동한다.
4. 화면 진입 시 `GET /api/team-analysis-clips?page=0&size=20` API를 호출한다.
5. 팀 분석 클립 목록을 표시한다.
6. 사용자가 클립 항목을 선택한다.
7. `GET /api/team-analysis-clips/{teamClipId}` 상세 조회 API를 호출한다.
8. 상세 정보와 영상 플레이어를 표시한다.
9. 상세 응답의 `matchVideoUrl`을 재생 가능한 URL로 변환한다.
10. `<video>` 태그로 원본 영상을 불러온다.
11. 영상 메타데이터 로드 후 `currentTime`을 `startTimeSec`로 이동시킨다.
12. 사용자가 재생 버튼을 누르면 해당 구간을 재생한다.
13. 현재 시간이 `endTimeSec` 이상이 되면 영상을 일시정지한다.

### 5.2 지도자/분석관 등록 흐름

1. `COACH` 또는 `ANALYST`가 `/team-analysis-clips`에 진입한다.
2. 팀 분석 클립 등록 폼이 노출된다.
3. 프론트는 경기 영상 선택을 위해 `GET /api/match-videos?page=0&size=20` API를 호출한다.
4. 사용자는 원본 경기 영상을 선택한다.
5. 사용자는 클립 유형, 제목, 코멘트, 시작 시간, 종료 시간을 입력한다.
6. 등록 버튼을 누르면 `POST /api/management/team-analysis-clips` API를 호출한다.
7. 등록 성공 후 팀 분석 클립 목록을 다시 조회한다.
8. 새로 생성된 클립을 선택하면 상세 정보와 구간 재생을 확인할 수 있다.

### 5.3 지도자/분석관 수정 흐름

1. `COACH` 또는 `ANALYST`가 팀 분석 클립 목록에서 클립을 선택한다.
2. 상세 조회 응답을 기준으로 수정 폼에 기존 값을 채운다.
3. 클립 유형, 제목, 코멘트, 시작 시간, 종료 시간, 원본 경기 영상을 수정한다.
4. 수정 저장 버튼을 누르면 `PATCH /api/management/team-analysis-clips/{teamClipId}` API를 호출한다.
5. 수정 성공 후 상세 정보와 목록을 다시 조회한다.

### 5.4 지도자 삭제 흐름

1. `COACH`가 팀 분석 클립을 선택한다.
2. 삭제 버튼을 클릭한다.
3. 확인창을 표시한다.
4. 확인 시 `DELETE /api/coach/team-analysis-clips/{teamClipId}` API를 호출한다.
5. 삭제 성공 후 목록을 다시 조회하고 상세 선택 상태를 초기화한다.

### 5.5 선수 조회 흐름

1. `PLAYER`가 팀 분석 클립 메뉴에 진입한다.
2. 팀 분석 클립 목록을 확인한다.
3. 필요한 경우 클립 유형 또는 경기 영상 기준으로 필터링한다.
4. 클립을 선택한다.
5. 원본 영상에서 해당 구간만 재생한다.
6. 클립 제목과 코멘트를 확인한다.
7. 등록, 수정, 삭제 UI는 표시하지 않는다.

---

## 6. API 흐름

### 6.1 팀 분석 클립 목록 조회

```http
GET /api/team-analysis-clips?page=0&size=20&matchVideoId=1&clipType=ATTACK
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`
* `PLAYER`

요청 파라미터는 다음과 같다.

| 파라미터           | 필수 여부 | 설명                |
| -------------- | ----: | ----------------- |
| `page`         |    선택 | 페이지 번호            |
| `size`         |    선택 | 페이지 크기            |
| `matchVideoId` |    선택 | 특정 원본 경기 영상 기준 필터 |
| `clipType`     |    선택 | 클립 유형 필터          |

프론트 처리 기준은 다음과 같다.

* 화면 진입 시 `page=0`, `size=20`으로 조회한다.
* 경기 영상 필터가 선택되면 `matchVideoId`를 함께 전달한다.
* 클립 유형 필터가 선택되면 `clipType`을 함께 전달한다.
* 응답의 `teamClips` 배열을 목록으로 렌더링한다.
* 페이지 정보는 `page`, `size`, `totalElements`, `totalPages`를 사용한다.

응답 예시는 다음과 같다.

```json
{
  "teamClips": [
    {
      "teamClipId": 1,
      "matchVideoId": 3,
      "matchVideoTitle": "2026 춘계리그 vs 서울FC",
      "clipType": "ATTACK",
      "title": "전방 압박 성공 장면",
      "startTimeSec": 755,
      "endTimeSec": 790,
      "status": "READY",
      "writerId": 2,
      "writerName": "김지도",
      "createdAt": "2026-06-20T19:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 6.2 팀 분석 클립 상세 조회

```http
GET /api/team-analysis-clips/{teamClipId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`
* `PLAYER`

프론트 처리 기준은 다음과 같다.

* 목록에서 클립을 선택하면 상세 조회 API를 호출한다.
* 응답의 `matchVideoUrl`을 재생 URL로 변환한다.
* `startTimeSec`, `endTimeSec`를 기준으로 영상 구간 재생을 처리한다.
* `comment`가 null이면 빈 문자열 또는 안내 문구로 표시한다.

응답 예시는 다음과 같다.

```json
{
  "teamClipId": 1,
  "matchVideoId": 3,
  "matchVideoTitle": "2026 춘계리그 vs 서울FC",
  "matchVideoUrl": "/uploads/match-videos/game-001.mp4",
  "clipType": "ATTACK",
  "title": "전방 압박 성공 장면",
  "comment": "전방 3명이 동시에 압박을 시작해서 상대 빌드업을 끊은 장면",
  "startTimeSec": 755,
  "endTimeSec": 790,
  "status": "READY",
  "writerId": 2,
  "writerName": "김지도",
  "createdAt": "2026-06-20T19:00:00",
  "updatedAt": "2026-06-20T19:00:00"
}
```

### 6.3 팀 분석 클립 등록

```http
POST /api/management/team-analysis-clips
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 예시는 다음과 같다.

```json
{
  "matchVideoId": 3,
  "clipType": "ATTACK",
  "title": "전방 압박 성공 장면",
  "comment": "전방 3명이 동시에 압박을 시작해서 상대 빌드업을 끊은 장면",
  "startTimeSec": 755,
  "endTimeSec": 790
}
```

프론트 처리 기준은 다음과 같다.

* `matchVideoId`, `clipType`, `title`, `startTimeSec`, `endTimeSec`는 필수 입력으로 처리한다.
* `comment`는 선택 입력으로 처리한다.
* `startTimeSec`와 `endTimeSec`는 숫자로 변환해서 전송한다.
* 프론트에서도 `startTimeSec < endTimeSec`를 1차 검증한다.
* 실제 검증은 백엔드가 최종 책임진다.
* 등록 성공 후 목록을 다시 조회한다.

### 6.4 팀 분석 클립 수정

```http
PATCH /api/management/team-analysis-clips/{teamClipId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 예시는 다음과 같다.

```json
{
  "matchVideoId": 3,
  "clipType": "DEFENSE",
  "title": "수비 전환 지연 장면",
  "comment": "공을 잃은 뒤 1차 압박 전환이 늦은 장면",
  "startTimeSec": 840,
  "endTimeSec": 880
}
```

프론트 처리 기준은 다음과 같다.

* 상세 조회 응답을 수정 폼 초기값으로 사용한다.
* 수정 요청은 전체 수정 방식에 가깝게 모든 필드를 전송한다.
* 수정 성공 후 목록과 상세 정보를 다시 조회한다.
* 수정 중인 클립 ID가 없으면 수정 요청을 보내지 않는다.

### 6.5 팀 분석 클립 삭제

```http
DELETE /api/coach/team-analysis-clips/{teamClipId}
```

사용 가능 역할은 다음과 같다.

* `COACH`

프론트 처리 기준은 다음과 같다.

* `COACH`에게만 삭제 버튼을 보여준다.
* 삭제 전 확인창을 띄운다.
* 삭제 성공 후 목록을 다시 조회한다.
* 선택된 상세 클립 상태를 초기화한다.
* `ANALYST`, `PLAYER`에게는 삭제 버튼을 표시하지 않는다.

### 6.6 경기 영상 목록 조회

팀 분석 클립 등록 폼에서 원본 경기 영상을 선택하기 위해 기존 경기 영상 목록 API를 사용한다.

```http
GET /api/match-videos?page=0&size=20
```

사용 목적은 다음과 같다.

* 클립 생성 대상 원본 경기 영상 선택
* 경기 영상 제목 표시
* 경기 영상 길이 `durationSec` 기반 프론트 1차 시간 검증
* 상세 재생 URL 확인은 팀 분석 클립 상세 API 응답의 `matchVideoUrl`을 우선 사용한다.

---

## 7. 타입 설계 방향

팀 분석 클립 프론트 타입 파일은 다음 위치에 둔다.

```text
frontend/src/types/teamAnalysisClip.ts
```

### 7.1 클립 유형 타입

```ts
export type TeamAnalysisClipType =
  | "HIGHLIGHT"
  | "ATTACK"
  | "DEFENSE"
  | "GOAL"
  | "CONCEDED"
  | "OFFSIDE"
  | "SETPIECE"
  | "ETC";
```

### 7.2 클립 상태 타입

```ts
export type TeamAnalysisClipStatus = "UPLOADING" | "READY" | "FAILED";
```

초기 메타데이터 기반 클립은 백엔드에서 `READY` 상태로 저장하는 것을 기준으로 한다.

### 7.3 목록 응답 타입

```ts
export type TeamAnalysisClipListItem = {
  teamClipId: number;
  matchVideoId: number;
  matchVideoTitle: string;
  clipType: TeamAnalysisClipType;
  title: string;
  startTimeSec: number;
  endTimeSec: number;
  status: TeamAnalysisClipStatus;
  writerId: number;
  writerName: string;
  createdAt: string;
};
```

### 7.4 페이지 응답 타입

```ts
export type TeamAnalysisClipPageResponse = {
  teamClips: TeamAnalysisClipListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
```

### 7.5 상세 응답 타입

```ts
export type TeamAnalysisClipDetailResponse = {
  teamClipId: number;
  matchVideoId: number;
  matchVideoTitle: string;
  matchVideoUrl: string;
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
  status: TeamAnalysisClipStatus;
  writerId: number;
  writerName: string;
  createdAt: string;
  updatedAt: string;
};
```

### 7.6 등록/수정 요청 타입

```ts
export type TeamAnalysisClipRequest = {
  matchVideoId: number;
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
};
```

### 7.7 검색 조건 타입

```ts
export type TeamAnalysisClipSearchParams = {
  page: number;
  size: number;
  matchVideoId?: number;
  clipType?: TeamAnalysisClipType;
};
```

---

## 8. 라우팅 및 메뉴 연결

### 8.1 라우트 상수

`frontend/src/constants/routes.ts`에 다음 상수를 추가한다.

```ts
TEAM_ANALYSIS_CLIP: "/team-analysis-clips",
```

전체 경로 문자열은 컴포넌트에서 직접 작성하지 않고 `ROUTES.TEAM_ANALYSIS_CLIP`을 사용한다.

### 8.2 App 라우팅

`frontend/src/App.tsx`에 팀 분석 클립 페이지 라우트를 추가한다.

접근 정책은 다음과 같다.

* 로그인 사용자만 접근 가능하다.
* `COACH`, `ANALYST`, `PLAYER` 모두 접근 가능하다.
* 역할별 UI 차이는 페이지 내부에서 처리한다.

### 8.3 메뉴 연결

다음 화면에서 팀 분석 클립 메뉴 버튼을 `ROUTES.TEAM_ANALYSIS_CLIP`로 연결한다.

```text
frontend/src/pages/DashboardHomePage.tsx
frontend/src/pages/PlayerHomePage.tsx
frontend/src/pages/MobileHomePage.tsx
```

노출 기준은 다음과 같다.

* `COACH`: 대시보드에서 팀 분석 클립 관리 메뉴 표시
* `ANALYST`: 대시보드에서 팀 분석 클립 관리 메뉴 표시
* `PLAYER`: 선수 홈과 모바일 홈에서 팀 분석 영상 메뉴 표시

---

## 9. 주요 파일 설계 방향

이번 기능에서 생성 또는 수정할 가능성이 있는 파일은 다음과 같다.

```text
frontend/src/types/teamAnalysisClip.ts
frontend/src/api/teamAnalysisClipApi.ts
frontend/src/pages/TeamAnalysisClipPage.tsx
frontend/src/constants/routes.ts
frontend/src/App.tsx
frontend/src/pages/DashboardHomePage.tsx
frontend/src/pages/PlayerHomePage.tsx
frontend/src/pages/MobileHomePage.tsx
frontend/src/utils/videoUrl.ts
```

각 파일 역할은 다음과 같다.

### 9.1 `teamAnalysisClip.ts`

팀 분석 클립 화면과 API 연동에서 사용하는 요청, 응답, 검색 조건 타입을 정의한다.

### 9.2 `teamAnalysisClipApi.ts`

팀 분석 클립 목록 조회, 상세 조회, 등록, 수정, 삭제 API 요청 함수를 모아둔다.

### 9.3 `TeamAnalysisClipPage.tsx`

로그인한 사용자의 역할에 따라 팀 분석 클립 조회와 관리 기능을 제공하는 화면이다.

### 9.4 `routes.ts`

팀 분석 클립 화면 경로 상수를 관리한다.

### 9.5 `App.tsx`

팀 분석 클립 페이지 라우팅을 추가한다.

### 9.6 홈 화면 파일

대시보드, 선수 홈, 모바일 홈에서 팀 분석 클립 메뉴 이동 버튼을 연결한다.

### 9.7 `videoUrl.ts`

백엔드가 상대 경로로 반환한 영상 URL을 프론트 재생 가능한 전체 URL로 변환한다.

---

## 10. 영상 구간 재생 정책

팀 분석 클립은 실제 클립 파일이 아니라 원본 경기 영상의 특정 구간 메타데이터다.

따라서 프론트에서는 다음 방식으로 재생한다.

1. 상세 조회 응답의 `matchVideoUrl`을 `<video>` 태그 `src`에 연결한다.
2. 영상 메타데이터가 로드되면 `video.currentTime = startTimeSec`로 이동한다.
3. 사용자가 재생 버튼을 누르면 원본 영상이 해당 구간부터 재생된다.
4. `timeupdate` 이벤트에서 현재 시간이 `endTimeSec` 이상인지 확인한다.
5. 현재 시간이 `endTimeSec` 이상이면 `video.pause()`를 호출한다.
6. 필요하면 다시 보기 버튼으로 `currentTime`을 `startTimeSec`로 되돌린다.

주의사항은 다음과 같다.

* 영상 길이보다 큰 시간으로 이동하면 브라우저 동작이 불안정할 수 있다.
* 프론트에서 1차 검증은 하되, 최종 시간 검증은 백엔드가 담당한다.
* `/uploads/match-videos/**` 직접 접근은 현재 MVP용 방식이다.
* 실제 서비스 운영 전에는 권한 검증이 들어간 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.

---

## 11. 예외 상황

### 11.1 목록 조회 실패

발생 가능 상황은 다음과 같다.

* Access Token 만료
* 서버 오류
* 잘못된 페이지 요청
* 잘못된 클립 유형

프론트 처리 기준은 다음과 같다.

* 에러 메시지를 화면에 표시한다.
* 인증 오류는 기존 axios 공통 처리 정책을 따른다.
* 목록 영역에는 빈 상태 메시지를 표시한다.

### 11.2 상세 조회 실패

발생 가능 상황은 다음과 같다.

* 존재하지 않는 팀 분석 클립
* 삭제된 팀 분석 클립
* 연결된 원본 경기 영상이 삭제됨
* 권한 없는 사용자 접근

프론트 처리 기준은 다음과 같다.

* 상세 영역에 에러 메시지를 표시한다.
* 필요하면 목록을 다시 조회한다.
* 선택된 클립 상태를 초기화한다.

### 11.3 등록 실패

발생 가능 상황은 다음과 같다.

* `matchVideoId` 미선택
* `clipType` 미선택
* 제목 미입력
* 시작 시간이 종료 시간보다 크거나 같음
* 원본 경기 영상이 삭제됨
* 선수 계정이 직접 API 호출
* 서버 검증 실패

프론트 처리 기준은 다음과 같다.

* 필수 입력값은 요청 전 1차 검증한다.
* 백엔드 에러 메시지를 화면에 표시한다.
* 등록 실패 시 입력값은 유지한다.

### 11.4 수정 실패

발생 가능 상황은 다음과 같다.

* 수정 대상 클립이 삭제됨
* 잘못된 시간 구간
* 권한 없는 사용자 접근
* 연결된 원본 경기 영상 문제

프론트 처리 기준은 다음과 같다.

* 백엔드 에러 메시지를 표시한다.
* 수정 실패 시 기존 입력값을 유지한다.
* 필요하면 상세 정보를 다시 조회한다.

### 11.5 삭제 실패

발생 가능 상황은 다음과 같다.

* 이미 삭제된 클립
* 존재하지 않는 클립
* `ANALYST` 또는 `PLAYER`가 직접 삭제 API 호출
* 서버 오류

프론트 처리 기준은 다음과 같다.

* `COACH`에게만 삭제 버튼을 보여준다.
* 삭제 실패 메시지를 표시한다.
* 실패 후 목록을 다시 조회할 수 있게 한다.

### 11.6 영상 재생 실패

발생 가능 상황은 다음과 같다.

* 영상 URL이 잘못됨
* 원본 영상 파일이 서버에 없음
* 브라우저가 영상 파일을 불러오지 못함
* 운영 환경에서 파일 접근 권한 문제가 발생함

프론트 처리 기준은 다음과 같다.

* 영상 영역에 재생 실패 안내 문구를 표시한다.
* 상세 정보는 그대로 표시한다.
* 현재 MVP의 직접 파일 접근 방식은 운영 전 보완 대상임을 문서에 유지한다.

---

## 12. 구현 순서

1. `docs/20_team_analysis_clip_frontend_integration_requirements.md` 문서 추가
2. `frontend/src/types/teamAnalysisClip.ts` 작성
3. `frontend/src/api/teamAnalysisClipApi.ts` 작성
4. `frontend/src/constants/routes.ts`에 `TEAM_ANALYSIS_CLIP` 추가
5. `frontend/src/App.tsx`에 팀 분석 클립 라우트 추가
6. `frontend/src/pages/TeamAnalysisClipPage.tsx` 기본 구조 작성
7. 팀 분석 클립 목록 조회 UI 구현
8. 팀 분석 클립 상세 조회 UI 구현
9. 원본 영상 URL 변환 및 구간 재생 기능 구현
10. `COACH`, `ANALYST` 전용 등록 폼 구현
11. 팀 분석 클립 등록 API 연결
12. 팀 분석 클립 수정 기능 구현
13. `COACH` 전용 삭제 기능 구현
14. 경기 영상 목록 API를 활용한 원본 경기 영상 선택 기능 구현
15. 대시보드, 선수 홈, 모바일 홈 메뉴 연결
16. 권한별 버튼 노출 확인
17. 목록/상세/등록/수정/삭제 API 연동 테스트
18. 선수 계정 조회 전용 화면 테스트
19. 분석관 삭제 버튼 미노출 테스트
20. 빌드 테스트

---

## 13. 테스트 기준

### 13.1 지도자 `COACH`

확인할 내용은 다음과 같다.

* 팀 분석 클립 목록 조회 가능
* 팀 분석 클립 상세 조회 가능
* 원본 영상 구간 재생 가능
* 팀 분석 클립 등록 가능
* 팀 분석 클립 수정 가능
* 팀 분석 클립 삭제 가능
* 삭제 후 목록에서 사라짐

### 13.2 분석관 `ANALYST`

확인할 내용은 다음과 같다.

* 팀 분석 클립 목록 조회 가능
* 팀 분석 클립 상세 조회 가능
* 원본 영상 구간 재생 가능
* 팀 분석 클립 등록 가능
* 팀 분석 클립 수정 가능
* 삭제 버튼 미노출
* 직접 삭제 API 호출은 백엔드에서 실패해야 함

### 13.3 선수 `PLAYER`

확인할 내용은 다음과 같다.

* 팀 분석 클립 목록 조회 가능
* 팀 분석 클립 상세 조회 가능
* 원본 영상 구간 재생 가능
* 등록 폼 미노출
* 수정 버튼 미노출
* 삭제 버튼 미노출
* 직접 등록/수정/삭제 API 호출은 백엔드에서 실패해야 함

### 13.4 공통

확인할 내용은 다음과 같다.

* Access Token이 없으면 `/login`으로 이동
* 잘못된 클립 유형 필터 처리
* 존재하지 않는 클립 상세 조회 실패 처리
* 영상 URL 변환 정상 동작
* `startTimeSec`부터 재생 시작
* `endTimeSec` 도달 시 재생 정지
* 새로고침 후 로그인 상태 복원
* 빌드 오류 없음

테스트 결과는 기능 완료 시 최종 문서에 반영한다.

---

## 14. DB 설계 방향

이번 기능은 프론트 연동 기능이므로 DB를 새로 추가하지 않는다.

사용하는 주요 테이블은 다음과 같다.

```text
game_video_upload
team_video_clip
```

### 14.1 `game_video_upload`

사용 목적은 다음과 같다.

* 팀 분석 클립의 원본 경기 영상
* 원본 영상 제목 표시
* 원본 영상 URL 제공
* 원본 영상 길이 기반 시간 검증 기준 제공

### 14.2 `team_video_clip`

사용 목적은 다음과 같다.

* 팀 분석 클립 메타데이터 저장
* 원본 경기 영상 ID 연결
* 클립 유형 저장
* 제목과 코멘트 저장
* 시작 시간과 종료 시간 저장
* 작성자 정보 연결
* 소프트 삭제 상태 관리

프론트는 DB에 직접 접근하지 않고 백엔드 API 응답만 사용한다.

---

## 15. 추후 확장 가능성

추후 아래 기능으로 확장할 수 있다.

* 팀 분석 클립 드로잉 프론트 연동
* 팀 분석 클립별 북마크 기능
* 클립별 댓글 또는 피드백 확인 기능
* 클립 조회 기록 기능
* 클립 유형별 통계
* 경기 영상 상세 화면 안에서 바로 클립 생성
* 영상 타임라인 기반 구간 선택 UI
* 키보드 단축키 기반 프레임 이동
* 0.25x, 0.5x, 1x 재생 속도 제어
* 실제 클립 파일 생성 및 인코딩
* 썸네일 생성
* CDN 연동
* 권한 검증이 포함된 스트리밍 API
* Signed URL 기반 영상 접근 제어
* 팀 분석 클립과 선수 개인 분석 클립 통합 편집기

---

## 16. 주의사항

* 프론트 버튼 숨김은 보안이 아니므로 백엔드 권한 검증을 반드시 유지한다.
* `PLAYER`는 팀 분석 클립을 조회할 수 있지만 등록, 수정, 삭제는 할 수 없다.
* `ANALYST`는 등록과 수정이 가능하지만 삭제는 할 수 없다.
* 삭제 버튼은 `COACH`에게만 보여준다.
* 실제 영상 파일을 자르지 않으므로 클립 재생은 원본 영상 URL과 시간 메타데이터에 의존한다.
* 프론트에서 `startTimeSec < endTimeSec`를 검증하더라도 백엔드 검증이 최종 기준이다.
* 현재 MVP의 `/uploads/match-videos/**` 직접 접근 방식은 운영 전 반드시 보완해야 한다.
* 실제 서비스 운영 단계에서는 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.
* 파일 상단에는 역할 주석을 작성한다.
* `FormEvent`는 사용하지 않는다.
* form submit 이벤트 타입은 `{ preventDefault: () => void }`로 작성한다.
