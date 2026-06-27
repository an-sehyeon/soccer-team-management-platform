// 공지사항 조회, 등록, 수정, 삭제 API 요청 함수를 모아둔 파일

import { axiosInstance } from "./axiosInstance";
import type {
  CreateNoticeRequest,
  NoticeDetailResponse,
  NoticeListSearchParams,
  NoticePageResponse,
  UpdateNoticeRequest,
} from "../types/notice";

export const getNotices = async (
  params: NoticeListSearchParams,
): Promise<NoticePageResponse> => {
  const response = await axiosInstance.get<NoticePageResponse>("/api/notices", {
    params,
  });

  return response.data;
};

export const getNoticeDetail = async (
  noticeId: number,
): Promise<NoticeDetailResponse> => {
  const response = await axiosInstance.get<NoticeDetailResponse>(
    `/api/notices/${noticeId}`,
  );

  return response.data;
};

export const createNotice = async (
  request: CreateNoticeRequest,
): Promise<NoticeDetailResponse> => {
  const response = await axiosInstance.post<NoticeDetailResponse>(
    "/api/coach/notices",
    request,
  );

  return response.data;
};

export const updateNotice = async (
  noticeId: number,
  request: UpdateNoticeRequest,
): Promise<NoticeDetailResponse> => {
  const response = await axiosInstance.patch<NoticeDetailResponse>(
    `/api/coach/notices/${noticeId}`,
    request,
  );

  return response.data;
};

export const deleteNotice = async (noticeId: number): Promise<void> => {
  await axiosInstance.delete(`/api/coach/notices/${noticeId}`);
};
