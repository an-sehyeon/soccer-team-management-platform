// 인증 상태 Context와 타입을 정의하는 파일

import { createContext } from "react";
import type { LoginRequest, MeResponse } from "../types/auth";

export interface AuthContextValue {
  member: MeResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  loginWithCredentials: (request: LoginRequest) => Promise<MeResponse>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
