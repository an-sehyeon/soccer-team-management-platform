// 팀 분석 클립 드로잉 관련 백엔드 API 호출 함수를 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  CreateTeamAnalysisClipDrawingRequest,
  CreateTeamAnalysisClipDrawingResponse,
  TeamAnalysisClipDrawingListResponse,
  TeamAnalysisClipDrawingResponse,
  UpdateTeamAnalysisClipDrawingRequest,
} from "../types/teamAnalysisClipDrawing";

export const getTeamAnalysisClipDrawings = async (
  teamClipId: number,
): Promise<TeamAnalysisClipDrawingListResponse> => {
  const response = await axiosInstance.get<TeamAnalysisClipDrawingListResponse>(
    `/api/team-analysis-clips/${teamClipId}/drawings`,
  );

  return response.data;
};

export const getTeamAnalysisClipDrawingDetail = async (
  drawingId: number,
): Promise<TeamAnalysisClipDrawingResponse> => {
  const response = await axiosInstance.get<TeamAnalysisClipDrawingResponse>(
    `/api/team-analysis-clip-drawings/${drawingId}`,
  );

  return response.data;
};

export const createTeamAnalysisClipDrawing = async (
  teamClipId: number,
  request: CreateTeamAnalysisClipDrawingRequest,
): Promise<CreateTeamAnalysisClipDrawingResponse> => {
  const response =
    await axiosInstance.post<CreateTeamAnalysisClipDrawingResponse>(
      `/api/management/team-analysis-clips/${teamClipId}/drawings`,
      request,
    );

  return response.data;
};

export const updateTeamAnalysisClipDrawing = async (
  drawingId: number,
  request: UpdateTeamAnalysisClipDrawingRequest,
): Promise<TeamAnalysisClipDrawingResponse> => {
  const response = await axiosInstance.patch<TeamAnalysisClipDrawingResponse>(
    `/api/management/team-analysis-clip-drawings/${drawingId}`,
    request,
  );

  return response.data;
};

export const deleteTeamAnalysisClipDrawing = async (
  drawingId: number,
): Promise<void> => {
  await axiosInstance.delete(
    `/api/coach/team-analysis-clip-drawings/${drawingId}`,
  );
};
