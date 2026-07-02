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

## 2. 문서 보관 기준

문서 보관 위치는 다음처럼 구분한다.

```text
GitHub 프로젝트 소스 docs/
→ 기능별 상세 md 문서를 전부 보관한다.
→ 요구사항 변경 이력, 커밋, PR 리뷰를 위해 삭제하지 않는다.

ChatGPT 프로젝트 소스 업로드
→ 모든 상세 md를 올리지 않는다.
→ 핵심 요약 파일 5개만 기본으로 유지한다.
→ 현재 작업에 꼭 필요한 상세 md나 코드 파일만 추가로 올린다.
```

따라서 GitHub의 `docs/` 안에는 기존 md 파일들을 계속 유지한다.

단, ChatGPT 프로젝트 소스 업로드 한도 때문에 ChatGPT 프로젝트에는 모든 md 파일을 올리지 않는다.

ChatGPT 프로젝트 소스에 이미 많은 기능별 md가 올라가 있어 25개 제한에 걸린 경우에는, 오래된 기능별 상세 md를 ChatGPT 프로젝트 소스에서 제거하고 아래 기본 파일만 남긴다.

```text
00_project_context_for_chatgpt.md
00_current_backend_policy.md
00_current_frontend_policy.md
soccer_platform.sql
README.md
```

기능별 상세 md는 GitHub에 남아 있으므로 삭제되는 것이 아니라, ChatGPT 프로젝트 업로드 대상에서만 제외되는 것이다.

ChatGPT가 작업 중 상세 요구사항 md 내용을 확인해야 하는 경우에는 사용자가 다시 모든 md 파일을 업로드하기를 기다리지 말고, 우선 GitHub docs 경로를 참고할 수 있음을 안내한다. 다만 코드 구현에 필요한 실제 Controller, DTO, Entity, Repository, Service 파일은 GitHub 문서만으로 추측하지 않고 사용자에게 업로드를 요청한다.

```text
GitHub docs 참고 경로:
https://github.com/an-sehyeon/soccer-team-management-platform/tree/main/docs
```

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

기본 답변 순서는 다음을 따른다.

1. 결론
2. 이유
3. 적용 방법
4. 필요하면 코드 또는 예시
5. 주의사항

---

## 4. ChatGPT 업로드 운영 방식

ChatGPT 프로젝트 업로드 제한 때문에 모든 md 파일을 매번 업로드하지 않는다.

새 채팅 또는 기능 작업 시작 시 기본 업로드 파일은 다음 5개를 우선 사용한다.

```text
00_project_context_for_chatgpt.md
00_current_backend_policy.md
00_current_frontend_policy.md
soccer_platform.sql
README.md
```

기능별 작업을 시작할 때는 위 5개에 더해서, 현재 작업에 꼭 필요한 상세 요구사항 문서와 실제 코드 파일만 추가로 업로드한다.

단, 상세 요구사항 md 파일이 ChatGPT 프로젝트 소스에 업로드되어 있지 않은 경우에는 아래 GitHub docs 경로를 참고한다.

```text
https://github.com/an-sehyeon/soccer-team-management-platform/tree/main/docs
```

모든 상세 md를 ChatGPT 프로젝트에 상시 업로드하지 않는다.

예시는 다음과 같다.

```text
선수 개인 분석 클립 백엔드 작업:
- 00_project_context_for_chatgpt.md
- 00_current_backend_policy.md
- soccer_platform.sql
- 12_player_analysis_clip_requirements_updated.md
- 13_player_analysis_clip_drawing_requirements.md
- 24_player_analysis_clip_file_generation_requirements.md
```

```text
프론트 연동 작업:
- 00_project_context_for_chatgpt.md
- 00_current_frontend_policy.md
- 관련 프론트 연동 요구사항 문서
- 실제 프론트 파일
- 실제 백엔드 Controller/DTO 파일
```

새 채팅에서 작업을 시작할 때 필요한 문서나 코드 파일이 불명확하면, ChatGPT는 바로 구현하지 말고 먼저 사용자에게 다음을 질문해야 한다.

```text
이번 작업에 필요한 상세 요구사항 md, Controller, DTO, Entity, Repository, Service, 프론트 타입/API/페이지 파일 중 어떤 파일을 업로드해야 하는지 먼저 확인하겠습니다.
현재 작업 기준으로 필요한 파일 목록을 요청드릴게요.
```

ChatGPT는 DTO 필드명, API 주소, Entity 컬럼명, Repository 메서드명을 추측해서 구현하지 않는다.

---

## 5. 기능 완료 후 자동 갱신 규칙

기능 작업이 끝나면 사용자가 따로 요청하지 않아도 마지막 정리 단계에서 다음을 제공한다.

1. 변경 내용이 반영된 최종 요구사항 md 문서
2. 생성/수정 파일 기준 기능별 커밋 메시지
3. GitHub PR 제목과 내용
4. 다음 작업 GitHub 이슈 제목과 10줄 이내 내용
5. 다음 기능을 새 채팅에서 시작할 수 있는 프롬프트
6. 다음 작업 기준으로 갱신된 `00_project_context_for_chatgpt.md` 내용

특히 `00_project_context_for_chatgpt.md`는 다음 채팅에서 바로 이어서 작업할 수 있도록 항상 최신 상태로 다시 제안한다.

다음 내용이 바뀌면 반드시 `00_project_context_for_chatgpt.md`에 반영한다.

- 현재까지 완료된 기능
- 현재 중단된 기능
- 다음 작업명
- 새로 확정된 백엔드 정책
- 새로 확정된 프론트 정책
- API 주소 변경
- DB 테이블/컬럼 변경
- 영상 저장 정책 변경
- 반복 발생한 에러와 예방 규칙
- 다음 채팅에서 반드시 요청해야 할 문서 또는 코드 파일 목록

---

## 6. 다음 채팅 프롬프트 작성 규칙

기능 완료 후 다음 작업용 프롬프트를 작성할 때는 반드시 아래 내용을 포함한다.

- 시작할 기능명
- 참고할 기본 업로드 파일 5개
- 현재까지 완료된 기능
- 현재 중단된 기능
- 사용할 DB 테이블
- 초기 구현 방식
- 권한 정책
- 삭제 정책
- 프론트 구현 여부
- 관련 상세 요구사항 md 파일명
- 다음 채팅에서 ChatGPT가 먼저 확인하거나 요청해야 할 문서/코드 파일 목록

다음 채팅 프롬프트에는 반드시 아래 문장을 포함한다.

```text
코드 작성 전, 현재 작업에 필요한 상세 요구사항 md와 실제 백엔드/프론트 코드 파일이 충분히 제공되었는지 먼저 확인해줘. 부족한 파일이 있으면 바로 구현하지 말고 필요한 파일 목록을 먼저 요청해줘. DTO 필드명, API 주소, Entity 컬럼명, Repository 메서드명은 추측하지 말고 실제 파일 기준으로 맞춰줘.
```

백엔드 작업 프롬프트에는 필요한 경우 다음 파일을 요청하라는 내용을 포함한다.

```text
Controller
Service
Validator
Repository
Entity
Request DTO
Response DTO
Enum
ErrorCode
application.properties
```

프론트 작업 프롬프트에는 필요한 경우 다음 파일을 요청하라는 내용을 포함한다.

```text
페이지 컴포넌트
API 함수 파일
타입 파일
라우트 상수
권한 유틸
axiosInstance
관련 백엔드 Controller/DTO
```

---

## 7. 현재 기술 스택

### Backend

- Java
- Spring Boot
- Gradle
- Spring Security
- JPA
- MySQL
- IntelliJ IDEA

### Frontend

- React
- TypeScript
- Vite
- Axios
- VS Code
- 반응형 UI

### 영상 처리

- 경기 원본 영상 업로드는 실제 `.mp4` 파일 업로드 방식
- 로컬 개발 기준 업로드 경로는 `backend/uploads` 하위 경로 사용
- 영상 길이 추출은 `ffprobe` 기반
- 클립 파일 생성은 `FFmpeg` 기반으로 확장

---

## 8. 사용자 역할

### COACH

지도자는 팀 운영과 영상 분석을 관리한다.

주요 권한은 다음과 같다.

- 스케줄 등록/수정/삭제/조회
- 공지사항 등록/수정/삭제/조회
- 경기 영상 업로드/조회/수정/삭제
- 팀 분석 클립 등록/조회/수정/삭제
- 선수 개인 분석 클립 등록/조회/수정/삭제
- 드로잉 등록/조회/수정/삭제
- 선수 기록 조회/관리

### ANALYST

분석관은 영상 분석 실무를 담당한다.

주요 권한은 다음과 같다.

- 스케줄/공지사항 조회
- 경기 영상 업로드/조회/수정
- 팀 분석 클립 등록/조회/수정
- 선수 개인 분석 클립 등록/조회/수정
- 드로잉 등록/조회/수정
- 기본적으로 삭제 권한 없음

단, 선수 기록 관리 기능에서는 기존 최종 정책상 분석관도 기록 삭제가 가능하다.

### PLAYER

선수는 조회 중심 사용자다.

주요 권한은 다음과 같다.

- 스케줄 조회
- 공지사항 조회
- 경기 원본 영상 조회
- 팀 분석 클립 조회
- 본인 선수 개인 분석 클립 조회
- 본인 선수 개인 분석 클립 드로잉 조회
- 본인 기록 조회

---

## 9. 현재까지 완료된 기능

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

---

## 10. 현재 중단된 기능

현재 중단된 기능은 다음과 같다.

```text
선수 개인 분석 클립 드로잉 프론트 연동
```

중단 이유는 선수 개인 분석 클립 저장 구조를 다시 설계해야 하기 때문이다.

기존에는 선수 개인 분석 클립이 실제 파일 없이 다음 방식으로 재생됐다.

```text
원본 경기 영상 URL + startTimeSec + endTimeSec
```

하지만 실제 운영 기준으로는 선수 개인 분석 클립 생성 시 원본 경기 영상에서 실제 mp4 클립 파일을 생성하고, 이후 재생은 `player_video_clip.url`을 기준으로 해야 한다.

따라서 프론트 작업은 잠시 중단하고 백엔드의 클립 파일 생성 구조를 먼저 구현한다.

---

## 11. 다음 작업

다음 작업명은 다음과 같다.

```text
선수 개인 분석 클립 비동기 파일 생성 구조 구현
```

권장 브랜치명은 다음과 같다.

```text
feature/player-analysis-clip-async-file-generation
```

다음 작업의 핵심 정책은 다음과 같다.

- 선수 개인 분석 클립 생성 시 실제 mp4 클립 파일을 생성한다.
- 클립 파일 생성은 비동기 작업 큐 방식으로 처리한다.
- 생성 요청 시 `player_video_clip.status = PROCESSING`으로 먼저 저장한다.
- 백그라운드 작업에서 FFmpeg로 원본 경기 영상 구간을 잘라낸다.
- 성공 시 `player_video_clip.url`에 생성된 클립 파일 URL을 저장하고 `status = READY`로 변경한다.
- 실패 시 `status = FAILED`로 변경한다.
- 클립 생성 요청에는 드로잉 JSON 목록을 함께 받을 수 있다.
- 드로잉은 영상 파일에 합성하지 않고 DB JSON으로 저장한다.
- 드로잉 시간 기준은 원본 영상 기준이 아니라 생성된 클립 영상 기준 초로 저장한다.
- 원본 경기 영상이 소프트 삭제되어도 이미 생성된 선수 개인 분석 클립은 독립 재생 가능해야 한다.
- 삭제된 원본 경기 영상으로는 새 클립을 생성할 수 없다.

다음 작업에서 우선 요청해야 할 파일은 다음과 같다.

```text
backend/src/main/java/com/soccer/platform/controller/PlayerAnalysisClipController.java
backend/src/main/java/com/soccer/platform/service/playerclip/PlayerAnalysisClipService.java
backend/src/main/java/com/soccer/platform/service/playerclip/PlayerAnalysisClipValidator.java
backend/src/main/java/com/soccer/platform/repository/PlayerVideoClipRepository.java
backend/src/main/java/com/soccer/platform/entity/PlayerVideoClipEntity.java
backend/src/main/java/com/soccer/platform/entity/PlayerVideoClipDrawingEntity.java
backend/src/main/java/com/soccer/platform/entity/GameVideoUploadEntity.java
backend/src/main/java/com/soccer/platform/common/constants/VideoUploadStatusEnum.java
backend/src/main/java/com/soccer/platform/common/exception/ErrorCode.java
backend/src/main/resources/application.properties
```

필요하면 이후 다음 파일도 요청한다.

```text
CreatePlayerAnalysisClipRequestDTO.java
CreatePlayerAnalysisClipResponseDTO.java
PlayerAnalysisClipDetailResponseDTO.java
CreatePlayerClipDrawingRequestDTO.java
PlayerClipDrawingController.java
PlayerClipDrawingService.java
PlayerClipDrawingValidator.java
```

---

## 12. 기능 개발 규칙

새 기능을 시작할 때는 바로 코드부터 작성하지 않는다.

순서는 다음을 따른다.

1. 기본 업로드 파일 5개 확인
2. 관련 상세 요구사항 md 문서 확인
3. 현재 작업에 필요한 코드 파일이 충분한지 확인
4. 부족한 문서나 코드 파일 요청
5. Controller, DTO, Entity, Repository, Service 실제 코드 확인
6. 요구사항 문서 작성 또는 갱신
7. GitHub 이슈 제목과 내용 작성
8. 구현 순서 제안
9. 사용자가 진행하면 코드 구현

DTO 필드명, API 주소, Entity 컬럼명, Repository 메서드명은 임의로 확정하지 않는다.

확실하지 않으면 사용자에게 실제 파일을 요청한다.

---

## 13. 코드 제공 규칙

백엔드 코드는 가독성을 우선한다.

클래스와 메서드 이름은 역할이 드러나게 작성한다.

프론트 작업 중 에러가 나거나 수정이 필요한 경우에는 수정된 파일의 전체 코드를 제공한다.

프론트 파일 수정 시 다음을 지킨다.

- 사용하지 않는 변수 생성 금지
- React `useEffect` 안에서 동기적인 `setState` 반복 금지
- effect 내부 async 함수와 `ignore` 또는 `isMounted` 플래그 사용
- API 응답 구조는 백엔드 DTO 확인 후 반영
- 에러 메시지는 사용자에게 필요한 message 중심으로 표시

---

## 14. 상세 문서 보관 기준

상세 요구사항 문서는 기능별로 계속 분리해서 GitHub `docs/`에 유지한다.

단, ChatGPT 프로젝트 소스에는 모든 상세 문서를 올리지 않는다.

ChatGPT 프로젝트 소스에는 기본 5개 파일만 상시 유지하고, 기능별 상세 문서는 현재 작업에 필요한 것만 임시로 업로드한다.

ChatGPT 프로젝트 업로드 한도에 도달하면 다음 순서로 정리한다.

1. 오래된 기능별 상세 md를 ChatGPT 프로젝트 소스에서 제거한다.
2. 현재 작업과 무관한 코드 파일을 ChatGPT 프로젝트 소스에서 제거한다.
3. 기본 5개 파일은 유지한다.
4. 현재 작업에 필요한 상세 md와 코드 파일만 추가한다.

모든 md를 하나로 합치지 않는다.

GitHub에는 기능별 상세 md를 계속 유지하고, ChatGPT에는 요약 문서와 현재 작업에 필요한 문서만 올린다.
