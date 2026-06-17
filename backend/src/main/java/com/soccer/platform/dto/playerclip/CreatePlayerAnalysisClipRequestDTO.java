package com.soccer.platform.dto.playerclip;

import com.soccer.platform.common.constants.PlayerClipTypeEnum;

import lombok.Getter;
import lombok.Setter;

// 선수 개인 분석 클립 등록 요청 DTO

@Getter
@Setter
public class CreatePlayerAnalysisClipRequestDTO {
	
	private Integer matchVideoId;
    private Integer playerId;
    private PlayerClipTypeEnum clipType;
    private String title;
    private String comment;
    private Integer startTimeSec;
    private Integer endTimeSec;

}
