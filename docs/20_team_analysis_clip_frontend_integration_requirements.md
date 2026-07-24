# 20. 팀 분석 클립 프론트 연동 요구사항

> [!IMPORTANT]
> 이 문서는 해당 기능의 기존 구현 당시 기준과 작업 이력을 보존하는 문서다.
> 경기 영상 편집기 v1의 신규 구현은 `docs/31_video_editor_v1_requirements.md`,
> `docs/00_current_backend_policy.md`, `docs/00_current_frontend_policy.md`를 우선한다.
> 역할 권한, 클립 상태, 시간 단위, 삭제, 드로잉 합성, 영상 제공 방식이 충돌하면 최신 문서를 적용한다.
> 기존 API는 프론트 전환 전 호환 목적으로 유지될 수 있으므로 실제 제거 여부는 현재 소스를 확인한다.


## 1. 기능 목적

팀 분석 클립 프론트 연동 기능의 목적은 지도자, 분석관, 선수가 팀 분석 클립을 하나의 화면에서 조회하고, 권한에 따라 등록/수정/삭제할 수 있도록 하는 것이다.

팀 분석 클립은 원본 경기 영상을 실제로 자르지 않고, 원본 경기 영상 URL과 시작/종료 시간 메타데이터를 기준으로 특정 구간을 클립처럼 재생한다.

초기 버전에서는 실제 클립 파일 생성, 인코딩, 썸네일 생성, CDN 연동은 하지 않는다.

## 2. 사용자 역할

### 지도자 `COACH`

* 팀 분석 클립 목록 조회
* 팀 분석 클립 상세 조회
* 팀 분석 클립 등록
* 팀 분석 클립 수정
* 팀 분석 클립 삭제
* 원본 경기 영상 선택 후 팀 분석 클립 생성 가능

### 분석관 `ANALYST`

* 팀 분석 클립 목록 조회
* 팀 분석 클립 상세 조회
* 팀 분석 클립 등록
* 팀 분석 클립 수정
* 팀 분석 클립 삭제 불가

### 선수 `PLAYER`

* 팀 분석 클립 목록 조회
* 팀 분석 클립 상세 조회
* 팀 분석 클립 영상 재생
* 팀 분석 클립 등록/수정/삭제 불가

## 3. 권한 정책

프론트에서는 역할에 따라 버튼과 입력 폼 노출을 제어한다.

* `COACH`, `ANALYST`는 팀 분석 클립 등록 폼을 볼 수 있다.
* `COACH`, `ANALYST`는 팀 분석 클립 수정 버튼을 볼 수 있다.
* `COACH`만 팀 분석 클립 삭제 버튼을 볼 수 있다.
* `PLAYER`는 조회와 영상 재생만 가능하다.
* `PLAYER`에게 등록 폼, 수정 버튼, 삭제 버튼은 노출하지 않는다.

단, 프론트 권한 제어는 사용자 경험을 위한 처리일 뿐이며 실제 권한 검증은 반드시 백엔드에서 처리한다.

## 4. 화면 흐름

### 4.1 페이지 경로

```text
/team-analysis-clips
```

라우트 상수는 다음 값을 사용한다.

```ts
TEAM_ANALYSIS_CLIP: "/team-analysis-clips"
```

### 4.2 진입 위치

관리자/지도자/분석관 홈 화면:

* 팀 분석 클립 관리 메뉴에서 진입

선수 홈 화면:

* 팀 분석 영상 메뉴에서 진입

모바일 홈 화면:

* 팀 분석 영상 메뉴에서 진입

### 4.3 화면 구성

팀 분석 클립 페이지는 다음 영역으로 구성한다.

1. 조회 조건 영역

    * 원본 경기 영상 필터
    * 클립 유형 필터

2. 팀 분석 클립 등록 영역

    * `COACH`, `ANALYST`에게만 노출
    * 원본 경기 영상 선택
    * 클립 유형 선택
    * 제목 입력
    * 코멘트 입력
    * 시작 시간 입력
    * 종료 시간 입력

3. 팀 분석 클립 목록 영역

    * 클립 제목
    * 원본 경기 영상 제목
    * 클립 유형
    * 시작/종료 시간
    * 작성자 표시

4. 팀 분석 클립 상세 영역

    * 원본 경기 영상 플레이어
    * 클립 구간 재생 버튼
    * 처음부터 다시 보기 버튼
    * 원본 경기 영상 제목
    * 클립 유형
    * 제목
    * 코멘트
    * 시작/종료 시간
    * 작성자
    * 권한에 따른 수정/삭제 버튼

## 5. API 흐름

## 5.1 경기 영상 목록 조회

팀 분석 클립 등록 시 원본 경기 영상을 선택해야 하므로 경기 영상 목록 조회 API를 사용한다.

```text
GET /api/match-videos?page=0&size=50
```

프론트 호출 방식은 다음과 같다.

```ts
getMatchVideos(0, 50)
```

`getMatchVideos` 함수는 객체가 아니라 `page`, `size` 숫자 인자를 받는다.

## 5.2 팀 분석 클립 목록 조회

```text
GET /api/team-analysis-clips?page=0&size=20&matchVideoId={matchVideoId}&clipType={clipType}
```

`matchVideoId`, `clipType`은 선택 조건이 없으면 전달하지 않는다.

백엔드 응답 배열 필드명은 다음과 같다.

```ts
teamAnalysisClips
```

프론트 타입도 반드시 `teamAnalysisClips`로 맞춘다.

## 5.3 팀 분석 클립 상세 조회

```text
GET /api/team-analysis-clips/{teamClipId}
```

상세 조회 응답에는 원본 경기 영상 URL과 원본 경기 영상 길이 정보가 포함된다.

사용 필드:

```text
teamClipId
matchVideoId
matchVideoUrl
matchVideoTitle
matchVideoDurationSec
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

## 5.4 팀 분석 클립 등록

```text
POST /api/management/team-analysis-clips
```

요청 body:

```json
{
  "matchVideoId": 1,
  "clipType": "ATTACK",
  "title": "전방 압박 성공 장면",
  "comment": "전방 3명이 동시에 압박을 시작한 장면",
  "startTimeSec": 5,
  "endTimeSec": 15
}
```

등록 시에는 원본 경기 영상을 선택해야 하므로 `matchVideoId`를 포함한다.

## 5.5 팀 분석 클립 수정

```text
PATCH /api/management/team-analysis-clips/{teamClipId}
```

요청 body:

```json
{
  "clipType": "DEFENSE",
  "title": "수비 전환 지연 장면",
  "comment": "공을 잃은 뒤 압박 전환이 늦은 장면",
  "startTimeSec": 10,
  "endTimeSec": 20
}
```

수정 시에는 원본 경기 영상을 변경하지 않는다.

따라서 수정 요청 body에는 `matchVideoId`를 포함하지 않는다.

원본 경기 영상을 변경해야 하는 경우 기존 팀 분석 클립을 삭제하고 새 팀 분석 클립을 등록하는 방식으로 처리한다.

## 5.6 팀 분석 클립 삭제

```text
DELETE /api/coach/team-analysis-clips/{teamClipId}
```

삭제는 `COACH`만 가능하다.

삭제 시 팀 분석 클립만 삭제 처리되며 원본 경기 영상은 삭제되지 않는다.

## 6. 프론트 타입 기준

팀 분석 클립 목록 응답 타입은 백엔드 DTO에 맞춰 다음 필드명을 사용한다.

```ts
export type TeamAnalysisClipPageResponse = {
  teamAnalysisClips: TeamAnalysisClipListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
```

작성자 정보는 `writerId`, `writerName`이 아니라 백엔드 응답 기준인 `editorId`, `editorName`을 사용한다.

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
  editorId: number;
  editorName: string;
  createdAt: string;
};
```

상세 응답도 동일하게 `editorId`, `editorName`을 사용한다.

```ts
export type TeamAnalysisClipDetailResponse = {
  teamClipId: number;
  matchVideoId: number;
  matchVideoUrl: string;
  matchVideoTitle: string;
  matchVideoDurationSec: number;
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
  status: TeamAnalysisClipStatus;
  editorId: number;
  editorName: string;
  createdAt: string;
  updatedAt: string;
};
```

## 7. 영상 재생 정책

팀 분석 클립은 실제 잘린 영상 파일이 아니라 원본 경기 영상과 시간 메타데이터를 기준으로 재생한다.

재생 방식:

1. 상세 조회 응답의 `matchVideoUrl`을 `<video>` 태그에 연결한다.
2. `createVideoSourceUrl()`을 사용해 브라우저에서 재생 가능한 URL로 변환한다.
3. 클립 구간 재생 버튼 클릭 시 `video.currentTime`을 `startTimeSec`으로 이동한다.
4. 영상이 재생되다가 `endTimeSec` 이상이 되면 자동으로 정지한다.
5. 처음부터 다시 보기 버튼 클릭 시 다시 `startTimeSec`으로 이동 후 재생한다.

## 8. 예외 상황

### 8.1 원본 경기 영상 미선택

팀 분석 클립 등록 시 원본 경기 영상을 선택하지 않으면 등록 요청을 보내지 않는다.

화면 메시지:

```text
원본 경기 영상을 선택해주세요.
```

### 8.2 제목 미입력

제목이 비어 있으면 등록/수정 요청을 보내지 않는다.

화면 메시지:

```text
클립 제목을 입력해주세요.
```

### 8.3 시작 시간이 종료 시간보다 크거나 같은 경우

`startTimeSec >= endTimeSec`이면 등록/수정 요청을 보내지 않는다.

화면 메시지:

```text
시작 시간은 종료 시간보다 작아야 합니다.
```

### 8.4 목록 응답 배열 필드 불일치

백엔드 응답 배열 필드명은 `teamAnalysisClips`이다.

프론트에서 `teamClips`로 접근하면 `undefined`가 되어 화면 렌더링 중 `clips.length` 오류가 발생할 수 있다.

따라서 프론트는 반드시 `response.teamAnalysisClips`를 사용한다.

### 8.5 작성자 필드 불일치

백엔드 상세/목록 응답은 작성자 정보를 `editorId`, `editorName`으로 반환한다.

프론트에서 `writerId`, `writerName`으로 접근하면 작성자가 화면에 표시되지 않는다.

따라서 프론트는 반드시 `editorId`, `editorName`을 사용한다.

### 8.6 영상 요청 중단 로그

현재 MVP에서는 `/uploads/match-videos/**`를 직접 `<video>` 태그로 재생한다.

이 과정에서 사용자가 클립 구간을 이동하거나, 영상을 멈추거나, 상세 클립을 변경하거나, 페이지를 이동하면 브라우저가 기존 mp4 요청을 중단할 수 있다.

이 경우 서버 콘솔에 다음 로그가 발생할 수 있다.

```text
org.apache.catalina.connector.ClientAbortException
java.io.IOException: 현재 연결은 사용자의 호스트 시스템의 소프트웨어에 의해 중단되었습니다
```

또는 공통 예외 처리 과정에서 다음 로그가 발생할 수 있다.

```text
HttpMessageNotWritableException:
No converter for ErrorResponse with preset Content-Type 'video/mp4;charset=UTF-8'
```

현재 기능 동작에는 문제가 없으므로 모든 주요 기능의 프론트 연동이 끝난 뒤, 배포 전 마지막 정리 단계에서 처리한다.

배포 전 처리 방향:

* `ClientAbortException`은 일반 API 오류로 보지 않는다.
* 영상 요청 중단 예외는 `ErrorResponse`로 변환하지 않는다.
* `GlobalExceptionHandler`에서 별도 처리하거나 로그 레벨을 낮춘다.
* 운영 전에는 권한 검증이 포함된 스트리밍 API 또는 Signed URL 방식도 함께 검토한다.

## 9. 구현 파일

프론트 구현 파일:

```text
frontend/src/types/teamAnalysisClip.ts
frontend/src/api/teamAnalysisClipApi.ts
frontend/src/pages/TeamAnalysisClipPage.tsx
frontend/src/constants/routes.ts
frontend/src/App.tsx
frontend/src/pages/DashboardHomePage.tsx
frontend/src/pages/PlayerHomePage.tsx
frontend/src/pages/MobileHomePage.tsx
```

## 10. 구현 순서

1. 팀 분석 클립 타입 파일 작성
2. 팀 분석 클립 API 파일 작성
3. 팀 분석 클립 페이지 작성
4. 라우트 상수 추가
5. `App.tsx`에 `/team-analysis-clips` 라우트 추가
6. 지도자/분석관 홈 메뉴 연결
7. 선수 홈 메뉴 연결
8. 모바일 홈 메뉴 연결
9. 권한별 버튼 노출 확인
10. 조회/등록/수정/삭제/영상 재생 테스트

## 11. 테스트 결과

확인 완료된 내용:

* 팀 분석 클립 페이지 접근 정상
* 팀 분석 클립 목록 조회 정상
* 팀 분석 클립 상세 조회 정상
* 팀 분석 클립 등록 정상
* 팀 분석 클립 수정 정상
* 팀 분석 클립 삭제 정상
* 원본 경기 영상 재생 정상
* 클립 구간 재생 정상
* 작성자 표시 정상
* `COACH` 삭제 버튼 노출 정상
* `ANALYST` 삭제 버튼 미노출 정상
* `PLAYER` 등록/수정/삭제 버튼 미노출 정상

## 12. 추후 확장 가능성

추후 다음 기능으로 확장할 수 있다.

* 팀 분석 클립 드로잉 프론트 연동
* 클립별 코멘트 UI 고도화
* 클립 조회 기록 저장
* 클립 썸네일 생성
* 실제 클립 파일 생성 및 인코딩
* CDN 또는 Signed URL 기반 영상 제공
* 권한 검증이 포함된 스트리밍 API
* 클립 유형별 통계
* 선수별 팀 분석 클립 확인 여부 관리

## 13. 운영 전 점검 사항

배포 전 반드시 다음 사항을 점검한다.

* `/uploads/match-videos/**` 직접 접근 허용 정책 제거 또는 제한
* 권한 검증이 포함된 영상 제공 방식 검토
* Signed URL 방식 검토
* 영상 요청 중단 예외 처리
* `ClientAbortException` 로그 정리
* `video/mp4` 응답 중 발생한 예외를 JSON `ErrorResponse`로 변환하지 않도록 처리
* 대용량 영상 재생 시 서버 부하 확인
* 모바일 영상 재생 UX 확인
