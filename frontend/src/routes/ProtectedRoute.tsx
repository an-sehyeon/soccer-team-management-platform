// 로그인한 사용자만 하위 페이지에 접근할 수 있게 제한하는 라우트 파일

import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ROUTES } from "../constants/routes";

interface ProtectedRouteProps {
  children: ReactNode;
}

// 인증 필요 페이지 접근 제한
export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <div>로그인 상태를 확인하는 중입니다.</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  return children;
}
