# 28. 경기 영상 기반 클립·선수 기록 이벤트 연결 요구사항

## 1. 결론

이번 기능은 기존에 계획했던 단순 선수 기록 프론트 연동 작업이 아니다.

기능 방향을 다음처럼 변경한다.

기존 방향:

```text
/player-records
→ 지도자/분석관이 선수 기록을 별도 페이지에서 등록/수정/삭제
→ 선수는 본인 기록 조회
```

수정 방향:

```text
/match-videos
→ 경기 영상 상세 화면에서 원본 경기를 보며 클립 생성
→ 팀 분석 클립 또는 선수 개인 분석 클립 선택
→ 시작/종료 시간 설정
→ 드로잉 작성
→ 제목/코멘트 등 클립 저장 정보 입력
→ 클립 생성 요청
→ 생성된 클립을 선수 기록 이벤트와 선택적으로 연결

/match-videos
→ 클립 생성 없이 선수 기록 이벤트만 빠르게 등록 가능

/player-records
→ 선수는 본인 경기 기록과 기록 이벤트, 연결된 클립 조회
```

핵심 목표는 다음과 같다.

- 지도자/분석관이 경기 영상을 보면서 실시간에 가깝게 분석 작업을 할 수 있게 한다.
- 팀 분석 클립과 선수 개인 분석 클립 생성을 경기 영상 상세 화면 중심으로 통합한다.
- 선수 기록을 단순 숫자 데이터가 아니라 영상 클립과 연결 가능한 기록 이벤트 구조로 확장한다.
- 클립과 연결된 기록도 저장할 수 있고, 클립 없이 기록만 저장할 수도 있어야 한다.
- 선수는 본인 기록 페이지에서 기록 타입별로 연결된 클립을 확인할 수 있게 한다.
- 기존 `player_record`는 경기별 선수 요약 기록으로 유지한다.
- 신규 `player_record_event`, `player_record_event_clip` 구조를 추가한다.
- `player_record_event_clip`은 필수 연결이 아니라 선택 연결 구조다.

---

## 2. 기능 목적

이번 기능의 목적은 선수 기록을 단순 수치 입력 기능이 아니라 경기 영상 분석 결과와 연결 가능한 실전 피드백 데이터로 바꾸는 것이다.

지도자와 분석관은 경기 영상을 보면서 다음 두 가지 방식으로 기록을 남길 수 있어야 한다.

### 2.1 클립과 연결된 기록 등록

```text
경기 장면 확인
→ 클립 생성
→ 드로잉 작성
→ 클립 저장
→ 선수 기록 이벤트 등록
→ 생성된 클립과 선수 기록 이벤트 연결
```

### 2.2 클립 없이 기록만 등록

```text
경기 장면 확인
→ 영상 하단 기록 등록 폼에서 선수 선택
→ 기록 타입 선택
→ 이벤트 시간/메모 입력
→ 선수 기록 이벤트만 저장
```

선수는 본인 기록을 볼 때 숫자만 확인하는 것이 아니라, 해당 기록이 발생한 장면의 클립이 있으면 함께 볼 수 있어야 한다.

예시는 다음과 같다.

```text
A팀전

골 1
- 75:12 득점 장면 보기

슈팅 3
- 12:30 슈팅 장면 보기
- 44:10 클립 없음
- 81:22 슈팅 장면 보기

태클 4
- 15:05 태클 장면 보기
- 39:20 클립 없음
```

---

## 3. 사용자 역할

## 3.1 지도자 `COACH`

지도자는 경기 영상 상세 화면에서 다음 작업을 할 수 있다.

- 원본 경기 영상 조회
- 팀 분석 클립 생성
- 선수 개인 분석 클립 생성
- 드로잉 작성
- 클립 저장
- 선수 기록 요약 등록/수정/삭제
- 선수 기록 이벤트 등록/수정/삭제
- 선수 기록 이벤트와 클립 연결
- 클립 없이 선수 기록 이벤트만 등록
- 선수 기록 전체 조회

## 3.2 분석관 `ANALYST`

분석관은 기존 선수 기록 최종 정책 기준으로 선수 기록 등록, 조회, 수정, 삭제가 가능하다.

이번 기능에서도 분석관은 다음 작업을 할 수 있다.

- 원본 경기 영상 조회
- 팀 분석 클립 생성
- 선수 개인 분석 클립 생성
- 드로잉 작성
- 클립 저장
- 선수 기록 요약 등록/수정/삭제
- 선수 기록 이벤트 등록/수정/삭제
- 선수 기록 이벤트와 클립 연결
- 클립 없이 선수 기록 이벤트만 등록
- 선수 기록 전체 조회

단, 다른 영상 기능에서는 분석관 삭제 권한이 제한될 수 있으므로, 이번 기능에서 분석관 삭제 권한은 선수 기록 관련 데이터에 한정한다.

## 3.3 선수 `PLAYER`

선수는 조회 중심 사용자다.

가능한 기능은 다음과 같다.

- 본인 경기별 선수 기록 조회
- 본인 기록 이벤트 조회
- 본인 기록 이벤트에 연결된 클립 조회
- 본인 개인 분석 클립 조회
- 팀 분석 클립 조회

불가능한 기능은 다음과 같다.

- 선수 기록 등록
- 선수 기록 수정
- 선수 기록 삭제
- 선수 기록 이벤트 등록
- 선수 기록 이벤트 수정
- 선수 기록 이벤트 삭제
- 다른 선수의 기록 조회
- 다른 선수의 개인 분석 클립 조회

---

## 4. 권한 정책

| 역할 | 클립 생성 | 기록 요약 등록 | 기록 이벤트 등록 | 클립 연결 | 본인 기록 조회 | 전체 기록 조회 |
|---|---:|---:|---:|---:|---:|---:|
| `COACH` | 가능 | 가능 | 가능 | 가능 | 가능 | 가능 |
| `ANALYST` | 가능 | 가능 | 가능 | 가능 | 가능 | 가능 |
| `PLAYER` | 불가 | 불가 | 불가 | 불가 | 가능 | 불가 |

권한 검증은 반드시 백엔드에서 처리한다.

프론트 권한 처리는 사용자 경험을 위한 버튼과 영역 노출 제어만 담당한다.

프론트에서 버튼이 보이지 않아도 사용자가 API를 직접 호출할 수 있으므로, 백엔드에서 다음을 반드시 검증한다.

- 인증 여부
- 사용자 역할
- 선수 본인 기록 접근 여부
- 삭제된 기록 접근 차단
- 삭제된 기록 이벤트 접근 차단
- 삭제된 클립 연결 차단
- 팀 클립/선수 개인 클립 접근 가능 여부
- 선수 개인 분석 클립의 대상 선수와 기록 대상 선수 일치 여부

---

## 5. 기존 구조와 변경 구조

## 5.1 기존 구조

기존 구조는 기능별 페이지가 분리되어 있다.

```text
/match-videos
/team-analysis-clips/new
/player-analysis-clips/new
/player-records
```

이 구조는 구현은 명확하지만 실제 분석 작업 흐름에는 불편하다.

지도자와 분석관은 원본 경기 영상을 보다가 분석 장면을 발견했을 때 바로 클립을 만들고 기록과 연결해야 한다.

따라서 신규 등록 흐름은 `/match-videos` 중심으로 변경한다.

## 5.2 변경 구조

변경 후 구조는 다음과 같다.

```text
/match-videos
→ 경기 목록 조회
→ 경기 상세 조회
→ 원본 영상 재생
→ 분석 작업 시작
→ 클립 생성
→ 드로잉 작성
→ 클립 저장
→ 선수 기록 이벤트 등록 및 선택적 연결
→ 또는 클립 없이 선수 기록 이벤트만 등록

/team-analysis-clips
→ 팀 분석 클립 목록/상세 조회
→ 기존 클립 수정/삭제

/player-analysis-clips
→ 선수 개인 분석 클립 목록/상세 조회
→ 기존 클립 수정/삭제
→ 선수 조회 기록 확인

/player-records
→ 선수 본인 기록 조회
→ 기록 이벤트별 연결 클립 조회
```

`/team-analysis-clips/new`, `/player-analysis-clips/new`는 당장 삭제하지 않는다.

다만 신규 등록의 주 진입점은 `/match-videos`로 이동한다.

추후 안정화 후 별도 등록 페이지 제거 여부를 결정한다.

---

## 6. 화면 흐름

## 6.1 지도자/분석관 경기 영상 기반 작업 흐름

경로는 다음을 사용한다.

```text
/match-videos
```

흐름은 다음과 같다.

1. 지도자 또는 분석관이 경기 영상 관리 화면에 진입한다.
2. 경기 영상 목록을 조회한다.
3. 특정 경기 영상을 선택한다.
4. 하단에 경기 상세 정보와 원본 영상이 표시된다.
5. 원본 영상 재생 중 `분석 작업 시작` 버튼을 클릭한다.
6. 클립 생성 패널 또는 모달이 열린다.
7. 클립 시작 시간을 현재 영상 시간 기준으로 설정한다.
8. 클립 종료 시간을 현재 영상 시간 기준으로 설정한다.
9. 클립 유형을 선택한다.
10. 드로잉 작업을 진행한다.
11. 드로잉 작업 완료 후 `클립 저장` 버튼을 클릭한다.
12. 제목, 코멘트, 클립 세부 유형 등 저장 정보를 입력한다.
13. 클립 생성 API를 호출한다.
14. 클립 생성 요청이 성공하면 생성된 클립 ID를 기준으로 선수 기록 이벤트 연결 창을 표시한다.
15. 선수 기록 이벤트를 등록하거나 기록 연결을 건너뛸 수 있다.
16. 저장 완료 후 경기 영상 상세 화면에 머문다.

## 6.2 영상 하단 선수 기록만 등록 흐름

클립을 생성하지 않고 기록만 등록할 수도 있어야 한다.

흐름은 다음과 같다.

1. 지도자 또는 분석관이 경기 영상 상세 화면에서 원본 영상을 재생한다.
2. 영상 하단의 `선수 기록 등록` 폼을 확인한다.
3. 기록 대상 선수를 선택한다.
4. 기록 이벤트 타입을 선택한다.
5. 이벤트 발생 시간을 현재 영상 시간 기준으로 설정한다.
6. 필요하면 이벤트 메모를 입력한다.
7. `기록 저장` 버튼을 클릭한다.
8. 백엔드는 해당 경기와 선수의 `player_record`를 조회한다.
9. 없으면 `player_record`를 자동 생성한다.
10. `player_record_event`를 새로 저장한다.
11. `player_record` 요약 수치를 갱신한다.
12. 클립 연결은 생성하지 않는다.

---

## 7. 클립 생성 단계

## 7.1 1단계: 클립 범위 설정

입력값은 다음과 같다.

```text
startTimeSec
endTimeSec
```

설정 방식은 다음과 같다.

- 현재 영상 시간 기준으로 시작 시간 설정
- 현재 영상 시간 기준으로 종료 시간 설정
- 시작 시간 초기화
- 종료 시간 초기화
- 선택 구간 미리보기

## 7.2 2단계: 클립 분류 선택

입력값은 다음과 같다.

```text
clipSourceType
```

값은 다음과 같다.

```text
TEAM_ANALYSIS
PLAYER_ANALYSIS
```

`TEAM_ANALYSIS` 선택 시 팀 분석 클립 생성 API를 사용한다.

`PLAYER_ANALYSIS` 선택 시 선수 개인 분석 클립 생성 API를 사용한다.

## 7.3 3단계: 대상 선수 선택

대상 선수 선택 정책은 다음과 같다.

```text
TEAM_ANALYSIS
→ 클립 대상 선수 선택은 필수가 아니다.
→ 하지만 저장 후 선수 기록 이벤트 연결 단계에서 기록 대상 선수는 선택할 수 있어야 한다.
→ 팀 분석 클립도 선수 기록 이벤트와 연결 가능하다.

PLAYER_ANALYSIS
→ 클립 대상 선수 선택은 필수다.
→ 해당 선수의 개인 분석 클립으로 저장된다.
→ 저장 후 같은 선수를 기본 기록 대상 선수로 제안한다.
```

예시는 다음과 같다.

```text
팀 분석 클립: 전방 압박 성공 장면
→ 기록 대상 선수: 홍길동
→ 이벤트 타입: TACKLE
→ 연결 클립: 방금 생성한 팀 분석 클립
```

## 7.4 4단계: 드로잉 작성

드로잉 유형은 기존 정책을 따른다.

```text
LINE
ARROW
CIRCLE
BOX
AREA
TEXT
```

드로잉 시간 기준은 생성된 클립 영상 기준 초다.

예시는 다음과 같다.

```text
원본 영상 100초 ~ 115초 구간을 클립으로 생성
드로잉은 2초 ~ 6초처럼 클립 내부 시간 기준으로 저장
```

## 7.5 5단계: 클립 저장 정보 입력

공통 입력값은 다음과 같다.

```text
title
comment
clipType
```

`TEAM_ANALYSIS`인 경우 팀 분석 클립 유형을 사용한다.

예시는 다음과 같다.

```text
HIGHLIGHT
ATTACK
DEFENSE
GOAL
CONCEDED
OFFSIDE
SETPIECE
ETC
```

`PLAYER_ANALYSIS`인 경우 선수 개인 분석 클립 유형을 사용한다.

예시는 다음과 같다.

```text
PLAYER_GOOD
PLAYER_MISTAKE
SHOOTING
PASS
DRIBBLE
DEFENSE
POSITIONING
PRESSING
OFF_THE_BALL
ETC
```

## 7.6 6단계: 클립 생성 요청

클립 생성 요청은 기존 API를 우선 재사용한다.

```http
POST /api/management/team-analysis-clips/with-drawings
POST /api/management/player-analysis-clips/with-drawings
```

백엔드는 기존처럼 클립 row를 저장하고, 실제 mp4 파일 생성은 비동기로 처리한다.

클립 생성 응답에는 선수 기록 이벤트 연결에 필요한 clip ID가 포함되어야 한다.

필요하면 기존 응답 DTO에 다음 값을 명확히 포함한다.

```text
clipSourceType
teamClipId
playerClipId
```

---

## 8. 선수 기록 이벤트 등록 흐름

선수 기록 이벤트는 두 방식으로 등록할 수 있다.

```text
1. 클립 없이 기록 이벤트만 등록
2. 클립 생성 후 기록 이벤트와 클립을 연결해 등록
```

따라서 `player_record_event`는 단독으로 존재할 수 있다.

`player_record_event_clip`은 선택 연결 테이블이다.

관계는 다음과 같다.

```text
player_record 1 : N player_record_event
player_record_event 1 : 0..N player_record_event_clip
```

`player_record`는 같은 `upload_id`, `player_id` 기준으로 1개만 유지한다.

기록 이벤트 등록 시 해당 경기·선수의 `player_record`가 없으면 자동 생성한다.

이미 존재하면 새 `player_record`를 생성하지 않고 기존 `player_record`의 요약 수치를 갱신한다.

`player_record_event`는 기록 이벤트가 등록될 때마다 새로 생성한다.

예시는 다음과 같다.

```text
같은 경기의 같은 선수가 슈팅을 3번 등록

player_record
- shots = 3

player_record_event
- SHOT 이벤트 3개
```

클립이 없는 기록 이벤트도 선수 기록 화면에서 표시한다.

선수 화면에서는 클립 연결 여부를 구분해 표시한다.

```text
슈팅 3
- 12:30 슈팅 장면 보기
- 44:10 클립 없음
- 70:20 슈팅 장면 보기
```

---

## 9. 선수 기록 이벤트 연결 흐름

클립 저장 요청이 성공하면 프론트는 선수 기록 이벤트 연결 창을 표시한다.

입력값은 다음과 같다.

```text
recordTargetPlayerId
eventType
eventTimeSec
eventMemo
```

정책은 다음과 같다.

- `recordTargetPlayerId`는 기록 대상 선수 ID다.
- `eventType`은 기록 이벤트 타입이다.
- `eventTimeSec`는 원본 경기 영상 기준 이벤트 발생 시간이다.
- `eventMemo`는 특정 기록 이벤트에 대한 메모다.
- `eventMemo`는 경기 전체 기록 메모인 `player_record.memo`와 다르다.
- 팀 분석 클립과 선수 개인 분석 클립 모두 선수 기록 이벤트에 연결할 수 있다.
- 기록 이벤트 연결은 선택 사항이다.

이 단계에서 백엔드는 다음을 처리한다.

1. 해당 경기와 선수에 대한 `player_record`가 있는지 확인한다.
2. 없으면 기본값 0으로 `player_record`를 생성한다.
3. 있으면 기존 `player_record`를 사용한다.
4. `player_record_event`를 생성한다.
5. 생성된 클립과 연결하는 경우 `player_record_event_clip`을 저장한다.
6. 이벤트 타입에 따라 `player_record`의 요약 수치를 증가시킨다.

---

## 10. DB 설계 방향

## 10.1 기존 `player_record`

기존 `player_record`는 유지한다.

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

`player_record`의 의미는 다음과 같다.

```text
특정 선수가 특정 경기에서 남긴 전체 요약 기록
```

`player_record.memo`는 경기 전체 또는 해당 선수 경기 기록 전체에 대한 메모다.

같은 `upload_id`, `player_id` 기준으로 활성 `player_record`는 1개만 유지한다.

초기에는 DB unique key를 바로 추가하지 않고 Service/Validator에서 중복 생성을 막는다.

추후 안정화 후 다음 유니크 정책을 검토한다.

```text
upload_id + player_id + is_deleted
```

단, MySQL에서 soft delete와 unique key 조합은 운영상 제약이 있을 수 있으므로 별도 검토 후 적용한다.

## 10.2 신규 테이블 `player_record_event`

`player_record_event`는 특정 기록 이벤트를 저장한다.

`upload_id`, `player_id`는 저장하지 않는다.

이유는 다음과 같다.

- `player_record_event.player_record_id`를 통해 `player_record`를 알 수 있다.
- `player_record`에서 `upload_id`, `player_id`를 확인할 수 있다.
- 중복 저장을 줄이고 정합성을 유지할 수 있다.

테이블 구조는 다음과 같다.

```sql
CREATE TABLE `player_record_event` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `player_record_id` INT NOT NULL COMMENT '선수 경기 요약 기록 ID',
    `event_type` VARCHAR(30) NOT NULL COMMENT 'GOAL, ASSIST, SHOT, SHOT_ON_TARGET, PASS, SUCCESSFUL_PASS, DRIBBLE, SUCCESSFUL_DRIBBLE, TACKLE, INTERCEPTION, CLEARANCE, SAVE, YELLOW_CARD, RED_CARD, ETC',
    `event_time_sec` INT UNSIGNED NULL COMMENT '원본 경기 영상 기준 이벤트 발생 시간(초)',
    `value` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '이벤트 반영 값',
    `memo` VARCHAR(255) NULL COMMENT '이벤트 단위 메모',
    `created_by` INT NOT NULL COMMENT '이벤트 작성 회원 ID',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_player_record_event_record`
        FOREIGN KEY (`player_record_id`) REFERENCES `player_record` (`id`),
    CONSTRAINT `fk_player_record_event_created_by`
        FOREIGN KEY (`created_by`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

`player_record_event.memo`는 특정 이벤트 단위 메모다.

프론트와 DTO에서는 의미를 명확히 하기 위해 `eventMemo`라는 필드명을 사용할 수 있다.

## 10.3 신규 테이블 `player_record_event_clip`

`player_record_event_clip`은 기록 이벤트와 클립을 연결한다.

팀 분석 클립과 선수 개인 분석 클립을 모두 연결할 수 있어야 한다.

기록 이벤트는 클립 연결 없이도 존재할 수 있으므로 이 테이블은 선택 연결 테이블이다.

테이블 구조는 다음과 같다.

```sql
CREATE TABLE `player_record_event_clip` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `player_record_event_id` INT NOT NULL COMMENT '선수 기록 이벤트 ID',
    `clip_source_type` VARCHAR(30) NOT NULL COMMENT 'TEAM_ANALYSIS, PLAYER_ANALYSIS',
    `team_clip_id` INT NULL COMMENT '팀 분석 클립 ID',
    `player_clip_id` INT NULL COMMENT '선수 개인 분석 클립 ID',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_player_record_event_clip_event`
        FOREIGN KEY (`player_record_event_id`) REFERENCES `player_record_event` (`id`),
    CONSTRAINT `fk_player_record_event_clip_team_clip`
        FOREIGN KEY (`team_clip_id`) REFERENCES `team_video_clip` (`id`),
    CONSTRAINT `fk_player_record_event_clip_player_clip`
        FOREIGN KEY (`player_clip_id`) REFERENCES `player_video_clip` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

검증 정책은 다음과 같다.

```text
clip_source_type = TEAM_ANALYSIS
→ team_clip_id 필수
→ player_clip_id null

clip_source_type = PLAYER_ANALYSIS
→ player_clip_id 필수
→ team_clip_id null
```

DB 차원에서 CHECK 제약을 강하게 걸 수도 있지만, 초기에는 MySQL 호환성과 JPA 처리를 고려해 Service/Validator에서 우선 검증한다.

---

## 11. 기록 이벤트 타입 Enum

신규 Enum을 추가한다.

```text
PlayerRecordEventTypeEnum
```

초기 값은 다음과 같다.

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

이 값은 기존 `player_record` 수치 컬럼과 연결된다.

추후 확장 후보는 다음과 같다.

```text
FOUL
FOUL_DRAWN
OFFSIDE
TURNOVER
RECOVERY
KEY_PASS
CROSS
SUCCESSFUL_CROSS
```

---

## 12. 클립 출처 타입 Enum

신규 Enum을 추가한다.

```text
PlayerRecordClipSourceTypeEnum
```

값은 다음과 같다.

```text
TEAM_ANALYSIS
PLAYER_ANALYSIS
```

---

## 13. API 설계 방향

## 13.1 기존 선수 기록 API 유지

기존 선수 기록 요약 API는 유지한다.

```http
POST   /api/management/player-records
GET    /api/management/player-records
GET    /api/management/player-records/{recordId}
PATCH  /api/management/player-records/{recordId}
DELETE /api/management/player-records/{recordId}

GET    /api/player/me/player-records
GET    /api/player/me/player-records/{recordId}
```

단, `/match-videos` 기반 분석 작업에서는 직접 `POST /api/management/player-records`를 호출하는 대신, 기록 이벤트 생성 API에서 필요한 경우 `player_record`를 자동 생성할 수 있게 한다.

## 13.2 신규 기록 이벤트만 등록 API

클립 없이 기록 이벤트만 등록하는 API를 추가한다.

```http
POST /api/management/player-record-events
```

요청 예시는 다음과 같다.

```json
{
  "uploadId": 1,
  "playerId": 5,
  "eventType": "TACKLE",
  "eventTimeSec": 302,
  "value": 1,
  "eventMemo": "중원에서 압박 후 볼 탈취"
}
```

처리 흐름은 다음과 같다.

1. 로그인 사용자 권한 검증
2. 경기 영상 존재 여부 검증
3. 기록 대상 선수가 `PLAYER`인지 검증
4. 해당 경기와 선수의 `player_record` 조회
5. 없으면 기본값 0으로 `player_record` 자동 생성
6. 이벤트 타입 검증
7. 이벤트 시간 검증
8. 이벤트 값 검증
9. `player_record_event` 저장
10. `player_record` 요약 수치 갱신
11. 응답 반환

## 13.3 신규 기록 이벤트 + 클립 연결 API

클립 연결은 이벤트 생성과 함께 처리할 수 있어야 한다.

초기 MVP에서는 이벤트 생성과 클립 연결을 한 번에 처리하는 API를 우선 구현한다.

```http
POST /api/management/player-record-events/with-clip-link
```

팀 분석 클립 연결 요청 예시는 다음과 같다.

```json
{
  "uploadId": 1,
  "playerId": 5,
  "eventType": "SHOT",
  "eventTimeSec": 431,
  "value": 1,
  "eventMemo": "박스 바깥 오른발 슈팅",
  "clipSourceType": "TEAM_ANALYSIS",
  "teamClipId": 12,
  "playerClipId": null
}
```

선수 개인 분석 클립 연결 요청 예시는 다음과 같다.

```json
{
  "uploadId": 1,
  "playerId": 5,
  "eventType": "GOAL",
  "eventTimeSec": 752,
  "value": 1,
  "eventMemo": "침투 후 마무리",
  "clipSourceType": "PLAYER_ANALYSIS",
  "teamClipId": null,
  "playerClipId": 22
}
```

처리 흐름은 다음과 같다.

1. 로그인 사용자 권한 검증
2. 경기 영상 존재 여부 검증
3. 기록 대상 선수가 `PLAYER`인지 검증
4. 해당 경기와 선수의 `player_record` 조회
5. 없으면 기본값 0으로 `player_record` 자동 생성
6. 이벤트 타입 검증
7. 이벤트 시간 검증
8. 이벤트 값 검증
9. 클립 출처 타입 검증
10. 팀 분석 클립 또는 선수 개인 분석 클립 존재 여부 검증
11. 클립이 해당 경기 영상에서 생성된 것인지 검증
12. 선수 개인 분석 클립이면 대상 선수가 기록 대상 선수와 일치하는지 검증
13. `player_record_event` 저장
14. `player_record_event_clip` 저장
15. `player_record` 요약 수치 갱신
16. 응답 반환

## 13.4 신규 기록 이벤트 조회 API

관리용 API는 `COACH`, `ANALYST`만 호출할 수 있다.

```http
GET    /api/management/player-records/{recordId}/events
GET    /api/management/player-record-events/{eventId}
PATCH  /api/management/player-record-events/{eventId}
DELETE /api/management/player-record-events/{eventId}
```

역할은 다음과 같다.

```text
GET /player-records/{recordId}/events
→ 특정 선수 경기 기록의 이벤트 목록 조회

GET /player-record-events/{eventId}
→ 이벤트 상세 조회

PATCH
→ 이벤트 타입, 시간, 값, 메모 수정
→ 요약 수치 보정

DELETE
→ 이벤트 소프트 삭제
→ 연결된 player_record_event_clip도 소프트 삭제
→ player_record 요약 수치 차감
```

## 13.5 선수 본인 기록 이벤트 조회 API

선수 본인 API는 `PLAYER`가 본인 기록만 조회할 수 있게 한다.

```http
GET /api/player/me/player-records/{recordId}/events
GET /api/player/me/player-record-events/{eventId}
```

검증 기준은 다음과 같다.

```text
로그인한 memberId == player_record.player_id
```

선수가 다른 선수의 `recordId`, `eventId`로 직접 호출하면 실패해야 한다.

---

## 14. 응답 DTO 방향

이벤트 생성 응답은 다음 정보를 포함한다.

```json
{
  "eventId": 1,
  "recordId": 10,
  "clipLinkId": 3,
  "message": "선수 기록 이벤트가 등록되었습니다."
}
```

클립 없이 이벤트만 등록한 경우 `clipLinkId`는 `null`이다.

이벤트 목록 응답은 다음 정보를 포함한다.

```json
{
  "events": [
    {
      "eventId": 1,
      "recordId": 10,
      "eventType": "SHOT",
      "eventTimeSec": 431,
      "value": 1,
      "eventMemo": "박스 바깥 오른발 슈팅",
      "clipLinks": [
        {
          "clipLinkId": 3,
          "clipSourceType": "TEAM_ANALYSIS",
          "teamClipId": 12,
          "playerClipId": null,
          "clipTitle": "전방 압박 후 슈팅",
          "clipUrl": "/uploads/team-analysis-clips/team-clip-12.mp4"
        }
      ],
      "createdBy": 2,
      "createdByName": "분석관",
      "createdAt": "2026-07-08T10:00:00",
      "updatedAt": "2026-07-08T10:00:00"
    }
  ]
}
```

클립이 없는 이벤트는 `clipLinks`가 빈 배열이다.

```json
{
  "eventId": 2,
  "recordId": 10,
  "eventType": "TACKLE",
  "eventTimeSec": 302,
  "value": 1,
  "eventMemo": "중원 압박 성공",
  "clipLinks": []
}
```

---

## 15. 선수 기록 요약 수치 갱신 정책

기록 이벤트가 생성되면 `player_record`의 요약 수치를 갱신한다.

초기 기본 정책은 다음과 같다.

| eventType | 갱신 컬럼 |
|---|---|
| `GOAL` | `goals` |
| `ASSIST` | `assists` |
| `SHOT` | `shots` |
| `SHOT_ON_TARGET` | `shots`, `shots_on_target` |
| `PASS` | `passes` |
| `SUCCESSFUL_PASS` | `passes`, `successful_passes` |
| `DRIBBLE` | `dribbles` |
| `SUCCESSFUL_DRIBBLE` | `dribbles`, `successful_dribbles` |
| `TACKLE` | `tackles` |
| `INTERCEPTION` | `interceptions` |
| `CLEARANCE` | `clearances` |
| `SAVE` | `saves` |
| `YELLOW_CARD` | `yellow_cards` |
| `RED_CARD` | `red_cards` |
| `ETC` | 요약 수치 갱신 없음 |

주의사항:

- `GOAL` 등록 시 `shots`, `shots_on_target`까지 자동 증가할지는 초기 MVP에서는 자동 처리하지 않는다.
- 중복 집계를 피하기 위해 자동 연쇄 증가 규칙은 최소화한다.
- 사용자가 명확히 `SHOT_ON_TARGET`을 등록한 경우에만 `shots`, `shots_on_target`을 함께 증가시킨다.
- 수정/삭제 시 기존 이벤트 타입과 신규 이벤트 타입을 비교해 요약 수치를 보정해야 한다.
- 요약 수치가 음수가 되면 안 된다.

---

## 16. 예외 상황

필요한 예외 코드는 다음 방향으로 추가한다.

```text
PLAYER_RECORD_NOT_FOUND
PLAYER_RECORD_EVENT_NOT_FOUND
PLAYER_RECORD_EVENT_CLIP_NOT_FOUND
INVALID_PLAYER_RECORD_EVENT_TYPE
INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE
INVALID_PLAYER_RECORD_EVENT_TIME
INVALID_PLAYER_RECORD_EVENT_VALUE
PLAYER_RECORD_CLIP_NOT_FOUND
PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH
PLAYER_RECORD_EVENT_PLAYER_MISMATCH
PLAYER_RECORD_EVENT_ACCESS_DENIED
```

예외 처리 기준은 다음과 같다.

- 존재하지 않는 경기 영상이면 실패
- 존재하지 않는 선수면 실패
- 기록 대상이 `PLAYER`가 아니면 실패
- 존재하지 않는 클립 연결 실패
- 삭제된 클립 연결 실패
- 클립과 기록의 경기 영상이 다르면 실패
- 선수 개인 분석 클립의 대상 선수와 기록 대상 선수가 다르면 실패
- 선수는 본인 기록 이벤트만 조회 가능
- 삭제된 이벤트는 목록/상세에서 제외
- 삭제된 이벤트 연결은 조회에서 제외

---

## 17. 프론트 설계 방향

이번 기능의 프론트 구현은 백엔드 작업 이후 진행한다.

프론트 핵심 변경 대상은 다음이다.

```text
frontend/src/pages/MatchVideoPage.tsx
```

`MatchVideoPage`에 다음 기능을 추가한다.

- 경기 상세 영상 하단 또는 우측에 `분석 작업 시작` 버튼 추가
- 클립 생성 모달 또는 패널 추가
- 팀 분석 클립 / 선수 개인 분석 클립 선택
- 시작/종료 시간 설정
- 드로잉 작성
- 클립 저장 정보 입력
- 클립 생성 API 호출
- 클립 생성 성공 후 선수 기록 이벤트 연결 창 표시
- 기록 대상 선수 선택
- 기록 이벤트 타입 선택
- 이벤트 시간 설정
- 이벤트 메모 입력
- 선수 기록 이벤트 + 클립 연결 API 호출
- 영상 하단에 클립 없이 선수 기록 이벤트만 등록하는 빠른 기록 폼 추가

선수 기록 조회 화면은 다음 경로를 사용한다.

```text
/player-records
```

`/player-records`의 역할은 다음과 같다.

- 선수 본인 경기별 기록 목록 조회
- 기록 상세 조회
- 기록 이벤트 목록 조회
- 기록 이벤트별 연결 클립 조회
- 기록 타입 클릭 시 연결 클립 재생
- 클립 없는 기록 이벤트는 `클립 없음`으로 표시

COACH/ANALYST의 선수 기록 직접 등록 화면은 이번 방향에서는 우선순위에서 제외한다.

---

## 18. 구현 순서

이번 기능은 백엔드 작업부터 진행한다.

순서는 다음과 같다.

1. 요구사항 문서 수정
2. DB 테이블 설계 확정
3. `soccer_platform.sql`에 `player_record_event`, `player_record_event_clip` DDL 추가
4. `PlayerRecordEventTypeEnum` 추가
5. `PlayerRecordClipSourceTypeEnum` 추가
6. `PlayerRecordEventEntity` 추가
7. `PlayerRecordEventClipEntity` 추가
8. `PlayerRecordEventRepository` 추가
9. `PlayerRecordEventClipRepository` 추가
10. `PlayerRecordEventValidator` 추가
11. `PlayerRecordEventService` 추가
12. 기록 이벤트 생성 DTO 추가
13. 기록 이벤트 + 클립 연결 생성 DTO 추가
14. 기록 이벤트 응답 DTO 추가
15. 기록 이벤트 목록 응답 DTO 추가
16. `PlayerRecordEventController` 추가
17. `ErrorCode`에 필요한 예외 코드 추가
18. `SecurityConfig`에서 신규 API 접근 정책 확인
19. 백엔드 API 테스트
20. 테스트 완료 후 프론트 작업으로 이동

---

## 19. 백엔드 테스트 기준

COACH 계정에서 확인할 항목은 다음과 같다.

- 클립 없이 선수 기록 이벤트 생성
- 팀 분석 클립과 연결된 선수 기록 이벤트 생성
- 선수 개인 분석 클립과 연결된 선수 기록 이벤트 생성
- 기존 `player_record`가 없을 때 자동 생성
- 기존 `player_record`가 있을 때 기존 기록 사용
- 이벤트 생성 시 요약 수치 증가
- 이벤트 목록 조회
- 이벤트 상세 조회
- 이벤트 수정
- 이벤트 삭제
- 삭제 시 요약 수치 보정

ANALYST 계정에서 확인할 항목은 다음과 같다.

- COACH와 동일하게 기록 이벤트 생성 가능
- 기록 이벤트 수정 가능
- 기록 이벤트 삭제 가능
- 클립 연결 생성 가능
- 클립 없는 기록 이벤트 생성 가능

PLAYER 계정에서 확인할 항목은 다음과 같다.

- 본인 기록 이벤트 목록 조회 가능
- 본인 기록 이벤트 상세 조회 가능
- 본인 기록 이벤트에 연결된 클립 정보 조회 가능
- 클립 없는 기록 이벤트 조회 가능
- 다른 선수 기록 이벤트 조회 실패
- 관리 API 접근 실패

검증 실패 테스트는 다음과 같다.

- 존재하지 않는 경기 영상으로 이벤트 생성 실패
- 존재하지 않는 선수로 이벤트 생성 실패
- `PLAYER`가 아닌 회원을 기록 대상 선수로 선택하면 실패
- 존재하지 않는 클립 연결 실패
- 클립과 기록의 경기 영상이 다르면 실패
- 선수 개인 분석 클립 대상 선수와 기록 대상 선수가 다르면 실패
- 잘못된 이벤트 타입 실패
- 잘못된 클립 출처 타입 실패
- 음수 이벤트 시간 실패
- 음수 value 실패

---

## 20. 삭제 정책

삭제는 실제 삭제가 아니라 소프트 삭제를 사용한다.

대상은 다음과 같다.

```text
player_record
player_record_event
player_record_event_clip
```

삭제 정책은 다음과 같다.

- 기록 이벤트 삭제 시 `player_record_event.is_deleted = true`
- 연결 정보도 `player_record_event_clip.is_deleted = true`
- 이벤트 삭제 시 `player_record` 요약 수치를 차감한다.
- 요약 수치가 음수가 되지 않게 검증한다.
- `player_record` 자체 삭제는 기존 정책대로 `is_deleted = true` 처리한다.
- `player_record` 삭제 시 하위 이벤트와 연결 정보를 함께 소프트 삭제할지 여부는 백엔드 구현 시 정책을 확정한다.

초기 추천은 다음과 같다.

```text
player_record 삭제
→ 하위 player_record_event 전체 소프트 삭제
→ 하위 player_record_event_clip 전체 소프트 삭제
```

---

## 21. 추후 확장 가능성

추후 확장 가능한 기능은 다음과 같다.

- 기록 이벤트별 클립 다중 연결
- 한 클립을 여러 선수 기록 이벤트에 연결
- 경기 타임라인 기반 이벤트 표시
- 선수별 기록 이벤트 히트맵
- 선수별 시즌 누적 기록
- 최근 N경기 평균 기록
- 기록 타입별 클립 모아보기
- AI 이벤트 분석 결과를 `player_record_event`로 자동 저장
- AI 하이라이트와 기록 이벤트 자동 연결
- 기록 이벤트 수정 이력 관리
- CSV 기록 업로드와 이벤트 연결
- 코치 코멘트와 선수 피드백 확인 여부 연결

---

## 22. 주의사항

- `player_record_event`에는 `upload_id`, `player_id`를 저장하지 않는다.
- `upload_id`, `player_id`는 `player_record_id`를 통해 `player_record`에서 확인한다.
- `player_record_event.memo`는 이벤트 단위 메모이므로 `player_record.memo`와 별도로 유지한다.
- DTO와 프론트에서는 이벤트 단위 메모를 `eventMemo`로 표현할 수 있다.
- 같은 경기와 같은 선수의 `player_record`는 1개만 유지한다.
- 기록 이벤트가 추가될 때마다 기존 `player_record` 요약 수치를 갱신한다.
- `player_record_event`는 기록 이벤트가 등록될 때마다 새로 저장한다.
- `player_record_event_clip`은 선택 저장이다.
- 클립과 연결된 기록도 가능하고, 클립 없이 기록만 저장하는 것도 가능하다.
- 팀 분석 클립도 선수 기록 이벤트와 연결할 수 있어야 한다.
- 선수 개인 분석 클립은 기록 대상 선수와 클립 대상 선수가 일치해야 한다.
- 팀 분석 클립은 팀 전체 클립이므로 기록 대상 선수를 별도로 선택해 이벤트와 연결한다.
- 클립 생성 API와 기록 이벤트 연결 API를 처음부터 완전히 하나로 합치지 않는다.
- 초기에는 클립 생성 후 응답받은 clip ID로 기록 이벤트 연결 API를 호출하는 단계형 구조가 안전하다.
- 클립 없이 기록만 등록하는 빠른 기록 API도 반드시 필요하다.
- 프론트 통합 작업은 백엔드 이벤트/연결 API가 완료된 뒤 진행한다.
- 기존 `/team-analysis-clips/new`, `/player-analysis-clips/new`는 즉시 제거하지 않고, 신규 등록의 주 흐름만 `/match-videos`로 이동한다.