# 15. 선수 기록 관리 기능 요구사항

## 1. 결론

선수 기록 관리 기능은 경기별 선수 요약 기록을 관리하는 백엔드 API 기능이다.

이번 최종 기준에서 `player_record`는 경기별 선수 요약 기록으로 유지한다.

개별 장면 단위 기록은 `player_record_event`에서 관리한다.

기록 이벤트와 팀 분석 클립 또는 선수 개인 분석 클립의 연결은 `player_record_event_clip`에서 관리한다.

최신 구현 기준은 다음과 같다.

* 인증은 기존 JWT 인증 구조를 사용한다.
* JWT 인증 후 `CustomUserPrincipal`의 `memberId`, `memberRole`, `isAdmin` 값을 사용한다.
* 선수 기록 요약은 `player_record` 테이블을 사용한다.
* 선수 기록 이벤트는 `player_record_event` 테이블을 사용한다.
* 선수 기록 이벤트와 분석 클립 연결은 `player_record_event_clip` 테이블을 사용한다.
* `player_record`의 경기 영상 FK 컬럼명은 기존 영상/클립 테이블과 맞춰 `upload_id`를 사용한다.
* 기록 대상 선수는 `player_id`로 저장한다.
* 최초 작성자는 `recorder_id`로 저장한다.
* 마지막 수정자는 `last_modifier_id`로 저장한다.
* `recorder_id`는 최초 작성자를 의미하므로 수정 시 변경하지 않는다.
* `last_modifier_id`는 기록 수정 시 로그인한 지도자 또는 분석관으로 갱신한다.
* 지도자 `COACH`는 선수 기록 등록, 조회, 수정, 삭제가 가능하다.
* 분석관 `ANALYST`도 선수 기록 등록, 조회, 수정, 삭제가 가능하다.
* 선수 `PLAYER`는 본인 경기 기록만 조회할 수 있다.
* 선수는 다른 선수의 기록을 조회할 수 없다.
* 삭제는 실제 삭제가 아니라 `is_deleted = true`로 처리한다.
* 같은 `upload_id`, `player_id` 기준 활성 `player_record`는 1개만 유지한다.
* 선수 기록 이벤트 등록 시 기존 `player_record`가 있으면 재사용한다.
* 선수 기록 이벤트 등록 시 기존 `player_record`가 없으면 기본값 0으로 자동 생성한다.
* 기존 `eventTimeSec` 단일 시점 구조는 폐기한다.
* 선수 기록 이벤트 시간은 `eventStartTimeSec`, `eventEndTimeSec` 구간으로 저장한다.
* 이벤트 종료 시간은 원본 경기 영상의 `durationSec`을 초과할 수 없다.
* 이벤트 생성, 수정, 삭제 시 `player_record` 요약 수치를 자동 보정한다.
* 이번 문서는 선수 기록 기본 정책 문서다.
* 선수 기록 이벤트-클립 연결 상세 정책은 `docs/28_player_record_frontend_integration_requirements.md`를 함께 참고한다.

---

## 2. 기능 목적

선수 기록 관리 기능의 목적은 경기별로 선수의 주요 경기 기록을 저장하고, 지도자와 분석관이 선수별 경기 내용을 관리할 수 있게 하는 것이다.

이 기능은 단순 통계 기능이 아니라, 경기 영상과 선수 피드백 데이터를 연결하기 위한 기반 기능이다.

초기 구현에서는 다음 기준을 우선한다.

* 특정 경기 영상 기준으로 선수 기록을 저장한다.
* 특정 선수 기준으로 경기별 기록을 조회한다.
* 선수는 본인 기록만 확인한다.
* 지도자와 분석관은 모든 선수 기록을 관리한다.
* 선수 기록 요약과 개별 장면 이벤트를 분리한다.
* 선수 기록 이벤트는 원본 경기 영상 구간과 연결된다.
* 선수 기록 이벤트는 팀 분석 클립 또는 선수 개인 분석 클립과 선택적으로 연결될 수 있다.
* 추후 선수 상세 화면, 경기별 기록 비교, 시즌 누적 기록, 시각화 기능으로 확장 가능하게 한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

가능한 기능은 다음과 같다.

* 선수 기록 등록
* 선수 기록 목록 조회
* 선수 기록 상세 조회
* 선수 기록 수정
* 선수 기록 삭제
* 선수 기록 이벤트 등록
* 선수 기록 이벤트 목록 조회
* 선수 기록 이벤트 상세 조회
* 선수 기록 이벤트 수정
* 선수 기록 이벤트 삭제
* 선수 기록 이벤트와 분석 클립 연결

지도자는 선수 기록 전체를 관리할 수 있다.

---

### 3.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

* 선수 기록 등록
* 선수 기록 목록 조회
* 선수 기록 상세 조회
* 선수 기록 수정
* 선수 기록 삭제
* 선수 기록 이벤트 등록
* 선수 기록 이벤트 목록 조회
* 선수 기록 이벤트 상세 조회
* 선수 기록 이벤트 수정
* 선수 기록 이벤트 삭제
* 선수 기록 이벤트와 분석 클립 연결

이번 기능에서는 분석관도 선수 기록 삭제가 가능하다.

이유는 선수 기록은 영상 원본이나 분석 클립과 달리 관리 데이터 성격이 강하고, 분석관이 경기 기록을 정정해야 할 가능성이 있기 때문이다.

---

### 3.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

* 본인 선수 기록 목록 조회
* 본인 선수 기록 상세 조회
* 본인 선수 기록 이벤트 목록 조회
* 본인 선수 기록 이벤트 상세 조회

불가능한 기능은 다음과 같다.

* 다른 선수 기록 조회
* 선수 기록 등록
* 선수 기록 수정
* 선수 기록 삭제
* 선수 기록 이벤트 등록
* 선수 기록 이벤트 수정
* 선수 기록 이벤트 삭제

선수 조회 API에서는 요청 파라미터로 `playerId`를 받지 않고, 로그인한 선수의 `memberId`를 기준으로 조회한다.

---

## 4. 권한 정책

| 역할 | 기록 등록 | 기록 목록 조회 | 기록 상세 조회 | 기록 수정 | 기록 삭제 | 이벤트 등록 | 이벤트 조회 | 이벤트 수정 | 이벤트 삭제 |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `COACH` | 가능 | 전체 가능 | 전체 가능 | 가능 | 가능 | 가능 | 전체 가능 | 가능 | 가능 |
| `ANALYST` | 가능 | 전체 가능 | 전체 가능 | 가능 | 가능 | 가능 | 전체 가능 | 가능 | 가능 |
| `PLAYER` | 불가 | 본인만 가능 | 본인만 가능 | 불가 | 불가 | 불가 | 본인만 가능 | 불가 | 불가 |

권한 검증은 반드시 백엔드에서 처리한다.

검증 기준은 다음과 같다.

* 인증되지 않은 사용자는 모든 선수 기록 API에 접근할 수 없다.
* `COACH`, `ANALYST`만 선수 기록 관리 API를 사용할 수 있다.
* `PLAYER`는 선수 본인 API만 사용할 수 있다.
* `PLAYER`는 본인 `memberId`와 `player_record.player_id`가 일치하는 기록만 조회할 수 있다.
* `PLAYER`가 다른 선수의 기록 ID로 상세 조회 API를 직접 호출하면 실패해야 한다.
* `PLAYER`가 다른 선수의 기록 이벤트 ID로 상세 조회 API를 직접 호출하면 실패해야 한다.
* `isAdmin = true`만으로 선수 기록 관리 권한을 주지 않는다.
* 삭제된 기록은 목록/상세/수정/삭제 대상에서 제외한다.
* 삭제된 기록 이벤트는 목록/상세/수정/삭제 대상에서 제외한다.

---

## 5. 핵심 정책

## 5.1 `player_record` 역할

`player_record`는 경기별 선수 요약 기록이다.

`player_record`는 개별 장면이 아니라, 한 경기에서 특정 선수의 전체 기록 요약을 저장한다.

예시는 다음과 같다.

```text
A 경기에서 5번 선수의 전체 기록
→ 출전 시간, 골, 도움, 슈팅, 패스, 태클 등 요약 수치 저장
```

`player_record`는 다음 기준으로 하나의 활성 기록만 유지한다.

```text
upload_id + player_id + is_deleted = false
```

같은 경기 영상과 같은 선수에 대한 활성 `player_record`가 이미 있으면 중복 생성하지 않는다.

---

## 5.2 `player_record_event` 역할

`player_record_event`는 개별 장면/이벤트 기록이다.

예시는 다음과 같다.

```text
A 경기 125초~135초 구간에서 5번 선수가 슈팅
A 경기 400초~420초 구간에서 5번 선수가 성공 패스
A 경기 720초~730초 구간에서 5번 선수가 태클
```

`player_record_event`는 반드시 하나의 `player_record`에 연결된다.

`player_record_event`에는 `upload_id`, `player_id`를 직접 저장하지 않는다.

경기 영상과 선수 정보는 다음 경로로 확인한다.

```text
player_record_event.player_record_id
→ player_record.id
→ player_record.upload_id
→ player_record.player_id
```

---

## 5.3 `player_record_event_clip` 역할

`player_record_event_clip`은 선수 기록 이벤트와 분석 클립을 연결하는 선택 테이블이다.

기록 이벤트는 클립 없이도 존재할 수 있다.

기록 이벤트는 다음 분석 클립과 선택적으로 연결할 수 있다.

```text
TEAM_ANALYSIS
→ team_video_clip 연결

PLAYER_ANALYSIS
→ player_video_clip 연결
```

연결 상세 정책은 `docs/28_player_record_frontend_integration_requirements.md`를 기준으로 한다.

---

## 5.4 기존 `eventTimeSec` 폐기 정책

초기 설계의 `eventTimeSec`는 폐기한다.

기록 이벤트는 한 순간이 아니라 영상 구간으로 관리한다.

따라서 다음 필드를 사용한다.

```text
eventStartTimeSec
eventEndTimeSec
```

DB 컬럼은 다음과 같다.

```text
event_start_time_sec
event_end_time_sec
```

검증 기준은 다음과 같다.

```text
eventStartTimeSec >= 0
eventEndTimeSec > eventStartTimeSec
eventEndTimeSec <= game_video_upload.duration_sec
```

원본 경기 영상의 `durationSec`이 없는 경우 선수 기록 이벤트를 생성하거나 수정할 수 없다.

---

## 5.5 `value` 정책

`value`는 해당 이벤트를 선수 기록 요약 수치에 몇 개로 반영할지 나타내는 값이다.

초기 MVP에서는 대부분 `1`을 사용한다.

예시는 다음과 같다.

```json
{
  "eventType": "GOAL",
  "eventStartTimeSec": 240,
  "eventEndTimeSec": 250,
  "value": 1
}
```

의미는 다음과 같다.

```text
240초~250초 구간에 득점 이벤트가 있고,
player_record.goals를 1 증가시킨다.
```

특정 구간에서 패스 3회를 한 번에 등록해야 한다면 다음처럼 확장할 수 있다.

```json
{
  "eventType": "PASS",
  "eventStartTimeSec": 600,
  "eventEndTimeSec": 620,
  "value": 3
}
```

초기 프론트에서는 `value`를 기본값 1로 처리해도 된다.

---

## 6. 화면 흐름

현재 15번 기능의 최초 구현 단계에서는 프론트를 구현하지 않았다.

다만 백엔드 API 응답은 추후 화면 구현을 고려해 설계한다.

선수 기록 이벤트-클립 연결 프론트 상세 흐름은 `docs/28_player_record_frontend_integration_requirements.md`에서 관리한다.

---

### 6.1 지도자/분석관 선수 기록 관리 화면

1. 지도자 또는 분석관이 선수 기록 관리 화면에 진입한다.
2. 경기 영상 또는 선수를 기준으로 기록을 필터링한다.
3. 특정 경기와 선수를 선택해 기록을 등록한다.
4. 등록된 기록 목록을 조회한다.
5. 특정 기록을 선택하면 상세 기록을 확인한다.
6. 필요한 경우 기록을 수정한다.
7. 잘못 등록된 기록은 삭제한다.
8. 선수 기록 상세에서 개별 이벤트 목록을 확인한다.
9. 필요한 경우 이벤트를 등록, 수정, 삭제한다.
10. 이벤트를 팀 분석 클립 또는 선수 개인 분석 클립과 연결한다.

---

### 6.2 선수 본인 기록 조회 화면

1. 선수가 로그인한다.
2. 내 기록 메뉴에 진입한다.
3. 본인의 경기별 기록 목록을 조회한다.
4. 특정 경기 기록을 선택한다.
5. 상세 기록을 확인한다.
6. 본인 기록에 연결된 이벤트 목록을 확인한다.
7. 이벤트 상세와 연결 클립 정보를 확인한다.

---

## 7. API 흐름

## 7.1 선수 기록 기본 API 목록

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

## 7.2 선수 기록 이벤트 API 목록

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

## 7.3 선수 기록 등록

```http
POST /api/management/player-records
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

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

## 7.4 관리용 선수 기록 목록 조회

```http
GET /api/management/player-records?page=0&size=20&uploadId=1&playerId=5
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

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

## 7.5 관리용 선수 기록 상세 조회

```http
GET /api/management/player-records/{recordId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 기록 ID로 선수 기록을 조회한다.
4. 기록이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 상세 기록을 반환한다.

---

## 7.6 선수 기록 수정

```http
PATCH /api/management/player-records/{recordId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

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

주의사항은 다음과 같다.

* 선수 기록 기본 수정 API는 요약 수치를 직접 수정하는 API다.
* 선수 기록 이벤트 수정 API를 통해 요약 수치가 자동 보정될 수도 있다.
* 운영상 같은 데이터를 동시에 수정하는 흐름이 생기면 충돌 방지 정책을 추가 검토한다.

---

## 7.7 선수 기록 삭제

```http
DELETE /api/management/player-records/{recordId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 기록 ID로 선수 기록을 조회한다.
4. 기록이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. `is_deleted = true`로 변경한다.
6. `204 No Content`를 반환한다.

주의사항은 다음과 같다.

* `player_record` 삭제 시 해당 기록에 연결된 이벤트 처리 정책은 별도 검토 대상이다.
* 현재 선수 기록 이벤트 삭제는 이벤트 API에서 별도로 처리한다.
* 운영 안정성을 위해 선수 기록 삭제 전 연결 이벤트 존재 여부를 확인하는 정책을 추후 추가할 수 있다.

---

## 7.8 선수 본인 기록 목록 조회

```http
GET /api/player/me/player-records?page=0&size=20
```

사용 가능 역할은 다음과 같다.

* `PLAYER`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `PLAYER`인지 확인한다.
3. 로그인한 선수의 `memberId`를 조회 기준으로 사용한다.
4. 요청에서 별도 `playerId`를 받지 않는다.
5. `player_id = 로그인 회원 ID`이고 `is_deleted = false`인 기록만 조회한다.
6. 페이지 응답을 반환한다.

---

## 7.9 선수 본인 기록 상세 조회

```http
GET /api/player/me/player-records/{recordId}
```

사용 가능 역할은 다음과 같다.

* `PLAYER`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `PLAYER`인지 확인한다.
3. 기록 ID로 선수 기록을 조회한다.
4. 기록이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 기록의 `player_id`와 로그인 회원 ID가 같은지 확인한다.
6. 다르면 접근을 차단한다.
7. 본인 기록이면 상세 기록을 반환한다.

---

## 8. 선수 기록 이벤트 확장 정책

## 8.1 이벤트 등록

```http
POST /api/management/player-record-events
```

요청 예시는 다음과 같다.

```json
{
  "uploadId": 1,
  "playerId": 5,
  "eventType": "SHOT",
  "eventStartTimeSec": 125,
  "eventEndTimeSec": 135,
  "value": 1,
  "eventMemo": "페널티 박스 안 오른발 슈팅"
}
```

처리 흐름은 다음과 같다.

1. `COACH` 또는 `ANALYST` 권한을 확인한다.
2. 요청값을 검증한다.
3. 경기 영상을 조회한다.
4. 경기 영상 `durationSec` 기준으로 이벤트 시간 구간을 검증한다.
5. 대상 선수가 `PLAYER`인지 검증한다.
6. 같은 `uploadId`, `playerId`의 활성 `player_record`를 조회한다.
7. 기존 `player_record`가 없으면 기본값 0으로 자동 생성한다.
8. `player_record_event`를 저장한다.
9. 이벤트 유형과 `value` 기준으로 `player_record` 요약 수치를 증가시킨다.
10. 응답을 반환한다.

---

## 8.2 이벤트 + 클립 연결 등록

```http
POST /api/management/player-record-events/with-clip-link
```

팀 분석 클립 연결 요청 예시는 다음과 같다.

```json
{
  "uploadId": 1,
  "playerId": 5,
  "eventType": "TACKLE",
  "eventStartTimeSec": 300,
  "eventEndTimeSec": 312,
  "value": 1,
  "eventMemo": "중앙 압박 후 태클 성공",
  "clipSourceType": "TEAM_ANALYSIS",
  "teamClipId": 5,
  "playerClipId": null
}
```

선수 개인 분석 클립 연결 요청 예시는 다음과 같다.

```json
{
  "uploadId": 1,
  "playerId": 5,
  "eventType": "SUCCESSFUL_PASS",
  "eventStartTimeSec": 400,
  "eventEndTimeSec": 420,
  "value": 1,
  "eventMemo": "빌드업 구간 전진 패스 성공",
  "clipSourceType": "PLAYER_ANALYSIS",
  "teamClipId": null,
  "playerClipId": 8
}
```

연결 검증 기준은 다음과 같다.

```text
TEAM_ANALYSIS
→ player_record.upload_id = team_video_clip.upload_id

PLAYER_ANALYSIS
→ player_record.upload_id = player_video_clip.upload_id
→ player_record.player_id = player_video_clip.player_id
```

---

## 8.3 이벤트 목록 조회

관리용 API는 다음과 같다.

```http
GET /api/management/player-records/{recordId}/events
```

선수 본인 API는 다음과 같다.

```http
GET /api/player/me/player-records/{recordId}/events
```

정렬 기준은 다음과 같다.

```text
eventStartTimeSec ASC
createdAt ASC
```

---

## 8.4 이벤트 상세 조회

관리용 API는 다음과 같다.

```http
GET /api/management/player-record-events/{eventId}
```

선수 본인 API는 다음과 같다.

```http
GET /api/player/me/player-record-events/{eventId}
```

---

## 8.5 이벤트 수정

```http
PATCH /api/management/player-record-events/{eventId}
```

요청 예시는 다음과 같다.

```json
{
  "eventType": "ASSIST",
  "eventStartTimeSec": 120,
  "eventEndTimeSec": 132,
  "value": 1,
  "eventMemo": "컷백 패스 도움으로 수정"
}
```

수정 정책은 다음과 같다.

* 이벤트 유형을 수정할 수 있다.
* 이벤트 시작/종료 시간을 수정할 수 있다.
* `value`를 수정할 수 있다.
* `eventMemo`를 수정할 수 있다.
* 클립 연결 수정은 초기 버전에서 제외한다.

수정 시 요약 수치 보정 흐름은 다음과 같다.

```text
1. 기존 이벤트 유형/value 기준으로 player_record 요약 수치 차감
2. 이벤트 내용 수정
3. 수정된 이벤트 유형/value 기준으로 player_record 요약 수치 증가
```

---

## 8.6 이벤트 삭제

```http
DELETE /api/management/player-record-events/{eventId}
```

삭제 정책은 다음과 같다.

```text
player_record_event.is_deleted = true
연결된 player_record_event_clip.is_deleted = true
player_record 요약 수치 차감
```

실제 row는 삭제하지 않는다.

---

## 9. 이벤트 유형별 요약 수치 반영 정책

이벤트 생성 시 `player_record` 요약 수치를 증가시킨다.

이벤트 수정 시 기존 이벤트 수치를 차감한 뒤 수정된 이벤트 수치를 다시 반영한다.

이벤트 삭제 시 기존 이벤트 수치를 차감한다.

반영 기준은 다음과 같다.

```text
GOAL
→ goals 증가

ASSIST
→ assists 증가

SHOT
→ shots 증가

SHOT_ON_TARGET
→ shots 증가
→ shotsOnTarget 증가

PASS
→ passes 증가

SUCCESSFUL_PASS
→ passes 증가
→ successfulPasses 증가

DRIBBLE
→ dribbles 증가

SUCCESSFUL_DRIBBLE
→ dribbles 증가
→ successfulDribbles 증가

TACKLE
→ tackles 증가

INTERCEPTION
→ interceptions 증가

CLEARANCE
→ clearances 증가

SAVE
→ saves 증가

YELLOW_CARD
→ yellowCards 증가

RED_CARD
→ redCards 증가

ETC
→ 요약 수치에는 반영하지 않음
```

초기 MVP에서는 `GOAL`이 자동으로 `shots`, `shotsOnTarget`까지 증가하지 않는다.

자동 연쇄 증가는 추후 통계 정책이 확정되면 별도 기능으로 검토한다.

---

## 10. DB 설계

## 10.1 `player_record`

`player_record`는 기존 테이블을 유지한다.

역할은 경기별 선수 요약 기록이다.

주요 컬럼은 다음과 같다.

```text
id
upload_id
player_id
recorder_id
last_modifier_id
minutes_played
goals
assists
shots
shots_on_target
passes
successful_passes
dribbles
successful_dribbles
tackles
interceptions
clearances
saves
yellow_cards
red_cards
memo
is_deleted
created_at
updated_at
```

인덱스 정책은 다음과 같다.

```text
idx_player_record_upload_player_deleted
→ upload_id, player_id, is_deleted

idx_player_record_player_deleted
→ player_id, is_deleted

idx_player_record_recorder_deleted
→ recorder_id, is_deleted

idx_player_record_last_modifier_deleted
→ last_modifier_id, is_deleted
```

---

## 10.2 `player_record_event`

`player_record_event`는 선수 기록 이벤트 테이블이다.

역할은 경기 영상 구간 기준 개별 선수 기록 이벤트 저장이다.

주요 컬럼은 다음과 같다.

```text
id
player_record_id
event_type
event_start_time_sec
event_end_time_sec
value
memo
created_by
is_deleted
created_at
updated_at
```

`upload_id`, `player_id`는 직접 저장하지 않는다.

---

## 10.3 `player_record_event_clip`

`player_record_event_clip`은 선수 기록 이벤트와 분석 클립의 선택 연결 테이블이다.

주요 컬럼은 다음과 같다.

```text
id
player_record_event_id
clip_source_type
team_clip_id
player_clip_id
is_deleted
created_at
updated_at
```

`clip_source_type = TEAM_ANALYSIS`이면 `team_clip_id`를 사용한다.

`clip_source_type = PLAYER_ANALYSIS`이면 `player_clip_id`를 사용한다.

---

## 11. 예외 상황

선수 기록 기본 예외는 다음과 같다.

| 상황 | ErrorCode | 권장 HTTP 상태 |
|---|---|---|
| 선수 기록이 없거나 삭제됨 | `PLAYER_RECORD_NOT_FOUND` | `404 Not Found` |
| 선수가 다른 선수 기록 조회 | `PLAYER_RECORD_ACCESS_DENIED` | `403 Forbidden` |
| 선수가 관리 API 호출 | `PLAYER_RECORD_MANAGE_ACCESS_DENIED` | `403 Forbidden` |
| 같은 경기/선수 활성 기록 중복 | `DUPLICATE_PLAYER_RECORD` | `409 Conflict` |
| 기록 대상이 선수 역할이 아님 | `INVALID_PLAYER_RECORD_PLAYER` | `400 Bad Request` |
| 기록 값이 올바르지 않음 | `INVALID_PLAYER_RECORD_VALUE` | `400 Bad Request` |
| 페이지 요청 값 오류 | `INVALID_PAGE_REQUEST` | `400 Bad Request` |

선수 기록 이벤트 예외는 다음과 같다.

| 상황 | ErrorCode | 권장 HTTP 상태 |
|---|---|---|
| 이벤트가 없거나 삭제됨 | `PLAYER_RECORD_EVENT_NOT_FOUND` | `404 Not Found` |
| 이벤트 클립 연결이 없거나 삭제됨 | `PLAYER_RECORD_EVENT_CLIP_NOT_FOUND` | `404 Not Found` |
| 선수가 타인 기록 이벤트 조회 | `PLAYER_RECORD_EVENT_ACCESS_DENIED` | `403 Forbidden` |
| 선수가 이벤트 관리 API 호출 | `PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED` | `403 Forbidden` |
| 이벤트 요청값 오류 | `INVALID_PLAYER_RECORD_EVENT_REQUEST` | `400 Bad Request` |
| 이벤트 유형 오류 | `INVALID_PLAYER_RECORD_EVENT_TYPE` | `400 Bad Request` |
| 클립 출처 유형 오류 | `INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE` | `400 Bad Request` |
| 이벤트 시간 오류 | `INVALID_PLAYER_RECORD_EVENT_TIME` | `400 Bad Request` |
| 이벤트 value 오류 | `INVALID_PLAYER_RECORD_EVENT_VALUE` | `400 Bad Request` |
| 클립 경기 영상 불일치 | `PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH` | `400 Bad Request` |
| 선수 개인 분석 클립 대상 선수 불일치 | `PLAYER_RECORD_EVENT_PLAYER_MISMATCH` | `400 Bad Request` |
| 경기 영상 길이 미추출 | `MATCH_VIDEO_DURATION_NOT_READY` | `400 Bad Request` |

---

## 12. 테스트 완료 내용

선수 기록 기본 기능에서 사용자가 다음 항목의 정상 동작을 확인했다.

* 서버 정상 실행
* `player_record` 테이블 정상 생성
* 선수 기록 등록 정상 동작
* 중복 등록 차단 정상 동작
* 관리용 목록 조회 정상 동작
* 관리용 상세 조회 정상 동작
* 선수 기록 수정 정상 동작
* 선수 기록 삭제 정상 동작
* 선수 본인 목록 조회 정상 동작
* 선수 본인 상세 조회 정상 동작
* 선수의 다른 선수 기록 접근 차단 정상 동작
* 선수의 관리 API 접근 차단 정상 동작

선수 기록 이벤트 기능에서 사용자가 다음 항목의 정상 동작을 확인했다.

* 서버 정상 실행
* `player_record_event` 테이블 정상 생성
* `player_record_event_clip` 테이블 정상 생성
* 클립 없는 선수 기록 이벤트 등록 정상 동작
* 팀 분석 클립 연결 이벤트 등록 정상 동작
* 선수 개인 분석 클립 연결 이벤트 등록 정상 동작
* 관리용 이벤트 목록 조회 정상 동작
* 관리용 이벤트 상세 조회 정상 동작
* 이벤트 수정 시 `player_record` 요약 수치 보정 정상 동작
* 이벤트 삭제 시 이벤트/연결 클립 소프트 삭제 및 요약 수치 차감 정상 동작
* 선수 본인 이벤트 조회 정상 동작
* 원본 영상 길이 기준 이벤트 시간 검증 정상 동작

---

## 13. 추후 확장 가능성

추후 확장 가능한 기능은 다음과 같다.

* 선수별 누적 기록 조회
* 시즌별 기록 합산
* 최근 N경기 평균 기록
* 경기별 기록 추이 그래프
* 포지션별 기록 항목 분리
* 선수 기록 수정 이력 테이블
* 기록 CSV 업로드
* 기록 이벤트별 코치 피드백
* 선수 피드백 확인 여부
* 이벤트 구간 클릭 시 원본 경기 영상 해당 구간 재생
* 이벤트와 연결된 팀/개인 분석 클립 바로 재생
* 선수 기록 이벤트 타임라인 UI
* 이벤트 유형별 필터
* 경기별 선수 이벤트 히트맵
* AI 이벤트 자동 생성
* AI 이벤트와 수동 이벤트 비교
* AI 이벤트 분석 결과와 선수 기록 자동 연결

---

## 14. 관련 문서

관련 문서는 다음과 같다.

```text
docs/15_player_record_requirements_final.md
→ 선수 기록 관리 기본 정책 문서

docs/28_player_record_frontend_integration_requirements.md
→ 선수 기록 이벤트-클립 연결 및 프론트 연동 상세 요구사항 문서

docs/00_current_backend_policy.md
→ 최신 백엔드 정책 요약 문서

docs/00_project_context_for_chatgpt.md
→ ChatGPT 작업 컨텍스트 요약 문서
```

---

## 15. 다음 작업 방향

다음 작업은 선수 기록 이벤트-클립 연결 프론트 연동이다.

다음 작업의 핵심 방향은 다음과 같다.

* 선수 기록 관리 프론트 화면에 선수 기록 이벤트 목록을 추가한다.
* 지도자/분석관이 선수 기록 상세에서 이벤트를 등록, 조회, 수정, 삭제할 수 있게 한다.
* 이벤트 등록 시 원본 경기 영상의 현재 재생 구간을 `eventStartTimeSec`, `eventEndTimeSec`로 저장할 수 있게 한다.
* 이벤트를 팀 분석 클립 또는 선수 개인 분석 클립과 선택적으로 연결할 수 있게 한다.
* 선수는 본인 기록 이벤트만 조회할 수 있게 한다.
* 프론트 타입은 백엔드 DTO 필드명과 정확히 맞춘다.
* API 주소는 백엔드 Controller 기준으로 작성한다.
* 권한 검증은 반드시 백엔드에서 처리하고, 프론트는 사용자 경험을 위해 버튼과 영역만 숨긴다.
