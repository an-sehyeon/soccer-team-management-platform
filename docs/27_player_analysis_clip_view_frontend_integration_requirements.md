# 선수 개인 분석 클립 조회 기록 프론트 연동 요구사항

> [!IMPORTANT]
> 이 문서는 해당 기능의 기존 구현 당시 기준과 작업 이력을 보존하는 문서다.
> 경기 영상 편집기 v1의 신규 구현은 `docs/31_video_editor_v1_requirements.md`,
> `docs/00_current_backend_policy.md`, `docs/00_current_frontend_policy.md`를 우선한다.
> 역할 권한, 클립 상태, 시간 단위, 삭제, 드로잉 합성, 영상 제공 방식이 충돌하면 최신 문서를 적용한다.
> 기존 API는 프론트 전환 전 호환 목적으로 유지될 수 있으므로 실제 제거 여부는 현재 소스를 확인한다.


## 1. 기능 목적

선수 개인 분석 클립 조회 기록 프론트 연동 기능은 지도자와 분석관이 선수에게 공유한 개인 분석 클립을 해당 선수가 실제로 확인했는지 화면에서 확인할 수 있게 하는 기능이다.

조회 기록에는 다음 정보를 표시한다.

* 조회 선수
* 최초 확인 시간
* 마지막 확인 시간
* 대상 선수 조회 횟수

이번 기능에서 표시하는 조회 횟수는 지도자나 분석관이 본 횟수가 아니라, 해당 클립의 대상 선수가 본인 개인 분석 클립을 조회한 횟수다.

---

## 2. 사용자 역할

### COACH

지도자는 선수 개인 분석 클립 상세 화면에서 해당 클립의 선수 확인 기록을 조회할 수 있다.

가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 조회 기록 확인
* 최초 확인 시간 확인
* 마지막 확인 시간 확인
* 대상 선수 조회 횟수 확인

불가능한 기능은 다음과 같다.

* 조회 기록 직접 생성
* 조회 기록 직접 수정
* 조회 기록 삭제

### ANALYST

분석관은 기존 백엔드 권한 정책에 따라 선수 개인 분석 클립 조회 기록을 확인할 수 있다.

가능한 기능은 다음과 같다.

* 선수 개인 분석 클립 조회 기록 확인
* 최초 확인 시간 확인
* 마지막 확인 시간 확인
* 대상 선수 조회 횟수 확인

불가능한 기능은 다음과 같다.

* 조회 기록 직접 생성
* 조회 기록 직접 수정
* 조회 기록 삭제

### PLAYER

선수는 조회 기록 관리 UI를 볼 수 없다.

선수가 본인 개인 분석 클립 상세 화면을 조회하면 백엔드에서 기존 정책대로 조회 기록을 자동 생성하거나 갱신한다.

---

## 3. 권한 정책

| 역할      | 조회 기록 UI |                    조회 기록 조회 API |            조회 기록 생성 | 조회 기록 삭제 |
| ------- | -------: | ------------------------------: | ------------------: | -------: |
| COACH   |       가능 |                              가능 |                  불가 |       불가 |
| ANALYST |       가능 |                              가능 |                  불가 |       불가 |
| PLAYER  |       불가 | 본인 기록 조회 API는 존재하지만 이번 UI에는 미노출 | 본인 상세 조회 시 서버 자동 처리 |       불가 |

권한 검증은 반드시 백엔드에서 처리한다.

프론트에서는 사용자 경험을 위해 COACH, ANALYST에게만 조회 기록 영역을 표시한다.

---

## 4. 화면 흐름

### 4.1 지도자/분석관 화면

1. 지도자 또는 분석관이 `/player-analysis-clips` 화면에 진입한다.
2. 선수 개인 분석 클립 목록에서 클립을 선택한다.
3. 프론트가 선수 개인 분석 클립 상세 API를 호출한다.
4. 상세 응답에서 `playerId`, `playerClipId`를 확인한다.
5. 프론트가 관리용 선수 조회 기록 API를 호출한다.
6. 해당 선수의 조회 기록 목록에서 현재 선택한 `playerClipId`와 일치하는 기록을 찾는다.
7. 기록이 있으면 선수 확인 기록을 표시한다.
8. 기록이 없으면 아직 선수가 해당 클립을 조회하지 않았다는 문구를 표시한다.

### 4.2 선수 화면

1. 선수가 본인 개인 분석 클립 목록에 진입한다.
2. 선수가 본인 개인 분석 클립 상세를 조회한다.
3. 백엔드가 조회 기록을 자동 저장 또는 갱신한다.
4. 선수 화면에는 조회 기록 관리 UI를 표시하지 않는다.

---

## 5. API 흐름

### 5.1 관리용 선수 조회 기록 API

```http
GET /api/management/players/{playerId}/player-analysis-clip-views?page=0&size=50
```

사용 대상은 다음과 같다.

* COACH
* ANALYST

프론트 처리 방식은 다음과 같다.

1. 선수 개인 분석 클립 상세 조회
2. 상세 응답의 `playerId` 기준으로 조회 기록 API 호출
3. 응답의 `views` 배열에서 현재 `playerClipId`와 일치하는 기록 검색
4. 일치하는 기록을 상세 화면의 선수 확인 기록 영역에 표시

### 5.2 선수 본인 조회 기록 API

```http
GET /api/player/me/player-analysis-clip-views?page=0&size=20
```

이번 프론트 화면에는 노출하지 않는다.

추후 선수 본인의 피드백 확인 이력 화면이 필요해지면 별도 기능으로 확장한다.

---

## 6. 응답 타입

프론트 조회 기록 타입은 다음 구조를 사용한다.

```ts
export type PlayerAnalysisClipViewResponse = {
  viewId: number;
  playerClipId: number;
  playerClipTitle: string;
  playerId: number;
  playerName: string;
  firstViewedAt: string;
  lastViewedAt: string;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
};

export type PlayerAnalysisClipViewPageResponse = {
  views: PlayerAnalysisClipViewResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
```

---

## 7. DB 설계 방향

이번 기능에서는 DB를 변경하지 않는다.

기존 `player_video_clip_view` 테이블을 그대로 사용한다.

주요 컬럼은 다음과 같다.

* `id`
* `player_video_clip_id`
* `member_id`
* `first_viewed_at`
* `last_viewed_at`
* `view_count`
* `is_deleted`
* `created_at`
* `updated_at`

`player_video_clip_id`, `member_id` 조합은 유니크하게 관리한다.

따라서 최초 조회 시 row를 생성하고, 재조회 시 기존 row의 `lastViewedAt`, `viewCount`를 갱신한다.

---

## 8. 구현 내용

### 8.1 백엔드

`main` 기준 조회 기록 Service, Validator, DTO, Repository는 존재했지만 외부에서 호출할 Controller가 없었다.

따라서 다음 Controller를 추가했다.

```text
backend/src/main/java/com/soccer/platform/controller/PlayerAnalysisClipViewController.java
```

추가된 API는 다음과 같다.

```http
GET /api/management/players/{playerId}/player-analysis-clip-views
GET /api/player/me/player-analysis-clip-views
```

### 8.2 프론트 타입

다음 파일을 추가했다.

```text
frontend/src/types/playerAnalysisClipView.ts
```

조회 기록 응답 타입과 페이지 응답 타입을 정의했다.

### 8.3 프론트 API

다음 파일을 추가했다.

```text
frontend/src/api/playerAnalysisClipViewApi.ts
```

관리용 선수 조회 기록 API와 선수 본인 조회 기록 API 호출 함수를 작성했다.

### 8.4 프론트 화면

다음 파일을 수정했다.

```text
frontend/src/pages/PlayerAnalysisClipPage.tsx
```

수정 내용은 다음과 같다.

* 선수 개인 분석 클립 상세 선택 시 조회 기록 API 호출
* COACH, ANALYST에게만 선수 확인 기록 영역 표시
* PLAYER에게는 조회 기록 UI 미노출
* 조회 기록이 있으면 조회 선수, 최초 확인 시간, 마지막 확인 시간, 대상 선수 조회 횟수 표시
* 조회 기록이 없으면 미조회 안내 문구 표시
* 조회 기록 API 실패 시 사용자 친화적 에러 메시지 표시

---

## 9. 예외 상황

| 상황               | 처리                                  |
| ---------------- | ----------------------------------- |
| 조회 기록 없음         | “아직 선수가 이 개인 분석 클립을 조회하지 않았습니다.” 표시 |
| 조회 기록 API 실패     | 조회 기록 영역에 에러 메시지 표시                 |
| PLAYER 로그인       | 조회 기록 UI 미노출                        |
| COACH/ANALYST 조회 | 조회 기록 확인 가능                         |
| 지도자/분석관이 상세 조회   | 조회 기록 증가 없음                         |
| 대상 선수가 본인 상세 조회  | 조회 기록 생성 또는 증가                      |

---

## 10. 테스트 확인

확인된 테스트는 다음과 같다.

* 선수 개인 분석 클립 상세 화면에서 조회 기록 영역 정상 표시
* 조회 기록이 있는 경우 선수 확인 기록 정상 표시
* 조회 기록이 없는 경우 미조회 안내 문구 정상 표시
* 지도자/분석관 조회가 대상 선수 조회 횟수로 오해되지 않도록 문구를 “대상 선수 조회 횟수”로 표시
* 화면 정상 동작 확인

---

## 11. 삭제 정책

이번 기능에서는 조회 기록 삭제 기능을 구현하지 않는다.

기존 조회 기록 데이터는 조회 전용으로 사용한다.

추후 삭제 또는 초기화가 필요하면 별도 관리 기능으로 분리한다.

---

## 12. 추후 확장 가능성

추후 확장 후보는 다음과 같다.

* 선수별 피드백 확인 현황 대시보드
* 미확인 개인 분석 클립 필터
* 클립별 조회 기록 상세 화면
* 선수별 조회율 통계
* 일정 기간 미확인 선수 목록
* 피드백 확인 알림
* 선수 코멘트 또는 확인 완료 버튼
* 지도자 확인 요청 기능

---

## 13. 최종 변경 사항

초기 요구사항 대비 변경된 내용은 다음과 같다.

* 프론트 연동만 예상했으나 `main` 기준 조회 기록 Controller가 없어 백엔드 Controller 추가가 포함되었다.
* 별도 관리 화면은 만들지 않고 기존 `/player-analysis-clips` 상세 영역에 조회 기록 섹션을 추가했다.
* 조회 횟수 문구는 오해 방지를 위해 “대상 선수 조회 횟수”로 표시했다.
