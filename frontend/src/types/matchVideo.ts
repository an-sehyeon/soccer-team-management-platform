// 경기 영상 프론트 타입을 정의하는 파일

export type MatchResult = "WIN" | "DRAW" | "LOSS";

export type MatchVideoStatus = "UPLOADING" | "READY" | "FAILED";

export type MatchVideoListItem = {
  matchVideoId: number;
  title: string;
  gameDate: string;
  place: string;
  homeScore: number;
  awayScore: number;
  matchResult: MatchResult;
  status: MatchVideoStatus;
  durationSec: number | null;
  createdAt: string;
};

export type MatchVideoPageResponse = {
  matchVideos: MatchVideoListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type MatchVideoDetailResponse = {
  matchVideoId: number;
  url: string;
  title: string;
  gameDate: string;
  place: string;
  homeScore: number;
  awayScore: number;
  matchResult: MatchResult;
  status: MatchVideoStatus;
  durationSec: number | null;
  uploaderId: number;
  uploaderName: string;
  createdAt: string;
  updatedAt: string;
};

export type CreateMatchVideoRequest = {
  videoFile: File;
  title: string;
  gameDate: string;
  place: string;
  homeScore: number;
  awayScore: number;
  matchResult: MatchResult;
};

export type CreateMatchVideoResponse = {
  matchVideoId: number;
  message: string;
};

export type UpdateMatchVideoRequest = {
  title: string;
  gameDate: string;
  place: string;
  homeScore: number;
  awayScore: number;
  matchResult: MatchResult;
};

export type DeleteMatchVideoResponse = {
  message: string;
};
