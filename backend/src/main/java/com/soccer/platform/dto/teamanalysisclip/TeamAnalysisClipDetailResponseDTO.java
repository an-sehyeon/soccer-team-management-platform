package com.soccer.platform.dto.teamanalysisclip;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.entity.TeamVideoClipEntity;

import lombok.Builder;
import lombok.Getter;

/*
 * 팀 분석 클립 상세 응답 DTO
 * 원본 영상 URL과 클립 시간 정보를 함께 반환한다.
 */
@Getter
@Builder
public class TeamAnalysisClipDetailResponseDTO {

    private Integer teamClipId;

    private Integer matchVideoId;
    private String matchVideoUrl;
    private String matchVideoTitle;
    private Integer matchVideoDurationSec;

    private TeamVideoClipTypeEnum clipType;
    private String title;
    private String comment;
    private Integer startTimeSec;
    private Integer endTimeSec;
    private VideoUploadStatusEnum status;

    private Integer editorId;
    private String editorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TeamAnalysisClipDetailResponseDTO from(TeamVideoClipEntity teamClip) {
        return TeamAnalysisClipDetailResponseDTO.builder()
                .teamClipId(teamClip.getId())
                .matchVideoId(teamClip.getGameVideoUpload().getId())
                .matchVideoUrl(teamClip.getGameVideoUpload().getUrl())
                .matchVideoTitle(teamClip.getGameVideoUpload().getTitle())
                .matchVideoDurationSec(teamClip.getGameVideoUpload().getDurationSec())
                .clipType(teamClip.getClipType())
                .title(teamClip.getTitle())
                .comment(teamClip.getComment())
                .startTimeSec(teamClip.getStartTimeSec())
                .endTimeSec(teamClip.getEndTimeSec())
                .status(teamClip.getStatus())
                .editorId(teamClip.getMember().getId())
                .editorName(teamClip.getMember().getName())
                .createdAt(teamClip.getCreatedAt())
                .updatedAt(teamClip.getUpdatedAt())
                .build();
    }
}