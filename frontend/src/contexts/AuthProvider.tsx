// 로그인 상태 복원과 인증 상태 Provider 컴포넌트를 관리하는 파일
// 1. 새로고침 시 Access Token 확인
// 2. 토큰이 있으면 /api/auth/me 호출
// 3. 로그인 상태 복원
// 4. 로그인 성공 시 토큰 저장
// 5. 로그아웃 시 토큰 삭제

import { useEffect, useState, type ReactNode } from "react";
import { getMe, login } from "../api/authApi";
import type { LoginRequest, MeResponse } from "../types/auth";
import {
  getAccessToken,
  removeAccessToken,
  saveAccessToken,
} from "../utils/tokenStorage";
import { AuthContext } from "./authContext";

interface AuthProviderProps {
  children: ReactNode;
}

// 인증 상태 Provider
export function AuthProvider({ children }: AuthProviderProps) {
  const [member, setMember] = useState<MeResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // 새로고침 시 Access Token이 있으면 로그인 상태 복원
  useEffect(() => {
    async function restoreLoginState() {
      const accessToken = getAccessToken();

      if (!accessToken) {
        setIsLoading(false);
        return;
      }

      try {
        const me = await getMe();
        setMember(me);
      } catch {
        removeAccessToken();
        setMember(null);
      } finally {
        setIsLoading(false);
      }
    }

    restoreLoginState();
  }, []);

  // 로그인 후 토큰 저장 및 내 정보 조회
  async function loginWithCredentials(request: LoginRequest) {
    const loginResponse = await login(request);

    saveAccessToken(loginResponse.accessToken);

    const me = await getMe();
    setMember(me);

    return me;
  }

  // 로그아웃 처리
  function logout() {
    removeAccessToken();
    setMember(null);
  }

  return (
    <AuthContext.Provider
      value={{
        member,
        isAuthenticated: Boolean(member),
        isLoading,
        loginWithCredentials,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
