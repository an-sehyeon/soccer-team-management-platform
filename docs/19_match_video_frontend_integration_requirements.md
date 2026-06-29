# 19. 경기 영상 프론트 연동 요구사항

## 1. 결론

경기 영상 프론트 연동 기능은 백엔드에 구현된 경기 영상 파일 업로드 API와 조회 API를 React 화면에 연결해, 지도자/분석관은 실제 `.mp4` 경기 영상을 업로드하고 모든 로그인 사용자는 등록된 경기 영상을 조회 및 재생할 수 있게 하는 기능이다.

이번 구현 기준은 다음과 같다.

* 프론트는 React + TypeScript + Vite를 사용한다.
* 인증은 기존 로그인/회원가입 프론트 구조를 사용한다.
* Access Token은 `localStorage`에 저장하고, `axiosInstance`에서 Authorization 헤더에 자동으로 추가한다.
* 경기 영상 화면 경로는 `/match-videos`를 사용한다.
* 라우트 경로는 문자열로 직접 작성하지 않고 `ROUTES.MATCH_VIDEO` 상수를 사용한다.
* `ROUTES` 상수는 `frontend/src/components/constants/routes.ts`에서 관리한다.
* 모든 로그인 사용자는 경기 영상 목록과 상세를 조회할 수 있다.
* `COACH`, `ANALYST`는 경기 영상 업로드와 메타데이터 수정 UI를 볼 수 있다.
* `COACH`만 경기 영상 삭제 UI를 볼 수 있다.
* `PLAYER`는 조회와 재생 UI만 볼 수 있다.
* 버튼 숨김은 사용자 경험을 위한 처리이며, 실제 권한 검증은 백엔드에서 처리한다.
* 경기 영상 등록 요청은 `multipart/form-data` 방식으로 전송한다.
* 영상 파일은 `videoFile` key로 전송한다.
* 프론트는 영상 길이를 DB 저장 기준으로 계산하지 않는다.
* 영상 길이 `durationSec`은 백엔드가 `ffprobe`로 추출해 응답한 값을 사용한다.
* 영상 재생은 상세 조회 응답의 `url`을 `<video>` 태그에 연결한다.
* 백엔드가 상대 경로 `/uploads/match-videos/{storedFileName}`을 반환하면 프론트에서 API base URL과 결합해 재생 URL을 만든다.
* 프론트 파일 상단에는 해당 파일 역할을 설명하는 한 줄 주석을 작성한다.
* `FormEvent`는 사용하지 않고 form submit 이벤트는 `{ preventDefault: () => void }` 타입으로 작성한다.

---

## 2. 기능 목적

경기 영상 프론트 연동의 목적은 실제 경기 영상을 화면에서 업로드하고, 업로드된 영상을 목록/상세/재생 흐름으로 확인할 수 있게 하는 것이다.

이 기능은 이후 영상 편집기, 팀 분석 클립, 선수 개인 분석 클립의 기준 화면이 된다.

따라서 초기 프론트 구현에서는 다음 기준을 우선한다.

* 지도자와 분석관이 PC에서 경기 영상을 쉽게 업로드할 수 있어야 한다.
* 선수는 모바일에서 경기 영상을 쉽게 조회하고 재생할 수 있어야 한다.
* 업로드 요청은 실제 파일 업로드 방식으로 처리한다.
* 업로드 성공 후 목록을 갱신한다.
* 상세 조회에서 영상 URL과 영상 길이를 확인할 수 있어야 한다.
* 영상 길이는 프론트에서 계산하지 않고 백엔드 응답값을 사용한다.
* 권한별 버튼 노출은 프론트에서 처리하되, 실제 차단은 백엔드 권한 검증에 맡긴다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

가능한 기능은 다음과 같다.

* 경기 영상 목록 조회
* 경기 영상 상세 조회
* 경기 영상 재생
* 경기 영상 파일 업로드
* 경기 영상 메타데이터 수정
* 경기 영상 삭제

화면에서는 다음 UI가 노출된다.

* 경기 영상 목록
* 경기 영상 상세 정보
* 경기 영상 재생 영역
* 경기 영상 업로드 폼
* 경기 메타데이터 수정 폼
* 삭제 버튼

### 3.2 분석관 `ANALYST`

가능한 기능은 다음과 같다.

* 경기 영상 목록 조회
* 경기 영상 상세 조회
* 경기 영상 재생
* 경기 영상 파일 업로드
* 경기 영상 메타데이터 수정

노출하지 않는 UI는 다음과 같다.

* 삭제 버튼

### 3.3 선수 `PLAYER`

가능한 기능은 다음과 같다.

* 경기 영상 목록 조회
* 경기 영상 상세 조회
* 경기 영상 재생

노출하지 않는 UI는 다음과 같다.

* 경기 영상 업로드 폼
* 경기 메타데이터 수정 폼
* 삭제 버튼

---

## 4. 프론트 권한 노출 정책

| 역할        | 목록 조회 | 상세 조회 | 재생 | 업로드 UI | 수정 UI | 삭제 UI |
| --------- | ----: | ----: | -: | -----: | ----: | ----: |
| `COACH`   |    가능 |    가능 | 가능 |     노출 |    노출 |    노출 |
| `ANALYST` |    가능 |    가능 | 가능 |     노출 |    노출 |   미노출 |
| `PLAYER`  |    가능 |    가능 | 가능 |    미노출 |   미노출 |   미노출 |

권한 처리 기준은 다음과 같다.

* `/match-videos` 라우트는 로그인 사용자라면 접근 가능하다.
* `/match-videos` 라우트를 `COACH` 전용 라우트로 막지 않는다.
* 화면 내부에서 로그인 사용자의 `memberRole`을 확인한다.
* `COACH`, `ANALYST`인 경우에만 업로드/수정 관련 UI를 렌더링한다.
* `COACH`인 경우에만 삭제 UI를 렌더링한다.
* `PLAYER`는 조회와 재생 화면만 사용한다.
* 프론트에서 버튼이 보이지 않더라도 API 직접 호출은 가능할 수 있으므로 백엔드에서 다시 권한을 검증한다.

---

## 5. 화면 흐름

## 5.1 공통 조회 흐름

1. 사용자가 로그인한다.
2. 경기 영상 메뉴에 진입한다.
3. `/api/match-videos` 목록 조회 API를 호출한다.
4. 경기 영상 목록을 최신순으로 표시한다.
5. 사용자가 경기 영상을 선택한다.
6. `/api/match-videos/{matchVideoId}` 상세 조회 API를 호출한다.
7. 상세 정보와 영상 플레이어를 표시한다.

## 5.2 지도자/분석관 업로드 흐름

1. `COACH` 또는 `ANALYST`가 경기 영상 화면에 진입한다.
2. 경기 영상 업로드 폼을 확인한다.
3. `.mp4` 파일을 선택한다.
4. 경기 제목, 경기일시, 장소, 점수, 경기 결과를 입력한다.
5. 저장 버튼을 누른다.
6. 프론트는 `FormData`를 생성한다.
7. `videoFile`, `title`, `gameDate`, `place`, `homeScore`, `awayScore`, `matchResult`를 담아 전송한다.
8. 백엔드가 파일을 저장하고 `ffprobe`로 영상 길이를 추출한다.
9. 업로드 성공 응답을 받으면 목록을 갱신한다.
10. 상세 조회에서 재생 URL과 `durationSec`을 확인한다.

## 5.3 영상 재생 흐름

1. 상세 조회 응답의 `url`을 가져온다.
2. `url`이 `http`로 시작하면 그대로 사용한다.
3. `url`이 `/uploads/...` 형태의 상대 경로이면 API base URL과 결합한다.
4. 결합된 URL을 `<video controls>`의 `src`에 연결한다.
5. `durationSec`은 백엔드 응답값을 화면에 표시한다.

---

## 6. API 연동 방향

## 6.1 경기 영상 목록 조회

```http
GET /api/match-videos?page=0&size=20
```

응답 타입은 `MatchVideoPageResponse`로 관리한다.

목록 아이템 필드는 다음과 같다.

* `matchVideoId`
* `title`
* `gameDate`
* `place`
* `homeScore`
* `awayScore`
* `matchResult`
* `status`
* `durationSec`
* `createdAt`

## 6.2 경기 영상 상세 조회

```http
GET /api/match-videos/{matchVideoId}
```

상세 응답 필드는 다음과 같다.

* `matchVideoId`
* `url`
* `title`
* `gameDate`
* `place`
* `homeScore`
* `awayScore`
* `matchResult`
* `status`
* `durationSec`
* `uploaderId`
* `uploaderName`
* `createdAt`
* `updatedAt`

## 6.3 경기 영상 업로드

```http
POST /api/management/match-videos
Content-Type: multipart/form-data
```

요청 필드는 다음과 같다.

| Key           | Type | 설명                    |
| ------------- | ---- | --------------------- |
| `videoFile`   | File | `.mp4` 경기 영상 파일       |
| `title`       | Text | 경기 제목                 |
| `gameDate`    | Text | ISO 형식 경기 일시          |
| `place`       | Text | 경기 장소                 |
| `homeScore`   | Text | 홈팀 점수                 |
| `awayScore`   | Text | 원정팀 점수                |
| `matchResult` | Text | `WIN`, `DRAW`, `LOSS` |

프론트 구현 기준은 다음과 같다.

* `FormData`를 사용한다.
* `videoFile` key 이름은 백엔드와 반드시 동일하게 맞춘다.
* `Content-Type`은 브라우저와 axios가 boundary를 포함해 자동 설정하도록 둔다.
* 업로드 중에는 중복 클릭을 막는다.
* 업로드 성공 후 목록을 다시 조회한다.
* 업로드 실패 시 백엔드 에러 메시지를 화면에 표시한다.

## 6.4 경기 영상 메타데이터 수정

```http
PATCH /api/management/match-videos/{matchVideoId}
```

수정 요청 필드는 다음과 같다.

* `title`
* `gameDate`
* `place`
* `homeScore`
* `awayScore`
* `matchResult`

수정 제외 필드는 다음과 같다.

* `videoFile`
* `url`
* `durationSec`
* `status`

## 6.5 경기 영상 삭제

```http
DELETE /api/coach/match-videos/{matchVideoId}
```

삭제 UI는 `COACH`에게만 노출한다.

삭제 성공 후 목록과 상세 선택 상태를 갱신한다.

---

## 7. 프론트 파일 설계 방향

생성 또는 수정 예상 파일은 다음과 같다.

```text
frontend/src/components/constants/routes.ts
frontend/src/App.tsx
frontend/src/layouts/AuthenticatedLayout.tsx
frontend/src/types/matchVideo.ts
frontend/src/api/matchVideoApi.ts
frontend/src/pages/MatchVideoPage.tsx
frontend/src/utils/videoUrl.ts
```

## 7.1 `routes.ts`

`ROUTES.MATCH_VIDEO`를 추가한다.

```ts
MATCH_VIDEO: "/match-videos"
```

## 7.2 `types/matchVideo.ts`

역할은 경기 영상 요청/응답 타입 관리다.

필요 타입은 다음과 같다.

* `MatchResult`
* `VideoUploadStatus`
* `MatchVideoListItem`
* `MatchVideoDetail`
* `MatchVideoPageResponse`
* `CreateMatchVideoRequest`
* `UpdateMatchVideoRequest`

## 7.3 `matchVideoApi.ts`

역할은 경기 영상 백엔드 API 호출이다.

필요 함수는 다음과 같다.

* `getMatchVideos(page, size)`
* `getMatchVideoDetail(matchVideoId)`
* `createMatchVideo(request)`
* `updateMatchVideo(matchVideoId, request)`
* `deleteMatchVideo(matchVideoId)`

## 7.4 `videoUrl.ts`

역할은 백엔드가 반환한 영상 URL을 프론트 재생 가능한 URL로 변환하는 것이다.

처리 기준은 다음과 같다.

* `http://` 또는 `https://`로 시작하면 그대로 반환한다.
* `/uploads/...`로 시작하면 `import.meta.env.VITE_API_BASE_URL`과 결합한다.
* 빈 값이면 빈 문자열을 반환한다.

## 7.5 `MatchVideoPage.tsx`

역할은 경기 영상 목록, 상세, 업로드, 수정, 삭제, 재생 UI를 제공하는 것이다.

구현 기준은 다음과 같다.

* 파일 상단에 한 줄 역할 주석을 작성한다.
* `FormEvent`는 사용하지 않는다.
* submit 이벤트는 `{ preventDefault: () => void }` 타입으로 작성한다.
* 선수 모바일 사용성을 고려해 단순한 카드형 목록을 사용한다.
* 지도자/분석관 PC 사용성을 고려해 업로드 폼과 상세 영역을 함께 배치한다.
* 드로잉 메뉴는 추가하지 않는다.

---

## 8. 예외 처리

| 상황               | 프론트 처리                      |
| ---------------- | --------------------------- |
| 목록 조회 실패         | 에러 메시지 표시                   |
| 상세 조회 실패         | 에러 메시지 표시                   |
| 업로드 성공           | 목록 갱신, 폼 초기화                |
| 업로드 실패           | 백엔드 에러 메시지 표시               |
| 파일 미선택           | 프론트에서 먼저 안내                 |
| `.mp4`가 아닌 파일 선택 | 프론트에서 먼저 안내, 백엔드에서도 검증      |
| 200MB 초과 파일 선택   | 프론트에서 먼저 안내, 백엔드에서도 검증      |
| 영상 길이 추출 실패      | 백엔드 에러 메시지 표시               |
| 수정 성공            | 목록/상세 갱신                    |
| 삭제 성공            | 목록 갱신, 상세 선택 해제             |
| PLAYER 업로드 시도    | UI 미노출, API 직접 호출은 백엔드에서 차단 |

---

## 9. 구현 순서

1. `ROUTES.MATCH_VIDEO` 추가
2. 인증 레이아웃 메뉴에 경기 영상 메뉴 추가
3. `types/matchVideo.ts` 작성
4. `api/matchVideoApi.ts` 작성
5. `utils/videoUrl.ts` 작성
6. `MatchVideoPage.tsx` 작성
7. 목록 조회 연동
8. 상세 조회 연동
9. 영상 재생 연동
10. `COACH`, `ANALYST` 업로드 폼 노출
11. `FormData` 업로드 연동
12. 백엔드 응답의 `durationSec` 표시
13. 메타데이터 수정 연동
14. `COACH` 삭제 연동
15. 역할별 UI 노출 테스트
16. 모바일/PC 반응형 기본 점검

---

## 10. 테스트 범위

* 로그인 사용자의 경기 영상 메뉴 접근 성공
* `COACH` 경기 영상 목록 조회 성공
* `ANALYST` 경기 영상 목록 조회 성공
* `PLAYER` 경기 영상 목록 조회 성공
* 경기 영상 상세 조회 성공
* 상세 화면에서 영상 재생 성공
* 상세 화면에서 `durationSec` 표시 확인
* `COACH` 업로드 UI 노출 확인
* `ANALYST` 업로드 UI 노출 확인
* `PLAYER` 업로드 UI 미노출 확인
* `COACH` mp4 업로드 성공
* `ANALYST` mp4 업로드 성공
* 업로드 성공 후 목록 갱신 확인
* `.mp4`가 아닌 파일 선택 시 안내 확인
* `COACH` 수정 UI 노출 확인
* `ANALYST` 수정 UI 노출 확인
* `PLAYER` 수정 UI 미노출 확인
* `COACH` 삭제 UI 노출 확인
* `ANALYST` 삭제 UI 미노출 확인
* `PLAYER` 삭제 UI 미노출 확인
* 모바일 화면에서 목록/재생 확인
* PC 화면에서 업로드/상세 확인

---

## 11. 주의사항

* 프론트의 권한 처리는 UI 노출 제어만 담당한다.
* 실제 권한 차단은 백엔드 API에서 처리한다.
* 영상 길이 `durationSec`은 프론트에서 계산해 서버에 보내지 않는다.
* `durationSec`은 백엔드가 `ffprobe`로 추출한 값을 사용한다.
* `Content-Type: multipart/form-data`를 수동으로 강제하면 boundary 문제가 생길 수 있으므로 axios가 자동 처리하도록 둔다.
* 백엔드가 반환한 상대 URL은 API base URL과 결합해야 한다.
* 영상 파일은 Git에 포함하지 않는다.
* 초기에는 썸네일, 인코딩, 실제 클립 파일 생성은 구현하지 않는다.
* 초기에는 원본 영상 URL과 클립 시작/종료 시간 메타데이터를 기준으로 클립을 관리한다.
