// 관리자 권한이 있는 사용자만 하위 페이지에 접근할 수 있게 제한하는 라우트 파일

import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { ROUTES } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";
import { getInitialRoute } from "../utils/authRoute";
import { isMobileOrTablet } from "../utils/device";

interface AdminOnlyRouteProps {
  children: ReactNode;
}

// 관리자 전용 페이지 접근 제한
export function AdminOnlyRoute({ children }: AdminOnlyRouteProps) {
  const { member, isLoading } = useAuth();

  if (isLoading) {
    return <div>관리자 권한을 확인하는 중입니다.</div>;
  }

  if (!member) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  if (!member.isAdmin) {
    const nextRoute = getInitialRoute(member.memberRole, isMobileOrTablet());

    return <Navigate to={nextRoute} replace />;
  }
  return children;
}
