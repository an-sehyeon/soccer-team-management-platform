package com.soccer.platform.dto.teamclipdrawing;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.soccer.platform.common.constants.DrawingTypeEnum;
import com.soccer.platform.entity.TeamVideoClipDrawingEntity;

import lombok.Builder;
import lombok.Getter;

// 팀 분석 클립 드로잉 응답 DTO

@Getter
@Builder
public class TeamAnalysisClipDrawingResponseDTO {

    private Integer drawingId;
    private Integer teamClipId;
    private DrawingTypeEnum drawingType;
    private Integer startTimeSec;
    private Integer endTimeSec;
    private JsonNode drawingData;
    private Integer writerId;
    private String writerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TeamAnalysisClipDrawingResponseDTO from(
            TeamVideoClipDrawingEntity drawing,
            JsonNode drawingData
    ) {
        return TeamAnalysisClipDrawingResponseDTO.builder()
                .drawingId(drawing.getId())
                .teamClipId(drawing.getTeamVideoClip().getId())
                .drawingType(drawing.getDrawingType())
                .startTimeSec(drawing.getStartTimeSec())
                .endTimeSec(drawing.getEndTimeSec())
                .drawingData(drawingData)
                .writerId(drawing.getMember().getId())
                .writerName(drawing.getMember().getName())
                .createdAt(drawing.getCreatedAt())
                .updatedAt(drawing.getUpdatedAt())
                .build();
    }
}
