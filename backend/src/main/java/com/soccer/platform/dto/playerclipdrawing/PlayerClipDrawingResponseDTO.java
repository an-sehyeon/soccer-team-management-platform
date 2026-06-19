package com.soccer.platform.dto.playerclipdrawing;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.soccer.platform.common.constants.DrawingTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 개인 분석 클립 드로잉 조회 응답 DTO

@Getter
@AllArgsConstructor
public class PlayerClipDrawingResponseDTO {

    private Integer drawingId;
    private Integer playerClipId;
    private DrawingTypeEnum drawingType;
    private Integer startTimeSec;
    private Integer endTimeSec;
    private JsonNode drawingData;
    private Integer writerId;
    private String writerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}