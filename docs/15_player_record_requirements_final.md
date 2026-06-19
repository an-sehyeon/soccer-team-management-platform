# 15. 선수 기록 관리 기능 요구사항

## 1. 결론

선수 기록 관리 기능은 지도자와 분석관이 경기별 선수 기록을 등록, 조회, 수정, 삭제하고, 선수는 본인 경기 기록만 조회할 수 있게 하는 백엔드 API 기능이다.

이번 최종 구현 기준은 다음과 같다.

- 인증은 기존 JWT 인증 구조를 사용한다.
- JWT 인증 후 `CustomUserPrincipal`의 `memberId`, `memberRole`, `isAdmin` 값을 사용한다.
- 선수 기록은 신규 `player_record` 테이블을 사용한다.
- 경기 영상 FK 컬럼명은 기존 영상/클립 테이블과 맞춰 `upload_id`를 사용한다.
- 기록 대상 선수는 `player_id`로 저장한다.
- 최초 작성자는 `recorder_id`로 저장한다.
- 마지막 수정자는 `last_modifier_id`로 저장한다.
- `recorder_id`는 최초 작성자를 의미하므로 수정 시 변경하지 않는다.
- `last_modifier_id`는 기록 수정 시 로그인한 지도자 또는 분석관으로 갱신한다.
- 지도자 `COACH`는 선수 기록 등록, 조회, 수정, 삭제가 가능하다.
- 분석관 `ANALYST`도 선수 기록 등록, 조회, 수정, 삭제가 가능하다.
- 선수 `PLAYER`는 본인 기록 목록과 상세만 조회할 수 있다.
- 선수는 다른 선수의 기록을 조회할 수 없다.
- 삭제는 실제 삭제가 아니라 `is_deleted = true`로 처리한다.
- 이번 범위에서는 경기당 선수 기록만 제공한다.
- 모든 경기 기록을 누적 합산하는 선수 기록 요약 API는 제외한다.
- 현재 단계에서는 프론트를 구현하지 않고 백엔드 API만 구현한다.

---

## 2. 기능 목적

선수 기록 관리 기능의 목적은 경기별로 선수의 주요 경기 기록을 저장하고, 지도자와 분석관이 선수별 경기 내용을 관리할 수 있게 하는 것이다.

이 기능은 단순 통계 기능이 아니라, 경기 영상과 선수 피드백 데이터를 연결하기 위한 기반 기능이다.

초기 구현에서는 다음 기준을 우선한다.

- 특정 경기 영상 기준으로 선수 기록을 저장한다.
- 특정 선수 기준으로 경기별 기록을 조회한다.
- 선수는 본인 기록만 확인한다.
- 지도자와 분석관은 모든 선수 기록을 관리한다.
- 추후 선수 상세 화면, 경기별 기록 비교, 시즌 누적 기록, 시각화 기능으로 확장 가능하게 한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

가능한 기능은 다음과 같다.

- 선수 기록 등록
- 선수 기록 목록 조회
- 선수 기록 상세 조회
- 선수 기록 수정
- 선수 기록 삭제

지도자는 선수 기록 전체를 관리할 수 있다.

### 3.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

- 선수 기록 등록
- 선수 기록 목록 조회
- 선수 기록 상세 조회
- 선수 기록 수정
- 선수 기록 삭제

이번 기능에서는 분석관도 선수 기록 삭제가 가능하다.

이유는 선수 기록은 영상 원본이나 분석 클립과 달리 관리 데이터 성격이 강하고, 분석관이 경기 기록을 정정해야 할 가능성이 있기 때문이다.

### 3.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

- 본인 선수 기록 목록 조회
- 본인 선수 기록 상세 조회

불가능한 기능은 다음과 같다.

- 다른 선수 기록 조회
- 선수 기록 등록
- 선수 기록 수정
- 선수 기록 삭제

선수 조회 API에서는 요청 파라미터로 `playerId`를 받지 않고, 로그인한 선수의 `memberId`를 기준으로 조회한다.

---

## 4. 권한 정책

| 역할 | 등록 | 목록 조회 | 상세 조회 | 수정 | 삭제 |
|---|---:|---:|---:|---:|---:|
| `COACH` | 가능 | 전체 가능 | 전체 가능 | 가능 | 가능 |
| `ANALYST` | 가능 | 전체 가능 | 전체 가능 | 가능 | 가능 |
| `PLAYER` | 불가 | 본인 기록만 가능 | 본인 기록만 가능 | 불가 | 불가 |

권한 검증은 반드시 백엔드에서 처리한다.

검증 기준은 다음과 같다.

- 인증되지 않은 사용자는 모든 선수 기록 API에 접근할 수 없다.
- `COACH`, `ANALYST`만 선수 기록 관리 API를 사용할 수 있다.
- `PLAYER`는 선수 본인 API만 사용할 수 있다.
- `PLAYER`는 본인 `memberId`와 `player_record.player_id`가 일치하는 기록만 조회할 수 있다.
- `PLAYER`가 다른 선수의 기록 ID로 상세 조회 API를 직접 호출하면 실패해야 한다.
- `isAdmin = true`만으로 선수 기록 관리 권한을 주지 않는다.
- 삭제된 기록은 목록/상세/수정/삭제 대상에서 제외한다.

---

## 5. 화면 흐름

현재 단계에서는 프론트를 구현하지 않는다.

다만 백엔드 API 응답은 추후 화면 구현을 고려해 설계한다.

### 5.1 지도자/분석관 선수 기록 관리 화면

1. 지도자 또는 분석관이 선수 기록 관리 화면에 진입한다.
2. 경기 영상 또는 선수를 기준으로 기록을 필터링한다.
3. 특정 경기와 선수를 선택해 기록을 등록한다.
4. 등록된 기록 목록을 조회한다.
5. 특정 기록을 선택하면 상세 기록을 확인한다.
6. 필요한 경우 기록을 수정한다.
7. 잘못 등록된 기록은 삭제한다.

### 5.2 선수 본인 기록 조회 화면

1. 선수가 로그인한다.
2. 내 기록 메뉴에 진입한다.
3. 본인의 경기별 기록 목록을 조회한다.
4. 특정 경기 기록을 선택한다.
5. 상세 기록을 확인한다.

---

## 6. API 흐름

### 6.1 최종 API 목록

```http
POST   /api/management/player-records
GET    /api/management/player-records
GET    /api/management/player-records/{recordId}
PATCH  /api/management/player-records/{recordId}
DELETE /api/management/player-records/{recordId}

GET    /api/player/me/player-records
GET    /api/player/me/player-records/{recordId}
```



---

### 6.2 선수 기록 등록

```http
POST /api/management/player-records
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

요청 예시는 다음과 같다.

```json
{
  "uploadId": 1,
  "playerId": 5,
  "minutesPlayed": 90,
  "goals": 1,
  "assists": 0,
  "shots": 3,
  "shotsOnTarget": 2,
  "passes": 45,
  "successfulPasses": 38,
  "dribbles": 4,
  "successfulDribbles": 2,
  "tackles": 3,
  "interceptions": 1,
  "clearances": 2,
  "saves": 0,
  "yellowCards": 0,
  "redCards": 0,
  "memo": "전반적으로 패스 선택이 좋았음"
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 요청의 `uploadId`, `playerId`가 null인지 확인한다.
4. 기록 수치가 `TINYINT UNSIGNED` 범위인 0~255 안에 있는지 확인한다.
5. 유효 슈팅 수가 전체 슈팅 수보다 크지 않은지 확인한다.
6. 패스 성공 수가 패스 시도 수보다 크지 않은지 확인한다.
7. 드리블 성공 수가 드리블 시도 수보다 크지 않은지 확인한다.
8. 경기 영상이 존재하고 삭제되지 않았는지 확인한다.
9. 대상 회원이 존재하고 삭제되지 않았으며 `PLAYER` 역할인지 확인한다.
10. 같은 `uploadId + playerId`의 활성 기록이 이미 있는지 확인한다.
11. `recorder_id`에 로그인 회원 ID를 저장한다.
12. `last_modifier_id`는 null로 저장한다.
13. 선수 기록을 저장한다.

응답 예시는 다음과 같다.

```json
{
  "recordId": 1,
  "message": "선수 기록이 등록되었습니다."
}
```

---

### 6.3 관리용 선수 기록 목록 조회

```http
GET /api/management/player-records?page=0&size=20&uploadId=1&playerId=5
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

요청 파라미터는 다음과 같다.

| 파라미터 | 필수 여부 | 설명 |
|---|---:|---|
| `page` | 선택 | 페이지 번호 |
| `size` | 선택 | 페이지 크기 |
| `uploadId` | 선택 | 경기 영상 ID |
| `playerId` | 선택 | 선수 회원 ID |

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 페이지 요청 값을 검증한다.
4. `uploadId`가 있으면 경기 영상을 조회한다.
5. `playerId`가 있으면 선수 회원을 조회한다.
6. 조건에 맞는 삭제되지 않은 선수 기록을 조회한다.
7. 최신 생성일 기준으로 정렬한다.
8. 페이지 응답을 반환한다.

---

### 6.4 관리용 선수 기록 상세 조회

```http
GET /api/management/player-records/{recordId}
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 기록 ID로 선수 기록을 조회한다.
4. 기록이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 상세 기록을 반환한다.

---

### 6.5 선수 기록 수정

```http
PATCH /api/management/player-records/{recordId}
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 기존 선수 기록을 조회한다.
4. 요청의 `uploadId`, `playerId`가 null인지 확인한다.
5. 기록 수치 범위와 성공 수 정합성을 검증한다.
6. 경기 영상이 존재하고 삭제되지 않았는지 확인한다.
7. 대상 회원이 존재하고 삭제되지 않았으며 `PLAYER` 역할인지 확인한다.
8. 자기 자신을 제외하고 같은 `uploadId + playerId` 활성 기록이 있는지 확인한다.
9. `recorder_id`는 변경하지 않는다.
10. `last_modifier_id`에 로그인 회원 ID를 저장한다.
11. 기록 값을 수정한다.
12. 수정된 상세 기록을 반환한다.

---

### 6.6 선수 기록 삭제

```http
DELETE /api/management/player-records/{recordId}
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 기록 ID로 선수 기록을 조회한다.
4. 기록이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. `is_deleted = true`로 변경한다.
6. `204 No Content`를 반환한다.

---

### 6.7 선수 본인 기록 목록 조회

```http
GET /api/player/me/player-records?page=0&size=20
```

사용 가능 역할은 다음과 같다.

- `PLAYER`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `PLAYER`인지 확인한다.
3. 로그인한 선수의 `memberId`를 조회 기준으로 사용한다.
4. 요청에서 별도 `playerId`를 받지 않는다.
5. `player_id = 로그인 회원 ID`이고 `is_deleted = false`인 기록만 조회한다.
6. 페이지 응답을 반환한다.

---

### 6.8 선수 본인 기록 상세 조회

```http
GET /api/player/me/player-records/{recordId}
```

사용 가능 역할은 다음과 같다.

- `PLAYER`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `PLAYER`인지 확인한다.
3. 기록 ID로 선수 기록을 조회한다.
4. 기록이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 기록의 `player_id`와 로그인 회원 ID가 같은지 확인한다.
6. 다르면 접근을 차단한다.
7. 본인 기록이면 상세 기록을 반환한다.

---



## 7. 예외 상황

| 상황 | ErrorCode | 권장 HTTP 상태 |
|---|---|---|
| 선수 기록이 없거나 삭제됨 | `PLAYER_RECORD_NOT_FOUND` | `404 Not Found` |
| 선수가 다른 선수 기록 조회 | `PLAYER_RECORD_ACCESS_DENIED` | `403 Forbidden` |
| 선수가 관리 API 호출 | `PLAYER_RECORD_MANAGE_ACCESS_DENIED` | `403 Forbidden` |
| 같은 경기/선수 활성 기록 중복 | `DUPLICATE_PLAYER_RECORD` | `409 Conflict` |
| 기록 대상이 선수 역할이 아님 | `INVALID_PLAYER_RECORD_PLAYER` | `400 Bad Request` |
| 기록 값이 올바르지 않음 | `INVALID_PLAYER_RECORD_VALUE` | `400 Bad Request` |
| 페이지 요청 값 오류 | `INVALID_PAGE_REQUEST` | `400 Bad Request` |

---



## 8. 테스트 완료 내용

사용자가 다음 항목의 정상 동작을 확인했다.

- 서버 정상 실행
- `player_record` 테이블 정상 생성
- 선수 기록 등록 정상 동작
- 중복 등록 차단 정상 동작
- 관리용 목록 조회 정상 동작
- 관리용 상세 조회 정상 동작
- 선수 기록 수정 정상 동작
- 선수 기록 삭제 정상 동작
- 선수 본인 목록 조회 정상 동작
- 선수 본인 상세 조회 정상 동작
- 선수의 다른 선수 기록 접근 차단 정상 동작
- 선수의 관리 API 접근 차단 정상 동작

---

## 9. 추후 확장 가능성

추후 확장 가능한 기능은 다음과 같다.

- 선수별 누적 기록 조회
- 시즌별 기록 합산
- 최근 N경기 평균 기록
- 경기별 기록 추이 그래프
- 포지션별 기록 항목 분리
- 선수 기록 수정 이력 테이블
- 기록 CSV 업로드
- 영상 클립과 기록 이벤트 연결
- AI 이벤트 분석 결과와 선수 기록 자동 연결

---

## 10. 다음 작업 방향

다음 작업은 신규 기능 구현이 아니라 서비스 계층 리팩토링이다.

목표 구조는 다음과 같다.

```text
backend/src/main/java/com/soccer/platform/service
 ├ common
 │  ├ PermissionValidator.java
 │  ├ MemberQueryService.java
 │  ├ MatchVideoQueryService.java
 │  └ PageRequestValidator.java
 │
 ├ playerrecord
 │  ├ PlayerRecordService.java
 │  └ PlayerRecordValidator.java
 │
 ├ ScheduleService.java
 ├ NoticeService.java
 ├ MatchVideoService.java
 ├ TeamAnalysisClipService.java
 └ ...
```

서비스 리팩토링은 사용자 기능 정책을 새로 만드는 작업이 아니므로 전체 요구사항 md 문서까지는 필수로 작성하지 않는다.

다만 작업 범위가 넓어질 수 있으므로 GitHub 이슈 또는 짧은 리팩토링 계획 문서를 작성한 뒤 진행한다.
