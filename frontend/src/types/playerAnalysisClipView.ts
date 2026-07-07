// 선수 개인 분석 클립 조회 기록 API 응답 타입을 정의하는 파일

export type PlayerAnalysisClipViewResponse = {
  viewId: number;
  playerClipId: number;
  playerClipTitle: string;
  playerId: number;
  playerName: string;
  firstViewedAt: string;
  lastViewedAt: string;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
};

export type PlayerAnalysisClipViewPageResponse = {
  views: PlayerAnalysisClipViewResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PlayerAnalysisClipViewSearchParams = {
  page: number;
  size: number;
};
