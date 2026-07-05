// 선수 개인 분석 클립 드로잉 API 요청/응답 타입을 정의하는 파일

export type PlayerAnalysisClipDrawingType =
  | "LINE"
  | "ARROW"
  | "CIRCLE"
  | "BOX"
  | "AREA"
  | "TEXT";

export type PlayerAnalysisClipDrawingData = Record<string, unknown>;

export type PlayerAnalysisClipDrawingResponse = {
  drawingId: number;
  playerClipId: number;
  drawingType: PlayerAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: PlayerAnalysisClipDrawingData;
  writerId: number;
  writerName: string;
  createdAt: string;
  updatedAt: string;
};

export type PlayerAnalysisClipDrawingListResponse =
  PlayerAnalysisClipDrawingResponse[];

export type CreatePlayerAnalysisClipDrawingRequest = {
  drawingType: PlayerAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: PlayerAnalysisClipDrawingData;
};

export type UpdatePlayerAnalysisClipDrawingRequest = {
  drawingType: PlayerAnalysisClipDrawingType;
  startTimeSec: number;
  endTimeSec: number;
  drawingData: PlayerAnalysisClipDrawingData;
};

export type CreatePlayerAnalysisClipDrawingResponse = {
  drawingId: number;
  message: string;
};

export const PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_OPTIONS: {
  value: PlayerAnalysisClipDrawingType;
  label: string;
}[] = [
  { value: "LINE", label: "선" },
  { value: "ARROW", label: "화살표" },
  { value: "CIRCLE", label: "원" },
  { value: "BOX", label: "박스" },
  { value: "AREA", label: "영역" },
  { value: "TEXT", label: "텍스트" },
];

export const PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_LABELS: Record<
  PlayerAnalysisClipDrawingType,
  string
> = {
  LINE: "선",
  ARROW: "화살표",
  CIRCLE: "원",
  BOX: "박스",
  AREA: "영역",
  TEXT: "텍스트",
};
