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
} as const;
