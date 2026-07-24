# 경기 영상 업로드 기능 요구사항

> [!IMPORTANT]
> 이 문서는 해당 기능의 기존 구현 당시 기준과 작업 이력을 보존하는 문서다.
> 경기 영상 편집기 v1의 신규 구현은 `docs/31_video_editor_v1_requirements.md`,
> `docs/00_current_backend_policy.md`, `docs/00_current_frontend_policy.md`를 우선한다.
> 역할 권한, 클립 상태, 시간 단위, 삭제, 드로잉 합성, 영상 제공 방식이 충돌하면 최신 문서를 적용한다.
> 기존 API는 프론트 전환 전 호환 목적으로 유지될 수 있으므로 실제 제거 여부는 현재 소스를 확인한다.


## 1. 결론

경기 영상 업로드 기능은 지도자와 분석관이 실제 원본 경기 영상 파일과 경기 정보를 등록하고, 지도자/분석관/선수가 등록된 원본 경기 영상을 조회하고 재생할 수 있게 하는 기능이다.

이번 구현 기준은 다음과 같다.

* 인증은 이미 구현된 JWT 인증 구조를 사용한다.
* JWT 인증 후 `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 값을 사용한다.
* 지도자 `COACH`는 경기 영상 업로드, 조회, 수정, 삭제가 가능하다.
* 분석관 `ANALYST`는 경기 영상 업로드, 조회, 수정이 가능하다.
* 분석관 `ANALYST`는 경기 영상 삭제가 불가능하다.
* 선수 `PLAYER`는 경기 원본 영상 목록 조회와 상세 조회가 가능하다.
* 선수 `PLAYER`는 경기 영상 업로드, 수정, 삭제가 불가능하다.
* 관리자 여부 `isAdmin`은 경기 영상 권한의 기본 기준으로 사용하지 않는다.
* 삭제는 실제 DB 삭제가 아니라 `is_deleted = true`로 처리하는 소프트 삭제를 사용한다.
* 실제 영상 파일은 초기 MVP에서 삭제하지 않는다.
* 초기 버전은 단일 팀 서비스 기준으로 설계한다.
* 현재 DB의 `game_video_upload` 테이블을 사용한다.
* 경기 영상 등록은 기존 URL 입력 방식이 아니라 실제 `.mp4` 파일 업로드 방식으로 구현한다.
* 업로드된 파일은 초기 MVP에서 로컬 서버의 `backend/uploads/match-videos` 경로에 저장한다.
* DB의 `url` 컬럼에는 `/uploads/match-videos/{storedFileName}` 형태의 접근 URL을 저장한다.
* DB의 `duration_sec` 컬럼에는 업로드된 영상의 실제 길이를 초 단위로 저장한다.
* 영상 길이 추출은 `ffprobe` 기반으로 처리한다.
* 영상 파일 저장 로직과 영상 길이 추출 로직은 각각 분리해서 추후 S3, 인코딩, 썸네일 생성으로 확장 가능하게 설계한다.

---

## 2. 기능 목적

경기 영상 업로드 기능의 목적은 경기 원본 영상을 플랫폼에 등록하고, 이후 영상 편집과 분석 클립 생성의 기준 데이터를 만드는 것이다.

이 기능은 단순 영상 목록 관리 기능이 아니라, 이후 다음 기능들의 출발점이다.

* 영상 편집기에서 원본 경기 영상 불러오기
* 팀 분석 클립 생성
* 선수 개인 분석 클립 생성
* 북마크 생성
* 드로잉 데이터 연결
* 경기별 분석 데이터 관리
* 클립 시작/종료 시간 검증
* 프론트 영상 편집 타임라인 표시

따라서 초기 구현에서는 실제 파일 업로드, 재생 가능한 URL 저장, 영상 길이 추출을 안정적으로 처리하는 것을 우선한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

지도자는 팀 운영과 영상 분석 책임자이므로 경기 영상을 관리할 수 있다.

가능한 기능은 다음과 같다.

* 경기 영상 업로드
* 경기 영상 목록 조회
* 경기 영상 상세 조회
* 경기 영상 수정
* 경기 영상 삭제

### 3.2 분석관 `ANALYST`

분석관은 영상 분석 실무를 담당할 수 있으므로 경기 영상 등록과 수정은 허용한다.

가능한 기능은 다음과 같다.

* 경기 영상 업로드
* 경기 영상 목록 조회
* 경기 영상 상세 조회
* 경기 영상 수정

불가능한 기능은 다음과 같다.

* 경기 영상 삭제

삭제 권한을 막는 이유는 원본 경기 영상이 팀 분석 클립과 선수 개인 분석 클립의 기준 데이터이기 때문이다.

### 3.3 선수 `PLAYER`

선수는 등록된 경기 원본 영상을 조회할 수 있다.

가능한 기능은 다음과 같다.

* 경기 영상 목록 조회
* 경기 영상 상세 조회
* 경기 영상 재생

불가능한 기능은 다음과 같다.

* 경기 영상 업로드
* 경기 영상 수정
* 경기 영상 삭제

---

## 4. 권한 정책

## 4.1 권한 표

| 역할        | 업로드 | 목록 조회 | 상세 조회 | 재생 | 수정 | 삭제 |
| --------- | --: | ----: | ----: | -: | -: | -: |
| `COACH`   |  가능 |    가능 |    가능 | 가능 | 가능 | 가능 |
| `ANALYST` |  가능 |    가능 |    가능 | 가능 | 가능 | 불가 |
| `PLAYER`  |  불가 |    가능 |    가능 | 가능 | 불가 | 불가 |

## 4.2 권한 검증 기준

경기 영상 권한 검증은 반드시 백엔드에서 처리한다.

검증 기준은 다음과 같다.

* 인증되지 않은 사용자는 모든 경기 영상 API에 접근할 수 없다.
* `COACH`, `ANALYST`만 경기 영상을 업로드할 수 있다.
* `COACH`, `ANALYST`, `PLAYER` 모두 경기 영상을 조회할 수 있다.
* `COACH`, `ANALYST`만 경기 영상 메타데이터를 수정할 수 있다.
* `COACH`만 경기 영상을 삭제할 수 있다.
* `is_deleted = true`인 영상은 목록/상세/수정/삭제 대상에서 제외한다.
* `isAdmin = true`만으로 경기 영상 업로드/수정/삭제 권한을 주지 않는다.

## 4.3 관리자 `isAdmin` 처리

초기 경기 영상 업로드 기능에서는 `isAdmin = true`를 영상 권한 기준으로 사용하지 않는다.

이유는 다음과 같다.

* `isAdmin`은 가입 승인 같은 운영자 권한에 가깝다.
* 영상 관리 권한은 실제 업무 역할인 `COACH`, `ANALYST` 기준으로 판단하는 것이 명확하다.
* 관리자가 선수 역할일 경우 영상 수정/삭제까지 가능해지면 역할 책임이 애매해진다.

---

## 5. 화면 흐름

현재 단계에서는 백엔드 파일 업로드 API를 먼저 구현한다.

다만 프론트 연동을 고려해 응답 데이터에는 영상 재생 URL과 영상 길이 `durationSec`을 포함한다.

## 5.1 선수 경기 영상 조회 화면

선수는 모바일에서 경기 영상을 확인할 가능성이 높다.

기본 흐름은 다음과 같다.

1. 선수가 로그인한다.
2. 경기 영상 메뉴에 진입한다.
3. 최신 경기 영상 목록을 확인한다.
4. 경기 영상을 선택한다.
5. 영상 URL과 경기 정보를 받아 재생 화면에서 확인한다.

목록 화면에 표시할 정보는 다음과 같다.

* 경기 영상 ID
* 경기 제목
* 경기 날짜
* 장소
* 홈 점수
* 원정 점수
* 경기 결과
* 영상 상태
* 영상 길이
* 등록일시

상세 화면에 표시할 정보는 다음과 같다.

* 경기 영상 ID
* 영상 URL
* 경기 제목
* 경기 날짜
* 장소
* 홈 점수
* 원정 점수
* 경기 결과
* 영상 상태
* 영상 길이
* 등록자 ID
* 등록자 이름
* 생성일시
* 수정일시

## 5.2 지도자/분석관 경기 영상 관리 화면

지도자와 분석관은 PC에서 영상 업로드와 편집을 시작할 가능성이 높다.

기본 흐름은 다음과 같다.

1. 지도자 또는 분석관이 경기 영상 관리 화면에 진입한다.
2. 경기 영상 등록 버튼을 누른다.
3. `.mp4` 영상 파일과 경기 정보를 입력한다.
4. 저장 버튼을 누른다.
5. 백엔드가 영상 파일을 저장한다.
6. 백엔드가 `ffprobe`로 영상 길이를 추출한다.
7. 백엔드가 영상 URL과 영상 길이를 DB에 저장한다.
8. 저장 성공 후 경기 영상 목록을 갱신한다.
9. 등록된 영상을 선택해 상세 정보를 확인한다.
10. 필요한 경우 경기 정보를 수정한다.
11. 지도자는 삭제할 수 있고, 분석관은 삭제할 수 없다.

## 5.3 프론트 버튼 노출 정책

* `COACH`에게 업로드, 수정, 삭제 버튼을 보여준다.
* `ANALYST`에게 업로드, 수정 버튼만 보여준다.
* `PLAYER`에게 조회 화면만 보여준다.
* 버튼 숨김은 UX 목적이며, 실제 권한 차단은 백엔드가 담당한다.

---

## 6. API 흐름

## 6.1 API 설계 방향

경기 영상 조회 API는 지도자, 분석관, 선수가 모두 사용하므로 공통 API로 둔다.

경기 영상 업로드/수정 API는 지도자와 분석관이 사용할 수 있으므로 관리 API로 둔다.

경기 영상 삭제 API는 지도자만 가능하게 서비스 계층에서 검증한다.

API는 다음과 같다.

```http
GET    /api/match-videos
GET    /api/match-videos/{matchVideoId}
POST   /api/management/match-videos
PATCH  /api/management/match-videos/{matchVideoId}
DELETE /api/coach/match-videos/{matchVideoId}
```

## 6.2 경기 영상 목록 조회

```http
GET /api/match-videos?page=0&size=20
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`
* `PLAYER`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH`, `ANALYST`, `PLAYER` 중 하나인지 확인한다.
3. 페이지 번호와 페이지 크기를 검증한다.
4. `is_deleted = false`인 경기 영상만 조회한다.
5. `game_date DESC`, `created_at DESC` 기준으로 정렬한다.
6. 목록 응답을 반환한다.

응답 예시는 다음과 같다.

```json
{
  "matchVideos": [
    {
      "matchVideoId": 1,
      "title": "2026 춘계리그 vs 서울FC",
      "gameDate": "2026-06-20T15:00:00",
      "place": "메인 운동장",
      "homeScore": 2,
      "awayScore": 1,
      "matchResult": "WIN",
      "status": "READY",
      "durationSec": 5421,
      "createdAt": "2026-06-20T18:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

## 6.3 경기 영상 상세 조회

```http
GET /api/match-videos/{matchVideoId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`
* `PLAYER`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 경기 영상 ID로 경기 영상을 조회한다.
3. 경기 영상이 존재하지 않거나 삭제된 경우 실패 처리한다.
4. 상세 정보를 반환한다.

응답 예시는 다음과 같다.

```json
{
  "matchVideoId": 1,
  "url": "/uploads/match-videos/0cdd1c3e-2fd5-4db7-a123-f0f2f1f4b001.mp4",
  "title": "2026 춘계리그 vs 서울FC",
  "gameDate": "2026-06-20T15:00:00",
  "place": "메인 운동장",
  "homeScore": 2,
  "awayScore": 1,
  "matchResult": "WIN",
  "status": "READY",
  "durationSec": 5421,
  "uploaderId": 3,
  "uploaderName": "김지도",
  "createdAt": "2026-06-20T18:00:00",
  "updatedAt": "2026-06-20T18:00:00"
}
```

## 6.4 경기 영상 업로드

```http
POST /api/management/match-videos
Content-Type: multipart/form-data
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 form-data는 다음과 같다.

| Key           | Type | 필수 | 설명                    |
| ------------- | ---- | -: | --------------------- |
| `videoFile`   | File | 필수 | 업로드할 `.mp4` 경기 영상     |
| `title`       | Text | 필수 | 경기 제목                 |
| `gameDate`    | Text | 필수 | 경기 일시, ISO 형식         |
| `place`       | Text | 필수 | 경기 장소                 |
| `homeScore`   | Text | 필수 | 홈팀 점수                 |
| `awayScore`   | Text | 필수 | 원정팀 점수                |
| `matchResult` | Text | 필수 | `WIN`, `DRAW`, `LOSS` |

요청 예시는 다음과 같다.

```text
videoFile   File    match-test.mp4
title       Text    2026 춘계리그 vs 서울FC
gameDate    Text    2026-06-20T15:00:00
place       Text    메인 운동장
homeScore   Text    2
awayScore   Text    1
matchResult Text    WIN
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 경기 메타데이터 필수값을 검증한다.
4. 업로드 파일이 존재하는지 검증한다.
5. `.mp4` 확장자인지 검증한다.
6. MIME 타입이 `video/mp4`인지 검증한다.
7. 파일 크기가 200MB 이하인지 검증한다.
8. 로컬 저장소 `backend/uploads/match-videos`에 UUID 기반 파일명으로 저장한다.
9. 저장된 파일 경로를 기준으로 `ffprobe`를 실행해 영상 길이를 초 단위로 추출한다.
10. 영상 길이가 `null`이거나 0 이하이면 업로드 실패 처리한다.
11. `member_id`에 등록한 회원 ID를 저장한다.
12. `url`에 `/uploads/match-videos/{storedFileName}`을 저장한다.
13. `duration_sec`에 추출된 영상 길이를 저장한다.
14. `status`는 `READY`로 저장한다.
15. 생성된 경기 영상 ID를 반환한다.

응답 예시는 다음과 같다.

```json
{
  "matchVideoId": 1,
  "message": "경기 영상이 업로드되었습니다."
}
```

## 6.5 경기 영상 수정

```http
PATCH /api/management/match-videos/{matchVideoId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

이번 MVP에서는 영상 파일 수정/교체를 지원하지 않는다.

수정 가능한 값은 경기 메타데이터만 허용한다.

요청 예시는 다음과 같다.

```json
{
  "title": "2026 춘계리그 vs 서울FC",
  "gameDate": "2026-06-20T15:00:00",
  "place": "메인 운동장",
  "homeScore": 2,
  "awayScore": 1,
  "matchResult": "WIN"
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 경기 영상 ID로 경기 영상을 조회한다.
4. 경기 영상이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 요청 값을 검증한다.
6. 경기 메타데이터만 수정한다.
7. `url`, `duration_sec`, `status`는 수정하지 않는다.
8. 수정된 경기 영상 정보를 반환한다.

## 6.6 경기 영상 삭제

```http
DELETE /api/coach/match-videos/{matchVideoId}
```

사용 가능 역할은 다음과 같다.

* `COACH`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH`인지 확인한다.
3. 경기 영상 ID로 경기 영상을 조회한다.
4. 경기 영상이 존재하지 않거나 이미 삭제된 경우 실패 처리한다.
5. `is_deleted = true`로 변경한다.
6. 실제 영상 파일은 삭제하지 않는다.
7. 삭제 완료 응답을 반환한다.

---

## 7. DB 설계 방향

## 7.1 현재 사용할 테이블

초기 구현에서는 현재 DB의 `game_video_upload` 테이블을 그대로 사용한다.

| 컬럼             | 사용 목적                |
| -------------- | -------------------- |
| `id`           | 경기 영상 PK             |
| `member_id`    | 영상 등록 회원 ID          |
| `url`          | 브라우저에서 접근 가능한 영상 URL |
| `title`        | 경기 제목                |
| `game_date`    | 경기 날짜와 시간            |
| `place`        | 경기 장소                |
| `home_score`   | 홈팀 득점                |
| `away_score`   | 원정팀 득점               |
| `match_result` | 경기 결과                |
| `status`       | 영상 상태                |
| `duration_sec` | 영상 길이 초 단위           |
| `is_deleted`   | 소프트 삭제 여부            |
| `created_at`   | 생성일시                 |
| `updated_at`   | 수정일시                 |

## 7.2 Entity 매핑 방향

`game_video_upload` 테이블은 `GameVideoUploadEntity`로 매핑한다.

권장 필드명은 다음과 같다.

| DB 컬럼          | Entity 필드명    |
| -------------- | ------------- |
| `id`           | `id`          |
| `member_id`    | `member`      |
| `url`          | `url`         |
| `title`        | `title`       |
| `game_date`    | `gameDate`    |
| `place`        | `place`       |
| `home_score`   | `homeScore`   |
| `away_score`   | `awayScore`   |
| `match_result` | `matchResult` |
| `status`       | `status`      |
| `duration_sec` | `durationSec` |
| `is_deleted`   | `isDeleted`   |
| `created_at`   | `createdAt`   |
| `updated_at`   | `updatedAt`   |

## 7.3 경기 결과 Enum

`match_result`는 Enum으로 관리한다.

초기 허용 값은 다음과 같다.

| 값      | 의미 |
| ------ | -- |
| `WIN`  | 승  |
| `DRAW` | 무  |
| `LOSS` | 패  |

## 7.4 영상 상태 Enum

`status`는 Enum으로 관리한다.

초기 허용 값은 다음과 같다.

| 값           | 의미          |
| ----------- | ----------- |
| `UPLOADING` | 업로드 또는 등록 중 |
| `READY`     | 시청 가능       |
| `FAILED`    | 등록 실패       |

업로드와 영상 길이 추출이 모두 성공하면 `READY`로 저장한다.

파일 저장 또는 영상 길이 추출이 실패하면 경기 영상 DB 데이터는 생성하지 않는다.

## 7.5 DB 변경 여부

현재 `game_video_upload.duration_sec` 컬럼이 이미 존재하고 `NOT NULL`이므로, 업로드 시 반드시 실제 영상 길이를 저장해야 한다.

이번 단계에서 추가 DB 컬럼은 만들지 않는다.

추후 운영에서 필요해지면 다음 컬럼 또는 테이블을 검토한다.

* 원본 파일명
* 저장 파일명
* 파일 크기
* MIME 타입
* 저장소 타입
* S3 object key
* 썸네일 URL
* 해상도
* bitrate
* 영상 처리 작업 테이블
* 경기 영상 수정 이력 테이블

---

## 8. 요청/응답 DTO 방향

## 8.1 등록 요청 DTO

`CreateMatchVideoRequestDTO`

영상 파일은 DTO에 넣지 않고 `MultipartFile videoFile`로 별도 수신한다.

필드 방향은 다음과 같다.

* `title`
* `gameDate`
* `place`
* `homeScore`
* `awayScore`
* `matchResult`

서버에서 생성/저장하는 값은 다음과 같다.

* `url`
* `durationSec`
* `status`

## 8.2 등록 응답 DTO

`CreateMatchVideoResponseDTO`

필드 방향은 다음과 같다.

* `matchVideoId`
* `message`

## 8.3 수정 요청 DTO

`UpdateMatchVideoRequestDTO`

이번 MVP에서는 영상 파일 교체를 지원하지 않으므로 `url`, `durationSec`, `status`는 수정 요청에서 받지 않는다.

필드 방향은 다음과 같다.

* `title`
* `gameDate`
* `place`
* `homeScore`
* `awayScore`
* `matchResult`

## 8.4 목록 응답 DTO

`MatchVideoListResponseDTO`

필드 방향은 다음과 같다.

* `matchVideoId`
* `title`
* `gameDate`
* `place`
* `homeScore`
* `awayScore`
* `matchResult`
* `status`
* `durationSec`
* `createdAt`

## 8.5 상세 응답 DTO

`MatchVideoDetailResponseDTO`

필드 방향은 다음과 같다.

* `matchVideoId`
* `url`
* `title`
* `gameDate`
* `place`
* `homeScore`
* `awayScore`
* `matchResult`
* `status`
* `durationSec`
* `uploaderId`
* `uploaderName`
* `createdAt`
* `updatedAt`

## 8.6 페이지 응답 DTO

`MatchVideoPageResponseDTO`

Spring의 `Page` 객체를 그대로 노출하지 않고, 프론트에서 필요한 페이징 정보만 반환한다.

필드 방향은 다음과 같다.

* `matchVideos`
* `page`
* `size`
* `totalElements`
* `totalPages`

---

## 9. 예외 상황

| 상황                      | 처리 방식  | 권장 HTTP 상태                                       |
| ----------------------- | ------ | ------------------------------------------------ |
| JWT 없음                  | 인증 실패  | `401 Unauthorized`                               |
| JWT 만료                  | 인증 실패  | `401 Unauthorized`                               |
| 선수의 업로드 API 호출          | 접근 차단  | `403 Forbidden`                                  |
| 선수의 수정 API 호출           | 접근 차단  | `403 Forbidden`                                  |
| 선수의 삭제 API 호출           | 접근 차단  | `403 Forbidden`                                  |
| 분석관의 삭제 API 호출          | 접근 차단  | `403 Forbidden`                                  |
| 존재하지 않는 경기 영상 조회        | 조회 실패  | `404 Not Found`                                  |
| 삭제된 경기 영상 조회            | 조회 실패  | `404 Not Found`                                  |
| 삭제된 경기 영상 수정            | 수정 실패  | `404 Not Found`                                  |
| 이미 삭제된 경기 영상 삭제         | 삭제 실패  | `404 Not Found`                                  |
| 필수 입력값 누락               | 요청 실패  | `400 Bad Request`                                |
| 제목 길이 초과                | 요청 실패  | `400 Bad Request`                                |
| 장소 길이 초과                | 요청 실패  | `400 Bad Request`                                |
| 점수 값 음수                 | 요청 실패  | `400 Bad Request`                                |
| 허용되지 않은 경기 결과           | 요청 실패  | `400 Bad Request`                                |
| 파일 미첨부                  | 요청 실패  | `400 Bad Request`                                |
| `.mp4`가 아닌 파일           | 요청 실패  | `400 Bad Request`                                |
| `video/mp4`가 아닌 MIME 타입 | 요청 실패  | `400 Bad Request`                                |
| 파일 크기 200MB 초과          | 요청 실패  | `400 Bad Request`                                |
| 파일 저장 실패                | 업로드 실패 | `500 Internal Server Error`                      |
| ffprobe 실행 불가           | 업로드 실패 | `500 Internal Server Error`                      |
| 영상 길이 추출 실패             | 업로드 실패 | `400 Bad Request` 또는 `500 Internal Server Error` |
| 영상 길이가 0 이하             | 업로드 실패 | `400 Bad Request`                                |
| 페이지 번호가 음수              | 요청 실패  | `400 Bad Request`                                |
| 페이지 크기가 허용 범위 초과        | 요청 실패  | `400 Bad Request`                                |

---

## 10. 구현 순서

## 10.1 백엔드 1단계: 업로드 설정

1. `application.properties`에 multipart 파일 크기 제한을 설정한다.
2. `application.properties`에 로컬 업로드 경로를 설정한다.
3. `application.properties`에 브라우저 접근 URL prefix를 설정한다.
4. `application.properties`에 `ffprobe` 실행 경로를 설정한다.
5. `WebConfig`에서 `/uploads/match-videos/**` 정적 리소스 매핑을 추가한다.
6. `backend/.gitignore`에 `uploads/`를 추가한다.

## 10.2 백엔드 2단계: 파일 저장소 분리

1. `MatchVideoStorageService` 인터페이스를 생성한다.
2. `LocalMatchVideoStorageService` 구현체를 생성한다.
3. `StoredMatchVideoFile`에 저장 파일명, 접근 URL, 실제 저장 파일 경로를 담는다.
4. 업로드 파일 존재 여부를 검증한다.
5. `.mp4` 확장자를 검증한다.
6. `video/mp4` MIME 타입을 검증한다.
7. 파일 크기 200MB 이하를 검증한다.
8. UUID 기반 파일명으로 로컬 저장한다.

## 10.3 백엔드 3단계: 영상 길이 추출 분리

1. `MatchVideoMetadataExtractor` 인터페이스를 생성한다.
2. `FfprobeMatchVideoMetadataExtractor` 구현체를 생성한다.
3. 저장된 실제 파일 경로를 받아 `ffprobe`를 실행한다.
4. `ffprobe` 출력에서 영상 길이를 초 단위로 파싱한다.
5. 소수점 duration은 올림 또는 반올림 정책을 정해 정수 초로 변환한다.
6. 추출된 길이가 `null` 또는 0 이하이면 실패 처리한다.

## 10.4 백엔드 4단계: DTO 작성/수정

1. `CreateMatchVideoRequestDTO`에서 `url`, `status` 입력을 제거한다.
2. `CreateMatchVideoResponseDTO`를 생성한다.
3. `UpdateMatchVideoRequestDTO`에서 영상 교체 관련 입력을 제거한다.
4. `MatchVideoListResponseDTO`에 `durationSec`을 포함한다.
5. `MatchVideoDetailResponseDTO`에 `durationSec`을 포함한다.
6. `MatchVideoPageResponseDTO`는 기존 페이징 응답 구조를 유지한다.

## 10.5 백엔드 5단계: Service 작성/수정

1. 경기 영상 등록 권한을 검증한다.
2. 경기 메타데이터를 검증한다.
3. 로그인 회원을 조회한다.
4. `MatchVideoStorageService`로 파일을 저장한다.
5. `MatchVideoMetadataExtractor`로 영상 길이를 추출한다.
6. `url`, `durationSec`, `status = READY`를 포함해 DB에 저장한다.
7. 경기 영상 등록 응답을 반환한다.
8. 수정 API는 영상 파일 교체 없이 메타데이터만 수정한다.
9. 삭제 API는 소프트 삭제만 처리하고 실제 파일은 삭제하지 않는다.

## 10.6 백엔드 6단계: Controller 작성/수정

1. 등록 API를 `multipart/form-data` 방식으로 수정한다.
2. `@RequestParam(name = "videoFile") MultipartFile videoFile`을 받는다.
3. 경기 메타데이터는 `@RequestParam`으로 받는다.
4. `@RequestParam`과 `@PathVariable`에는 이름을 명시한다.
5. 수정/조회/삭제 API는 기존 경로를 유지한다.

## 10.7 백엔드 7단계: 테스트

1. `ffprobe -version` 실행 확인
2. 지도자 경기 영상 파일 업로드 성공 테스트
3. 분석관 경기 영상 파일 업로드 성공 테스트
4. 선수 경기 영상 파일 업로드 실패 테스트
5. `.mp4` 파일 업로드 성공 테스트
6. `.mp4`가 아닌 파일 업로드 실패 테스트
7. 파일 미첨부 업로드 실패 테스트
8. 200MB 초과 파일 업로드 실패 테스트
9. DB `url` 저장 확인
10. DB `duration_sec` 실제 영상 길이 저장 확인
11. 상세 조회 응답의 `url` 확인
12. 상세 조회 응답의 `durationSec` 확인
13. 브라우저에서 `/uploads/match-videos/{storedFileName}` 접근 확인
14. 경기 영상 메타데이터 수정 성공 테스트
15. 수정 시 `url`, `durationSec`, `status`가 바뀌지 않는지 확인
16. 지도자 경기 영상 소프트 삭제 성공 테스트
17. 분석관 삭제 실패 테스트
18. 선수 수정/삭제 실패 테스트

---

## 11. 추후 확장 가능성

초기 구현에서는 로컬 파일 저장, ffprobe 기반 영상 길이 추출, 경기 영상 CRUD에 집중한다.

추후 확장 후보는 다음과 같다.

* S3 파일 업로드
* Presigned URL 업로드
* S3 object key 저장
* CloudFront CDN 연동
* 영상 인코딩 작업 큐
* 썸네일 자동 생성
* 해상도/비트레이트 추출
* 영상 처리 상태 관리
* 업로드 실패 파일 정리 배치
* 영상 삭제 시 실제 파일 삭제 정책
* 경기 영상과 스케줄 연결
* 상대팀명, 대회명, 경기 구분 컬럼 추가
* 영상 수정 이력 관리
* 삭제 전 연결된 클립 존재 여부 확인
* 원본 영상 접근 URL 만료 처리
* 여러 팀을 받는 구조로 확장 시 `team_id` 추가

---

## 12. 주의사항

* 경기 영상 업로드, 수정, 삭제 권한은 프론트가 아니라 백엔드에서 반드시 검증한다.
* 선수는 경기 원본 영상 조회는 가능하지만 변경 권한은 없다.
* 분석관은 업로드와 수정은 가능하지만 삭제는 불가능하다.
* 삭제는 실제 삭제가 아니라 `is_deleted = true`로 처리한다.
* 조회 API는 삭제되지 않은 경기 영상만 반환한다.
* 업로드 파일은 초기 MVP에서 로컬 서버에 저장한다.
* 로컬 저장 경로 `backend/uploads/match-videos`는 Git에 포함하지 않는다.
* 실제 운영에서는 로컬 저장소가 아니라 S3 같은 외부 스토리지로 전환하는 것을 전제로 한다.
* 영상 길이 추출은 프론트가 아니라 백엔드에서 처리한다.
* 프론트에서 계산한 duration은 조작 가능하므로 DB 저장 기준으로 사용하지 않는다.
* `ffprobe`가 설치되어 있지 않으면 업로드 API가 실패할 수 있다.
* 운영 서버 배포 시 `ffprobe` 설치 또는 컨테이너 이미지 포함이 필요하다.
* 영상 길이가 없으면 클립 생성과 영상 편집 기능이 정상 동작하기 어렵다.
* 원본 경기 영상은 이후 팀 분석 클립과 선수 개인 분석 클립의 기준 데이터이므로 삭제 정책을 신중하게 유지해야 한다.
