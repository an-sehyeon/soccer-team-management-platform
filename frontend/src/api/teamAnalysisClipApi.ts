// 팀 분석 클립 관련 백엔드 API 호출 함수를 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  CreateTeamAnalysisClipRequest,
  CreateTeamAnalysisClipResponse,
  TeamAnalysisClipDetailResponse,
  TeamAnalysisClipPageResponse,
  TeamAnalysisClipSearchParams,
  UpdateTeamAnalysisClipRequest,
} from "../types/teamAnalysisClip";

export const getTeamAnalysisClips = async (
  params: TeamAnalysisClipSearchParams,
): Promise<TeamAnalysisClipPageResponse> => {
  const response = await axiosInstance.get<TeamAnalysisClipPageResponse>(
    "/api/team-analysis-clips",
    {
      params,
    },
  );

  return response.data;
};

export const getTeamAnalysisClipDetail = async (
  teamClipId: number,
): Promise<TeamAnalysisClipDetailResponse> => {
  const response = await axiosInstance.get<TeamAnalysisClipDetailResponse>(
    `/api/team-analysis-clips/${teamClipId}`,
  );

  return response.data;
};

export const createTeamAnalysisClip = async (
  request: CreateTeamAnalysisClipRequest,
): Promise<CreateTeamAnalysisClipResponse> => {
  const response = await axiosInstance.post<CreateTeamAnalysisClipResponse>(
    "/api/management/team-analysis-clips",
    request,
  );

  return response.data;
};

export const updateTeamAnalysisClip = async (
  teamClipId: number,
  request: UpdateTeamAnalysisClipRequest,
): Promise<TeamAnalysisClipDetailResponse> => {
  const response = await axiosInstance.patch<TeamAnalysisClipDetailResponse>(
    `/api/management/team-analysis-clips/${teamClipId}`,
    request,
  );

  return response.data;
};

export const deleteTeamAnalysisClip = async (
  teamClipId: number,
): Promise<void> => {
  await axiosInstance.delete(`/api/coach/team-analysis-clips/${teamClipId}`);
};
