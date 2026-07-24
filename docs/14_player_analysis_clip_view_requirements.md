# 선수 개인 분석 클립 조회 기록 기능 요구사항

> [!IMPORTANT]
> 이 문서는 해당 기능의 기존 구현 당시 기준과 작업 이력을 보존하는 문서다.
> 경기 영상 편집기 v1의 신규 구현은 `docs/31_video_editor_v1_requirements.md`,
> `docs/00_current_backend_policy.md`, `docs/00_current_frontend_policy.md`를 우선한다.
> 역할 권한, 클립 상태, 시간 단위, 삭제, 드로잉 합성, 영상 제공 방식이 충돌하면 최신 문서를 적용한다.
> 기존 API는 프론트 전환 전 호환 목적으로 유지될 수 있으므로 실제 제거 여부는 현재 소스를 확인한다.


## 1. 결론

선수 개인 분석 클립 조회 기록 기능은 선수가 본인 개인 분석 클립을 상세 조회했을 때 조회 여부와 조회 횟수를 기록하고, 지도자와 분석관이 선수별 피드백 확인 상태를 파악할 수 있게 하는 기능이다.

초기 구현은 다음 정책을 기준으로 진행한다.

- 인증은 기존 JWT 인증 구조를 사용한다.
- JWT 인증 후 `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 값을 사용한다.
- 조회 기록은 현재 DB의 `player_video_clip_view` 테이블을 사용한다.
- DB의 `player_video_clip_view.is_deleted` 컬럼은 유지한다.
- 이번 MVP에서는 조회 기록 삭제 API를 제공하지 않는다.
- 선수 `PLAYER`가 본인 개인 분석 클립 상세 조회 API를 호출했을 때만 조회 기록을 저장 또는 갱신한다.
- 선수 개인 분석 클립 목록 조회만으로는 조회 기록을 저장하지 않는다.
- 지도자 `COACH`와 분석관 `ANALYST`가 개인 분석 클립을 조회하는 경우에는 선수 조회 기록으로 저장하지 않는다.
- 최초 조회 시 `firstViewedAt`, `lastViewedAt`, `viewCount = 1`로 저장한다.
- 이미 조회 기록이 있으면 `lastViewedAt`을 현재 시간으로 갱신하고 `viewCount`를 1 증가시킨다.
- `viewCount`가 존재하면 해당 클립을 본 것으로 판단한다.
- `viewCount` 값으로 몇 번 봤는지 확인한다.
- 지도자 `COACH`와 분석관 `ANALYST`는 특정 선수의 개인 분석 클립 조회 기록을 확인할 수 있다.
- 선수 `PLAYER`는 본인의 조회 기록만 확인할 수 있다.
- 선수 `PLAYER`는 다른 선수의 조회 기록을 볼 수 없다.
- 특정 개인 분석 클립 기준 조회 기록 목록 API는 제공하지 않는다.
- 현재 단계에서는 프론트를 구현하지 않고 백엔드 API 기능만 개발한다.

---

## 2. 기능 목적

선수 개인 분석 클립 조회 기록 기능의 목적은 지도자와 분석관이 선수에게 공유한 개인 피드백 클립을 선수가 실제로 확인했는지 파악할 수 있게 하는 것이다.

선수 개인 분석 클립은 특정 선수에게만 공유되는 개인 피드백 자료다.

따라서 실제 팀 운영에서는 다음 확인이 필요하다.

- 선수가 개인 피드백 영상을 처음 확인했는지
- 선수가 마지막으로 언제 확인했는지
- 선수가 같은 클립을 몇 번 다시 봤는지
- 지도자와 분석관이 피드백 전달 이후 후속 지도를 할 수 있는지

이 기능은 단순 조회수 기능이 아니라, 선수별 피드백 확인 상태 관리 기능으로 본다.

조회 기록은 피드백 확인 여부를 판단하는 기준 데이터이므로 일반 사용자 기능에서 삭제하지 않는다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

가능한 기능은 다음과 같다.

- 특정 선수의 개인 분석 클립 조회 기록 목록 조회

불가능한 기능은 다음과 같다.

- 선수 개인 분석 클립 조회 기록 직접 생성
- 선수 개인 분석 클립 조회 기록 직접 수정
- 선수 개인 분석 클립 조회 기록 삭제
- 특정 개인 분석 클립 기준 조회 기록 목록 조회

지도자는 선수 개인 피드백 관리 책임자이므로 선수별 조회 기록을 확인할 수 있다.

단, 조회 기록은 실제 피드백 확인 근거이므로 이번 MVP에서는 지도자에게도 삭제 기능을 제공하지 않는다.

### 3.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

- 특정 선수의 개인 분석 클립 조회 기록 목록 조회

불가능한 기능은 다음과 같다.

- 선수 개인 분석 클립 조회 기록 직접 생성
- 선수 개인 분석 클립 조회 기록 직접 수정
- 선수 개인 분석 클립 조회 기록 삭제
- 특정 개인 분석 클립 기준 조회 기록 목록 조회

분석관은 피드백 확인 상태를 확인할 수 있지만, 조회 기록을 조작할 수 없다.

### 3.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

- 본인 개인 분석 클립 상세 조회 시 조회 기록 자동 저장 또는 갱신
- 본인의 개인 분석 클립 조회 기록 목록 조회

불가능한 기능은 다음과 같다.

- 다른 선수의 조회 기록 조회
- 조회 기록 직접 생성
- 조회 기록 직접 수정
- 조회 기록 삭제

선수는 조회 기록을 별도 API로 생성하지 않는다.

선수가 본인 개인 분석 클립 상세 조회 API를 호출하면 서버가 자동으로 조회 기록을 저장 또는 갱신한다.

---

## 4. 권한 정책

| 역할 | 조회 기록 자동 저장 | 선수별 조회 | 본인 기록 조회 | 직접 생성/수정/삭제 |
|---|---:|---:|---:|---:|
| `COACH` | 불가 | 가능 | 해당 없음 | 불가 |
| `ANALYST` | 불가 | 가능 | 해당 없음 | 불가 |
| `PLAYER` | 가능 | 불가 | 가능 | 불가 |

권한 검증은 반드시 백엔드에서 처리한다.

검증 기준은 다음과 같다.

- 인증되지 않은 사용자는 모든 조회 기록 API에 접근할 수 없다.
- `PLAYER`가 본인 개인 분석 클립 상세 조회를 했을 때만 조회 기록을 저장 또는 갱신한다.
- `COACH`, `ANALYST`가 개인 분석 클립 상세 조회를 해도 `player_video_clip_view`에는 기록하지 않는다.
- `COACH`, `ANALYST`는 특정 선수의 삭제되지 않은 조회 기록을 조회할 수 있다.
- `PLAYER`는 `member_id = 로그인 회원 ID`인 조회 기록만 조회할 수 있다.
- `PLAYER`가 다른 선수의 조회 기록 목록 API를 직접 호출해도 실패해야 한다.
- 이번 MVP에서는 조회 기록 삭제 API를 제공하지 않는다.
- `isAdmin = true`만으로 조회 기록 조회 권한을 주지 않는다.

---

## 5. 조회 기록 저장 정책

### 5.1 저장 시점

조회 기록은 선수 본인 개인 분석 클립 상세 조회 API에서만 저장 또는 갱신한다.

대상 API는 다음이다.

```http
GET /api/player/me/player-analysis-clips/{playerClipId}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `PLAYER`인지 확인한다.
3. `playerClipId`로 선수 개인 분석 클립을 조회한다.
4. 클립이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 클립의 `player_id`와 로그인 선수의 `memberId`가 일치하는지 확인한다.
6. 연결된 원본 경기 영상이 삭제되지 않았는지 확인한다.
7. 권한 검증이 통과하면 조회 기록을 저장 또는 갱신한다.
8. 개인 분석 클립 상세 응답을 반환한다.

### 5.2 최초 조회 처리

조회 기록이 없으면 새로 저장한다.

저장 값은 다음과 같다.

| 컬럼 | 저장 값 |
|---|---|
| `player_video_clip_id` | 조회한 개인 분석 클립 ID |
| `member_id` | 로그인한 선수 회원 ID |
| `first_viewed_at` | 현재 시간 |
| `last_viewed_at` | 현재 시간 |
| `view_count` | `1` |
| `is_deleted` | `false` |

### 5.3 재조회 처리

이미 조회 기록이 있으면 기존 데이터를 갱신한다.

갱신 값은 다음과 같다.

| 컬럼 | 처리 방식 |
|---|---|
| `first_viewed_at` | 변경하지 않음 |
| `last_viewed_at` | 현재 시간으로 갱신 |
| `view_count` | 기존 값에서 1 증가 |
| `is_deleted` | `false` 유지 |

### 5.4 `is_deleted` 컬럼 처리 정책

이번 MVP에서는 조회 기록 삭제 API를 제공하지 않는다.

따라서 일반 서비스 흐름에서는 `player_video_clip_view.is_deleted`가 `true`로 바뀌는 일이 없다.

다만 DB에는 `is_deleted` 컬럼을 유지한다.

이유는 다음과 같다.

- 기존 DB 구조를 크게 변경하지 않아도 된다.
- 추후 운영자 전용 기록 정리 기능이 필요해질 수 있다.
- 잘못 쌓인 테스트 데이터나 버그 데이터 처리 여지를 남길 수 있다.
- 다른 테이블과 동일한 소프트 삭제 구조를 유지할 수 있다.

저장/갱신 로직에서는 방어적으로 다음 정책을 적용한다.

- 기존 조회 기록이 없으면 새로 저장한다.
- 기존 조회 기록이 있고 `isDeleted = false`이면 `lastViewedAt`, `viewCount`를 갱신한다.
- 기존 조회 기록이 있고 `isDeleted = true`이면 예외 데이터로 보고 `isDeleted = false`로 복구한 뒤 `lastViewedAt`, `viewCount`를 갱신한다.

이 복구 정책은 삭제 API를 제공하기 위한 목적이 아니라, DB에 `is_deleted` 컬럼이 남아 있기 때문에 예외 데이터를 안전하게 처리하기 위한 방어 로직이다.

### 5.5 유니크 키 처리 정책

`player_video_clip_view` 테이블에는 `player_video_clip_id`, `member_id` 유니크 키가 있다.

따라서 한 선수가 같은 개인 분석 클립에 대해 여러 개의 조회 기록 row를 가질 수 없다.

정상 처리 기준은 다음과 같다.

```text
같은 선수 + 같은 개인 분석 클립 = 조회 기록 row 1개
```

그러므로 같은 선수가 같은 클립을 다시 조회하면 새 row를 만들지 않고 기존 row를 갱신한다.

---

## 6. 화면 흐름

현재 단계에서는 프론트를 구현하지 않는다.

다만 백엔드 API 응답은 추후 화면 구현을 고려해서 설계한다.

### 6.1 선수 개인 분석 클립 상세 조회 흐름

1. 선수가 본인 개인 분석 클립 목록에서 클립을 선택한다.
2. 프론트는 선수 개인 분석 클립 상세 API를 호출한다.
3. 서버는 본인 클립인지 확인한다.
4. 서버는 조회 기록을 저장 또는 갱신한다.
5. 서버는 클립 상세 정보를 반환한다.
6. 프론트는 원본 영상 URL과 시작/종료 시간을 기준으로 클립 구간을 재생한다.

### 6.2 지도자/분석관 조회 기록 확인 흐름

1. 지도자 또는 분석관이 선수 개인 분석 클립 관리 화면에 진입한다.
2. 특정 선수를 선택한다.
3. 선수별 조회 기록 API를 호출한다.
4. 서버는 `COACH` 또는 `ANALYST` 권한인지 확인한다.
5. 서버는 대상 회원이 실제 `PLAYER`인지 확인한다.
6. 조회 기록 목록을 반환한다.
7. 화면에는 최초 조회 시간, 마지막 조회 시간, 조회 횟수를 표시한다.

### 6.3 선수 본인 조회 기록 확인 흐름

1. 선수가 내 개인 분석 영상 메뉴에 진입한다.
2. 본인 조회 기록 API를 호출한다.
3. 서버는 로그인 선수의 `memberId`를 기준으로 조회한다.
4. 다른 선수의 기록은 절대 반환하지 않는다.
5. 화면에는 본인이 확인한 개인 분석 클립과 조회 시간을 표시한다.

---

## 7. API 흐름

### 7.1 API 설계 방향

조회 기록 저장은 별도 생성 API를 만들지 않고, 선수 본인 개인 분석 클립 상세 조회 API 내부에서 자동 처리한다.

이유는 다음과 같다.

- 프론트가 조회 기록 저장 API를 누락해도 서버에서 일관되게 기록할 수 있다.
- 선수가 임의로 조회 기록을 조작하는 것을 막을 수 있다.
- 실제로 “상세 조회했다”는 행위와 기록 저장 시점을 일치시킬 수 있다.

조회 기록 확인 API는 관리용 API와 선수 본인 API를 분리한다.

이번 MVP에서는 조회 기록 삭제 API를 제공하지 않는다.

특정 개인 분석 클립 기준 조회 기록 목록 API도 제공하지 않는다.

이유는 선수 개인 분석 클립은 특정 선수 1명에게만 연결되므로, “특정 클립을 어떤 선수들이 봤는지”보다 “특정 선수가 본 개인 피드백 기록”이 실제 운영에 더 맞기 때문이다.

최종 API는 다음과 같다.

```http
GET /api/player/me/player-analysis-clips/{playerClipId}
GET /api/management/players/{playerId}/player-analysis-clip-views
GET /api/player/me/player-analysis-clip-views
```

제공하지 않는 API는 다음과 같다.

```http
GET /api/management/player-analysis-clips/{playerClipId}/views
DELETE /api/coach/player-analysis-clip-views/{viewId}
```

### 7.2 선수 본인 개인 분석 클립 상세 조회 시 조회 기록 저장

```http
GET /api/player/me/player-analysis-clips/{playerClipId}
```

사용 가능 역할은 다음과 같다.

- `PLAYER`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `PLAYER`인지 확인한다.
3. 선수 개인 분석 클립 ID로 클립을 조회한다.
4. 클립이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 클립의 `player_id`와 로그인 선수의 `memberId`가 같은지 확인한다.
6. 다르면 접근을 차단한다.
7. 연결된 원본 경기 영상이 삭제되지 않았는지 확인한다.
8. 조회 기록을 조회한다.
9. 조회 기록이 없으면 최초 조회 기록을 저장한다.
10. 조회 기록이 있으면 `lastViewedAt`, `viewCount`를 갱신한다.
11. 조회 기록의 `isDeleted = true`이면 `isDeleted = false`로 복구하고 갱신한다.
12. 선수 개인 분석 클립 상세 정보를 반환한다.

### 7.3 특정 선수의 개인 분석 클립 조회 기록 목록 조회

```http
GET /api/management/players/{playerId}/player-analysis-clip-views?page=0&size=20
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. `playerId`로 회원을 조회한다.
4. 대상 회원이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 대상 회원의 역할이 `PLAYER`인지 확인한다.
6. `member_id = playerId`이고 `is_deleted = false`인 조회 기록만 조회한다.
7. 페이지 응답을 반환한다.

응답 예시는 다음과 같다.

```json
{
  "views": [
    {
      "viewId": 1,
      "playerClipId": 3,
      "playerClipTitle": "전진 패스 선택 가능 장면",
      "playerId": 5,
      "playerName": "홍길동",
      "firstViewedAt": "2026-06-20T20:00:00",
      "lastViewedAt": "2026-06-21T09:30:00",
      "viewCount": 3,
      "createdAt": "2026-06-20T20:00:00",
      "updatedAt": "2026-06-21T09:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 7.4 선수 본인 조회 기록 목록 조회

```http
GET /api/player/me/player-analysis-clip-views?page=0&size=20
```

사용 가능 역할은 다음과 같다.

- `PLAYER`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `PLAYER`인지 확인한다.
3. 로그인한 선수의 `memberId`를 조회 기준으로 사용한다.
4. 요청 파라미터로 `playerId`를 받지 않는다.
5. `member_id = 로그인 선수 memberId`이고 `is_deleted = false`인 조회 기록만 조회한다.
6. 페이지 응답을 반환한다.

---

## 8. DB 설계 방향

초기 구현에서는 현재 `player_video_clip_view` 테이블을 그대로 사용한다.

| 컬럼 | 사용 목적 |
|---|---|
| `id` | 조회 기록 PK |
| `player_video_clip_id` | 선수 개인 분석 클립 ID |
| `member_id` | 조회한 선수 회원 ID |
| `first_viewed_at` | 최초 조회 시간 |
| `last_viewed_at` | 마지막 조회 시간 |
| `view_count` | 조회 횟수 |
| `is_deleted` | 소프트 삭제 여부. 이번 MVP에서는 삭제 API가 없으므로 일반 흐름에서는 `false` 유지 |
| `created_at` | 생성일시 |
| `updated_at` | 수정일시 |

Entity 매핑 방향은 다음과 같다.

| DB 컬럼 | Entity 필드명 |
|---|---|
| `id` | `id` |
| `player_video_clip_id` | `playerVideoClip` |
| `member_id` | `member` |
| `first_viewed_at` | `firstViewedAt` |
| `last_viewed_at` | `lastViewedAt` |
| `view_count` | `viewCount` |
| `is_deleted` | `isDeleted` |
| `created_at` | `createdAt` |
| `updated_at` | `updatedAt` |

`player_video_clip_id`, `member_id` 유니크 키를 사용해 한 선수가 같은 개인 클립에 대해 하나의 조회 기록만 가지도록 한다.

이번 MVP에서는 DB 컬럼을 추가하거나 제거하지 않는다.

---

## 9. Repository 설계 방향

이번 MVP에서는 Repository 메서드를 2개만 사용한다.

```java
Optional<PlayerVideoClipViewEntity> findByPlayerVideoClipAndMember(
        PlayerVideoClipEntity playerVideoClip,
        MemberEntity member
);

Page<PlayerVideoClipViewEntity> findByMemberAndIsDeletedFalse(
        MemberEntity member,
        Pageable pageable
);
```

### 9.1 `findByPlayerVideoClipAndMember(...)`

이 메서드는 선수가 특정 개인 분석 클립을 이미 조회했는지 확인할 때 사용한다.

사용 위치는 다음이다.

```text
GET /api/player/me/player-analysis-clips/{playerClipId}
```

처리 목적은 다음과 같다.

- 기존 기록이 없으면 최초 조회 기록 생성
- 기존 기록이 있으면 `lastViewedAt` 갱신 및 `viewCount + 1`
- 기존 기록의 `isDeleted = true`이면 `isDeleted = false`로 복구 후 갱신

주의사항은 다음과 같다.

- 이 메서드에는 `IsDeletedFalse` 조건을 붙이지 않는다.
- 이유는 예외적으로 `isDeleted = true`인 기존 row가 있어도 새 row를 만들지 않고 기존 row를 재사용하기 위해서다.

### 9.2 `findByMemberAndIsDeletedFalse(...)`

이 메서드는 특정 선수의 조회 기록 목록을 조회할 때 사용한다.

사용 위치는 다음이다.

```text
GET /api/management/players/{playerId}/player-analysis-clip-views
GET /api/player/me/player-analysis-clip-views
```

처리 목적은 다음과 같다.

- 특정 선수가 조회한 개인 분석 클립 목록 확인
- `viewCount`로 조회 여부와 조회 횟수 확인
- `firstViewedAt`, `lastViewedAt`으로 최초/마지막 확인 시간 확인

### 9.3 사용하지 않는 Repository 메서드

이번 MVP에서는 다음 메서드를 사용하지 않는다.

```java
Optional<PlayerVideoClipViewEntity> findByIdAndIsDeletedFalse(Integer viewId);

Page<PlayerVideoClipViewEntity> findByPlayerVideoClipAndIsDeletedFalse(
        PlayerVideoClipEntity playerVideoClip,
        Pageable pageable
);
```

사용하지 않는 이유는 다음과 같다.

- 조회 기록 삭제 API를 제공하지 않으므로 `viewId` 기준 조회가 필요하지 않다.
- 선수 개인 분석 클립은 특정 선수 1명에게만 연결되므로 특정 클립 기준 조회 기록 목록 API의 의미가 크지 않다.
- 실제 운영에서는 선수 기준으로 피드백 확인 상태를 보는 것이 더 자연스럽다.

---

## 10. 요청/응답 DTO 방향

### 10.1 조회 기록 응답 DTO

`PlayerAnalysisClipViewResponseDTO`

필드 방향은 다음과 같다.

- `viewId`
- `playerClipId`
- `playerClipTitle`
- `playerId`
- `playerName`
- `firstViewedAt`
- `lastViewedAt`
- `viewCount`
- `createdAt`
- `updatedAt`

### 10.2 조회 기록 페이지 응답 DTO

`PlayerAnalysisClipViewPageResponseDTO`

Spring의 `Page` 객체를 그대로 노출하지 않고 프론트에서 필요한 페이징 정보만 반환한다.

필드 방향은 다음과 같다.

- `views`
- `page`
- `size`
- `totalElements`
- `totalPages`

---

## 11. 예외 상황

| 상황 | 처리 방식 | 권장 HTTP 상태 |
|---|---|---|
| JWT 없음 | 인증 실패 | `401 Unauthorized` |
| JWT 만료 | 인증 실패 | `401 Unauthorized` |
| 선수의 다른 선수 개인 클립 상세 조회 | 접근 차단 | `403 Forbidden` |
| 선수의 다른 선수 조회 기록 조회 | 접근 차단 | `403 Forbidden` |
| 선수의 관리용 조회 기록 API 호출 | 접근 차단 | `403 Forbidden` |
| 지도자/분석관이 선수 본인 조회 기록 API 호출 | 접근 차단 | `403 Forbidden` |
| 존재하지 않는 선수 개인 분석 클립 ID | 조회 실패 | `404 Not Found` |
| 삭제된 선수 개인 분석 클립 ID | 조회 실패 | `404 Not Found` |
| 존재하지 않는 선수 ID | 조회 실패 | `404 Not Found` |
| 삭제된 선수 ID | 조회 실패 | `404 Not Found` |
| 대상 회원이 선수 역할이 아님 | 조회 실패 | `403 Forbidden` 또는 `400 Bad Request` |
| 페이지 번호가 음수 | 요청 실패 | `400 Bad Request` |
| 페이지 크기가 0 이하 | 요청 실패 | `400 Bad Request` |
| 페이지 크기가 허용 범위 초과 | 요청 실패 | `400 Bad Request` |
| 조회 기록 저장 중 유니크 키 충돌 | 기존 기록 재조회 후 갱신 | `200 OK` 또는 내부 재시도 |
| 기존 조회 기록의 `isDeleted = true` | 예외 데이터로 보고 복구 후 갱신 | `200 OK` |

권장 에러 코드는 다음과 같다.

```java
// 선수 개인 분석 클립 조회 기록
PLAYER_ANALYSIS_CLIP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "선수 개인 분석 클립에 접근할 권한이 없습니다."),
PLAYER_ANALYSIS_CLIP_VIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "선수 개인 분석 클립 조회 기록에 접근할 권한이 없습니다."),

// 공통 요청 검증
INVALID_PAGE_REQUEST(HttpStatus.BAD_REQUEST, "페이지 요청 값이 올바르지 않습니다.")
```

---

## 12. 구현 순서

### 12.1 백엔드 1단계: 기본 구조 확인

1. `PlayerVideoClipViewEntity` 필드와 DB 컬럼 매핑 확인
2. `PlayerVideoClipEntity`와 `PlayerVideoClipViewEntity` 연관관계 확인
3. `MemberEntity`와 `PlayerVideoClipViewEntity` 연관관계 확인
4. 기존 `PlayerVideoClipRepository` 확인
5. 기존 `MemberRepository` 확인
6. 기존 공통 예외 구조와 `ErrorCode` 확인

### 12.2 백엔드 2단계: DTO 작성

1. `PlayerAnalysisClipViewResponseDTO` 작성
2. `PlayerAnalysisClipViewPageResponseDTO` 작성

### 12.3 백엔드 3단계: Repository 작성

1. `PlayerVideoClipViewRepository` 작성
2. `findByPlayerVideoClipAndMember(...)` 작성
3. `findByMemberAndIsDeletedFalse(...)` 작성

조회 기록 저장/갱신 로직에서는 `isDeleted = false` 조건 없이 기존 기록을 먼저 찾아야 한다.

조회 목록 API에서는 `isDeleted = false` 조건을 사용한다.

### 12.4 백엔드 4단계: 조회 기록 저장/갱신 Service 작성

1. `PlayerAnalysisClipViewService.recordViewIfPlayer(...)` 작성
2. 로그인 사용자가 `PLAYER`가 아니면 기록하지 않고 종료
3. 개인 분석 클립 대상 선수와 로그인 선수가 같은지 확인
4. 기존 조회 기록이 없으면 최초 조회 기록 저장
5. 기존 조회 기록이 있으면 `lastViewedAt`, `viewCount` 갱신
6. 기존 조회 기록이 `isDeleted = true`이면 `isDeleted = false`로 복구 후 갱신

### 12.5 백엔드 5단계: 기존 선수 본인 상세 조회 Service에 연결

`PlayerAnalysisClipService.findMyPlayerAnalysisClipDetail(...)` 안에서 권한 검증 후, 응답 반환 전에 조회 기록 저장/갱신 메서드를 호출한다.

```java
@Transactional
public PlayerAnalysisClipDetailResponseDTO findMyPlayerAnalysisClipDetail(
        Integer playerClipId,
        CustomUserPrincipal principal
) {
    checkCanViewMyPlayerClip(principal);

    PlayerVideoClipEntity playerVideoClip = findValidPlayerAnalysisClip(playerClipId);

    if (!playerVideoClip.getPlayer().getId().equals(principal.getMemberId())) {
        throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    checkMatchVideoIsNotDeleted(playerVideoClip.getGameVideoUpload());

    playerAnalysisClipViewService.recordViewIfPlayer(playerVideoClip, principal);

    return toPlayerAnalysisClipDetailResponseDTO(playerVideoClip);
}
```

주의사항은 다음과 같다.

- 이 메서드는 조회 기록 저장/갱신을 수행하므로 `@Transactional(readOnly = true)`를 제거하고 `@Transactional`로 처리한다.
- 관리용 상세 조회 메서드에는 조회 기록 저장/갱신 로직을 연결하지 않는다.

### 12.6 백엔드 6단계: 조회 기록 목록 Service 작성

1. 지도자/분석관용 특정 선수 조회 기록 목록 조회 작성
2. 선수 본인 조회 기록 목록 조회 작성
3. 페이지 요청 값 검증 작성
4. 관리용 API는 `COACH`, `ANALYST`만 허용
5. 선수 본인 API는 `PLAYER`만 허용

### 12.7 백엔드 7단계: Controller 작성

1. `PlayerAnalysisClipViewController` 작성
2. `GET /api/management/players/{playerId}/player-analysis-clip-views` 작성
3. `GET /api/player/me/player-analysis-clip-views` 작성
4. `@RequestParam`과 `@PathVariable`에는 이름을 명시한다.

### 12.8 백엔드 8단계: 테스트

1. 선수가 본인 개인 분석 클립 상세 조회 시 최초 조회 기록 저장 성공 테스트
2. 선수가 본인 개인 분석 클립 상세 재조회 시 `lastViewedAt`, `viewCount` 갱신 테스트
3. 지도자가 개인 분석 클립 관리용 상세 조회 시 조회 기록이 저장되지 않는지 테스트
4. 분석관이 개인 분석 클립 관리용 상세 조회 시 조회 기록이 저장되지 않는지 테스트
5. 선수가 다른 선수 개인 분석 클립 상세 조회 시 접근 실패 테스트
6. 지도자 특정 선수 조회 기록 목록 조회 성공 테스트
7. 분석관 특정 선수 조회 기록 목록 조회 성공 테스트
8. 선수 관리용 조회 기록 목록 조회 실패 테스트
9. 선수 본인 조회 기록 목록 조회 성공 테스트
10. 지도자/분석관이 선수 본인 조회 기록 API 호출 시 실패 테스트
11. 페이지 번호/크기 검증 실패 테스트

---

## 13. 테스트 완료 기준

기능 완료 기준은 다음과 같다.

- 선수 본인 상세 조회 시 `player_video_clip_view`에 최초 조회 기록이 생성된다.
- 같은 클립을 다시 조회하면 새 row가 생기지 않고 `viewCount`만 증가한다.
- 재조회 시 `firstViewedAt`은 유지되고 `lastViewedAt`은 갱신된다.
- 지도자/분석관의 관리용 상세 조회로는 `viewCount`가 증가하지 않는다.
- 선수는 본인 조회 기록 목록만 조회할 수 있다.
- 지도자/분석관은 특정 선수의 조회 기록 목록을 조회할 수 있다.
- 선수는 관리용 선수 조회 기록 API를 호출할 수 없다.
- 지도자/분석관은 선수 본인 조회 기록 API를 호출할 수 없다.
- 조회 기록 삭제 API는 존재하지 않는다.
- 특정 개인 분석 클립 기준 조회 기록 목록 API는 존재하지 않는다.

---

## 14. 추후 확장 가능성

초기 구현에서는 조회 기록 저장/갱신과 역할별 조회 API에 집중한다.

추후 확장 후보는 다음과 같다.

- 선수별 미확인 개인 분석 클립 목록
- 지도자 대시보드의 피드백 확인률 표시
- 개인 분석 클립별 확인 상태 배지
- 조회 기록 기반 알림 재전송
- 마지막 조회 이후 코멘트 추가 여부 표시
- 선수별 피드백 확인 리포트
- 조회 기록 수정 이력
- 운영자 전용 조회 기록 정리 기능
- 팀 분석 클립 조회 기록 기능으로 확장
- 영상 실제 재생 시간 기반 시청 완료율 기록

---

## 15. 주의사항

- 조회 기록 저장은 선수 본인 상세 조회 API에서만 자동 처리한다.
- 목록 조회만으로는 조회 기록을 남기지 않는다.
- 지도자와 분석관의 조회는 선수 조회 기록으로 저장하지 않는다.
- 선수는 본인 조회 기록만 확인할 수 있어야 한다.
- 다른 선수의 조회 기록은 API 주소를 직접 호출해도 접근 차단되어야 한다.
- 조회 기록 삭제 API는 이번 MVP에서 만들지 않는다.
- `is_deleted` 컬럼은 DB에 유지한다.
- `player_video_clip_id`, `member_id` 유니크 키 때문에 같은 선수와 같은 클립 조합은 하나의 row만 가진다.
- 기존 조회 기록이 있으면 새 row를 만들지 않고 `viewCount`를 증가시킨다.
- 권한 검증은 프론트가 아니라 백엔드에서 반드시 처리한다.
