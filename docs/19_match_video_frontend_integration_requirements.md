# 19. 경기 영상 프론트 연동 요구사항

## 1. 결론

경기 영상 프론트 연동 기능은 지도자와 분석관이 실제 동영상 파일과 경기 메타데이터를 업로드하고, 지도자/분석관/선수가 업로드된 경기 원본 영상을 화면에서 조회 및 재생할 수 있게 하는 기능이다.

이번 구현 기준은 다음과 같다.

* 프론트는 React + TypeScript + Vite를 사용한다.
* 인증은 기존 로그인/회원가입 프론트 구조를 사용한다.
* Access Token은 `localStorage`에 저장하고, `axiosInstance`에서 Authorization 헤더에 자동으로 추가한다.
* 경기 영상 화면 경로는 `/match-videos`를 사용한다.
* 라우트 경로는 문자열로 직접 작성하지 않고 `ROUTES.MATCH_VIDEO` 상수를 사용한다.
* `ROUTES` 상수는 `frontend/src/components/constants/routes.ts`에서 관리한다.
* 모든 로그인 사용자는 경기 영상 목록과 상세를 조회할 수 있다.
* `COACH`는 경기 영상 조회, 파일 업로드, 메타데이터 등록, 수정, 삭제 UI를 볼 수 있다.
* `ANALYST`는 경기 영상 조회, 파일 업로드, 메타데이터 등록, 수정 UI를 볼 수 있고 삭제 UI는 볼 수 없다.
* `PLAYER`는 경기 원본 영상 조회와 재생 화면만 볼 수 있다.
* 버튼 숨김은 사용자 경험을 위한 처리이며, 실제 권한 검증은 백엔드에서 처리한다.
* 이번 구현 범위는 실제 동영상 파일 업로드와 업로드된 영상의 화면 재생까지 포함한다.
* 이번 구현에서는 실제 영상 자르기, 인코딩, 썸네일 생성, CDN 연동은 제외한다.
* 업로드된 파일은 초기에는 서버 로컬 디렉터리에 저장한다.
* 백엔드는 저장된 파일에 접근 가능한 URL을 생성하고, 기존 `game_video_upload.url` 컬럼에 저장한다.
* 프론트는 파일 선택 후 `multipart/form-data`로 업로드 요청을 보낸다.
* 상세 조회 응답의 `url`을 `<video>` 태그의 `src`로 연결해 재생한다.
* 드로잉은 별도 메뉴로 만들지 않고 추후 팀 분석 클립/선수 개인 분석 클립 편집 화면 내부 기능으로 처리한다.
* 새 프론트 파일 상단에는 해당 파일 역할을 설명하는 한 줄 주석을 작성한다.
* `FormEvent`는 사용하지 않고 form submit 이벤트는 `{ preventDefault: () => void }` 타입으로 작성한다.

---

## 2. 기능 목적

경기 영상 프론트 연동의 목적은 지도자와 분석관이 경기 원본 영상을 실제 파일로 업로드하고, 모든 로그인 사용자가 업로드된 경기 원본 영상을 조회 및 재생할 수 있게 하는 것이다.

이 기능은 이후 영상 편집, 팀 분석 클립, 선수 개인 분석 클립 생성의 출발점이다.

초기 프론트 구현에서는 다음 기준을 우선한다.

* 지도자와 분석관은 PC에서 경기 영상을 쉽게 업로드할 수 있어야 한다.
* 선수는 모바일에서 경기 원본 영상을 빠르게 확인할 수 있어야 한다.
* 실제 동영상 파일을 선택해 서버에 업로드할 수 있어야 한다.
* 업로드 성공 후 백엔드는 재생 가능한 영상 URL을 저장해야 한다.
* 경기 영상 상세 화면에서 원본 영상을 재생할 수 있어야 한다.
* 삭제는 `COACH`에게만 노출한다.
* 분석관은 삭제 권한이 없으므로 삭제 버튼을 노출하지 않는다.
* 선수는 등록, 수정, 삭제 버튼 없이 조회와 재생 화면만 사용한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

가능한 기능은 다음과 같다.

* 경기 영상 목록 조회
* 경기 영상 상세 조회
* 경기 영상 파일 업로드
* 경기 메타데이터 등록
* 경기 영상 정보 수정
* 경기 영상 삭제
* 업로드된 경기 원본 영상 재생

화면에서는 다음 UI가 노출된다.

* 경기 영상 목록
* 경기 영상 상세 정보
* 영상 재생 영역
* 동영상 파일 선택 input
* 경기 영상 등록 폼
* 수정 버튼
* 삭제 버튼

### 3.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

* 경기 영상 목록 조회
* 경기 영상 상세 조회
* 경기 영상 파일 업로드
* 경기 메타데이터 등록
* 경기 영상 정보 수정
* 업로드된 경기 원본 영상 재생

노출하지 않는 UI는 다음과 같다.

* 삭제 버튼

분석관에게 삭제 UI를 노출하지 않는 이유는 원본 경기 영상이 팀 분석 클립과 선수 개인 분석 클립의 기준 데이터이기 때문이다.

### 3.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

* 경기 영상 목록 조회
* 경기 영상 상세 조회
* 원본 경기 영상 재생

노출하지 않는 UI는 다음과 같다.

* 동영상 파일 선택 input
* 경기 영상 등록 폼
* 수정 버튼
* 삭제 버튼

---

## 4. 프론트 권한 노출 정책

| 역할        | 목록 조회 | 상세 조회 | 재생 | 파일 업로드 UI | 수정 UI | 삭제 UI |
| --------- | ----: | ----: | -: | --------: | ----: | ----: |
| `COACH`   |    가능 |    가능 | 가능 |        노출 |    노출 |    노출 |
| `ANALYST` |    가능 |    가능 | 가능 |        노출 |    노출 |   미노출 |
| `PLAYER`  |    가능 |    가능 | 가능 |       미노출 |   미노출 |   미노출 |

권한 처리 기준은 다음과 같다.

* `/match-videos` 라우트는 로그인 사용자라면 접근 가능하다.
* `/match-videos` 라우트를 `COACH` 전용 `RoleRoute`로 막지 않는다.
* 화면 내부에서 로그인 사용자의 `memberRole`을 확인한다.
* `COACH` 또는 `ANALYST`인 경우 파일 업로드/등록/수정 UI를 렌더링한다.
* `COACH`인 경우에만 삭제 UI를 렌더링한다.
* `PLAYER`는 조회와 재생 화면만 사용한다.
* 프론트에서 버튼이 보이지 않더라도 API 직접 호출은 가능할 수 있으므로 백엔드에서 다시 권한을 검증한다.

사용 기준은 다음과 같다.

```tsx
const isCoach = member?.memberRole === "COACH";
const canManageMatchVideo =
  member?.memberRole === "COACH" || member?.memberRole === "ANALYST";
const canDeleteMatchVideo = member?.memberRole === "COACH";
```

---

## 5. 화면 흐름

### 5.1 공통 조회 흐름

1. 사용자가 로그인한다.
2. 대시보드, 선수 홈, 모바일 홈에서 경기 원본 영상 메뉴를 클릭한다.
3. `ROUTES.MATCH_VIDEO` 경로로 이동한다.
4. 경기 영상 화면 진입 시 `GET /api/match-videos?page=0&size=20`을 호출한다.
5. 응답받은 경기 영상 목록을 화면에 표시한다.
6. 사용자가 경기 영상을 선택하면 `GET /api/match-videos/{matchVideoId}`를 호출한다.
7. 상세 정보와 영상 URL을 화면에 표시한다.
8. 영상 URL을 `<video>` 태그의 `src`로 연결해 원본 영상을 재생한다.

### 5.2 지도자/분석관 업로드 흐름

1. `COACH` 또는 `ANALYST`가 `/match-videos`에 진입한다.
2. 경기 영상 등록 폼과 동영상 파일 선택 input이 노출된다.
3. 사용자가 동영상 파일을 선택한다.
4. 경기 제목, 경기일, 장소, 홈 점수, 원정 점수, 경기 결과를 입력한다.
5. 등록 버튼을 누르면 프론트는 `FormData`를 생성한다.
6. 프론트는 동영상 파일과 경기 메타데이터를 `multipart/form-data`로 전송한다.
7. 백엔드는 파일을 서버 로컬 디렉터리에 저장한다.
8. 백엔드는 저장된 파일의 접근 URL을 생성한다.
9. 백엔드는 `game_video_upload.url`에 영상 URL을 저장한다.
10. 저장 성공 후 프론트는 경기 영상 목록을 다시 조회한다.
11. 등록된 영상을 선택하면 상세 정보와 업로드된 영상을 재생할 수 있다.

### 5.3 지도자/분석관 수정 흐름

1. `COACH` 또는 `ANALYST`가 기존 경기 영상의 수정 버튼을 누른다.
2. 기존 경기 메타데이터가 수정 폼에 채워진다.
3. 기본 수정은 경기 메타데이터 수정만 처리한다.
4. 파일 교체는 이번 MVP에서 필수로 구현하지 않는다.
5. 수정 버튼을 누르면 `PATCH /api/management/match-videos/{matchVideoId}`를 호출한다.
6. 수정 성공 후 목록과 상세 정보를 다시 조회한다.

### 5.4 지도자 삭제 흐름

1. `COACH`가 삭제 버튼을 누른다.
2. 프론트는 확인창을 표시한다.
3. 확인 시 `DELETE /api/coach/match-videos/{matchVideoId}`를 호출한다.
4. 백엔드는 DB에서 `is_deleted = true`로 변경한다.
5. 초기 MVP에서는 로컬 저장소의 실제 영상 파일은 삭제하지 않아도 된다.
6. 삭제 성공 후 프론트는 목록을 다시 조회하고 선택된 상세 상태를 비운다.

### 5.5 선수 조회 흐름

1. `PLAYER`가 `/match-videos`에 진입한다.
2. 경기 영상 목록만 확인한다.
3. 등록 폼, 파일 선택 input, 수정 버튼, 삭제 버튼은 화면에 보이지 않는다.
4. 경기 영상을 선택해 상세 정보와 원본 영상을 확인한다.
5. 영상 URL이 재생 가능하면 `<video>` 영역에서 바로 재생한다.

---

## 6. API 흐름

## 6.1 최종 API 목록

```http
GET    /api/match-videos?page=0&size=20
GET    /api/match-videos/{matchVideoId}
POST   /api/management/match-videos
PATCH  /api/management/match-videos/{matchVideoId}
DELETE /api/coach/match-videos/{matchVideoId}
GET    /uploads/match-videos/{storedFileName}
```

`POST /api/management/match-videos`는 이번 범위부터 JSON 요청이 아니라 `multipart/form-data` 요청으로 변경한다.

---

### 6.2 경기 영상 목록 조회

```http
GET /api/match-videos?page=0&size=20
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`
* `PLAYER`

프론트 처리 기준은 다음과 같다.

* 화면 진입 시 기본값으로 `page=0`, `size=20`을 사용한다.
* 응답은 `MatchVideoPageResponse` 타입으로 처리한다.
* 목록은 백엔드 정렬 기준을 그대로 사용한다.
* 페이지 이동이 필요하면 응답의 `page`, `totalPages` 기준으로 처리한다.

---

### 6.3 경기 영상 상세 조회

```http
GET /api/match-videos/{matchVideoId}
```

사용 목적은 목록에서 선택한 경기 영상의 상세 정보와 영상 URL을 확인하는 것이다.

프론트 처리 기준은 다음과 같다.

* 상세 조회 성공 시 선택된 경기 영상 상태를 갱신한다.
* `url` 값이 있으면 영상 재생 영역에 연결한다.
* `status`가 `READY`가 아니면 영상 재생보다 상태 안내 메시지를 우선 표시한다.

---

### 6.4 경기 영상 파일 업로드 및 등록

```http
POST /api/management/match-videos
Content-Type: multipart/form-data
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 파트 기준은 다음과 같다.

| 파트명           | 타입     | 필수 여부 | 설명                    |
| ------------- | ------ | ----: | --------------------- |
| `videoFile`   | File   |    필수 | 업로드할 경기 영상 파일         |
| `title`       | String |    필수 | 경기 제목                 |
| `gameDate`    | String |    필수 | 경기 날짜와 시간             |
| `place`       | String |    필수 | 경기 장소                 |
| `homeScore`   | Number |    필수 | 홈팀 점수                 |
| `awayScore`   | Number |    필수 | 원정팀 점수                |
| `matchResult` | String |    필수 | `WIN`, `DRAW`, `LOSS` |

프론트 `FormData` 구성 예시는 다음과 같다.

```ts
const formData = new FormData();
formData.append("videoFile", selectedFile);
formData.append("title", request.title);
formData.append("gameDate", request.gameDate);
formData.append("place", request.place);
formData.append("homeScore", String(request.homeScore));
formData.append("awayScore", String(request.awayScore));
formData.append("matchResult", request.matchResult);
```

백엔드 처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. `videoFile`이 비어 있는지 확인한다.
4. 영상 파일 확장자와 MIME 타입을 검증한다.
5. 파일 크기 제한을 검증한다.
6. 경기 메타데이터 필수값을 검증한다.
7. 점수 값이 음수가 아닌지 검증한다.
8. 경기 결과 값이 허용된 Enum인지 검증한다.
9. 업로드 파일명을 안전한 저장 파일명으로 변경한다.
10. 서버 로컬 업로드 디렉터리에 파일을 저장한다.
11. 저장된 파일 접근 URL을 생성한다.
12. `game_video_upload.url`에 접근 URL을 저장한다.
13. `status`는 `READY`로 저장한다.
14. `member_id`에 등록한 회원 ID를 저장한다.
15. 경기 영상 정보를 저장한다.
16. 생성된 경기 영상 ID를 반환한다.

응답 예시는 다음과 같다.

```json
{
  "matchVideoId": 1,
  "message": "경기 영상이 업로드되었습니다."
}
```

---

### 6.5 경기 영상 정보 수정

```http
PATCH /api/management/match-videos/{matchVideoId}
Content-Type: application/json
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

이번 MVP에서 수정 API는 파일 교체가 아니라 경기 메타데이터 수정만 처리한다.

요청 필드 기준은 다음과 같다.

```json
{
  "title": "2026 춘계리그 vs 서울FC",
  "gameDate": "2026-06-20T15:00:00",
  "place": "메인 운동장",
  "homeScore": 2,
  "awayScore": 1,
  "matchResult": "WIN"
}
```

수정 성공 후 프론트는 다음을 처리한다.

1. 목록을 다시 조회한다.
2. 수정한 경기 영상 상세를 다시 조회한다.
3. 수정 폼 상태를 초기화한다.

파일 교체 기능은 추후 별도 API로 분리한다.

---

### 6.6 경기 영상 삭제

```http
DELETE /api/coach/match-videos/{matchVideoId}
```

사용 가능 역할은 다음과 같다.

* `COACH`

삭제 성공 후 프론트는 다음을 처리한다.

1. 목록을 다시 조회한다.
2. 선택된 경기 영상 상세 상태를 비운다.
3. 삭제 완료 메시지를 표시한다.

초기 MVP에서는 DB의 `is_deleted`만 `true`로 변경하고 실제 로컬 파일 삭제는 하지 않는다.

---

### 6.7 업로드된 영상 파일 조회

```http
GET /uploads/match-videos/{storedFileName}
```

사용 목적은 브라우저 `<video>` 태그에서 업로드된 영상을 재생하는 것이다.

초기 구현 기준은 다음과 같다.

* 업로드된 파일은 서버 로컬 디렉터리에 저장한다.
* Spring MVC 정적 리소스 설정으로 `/uploads/match-videos/**` 경로를 제공한다.
* 상세 조회 응답의 `url`은 이 정적 리소스 경로를 포함한다.
* 프론트는 별도 인증 헤더 없이 `<video src={url}>`로 재생한다.

주의사항은 다음과 같다.

* 이 방식은 초기 개발용으로 적합하다.
* 운영 서비스에서는 원본 영상 접근 권한, URL 노출, 저장소 비용, CDN, 서명 URL을 별도로 검토해야 한다.
* `<video>` 태그는 Authorization 헤더를 쉽게 붙일 수 없기 때문에, 운영 단계에서는 공개 URL 또는 서명 URL 방식이 필요하다.

---

## 7. 타입 설계 방향

경기 영상 프론트 타입 파일은 다음 위치에 둔다.

```text
frontend/src/types/matchVideo.ts
```

주요 타입은 다음과 같다.

```ts
export type MatchResult = "WIN" | "DRAW" | "LOSS";

export type MatchVideoStatus = "UPLOADING" | "READY" | "FAILED";
```

경기 영상 목록 응답 타입은 다음과 같다.

```ts
export type MatchVideoListResponse = {
  matchVideoId: number;
  title: string;
  gameDate: string;
  place: string;
  homeScore: number;
  awayScore: number;
  matchResult: MatchResult;
  status: MatchVideoStatus;
  createdAt: string;
};
```

경기 영상 상세 응답 타입은 다음과 같다.

```ts
export type MatchVideoDetailResponse = {
  matchVideoId: number;
  url: string;
  title: string;
  gameDate: string;
  place: string;
  homeScore: number;
  awayScore: number;
  matchResult: MatchResult;
  status: MatchVideoStatus;
  uploaderId: number;
  uploaderName: string;
  createdAt: string;
  updatedAt: string;
};
```

경기 영상 페이지 응답 타입은 다음과 같다.

```ts
export type MatchVideoPageResponse = {
  matchVideos: MatchVideoListResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
```

경기 영상 업로드 요청 타입은 다음과 같다.

```ts
export type MatchVideoUploadRequest = {
  videoFile: File;
  title: string;
  gameDate: string;
  place: string;
  homeScore: number;
  awayScore: number;
  matchResult: MatchResult;
};
```

경기 영상 수정 요청 타입은 다음과 같다.

```ts
export type MatchVideoUpdateRequest = {
  title: string;
  gameDate: string;
  place: string;
  homeScore: number;
  awayScore: number;
  matchResult: MatchResult;
};
```

경기 영상 업로드 응답 타입은 다음과 같다.

```ts
export type MatchVideoCreateResponse = {
  matchVideoId: number;
  message: string;
};
```

---

## 8. 백엔드 설계 방향

이번 기능은 프론트 연동이지만, 실제 파일 업로드를 포함하므로 백엔드 수정이 필요하다.

### 8.1 저장 방식

초기 MVP에서는 로컬 파일 저장 방식을 사용한다.

예시 저장 경로는 다음과 같다.

```text
backend/uploads/match-videos
```

DB에는 파일 자체를 저장하지 않고, 브라우저가 접근 가능한 URL만 저장한다.

```text
game_video_upload.url = /uploads/match-videos/{storedFileName}
```

### 8.2 파일명 정책

사용자가 업로드한 원본 파일명을 그대로 저장하지 않는다.

저장 파일명은 다음 방식으로 만든다.

```text
UUID + 원본 확장자
```

예시:

```text
7c2b9b84-930b-4cc3-bb0b-223c52d9a456.mp4
```

원본 파일명은 현재 DB에 저장 컬럼이 없으므로 이번 MVP에서는 저장하지 않는다.

### 8.3 허용 파일 형식

초기 허용 형식은 다음으로 제한한다.

```text
video/mp4
.mp4
```

이유는 브라우저 기본 재생 호환성과 구현 단순성을 우선하기 때문이다.

추후 필요하면 다음 형식을 확장한다.

```text
video/webm
.mov
.m4v
```

### 8.4 파일 크기 제한

초기 개발 테스트 기준으로 최대 파일 크기를 제한한다.

권장 초기값은 다음과 같다.

```text
200MB
```

운영 서비스에서는 실제 경기 영상 용량이 훨씬 클 수 있으므로, 추후 클라우드 스토리지 직접 업로드 또는 멀티파트 업로드 구조로 전환한다.

### 8.5 정적 리소스 제공

Spring Boot에서 업로드 디렉터리를 정적 리소스로 매핑한다.

예상 경로는 다음과 같다.

```text
/upload/match-videos/**
또는
/uploads/match-videos/**
```

이번 문서에서는 최종 경로를 다음으로 통일한다.

```text
/uploads/match-videos/**
```

프론트 상세 조회 응답의 `url`이 상대 경로라면 `VITE_API_BASE_URL`을 붙여 재생 URL을 만든다.

---

## 9. 라우팅 및 메뉴 연결

경기 영상 경로는 `frontend/src/components/constants/routes.ts`에서 관리한다.

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
  NOTICE: "/notices",
  MATCH_VIDEO: "/match-videos",
} as const;
```

라우트 등록 시 문자열 `"/match-videos"`를 직접 사용하지 않고 `ROUTES.MATCH_VIDEO`를 사용한다.

```tsx
<Route
  path={ROUTES.MATCH_VIDEO}
  element={
    <ProtectedRoute>
      <MatchVideoPage />
    </ProtectedRoute>
  }
/>
```

홈 화면 메뉴 이동도 `ROUTES.MATCH_VIDEO`를 사용한다.

```tsx
<button type="button" onClick={() => navigate(ROUTES.MATCH_VIDEO)}>
  경기 원본 영상
</button>
```

연결 대상 화면은 다음과 같다.

```text
frontend/src/pages/DashboardHomePage.tsx
frontend/src/pages/PlayerHomePage.tsx
frontend/src/pages/MobileHomePage.tsx
frontend/src/App.tsx
```

---

## 10. DB 설계 방향

이번 작업에서는 신규 테이블을 추가하지 않는다.

기존 백엔드 경기 영상 기능에서 사용하는 테이블은 다음과 같다.

```text
game_video_upload
member
```

프론트는 DB에 직접 접근하지 않고 백엔드 API 응답 필드만 사용한다.

DB에는 파일 자체가 아니라 파일 접근 URL만 저장한다.

DB 컬럼명과 API 필드명은 다음 기준으로 관리한다.

```text
DB 컬럼명: url
DTO 필드명: url
프론트 타입 필드명: url
JSON 필드명: url
```

```text
DB 컬럼명: game_date
DTO 필드명: gameDate
프론트 타입 필드명: gameDate
JSON 필드명: gameDate
```

이번 MVP에서는 다음 컬럼을 추가하지 않는다.

```text
original_file_name
stored_file_name
file_size
content_type
duration_sec
thumbnail_url
storage_type
```

단, 실제 운영 전에는 위 컬럼 추가를 다시 검토한다.

---

## 11. 예외 상황

### 11.1 목록 조회 실패

상황:

* Access Token 만료
* 서버 오류
* 네트워크 오류

처리:

* 에러 메시지를 화면에 표시한다.
* 401 응답은 기존 `axiosInstance` 인증 실패 처리 정책을 따른다.

### 11.2 상세 조회 실패

상황:

* 경기 영상이 존재하지 않음
* 삭제된 경기 영상
* 권한 없음
* 서버 오류

처리:

* 선택된 상세 상태를 비운다.
* 에러 메시지를 표시한다.
* 목록을 다시 조회해 최신 상태를 맞춘다.

### 11.3 파일 업로드 실패

상황:

* 파일을 선택하지 않음
* 허용되지 않은 확장자
* 허용되지 않은 MIME 타입
* 파일 크기 초과
* 서버 저장 실패
* 권한 없음
* 서버 오류

처리:

* 프론트에서 파일 선택 여부를 1차 검증한다.
* 백엔드에서 파일 형식, 크기, 권한을 최종 검증한다.
* 실패 시 에러 메시지를 화면에 표시한다.
* 실패 시 입력값은 유지한다.

### 11.4 등록 실패

상황:

* 필수 메타데이터 누락
* 점수 값 오류
* 허용되지 않은 경기 결과
* 권한 없음

처리:

* 프론트에서 필수값을 1차 검증한다.
* 백엔드 에러 메시지를 화면에 표시한다.
* 실패 시 입력값은 유지한다.

### 11.5 수정 실패

상황:

* 필수값 누락
* 경기 영상이 존재하지 않음
* 삭제된 경기 영상
* 권한 없음
* 서버 오류

처리:

* 백엔드 에러 메시지를 화면에 표시한다.
* 실패 시 수정 폼 입력값은 유지한다.

### 11.6 삭제 실패

상황:

* `COACH`가 아닌 사용자의 삭제 요청
* 경기 영상이 존재하지 않음
* 이미 삭제된 경기 영상
* 서버 오류

처리:

* 백엔드 에러 메시지를 화면에 표시한다.
* 실패 후 목록을 다시 조회해 최신 상태를 맞춘다.

### 11.7 영상 재생 실패

상황:

* 업로드 파일이 깨짐
* 브라우저에서 재생 불가한 코덱
* 정적 리소스 URL 매핑 실패
* CORS 문제
* 서버 파일이 존재하지 않음

처리:

* 영상 영역에 재생 실패 안내 문구를 표시한다.
* 영상 URL 자체는 상세 정보에서 확인할 수 있게 한다.
* 개발 중에는 브라우저 개발자 도구 Network 탭에서 영상 요청 상태를 확인한다.

---

## 12. 구현 순서

### 12.1 백엔드 1단계: 업로드 설정 추가

1. `application.properties`에 multipart 업로드 크기 제한을 추가한다.
2. 로컬 업로드 디렉터리 경로 설정 값을 추가한다.
3. 정적 리소스 매핑 설정을 추가한다.
4. `/uploads/match-videos/**` 경로로 영상 파일에 접근할 수 있게 한다.

### 12.2 백엔드 2단계: 파일 저장 서비스 작성

1. 경기 영상 파일 저장 전용 서비스를 작성한다.
2. 파일이 비어 있는지 검증한다.
3. `.mp4`, `video/mp4`만 허용한다.
4. 파일 크기를 검증한다.
5. UUID 기반 저장 파일명을 생성한다.
6. 로컬 디렉터리에 파일을 저장한다.
7. 브라우저 접근 URL을 반환한다.

### 12.3 백엔드 3단계: 업로드 요청 DTO 또는 파라미터 정리

1. `multipart/form-data` 요청을 받도록 Controller를 수정한다.
2. `@RequestPart` 또는 `@RequestParam`으로 `videoFile`과 메타데이터를 받는다.
3. 기존 JSON 기반 등록 요청 DTO는 파일 업로드 방식에 맞게 사용 범위를 조정한다.
4. 등록 시 `url`은 요청에서 받지 않고 서버에서 생성한 URL을 사용한다.
5. `status`는 서버에서 `READY`로 저장한다.

### 12.4 백엔드 4단계: 경기 영상 등록 로직 수정

1. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
2. 영상 파일을 저장한다.
3. 저장 URL을 생성한다.
4. 경기 메타데이터를 검증한다.
5. `game_video_upload.url`에 저장 URL을 넣는다.
6. `status`는 `READY`로 저장한다.
7. 경기 영상 정보를 DB에 저장한다.

### 12.5 백엔드 5단계: 수정/삭제 정책 정리

1. 수정 API는 경기 메타데이터만 수정한다.
2. 수정 API에서 파일 교체는 처리하지 않는다.
3. 삭제 API는 기존처럼 `is_deleted = true`로 처리한다.
4. 초기 MVP에서는 실제 로컬 파일 삭제는 하지 않는다.
5. 파일 삭제가 필요하면 추후 별도 정책으로 추가한다.

### 12.6 프론트 1단계: 타입 작성

1. `frontend/src/types/matchVideo.ts` 파일을 생성한다.
2. 파일 상단에 한 줄 역할 주석을 작성한다.
3. `MatchResult`, `MatchVideoStatus` 타입을 작성한다.
4. 목록 응답 타입을 작성한다.
5. 상세 응답 타입을 작성한다.
6. 페이지 응답 타입을 작성한다.
7. 업로드 요청 타입을 작성한다.
8. 수정 요청 타입을 작성한다.

### 12.7 프론트 2단계: API 함수 작성

1. `frontend/src/api/matchVideoApi.ts` 파일을 생성한다.
2. 파일 상단에 한 줄 역할 주석을 작성한다.
3. 경기 영상 목록 조회 API 함수를 작성한다.
4. 경기 영상 상세 조회 API 함수를 작성한다.
5. 경기 영상 파일 업로드 API 함수를 작성한다.
6. 경기 영상 수정 API 함수를 작성한다.
7. 경기 영상 삭제 API 함수를 작성한다.
8. 모든 요청은 기존 `axiosInstance`를 사용한다.
9. 업로드 요청은 `FormData`를 사용한다.
10. 업로드 요청 시 `Content-Type`은 브라우저가 boundary를 자동 설정하도록 직접 고정하지 않는 것을 우선한다.

### 12.8 프론트 3단계: 라우트 상수 추가

1. `frontend/src/components/constants/routes.ts`에 `MATCH_VIDEO: "/match-videos"`를 추가한다.
2. 문자열 경로를 직접 사용하지 않도록 한다.

### 12.9 프론트 4단계: 경기 영상 화면 작성

1. `frontend/src/pages/MatchVideoPage.tsx` 파일을 생성한다.
2. 파일 상단에 한 줄 역할 주석을 작성한다.
3. 화면 진입 시 경기 영상 목록을 조회한다.
4. 경기 영상 목록 UI를 작성한다.
5. 경기 영상 상세 조회 UI를 작성한다.
6. 영상 URL을 `<video>` 태그에 연결한다.
7. `COACH`, `ANALYST`에게만 파일 업로드/등록/수정 폼을 노출한다.
8. `COACH`에게만 삭제 버튼을 노출한다.
9. `PLAYER`는 조회와 재생 화면만 볼 수 있게 처리한다.
10. form submit 이벤트 타입은 `{ preventDefault: () => void }`로 작성한다.

### 12.10 프론트 5단계: App 라우트 연결

1. `frontend/src/App.tsx`에 경기 영상 라우트를 추가한다.
2. `ProtectedRoute`로 감싸 로그인 사용자만 접근 가능하게 한다.
3. `RoleRoute`로 `COACH` 전용 제한을 걸지 않는다.

### 12.11 프론트 6단계: 홈 메뉴 연결

1. `DashboardHomePage.tsx`에서 경기 영상 메뉴를 `ROUTES.MATCH_VIDEO`로 연결한다.
2. `PlayerHomePage.tsx`에서 경기 원본 영상 메뉴를 `ROUTES.MATCH_VIDEO`로 연결한다.
3. `MobileHomePage.tsx`에서 경기 원본 영상 메뉴를 `ROUTES.MATCH_VIDEO`로 연결한다.
4. 드로잉 메뉴는 별도로 추가하지 않는다.

### 12.12 프론트 7단계: 테스트

1. `COACH` 경기 영상 파일 업로드 성공 테스트
2. `COACH` 업로드 후 목록 조회 성공 테스트
3. `COACH` 업로드 후 상세 조회 성공 테스트
4. `COACH` 업로드된 영상 재생 성공 테스트
5. `COACH` 경기 영상 정보 수정 성공 테스트
6. `COACH` 경기 영상 삭제 성공 테스트
7. `ANALYST` 경기 영상 파일 업로드 성공 테스트
8. `ANALYST` 경기 영상 정보 수정 성공 테스트
9. `ANALYST` 삭제 버튼 미노출 확인
10. `ANALYST` 삭제 API 직접 호출 시 백엔드 차단 확인
11. `PLAYER` 경기 영상 목록/상세 조회 성공 테스트
12. `PLAYER` 업로드/수정/삭제 UI 미노출 확인
13. `PLAYER` 업로드/수정/삭제 API 직접 호출 시 백엔드 차단 확인
14. 비로그인 사용자의 `/match-videos` 접근 시 로그인 화면 이동 확인
15. 파일 미선택 업로드 실패 확인
16. mp4가 아닌 파일 업로드 실패 확인
17. 허용 크기 초과 파일 업로드 실패 확인
18. 등록/수정/삭제 후 목록 갱신 확인

---

## 13. 테스트 결과

아직 구현 전이므로 테스트 결과는 없다.

구현 후 사용자가 실제 정상 확인한 항목만 이 문서에 반영한다.

---

## 14. 추후 확장 가능성

추후 확장 가능한 기능은 다음과 같다.

* 실제 운영용 클라우드 스토리지 연동
* S3 Presigned URL 기반 직접 업로드
* 영상 업로드 진행률 표시
* 영상 업로드 취소
* 영상 인코딩 상태 조회
* 썸네일 자동 생성
* 영상 길이 자동 추출
* 파일 크기 저장
* 원본 파일명 저장
* 저장 파일명 저장
* 영상 접근 권한 URL 서명 처리
* 경기 영상 검색
* 경기 결과 필터
* 경기일 기준 필터
* 상대팀명 컬럼 추가
* 대회명 컬럼 추가
* 원본 영상에서 팀 분석 클립 생성 화면 연결
* 원본 영상에서 선수 개인 분석 클립 생성 화면 연결
* 영상 북마크 기능
* 영상 편집기 고도화
* 파일 교체 API
* 삭제 시 실제 파일 삭제 또는 보관 정책

---

## 15. 주의사항

* 경기 영상 등록, 수정, 삭제 권한은 프론트가 아니라 백엔드에서 반드시 검증한다.
* 분석관은 업로드와 수정은 가능하지만 삭제는 불가능하다.
* 선수는 원본 경기 영상 조회와 재생만 가능하다.
* 삭제는 실제 삭제가 아니라 `is_deleted = true`로 처리한다.
* 초기 MVP에서는 삭제 시 실제 로컬 영상 파일을 삭제하지 않는다.
* 조회 API는 삭제되지 않은 경기 영상만 반환한다.
* 이번 범위는 실제 파일 업로드와 화면 재생까지 포함한다.
* 이번 범위에서 영상 자르기, 인코딩, 썸네일 생성은 구현하지 않는다.
* 업로드된 파일은 서버 로컬 디렉터리에 저장한다.
* 로컬 파일 저장은 개발 및 MVP 검증에는 적합하지만 운영 서비스에서는 스토리지, CDN, 접근 권한 정책을 다시 설계해야 한다.
* `<video>` 태그는 일반적으로 Authorization 헤더를 붙여 요청하기 어렵기 때문에 운영에서는 공개 URL, 서명 URL, 스트리밍 서버 방식을 검토해야 한다.
* 파일 확장자만 믿지 말고 MIME 타입도 함께 검증한다.
* 업로드 파일명은 사용자가 올린 원본 파일명을 그대로 사용하지 않고 UUID 기반으로 저장한다.
* 대용량 경기 영상은 서버 디스크 용량과 업로드 시간 문제가 생길 수 있다.
* 현재 서비스는 단일 팀 기준이므로 `team_id`를 사용하지 않는다.
* 추후 여러 팀을 받을 경우 `game_video_upload` 테이블에 `team_id` 또는 팀 소속 구조가 필요하다.
