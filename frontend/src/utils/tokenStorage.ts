// Access Token을 브라우저 localStorage에 저장, 조회, 삭제하는 파일

const ACCESS_TOKEN_KEY = "accessToken";

// Access Token 저장
export function saveAccessToken(accessToken: string) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
}

// Access Token 조회
export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

// Access Token 삭제
export function removeAccessToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
}

// Access Token 존재 여부 확인
export function hasAccessToken() {
  return Boolean(getAccessToken());
}
