# 15. 선수 기록 관리 기능 최종 요구사항

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 선수 기록 관리 기능에 대한 최신 최종 정책을 정의한다.

선수 기록 기능은 다음 두 데이터를 분리해서 관리한다.

```text
player_record
→ 한 경기에서 특정 선수의 최종 요약 기록

player_record_event
→ 분석 클립과 연결된 개별 기록 근거

player_record_event_clip
→ 선수 기록 이벤트와 팀 분석 클립 또는 선수 개인 분석 클립의 연결 정보
```

이 문서는 선수 기록 백엔드와 프론트엔드 구현 시 최우선으로 참고한다.

---

## 2. 최종 결론

최종 정책은 다음과 같다.

- `PlayerRecordPage`는 검색, 목록 조회, 상세 조회 전용 화면으로 사용한다.
- 선수 기록 등록과 갱신은 `MatchVideoPage`에서 경기 영상을 기준으로 진행한다.
- 화면 버튼명은 `선수 기록 이벤트 등록`이 아니라 `선수 기록 등록`을 사용한다.
- 선수 기록 등록 방식은 `클립 없이 등록`, `팀 분석 클립 연결`, `선수 개인 분석 클립 연결` 세 가지다.
- `클립 없이 등록`은 `player_record` 요약 기록만 생성하거나 갱신한다.
- `클립 없이 등록`에서는 `player_record_event`, `player_record_event_clip`을 생성하지 않는다.
- 같은 경기와 같은 선수의 활성 `player_record`가 있으면 기존 값을 조회해 갱신한다.
- 기존 활성 `player_record`가 없으면 모든 기록 값을 0으로 시작해 새로 생성한다.
- 클립 없이 등록 화면은 모든 요약 기록을 한 번에 표시한다.
- 각 기록은 숫자 직접 입력이 아니라 `-`, 현재 값, `+` 버튼으로 1씩 조정한다.
- 여러 기록 항목을 한 번에 조정하고 최종 등록 버튼으로 한 번에 저장한다.
- `-` 버튼은 최소 0까지만 감소시킨다.
- 유효 슈팅과 슈팅, 성공 패스와 패스, 성공 드리블과 드리블 사이의 추가 정합성 검증은 이번 단계에서는 적용하지 않는다.
- DB 컬럼 범위에 맞춰 모든 수치는 백엔드에서 0 이상 255 이하를 검증한다.
- 클립 연결 등록은 한 요청에서 클립 하나와 이벤트 유형 하나만 등록한다.
- 같은 클립에 다른 이벤트 유형을 각각 별도 요청으로 등록하는 것은 허용한다.
- 같은 클립에 같은 이벤트 유형을 다시 등록하는 것은 금지한다.
- 중복 요청 시 저장하지 않고 해당 클립에 해당 유형이 이미 연결되어 있다는 사용자 메시지를 반환한다.
- 클립 연결 등록 시 선택한 이벤트 유형에 대응하는 `player_record` 요약 값이 1 증가한다.
- 연결 등록 시 `value`는 백엔드에서 항상 1로 결정한다.
- 사용자는 `eventStartTimeSec`, `eventEndTimeSec`, `value`를 입력하지 않는다.
- 백엔드는 선택한 클립의 원본 경기 영상 구간을 조회해 이벤트 시작·종료 시간을 자동 저장한다.
- `player_record_event.event_start_time_sec`, `event_end_time_sec`, `value` 컬럼은 DB에 유지한다.
- 이벤트 시간과 값은 등록 당시 클립 정보의 스냅샷과 이력 확인용 데이터다.
- 클립 시간이 나중에 수정되어도 기존 이벤트 시간 스냅샷은 변경하지 않는다.

---

## 3. 기능 목적

선수 기록 기능의 목적은 다음과 같다.

- 한 경기에서 특정 선수의 전체 경기 기록을 관리한다.
- 경기 영상을 보면서 지도자와 분석관이 기록을 입력할 수 있게 한다.
- 분석 클립을 선수 기록의 영상 근거로 연결한다.
- 클립 연결 기록을 선수 요약 기록에 자동 반영한다.
- 선수는 본인의 경기별 기록과 연결 근거를 조회할 수 있게 한다.
- 추후 시즌 누적 기록, 선수 비교, 데이터 시각화, AI 이벤트 분석으로 확장할 수 있게 한다.

---

## 4. 사용자 역할

### 4.1 지도자 `COACH`

가능한 기능은 다음과 같다.

- 경기 영상 기준 선수 기록 등록
- 경기 영상 기준 기존 선수 기록 갱신
- 팀 분석 클립 연결 기록 등록
- 선수 개인 분석 클립 연결 기록 등록
- 전체 선수 기록 검색
- 전체 선수 기록 목록 조회
- 전체 선수 기록 상세 조회
- 선수 기록 이벤트 목록 조회
- 선수 기록 이벤트 상세 조회

`PlayerRecordPage`에서는 조회 기능만 제공한다.

### 4.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

- 경기 영상 기준 선수 기록 등록
- 경기 영상 기준 기존 선수 기록 갱신
- 팀 분석 클립 연결 기록 등록
- 선수 개인 분석 클립 연결 기록 등록
- 전체 선수 기록 검색
- 전체 선수 기록 목록 조회
- 전체 선수 기록 상세 조회
- 선수 기록 이벤트 목록 조회
- 선수 기록 이벤트 상세 조회

`PlayerRecordPage`에서는 조회 기능만 제공한다.

### 4.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

- 본인 선수 기록 목록 조회
- 본인 선수 기록 상세 조회
- 본인 선수 기록 이벤트 목록 조회
- 본인 선수 기록 이벤트 상세 조회
- 본인에게 공개된 연결 클립 조회

불가능한 기능은 다음과 같다.

- 다른 선수 기록 조회
- 선수 기록 등록
- 선수 기록 갱신
- 선수 기록 이벤트 등록
- 선수 기록 이벤트 관리

선수 조회 API는 요청값의 `playerId`를 신뢰하지 않고 로그인한 선수의 `memberId`를 기준으로 접근 범위를 제한한다.

---

## 5. 권한 정책

| 기능 | COACH | ANALYST | PLAYER |
|---|---:|---:|---:|
| 경기 영상 기준 선수 기록 등록·갱신 | 가능 | 가능 | 불가 |
| 클립 연결 기록 등록 | 가능 | 가능 | 불가 |
| 관리용 선수 기록 검색·목록·상세 | 가능 | 가능 | 불가 |
| 관리용 이벤트 목록·상세 | 가능 | 가능 | 불가 |
| 본인 선수 기록 조회 | 가능 | 가능 | 본인만 가능 |
| 본인 이벤트 조회 | 가능 | 가능 | 본인만 가능 |

권한 검증은 반드시 백엔드에서 처리한다.

`isAdmin = true`만으로 선수 기록 관리 권한을 부여하지 않는다.

---

## 6. 데이터 역할

### 6.1 `player_record`

`player_record`는 경기별 선수 최종 요약 기록이다.

활성 기록의 논리적 유일 기준은 다음과 같다.

```text
upload_id + player_id + is_deleted = false
```

같은 경기와 같은 선수의 활성 기록은 하나만 유지한다.

주요 필드는 다음과 같다.

```text
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
```

### 6.2 `player_record_event`

`player_record_event`는 분석 클립과 연결된 개별 기록 근거다.

이번 최종 정책에서는 신규 이벤트는 반드시 팀 분석 클립 또는 선수 개인 분석 클립과 연결해서 생성한다.

주요 필드는 다음과 같다.

```text
player_record_id
event_type
event_start_time_sec
event_end_time_sec
value
memo
created_by
```

`event_start_time_sec`, `event_end_time_sec`, `value`는 사용자가 입력하지 않는다.

백엔드가 다음 방식으로 자동 결정한다.

```text
event_start_time_sec = 선택한 클립의 원본 경기 영상 시작 시간
event_end_time_sec = 선택한 클립의 원본 경기 영상 종료 시간
value = 1
```

### 6.3 `player_record_event_clip`

`player_record_event_clip`은 이벤트와 분석 클립의 연결 정보를 저장한다.

지원 소스는 다음과 같다.

```text
TEAM_ANALYSIS
PLAYER_ANALYSIS
```

유효한 연결 조합은 다음과 같다.

```text
TEAM_ANALYSIS
→ team_clip_id만 존재
→ player_clip_id는 null

PLAYER_ANALYSIS
→ player_clip_id만 존재
→ team_clip_id는 null
```

두 클립 ID가 동시에 존재하거나 둘 다 없는 연결 데이터는 허용하지 않는다.

---

## 7. 이벤트 유형

지원 이벤트 유형은 다음과 같다.

```text
GOAL
ASSIST
SHOT
SHOT_ON_TARGET
PASS
SUCCESSFUL_PASS
DRIBBLE
SUCCESSFUL_DRIBBLE
TACKLE
INTERCEPTION
CLEARANCE
SAVE
YELLOW_CARD
RED_CARD
ETC
```

`ETC`는 개별 이벤트 메모와 근거 보존에는 사용할 수 있지만 대응하는 요약 기록 컬럼은 증가시키지 않는다.

---

## 8. 요약 기록 반영 정책

클립 연결 이벤트 등록 시 `value = 1`을 적용한다.

이벤트 유형별 요약 기록 반영은 다음과 같다.

| 이벤트 유형 | 증가하는 요약 기록 |
|---|---|
| `GOAL` | `goals + 1` |
| `ASSIST` | `assists + 1` |
| `SHOT` | `shots + 1` |
| `SHOT_ON_TARGET` | `shots + 1`, `shots_on_target + 1` |
| `PASS` | `passes + 1` |
| `SUCCESSFUL_PASS` | `passes + 1`, `successful_passes + 1` |
| `DRIBBLE` | `dribbles + 1` |
| `SUCCESSFUL_DRIBBLE` | `dribbles + 1`, `successful_dribbles + 1` |
| `TACKLE` | `tackles + 1` |
| `INTERCEPTION` | `interceptions + 1` |
| `CLEARANCE` | `clearances + 1` |
| `SAVE` | `saves + 1` |
| `YELLOW_CARD` | `yellow_cards + 1` |
| `RED_CARD` | `red_cards + 1` |
| `ETC` | 요약 기록 변경 없음 |

예시는 다음과 같다.

```text
기존 수치
슈팅 3
태클 2
패스 3

팀 분석 클립 연결
이벤트 유형 SHOT 등록

최종 수치
슈팅 4
태클 2
패스 3
```

---

## 9. 중복 연결 정책

한 번의 요청에서는 다음 조합만 등록할 수 있다.

```text
클립 1개 + 이벤트 유형 1개
```

같은 클립에 다른 이벤트 유형을 각각 별도로 등록하는 것은 허용한다.

```text
클립 10 + SHOT
클립 10 + PASS
클립 10 + TACKLE
→ 허용
```

같은 클립에 같은 이벤트 유형을 다시 등록하는 것은 금지한다.

```text
클립 10 + SHOT
클립 10 + SHOT
→ 두 번째 등록 차단
```

중복 판단 기준은 다음과 같다.

```text
clip_source_type
+ 실제 clip_id
+ event_type
+ event is_deleted = false
+ event_clip is_deleted = false
```

팀 분석 클립은 여러 선수가 등장할 수 있더라도 같은 클립과 같은 이벤트 유형의 활성 연결은 하나만 허용한다.

중복 요청 시 저장하지 않고 다음 취지의 메시지를 반환한다.

```text
선택한 클립에는 해당 선수 기록 유형이 이미 연결되어 있습니다.
```

HTTP 상태는 충돌을 의미하는 `409 Conflict` 사용을 권장한다.

정확한 `ErrorCode` 상수명은 구현 시 현재 프로젝트의 네이밍 규칙을 확인해 확정한다.

동시 요청으로 중복 저장되지 않도록 트랜잭션 범위에서 중복 검증을 수행한다.

---

## 10. 화면 흐름

### 10.1 `PlayerRecordPage`

`PlayerRecordPage`는 조회 전용 화면이다.

유지 기능은 다음과 같다.

- 검색 조건 입력
- 선수 기록 목록 조회
- 선수 기록 상세 조회
- 연결된 이벤트 목록 조회
- 이벤트 상세 조회
- 연결된 클립 정보 조회

제거 기능은 다음과 같다.

- 선수 기록 등록 폼
- 선수 기록 수정 폼
- 선수 기록 삭제 버튼
- 선수 기록 이벤트 등록 폼
- 선수 기록 이벤트 수정 폼
- 선수 기록 이벤트 삭제 버튼

### 10.2 `MatchVideoPage`

`MatchVideoPage`는 선수 기록 등록 허브다.

버튼명은 다음을 사용한다.

```text
선수 기록 등록
```

버튼 클릭 후 등록 방식을 선택한다.

```text
클립 없이 등록
팀 분석 클립 연결
선수 개인 분석 클립 연결
```

---

## 11. 클립 없이 등록 화면

### 11.1 기본 흐름

1. 지도자 또는 분석관이 경기 영상을 선택한다.
2. `선수 기록 등록` 버튼을 클릭한다.
3. `클립 없이 등록`을 선택한다.
4. 대상 선수를 선택한다.
5. 현재 경기와 선택 선수 기준 활성 `player_record`를 조회한다.
6. 기존 기록이 있으면 기존 값을 표시한다.
7. 기존 기록이 없으면 모든 수치를 0으로 표시한다.
8. 각 항목의 `-`, `+` 버튼으로 1씩 조정한다.
9. 여러 항목을 한 번에 조정한다.
10. 경기 전체 메모를 입력한다.
11. 최종 저장 버튼을 클릭한다.
12. 기존 기록이 있으면 갱신하고, 없으면 생성한다.

### 11.2 표시 항목

```text
출전 시간
득점
도움
슈팅
유효 슈팅
패스
성공 패스
드리블
성공 드리블
태클
인터셉트
클리어링
세이브
경고
퇴장
경기 전체 메모
```

### 11.3 버튼 정책

각 수치는 다음 UI를 사용한다.

```text
[-] 0 [+]
```

- `+` 클릭 시 1 증가
- `-` 클릭 시 1 감소
- 최소값은 0
- 한 번에 여러 항목 변경 가능
- 최종 저장 버튼 클릭 전에는 서버에 저장하지 않음

### 11.4 저장 정책

기존 활성 기록이 없는 경우 선수 기록 생성 API를 사용한다.

기존 활성 기록이 있는 경우 선수 기록 수정 API를 사용한다.

프론트는 조회 결과의 `recordId` 존재 여부로 생성과 수정을 구분한다.

한 번의 저장 요청에는 현재 화면의 전체 최종 수치를 전송한다.

---

## 12. 팀 분석 클립 연결 등록 화면

### 12.1 기본 흐름

1. `팀 분석 클립 연결`을 선택한다.
2. 현재 경기의 `READY` 팀 분석 클립 목록을 조회한다.
3. 연결할 팀 분석 클립 하나를 선택한다.
4. 기록 대상 선수 하나를 선택한다.
5. 이벤트 유형 하나를 선택한다.
6. 선택 입력으로 이벤트 메모를 작성한다.
7. 최종 등록 버튼을 클릭한다.
8. 백엔드가 클립의 경기 영상과 시작·종료 시간을 검증한다.
9. 같은 클립과 같은 이벤트 유형의 활성 연결이 있는지 검증한다.
10. `player_record`가 없으면 0으로 자동 생성한다.
11. 이벤트와 클립 연결을 생성한다.
12. 선택 이벤트 유형에 대응하는 요약 기록을 1 증가시킨다.

### 12.2 사용자에게 표시하지 않는 값

```text
eventStartTimeSec
eventEndTimeSec
value
```

### 12.3 자동 저장 값

```text
eventStartTimeSec = team_video_clip.start_time_sec
eventEndTimeSec = team_video_clip.end_time_sec
value = 1
```

---

## 13. 선수 개인 분석 클립 연결 등록 화면

### 13.1 기본 흐름

1. `선수 개인 분석 클립 연결`을 선택한다.
2. 현재 경기의 `READY` 선수 개인 분석 클립 목록을 조회한다.
3. 연결할 선수 개인 분석 클립 하나를 선택한다.
4. 클립의 대상 선수를 기록 대상 선수로 사용한다.
5. 화면에는 대상 선수를 읽기 전용으로 표시한다.
6. 이벤트 유형 하나를 선택한다.
7. 선택 입력으로 이벤트 메모를 작성한다.
8. 최종 등록 버튼을 클릭한다.
9. 백엔드가 클립의 경기 영상, 대상 선수, 시작·종료 시간을 검증한다.
10. 같은 클립과 같은 이벤트 유형의 활성 연결이 있는지 검증한다.
11. `player_record`가 없으면 0으로 자동 생성한다.
12. 이벤트와 클립 연결을 생성한다.
13. 선택 이벤트 유형에 대응하는 요약 기록을 1 증가시킨다.

### 13.2 자동 저장 값

```text
playerId = player_video_clip.player_id
eventStartTimeSec = player_video_clip.start_time_sec
eventEndTimeSec = player_video_clip.end_time_sec
value = 1
```

---

## 14. API 흐름

현재 기존 API 주소 체계를 우선 유지한다.

### 14.1 관리용 선수 기록 조회

```http
GET /api/management/player-records?page=0&size=20&uploadId={uploadId}&playerId={playerId}
GET /api/management/player-records/{recordId}
```

클립 없이 등록 화면은 `uploadId`, `playerId` 조건으로 기존 기록을 조회한다.

### 14.2 클립 없이 선수 기록 생성

```http
POST /api/management/player-records
```

기존 활성 기록이 없을 때 사용한다.

### 14.3 클립 없이 선수 기록 갱신

```http
PATCH /api/management/player-records/{recordId}
```

기존 활성 기록이 있을 때 사용한다.

### 14.4 클립 연결 기록 등록

기존 연결 API 주소를 유지하되 요청 DTO를 개편한다.

```http
POST /api/management/player-record-events/with-clip-link
```

요청에서 제거할 필드는 다음과 같다.

```text
eventStartTimeSec
eventEndTimeSec
value
```

팀 분석 클립 연결 요청 예시는 다음과 같다.

```json
{
  "uploadId": 1,
  "playerId": 5,
  "eventType": "SHOT",
  "eventMemo": "페널티 박스 우측 슈팅",
  "clipSourceType": "TEAM_ANALYSIS",
  "teamClipId": 10,
  "playerClipId": null
}
```

선수 개인 분석 클립 연결 요청 예시는 다음과 같다.

```json
{
  "uploadId": 1,
  "playerId": 5,
  "eventType": "PASS",
  "eventMemo": "전진 패스 성공 장면",
  "clipSourceType": "PLAYER_ANALYSIS",
  "teamClipId": null,
  "playerClipId": 20
}
```

선수 개인 분석 클립 방식의 `playerId`는 요청값만 신뢰하지 않고 클립 대상 선수와 반드시 일치하는지 백엔드에서 검증한다.

### 14.5 선수 본인 조회

```http
GET /api/player/me/player-records
GET /api/player/me/player-records/{recordId}
GET /api/player/me/player-records/{recordId}/events
GET /api/player/me/player-record-events/{eventId}
```

---

## 15. 백엔드 처리 흐름

### 15.1 클립 없이 기록 생성·갱신

1. JWT 인증 정보를 확인한다.
2. `COACH` 또는 `ANALYST`인지 검증한다.
3. 경기 영상이 존재하고 삭제되지 않았는지 확인한다.
4. 대상 회원이 존재하고 `PLAYER` 역할인지 확인한다.
5. 모든 수치가 0 이상 255 이하인지 확인한다.
6. 같은 경기와 선수의 활성 기록을 조회한다.
7. 활성 기록이 없으면 생성한다.
8. 활성 기록이 있으면 기존 데이터를 갱신한다.
9. 최초 생성 시 `recorder_id`를 저장한다.
10. 갱신 시 `recorder_id`는 유지하고 `last_modifier_id`를 갱신한다.

### 15.2 클립 연결 이벤트 등록

1. JWT 인증 정보를 확인한다.
2. `COACH` 또는 `ANALYST`인지 검증한다.
3. `clipSourceType`과 클립 ID 조합을 검증한다.
4. 선택한 클립이 존재하고 삭제되지 않았는지 확인한다.
5. 선택한 클립이 `READY` 상태인지 확인한다.
6. 요청 경기와 클립 원본 경기 영상이 일치하는지 확인한다.
7. 선수 개인 분석 클립이면 요청 선수와 클립 대상 선수가 일치하는지 확인한다.
8. 동일 클립과 동일 이벤트 유형의 활성 연결이 있는지 확인한다.
9. 중복이면 저장하지 않고 충돌 응답을 반환한다.
10. 같은 경기와 선수의 활성 `player_record`를 조회한다.
11. 없으면 모든 요약 값을 0으로 생성한다.
12. 클립의 시작·종료 시간을 조회한다.
13. `value = 1`로 결정한다.
14. `player_record_event`를 저장한다.
15. `player_record_event_clip`을 저장한다.
16. 이벤트 유형에 대응하는 요약 기록을 1 증가시킨다.
17. 전체 작업을 하나의 트랜잭션으로 처리한다.

---

## 16. 시간 스냅샷 정책

`event_start_time_sec`, `event_end_time_sec`는 DB에 유지한다.

용도는 다음과 같다.

- 기록 등록 당시 클립 구간 보존
- 클립 시간 수정 전 과거 상태 확인
- 소프트 삭제된 클립의 기록 이력 확인
- 이벤트 상세 조회 시 등록 당시 구간 표시
- 추후 감사 로그와 데이터 복구 참고

클립 시간이 수정되어도 기존 이벤트 시간은 자동 갱신하지 않는다.

예시는 다음과 같다.

```text
이벤트 등록 당시 클립
120초 ~ 135초

이후 클립 수정
115초 ~ 140초

player_record_event 스냅샷
120초 ~ 135초 유지
```

실제 클립 재생은 현재 연결된 클립 URL과 현재 클립 메타데이터를 사용한다.

이벤트 시간은 재생 제어의 원본값이 아니라 확인용 스냅샷이다.

---

## 17. 예외 상황

### 공통

- 인증되지 않은 사용자
- 관리 권한이 없는 사용자
- 존재하지 않거나 삭제된 경기 영상
- 존재하지 않거나 삭제된 선수
- 대상 회원이 `PLAYER`가 아닌 경우
- 기록 수치가 0 미만 또는 255 초과

### 클립 연결

- 존재하지 않거나 삭제된 클립
- `READY`가 아닌 클립
- 경기 영상과 클립 원본 영상 불일치
- `clipSourceType`과 클립 ID 조합 오류
- 팀 클립 ID와 선수 클립 ID가 동시에 존재
- 선수 개인 클립 대상 선수 불일치
- 같은 클립과 같은 이벤트 유형의 중복 연결
- 클립 시작·종료 시간 정보 오류

중복 연결 사용자 메시지는 다음 취지를 사용한다.

```text
선택한 클립에는 해당 선수 기록 유형이 이미 연결되어 있습니다.
```

---

## 18. DB 설계 방향

이번 정책 변경으로 신규 테이블이나 신규 컬럼은 필요하지 않다.

유지 테이블은 다음과 같다.

```text
player_record
player_record_event
player_record_event_clip
team_video_clip
player_video_clip
game_video_upload
member
```

유지 컬럼은 다음과 같다.

```text
player_record_event.event_start_time_sec
player_record_event.event_end_time_sec
player_record_event.value
```

중복 연결은 우선 Service/Repository 계층에서 검증한다.

동시 요청 중복 방지가 필요하므로 트랜잭션 잠금 또는 DB 수준 보강 방식을 구현 단계에서 검토한다.

---

## 19. 프론트엔드 구현 방향

주요 수정 파일은 다음과 같다.

```text
frontend/src/pages/PlayerRecordPage.tsx
frontend/src/pages/MatchVideoPage.tsx
frontend/src/components/analysis/PlayerRecordEventEditorPanel.tsx
frontend/src/types/playerRecord.ts
frontend/src/types/playerRecordEvent.ts
frontend/src/api/playerRecordApi.ts
frontend/src/api/playerRecordEventApi.ts
frontend/src/constants/routes.ts
```

컴포넌트명 `PlayerRecordEventEditorPanel`은 최종 역할에 맞춰 `PlayerRecordEditorPanel` 등으로 변경할 수 있다.

정확한 파일명 변경은 실제 참조 범위를 확인한 후 진행한다.

---

## 20. 구현 순서

### 백엔드

1. 기존 선수 기록 Controller, DTO, Service, Validator, Repository 확인
2. 기존 이벤트-클립 연결 DTO와 서비스 확인
3. 요구사항 문서와 기존 구현 차이 정리
4. 클립 연결 요청 DTO에서 시간과 `value` 제거
5. 클립 시간 자동 추출 로직 구현
6. `value = 1` 고정 처리
7. 동일 클립·동일 이벤트 유형 중복 검증 구현
8. 기존 기록이 없을 때 자동 생성 로직 확인 및 보완
9. 요약 기록 +1 반영 로직 확인
10. 트랜잭션 테스트
11. 권한 테스트
12. 중복 요청 테스트
13. 기존 조회 API 회귀 테스트

### 프론트엔드

1. 백엔드 DTO와 API 확정 후 타입 동기화
2. `PlayerRecordPage` 등록·수정·삭제 UI 제거
3. `MatchVideoPage` 버튼명을 `선수 기록 등록`으로 변경
4. 선수 기록 등록 패널 재구성
5. 클립 없이 등록 전체 카운터 UI 구현
6. 기존 기록 조회 후 생성·수정 분기
7. 팀 분석 클립 연결 목록 구현
8. 선수 개인 분석 클립 연결 목록 구현
9. 클립 연결 시 이벤트 유형 단일 선택 구현
10. 중복 오류 메시지 표시
11. 빌드와 전체 회귀 테스트

---

## 21. 테스트 기준

### 클립 없이 등록

- 기존 기록이 없으면 모든 값 0 표시
- 여러 항목 `+` 조정
- 여러 항목 `-` 조정
- 0 아래로 감소하지 않음
- 최종 저장 한 번으로 전체 값 저장
- 기존 기록이 있으면 기존 값 표시
- 기존 값에서 갱신 저장
- 중복 `player_record`가 생성되지 않음

### 팀 분석 클립 연결

- 같은 경기의 `READY` 팀 클립만 표시
- 클립 하나 선택
- 대상 선수 선택
- 이벤트 유형 하나 선택
- 시간과 `value` 입력칸 미노출
- 선택 유형 요약 기록 +1
- 같은 클립의 다른 유형 등록 허용
- 같은 클립의 같은 유형 재등록 차단

### 선수 개인 분석 클립 연결

- 같은 경기의 `READY` 개인 클립만 표시
- 개인 클립의 대상 선수 자동 적용
- 이벤트 유형 하나 선택
- 선택 유형 요약 기록 +1
- 같은 클립의 다른 유형 등록 허용
- 같은 클립의 같은 유형 재등록 차단

### 권한

- `COACH`, `ANALYST` 등록 가능
- `PLAYER` 등록 API 차단
- 선수는 본인 조회만 가능

### 회귀

- 관리용 선수 기록 검색 정상
- 관리용 목록 조회 정상
- 관리용 상세 조회 정상
- 선수 본인 목록 조회 정상
- 선수 본인 상세 조회 정상
- 이벤트 목록·상세 조회 정상

---

## 22. 추후 확장 가능성

- 선수 기록 입력 항목 사용자 설정
- 경기 포지션별 기록 항목 분리
- 시즌 누적 기록
- 경기별 기록 비교
- 선수 간 기록 비교
- 기록 차트와 시각화
- 이벤트 클립 바로 재생
- 이벤트 수정·삭제 전용 관리 화면
- AI 이벤트 자동 인식
- AI가 제안한 기록의 지도자 승인 흐름
- 기록 변경 감사 로그
- 중복 요청 방지를 위한 DB 유니크 구조 보강

---

## 23. 이번 정책 변경 요약

기존 정책에서 변경된 핵심 내용은 다음과 같다.

- 선수 기록 쓰기 기능을 `PlayerRecordPage`에서 `MatchVideoPage`로 이동
- `PlayerRecordPage`를 조회 전용으로 변경
- 버튼명을 `선수 기록 이벤트 등록`에서 `선수 기록 등록`으로 변경
- 클립 없이 등록 시 요약 기록 전체를 한 번에 생성·갱신
- 기존 기록이 있으면 기존 값을 불러와 갱신
- 기록 카운터를 숫자 입력에서 `-`, `+` 버튼 방식으로 변경
- 클립 연결 시 사용자 시간·수치 입력 제거
- 클립 시간과 `value = 1`을 백엔드 자동 결정
- 같은 클립의 다른 유형 등록 허용
- 같은 클립의 같은 유형 중복 등록 차단
- 이벤트 시간과 수치를 DB 스냅샷으로 유지
