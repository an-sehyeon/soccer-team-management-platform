# 23. 선수 개인 분석 클립 드로잉 프론트 연동 요구사항

## 1. 결론

선수 개인 분석 클립 드로잉 프론트 연동 기능은 기존 `PlayerAnalysisClipPage.tsx`의 선수 개인 분석 클립 상세 영역에 드로잉 조회, 등록, 수정, 삭제 UI를 연결하는 기능이다.

이번 구현 기준은 다음과 같다.

* 프론트는 기존 React + TypeScript + Vite 구조를 사용한다.
* 인증은 기존 로그인/JWT 인증 구조를 사용한다.
* Access Token은 기존 `axiosInstance`에서 Authorization 헤더에 자동으로 포함한다.
* 별도 드로잉 전용 라우트는 추가하지 않는다.
* 기존 `/player-analysis-clips` 화면의 상세 영역 안에 드로잉 기능을 통합한다.
* 실제 영상 파일에 그래픽을 합성하지 않는다.
* 실제 영상 위에 캔버스를 정교하게 맞추는 고도화 UI는 추후 개선한다.
* 초기에는 기존 팀 분석 클립 드로잉 프론트 구조를 참고해 최소 기능 중심으로 구현한다.
* 프론트 캔버스에서 생성한 좌표, 텍스트, 도형 데이터를 `drawingData` JSON으로 저장한다.
* 조회 시 저장된 `drawingData`를 다시 캔버스 오버레이로 렌더링한다.
* 영상 재생 시간이 드로잉의 `startTimeSec`, `endTimeSec` 범위 안에 있을 때만 해당 드로잉을 표시한다.
* 드로잉 시간은 선수 개인 분석 클립의 `startTimeSec`, `endTimeSec` 범위 안에서만 입력되도록 프론트에서 1차 검증한다.
* 최종 시간 범위 검증과 권한 검증은 반드시 백엔드에서 처리한다.
* `COACH`는 드로잉 등록, 조회, 수정, 삭제 UI를 볼 수 있다.
* `ANALYST`는 드로잉 등록, 조회, 수정 UI를 볼 수 있고 삭제 UI는 볼 수 없다.
* `PLAYER`는 본인 개인 분석 클립 드로잉 조회만 가능하며 등록, 수정, 삭제 UI는 볼 수 없다.
* `PLAYER`는 다른 선수의 개인 분석 클립 드로잉을 조회할 수 없다.
* 버튼 숨김은 사용자 경험을 위한 처리이며, 실제 권한 검증은 반드시 백엔드에서 처리한다.
* 구현 전 백엔드 `Controller`, 요청 DTO, 응답 DTO를 확인하고 필드명과 응답 구조를 확정한다.
* 드로잉 목록 응답이 배열인지 `{ drawings: [...] }` 객체인지 추측하지 않고 실제 백엔드 반환 타입에 맞춘다.
* React `useEffect` 내부에서는 effect 전용 async 함수를 정의하고 API 응답 이후에만 `setState`를 실행한다.
* `ignore` 또는 `isMounted` 플래그로 컴포넌트 언마운트 후 `setState` 실행을 방지한다.
* React Compiler 경고인 `Calling setState synchronously within an effect can trigger cascading renders`가 발생하지 않게 작성한다.

---

## 2. 기능 목적

이 기능의 목적은 지도자와 분석관이 선수 개인 분석 클립의 특정 장면에 전술적 의미를 시각적으로 표시하고, 해당 선수 본인이 모바일 또는 PC에서 그 의도를 명확하게 확인할 수 있게 하는 것이다.

선수 개인 분석 클립은 팀 전체 공유 영상이 아니라 특정 선수 1명에게 제공되는 개인 피드백 자료다.

따라서 드로잉 프론트 연동에서는 단순 CRUD보다 다음 기준이 더 중요하다.

* 선수 본인만 개인 드로잉을 조회할 수 있어야 한다.
* 지도자와 분석관은 개인 피드백 장면을 쉽게 표시할 수 있어야 한다.
* 선수는 등록/수정/삭제 기능 없이 조회만 할 수 있어야 한다.
* 드로잉은 클립 구간 기준 시간 안에서만 표시되어야 한다.
* 백엔드 DTO와 API 응답 필드명이 프론트 타입과 정확히 일치해야 한다.

예시 사용 상황은 다음과 같다.

* 수비 위치 조정 표시
* 압박 시작 타이밍 화살표 표시
* 패스 선택지 표시
* 오프 더 볼 움직임 방향 표시
* 특정 공간 침투 경로 표시
* 개인 실수 장면의 원인 텍스트 설명

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 목록 조회
* 선수 개인 분석 클립 상세 조회
* 선수 개인 분석 클립 구간 재생
* 선수 개인 분석 클립 드로잉 목록 조회
* 선수 개인 분석 클립 드로잉 등록
* 선수 개인 분석 클립 드로잉 수정
* 선수 개인 분석 클립 드로잉 삭제
* 영상 재생 시간에 맞는 드로잉 확인

화면에서는 다음 UI를 볼 수 있다.

* 드로잉 도구 선택
* 드로잉 시작/종료 시간 입력
* 현재 시간부터 5초 설정 버튼
* 영상 위에 그리기 버튼
* 드로잉 등록 버튼
* 드로잉 목록
* 드로잉 수정 버튼
* 드로잉 삭제 버튼

### 3.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 목록 조회
* 선수 개인 분석 클립 상세 조회
* 선수 개인 분석 클립 구간 재생
* 선수 개인 분석 클립 드로잉 목록 조회
* 선수 개인 분석 클립 드로잉 등록
* 선수 개인 분석 클립 드로잉 수정
* 영상 재생 시간에 맞는 드로잉 확인

불가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 드로잉 삭제

화면에서는 삭제 버튼을 노출하지 않는다.

### 3.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

* 본인 선수 개인 분석 클립 목록 조회
* 본인 선수 개인 분석 클립 상세 조회
* 본인 선수 개인 분석 클립 구간 재생
* 본인 선수 개인 분석 클립 드로잉 목록 조회
* 영상 재생 시간에 맞는 드로잉 확인

불가능한 기능은 다음과 같다.

* 다른 선수의 개인 분석 클립 드로잉 조회
* 선수 개인 분석 클립 드로잉 등록
* 선수 개인 분석 클립 드로잉 수정
* 선수 개인 분석 클립 드로잉 삭제

화면에서는 드로잉 등록 폼, 수정 버튼, 삭제 버튼을 노출하지 않는다.

---

## 4. 프론트 권한 노출 정책

| 역할 | 드로잉 조회 | 드로잉 등록 UI | 드로잉 수정 UI | 드로잉 삭제 UI |
|---|---:|---:|---:|---:|
| `COACH` | 가능 | 노출 | 노출 | 노출 |
| `ANALYST` | 가능 | 노출 | 노출 | 미노출 |
| `PLAYER` | 본인 클립만 가능 | 미노출 | 미노출 | 미노출 |

권한 처리 기준은 다음과 같다.

* `/player-analysis-clips` 라우트는 기존 선수 개인 분석 클립 화면을 그대로 사용한다.
* 선수 개인 분석 클립 상세 선택 후 선택된 `playerClipId` 기준으로 드로잉 목록을 조회한다.
* `COACH`, `ANALYST`에게만 드로잉 작성 영역을 보여준다.
* `COACH`, `ANALYST`에게만 드로잉 수정 버튼을 보여준다.
* `COACH`에게만 드로잉 삭제 버튼을 보여준다.
* `PLAYER`는 영상과 드로잉 조회만 가능하다.
* 프론트에서 버튼을 숨기더라도 API 직접 호출은 가능하므로 백엔드 권한 검증은 반드시 유지한다.

권한 분기 기준 예시는 다음과 같다.

```ts
const isCoach = member?.memberRole === "COACH";
const isAnalyst = member?.memberRole === "ANALYST";
const isPlayer = member?.memberRole === "PLAYER";
const canManageDrawing = isCoach || isAnalyst;
const canDeleteDrawing = isCoach;
```

단, 사용하지 않는 변수는 만들지 않는다.

---

## 5. 화면 흐름

### 5.1 진입 위치

기존 선수 개인 분석 클립 화면을 사용한다.

```text
/player-analysis-clips
```

라우트 상수는 기존 값을 사용한다.

```ts
PLAYER_ANALYSIS_CLIP: "/player-analysis-clips"
```

별도 드로잉 전용 라우트는 추가하지 않는다.

### 5.2 공통 조회 흐름

1. 사용자가 로그인한다.
2. 선수 개인 분석 클립 화면에 진입한다.
3. 역할에 맞게 선수 개인 분석 클립 목록을 조회한다.
4. 사용자가 특정 선수 개인 분석 클립을 선택한다.
5. 선수 개인 분석 클립 상세 정보를 조회한다.
6. 원본 경기 영상 URL과 클립 시작/종료 시간을 기준으로 영상을 재생한다.
7. 선택된 `playerClipId` 기준으로 드로잉 목록 API를 호출한다.
8. 드로잉 목록을 상태로 저장한다.
9. 영상 재생 시간이 각 드로잉의 표시 시간 범위 안에 들어오면 캔버스 오버레이에 표시한다.

### 5.3 지도자/분석관 드로잉 등록 흐름

1. `COACH` 또는 `ANALYST`가 선수 개인 분석 클립 상세를 선택한다.
2. 영상에서 설명할 장면으로 이동한다.
3. `현재 시간부터 5초 설정` 버튼으로 드로잉 표시 시간을 설정한다.
4. 드로잉 도구에서 선, 화살표, 원, 박스, 영역, 텍스트 중 하나를 선택한다.
5. `영상 위에 그리기` 버튼을 누른다.
6. 영상 위 캔버스에 드로잉을 작성한다.
7. 프론트는 캔버스 데이터를 JSON 형태로 만든다.
8. 드로잉 시간이 선수 개인 분석 클립의 시작/종료 시간 안에 있는지 1차 검증한다.
9. 드로잉 등록 API를 호출한다.
10. 등록 성공 후 드로잉 목록을 다시 조회한다.
11. 영상 재생 시 등록된 드로잉이 시간에 맞춰 표시된다.

### 5.4 지도자/분석관 드로잉 수정 흐름

1. 드로잉 목록에서 수정할 드로잉을 선택한다.
2. 기존 드로잉 데이터를 수정 폼에 반영한다.
3. 드로잉 유형, 시작 시간, 종료 시간, JSON 데이터를 수정한다.
4. 수정 시간이 선수 개인 분석 클립 범위 안에 있는지 1차 검증한다.
5. 수정 API를 호출한다.
6. 수정 성공 후 드로잉 목록을 다시 조회한다.

### 5.5 지도자 드로잉 삭제 흐름

1. `COACH`가 드로잉 목록에서 삭제 버튼을 누른다.
2. 확인창을 표시한다.
3. 삭제 API를 호출한다.
4. 삭제 성공 후 드로잉 목록을 다시 조회한다.
5. 삭제된 드로잉은 화면에 표시하지 않는다.

### 5.6 선수 조회 흐름

1. `PLAYER`가 본인 선수 개인 분석 클립 상세를 선택한다.
2. 드로잉 목록을 조회한다.
3. 등록, 수정, 삭제 UI는 보이지 않는다.
4. 영상 재생 시간에 맞는 드로잉만 오버레이로 표시된다.
5. 다른 선수의 개인 분석 클립 드로잉은 백엔드에서 차단된다.

---

## 6. API 흐름

## 6.1 API 설계 방향

선수 개인 분석 클립 드로잉 API는 기존 백엔드 API를 사용한다.

현재 요구사항 문서 기준 API는 다음과 같다.

```http
GET    /api/player-analysis-clips/{playerClipId}/drawings
GET    /api/player-analysis-clip-drawings/{drawingId}
POST   /api/management/player-analysis-clips/{playerClipId}/drawings
PATCH  /api/management/player-analysis-clip-drawings/{drawingId}
DELETE /api/coach/player-analysis-clip-drawings/{drawingId}
```

주의사항:

* 실제 구현 전 `PlayerAnalysisClipDrawingController`의 최종 주소와 반환 타입을 확인한다.
* 목록 응답이 배열인지 `{ drawings: [...] }` 객체인지 확인 후 타입 파일에 반영한다.
* 요청/응답 필드명은 DTO와 1:1로 맞춘다.

### 6.2 드로잉 목록 조회

```http
GET /api/player-analysis-clips/{playerClipId}/drawings
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`
* `PLAYER` 단, 본인 클립만 가능

프론트 처리 기준은 다음과 같다.

* 선수 개인 분석 클립 상세 선택 시 호출한다.
* `selectedClip?.playerClipId`처럼 객체 전체가 아니라 필요한 원시값을 `useEffect` dependency에 사용한다.
* API 호출 성공 후에만 드로잉 목록 상태를 갱신한다.
* 목록 조회 실패 시 기존 선수 개인 분석 클립 상세 정보는 유지한다.
* 드로잉 영역에만 오류 메시지를 표시한다.

응답 필드 기준은 다음과 같다.

```ts
drawingId
playerClipId
drawingType
startTimeSec
endTimeSec
drawingData
writerId
writerName
createdAt
updatedAt
```

### 6.3 드로잉 상세 조회

```http
GET /api/player-analysis-clip-drawings/{drawingId}
```

현재 프론트 구현에서는 목록 응답만으로 수정과 표시가 가능하면 상세 조회 API는 별도로 사용하지 않는다.

추후 목록 응답과 상세 응답이 분리되면 상세 조회 API를 사용한다.

### 6.4 드로잉 등록

```http
POST /api/management/player-analysis-clips/{playerClipId}/drawings
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 body 기준은 다음과 같다.

```json
{
  "drawingType": "TEXT",
  "startTimeSec": 755,
  "endTimeSec": 760,
  "drawingData": {
    "x": 0.35,
    "y": 0.22,
    "text": "여기서 몸 방향을 먼저 열어야 함",
    "color": "#ff0000",
    "fontSize": 18
  }
}
```

응답 body 기준은 다음과 같다.

```json
{
  "drawingId": 1,
  "message": "선수 개인 분석 클립 드로잉이 등록되었습니다."
}
```

### 6.5 드로잉 수정

```http
PATCH /api/management/player-analysis-clip-drawings/{drawingId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 body는 등록 요청과 동일하다.

```json
{
  "drawingType": "ARROW",
  "startTimeSec": 756,
  "endTimeSec": 762,
  "drawingData": {
    "fromX": 0.2,
    "fromY": 0.4,
    "toX": 0.6,
    "toY": 0.3,
    "color": "#ff0000",
    "lineWidth": 4
  }
}
```

### 6.6 드로잉 삭제

```http
DELETE /api/coach/player-analysis-clip-drawings/{drawingId}
```

사용 가능 역할은 다음과 같다.

* `COACH`

삭제 성공 후 프론트는 드로잉 목록을 다시 조회한다.

삭제는 드로잉 데이터만 삭제 처리하며, 선수 개인 분석 클립과 원본 경기 영상은 삭제하지 않는다.

---

## 7. 프론트 타입 설계 방향

프론트 타입 파일은 다음 위치에 생성한다.

```text
frontend/src/types/playerAnalysisClipDrawing.ts
```

주요 타입은 다음과 같다.

```ts
export type PlayerAnalysisClipDrawingType =
  | "LINE"
  | "ARROW"
  | "CIRCLE"
  | "BOX"
  | "AREA"
  | "TEXT";

export type PlayerAnalysisClipDrawingData = Record<string, unknown>;

export type PlayerAnalysisClipDrawingResponse = {
  drawingId: number;
  playerClipId: number;
  drawingType: PlayerAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: PlayerAnalysisClipDrawingData;
  writerId: number;
  writerName: string;
  createdAt: string;
  updatedAt: string;
};

export type CreatePlayerAnalysisClipDrawingRequest = {
  drawingType: PlayerAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: PlayerAnalysisClipDrawingData;
};

export type UpdatePlayerAnalysisClipDrawingRequest = CreatePlayerAnalysisClipDrawingRequest;

export type CreatePlayerAnalysisClipDrawingResponse = {
  drawingId: number;
  message: string;
};
```

목록 응답 타입은 실제 백엔드 반환 구조 확인 후 다음 중 하나로 확정한다.

```ts
export type PlayerAnalysisClipDrawingListResponse = PlayerAnalysisClipDrawingResponse[];
```

또는

```ts
export type PlayerAnalysisClipDrawingListResponse = {
  drawings: PlayerAnalysisClipDrawingResponse[];
};
```

주의사항은 다음과 같다.

* `writerId`, `writerName`은 백엔드 응답 필드명 그대로 사용한다.
* `drawingData`는 백엔드에서 JSON으로 응답하므로 `Record<string, unknown>`으로 처리한다.
* 실제 백엔드가 `drawingData`를 문자열로 내려주면 API 계층에서 파싱하거나 타입을 `string | Record<string, unknown>`으로 조정한다.
* 백엔드 DTO 확인 전에는 타입을 확정하지 않는다.

---

## 8. API 파일 설계 방향

API 파일은 다음 위치에 생성한다.

```text
frontend/src/api/playerAnalysisClipDrawingApi.ts
```

구현 함수는 다음과 같다.

```ts
getPlayerAnalysisClipDrawings(playerClipId: number)
getPlayerAnalysisClipDrawing(drawingId: number)
createPlayerAnalysisClipDrawing(playerClipId: number, request)
updatePlayerAnalysisClipDrawing(drawingId: number, request)
deletePlayerAnalysisClipDrawing(drawingId: number)
```

처리 기준은 다음과 같다.

* API 주소는 백엔드 컨트롤러와 정확히 맞춘다.
* `axiosInstance`를 사용한다.
* API 함수 내부에서는 화면 상태를 변경하지 않는다.
* 화면 메시지 처리는 `PlayerAnalysisClipPage.tsx`에서 담당한다.
* 백엔드 에러 응답의 상태 코드, 에러 코드, 메시지를 화면에서 표시할 수 있도록 `throw` 흐름을 유지한다.

---

## 9. 캔버스 컴포넌트 설계 방향

초기 구현에서는 팀 분석 드로잉 컴포넌트 구조를 참고하되, 선수 개인 분석 도메인에 맞춰 별도 컴포넌트를 생성한다.

생성 파일은 다음과 같다.

```text
frontend/src/components/PlayerAnalysisDrawingCanvas.tsx
```

별도 컴포넌트로 분리하는 이유는 다음과 같다.

* 팀 분석 클립과 선수 개인 분석 클립의 타입명이 다르다.
* 향후 개인 피드백 전용 UI가 추가될 가능성이 있다.
* 기존 팀 분석 드로잉 기능에 영향을 주지 않고 구현할 수 있다.

단, 내부 로직은 기존 `TeamAnalysisDrawingCanvas.tsx`의 구조를 최대한 재사용한다.

### 9.1 드로잉 도구

초기 지원 도구는 다음과 같다.

* 선 `LINE`
* 화살표 `ARROW`
* 원 `CIRCLE`
* 박스 `BOX`
* 영역 `AREA`
* 텍스트 `TEXT`

### 9.2 좌표 저장 기준

좌표 저장 기준은 다음과 같다.

* 영상 표시 영역 기준 상대 좌표를 사용한다.
* `x`, `y`, `fromX`, `fromY`, `toX`, `toY` 값은 0~1 사이 비율로 저장한다.
* 캔버스 렌더링 시 상대 좌표를 실제 캔버스 width, height 기준으로 변환한다.
* 화면 크기가 달라져도 동일한 위치에 표시되도록 한다.

### 9.3 영상 오버레이 렌더링 정책

선수 개인 분석 클립 상세 영역에서 `<video>` 위에 `<canvas>`를 겹쳐 표시한다.

처리 기준은 다음과 같다.

1. 영상 컨테이너를 `position: relative`로 둔다.
2. `<video>`를 렌더링한다.
3. `<canvas>`를 `position: absolute`로 영상 위에 겹친다.
4. 영상 크기가 바뀌면 캔버스 크기도 함께 갱신한다.
5. 현재 재생 시간 `video.currentTime`을 기준으로 표시할 드로잉을 필터링한다.
6. `startTimeSec <= currentTime <= endTimeSec`인 드로잉만 캔버스에 그린다.
7. 드로잉이 없는 시간대에는 캔버스를 clear 한다.

---

## 10. `PlayerAnalysisClipPage.tsx` 연동 정책

기존 선수 개인 분석 클립 화면에 다음 내용을 추가한다.

* 선택된 선수 개인 분석 클립 상세 기준 드로잉 목록 조회
* 영상 위 캔버스 컴포넌트 연결
* 현재 재생 시간 상태 관리
* 드로잉 등록 폼 추가
* 드로잉 수정 폼 또는 수정 모드 추가
* 드로잉 삭제 버튼 추가
* `COACH`, `ANALYST`, `PLAYER` 권한별 UI 노출 제어
* 드로잉 등록/수정/삭제 성공 후 목록 재조회
* 드로잉 시간 범위 프론트 1차 검증
* 드로잉 API 오류 메시지 표시

주의사항은 다음과 같다.

* 기존 선수 개인 분석 클립 목록/상세/등록/수정/삭제 기능을 깨뜨리지 않는다.
* 기존 상세 선택 상태를 불필요하게 초기화하지 않는다.
* 드로잉 목록 조회 실패가 클립 상세 조회 실패처럼 처리되지 않게 분리한다.
* 드로잉 에러 메시지는 클립 상세 에러 메시지와 분리한다.
* `useEffect` 시작 직후 동기 `setState`를 호출하지 않는다.
* `ignore` 플래그를 사용해 언마운트 후 상태 업데이트를 방지한다.

---

## 11. 예외 상황

### 11.1 API 오류

* 드로잉 목록 조회 실패 시 기존 선수 개인 분석 클립 상세 정보는 유지한다.
* 드로잉 영역에는 오류 메시지를 표시한다.
* 등록 실패 시 입력값은 유지한다.
* 수정 실패 시 기존 상세 정보와 기존 드로잉 목록을 임의로 변경하지 않는다.
* 삭제 실패 시 목록에서 임의로 제거하지 않는다.
* 권한 오류가 발생하면 백엔드 메시지를 우선 표시한다.
* 전체 공통 에러 처리는 추후 모든 기능 프론트 연결 후 배포 전 마지막 단계에서 정리한다.

### 11.2 영상/캔버스 오류

* 영상 URL이 없으면 드로잉 캔버스를 비활성화한다.
* 영상 메타데이터 로드 전에는 캔버스 좌표 계산을 하지 않는다.
* 영상 크기가 0이면 캔버스 렌더링을 건너뛴다.
* 모바일에서는 드로잉 작성 UI보다 조회 UI를 우선한다.

### 11.3 시간 범위 오류

* 드로잉 시작 시간은 종료 시간보다 작아야 한다.
* 드로잉 시간은 선수 개인 분석 클립의 `startTimeSec`, `endTimeSec` 범위 안에 있어야 한다.
* 프론트에서 1차 검증하되, 최종 검증은 백엔드에서 처리한다.

### 11.4 권한 오류

* `PLAYER`에게 등록, 수정, 삭제 UI를 노출하지 않는다.
* `ANALYST`에게 삭제 UI를 노출하지 않는다.
* 프론트 UI가 숨겨져 있어도 백엔드에서 반드시 차단해야 한다.
* `PLAYER`가 다른 선수 개인 분석 클립 드로잉에 접근하면 백엔드에서 `403 Forbidden`으로 차단해야 한다.

---

## 12. 구현 파일

이번 기능에서 생성/수정할 파일은 다음과 같다.

```text
docs/23_player_analysis_clip_drawing_frontend_integration_requirements.md
frontend/src/types/playerAnalysisClipDrawing.ts
frontend/src/api/playerAnalysisClipDrawingApi.ts
frontend/src/components/PlayerAnalysisDrawingCanvas.tsx
frontend/src/pages/PlayerAnalysisClipPage.tsx
```

수정하지 않는 파일은 다음과 같다.

```text
frontend/src/constants/routes.ts
frontend/src/App.tsx
frontend/src/pages/DashboardHomePage.tsx
frontend/src/pages/PlayerHomePage.tsx
frontend/src/pages/MobileHomePage.tsx
```

이유는 이번 기능은 기존 `/player-analysis-clips` 화면 내부 확장이므로 새 라우트나 메뉴가 필요하지 않기 때문이다.

---

## 13. 구현 순서

1. `docs/23_player_analysis_clip_drawing_frontend_integration_requirements.md` 문서를 작성한다.
2. 백엔드 `PlayerAnalysisClipDrawingController`의 API 주소를 확인한다.
3. 백엔드 요청 DTO와 응답 DTO 필드명을 확인한다.
4. 드로잉 목록 응답 구조가 배열인지 객체인지 확인한다.
5. 기존 팀 분석 클립 드로잉 프론트 파일 구조를 확인한다.
6. 기존 선수 개인 분석 클립 프론트 화면 구조를 확인한다.
7. `frontend/src/types/playerAnalysisClipDrawing.ts` 타입 파일을 작성한다.
8. `frontend/src/api/playerAnalysisClipDrawingApi.ts` API 파일을 작성한다.
9. `frontend/src/components/PlayerAnalysisDrawingCanvas.tsx` 컴포넌트를 작성한다.
10. `PlayerAnalysisClipPage.tsx` 상세 영역에 드로잉 목록 조회를 연결한다.
11. 영상 위 캔버스 오버레이를 연결한다.
12. 드로잉 등록 UI와 API 호출을 연결한다.
13. 드로잉 수정 UI와 API 호출을 연결한다.
14. 드로잉 삭제 UI와 API 호출을 연결한다.
15. 권한별 UI 노출을 적용한다.
16. `npm run build`를 실행한다.
17. `npm run dev`로 화면 동작을 확인한다.
18. 실제 API 기능 테스트를 진행한다.

---

## 14. 테스트 순서

### 14.1 공통 테스트

* `/player-analysis-clips` 페이지 접근 확인
* 선수 개인 분석 클립 목록 조회 확인
* 선수 개인 분석 클립 상세 조회 확인
* 상세 선택 시 드로잉 목록 조회 확인
* 영상 재생 시 시간 범위에 맞는 드로잉 표시 확인
* 드로잉 없는 시간대에 캔버스 clear 확인

### 14.2 `COACH` 테스트

* 드로잉 등록 UI 노출 확인
* 드로잉 수정 UI 노출 확인
* 드로잉 삭제 UI 노출 확인
* 드로잉 등록 성공 확인
* 드로잉 수정 성공 확인
* 드로잉 삭제 성공 확인
* 등록/수정/삭제 후 목록 재조회 확인

### 14.3 `ANALYST` 테스트

* 드로잉 등록 UI 노출 확인
* 드로잉 수정 UI 노출 확인
* 드로잉 삭제 UI 미노출 확인
* 드로잉 등록 성공 확인
* 드로잉 수정 성공 확인
* 삭제 API 직접 호출 시 백엔드 차단 확인

### 14.4 `PLAYER` 테스트

* 본인 개인 분석 클립 드로잉 목록 조회 확인
* 본인 개인 분석 클립 영상 위 드로잉 표시 확인
* 드로잉 등록/수정/삭제 UI 미노출 확인
* 다른 선수 개인 분석 클립 드로잉 접근 시 백엔드 차단 확인

### 14.5 오류 테스트

* 존재하지 않는 `playerClipId` 드로잉 목록 조회 실패 메시지 확인
* 존재하지 않는 `drawingId` 수정 실패 메시지 확인
* 드로잉 시간이 클립 범위를 벗어난 경우 실패 메시지 확인
* `drawingData`가 비어 있는 경우 실패 메시지 확인
* 권한 없는 요청 실패 메시지 확인

---

## 15. 추후 확장 가능성

추후 다음 기능으로 확장할 수 있다.

* 팀/선수 공통 드로잉 캔버스 컴포넌트 리팩토링
* 드로잉 레이어 순서 관리
* 드로잉 그룹 관리
* 드로잉 복제
* 드로잉 템플릿 저장
* 드로잉 작성자별 필터
* 드로잉 수정 이력
* 특정 선수 태그 연결
* 드로잉과 선수 피드백 확인 여부 연결
* 선수 개인 분석 클립 조회 기록 프론트 표시
* 모바일 선수 전용 드로잉 조회 UI 개선
* 실제 영상 위 정교한 캔버스 좌표 보정
* AI 이벤트와 드로잉 연결

---

## 16. 주의사항

* 선수 개인 분석 클립 드로잉은 개인 피드백 데이터이므로 접근 제어가 가장 중요하다.
* 프론트 버튼 숨김은 보안이 아니며 반드시 백엔드 권한 검증을 유지한다.
* `PLAYER`는 본인 개인 분석 클립 드로잉만 조회할 수 있어야 한다.
* `ANALYST`는 등록과 수정은 가능하지만 삭제는 불가능하다.
* 삭제는 실제 삭제가 아니라 `is_deleted = true` 소프트 삭제로 처리한다.
* 드로잉 시간은 연결된 선수 개인 분석 클립 시간 범위를 벗어나면 안 된다.
* `drawingData`는 JSON으로 저장하되 운영 전에는 요청 크기 제한을 검토해야 한다.
* 백엔드 DTO와 API 응답 필드명을 확인하지 않은 상태로 타입을 확정하지 않는다.
* React `useEffect`에서 동기 `setState` 호출로 Compiler 경고가 반복되지 않게 작성한다.
* 수정이나 에러 때문에 코드를 교체해야 하는 경우 수정 반영한 전체 코드로 관리한다.
