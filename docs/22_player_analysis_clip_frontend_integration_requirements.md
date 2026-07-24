# 22. 선수 개인 분석 클립 프론트 연동 요구사항

> [!IMPORTANT]
> 이 문서는 해당 기능의 기존 구현 당시 기준과 작업 이력을 보존하는 문서다.
> 경기 영상 편집기 v1의 신규 구현은 `docs/31_video_editor_v1_requirements.md`,
> `docs/00_current_backend_policy.md`, `docs/00_current_frontend_policy.md`를 우선한다.
> 역할 권한, 클립 상태, 시간 단위, 삭제, 드로잉 합성, 영상 제공 방식이 충돌하면 최신 문서를 적용한다.
> 기존 API는 프론트 전환 전 호환 목적으로 유지될 수 있으므로 실제 제거 여부는 현재 소스를 확인한다.


## 1. 기능 목적

선수 개인 분석 클립 프론트 연동 기능은 백엔드에 구현된 선수 개인 분석 클립 API를 React 화면과 연결해, 지도자와 분석관이 특정 선수에게 개인 분석 클립을 등록/수정/삭제하고 선수는 본인에게 공유된 개인 분석 클립만 조회할 수 있게 하는 기능이다.

선수 개인 분석 클립은 팀 전체가 보는 팀 분석 클립과 다르게 특정 선수 1명에게만 제공되는 개인 피드백 자료다.

초기 구현에서는 실제 영상 파일을 자르지 않고 원본 경기 영상 URL과 `startTimeSec`, `endTimeSec` 메타데이터를 기준으로 클립 구간을 재생한다.

---

## 2. 사용자 역할

### 2.1 지도자 `COACH`

* 선수 개인 분석 클립 목록 조회
* 선수 개인 분석 클립 상세 조회
* 선수 개인 분석 클립 등록
* 선수 개인 분석 클립 수정
* 선수 개인 분석 클립 삭제
* 원본 경기 영상 선택
* 대상 선수 선택
* 클립 구간 재생

### 2.2 분석관 `ANALYST`

* 선수 개인 분석 클립 목록 조회
* 선수 개인 분석 클립 상세 조회
* 선수 개인 분석 클립 등록
* 선수 개인 분석 클립 수정
* 원본 경기 영상 선택
* 대상 선수 선택
* 클립 구간 재생
* 삭제는 불가

### 2.3 선수 `PLAYER`

* 본인 선수 개인 분석 클립 목록 조회
* 본인 선수 개인 분석 클립 상세 조회
* 본인 선수 개인 분석 클립 구간 재생
* 등록/수정/삭제 불가
* 다른 선수의 개인 분석 클립 조회 불가

---

## 3. 권한 정책

| 역할        |  목록 조회 |  상세 조회 | 등록 | 수정 | 삭제 |
| --------- | -----: | -----: | -: | -: | -: |
| `COACH`   |  전체 가능 |  전체 가능 | 가능 | 가능 | 가능 |
| `ANALYST` |  전체 가능 |  전체 가능 | 가능 | 가능 | 불가 |
| `PLAYER`  | 본인만 가능 | 본인만 가능 | 불가 | 불가 | 불가 |

프론트에서는 역할에 따라 버튼과 폼 노출을 제어한다.

* `COACH`, `ANALYST`는 관리용 API를 사용한다.
* `PLAYER`는 선수 본인 API를 사용한다.
* `PLAYER`는 `playerId`를 요청 파라미터로 보내지 않는다.
* `COACH`에게만 삭제 버튼을 노출한다.
* `ANALYST`에게는 삭제 버튼을 노출하지 않는다.
* `PLAYER`에게는 등록/수정/삭제 UI를 노출하지 않는다.
* 실제 권한 검증은 반드시 백엔드에서 처리한다.

---

## 4. 화면 흐름

### 4.1 페이지 경로

```text
/player-analysis-clips
```

라우트 상수는 다음 값을 사용한다.

```ts
PLAYER_ANALYSIS_CLIP: "/player-analysis-clips"
```

### 4.2 진입 위치

지도자/분석관 PC 대시보드:

* 선수 개인 분석 클립 조회
* 선수 개인 분석 클립 등록
* 선수 개인 분석 클립 수정
* 선수 개인 분석 클립 삭제

선수 PC 홈:

* 내 개인 분석 클립 조회

모바일 홈:

* 선수 개인 분석 클립 조회
* 역할별 권한에 따라 등록/수정/삭제 버튼 노출

### 4.3 화면 구성

선수 개인 분석 클립 페이지는 다음 영역으로 구성한다.

1. 조회 조건 영역

    * 경기 영상 필터
    * 대상 선수 필터
    * 클립 유형 필터

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
    * 대상 선수명
    * 클립 유형
    * 시작/종료 시간
    * 작성자명

4. 선수 개인 분석 클립 상세 영역

    * 원본 경기 영상 재생
    * 클립 구간 재생
    * 처음부터 다시 보기
    * 제목
    * 코멘트
    * 구간 정보
    * 작성자 정보
    * 수정/삭제 버튼

---

## 5. API 흐름

### 5.1 선수 목록 조회 API

선수 개인 분석 클립 등록/수정 화면에서 대상 선수 드롭다운을 구성하기 위해 선수 목록 조회 API를 사용한다.

```http
GET /api/management/players
```

사용 가능 역할:

* `COACH`
* `ANALYST`

응답 형식:

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

프론트 사용 위치:

* 선수 개인 분석 클립 등록 폼
* 선수 개인 분석 클립 수정 폼
* 관리용 조회 필터

`PLAYER`는 이 API를 호출하지 않는다.

---

### 5.2 관리용 선수 개인 분석 클립 목록 조회

```http
GET /api/management/player-analysis-clips?page=0&size=20&matchVideoId={matchVideoId}&playerId={playerId}&clipType={clipType}
```

사용 가능 역할:

* `COACH`
* `ANALYST`

응답 목록 필드명:

```ts
playerClips
```

---

### 5.3 선수 본인 개인 분석 클립 목록 조회

```http
GET /api/player/me/player-analysis-clips?page=0&size=20&matchVideoId={matchVideoId}&clipType={clipType}
```

사용 가능 역할:

* `PLAYER`

주의사항:

* `playerId`를 보내지 않는다.
* 백엔드에서 로그인한 선수 본인 기준으로 조회한다.

---

### 5.4 관리용 상세 조회

```http
GET /api/management/player-analysis-clips/{playerClipId}
```

사용 가능 역할:

* `COACH`
* `ANALYST`

---

### 5.5 선수 본인 상세 조회

```http
GET /api/player/me/player-analysis-clips/{playerClipId}
```

사용 가능 역할:

* `PLAYER`

주의사항:

* 백엔드에서 본인 클립 여부를 검증한다.
* 선수 본인 상세 조회 시 조회 기록 저장/갱신 정책을 따른다.

---

### 5.6 등록

```http
POST /api/management/player-analysis-clips
```

요청 body:

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

사용 가능 역할:

* `COACH`
* `ANALYST`

---

### 5.7 수정

```http
PATCH /api/management/player-analysis-clips/{playerClipId}
```

요청 body:

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

사용 가능 역할:

* `COACH`
* `ANALYST`

---

### 5.8 삭제

```http
DELETE /api/coach/player-analysis-clips/{playerClipId}
```

사용 가능 역할:

* `COACH`

삭제는 소프트 삭제로 처리한다.

---

## 6. 프론트 타입 기준

생성 파일:

```text
frontend/src/types/playerAnalysisClip.ts
```

주요 타입:

* `PlayerSelectItem`
* `PlayerAnalysisClipType`
* `PlayerAnalysisClipListItem`
* `PlayerAnalysisClipDetailResponse`
* `PlayerAnalysisClipPageResponse`
* `CreatePlayerAnalysisClipRequest`
* `UpdatePlayerAnalysisClipRequest`

선수 목록 응답은 다음 필드를 사용한다.

```ts
playerId
name
grade
uniformNumber
```

선수 개인 분석 클립 목록 응답은 다음 필드를 사용한다.

```ts
playerClips
page
size
totalElements
totalPages
```

작성자 정보는 다음 필드를 사용한다.

```ts
editorId
editorName
```

---

## 7. 영상 재생 정책

선수 개인 분석 클립은 실제 영상 파일을 자르지 않는다.

프론트에서는 상세 응답의 `matchVideoUrl`을 원본 영상으로 사용하고, `startTimeSec`, `endTimeSec` 구간만 클립처럼 재생한다.

구현 기준:

1. 상세 조회 응답의 `matchVideoUrl`을 가져온다.
2. `createVideoSourceUrl(matchVideoUrl)`로 실제 영상 재생 URL을 만든다.
3. `<video>` 태그의 `src`에 연결한다.
4. 클립 구간 재생 버튼 클릭 시 `video.currentTime = startTimeSec`로 이동한다.
5. 영상이 재생된다.
6. 현재 시간이 `endTimeSec` 이상이 되면 영상을 일시정지한다.

주의사항:

* 현재 MVP에서는 `/uploads/match-videos/**` 직접 접근 방식을 사용한다.
* 실제 운영 전에는 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식으로 전환해야 한다.
* 선수 개인 분석 클립은 개인 피드백 데이터이므로 영상 접근 제어가 특히 중요하다.

---

## 8. 에러 처리 정책

이번 구현에서는 상세 조회 실패 원인 확인을 위해 백엔드 에러 정보를 화면에 표시하도록 보완했다.

표시 대상:

* HTTP 상태 코드
* 백엔드 에러 코드
* 백엔드 에러 메시지
* 요청 경로

예시:

```text
선수 개인 분석 클립 상세 정보를 불러오지 못했습니다. / 상태: 403 / 코드: PLAYER_ANALYSIS_CLIP_ACCESS_DENIED / 메시지: 선수 개인 분석 클립에 접근할 권한이 없습니다.
```

목록, 상세, 폼 에러 메시지는 분리해서 관리한다.

* `listErrorMessage`
* `detailErrorMessage`
* `formErrorMessage`

이렇게 분리한 이유는 목록 조회 성공 상태에서 상세 조회 실패 메시지가 목록 상태를 덮어쓰지 않게 하기 위함이다.

---

## 9. React useEffect 구현 주의사항

이번 구현 중 React Compiler 경고를 방지하기 위해 `useEffect` 작성 방식을 보완했다.

방지해야 하는 에러:

```text
Error: Calling setState synchronously within an effect can trigger cascading renders
```

프론트 구현 기준:

* `useEffect` 내부에서 실행 즉시 `setState`를 호출하는 함수를 직접 호출하지 않는다.
* `useEffect` 안에서는 effect 전용 `async` 함수를 정의한다.
* API 응답을 받은 뒤에만 `setState`를 실행한다.
* `useEffect` 시작 직후 `setErrorMessage("")`, `setMessage("")`, `setLoading(true)` 같은 동기 setState를 먼저 호출하지 않는다.
* `ignore` 플래그를 사용해 컴포넌트 언마운트 후 setState 실행을 방지한다.
* dependency 배열에는 가능하면 `member` 전체 객체가 아니라 `member?.memberRole` 같은 필요한 원시값만 넣는다.
* 목록 조회처럼 이벤트 핸들러와 effect에서 함께 필요한 로직은 effect 전용 조회 로직과 재조회 로직을 분리한다.
* 앞으로 프론트 기능 구현 시 동일한 경고가 반복되지 않도록 이 기준을 적용한다.

---

## 10. 예외 상황

### 10.1 목록 조회 실패

* 목록 조회 실패 시 `listErrorMessage`에 표시한다.
* 기존 상세 선택 상태를 임의로 삭제하지 않는다.

### 10.2 상세 조회 실패

* 상세 조회 실패 시 `detailErrorMessage`에 표시한다.
* 실패한 상세 클립은 선택 해제한다.
* 백엔드 상태 코드와 메시지를 함께 표시한다.

### 10.3 등록 실패

* 등록 실패 시 `formErrorMessage`에 표시한다.
* 입력값은 유지한다.

### 10.4 수정 실패

* 수정 실패 시 `formErrorMessage`에 표시한다.
* 기존 상세 정보는 임의로 변경하지 않는다.

### 10.5 삭제 실패

* 삭제 실패 시 `formErrorMessage`에 표시한다.
* 목록과 상세 정보를 임의로 제거하지 않는다.

---

## 11. 구현 파일

이번 기능에서 생성/수정한 프론트 파일은 다음과 같다.

```text
frontend/src/App.tsx
frontend/src/pages/DashboardHomePage.tsx
frontend/src/pages/MobileHomePage.tsx
frontend/src/types/playerAnalysisClip.ts
frontend/src/api/playerAnalysisClipApi.ts
frontend/src/pages/PlayerAnalysisClipPage.tsx
frontend/src/pages/PlayerHomePage.tsx
frontend/src/constants/routes.ts
```

문서 파일은 다음 파일을 최종 요구사항 문서로 관리한다.

```text
docs/22_player_analysis_clip_frontend_integration_requirements.md
```

---

## 12. 테스트 결과

사용자가 실제 확인한 테스트 결과는 다음과 같다.

* `build` 정상 실행 확인
* `dev` 정상 실행 확인
* `/player-analysis-clips` 페이지 이동 정상 확인
* 선수 개인 분석 클립 목록 조회 정상 확인
* 선수 개인 분석 클립 상세 조회 정상 확인
* 선수 개인 분석 클립 수정 테스트 정상 확인
* 선수 개인 분석 클립 삭제 테스트 정상 확인
* 전체 테스트 정상 확인

---

## 13. 추후 확장 가능성

추후 확장 가능성은 다음과 같다.

* 선수 개인 분석 클립 드로잉 프론트 연동
* 선수 개인 분석 클립 조회 기록 프론트 표시
* 선수별 개인 분석 클립 통계 화면
* 영상 썸네일 표시
* 클립 구간 타임라인 UI
* 코멘트 작성자/작성일 표시 개선
* 모바일 선수 전용 UI 개선
* 운영 단계 영상 접근 제어 개선
* Signed URL 또는 인증 기반 스트리밍 API 적용
