# 스케줄 관리 기능 요구사항

## 1. 결론

스케줄 관리 기능은 지도자가 팀 일정을 등록, 수정, 삭제하고 선수와 분석관이 일정을 조회하는 기능이다.

최종 구현은 다음 정책을 기준으로 정리한다.

- 인증은 이미 구현된 JWT 인증 구조를 사용한다.
- JWT 인증 후 `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 값을 사용한다.
- 지도자 `COACH`는 스케줄 등록, 수정, 삭제, 조회가 가능하다.
- 선수 `PLAYER`는 스케줄 조회만 가능하다.
- 분석관 `ANALYST`는 스케줄 조회만 가능하다.
- 관리자 여부 `isAdmin`은 스케줄 권한의 기본 기준으로 사용하지 않는다.
- 삭제는 실제 DB 삭제가 아니라 `is_deleted = true`로 처리하는 소프트 삭제를 사용한다.
- 초기 버전은 단일 팀 서비스 기준으로 설계한다.
- 현재 DB의 `schedule` 테이블 구조를 우선 사용하고, 별도 `title` 컬럼은 추가하지 않는다.
- API 요청/응답 JSON 필드명은 `scheduleDateTime`으로 통일한다.
- DB 컬럼명은 기존 `schedule_datetime`을 유지한다.
- 현재 백엔드 Entity/Repository 내부 필드명은 기존 구현 기준인 `scheduleDatetime`을 유지할 수 있다.
- 프론트 타입과 API 응답 필드명은 `scheduleDateTime`을 사용한다.
- 스케줄 목록 조회는 `startDate`, `endDate`를 `yyyy-MM-dd` 형식으로 받는다.
- 백엔드는 조회 기간을 `LocalDate`로 받은 뒤 `LocalDateTime` 범위로 변환해서 조회한다.
- 종료일 당일 일정이 누락되지 않도록 `endDate.plusDays(1).atStartOfDay()` 미만 조건을 사용한다.

---

## 2. 기능 목적

스케줄 관리 기능의 목적은 팀 훈련, 경기, 미팅, 행사 등의 일정을 지도자가 등록하고 팀 구성원이 확인할 수 있게 하는 것이다.

이 기능은 선수들이 모바일에서 가장 자주 확인할 가능성이 높은 운영 기능이다.

따라서 복잡한 관리 기능보다 다음 기준을 우선한다.

- 선수는 오늘/이번 주 일정을 빠르게 확인할 수 있어야 한다.
- 지도자는 PC 또는 모바일에서 일정을 쉽게 등록할 수 있어야 한다.
- 권한 없는 사용자가 스케줄을 변경할 수 없어야 한다.
- 삭제된 스케줄은 조회 목록에 노출되지 않아야 한다.
- 스케줄 조회 기간 처리에서 종료일 당일 일정이 누락되지 않아야 한다.
- 추후 반복 일정, 출석 체크, 일정 알림으로 확장할 수 있어야 한다.

---

## 3. 사용자 역할

### 3.1 지도자 `COACH`

지도자는 팀 운영 책임자이므로 스케줄을 관리할 수 있다.

가능한 기능은 다음과 같다.

- 스케줄 등록
- 스케줄 수정
- 스케줄 삭제
- 스케줄 목록 조회
- 스케줄 상세 조회

### 3.2 선수 `PLAYER`

선수는 팀 일정을 확인하는 사용자이다.

가능한 기능은 다음과 같다.

- 스케줄 목록 조회
- 스케줄 상세 조회

불가능한 기능은 다음과 같다.

- 스케줄 등록
- 스케줄 수정
- 스케줄 삭제

### 3.3 분석관 `ANALYST`

분석관은 영상 분석 업무를 담당하지만 팀 운영 일정 변경 권한은 가지지 않는다.

가능한 기능은 다음과 같다.

- 스케줄 목록 조회
- 스케줄 상세 조회

불가능한 기능은 다음과 같다.

- 스케줄 등록
- 스케줄 수정
- 스케줄 삭제

---

## 4. 권한 정책

## 4.1 권한 표

| 역할 | 등록 | 수정 | 삭제 | 목록 조회 | 상세 조회 |
|---|---:|---:|---:|---:|---:|
| `COACH` | 가능 | 가능 | 가능 | 가능 | 가능 |
| `PLAYER` | 불가 | 불가 | 불가 | 가능 | 가능 |
| `ANALYST` | 불가 | 불가 | 불가 | 가능 | 가능 |

## 4.2 권한 검증 기준

스케줄 권한 검증은 반드시 백엔드에서 처리한다.

프론트에서 버튼을 숨기더라도 API를 직접 호출할 수 있으므로 서비스 계층 또는 공통 권한 검증 메서드에서 다시 확인한다.

검증 기준은 다음과 같다.

- 인증되지 않은 사용자는 모든 스케줄 API에 접근할 수 없다.
- `memberRole = COACH`인 회원만 등록, 수정, 삭제할 수 있다.
- `memberRole = PLAYER`인 회원은 조회만 가능하다.
- `memberRole = ANALYST`인 회원은 조회만 가능하다.
- 삭제된 스케줄은 수정, 삭제, 상세 조회 대상이 될 수 없다.
- 프론트에서 등록/수정/삭제 버튼이 보이지 않더라도 백엔드 권한 검증은 반드시 유지한다.

## 4.3 관리자 `isAdmin` 처리

초기 스케줄 기능에서는 `isAdmin = true`만으로 스케줄 등록/수정/삭제 권한을 주지 않는다.

이유는 다음과 같다.

- `isAdmin`은 가입 승인 같은 운영자 권한에 가깝다.
- 스케줄 변경 권한은 팀 운영 역할인 `COACH` 기준으로 판단하는 것이 명확하다.
- 관리자가 분석관 역할일 경우 스케줄 수정까지 허용하면 역할 책임이 애매해질 수 있다.

단, 추후 운영 관리자에게 전체 일정 관리 권한을 줄 필요가 생기면 별도 정책으로 확장한다.

---

## 5. 화면 흐름

## 5.1 선수/분석관 스케줄 조회 화면

선수와 분석관은 모바일 조회 사용성을 우선한다.

기본 화면 흐름은 다음과 같다.

1. 사용자가 로그인한다.
2. 메인 화면 또는 스케줄 메뉴에 진입한다.
3. 스케줄 화면은 기본적으로 현재 월의 첫날과 마지막 날을 조회 기간으로 설정한다.
4. 스케줄 목록 API를 호출한다.
5. 사용자가 조회 기간을 변경하면 해당 기간의 스케줄 목록을 다시 조회한다.
6. 스케줄 항목을 누르면 상세 내용을 확인한다.

조회 화면에 표시할 정보는 다음과 같다.

- 일정 날짜와 시간
- 일정 유형
- 장소
- 작성자 이름
- 운동 강도
- 상세 내용
- 생성일시
- 수정일시

## 5.2 지도자 스케줄 관리 화면

지도자는 PC와 모바일 모두 사용할 수 있지만, 관리 화면은 입력 편의성을 우선한다.

기본 화면 흐름은 다음과 같다.

1. 지도자가 스케줄 관리 화면에 진입한다.
2. 조회 기간 기준 스케줄 목록을 확인한다.
3. 등록 폼에 일정 날짜와 시간, 장소, 일정 유형, 운동 강도, 상세 내용을 입력한다.
4. 저장 버튼을 누른다.
5. 저장 성공 후 목록을 갱신한다.
6. 기존 스케줄의 수정 버튼을 누르면 등록 폼에 기존 값이 채워진다.
7. 수정 저장 후 목록을 갱신한다.
8. 삭제 버튼을 누르면 확인창을 띄운 뒤 삭제 API를 호출한다.
9. 삭제 성공 후 목록을 갱신한다.

## 5.3 프론트 버튼 노출 정책

- `COACH`에게만 등록/수정/삭제 버튼을 보여준다.
- `PLAYER`, `ANALYST`에게는 조회 화면만 보여준다.
- 버튼 숨김은 UX 목적이며, 실제 권한 차단은 백엔드가 담당한다.

---

## 6. API 흐름

## 6.1 API 설계 방향

스케줄 조회 API는 지도자, 선수, 분석관이 모두 사용하므로 공통 API로 둔다.

스케줄 등록/수정/삭제 API는 지도자만 사용할 수 있으므로 지도자 전용 경로로 분리한다.

최종 API는 다음과 같다.

```http
GET    /api/schedules?startDate=2026-06-01&endDate=2026-06-30
GET    /api/schedules/{scheduleId}
POST   /api/coach/schedules
PATCH  /api/coach/schedules/{scheduleId}
DELETE /api/coach/schedules/{scheduleId}
```

## 6.2 스케줄 목록 조회

```http
GET /api/schedules?startDate=2026-06-01&endDate=2026-06-30
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `PLAYER`
- `ANALYST`

요청 파라미터는 다음과 같다.

| 파라미터 | 필수 여부 | 형식 | 설명 |
|---|---:|---|---|
| `startDate` | 필수 | `yyyy-MM-dd` | 조회 시작일 |
| `endDate` | 필수 | `yyyy-MM-dd` | 조회 종료일 |

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH`, `PLAYER`, `ANALYST` 중 하나인지 확인한다.
3. 조회 시작일과 종료일을 `LocalDate`로 받는다.
4. 조회 시작일과 종료일이 비어 있는지 확인한다.
5. 종료일이 시작일보다 빠른지 검증한다.
6. `startDate.atStartOfDay()`로 조회 시작 일시를 만든다.
7. `endDate.plusDays(1).atStartOfDay()`로 조회 종료 일시를 만든다.
8. `schedule_datetime >= 시작 일시` 그리고 `schedule_datetime < 종료 일시` 조건으로 조회한다.
9. `is_deleted = false`인 스케줄만 조회한다.
10. `schedule_datetime` 기준 오름차순으로 정렬한다.
11. 목록 응답을 반환한다.

조회 조건은 다음 기준을 사용한다.

```text
schedule_datetime >= startDate.atStartOfDay()
schedule_datetime < endDate.plusDays(1).atStartOfDay()
```

이유는 `endDate`를 `2026-06-30T00:00:00`으로 직접 처리하면 6월 30일 오후 일정이 누락될 수 있기 때문이다.

응답 예시는 다음과 같다.

```json
[
  {
    "scheduleId": 1,
    "writerMemberId": 5,
    "writerName": "이승원",
    "scheduleDateTime": "2026-06-20T15:00:00",
    "place": "운동장",
    "scheduleType": "TRAINING",
    "comment": "전술 훈련",
    "intensity": "HIGH",
    "createdAt": "2026-06-16T13:16:35.962399",
    "updatedAt": "2026-06-16T13:16:35.962399"
  }
]
```

## 6.3 스케줄 상세 조회

```http
GET /api/schedules/{scheduleId}
```

사용 가능 역할은 다음과 같다.

- `COACH`
- `PLAYER`
- `ANALYST`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 스케줄 ID로 스케줄을 조회한다.
3. 스케줄이 존재하지 않거나 삭제된 경우 실패 처리한다.
4. 상세 정보를 반환한다.

응답 예시는 다음과 같다.

```json
{
  "scheduleId": 1,
  "writerMemberId": 5,
  "writerName": "이승원",
  "scheduleDateTime": "2026-06-20T15:00:00",
  "place": "운동장",
  "scheduleType": "TRAINING",
  "comment": "전술 훈련",
  "intensity": "HIGH",
  "createdAt": "2026-06-16T13:16:35.962399",
  "updatedAt": "2026-06-16T13:16:35.962399"
}
```

## 6.4 스케줄 등록

```http
POST /api/coach/schedules
```

사용 가능 역할은 다음과 같다.

- `COACH`

요청 예시는 다음과 같다.

```json
{
  "scheduleDateTime": "2026-06-20T15:00:00",
  "place": "메인 운동장",
  "scheduleType": "TRAINING",
  "intensity": "HIGH",
  "comment": "전술 훈련 및 미니게임"
}
```

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH`인지 확인한다.
3. 요청 필수값을 검증한다.
4. `scheduleDateTime`이 비어 있지 않은지 확인한다.
5. `scheduleType`이 허용된 Enum 값인지 확인한다.
6. `intensity`가 null이 아니면 허용된 Enum 값인지 확인한다.
7. `member_id`에 등록한 지도자의 회원 ID를 저장한다.
8. 스케줄을 저장한다.
9. 생성된 스케줄 ID를 반환한다.

## 6.5 스케줄 수정

```http
PATCH /api/coach/schedules/{scheduleId}
```

사용 가능 역할은 다음과 같다.

- `COACH`

요청 필드 기준은 등록 요청과 동일하다.

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH`인지 확인한다.
3. 스케줄 ID로 스케줄을 조회한다.
4. 스케줄이 존재하지 않거나 삭제된 경우 실패 처리한다.
5. 요청 값을 검증한다.
6. 스케줄 정보를 수정한다.
7. 수정된 스케줄 정보를 반환한다.

## 6.6 스케줄 삭제

```http
DELETE /api/coach/schedules/{scheduleId}
```

사용 가능 역할은 다음과 같다.

- `COACH`

처리 흐름은 다음과 같다.

1. JWT 인증 정보를 확인한다.
2. 로그인 회원의 역할이 `COACH`인지 확인한다.
3. 스케줄 ID로 스케줄을 조회한다.
4. 스케줄이 존재하지 않거나 이미 삭제된 경우 실패 처리한다.
5. `is_deleted = true`로 변경한다.
6. 삭제 완료 응답을 반환한다.

---

## 7. DB 설계 방향

## 7.1 현재 사용할 테이블

초기 구현에서는 현재 DB의 `schedule` 테이블을 그대로 사용한다.

| 컬럼 | 사용 목적 |
|---|---|
| `id` | 스케줄 PK |
| `member_id` | 스케줄 등록 회원 ID |
| `schedule_datetime` | 스케줄 날짜와 시간 |
| `place` | 장소 |
| `schedule_type` | 일정 유형 |
| `comment` | 스케줄 상세 내용 |
| `intensity` | 훈련 강도 |
| `is_deleted` | 소프트 삭제 여부 |
| `created_at` | 생성일시 |
| `updated_at` | 수정일시 |

## 7.2 Entity 매핑 방향

`schedule` 테이블은 `ScheduleEntity`로 매핑한다.

현재 구현 기준 필드명은 다음과 같다.

| DB 컬럼 | Entity 필드명 | API JSON 필드명 |
|---|---|---|
| `id` | `id` | `scheduleId` |
| `member_id` | `member` | `writerMemberId`, `writerName` |
| `schedule_datetime` | `scheduleDatetime` | `scheduleDateTime` |
| `place` | `place` | `place` |
| `schedule_type` | `scheduleType` | `scheduleType` |
| `comment` | `comment` | `comment` |
| `intensity` | `intensity` | `intensity` |
| `is_deleted` | `isDeleted` | 응답 제외 |
| `created_at` | `createdAt` | `createdAt` |
| `updated_at` | `updatedAt` | `updatedAt` |

정리하면 다음과 같다.

```text
DB 컬럼명: schedule_datetime
Entity 필드명: scheduleDatetime
DTO 필드명: scheduleDateTime
프론트 타입 필드명: scheduleDateTime
JSON 필드명: scheduleDateTime
```

Entity 내부 필드명은 기존 구현을 유지할 수 있지만, API 외부 요청/응답은 `scheduleDateTime`으로 통일한다.

## 7.3 일정 유형

`schedule_type`은 Enum으로 관리한다.

초기 허용 값은 다음과 같다.

| 값 | 의미 |
|---|---|
| `TRAINING` | 훈련 |
| `MATCH` | 경기 |
| `MEETING` | 미팅 |
| `EVENT` | 팀 행사 |
| `EXTERNAL` | 외부 일정 |
| `ETC` | 기타 |

## 7.4 운동 강도

`intensity`는 Enum으로 관리한다.

초기 허용 값은 다음과 같다.

| 값 | 의미 |
|---|---|
| `HIGH` | 높음 |
| `MEDIUM` | 보통 |
| `LOW` | 낮음 |

운동 강도는 모든 일정에 필수는 아니다.

예를 들어 미팅, 외부 일정, 팀 행사는 운동 강도가 없을 수 있다.

## 7.5 별도 title 컬럼 추가 여부

현재 `schedule` 테이블에는 일정 제목 컬럼이 없다.

초기 구현에서는 DB 변경을 줄이기 위해 별도 `title` 컬럼을 추가하지 않는다.

대신 다음 방식으로 화면에 표시한다.

- 일정 유형: `scheduleType`
- 일정 상세 설명: `comment`

예시:

```text
TRAINING / 전술 훈련 및 미니게임
MATCH / 연습경기 vs OO고
```

추후 일정 제목이 꼭 필요해지면 `title` 컬럼 추가를 검토한다.

---

## 8. 요청/응답 DTO 방향

## 8.1 등록 요청 DTO

`CreateScheduleRequestDTO`

필드 방향은 다음과 같다.

- `scheduleDateTime`
- `place`
- `scheduleType`
- `intensity`
- `comment`

주의사항은 다음과 같다.

- JSON 필드명은 반드시 `scheduleDateTime`을 사용한다.
- `scheduleDatetime`으로 받지 않는다.
- `scheduleDateTime`은 `LocalDateTime`으로 매핑한다.

## 8.2 수정 요청 DTO

`UpdateScheduleRequestDTO`

초기 구현에서는 부분 수정 방식보다 전체 수정 방식에 가깝게 처리한다.

필드 방향은 다음과 같다.

- `scheduleDateTime`
- `place`
- `scheduleType`
- `intensity`
- `comment`

주의사항은 다음과 같다.

- JSON 필드명은 반드시 `scheduleDateTime`을 사용한다.
- 등록 요청 DTO와 같은 필드 기준을 사용한다.

## 8.3 응답 DTO

`ScheduleResponseDTO`

필드 방향은 다음과 같다.

- `scheduleId`
- `writerMemberId`
- `writerName`
- `scheduleDateTime`
- `place`
- `scheduleType`
- `intensity`
- `comment`
- `createdAt`
- `updatedAt`

주의사항은 다음과 같다.

- 작성자 정보는 `member_id`에 연결된 회원 정보를 기준으로 내려준다.
- `scheduleDatetime`이 아니라 `scheduleDateTime`으로 응답한다.
- 프론트 `ScheduleResponse` 타입과 응답 필드명을 맞춘다.

---

## 9. Repository 설계 방향

최종 구현에서는 기간 조회 시 `Between`보다 시작 이상, 종료 미만 조건을 사용한다.

권장 Repository 메서드는 다음과 같다.

```java
List<ScheduleEntity> findByScheduleDatetimeGreaterThanEqualAndScheduleDatetimeLessThanAndIsDeletedFalseOrderByScheduleDatetimeAsc(
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
);
```

이 메서드는 다음 조건으로 조회한다.

```text
schedule_datetime >= startDateTime
schedule_datetime < endDateTime
is_deleted = false
ORDER BY schedule_datetime ASC
```

`Between`을 사용하지 않는 이유는 다음과 같다.

- `endDate`를 당일 00시로 해석하면 종료일 오후 일정이 누락될 수 있다.
- 종료일 다음날 00시 미만 조건이 날짜 범위 조회에 더 안전하다.
- 월간 조회, 주간 조회, 일간 조회 모두 같은 방식으로 처리할 수 있다.

---

## 10. 예외 상황

| 상황 | 처리 방식 | 권장 HTTP 상태 |
|---|---|---|
| JWT 없음 | 인증 실패 | `401 Unauthorized` |
| JWT 만료 | 인증 실패 | `401 Unauthorized` |
| 선수의 등록 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 수정 API 호출 | 접근 차단 | `403 Forbidden` |
| 선수의 삭제 API 호출 | 접근 차단 | `403 Forbidden` |
| 분석관의 등록 API 호출 | 접근 차단 | `403 Forbidden` |
| 분석관의 수정 API 호출 | 접근 차단 | `403 Forbidden` |
| 분석관의 삭제 API 호출 | 접근 차단 | `403 Forbidden` |
| 존재하지 않는 스케줄 조회 | 조회 실패 | `404 Not Found` |
| 삭제된 스케줄 조회 | 조회 실패 | `404 Not Found` |
| 필수 입력값 누락 | 요청 실패 | `400 Bad Request` |
| 허용되지 않은 일정 유형 | 요청 실패 | `400 Bad Request` |
| 허용되지 않은 운동 강도 | 요청 실패 | `400 Bad Request` |
| 조회 시작일 또는 종료일 누락 | 요청 실패 | `400 Bad Request` |
| 조회 시작일이 종료일보다 늦음 | 요청 실패 | `400 Bad Request` |
| 장소 길이 초과 | 요청 실패 | `400 Bad Request` |
| 상세 내용 길이 초과 | 요청 실패 | `400 Bad Request` |

---

## 11. 구현 순서

## 11.1 백엔드 1단계: 기본 구조 확인

1. `ScheduleEntity` 필드와 DB 컬럼 매핑 확인
2. `ScheduleTypeEnum` 확인 또는 생성
3. `ScheduleIntensityEnum` 확인 또는 생성
4. `ScheduleRepository` 생성
5. 기존 공통 예외 구조와 에러 코드 확인

## 11.2 백엔드 2단계: DTO 작성

1. `CreateScheduleRequestDTO` 작성
2. `UpdateScheduleRequestDTO` 작성
3. `ScheduleResponseDTO` 작성
4. JSON 필드명을 `scheduleDateTime`으로 통일
5. 응답 DTO에 `writerMemberId`, `writerName` 포함
6. 날짜 범위 조회용 요청 파라미터 정책 정리

## 11.3 백엔드 3단계: 권한 검증 로직 작성

1. `CustomUserPrincipal`에서 `memberId`, `memberRole`, `isAdmin` 사용
2. 스케줄 변경 권한 검증 메서드 작성
3. `COACH`가 아니면 등록/수정/삭제 실패 처리
4. 조회는 `COACH`, `PLAYER`, `ANALYST` 허용

## 11.4 백엔드 4단계: Service 작성

1. 스케줄 등록 기능 작성
2. 스케줄 목록 조회 기능 작성
3. 목록 조회 시 `LocalDate`를 `LocalDateTime` 범위로 변환
4. 조회 범위는 시작일 00시 이상, 종료일 다음날 00시 미만으로 처리
5. 스케줄 상세 조회 기능 작성
6. 스케줄 수정 기능 작성
7. 스케줄 삭제 기능 작성

## 11.5 백엔드 5단계: Controller 작성

1. 공통 조회 Controller 작성
2. 목록 조회는 `startDate`, `endDate`를 `LocalDate`로 받음
3. `@RequestParam(name = "startDate")`와 `@RequestParam(name = "endDate")`를 명시
4. `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)`를 사용
5. 지도자 전용 등록 Controller 작성
6. 지도자 전용 수정 Controller 작성
7. 지도자 전용 삭제 Controller 작성

## 11.6 백엔드 6단계: 테스트

1. 지도자 스케줄 등록 성공 테스트
2. 선수 스케줄 등록 실패 테스트
3. 분석관 스케줄 등록 실패 테스트
4. 지도자 스케줄 수정 성공 테스트
5. 선수 스케줄 수정 실패 테스트
6. 분석관 스케줄 수정 실패 테스트
7. 지도자 스케줄 삭제 성공 테스트
8. 선수 스케줄 삭제 실패 테스트
9. 분석관 스케줄 삭제 실패 테스트
10. 전체 역할 스케줄 조회 성공 테스트
11. 삭제된 스케줄 조회 제외 테스트
12. 잘못된 날짜 범위 조회 실패 테스트
13. 종료일 당일 오후 일정이 조회되는지 테스트
14. 응답 JSON 필드명이 `scheduleDateTime`인지 테스트

## 11.7 프론트 연동 기준

스케줄 프론트 연동 세부 요구사항은 별도 문서에서 관리한다.

```text
docs/17_schedule_frontend_integration_requirements.md
```

이 문서는 백엔드 스케줄 API 정책을 기준으로 하고, 프론트 화면 구조, 라우트 상수, 파일 상단 주석, 역할별 버튼 노출 정책은 17번 문서에서 관리한다.

---

## 12. 추후 확장 가능성

초기 구현에서는 단순 일정 CRUD와 조회에 집중한다.

추후 확장 후보는 다음과 같다.

- 반복 일정
- 일정 알림
- 선수 출석 체크
- 일정별 참석 여부
- 경기 일정과 경기 영상 연결
- 스케줄 캘린더 UI 고도화
- 일정 변경 이력 관리
- 코치/감독/분석관 세부 권한 분리
- 여러 팀을 받는 구조로 확장 시 `team_id` 추가

---

## 13. 주의사항

- 스케줄 등록/수정/삭제 권한은 프론트가 아니라 백엔드에서 반드시 검증한다.
- 분석관은 조회만 가능하므로 영상 관리 권한과 스케줄 관리 권한을 혼동하지 않는다.
- 삭제는 실제 삭제가 아니라 `is_deleted = true`로 처리한다.
- 조회 API는 삭제되지 않은 스케줄만 반환한다.
- API 요청/응답 JSON 필드명은 `scheduleDateTime`으로 통일한다.
- Entity 내부 필드명이 `scheduleDatetime`이어도 외부 API 필드명은 `scheduleDateTime`으로 응답한다.
- 날짜 조회는 서버와 클라이언트의 시간대 차이를 고려해야 한다.
- 프론트에서 월간 조회 기간을 만들 때 `toISOString()`을 사용하면 UTC 변환으로 날짜가 밀릴 수 있으므로 로컬 날짜 포맷을 사용한다.
- 종료일 당일 일정이 누락되지 않도록 종료일 다음날 00시 미만 조건을 사용한다.
- 현재 서비스는 단일 팀 기준이므로 `team_id`를 사용하지 않는다.
- 추후 여러 팀을 받을 경우 `schedule` 테이블에 `team_id` 또는 팀 소속 구조가 필요하다.
