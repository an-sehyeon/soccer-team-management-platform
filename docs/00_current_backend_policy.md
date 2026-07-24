# 00. 현재 백엔드 정책

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 최신 백엔드 구현 상태와 확정된 목표 정책을 요약한다.

새 채팅에서 백엔드 작업을 시작할 때 다음 순서로 참고한다.

1. 현재 채팅의 최신 사용자 결정
2. `docs/31_video_editor_v1_requirements.md`
3. 이 문서
4. 기능별 요구사항 문서
5. 실제 GitHub `main` 소스와 `soccer_platform.sql`

현재 로컬 소스와 GitHub `main`은 `video-editor-backend-foundation` 브랜치 생성 시점에 동일하다. 실제 클래스명, API 주소, DTO 필드명, Entity 필드명과 Repository 메서드는 GitHub `main` 소스를 직접 확인해 사용한다.

경기 영상 편집기 v1의 최신 상세 요구사항은 다음 문서를 기준으로 한다.

```text
docs/31_video_editor_v1_requirements.md
```

선수 기록과 북마크 최신 상세 문서는 다음과 같다.

```text
docs/15_player_record_requirements_final.md
docs/29_match_video_bookmark_requirements.md
```

---

## 2. 현재 작업 상태

현재 작업은 경기 영상 편집기 전체 구현이 아니라 백엔드 기반 구조 개편이다.

```text
작업명: 경기 영상 편집기 v1 백엔드 기반 구조 개편
브랜치: video-editor-backend-foundation
상태: 요구사항 확정 완료, 구현 전
```

이번 브랜치의 우선 범위는 다음과 같다.

- 기존 경기 영상·팀 클립·선수 클립 Entity와 Enum 분석
- DRAFT, QUEUED 상태를 수용할 상태 모델 개편
- 초 단위 클립 시간을 밀리초 단위로 전환할 DB 방향 확정
- 원본 영상 FPS·해상도·오디오 메타데이터 확장
- 렌더링 요청·시작·완료 시각과 실패 사유 기반 추가
- COACH·ANALYST 영상 도메인 권한 동일화
- PLAYER의 READY 전용 조회 보장
- 기존 API와 기존 데이터의 호환·마이그레이션 방향 확정

이번 브랜치에서 자동 저장, 잠금, 스냅샷, 렌더링 실행 전체, 인증 스트리밍 전체와 프론트 편집기를 한 번에 구현하지 않는다.

---

## 3. 기술 스택

- Java
- Spring Boot
- Gradle
- Spring Security
- JPA
- MySQL
- JWT 인증
- 로컬 파일 저장소
- ffprobe
- FFmpeg
- 현재 Spring `@Async` 기반 비동기 처리

현재 비동기 생성 구조는 후속 공용 렌더링 대기열로 개편할 대상이다. 기존 구현을 확인하지 않고 즉시 제거하지 않는다.

---

## 4. 인증과 권한 검증 원칙

- 주요 API는 인증된 사용자만 접근한다.
- 회원가입과 로그인 API만 공개한다.
- 권한 검증은 반드시 백엔드에서 수행한다.
- 프론트 버튼 숨김은 보안 기준이 아니다.
- `isAdmin = true`만으로 영상 또는 선수 기록 관리 권한을 부여하지 않는다.
- 선수 본인 조회는 로그인한 `memberId`와 대상 선수의 회원 ID를 검증한다.
- 파일 URL 또는 스트리밍 식별자를 직접 입력해도 권한 검증을 통과해야 한다.

---

## 5. 역할 정책

### COACH

- 스케줄 등록·조회·수정·삭제
- 공지사항 등록·조회·수정·삭제
- 경기 원본 영상 등록·조회·수정·삭제·복구
- 팀 분석 클립 등록·조회·수정·삭제
- 선수 개인 분석 클립 등록·조회·수정·삭제
- 드로잉·텍스트·정지·줌 편집
- DRAFT, 스냅샷, 편집 잠금 관리
- 개별·일괄 렌더링과 FAILED 재시도
- 편집 잠금 강제 해제
- 선수 기록 등록·갱신과 이벤트 연결
- 전체 선수 기록과 이벤트 조회
- 본인 북마크 관리

### ANALYST

스케줄과 공지사항은 조회만 가능하다.

영상 도메인에서는 COACH와 동일한 전체 권한을 가진다.

- 경기 원본 영상 등록·조회·수정·삭제·복구
- 팀 분석 클립 등록·조회·수정·삭제
- 선수 개인 분석 클립 등록·조회·수정·삭제
- 드로잉·텍스트·정지·줌 편집
- DRAFT, 스냅샷, 편집 잠금 관리
- 개별·일괄 렌더링과 FAILED 재시도
- 편집 잠금 강제 해제
- 선수 기록 등록·갱신과 이벤트 연결
- 전체 선수 기록과 이벤트 조회
- 본인 북마크 관리

기존 문서의 “ANALYST는 영상과 클립 삭제 불가” 정책은 경기 영상 편집기 v1에서 폐기한다.

### PLAYER

- 스케줄 조회
- 공지사항 조회
- 접근 가능한 경기 원본 영상 조회
- READY 팀 분석 클립 조회
- 본인의 READY 선수 개인 분석 클립 조회
- 본인 선수 기록과 이벤트 조회
- 본인 선수 개인 분석 클립 조회 기록 생성·갱신

다음 상태의 클립은 조회할 수 없다.

```text
DRAFT
QUEUED
PROCESSING
FAILED
```

영상 편집, 렌더링, 북마크 관리와 선수 기록 쓰기 API에는 접근할 수 없다.

---

## 6. API 주소 정책

역할과 목적이 주소에 드러나야 한다.

```text
공통 조회: /api/{resources}
관리용: /api/management/{resources}
선수 본인: /api/player/me/{resources}
관리자: /api/admin/{resources}
```

기존 영상 삭제 API에 `/api/coach/**` 주소가 남아 있을 수 있다. 이번 기반 구조 작업에서는 실제 Controller, SecurityConfig와 프론트 호출부를 확인한 뒤 다음 중 하나를 선택한다.

1. `/api/management/**`로 이동하고 COACH·ANALYST를 허용
2. 기존 주소를 임시 유지하되 서비스 권한을 COACH·ANALYST로 확장

주소를 확인하기 전에 임의로 변경하지 않는다. 신규 영상 관리 API는 가능하면 `/api/management/**`를 사용한다.

---

## 7. DB 기본 정책

현재 단일 팀 서비스이므로 별도 `team` 테이블을 사용하지 않는다.

주요 테이블은 다음과 같다.

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

현재 SQL과 Entity가 기준이며 정확한 신규 컬럼명은 실제 소스 검토 후 확정한다.

경기 영상 편집기 v1에서 검토할 주요 변경은 다음과 같다.

### `game_video_upload`

- 삭제 시각
- 영상 길이 밀리초
- 가로·세로 해상도
- 정확한 FPS 정보
- 오디오 존재 여부
- 필요한 경우 회전 정보

### `team_video_clip`, `player_video_clip`

- DRAFT, QUEUED 상태
- 시작·종료 밀리초
- 렌더링 요청·시작·완료 시각
- 렌더링 실패 사유
- 결과 파일 메타데이터
- 썸네일 정보
- 렌더링 프로필 버전
- 편집 버전

기존 초 단위 데이터의 기본 마이그레이션 원칙은 `기존 초 × 1000 = 밀리초`다.

---

## 8. 경기 원본 영상 정책

- 실제 MP4 파일을 업로드한다.
- DB에는 파일 자체가 아니라 저장 식별자, 경로와 메타데이터를 저장한다.
- ffprobe로 길이, 해상도, FPS, 오디오 정보를 추출한다.
- 신규 편집 데이터의 기준 시간은 밀리초다.
- 원본 영상은 팀·선수 클립, 선수 기록, 북마크와 편집기의 기준 데이터다.

### 삭제

원본 영상 삭제는 소프트 삭제한다.

```text
is_deleted = true
deleted_at = 현재 시각
```

- 원본 MP4는 10일간 유지한다.
- 10일 이내이며 실제 파일이 존재할 때만 복구한다.
- 원본 북마크는 삭제 후 복구하지 않는다.
- 연결된 팀 분석 클립은 소프트 삭제한다.
- 팀 클립 MP4는 즉시 물리 삭제 대상으로 등록한다.
- READY 선수 개인 클립은 유지한다.
- 10일 후 원본 MP4와 연결된 DRAFT·FAILED 선수 개인 클립을 영구 삭제한다.
- QUEUED 또는 PROCESSING 클립이 있으면 원본 삭제를 차단한다.
- 삭제 이력 메타데이터는 선수 클립과 기록의 출처 확인을 위해 유지한다.

---

## 9. 분석 클립 상태 정책

경기 영상 편집기 v1의 canonical 상태는 다음 다섯 개다.

```text
DRAFT
QUEUED
PROCESSING
READY
FAILED
```

상태 흐름:

```text
DRAFT → QUEUED → PROCESSING → READY
DRAFT → QUEUED → PROCESSING → FAILED
FAILED → QUEUED → PROCESSING → READY 또는 FAILED
```

- 신규 클립은 DRAFT로 저장한다.
- QUEUED와 PROCESSING에서는 편집·삭제·재시도를 차단한다.
- READY는 완성 MP4를 재생한다.
- READY에서 직접 수정 가능한 항목은 제목, 클립 유형, 코멘트다.
- 구간, 대상 선수, 드로잉, 정지와 줌을 변경하려면 새 DRAFT를 생성한다.
- FAILED는 편집 원본을 유지하고 재편집·재렌더링·삭제를 허용한다.
- 기존 `UPLOADING` 상태의 실제 사용 여부를 확인한 뒤 제거 또는 변환한다.

일반 PLAYER 조회 Repository 또는 Service는 READY 조건을 반드시 포함한다.

---

## 10. 편집 데이터 정책

- 하나의 클립은 하나의 연속 구간만 가진다.
- 원본 구간 최소 1초, 최종 결과 최대 15분이다.
- 클립, 드로잉과 효과 시간은 밀리초로 관리한다.
- 드로잉 좌표는 레터박스를 제외한 영상 표시 영역 기준 정규화 좌표로 저장한다.
- 드로잉, 텍스트, 화면 정지와 줌은 READY MP4에 합성한다.
- READY 이후에도 최종 편집 원본 데이터는 보관한다.
- READY 완료 후 복구 스냅샷과 임시 파일은 정리한다.
- 영상 내용을 변경할 때 기존 READY 행을 직접 덮어쓰지 않는다.

---

## 11. 렌더링 정책

팀 클립과 선수 클립은 하나의 공용 FIFO 대기열을 사용한다.

- 동시 실행 기본값: 2
- 대기 기준: 렌더링 요청 시각 오름차순
- 일괄 요청: DRAFT·FAILED 최대 20개
- 일괄 요청 내부 정렬: `created_at ASC`, 동일하면 클립 ID ASC
- 각 클립은 독립적으로 성공·실패한다.
- 실제 PROCESSING 시작 후 60분 타임아웃
- 자동 재시도 없음

실패 사유:

```text
RENDER_ERROR
TIMEOUT
SERVER_RESTART
```

서버 시작 시 남은 PROCESSING은 SERVER_RESTART 사유로 FAILED 처리하고, QUEUED는 요청 순서대로 재등록한다.

---

## 12. 결과 영상 규격

```text
컨테이너: MP4
영상: H.264(libx264)
오디오: AAC
픽셀 포맷: yuv420p
화질: CRF 21
preset: medium
최대 해상도: 1080p
최대 FPS: 60
faststart: 적용
키프레임 간격: 약 2초
```

- 원본보다 해상도와 FPS를 높이지 않는다.
- 화면 비율을 유지한다.
- 원본에 오디오가 없으면 무음 트랙을 만들지 않는다.
- 정지 구간의 오디오는 무음 처리한다.
- 설정값은 외부 설정으로 관리한다.
- READY 전 ffprobe 기반 결과 파일을 검증한다.
- 썸네일은 결과 1초 지점에서 JPEG로 생성하고 실패해도 MP4가 정상이라면 READY를 유지한다.

---

## 13. 영상 제공 보안 정책

현재 `/uploads/**` 직접 접근은 기존 MVP 호환을 위한 임시 구조다.

목표 정책:

- 운영 환경에서 `/uploads/**`를 `permitAll`로 공개하지 않는다.
- 백엔드 인증 스트리밍 API 또는 짧은 만료 재생 토큰을 사용한다.
- HTTP Range 요청을 지원한다.
- 팀 클립은 팀 사용자 권한을 검증한다.
- 선수 개인 클립은 대상 선수 본인 또는 COACH·ANALYST인지 검증한다.
- 내부 파일 경로를 API 응답에 노출하지 않는다.

클라우드 스토리지와 CDN 도입 시 Signed URL 방식으로 교체할 수 있게 저장소 계층을 분리한다.

---

## 14. 클립 삭제 정책

DRAFT, FAILED, READY 클립은 즉시 영구 삭제한다.

```text
이 클립을 영구 삭제하시겠습니까?

삭제하면 클립 영상과 모든 관련 데이터가 즉시 영구 삭제되며 복구할 수 없습니다.
```

삭제 대상:

- 클립 DB 행
- MP4와 썸네일
- 최종 편집 데이터
- 드로잉·텍스트·정지·줌
- 북마크와 조회 기록
- 선수 기록 이벤트와 클립 연결
- 스냅샷과 임시 파일

유지 대상:

- 선수 기록 이벤트 자체
- 선수 경기 기록
- 선수 누적 통계

DB 트랜잭션에서 파일 정리 작업을 기록하고 커밋 후 삭제를 시도한다. 실패한 파일 삭제는 재시도하고 로그를 남긴다.

---

## 15. 선수 기록 연결 정책

최신 상세 정책은 `docs/15_player_record_requirements_final.md`를 기준으로 한다.

- 선수 기록 이벤트에는 READY 클립만 연결한다.
- 클립 연결 당시 구간을 이벤트 스냅샷으로 저장한다.
- 신규 canonical 시간은 밀리초로 전환한다.
- 기존 초 단위 값은 마이그레이션 시 1000을 곱해 보존한다.
- 직접 클립 영구 삭제 시 `player_record_event_clip` 연결을 삭제한다.
- 이벤트와 선수 기록·누적 통계는 유지한다.
- 원본 삭제로 팀 클립이 소프트 삭제된 경우 연결 이력은 유지하고 `해당 클립이 삭제되었습니다`를 표시한다.
- 클립 삭제로 선수 기록 수치를 자동 차감하지 않는다.

---

## 16. 북마크 정책

최신 상세 정책은 `docs/29_match_video_bookmark_requirements.md`를 기준으로 한다.

- COACH·ANALYST 본인 북마크만 관리한다.
- PLAYER는 접근할 수 없다.
- READY 영상만 북마크할 수 있다.
- 경기 영상 편집기 v1에서는 북마크 시간도 밀리초 canonical 저장을 사용한다.
- 기존 초 단위 값은 1000을 곱해 마이그레이션한다.
- 직접 클립 영구 삭제 시 관련 북마크를 영구 삭제한다.
- 원본 삭제 시 원본 북마크는 소프트 삭제하고 복구하지 않는다.
- 원본 삭제로 제거되는 팀 클립 북마크도 복구하지 않는다.

---

## 17. 구현 순서

`video-editor-backend-foundation` 브랜치에서는 다음 순서로 진행한다.

1. GitHub `main`의 Entity와 Enum 확인
2. Repository와 `soccer_platform.sql` 확인
3. Service와 Validator의 상태·권한 흐름 확인
4. Controller와 DTO 호환성 확인
5. FFmpeg, 저장소와 비동기 처리 구조 확인
6. SecurityConfig와 ErrorCode 확인
7. 현재 구조와 31번 요구사항 충돌 목록 작성
8. DB·Entity 변경안과 마이그레이션 순서 확정
9. 사용자가 진행한다고 하면 코드 구현
10. 기존 API 회귀 테스트

기존 API를 즉시 제거하지 않는다. 신규 구조를 추가한 뒤 프론트 전환이 끝날 때까지 필요한 호환성을 유지한다.

---

## 18. 완료 후 갱신 대상

구현 결과가 확정되면 다음을 실제 코드 기준으로 다시 갱신한다.

- 이 문서
- `00_current_frontend_policy.md`
- `00_project_context_for_chatgpt.md`
- `docs/31_video_editor_v1_requirements.md`
- `soccer_platform.sql`
- 변경된 기능별 요구사항 문서
