# 축구팀 관리 플랫폼 Frontend

축구팀 관리 플랫폼의 프론트엔드 프로젝트입니다.

지도자, 분석관, 선수가 로그인 후 역할과 기기 환경에 맞는 화면에서 팀 운영 정보와 영상 분석 기능을 사용할 수 있도록 구현합니다.

---

## 기술 스택

- React
- TypeScript
- Vite
- React Router
- Axios
- ESLint

---

## 실행 환경

- Node.js
- npm

---

## 설치 방법

```bash
npm install
```

---

## 환경 변수 설정

프로젝트 루트의 `frontend` 폴더에 `.env` 파일을 생성합니다.

```env
VITE_API_BASE_URL=http://localhost:8080
```

주의사항:

- `.env` 파일은 GitHub에 올리지 않습니다.
- `.env.example` 파일은 예시 용도로만 GitHub에 올릴 수 있습니다.
- `VITE_`로 시작하는 환경 변수는 프론트 번들에 포함될 수 있으므로 Secret Key를 넣으면 안 됩니다.

---

## 개발 서버 실행

```bash
npm run dev
```

기본 개발 서버 주소:

```text
http://localhost:5173
```

---

## 빌드

```bash
npm run build
```

---

## 주요 기능

### 로그인

- 아이디와 비밀번호로 로그인
- 로그인 성공 시 Access Token 저장
- 로그인 후 `/api/auth/me` 호출로 사용자 정보 조회
- 역할과 기기 기준으로 초기 화면 이동

### 회원가입

- 역할 선택 후 회원가입 신청
- PLAYER 선택 시 선수 추가 정보 입력
- 회원가입 성공 시 관리자 승인 안내 alert 표시
- 확인 후 로그인 화면으로 이동

### 인증 상태 관리

- Access Token localStorage 저장
- Axios 요청 시 Authorization 헤더 자동 추가
- 새로고침 시 로그인 상태 복원
- 401 응답 발생 시 Access Token 삭제

### 라우팅

로그인 후 초기 이동 정책:

| 조건          | 이동 경로    |
| ------------- | ------------ |
| PC + COACH    | `/dashboard` |
| PC + ANALYST  | `/dashboard` |
| PC + PLAYER   | `/player`    |
| 모바일/태블릿 | `/mobile`    |

보호 라우트:

- 비로그인 사용자가 보호 페이지 접근 시 `/login`으로 이동
- 로그인 사용자가 `/login`, `/sign-up` 접근 시 역할/기기 기준 홈으로 이동
- 권한이 없는 역할이 특정 페이지에 접근하면 역할/기기 기준 홈으로 이동

---

## 주요 폴더 구조

```text
src/
├── api/
│   ├── axiosInstance.ts
│   └── authApi.ts
├── contexts/
│   ├── authContext.ts
│   └── AuthProvider.tsx
├── hooks/
│   └── useAuth.ts
├── pages/
│   ├── LoginPage.tsx
│   ├── SignUpPage.tsx
│   ├── ManagementHomePage.tsx
│   ├── PlayerHomePage.tsx
│   └── MobileHomePage.tsx
├── routes/
│   ├── ProtectedRoute.tsx
│   └── RoleRoute.tsx
├── types/
│   └── auth.ts
└── utils/
    ├── apiError.ts
    ├── authRoute.ts
    ├── device.ts
    └── tokenStorage.ts
```

---

## 백엔드 연동

프론트는 백엔드 API 서버와 연동합니다.

개발 기준 백엔드 주소:

```text
http://localhost:8080
```

백엔드 CORS 설정에서 아래 Origin을 허용해야 합니다.

```text
http://localhost:5173
```

사용 API:

```text
POST /api/auth/login
POST /api/auth/sign-up
GET  /api/auth/me
```

---

## Git 관리 주의사항

GitHub에 올리지 않는 파일:

```text
node_modules/
dist/
.env
.env.local
.env.development.local
.env.test.local
.env.production.local
```

GitHub에 올려도 되는 파일:

```text
.env.example
package.json
package-lock.json
vite.config.ts
tsconfig.json
README.md
```
