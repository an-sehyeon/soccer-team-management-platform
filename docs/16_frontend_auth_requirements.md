# 프론트 로그인 회원가입 요구사항 문서

## 1. 기능 목적

축구팀 영상분석 플랫폼의 프론트엔드 초기 인증 화면을 구현한다.

사용자는 회원가입 후 관리자 승인을 기다리고, 승인된 계정만 로그인할 수 있다.

로그인 성공 후에는 사용자 역할과 기기 환경에 따라 적절한 초기 화면으로 이동한다.

이 기능의 목적은 다음과 같다.

* React 기반 프론트엔드 프로젝트 초기 구조 세팅
* 로그인 화면 구현
* 회원가입 화면 구현
* 로그인 API 연결
* 회원가입 API 연결
* Access Token 저장 및 API 요청 인증 처리
* 새로고침 시 로그인 상태 복원
* 역할과 기기 기준 초기 페이지 이동
* 인증 필요 페이지 접근 제한
* 비로그인 전용 페이지 접근 제한
* 백엔드 CORS 설정 연동

---

## 2. 사용자 역할

### COACH

지도자 역할이다.

PC 환경에서는 대시보드 화면으로 이동한다.

관리 기능 사용을 전제로 한다.

### ANALYST

분석관 역할이다.

PC 환경에서는 대시보드 화면으로 이동한다.

지도자와 같은 대시보드 화면을 사용하되, 삭제 권한 등 일부 기능은 제한된다.

### PLAYER

선수 역할이다.

PC 환경에서는 선수 홈 화면으로 이동한다.

모바일/태블릿 환경에서는 공통 모바일 홈 화면으로 이동한다.

---

## 3. 권한 정책

프론트 권한 분기는 사용자 경험을 위한 처리이다.

버튼 숨김, 메뉴 숨김, 화면 접근 제한은 프론트에서 처리하지만 실제 권한 검증은 반드시 백엔드에서 처리한다.

### 프론트 접근 정책

| 역할      | PC 초기 경로     | 모바일/태블릿 초기 경로 |
| ------- | ------------ | ------------- |
| COACH   | `/dashboard` | `/mobile`     |
| ANALYST | `/dashboard` | `/mobile`     |
| PLAYER  | `/player`    | `/mobile`     |

### 인증 접근 정책

| 상태                     | 접근 가능 화면                           |
| ---------------------- | ---------------------------------- |
| 비로그인 사용자               | `/login`, `/sign-up`               |
| 로그인 사용자                | `/dashboard`, `/player`, `/mobile` |
| 로그인 사용자가 `/login` 접근   | 역할/기기 기준 홈으로 이동                    |
| 로그인 사용자가 `/sign-up` 접근 | 역할/기기 기준 홈으로 이동                    |
| 비로그인 사용자가 보호 페이지 접근    | `/login`으로 이동                      |

---

## 4. 화면 흐름

### 로그인 화면

경로:

```text
/login
```

기능:

* 아이디 입력
* 비밀번호 입력
* 로그인 버튼
* 회원가입 화면 이동 링크
* 로그인 실패 메시지 표시

로그인 성공 후 처리:

1. `POST /api/auth/login` 호출
2. Access Token 저장
3. `GET /api/auth/me` 호출
4. 로그인 사용자 정보 저장
5. 기기와 역할 기준 초기 경로로 이동

이동 정책:

```text
PC + COACH   → /dashboard
PC + ANALYST → /dashboard
PC + PLAYER  → /player
모바일/태블릿 → /mobile
```

---

### 회원가입 화면

경로:

```text
/sign-up
```

기능:

* 역할 선택
* 아이디 입력
* 비밀번호 입력
* 이름 입력
* 휴대폰 번호 입력
* PLAYER 선택 시 선수 추가 정보 입력
* 회원가입 신청 버튼
* 로그인 화면 이동 링크

PLAYER 추가 입력값:

* 학년
* 등번호
* 주장 여부
* 출신학교

회원가입 성공 후 처리:

1. `POST /api/auth/sign-up` 호출
2. 백엔드 응답의 `message` 값을 alert로 표시
3. 확인 버튼 클릭 후 `/login`으로 이동

성공 메시지 출처:

```text
백엔드 회원가입 API 응답 body의 message 필드
```

예시:

```json
{
  "memberId": 1,
  "approvalStatus": "PENDING",
  "message": "회원가입 신청이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다."
}
```

---

### 대시보드 화면

경로:

```text
/dashboard
```

접근 역할:

* COACH
* ANALYST

기능:

* 로그인 사용자 이름 표시
* 로그인 사용자 역할 표시
* 관리 메뉴 표시
* 로그아웃
* 추후 스케줄, 공지사항, 경기 영상, 분석 클립, 선수 기록 화면으로 이동

초기 메뉴:

* 스케줄 관리
* 공지사항 관리
* 경기 영상 관리
* 팀 분석 클립 관리
* 선수 개인 분석 클립 관리
* 드로잉 관리
* 선수 기록 관리

---

### 선수 홈 화면

경로:

```text
/player
```

접근 역할:

* PLAYER

기능:

* 로그인 선수 정보 표시
* 선수용 메뉴 표시
* 로그아웃

초기 메뉴:

* 오늘 일정
* 공지사항
* 경기 원본 영상
* 팀 분석 영상
* 내 개인 분석 영상
* 내 기록

---

### 모바일 홈 화면

경로:

```text
/mobile
```

접근 역할:

* COACH
* ANALYST
* PLAYER

기능:

* 모바일/태블릿 공통 홈 화면 표시
* 로그인 사용자 정보 표시
* 모바일용 메뉴 표시
* 로그아웃

초기 메뉴:

* 오늘 일정
* 공지사항
* 경기 원본 영상
* 팀 분석 영상
* 내 개인 분석 영상
* 내 기록

---

## 5. API 흐름

### 로그인 API

```http
POST /api/auth/login
```

요청:

```json
{
  "loginId": "user01",
  "password": "password1234"
}
```

응답:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "member": {
    "memberId": 1,
    "loginId": "user01",
    "name": "홍길동",
    "memberRole": "PLAYER",
    "isAdmin": false,
    "isCaptain": false
  }
}
```

프론트 처리:

* `accessToken`을 localStorage에 저장
* 이후 API 요청 시 Authorization 헤더에 자동 적용
* 로그인 성공 후 `GET /api/auth/me`를 호출해 사용자 정보를 복원

---

### 회원가입 API

```http
POST /api/auth/sign-up
```

PLAYER 요청 예시:

```json
{
  "loginId": "player01",
  "password": "password1234",
  "name": "선수",
  "phone": "01012345678",
  "memberRole": "PLAYER",
  "grade": 3,
  "uniformNumber": 10,
  "isCaptain": false,
  "almaMater": "OO고등학교"
}
```

COACH 요청 예시:

```json
{
  "loginId": "coach01",
  "password": "password1234",
  "name": "지도자",
  "phone": "01012345678",
  "memberRole": "COACH"
}
```

ANALYST 요청 예시:

```json
{
  "loginId": "analyst01",
  "password": "password1234",
  "name": "분석관",
  "phone": "01012345678",
  "memberRole": "ANALYST"
}
```

응답:

```json
{
  "memberId": 1,
  "approvalStatus": "PENDING",
  "message": "회원가입 신청이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다."
}
```

---

### 내 정보 조회 API

```http
GET /api/auth/me
```

사용 목적:

* 새로고침 시 로그인 상태 복원
* Access Token 유효성 확인
* 로그인 사용자 역할 확인
* 화면 라우팅 분기

응답 예시:

```json
{
  "memberId": 1,
  "loginId": "player01",
  "name": "선수",
  "phone": "01012345678",
  "memberRole": "PLAYER",
  "approvalStatus": "APPROVED",
  "isAdmin": false,
  "isCaptain": false,
  "grade": 3,
  "uniformNumber": 10,
  "almaMater": "OO고등학교"
}
```

---

## 6. 상태 관리

초기 버전에서는 별도 상태 관리 라이브러리를 사용하지 않는다.

React Context를 사용해 인증 상태를 관리한다.

관리 항목:

* 로그인 사용자 정보
* 로그인 여부
* 로그인 상태 복원 중 여부
* 로그인 함수
* 로그아웃 함수

Access Token 저장 위치:

```text
localStorage
```

관련 파일:

```text
src/contexts/authContext.ts
src/contexts/AuthProvider.tsx
src/hooks/useAuth.ts
src/utils/tokenStorage.ts
```

---

## 7. Axios 설정

공통 API 요청은 Axios 인스턴스를 통해 처리한다.

관련 파일:

```text
src/api/axiosInstance.ts
```

처리 내용:

* `VITE_API_BASE_URL`을 baseURL로 사용
* Access Token이 있으면 Authorization 헤더 자동 추가
* 401 응답 발생 시 저장된 Access Token 삭제

Authorization 헤더 형식:

```http
Authorization: Bearer {accessToken}
```

---

## 8. 환경 변수

프론트 API 기본 주소는 `.env`에서 관리한다.

개발 환경 예시:

```env
VITE_API_BASE_URL=http://localhost:8080
```

주의사항:

* `.env`는 GitHub에 올리지 않는다.
* `.env.example`은 예시 용도로만 올릴 수 있다.
* `VITE_`로 시작하는 값은 프론트 번들에 포함될 수 있으므로 Secret Key를 넣으면 안 된다.

---

## 9. 라우팅 구조

라우팅은 React Router를 사용한다.

주요 경로:

```text
/login
/sign-up
/dashboard
/player
/mobile
```

라우트 보호 파일:

```text
src/routes/ProtectedRoute.tsx
src/routes/PublicOnlyRoute.tsx
src/routes/RoleRoute.tsx
```

### ProtectedRoute

로그인한 사용자만 접근할 수 있는 페이지를 보호한다.

비로그인 사용자가 접근하면 `/login`으로 이동한다.

### PublicOnlyRoute

비로그인 사용자만 접근할 수 있는 페이지를 보호한다.

로그인 사용자가 `/login` 또는 `/sign-up`에 접근하면 역할과 기기 기준 초기 화면으로 이동한다.

### RoleRoute

특정 역할만 접근할 수 있는 페이지를 보호한다.

허용되지 않은 역할이 접근하면 해당 사용자의 역할과 기기 기준 초기 화면으로 이동한다.

---

## 10. 기기 판단 정책

초기 구현에서는 브라우저 환경 정보를 기준으로 모바일/태블릿 여부를 판단한다.

기본 정책:

```text
휴대폰 화면 너비 → 모바일 화면
터치 지원 + 태블릿 너비 → 모바일 공통 화면
그 외 → PC 화면
```

주의사항:

* 기기 판단은 완벽하지 않다.
* 일부 터치 노트북은 태블릿처럼 판단될 수 있다.
* 실제 운영 단계에서는 반응형 UI와 사용자 경험을 기준으로 보완할 수 있다.

---

## 11. 백엔드 CORS 설정

프론트 개발 서버에서 백엔드 API를 호출하기 위해 백엔드 CORS 설정을 추가한다.

개발 프론트 주소:

```text
http://localhost:5173
```

백엔드 허용 설정:

* Origin: `http://localhost:5173`
* Methods: `GET`, `POST`, `PATCH`, `DELETE`, `OPTIONS`
* Headers: `Authorization`, `Content-Type`

주의사항:

* 운영 배포 시 실제 프론트 도메인을 추가해야 한다.
* 개발 중에는 `localhost:5173`만 허용한다.
* 프론트 CORS 우회가 아니라 백엔드에서 명시적으로 허용해야 한다.

---

## 12. 예외 상황

### 로그인 실패

상황:

* 잘못된 아이디
* 잘못된 비밀번호
* 승인되지 않은 계정
* 탈퇴 또는 삭제된 계정

처리:

* 백엔드 에러 메시지를 화면에 표시한다.
* Access Token은 저장하지 않는다.

### 회원가입 실패

상황:

* 중복 아이디
* 필수 입력값 누락
* PLAYER 필수 정보 누락
* 유효하지 않은 학년 또는 등번호

처리:

* 프론트 1차 검증
* 백엔드 최종 검증
* 실패 시 에러 메시지 표시

### Access Token 만료 또는 유효하지 않음

상황:

* `/api/auth/me` 호출 실패
* 인증 API 요청 중 401 응답 발생

처리:

* localStorage에서 Access Token 삭제
* 로그인 상태 초기화
* 로그인 화면으로 이동

---

## 13. 구현된 주요 파일

```text
frontend/src/api/axiosInstance.ts
frontend/src/api/authApi.ts
frontend/src/contexts/authContext.ts
frontend/src/contexts/AuthProvider.tsx
frontend/src/hooks/useAuth.ts
frontend/src/pages/LoginPage.tsx
frontend/src/pages/SignUpPage.tsx
frontend/src/pages/ManagementHomePage.tsx
frontend/src/pages/PlayerHomePage.tsx
frontend/src/pages/MobileHomePage.tsx
frontend/src/routes/ProtectedRoute.tsx
frontend/src/routes/RoleRoute.tsx
frontend/src/types/auth.ts
frontend/src/utils/apiError.ts
frontend/src/utils/authRoute.ts
frontend/src/utils/device.ts
frontend/src/utils/tokenStorage.ts
backend/src/main/java/com/soccer/platform/config/SecurityConfig.java
```

---

## 14. 구현 순서

1. React TypeScript Vite 프로젝트 생성
2. Axios 및 React Router 설치
3. `.env` 기반 API 주소 설정
4. 인증 타입 정의
5. Access Token 저장 유틸 작성
6. Axios 공통 인스턴스 작성
7. 로그인 API 함수 작성
8. 회원가입 API 함수 작성
9. 내 정보 조회 API 함수 작성
10. Auth Context와 Provider 구현
11. 로그인 화면 구현
12. 회원가입 화면 구현
13. 역할/기기별 초기 경로 계산
14. 보호 라우트 구현
15. 역할 라우트 구현
16. 로그인 후 기본 화면 구현
17. 백엔드 CORS 설정 추가
18. 빌드 및 로그인/회원가입 테스트

---

## 15. 추후 확장 가능성

추후 아래 기능을 확장할 수 있다.

* Refresh Token 기반 자동 로그인 연장
* Access Token 만료 전 자동 갱신
* 관리자 회원 승인 화면
* 권한별 사이드바 메뉴
* 공통 레이아웃 분리
* 스케줄 화면 연동
* 공지사항 화면 연동
* 경기 영상 업로드 화면 연동
* 팀 분석 클립 화면 연동
* 선수 개인 분석 클립 화면 연동
* 선수 기록 화면 연동
* 모바일 전용 하단 네비게이션
* 반응형 UI 디자인 개선

```
```
