// 로그인 회원 역할에 따라 프론트에서 노출할 메뉴와 버튼 권한을 계산하는 파일

import type { MeResponse } from "../types/auth";

export interface FrontendPermission {
  canManageMembers: boolean;

  canCreateSchedule: boolean;
  canUpdateSchedule: boolean;
  canDeleteSchedule: boolean;

  canCreateNotice: boolean;
  canUpdateNotice: boolean;
  canDeleteNotice: boolean;

  canCreateMatchVideo: boolean;
  canUpdateMatchVideo: boolean;
  canDeleteMatchVideo: boolean;

  canCreateTeamAnalysisClip: boolean;
  canUpdateTeamAnalysisClip: boolean;
  canDeleteTeamAnalysisClip: boolean;

  canCreatePlayerAnalysisClip: boolean;
  canUpdatePlayerAnalysisClip: boolean;
  canDeletePlayerAnalysisClip: boolean;

  canCreateDrawing: boolean;
  canUpdateDrawing: boolean;
  canDeleteDrawing: boolean;

  canCreatePlayerRecord: boolean;
  canUpdatePlayerRecord: boolean;
  canDeletePlayerRecord: boolean;
}

// 프론트 화면에서 사용할 역할별 권한 계산
export function getFrontendPermissions(
  member: Pick<MeResponse, "memberRole" | "isAdmin"> | null,
): FrontendPermission {
  const isCoach = member?.memberRole === "COACH";
  const isAnalyst = member?.memberRole === "ANALYST";
  const isManagementUser = isCoach || isAnalyst;

  return {
    canManageMembers: Boolean(member?.isAdmin),

    canCreateSchedule: isCoach,
    canUpdateSchedule: isCoach,
    canDeleteSchedule: isCoach,

    canCreateNotice: isCoach,
    canUpdateNotice: isCoach,
    canDeleteNotice: isCoach,

    canCreateMatchVideo: isManagementUser,
    canUpdateMatchVideo: isManagementUser,
    canDeleteMatchVideo: isCoach,

    canCreateTeamAnalysisClip: isManagementUser,
    canUpdateTeamAnalysisClip: isManagementUser,
    canDeleteTeamAnalysisClip: isCoach,

    canCreatePlayerAnalysisClip: isManagementUser,
    canUpdatePlayerAnalysisClip: isManagementUser,
    canDeletePlayerAnalysisClip: isCoach,

    canCreateDrawing: isManagementUser,
    canUpdateDrawing: isManagementUser,
    canDeleteDrawing: isCoach,

    canCreatePlayerRecord: isManagementUser,
    canUpdatePlayerRecord: isManagementUser,
    canDeletePlayerRecord: isManagementUser,
  };
}
