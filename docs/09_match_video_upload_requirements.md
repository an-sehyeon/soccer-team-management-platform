# 경기 영상 업로드 기능 요구사항

## 1. 결론

경기 영상 업로드 기능은 지도자와 분석관이 원본 경기 영상 URL과 경기 정보를 등록하고, 지도자/분석관/선수가 원본 경기 영상을 조회할 수 있게 하는 기능이다.

초기 구현은 다음 정책을 기준으로 진행한다.

- 인증은 이미 구현된 JWT 인증 구조를 사용한다.
- JWT 인증 후 `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 값을 사용한다.
- 지도자 `COACH`는 경기 영상 업로드, 조회, 수정, 삭제가 가능하다.
- 분석관 `ANALYST`는 경기 영상 업로드, 조회, 수정이 가능하다.
- 분석관 `ANALYST`는 경기 영상 삭제가 불가능하다.
- 선수 `PLAYER`는 경기 원본 영상 목록 조회와 상세 조회가 가능하다.
- 선수 `PLAYER`는 경기 영상 업로드, 수정, 삭제가 불가능하다.
- 관리자 여부 `isAdmin`은 경기 영상 권한의 기본 기준으로 사용하지 않는다.
- 삭제는 실제 DB 삭제가 아니라 `is_deleted = true`로 처리하는 소프트 삭제를 사용한다.
- 초기 버전은 단일 팀 서비스 기준으로 설계한다.
- 현재 DB의 `game_video_upload` 테이블을 우선 사용한다.
- 초기에는 영상 파일 업로드가 아니라 영상 URL을 저장하는 방식으로 구현한다.

---

## 2. 기능 목적

경기 영상 업로드 기능의 목적은 경기 원본 영상을 플랫폼에 등록하고, 이후 영상 편집과 분석 클립 생성의 기준 데이터를 만드는 것이다.

이 기능은 단순 영상 목록 관리 기능이 아니라, 이후 다음 기능들의 출발점이다.

- 영상 편집기에서 원본 경기 영상 불러오기
- 팀 분석 클립 생성
- 선수 개인 분석 클립 생성
- 북마크 생성
- 드로잉 데이터 연결
- 경기별 분석 데이터 관리

따라서 초기 구현에서는 복잡한 파일 인코딩보다 원본 영상 URL과 경기 메타데이터를 안정적으로 관리하는 것을 우선한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

지도자는 팀 운영과 영상 분석 책임자이므로 경기 영상을 관리할 수 있다.

가능한 기능은 다음과 같다.

- 경기 영상 업로드
- 경기 영상 목록 조회
- 경기 영상 상세 조회
- 경기 영상 수정
- 경기 영상 삭제

### 3.2 분석관 `ANALYST`

분석관은 영상 분석 실무를 담당할 수 있으므로 경기 영상 등록과 수정은 허용한다.

가능한 기능은 다음과 같다.

- 경기 영상 업로드
- 경기 영상 목록 조회
- 경기 영상 상세 조회
- 경기 영상 수정

불가능한 기능은 다음과 같다.

- 경기 영상 삭제

삭제 권한을 막는 이유는 원본 경기 영상이 팀 분석 클립과 선수 개인 분석 클립의 기준 데이터이기 때문이다. 분석관이 실수로 삭제하면 연결된 분석 데이터 운영에 문제가 생길 수 있다.

### 3.3 선수 `PLAYER`

선수는 등록된 경기 원본 영상을 조회할 수 있다.

가능한 기능은 다음과 같다.

- 경기 영상 목록 조회
- 경기 영상 상세 조회

불가능한 기능은 다음과 같다.

- 경기 영상 업로드
- 경기 영상 수정
- 경기 영상 삭제

선수에게 원본 영상 조회를 허용하되, 변경 권한은 주지 않는다.

---

## 4. 권한 정책

## 4.1 권한 표

| 역할 | 업로드 | 목록 조회 | 상세 조회 | 수정 | 삭제 |
|---|---:|---:|---:|---:|---:|
| `COACH` | 가능 | 가능 | 가능 | 가능 | 가능 |
| `ANALYST` | 가능 | 가능 | 가능 | 가능 | 불가 |
| `PLAYER` | 불가 | 가능 | 가능 | 불가 | 불가 |

## 4.2 권한 검증 기준

경기 영상 권한 검증은 반드시 백엔드에서 처리한다.

검증 기준은 다음과 같다.

- 인증되지 않은 사용자는 모든 경기 영상 API에 접근할 수 없다.
- `COACH`, `ANALYST`만 경기 영상을 업로드할 수 있다.
- `COACH`, `ANALYST`, `PLAYER` 모두 경기 영상을 조회할 수 있다.
- `COACH`, `ANALYST`만 경기 영상을 수정할 수 있다.
- `COACH`만 경기 영상을 삭제할 수 있다.
- `is_deleted = true`인 영상은 목록/상세/수정/삭제 대상에서 제외한다.
- `isAdmin = true`만으로 경기 영상 업로드/수정/삭제 권한을 주지 않는다.

## 4.3 관리자 `isAdmin` 처리

초기 경기 영상 업로드 기능에서는 `isAdmin = true`를 영상 권한 기준으로 사용하지 않는다.

이유는 다음과 같다.

- `isAdmin`은 가입 승인 같은 운영자 권한에 가깝다.
- 영상 관리 권한은 실제 업무 역할인 `COACH`, `ANALYST` 기준으로 판단하는 것이 명확하다.
- 관리자가 선수 역할일 경우 영상 수정/삭제까지 가능해지면 역할 책임이 애매해진다.

단, 추후 운영 관리자에게 전체 영상 관리 권한을 줄 필요가 생기면 별도 정책으로 확장한다.

---

## 5. 화면 흐름

현재 단계에서는 프론트를 구현하지 않는다.

다만 백엔드 API 응답은 추후 화면을 고려해 설계한다.

## 5.1 선수 경기 영상 조회 화면

선수는 모바일에서 경기 영상을 확인할 가능성이 높다.

기본 흐름은 다음과 같다.

1. 선수가 로그인한다.
2. 경기 영상 메뉴에 진입한다.
3. 최신 경기 영상 목록을 확인한다.
4. 경기 영상을 선택한다.
5. 영상 URL과 경기 정보를 받아 재생 화면에서 확인한다.

목록 화면에 표시할 정보는 다음과 같다.

- 경기 영상 ID
- 경기 제목
- 경기 날짜
- 장소
- 홈 점수
- 원정 점수
- 경기 결과
- 영상 상태
- 등록일시

상세 화면에 표시할 정보는 다음과 같다.

- 경기 영상 ID
- 영상 URL
- 경기 제목
- 경기 날짜
- 장소
- 홈 점수
- 원정 점수
- 경기 결과
- 영상 상태
- 등록자 ID
- 등록자 이름
- 생성일시
- 수정일시

## 5.2 지도자/분석관 경기 영상 관리 화면

지도자와 분석관은 PC에서 영상 업로드와 편집을 시작할 가능성이 높다.

기본 흐름은 다음과 같다.

1. 지도자 또는 분석관이 경기 영상 관리 화면에 진입한다.
2. 경기 영상 등록 버튼을 누른다.
3. 영상 URL과 경기 정보를 입력한다.
4. 저장 버튼을 누른다.
5. 저장 성공 후 경기 영상 목록을 갱신한다.
6. 등록된 영상을 선택해 상세 정보를 확인한다.
7. 필요한 경우 경기 정보를 수정한다.
8. 지도자는 삭제할 수 있고, 분석관은 삭제할 수 없다.

## 5.3 프론트 버튼 노출 정책

- `COACH`에게 업로드, 수정, 삭제 버튼을 보여준다.
- `ANALYST`에게 업로드, 수정 버튼만 보여준다.
- `PLAYER`에게 조회 화면만 보여준다.
- 버튼 숨김은 UX 목적이며, 실제 권한 차단은 백엔드가 담당한다.

---

## 6. API 흐름

## 6.1 API 설계 방향

경기 영상 조회 API는 지도자, 분석관, 선수가 모두 사용하므로 공통 API로 둔다.

경기 영상 업로드/수정 API는 지도자와 분석관이 사용할 수 있으므로 관리 API로 둔다.

경기 영상 삭제 API는 지도자만 가능하게 서비스 계층에서 검증한다.

권장 API 초안은 다음과 같다.

```http
GET    /api/match-videos
GET    /api/match-videos/{matchVideoId}
POST   /api/management/match-videos
PATCH  /api/management/match-videos/{matchVideoId}
DELETE /api/coach/match-videos/{matchVideoId}
```

`management` 경로는 `COACH`, `ANALYST`가 함께 사용하는 관리성 API라는 의미다.

삭제는 정책상 `COACH`만 가능하므로 `/api/coach` 경로로 분리한다.

## 6.2 경기 영상 목록 조회

```http
GET /api/match-videos?page=0&size=20
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`
- `PLAYER`

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

- `COACH`
- `ANALYST`
- `PLAYER`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 경기 영상 ID로 경기 영상을 조회한다.
3. 경기 영상이 존재하지 않거나 삭제된 경우 실패 처리한다.
4. 상세 정보를 반환한다.

응답 예시는 다음과 같다.

```json
{
  "matchVideoId": 1,
  "url": "https://example.com/videos/game-001.mp4",
  "title": "2026 춘계리그 vs 서울FC",
  "gameDate": "2026-06-20T15:00:00",
  "place": "메인 운동장",
  "homeScore": 2,
  "awayScore": 1,
  "matchResult": "WIN",
  "status": "READY",
  "uploaderId": 3,
  "uploaderName": "김지도",
  "createdAt": "2026-06-20T18:00:00",
  "updatedAt": "2026-06-20T18:00:00"
}
```

## 6.4 경기 영상 업로드

```http
POST /api/management/match-videos
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

요청 예시는 다음과 같다.

```json
{
  "url": "https://example.com/videos/game-001.mp4",
  "title": "2026 춘계리그 vs 서울FC",
  "gameDate": "2026-06-20T15:00:00",
  "place": "메인 운동장",
  "homeScore": 2,
  "awayScore": 1,
  "matchResult": "WIN",
  "status": "READY"
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 요청 필수값을 검증한다.
4. URL 형식을 검증한다.
5. 점수 값이 음수가 아닌지 검증한다.
6. 경기 결과 값이 허용된 Enum인지 검증한다.
7. 영상 상태 값이 허용된 Enum인지 검증한다.
8. `member_id`에 등록한 회원 ID를 저장한다.
9. 경기 영상 정보를 저장한다.
10. 생성된 경기 영상 ID를 반환한다.

## 6.5 경기 영상 수정

```http
PATCH /api/management/match-videos/{matchVideoId}
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

초기 구현에서는 부분 수정 방식보다 전체 수정 방식에 가깝게 처리한다.

요청 예시는 다음과 같다.

```json
{
  "url": "https://example.com/videos/game-001-fixed.mp4",
  "title": "2026 춘계리그 vs 서울FC",
  "gameDate": "2026-06-20T15:00:00",
  "place": "메인 운동장",
  "homeScore": 2,
  "awayScore": 1,
  "matchResult": "WIN",
  "status": "READY"
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 경기 영상 ID로 경기 영상을 조회한다.
4. 경기 영상이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 요청 값을 검증한다.
6. 경기 영상 정보를 수정한다.
7. 수정된 경기 영상 정보를 반환한다.

## 6.6 경기 영상 삭제

```http
DELETE /api/coach/match-videos/{matchVideoId}
```

사용 가능 역할은 다음과 같다.

- `COACH`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH`인지 확인한다.
3. 경기 영상 ID로 경기 영상을 조회한다.
4. 경기 영상이 존재하지 않거나 이미 삭제된 경우 실패 처리한다.
5. `is_deleted = true`로 변경한다.
6. 삭제 완료 응답을 반환한다.

---

## 7. DB 설계 방향

## 7.1 현재 사용할 테이블

초기 구현에서는 현재 DB의 `game_video_upload` 테이블을 그대로 사용한다.

| 컬럼 | 사용 목적 |
|---|---|
| `id` | 경기 영상 PK |
| `member_id` | 영상 등록 회원 ID |
| `url` | 실제 영상 접근 URL |
| `title` | 경기 제목 |
| `game_date` | 경기 날짜와 시간 |
| `place` | 경기 장소 |
| `home_score` | 홈팀 득점 |
| `away_score` | 원정팀 득점 |
| `match_result` | 경기 결과 |
| `status` | 영상 상태 |
| `is_deleted` | 소프트 삭제 여부 |
| `created_at` | 생성일시 |
| `updated_at` | 수정일시 |

## 7.2 Entity 매핑 방향

`game_video_upload` 테이블은 `GameVideoUploadEntity`로 매핑한다.

권장 필드명은 다음과 같다.

| DB 컬럼 | Entity 필드명 |
|---|---|
| `id` | `id` |
| `member_id` | `member` |
| `url` | `url` |
| `title` | `title` |
| `game_date` | `gameDate` |
| `place` | `place` |
| `home_score` | `homeScore` |
| `away_score` | `awayScore` |
| `match_result` | `matchResult` |
| `status` | `status` |
| `is_deleted` | `isDeleted` |
| `created_at` | `createdAt` |
| `updated_at` | `updatedAt` |

## 7.3 경기 결과 Enum

`match_result`는 Enum으로 관리한다.

초기 허용 값은 다음과 같다.

| 값 | 의미 |
|---|---|
| `WIN` | 승 |
| `DRAW` | 무 |
| `LOSS` | 패 |

## 7.4 영상 상태 Enum

`status`는 Enum으로 관리한다.

초기 허용 값은 다음과 같다.

| 값 | 의미 |
|---|---|
| `UPLOADING` | 업로드 또는 등록 중 |
| `READY` | 시청 가능 |
| `FAILED` | 등록 실패 |

초기에는 실제 파일 업로드가 아니라 URL 저장 방식이므로, 정상 등록 요청은 기본적으로 `READY`로 저장하는 것을 권장한다.

다만 요청에서 `status`를 받는다면 허용 값 검증을 반드시 해야 한다.

## 7.5 DB 변경 여부

초기 구현에서는 DB 컬럼을 추가하지 않는다.

현재 테이블만으로 다음 기능을 처리할 수 있다.

- 경기 영상 URL 저장
- 경기 기본 정보 저장
- 등록자 연결
- 경기 결과 저장
- 영상 상태 관리
- 소프트 삭제

추후 운영에서 필요해지면 다음 컬럼 또는 테이블을 검토한다.

- 상대팀명
- 대회명
- 경기 구분
- 전반/후반 구분 정보
- 영상 길이
- 썸네일 URL
- 파일 크기
- 원본 파일명
- 저장소 타입
- 영상 처리 작업 테이블
- 경기 영상 수정 이력 테이블

---

## 8. 요청/응답 DTO 방향

## 8.1 등록 요청 DTO

`CreateMatchVideoRequestDTO`

필드 방향은 다음과 같다.

- `url`
- `title`
- `gameDate`
- `place`
- `homeScore`
- `awayScore`
- `matchResult`
- `status`

초기 구현에서 `status`는 요청에서 받지 않고 서버에서 `READY`로 고정하는 방식도 가능하다. 단순성을 우선하면 서버 고정 방식을 권장한다.

## 8.2 수정 요청 DTO

`UpdateMatchVideoRequestDTO`

필드 방향은 다음과 같다.

- `url`
- `title`
- `gameDate`
- `place`
- `homeScore`
- `awayScore`
- `matchResult`
- `status`

## 8.3 목록 응답 DTO

`MatchVideoListResponseDTO`

필드 방향은 다음과 같다.

- `matchVideoId`
- `title`
- `gameDate`
- `place`
- `homeScore`
- `awayScore`
- `matchResult`
- `status`
- `createdAt`

## 8.4 상세 응답 DTO

`MatchVideoDetailResponseDTO`

필드 방향은 다음과 같다.

- `matchVideoId`
- `url`
- `title`
- `gameDate`
- `place`
- `homeScore`
- `awayScore`
- `matchResult`
- `status`
- `uploaderId`
- `uploaderName`
- `createdAt`
- `updatedAt`

## 8.5 페이지 응답 DTO

`MatchVideoPageResponseDTO`

Spring의 `Page` 객체를 그대로 노출하지 않고, 프론트에서 필요한 페이징 정보만 반환한다.

필드 방향은 다음과 같다.

- `matchVideos`
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
| 선수의 업로드 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 수정 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 삭제 API 호출 | 접근 차단 | `403 Forbidden` |
| 분석관의 삭제 API 호출 | 접근 차단 | `403 Forbidden` |
| 존재하지 않는 경기 영상 조회 | 조회 실패 | `404 Not Found` |
| 삭제된 경기 영상 조회 | 조회 실패 | `404 Not Found` |
| 삭제된 경기 영상 수정 | 수정 실패 | `404 Not Found` |
| 이미 삭제된 경기 영상 삭제 | 삭제 실패 | `404 Not Found` |
| 필수 입력값 누락 | 요청 실패 | `400 Bad Request` |
| URL 형식 오류 | 요청 실패 | `400 Bad Request` |
| 제목 길이 초과 | 요청 실패 | `400 Bad Request` |
| 장소 길이 초과 | 요청 실패 | `400 Bad Request` |
| 점수 값 음수 | 요청 실패 | `400 Bad Request` |
| 허용되지 않은 경기 결과 | 요청 실패 | `400 Bad Request` |
| 허용되지 않은 영상 상태 | 요청 실패 | `400 Bad Request` |
| 페이지 번호가 음수 | 요청 실패 | `400 Bad Request` |
| 페이지 크기가 허용 범위 초과 | 요청 실패 | `400 Bad Request` |

---

## 10. 구현 순서

## 10.1 백엔드 1단계: 기본 구조 확인

1. `GameVideoUploadEntity` 필드와 DB 컬럼 매핑 확인
2. `MemberEntity`와 `GameVideoUploadEntity` 연관관계 확인
3. `MatchResultEnum` 확인 또는 생성
4. `VideoUploadStatusEnum` 확인 또는 생성
5. `GameVideoUploadRepository` 생성
6. 기존 공통 예외 구조와 에러 코드 확인

## 10.2 백엔드 2단계: DTO 작성

1. `CreateMatchVideoRequestDTO` 작성
2. `UpdateMatchVideoRequestDTO` 작성
3. `MatchVideoListResponseDTO` 작성
4. `MatchVideoDetailResponseDTO` 작성
5. `MatchVideoPageResponseDTO` 작성

## 10.3 백엔드 3단계: 권한 검증 로직 작성

1. `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 사용
2. 경기 영상 업로드/수정 권한 검증 메서드 작성
3. `COACH`, `ANALYST`가 아니면 업로드/수정 실패 처리
4. 경기 영상 삭제 권한 검증 메서드 작성
5. `COACH`가 아니면 삭제 실패 처리
6. 조회는 `COACH`, `ANALYST`, `PLAYER` 허용
7. `isAdmin`만으로 영상 변경 권한을 주지 않도록 처리

## 10.4 백엔드 4단계: Service 작성

1. 경기 영상 등록 기능 작성
2. 경기 영상 목록 조회 기능 작성
3. 경기 영상 상세 조회 기능 작성
4. 경기 영상 수정 기능 작성
5. 경기 영상 삭제 기능 작성

## 10.5 백엔드 5단계: Controller 작성

1. 공통 조회 Controller 작성
2. 지도자/분석관 공통 관리 Controller 작성
3. 지도자 전용 삭제 Controller 작성
4. `@RequestParam`과 `@PathVariable`에는 이름을 명시한다.

## 10.6 백엔드 6단계: 테스트

1. 지도자 경기 영상 등록 성공 테스트
2. 분석관 경기 영상 등록 성공 테스트
3. 선수 경기 영상 등록 실패 테스트
4. 전체 역할 경기 영상 목록 조회 성공 테스트
5. 전체 역할 경기 영상 상세 조회 성공 테스트
6. 지도자 경기 영상 수정 성공 테스트
7. 분석관 경기 영상 수정 성공 테스트
8. 선수 경기 영상 수정 실패 테스트
9. 지도자 경기 영상 삭제 성공 테스트
10. 분석관 경기 영상 삭제 실패 테스트
11. 선수 경기 영상 삭제 실패 테스트
12. 삭제된 경기 영상 조회 제외 테스트
13. 잘못된 URL 요청 실패 테스트
14. 잘못된 경기 결과 요청 실패 테스트
15. 잘못된 영상 상태 요청 실패 테스트
16. 페이징 조회 테스트

---

## 11. 추후 확장 가능성

초기 구현에서는 URL 저장 기반 경기 영상 CRUD에 집중한다.

추후 확장 후보는 다음과 같다.

- 실제 영상 파일 업로드
- 클라우드 스토리지 연동
- 영상 인코딩 작업 큐
- 썸네일 자동 생성
- 영상 길이 자동 추출
- 경기 영상과 스케줄 연결
- 상대팀명, 대회명, 경기 구분 컬럼 추가
- 영상 수정 이력 관리
- 삭제 전 연결된 클립 존재 여부 확인
- 원본 영상 접근 URL 만료 처리
- 영상 재생 권한 URL 서명 처리
- 여러 팀을 받는 구조로 확장 시 `team_id` 추가

---

## 12. 주의사항

- 경기 영상 업로드, 수정, 삭제 권한은 프론트가 아니라 백엔드에서 반드시 검증한다.
- 선수는 경기 원본 영상 조회는 가능하지만 변경 권한은 없다.
- 분석관은 업로드와 수정은 가능하지만 삭제는 불가능하다.
- 삭제는 실제 삭제가 아니라 `is_deleted = true`로 처리한다.
- 조회 API는 삭제되지 않은 경기 영상만 반환한다.
- 초기에는 URL 저장 방식이므로 실제 파일 저장, 인코딩, 썸네일 생성은 구현 범위에서 제외한다.
- URL을 그대로 저장하더라도 운영 전에는 외부 유출 위험을 고려해야 한다.
- 현재 서비스는 단일 팀 기준이므로 `team_id`를 사용하지 않는다.
- 추후 여러 팀을 받을 경우 `game_video_upload` 테이블에 `team_id` 또는 팀 소속 구조가 필요하다.
- 원본 경기 영상은 이후 팀 분석 클립과 선수 개인 분석 클립의 기준 데이터이므로 삭제 정책을 신중하게 유지해야 한다.
