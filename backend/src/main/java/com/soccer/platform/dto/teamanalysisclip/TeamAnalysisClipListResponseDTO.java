package com.soccer.platform.dto.teamanalysisclip;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.entity.TeamVideoClipEntity;

import lombok.Builder;
import lombok.Getter;

/*
 * 팀 분석 클립 목록 응답 DTO
 * 목록 화면에서 필요한 클립 요약 정보를 반환
 * 상세 재생은 teamClipUrl 기준이므로 원본 영상 구간 정보는 목록 응답에서 제외
 */
@Getter
@Builder
public class TeamAnalysisClipListResponseDTO {

    private Integer teamClipId;
    private Integer matchVideoId;
    private String matchVideoTitle;
    private TeamVideoClipTypeEnum clipType;
    private String title;
    private String comment;
    private VideoUploadStatusEnum status;
    private Integer editorId;
    private String editorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TeamAnalysisClipListResponseDTO from(TeamVideoClipEntity teamClip) {
        return TeamAnalysisClipListResponseDTO.builder()
                .teamClipId(teamClip.getId())
                .matchVideoId(teamClip.getGameVideoUpload().getId())
                .matchVideoTitle(teamClip.getGameVideoUpload().getTitle())
                .clipType(teamClip.getClipType())
                .title(teamClip.getTitle())
                .comment(teamClip.getComment())
                .status(teamClip.getStatus())
                .editorId(teamClip.getMember().getId())
                .editorName(teamClip.getMember().getName())
                .createdAt(teamClip.getCreatedAt())
                .updatedAt(teamClip.getUpdatedAt())
                .build();
    }
}