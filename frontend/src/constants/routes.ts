// 프론트에서 사용하는 주요 페이지 경로를 한 곳에서 관리하는 파일

export const ROUTES = {
  LOGIN: "/login",
  SIGN_UP: "/sign-up",

  DASHBOARD: "/dashboard",
  PLAYER: "/player",
  MOBILE: "/mobile",

  MEMBER_APPROVAL: "/dashboard/member-approval",

  SCHEDULE: "/schedules",
  NOTICE: "/notices",

  MATCH_VIDEO: "/match-videos",
  MATCH_VIDEO_CREATE: "/match-videos/new",

  TEAM_ANALYSIS_CLIP: "/team-analysis-clips",

  PLAYER_ANALYSIS_CLIP: "/player-analysis-clips",

  PLAYER_RECORD: "/player-records",
} as const;

export type MatchVideoAnalysisMode =
  | "team-clip-create"
  | "team-clip-edit"
  | "player-clip-create"
  | "player-clip-edit"
  | "player-record-event";

export function createMatchVideoAnalysisRoute(params: {
  analysisMode: MatchVideoAnalysisMode;
  teamClipId?: number;
  playerClipId?: number;
}) {
  const searchParams = new URLSearchParams({
    analysisMode: params.analysisMode,
  });

  if (params.teamClipId !== undefined) {
    searchParams.set("teamClipId", String(params.teamClipId));
  }

  if (params.playerClipId !== undefined) {
    searchParams.set("playerClipId", String(params.playerClipId));
  }

  return `${ROUTES.MATCH_VIDEO}?${searchParams.toString()}`;
}
