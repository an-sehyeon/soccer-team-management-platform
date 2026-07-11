# 29. 경기 영상 북마크 기능 요구사항

## 1. 문서 목적

이 문서는 축구팀 영상분석 플랫폼의 경기 영상 북마크 기능에 대한 요구사항을 정의한다.

북마크 기능은 지도자와 분석관이 경기 원본 영상, 팀 분석 클립, 선수 개인 분석 클립을 시청하면서 중요한 시점을 개인 북마크로 저장하고, 이후 해당 시점부터 팀 분석 클립, 선수 개인 분석 클립 또는 선수 기록 이벤트를 순서대로 등록할 수 있도록 지원하는 기능이다.

북마크는 팀 구성원에게 공유하는 데이터가 아니라 작성자 개인의 영상 분석 작업을 돕기 위한 임시 작업 데이터로 사용한다.

---

## 2. 기능 목적

북마크 기능의 주요 목적은 다음과 같다.

* 경기 영상을 시청하면서 중요한 장면을 빠르게 표시한다.
* 영상 시청을 중단하지 않고 분석할 장면을 먼저 수집한다.
* 수집한 북마크를 최신 등록순으로 확인한다.
* 북마크를 순서대로 선택해 팀 분석 클립을 등록한다.
* 북마크를 순서대로 선택해 선수 개인 분석 클립을 등록한다.
* 북마크를 이용해 선수 기록 이벤트를 등록한다.
* 북마크를 클릭하면 해당 시점으로 즉시 이동하고 재생한다.
* 북마크 사용 후 작성자가 필요할 때 직접 수정하거나 삭제한다.

초기 구현에서는 여러 북마크를 선택해 한 번에 클립을 생성하는 일괄 생성 기능은 구현하지 않는다.

북마크는 하나씩 선택해 기존 분석 등록 화면으로 연결한다.

---

## 3. 사용자 역할

### 3.1 COACH

`COACH`는 다음 기능을 사용할 수 있다.

* 본인 북마크 등록
* 본인 북마크 조회
* 본인 북마크 수정
* 본인 북마크 삭제
* 북마크를 이용한 팀 분석 클립 등록
* 북마크를 이용한 선수 개인 분석 클립 등록
* 북마크를 이용한 선수 기록 이벤트 등록

다른 지도자 또는 분석관이 작성한 북마크는 조회하거나 수정·삭제할 수 없다.

### 3.2 ANALYST

`ANALYST`는 다음 기능을 사용할 수 있다.

* 본인 북마크 등록
* 본인 북마크 조회
* 본인 북마크 수정
* 본인 북마크 삭제
* 북마크를 이용한 팀 분석 클립 등록
* 북마크를 이용한 선수 개인 분석 클립 등록
* 북마크를 이용한 선수 기록 이벤트 등록

북마크는 개인 작업 데이터이므로 기존의 일반적인 분석관 삭제 제한 정책과 다르게 본인이 작성한 북마크는 삭제할 수 있다.

다른 지도자 또는 분석관이 작성한 북마크는 조회하거나 수정·삭제할 수 없다.

### 3.3 PLAYER

`PLAYER`는 북마크 기능을 사용하지 않는다.

* 북마크 등록 불가
* 북마크 조회 불가
* 북마크 수정 불가
* 북마크 삭제 불가
* 북마크 UI 미노출
* 북마크 API 접근 불가

---

## 4. 북마크 대상 영상

북마크는 다음 영상에서 생성할 수 있다.

```text
MATCH_VIDEO
TEAM_ANALYSIS
PLAYER_ANALYSIS
```

별도의 `source_type` 컬럼은 사용하지 않는다.

북마크 대상은 `upload_id`, `team_clip_id`, `player_clip_id` 조합으로 판단한다.

### 4.1 경기 원본 영상 북마크

```text
upload_id = 경기 영상 ID
team_clip_id = null
player_clip_id = null
```

### 4.2 팀 분석 클립 북마크

```text
upload_id = 원본 경기 영상 ID
team_clip_id = 팀 분석 클립 ID
player_clip_id = null
```

### 4.3 선수 개인 분석 클립 북마크

```text
upload_id = 원본 경기 영상 ID
team_clip_id = null
player_clip_id = 선수 개인 분석 클립 ID
```

다음 조합은 허용하지 않는다.

```text
team_clip_id와 player_clip_id가 동시에 존재
```

북마크 생성 시 사용자가 영상 유형을 별도로 선택하지 않는다.

현재 재생 중인 영상의 종류와 ID를 프론트가 판단해 요청하고, 백엔드는 전달된 ID 조합과 실제 연관관계를 검증한다.

---

## 5. 북마크 시간 정책

### 5.1 저장 기준

북마크 시간은 현재 재생 중인 영상 기준 정수 초로 저장한다.

```text
bookmark_time_sec INT UNSIGNED
```

예시는 다음과 같다.

```text
경기 원본 영상 125초에서 생성
→ bookmark_time_sec = 125

팀 분석 클립 내부 12초에서 생성
→ bookmark_time_sec = 12

선수 개인 분석 클립 내부 8초에서 생성
→ bookmark_time_sec = 8
```

초기 구현에서는 소수점 이하 시간은 저장하지 않는다.

영상의 현재 재생 시간이 소수점인 경우 소수점 이하를 버리고 정수 초로 저장한다.

### 5.2 중복 정책

같은 영상의 같은 시간에 여러 북마크를 등록할 수 있다.

다음 경우 모두 허용한다.

```text
같은 작성자
같은 영상
같은 bookmark_time_sec
서로 다른 제목 또는 메모
```

시간 중복을 제한하는 유니크 제약조건은 추가하지 않는다.

---

## 6. 영상 길이 검증

북마크 시간은 반드시 현재 대상 영상의 재생 길이 안에 있어야 한다.

공통 검증은 다음과 같다.

```text
bookmarkTimeSec >= 0
bookmarkTimeSec <= 대상 영상 길이
```

### 6.1 경기 원본 영상

경기 원본 영상은 다음 값을 사용한다.

```text
game_video_upload.duration_sec
```

검증 기준은 다음과 같다.

```text
bookmarkTimeSec <= game_video_upload.duration_sec
```

`duration_sec`이 없으면 북마크를 등록하거나 시간을 수정할 수 없다.

### 6.2 팀 분석 클립

팀 분석 클립 길이는 다음과 같이 계산한다.

```text
clipDurationSec
= team_video_clip.end_time_sec
- team_video_clip.start_time_sec
```

검증 기준은 다음과 같다.

```text
bookmarkTimeSec <= clipDurationSec
```

팀 분석 클립은 다음 조건을 모두 만족해야 한다.

* `is_deleted = false`
* `status = READY`
* 원본 경기 영상 `is_deleted = false`
* 요청의 `upload_id`와 팀 분석 클립의 `upload_id` 일치

### 6.3 선수 개인 분석 클립

선수 개인 분석 클립 길이는 다음과 같이 계산한다.

```text
clipDurationSec
= player_video_clip.end_time_sec
- player_video_clip.start_time_sec
```

검증 기준은 다음과 같다.

```text
bookmarkTimeSec <= clipDurationSec
```

선수 개인 분석 클립은 다음 조건을 모두 만족해야 한다.

* `is_deleted = false`
* `status = READY`
* 원본 경기 영상 `is_deleted = false`
* 요청의 `upload_id`와 선수 개인 분석 클립의 `upload_id` 일치

### 6.4 등록 불가능 상태

다음 상태에서는 북마크를 등록할 수 없다.

```text
PROCESSING
FAILED
UPLOADING
삭제된 영상
삭제된 클립
```

실제 재생 가능한 `READY` 상태에서만 북마크 등록을 허용한다.

---

## 7. 북마크 입력값 정책

### 7.1 제목

```text
title
```

정책은 다음과 같다.

* 필수
* 앞뒤 공백 제거
* 공백만 입력 불가
* 최대 255자

### 7.2 메모

```text
memo
```

정책은 다음과 같다.

* 선택
* 앞뒤 공백 제거
* 공백만 입력하면 `null` 저장
* 최대 255자

### 7.3 시간

```text
bookmarkTimeSec
```

정책은 다음과 같다.

* 필수
* 정수
* 0 이상
* 현재 대상 영상 길이 이하

---

## 8. DB 설계

### 8.1 대상 테이블

```text
video_bookmark
```

### 8.2 최종 컬럼 방향

```sql
CREATE TABLE `video_bookmark` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `upload_id` INT NOT NULL COMMENT '원본 경기 영상 업로드 ID',
    `team_clip_id` INT NULL COMMENT '팀 분석 클립 ID',
    `player_clip_id` INT NULL COMMENT '선수 개인 분석 클립 ID',
    `member_id` INT NOT NULL COMMENT '북마크 작성 회원 ID',
    `bookmark_time_sec` INT UNSIGNED NOT NULL COMMENT '현재 대상 영상 기준 북마크 시간(초)',
    `title` VARCHAR(255) NOT NULL COMMENT '북마크 제목',
    `memo` VARCHAR(255) NULL COMMENT '북마크 메모',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 전: 0, 삭제 후: 1',
    `created_at` DATETIME NOT NULL COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL COMMENT '수정일시',
    PRIMARY KEY (`id`),
    KEY `idx_video_bookmark_source_owner_deleted`
        (`upload_id`, `team_clip_id`, `player_clip_id`, `member_id`, `is_deleted`, `created_at`),
    CONSTRAINT `fk_video_bookmark_upload`
        FOREIGN KEY (`upload_id`) REFERENCES `game_video_upload` (`id`),
    CONSTRAINT `fk_video_bookmark_team_clip`
        FOREIGN KEY (`team_clip_id`) REFERENCES `team_video_clip` (`id`),
    CONSTRAINT `fk_video_bookmark_player_clip`
        FOREIGN KEY (`player_clip_id`) REFERENCES `player_video_clip` (`id`),
    CONSTRAINT `fk_video_bookmark_member`
        FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `chk_video_bookmark_single_clip_source`
        CHECK (
            NOT (
                `team_clip_id` IS NOT NULL
                AND `player_clip_id` IS NOT NULL
            )
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

실제 기존 테이블을 변경할 때는 `ALTER TABLE` 또는 개발 DB 초기화 정책에 맞춰 스키마를 수정한다.

### 8.3 작성자 저장

`member_id`에는 현재 로그인한 사용자의 ID를 저장한다.

클라이언트에서 작성자 ID를 요청값으로 받지 않는다.

```text
CustomUserPrincipal.memberId
→ video_bookmark.member_id
```

### 8.4 삭제 정책

북마크 삭제는 소프트 삭제를 사용한다.

```text
is_deleted = false
→ 활성 북마크

is_deleted = true
→ 삭제된 북마크
```

일반 목록과 수정 대상 조회에서는 삭제된 북마크를 제외한다.

---

## 9. API 설계

### 9.1 북마크 등록

```http
POST /api/management/video-bookmarks
```

호출 가능 역할은 다음과 같다.

```text
COACH
ANALYST
```

요청 필드는 다음을 기준으로 한다.

```text
matchVideoId
teamClipId
playerClipId
bookmarkTimeSec
title
memo
```

정책은 다음과 같다.

* `matchVideoId` 필수
* `teamClipId` 선택
* `playerClipId` 선택
* `teamClipId`, `playerClipId` 동시 전달 불가
* 작성자는 인증 사용자 기준으로 저장
* 현재 대상 영상의 길이 검증
* 삭제되거나 재생 불가능한 영상에는 등록 불가

### 9.2 북마크 목록 조회

```http
GET /api/management/video-bookmarks
```

호출 가능 역할은 다음과 같다.

```text
COACH
ANALYST
```

#### 경기 원본 영상 조회

```http
GET /api/management/video-bookmarks?matchVideoId={matchVideoId}
```

조회 조건은 다음과 같다.

```text
upload_id = matchVideoId
team_clip_id IS NULL
player_clip_id IS NULL
member_id = 로그인 사용자 ID
is_deleted = false
```

#### 팀 분석 클립 조회

```http
GET /api/management/video-bookmarks
    ?matchVideoId={matchVideoId}
    &teamClipId={teamClipId}
```

조회 조건은 다음과 같다.

```text
upload_id = matchVideoId
team_clip_id = teamClipId
player_clip_id IS NULL
member_id = 로그인 사용자 ID
is_deleted = false
```

#### 선수 개인 분석 클립 조회

```http
GET /api/management/video-bookmarks
    ?matchVideoId={matchVideoId}
    &playerClipId={playerClipId}
```

조회 조건은 다음과 같다.

```text
upload_id = matchVideoId
team_clip_id IS NULL
player_clip_id = playerClipId
member_id = 로그인 사용자 ID
is_deleted = false
```

목록은 페이지네이션 없이 현재 영상의 활성 북마크 전체를 반환한다.

정렬 기준은 다음과 같다.

```text
created_at DESC
```

최신 등록 북마크를 가장 위에 표시한다.

다른 사용자가 작성한 북마크는 반환하지 않는다.

### 9.3 북마크 수정

```http
PATCH /api/management/video-bookmarks/{bookmarkId}
```

수정 가능 필드는 다음과 같다.

```text
title
memo
bookmarkTimeSec
```

정책은 다음과 같다.

* 작성자 본인만 수정 가능
* 다른 사용자의 북마크 수정 불가
* 북마크 대상 영상 변경 불가
* `upload_id` 변경 불가
* `team_clip_id` 변경 불가
* `player_clip_id` 변경 불가
* 수정된 시간에 대해 영상 길이 재검증
* 대상 영상 또는 클립이 삭제된 경우 수정 불가
* 대상 클립이 `READY` 상태가 아닌 경우 시간 수정 불가

### 9.4 북마크 삭제

```http
DELETE /api/management/video-bookmarks/{bookmarkId}
```

정책은 다음과 같다.

* 작성자 본인만 삭제 가능
* `COACH`, `ANALYST` 모두 본인 북마크 삭제 가능
* 다른 사용자의 북마크 삭제 불가
* 물리 삭제하지 않음
* `is_deleted = true`로 변경
* 삭제 성공 후 현재 사이드바 목록에서 제거

### 9.5 별도 상세 조회

초기 구현에서는 별도 상세 조회 API를 만들지 않는다.

목록 응답에 다음 정보를 포함해 선택, 수정, 삭제에 사용한다.

```text
bookmarkId
matchVideoId
teamClipId
playerClipId
bookmarkTimeSec
title
memo
createdAt
updatedAt
```

실제 DTO 클래스명과 기존 공통 응답 구조는 구현 전 현재 프로젝트 소스를 확인해 맞춘다.

---

## 10. 백엔드 권한 검증

백엔드에서는 다음 사항을 반드시 검증한다.

* 로그인 사용자 역할이 `COACH` 또는 `ANALYST`인지 확인
* `PLAYER` 요청 차단
* 로그인 사용자 ID와 북마크 작성자 ID 일치 여부 확인
* 원본 경기 영상 존재 여부 확인
* 원본 경기 영상 삭제 여부 확인
* 팀 분석 클립 존재 여부 확인
* 팀 분석 클립 삭제 여부 확인
* 선수 개인 분석 클립 존재 여부 확인
* 선수 개인 분석 클립 삭제 여부 확인
* 클립과 요청 `matchVideoId`의 원본 경기 일치 여부 확인
* `teamClipId`, `playerClipId` 동시 전달 차단
* 북마크 시간 범위 검증
* 삭제된 북마크 수정·삭제 차단

프론트에서 버튼을 숨겨도 API 직접 호출이 가능하므로 모든 권한과 소유권 검증은 백엔드에서 다시 처리한다.

---

## 11. 화면 구성

### 11.1 대상 화면

```text
frontend/src/pages/MatchVideoPage.tsx
```

별도 북마크 페이지나 라우트는 만들지 않는다.

북마크 기능은 기존 경기 영상·분석 작업 화면 안에서 처리한다.

### 11.2 북마크 목록 버튼

`COACH`, `ANALYST` 화면에 북마크 목록 버튼을 표시한다.

```text
북마크 목록
```

버튼 클릭 시 화면 우측에 북마크 사이드바를 표시한다.

`PLAYER`에게는 버튼을 표시하지 않는다.

### 11.3 우측 사이드바

사이드바에는 현재 재생 중인 영상에 속한 본인의 북마크만 표시한다.

다른 영상의 북마크는 표시하지 않는다.

표시 정보는 다음과 같다.

* 북마크 제목
* 북마크 시간
* 북마크 메모
* 생성 시간
* 수정 버튼
* 삭제 버튼

정렬은 최신 등록순이다.

사이드바는 닫기 버튼을 제공한다.

### 11.4 북마크 등록

북마크 생성 시 사용자가 대상 영상 유형을 선택하지 않는다.

현재 재생 중인 영상을 기준으로 다음 값을 자동 설정한다.

```text
경기 원본 영상
→ matchVideoId만 전달

팀 분석 클립
→ matchVideoId, teamClipId 전달

선수 개인 분석 클립
→ matchVideoId, playerClipId 전달
```

현재 재생 시간을 정수 초로 변환해 등록 폼의 시간 초기값으로 사용한다.

등록 시 제목과 선택 메모를 입력한다.

정확한 등록 버튼 위치와 컴포넌트 분리 방식은 현재 `MatchVideoPage`와 분석 패널 구조를 확인한 후 결정한다.

### 11.5 북마크 선택

사이드바에서 북마크를 클릭하면 다음 순서로 처리한다.

1. 현재 영상의 `currentTime`을 `bookmarkTimeSec`으로 변경
2. 영상 즉시 재생
3. 선택한 북마크를 활성 상태로 표시
4. 영상 아래에 선택한 북마크 상세 정보 표시
5. 분석 등록 버튼 표시

표시할 버튼은 다음과 같다.

```text
팀 분석 클립 등록
선수 개인 분석 클립 등록
선수 기록 이벤트 등록
```

### 11.6 북마크 수정

수정 버튼 클릭 시 다음 값을 수정할 수 있다.

* 제목
* 메모
* 북마크 시간

북마크 시간은 현재 영상 길이 범위 안에서만 변경할 수 있다.

수정 성공 후 사이드바 목록을 다시 반영한다.

### 11.7 북마크 삭제

사이드바 각 항목에 삭제 버튼을 표시한다.

삭제 버튼 클릭 시 삭제 확인을 받은 뒤 API를 호출한다.

삭제 성공 후 해당 항목은 사이드바 목록에서 제거한다.

북마크를 이용해 분석 클립이나 선수 기록 이벤트를 등록해도 북마크는 자동 삭제하지 않는다.

사용자가 필요할 때 사이드바에서 직접 삭제한다.

---

## 12. 북마크를 이용한 분석 작업

### 12.1 기본 정책

북마크에서 분석 작업 버튼을 클릭하면 기존 `MatchVideoPage`의 분석 모드를 사용한다.

새로운 독립 편집 페이지는 만들지 않는다.

기존 분석 모드는 다음과 같다.

```text
team-clip-create
player-clip-create
player-record-event
```

선택한 북마크의 시간 정보를 해당 분석 패널의 초기값으로 전달한다.

북마크를 사용해 등록을 완료해도 북마크는 유지한다.

### 12.2 경기 원본 영상 북마크

경기 원본 영상 북마크는 다음 초기값을 사용한다.

```text
startTimeSec = bookmarkTimeSec
endTimeSec = game_video_upload.duration_sec
```

사용자는 분석 패널에서 종료 시간을 다시 조정할 수 있다.

### 12.3 팀 분석 클립 북마크

팀 분석 클립 내부 시간은 원본 경기 영상 시간으로 변환한다.

```text
originalStartTimeSec
= team_video_clip.start_time_sec
+ bookmarkTimeSec
```

종료 시간은 기존 팀 분석 클립의 원본 종료 시간을 사용한다.

```text
originalEndTimeSec
= team_video_clip.end_time_sec
```

분석 패널 초기값은 다음과 같다.

```text
startTimeSec = originalStartTimeSec
endTimeSec = originalEndTimeSec
```

새로운 분석 클립은 기존 생성된 mp4 파일을 다시 자르지 않는다.

항상 원본 경기 영상을 기준으로 생성한다.

### 12.4 선수 개인 분석 클립 북마크

선수 개인 분석 클립 내부 시간은 원본 경기 영상 시간으로 변환한다.

```text
originalStartTimeSec
= player_video_clip.start_time_sec
+ bookmarkTimeSec
```

종료 시간은 기존 선수 개인 분석 클립의 원본 종료 시간을 사용한다.

```text
originalEndTimeSec
= player_video_clip.end_time_sec
```

분석 패널 초기값은 다음과 같다.

```text
startTimeSec = originalStartTimeSec
endTimeSec = originalEndTimeSec
```

새로운 분석 클립은 원본 경기 영상을 기준으로 생성한다.

### 12.5 선수 기록 이벤트

선수 기록 이벤트도 원본 경기 영상 기준 시간을 사용한다.

경기 원본 영상 북마크는 북마크 시간을 그대로 사용한다.

팀·선수 분석 클립 북마크는 기존 클립 시작 시간과 북마크 시간을 합산해 원본 경기 영상 시간으로 변환한다.

초기 이벤트 구간은 다음 기준을 사용한다.

```text
경기 원본 영상 북마크
eventStartTimeSec = bookmarkTimeSec
eventEndTimeSec = 경기 영상 길이

팀 분석 클립 북마크
eventStartTimeSec = teamClip.startTimeSec + bookmarkTimeSec
eventEndTimeSec = teamClip.endTimeSec

선수 개인 분석 클립 북마크
eventStartTimeSec = playerClip.startTimeSec + bookmarkTimeSec
eventEndTimeSec = playerClip.endTimeSec
```

사용자는 선수 기록 이벤트 등록 패널에서 종료 시간을 다시 조정할 수 있다.

---

## 13. 클립 수정과 북마크 처리

### 13.1 시간 구간이 변경된 클립 수정

팀 분석 클립 또는 선수 개인 분석 클립의 다음 값이 변경된 경우 해당 클립에서 생성한 활성 북마크를 모두 소프트 삭제한다.

```text
startTimeSec 변경
endTimeSec 변경
```

처리 기준은 다음과 같다.

```text
클립 시간 구간 변경
→ 연결된 활성 북마크 전체 is_deleted = true
→ 클립 mp4 재생성 처리
```

북마크 삭제는 클립 수정 트랜잭션 안에서 함께 처리한다.

### 13.2 단순 정보 수정

다음 값만 변경된 경우 기존 북마크는 유지한다.

```text
title
comment
drawing
clipType
그 외 영상 시간 구간을 변경하지 않는 값
```

제목, 코멘트, 드로잉만 수정해 같은 영상 파일과 시간 구간이 유지되면 북마크를 삭제하지 않는다.

### 13.3 클립 삭제

팀 분석 클립 또는 선수 개인 분석 클립이 삭제되면 해당 클립에서 생성한 활성 북마크를 모두 소프트 삭제한다.

```text
클립 소프트 삭제
→ 연결 북마크 전체 소프트 삭제
```

북마크 row는 물리 삭제하지 않는다.

### 13.4 프론트 안내 메시지

클립 수정 또는 삭제로 인해 활성 북마크가 삭제된 경우 다음 메시지를 표시한다.

```text
기존 클립이 수정 또는 삭제되어 해당 클립에서 생성한 북마크가 모두 삭제되었습니다.
```

백엔드에서는 실제 삭제된 북마크 수 또는 삭제 여부를 확인할 수 있어야 한다.

기존 클립 수정·삭제 응답 DTO 구조를 확인한 뒤 다음 중 기존 코드와 충돌이 적은 방식으로 전달한다.

* 삭제된 북마크 개수 반환
* 북마크 삭제 여부 반환
* 클립 수정 응답에 북마크 정리 결과 포함

실제 필드명과 응답 구조는 기존 Controller와 DTO를 확인한 후 확정한다.

---

## 14. 원본 경기 영상 삭제 처리

경기 원본 영상이 소프트 삭제되면 연결된 북마크 row는 유지한다.

다만 일반 북마크 목록에서는 조회하지 않는다.

```text
game_video_upload.is_deleted = true
→ 연결된 북마크 row 유지
→ 일반 목록 조회 제외
→ 북마크 수정 불가
→ 북마크를 이용한 분석 작업 불가
```

경기 원본 영상 삭제 시 모든 북마크를 즉시 소프트 삭제하는 방식은 사용하지 않는다.

---

## 15. 조회 제외 조건

다음 북마크는 일반 목록에서 제외한다.

* `video_bookmark.is_deleted = true`
* 작성자가 로그인 사용자와 다른 북마크
* 연결 경기 원본 영상이 삭제된 북마크
* 연결 팀 분석 클립이 삭제된 북마크
* 연결 선수 개인 분석 클립이 삭제된 북마크
* 요청한 현재 재생 영상과 다른 북마크
* DB 연결 조합이 잘못된 북마크

클립이 수정되거나 삭제된 경우 해당 클립의 북마크는 별도 소프트 삭제되므로 목록에 노출되지 않는다.

---

## 16. 예외 상황

다음 예외 상황을 처리한다.

### 16.1 북마크를 찾을 수 없음

```text
요청한 bookmarkId가 없음
또는 이미 삭제됨
```

### 16.2 북마크 소유권 없음

```text
로그인 사용자와 video_bookmark.member_id 불일치
```

### 16.3 북마크 역할 접근 불가

```text
PLAYER가 북마크 API 호출
```

### 16.4 경기 영상 없음

```text
matchVideoId에 해당하는 활성 경기 영상이 없음
```

### 16.5 팀 분석 클립 없음

```text
teamClipId에 해당하는 활성 팀 분석 클립이 없음
```

### 16.6 선수 개인 분석 클립 없음

```text
playerClipId에 해당하는 활성 선수 개인 분석 클립이 없음
```

### 16.7 잘못된 대상 조합

```text
teamClipId와 playerClipId 동시 요청
```

### 16.8 경기 영상 불일치

```text
요청 matchVideoId와
teamClip 또는 playerClip의 upload_id 불일치
```

### 16.9 영상 길이 준비 안 됨

```text
경기 원본 영상 duration_sec 없음
```

### 16.10 잘못된 북마크 시간

```text
bookmarkTimeSec < 0
bookmarkTimeSec > 대상 영상 길이
```

### 16.11 재생 불가능한 클립

```text
PROCESSING
FAILED
UPLOADING
```

실제 `ErrorCode` 이름은 현재 `ErrorCode.java`를 확인한 뒤 기존 명명 규칙에 맞춰 추가한다.

---

## 17. 백엔드 구조 방향

기본 구조는 다음과 같이 분리한다.

```text
VideoBookmarkController
→ API 요청과 응답 담당

VideoBookmarkService
→ 등록, 조회, 수정, 삭제 트랜잭션 담당
→ 클립 수정·삭제 시 북마크 일괄 소프트 삭제 담당

VideoBookmarkValidator
→ 역할, 소유권, 대상 영상 조합, 영상 길이 검증 담당

VideoBookmarkRepository
→ 북마크 조회와 일괄 소프트 삭제 대상 조회 담당

VideoBookmarkEntity
→ video_bookmark 매핑 담당
```

역할이 드러나는 메서드명을 사용한다.

예시는 다음과 같다.

```text
createVideoBookmark()
findMyVideoBookmarks()
updateMyVideoBookmark()
deleteMyVideoBookmark()
validateBookmarkSource()
validateBookmarkTimeWithinVideo()
softDeleteBookmarksByTeamClip()
softDeleteBookmarksByPlayerClip()
```

실제 Repository 메서드명은 구현 전 프로젝트 Repository 작성 방식을 확인한 후 정한다.

---

## 18. 프론트 구조 방향

북마크 관련 프론트 파일은 다음 방향으로 구성한다.

```text
frontend/src/types/videoBookmark.ts
frontend/src/api/videoBookmarkApi.ts
frontend/src/components/bookmark/VideoBookmarkSidebar.tsx
frontend/src/pages/MatchVideoPage.tsx
```

필요하면 등록·수정 폼을 별도 컴포넌트로 분리한다.

```text
frontend/src/components/bookmark/VideoBookmarkForm.tsx
```

북마크 관련 타입은 백엔드 요청·응답 DTO와 정확히 맞춘다.

기존 팀 분석 클립, 선수 개인 분석 클립, 선수 기록 이벤트 패널에는 북마크 초기 시간을 전달할 수 있는 입력 구조를 추가한다.

대상 패널은 다음과 같다.

```text
TeamAnalysisClipEditorPanel.tsx
PlayerAnalysisClipEditorPanel.tsx
PlayerRecordEventEditorPanel.tsx
```

라우트 추가는 필요하지 않다.

---

## 19. API 흐름

### 19.1 등록 흐름

```text
COACH 또는 ANALYST 로그인
→ MatchVideoPage에서 영상 재생
→ 현재 재생 시점으로 북마크 등록
→ 프론트가 현재 영상 종류 자동 판단
→ POST /api/management/video-bookmarks
→ 백엔드 역할 및 대상 영상 검증
→ 현재 영상 길이 검증
→ 작성자 memberId 자동 저장
→ 북마크 생성
→ 현재 영상 북마크 목록 갱신
```

### 19.2 목록 조회 흐름

```text
북마크 목록 버튼 클릭
→ 우측 사이드바 열기
→ 현재 재생 영상 ID 조합으로 목록 API 호출
→ 로그인 사용자의 활성 북마크만 조회
→ 최신 등록순으로 표시
```

### 19.3 북마크 선택 흐름

```text
사이드바 북마크 클릭
→ 현재 영상 bookmarkTimeSec으로 이동
→ 영상 즉시 재생
→ 영상 아래 북마크 정보 표시
→ 분석 작업 버튼 표시
```

### 19.4 분석 작업 연결 흐름

```text
북마크 선택
→ 팀 클립 / 선수 클립 / 기록 이벤트 버튼 클릭
→ 원본 경기 영상 기준 시간으로 환산
→ 기존 analysisMode 패널 표시
→ 시작·종료 시간 초기화
→ 사용자가 세부 정보 수정
→ 기존 생성 API 호출
→ 북마크는 유지
```

### 19.5 수정 흐름

```text
수정 버튼 클릭
→ 제목, 메모, 시간 수정
→ PATCH /api/management/video-bookmarks/{bookmarkId}
→ 소유권 및 시간 재검증
→ 수정 완료
→ 사이드바 목록 갱신
```

### 19.6 삭제 흐름

```text
삭제 버튼 클릭
→ 삭제 확인
→ DELETE /api/management/video-bookmarks/{bookmarkId}
→ 소유권 검증
→ 소프트 삭제
→ 사이드바 목록에서 제거
```

---

## 20. 테스트 항목

### 20.1 권한 테스트

* COACH 본인 북마크 등록 성공
* ANALYST 본인 북마크 등록 성공
* PLAYER 등록 요청 차단
* 다른 사용자의 북마크 조회 불가
* 다른 사용자의 북마크 수정 불가
* 다른 사용자의 북마크 삭제 불가
* ANALYST 본인 북마크 삭제 성공

### 20.2 경기 원본 영상 테스트

* 경기 원본 영상 북마크 등록 성공
* `teamClipId`, `playerClipId`가 모두 null로 저장
* 경기 영상 길이 안의 시간 등록 성공
* 경기 영상 길이 초과 등록 실패
* 영상 길이가 없는 경기 영상 등록 실패

### 20.3 팀 분석 클립 테스트

* 팀 분석 클립 북마크 등록 성공
* `upload_id`, `team_clip_id` 저장
* `player_clip_id` null 저장
* 팀 클립 내부 시간 검증 성공
* 다른 경기의 `matchVideoId` 요청 실패
* PROCESSING 팀 클립 등록 실패
* FAILED 팀 클립 등록 실패
* 삭제된 팀 클립 등록 실패

### 20.4 선수 개인 분석 클립 테스트

* 선수 개인 분석 클립 북마크 등록 성공
* `upload_id`, `player_clip_id` 저장
* `team_clip_id` null 저장
* 선수 클립 내부 시간 검증 성공
* 다른 경기의 `matchVideoId` 요청 실패
* PROCESSING 선수 클립 등록 실패
* FAILED 선수 클립 등록 실패
* 삭제된 선수 클립 등록 실패

### 20.5 대상 조합 테스트

* `teamClipId`, `playerClipId` 동시 전달 실패
* 두 클립 ID가 모두 없으면 경기 원본 북마크로 저장
* 동일 시간 중복 북마크 등록 성공

### 20.6 목록 테스트

* 현재 경기 원본 영상 북마크만 조회
* 현재 팀 분석 클립 북마크만 조회
* 현재 선수 개인 분석 클립 북마크만 조회
* 다른 사용자의 북마크 미노출
* 최신 등록순 정렬 확인
* 삭제된 북마크 미노출

### 20.7 수정·삭제 테스트

* 제목 수정 성공
* 메모 수정 성공
* 시간 수정 성공
* 영상 길이 초과 시간 수정 실패
* 작성자 본인 삭제 성공
* 삭제된 북마크 재수정 실패
* 삭제된 북마크 재삭제 실패

### 20.8 클립 연동 테스트

* 팀 클립 시간 변경 시 연결 북마크 전체 소프트 삭제
* 선수 클립 시간 변경 시 연결 북마크 전체 소프트 삭제
* 팀 클립 제목·코멘트만 수정 시 북마크 유지
* 선수 클립 제목·코멘트만 수정 시 북마크 유지
* 팀 클립 삭제 시 연결 북마크 전체 소프트 삭제
* 선수 클립 삭제 시 연결 북마크 전체 소프트 삭제
* 북마크 삭제 안내 메시지 표시

### 20.9 프론트 테스트

* 북마크 목록 버튼 클릭 시 우측 사이드바 표시
* 현재 재생 영상의 북마크만 표시
* 북마크 클릭 시 시간 이동
* 시간 이동 후 즉시 재생
* 북마크 상세 정보 표시
* 수정 버튼 동작
* 삭제 버튼 동작
* 팀 분석 클립 등록 패널 시간 초기화
* 선수 개인 분석 클립 등록 패널 시간 초기화
* 선수 기록 이벤트 패널 시간 초기화
* 분석 등록 후 북마크 유지
* PLAYER UI 미노출

---

## 21. 구현 순서

1. `video_bookmark` DB 구조 변경
2. `VideoBookmarkEntity` 추가
3. `VideoBookmarkRepository` 추가
4. 북마크 요청·응답 DTO 추가
5. `VideoBookmarkValidator` 구현
6. `VideoBookmarkService` 등록·조회·수정·삭제 구현
7. `VideoBookmarkController` API 구현
8. `SecurityConfig` 북마크 API 권한 설정
9. `ErrorCode` 북마크 예외 추가
10. 팀 분석 클립 시간 변경 시 북마크 일괄 소프트 삭제 연동
11. 선수 개인 분석 클립 시간 변경 시 북마크 일괄 소프트 삭제 연동
12. 팀·선수 분석 클립 삭제 시 북마크 일괄 소프트 삭제 연동
13. 백엔드 API 테스트
14. 프론트 북마크 타입과 API 추가
15. 우측 북마크 사이드바 컴포넌트 구현
16. `MatchVideoPage` 북마크 등록·조회·선택·수정·삭제 연동
17. 분석 패널 북마크 시간 초기값 연동
18. 클립 수정·삭제 북마크 정리 안내 메시지 연동
19. 프론트 빌드 및 통합 테스트
20. 최종 요구사항 문서와 정책 문서 갱신

---

## 22. 초기 구현 제외 범위

초기 구현에서는 다음 기능을 제외한다.

* PLAYER 북마크 조회
* 북마크 공유
* 북마크 공개/비공개 설정
* 다중 북마크 선택
* 북마크 기반 클립 일괄 생성 API
* 북마크 자동 삭제
* 북마크 완료 상태
* 북마크 카테고리
* 북마크 태그
* 소수점 이하 시간 저장
* 프레임 단위 북마크
* 삭제 북마크 복구 화면
* 별도 북마크 관리 페이지

---

## 23. 추후 확장 가능성

추후 다음 기능으로 확장할 수 있다.

* 여러 북마크 선택 후 팀 분석 클립 일괄 생성
* 여러 북마크 선택 후 선수 개인 분석 클립 일괄 생성
* 북마크 처리 완료 상태
* 북마크 태그와 분류
* 북마크 검색
* 북마크 시간순 정렬
* 북마크 드래그 정렬
* 키보드 단축키 등록
* 소수점 이하 정밀 시간 저장
* 프레임 단위 북마크
* 북마크 공유 기능
* 북마크를 분석 작업 큐로 전환
* 북마크와 선수 기록 이벤트 자동 연결
* AI 분석 결과에서 북마크 자동 생성

---

## 24. 운영상 주의사항

북마크 소유권은 프론트 UI가 아니라 백엔드에서 반드시 검증한다.

클립의 시간 구간 변경과 북마크 소프트 삭제는 같은 트랜잭션에서 처리해 데이터 불일치를 방지한다.

경기 원본 영상이나 클립이 삭제된 경우 FK 데이터는 유지하되 일반 조회와 수정에서는 제외한다.

현재 개발 환경의 `/uploads/**` 직접 접근 구조는 운영 전 권한 검증 스트리밍 API 또는 Signed URL 방식으로 변경해야 한다.

북마크 시간은 현재 정수 초지만, 경기 영상과 분석 클립 전체 시간 정책이 추후 소수점 단위로 변경될 가능성을 고려해 시간 검증 로직을 한 곳에 집중시킨다.
