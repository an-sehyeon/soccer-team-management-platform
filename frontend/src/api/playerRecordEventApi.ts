// 선수 기록 이벤트와 이벤트-클립 연결 API 요청을 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  CreatePlayerRecordEventRequest,
  CreatePlayerRecordEventResponse,
  CreatePlayerRecordEventWithClipLinkRequest,
  PlayerRecordEventListResponse,
  PlayerRecordEventResponse,
  UpdatePlayerRecordEventRequest,
} from "../types/playerRecordEvent";

// 관리용 선수 기록 이벤트 등록
export async function createPlayerRecordEvent(
  request: CreatePlayerRecordEventRequest,
): Promise<CreatePlayerRecordEventResponse> {
  const response = await axiosInstance.post(
    "/api/management/player-record-events",
    request,
  );

  return response.data;
}

// 관리용 선수 기록 이벤트 + 클립 연결 등록
export async function createPlayerRecordEventWithClipLink(
  request: CreatePlayerRecordEventWithClipLinkRequest,
): Promise<CreatePlayerRecordEventResponse> {
  const response = await axiosInstance.post(
    "/api/management/player-record-events/with-clip-link",
    request,
  );

  return response.data;
}

// 관리용 특정 선수 기록의 이벤트 목록 조회
export async function getManagementPlayerRecordEvents(
  recordId: number,
): Promise<PlayerRecordEventListResponse> {
  const response = await axiosInstance.get(
    `/api/management/player-records/${recordId}/events`,
  );

  return response.data;
}

// 관리용 선수 기록 이벤트 상세 조회
export async function getManagementPlayerRecordEventDetail(
  eventId: number,
): Promise<PlayerRecordEventResponse> {
  const response = await axiosInstance.get(
    `/api/management/player-record-events/${eventId}`,
  );

  return response.data;
}

// 관리용 선수 기록 이벤트 수정
export async function updatePlayerRecordEvent(
  eventId: number,
  request: UpdatePlayerRecordEventRequest,
): Promise<PlayerRecordEventResponse> {
  const response = await axiosInstance.patch(
    `/api/management/player-record-events/${eventId}`,
    request,
  );

  return response.data;
}

// 관리용 선수 기록 이벤트 삭제
export async function deletePlayerRecordEvent(eventId: number): Promise<void> {
  await axiosInstance.delete(`/api/management/player-record-events/${eventId}`);
}

// 선수 본인 특정 기록의 이벤트 목록 조회
export async function getMyPlayerRecordEvents(
  recordId: number,
): Promise<PlayerRecordEventListResponse> {
  const response = await axiosInstance.get(
    `/api/player/me/player-records/${recordId}/events`,
  );

  return response.data;
}

// 선수 본인 기록 이벤트 상세 조회
export async function getMyPlayerRecordEventDetail(
  eventId: number,
): Promise<PlayerRecordEventResponse> {
  const response = await axiosInstance.get(
    `/api/player/me/player-record-events/${eventId}`,
  );

  return response.data;
}
