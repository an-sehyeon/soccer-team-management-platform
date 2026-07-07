// 선수 개인 분석 클립 조회 기록 관련 백엔드 API 호출 함수를 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  PlayerAnalysisClipViewPageResponse,
  PlayerAnalysisClipViewSearchParams,
} from "../types/playerAnalysisClipView";

// 관리용 특정 선수의 개인 분석 클립 조회 기록 목록 조회
export const getManagementPlayerAnalysisClipViews = async (
  playerId: number,
  params: PlayerAnalysisClipViewSearchParams,
): Promise<PlayerAnalysisClipViewPageResponse> => {
  const response = await axiosInstance.get(
    `/api/management/players/${playerId}/player-analysis-clip-views`,
    { params },
  );

  return response.data;
};

// 선수 본인 개인 분석 클립 조회 기록 목록 조회
export const getMyPlayerAnalysisClipViews = async (
  params: PlayerAnalysisClipViewSearchParams,
): Promise<PlayerAnalysisClipViewPageResponse> => {
  const response = await axiosInstance.get(
    "/api/player/me/player-analysis-clip-views",
    { params },
  );

  return response.data;
};
