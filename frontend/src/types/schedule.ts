// 스케줄 화면과 API 연동에서 사용하는 요청, 응답, 검색 조건 타입을 정의하는 파일

export type ScheduleType =
  | "TRAINING"
  | "MATCH"
  | "MEETING"
  | "EVENT"
  | "EXTERNAL"
  | "ETC";

export type ScheduleIntensity = "HIGH" | "MEDIUM" | "LOW";

export type ScheduleResponse = {
  scheduleId: number;
  writerMemberId: number;
  writerName: string;
  scheduleDateTime: string;
  place: string;
  scheduleType: ScheduleType;
  comment: string | null;
  intensity: ScheduleIntensity | null;
  createdAt: string;
  updatedAt: string;
};

export type ScheduleRequest = {
  scheduleDateTime: string;
  place: string;
  scheduleType: ScheduleType;
  intensity: ScheduleIntensity | null;
  comment: string | null;
};

export type CreateScheduleResponse = {
  scheduleId: number;
  message: string;
};

export type ScheduleSearchParams = {
  startDate: string;
  endDate: string;
};
