// 팀 분석 클립 API 요청/응답 타입을 정의하는 파일
import type {
  CreateTeamAnalysisClipDrawingRequest,
  TeamAnalysisClipDrawingResponse,
} from "./teamAnalysisClipDrawing";

export type TeamAnalysisClipType =
  | "HIGHLIGHT"
  | "ATTACK"
  | "DEFENSE"
  | "GOAL"
  | "CONCEDED"
  | "OFFSIDE"
  | "SETPIECE"
  | "ETC";

export type TeamAnalysisClipStatus =
  | "UPLOADING"
  | "PROCESSING"
  | "READY"
  | "FAILED";

export type TeamAnalysisClipPageResponse = {
  teamAnalysisClips: TeamAnalysisClipListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type TeamAnalysisClipListItem = {
  teamClipId: number;
  matchVideoId: number;
  matchVideoTitle: string;
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string | null;
  status: TeamAnalysisClipStatus;
  editorId: number;
  editorName: string;
  startTimeSec: number;
  endTimeSec: number;
  createdAt: string;
  updatedAt?: string;
};

export type TeamAnalysisClipDetailResponse = {
  teamClipId: number;
  matchVideoId: number;
  matchVideoTitle: string;
  matchVideoUrl: string | null;
  teamClipUrl: string | null;
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
  status: TeamAnalysisClipStatus;
  editorId: number;
  editorName: string;
  createdAt: string;
  updatedAt: string;
};

export type CreateTeamAnalysisClipRequest = {
  matchVideoId: number;
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
};

export type UpdateTeamAnalysisClipRequest = {
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
};

export type CreateTeamAnalysisClipWithDrawingsRequest = {
  matchVideoId: number;
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
  drawings: CreateTeamAnalysisClipDrawingRequest[];
};

export type UpdateTeamAnalysisClipWithDrawingsRequest = {
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string | null;
  startTimeSec: number;
  endTimeSec: number;
  drawings: CreateTeamAnalysisClipDrawingRequest[];
};

export type CreateTeamAnalysisClipResponse = {
  teamClipId: number;
  status: TeamAnalysisClipStatus;
  message: string;
};

export type CreateTeamAnalysisClipWithDrawingsResponse = {
  teamClipId: number;
  status: TeamAnalysisClipStatus;
  message: string;
};

export type UpdateTeamAnalysisClipWithDrawingsResponse = {
  teamClipId: number;
  status: TeamAnalysisClipStatus;
  fileGenerationRequested: boolean;
  message: string;
};

export type TeamAnalysisClipEditorForm = {
  matchVideoId: number;
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string;
  startTimeSec: number;
  endTimeSec: number;
};

export type TeamAnalysisClipEditorDrawing = TeamAnalysisClipDrawingResponse;

export type TeamAnalysisClipSearchParams = {
  page: number;
  size: number;
  matchVideoId?: number;
  clipType?: TeamAnalysisClipType;
};

export const TEAM_ANALYSIS_CLIP_TYPE_OPTIONS: {
  value: TeamAnalysisClipType;
  label: string;
}[] = [
  { value: "HIGHLIGHT", label: "하이라이트" },
  { value: "ATTACK", label: "공격" },
  { value: "DEFENSE", label: "수비" },
  { value: "GOAL", label: "득점" },
  { value: "CONCEDED", label: "실점" },
  { value: "OFFSIDE", label: "오프사이드" },
  { value: "SETPIECE", label: "세트피스" },
  { value: "ETC", label: "기타" },
];

export const TEAM_ANALYSIS_CLIP_TYPE_LABELS: Record<
  TeamAnalysisClipType,
  string
> = {
  HIGHLIGHT: "하이라이트",
  ATTACK: "공격",
  DEFENSE: "수비",
  GOAL: "득점",
  CONCEDED: "실점",
  OFFSIDE: "오프사이드",
  SETPIECE: "세트피스",
  ETC: "기타",
};
