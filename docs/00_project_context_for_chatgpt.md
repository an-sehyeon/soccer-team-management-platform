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

- 실제 서비스 운영 가능성
- 권한과 보안
- 유지보수성
- 영상 저장 비용과 서버 부하
- 지도자·분석관의 PC 편집 사용성
- 선수의 모바일 조회 사용성
- 추후 AI 분석 확장 가능성

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

- Java
- Spring Boot
- Gradle
- Spring Security
- JPA
- MySQL
- IntelliJ IDEA
- JWT 인증
- 로컬 파일 저장소
- ffprobe
- FFmpeg
- Spring `@Async`

### Frontend

- React
- TypeScript
- Vite
- Axios
- VS Code
- 반응형 UI

### 영상 처리

- 경기 원본 영상 실제 mp4 업로드
- 로컬 개발 저장소 사용
- ffprobe 기반 영상 길이 추출
- FFmpeg 기반 팀·선수 분석 클립 파일 생성
- 비동기 클립 생성
- MVP에서는 `/uploads/**` 직접 접근
- 운영 전 권한 검증 스트리밍 API 또는 Signed URL로 전환 필요

---

## 8. 사용자 역할

### COACH

- 스케줄·공지사항 전체 관리
- 경기 영상 전체 관리
- 팀 분석 클립 전체 관리
- 선수 개인 분석 클립 전체 관리
- 드로잉 전체 관리
- 선수 개인 분석 클립 조회 기록 확인
- 경기 영상 기준 선수 기록 등록·갱신
- 분석 클립 연결 선수 기록 등록
- 전체 선수 기록과 이벤트 조회
- 본인 북마크 관리

### ANALYST

- 스케줄·공지사항 조회
- 경기 영상 업로드·조회·수정
- 팀 분석 클립 등록·조회·수정
- 선수 개인 분석 클립 등록·조회·수정
- 드로잉 등록·조회·수정
- 선수 개인 분석 클립 조회 기록 확인
- 경기 영상 기준 선수 기록 등록·갱신
- 분석 클립 연결 선수 기록 등록
- 전체 선수 기록과 이벤트 조회
- 본인 북마크 관리

영상 및 분석 클립 삭제 권한은 기본적으로 없다.

### PLAYER

- 스케줄 조회
- 공지사항 조회
- 경기 원본 영상 조회
- READY 팀 분석 클립 조회
- 본인 선수 개인 분석 클립 조회
- 본인 드로잉 조회
- 본인 선수 개인 분석 클립 조회 기록 자동 생성·갱신
- 본인 선수 기록 조회
- 본인 선수 기록 이벤트 조회

선수 기록 등록과 북마크 관리는 불가하다.

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

---

## 10. 현재까지 완료된 기능

- 회원가입·로그인·JWT 인증
- 역할 기반 접근 제어
- 관리자 회원 승인
- 스케줄 백엔드·프론트
- 공지사항 백엔드·프론트
- 경기 영상 업로드 백엔드·프론트
- ffprobe 영상 길이 추출
- 팀 분석 클립 백엔드·프론트
- 팀 분석 클립 드로잉 백엔드·프론트
- 팀 분석 클립 실제 mp4 비동기 생성
- 팀 분석 클립+드로잉 통합 생성·수정
- 팀 분석 클립 등록·수정을 `MatchVideoPage`로 통합
- 선수 개인 분석 클립 백엔드·프론트
- 선수 개인 분석 클립 드로잉 백엔드·프론트
- 선수 개인 분석 클립 조회 기록 백엔드·프론트
- 선수 목록 조회 API
- 선수 개인 분석 클립 실제 mp4 비동기 생성
- 선수 개인 분석 클립+드로잉 통합 생성·수정
- 선수 개인 분석 클립 등록·수정을 `MatchVideoPage`로 통합
- 선수 기록 기본 CRUD 백엔드
- 선수 기록 조회·관리 프론트
- 선수 기록 이벤트 백엔드
- 선수 기록 이벤트 조회·수정·삭제 프론트
- 선수 기록 이벤트와 팀 분석 클립 연결
- 선수 기록 이벤트와 선수 개인 분석 클립 연결
- 이벤트 생성·수정·삭제 시 요약 수치 자동 보정
- 경기 영상 화면을 분석 작업 허브로 통합

---

## 11. 현재 진행 중인 기능

현재 `front-bookmark` 브랜치에서 경기 영상 북마크 기능을 구현 중이다.

주요 구현 범위는 다음과 같다.

- 영상 북마크 Entity, Repository, DTO, Validator, Service, Controller
- 경기 원본 영상 북마크 등록·조회·수정·삭제
- 팀·선수 분석 클립 변경 또는 삭제 시 북마크 일괄 소프트 삭제
- 프론트 북마크 타입과 API
- 오른쪽 고정 북마크 사이드바
- 북마크 선택 시 자동 재생 없이 시간 이동
- 영상 위치로 화면 스크롤
- 북마크 기반 팀·선수 분석 클립 초기 시간 전달

현재 작업 브랜치에는 커밋되지 않은 백엔드와 프론트 변경 및 untracked 파일이 존재할 수 있다.

다른 기능 브랜치로 이동하기 전 반드시 커밋하거나 `git stash push -u`로 보존한다.

---

## 12. 새로 확정된 선수 기록 정책

상세 문서는 다음을 기준으로 한다.

```text
docs/15_player_record_requirements_final.md
```

### 화면 책임

- `PlayerRecordPage`는 검색·목록·상세 조회 전용
- 선수 기록 등록·갱신은 `MatchVideoPage`에서 진행
- 버튼명은 `선수 기록 등록`

### 클립 없이 등록

- 현재 경기와 선수의 기존 기록 조회
- 기존 기록이 있으면 기존 값 표시 후 갱신
- 없으면 모든 값 0으로 생성
- 기록 항목별 `-`, 현재 값, `+` 버튼
- 여러 유형을 한 번에 조정
- 최종 저장 버튼으로 전체 요약 기록 한 번에 저장
- 최소값 0
- 성공 기록과 전체 시도 수 사이 추가 검증은 현재 제외
- 이벤트와 클립 연결 데이터는 생성하지 않음

### 클립 연결 등록

- 팀 분석 클립 또는 선수 개인 분석 클립 하나 선택
- 한 요청에서 이벤트 유형 하나 선택
- 같은 클립의 다른 유형은 별도 요청으로 등록 가능
- 같은 클립의 같은 유형은 중복 등록 금지
- 중복 시 저장하지 않고 사용자 안내
- 이벤트 시간은 선택 클립에서 백엔드가 자동 추출
- `value = 1` 백엔드 고정
- 선택 유형에 대응하는 요약 기록 +1
- 기존 선수 기록이 없으면 0으로 자동 생성

### 이벤트 스냅샷

```text
player_record_event.event_start_time_sec
player_record_event.event_end_time_sec
player_record_event.value
```

- DB 컬럼 유지
- 프론트 입력과 요청 DTO에서는 제거
- 등록 당시 클립 구간 스냅샷
- 클립 시간 수정 후에도 기존 스냅샷 유지
- 실제 영상 재생은 연결된 현재 클립 기준

---

## 13. 영상 북마크 최신 정책

상세 문서는 다음을 기준으로 한다.

```text
docs/29_match_video_bookmark_requirements.md
```

- COACH·ANALYST 개인 북마크
- PLAYER 접근 불가
- 현재 영상 소스별 본인 북마크만 조회
- 같은 시간 중복 허용
- 북마크 클릭 시 영상 일시 정지
- 해당 시간으로 이동
- 자동 재생하지 않음
- 사이드바 닫기
- 영상 위치로 화면 이동
- 팀·선수 분석 클립 생성 초기 시간으로 사용 가능
- 선수 기록 등록에는 북마크 시간을 이벤트 시간으로 사용하지 않음

---

## 14. 다음 백엔드 작업

다음 작업은 다음과 같다.

```text
경기 영상 기반 선수 기록 등록 및 클립 연결 API 구조 개편
```

권장 이슈 제목은 다음과 같다.

```text
경기 영상 기반 선수 기록 등록 및 클립 연결 API 구조 개편
```

권장 브랜치는 다음과 같다.

```text
feature/player-record-registration-redesign-api
```

핵심 구현 범위는 다음과 같다.

1. 기존 선수 기록 Controller, DTO, Service, Validator, Repository 확인
2. 클립 연결 요청 DTO에서 시간과 `value` 제거
3. 팀·선수 클립 시간 자동 조회
4. `value = 1` 고정
5. 동일 클립·동일 이벤트 유형 중복 차단
6. 기존 `player_record`가 없으면 자동 생성
7. 선택 유형 요약 기록 +1
8. 기존 조회 API 회귀 테스트
9. 요구사항과 정책 문서 갱신

---

## 15. 다음 백엔드 채팅에 필요한 파일

다음 파일을 우선 확인하거나 업로드한다.

```text
docs/15_player_record_requirements_final.md
docs/29_match_video_bookmark_requirements.md
docs/00_current_backend_policy.md
docs/00_current_frontend_policy.md
docs/00_project_context_for_chatgpt.md

backend/src/main/java/com/soccer/platform/controller/PlayerRecordController.java
backend/src/main/java/com/soccer/platform/controller/PlayerRecordEventController.java
backend/src/main/java/com/soccer/platform/dto/playerrecord/
backend/src/main/java/com/soccer/platform/dto/playerrecordevent/
backend/src/main/java/com/soccer/platform/entity/PlayerRecordEntity.java
backend/src/main/java/com/soccer/platform/entity/PlayerRecordEventEntity.java
backend/src/main/java/com/soccer/platform/entity/PlayerRecordEventClipEntity.java
backend/src/main/java/com/soccer/platform/repository/PlayerRecordRepository.java
backend/src/main/java/com/soccer/platform/repository/PlayerRecordEventRepository.java
backend/src/main/java/com/soccer/platform/repository/PlayerRecordEventClipRepository.java
backend/src/main/java/com/soccer/platform/service/playerrecord/
backend/src/main/java/com/soccer/platform/service/playerrecordevent/
backend/src/main/java/com/soccer/platform/common/exception/ErrorCode.java
```

실제 패키지명과 파일명이 다르면 현재 소스 기준으로 맞춘다.

---

## 16. 다음 프론트 작업

백엔드 선수 기록 API 개편과 테스트가 완료된 후 진행한다.

- `PlayerRecordPage` 조회 전용 정리
- `MatchVideoPage` 버튼명을 `선수 기록 등록`으로 변경
- 분석 모드명 정리
- 클립 없이 등록 카운터 UI
- 기존 기록 조회 후 생성·수정 분기
- 팀 분석 클립 연결 등록
- 선수 개인 분석 클립 연결 등록
- 같은 클립·같은 유형 중복 오류 표시
- 기존 북마크 기능과 분석 클립 기능 회귀 테스트

---

## 17. 반복 오류 예방 규칙

- 프론트 Props 추가 시 Props 타입, 구조 분해, 호출부를 함께 수정한다.
- 백엔드 DTO 변경 전 프론트 타입을 확정하지 않는다.
- API 응답 배열 래핑 여부를 확인한다.
- React effect에서 async 호출과 언마운트 상태를 관리한다.
- TypeScript 타입 import는 `import type`을 사용한다.
- 사용하지 않는 변수를 남기지 않는다.
- 수정 요청 시 전체 파일 코드를 제공한다.
- 브랜치 이동 전 untracked 파일을 포함해 변경사항을 커밋하거나 stash한다.

---

## 18. 브랜치 운영 주의사항

현재 기능과 다음 기능의 범위가 다르면 새 이슈와 새 브랜치를 사용한다.

```text
front-bookmark
→ 경기 영상 북마크 기능

feature/player-record-registration-redesign-api
→ 선수 기록 백엔드 정책 개편
```

테스트가 끝나지 않은 변경사항은 새 백엔드 브랜치에 섞지 않는다.

`git stash push -u`를 사용하면 추적 중인 수정 파일과 untracked 파일을 함께 보관할 수 있다.

새 백엔드 브랜치는 최신 `main`에서 생성한다.
