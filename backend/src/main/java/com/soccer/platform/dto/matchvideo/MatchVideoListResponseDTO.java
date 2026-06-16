package com.soccer.platform.dto.matchvideo;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.MatchResultEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.entity.GameVideoUploadEntity;

import lombok.Builder;
import lombok.Getter;

/*
 * 경기 영상 목록 응답 DTO
 * 목록 화면에서 필요한 요약 정보만 반환
 */
@Getter
@Builder
public class MatchVideoListResponseDTO {
    private Integer matchVideoId;
    private Integer durationSec;
    private String title;
    private LocalDateTime gameDate;
    private String place;
    private Integer homeScore;
    private Integer awayScore;
    private MatchResultEnum matchResult;
    private VideoUploadStatusEnum status;
    private Integer uploaderId;
    private String uploaderName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MatchVideoListResponseDTO from(GameVideoUploadEntity matchVideo) {
        return MatchVideoListResponseDTO.builder()
                .matchVideoId(matchVideo.getId())
                .durationSec(matchVideo.getDurationSec())
                .title(matchVideo.getTitle())
                .gameDate(matchVideo.getGameDate())
                .place(matchVideo.getPlace())
                .homeScore(matchVideo.getHomeScore())
                .awayScore(matchVideo.getAwayScore())
                .matchResult(matchVideo.getMatchResult())
                .status(matchVideo.getStatus())
                .uploaderId(matchVideo.getMember().getId())
                .uploaderName(matchVideo.getMember().getName())
                .createdAt(matchVideo.getCreatedAt())
                .updatedAt(matchVideo.getUpdatedAt())
                .build();
    }
}