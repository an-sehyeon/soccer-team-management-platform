// 선수 개인 분석 클립 API 요청/응답 타입을 정의하는 파일

export type PlayerAnalysisClipType =
  | "PLAYER_GOOD"
  | "PLAYER_MISTAKE"
  | "SHOOTING"
  | "PASS"
  | "DRIBBLE"
  | "DEFENSE"
  | "POSITIONING"
  | "PRESSING"
  | "OFF_THE_BALL"
  | "ETC";

export type PlayerAnalysisClipStatus = "UPLOADING" | "READY" | "FAILED";

export type PlayerSelectItem = {
  playerId: number;
  name: string;
  grade: number | null;
  uniformNumber: number | null;
};

export type PlayerAnalysisClipPageResponse = {
  playerClips: PlayerAnalysisClipListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PlayerAnalysisClipListItem = {
  playerClipId: number;
  matchVideoId: number;
  matchVideoTitle: string;
  playerId: number;
  playerName: string;
  clipType: PlayerAnalysisClipType;
  title: string;
  startTimeSec: number;
  endTimeSec: number;
  status: PlayerAnalysisClipStatus;
  editorId: number;
  editorName: string;
  createdAt: string;
};

export type PlayerAnalysisClipDetailResponse = {
  playerClipId: number;
  matchVideoId: number;
  matchVideoTitle: string;
  matchVideoUrl: string;
  playerId: number;
  playerName: string;
  clipType: PlayerAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
  status: PlayerAnalysisClipStatus;
  editorId: number;
  editorName: string;
  createdAt: string;
  updatedAt: string;
};

export type CreatePlayerAnalysisClipRequest = {
  matchVideoId: number;
  playerId: number;
  clipType: PlayerAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
};

export type UpdatePlayerAnalysisClipRequest = {
  matchVideoId: number;
  playerId: number;
  clipType: PlayerAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
};

export type CreatePlayerAnalysisClipResponse = {
  playerClipId: number;
  message: string;
};

export type PlayerAnalysisClipSearchParams = {
  page: number;
  size: number;
  matchVideoId?: number;
  playerId?: number;
  clipType?: PlayerAnalysisClipType;
};

export const PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS: {
  value: PlayerAnalysisClipType;
  label: string;
}[] = [
  { value: "PLAYER_GOOD", label: "좋은 장면" },
  { value: "PLAYER_MISTAKE", label: "개선 장면" },
  { value: "SHOOTING", label: "슈팅" },
  { value: "PASS", label: "패스" },
  { value: "DRIBBLE", label: "드리블" },
  { value: "DEFENSE", label: "수비" },
  { value: "POSITIONING", label: "포지셔닝" },
  { value: "PRESSING", label: "압박" },
  { value: "OFF_THE_BALL", label: "오프 더 볼" },
  { value: "ETC", label: "기타" },
];

export const PLAYER_ANALYSIS_CLIP_TYPE_LABELS: Record<
  PlayerAnalysisClipType,
  string
> = {
  PLAYER_GOOD: "좋은 장면",
  PLAYER_MISTAKE: "개선 장면",
  SHOOTING: "슈팅",
  PASS: "패스",
  DRIBBLE: "드리블",
  DEFENSE: "수비",
  POSITIONING: "포지셔닝",
  PRESSING: "압박",
  OFF_THE_BALL: "오프 더 볼",
  ETC: "기타",
};
