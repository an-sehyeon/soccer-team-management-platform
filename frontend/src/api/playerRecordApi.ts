// 선수 기록 관리 API 요청을 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  CreatePlayerRecordRequest,
  CreatePlayerRecordResponse,
  PlayerRecordDetailResponse,
  PlayerRecordPageResponse,
  PlayerRecordSearchParams,
  UpdatePlayerRecordRequest,
} from "../types/playerRecord";

// 관리용 선수 기록 등록
export async function createPlayerRecord(
  request: CreatePlayerRecordRequest,
): Promise<CreatePlayerRecordResponse> {
  const response = await axiosInstance.post(
    "/api/management/player-records",
    request,
  );

  return response.data;
}

// 관리용 선수 기록 목록 조회
export async function getManagementPlayerRecords(
  params: PlayerRecordSearchParams,
): Promise<PlayerRecordPageResponse> {
  const response = await axiosInstance.get("/api/management/player-records", {
    params,
  });

  return response.data;
}

// 관리용 선수 기록 상세 조회
export async function getManagementPlayerRecordDetail(
  recordId: number,
): Promise<PlayerRecordDetailResponse> {
  const response = await axiosInstance.get(
    `/api/management/player-records/${recordId}`,
  );

  return response.data;
}

// 관리용 선수 기록 수정
export async function updatePlayerRecord(
  recordId: number,
  request: UpdatePlayerRecordRequest,
): Promise<PlayerRecordDetailResponse> {
  const response = await axiosInstance.patch(
    `/api/management/player-records/${recordId}`,
    request,
  );

  return response.data;
}

// 관리용 선수 기록 삭제
export async function deletePlayerRecord(recordId: number): Promise<void> {
  await axiosInstance.delete(`/api/management/player-records/${recordId}`);
}

// 선수 본인 기록 목록 조회
export async function getMyPlayerRecords(
  page = 0,
  size = 20,
): Promise<PlayerRecordPageResponse> {
  const response = await axiosInstance.get("/api/player/me/player-records", {
    params: {
      page,
      size,
    },
  });

  return response.data;
}

// 선수 본인 기록 상세 조회
export async function getMyPlayerRecordDetail(
  recordId: number,
): Promise<PlayerRecordDetailResponse> {
  const response = await axiosInstance.get(
    `/api/player/me/player-records/${recordId}`,
  );

  return response.data;
}
