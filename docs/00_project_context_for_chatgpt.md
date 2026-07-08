# 00. ChatGPT 프로젝트 컨텍스트

## 1. 문서 목적

이 문서는 ChatGPT 새 채팅 또는 기능별 작업 시작 시 업로드할 핵심 요약 문서다.

기능별 상세 요구사항 문서를 모두 ChatGPT 프로젝트 소스에 업로드하지 못하는 상황을 대비해, 프로젝트의 최신 기준과 작업 규칙을 한 곳에 요약한다.

이 문서는 상세 요구사항 문서를 대체하지 않는다.

상세 요구사항 문서는 GitHub 프로젝트의 `docs/` 디렉터리에 기능별로 계속 유지하고, 이 문서는 ChatGPT가 작업을 빠르게 이어갈 수 있게 하는 업로드용 요약 문서로 사용한다.

상세 요구사항 md 참고가 필요하면 아래 GitHub docs 경로를 우선 기준으로 확인한다.

```text
https://github.com/an-sehyeon/soccer-team-management-platform/tree/main/docs
```

---

## 2. 기본 업로드 파일

새 채팅 또는 기능 작업 시작 시 기본 업로드 파일은 다음 5개를 우선 사용한다.

```text
00_project_context_for_chatgpt.md
00_current_backend_policy.md
00_current_frontend_policy.md
soccer_platform.sql
README.md
```

기능별 작업을 시작할 때는 위 5개에 더해서 현재 작업에 꼭 필요한 상세 요구사항 문서와 실제 코드 파일만 추가로 업로드한다.

ChatGPT는 DTO 필드명, API 주소, Entity 컬럼명, Repository 메서드명을 추측해서 구현하지 않는다.

단, 사용자가 현재 로컬 파일과 GitHub `main`이 동일하다고 명확히 말한 경우에는 GitHub `main`을 기준으로 실제 코드 구조를 확인한 뒤 작업할 수 있다.

---

## 3. 기본 원칙

이 프로젝트는 포트폴리오용 예제가 아니라 실제 축구팀에 판매하고 운영할 목적의 플랫폼이다.

모든 설계와 구현은 다음 기준을 따른다.

- 실제 서비스 운영 가능성
- 권한과 보안
- 유지보수성
- 영상 저장 비용과 서버 부하
- 지도자/분석관의 PC 편집 사용성
- 선수의 모바일 조회 사용성
- 추후 AI 분석 확장 가능성

답변과 작업은 항상 결론부터 시작한다.

---

## 4. 현재 기술 스택

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
- ffprobe 기반 영상 길이 추출
- FFmpeg 기반 클립 파일 생성
- Spring `@Async` 기반 비동기 파일 생성

### Frontend

- React
- TypeScript
- Vite
- Axios
- VS Code
- 반응형 UI

---

## 5. 현재까지 완료된 기능

현재까지 구현 또는 연동이 완료된 기능은 다음과 같다.

- 회원가입/로그인/JWT 인증
- 역할 기반 접근 제어
- 관리자 회원 승인
- 스케줄 백엔드 구현
- 스케줄 프론트 연동
- 공지사항 백엔드 구현
- 공지사항 프론트 연동
- 경기 영상 업로드 백엔드 구현
- 경기 영상 길이 `ffprobe` 추출
- 경기 영상 프론트 연동
- 팀 분석 클립 백엔드 구현
- 팀 분석 클립 프론트 연동
- 팀 분석 클립 드로잉 백엔드 구현
- 팀 분석 클립 드로잉 프론트 연동
- 선수 개인 분석 클립 백엔드 구현
- 선수 개인 분석 클립 조회 기록 백엔드 구현
- 선수 개인 분석 클립 드로잉 백엔드 구현
- 선수 목록 조회 API 백엔드 구현
- 선수 개인 분석 클립 프론트 연동
- 선수 개인 분석 클립 비동기 파일 생성 구조 구현
- 팀 분석 클립 실제 mp4 비동기 파일 생성 구조 구현
- 팀 분석 클립 + 드로잉 통합 생성 API 구현
- 팀 분석 클립 + 드로잉 통합 수정 API 구현
- 팀 분석 클립 목록/상세 페이지와 등록/수정 편집기 페이지 분리
- 팀 분석 클립 드로잉 시간 기준을 생성된 팀 분석 클립 영상 기준으로 정리
- 선수 개인 분석 클립 + 드로잉 통합 수정 API 구현
- 선수 개인 분석 클립 목록/상세 페이지와 등록/수정 편집기 페이지 분리
- 선수 개인 분석 클립 상세 재생을 `playerClipUrl` 기준으로 정리
- 선수 개인 분석 클립 드로잉 시간 기준을 생성된 선수 개인 분석 클립 영상 기준으로 정리
- 선수 개인 분석 클립 조회 기록 Controller 추가
- 선수 개인 분석 클립 조회 기록 프론트 연동 완료
- 선수 기록 관리 백엔드 기본 CRUD 구현

---

## 6. 현재 중단/변경된 기능

기존 다음 작업은 선수 기록 관리 프론트 연동이었다.

하지만 요구사항이 변경되어 단순 `/player-records` 관리 CRUD 프론트 연동은 보류한다.

변경된 방향은 다음과 같다.

```text
/match-videos 경기 영상 상세 화면
→ 클립 생성
→ 드로잉 작성
→ 클립 저장
→ 선수 기록 이벤트와 선택적 연결
→ 또는 클립 없이 선수 기록 이벤트만 등록
```

따라서 다음 작업은 프론트가 아니라 백엔드 DB/API 확장 작업이다.

---

## 7. 다음 작업

다음 작업명은 다음과 같다.

```text
경기 영상 기반 선수 기록 이벤트와 클립 연결 API 구현
```

브랜치명은 다음을 사용한다.

```text
feature/player-record-event-clip-link-api
```

관련 요구사항 문서는 다음이다.

```text
docs/28_player_record_frontend_integration_requirements.md
```

이번 작업은 백엔드 작업만 진행한다.

프론트 구현은 백엔드 API 테스트 완료 후 기존 채팅으로 돌아와 진행한다.

---

## 8. 다음 작업 핵심 정책

핵심 정책은 다음과 같다.

- 기존 `player_record`는 경기별 선수 요약 기록으로 유지한다.
- 같은 `upload_id`, `player_id` 기준으로 활성 `player_record`는 1개만 유지한다.
- 기록 이벤트 등록 시 기존 `player_record`가 있으면 새로 만들지 않고 요약 수치를 갱신한다.
- 기존 `player_record`가 없으면 기본값 0으로 자동 생성한다.
- `player_record_event`는 기록 이벤트가 등록될 때마다 새로 저장한다.
- `player_record_event`에는 `upload_id`, `player_id`를 저장하지 않는다.
- `upload_id`, `player_id`는 `player_record_id`를 통해 `player_record`에서 확인한다.
- `player_record_event.memo`는 이벤트 단위 메모이며, `player_record.memo`와 별도로 유지한다.
- DTO에서는 이벤트 단위 메모를 `eventMemo`로 표현할 수 있다.
- `player_record_event_clip`은 선택 연결 테이블이다.
- 기록 이벤트는 클립 없이도 존재할 수 있다.
- 팀 분석 클립과 선수 개인 분석 클립 모두 기록 이벤트와 연결할 수 있다.
- 선수 개인 분석 클립 연결 시 기록 대상 선수와 클립 대상 선수가 일치해야 한다.
- 팀 분석 클립 연결 시 기록 대상 선수를 별도로 선택한다.
- 이벤트 생성/수정/삭제 시 `player_record` 요약 수치를 갱신 또는 보정한다.
- 삭제는 소프트 삭제를 사용한다.
- 권한 검증은 반드시 백엔드에서 처리한다.

---

## 9. 사용할 주요 테이블

이번 작업에서 사용할 주요 테이블은 다음과 같다.

```text
player_record
player_record_event
player_record_event_clip
game_video_upload
team_video_clip
player_video_clip
member
```

신규 추가 테이블은 다음과 같다.

```text
player_record_event
player_record_event_clip
```

---

## 10. 추가할 주요 API

이번 작업에서 추가할 주요 API는 다음과 같다.

```http
POST   /api/management/player-record-events
POST   /api/management/player-record-events/with-clip-link
GET    /api/management/player-records/{recordId}/events
GET    /api/management/player-record-events/{eventId}
PATCH  /api/management/player-record-events/{eventId}
DELETE /api/management/player-record-events/{eventId}

GET    /api/player/me/player-records/{recordId}/events
GET    /api/player/me/player-record-events/{eventId}
```

---

## 11. 권한 정책

COACH는 다음 작업이 가능하다.

- 기록 이벤트 등록
- 기록 이벤트 수정
- 기록 이벤트 삭제
- 기록 이벤트 조회
- 기록 이벤트와 클립 연결

ANALYST는 다음 작업이 가능하다.

- 기록 이벤트 등록
- 기록 이벤트 수정
- 기록 이벤트 삭제
- 기록 이벤트 조회
- 기록 이벤트와 클립 연결

PLAYER는 다음 작업만 가능하다.

- 본인 기록 이벤트 조회
- 본인 기록 이벤트에 연결된 클립 조회

---

## 12. 삭제 정책

삭제는 실제 삭제가 아니라 소프트 삭제를 사용한다.

대상은 다음과 같다.

```text
player_record
player_record_event
player_record_event_clip
```

기록 이벤트 삭제 시 다음을 처리한다.

```text
player_record_event.is_deleted = true
player_record_event_clip.is_deleted = true
player_record 요약 수치 차감
```

요약 수치가 음수가 되면 안 된다.

---

## 13. 백엔드 작업에서 우선 확인할 파일

다음 백엔드 파일을 우선 확인한다.

```text
soccer_platform.sql
backend/src/main/java/com/soccer/platform/entity/PlayerRecordEntity.java
backend/src/main/java/com/soccer/platform/repository/PlayerRecordRepository.java
backend/src/main/java/com/soccer/platform/service/playerrecord/PlayerRecordService.java
backend/src/main/java/com/soccer/platform/service/playerrecord/PlayerRecordValidator.java
backend/src/main/java/com/soccer/platform/controller/PlayerRecordController.java
backend/src/main/java/com/soccer/platform/entity/TeamVideoClipEntity.java
backend/src/main/java/com/soccer/platform/repository/TeamVideoClipRepository.java
backend/src/main/java/com/soccer/platform/entity/PlayerVideoClipEntity.java
backend/src/main/java/com/soccer/platform/repository/PlayerVideoClipRepository.java
backend/src/main/java/com/soccer/platform/entity/GameVideoUploadEntity.java
backend/src/main/java/com/soccer/platform/repository/GameVideoUploadRepository.java
backend/src/main/java/com/soccer/platform/entity/MemberEntity.java
backend/src/main/java/com/soccer/platform/repository/MemberRepository.java
backend/src/main/java/com/soccer/platform/common/exception/ErrorCode.java
backend/src/main/java/com/soccer/platform/config/SecurityConfig.java
```

---

## 14. 백엔드 구현 순서

구현 순서는 다음과 같다.

1. `soccer_platform.sql`에 신규 테이블 DDL 추가
2. `PlayerRecordEventTypeEnum` 추가
3. `PlayerRecordClipSourceTypeEnum` 추가
4. `PlayerRecordEventEntity` 추가
5. `PlayerRecordEventClipEntity` 추가
6. `PlayerRecordEventRepository` 추가
7. `PlayerRecordEventClipRepository` 추가
8. 요청 DTO 추가
9. 응답 DTO 추가
10. `PlayerRecordEventValidator` 추가
11. `PlayerRecordEventService` 추가
12. `PlayerRecordEventController` 추가
13. `ErrorCode` 추가
14. `SecurityConfig` 신규 API 권한 확인
15. API 테스트

---

## 15. 다음 채팅 프롬프트 작성 규칙

다음 채팅 프롬프트에는 반드시 아래 문장을 포함한다.

```text
코드 작성 전, 현재 작업에 필요한 상세 요구사항 md와 실제 백엔드/프론트 코드 파일이 충분히 제공되었는지 먼저 확인해줘. 부족한 파일이 있으면 바로 구현하지 말고 필요한 파일 목록을 먼저 요청해줘. DTO 필드명, API 주소, Entity 컬럼명, Repository 메서드명은 추측하지 말고 실제 파일 기준으로 맞춰줘.
```

---

## 16. 기능 완료 후 자동 정리 규칙

기능 작업이 끝나면 사용자가 따로 요청하지 않아도 마지막 정리 단계에서 다음을 제공한다.

1. 변경 내용이 반영된 최종 요구사항 md 문서
2. 생성/수정 파일 기준 기능별 커밋 메시지
3. GitHub PR 제목과 내용
4. 다음 작업 GitHub 이슈 제목과 10줄 이내 내용
5. 다음 기능을 새 채팅에서 시작할 수 있는 프롬프트
6. 다음 작업 기준으로 갱신된 `00_project_context_for_chatgpt.md`
7. 필요하면 `00_current_backend_policy.md`, `00_current_frontend_policy.md` 갱신 내용