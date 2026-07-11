// 선수 기록 관리 API 요청/응답 타입을 정의하는 파일

export type PlayerRecordListItem = {
  recordId: number;
  uploadId: number;
  matchVideoTitle: string;
  playerId: number;
  playerName: string;
  recorderId: number;
  recorderName: string;
  lastModifierId: number | null;
  lastModifierName: string | null;
  createdAt: string;
  updatedAt: string;
};

export type PlayerRecordPageResponse = {
  records: PlayerRecordListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PlayerRecordDetailResponse = PlayerRecordListItem & {
  minutesPlayed: number;
  goals: number;
  assists: number;
  shots: number;
  shotsOnTarget: number;
  passes: number;
  successfulPasses: number;
  dribbles: number;
  successfulDribbles: number;
  tackles: number;
  interceptions: number;
  clearances: number;
  saves: number;
  yellowCards: number;
  redCards: number;
  memo: string | null;
};

export type CreatePlayerRecordRequest = {
  uploadId: number;
  playerId: number;
  minutesPlayed: number;
  goals: number;
  assists: number;
  shots: number;
  shotsOnTarget: number;
  passes: number;
  successfulPasses: number;
  dribbles: number;
  successfulDribbles: number;
  tackles: number;
  interceptions: number;
  clearances: number;
  saves: number;
  yellowCards: number;
  redCards: number;
  memo: string | null;
};

export type UpdatePlayerRecordRequest = CreatePlayerRecordRequest;

export type CreatePlayerRecordResponse = {
  recordId: number;
  message: string;
};

export type PlayerRecordSearchParams = {
  page: number;
  size: number;
  uploadId?: number;
  playerId?: number;
};

export const PLAYER_RECORD_STAT_LABELS: {
  key: keyof Pick<
    PlayerRecordDetailResponse,
    | "minutesPlayed"
    | "goals"
    | "assists"
    | "shots"
    | "shotsOnTarget"
    | "passes"
    | "successfulPasses"
    | "dribbles"
    | "successfulDribbles"
    | "tackles"
    | "interceptions"
    | "clearances"
    | "saves"
    | "yellowCards"
    | "redCards"
  >;
  label: string;
}[] = [
  { key: "minutesPlayed", label: "출전 시간" },
  { key: "goals", label: "득점" },
  { key: "assists", label: "도움" },
  { key: "shots", label: "슈팅" },
  { key: "shotsOnTarget", label: "유효 슈팅" },
  { key: "passes", label: "패스" },
  { key: "successfulPasses", label: "성공 패스" },
  { key: "dribbles", label: "드리블" },
  { key: "successfulDribbles", label: "성공 드리블" },
  { key: "tackles", label: "태클" },
  { key: "interceptions", label: "인터셉트" },
  { key: "clearances", label: "클리어링" },
  { key: "saves", label: "세이브" },
  { key: "yellowCards", label: "경고" },
  { key: "redCards", label: "퇴장" },
];
