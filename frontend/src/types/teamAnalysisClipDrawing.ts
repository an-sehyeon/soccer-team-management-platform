// 팀 분석 클립 드로잉 API 요청/응답 타입을 정의하는 파일

export type TeamAnalysisClipDrawingType =
  | "LINE"
  | "ARROW"
  | "CIRCLE"
  | "BOX"
  | "AREA"
  | "TEXT";

export type TeamAnalysisClipDrawingData = Record<string, unknown>;

export type TeamAnalysisClipDrawingResponse = {
  drawingId: number;
  teamClipId: number;
  drawingType: TeamAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: TeamAnalysisClipDrawingData;
  writerId: number;
  writerName: string;
  createdAt: string;
  updatedAt: string;
};

export type TeamAnalysisClipDrawingListResponse =
  TeamAnalysisClipDrawingResponse[];

export type CreateTeamAnalysisClipDrawingRequest = {
  drawingType: TeamAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: TeamAnalysisClipDrawingData;
};

export type UpdateTeamAnalysisClipDrawingRequest = {
  drawingType: TeamAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: TeamAnalysisClipDrawingData;
};

export type CreateTeamAnalysisClipDrawingResponse = {
  drawingId: number;
  message: string;
};

export const TEAM_ANALYSIS_CLIP_DRAWING_TYPE_OPTIONS: {
  value: TeamAnalysisClipDrawingType;
  label: string;
}[] = [
  { value: "LINE", label: "선" },
  { value: "ARROW", label: "화살표" },
  { value: "CIRCLE", label: "원" },
  { value: "BOX", label: "박스" },
  { value: "AREA", label: "영역" },
  { value: "TEXT", label: "텍스트" },
];

export const TEAM_ANALYSIS_CLIP_DRAWING_TYPE_LABELS: Record<
  TeamAnalysisClipDrawingType,
  string
> = {
  LINE: "선",
  ARROW: "화살표",
  CIRCLE: "원",
  BOX: "박스",
  AREA: "영역",
  TEXT: "텍스트",
};
