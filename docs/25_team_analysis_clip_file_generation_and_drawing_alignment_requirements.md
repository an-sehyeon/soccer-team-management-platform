# 25. 팀 분석 클립 실제 mp4 생성 및 드로잉 구조 정렬 요구사항

> [!IMPORTANT]
> 이 문서는 해당 기능의 기존 구현 당시 기준과 작업 이력을 보존하는 문서다.
> 경기 영상 편집기 v1의 신규 구현은 `docs/31_video_editor_v1_requirements.md`,
> `docs/00_current_backend_policy.md`, `docs/00_current_frontend_policy.md`를 우선한다.
> 역할 권한, 클립 상태, 시간 단위, 삭제, 드로잉 합성, 영상 제공 방식이 충돌하면 최신 문서를 적용한다.
> 기존 API는 프론트 전환 전 호환 목적으로 유지될 수 있으므로 실제 제거 여부는 현재 소스를 확인한다.


## 1. 기능 목적

팀 분석 클립을 기존 원본 경기 영상 URL + 시작/종료 시간 메타데이터 재생 구조에서 실제 mp4 클립 파일 생성 구조로 전환한다.

이번 기능의 목적은 다음과 같다.

- 팀 분석 클립을 선수 개인 분석 클립과 동일한 실제 mp4 파일 기반 구조로 맞춘다.
- 지도자/분석관이 원본 영상을 보면서 팀 분석 클립 구간을 지정할 수 있게 한다.
- 팀 분석 클립과 드로잉을 한 화면에서 생성/수정할 수 있게 한다.
- 선수는 READY 상태의 팀 분석 클립만 조회하고 재생한다.
- 드로잉은 생성된 팀 분석 클립 영상 기준 시간으로 표시한다.
- 추후 선수 개인 분석 클립도 동일한 목록/상세 + 등록/수정 편집기 구조로 맞출 수 있게 한다.

---

## 2. 사용자 역할

### COACH

- 팀 분석 클립 등록 가능
- 팀 분석 클립 조회 가능
- 팀 분석 클립 수정 가능
- 팀 분석 클립 삭제 가능
- 팀 분석 클립 드로잉 등록/조회/수정/삭제 가능
- 팀 분석 클립 파일 재생성 가능

### ANALYST

- 팀 분석 클립 등록 가능
- 팀 분석 클립 조회 가능
- 팀 분석 클립 수정 가능
- 팀 분석 클립 삭제 불가
- 팀 분석 클립 드로잉 등록/조회/수정 가능
- 팀 분석 클립 드로잉 삭제 불가
- 팀 분석 클립 파일 재생성 가능

### PLAYER

- READY 상태의 팀 분석 클립 조회 가능
- READY 상태의 팀 분석 클립 드로잉 조회 가능
- 등록/수정/삭제 불가

---

## 3. 권한 정책

권한 검증은 프론트가 아니라 백엔드에서 처리한다.

기본 권한은 다음과 같다.

```text
COACH:
- 팀 분석 클립 등록/조회/수정/삭제
- 팀 분석 클립 드로잉 등록/조회/수정/삭제

ANALYST:
- 팀 분석 클립 등록/조회/수정
- 팀 분석 클립 드로잉 등록/조회/수정
- 삭제 불가

PLAYER:
- READY 상태 팀 분석 클립 조회
- READY 상태 팀 분석 클립 드로잉 조회
```

프론트에서는 사용자 경험을 위해 버튼을 숨긴다.

```text
COACH:
- 등록 버튼 표시
- 수정 버튼 표시
- 삭제 버튼 표시

ANALYST:
- 등록 버튼 표시
- 수정 버튼 표시
- 삭제 버튼 숨김

PLAYER:
- 등록/수정/삭제 버튼 숨김
```

---

## 4. 화면 흐름

### 4-1. 팀 분석 클립 목록 페이지

경로는 다음과 같다.

```text
/team-analysis-clips
```

역할은 다음과 같다.

- READY 상태 팀 분석 클립 목록 조회
- 경기 영상 조건 조회
- 클립 유형 조건 조회
- 클립 상세 조회
- 생성된 팀 분석 클립 영상 재생
- 드로잉 오버레이 표시
- 등록 페이지 이동
- 수정 페이지 이동
- COACH 삭제 처리

목록 페이지에서는 PROCESSING, FAILED 클립을 기본 조회 대상으로 노출하지 않는다.

실패 클립 관리는 추후 별도 관리자용 생성 실패 관리 화면에서 검토한다.

---

### 4-2. 팀 분석 클립 등록 페이지

경로는 다음과 같다.

```text
/team-analysis-clips/new
```

등록 페이지 초기 진입 시에는 원본 경기 영상 선택 영역만 표시한다.

원본 영상을 선택하기 전에는 아래 영역을 숨긴다.

- 클립 정보
- 드로잉 작성
- 저장 전 드로잉 목록
- 최종 저장

원본 경기 영상을 선택하면 다음 영역을 표시한다.

- 원본 영상 재생 영역
- 클립 시작/종료 시간 설정 버튼
- 클립 시간 초기화 버튼
- 선택 구간 재생 버튼
- 드로잉 시작/종료 시간 설정 버튼
- 드로잉 시간 초기화 버튼
- 클립 정보 입력
- 드로잉 작성
- 저장 전 드로잉 목록
- 최종 저장

클립 시작/종료 시간은 직접 입력하지 않고 영상 현재 시간을 기준으로 버튼으로 설정한다.

---

### 4-3. 팀 분석 클립 수정 페이지

경로는 다음과 같다.

```text
/team-analysis-clips/{teamClipId}/edit
```

수정 페이지는 기존 클립 정보를 유지한 상태로 전체 편집 영역을 바로 표시한다.

수정 가능한 정보는 다음과 같다.

- 클립 유형
- 제목
- 코멘트
- 클립 시작/종료 시간
- 드로잉 목록

수정 시 정책은 다음과 같다.

- 제목/코멘트/드로잉만 변경하면 기존 mp4 파일을 유지한다.
- 시작/종료 시간이 변경되면 mp4 파일을 재생성한다.
- 기존 드로잉은 전체 교체 방식으로 처리한다.
- 수정 요청 시 기존 드로잉을 소프트 삭제하고 요청 드로잉을 새로 저장한다.
- 파일 재생성이 필요한 경우 상태를 PROCESSING으로 변경하고 비동기 생성 후 READY 또는 FAILED로 변경한다.

---

## 5. API 흐름

### 5-1. 팀 분석 클립 목록 조회

```http
GET /api/team-analysis-clips?page=0&size=20
GET /api/team-analysis-clips?matchVideoId=1&page=0&size=20
GET /api/team-analysis-clips?clipType=ATTACK&page=0&size=20
GET /api/team-analysis-clips?matchVideoId=1&clipType=ATTACK&page=0&size=20
```

정책은 다음과 같다.

- READY 상태 클립만 조회한다.
- `matchVideoId`는 선택 조건이다.
- `clipType`은 선택 조건이다.
- `clipType`은 Controller에서 Enum으로 바로 받지 않고 String으로 받은 뒤 Validator에서 변환한다.
- `clipType`이 빈 값이면 전체 유형 조회로 처리한다.
- 잘못된 `clipType`이면 400 에러를 반환한다.

---

### 5-2. 팀 분석 클립 상세 조회

```http
GET /api/team-analysis-clips/{teamClipId}
```

응답에는 다음 필드가 포함되어야 한다.

```text
teamClipId
matchVideoId
matchVideoTitle
clipType
title
comment
startTimeSec
endTimeSec
teamClipUrl
status
editorId
editorName
createdAt
updatedAt
```

프론트 상세 재생은 `teamClipUrl` 기준으로 처리한다.

---

### 5-3. 팀 분석 클립 드로잉 조회

```http
GET /api/team-analysis-clips/{teamClipId}/drawings
```

드로잉 시간 기준은 생성된 팀 분석 클립 영상 기준 초다.

---

### 5-4. 팀 분석 클립 + 드로잉 통합 생성

```http
POST /api/management/team-analysis-clips/with-drawings
```

요청 필드는 다음과 같다.

```text
matchVideoId
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

- `COACH`, `ANALYST`만 호출 가능하다.
- 클립의 `startTimeSec`, `endTimeSec`는 원본 경기 영상 기준 초다.
- 드로잉의 `startTimeSec`, `endTimeSec`는 생성될 팀 분석 클립 기준 초다.
- 생성 요청 시 `team_video_clip.status = PROCESSING`으로 저장한다.
- 드로잉은 함께 저장한다.
- 트랜잭션 커밋 후 FFmpeg 비동기 작업을 실행한다.
- 성공 시 `team_video_clip.url`에 생성된 mp4 URL을 저장하고 `READY`로 변경한다.
- 실패 시 `FAILED`로 변경한다.

---

### 5-5. 팀 분석 클립 + 드로잉 통합 수정

```http
PUT /api/management/team-analysis-clips/{teamClipId}/with-drawings
```

요청 필드는 다음과 같다.

```text
clipType
title
comment
startTimeSec
endTimeSec
drawings[]
```

정책은 다음과 같다.

- `COACH`, `ANALYST`만 호출 가능하다.
- `PLAYER`는 호출 불가다.
- 드로잉은 전체 교체 방식으로 처리한다.
- 기존 드로잉은 소프트 삭제한다.
- 요청 드로잉을 새로 저장한다.
- 클립 시작/종료 시간이 변경된 경우 mp4 파일을 재생성한다.
- 제목/코멘트/드로잉만 변경된 경우 기존 mp4 파일을 유지한다.
- PROCESSING 상태에서는 수정할 수 없다.
- FAILED 상태는 수정 저장을 통해 재생성을 요청할 수 있다.

---

## 6. DB 설계 방향

### 6-1. team_video_clip

사용 필드는 다음과 같다.

```text
id
upload_id
member_id
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

- `start_time_sec`, `end_time_sec`는 원본 경기 영상 기준 초다.
- `url`은 생성된 팀 분석 클립 mp4 접근 URL이다.
- `status`는 PROCESSING, READY, FAILED를 사용한다.
- 삭제는 소프트 삭제다.

---

### 6-2. team_video_clip_drawing

사용 필드는 다음과 같다.

```text
id
team_video_clip_id
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

- 드로잉 시간은 생성된 팀 분석 클립 영상 기준 초다.
- `drawing_data`는 프론트 캔버스에서 생성한 JSON 데이터다.
- 수정 시 기존 드로잉은 전체 소프트 삭제 후 새로 저장한다.

---

## 7. 파일 생성 정책

팀 분석 클립 파일 생성은 FFmpeg 기반 비동기 방식으로 처리한다.

흐름은 다음과 같다.

1. 통합 생성 또는 재생성 요청을 받는다.
2. DB에 팀 분석 클립과 드로잉을 저장한다.
3. 트랜잭션 커밋 후 비동기 파일 생성 작업을 실행한다.
4. FFmpeg로 원본 경기 영상의 지정 구간을 mp4로 생성한다.
5. 성공 시 `url`과 `READY` 상태를 저장한다.
6. 실패 시 `FAILED` 상태를 저장한다.

드로잉은 mp4 파일에 합성하지 않는다.

현재 구조는 다음과 같다.

```text
mp4 파일 = 원본 영상 구간만 잘라낸 영상
드로잉 = DB JSON 저장 후 프론트에서 오버레이 표시
```

드로잉 합성 다운로드는 추후 별도 기능으로 검토한다.

---

## 8. CORS 정책

팀 분석 클립 통합 수정 API는 PUT 요청을 사용한다.

프론트 개발 서버에서 PUT 요청 시 preflight OPTIONS 요청이 발생하므로 백엔드 CORS 설정에서 다음을 허용한다.

```text
GET
POST
PUT
PATCH
DELETE
OPTIONS
```

SecurityConfig에서는 다음 정책을 적용한다.

```text
OPTIONS /** permitAll
실제 PUT 요청은 JWT 인증 필요
```

`Authorization`, `Content-Type` 요청 헤더를 허용한다.

---

## 9. 예외 상황

### 9-1. 권한 오류

- PLAYER가 관리 API 호출 시 403 처리
- ANALYST가 삭제 API 호출 시 403 처리

### 9-2. 클립 시간 오류

- 시작 시간이 종료 시간보다 크거나 같으면 오류
- 시작/종료 시간이 0보다 작으면 오류
- 종료 시간이 원본 경기 영상 길이를 초과하면 오류

### 9-3. 드로잉 시간 오류

- 드로잉 시작 시간이 종료 시간보다 크거나 같으면 오류
- 드로잉 시간이 생성될 팀 분석 클립 길이를 초과하면 오류
- 드로잉 데이터가 비어 있으면 오류

### 9-4. 파일 생성 오류

- 원본 영상 파일이 없으면 FAILED 처리
- FFmpeg 실행 실패 시 FAILED 처리
- 저장 디렉터리 생성 실패 시 예외 처리

### 9-5. 목록 조회 오류

- DB에 잘못된 `clip_type` 값이 있으면 Enum 변환 오류가 발생할 수 있으므로 DB 데이터를 정리한다.
- Controller는 `clipType`을 String으로 받고 Validator에서 안전하게 Enum으로 변환한다.

---

## 10. 구현 순서

1. 요구사항 문서 작성
2. Entity 상태 필드 및 URL 정책 확인
3. 통합 생성/수정 Request DTO 작성
4. 통합 생성/수정 Response DTO 작성
5. FFmpeg 파일 생성 서비스 작성
6. 파일 생성 상태 갱신 서비스 작성
7. 비동기 생성 서비스 작성
8. Validator 권한/시간 검증 보완
9. 팀 분석 클립 통합 생성 API 구현
10. 팀 분석 클립 통합 수정 API 구현
11. 목록 조회 API를 READY 상태 기준으로 보완
12. 상세 응답 DTO에 `startTimeSec`, `endTimeSec`, `teamClipUrl` 반영
13. CORS PUT/OPTIONS 허용
14. 프론트 라우트 추가
15. 팀 분석 클립 목록 페이지 정리
16. 팀 분석 클립 등록/수정 편집기 페이지 추가
17. 대시보드/모바일/선수 홈 진입 경로 정리
18. 기능 테스트

---

## 11. 최종 구현 결과

이번 기능에서 최종 반영된 내용은 다음과 같다.

- 팀 분석 클립 실제 mp4 파일 비동기 생성 구조 구현
- 팀 분석 클립 + 드로잉 통합 생성 API 추가
- 팀 분석 클립 + 드로잉 통합 수정 API 추가
- 팀 분석 클립 목록 조회를 READY 상태 기준으로 보완
- `clipType` 요청 파라미터를 String으로 받은 뒤 Validator에서 Enum 변환
- 잘못된 `clipType` 요청은 400 에러 처리
- 팀 분석 클립 상세 DTO에 `startTimeSec`, `endTimeSec`, `teamClipUrl` 반영
- 드로잉 시간 기준을 생성된 팀 분석 클립 영상 기준 초로 정리
- 등록/수정 전용 편집기 페이지 추가
- 등록 페이지에서는 원본 영상 선택 전 편집 영역 숨김
- 수정 페이지에서는 기존 데이터 기준으로 전체 편집 영역 표시
- 클립 시작/종료 시간 버튼 설정
- 드로잉 시작/종료 시간 버튼 설정
- 클립 시간 초기화 버튼 추가
- 드로잉 시간 초기화 버튼 추가
- NaN 표시 방지
- 목록 페이지에서 READY 클립 재생 및 드로잉 오버레이 표시
- COACH/ANALYST/PLAYER 권한별 버튼 노출 정리
- PUT 요청 CORS preflight 오류 수정

---

## 12. 추후 확장 가능성

추후 확장 가능성은 다음과 같다.

- 드로잉이 합성된 mp4 다운로드 기능
- 팀 분석 클립 생성 실패 관리 화면
- 생성 실패 클립 재생성 요청 전용 API
- 썸네일 생성
- CDN 연동
- Signed URL 기반 영상 접근 제어
- 클립 조회 기록
- 팀 분석 클립 코멘트 스레드
- 선수별 팀 클립 추천
- AI 이벤트 인식 후 자동 팀 클립 생성
- 선수 개인 분석 클립도 동일한 편집기 구조로 정리
