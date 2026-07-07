# 26. 선수 개인 분석 클립 화면 구조 정렬 요구사항

## 1. 기능 목적

선수 개인 분석 클립 화면 구조를 팀 분석 클립 최신 프론트 구조와 동일하게 정리한다.

이번 기능의 목적은 다음과 같다.

* `/player-analysis-clips`를 목록/상세/삭제/등록·수정 이동 전용 페이지로 정리한다.
* `/player-analysis-clips/new`를 등록 전용 편집기 페이지로 분리한다.
* `/player-analysis-clips/:playerClipId/edit`를 수정 전용 편집기 페이지로 분리한다.
* 상세 재생 기준을 원본 경기 영상 URL + 시작/종료 시간이 아니라 생성된 `playerClipUrl` 기준으로 정리한다.
* 등록 화면에서는 원본 경기 영상과 대상 선수를 선택하기 전까지 편집 영역을 숨긴다.
* 수정 화면에서는 기존 클립 정보와 드로잉을 유지한 상태로 전체 편집 영역을 바로 표시한다.
* 클립 시작/종료 시간은 원본 경기 영상 기준 초로 관리한다.
* 드로잉 시작/종료 시간은 생성된 선수 개인 분석 클립 영상 기준 초로 관리한다.
* 클립/드로잉 시간은 직접 입력하지 않고 영상 현재 시간 기준 버튼으로 설정한다.
* 클립 시간 초기화 버튼과 드로잉 시간 초기화 버튼을 추가한다.
* 숫자 값이 비어 있거나 잘못된 경우 `NaN`이 화면에 표시되지 않도록 안전 처리한다.

---

## 2. 사용자 역할

### COACH

* 선수 개인 분석 클립 등록 가능
* 선수 개인 분석 클립 조회 가능
* 선수 개인 분석 클립 수정 가능
* 선수 개인 분석 클립 삭제 가능
* 선수 개인 분석 클립 드로잉 등록/조회/수정 가능
* 대상 선수 선택 가능
* 원본 경기 영상 선택 가능

### ANALYST

* 선수 개인 분석 클립 등록 가능
* 선수 개인 분석 클립 조회 가능
* 선수 개인 분석 클립 수정 가능
* 선수 개인 분석 클립 삭제 불가
* 선수 개인 분석 클립 드로잉 등록/조회/수정 가능
* 대상 선수 선택 가능
* 원본 경기 영상 선택 가능

### PLAYER

* 본인 선수 개인 분석 클립 목록 조회 가능
* 본인 선수 개인 분석 클립 상세 조회 가능
* 본인 선수 개인 분석 클립 드로잉 조회 가능
* 다른 선수 개인 분석 클립 조회 불가
* 등록/수정/삭제 불가

---

## 3. 권한 정책

권한 검증은 프론트가 아니라 반드시 백엔드에서 처리한다.

프론트에서는 사용자 경험을 위해 버튼과 화면 진입만 제어한다.

```text
COACH
- 등록 버튼 표시
- 수정 버튼 표시
- 삭제 버튼 표시
- 등록/수정 편집기 접근 가능

ANALYST
- 등록 버튼 표시
- 수정 버튼 표시
- 삭제 버튼 숨김
- 등록/수정 편집기 접근 가능

PLAYER
- 등록/수정/삭제 버튼 숨김
- 본인 클립 조회만 가능
- 등록/수정 편집기 접근 불가
```

선수 본인 조회는 반드시 로그인한 `memberId` 기준으로 백엔드에서 제한한다.

프론트에서 버튼을 숨겨도 API 직접 호출이 가능하므로, 프론트 권한 분기를 보안으로 간주하지 않는다.

---

## 4. 화면 흐름

## 4-1. 선수 개인 분석 클립 목록/상세 페이지

경로는 다음과 같다.

```text
/player-analysis-clips
```

역할은 다음과 같다.

* 선수 개인 분석 클립 목록 조회
* 관리자는 전체 또는 조건 기반 목록 조회
* 선수는 본인 클립 목록 조회
* 선수 개인 분석 클립 상세 조회
* 생성된 `playerClipUrl` 기준 영상 재생
* 드로잉 오버레이 표시
* 등록 페이지 이동
* 수정 페이지 이동
* COACH 삭제 처리

목록/상세 페이지에서는 등록/수정용 편집 폼을 직접 포함하지 않는다.

`COACH`, `ANALYST`는 등록/수정 버튼을 볼 수 있다.

`COACH`만 삭제 버튼을 볼 수 있다.

`PLAYER`는 조회 전용 화면만 볼 수 있다.

---

## 4-2. 선수 개인 분석 클립 등록 편집기 페이지

경로는 다음과 같다.

```text
/player-analysis-clips/new
```

등록 페이지 초기 진입 시에는 다음 영역만 표시한다.

* 원본 경기 영상 선택
* 대상 선수 선택

원본 경기 영상과 대상 선수를 모두 선택하기 전에는 아래 영역을 숨긴다.

* 원본 영상 재생 영역
* 클립 정보 입력
* 클립 시작/종료 시간 설정
* 클립 시간 초기화
* 선택 구간 재생
* 드로잉 작성
* 드로잉 시작/종료 시간 설정
* 드로잉 시간 초기화
* 저장 전 드로잉 목록
* 최종 저장

원본 경기 영상과 대상 선수를 모두 선택하면 전체 편집 영역을 표시한다.

클립 시작/종료 시간은 원본 경기 영상의 현재 재생 시간을 기준으로 버튼으로 설정한다.

드로잉 시작/종료 시간은 생성될 선수 개인 분석 클립 내부 시간 기준으로 버튼으로 설정한다.

등록 요청은 선수 개인 분석 클립과 드로잉을 함께 저장하는 통합 생성 API를 사용한다.

```http
POST /api/management/player-analysis-clips/with-drawings
```

---

## 4-3. 선수 개인 분석 클립 수정 편집기 페이지

경로는 다음과 같다.

```text
/player-analysis-clips/:playerClipId/edit
```

수정 페이지는 기존 클립 정보를 조회한 뒤 전체 편집 영역을 바로 표시한다.

수정 페이지에서 유지해야 하는 정보는 다음과 같다.

* 기존 원본 경기 영상
* 기존 대상 선수
* 기존 클립 유형
* 기존 제목
* 기존 코멘트
* 기존 클립 시작/종료 시간
* 기존 드로잉 목록

수정 가능한 정보는 다음과 같다.

* 대상 선수
* 클립 유형
* 제목
* 코멘트
* 클립 시작/종료 시간
* 드로잉 목록

수정 요청은 선수 개인 분석 클립과 드로잉을 함께 수정하는 통합 수정 API를 사용한다.

```http
PUT /api/management/player-analysis-clips/{playerClipId}/with-drawings
```

수정 정책은 다음과 같다.

* `COACH`, `ANALYST`만 호출 가능하다.
* `PLAYER`는 호출 불가다.
* 드로잉은 전체 교체 방식으로 처리한다.
* 기존 드로잉은 소프트 삭제한다.
* 요청 드로잉을 새로 저장한다.
* 제목/코멘트/드로잉만 변경하면 기존 mp4 파일을 유지한다.
* 클립 시작/종료 시간이 변경되면 백엔드에서 mp4 파일 재생성을 요청한다.
* `FAILED` 상태는 수정 저장을 통해 재생성을 요청할 수 있다.
* `PROCESSING` 상태에서는 수정할 수 없도록 백엔드 정책을 따른다.

---

## 5. API 흐름

## 5-1. 목록 조회

관리용 목록 조회 API는 다음을 사용한다.

```http
GET /api/management/player-analysis-clips
```

선수 본인 목록 조회 API는 다음을 사용한다.

```http
GET /api/player/me/player-analysis-clips
```

정책은 다음과 같다.

* `COACH`, `ANALYST`는 관리용 목록 API를 사용한다.
* `PLAYER`는 본인 목록 API를 사용한다.
* `PLAYER`는 `playerId`를 직접 전달하지 않는다.
* 목록 응답은 `playerClips` 배열 기준으로 처리한다.
* 상태 응답이 포함되어 있으면 `PROCESSING`, `READY`, `FAILED` 표시 정책을 적용한다.

---

## 5-2. 상세 조회

관리용 상세 조회 API는 다음을 사용한다.

```http
GET /api/management/player-analysis-clips/{playerClipId}
```

선수 본인 상세 조회 API는 다음을 사용한다.

```http
GET /api/player/me/player-analysis-clips/{playerClipId}
```

상세 재생은 `playerClipUrl` 기준으로 처리한다.

기존 원본 경기 영상 URL + `startTimeSec`, `endTimeSec` 구간 재생 방식은 상세 조회 재생에서 사용하지 않는다.

수정 페이지에서 원본 영상 기준 클립 구간을 다시 편집해야 하므로 상세 응답의 `matchVideoId`, `startTimeSec`, `endTimeSec`, `playerId` 값을 사용한다.

---

## 5-3. 드로잉 조회

선수 개인 분석 클립 드로잉 조회 API는 기존 구현을 사용한다.

```http
GET /api/player-analysis-clips/{playerClipId}/drawings
```

정책은 다음과 같다.

* 드로잉 시간 기준은 생성된 선수 개인 분석 클립 영상 기준 초다.
* `READY` 상태에서만 영상 재생과 드로잉 오버레이를 활성화한다.
* `PROCESSING`, `FAILED` 상태에서는 영상 재생과 드로잉 표시를 막는다.

---

## 5-4. 통합 생성

통합 생성 API는 다음을 사용한다.

```http
POST /api/management/player-analysis-clips/with-drawings
```

기본 요청 필드는 다음과 같다.

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

`drawings[]` 항목은 다음과 같다.

```text
drawingType
startTimeSec
endTimeSec
drawingData
```

정책은 다음과 같다.

* `COACH`, `ANALYST`만 호출 가능하다.
* 클립의 `startTimeSec`, `endTimeSec`는 원본 경기 영상 기준 초다.
* 드로잉의 `startTimeSec`, `endTimeSec`는 생성될 선수 개인 분석 클립 기준 초다.
* 생성 요청 시 백엔드는 `PROCESSING` 상태로 저장하고 FFmpeg 비동기 작업을 실행한다.
* 성공 시 `player_video_clip.url`에 생성된 mp4 URL을 저장하고 `READY`로 변경한다.
* 실패 시 `FAILED`로 변경한다.

---

## 5-5. 통합 수정

이번 기능에서 선수 개인 분석 클립 통합 수정 API를 추가했다.

```http
PUT /api/management/player-analysis-clips/{playerClipId}/with-drawings
```

기본 요청 필드는 다음과 같다.

```text
playerId
clipType
title
comment
startTimeSec
endTimeSec
drawings[]
```

정책은 다음과 같다.

* `COACH`, `ANALYST`만 호출 가능하다.
* `PLAYER`는 호출 불가다.
* `ANALYST`는 삭제 불가지만 수정은 가능하다.
* 드로잉은 전체 교체 방식으로 처리한다.
* 기존 드로잉은 소프트 삭제한다.
* 요청 드로잉을 새로 저장한다.
* 클립 시작/종료 시간이 변경된 경우 mp4 파일을 재생성한다.
* 기존 mp4 URL이 없거나 상태가 `FAILED`인 경우 mp4 파일을 재생성한다.
* 제목/코멘트/드로잉만 변경된 경우 기존 mp4 파일을 유지한다.

---

## 5-6. 삭제

삭제 API는 기존 정책을 따른다.

```http
DELETE /api/coach/player-analysis-clips/{playerClipId}
```

정책은 다음과 같다.

* `COACH`만 삭제 가능하다.
* 삭제는 소프트 삭제다.
* 원본 경기 영상은 삭제하지 않는다.
* 생성된 mp4 파일의 실제 삭제 여부는 기존 백엔드 정책을 따른다.
* `ANALYST`, `PLAYER`는 삭제할 수 없다.

---

## 6. DB 설계 방향

이번 작업에서는 신규 DB 테이블을 추가하지 않는다.

사용 테이블은 다음과 같다.

```text
game_video_upload
player_video_clip
player_video_clip_drawing
player_video_clip_view
member
```

## 6-1. player_video_clip

주요 필드는 다음과 같다.

```text
id
upload_id
editor_id
player_id
clip_type
title
comment
start_time_sec
end_time_sec
url
status
is_deleted
created_at
updated_at
```

정책은 다음과 같다.

* `start_time_sec`, `end_time_sec`는 원본 경기 영상 기준 초다.
* `url`은 생성된 선수 개인 분석 클립 mp4 접근 URL이다.
* 상세 재생은 프론트에서 `playerClipUrl` 기준으로 처리한다.
* 삭제는 소프트 삭제다.

## 6-2. player_video_clip_drawing

주요 필드는 다음과 같다.

```text
id
player_video_clip_id
member_id
drawing_type
start_time_sec
end_time_sec
drawing_data
is_deleted
created_at
updated_at
```

정책은 다음과 같다.

* 드로잉 시간은 생성된 선수 개인 분석 클립 영상 기준 초다.
* `drawing_data`는 프론트 캔버스에서 생성한 JSON 데이터다.
* 수정 시 기존 드로잉은 전체 소프트 삭제 후 새로 저장한다.

---

## 7. 프론트 라우트 설계

이번 기능에서 선수 개인 분석 클립 라우트를 다음처럼 정리한다.

```text
/player-analysis-clips
/player-analysis-clips/new
/player-analysis-clips/:playerClipId/edit
```

`ROUTES` 상수에는 다음 경로를 추가한다.

```text
PLAYER_ANALYSIS_CLIP
PLAYER_ANALYSIS_CLIP_CREATE
PLAYER_ANALYSIS_CLIP_EDIT
```

선수 개인 분석 클립 수정 페이지 이동을 위해 다음 유틸 함수를 추가한다.

```text
createPlayerAnalysisClipEditRoute(playerClipId)
```

---

## 8. 프론트 파일 설계 방향

이번 기능에서 생성/수정한 프론트 파일은 다음과 같다.

```text
frontend/src/types/playerAnalysisClip.ts
frontend/src/api/playerAnalysisClipApi.ts
frontend/src/constants/routes.ts
frontend/src/App.tsx
frontend/src/pages/PlayerAnalysisClipPage.tsx
frontend/src/pages/PlayerAnalysisClipEditorPage.tsx
```

핵심 변경은 다음과 같다.

* `PlayerAnalysisClipPage.tsx`는 목록/상세/삭제/편집기 이동 전용으로 정리한다.
* `PlayerAnalysisClipEditorPage.tsx`는 등록/수정 전용 편집기 페이지로 신규 생성한다.
* 상세 재생은 `playerClipUrl` 기준으로 처리한다.
* 등록 페이지에서는 원본 경기 영상과 대상 선수 선택 전 편집 영역을 숨긴다.
* 수정 페이지에서는 기존 클립 정보와 드로잉을 불러와 전체 편집 영역을 바로 표시한다.
* 클립/드로잉 시간은 직접 입력하지 않고 영상 현재 시간 기준 버튼으로 설정한다.
* `NaN`이 화면에 표시되지 않도록 안전한 숫자 처리 함수를 적용한다.

---

## 9. 백엔드 파일 설계 방향

이번 기능에서 생성/수정한 백엔드 파일은 다음과 같다.

```text
backend/src/main/java/com/soccer/platform/dto/playerclip/UpdatePlayerAnalysisClipWithDrawingsRequestDTO.java
backend/src/main/java/com/soccer/platform/dto/playerclip/UpdatePlayerAnalysisClipWithDrawingsResponseDTO.java
backend/src/main/java/com/soccer/platform/controller/PlayerAnalysisClipController.java
backend/src/main/java/com/soccer/platform/service/playerclip/PlayerAnalysisClipService.java
backend/src/main/java/com/soccer/platform/service/playerclip/PlayerAnalysisClipValidator.java
```

핵심 변경은 다음과 같다.

* 선수 개인 분석 클립 통합 수정 API를 추가한다.
* 기존 드로잉은 전체 소프트 삭제하고 요청 드로잉을 새로 저장한다.
* 시작/종료 시간이 변경된 경우 mp4 파일을 재생성한다.
* 기존 URL이 없거나 상태가 `FAILED`인 경우 mp4 파일을 재생성한다.
* 제목/코멘트/드로잉만 변경된 경우 기존 mp4 파일을 유지한다.

---

## 10. 예외 상황

### 10-1. 권한 오류

* `PLAYER`가 등록/수정/삭제 화면에 직접 접근하면 접근 제한 처리한다.
* `PLAYER`가 다른 선수 클립을 조회하려고 하면 백엔드에서 제한한다.
* `ANALYST`가 삭제 요청을 보내면 백엔드에서 제한한다.

### 10-2. 원본 경기 영상 미선택

* 등록 페이지에서 원본 경기 영상 미선택 시 편집 영역을 숨긴다.
* 저장 버튼을 누를 수 없게 처리한다.

### 10-3. 대상 선수 미선택

* 등록 페이지에서 대상 선수 미선택 시 편집 영역을 숨긴다.
* 저장 버튼을 누를 수 없게 처리한다.

### 10-4. 클립 시간 오류

* 클립 시작 시간이 비어 있으면 저장 불가
* 클립 종료 시간이 비어 있으면 저장 불가
* 시작 시간이 종료 시간보다 크거나 같으면 저장 불가
* 종료 시간이 원본 경기 영상 길이를 초과하면 저장 불가
* 화면에는 `NaN`을 표시하지 않는다.

### 10-5. 드로잉 시간 오류

* 드로잉 시작 시간이 비어 있으면 드로잉 추가 불가
* 드로잉 종료 시간이 비어 있으면 드로잉 추가 불가
* 드로잉 시작 시간이 종료 시간보다 크거나 같으면 드로잉 추가 불가
* 드로잉 종료 시간이 생성될 클립 길이를 초과하면 드로잉 추가 불가
* 화면에는 `NaN`을 표시하지 않는다.

### 10-6. 상태 오류

* `PROCESSING` 상태는 생성 중 메시지를 표시하고 영상 재생과 드로잉 표시를 막는다.
* `FAILED` 상태는 생성 실패 메시지를 표시하고 영상 재생과 드로잉 표시를 막는다.
* `READY` 상태에서만 `playerClipUrl` 기준으로 영상 재생과 드로잉 오버레이를 활성화한다.

---

## 11. 테스트 결과

사용자가 다음 테스트를 정상 확인했다.

* 백엔드 서버 정상 실행
* 선수 개인 분석 클립 목록 조회 정상
* 선수 개인 분석 클립 상세 조회 정상
* 상세 화면에서 `playerClipUrl` 기준 재생 정상
* 선수 개인 분석 클립 등록 페이지 이동 정상
* 등록 페이지에서 원본 경기 영상과 대상 선수 선택 전 편집 영역 숨김 정상
* 원본 경기 영상과 대상 선수 선택 후 편집 영역 표시 정상
* 클립 시작/종료 시간 버튼 설정 정상
* 클립 시간 초기화 정상
* 드로잉 시작/종료 시간 버튼 설정 정상
* 드로잉 시간 초기화 정상
* 드로잉 작성 및 저장 전 목록 추가 정상
* 선수 개인 분석 클립 + 드로잉 통합 등록 정상
* 선수 개인 분석 클립 수정 페이지 이동 정상
* 기존 클립 정보와 드로잉 유지 정상
* 선수 개인 분석 클립 + 드로잉 통합 수정 정상
* 목록/상세 화면 복귀 정상
* 삭제 권한 UI 정상
* `NaN` 미표시 정상

---

## 12. 구현 완료 기준

이번 기능은 다음 기준으로 완료되었다.

* 요구사항 문서 작성 완료
* 선수 개인 분석 클립 통합 수정 API 추가 완료
* 선수 개인 분석 클립 목록/상세 페이지 정리 완료
* 선수 개인 분석 클립 등록/수정 편집기 페이지 분리 완료
* 상세 재생을 `playerClipUrl` 기준으로 변경 완료
* 클립/드로잉 시간 기준 정리 완료
* 클립/드로잉 시간 설정 버튼 및 초기화 버튼 추가 완료
* 정상 테스트 확인 완료

---

## 13. 추후 확장 가능성

추후 확장 가능한 기능은 다음과 같다.

* `PROCESSING`, `FAILED` 클립 관리 전용 화면
* 실패 클립 재생성 버튼
* mp4 파일 생성 실패 사유 저장
* 썸네일 생성
* 드로잉 합성 다운로드
* 선수별 조회 기록 시각화
* 선수 피드백 확인 여부 표시
* Signed URL 또는 권한 검증 스트리밍 API 전환
* CDN 연동
* AI 이벤트 자동 클립 생성
