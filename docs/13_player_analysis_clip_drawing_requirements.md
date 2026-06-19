# 선수 개인 분석 클립 드로잉 기능 요구사항

## 1. 결론

선수 개인 분석 클립 드로잉 기능은 지도자와 분석관이 선수 개인 분석 클립 위에 전술 설명용 드로잉 데이터를 저장하고, 해당 개인 클립의 대상 선수 본인만 드로잉을 조회할 수 있게 하는 기능이다.

초기 구현은 다음 정책을 기준으로 진행한다.

* 인증은 기존 JWT 인증 구조를 사용한다.
* JWT 인증 후 `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 값을 사용한다.
* 드로잉 데이터는 `player_video_clip_drawing` 테이블에 저장한다.
* 드로잉은 프론트 캔버스에서 생성한 좌표, 텍스트, 도형 데이터를 JSON으로 저장한다.
* 지도자 `COACH`는 선수 개인 분석 클립 드로잉 등록, 조회, 수정, 삭제가 가능하다.
* 분석관 `ANALYST`는 선수 개인 분석 클립 드로잉 등록, 조회, 수정이 가능하고 삭제는 불가능하다.
* 선수 `PLAYER`는 본인에게 지정된 선수 개인 분석 클립 드로잉만 조회할 수 있다.
* 선수 `PLAYER`는 다른 선수의 개인 분석 클립 드로잉을 조회할 수 없다.
* 선수 `PLAYER`는 드로잉 등록, 수정, 삭제가 불가능하다.
* 삭제는 실제 DB 삭제가 아니라 `is_deleted = true`로 처리한다.
* 드로잉 생성 시 `playerClipId`가 존재하고 삭제되지 않았는지 반드시 백엔드에서 검증한다.
* 선수 조회 시 `player_video_clip.player_id`와 로그인 선수의 `memberId`가 일치하는지 반드시 검증한다.
* 드로잉의 `startTimeSec`, `endTimeSec`는 연결된 선수 개인 분석 클립의 `startTimeSec`, `endTimeSec` 범위 안에 있어야 한다.
* 현재 단계에서는 프론트를 구현하지 않고 백엔드 API 기능만 개발한다.

---

## 2. 기능 목적

선수 개인 분석 클립 드로잉 기능의 목적은 특정 선수에게 공유되는 개인 분석 클립의 특정 시간대에 전술적 의미를 시각적으로 표시하는 것이다.

예를 들어 다음과 같은 개인 피드백을 표현할 수 있다.

* 수비 위치 조정 표시
* 압박 시작 타이밍 화살표
* 패스 선택지 표시
* 오프 더 볼 움직임 방향 표시
* 특정 공간 침투 경로 표시
* 개인 실수 장면의 원인 텍스트 설명

이 기능은 단순히 개인 분석 영상을 보여주는 기능이 아니라, 지도자와 분석관이 특정 선수에게 “이 장면에서 무엇을 봐야 하는지”를 명확히 전달하기 위한 핵심 피드백 기능이다.

팀 분석 클립 드로잉과 구조는 유사하지만, 선수 개인 분석 클립 드로잉은 접근 제어가 더 중요하다.

특히 선수는 본인에게 지정된 개인 클립의 드로잉만 조회할 수 있어야 하며, 같은 팀의 다른 선수 개인 드로잉은 API 주소를 직접 호출해도 조회할 수 없어야 한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 드로잉 등록
* 선수 개인 분석 클립 드로잉 목록 조회
* 선수 개인 분석 클립 드로잉 상세 조회
* 선수 개인 분석 클립 드로잉 수정
* 선수 개인 분석 클립 드로잉 삭제

지도자는 모든 선수 개인 분석 클립과 그 드로잉을 관리할 수 있다.

### 3.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 드로잉 등록
* 선수 개인 분석 클립 드로잉 목록 조회
* 선수 개인 분석 클립 드로잉 상세 조회
* 선수 개인 분석 클립 드로잉 수정

불가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 드로잉 삭제

분석관에게 삭제 권한을 주지 않는 이유는 드로잉 데이터가 특정 선수에게 전달되는 개인 피드백 자료이고, 실수로 삭제될 경우 피드백 의도가 사라질 수 있기 때문이다.

### 3.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

* 본인에게 지정된 선수 개인 분석 클립 드로잉 목록 조회
* 본인에게 지정된 선수 개인 분석 클립 드로잉 상세 조회

불가능한 기능은 다음과 같다.

* 다른 선수의 개인 분석 클립 드로잉 조회
* 선수 개인 분석 클립 드로잉 등록
* 선수 개인 분석 클립 드로잉 수정
* 선수 개인 분석 클립 드로잉 삭제

선수 조회 권한은 단순히 `PLAYER` 역할만으로 허용하지 않고, 반드시 연결된 `player_video_clip.player_id`와 로그인 회원 ID가 일치하는지 확인해야 한다.

---

## 4. 권한 정책

| 역할 | 등록 | 목록 조회 | 상세 조회 | 수정 | 삭제 |
|---|---:|---:|---:|---:|---:|
| `COACH` | 가능 | 가능 | 가능 | 가능 | 가능 |
| `ANALYST` | 가능 | 가능 | 가능 | 가능 | 불가 |
| `PLAYER` | 불가 | 본인 클립만 가능 | 본인 클립만 가능 | 불가 | 불가 |

권한 검증은 반드시 백엔드에서 처리한다.

검증 기준은 다음과 같다.

* 인증되지 않은 사용자는 모든 드로잉 API에 접근할 수 없다.
* `COACH`, `ANALYST`만 드로잉을 등록할 수 있다.
* `COACH`, `ANALYST`는 모든 선수 개인 분석 클립 드로잉을 조회할 수 있다.
* `PLAYER`는 본인에게 지정된 선수 개인 분석 클립 드로잉만 조회할 수 있다.
* `PLAYER`가 다른 선수의 개인 분석 클립 드로잉 목록 또는 상세 API를 호출하면 실패해야 한다.
* `COACH`, `ANALYST`만 드로잉을 수정할 수 있다.
* `COACH`만 드로잉을 삭제할 수 있다.
* `PLAYER`는 등록, 수정, 삭제 API를 직접 호출해도 실패해야 한다.
* `ANALYST`는 삭제 API를 직접 호출해도 실패해야 한다.
* `isAdmin = true`만으로 드로잉 등록, 수정, 삭제 권한을 주지 않는다.

---

## 5. 화면 흐름

현재 단계에서는 프론트를 구현하지 않는다.

다만 백엔드 API 응답은 추후 프론트 캔버스 구현을 고려해서 설계한다.

### 5.1 지도자/분석관 드로잉 생성 흐름

1. 지도자 또는 분석관이 선수 개인 분석 클립 상세 화면에 진입한다.
2. 특정 시간대에서 영상을 멈춘다.
3. 캔버스 위에 선, 화살표, 원, 박스, 영역, 텍스트를 추가한다.
4. 프론트는 드로잉 데이터를 JSON으로 만든다.
5. 서버에 `playerClipId`, `drawingType`, `startTimeSec`, `endTimeSec`, `drawingData`를 전송한다.
6. 서버는 선수 개인 분석 클립 존재 여부와 삭제 여부를 검증한다.
7. 서버는 드로잉 시간이 선수 개인 분석 클립 시간 범위 안에 있는지 검증한다.
8. 검증 통과 시 드로잉을 저장한다.

### 5.2 선수 드로잉 조회 흐름

1. 선수가 본인 개인 분석 클립 상세 화면에 진입한다.
2. 클립의 드로잉 목록을 조회한다.
3. 서버는 해당 클립의 `player_id`와 로그인 선수의 `memberId`가 일치하는지 검증한다.
4. 권한이 있으면 드로잉 목록을 반환한다.
5. 영상 재생 시간이 드로잉의 `startTimeSec`, `endTimeSec` 범위 안에 들어오면 프론트에서 해당 드로잉을 캔버스에 표시한다.

### 5.3 다른 선수 드로잉 접근 차단 흐름

1. 선수가 다른 선수의 `playerClipId` 또는 `drawingId`를 알고 API를 직접 호출한다.
2. 서버는 연결된 선수 개인 분석 클립의 `player_id`를 확인한다.
3. `player_id`와 로그인 선수의 `memberId`가 다르면 접근을 차단한다.
4. 응답은 `403 Forbidden`으로 처리한다.

---

## 6. API 흐름

### 6.1 API 설계 방향

드로잉 조회 API는 지도자, 분석관, 선수가 모두 사용하므로 공통 API로 둔다.

단, 선수는 본인에게 지정된 선수 개인 분석 클립의 드로잉만 조회할 수 있게 서비스 계층에서 검증한다.

드로잉 등록/수정 API는 지도자와 분석관이 사용할 수 있으므로 관리 API로 둔다.

드로잉 삭제 API는 지도자만 가능하므로 지도자 전용 API로 분리한다.

권장 API 초안은 다음과 같다.

```http
GET    /api/player-analysis-clips/{playerClipId}/drawings
GET    /api/player-analysis-clip-drawings/{drawingId}
POST   /api/management/player-analysis-clips/{playerClipId}/drawings
PATCH  /api/management/player-analysis-clip-drawings/{drawingId}
DELETE /api/coach/player-analysis-clip-drawings/{drawingId}
```

### 6.2 드로잉 목록 조회

```http
GET /api/player-analysis-clips/{playerClipId}/drawings
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`
* `PLAYER` 단, 본인 클립만 가능

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH`, `ANALYST`, `PLAYER` 중 하나인지 확인한다.
3. `playerClipId`로 선수 개인 분석 클립을 조회한다.
4. 선수 개인 분석 클립이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 로그인 회원이 `PLAYER`이면 `player_video_clip.player_id`와 로그인 회원 ID가 일치하는지 확인한다.
6. 일치하지 않으면 접근을 차단한다.
7. `is_deleted = false`인 드로잉만 조회한다.
8. `start_time_sec ASC`, `id ASC` 기준으로 정렬한다.
9. 드로잉 목록을 반환한다.

응답 예시는 다음과 같다.

```json
{
  "drawings": [
    {
      "drawingId": 1,
      "playerClipId": 3,
      "drawingType": "ARROW",
      "startTimeSec": 755,
      "endTimeSec": 760,
      "drawingData": {
        "fromX": 0.35,
        "fromY": 0.42,
        "toX": 0.62,
        "toY": 0.38,
        "color": "#ff0000",
        "lineWidth": 4
      },
      "writerId": 2,
      "writerName": "김지도",
      "createdAt": "2026-06-20T19:10:00",
      "updatedAt": "2026-06-20T19:10:00"
    }
  ]
}
```

### 6.3 드로잉 상세 조회

```http
GET /api/player-analysis-clip-drawings/{drawingId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`
* `PLAYER` 단, 본인 클립의 드로잉만 가능

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 드로잉 ID로 드로잉을 조회한다.
3. 드로잉이 존재하지 않거나 삭제된 경우 실패 처리한다.
4. 연결된 선수 개인 분석 클립이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 로그인 회원이 `PLAYER`이면 연결된 선수 개인 분석 클립의 `player_id`와 로그인 회원 ID가 일치하는지 확인한다.
6. 일치하지 않으면 접근을 차단한다.
7. 드로잉 상세 정보를 반환한다.

### 6.4 드로잉 등록

```http
POST /api/management/player-analysis-clips/{playerClipId}/drawings
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 예시는 다음과 같다.

```json
{
  "drawingType": "TEXT",
  "startTimeSec": 755,
  "endTimeSec": 760,
  "drawingData": {
    "x": 0.35,
    "y": 0.22,
    "text": "여기서 몸 방향을 먼저 열어야 함",
    "color": "#ffffff",
    "fontSize": 18
  }
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. `playerClipId`로 선수 개인 분석 클립을 조회한다.
4. 선수 개인 분석 클립이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. `drawingType`이 허용된 Enum 값인지 확인한다.
6. `startTimeSec`, `endTimeSec`가 0 이상인지 확인한다.
7. `startTimeSec < endTimeSec`인지 확인한다.
8. 드로잉 시간이 선수 개인 분석 클립 시간 범위 안에 있는지 확인한다.
9. `drawingData`가 비어 있지 않은지 확인한다.
10. `member_id`에 드로잉을 등록한 회원 ID를 저장한다.
11. 드로잉을 저장한다.
12. 생성된 드로잉 ID를 반환한다.

응답 예시는 다음과 같다.

```json
{
  "drawingId": 1,
  "message": "선수 개인 분석 클립 드로잉이 등록되었습니다."
}
```

### 6.5 드로잉 수정

```http
PATCH /api/management/player-analysis-clip-drawings/{drawingId}
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

초기 구현에서는 전체 수정 방식에 가깝게 처리한다.

요청 예시는 다음과 같다.

```json
{
  "drawingType": "ARROW",
  "startTimeSec": 756,
  "endTimeSec": 762,
  "drawingData": {
    "fromX": 0.2,
    "fromY": 0.4,
    "toX": 0.6,
    "toY": 0.3,
    "color": "#ff0000",
    "lineWidth": 4
  }
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. 드로잉 ID로 기존 드로잉을 조회한다.
4. 드로잉이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 연결된 선수 개인 분석 클립이 존재하지 않거나 삭제된 경우 실패 처리한다.
6. 요청 값을 검증한다.
7. 드로잉 시간이 선수 개인 분석 클립 시간 범위 안에 있는지 확인한다.
8. 드로잉 정보를 수정한다.
9. 수정된 드로잉 정보를 반환한다.

### 6.6 드로잉 삭제

```http
DELETE /api/coach/player-analysis-clip-drawings/{drawingId}
```

사용 가능 역할은 다음과 같다.

* `COACH`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH`인지 확인한다.
3. 드로잉 ID로 드로잉을 조회한다.
4. 드로잉이 존재하지 않거나 이미 삭제된 경우 실패 처리한다.
5. `is_deleted = true`로 변경한다.
6. 삭제 완료 응답을 반환한다.

---

## 7. DB 설계 방향

초기 구현에서는 현재 `player_video_clip_drawing` 테이블을 그대로 사용한다.

| 컬럼 | 사용 목적 |
|---|---|
| `id` | 드로잉 PK |
| `player_video_clip_id` | 선수 개인 분석 클립 ID |
| `member_id` | 드로잉 작성 회원 ID |
| `drawing_type` | 드로잉 유형 |
| `start_time_sec` | 원본 영상 기준 드로잉 표시 시작 시간 |
| `end_time_sec` | 원본 영상 기준 드로잉 표시 종료 시간 |
| `drawing_data` | 좌표, 색상, 두께, 텍스트 등 JSON 데이터 |
| `is_deleted` | 소프트 삭제 여부 |
| `created_at` | 생성일시 |
| `updated_at` | 수정일시 |

Entity 매핑 방향은 다음과 같다.

| DB 컬럼 | Entity 필드명 |
|---|---|
| `id` | `id` |
| `player_video_clip_id` | `playerVideoClip` |
| `member_id` | `member` |
| `drawing_type` | `drawingType` |
| `start_time_sec` | `startTimeSec` |
| `end_time_sec` | `endTimeSec` |
| `drawing_data` | `drawingData` |
| `is_deleted` | `isDeleted` |
| `created_at` | `createdAt` |
| `updated_at` | `updatedAt` |

선수 본인 접근 제어를 위해 연결된 `player_video_clip`의 다음 컬럼을 함께 사용한다.

| 컬럼 | 사용 목적 |
|---|---|
| `player_video_clip.id` | 선수 개인 분석 클립 PK |
| `player_video_clip.player_id` | 해당 개인 클립의 대상 선수 회원 ID |
| `player_video_clip.start_time_sec` | 개인 클립 시작 시간 |
| `player_video_clip.end_time_sec` | 개인 클립 종료 시간 |
| `player_video_clip.is_deleted` | 개인 클립 삭제 여부 |

---

## 8. 드로잉 타입

초기 허용 값은 다음과 같다.

| 값 | 의미 |
|---|---|
| `LINE` | 선 |
| `ARROW` | 화살표 |
| `CIRCLE` | 원 |
| `BOX` | 박스 |
| `AREA` | 영역 |
| `TEXT` | 텍스트 |

드로잉 타입은 Enum으로 관리한다.

팀 분석 클립 드로잉에서 이미 `DrawingTypeEnum`을 만들었다면 새 Enum을 만들지 않고 공통으로 재사용한다.

---

## 9. 요청/응답 DTO 방향

### 9.1 등록 요청 DTO

`CreatePlayerAnalysisClipDrawingRequestDTO`

필드 방향은 다음과 같다.

* `drawingType`
* `startTimeSec`
* `endTimeSec`
* `drawingData`

### 9.2 수정 요청 DTO

`UpdatePlayerAnalysisClipDrawingRequestDTO`

필드 방향은 다음과 같다.

* `drawingType`
* `startTimeSec`
* `endTimeSec`
* `drawingData`

### 9.3 목록/상세 응답 DTO

`PlayerAnalysisClipDrawingResponseDTO`

필드 방향은 다음과 같다.

* `drawingId`
* `playerClipId`
* `drawingType`
* `startTimeSec`
* `endTimeSec`
* `drawingData`
* `writerId`
* `writerName`
* `createdAt`
* `updatedAt`

### 9.4 생성 응답 DTO

`CreatePlayerAnalysisClipDrawingResponseDTO`

필드 방향은 다음과 같다.

* `drawingId`
* `message`

---

## 10. 예외 상황

| 상황 | 처리 방식 | 권장 HTTP 상태 |
|---|---|---|
| JWT 없음 | 인증 실패 | `401 Unauthorized` |
| JWT 만료 | 인증 실패 | `401 Unauthorized` |
| 선수의 등록 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 수정 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 삭제 API 호출 | 접근 차단 | `403 Forbidden` |
| 분석관의 삭제 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수가 다른 선수 개인 클립 드로잉 목록 조회 | 접근 차단 | `403 Forbidden` |
| 선수가 다른 선수 개인 클립 드로잉 상세 조회 | 접근 차단 | `403 Forbidden` |
| 존재하지 않는 선수 개인 분석 클립 ID | 등록/조회 실패 | `404 Not Found` |
| 삭제된 선수 개인 분석 클립 ID | 등록/조회 실패 | `404 Not Found` |
| 존재하지 않는 드로잉 ID | 조회/수정/삭제 실패 | `404 Not Found` |
| 삭제된 드로잉 ID | 조회/수정/삭제 실패 | `404 Not Found` |
| 허용되지 않은 드로잉 타입 | 요청 실패 | `400 Bad Request` |
| 시작 시간이 음수 | 요청 실패 | `400 Bad Request` |
| 종료 시간이 음수 | 요청 실패 | `400 Bad Request` |
| 시작 시간이 종료 시간보다 같거나 큼 | 요청 실패 | `400 Bad Request` |
| 드로잉 시간이 선수 개인 분석 클립 범위를 벗어남 | 요청 실패 | `400 Bad Request` |
| 드로잉 JSON이 비어 있음 | 요청 실패 | `400 Bad Request` |
| 드로잉 JSON 크기가 너무 큼 | 요청 실패 | `400 Bad Request` |

---

## 11. 구현 순서

### 11.1 백엔드 1단계: 기본 구조 확인

1. `PlayerVideoClipDrawingEntity` 필드와 DB 컬럼 매핑 확인
2. `PlayerVideoClipEntity`와 `PlayerVideoClipDrawingEntity` 연관관계 확인
3. `MemberEntity`와 `PlayerVideoClipDrawingEntity` 연관관계 확인
4. 기존 `PlayerVideoClipRepository` 확인
5. 기존 공통 예외 구조와 `ErrorCode` 확인
6. 팀 분석 클립 드로잉 기능에서 만든 구조 중 재사용 가능한 코드 확인

### 11.2 백엔드 2단계: Enum 확인

1. `DrawingTypeEnum` 확인
2. 허용 값은 `LINE`, `ARROW`, `CIRCLE`, `BOX`, `AREA`, `TEXT`로 제한
3. 팀 분석 클립 드로잉에서 이미 사용 중이면 공통 Enum으로 재사용

### 11.3 백엔드 3단계: DTO 작성

1. `CreatePlayerAnalysisClipDrawingRequestDTO` 작성
2. `UpdatePlayerAnalysisClipDrawingRequestDTO` 작성
3. `CreatePlayerAnalysisClipDrawingResponseDTO` 작성
4. `PlayerAnalysisClipDrawingResponseDTO` 작성

### 11.4 백엔드 4단계: Repository 작성

1. `PlayerVideoClipDrawingRepository` 작성
2. `findByPlayerVideoClipAndIsDeletedFalseOrderByStartTimeSecAscIdAsc()` 계열 메서드 작성
3. `findByIdAndIsDeletedFalse()` 계열 메서드 작성

### 11.5 백엔드 5단계: 권한 검증 로직 작성

1. 드로잉 등록/수정 권한 검증 메서드 작성
2. `COACH`, `ANALYST`가 아니면 등록/수정 실패 처리
3. 드로잉 삭제 권한 검증 메서드 작성
4. `COACH`가 아니면 삭제 실패 처리
5. 조회는 `COACH`, `ANALYST`, `PLAYER` 허용
6. `PLAYER` 조회 시 본인 클립 여부 검증 메서드 작성
7. `isAdmin`만으로 드로잉 변경 권한을 주지 않도록 처리

### 11.6 백엔드 6단계: 선수 개인 분석 클립 검증 로직 작성

1. `playerClipId`로 `player_video_clip` 조회
2. 존재하지 않으면 실패 처리
3. `isDeleted = true`이면 실패 처리
4. 드로잉 등록/목록 조회 시 반드시 이 검증 메서드 사용
5. 선수 조회 요청이면 `playerVideoClip.player.id`와 로그인 회원 ID가 같은지 확인

### 11.7 백엔드 7단계: 드로잉 시간 검증 로직 작성

1. `startTimeSec >= 0` 검증
2. `endTimeSec >= 0` 검증
3. `startTimeSec < endTimeSec` 검증
4. `drawing.startTimeSec >= playerClip.startTimeSec` 검증
5. `drawing.endTimeSec <= playerClip.endTimeSec` 검증

### 11.8 백엔드 8단계: Service 작성

1. 드로잉 등록 기능 작성
2. 드로잉 목록 조회 기능 작성
3. 드로잉 상세 조회 기능 작성
4. 드로잉 수정 기능 작성
5. 드로잉 삭제 기능 작성

### 11.9 백엔드 9단계: Controller 작성

1. 공통 조회 Controller 작성
2. 지도자/분석관 공통 관리 Controller 작성
3. 지도자 전용 삭제 Controller 작성
4. `@RequestParam`과 `@PathVariable`에는 이름을 명시한다.

### 11.10 백엔드 10단계: 테스트

1. 지도자 드로잉 등록 성공 테스트
2. 분석관 드로잉 등록 성공 테스트
3. 선수 드로잉 등록 실패 테스트
4. 존재하지 않는 `playerClipId`로 등록 실패 테스트
5. 삭제된 `playerClipId`로 등록 실패 테스트
6. `startTimeSec >= endTimeSec` 등록 실패 테스트
7. 드로잉 시간이 선수 개인 분석 클립 범위를 벗어나는 경우 실패 테스트
8. 지도자 드로잉 목록 조회 성공 테스트
9. 분석관 드로잉 목록 조회 성공 테스트
10. 대상 선수 본인 드로잉 목록 조회 성공 테스트
11. 다른 선수 드로잉 목록 조회 실패 테스트
12. 지도자 드로잉 상세 조회 성공 테스트
13. 분석관 드로잉 상세 조회 성공 테스트
14. 대상 선수 본인 드로잉 상세 조회 성공 테스트
15. 다른 선수 드로잉 상세 조회 실패 테스트
16. 지도자 드로잉 수정 성공 테스트
17. 분석관 드로잉 수정 성공 테스트
18. 선수 드로잉 수정 실패 테스트
19. 지도자 드로잉 삭제 성공 테스트
20. 분석관 드로잉 삭제 실패 테스트
21. 선수 드로잉 삭제 실패 테스트
22. 삭제된 드로잉 조회 제외 테스트

---

## 12. 추후 확장 가능성

초기 구현에서는 JSON 저장 기반 드로잉 CRUD와 선수 본인 접근 제어에 집중한다.

추후 확장 후보는 다음과 같다.

* 드로잉 레이어 순서 관리
* 드로잉 그룹 관리
* 드로잉 복제
* 드로잉 템플릿 저장
* 드로잉 작성자별 필터
* 드로잉 수정 이력
* 특정 선수 태그 연결
* 드로잉과 선수 피드백 확인 여부 연결
* 드로잉과 코멘트 연결
* 드로잉 표시 애니메이션
* 개인 피드백 확인 여부 통계
* AI 이벤트와 드로잉 연결

---

## 13. 주의사항

* 드로잉 등록, 수정, 삭제 권한은 프론트가 아니라 백엔드에서 반드시 검증한다.
* 분석관은 등록과 수정은 가능하지만 삭제는 불가능하다.
* 선수는 본인 개인 분석 클립 드로잉만 조회할 수 있다.
* 선수는 다른 선수 개인 분석 클립 드로잉을 조회할 수 없다.
* 선수의 본인 여부는 `player_video_clip.player_id`와 로그인 회원 ID로 검증한다.
* 삭제는 실제 삭제가 아니라 `is_deleted = true`로 처리한다.
* 조회 API는 삭제되지 않은 드로잉만 반환한다.
* 드로잉 생성 시 `playerClipId`가 존재하고 삭제되지 않았는지 반드시 검증한다.
* 드로잉 시간은 연결된 선수 개인 분석 클립 시간 범위를 벗어나면 안 된다.
* `drawingData`는 JSON으로 저장하되, 운영 전에는 요청 크기 제한을 반드시 검토해야 한다.
* JSON 구조는 프론트 캔버스 구현과 맞춰야 하므로 백엔드는 초기에는 JSON 유효성 및 비어 있음 정도만 검증한다.
* 개인 분석 클립 드로잉은 민감한 개인 피드백 데이터이므로 권한 검증 누락이 가장 큰 보안 위험이다.
