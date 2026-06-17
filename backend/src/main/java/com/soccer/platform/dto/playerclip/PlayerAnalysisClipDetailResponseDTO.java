package com.soccer.platform.dto.playerclip;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.PlayerClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 개인 분석 클립 상세 응답 DTO

@Getter
@AllArgsConstructor
public class PlayerAnalysisClipDetailResponseDTO {
	
	private Integer playerClipId;
    private Integer matchVideoId;
    private String matchVideoTitle;
    private String matchVideoUrl;
    private Integer playerId;
    private String playerName;
    private PlayerClipTypeEnum clipType;
    private String title;
    private String comment;
    private Integer startTimeSec;
    private Integer endTimeSec;
    private VideoUploadStatusEnum status;
    private Integer editorId;
    private String editorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
