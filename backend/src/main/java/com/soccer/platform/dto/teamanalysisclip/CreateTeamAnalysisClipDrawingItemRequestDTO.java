package com.soccer.platform.dto.teamanalysisclip;

import com.fasterxml.jackson.databind.JsonNode;
import com.soccer.platform.common.constants.DrawingTypeEnum;

import lombok.Getter;
import lombok.Setter;

// 팀 분석 클립 통합 생성 요청에 포함되는 드로잉 항목 DTO

@Getter
@Setter
public class CreateTeamAnalysisClipDrawingItemRequestDTO {

    private DrawingTypeEnum drawingType;
    private Integer startTimeSec;
    private Integer endTimeSec;
    private JsonNode drawingData;
}