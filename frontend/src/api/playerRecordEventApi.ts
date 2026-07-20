// 선수 기록 이벤트와 이벤트-클립 연결 API 요청을 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  CreatePlayerRecordEventResponse,
  CreatePlayerRecordEventWithClipLinkRequest,
  PlayerRecordEventListResponse,
  PlayerRecordEventResponse,
} from "../types/playerRecordEvent";

/**
 * 선수 기록 이벤트와 분석 클립 연결 등록
 *
 * 이벤트 시간과 value는 요청하지 않으며,
 * 선택한 분석 클립을 기준으로 백엔드에서 자동 저장한다.
 */
export async function createPlayerRecordEventWithClipLink(
  request: CreatePlayerRecordEventWithClipLinkRequest,
): Promise<CreatePlayerRecordEventResponse> {
  const response = await axiosInstance.post<CreatePlayerRecordEventResponse>(
    "/api/management/player-record-events/with-clip-link",
    request,
  );

  return response.data;
}

// 관리용 특정 선수 기록의 이벤트 목록 조회
export async function getManagementPlayerRecordEvents(
  recordId: number,
): Promise<PlayerRecordEventListResponse> {
  const response = await axiosInstance.get<PlayerRecordEventListResponse>(
    `/api/management/player-records/${recordId}/events`,
  );

  return response.data;
}

// 관리용 선수 기록 이벤트 상세 조회
export async function getManagementPlayerRecordEventDetail(
  eventId: number,
): Promise<PlayerRecordEventResponse> {
  const response = await axiosInstance.get<PlayerRecordEventResponse>(
    `/api/management/player-record-events/${eventId}`,
  );

  return response.data;
}

// 선수 본인 특정 기록의 이벤트 목록 조회
export async function getMyPlayerRecordEvents(
  recordId: number,
): Promise<PlayerRecordEventListResponse> {
  const response = await axiosInstance.get<PlayerRecordEventListResponse>(
    `/api/player/me/player-records/${recordId}/events`,
  );

  return response.data;
}

// 선수 본인 기록 이벤트 상세 조회
export async function getMyPlayerRecordEventDetail(
  eventId: number,
): Promise<PlayerRecordEventResponse> {
  const response = await axiosInstance.get<PlayerRecordEventResponse>(
    `/api/player/me/player-record-events/${eventId}`,
  );

  return response.data;
}
