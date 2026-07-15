package com.soccer.platform.dto.videobookmark;

import java.time.LocalDateTime;

import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.entity.VideoBookmarkEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * 영상 북마크 응답 DTO
 * 북마크 등록, 목록 조회, 수정 응답에 공통으로 사용
 * teamClipId와 playerClipId의 존재 여부로
 * 북마크 대상 영상 종류를 판단할 수 있다.
 */
@Getter
@AllArgsConstructor
public class VideoBookmarkResponseDTO {

    private Integer bookmarkId;
    private Integer matchVideoId;
    private Integer teamClipId;
    private Integer playerClipId;
    private Integer bookmarkTimeSec;
    private String title;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /*
     * 영상 북마크 Entity를 API 응답 DTO로 변환
     * 경기 원본 영상 북마크는 teamClipId와 playerClipId가 모두 null이다.
     * 팀 분석 클립 북마크는 teamClipId만 값을 가진다.
     * 선수 개인 분석 클립 북마크는 playerClipId만 값을 가진다.
     */
    public static VideoBookmarkResponseDTO from(
        VideoBookmarkEntity videoBookmark
    ) {
        TeamVideoClipEntity teamVideoClip =
            videoBookmark.getTeamVideoClip();

        PlayerVideoClipEntity playerVideoClip =
            videoBookmark.getPlayerVideoClip();

        return new VideoBookmarkResponseDTO(
            videoBookmark.getId(),
            videoBookmark.getGameVideoUpload().getId(),
            teamVideoClip != null ? teamVideoClip.getId() : null,
            playerVideoClip != null ? playerVideoClip.getId() : null,
            videoBookmark.getBookmarkTimeSec(),
            videoBookmark.getTitle(),
            videoBookmark.getMemo(),
            videoBookmark.getCreatedAt(),
            videoBookmark.getUpdatedAt()
        );
    }
}