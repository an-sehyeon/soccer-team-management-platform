// 영상 북마크 등록, 조회, 수정 API 타입을 정의하는 파일

export type CreateVideoBookmarkRequest = {
  matchVideoId: number;
  teamClipId: number | null;
  playerClipId: number | null;
  bookmarkTimeSec: number;
  title: string;
  memo: string | null;
};

export type UpdateVideoBookmarkRequest = {
  bookmarkTimeSec: number;
  title: string;
  memo: string | null;
};

export type VideoBookmarkResponse = {
  bookmarkId: number;
  matchVideoId: number;
  teamClipId: number | null;
  playerClipId: number | null;
  bookmarkTimeSec: number;
  title: string;
  memo: string | null;
  createdAt: string;
  updatedAt: string;
};

export type GetVideoBookmarksParams = {
  matchVideoId: number;
  teamClipId?: number;
  playerClipId?: number;
};
