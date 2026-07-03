package com.soccer.platform.dto.playerclip;

import com.fasterxml.jackson.databind.JsonNode;
import com.soccer.platform.common.constants.DrawingTypeEnum;

import lombok.Getter;
import lombok.Setter;

// 선수 개인 분석 클립 통합 생성 요청에 포함되는 드로잉 항목 DTO
@Getter
@Setter
public class CreatePlayerAnalysisClipDrawingItemRequestDTO {

	private DrawingTypeEnum drawingType;
	private Integer startTimeSec;
	private Integer endTimeSec;
	private JsonNode drawingData;

}