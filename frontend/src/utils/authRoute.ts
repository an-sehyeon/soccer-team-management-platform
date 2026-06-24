// 로그인 회원 역할과 기기 유형에 따라 초기 이동 경로 결정하는 파일

import type { MemberRole } from "../types/auth";
import { ROUTES } from "../constants/routes";

// 로그인 후 이동할 초기 경로 반환
export function getInitialRoute(
  memberRole: MemberRole,
  isMobileOrTablet: boolean,
) {
  if (isMobileOrTablet) {
    return ROUTES.LOGIN;
  }

  if (memberRole === "COACH" || memberRole === "ANALYST") {
    return ROUTES.DASHBOARD;
  }

  return ROUTES.PLAYER;
}
