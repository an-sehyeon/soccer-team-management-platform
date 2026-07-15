# 00. 현재 백엔드 정책

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 최신 백엔드 정책을 요약한다.

새 채팅에서 백엔드 기능을 이어서 구현할 때 이 문서를 우선 참고한다.

상세 정책은 기능별 요구사항 문서와 실제 소스 코드를 기준으로 확인한다.

선수 기록 관련 최신 상세 문서는 다음과 같다.

```text
docs/15_player_record_requirements_final.md
```

경기 영상 북마크 관련 최신 상세 문서는 다음과 같다.

```text
docs/29_match_video_bookmark_requirements.md
```

---

## 2. 기술 스택

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

---

## 3. 인증 정책

- 회원가입과 로그인 기능은 구현되어 있다.
- 로그인 성공 시 JWT Access Token을 발급한다.
- 인증 후 `CustomUserPrincipal`에서 다음 값을 사용한다.

```text
memberId
memberRole
isAdmin
```

- 주요 API는 인증된 사용자만 접근할 수 있다.
- 회원가입과 로그인 API만 `permitAll` 대상이다.
- 권한 검증은 프론트가 아니라 반드시 백엔드에서 처리한다.

---

## 4. 역할 정책

### COACH

- 스케줄 등록·조회·수정·삭제
- 공지사항 등록·조회·수정·삭제
- 경기 영상 업로드·조회·수정·삭제
- 팀 분석 클립 등록·조회·수정·삭제
- 선수 개인 분석 클립 등록·조회·수정·삭제
- 드로잉 등록·조회·수정·삭제
- 선수 개인 분석 클립 조회 기록 확인
- 경기 영상 기준 선수 기록 등록·갱신
- 분석 클립 연결 선수 기록 등록
- 전체 선수 기록과 이벤트 조회
- 본인 영상 북마크 등록·조회·수정·삭제

### ANALYST

- 스케줄과 공지사항 조회
- 경기 영상 업로드·조회·수정
- 팀 분석 클립 등록·조회·수정
- 선수 개인 분석 클립 등록·조회·수정
- 드로잉 등록·조회·수정
- 선수 개인 분석 클립 조회 기록 확인
- 경기 영상 기준 선수 기록 등록·갱신
- 분석 클립 연결 선수 기록 등록
- 전체 선수 기록과 이벤트 조회
- 본인 영상 북마크 등록·조회·수정·삭제

영상과 분석 클립의 삭제 권한은 기본적으로 없다.

북마크는 개인 임시 작업 데이터이므로 본인 북마크 삭제를 허용한다.

### PLAYER

- 스케줄 조회
- 공지사항 조회
- 경기 원본 영상 조회
- READY 팀 분석 클립 조회
- 본인 선수 개인 분석 클립 조회
- 본인 드로잉 조회
- 본인 선수 개인 분석 클립 조회 기록 생성·갱신
- 본인 선수 기록과 이벤트 조회

선수 기록 등록과 북마크 관리 API에는 접근할 수 없다.

---

## 5. 권한 검증 원칙

- 권한 검증은 Service 또는 Validator 계층에서 처리한다.
- 프론트에서 버튼을 숨겨도 백엔드 검증은 유지한다.
- `isAdmin = true`만으로 영상, 클립, 선수 기록 관리 권한을 부여하지 않는다.
- 선수 본인 조회는 로그인한 `memberId`를 기준으로 제한한다.
- 사용자 소유 데이터는 로그인 사용자 ID를 함께 검증한다.

---

## 6. API 주소 정책

역할과 목적이 API 주소에 드러나야 한다.

```text
공통 조회: /api/{resources}
관리용: /api/management/{resources}
지도자 전용: /api/coach/{resources}
선수 본인: /api/player/me/{resources}
관리자: /api/admin/{resources}
```

주요 예시는 다음과 같다.

```http
GET    /api/match-videos
POST   /api/management/match-videos
DELETE /api/coach/match-videos/{matchVideoId}

GET    /api/team-analysis-clips
POST   /api/management/team-analysis-clips/with-drawings
PUT    /api/management/team-analysis-clips/{teamClipId}/with-drawings
DELETE /api/coach/team-analysis-clips/{teamClipId}

GET    /api/management/player-analysis-clips
GET    /api/player/me/player-analysis-clips
DELETE /api/coach/player-analysis-clips/{playerClipId}

GET    /api/management/player-records
GET    /api/management/player-records/{recordId}
POST   /api/management/player-records
PATCH  /api/management/player-records/{recordId}
POST   /api/management/player-record-events/with-clip-link

POST   /api/management/video-bookmarks
GET    /api/management/video-bookmarks
PATCH  /api/management/video-bookmarks/{bookmarkId}
DELETE /api/management/video-bookmarks/{bookmarkId}
```

새 API를 만들기 전 기존 Controller와 주소 체계를 먼저 확인한다.

---

## 7. DB 기본 정책

현재 서비스는 단일 팀 기준이다.

초기 구조에서는 별도 `team` 테이블을 사용하지 않는다.

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

삭제는 기본적으로 소프트 삭제를 사용한다.

```text
is_deleted = false
→ 활성 데이터

is_deleted = true
→ 삭제 데이터
```

일반 목록과 상세 조회에서는 삭제 데이터를 제외한다.

---

## 8. 경기 영상 정책

- 경기 원본 영상은 실제 `.mp4` 업로드 방식으로 관리한다.
- 로컬 저장 경로는 `backend/uploads/match-videos`를 사용한다.
- DB에는 파일 자체가 아니라 URL과 메타데이터를 저장한다.
- 영상 길이는 `ffprobe`로 추출한다.
- 영상 길이는 `game_video_upload.duration_sec`에 저장한다.
- 경기 영상 삭제는 소프트 삭제다.
- 초기 MVP에서는 실제 영상 파일을 바로 삭제하지 않는다.

경기 영상은 다음 기능의 기준 데이터다.

- 팀 분석 클립 생성
- 선수 개인 분석 클립 생성
- 선수 기록 등록
- 분석 클립 연결 기록 검증
- 영상 북마크
- 영상 편집기

---

## 9. 팀 분석 클립 정책

- 팀 분석 클립은 실제 mp4 파일을 비동기로 생성한다.
- 생성 직후 `PROCESSING` 상태로 저장한다.
- FFmpeg 성공 시 URL을 저장하고 `READY`로 변경한다.
- 실패 시 `FAILED`로 변경한다.
- 일반 목록 조회는 `READY`만 반환한다.
- 팀 분석 클립과 드로잉은 통합 생성·수정 API를 사용한다.
- 시작·종료 시간이 변경되거나 삭제되면 해당 클립의 활성 북마크를 일괄 소프트 삭제한다.
- 제목, 코멘트, 유형, 드로잉만 변경된 경우 북마크를 유지한다.

---

## 10. 선수 개인 분석 클립 정책

- 선수 개인 분석 클립은 실제 mp4 파일을 비동기로 생성한다.
- 생성 직후 `PROCESSING` 상태로 저장한다.
- FFmpeg 성공 시 URL을 저장하고 `READY`로 변경한다.
- 실패 시 `FAILED`로 변경한다.
- 선수는 본인 클립만 조회할 수 있다.
- 관리자는 관리용 API로 전체 선수 클립을 조회한다.
- 선수 개인 분석 클립과 드로잉은 통합 생성·수정 API를 사용한다.
- 시작·종료 시간, 원본 경기 영상이 변경되거나 삭제되면 해당 클립의 활성 북마크를 일괄 소프트 삭제한다.
- 제목, 코멘트, 유형, 대상 선수, 드로잉만 변경된 경우 북마크를 유지한다.

---

## 11. 영상 북마크 최신 정책

북마크는 `COACH`, `ANALYST` 개인의 임시 분석 작업 데이터다.

- `PLAYER` 접근 불가
- 사용자 본인 북마크만 조회·수정·삭제
- 같은 시간 중복 북마크 허용
- 정수 초 저장
- 제목 필수, 메모 선택
- 경기 원본 영상, 팀 분석 클립, 선수 개인 분석 클립 지원
- `READY` 소스만 사용
- 삭제는 소프트 삭제

소스 조합은 다음과 같다.

```text
원본 영상
upload_id 있음, 두 clip_id null

팀 분석 클립
upload_id 있음, team_clip_id 있음, player_clip_id null

선수 개인 분석 클립
upload_id 있음, team_clip_id null, player_clip_id 있음
```

---

## 12. 선수 기록 최신 정책

### 12.1 화면 책임

- `PlayerRecordPage`는 검색·목록·상세 조회 전용이다.
- 선수 기록 등록과 갱신은 `MatchVideoPage`에서만 진행한다.

### 12.2 클립 없이 등록

- 현재 경기와 선수 기준 활성 `player_record`를 조회한다.
- 기존 기록이 있으면 기존 값을 갱신한다.
- 기존 기록이 없으면 모든 수치 0으로 생성한다.
- 한 요청으로 전체 요약 기록을 저장한다.
- `player_record_event`, `player_record_event_clip`은 생성하지 않는다.

### 12.3 클립 연결 등록

- 팀 분석 클립 또는 선수 개인 분석 클립 하나를 선택한다.
- 한 요청에서 이벤트 유형 하나만 등록한다.
- 같은 클립에 다른 유형을 별도 등록할 수 있다.
- 같은 클립에 같은 유형을 다시 등록할 수 없다.
- 중복 시 저장하지 않고 충돌 응답을 반환한다.
- `value`는 백엔드에서 1로 고정한다.
- 이벤트 시작·종료 시간은 선택한 클립에서 자동 추출한다.
- 선택 이벤트 유형에 대응하는 `player_record` 값이 1 증가한다.
- 기존 `player_record`가 없으면 0으로 자동 생성한다.

### 12.4 이벤트 스냅샷

다음 DB 컬럼을 유지한다.

```text
player_record_event.event_start_time_sec
player_record_event.event_end_time_sec
player_record_event.value
```

프론트 요청에서는 받지 않는다.

클립 시간이 수정되어도 기존 이벤트 스냅샷은 유지한다.

---

## 13. 선수 기록 이벤트 중복 정책

중복 기준은 다음과 같다.

```text
clip_source_type
+ 실제 clip_id
+ event_type
+ 활성 이벤트
+ 활성 연결
```

허용 예시는 다음과 같다.

```text
클립 10 + SHOT
클립 10 + PASS
```

차단 예시는 다음과 같다.

```text
클립 10 + SHOT
클립 10 + SHOT
```

사용자 메시지는 다음 취지를 사용한다.

```text
선택한 클립에는 해당 선수 기록 유형이 이미 연결되어 있습니다.
```

동시 요청 중복 방지를 트랜잭션 수준에서 고려한다.

---

## 14. 선수 기록 요약 반영 정책

클립 연결 이벤트 등록 시 기본 반영은 다음과 같다.

```text
GOAL → goals +1
ASSIST → assists +1
SHOT → shots +1
SHOT_ON_TARGET → shots +1, shots_on_target +1
PASS → passes +1
SUCCESSFUL_PASS → passes +1, successful_passes +1
DRIBBLE → dribbles +1
SUCCESSFUL_DRIBBLE → dribbles +1, successful_dribbles +1
TACKLE → tackles +1
INTERCEPTION → interceptions +1
CLEARANCE → clearances +1
SAVE → saves +1
YELLOW_CARD → yellow_cards +1
RED_CARD → red_cards +1
ETC → 요약 수치 변경 없음
```

---

## 15. 조회 기록 정책

선수가 본인 선수 개인 분석 클립 상세를 조회하면 조회 기록을 생성하거나 갱신한다.

```text
최초 조회
firstViewedAt 저장
lastViewedAt 저장
viewCount = 1

재조회
lastViewedAt 갱신
viewCount + 1
```

관리용 상세 조회는 선수 조회 기록에 반영하지 않는다.

---

## 16. 영상 파일 접근 정책

현재 MVP는 `/uploads/**` 직접 접근을 사용한다.

운영 전에는 다음 중 하나로 전환해야 한다.

- 권한 검증 스트리밍 API
- Signed URL
- CDN과 서명 URL

개인 분석 클립은 반드시 백엔드 권한 검증을 거쳐야 한다.

---

## 17. 예외 처리 정책

- 공통 예외는 `ErrorCode`와 `CustomException` 구조를 사용한다.
- 사용자 메시지는 구체적이어야 한다.
- HTTP status, path, 내부 스택은 프론트에 그대로 노출하지 않는다.
- 검증 실패는 저장 전에 처리한다.
- 연관 데이터 저장은 하나의 트랜잭션으로 묶는다.

선수 기록 개편에서 추가해야 할 주요 예외는 다음 범주다.

- 잘못된 클립 연결 요청
- 경기 영상과 클립 불일치
- 개인 클립 대상 선수 불일치
- 같은 클립과 같은 이벤트 유형 중복
- `READY`가 아닌 클립

정확한 ErrorCode 이름은 기존 프로젝트 네이밍을 확인한 후 확정한다.

---

## 18. 현재 다음 백엔드 작업

다음 작업은 경기 영상 기반 선수 기록 등록 및 클립 연결 API 구조 개편이다.

핵심 작업은 다음과 같다.

1. 클립 연결 요청 DTO에서 시간과 `value` 제거
2. 클립 시간 자동 조회
3. `value = 1` 고정
4. 동일 클립·동일 이벤트 유형 중복 차단
5. 기존 기록이 없을 때 자동 생성
6. 기존 기록이 있을 때 요약 수치 갱신
7. 기존 조회 API 회귀 테스트

권장 브랜치는 다음과 같다.

```text
feature/player-record-registration-redesign-api
```
