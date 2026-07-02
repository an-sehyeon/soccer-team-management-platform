# 24. 선수 개인 분석 클립 비동기 파일 생성 요구사항

## 1. 결론

선수 개인 분석 클립 비동기 파일 생성 기능은 기존에 원본 경기 영상 URL과 `startTimeSec`, `endTimeSec` 메타데이터만 저장하던 구조를 변경하여, 원본 경기 영상에서 실제 선수 개인 분석 클립 mp4 파일을 생성하고 해당 파일 URL을 `player_video_clip.url`에 저장하는 기능이다.

이번 구현 기준은 다음과 같다.

* 프론트 작업은 일시 중단하고 백엔드 구조를 먼저 변경한다.
* 선수 개인 분석 클립 생성 시 실제 mp4 클립 파일을 생성한다.
* 클립 파일 생성은 비동기 작업 큐 방식으로 처리한다.
* 클립 생성 요청이 들어오면 먼저 `player_video_clip` row를 `PROCESSING` 상태로 저장한다.
* 이후 백그라운드 작업에서 FFmpeg를 사용해 원본 경기 영상의 지정 구간을 잘라낸다.
* 클립 파일 생성 성공 시 `player_video_clip.url`에 생성된 클립 파일 URL을 저장하고 상태를 `READY`로 변경한다.
* 클립 파일 생성 실패 시 상태를 `FAILED`로 변경한다.
* 클립 생성 요청에는 드로잉 JSON 목록을 함께 받을 수 있다.
* 클립 메타데이터와 드로잉 데이터는 최종 저장 버튼 클릭 시 함께 저장한다.
* 클립 데이터는 `player_video_clip` 테이블에 저장한다.
* 드로잉 데이터는 `player_video_clip_drawing` 테이블에 저장한다.
* 드로잉은 영상 파일에 합성하지 않고 JSON 데이터로 별도 저장한다.
* 드로잉 시간 기준은 원본 경기 영상 기준이 아니라 생성될 클립 영상 기준 초로 저장한다.
* 원본 경기 영상이 이후 소프트 삭제되어도 이미 생성된 선수 개인 분석 클립 파일은 독립적으로 재생 가능해야 한다.
* 삭제된 원본 경기 영상으로는 새 클립을 생성할 수 없다.
* 기존 개별 선수 개인 분석 클립 생성 API와 드로잉 생성/수정/삭제 API는 유지하되, 등록 화면에서는 통합 생성 API를 우선 사용한다.

---

## 2. 기능 목적

이 기능의 목적은 선수 개인 분석 클립이 원본 경기 영상에 계속 의존하지 않도록 실제 클립 파일을 생성하는 것이다.

기존 구조에서는 선수 개인 분석 클립이 실제 파일이 아니라 다음 정보 조합으로 동작했다.

```text
원본 경기 영상 URL + startTimeSec + endTimeSec
```

이 구조는 빠르게 MVP를 만들기에는 좋지만, 실제 운영에서는 다음 문제가 있다.

* 원본 경기 영상이 소프트 삭제되면 클립 상세 조회나 재생 정책이 애매해진다.
* 원본 경기 영상 접근 권한과 선수 개인 분석 클립 접근 권한이 섞인다.
* 선수 개인 분석 클립이 독립적인 분석 자료로 관리되지 않는다.
* 추후 CDN, Signed URL, 스트리밍 API로 전환할 때 권한 정책이 복잡해진다.
* 선수 개인 피드백 자료를 장기 보관하기 어렵다.

따라서 선수 개인 분석 클립 생성 시 실제 mp4 파일을 생성하고, 이후 재생은 원본 경기 영상이 아니라 생성된 클립 파일 URL을 기준으로 처리한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

지도자는 선수 개인 분석 클립 파일 생성 기능을 사용할 수 있다.

가능한 기능은 다음과 같다.

* 원본 경기 영상 선택
* 대상 선수 선택
* 클립 구간 선택
* 드로잉 작성
* 선수 개인 분석 클립 통합 저장
* 클립 생성 상태 조회
* 생성 완료된 클립 재생
* 선수 개인 분석 클립 삭제

### 3.2 분석관 `ANALYST`

분석관도 선수 개인 분석 클립 파일 생성 기능을 사용할 수 있다.

가능한 기능은 다음과 같다.

* 원본 경기 영상 선택
* 대상 선수 선택
* 클립 구간 선택
* 드로잉 작성
* 선수 개인 분석 클립 통합 저장
* 클립 생성 상태 조회
* 생성 완료된 클립 재생

불가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 삭제

### 3.3 선수 `PLAYER`

선수는 본인에게 공유된 생성 완료 클립만 조회하고 재생할 수 있다.

가능한 기능은 다음과 같다.

* 본인 선수 개인 분석 클립 목록 조회
* 본인 선수 개인 분석 클립 상세 조회
* 생성 완료된 클립 재생
* 본인 클립 드로잉 조회

불가능한 기능은 다음과 같다.

* 다른 선수 클립 조회
* 클립 생성
* 클립 수정
* 클립 삭제
* 드로잉 등록/수정/삭제

---

## 4. 권한 정책

| 역할        | 통합 생성 |  목록 조회 |  상세 조회 | 재생 | 수정 | 삭제 |
| --------- | ----: | -----: | -----: | -: | -: | -: |
| `COACH`   |    가능 |  전체 가능 |  전체 가능 | 가능 | 가능 | 가능 |
| `ANALYST` |    가능 |  전체 가능 |  전체 가능 | 가능 | 가능 | 불가 |
| `PLAYER`  |    불가 | 본인만 가능 | 본인만 가능 | 가능 | 불가 | 불가 |

권한 검증은 반드시 백엔드에서 처리한다.

검증 기준은 다음과 같다.

* 인증되지 않은 사용자는 모든 선수 개인 분석 클립 API에 접근할 수 없다.
* `COACH`, `ANALYST`만 클립 파일 생성 요청을 할 수 있다.
* `PLAYER`는 클립 생성 요청을 직접 호출해도 실패해야 한다.
* `COACH`, `ANALYST`는 전체 선수 개인 분석 클립을 조회할 수 있다.
* `PLAYER`는 `player_video_clip.player_id`와 로그인 회원 ID가 일치하는 클립만 조회할 수 있다.
* `COACH`만 삭제할 수 있다.
* `ANALYST`는 삭제 API를 직접 호출해도 실패해야 한다.
* `isAdmin = true`만으로 클립 생성, 수정, 삭제 권한을 주지 않는다.

---

## 5. 화면 흐름

### 5.1 지도자/분석관 통합 생성 흐름

1. 지도자 또는 분석관이 선수 개인 분석 클립 등록 화면에 진입한다.
2. 원본 경기 영상을 선택한다.
3. 대상 선수를 선택한다.
4. 원본 경기 영상에서 클립 시작 시간과 종료 시간을 선택한다.
5. 선택한 구간을 기준으로 미리보기를 재생한다.
6. 해당 구간 위에 드로잉을 작성한다.
7. 제목, 메모, 클립 유형을 입력한다.
8. 최종 저장 버튼을 누른다.
9. 프론트는 클립 메타데이터와 드로잉 JSON 목록을 한 번에 전송한다.
10. 백엔드는 `player_video_clip`을 `PROCESSING` 상태로 저장한다.
11. 백엔드는 드로잉 목록을 `player_video_clip_drawing`에 저장한다.
12. 백그라운드 작업이 실제 mp4 클립 파일을 생성한다.
13. 생성 성공 시 `url` 저장 및 `READY` 상태로 변경한다.
14. 생성 실패 시 `FAILED` 상태로 변경한다.
15. 프론트는 목록에서 생성 상태를 표시한다.

### 5.2 선수 조회 흐름

1. 선수가 내 개인 분석 클립 화면에 진입한다.
2. 본인에게 지정된 클립 목록을 조회한다.
3. `PROCESSING` 상태 클립은 생성 중으로 표시한다.
4. `READY` 상태 클립은 재생 가능하게 표시한다.
5. `FAILED` 상태 클립은 생성 실패로 표시한다.
6. 선수가 `READY` 상태 클립을 선택한다.
7. 프론트는 `player_video_clip.url`에 저장된 클립 파일을 재생한다.
8. 드로잉은 클립 영상 위에 캔버스 오버레이로 표시한다.

---

## 6. API 흐름

### 6.1 통합 생성 API

```http
POST /api/management/player-analysis-clips/with-drawings
```

사용 가능 역할은 다음과 같다.

* `COACH`
* `ANALYST`

요청 예시는 다음과 같다.

```json
{
  "matchVideoId": 1,
  "playerId": 5,
  "clipType": "PASS",
  "title": "전진 패스 선택 가능 장면",
  "comment": "오른쪽 전진 패스 선택이 가능했던 장면",
  "startTimeSec": 755,
  "endTimeSec": 790,
  "drawings": [
    {
      "drawingType": "ARROW",
      "startTimeSec": 3,
      "endTimeSec": 8,
      "drawingData": {
        "fromX": 0.35,
        "fromY": 0.42,
        "toX": 0.62,
        "toY": 0.38,
        "color": "#ff0000",
        "lineWidth": 4
      }
    },
    {
      "drawingType": "TEXT",
      "startTimeSec": 5,
      "endTimeSec": 10,
      "drawingData": {
        "x": 0.4,
        "y": 0.2,
        "text": "여기서 전진 패스 가능",
        "color": "#ffffff",
        "fontSize": 18
      }
    }
  ]
}
```

응답 예시는 다음과 같다.

```json
{
  "playerClipId": 1,
  "status": "PROCESSING",
  "message": "선수 개인 분석 클립 생성 작업이 등록되었습니다."
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원 역할이 `COACH` 또는 `ANALYST`인지 확인한다.
3. `matchVideoId`가 존재하고 삭제되지 않았는지 확인한다.
4. 원본 경기 영상의 실제 파일 경로를 확인한다.
5. 원본 경기 영상의 `durationSec`가 준비되어 있는지 확인한다.
6. `playerId`가 존재하고 삭제되지 않았으며 `PLAYER` 역할인지 확인한다.
7. 클립 시작/종료 시간이 원본 경기 영상 길이 안에 있는지 확인한다.
8. 드로잉 목록의 시간 값이 생성될 클립 길이 안에 있는지 확인한다.
9. `player_video_clip`을 `PROCESSING` 상태로 저장한다.
10. `player_video_clip_drawing`에 드로잉 목록을 저장한다.
11. 비동기 클립 생성 작업을 큐에 등록한다.
12. `playerClipId`, `status`, `message`를 반환한다.

---

## 7. 비동기 작업 흐름

### 7.1 작업 등록

통합 생성 API가 호출되면 백엔드는 DB 저장 후 클립 생성 작업을 비동기로 실행한다.

초기 구현에서는 Spring `@Async` 기반으로 시작한다.

추후 운영 규모가 커지면 Redis Queue, RabbitMQ, Kafka, 또는 별도 워커 서버로 확장할 수 있게 구조를 분리한다.

### 7.2 작업 처리

비동기 작업은 다음 순서로 처리한다.

1. `playerClipId`로 `player_video_clip`을 다시 조회한다.
2. 연결된 원본 경기 영상 파일 경로를 확인한다.
3. FFmpeg 명령어로 지정 구간을 추출한다.
4. 생성 파일을 `backend/uploads/player-analysis-clips` 경로에 저장한다.
5. 생성된 파일 접근 URL을 만든다.
6. 성공 시 `player_video_clip.url`에 URL을 저장한다.
7. 성공 시 `player_video_clip.status = READY`로 변경한다.
8. 실패 시 `player_video_clip.status = FAILED`로 변경한다.
9. 실패 메시지는 로그에 남기고, 추후 컬럼 추가 시 DB에도 저장할 수 있게 한다.

### 7.3 상태 정책

`player_video_clip.status`는 다음 값을 사용한다.

| 상태           | 의미                 |
| ------------ | ------------------ |
| `PROCESSING` | 클립 파일 생성 중         |
| `READY`      | 클립 파일 생성 완료, 재생 가능 |
| `FAILED`     | 클립 파일 생성 실패        |

기존 `UPLOADING` 상태는 선수 개인 분석 클립 생성 흐름에는 맞지 않으므로 사용하지 않는다.

---

## 8. DB 설계 방향

초기 구현에서는 기존 `player_video_clip.url` 컬럼을 사용한다.

```text
player_video_clip.url = 생성된 선수 개인 분석 클립 mp4 URL
```

기존 `start_time_sec`, `end_time_sec` 컬럼은 원본 경기 영상 기준 시간을 유지한다.

```text
player_video_clip.start_time_sec = 원본 경기 영상 기준 시작 시간
player_video_clip.end_time_sec = 원본 경기 영상 기준 종료 시간
```

드로잉 테이블의 시간 기준은 생성된 클립 영상 기준으로 변경한다.

```text
player_video_clip_drawing.start_time_sec = 클립 영상 기준 시작 시간
player_video_clip_drawing.end_time_sec = 클립 영상 기준 종료 시간
```

추후 운영 단계에서는 아래 컬럼 추가를 검토한다.

```text
player_video_clip.stored_file_name
player_video_clip.file_size
player_video_clip.duration_sec
player_video_clip.processing_message
player_video_clip.thumbnail_url
```

초기에는 필수 컬럼으로 추가하지 않고, 현재 테이블 구조를 최대한 활용한다.

---

## 9. 예외 상황

### 9.1 원본 경기 영상이 없는 경우

`matchVideoId`에 해당하는 원본 경기 영상이 없으면 실패한다.

예외 코드 후보:

```text
MATCH_VIDEO_NOT_FOUND
```

### 9.2 원본 경기 영상이 삭제된 경우

삭제된 원본 경기 영상으로 새 클립을 생성할 수 없다.

예외 코드 후보:

```text
MATCH_VIDEO_NOT_FOUND
```

### 9.3 원본 경기 영상 길이가 없는 경우

`durationSec`가 없거나 0 이하이면 클립 생성 요청을 실패 처리한다.

예외 코드 후보:

```text
MATCH_VIDEO_DURATION_NOT_READY
```

### 9.4 클립 시간 범위가 잘못된 경우

`startTimeSec >= endTimeSec`이거나 원본 영상 길이를 초과하면 실패한다.

예외 코드 후보:

```text
INVALID_CLIP_TIME_RANGE
```

### 9.5 드로잉 시간 범위가 잘못된 경우

드로잉 시간이 생성될 클립 영상 길이를 벗어나면 실패한다.

예외 코드 후보:

```text
INVALID_DRAWING_TIME_RANGE
```

### 9.6 FFmpeg 작업 실패

비동기 작업 중 FFmpeg 실행이 실패하면 API 요청 자체를 되돌리지 않고, 해당 클립의 상태를 `FAILED`로 변경한다.

예외 처리 기준은 다음과 같다.

* DB row는 유지한다.
* `status = FAILED`로 변경한다.
* `url`은 null로 유지한다.
* 실패 로그를 남긴다.
* 추후 재시도 API를 추가할 수 있게 구조를 남긴다.

---

## 10. 구현 순서

1. `docs/24_player_analysis_clip_file_generation_requirements.md` 문서를 추가한다.
2. `VideoUploadStatusEnum` 또는 별도 클립 상태 Enum에 `PROCESSING`, `READY`, `FAILED` 사용 가능 여부를 확인한다.
3. 통합 생성 요청 DTO를 추가한다.
4. 통합 생성 응답 DTO를 추가한다.
5. 드로잉 포함 요청 DTO를 추가한다.
6. `PlayerAnalysisClipController`에 통합 생성 API를 추가한다.
7. `PlayerAnalysisClipService`에 통합 생성 메서드를 추가한다.
8. 클립 메타데이터와 드로잉 목록을 먼저 DB에 저장한다.
9. 비동기 작업 실행 서비스를 추가한다.
10. FFmpeg 기반 클립 파일 생성 서비스를 추가한다.
11. 로컬 저장 경로 `backend/uploads/player-analysis-clips`를 설정한다.
12. 클립 생성 성공 시 `url`, `status = READY`로 갱신한다.
13. 클립 생성 실패 시 `status = FAILED`로 갱신한다.
14. 상세 조회 응답에서 원본 경기 영상 URL이 아니라 생성된 클립 URL을 재생 기준으로 사용할 수 있게 응답 필드를 점검한다.
15. 기존 드로잉 시간 검증 기준을 클립 영상 기준으로 변경한다.
16. Postman 또는 프론트 호출 전 테스트로 통합 생성 요청을 검증한다.

---

## 11. 추후 확장 가능성

추후 다음 기능으로 확장할 수 있다.

* 클립 생성 재시도 API
* 클립 생성 실패 사유 DB 저장
* 썸네일 생성
* 클립 파일 용량 저장
* 클립 영상 길이 저장
* CDN 업로드
* Signed URL 기반 재생
* Redis Queue 기반 작업 큐
* 별도 인코딩 워커 서버
* 드로잉 합성 export mp4 생성
* 팀 분석 클립도 동일한 비동기 파일 생성 구조로 전환
