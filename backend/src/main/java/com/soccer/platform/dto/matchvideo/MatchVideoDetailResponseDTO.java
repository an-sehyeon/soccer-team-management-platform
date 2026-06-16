package com.soccer.platform.dto.matchvideo;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.MatchResultEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.entity.GameVideoUploadEntity;

import lombok.Builder;
import lombok.Getter;

/*
 * 경기 영상 상세 응답 DTO
 * 상세 조회와 영상 재생에 필요한 URL을 포함한다.
 */
@Getter
@Builder
public class MatchVideoDetailResponseDTO {

    private Integer matchVideoId;
    private String url;
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

    public static MatchVideoDetailResponseDTO from(GameVideoUploadEntity matchVideo) {
        return MatchVideoDetailResponseDTO.builder()
                .matchVideoId(matchVideo.getId())
                .url(matchVideo.getUrl())
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