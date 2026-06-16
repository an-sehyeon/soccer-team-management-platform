# 팀 분석 클립 기능 요구사항

## 1. 결론

팀 분석 클립 기능은 지도자와 분석관이 원본 경기 영상의 특정 구간을 팀 전체 공유용 분석 클립으로 저장하고, 지도자/분석관/선수가 조회할 수 있게 하는 기능이다.

초기 구현은 다음 정책을 기준으로 진행한다.

- 인증은 이미 구현된 JWT 인증 구조를 사용한다.
- JWT 인증 후 `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 값을 사용한다.
- 팀 분석 클립은 `team_video_clip` 테이블을 사용한다.
- 원본 경기 영상은 `game_video_upload` 테이블의 데이터를 사용한다.
- 초기에는 실제 영상을 자르지 않고 `startTimeSec`, `endTimeSec` 메타데이터로 클립을 저장한다.
- 클립 생성 시 `matchVideoId`가 존재하고 삭제되지 않았는지 반드시 백엔드에서 검증한다.
- 지도자 `COACH`는 팀 분석 클립 등록, 조회, 수정, 삭제가 가능하다.
- 분석관 `ANALYST`는 팀 분석 클립 등록, 조회, 수정이 가능하고 삭제는 불가능하다.
- 선수 `PLAYER`는 팀 분석 클립 조회만 가능하다.
- 삭제는 실제 DB 삭제가 아니라 `is_deleted = true`로 처리한다.
- 현재 단계에서는 프론트를 구현하지 않고 백엔드 API 기능만 개발한다.
- 현재 서비스는 단일 팀 기준이므로 별도 `team_id`는 사용하지 않는다.

---

## 2. 기능 목적

팀 분석 클립 기능의 목적은 전체 경기 영상 중 팀 전술, 공격, 수비, 전환, 세트피스 등 팀 전체가 함께 봐야 하는 장면을 클립 단위로 저장하고 공유하는 것이다.

이 기능은 경기 영상 편집 MVP의 핵심 기능이다.

팀 분석 클립은 다음 상황에서 사용된다.

- 지도자가 경기 중 중요한 장면을 구간으로 저장한다.
- 분석관이 경기 영상을 보며 공격/수비/세트피스 장면을 분류한다.
- 선수들이 모바일에서 팀 분석 클립을 조회한다.
- 이후 드로잉, 코멘트, 북마크, 선수 개인 클립 기능과 연결된다.

초기에는 실제 영상 파일을 잘라서 저장하지 않는다.

대신 원본 영상 URL과 원본 영상 기준 시작/종료 시간을 함께 응답해서 프론트에서 해당 구간을 재생하는 방식으로 처리한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

지도자는 팀 분석 클립을 전체 관리할 수 있다.

가능한 기능은 다음과 같다.

- 팀 분석 클립 등록
- 팀 분석 클립 목록 조회
- 팀 분석 클립 상세 조회
- 팀 분석 클립 수정
- 팀 분석 클립 삭제

### 3.2 분석관 `ANALYST`

분석관은 영상 분석 실무를 담당하므로 팀 분석 클립 등록과 수정은 가능하다.

가능한 기능은 다음과 같다.

- 팀 분석 클립 등록
- 팀 분석 클립 목록 조회
- 팀 분석 클립 상세 조회
- 팀 분석 클립 수정

불가능한 기능은 다음과 같다.

- 팀 분석 클립 삭제

분석관에게 삭제 권한을 주지 않는 이유는 팀 분석 클립이 선수들에게 공유되는 자료이며, 이후 드로잉/코멘트 데이터와 연결될 수 있기 때문이다.

### 3.3 선수 `PLAYER`

선수는 팀 전체에게 공유된 분석 클립을 조회하는 사용자이다.

가능한 기능은 다음과 같다.

- 팀 분석 클립 목록 조회
- 팀 분석 클립 상세 조회

불가능한 기능은 다음과 같다.

- 팀 분석 클립 등록
- 팀 분석 클립 수정
- 팀 분석 클립 삭제

---

## 4. 권한 정책

## 4.1 권한 표

| 역할 | 등록 | 목록 조회 | 상세 조회 | 수정 | 삭제 |
|---|---:|---:|---:|---:|---:|
| `COACH` | 가능 | 가능 | 가능 | 가능 | 가능 |
| `ANALYST` | 가능 | 가능 | 가능 | 가능 | 불가 |
| `PLAYER` | 불가 | 가능 | 가능 | 불가 | 불가 |

## 4.2 권한 검증 기준

팀 분석 클립 권한 검증은 반드시 백엔드에서 처리한다.

검증 기준은 다음과 같다.

- 인증되지 않은 사용자는 모든 팀 분석 클립 API에 접근할 수 없다.
- `COACH`, `ANALYST`만 팀 분석 클립을 등록할 수 있다.
- `COACH`, `ANALYST`, `PLAYER` 모두 팀 분석 클립을 조회할 수 있다.
- `COACH`, `ANALYST`만 팀 분석 클립을 수정할 수 있다.
- `COACH`만 팀 분석 클립을 삭제할 수 있다.
- `PLAYER`는 등록, 수정, 삭제 API를 직접 호출해도 실패해야 한다.
- `ANALYST`는 삭제 API를 직접 호출해도 실패해야 한다.
- `isAdmin = true`만으로 팀 분석 클립 등록, 수정, 삭제 권한을 주지 않는다.
- 삭제된 원본 경기 영상에는 새 팀 분석 클립을 만들 수 없다.
- 삭제된 팀 분석 클립은 목록/상세/수정/삭제 대상에서 제외한다.

## 4.3 원본 경기 영상 검증 정책

팀 분석 클립 생성 시 `matchVideoId` 검증은 필수다.

검증 기준은 다음과 같다.

- `matchVideoId`가 null이면 실패한다.
- `game_video_upload.id = matchVideoId`인 원본 영상이 존재해야 한다.
- 원본 영상의 `is_deleted`가 false여야 한다.
- 원본 영상이 삭제된 경우 새 클립 생성은 실패한다.
- 원본 영상이 존재하지 않으면 `404 Not Found`로 처리한다.

---

## 5. 화면 흐름

현재 단계에서는 프론트를 구현하지 않는다.

다만 백엔드 API 응답은 추후 화면 구현을 고려해서 설계한다.

## 5.1 선수 팀 분석 클립 조회 화면

선수는 모바일에서 팀 분석 클립을 조회할 가능성이 높다.

기본 흐름은 다음과 같다.

1. 선수가 로그인한다.
2. 팀 분석 클립 메뉴에 진입한다.
3. 최신 팀 분석 클립 목록을 확인한다.
4. 필요한 경우 경기 영상별 또는 클립 유형별로 필터링한다.
5. 클립을 선택한다.
6. 원본 영상 URL, 시작 시간, 종료 시간을 받아 해당 구간을 재생한다.
7. 클립 제목과 코멘트를 함께 확인한다.

목록 화면에 표시할 정보는 다음과 같다.

- 팀 분석 클립 ID
- 원본 경기 영상 ID
- 원본 경기 제목
- 클립 유형
- 클립 제목
- 시작 시간
- 종료 시간
- 작성자 이름
- 생성일시

상세 화면에 표시할 정보는 다음과 같다.

- 팀 분석 클립 ID
- 원본 경기 영상 ID
- 원본 경기 영상 URL
- 원본 경기 제목
- 클립 유형
- 클립 제목
- 클립 코멘트
- 시작 시간
- 종료 시간
- 클립 상태
- 작성자 ID
- 작성자 이름
- 생성일시
- 수정일시

## 5.2 지도자/분석관 팀 분석 클립 관리 화면

지도자와 분석관은 PC에서 경기 영상을 보며 클립을 생성할 가능성이 높다.

기본 흐름은 다음과 같다.

1. 지도자 또는 분석관이 원본 경기 영상 상세 화면 또는 영상 편집기에 진입한다.
2. 원본 경기 영상을 재생한다.
3. 필요한 장면의 시작 시간과 종료 시간을 선택한다.
4. 클립 유형, 제목, 코멘트를 입력한다.
5. 저장 버튼을 누른다.
6. 서버는 원본 경기 영상 존재 여부와 삭제 여부를 검증한다.
7. 팀 분석 클립을 저장한다.
8. 저장된 클립 목록을 갱신한다.

---

## 6. API 흐름

## 6.1 API 설계 방향

팀 분석 클립 조회 API는 지도자, 분석관, 선수가 모두 사용하므로 공통 API로 둔다.

팀 분석 클립 등록/수정 API는 지도자와 분석관이 사용할 수 있으므로 관리 API로 둔다.

팀 분석 클립 삭제 API는 지도자만 가능하므로 지도자 전용 API로 분리한다.

권장 API 초안은 다음과 같다.

```http
GET    /api/team-analysis-clips
GET    /api/team-analysis-clips/{teamClipId}
POST   /api/management/team-analysis-clips
PATCH  /api/management/team-analysis-clips/{teamClipId}
DELETE /api/coach/team-analysis-clips/{teamClipId}
```

## 6.2 팀 분석 클립 목록 조회

```http
GET /api/team-analysis-clips?page=0&size=20&matchVideoId=1&clipType=ATTACK
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`
- `PLAYER`

요청 파라미터는 다음과 같다.

| 파라미터 | 필수 여부 | 설명 |
|---|---:|---|
| `page` | 선택 | 페이지 번호 |
| `size` | 선택 | 페이지 크기 |
| `matchVideoId` | 선택 | 특정 원본 경기 영상 기준 필터 |
| `clipType` | 선택 | 클립 유형 필터 |

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH`, `ANALYST`, `PLAYER` 중 하나인지 확인한다.
3. 페이지 번호와 페이지 크기를 검증한다.
4. `matchVideoId`가 있으면 원본 경기 영상 존재 여부와 삭제 여부를 검증한다.
5. `clipType`이 있으면 허용된 Enum 값인지 검증한다.
6. `is_deleted = false`인 팀 분석 클립만 조회한다.
7. 최신 생성일 기준으로 정렬한다.
8. 목록 응답을 반환한다.

응답 예시는 다음과 같다.

```json
{
  "teamClips": [
    {
      "teamClipId": 1,
      "matchVideoId": 3,
      "matchVideoTitle": "2026 춘계리그 vs 서울FC",
      "clipType": "ATTACK",
      "title": "전방 압박 성공 장면",
      "startTimeSec": 755,
      "endTimeSec": 790,
      "status": "READY",
      "writerId": 2,
      "writerName": "김지도",
      "createdAt": "2026-06-20T19:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

## 6.3 팀 분석 클립 상세 조회

```http
GET /api/team-analysis-clips/{teamClipId}
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`
- `PLAYER`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 팀 분석 클립 ID로 클립을 조회한다.
3. 클립이 존재하지 않거나 삭제된 경우 실패 처리한다.
4. 연결된 원본 경기 영상이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 원본 영상 URL과 클립 메타데이터를 함께 반환한다.

응답 예시는 다음과 같다.

```json
{
  "teamClipId": 1,
  "matchVideoId": 3,
  "matchVideoTitle": "2026 춘계리그 vs 서울FC",
  "matchVideoUrl": "https://example.com/videos/game-001.mp4",
  "clipType": "ATTACK",
  "title": "전방 압박 성공 장면",
  "comment": "전방 3명이 동시에 압박을 시작해서 상대 빌드업을 끊은 장면",
  "startTimeSec": 755,
  "endTimeSec": 790,
  "status": "READY",
  "writerId": 2,
  "writerName": "김지도",
  "createdAt": "2026-06-20T19:00:00",
  "updatedAt": "2026-06-20T19:00:00"
}
```

## 6.4 팀 분석 클립 등록

```http
POST /api/management/team-analysis-clips
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

요청 예시는 다음과 같다.

```json
{
  "matchVideoId": 3,
  "clipType": "ATTACK",
  "title": "전방 압박 성공 장면",
  "comment": "전방 3명이 동시에 압박을 시작해서 상대 빌드업을 끊은 장면",
  "startTimeSec": 755,
  "endTimeSec": 790
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. `matchVideoId`가 존재하는지 확인한다.
4. 원본 경기 영상이 삭제되지 않았는지 확인한다.
5. 요청 필수값을 검증한다.
6. `clipType`이 허용된 Enum인지 확인한다.
7. `startTimeSec`와 `endTimeSec`가 0 이상인지 확인한다.
8. `startTimeSec < endTimeSec`인지 확인한다.
9. 실제 영상을 자르지 않으므로 `url`은 null로 저장한다.
10. 메타데이터 기반 클립이므로 `status`는 `READY`로 저장한다.
11. `member_id`에 등록한 회원 ID를 저장한다.
12. 팀 분석 클립을 저장한다.
13. 생성된 팀 분석 클립 ID를 반환한다.

응답 예시는 다음과 같다.

```json
{
  "teamClipId": 1,
  "message": "팀 분석 클립이 등록되었습니다."
}
```

## 6.5 팀 분석 클립 수정

```http
PATCH /api/management/team-analysis-clips/{teamClipId}
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

초기 구현에서는 부분 수정 방식보다 전체 수정 방식에 가깝게 처리한다.

요청 예시는 다음과 같다.

```json
{
  "matchVideoId": 3,
  "clipType": "DEFENSE",
  "title": "수비 전환 지연 장면",
  "comment": "공을 잃은 뒤 1차 압박 전환이 늦은 장면",
  "startTimeSec": 840,
  "endTimeSec": 880
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 팀 분석 클립 ID로 기존 클립을 조회한다.
4. 클립이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. `matchVideoId`가 존재하고 삭제되지 않았는지 검증한다.
6. 요청 필수값을 검증한다.
7. `clipType`이 허용된 Enum인지 확인한다.
8. `startTimeSec < endTimeSec`인지 확인한다.
9. 팀 분석 클립 정보를 수정한다.
10. 수정된 팀 분석 클립 정보를 반환한다.

## 6.6 팀 분석 클립 삭제

```http
DELETE /api/coach/team-analysis-clips/{teamClipId}
```

사용 가능 역할은 다음과 같다.

- `COACH`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH`인지 확인한다.
3. 팀 분석 클립 ID로 클립을 조회한다.
4. 클립이 존재하지 않거나 이미 삭제된 경우 실패 처리한다.
5. `is_deleted = true`로 변경한다.
6. 삭제 완료 응답을 반환한다.

---

## 7. DB 설계 방향

## 7.1 현재 사용할 테이블

초기 구현에서는 현재 DB의 `team_video_clip` 테이블을 그대로 사용한다.

| 컬럼 | 사용 목적 |
|---|---|
| `id` | 팀 분석 클립 PK |
| `upload_id` | 원본 경기 영상 ID |
| `member_id` | 클립 작성 회원 ID |
| `clip_type` | 클립 유형 |
| `title` | 클립 제목 |
| `comment` | 클립 설명 |
| `start_time_sec` | 원본 영상 기준 시작 시간 |
| `end_time_sec` | 원본 영상 기준 종료 시간 |
| `url` | 실제 잘라낸 클립 URL, 초기에는 null |
| `status` | 클립 상태 |
| `is_deleted` | 소프트 삭제 여부 |
| `created_at` | 생성일시 |
| `updated_at` | 수정일시 |

## 7.2 Entity 매핑 방향

`team_video_clip` 테이블은 `TeamVideoClipEntity`로 매핑한다.

권장 필드명은 다음과 같다.

| DB 컬럼 | Entity 필드명 |
|---|---|
| `id` | `id` |
| `upload_id` | `matchVideo` 또는 `gameVideoUpload` |
| `member_id` | `member` |
| `clip_type` | `clipType` |
| `title` | `title` |
| `comment` | `comment` |
| `start_time_sec` | `startTimeSec` |
| `end_time_sec` | `endTimeSec` |
| `url` | `url` |
| `status` | `status` |
| `is_deleted` | `isDeleted` |
| `created_at` | `createdAt` |
| `updated_at` | `updatedAt` |

## 7.3 클립 유형 Enum

`clip_type`은 Enum으로 관리한다.

초기 허용 값은 다음과 같다.

| 값 | 의미 |
|---|---|
| `HIGHLIGHT` | 주요 장면 |
| `ATTACK` | 공격 |
| `DEFENSE` | 수비 |
| `GOAL` | 득점 |
| `CONCEDED` | 실점 |
| `OFFSIDE` | 오프사이드 |
| `SETPIECE` | 세트피스 |
| `ETC` | 기타 |

## 7.4 클립 상태 Enum

`status`는 Enum으로 관리한다.

초기 메타데이터 방식에서는 실제 인코딩 작업이 없으므로 등록 성공 시 `READY`로 저장한다.

초기 허용 값은 다음과 같다.

| 값 | 의미 |
|---|---|
| `READY` | 재생 가능 |
| `PROCESSING` | 실제 클립 파일 생성 중 |
| `FAILED` | 클립 처리 실패 |

현재 DB 기본값이 `UPLOADING`이라면 서비스 코드에서 `READY`를 명시적으로 저장하거나, 추후 실제 인코딩 기능을 만들 때 상태 정책을 다시 정리한다.

## 7.5 DB 변경 여부

초기 구현에서는 DB 컬럼을 추가하지 않는다.

현재 `team_video_clip` 테이블만으로 다음 기능을 처리할 수 있다.

- 원본 경기 영상 연결
- 클립 작성자 연결
- 클립 유형 저장
- 제목/코멘트 저장
- 원본 영상 기준 시작/종료 시간 저장
- 클립 상태 저장
- 소프트 삭제

추후 운영에서 필요해지면 다음 컬럼 또는 테이블을 검토한다.

- 클립 조회수
- 선수별 확인 여부
- 클립 묶음 테이블
- 클립 정렬 순서
- 클립 수정 이력
- 클립 공유 범위
- 실제 잘라낸 클립 파일 URL
- 클립 썸네일 URL
- 영상 인코딩 작업 테이블

---

## 8. 요청/응답 DTO 방향

## 8.1 등록 요청 DTO

`CreateTeamAnalysisClipRequestDTO`

필드 방향은 다음과 같다.

- `matchVideoId`
- `clipType`
- `title`
- `comment`
- `startTimeSec`
- `endTimeSec`

## 8.2 수정 요청 DTO

`UpdateTeamAnalysisClipRequestDTO`

초기 구현에서는 전체 수정 방식에 가깝게 처리한다.

필드 방향은 다음과 같다.

- `matchVideoId`
- `clipType`
- `title`
- `comment`
- `startTimeSec`
- `endTimeSec`

## 8.3 목록 응답 DTO

`TeamAnalysisClipListResponseDTO`

필드 방향은 다음과 같다.

- `teamClipId`
- `matchVideoId`
- `matchVideoTitle`
- `clipType`
- `title`
- `startTimeSec`
- `endTimeSec`
- `status`
- `writerId`
- `writerName`
- `createdAt`

## 8.4 상세 응답 DTO

`TeamAnalysisClipDetailResponseDTO`

필드 방향은 다음과 같다.

- `teamClipId`
- `matchVideoId`
- `matchVideoTitle`
- `matchVideoUrl`
- `clipType`
- `title`
- `comment`
- `startTimeSec`
- `endTimeSec`
- `status`
- `writerId`
- `writerName`
- `createdAt`
- `updatedAt`

## 8.5 페이지 응답 DTO

`TeamAnalysisClipPageResponseDTO`

Spring의 `Page` 객체를 그대로 노출하지 않고 프론트에서 필요한 페이징 정보만 반환한다.

필드 방향은 다음과 같다.

- `teamClips`
- `page`
- `size`
- `totalElements`
- `totalPages`

---

## 9. 예외 상황

| 상황 | 처리 방식 | 권장 HTTP 상태 |
|---|---|---|
| JWT 없음 | 인증 실패 | `401 Unauthorized` |
| JWT 만료 | 인증 실패 | `401 Unauthorized` |
| 선수의 등록 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 수정 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 삭제 API 호출 | 접근 차단 | `403 Forbidden` |
| 분석관의 삭제 API 호출 | 접근 차단 | `403 Forbidden` |
| 존재하지 않는 원본 경기 영상 ID | 생성/수정 실패 | `404 Not Found` |
| 삭제된 원본 경기 영상 ID | 생성/수정 실패 | `404 Not Found` |
| 존재하지 않는 팀 분석 클립 조회 | 조회 실패 | `404 Not Found` |
| 삭제된 팀 분석 클립 조회 | 조회 실패 | `404 Not Found` |
| 삭제된 팀 분석 클립 수정 | 수정 실패 | `404 Not Found` |
| 이미 삭제된 팀 분석 클립 삭제 | 삭제 실패 | `404 Not Found` |
| 필수 입력값 누락 | 요청 실패 | `400 Bad Request` |
| 허용되지 않은 클립 유형 | 요청 실패 | `400 Bad Request` |
| 제목 길이 초과 | 요청 실패 | `400 Bad Request` |
| 코멘트 길이 초과 | 요청 실패 | `400 Bad Request` |
| 시작 시간이 음수 | 요청 실패 | `400 Bad Request` |
| 종료 시간이 음수 | 요청 실패 | `400 Bad Request` |
| 시작 시간이 종료 시간보다 같거나 큼 | 요청 실패 | `400 Bad Request` |
| 페이지 번호가 음수 | 요청 실패 | `400 Bad Request` |
| 페이지 크기가 허용 범위 초과 | 요청 실패 | `400 Bad Request` |

---

## 10. 구현 순서

## 10.1 백엔드 1단계: 기본 구조 확인

1. `TeamVideoClipEntity` 필드와 DB 컬럼 매핑 확인
2. `GameVideoUploadEntity`와 `TeamVideoClipEntity` 연관관계 확인
3. `MemberEntity`와 `TeamVideoClipEntity` 연관관계 확인
4. `TeamVideoClipRepository` 생성
5. `GameVideoUploadRepository`에 원본 영상 조회 메서드 확인 또는 추가
6. 기존 공통 예외 구조와 `ErrorCode` 확인

## 10.2 백엔드 2단계: Enum 확인 또는 생성

1. `TeamClipTypeEnum` 확인 또는 생성
2. `VideoClipStatusEnum` 확인 또는 생성
3. DB의 `clip_type`, `status`와 Enum 값이 맞는지 확인

## 10.3 백엔드 3단계: DTO 작성

1. `CreateTeamAnalysisClipRequestDTO` 작성
2. `UpdateTeamAnalysisClipRequestDTO` 작성
3. `TeamAnalysisClipListResponseDTO` 작성
4. `TeamAnalysisClipDetailResponseDTO` 작성
5. `TeamAnalysisClipPageResponseDTO` 작성

## 10.4 백엔드 4단계: 권한 검증 로직 작성

1. `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 사용
2. 팀 분석 클립 등록/수정 권한 검증 메서드 작성
3. `COACH`, `ANALYST`가 아니면 등록/수정 실패 처리
4. 팀 분석 클립 삭제 권한 검증 메서드 작성
5. `COACH`가 아니면 삭제 실패 처리
6. 조회는 `COACH`, `ANALYST`, `PLAYER` 허용
7. `isAdmin`만으로 클립 변경 권한을 주지 않도록 처리

## 10.5 백엔드 5단계: 원본 경기 영상 검증 로직 작성

1. `matchVideoId`로 `game_video_upload` 조회
2. 존재하지 않으면 실패 처리
3. `isDeleted = true`이면 실패 처리
4. 클립 생성/수정 시 반드시 이 검증 메서드 사용

## 10.6 백엔드 6단계: Service 작성

1. 팀 분석 클립 등록 기능 작성
2. 팀 분석 클립 목록 조회 기능 작성
3. 팀 분석 클립 상세 조회 기능 작성
4. 팀 분석 클립 수정 기능 작성
5. 팀 분석 클립 삭제 기능 작성

## 10.7 백엔드 7단계: Controller 작성

1. 공통 조회 Controller 작성
2. 지도자/분석관 공통 관리 Controller 작성
3. 지도자 전용 삭제 Controller 작성
4. `@RequestParam`과 `@PathVariable`에는 이름을 명시한다.

## 10.8 백엔드 8단계: 테스트

1. 지도자 팀 분석 클립 등록 성공 테스트
2. 분석관 팀 분석 클립 등록 성공 테스트
3. 선수 팀 분석 클립 등록 실패 테스트
4. 존재하지 않는 `matchVideoId`로 등록 실패 테스트
5. 삭제된 `matchVideoId`로 등록 실패 테스트
6. `startTimeSec >= endTimeSec` 등록 실패 테스트
7. 전체 역할 팀 분석 클립 목록 조회 성공 테스트
8. 전체 역할 팀 분석 클립 상세 조회 성공 테스트
9. 지도자 팀 분석 클립 수정 성공 테스트
10. 분석관 팀 분석 클립 수정 성공 테스트
11. 선수 팀 분석 클립 수정 실패 테스트
12. 지도자 팀 분석 클립 삭제 성공 테스트
13. 분석관 팀 분석 클립 삭제 실패 테스트
14. 선수 팀 분석 클립 삭제 실패 테스트
15. 삭제된 팀 분석 클립 조회 제외 테스트
16. 페이징 조회 테스트
17. 클립 유형 필터 조회 테스트
18. 원본 경기 영상별 필터 조회 테스트

---

## 11. 추후 확장 가능성

초기 구현에서는 메타데이터 기반 팀 분석 클립 CRUD와 조회에 집중한다.

추후 확장 후보는 다음과 같다.

- 실제 클립 파일 자르기
- 영상 인코딩 작업 큐
- 클립 썸네일 생성
- 클립 묶음 생성
- 드로잉 데이터 연결
- 특정 시간대 코멘트 연결
- 선수별 확인 여부
- 클립 조회 기록
- 팀 분석 클립 검색
- 경기 이벤트 데이터와 클립 연결
- AI 자동 하이라이트 생성
- AI 이벤트 인식 후 클립 자동 생성

---

## 12. 주의사항

- 팀 분석 클립 등록, 수정, 삭제 권한은 프론트가 아니라 백엔드에서 반드시 검증한다.
- 분석관은 등록과 수정은 가능하지만 삭제는 불가능하다.
- 선수는 조회만 가능하다.
- 삭제는 실제 삭제가 아니라 `is_deleted = true`로 처리한다.
- 조회 API는 삭제되지 않은 팀 분석 클립만 반환한다.
- 클립 생성 시 `matchVideoId`가 존재하고 삭제되지 않았는지 반드시 검증한다.
- 초기에는 실제 영상을 자르지 않으므로 `url`은 null이어도 된다.
- 초기 메타데이터 방식에서는 `status`를 `READY`로 저장하는 것이 자연스럽다.
- `startTimeSec`는 반드시 `endTimeSec`보다 작아야 한다.
- 현재 원본 영상 길이 컬럼이 없으므로 `endTimeSec`가 실제 영상 길이보다 짧은지는 초기에는 검증할 수 없다.
- 추후 `game_video_upload`에 영상 길이 컬럼이 추가되면 클립 시간이 영상 길이를 넘지 않는지 검증해야 한다.
- 현재 서비스는 단일 팀 기준이므로 `team_id`를 사용하지 않는다.
- 추후 여러 팀을 받을 경우 원본 영상과 팀 분석 클립 모두 팀 소속 검증이 필요하다.
