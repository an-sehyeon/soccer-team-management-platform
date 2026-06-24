// 인증 상태 Context를 쉽게 사용할 수 있게 하는 Hook 파일
// 페이지에서 로그인 정보와 로그아웃 기능을 꺼내 쓸 때 사용

import { useContext } from "react";
import { AuthContext } from "../contexts/authContext";

// 인증 상태 사용 Hook
export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth는 AuthProvider 안에서만 사용할 수 있습니다.");
  }

  return context;
}
