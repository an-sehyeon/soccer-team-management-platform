// 로그인 회원 역할에 따라 페이지 접근을 제한하는 라우트 파일

import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import type { MemberRole } from "../types/auth";
import { useAuth } from "../hooks/useAuth";
import { getInitialRoute } from "../utils/authRoute";
import { isMobileOrTablet } from "../utils/device";
import { ROUTES } from "../constants/routes";

interface RoleRouteProps {
  allowedRoles: MemberRole[];
  children: ReactNode;
}

// 역할별 페이지 접근 제한
export function RoleRoute({ allowedRoles, children }: RoleRouteProps) {
  const { member, isLoading } = useAuth();

  if (isLoading) {
    return <div>권한을 확인하는 중입니다.</div>;
  }

  if (!member) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  if (!allowedRoles.includes(member.memberRole)) {
    const nextRoute = getInitialRoute(member.memberRole, isMobileOrTablet());

    return <Navigate to={nextRoute} replace />;
  }

  return children;
}
