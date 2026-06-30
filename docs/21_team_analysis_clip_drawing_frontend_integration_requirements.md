# 21. 팀 분석 클립 드로잉 프론트 연동 요구사항

## 1. 결론

팀 분석 클립 드로잉 프론트 연동 기능은 백엔드에 구현된 팀 분석 클립 드로잉 API를 React 화면에 연결해, 지도자와 분석관이 팀 분석 클립 영상 위에 전술 설명용 드로잉을 등록/수정하고, 선수는 해당 드로잉을 조회할 수 있게 하는 기능이다.

이번 구현 기준은 다음과 같다.

* 프론트는 기존 React + TypeScript + Vite 구조를 사용한다.
* 인증은 기존 로그인/JWT 인증 구조를 사용한다.
* Access Token은 기존 `axiosInstance`에서 Authorization 헤더에 자동으로 포함한다.
* 팀 분석 클립 드로잉은 기존 팀 분석 클립 상세 화면 안에 통합한다.
* 별도 페이지를 새로 만들기보다 `/team-analysis-clips` 화면의 상세 영역에 드로잉 UI를 추가한다.
* 실제 영상 파일에는 그래픽을 합성하지 않는다.
* 프론트 캔버스에서 생성한 드로잉 데이터를 JSON으로 저장한다.
* 조회 시 저장된 JSON 데이터를 다시 캔버스 오버레이로 렌더링한다.
* 드로잉은 팀 분석 클립의 원본 경기 영상 위에 표시한다.
* 영상 재생 시간이 드로잉의 `startTimeSec`, `endTimeSec` 범위 안에 있을 때만 해당 드로잉을 표시한다.
* `COACH`는 드로잉 등록/조회/수정/삭제 UI를 볼 수 있다.
* `ANALYST`는 드로잉 등록/조회/수정 UI를 볼 수 있고 삭제 UI는 볼 수 없다.
* `PLAYER`는 드로잉 조회만 가능하며 등록/수정/삭제 UI는 볼 수 없다.
* 프론트의 버튼 숨김은 UX 목적이며, 실제 권한 검증은 반드시 백엔드에서 처리한다.
* 프론트 타입은 실제 백엔드 DTO 필드명과 1:1로 맞춘다.
* 백엔드 DTO 필드명이 확인되기 전에는 `drawingData`, `drawingJson`, `shapeType`, `displayStartTimeSec`, `displayEndTimeSec`, `editorName` 같은 이름을 임의로 확정하지 않는다.
* 프론트 파일 상단에는 해당 파일 역할을 설명하는 한 줄 주석을 작성한다.

---

## 2. 기능 목적

팀 분석 클립 드로잉 프론트 연동의 목적은 지도자와 분석관이 팀 분석 클립을 보면서 전술적으로 중요한 장면을 시각적으로 설명할 수 있게 하는 것이다.

이 기능은 단순한 영상 조회 기능이 아니라, 실제 서비스의 영상 분석 경쟁력을 만드는 핵심 기능이다.

예를 들어 다음 분석을 화면에서 표현할 수 있어야 한다.

* 패스 방향 화살표
* 수비 라인 표시
* 압박 구역 표시
* 특정 선수 강조 원
* 위험 지역 박스
* 텍스트 메모
* 특정 시간 동안만 유지되는 드로잉

초기 버전에서는 완성도 높은 전문 영상 편집기보다, 다음을 우선한다.

* 팀 분석 클립 위에 드로잉을 만들 수 있다.
* 드로잉을 저장하고 다시 조회할 수 있다.
* 영상 시간에 맞춰 드로잉을 표시할 수 있다.
* 권한별로 등록/수정/삭제 버튼 노출을 제어한다.
* 실제 권한 검증은 백엔드 정책을 따른다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

가능한 기능은 다음과 같다.

* 팀 분석 클립 조회
* 팀 분석 클립 영상 구간 재생
* 팀 분석 클립 드로잉 목록 조회
* 팀 분석 클립 드로잉 상세 조회
* 팀 분석 클립 드로잉 등록
* 팀 분석 클립 드로잉 수정
* 팀 분석 클립 드로잉 삭제

화면에서는 다음 UI를 볼 수 있다.

* 드로잉 도구 버튼
* 드로잉 시작/종료 시간 입력
* 드로잉 등록 버튼
* 드로잉 목록
* 드로잉 수정 버튼
* 드로잉 삭제 버튼

### 3.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

* 팀 분석 클립 조회
* 팀 분석 클립 영상 구간 재생
* 팀 분석 클립 드로잉 목록 조회
* 팀 분석 클립 드로잉 상세 조회
* 팀 분석 클립 드로잉 등록
* 팀 분석 클립 드로잉 수정

불가능한 기능은 다음과 같다.

* 팀 분석 클립 드로잉 삭제

화면에서는 삭제 버튼을 노출하지 않는다.

### 3.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

* 팀 분석 클립 조회
* 팀 분석 클립 영상 구간 재생
* 팀 분석 클립 드로잉 목록 조회
* 영상 재생 시간에 맞는 드로잉 확인

불가능한 기능은 다음과 같다.

* 팀 분석 클립 드로잉 등록
* 팀 분석 클립 드로잉 수정
* 팀 분석 클립 드로잉 삭제

화면에서는 드로잉 도구, 등록 폼, 수정 버튼, 삭제 버튼을 노출하지 않는다.

---

## 4. 프론트 권한 노출 정책

| 역할        | 드로잉 조회 | 드로잉 등록 UI | 드로잉 수정 UI | 드로잉 삭제 UI |
| --------- | -----: | --------: | --------: | --------: |
| `COACH`   |     가능 |        노출 |        노출 |        노출 |
| `ANALYST` |     가능 |        노출 |        노출 |       미노출 |
| `PLAYER`  |     가능 |       미노출 |       미노출 |       미노출 |

권한 처리 기준은 다음과 같다.

* `/team-analysis-clips` 라우트는 로그인 사용자라면 접근 가능하다.
* 팀 분석 클립 상세 선택 후 드로잉 목록을 조회한다.
* `COACH`, `ANALYST`에게만 드로잉 작성 영역을 보여준다.
* `COACH`, `ANALYST`에게만 드로잉 수정 버튼을 보여준다.
* `COACH`에게만 드로잉 삭제 버튼을 보여준다.
* `PLAYER`는 영상과 드로잉 조회만 가능하다.
* 프론트에서 버튼을 숨기더라도 API 직접 호출은 가능하므로 백엔드 권한 검증은 반드시 유지한다.

---

## 5. 화면 흐름

### 5.1 진입 위치

기존 팀 분석 클립 화면을 사용한다.

```text
/team-analysis-clips
```

라우트 상수는 기존 값을 사용한다.

```ts
TEAM_ANALYSIS_CLIP: "/team-analysis-clips"
```

### 5.2 공통 조회 흐름

1. 사용자가 로그인한다.
2. 팀 분석 클립 화면에 진입한다.
3. 팀 분석 클립 목록을 조회한다.
4. 사용자가 특정 팀 분석 클립을 선택한다.
5. 팀 분석 클립 상세 정보를 조회한다.
6. 원본 경기 영상 URL과 클립 시작/종료 시간을 기준으로 영상을 재생한다.
7. 선택된 `teamClipId` 기준으로 드로잉 목록 API를 호출한다.
8. 드로잉 목록을 상태로 저장한다.
9. 영상 재생 시간이 각 드로잉의 표시 시간 범위 안에 들어오면 캔버스 오버레이에 표시한다.

### 5.3 지도자/분석관 드로잉 등록 흐름

1. `COACH` 또는 `ANALYST`가 팀 분석 클립 상세를 선택한다.
2. 영상에서 설명할 장면으로 이동한다.
3. 영상을 정지한다.
4. 드로잉 도구에서 선, 화살표, 원, 박스, 텍스트 중 하나를 선택한다.
5. 영상 위 캔버스에 드로잉을 작성한다.
6. 드로잉 시작 시간과 종료 시간을 입력한다.
7. 프론트는 캔버스 데이터를 JSON 형태로 만든다.
8. 드로잉 등록 API를 호출한다.
9. 등록 성공 후 드로잉 목록을 다시 조회한다.
10. 영상 재생 시 등록된 드로잉이 시간에 맞춰 표시된다.

### 5.4 지도자/분석관 드로잉 수정 흐름

1. 드로잉 목록에서 수정할 드로잉을 선택한다.
2. 기존 드로잉 데이터를 캔버스 또는 수정 폼에 반영한다.
3. 드로잉 유형, 시작 시간, 종료 시간, JSON 데이터를 수정한다.
4. 수정 API를 호출한다.
5. 수정 성공 후 드로잉 목록을 다시 조회한다.

### 5.5 지도자 드로잉 삭제 흐름

1. `COACH`가 드로잉 목록에서 삭제 버튼을 누른다.
2. 확인창을 표시한다.
3. 삭제 API를 호출한다.
4. 삭제 성공 후 드로잉 목록을 다시 조회한다.
5. 삭제된 드로잉은 화면에 표시하지 않는다.

### 5.6 선수 조회 흐름

1. `PLAYER`가 팀 분석 클립 상세를 선택한다.
2. 드로잉 목록을 조회한다.
3. 등록/수정/삭제 UI는 보이지 않는다.
4. 영상 재생 시간에 맞는 드로잉만 오버레이로 표시된다.

---

## 6. API 흐름

실제 API 주소와 DTO 필드명은 백엔드 Controller/DTO 코드 확인 후 최종 확정한다.

현재 요구사항 기준 API는 다음과 같다.

### 6.1 드로잉 목록 조회

```http
GET /api/team-analysis-clips/{teamClipId}/drawings
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`
* `PLAYER`

프론트 처리 기준은 다음과 같다.

* 팀 분석 클립 상세 선택 시 호출한다.
* 응답의 배열 필드명은 실제 백엔드 목록 응답 DTO를 확인한 뒤 그대로 사용한다.
* 임의로 `drawings`, `teamDrawings`, `drawingList` 등을 확정하지 않는다.

요구사항 기준 응답 예시는 다음과 같다.

```json
{
  "drawings": [
    {
      "drawingId": 1,
      "teamClipId": 3,
      "drawingType": "ARROW",
      "startTimeSec": 755,
      "endTimeSec": 760,
      "drawingData": {
        "fromX": 0.35,
        "fromY": 0.42,
        "toX": 0.62,
        "toY": 0.38,
        "color": "#ff0000",
        "lineWidth": 4
      },
      "writerId": 2,
      "writerName": "김지도",
      "createdAt": "2026-06-20T19:10:00",
      "updatedAt": "2026-06-20T19:10:00"
    }
  ]
}
```

### 6.2 드로잉 상세 조회

```http
GET /api/team-analysis-clip-drawings/{drawingId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`
* `PLAYER`

초기 프론트 구현에서는 목록 응답만으로 수정/표시가 가능하면 상세 조회 호출은 생략할 수 있다.

단, 백엔드 상세 DTO와 목록 DTO가 다르면 상세 조회 API를 사용한다.

### 6.3 드로잉 등록

```http
POST /api/management/team-analysis-clips/{teamClipId}/drawings
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요구사항 기준 요청 예시는 다음과 같다.

```json
{
  "drawingType": "TEXT",
  "startTimeSec": 755,
  "endTimeSec": 760,
  "drawingData": {
    "x": 0.35,
    "y": 0.22,
    "text": "여기서 전진 패스 가능",
    "color": "#ffffff",
    "fontSize": 18
  }
}
```

프론트 타입은 실제 백엔드 요청 DTO 필드명을 확인한 뒤 작성한다.

### 6.4 드로잉 수정

```http
PATCH /api/management/team-analysis-clip-drawings/{drawingId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요구사항 기준 요청 예시는 다음과 같다.

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

### 6.5 드로잉 삭제

```http
DELETE /api/coach/team-analysis-clip-drawings/{drawingId}
```

사용 가능 역할은 다음과 같다.

* `COACH`

삭제 성공 후 프론트는 드로잉 목록을 다시 조회한다.

삭제는 드로잉 데이터만 삭제 처리하며, 팀 분석 클립과 원본 경기 영상은 삭제하지 않는다.

---

## 7. 타입 설계 방향

프론트 타입 파일은 다음 위치에 둔다.

```text
frontend/src/types/teamAnalysisClipDrawing.ts
```

작성 전 반드시 실제 백엔드 DTO 필드명을 확인한다.

요구사항 기준 타입 초안은 다음과 같다.

```ts
export type TeamAnalysisClipDrawingType =
  | "LINE"
  | "ARROW"
  | "CIRCLE"
  | "BOX"
  | "AREA"
  | "TEXT";

export type TeamAnalysisClipDrawingData = Record<string, unknown>;

export type TeamAnalysisClipDrawingResponse = {
  drawingId: number;
  teamClipId: number;
  drawingType: TeamAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: TeamAnalysisClipDrawingData;
  writerId: number;
  writerName: string;
  createdAt: string;
  updatedAt: string;
};

export type TeamAnalysisClipDrawingListResponse = {
  drawings: TeamAnalysisClipDrawingResponse[];
};

export type CreateTeamAnalysisClipDrawingRequest = {
  drawingType: TeamAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: TeamAnalysisClipDrawingData;
};

export type UpdateTeamAnalysisClipDrawingRequest = {
  drawingType: TeamAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: TeamAnalysisClipDrawingData;
};
```

주의사항:

* 위 타입은 요구사항 기준 초안이다.
* 실제 백엔드 DTO가 `drawingJson`, `drawingDataJson`, `creatorId`, `editorId` 등 다른 이름이면 실제 DTO에 맞춘다.
* 목록 응답 필드가 `drawings`가 아니면 실제 응답 필드명을 사용한다.
* 프론트 타입은 백엔드 응답 필드명과 1:1로 맞춘다.

---

## 8. 캔버스 드로잉 데이터 기준

초기 프론트 구현에서 지원할 드로잉 도구는 다음으로 제한한다.

* 선
* 화살표
* 원
* 박스
* 텍스트

`AREA`, `ZOOM`, `PLAYER_HIGHLIGHT` 등은 백엔드 Enum에 존재하더라도 초기 UI에서 바로 구현하지 않고 추후 확장으로 둔다.

좌표 저장 기준은 다음과 같다.

* 영상 표시 영역 기준 상대 좌표를 사용한다.
* `x`, `y`, `fromX`, `fromY`, `toX`, `toY` 값은 0~1 사이 비율로 저장한다.
* 이렇게 저장하면 PC/모바일 화면 크기가 달라도 같은 위치에 가깝게 표시할 수 있다.
* 색상, 선 두께, 텍스트 크기는 JSON 내부에 함께 저장한다.

예시:

```json
{
  "fromX": 0.25,
  "fromY": 0.4,
  "toX": 0.6,
  "toY": 0.35,
  "color": "#ff0000",
  "lineWidth": 4
}
```

주의사항:

* 실제 운영에서는 JSON 구조 버전 관리가 필요하다.
* 초기에는 `version: 1` 필드를 `drawingData` 안에 포함하는 것을 권장한다.
* 다만 백엔드 검증이 엄격하게 필드 구조를 제한하고 있다면 백엔드 DTO 기준을 우선한다.

---

## 9. 영상 오버레이 렌더링 정책

팀 분석 클립 상세 영역에서 `<video>` 위에 `<canvas>`를 겹쳐 표시한다.

처리 기준은 다음과 같다.

1. 영상 컨테이너를 `position: relative`로 둔다.
2. `<video>`를 렌더링한다.
3. `<canvas>`를 `position: absolute`로 영상 위에 겹친다.
4. 영상 크기가 바뀌면 캔버스 크기도 함께 갱신한다.
5. 현재 재생 시간 `video.currentTime`을 기준으로 표시할 드로잉을 필터링한다.
6. `startTimeSec <= currentTime <= endTimeSec`인 드로잉만 캔버스에 그린다.
7. 캔버스 좌표는 저장된 상대 좌표를 현재 캔버스 실제 width/height로 변환해 사용한다.
8. 드로잉이 없는 시간대에는 캔버스를 clear 한다.

---

## 10. 예외 상황

### 10.1 API 오류

* 드로잉 목록 조회 실패 시 기존 팀 분석 클립 상세 정보는 유지한다.
* 드로잉 영역에는 오류 메시지를 표시한다.
* 등록/수정/삭제 실패 시 alert 또는 화면 메시지로 사용자에게 안내한다.
* 권한 오류가 발생하면 “권한이 없습니다.”로 표시한다.

### 10.2 영상/캔버스 오류

* 영상 URL이 없으면 드로잉 캔버스를 비활성화한다.
* 영상 메타데이터 로드 전에는 캔버스 좌표 계산을 하지 않는다.
* 영상 크기가 0이면 캔버스 렌더링을 건너뛴다.
* 모바일에서는 드로잉 작성 UI보다 조회 UI를 우선한다.

### 10.3 시간 범위 오류

* 드로잉 시작 시간은 종료 시간보다 작아야 한다.
* 드로잉 시간은 팀 분석 클립의 `startTimeSec`, `endTimeSec` 범위 안에 있어야 한다.
* 프론트에서 1차 검증하되, 최종 검증은 백엔드에서 처리한다.

---

## 11. 구현 파일

신규 생성 예상 파일:

```text
frontend/src/types/teamAnalysisClipDrawing.ts
frontend/src/api/teamAnalysisClipDrawingApi.ts
frontend/src/components/TeamAnalysisDrawingCanvas.tsx
```

수정 예상 파일:

```text
frontend/src/pages/TeamAnalysisClipPage.tsx
frontend/src/types/teamAnalysisClip.ts
frontend/src/api/teamAnalysisClipApi.ts
frontend/src/constants/routes.ts
frontend/src/App.tsx
```

라우트와 메뉴는 기존 `/team-analysis-clips`를 그대로 사용할 경우 `routes.ts`, `App.tsx` 수정이 필요 없을 수 있다.

실제 수정 여부는 현재 프론트 코드 확인 후 결정한다.

---

## 12. 구현 순서

1. 백엔드 팀 분석 클립 드로잉 Controller/DTO/Entity 코드 확인
2. 실제 API 주소, 요청 DTO, 응답 DTO, 목록 응답 필드명 확정
3. `21_team_analysis_clip_drawing_frontend_integration_requirements.md` 문서 저장
4. 프론트 드로잉 타입 파일 작성
5. 프론트 드로잉 API 파일 작성
6. 캔버스 오버레이 컴포넌트 작성
7. 기존 `TeamAnalysisClipPage.tsx` 상세 영역에 드로잉 목록 조회 연결
8. 영상 재생 시간에 맞춰 드로잉 표시
9. `COACH`, `ANALYST` 전용 드로잉 등록 UI 추가
10. `COACH`, `ANALYST` 전용 드로잉 수정 UI 추가
11. `COACH` 전용 드로잉 삭제 UI 추가
12. 권한별 버튼 노출 확인
13. 드로잉 조회/등록/수정/삭제 테스트
14. 선수 계정에서 조회만 가능한지 확인
15. 화면 크기 변경 시 캔버스 위치가 깨지지 않는지 확인

---

## 13. 테스트 기준

기능 완료 전 확인할 테스트는 다음과 같다.

* `COACH` 팀 분석 클립 상세 진입 정상
* `COACH` 드로잉 목록 조회 정상
* `COACH` 드로잉 등록 정상
* `COACH` 드로잉 수정 정상
* `COACH` 드로잉 삭제 정상
* `ANALYST` 드로잉 목록 조회 정상
* `ANALYST` 드로잉 등록 정상
* `ANALYST` 드로잉 수정 정상
* `ANALYST` 삭제 버튼 미노출 정상
* `PLAYER` 드로잉 목록 조회 정상
* `PLAYER` 등록/수정/삭제 UI 미노출 정상
* 영상 재생 시간이 드로잉 시간 범위 안일 때만 드로잉 표시 정상
* 영상 재생 시간이 드로잉 시간 범위 밖이면 드로잉 미표시 정상
* 새로고침 후 팀 분석 클립 상세 선택 시 드로잉 다시 조회 정상
* 모바일 화면에서 드로잉 조회가 깨지지 않는지 확인

---

## 14. 추후 확장 가능성

추후 다음 기능으로 확장할 수 있다.

* 드로잉 도구 UI 고도화
* 자유 곡선 그리기
* 선수 번호 기반 자동 강조
* 드로잉 복사/붙여넣기
* 드로잉 순서 변경
* 드로잉 표시 시간 타임라인 편집
* 드로잉 JSON 구조 버전 관리
* 팀 분석 클립별 코멘트와 드로잉 연결
* 실제 클립 파일 생성 시 드로잉 합성 인코딩
* 모바일 드로잉 조회 UI 최적화
* PC 편집기 전용 확대/축소 기능
* 캔버스 렌더링 성능 최적화
