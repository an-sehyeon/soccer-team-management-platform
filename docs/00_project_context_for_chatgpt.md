# 00. ChatGPT 프로젝트 컨텍스트

## 1. 문서 목적

이 문서는 ChatGPT 새 채팅 또는 기능별 작업 시작 시 업로드할 핵심 요약 문서다.

기능별 상세 요구사항 문서를 모두 ChatGPT 프로젝트 소스에 업로드하지 못하는 상황을 대비해 프로젝트의 최신 기준과 작업 규칙을 한 곳에 요약한다.

이 문서는 상세 요구사항 문서를 대체하지 않는다.

상세 요구사항 문서는 GitHub 프로젝트의 `docs/` 디렉터리에 유지한다.

```text
https://github.com/an-sehyeon/soccer-team-management-platform/tree/main/docs
```

---

## 2. ChatGPT 프로젝트 기본 업로드 파일

기본 업로드 파일은 다음 5개다.

```text
00_project_context_for_chatgpt.md
00_current_backend_policy.md
00_current_frontend_policy.md
soccer_platform.sql
README.md
```

기능별 작업을 시작할 때는 기본 5개 파일에 더해 현재 작업에 꼭 필요한 상세 요구사항 문서와 실제 코드 파일만 추가한다.

현재 로컬 파일과 GitHub `main`이 동일하다고 사용자가 명확히 말한 경우 GitHub `main`을 기준으로 실제 코드 구조를 확인할 수 있다.

그 외에는 DTO 필드명, API 주소, Entity 컬럼명, Repository 메서드명을 추측하지 않는다.

---

## 3. 프로젝트 목적

이 프로젝트는 포트폴리오 예제가 아니라 실제 축구팀에 판매하고 운영할 목적의 플랫폼이다.

모든 설계와 구현은 다음 기준을 따른다.

* 실제 서비스 운영 가능성
* 권한과 보안
* 유지보수성
* 영상 저장 비용과 서버 부하
* 지도자·분석관의 PC 편집 사용성
* 선수의 모바일 조회 사용성
* 추후 AI 분석 확장 가능성

답변은 결론부터 시작한다.

기본 답변 순서는 다음과 같다.

1. 결론
2. 이유
3. 적용 방법
4. 필요하면 코드 또는 예시
5. 주의사항

---

## 4. 기능 개발 규칙

새 기능을 시작할 때 바로 코드부터 작성하지 않는다.

1. 필요한 요구사항을 확인한다.
2. 정책이 부족하면 핵심 질문을 한다.
3. 요구사항 md 문서를 작성하거나 갱신한다.
4. 구현 순서를 제안한다.
5. 사용자가 진행하겠다고 하면 코드를 구현한다.

이미 요구사항이 충분하면 추가 질문 없이 요구사항 문서를 먼저 정리한다.

기존 요구사항과 현재 요청이 충돌하면 현재 채팅의 최신 사용자 결정을 우선한다.

---

## 5. 기능 완료 후 자동 정리

기능이 완료되면 다음을 정리한다.

1. 최종 요구사항 md 문서
2. 기능별 커밋 메시지
3. GitHub PR 제목과 내용
4. 다음 작업 GitHub 이슈 제목과 10줄 이내 내용
5. 다음 채팅 시작 프롬프트
6. `00_project_context_for_chatgpt.md` 갱신
7. 필요한 경우 백엔드·프론트 정책 문서 갱신
8. DB 변경 시 `soccer_platform.sql` 갱신

사용자가 범위를 제한하면 요청한 항목만 제공한다.

---

## 6. 다음 채팅 프롬프트 필수 문장

다음 채팅 프롬프트에는 아래 문장을 포함한다.

```text
코드 작성 전, 현재 작업에 필요한 상세 요구사항 md와 실제 백엔드/프론트 코드 파일이 충분히 제공되었는지 먼저 확인해줘. 부족한 파일이 있으면 바로 구현하지 말고 필요한 파일 목록을 먼저 요청해줘. DTO 필드명, API 주소, Entity 컬럼명, Repository 메서드명은 추측하지 말고 실제 파일 기준으로 맞춰줘.
```

---

## 7. 기술 스택

### Backend

* Java
* Spring Boot
* Gradle
* Spring Security
* JPA
* MySQL
* IntelliJ IDEA
* JWT 인증
* 로컬 파일 저장소
* ffprobe
* FFmpeg
* Spring `@Async`

### Frontend

* React
* TypeScript
* Vite
* Axios
* VS Code
* 반응형 UI

### 영상 처리

* 경기 원본 영상 실제 mp4 업로드
* 로컬 개발 저장소 사용
* ffprobe 기반 영상 길이 추출
* FFmpeg 기반 팀·선수 분석 클립 파일 생성
* 비동기 클립 생성
* MVP에서는 `/uploads/**` 직접 접근
* 운영 전 권한 검증 스트리밍 API 또는 Signed URL로 전환 필요

---

## 8. 사용자 역할

### COACH

* 스케줄·공지사항 전체 관리
* 경기 영상 전체 관리
* 팀 분석 클립 전체 관리
* 선수 개인 분석 클립 전체 관리
* 드로잉 전체 관리
* 선수 개인 분석 클립 조회 기록 확인
* 경기 영상 기준 선수 기록 등록·갱신
* 분석 클립 연결 선수 기록 이벤트 등록
* 전체 선수 기록과 이벤트 조회
* 본인 북마크 관리

### ANALYST

* 스케줄·공지사항 조회
* 경기 영상 업로드·조회·수정
* 팀 분석 클립 등록·조회·수정
* 선수 개인 분석 클립 등록·조회·수정
* 드로잉 등록·조회·수정
* 선수 개인 분석 클립 조회 기록 확인
* 경기 영상 기준 선수 기록 등록·갱신
* 분석 클립 연결 선수 기록 이벤트 등록
* 전체 선수 기록과 이벤트 조회
* 본인 북마크 관리

영상 및 분석 클립 삭제 권한은 기본적으로 없다.

### PLAYER

* 스케줄 조회
* 공지사항 조회
* 경기 원본 영상 조회
* READY 팀 분석 클립 조회
* 본인 선수 개인 분석 클립 조회
* 본인 드로잉 조회
* 본인 선수 개인 분석 클립 조회 기록 자동 생성·갱신
* 본인 선수 기록 조회
* 본인 선수 기록 이벤트 조회

선수 기록 등록과 북마크 관리는 불가하다.

`isAdmin = true`만으로 선수 기록 관리 권한을 부여하지 않는다.

---

## 9. 주요 DB 테이블

```text
member
schedule
notice
game_video_upload
team_video_clip
player_video_clip
team_video_clip_drawing
player_video_clip_drawing
player_video_clip_view
player_record
player_record_event
player_record_event_clip
video_bookmark
```

현재 단일 팀 기준이므로 별도 `team` 테이블은 사용하지 않는다.

삭제는 기본적으로 `is_deleted` 기반 소프트 삭제다.

선수 기록 API 개편에서는 신규 테이블과 신규 컬럼을 추가하지 않았다.

---

## 10. 현재까지 완료된 기능

* 회원가입·로그인·JWT 인증
* 역할 기반 접근 제어
* 관리자 회원 승인
* 스케줄 백엔드·프론트
* 공지사항 백엔드·프론트
* 경기 영상 업로드 백엔드·프론트
* ffprobe 영상 길이 추출
* 팀 분석 클립 백엔드·프론트
* 팀 분석 클립 드로잉 백엔드·프론트
* 팀 분석 클립 실제 mp4 비동기 생성
* 팀 분석 클립+드로잉 통합 생성·수정
* 팀 분석 클립 등록·수정을 `MatchVideoPage`로 통합
* 선수 개인 분석 클립 백엔드·프론트
* 선수 개인 분석 클립 드로잉 백엔드·프론트
* 선수 개인 분석 클립 조회 기록 백엔드·프론트
* 선수 목록 조회 API
* 선수 개인 분석 클립 실제 mp4 비동기 생성
* 선수 개인 분석 클립+드로잉 통합 생성·수정
* 선수 개인 분석 클립 등록·수정을 `MatchVideoPage`로 통합
* 선수 기록 기본 CRUD 백엔드
* 선수 기록 조회·관리 프론트
  개인 분석 클립 등록·수정을 `MatchVideoPage`로 통합- 선수 기록 이벤트 백엔드
* 선수 기록 이벤트 조회·수정·삭제 프론트
* 선수 기록 이벤트와 팀 분석 클립 연결
* 선수 기록 이벤트와 선수 개인 분석 클립 연결
* 이벤트 생성·수정·삭제 시 요약 수치 자동 보정
* 경기 영상 화면을 분석 작업 허브로 통합
* 경기 영상 북마크 백엔드·프론트 연동
* 경기 원본·팀 분석 클립·선수 개인 클립 북마크 관리
* 북마크 기반 팀·선수 분석 클립 초기 시간 전달
* 경기 영상 기반 선수 기록 등록 및 클립 연결 백엔드 API 구조 개편

---

## 11. 경기 영상 북마크 완료 정책

상세 문서는 다음을 기준으로 한다.

```text
docs/29_match_video_bookmark_requirements.md
```

* `COACH`, `ANALYST` 개인 북마크
* `PLAYER` 접근 불가
* 현재 영상 소스별 본인 북마크만 조회
* 같은 시간 중복 허용
* 북마크 클릭 시 영상 일시 정지
* 해당 시간으로 이동
* 자동 재생하지 않음
* 사이드바 닫기
* 영상 위치로 화면 이동
* 팀·선수 분석 클립 생성 초기 시간으로 사용 가능
* 선수 기록 이벤트 시간으로는 사용하지 않음
* 팀·선수 클립 시간 변경 또는 삭제 시 관련 활성 북마크 소프트 삭제

---

## 12. 선수 기록 최종 요구사항

상세 문서는 다음을 기준으로 한다.

```text
docs/15_player_record_requirements_final.md
```

### 화면 책임

* `PlayerRecordPage`는 검색·목록·상세 조회 전용
* 선수 기록 등록·갱신은 `MatchVideoPage`에서 진행
* 버튼명은 `선수 기록 등록`
* 독립 이벤트 등록·수정·삭제 UI 제거

### 클립 없이 등록

* 현재 경기와 선수의 기존 기록 조회
* 기존 기록이 있으면 기존 값 표시 후 수정
* 기존 기록이 없으면 모든 값 0 표시 후 생성
* 기록 항목별 `-`, 현재 값, `+` 버튼
* 여러 유형을 한 번에 조정
* 최종 저장 버튼으로 전체 요약 기록 저장
* 최소값 0
* 최대값 255
* 성공 기록과 전체 시도 수 사이 추가 정합성 검증 제외
* 이벤트와 클립 연결 데이터는 생성하지 않음

### 클립 연결 등록

* 팀 분석 클립 또는 선수 개인 분석 클립 하나 선택
* 한 요청에서 이벤트 유형 하나 선택
* 같은 클립의 다른 유형은 별도 요청으로 등록 가능
* 같은 클립의 같은 유형은 중복 등록 금지
* 중복 시 저장하지 않고 `409 Conflict`
* 이벤트 시간은 선택 클립에서 백엔드가 자동 추출
* `value = 1` 백엔드 고정
* 선택 유형에 대응하는 요약 기록 증가
* 기존 선수 기록이 없으면 0으로 자동 생성

### 이벤트 스냅샷

```text
player_record_event.event_start_time_sec
player_record_event.event_end_time_sec
player_record_event.value
```

* DB 컬럼 유지
* 프론트 입력과 요청 DTO에서는 제거
* 등록 당시 클립 구간 스냅샷
* 클립 시간 수정 후에도 기존 스냅샷 유지
* 실제 영상 재생은 연결된 현재 클립 기준

---

## 13. 선수 기록 백엔드 API 개편 완료 내용

완료된 핵심 내용은 다음과 같다.

1. 클립 연결 요청 DTO에서 `eventStartTimeSec` 제거
2. `eventEndTimeSec` 제거
3. `value` 제거
4. 팀·선수 클립 시간 자동 조회
5. `value = 1` 고정
6. READY 상태 검증
7. 요청 경기 영상과 클립 경기 영상 일치 검증
8. 개인 클립 대상 선수와 요청 선수 일치 검증
9. 동일 클립·동일 이벤트 유형 중복 차단
10. 비관적 잠금으로 동시 중복 요청 방지
11. 기존 `player_record`가 없으면 모든 수치를 0으로 자동 생성
12. 이벤트 유형별 선수 요약 기록 자동 반영
13. 독립 이벤트 등록·수정·삭제 API 제거
14. 독립 이벤트 등록·수정 요청 DTO 삭제
15. 관리용·선수 본인 이벤트 조회 API 유지
16. 트랜잭션 롤백 처리
17. 신규 DB 테이블과 컬럼 추가 없음

---

## 14. 선수 기록 최종 API

### 선수 요약 기록

```http
GET   /api/management/player-records
GET   /api/management/player-records/{recordId}
POST  /api/management/player-records
PATCH /api/management/player-records/{recordId}
```

### 클립 연결 이벤트 등록

```http
POST /api/management/player-record-events/with-clip-link
```

### 관리용 이벤트 조회

```http
GET /api/management/player-records/{recordId}/events
GET /api/management/player-record-events/{eventId}
```

### 선수 본인 이벤트 조회

```http
GET /api/player/me/player-records/{recordId}/events
GET /api/player/me/player-record-events/{eventId}
```

### 제거된 API

```http
POST   /api/management/player-record-events
PATCH  /api/management/player-record-events/{eventId}
DELETE /api/management/player-record-events/{eventId}
```

---

## 15. 선수 기록 클립 연결 요청 DTO

최종 요청 필드는 다음과 같다.

```text
uploadId
playerId
eventType
eventMemo
clipSourceType
teamClipId
playerClipId
```

### 팀 분석 클립 연결

```json
{
  "uploadId": 8,
  "playerId": 8,
  "eventType": "SHOT",
  "eventMemo": "팀 분석 클립 연결",
  "clipSourceType": "TEAM_ANALYSIS",
  "teamClipId": 10,
  "playerClipId": null
}
```

### 선수 개인 분석 클립 연결

```json
{
  "uploadId": 8,
  "playerId": 8,
  "eventType": "SUCCESSFUL_PASS",
  "eventMemo": "선수 개인 분석 클립 연결",
  "clipSourceType": "PLAYER_ANALYSIS",
  "teamClipId": null,
  "playerClipId": 15
}
```

다음 필드는 요청하지 않는다.

```text
eventStartTimeSec
eventEndTimeSec
value
```

---

## 16. 선수 기록 백엔드 테스트 완료 내용

사용자가 다음 항목을 실제로 정상 확인했다.

### 선수 요약 기록

* 경기와 선수 기준 기존 기록 조회
* 기존 기록 신규 생성
* 기존 기록 전체 값 수정
* 추가 상호 정합성 검증 제거
* 음수 값 차단
* 255 초과 값 차단
* 동일 경기·동일 선수 중복 생성 차단
* 클립 없는 등록 시 이벤트·연결 데이터 미생성

### 팀 분석 클립 연결

* 정상 등록
* 클립 시간 자동 저장
* `value = 1` 자동 저장
* 선수 요약 기록 증가
* 같은 클립·같은 유형 중복 차단
* 같은 클립·다른 유형 등록 허용

### 선수 개인 분석 클립 연결

* 정상 등록
* 클립 시간 자동 저장
* `value = 1` 자동 저장
* 선수 요약 기록 증가
* 같은 클립·같은 유형 중복 차단
* 같은 클립·다른 유형 등록 허용
* 개인 클립 대상 선수 불일치 차단
* 잘못된 클립 ID 조합 차단

### 권한·예외·트랜잭션

* 기존 `player_record`가 없을 때 자동 생성
* READY가 아닌 팀 클립 차단
* READY가 아닌 개인 클립 차단
* 경기 영상 불일치 차단
* COACH 등록 허용
* ANALYST 등록 허용
* PLAYER 등록 차단
* `isAdmin = true`인 PLAYER 등록 차단
* 실패 요청 시 이벤트·연결·요약 수치 미변경
* 관리용 이벤트 목록·상세 조회
* 선수 본인 이벤트 목록·상세 조회
* 다른 선수 이벤트 접근 차단
* 복합 이벤트 요약 수치 반영
* `ETC` 이벤트 요약 미반영
* 클립 수정 후 이벤트 시간 스냅샷 유지
* 요약 수치 255 초과 시 전체 트랜잭션 롤백
* 동일 요청 동시 전송 시 한 건만 생성
* 독립 이벤트 등록·수정·삭제 API 제거

---

## 17. 현재 작업 상태

현재 백엔드 작업 브랜치는 다음과 같다.

```text
player-record-registration-redesign-api
```

경기 영상 기반 선수 기록 등록 및 클립 연결 백엔드 API 개편과 수동 테스트는 완료됐다.

현재 단계는 다음 작업이다.

1. 최종 요구사항과 정책 문서 커밋
2. 백엔드 PR 생성
3. 백엔드 PR을 `main`에 병합
4. 최신 `main`에서 선수 기록 프론트 연동 브랜치 생성
5. 프론트 구현 시작

---

## 18. 다음 프론트 작업

다음 작업명은 다음과 같다.

```text
경기 영상 기반 선수 기록 등록 프론트 연동
```

권장 이슈 제목은 다음과 같다.

```text
경기 영상 기반 선수 기록 등록 프론트 연동
```

권장 브랜치는 다음과 같다.

```text
feature/player-record-registration-redesign-frontend
```

백엔드 PR이 `main`에 병합된 후 최신 `main`에서 브랜치를 생성한다.

주요 구현 범위는 다음과 같다.

1. `PlayerRecordPage`를 조회 전용으로 변경
2. 선수 기록 등록·수정·삭제 UI 제거
3. 독립 이벤트 등록·수정·삭제 UI 제거
4. `MatchVideoPage` 버튼명을 `선수 기록 등록`으로 변경
5. 분석 모드명 정리
6. 기존 선수 기록 조회
7. 기록이 없으면 모든 값 0 표시
8. 기록 카운터 UI 구현
9. 기존 기록 유무에 따라 생성·수정 API 분기
10. 팀 분석 클립과 이벤트 유형 연결
11. 선수 개인 분석 클립과 이벤트 유형 연결
12. 이벤트 시간과 `value` 입력 UI 제거
13. 동일 클립·동일 유형 중복 메시지 표시
14. 기존 북마크와 분석 클립 기능 회귀 테스트

---

## 19. 다음 프론트 채팅에 필요한 파일

다음 파일을 우선 확인하거나 업로드한다.

```text
docs/15_player_record_requirements_final.md
docs/29_match_video_bookmark_requirements.md
docs/00_current_backend_policy.md
docs/00_current_frontend_policy.md
docs/00_project_context_for_chatgpt.md

frontend/src/pages/PlayerRecordPage.tsx
frontend/src/pages/MatchVideoPage.tsx
frontend/src/components/PlayerRecordEditorPanel.tsx
frontend/src/components/PlayerRecordEventEditorPanel.tsx
frontend/src/types/playerRecord.ts
frontend/src/types/playerRecordEvent.ts
frontend/src/api/playerRecordApi.ts
frontend/src/api/playerRecordEventApi.ts
frontend/src/constants/routes.ts
frontend/src/App.tsx
frontend/src/App.css

backend/src/main/java/com/soccer/platform/controller/PlayerRecordController.java
backend/src/main/java/com/soccer/platform/controller/PlayerRecordEventController.java
backend/src/main/java/com/soccer/platform/dto/playerrecord/
backend/src/main/java/com/soccer/platform/dto/playerrecordevent/
backend/src/main/java/com/soccer/platform/common/exception/ErrorCode.java
```

실제 파일명과 경로가 다르면 현재 `main` 소스 기준으로 맞춘다.

---

## 20. 반복 오류 예방 규칙

* 프론트 Props 추가 시 Props 타입, 구조 분해, 호출부를 함께 수정한다.
* 백엔드 DTO와 프론트 타입을 정확히 일치시킨다.
* API 응답 배열 래핑 여부를 확인한다.
* React effect에서 async 호출과 언마운트 상태를 관리한다.
* TypeScript 타입 import는 `import type`을 사용한다.
* 사용하지 않는 변수를 남기지 않는다.
* 수정 요청 시 전체 파일 코드를 제공한다.
* 제거된 백엔드 API를 프론트에서 호출하지 않는다.
* 제거된 DTO 필드를 프론트 타입에 남기지 않는다.
* 브랜치 이동 전 untracked 파일을 포함해 변경사항을 커밋하거나 stash한다.

---

## 21. 브랜치 운영 주의사항

현재 백엔드 브랜치:

```text
player-record-registration-redesign-api
```

다음 프론트 브랜치:

```text
feature/player-record-registration-redesign-frontend
```

프론트 브랜치는 백엔드 PR이 `main`에 병합된 후 최신 `main`에서 생성한다.

백엔드와 프론트 작업은 서로 다른 이슈와 브랜치로 분리한다.
