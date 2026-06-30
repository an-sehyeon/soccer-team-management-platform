// 선수 개인 분석 클립 관련 백엔드 API 호출 함수를 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  CreatePlayerAnalysisClipRequest,
  CreatePlayerAnalysisClipResponse,
  PlayerAnalysisClipDetailResponse,
  PlayerAnalysisClipPageResponse,
  PlayerAnalysisClipSearchParams,
  PlayerSelectItem,
  UpdatePlayerAnalysisClipRequest,
} from "../types/playerAnalysisClip";

// 관리용 선수 선택 목록 조회
export const getManagementPlayers = async (): Promise<PlayerSelectItem[]> => {
  const response = await axiosInstance.get<PlayerSelectItem[]>(
    "/api/management/players",
  );

  return response.data;
};

// 관리용 선수 개인 분석 클립 목록 조회
export const getManagementPlayerAnalysisClips = async (
  params: PlayerAnalysisClipSearchParams,
): Promise<PlayerAnalysisClipPageResponse> => {
  const response = await axiosInstance.get<PlayerAnalysisClipPageResponse>(
    "/api/management/player-analysis-clips",
    {
      params,
    },
  );

  return response.data;
};

// 선수 본인 개인 분석 클립 목록 조회
export const getMyPlayerAnalysisClips = async (
  params: Omit<PlayerAnalysisClipSearchParams, "playerId">,
): Promise<PlayerAnalysisClipPageResponse> => {
  const response = await axiosInstance.get<PlayerAnalysisClipPageResponse>(
    "/api/player/me/player-analysis-clips",
    {
      params,
    },
  );

  return response.data;
};

// 관리용 선수 개인 분석 클립 상세 조회
export const getManagementPlayerAnalysisClipDetail = async (
  playerClipId: number,
): Promise<PlayerAnalysisClipDetailResponse> => {
  const response = await axiosInstance.get<PlayerAnalysisClipDetailResponse>(
    `/api/management/player-analysis-clips/${playerClipId}`,
  );

  return response.data;
};

// 선수 본인 개인 분석 클립 상세 조회
export const getMyPlayerAnalysisClipDetail = async (
  playerClipId: number,
): Promise<PlayerAnalysisClipDetailResponse> => {
  const response = await axiosInstance.get<PlayerAnalysisClipDetailResponse>(
    `/api/player/me/player-analysis-clips/${playerClipId}`,
  );

  return response.data;
};

// 선수 개인 분석 클립 등록
export const createPlayerAnalysisClip = async (
  request: CreatePlayerAnalysisClipRequest,
): Promise<CreatePlayerAnalysisClipResponse> => {
  const response = await axiosInstance.post<CreatePlayerAnalysisClipResponse>(
    "/api/management/player-analysis-clips",
    request,
  );

  return response.data;
};

// 선수 개인 분석 클립 수정
export const updatePlayerAnalysisClip = async (
  playerClipId: number,
  request: UpdatePlayerAnalysisClipRequest,
): Promise<PlayerAnalysisClipDetailResponse> => {
  const response = await axiosInstance.patch<PlayerAnalysisClipDetailResponse>(
    `/api/management/player-analysis-clips/${playerClipId}`,
    request,
  );

  return response.data;
};

// 선수 개인 분석 클립 삭제
export const deletePlayerAnalysisClip = async (
  playerClipId: number,
): Promise<void> => {
  await axiosInstance.delete(
    `/api/coach/player-analysis-clips/${playerClipId}`,
  );
};
