// 비로그인 사용자만 로그인/회원가입 페이지에 접근할 수 있게 제한하는 라우트 파일

import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { getInitialRoute } from "../utils/authRoute";
import { isMobileOrTablet } from "../utils/device";

interface PublicOnlyRouteProps {
  children: ReactNode;
}

// 로그인한 사용자는 역할과 기기에 맞는 홈으로 이동
export function PublicOnlyRoute({ children }: PublicOnlyRouteProps) {
  const { member, isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <div>로그인 상태를 확인하는 중입니다.</div>;
  }

  if (isAuthenticated && member) {
    const nextRoute = getInitialRoute(member.memberRole, isMobileOrTablet());

    return <Navigate to={nextRoute} replace />;
  }

  return children;
}
