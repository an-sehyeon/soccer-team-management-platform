// 선수 기록 이벤트와 이벤트-클립 연결 API 타입을 정의하는 파일

export type PlayerRecordEventType =
  | "GOAL"
  | "ASSIST"
  | "SHOT"
  | "SHOT_ON_TARGET"
  | "PASS"
  | "SUCCESSFUL_PASS"
  | "DRIBBLE"
  | "SUCCESSFUL_DRIBBLE"
  | "TACKLE"
  | "INTERCEPTION"
  | "CLEARANCE"
  | "SAVE"
  | "YELLOW_CARD"
  | "RED_CARD"
  | "ETC";

export type PlayerRecordClipSourceType = "TEAM_ANALYSIS" | "PLAYER_ANALYSIS";

/**
 * 선수 기록 이벤트와 분석 클립 연결 등록 요청
 *
 * 이벤트 시간과 value는 선택한 분석 클립을 기준으로
 * 백엔드에서 자동 결정한다.
 */
export type CreatePlayerRecordEventWithClipLinkRequest = {
  uploadId: number;
  playerId: number;
  eventType: PlayerRecordEventType;
  eventMemo: string | null;
  clipSourceType: PlayerRecordClipSourceType;
  teamClipId: number | null;
  playerClipId: number | null;
};

export type CreatePlayerRecordEventResponse = {
  eventId: number;
  recordId: number;
  uploadId: number;
  matchVideoTitle: string;
  playerId: number;
  playerName: string;
  eventType: PlayerRecordEventType;
  eventStartTimeSec: number;
  eventEndTimeSec: number;
  value: number;
  eventMemo: string | null;
  createdAt: string;
};

export type PlayerRecordEventClipResponse = {
  eventClipId: number;
  clipSourceType: PlayerRecordClipSourceType;
  teamClipId: number | null;
  teamClipTitle: string | null;
  playerClipId: number | null;
  playerClipTitle: string | null;
};

export type PlayerRecordEventResponse = {
  eventId: number;
  recordId: number;
  uploadId: number;
  matchVideoTitle: string;
  playerId: number;
  playerName: string;
  createdById: number;
  createdByName: string;
  eventType: PlayerRecordEventType;
  eventStartTimeSec: number;
  eventEndTimeSec: number;
  value: number;
  eventMemo: string | null;
  clips: PlayerRecordEventClipResponse[];
  createdAt: string;
  updatedAt: string;
};

export type PlayerRecordEventListResponse = {
  events: PlayerRecordEventResponse[];
};

export const PLAYER_RECORD_EVENT_TYPE_OPTIONS: {
  value: PlayerRecordEventType;
  label: string;
}[] = [
  { value: "GOAL", label: "득점" },
  { value: "ASSIST", label: "도움" },
  { value: "SHOT", label: "슈팅" },
  { value: "SHOT_ON_TARGET", label: "유효 슈팅" },
  { value: "PASS", label: "패스" },
  { value: "SUCCESSFUL_PASS", label: "성공 패스" },
  { value: "DRIBBLE", label: "드리블" },
  { value: "SUCCESSFUL_DRIBBLE", label: "성공 드리블" },
  { value: "TACKLE", label: "태클" },
  { value: "INTERCEPTION", label: "인터셉트" },
  { value: "CLEARANCE", label: "클리어링" },
  { value: "SAVE", label: "세이브" },
  { value: "YELLOW_CARD", label: "경고" },
  { value: "RED_CARD", label: "퇴장" },
  { value: "ETC", label: "기타" },
];

export const PLAYER_RECORD_EVENT_TYPE_LABELS: Record<
  PlayerRecordEventType,
  string
> = {
  GOAL: "득점",
  ASSIST: "도움",
  SHOT: "슈팅",
  SHOT_ON_TARGET: "유효 슈팅",
  PASS: "패스",
  SUCCESSFUL_PASS: "성공 패스",
  DRIBBLE: "드리블",
  SUCCESSFUL_DRIBBLE: "성공 드리블",
  TACKLE: "태클",
  INTERCEPTION: "인터셉트",
  CLEARANCE: "클리어링",
  SAVE: "세이브",
  YELLOW_CARD: "경고",
  RED_CARD: "퇴장",
  ETC: "기타",
};

export const PLAYER_RECORD_CLIP_SOURCE_TYPE_LABELS: Record<
  PlayerRecordClipSourceType,
  string
> = {
  TEAM_ANALYSIS: "팀 분석 클립",
  PLAYER_ANALYSIS: "선수 개인 분석 클립",
};
