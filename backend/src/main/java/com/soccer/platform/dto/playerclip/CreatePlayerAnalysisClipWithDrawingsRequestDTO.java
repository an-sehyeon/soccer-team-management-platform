package com.soccer.platform.dto.playerclip;

import java.util.List;

import com.soccer.platform.common.constants.PlayerClipTypeEnum;

import lombok.Getter;
import lombok.Setter;

// 선수 개인 분석 클립과 드로잉 통합 생성 요청 DTO
@Getter
@Setter
public class CreatePlayerAnalysisClipWithDrawingsRequestDTO {

	private Integer matchVideoId;
	private Integer playerId;
	private PlayerClipTypeEnum clipType;
	private String title;
	private String comment;
	private Integer startTimeSec;
	private Integer endTimeSec;
	private List<CreatePlayerAnalysisClipDrawingItemRequestDTO> drawings;

}