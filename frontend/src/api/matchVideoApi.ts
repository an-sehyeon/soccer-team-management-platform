// 경기 영상 API 요청을 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  CreateMatchVideoRequest,
  CreateMatchVideoResponse,
  DeleteMatchVideoResponse,
  MatchVideoDetailResponse,
  MatchVideoPageResponse,
  UpdateMatchVideoRequest,
} from "../types/matchVideo";

// 경기 영상 목록 조회
export async function getMatchVideos(page = 0, size = 20) {
  const response = await axiosInstance.get<MatchVideoPageResponse>(
    "/api/match-videos",
    {
      params: {
        page,
        size,
      },
    },
  );

  return response.data;
}

// 경기 영상 상세 조회
export async function getMatchVideoDetail(matchVideoId: number) {
  const response = await axiosInstance.get<MatchVideoDetailResponse>(
    `/api/match-videos/${matchVideoId}`,
  );

  return response.data;
}

// 경기 영상 업로드
export async function createMatchVideo(request: CreateMatchVideoRequest) {
  const formData = new FormData();

  formData.append("videoFile", request.videoFile);
  formData.append("title", request.title);
  formData.append("gameDate", request.gameDate);
  formData.append("place", request.place);
  formData.append("homeScore", String(request.homeScore));
  formData.append("awayScore", String(request.awayScore));
  formData.append("matchResult", request.matchResult);

  const response = await axiosInstance.post<CreateMatchVideoResponse>(
    "/api/management/match-videos",
    formData,
  );

  return response.data;
}

// 경기 영상 메타데이터 수정
export async function updateMatchVideo(
  matchVideoId: number,
  request: UpdateMatchVideoRequest,
) {
  const response = await axiosInstance.patch<MatchVideoDetailResponse>(
    `/api/management/match-videos/${matchVideoId}`,
    request,
  );

  return response.data;
}

// 경기 영상 삭제
export async function deleteMatchVideo(matchVideoId: number) {
  const response = await axiosInstance.delete<DeleteMatchVideoResponse>(
    `/api/coach/match-videos/${matchVideoId}`,
  );

  return response.data;
}
