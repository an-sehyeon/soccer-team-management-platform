# 23. 선수 개인 분석 클립 드로잉 프론트 연동 요구사항 최종 업데이트

## 1. 기능 목적

선수 개인 분석 클립 화면에서 지도자와 분석관이 원본 경기 영상 구간을 선택하고, 해당 구간에 드로잉을 추가하여 선수 개인 분석 클립을 등록할 수 있게 한다.

이번 기능에서는 기존 메타데이터 기반 클립 조회 구조를 개선하여, 백엔드에서 생성된 실제 선수 개인 분석 클립 파일을 `playerClipUrl` 기준으로 재생하도록 프론트를 연결했다.

또한 드로잉은 등록 완료 후에만 확인하는 방식이 아니라, 편집 중 영상 위에 즉시 표시되도록 개선했다.

---

## 2. 사용자 역할

### 지도자 `COACH`

* 선수 개인 분석 클립 목록 조회
* 선수 개인 분석 클립 상세 조회
* 선수 개인 분석 클립 등록
* 선수 개인 분석 클립 수정
* 선수 개인 분석 클립 삭제
* 선수 개인 분석 클립 드로잉 조회
* 선수 개인 분석 클립 등록 시 드로잉 함께 저장

### 분석관 `ANALYST`

* 선수 개인 분석 클립 목록 조회
* 선수 개인 분석 클립 상세 조회
* 선수 개인 분석 클립 등록
* 선수 개인 분석 클립 수정
* 선수 개인 분석 클립 드로잉 조회
* 선수 개인 분석 클립 등록 시 드로잉 함께 저장
* 삭제 권한 없음

### 선수 `PLAYER`

* 본인 선수 개인 분석 클립 목록 조회
* 본인 선수 개인 분석 클립 상세 조회
* 본인 선수 개인 분석 클립 드로잉 조회
* 다른 선수 개인 분석 클립 조회 불가
* 등록, 수정, 삭제 불가

---

## 3. 권한 정책

권한 검증은 프론트가 아니라 반드시 백엔드에서 처리한다.

프론트에서는 사용자 역할에 따라 화면 노출만 제어한다.

* `COACH`: 등록, 조회, 수정, 삭제 가능
* `ANALYST`: 등록, 조회, 수정 가능, 삭제 불가
* `PLAYER`: 본인 클립 조회만 가능

선수 개인 분석 클립 상세 조회 시 선수는 반드시 본인에게 할당된 클립만 조회할 수 있어야 한다.

관리자성 조회 API와 선수 본인 조회 API는 분리한다.

---

## 4. 화면 흐름

### 관리 사용자 흐름

1. 선수 개인 분석 클립 화면 진입
2. 경기 영상 목록 조회
3. 선수 목록 조회
4. 선수 개인 분석 클립 목록 조회
5. 원본 경기 영상 선택
6. 대상 선수 선택
7. 클립 유형 선택
8. 제목, 코멘트 입력
9. 클립 시작 시간, 종료 시간 입력
10. 원본 경기 영상 미리보기
11. 드로잉 유형 선택
12. 드로잉 시작 시간, 종료 시간 입력
13. 드로잉 모드 켜기
14. 영상 위에 드로잉 작성
15. 작성 즉시 화면에 드로잉 표시
16. 필요 시 추가된 드로잉 제거
17. 등록 버튼 클릭
18. 백엔드에서 선수 개인 분석 클립 생성 요청 처리
19. 상태가 `PROCESSING` 또는 `READY`로 표시
20. `READY` 상태에서 생성된 `playerClipUrl` 기준으로 재생

### 선수 사용자 흐름

1. 선수 개인 분석 클립 화면 진입
2. 본인에게 할당된 선수 개인 분석 클립 목록 조회
3. 클립 선택
4. `READY` 상태 클립 재생
5. 저장된 드로잉이 해당 시간대에 영상 위에 표시

---

## 5. API 흐름

### 선수 개인 분석 클립 목록 조회

관리자성 조회:

```text
GET /api/management/player-analysis-clips
```

선수 본인 조회:

```text
GET /api/player/me/player-analysis-clips
```

### 선수 개인 분석 클립 상세 조회

관리자성 상세 조회:

```text
GET /api/management/player-analysis-clips/{playerClipId}
```

선수 본인 상세 조회:

```text
GET /api/player/me/player-analysis-clips/{playerClipId}
```

### 선수 개인 분석 클립 + 드로잉 통합 등록

```text
POST /api/management/player-analysis-clips/with-drawings
```

요청 주요 필드:

```text
matchVideoId
playerId
clipType
title
comment
startTimeSec
endTimeSec
drawings
```

드로잉 요청 필드:

```text
drawingType
startTimeSec
endTimeSec
drawingData
```

### 선수 개인 분석 클립 수정

```text
PATCH /api/management/player-analysis-clips/{playerClipId}
```

수정 시 백엔드에서 새 선수 개인 분석 클립 파일을 비동기로 생성하고, 생성 상태에 따라 `PROCESSING`, `READY`, `FAILED`를 반영한다.

### 선수 개인 분석 클립 삭제

```text
DELETE /api/coach/player-analysis-clips/{playerClipId}
```

삭제는 지도자만 가능하다.

### 선수 개인 분석 클립 드로잉 조회

```text
GET /api/player-analysis-clips/{playerClipId}/drawings
```

드로잉은 클립 기준 시간으로 표시한다.

---

## 6. DB 설계 방향

주요 테이블은 다음을 사용한다.

```text
player_video_clip
player_video_clip_drawing
game_video_upload
member
```

### player_video_clip

선수 개인 분석 클립 정보를 저장한다.

주요 필드:

```text
id
game_video_upload_id
player_id
editor_id
clip_type
title
comment
start_time_sec
end_time_sec
url
status
created_at
updated_at
```

`start_time_sec`, `end_time_sec`는 원본 경기 영상에서 클립을 생성할 구간을 의미한다.

`url`은 생성된 선수 개인 분석 클립 파일 경로를 저장한다.

`status`는 클립 파일 생성 상태를 의미한다.

```text
UPLOADING
PROCESSING
READY
FAILED
```

### player_video_clip_drawing

선수 개인 분석 클립에 표시할 드로잉 정보를 저장한다.

주요 필드:

```text
id
player_video_clip_id
drawing_type
start_time_sec
end_time_sec
drawing_data
writer_id
created_at
updated_at
```

`start_time_sec`, `end_time_sec`는 원본 경기 영상 기준이 아니라 생성된 선수 개인 분석 클립 기준 시간이다.

---

## 7. 프론트 구현 내용

### 선수 개인 분석 클립 타입 수정

`PlayerAnalysisClipDetailResponse`에 수정 폼 초기값으로 사용할 필드를 포함했다.

```text
startTimeSec
endTimeSec
```

상세 재생은 `startTimeSec`, `endTimeSec`가 아니라 `playerClipUrl` 기준으로 처리한다.

### 선수 개인 분석 클립 API 수정

`POST /api/management/player-analysis-clips/with-drawings` 호출 함수를 추가했다.

수정 API는 기존 `PATCH /api/management/player-analysis-clips/{playerClipId}`를 사용한다.

### 선수 개인 분석 클립 상세 재생

`READY` 상태일 때만 `playerClipUrl` 기준으로 영상을 재생한다.

`PROCESSING`, `UPLOADING` 상태에서는 생성 중 메시지를 표시한다.

`FAILED` 상태에서는 생성 실패 메시지를 표시한다.

### 드로잉 즉시 미리보기

기존에는 영상 위에 드로잉을 작성한 뒤 별도 추가 버튼을 눌러야 화면에 표시되었다.

최종 구현에서는 드로잉 모드에서 영상 위에 작성하는 즉시 `createDrawings`에 추가되도록 변경했다.

### 드로잉 유지 시간 설정

드로잉별로 클립 기준 시작 시간과 종료 시간을 입력할 수 있게 했다.

```text
드로잉 시작 시간(초)
드로잉 종료 시간(초)
```

입력한 시간 범위에 따라 상세 재생 중 해당 시간대에만 드로잉이 표시된다.

### 드로잉 삭제

저장 전 추가된 드로잉은 목록에서 제거할 수 있다.

저장된 드로잉의 수정/삭제는 추후 영상 편집기 UX 고도화 단계에서 별도 기능으로 구현한다.

---

## 8. 백엔드 수정 내용

### PlayerAnalysisClipDetailResponseDTO

상세 응답 DTO에 아래 필드를 포함했다.

```text
startTimeSec
endTimeSec
```

이 필드는 프론트 수정 폼 초기값과 파일 재생성 요청용으로만 사용한다.

상세 재생은 `playerClipUrl` 기준으로 처리한다.

### PlayerAnalysisClipService

상세 응답 변환은 `PlayerAnalysisClipDetailResponseDTO.from(playerVideoClip)` 구조로 유지한다.

DTO 내부에서 Entity를 기준으로 응답 필드를 변환한다.

### SecurityConfig

로컬 업로드된 선수 개인 분석 클립 파일 접근을 위해 임시로 정적 파일 경로 접근을 허용했다.

```text
/uploads/player-analysis-clips/**
```

### WebConfig

로컬 저장소의 선수 개인 분석 클립 파일을 브라우저에서 접근할 수 있도록 정적 리소스 매핑을 추가했다.

---

## 9. 예외 상황

### 원본 경기 영상을 선택하지 않은 경우

등록을 막고 안내 메시지를 표시한다.

### 대상 선수를 선택하지 않은 경우

등록을 막고 안내 메시지를 표시한다.

### 클립 제목이 비어 있는 경우

등록 또는 수정을 막고 안내 메시지를 표시한다.

### 시작 시간이 종료 시간보다 크거나 같은 경우

등록 또는 수정을 막고 안내 메시지를 표시한다.

### 원본 경기 영상 길이를 초과하는 경우

등록을 막고 안내 메시지를 표시한다.

### 드로잉이 없는 경우

통합 등록을 막고 안내 메시지를 표시한다.

### 드로잉 시작 시간이 종료 시간보다 크거나 같은 경우

드로잉 추가를 막고 안내 메시지를 표시한다.

### 드로잉 종료 시간이 클립 길이를 초과하는 경우

드로잉 추가를 막고 안내 메시지를 표시한다.

### 클립 상태가 PROCESSING인 경우

영상 재생 대신 생성 중 상태를 표시한다.

### 클립 상태가 FAILED인 경우

영상 재생 대신 생성 실패 상태를 표시한다.

---

## 10. 구현 순서

최종 구현은 다음 순서로 진행했다.

1. 선수 개인 분석 클립 상세 응답 DTO에 시작/종료 시간 필드 반영
2. 선수 개인 분석 클립 타입 수정
3. 통합 등록 API 함수 추가
4. 선수 개인 분석 클립 상세 재생을 `playerClipUrl` 기준으로 변경
5. `PROCESSING`, `READY`, `FAILED` 상태별 화면 처리
6. 선수 개인 분석 클립 수정 기능 프론트 연결
7. 선수 개인 분석 클립 드로잉 조회 연결
8. 등록 화면에서 원본 경기 영상 미리보기 연결
9. 드로잉 즉시 미리보기 구현
10. 드로잉 유지 시간 설정 복구
11. 저장 전 드로잉 제거 기능 유지
12. 로컬 정적 리소스 접근 설정 반영
13. 전체 등록, 수정, 조회, 재생, 드로잉 표시 테스트

---

## 11. 테스트 확인 결과

사용자가 직접 확인한 정상 항목은 다음과 같다.

```text
백엔드 서버 재시작 정상
프론트 서버 재시작 정상
선수 개인 분석 클립 목록 조회 정상
기존 클립 상세 조회 정상
상세 화면 수정 버튼 동작 정상
시작 시간/종료 시간 기존 값 반영 정상
제목/코멘트 수정 정상
수정 저장 정상
수정 후 PROCESSING 또는 READY 상태 반영 정상
READY 이후 새 playerClipUrl 재생 정상
통합 등록 정상
드로잉 즉시 미리보기 정상
드로잉 유지 시간 설정 정상
저장된 드로잉 상세 재생 표시 정상
```

---

## 12. 추후 확장 가능성

### 드로잉 편집 UX 개선

추후 디자인 단계에서 다음 기능을 추가한다.

```text
저장 전 드로잉 수정
저장 전 드로잉 선택 삭제
저장된 드로잉 수정
저장된 드로잉 삭제
드로잉 색상 설정
드로잉 두께 설정
텍스트 크기 설정
드로잉 타임라인 UI
드로잉 구간 드래그 조절
```

### 영상 접근 보안 개선

현재 로컬 개발 환경에서는 `/uploads/player-analysis-clips/**` 정적 접근을 허용했다.

실제 운영 단계에서는 이 방식 대신 아래 중 하나로 전환해야 한다.

```text
인증 기반 스트리밍 API
Signed URL
CDN Signed URL
권한 검증이 포함된 파일 다운로드/재생 API
```

선수 개인 분석 클립은 개인 접근 제어가 중요하므로, 운영 배포 전 반드시 정적 파일 직접 접근 구조를 제거하거나 보호해야 한다.

### 팀 분석 클립 구조 통일

다음 작업에서는 팀 분석 클립과 팀 분석 클립 드로잉도 이번 선수 개인 분석 클립 구조와 동일하게 맞춘다.

```text
팀 분석 클립 실제 mp4 파일 생성
teamClipUrl 기준 상세 재생
PROCESSING/READY/FAILED 상태 처리
팀 분석 드로잉 즉시 미리보기
팀 분석 드로잉 유지 시간 설정
READY 상태 상세 재생 시 드로잉 표시
```
