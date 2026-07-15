// 영상 북마크 등록, 조회, 수정, 삭제 API 요청을 관리하는 파일

import { axiosInstance } from "./axiosInstance";

import type {
  CreateVideoBookmarkRequest,
  GetVideoBookmarksParams,
  UpdateVideoBookmarkRequest,
  VideoBookmarkResponse,
} from "../types/videoBookmark";

/**
 * 현재 재생 중인 영상에 개인 북마크를 등록한다.
 */
export async function createVideoBookmark(
  request: CreateVideoBookmarkRequest,
): Promise<VideoBookmarkResponse> {
  const response = await axiosInstance.post<VideoBookmarkResponse>(
    "/api/management/video-bookmarks",
    request,
  );

  return response.data;
}

/**
 * 현재 재생 중인 영상에서 작성한 본인의 북마크 목록을 조회한다.
 *
 * teamClipId와 playerClipId가 모두 없으면 경기 원본 영상 북마크를 조회한다.
 */
export async function getVideoBookmarks(
  params: GetVideoBookmarksParams,
): Promise<VideoBookmarkResponse[]> {
  const response = await axiosInstance.get<VideoBookmarkResponse[]>(
    "/api/management/video-bookmarks",
    {
      params: {
        matchVideoId: params.matchVideoId,
        teamClipId: params.teamClipId,
        playerClipId: params.playerClipId,
      },
    },
  );

  return response.data;
}

/**
 * 작성자 본인의 북마크 제목, 메모, 시간을 수정한다.
 */
export async function updateVideoBookmark(
  bookmarkId: number,
  request: UpdateVideoBookmarkRequest,
): Promise<VideoBookmarkResponse> {
  const response = await axiosInstance.patch<VideoBookmarkResponse>(
    `/api/management/video-bookmarks/${bookmarkId}`,
    request,
  );

  return response.data;
}

/**
 * 작성자 본인의 북마크를 소프트 삭제한다.
 */
export async function deleteVideoBookmark(bookmarkId: number): Promise<void> {
  await axiosInstance.delete(`/api/management/video-bookmarks/${bookmarkId}`);
}
