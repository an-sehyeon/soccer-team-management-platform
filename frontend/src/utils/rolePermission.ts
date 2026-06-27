// 로그인 회원 역할에 따라 프론트 화면에서 사용할 버튼 노출 권한을 계산하는 파일

import type { MeResponse } from "../types/auth";

export interface FrontendPermissions {
  canReadSchedule: boolean;
  canCreateSchedule: boolean;
  canUpdateSchedule: boolean;
  canDeleteSchedule: boolean;

  canReadNotice: boolean;
  canCreateNotice: boolean;
  canUpdateNotice: boolean;
  canDeleteNotice: boolean;

  canReadMatchVideo: boolean;
  canCreateMatchVideo: boolean;
  canUpdateMatchVideo: boolean;
  canDeleteMatchVideo: boolean;

  canReadTeamAnalysisClip: boolean;
  canCreateTeamAnalysisClip: boolean;
  canUpdateTeamAnalysisClip: boolean;
  canDeleteTeamAnalysisClip: boolean;

  canReadPlayerAnalysisClip: boolean;
  canCreatePlayerAnalysisClip: boolean;
  canUpdatePlayerAnalysisClip: boolean;
  canDeletePlayerAnalysisClip: boolean;

  canReadDrawing: boolean;
  canCreateDrawing: boolean;
  canUpdateDrawing: boolean;
  canDeleteDrawing: boolean;

  canReadPlayerRecord: boolean;
  canCreatePlayerRecord: boolean;
  canUpdatePlayerRecord: boolean;
  canDeletePlayerRecord: boolean;

  canManageMembers: boolean;
}

const EMPTY_PERMISSIONS: FrontendPermissions = {
  canReadSchedule: false,
  canCreateSchedule: false,
  canUpdateSchedule: false,
  canDeleteSchedule: false,

  canReadNotice: false,
  canCreateNotice: false,
  canUpdateNotice: false,
  canDeleteNotice: false,

  canReadMatchVideo: false,
  canCreateMatchVideo: false,
  canUpdateMatchVideo: false,
  canDeleteMatchVideo: false,

  canReadTeamAnalysisClip: false,
  canCreateTeamAnalysisClip: false,
  canUpdateTeamAnalysisClip: false,
  canDeleteTeamAnalysisClip: false,

  canReadPlayerAnalysisClip: false,
  canCreatePlayerAnalysisClip: false,
  canUpdatePlayerAnalysisClip: false,
  canDeletePlayerAnalysisClip: false,

  canReadDrawing: false,
  canCreateDrawing: false,
  canUpdateDrawing: false,
  canDeleteDrawing: false,

  canReadPlayerRecord: false,
  canCreatePlayerRecord: false,
  canUpdatePlayerRecord: false,
  canDeletePlayerRecord: false,

  canManageMembers: false,
};

export function getFrontendPermissions(
  member: MeResponse | null,
): FrontendPermissions {
  if (!member) {
    return EMPTY_PERMISSIONS;
  }

  const isCoach = member.memberRole === "COACH";
  const isAnalyst = member.memberRole === "ANALYST";
  const isPlayer = member.memberRole === "PLAYER";

  return {
    canReadSchedule: isCoach || isAnalyst || isPlayer,
    canCreateSchedule: isCoach,
    canUpdateSchedule: isCoach,
    canDeleteSchedule: isCoach,

    canReadNotice: isCoach || isAnalyst || isPlayer,
    canCreateNotice: isCoach,
    canUpdateNotice: isCoach,
    canDeleteNotice: isCoach,

    canReadMatchVideo: isCoach || isAnalyst || isPlayer,
    canCreateMatchVideo: isCoach || isAnalyst,
    canUpdateMatchVideo: isCoach || isAnalyst,
    canDeleteMatchVideo: isCoach,

    canReadTeamAnalysisClip: isCoach || isAnalyst || isPlayer,
    canCreateTeamAnalysisClip: isCoach || isAnalyst,
    canUpdateTeamAnalysisClip: isCoach || isAnalyst,
    canDeleteTeamAnalysisClip: isCoach,

    canReadPlayerAnalysisClip: isCoach || isAnalyst || isPlayer,
    canCreatePlayerAnalysisClip: isCoach || isAnalyst,
    canUpdatePlayerAnalysisClip: isCoach || isAnalyst,
    canDeletePlayerAnalysisClip: isCoach,

    canReadDrawing: isCoach || isAnalyst || isPlayer,
    canCreateDrawing: isCoach || isAnalyst,
    canUpdateDrawing: isCoach || isAnalyst,
    canDeleteDrawing: isCoach,

    canReadPlayerRecord: isCoach || isAnalyst || isPlayer,
    canCreatePlayerRecord: isCoach || isAnalyst,
    canUpdatePlayerRecord: isCoach || isAnalyst,
    canDeletePlayerRecord: isCoach || isAnalyst,

    canManageMembers: member.isAdmin,
  };
}
