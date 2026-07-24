# 선수 개인 분석 클립 기능 요구사항

> [!IMPORTANT]
> 이 문서는 해당 기능의 기존 구현 당시 기준과 작업 이력을 보존하는 문서다.
> 경기 영상 편집기 v1의 신규 구현은 `docs/31_video_editor_v1_requirements.md`,
> `docs/00_current_backend_policy.md`, `docs/00_current_frontend_policy.md`를 우선한다.
> 역할 권한, 클립 상태, 시간 단위, 삭제, 드로잉 합성, 영상 제공 방식이 충돌하면 최신 문서를 적용한다.
> 기존 API는 프론트 전환 전 호환 목적으로 유지될 수 있으므로 실제 제거 여부는 현재 소스를 확인한다.


## 1. 결론

선수 개인 분석 클립 기능은 지도자와 분석관이 원본 경기 영상의 특정 구간을 특정 선수 1명에게 공유하는 개인 피드백용 분석 클립으로 저장하고, 해당 선수 본인만 조회할 수 있게 하는 기능이다.

초기 구현은 다음 정책을 기준으로 진행한다.

- 인증은 기존 JWT 인증 구조를 사용한다.
- JWT 인증 후 `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 값을 사용한다.
- 선수 개인 분석 클립은 `player_video_clip` 테이블을 사용한다.
- 원본 경기 영상은 `game_video_upload` 테이블의 데이터를 사용한다.
- 초기에는 실제 영상을 자르지 않고 원본 경기 영상 기준 `startTimeSec`, `endTimeSec` 메타데이터로 클립을 저장한다.
- 클립 생성 시 `matchVideoId`가 존재하고 삭제되지 않았는지 반드시 백엔드에서 검증한다.
- 클립 생성 및 수정 시 원본 경기 영상의 `duration_sec`가 준비되어 있고, 클립 시간이 원본 영상 길이 안에 있는지 반드시 백엔드에서 검증한다.
- 클립 생성 시 `playerId`가 존재하고 삭제되지 않았으며 실제 선수 역할 `PLAYER`인지 반드시 백엔드에서 검증한다.
- 지도자 `COACH`는 선수 개인 분석 클립 등록, 조회, 수정, 삭제가 가능하다.
- 분석관 `ANALYST`는 선수 개인 분석 클립 등록, 조회, 수정이 가능하고 삭제는 불가능하다.
- 선수 `PLAYER`는 본인에게 지정된 개인 분석 클립만 조회할 수 있다.
- 선수는 다른 선수의 개인 분석 클립을 조회할 수 없다.
- 삭제는 실제 DB 삭제가 아니라 `is_deleted = true`로 처리한다.
- 현재 단계에서는 프론트를 구현하지 않고 백엔드 API 기능만 개발한다.
- 현재 서비스는 단일 팀 기준이므로 별도 `team_id`는 사용하지 않는다.

---

## 2. 기능 목적

선수 개인 분석 클립 기능의 목적은 지도자와 분석관이 특정 선수에게만 전달해야 하는 개인 피드백 장면을 안전하게 저장하고 공유하는 것이다.

팀 분석 클립이 팀 전체 전술 공유용이라면, 선수 개인 분석 클립은 특정 선수의 플레이 선택, 위치, 움직임, 압박 타이밍, 수비 전환, 오프 더 볼 움직임 등을 개별적으로 피드백하기 위한 기능이다.

이 기능은 다음 상황에서 사용된다.

- 지도자가 특정 선수의 좋은 장면을 개인 피드백으로 저장한다.
- 지도자가 특정 선수의 실수 장면을 본인에게만 공유한다.
- 분석관이 경기 영상을 보며 선수별 피드백 장면을 분류한다.
- 선수는 모바일에서 본인에게 공유된 개인 분석 클립만 확인한다.
- 이후 개인 클립 드로잉, 조회 기록, 피드백 확인 여부 기능과 연결된다.

초기에는 실제 영상 파일을 잘라서 저장하지 않는다.

대신 원본 경기 영상 URL과 원본 영상 기준 시작/종료 시간을 함께 응답해서 프론트에서 해당 구간을 재생하는 방식으로 처리한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

지도자는 선수 개인 분석 클립을 전체 관리할 수 있다.

가능한 기능은 다음과 같다.

- 선수 개인 분석 클립 등록
- 선수 개인 분석 클립 목록 조회
- 선수 개인 분석 클립 상세 조회
- 선수 개인 분석 클립 수정
- 선수 개인 분석 클립 삭제

### 3.2 분석관 `ANALYST`

분석관은 영상 분석 실무를 담당하므로 선수 개인 분석 클립 등록과 수정은 가능하다.

가능한 기능은 다음과 같다.

- 선수 개인 분석 클립 등록
- 선수 개인 분석 클립 목록 조회
- 선수 개인 분석 클립 상세 조회
- 선수 개인 분석 클립 수정

불가능한 기능은 다음과 같다.

- 선수 개인 분석 클립 삭제

분석관에게 삭제 권한을 주지 않는 이유는 선수 개인 피드백이 민감한 분석 자료이고, 이후 드로잉/조회 기록/확인 여부 데이터와 연결될 수 있기 때문이다.

### 3.3 선수 `PLAYER`

선수는 본인에게 지정된 개인 분석 클립만 조회할 수 있다.

가능한 기능은 다음과 같다.

- 본인 개인 분석 클립 목록 조회
- 본인 개인 분석 클립 상세 조회

불가능한 기능은 다음과 같다.

- 다른 선수의 개인 분석 클립 조회
- 선수 개인 분석 클립 등록
- 선수 개인 분석 클립 수정
- 선수 개인 분석 클립 삭제

---

## 4. 권한 정책

## 4.1 권한 표

| 역할 | 등록 | 목록 조회 | 상세 조회 | 수정 | 삭제 |
|---|---:|---:|---:|---:|---:|
| `COACH` | 가능 | 전체 조회 가능 | 전체 조회 가능 | 가능 | 가능 |
| `ANALYST` | 가능 | 전체 조회 가능 | 전체 조회 가능 | 가능 | 불가 |
| `PLAYER` | 불가 | 본인 클립만 가능 | 본인 클립만 가능 | 불가 | 불가 |

## 4.2 권한 검증 기준

선수 개인 분석 클립 권한 검증은 반드시 백엔드에서 처리한다.

검증 기준은 다음과 같다.

- 인증되지 않은 사용자는 모든 선수 개인 분석 클립 API에 접근할 수 없다.
- `COACH`, `ANALYST`만 선수 개인 분석 클립을 등록할 수 있다.
- `COACH`, `ANALYST`는 삭제되지 않은 모든 선수 개인 분석 클립을 조회할 수 있다.
- `PLAYER`는 `player_id`가 로그인한 본인 `memberId`와 같은 클립만 조회할 수 있다.
- `PLAYER`가 다른 선수의 클립 ID로 상세 조회 API를 직접 호출해도 실패해야 한다.
- `COACH`, `ANALYST`만 선수 개인 분석 클립을 수정할 수 있다.
- `COACH`만 선수 개인 분석 클립을 삭제할 수 있다.
- `PLAYER`는 등록, 수정, 삭제 API를 직접 호출해도 실패해야 한다.
- `ANALYST`는 삭제 API를 직접 호출해도 실패해야 한다.
- `isAdmin = true`만으로 선수 개인 분석 클립 등록, 수정, 삭제 권한을 주지 않는다.
- 삭제된 원본 경기 영상에는 새 선수 개인 분석 클립을 만들 수 없다.
- 삭제된 선수 개인 분석 클립은 목록/상세/수정/삭제 대상에서 제외한다.

## 4.3 선수 본인 조회 제한 정책

선수 조회 API에서는 로그인한 선수의 `memberId`를 서버에서 직접 사용한다.

따라서 선수용 목록 조회 API는 `playerId`를 요청 파라미터로 받지 않는다.

이유는 다음과 같다.

- 프론트 조작으로 다른 선수 ID를 넣는 것을 막을 수 있다.
- 개인 피드백 영상 접근 범위를 백엔드에서 강제할 수 있다.
- URL 직접 호출 공격에도 안전하게 대응할 수 있다.

선수 상세 조회 시에도 다음 조건을 반드시 검증한다.

- 클립이 존재해야 한다.
- 클립이 삭제되지 않아야 한다.
- 클립의 `player_id`가 로그인한 선수의 `memberId`와 같아야 한다.

---

## 5. 원본 경기 영상 검증 정책

선수 개인 분석 클립 생성 및 수정 시 `matchVideoId` 검증은 필수다.

검증 기준은 다음과 같다.

- `matchVideoId`가 null이면 실패한다.
- `game_video_upload.id = matchVideoId`인 원본 영상이 존재해야 한다.
- 원본 영상의 `is_deleted`가 false여야 한다.
- 원본 영상이 삭제된 경우 새 개인 클립 생성은 실패한다.
- 원본 영상이 존재하지 않으면 `404 Not Found`로 처리한다.
- 원본 영상의 `duration_sec`가 null이거나 0 이하이면 개인 클립 생성 및 수정은 실패한다.
- 개인 클립의 `endTimeSec`는 원본 영상의 `duration_sec`보다 클 수 없다.

## 5.1 원본 영상 길이 기준 클립 시간 검증 정책

선수 개인 분석 클립은 원본 경기 영상의 총 길이 안에서만 생성 및 수정할 수 있다.

검증 기준은 다음과 같다.

- `duration_sec`가 null이면 실패한다.
- `duration_sec`가 0 이하이면 실패한다.
- `startTimeSec`가 null이면 실패한다.
- `endTimeSec`가 null이면 실패한다.
- `startTimeSec`와 `endTimeSec`는 0 이상이어야 한다.
- `startTimeSec`는 `endTimeSec`보다 작아야 한다.
- `endTimeSec`는 `duration_sec` 이하이어야 한다.

원본 영상 길이 정보가 준비되지 않은 경우에는 `MATCH_VIDEO_DURATION_NOT_READY`로 처리한다.

클립 시간 구간이 올바르지 않은 경우에는 `INVALID_CLIP_TIME_RANGE`로 처리한다.

---

## 6. 대상 선수 검증 정책

선수 개인 분석 클립 생성 및 수정 시 `playerId` 검증은 필수다.

검증 기준은 다음과 같다.

- `playerId`가 null이면 실패한다.
- `member.id = playerId`인 회원이 존재해야 한다.
- 대상 회원의 `is_deleted`가 false여야 한다.
- 대상 회원의 `memberRole`은 반드시 `PLAYER`여야 한다.
- 대상 회원이 `COACH` 또는 `ANALYST`이면 개인 분석 클립 대상이 될 수 없다.
- 대상 선수가 존재하지 않으면 `404 Not Found`로 처리한다.
- 대상 회원이 선수 역할이 아니면 `400 Bad Request`로 처리한다.

---

## 7. 화면 흐름

현재 단계에서는 프론트를 구현하지 않는다.

다만 백엔드 API 응답은 추후 화면 구현을 고려해서 설계한다.

## 7.1 선수 개인 분석 클립 조회 화면

선수는 모바일에서 개인 분석 클립을 조회할 가능성이 높다.

기본 흐름은 다음과 같다.

1. 선수가 로그인한다.
2. 내 개인 분석 영상 메뉴에 진입한다.
3. 본인에게 지정된 최신 개인 분석 클립 목록을 확인한다.
4. 필요한 경우 경기 영상별 또는 클립 유형별로 필터링한다.
5. 클립을 선택한다.
6. 원본 영상 URL, 시작 시간, 종료 시간을 받아 해당 구간을 재생한다.
7. 클립 제목과 코멘트를 함께 확인한다.

목록 화면에 표시할 정보는 다음과 같다.

- 개인 분석 클립 ID
- 원본 경기 영상 ID
- 원본 경기 제목
- 클립 유형
- 클립 제목
- 시작 시간
- 종료 시간
- 클립 상태
- 작성자 이름
- 생성일시

상세 화면에 표시할 정보는 다음과 같다.

- 개인 분석 클립 ID
- 원본 경기 영상 ID
- 원본 경기 영상 URL
- 원본 경기 제목
- 대상 선수 ID
- 대상 선수 이름
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

## 7.2 지도자/분석관 선수 개인 분석 클립 관리 화면

지도자와 분석관은 PC에서 경기 영상을 보며 특정 선수용 클립을 생성할 가능성이 높다.

기본 흐름은 다음과 같다.

1. 지도자 또는 분석관이 원본 경기 영상 상세 화면 또는 영상 편집기에 진입한다.
2. 원본 경기 영상을 재생한다.
3. 필요한 장면의 시작 시간과 종료 시간을 선택한다.
4. 대상 선수, 클립 유형, 제목, 코멘트를 입력한다.
5. 저장 버튼을 누른다.
6. 서버는 원본 경기 영상 존재 여부와 삭제 여부를 검증한다.
7. 서버는 대상 회원이 실제 선수 역할인지 검증한다.
8. 선수 개인 분석 클립을 저장한다.
9. 저장된 개인 분석 클립 목록을 갱신한다.

---

## 8. API 흐름

## 8.1 API 설계 방향

선수 개인 분석 클립은 접근 범위가 다르므로 관리 API와 선수 본인 조회 API를 분리한다.

관리 API는 지도자와 분석관이 사용하고, 선수 조회 API는 로그인한 선수 본인만 사용한다.

권장 API 초안은 다음과 같다.

```http
GET    /api/management/player-analysis-clips
GET    /api/management/player-analysis-clips/{playerClipId}
POST   /api/management/player-analysis-clips
PATCH  /api/management/player-analysis-clips/{playerClipId}
DELETE /api/coach/player-analysis-clips/{playerClipId}
GET    /api/player/me/player-analysis-clips
GET    /api/player/me/player-analysis-clips/{playerClipId}
```

## 8.2 관리용 선수 개인 분석 클립 목록 조회

```http
GET /api/management/player-analysis-clips?page=0&size=20&matchVideoId=1&playerId=5&clipType=PASS
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

요청 파라미터는 다음과 같다.

| 파라미터 | 필수 여부 | 설명 |
|---|---:|---|
| `page` | 선택 | 페이지 번호 |
| `size` | 선택 | 페이지 크기 |
| `matchVideoId` | 선택 | 특정 원본 경기 영상 기준 필터 |
| `playerId` | 선택 | 특정 선수 기준 필터 |
| `clipType` | 선택 | 클립 유형 필터 |

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 페이지 번호와 페이지 크기를 검증한다.
4. `matchVideoId`가 있으면 원본 경기 영상 존재 여부와 삭제 여부를 검증한다.
5. `playerId`가 있으면 대상 회원이 선수 역할인지 검증한다.
6. `clipType`이 있으면 허용된 Enum 값인지 검증한다.
7. `is_deleted = false`인 선수 개인 분석 클립만 조회한다.
8. 최신 생성일 기준으로 정렬한다.
9. 목록 응답을 반환한다.

응답 예시는 다음과 같다.

```json
{
  "playerClips": [
    {
      "playerClipId": 1,
      "matchVideoId": 3,
      "matchVideoTitle": "2026 춘계리그 vs 서울FC",
      "playerId": 5,
      "playerName": "홍길동",
      "clipType": "PASS",
      "title": "전진 패스 선택 가능 장면",
      "startTimeSec": 755,
      "endTimeSec": 790,
      "status": "READY",
      "editorId": 2,
      "editorName": "김지도",
      "createdAt": "2026-06-20T19:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

## 8.3 관리용 선수 개인 분석 클립 상세 조회

```http
GET /api/management/player-analysis-clips/{playerClipId}
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 선수 개인 분석 클립 ID로 클립을 조회한다.
4. 클립이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 연결된 원본 경기 영상이 존재하지 않거나 삭제된 경우 실패 처리한다.
6. 원본 영상 URL과 클립 메타데이터를 함께 반환한다.

응답 예시는 다음과 같다.

```json
{
  "playerClipId": 1,
  "matchVideoId": 3,
  "matchVideoTitle": "2026 춘계리그 vs 서울FC",
  "matchVideoUrl": "https://example.com/videos/game-001.mp4",
  "playerId": 5,
  "playerName": "홍길동",
  "clipType": "PASS",
  "title": "전진 패스 선택 가능 장면",
  "comment": "수비를 끌어낸 뒤 오른쪽 전진 패스 선택이 가능했던 장면",
  "startTimeSec": 755,
  "endTimeSec": 790,
  "status": "READY",
  "editorId": 2,
  "editorName": "김지도",
  "createdAt": "2026-06-20T19:00:00",
  "updatedAt": "2026-06-20T19:00:00"
}
```

## 8.4 선수 본인 개인 분석 클립 목록 조회

```http
GET /api/player/me/player-analysis-clips?page=0&size=20&matchVideoId=1&clipType=PASS
```

사용 가능 역할은 다음과 같다.

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
2. 로그인 회원 역할이 `PLAYER`인지 확인한다.
3. 로그인한 선수의 `memberId`를 조회 기준으로 사용한다.
4. 요청에서 별도 `playerId`는 받지 않는다.
5. `matchVideoId`가 있으면 원본 경기 영상 존재 여부와 삭제 여부를 검증한다.
6. `clipType`이 있으면 허용된 Enum 값인지 검증한다.
7. `player_id = 로그인 선수 memberId`이고 `is_deleted = false`인 클립만 조회한다.
8. 최신 생성일 기준으로 정렬한다.
9. 목록 응답을 반환한다.

## 8.5 선수 본인 개인 분석 클립 상세 조회

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
5. 클립의 `player_id`가 로그인 선수 `memberId`와 같은지 확인한다.
6. 다르면 접근 차단 처리한다.
7. 연결된 원본 경기 영상이 존재하지 않거나 삭제된 경우 실패 처리한다.
8. 원본 영상 URL과 클립 메타데이터를 함께 반환한다.

## 8.6 선수 개인 분석 클립 등록

```http
POST /api/management/player-analysis-clips
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

요청 예시는 다음과 같다.

```json
{
  "matchVideoId": 3,
  "playerId": 5,
  "clipType": "PASS",
  "title": "전진 패스 선택 가능 장면",
  "comment": "수비를 끌어낸 뒤 오른쪽 전진 패스 선택이 가능했던 장면",
  "startTimeSec": 755,
  "endTimeSec": 790
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. `matchVideoId`가 존재하는지 확인한다.
4. 원본 경기 영상이 삭제되지 않았는지 확인한다.
5. 원본 경기 영상의 `duration_sec`가 null이 아니고 0보다 큰지 확인한다.
6. `playerId`가 존재하는지 확인한다.
7. 대상 회원이 삭제되지 않은 `PLAYER`인지 확인한다.
8. 요청 필수값을 검증한다.
9. `clipType`이 허용된 Enum인지 확인한다.
10. `startTimeSec`와 `endTimeSec`가 0 이상인지 확인한다.
11. `startTimeSec < endTimeSec`인지 확인한다.
12. `endTimeSec <= duration_sec`인지 확인한다.
13. 실제 영상을 자르지 않으므로 `url`은 null로 저장한다.
14. 메타데이터 기반 클립이므로 `status`는 `READY`로 저장한다.
15. `editor_id`에 등록한 회원 ID를 저장한다.
16. `player_id`에 대상 선수 회원 ID를 저장한다.
17. 선수 개인 분석 클립을 저장한다.
18. 생성된 선수 개인 분석 클립 ID를 반환한다.

응답 예시는 다음과 같다.

```json
{
  "playerClipId": 1,
  "message": "선수 개인 분석 클립이 등록되었습니다."
}
```

## 8.7 선수 개인 분석 클립 수정

```http
PATCH /api/management/player-analysis-clips/{playerClipId}
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `ANALYST`

초기 구현에서는 부분 수정 방식보다 전체 수정 방식에 가깝게 처리한다.

요청 예시는 다음과 같다.

```json
{
  "matchVideoId": 3,
  "playerId": 5,
  "clipType": "POSITIONING",
  "title": "수비 위치 조정 필요 장면",
  "comment": "상대 윙어를 보면서 안쪽 공간을 같이 막아야 하는 장면",
  "startTimeSec": 840,
  "endTimeSec": 880
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 선수 개인 분석 클립 ID로 기존 클립을 조회한다.
4. 클립이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. `matchVideoId`가 존재하고 삭제되지 않았는지 검증한다.
6. 원본 경기 영상의 `duration_sec`가 null이 아니고 0보다 큰지 확인한다.
7. `playerId`가 존재하고 삭제되지 않은 `PLAYER`인지 검증한다.
8. 요청 필수값을 검증한다.
9. `clipType`이 허용된 Enum인지 확인한다.
10. `startTimeSec`와 `endTimeSec`가 0 이상인지 확인한다.
11. `startTimeSec < endTimeSec`인지 확인한다.
12. `endTimeSec <= duration_sec`인지 확인한다.
13. 선수 개인 분석 클립 정보를 수정한다.
14. 수정된 선수 개인 분석 클립 정보를 반환한다.

## 8.8 선수 개인 분석 클립 삭제

```http
DELETE /api/coach/player-analysis-clips/{playerClipId}
```

사용 가능 역할은 다음과 같다.

- `COACH`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH`인지 확인한다.
3. 선수 개인 분석 클립 ID로 클립을 조회한다.
4. 클립이 존재하지 않거나 이미 삭제된 경우 실패 처리한다.
5. `is_deleted = true`로 변경한다.
6. 삭제 완료 응답을 반환한다.

---

## 9. DB 설계 방향

## 9.1 현재 사용할 테이블

초기 구현에서는 현재 DB의 `player_video_clip` 테이블을 그대로 사용한다.

| 컬럼 | 사용 목적 |
|---|---|
| `id` | 선수 개인 분석 클립 PK |
| `upload_id` | 원본 경기 영상 ID |
| `editor_id` | 클립 작성 회원 ID |
| `player_id` | 클립 대상 선수 회원 ID |
| `clip_type` | 개인 클립 유형 |
| `title` | 개인 분석 클립 제목 |
| `comment` | 개인 분석 클립 설명 |
| `start_time_sec` | 원본 영상 기준 시작 시간 |
| `end_time_sec` | 원본 영상 기준 종료 시간 |
| `url` | 실제 잘라낸 클립 URL, 초기에는 null |
| `status` | 클립 상태 |
| `is_deleted` | 소프트 삭제 여부 |
| `created_at` | 생성일시 |
| `updated_at` | 수정일시 |

## 9.2 Entity 매핑 방향

`player_video_clip` 테이블은 `PlayerVideoClipEntity`로 매핑한다.

권장 필드명은 다음과 같다.

| DB 컬럼 | Entity 필드명 |
|---|---|
| `id` | `id` |
| `upload_id` | `matchVideo` 또는 `gameVideoUpload` |
| `editor_id` | `editor` |
| `player_id` | `player` |
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

## 9.3 개인 클립 유형 Enum

`clip_type`은 Enum으로 관리한다.

초기 허용 값은 다음과 같다.

| 값 | 의미 |
|---|---|
| `PLAYER_GOOD` | 선수 좋은 장면 |
| `PLAYER_MISTAKE` | 선수 실수 장면 |
| `SHOOTING` | 슈팅 |
| `PASS` | 패스 |
| `DRIBBLE` | 드리블 |
| `DEFENSE` | 수비 |
| `POSITIONING` | 위치 선정 |
| `PRESSING` | 압박 |
| `OFF_THE_BALL` | 오프 더 볼 움직임 |
| `ETC` | 기타 |

권장 Enum 이름은 `PlayerClipTypeEnum`이다.

## 9.4 클립 상태 Enum

`status`는 Enum으로 관리한다.

초기 메타데이터 방식에서는 실제 인코딩 작업이 없으므로 등록 성공 시 `READY`로 저장한다.

초기 허용 값은 다음과 같다.

| 값 | 의미 |
|---|---|
| `READY` | 재생 가능 |
| `PROCESSING` | 실제 클립 파일 생성 중 |
| `FAILED` | 클립 처리 실패 |

팀 분석 클립에서 이미 `VideoClipStatusEnum`을 사용하고 있다면 같은 Enum을 재사용한다.

## 9.5 DB 변경 여부

초기 구현에서는 DB 컬럼을 추가하지 않는다.

현재 `player_video_clip` 테이블만으로 다음 기능을 처리할 수 있다.

- 원본 경기 영상 연결
- 클립 작성자 연결
- 대상 선수 연결
- 개인 클립 유형 저장
- 제목/코멘트 저장
- 원본 영상 기준 시작/종료 시간 저장
- 클립 상태 저장
- 소프트 삭제

단, 클립 시간 검증은 원본 경기 영상 테이블의 `duration_sec` 값을 사용한다.

`player_video_clip`에는 별도 영상 길이 컬럼을 추가하지 않는다.

추후 운영에서 필요해지면 다음 컬럼 또는 테이블을 검토한다.

- 개인 클립 확인 여부
- 선수별 조회 기록 고도화
- 개인 클립 드로잉 데이터 연결
- 클립 수정 이력
- 실제 잘라낸 클립 파일 URL
- 클립 썸네일 URL
- 영상 인코딩 작업 테이블

---

## 10. 요청/응답 DTO 방향

## 10.1 등록 요청 DTO

`CreatePlayerAnalysisClipRequestDTO`

필드 방향은 다음과 같다.

- `matchVideoId`
- `playerId`
- `clipType`
- `title`
- `comment`
- `startTimeSec`
- `endTimeSec`

## 10.2 수정 요청 DTO

`UpdatePlayerAnalysisClipRequestDTO`

초기 구현에서는 전체 수정 방식에 가깝게 처리한다.

필드 방향은 다음과 같다.

- `matchVideoId`
- `playerId`
- `clipType`
- `title`
- `comment`
- `startTimeSec`
- `endTimeSec`

## 10.3 목록 응답 DTO

`PlayerAnalysisClipListResponseDTO`

필드 방향은 다음과 같다.

- `playerClipId`
- `matchVideoId`
- `matchVideoTitle`
- `playerId`
- `playerName`
- `clipType`
- `title`
- `startTimeSec`
- `endTimeSec`
- `status`
- `editorId`
- `editorName`
- `createdAt`

## 10.4 상세 응답 DTO

`PlayerAnalysisClipDetailResponseDTO`

필드 방향은 다음과 같다.

- `playerClipId`
- `matchVideoId`
- `matchVideoTitle`
- `matchVideoUrl`
- `playerId`
- `playerName`
- `clipType`
- `title`
- `comment`
- `startTimeSec`
- `endTimeSec`
- `status`
- `editorId`
- `editorName`
- `createdAt`
- `updatedAt`

## 10.5 페이지 응답 DTO

`PlayerAnalysisClipPageResponseDTO`

Spring의 `Page` 객체를 그대로 노출하지 않고 프론트에서 필요한 페이징 정보만 반환한다.

필드 방향은 다음과 같다.

- `playerClips`
- `page`
- `size`
- `totalElements`
- `totalPages`

## 10.6 생성 응답 DTO

`CreatePlayerAnalysisClipResponseDTO`

필드 방향은 다음과 같다.

- `playerClipId`
- `message`

---

## 11. 예외 상황

| 상황 | 처리 방식 | 권장 HTTP 상태 |
|---|---|---|
| JWT 없음 | 인증 실패 | `401 Unauthorized` |
| JWT 만료 | 인증 실패 | `401 Unauthorized` |
| 선수의 등록 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 수정 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 삭제 API 호출 | 접근 차단 | `403 Forbidden` |
| 분석관의 삭제 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 관리용 목록 조회 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 다른 선수 개인 클립 상세 조회 | 접근 차단 | `403 Forbidden` |
| 존재하지 않는 원본 경기 영상 ID | 생성/수정 실패 | `404 Not Found` |
| 삭제된 원본 경기 영상 ID | 생성/수정 실패 | `404 Not Found` |
| 원본 경기 영상의 `duration_sec`가 null | 생성/수정 실패 | `400 Bad Request` |
| 원본 경기 영상의 `duration_sec`가 0 이하 | 생성/수정 실패 | `400 Bad Request` |
| `endTimeSec`가 원본 영상 길이를 초과 | 생성/수정 실패 | `400 Bad Request` |
| 존재하지 않는 선수 ID | 생성/수정 실패 | `404 Not Found` |
| 삭제된 선수 ID | 생성/수정 실패 | `404 Not Found` |
| 대상 회원이 선수 역할이 아님 | 생성/수정 실패 | `400 Bad Request` |
| 존재하지 않는 선수 개인 분석 클립 조회 | 조회 실패 | `404 Not Found` |
| 삭제된 선수 개인 분석 클립 조회 | 조회 실패 | `404 Not Found` |
| 삭제된 선수 개인 분석 클립 수정 | 수정 실패 | `404 Not Found` |
| 이미 삭제된 선수 개인 분석 클립 삭제 | 삭제 실패 | `404 Not Found` |
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

## 12. 구현 순서

## 12.1 백엔드 1단계: 기본 구조 확인

1. `PlayerVideoClipEntity` 필드와 DB 컬럼 매핑 확인
2. `GameVideoUploadEntity`와 `PlayerVideoClipEntity` 연관관계 확인
3. `MemberEntity`와 `PlayerVideoClipEntity.editor` 연관관계 확인
4. `MemberEntity`와 `PlayerVideoClipEntity.player` 연관관계 확인
5. `PlayerVideoClipRepository` 생성
6. `GameVideoUploadRepository`에 원본 영상 조회 메서드 확인 또는 추가
7. `MemberRepository`에 대상 선수 조회 메서드 확인 또는 추가
8. 기존 공통 예외 구조와 `ErrorCode` 확인

## 12.2 백엔드 2단계: Enum 확인 또는 생성

1. `PlayerClipTypeEnum` 생성
2. `VideoClipStatusEnum` 재사용 여부 확인
3. DB의 `clip_type`, `status`와 Enum 값이 맞는지 확인

## 12.3 백엔드 3단계: DTO 작성

1. `CreatePlayerAnalysisClipRequestDTO` 작성
2. `UpdatePlayerAnalysisClipRequestDTO` 작성
3. `CreatePlayerAnalysisClipResponseDTO` 작성
4. `PlayerAnalysisClipListResponseDTO` 작성
5. `PlayerAnalysisClipDetailResponseDTO` 작성
6. `PlayerAnalysisClipPageResponseDTO` 작성

## 12.4 백엔드 4단계: 권한 검증 로직 작성

1. `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 사용
2. 선수 개인 분석 클립 등록/수정 권한 검증 메서드 작성
3. `COACH`, `ANALYST`가 아니면 등록/수정 실패 처리
4. 선수 개인 분석 클립 삭제 권한 검증 메서드 작성
5. `COACH`가 아니면 삭제 실패 처리
6. 관리용 조회는 `COACH`, `ANALYST`만 허용
7. 선수 본인 조회는 `PLAYER`만 허용
8. `PLAYER` 상세 조회 시 `clip.player.id == principal.memberId` 검증
9. `isAdmin`만으로 클립 변경 권한을 주지 않도록 처리

## 12.5 백엔드 5단계: 원본 경기 영상 검증 로직 작성

1. `matchVideoId`로 `game_video_upload` 조회
2. 존재하지 않으면 실패 처리
3. `isDeleted = true`이면 실패 처리
4. `duration_sec`가 null이거나 0 이하이면 실패 처리
5. 클립 생성/수정 시 반드시 이 검증 메서드 사용

## 12.6 백엔드 6단계: 대상 선수 검증 로직 작성

1. `playerId`로 `member` 조회
2. 존재하지 않으면 실패 처리
3. `isDeleted = true`이면 실패 처리
4. `memberRole != PLAYER`이면 실패 처리
5. 클립 생성/수정 시 반드시 이 검증 메서드 사용

## 12.7 백엔드 7단계: Service 작성

1. 선수 개인 분석 클립 등록 기능 작성
2. 관리용 선수 개인 분석 클립 목록 조회 기능 작성
3. 관리용 선수 개인 분석 클립 상세 조회 기능 작성
4. 선수 본인 개인 분석 클립 목록 조회 기능 작성
5. 선수 본인 개인 분석 클립 상세 조회 기능 작성
6. 원본 영상 `duration_sec` 기준 클립 시간 검증 기능 작성
7. 선수 개인 분석 클립 수정 기능 작성
8. 선수 개인 분석 클립 삭제 기능 작성

## 12.8 백엔드 8단계: Controller 작성

1. 지도자/분석관 공통 관리 Controller 작성
2. 지도자 전용 삭제 Controller 작성
3. 선수 본인 조회 Controller 작성
4. `@RequestParam`과 `@PathVariable`에는 이름을 명시한다.

## 12.9 백엔드 9단계: 테스트

1. 지도자 선수 개인 분석 클립 등록 성공 테스트
2. 분석관 선수 개인 분석 클립 등록 성공 테스트
3. 선수 선수 개인 분석 클립 등록 실패 테스트
4. 존재하지 않는 `matchVideoId`로 등록 실패 테스트
5. 삭제된 `matchVideoId`로 등록 실패 테스트
6. 원본 경기 영상의 `duration_sec`가 null이면 등록 실패 테스트
7. 원본 경기 영상의 `duration_sec`가 0 이하이면 등록 실패 테스트
8. `endTimeSec`가 원본 영상 길이를 초과하면 등록 실패 테스트
9. 존재하지 않는 `playerId`로 등록 실패 테스트
10. `playerId`가 `COACH`이면 등록 실패 테스트
11. `playerId`가 `ANALYST`이면 등록 실패 테스트
12. `startTimeSec >= endTimeSec` 등록 실패 테스트
13. 지도자 관리용 목록 조회 성공 테스트
14. 분석관 관리용 목록 조회 성공 테스트
15. 선수 관리용 목록 조회 실패 테스트
16. 선수 본인 개인 분석 클립 목록 조회 성공 테스트
17. 선수 본인 개인 분석 클립 상세 조회 성공 테스트
18. 선수의 다른 선수 개인 분석 클립 상세 조회 실패 테스트
19. 지도자 선수 개인 분석 클립 수정 성공 테스트
20. 분석관 선수 개인 분석 클립 수정 성공 테스트
21. 원본 경기 영상의 `duration_sec`가 0 이하이면 수정 실패 테스트
22. 수정 시 `endTimeSec`가 원본 영상 길이를 초과하면 실패 테스트
23. 선수 선수 개인 분석 클립 수정 실패 테스트
24. 지도자 선수 개인 분석 클립 삭제 성공 테스트
25. 분석관 선수 개인 분석 클립 삭제 실패 테스트
26. 선수 선수 개인 분석 클립 삭제 실패 테스트
27. 삭제된 선수 개인 분석 클립 조회 제외 테스트
28. 페이징 조회 테스트
29. 클립 유형 필터 조회 테스트
30. 원본 경기 영상별 필터 조회 테스트
31. 대상 선수별 필터 조회 테스트

---

## 13. 추후 확장 가능성

초기 구현에서는 메타데이터 기반 선수 개인 분석 클립 CRUD와 본인 조회 제한에 집중한다.

추후 확장 후보는 다음과 같다.

- 개인 클립 드로잉 데이터 연결
- 개인 클립 조회 기록 저장
- 선수별 피드백 확인 여부
- 지도자 코멘트와 선수 답변
- 실제 클립 파일 자르기
- 영상 인코딩 작업 큐
- 클립 썸네일 생성
- 클립 묶음 생성
- 선수별 클립 검색
- 선수 기록 데이터와 개인 클립 연결
- AI 선수 추적 결과와 개인 클립 연결

---

## 14. 주의사항

- 선수 개인 분석 클립 등록, 수정, 삭제 권한은 프론트가 아니라 백엔드에서 반드시 검증한다.
- 분석관은 등록과 수정은 가능하지만 삭제는 불가능하다.
- 선수는 본인에게 지정된 개인 분석 클립만 조회할 수 있다.
- 선수 조회 API에서는 요청 파라미터로 `playerId`를 받지 않는다.
- 선수 상세 조회에서는 `clip.player.id == principal.memberId`를 반드시 검증한다.
- 삭제는 실제 삭제가 아니라 `is_deleted = true`로 처리한다.
- 조회 API는 삭제되지 않은 선수 개인 분석 클립만 반환한다.
- 클립 생성 시 `matchVideoId`가 존재하고 삭제되지 않았는지 반드시 검증한다.
- 클립 생성 시 `playerId`가 실제 선수 역할인지 반드시 검증한다.
- 초기에는 실제 영상을 자르지 않으므로 `url`은 null이어도 된다.
- 초기 메타데이터 방식에서는 `status`를 `READY`로 저장하는 것이 자연스럽다.
- `startTimeSec`는 반드시 `endTimeSec`보다 작아야 한다.
- 원본 경기 영상의 `duration_sec`가 null이거나 0 이하이면 클립 생성 및 수정은 실패해야 한다.
- `endTimeSec`는 반드시 원본 경기 영상의 `duration_sec` 이하이어야 한다.
- 현재 서비스는 단일 팀 기준이므로 `team_id`를 사용하지 않는다.
- 추후 여러 팀을 받을 경우 원본 영상과 선수 개인 분석 클립 모두 팀 소속 검증이 필요하다.
