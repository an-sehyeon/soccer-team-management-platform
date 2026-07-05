// 선수 개인 분석 클립 드로잉 관련 백엔드 API 호출 함수를 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  CreatePlayerAnalysisClipDrawingRequest,
  CreatePlayerAnalysisClipDrawingResponse,
  PlayerAnalysisClipDrawingListResponse,
  PlayerAnalysisClipDrawingResponse,
  UpdatePlayerAnalysisClipDrawingRequest,
} from "../types/playerAnalysisClipDrawing";

export const getPlayerAnalysisClipDrawings = async (
  playerClipId: number,
): Promise<PlayerAnalysisClipDrawingListResponse> => {
  const response = await axiosInstance.get<PlayerAnalysisClipDrawingListResponse>(
    `/api/player-analysis-clips/${playerClipId}/drawings`,
  );

  return response.data;
};

export const getPlayerAnalysisClipDrawingDetail = async (
  drawingId: number,
): Promise<PlayerAnalysisClipDrawingResponse> => {
  const response = await axiosInstance.get<PlayerAnalysisClipDrawingResponse>(
    `/api/player-analysis-clip-drawings/${drawingId}`,
  );

  return response.data;
};

export const createPlayerAnalysisClipDrawing = async (
  playerClipId: number,
  request: CreatePlayerAnalysisClipDrawingRequest,
): Promise<CreatePlayerAnalysisClipDrawingResponse> => {
  const response =
    await axiosInstance.post<CreatePlayerAnalysisClipDrawingResponse>(
      `/api/management/player-analysis-clips/${playerClipId}/drawings`,
      request,
    );

  return response.data;
};

export const updatePlayerAnalysisClipDrawing = async (
  drawingId: number,
  request: UpdatePlayerAnalysisClipDrawingRequest,
): Promise<PlayerAnalysisClipDrawingResponse> => {
  const response = await axiosInstance.patch<PlayerAnalysisClipDrawingResponse>(
    `/api/management/player-analysis-clip-drawings/${drawingId}`,
    request,
  );

  return response.data;
};

export const deletePlayerAnalysisClipDrawing = async (
  drawingId: number,
): Promise<void> => {
  await axiosInstance.delete(
    `/api/coach/player-analysis-clip-drawings/${drawingId}`,
  );
};
