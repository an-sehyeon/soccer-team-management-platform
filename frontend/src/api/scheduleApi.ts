// 스케줄 조회, 등록, 수정, 삭제 API 요청 함수를 모아둔 파일

import { axiosInstance } from "./axiosInstance";
import type {
  CreateScheduleResponse,
  ScheduleRequest,
  ScheduleResponse,
  ScheduleSearchParams,
} from "../types/schedule";

export const getSchedules = async (
  params: ScheduleSearchParams,
): Promise<ScheduleResponse[]> => {
  const response = await axiosInstance.get<ScheduleResponse[]>(
    "/api/schedules",
    {
      params,
    },
  );

  return response.data;
};

export const getScheduleDetail = async (
  scheduleId: number,
): Promise<ScheduleResponse> => {
  const response = await axiosInstance.get<ScheduleResponse>(
    `/api/schedules/${scheduleId}`,
  );

  return response.data;
};

export const createSchedule = async (
  request: ScheduleRequest,
): Promise<CreateScheduleResponse> => {
  const response = await axiosInstance.post<CreateScheduleResponse>(
    "/api/coach/schedules",
    request,
  );

  return response.data;
};

export const updateSchedule = async (
  scheduleId: number,
  request: ScheduleRequest,
): Promise<ScheduleResponse> => {
  const response = await axiosInstance.patch<ScheduleResponse>(
    `/api/coach/schedules/${scheduleId}`,
    request,
  );

  return response.data;
};

export const deleteSchedule = async (scheduleId: number): Promise<void> => {
  await axiosInstance.delete(`/api/coach/schedules/${scheduleId}`);
};
