# 22. 선수 개인 분석 클립 프론트 연동 요구사항

## 1. 결론

선수 개인 분석 클립 프론트 연동 기능은 백엔드에 구현된 선수 개인 분석 클립 API를 React 화면과 연결해, 지도자와 분석관은 특정 선수에게 개인 분석 클립을 등록, 수정, 삭제하고 선수는 본인에게 공유된 개인 분석 클립만 조회할 수 있게 하는 기능이다.

이번 구현 기준은 다음과 같다.

* 프론트는 기존 React + TypeScript + Vite 구조를 사용한다.
* 인증은 기존 로그인/JWT 인증 구조를 사용한다.
* Access Token은 기존 `axiosInstance`에서 Authorization 헤더에 자동으로 포함한다.
* 선수 개인 분석 클립 화면 경로는 `/player-analysis-clips`를 사용한다.
* 라우트 경로는 문자열로 직접 작성하지 않고 `ROUTES.PLAYER_ANALYSIS_CLIP` 상수를 사용한다.
* 선수 개인 분석 클립은 실제 영상 파일을 자르지 않고 원본 경기 영상 URL과 `startTimeSec`, `endTimeSec` 메타데이터를 기준으로 구간 재생한다.
* 지도자 `COACH`는 선수 개인 분석 클립 등록, 조회, 수정, 삭제 UI를 볼 수 있다.
* 분석관 `ANALYST`는 선수 개인 분석 클립 등록, 조회, 수정 UI를 볼 수 있고 삭제 UI는 볼 수 없다.
* 선수 `PLAYER`는 본인 개인 분석 클립 목록과 상세만 조회할 수 있다.
* 선수는 다른 선수의 개인 분석 클립을 조회할 수 없다.
* 프론트의 버튼 숨김은 UX 목적이며, 실제 권한 검증은 반드시 백엔드에서 처리한다.
* 선수 개인 분석 클립 등록/수정 화면에서는 대상 선수를 선택해야 하므로 선수 목록 조회 API를 먼저 백엔드에 추가한다.
* 선수 목록 조회 API는 `COACH`, `ANALYST`만 사용할 수 있다.
* 선수 목록 조회 API는 `APPROVED` 상태이고 삭제되지 않은 `PLAYER` 역할 회원만 반환한다.
* 선수 목록 조회 API는 선수 개인 분석 클립 관리 화면의 드롭다운용이며, 선수 본인 화면에서는 사용하지 않는다.
* 선수 개인 분석 클립 목록 응답 배열 필드명은 `playerClips`를 사용한다.
* 작성자 정보는 `editorId`, `editorName`을 사용한다.
* 영상 URL은 `matchVideoUrl`을 사용하고, 상대 경로인 경우 기존 `createVideoSourceUrl()` 유틸로 API base URL과 결합한다.
* 상세 응답에 `matchVideoDurationSec`가 없으므로 프론트의 영상 길이 검증은 경기 영상 목록의 `durationSec` 또는 백엔드 검증 결과에 의존한다.
* 실제 서비스 운영 전에는 `/uploads/match-videos/**` 직접 접근 방식을 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.

---

## 2. 기능 목적

선수 개인 분석 클립 프론트 연동의 목적은 지도자와 분석관이 원본 경기 영상에서 특정 장면을 선택해 특정 선수 1명에게 개인 피드백 클립으로 공유하고, 해당 선수는 본인에게 공유된 개인 분석 클립만 확인할 수 있게 하는 것이다.

팀 분석 클립이 팀 전체 전술 공유용이라면, 선수 개인 분석 클립은 특정 선수에게만 전달되는 개인 피드백 자료다.

따라서 초기 프론트 구현에서는 다음 기준을 우선한다.

* 지도자와 분석관은 원본 경기 영상을 선택해 개인 분석 클립을 만들 수 있어야 한다.
* 지도자와 분석관은 대상 선수를 직접 숫자로 입력하지 않고 선수 목록에서 선택할 수 있어야 한다.
* 선수는 모바일에서 본인 개인 분석 클립을 쉽게 확인할 수 있어야 한다.
* 선수는 다른 선수의 개인 분석 클립을 볼 수 없어야 한다.
* 실제 영상 파일을 자르지 않고 원본 영상 기준 시간 구간을 재생한다.
* 권한별 버튼 노출은 프론트에서 처리하되, 실제 차단은 백엔드 권한 검증에 맡긴다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 목록 조회
* 선수 개인 분석 클립 상세 조회
* 선수 개인 분석 클립 등록
* 선수 개인 분석 클립 수정
* 선수 개인 분석 클립 삭제
* 원본 경기 영상 선택
* 대상 선수 선택
* 클립 구간 재생

화면에서는 다음 UI가 노출된다.

* 조회 조건 영역
* 선수 개인 분석 클립 등록 폼
* 원본 경기 영상 선택 드롭다운
* 대상 선수 선택 드롭다운
* 클립 유형 선택
* 제목 입력
* 코멘트 입력
* 시작 시간 입력
* 종료 시간 입력
* 수정 버튼
* 삭제 버튼

### 3.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 목록 조회
* 선수 개인 분석 클립 상세 조회
* 선수 개인 분석 클립 등록
* 선수 개인 분석 클립 수정
* 원본 경기 영상 선택
* 대상 선수 선택
* 클립 구간 재생

불가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 삭제

화면에서는 삭제 버튼을 노출하지 않는다.

### 3.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

* 본인 개인 분석 클립 목록 조회
* 본인 개인 분석 클립 상세 조회
* 본인 개인 분석 클립 영상 구간 재생

불가능한 기능은 다음과 같다.

* 다른 선수의 개인 분석 클립 조회
* 선수 개인 분석 클립 등록
* 선수 개인 분석 클립 수정
* 선수 개인 분석 클립 삭제
* 선수 목록 조회

화면에서는 등록 폼, 대상 선수 선택 영역, 수정 버튼, 삭제 버튼을 노출하지 않는다.

---

## 4. 권한 정책

| 역할        |  목록 조회 |  상세 조회 | 등록 UI | 수정 UI | 삭제 UI | 선수 목록 조회 |
| --------- | -----: | -----: | ----: | ----: | ----: | -------: |
| `COACH`   |  전체 가능 |  전체 가능 |    노출 |    노출 |    노출 |       가능 |
| `ANALYST` |  전체 가능 |  전체 가능 |    노출 |    노출 |   미노출 |       가능 |
| `PLAYER`  | 본인만 가능 | 본인만 가능 |   미노출 |   미노출 |   미노출 |       불가 |

권한 처리 기준은 다음과 같다.

* `/player-analysis-clips` 라우트는 로그인 사용자라면 접근 가능하다.
* 화면 내부에서 로그인 사용자의 `memberRole`에 따라 API와 UI를 분기한다.
* `COACH`, `ANALYST`는 관리용 API를 사용한다.
* `PLAYER`는 선수 본인 API를 사용한다.
* `PLAYER` 요청에는 `playerId`를 보내지 않는다.
* `COACH`, `ANALYST`에게만 등록/수정 폼을 보여준다.
* `COACH`에게만 삭제 버튼을 보여준다.
* 프론트에서 버튼을 숨기더라도 백엔드 권한 검증은 반드시 유지한다.

---

## 5. 화면 흐름

### 5.1 페이지 경로

```text
/player-analysis-clips
```

라우트 상수는 다음 값을 사용한다.

```ts
PLAYER_ANALYSIS_CLIP: "/player-analysis-clips"
```

### 5.2 진입 위치

관리자/지도자/분석관 홈 화면:

* 선수 개인 분석 클립 관리 메뉴에서 진입

선수 홈 화면:

* 내 개인 분석 영상 메뉴에서 진입

모바일 홈 화면:

* 내 개인 분석 영상 메뉴에서 진입

### 5.3 화면 구성

선수 개인 분석 클립 페이지는 다음 영역으로 구성한다.

1. 조회 조건 영역

    * 원본 경기 영상 필터
    * 대상 선수 필터
    * 클립 유형 필터
    * 대상 선수 필터는 `COACH`, `ANALYST`에게만 노출한다.
    * `PLAYER`에게는 대상 선수 필터를 노출하지 않는다.

2. 선수 개인 분석 클립 등록 영역

    * `COACH`, `ANALYST`에게만 노출
    * 원본 경기 영상 선택
    * 대상 선수 선택
    * 클립 유형 선택
    * 제목 입력
    * 코멘트 입력
    * 시작 시간 입력
    * 종료 시간 입력

3. 선수 개인 분석 클립 목록 영역

    * 클립 제목
    * 원본 경기 영상 제목
    * 대상 선수 이름
    * 클립 유형
    * 시작/종료 시간
    * 작성자 표시
    * 생성일시

4. 선수 개인 분석 클립 상세 영역

    * 원본 경기 영상 플레이어
    * 클립 구간 재생 버튼
    * 처음부터 다시 보기 버튼
    * 원본 경기 영상 제목
    * 대상 선수 이름
    * 클립 유형
    * 제목
    * 코멘트
    * 시작/종료 시간
    * 작성자
    * 권한에 따른 수정/삭제 버튼

---

## 6. API 흐름

### 6.1 선수 목록 조회

선수 개인 분석 클립 등록/수정 시 대상 선수를 선택해야 하므로 선수 목록 조회 API를 먼저 추가한다.

```http
GET /api/management/players
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

사용 불가능 역할은 다음과 같다.

* `PLAYER`

조회 대상은 다음 조건을 만족해야 한다.

* `member_role = PLAYER`
* `approval_status = APPROVED`
* `is_deleted = false`

정렬 기준은 다음과 같다.

```text
uniformNumber ASC, name ASC
```

응답 예시는 다음과 같다.

```json
[
  {
    "playerId": 5,
    "name": "홍길동",
    "grade": 2,
    "uniformNumber": 10
  }
]
```

프론트 처리 기준은 다음과 같다.

* `COACH`, `ANALYST`가 `/player-analysis-clips` 화면에 진입하면 선수 목록을 조회한다.
* 등록 폼과 수정 폼의 대상 선수 드롭다운에 사용한다.
* `PLAYER`는 이 API를 호출하지 않는다.
* 선수 목록 API 실패 시 등록/수정 폼 대신 오류 메시지를 표시한다.
* 선수 목록이 비어 있으면 “등록 가능한 선수가 없습니다.” 메시지를 표시한다.

주의사항은 다음과 같다.

* 이 API는 선수 개인 분석 클립 등록/수정 편의를 위한 조회 API다.
* 대상 선수 검증은 선수 개인 분석 클립 등록/수정 백엔드 서비스에서도 반드시 유지한다.
* 프론트에서 드롭다운으로 선택한다고 해서 백엔드의 `PLAYER` 역할 검증을 제거하면 안 된다.

---

### 6.2 경기 영상 목록 조회

선수 개인 분석 클립 등록 시 원본 경기 영상을 선택해야 하므로 기존 경기 영상 목록 조회 API를 사용한다.

```http
GET /api/match-videos?page=0&size=50
```

프론트 호출 방식은 다음과 같다.

```ts
getMatchVideos(0, 50)
```

프론트 처리 기준은 다음과 같다.

* `COACH`, `ANALYST`는 등록/수정 폼에서 원본 경기 영상을 선택한다.
* `PLAYER`는 조회 필터로만 원본 경기 영상을 사용할 수 있다.
* 경기 영상의 `durationSec`가 있으면 프론트에서 시작/종료 시간 입력 보조 검증에 활용할 수 있다.
* 최종 시간 검증은 백엔드가 담당한다.

---

### 6.3 관리용 선수 개인 분석 클립 목록 조회

```http
GET /api/management/player-analysis-clips?page=0&size=20&matchVideoId={matchVideoId}&playerId={playerId}&clipType={clipType}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 파라미터는 다음과 같다.

| 파라미터           | 필수 여부 | 설명             |
| -------------- | ----: | -------------- |
| `page`         |    선택 | 페이지 번호         |
| `size`         |    선택 | 페이지 크기         |
| `matchVideoId` |    선택 | 원본 경기 영상 기준 필터 |
| `playerId`     |    선택 | 대상 선수 기준 필터    |
| `clipType`     |    선택 | 클립 유형 필터       |

응답 배열 필드명은 다음과 같다.

```ts
playerClips
```

---

### 6.4 선수 본인 개인 분석 클립 목록 조회

```http
GET /api/player/me/player-analysis-clips?page=0&size=20&matchVideoId={matchVideoId}&clipType={clipType}
```

사용 가능 역할은 다음과 같다.

* `PLAYER`

요청 파라미터는 다음과 같다.

| 파라미터           | 필수 여부 | 설명             |
| -------------- | ----: | -------------- |
| `page`         |    선택 | 페이지 번호         |
| `size`         |    선택 | 페이지 크기         |
| `matchVideoId` |    선택 | 원본 경기 영상 기준 필터 |
| `clipType`     |    선택 | 클립 유형 필터       |

프론트 처리 기준은 다음과 같다.

* `PLAYER`는 `playerId`를 보내지 않는다.
* 로그인한 선수 본인의 개인 분석 클립만 백엔드에서 반환한다.
* 다른 선수의 개인 분석 클립 접근 차단은 백엔드가 처리한다.

---

### 6.5 관리용 선수 개인 분석 클립 상세 조회

```http
GET /api/management/player-analysis-clips/{playerClipId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

상세 응답 사용 필드는 다음과 같다.

```text
playerClipId
matchVideoId
matchVideoTitle
matchVideoUrl
playerId
playerName
clipType
title
comment
startTimeSec
endTimeSec
status
editorId
editorName
createdAt
updatedAt
```

---

### 6.6 선수 본인 개인 분석 클립 상세 조회

```http
GET /api/player/me/player-analysis-clips/{playerClipId}
```

사용 가능 역할은 다음과 같다.

* `PLAYER`

프론트 처리 기준은 다음과 같다.

* 선수가 목록에서 클립을 선택하면 상세 조회 API를 호출한다.
* 백엔드는 본인 클립인지 검증한다.
* 백엔드는 상세 조회 시 조회 기록을 저장 또는 갱신한다.
* 프론트는 응답의 `matchVideoUrl`, `startTimeSec`, `endTimeSec` 기준으로 구간 재생한다.

---

### 6.7 선수 개인 분석 클립 등록

```http
POST /api/management/player-analysis-clips
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 body는 다음과 같다.

```json
{
  "matchVideoId": 1,
  "playerId": 5,
  "clipType": "PASS",
  "title": "전진 패스 선택 가능 장면",
  "comment": "오른쪽 전진 패스 선택이 가능했던 장면",
  "startTimeSec": 5,
  "endTimeSec": 15
}
```

등록 시에는 원본 경기 영상과 대상 선수를 선택해야 하므로 `matchVideoId`, `playerId`를 포함한다.

---

### 6.8 선수 개인 분석 클립 수정

```http
PATCH /api/management/player-analysis-clips/{playerClipId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 body는 다음과 같다.

```json
{
  "matchVideoId": 1,
  "playerId": 5,
  "clipType": "DEFENSE",
  "title": "수비 위치 조정 장면",
  "comment": "중앙 커버 위치를 더 빨리 잡아야 하는 장면",
  "startTimeSec": 10,
  "endTimeSec": 20
}
```

현재 백엔드 수정 DTO는 `matchVideoId`, `playerId`를 포함하므로 프론트 수정 요청에도 두 값을 포함한다.

---

### 6.9 선수 개인 분석 클립 삭제

```http
DELETE /api/coach/player-analysis-clips/{playerClipId}
```

사용 가능 역할은 다음과 같다.

* `COACH`

삭제는 선수 개인 분석 클립만 소프트 삭제 처리한다.

원본 경기 영상은 삭제하지 않는다.

`ANALYST`, `PLAYER`에게는 삭제 버튼을 노출하지 않는다.

---

## 7. 프론트 타입 기준

### 7.1 선수 선택 응답 타입

```ts
export type PlayerSelectItem = {
  playerId: number;
  name: string;
  grade: number | null;
  uniformNumber: number | null;
};
```

### 7.2 선수 개인 분석 클립 타입

```ts
export type PlayerAnalysisClipType =
  | "PLAYER_GOOD"
  | "PLAYER_MISTAKE"
  | "SHOOTING"
  | "PASS"
  | "DRIBBLE"
  | "DEFENSE"
  | "POSITIONING"
  | "PRESSING"
  | "OFF_THE_BALL"
  | "ETC";

export type PlayerAnalysisClipStatus = "UPLOADING" | "READY" | "FAILED";

export type PlayerAnalysisClipListItem = {
  playerClipId: number;
  matchVideoId: number;
  matchVideoTitle: string;
  playerId: number;
  playerName: string;
  clipType: PlayerAnalysisClipType;
  title: string;
  startTimeSec: number;
  endTimeSec: number;
  status: PlayerAnalysisClipStatus;
  editorId: number;
  editorName: string;
  createdAt: string;
};

export type PlayerAnalysisClip = {
  playerClipId: number;
  matchVideoId: number;
  matchVideoTitle: string;
  matchVideoUrl: string;
  playerId: number;
  playerName: string;
  clipType: PlayerAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
  status: PlayerAnalysisClipStatus;
  editorId: number;
  editorName: string;
  createdAt: string;
  updatedAt: string;
};

export type PlayerAnalysisClipPageResponse = {
  playerClips: PlayerAnalysisClipListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type CreatePlayerAnalysisClipRequest = {
  matchVideoId: number;
  playerId: number;
  clipType: PlayerAnalysisClipType;
  title: string;
  comment: string;
  startTimeSec: number;
  endTimeSec: number;
};

export type UpdatePlayerAnalysisClipRequest = {
  matchVideoId: number;
  playerId: number;
  clipType: PlayerAnalysisClipType;
  title: string;
  comment: string;
  startTimeSec: number;
  endTimeSec: number;
};
```

---

## 8. 영상 재생 정책

선수 개인 분석 클립은 실제 파일을 자르지 않는다.

프론트는 상세 조회 응답의 `matchVideoUrl`을 원본 영상으로 사용하고, `startTimeSec`, `endTimeSec` 구간만 클립처럼 재생한다.

처리 기준은 다음과 같다.

1. 상세 조회 응답의 `matchVideoUrl`을 가져온다.
2. `createVideoSourceUrl(matchVideoUrl)`로 실제 재생 URL을 만든다.
3. `<video>` 태그의 `src`에 연결한다.
4. 구간 재생 버튼을 누르면 `video.currentTime = startTimeSec`로 이동한다.
5. 영상을 재생한다.
6. 재생 중 현재 시간이 `endTimeSec` 이상이 되면 영상을 일시정지한다.
7. 처음부터 다시 보기 버튼은 다시 `startTimeSec`로 이동시킨다.

주의사항은 다음과 같다.

* 프론트에서 구간 재생을 하더라도 실제 보안은 영상 URL 접근 제어와 백엔드 권한 검증에 달려 있다.
* 현재 MVP의 `/uploads/match-videos/**` 직접 접근 방식은 실제 서비스 운영 전 반드시 개선해야 한다.
* 선수 개인 분석 클립은 민감한 피드백 영상이므로 운영 단계에서는 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식이 필요하다.

---

## 9. 예외 상황

### 9.1 공통 예외

* 인증 토큰이 없으면 로그인 화면으로 이동한다.
* API 요청 중 401이 발생하면 기존 인증 처리 정책에 따라 토큰을 삭제하고 로그인 화면으로 이동한다.
* API 요청 중 403이 발생하면 권한이 없다는 메시지를 표시한다.
* 목록 조회 실패 시 오류 메시지와 재시도 버튼을 표시한다.
* 상세 조회 실패 시 선택된 상세 상태를 초기화한다.

### 9.2 선수 목록 조회 예외

* 선수 목록 조회에 실패하면 등록/수정 폼을 비활성화한다.
* 조회 가능한 선수가 없으면 대상 선수 선택 영역에 “등록 가능한 선수가 없습니다.”를 표시한다.
* `PLAYER`는 선수 목록 조회 API를 호출하지 않는다.
* `PLAYER`가 직접 API를 호출해도 백엔드에서 실패해야 한다.

### 9.3 등록/수정 예외

* 원본 경기 영상을 선택하지 않으면 등록/수정을 요청하지 않는다.
* 대상 선수를 선택하지 않으면 등록/수정을 요청하지 않는다.
* 제목이 비어 있으면 등록/수정을 요청하지 않는다.
* 시작 시간이 종료 시간보다 크거나 같으면 등록/수정을 요청하지 않는다.
* 시작 시간과 종료 시간은 0 이상이어야 한다.
* 최종 시간 범위 검증은 백엔드 응답을 기준으로 처리한다.
* 백엔드에서 원본 영상 길이 정보가 준비되지 않았다고 응답하면 해당 메시지를 화면에 표시한다.

### 9.4 삭제 예외

* 삭제 전 확인창을 표시한다.
* 삭제 실패 시 목록을 임의로 제거하지 않는다.
* 삭제 성공 후 목록을 다시 조회한다.
* 삭제 성공 후 선택된 상세 클립이 삭제 대상이면 상세 영역을 초기화한다.

---

## 10. DB 설계 방향

이번 프론트 연동에서는 새 테이블을 추가하지 않는다.

사용하는 주요 테이블은 다음과 같다.

* `player_video_clip`
* `game_video_upload`
* `member`

다만 선수 목록 조회 API는 `member` 테이블을 사용한다.

조회 조건은 다음과 같다.

```text
member_role = PLAYER
approval_status = APPROVED
is_deleted = false
```

선수 개인 분석 클립 등록/수정 시에는 기존 백엔드 정책에 따라 `player_video_clip.player_id`에 대상 선수 ID를 저장한다.

---

## 11. 구현 파일 방향

### 11.1 백엔드 선행 작업

선수 목록 조회 API 추가를 먼저 진행한다.

생성 예상 파일:

```text
backend/src/main/java/com/soccer/platform/controller/ManagementPlayerController.java
backend/src/main/java/com/soccer/platform/dto/member/PlayerSelectResponseDTO.java
backend/src/main/java/com/soccer/platform/service/member/PlayerQueryService.java
```

수정 예상 파일:

```text
backend/src/main/java/com/soccer/platform/repository/MemberRepository.java
backend/src/main/java/com/soccer/platform/common/exception/ErrorCode.java
```

### 11.2 프론트 작업

생성 예상 파일:

```text
frontend/src/types/playerAnalysisClip.ts
frontend/src/api/playerAnalysisClipApi.ts
frontend/src/pages/PlayerAnalysisClipPage.tsx
```

수정 예상 파일:

```text
frontend/src/constants/routes.ts
frontend/src/App.tsx
frontend/src/pages/DashboardHomePage.tsx
frontend/src/pages/MobileHomePage.tsx
frontend/src/pages/PlayerHomePage.tsx
```

---

## 12. 구현 순서

1. 선수 목록 조회 API 백엔드 요구사항 확정
2. GitHub 이슈 생성
3. `feature/management-player-list-api` 브랜치에서 백엔드 구현
4. 선수 목록 조회 API 테스트
5. 백엔드 작업 완료 후 커밋/PR 정리
6. 현재 채팅으로 돌아와 선수 개인 분석 클립 프론트 연동 진행
7. `playerAnalysisClip.ts` 작성
8. `playerAnalysisClipApi.ts` 작성
9. `ROUTES.PLAYER_ANALYSIS_CLIP` 추가
10. `App.tsx` 라우트 추가
11. 대시보드/선수 홈/모바일 홈 메뉴 연결
12. `PlayerAnalysisClipPage.tsx` 구현
13. 권한별 목록/상세/등록/수정/삭제 테스트
14. 선수 본인 개인 분석 클립 조회 테스트
15. 선수 다른 사람 클립 접근 차단 테스트

---

## 13. 테스트 범위

### 13.1 선수 목록 조회 API 테스트

* `COACH`가 선수 목록 조회 성공
* `ANALYST`가 선수 목록 조회 성공
* `PLAYER`가 선수 목록 조회 실패
* 승인 완료된 선수만 조회되는지 확인
* 승인 대기 선수는 조회되지 않는지 확인
* 삭제된 선수는 조회되지 않는지 확인
* 지도자/분석관 계정은 선수 목록에 포함되지 않는지 확인

### 13.2 선수 개인 분석 클립 프론트 테스트

* `COACH` 로그인 시 등록/수정/삭제 UI 노출
* `ANALYST` 로그인 시 등록/수정 UI 노출, 삭제 UI 미노출
* `PLAYER` 로그인 시 조회 UI만 노출
* `COACH`, `ANALYST`가 선수 목록 드롭다운을 볼 수 있는지 확인
* `PLAYER`가 선수 목록 API를 호출하지 않는지 확인
* 원본 경기 영상 선택 후 개인 분석 클립 등록 성공
* 등록 후 목록 갱신
* 목록 항목 클릭 시 상세 조회 성공
* 상세에서 원본 영상 구간 재생
* 수정 성공 후 상세/목록 갱신
* 삭제 성공 후 목록 갱신
* 선수는 본인 개인 분석 클립만 조회
* 선수 상세 조회 시 조회 기록 저장 여부 확인

---

## 14. 주의사항

* 선수 목록 조회 API는 편의 기능이지만, 선수 개인 분석 클립 프론트 구현에는 사실상 필수다.
* 프론트에서 `playerId`를 직접 숫자로 입력하게 만드는 방식은 테스트용으로는 가능하지만 실제 서비스 UI로는 부적합하다.
* 선수 목록 API가 추가되어도 선수 개인 분석 클립 등록/수정 서비스의 대상 선수 검증은 반드시 유지한다.
* 선수 개인 분석 클립은 개인 피드백 데이터이므로 접근 제어가 특히 중요하다.
* `PLAYER` 화면에서는 관리용 API를 호출하지 않는다.
* `PLAYER` 화면에서는 `playerId`를 요청 파라미터나 body에 포함하지 않는다.
* 현재 MVP에서는 영상 파일 직접 접근을 허용하고 있지만, 운영 전에는 반드시 권한 검증이 포함된 영상 제공 방식으로 전환해야 한다.
