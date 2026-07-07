package com.soccer.platform.dto.teamanalysisclip;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.entity.TeamVideoClipEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 팀 분석 클립 상세 조회 응답 DTO
@Getter
@AllArgsConstructor
public class TeamAnalysisClipDetailResponseDTO {

    private Integer teamClipId;
    private Integer matchVideoId;
    private String matchVideoTitle;
    private TeamVideoClipTypeEnum clipType;
    private String title;
    private String comment;
    private Integer startTimeSec;
    private Integer endTimeSec;
    private String teamClipUrl;
    private VideoUploadStatusEnum status;
    private Integer editorId;
    private String editorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TeamAnalysisClipDetailResponseDTO from(TeamVideoClipEntity teamClip) {
        return new TeamAnalysisClipDetailResponseDTO(
                teamClip.getId(),
                teamClip.getGameVideoUpload().getId(),
                teamClip.getGameVideoUpload().getTitle(),
                teamClip.getClipType(),
                teamClip.getTitle(),
                teamClip.getComment(),
                teamClip.getStartTimeSec(),
                teamClip.getEndTimeSec(),
                teamClip.getUrl(),
                teamClip.getStatus(),
                teamClip.getMember().getId(),
                teamClip.getMember().getName(),
                teamClip.getCreatedAt(),
                teamClip.getUpdatedAt()
        );
    }
}