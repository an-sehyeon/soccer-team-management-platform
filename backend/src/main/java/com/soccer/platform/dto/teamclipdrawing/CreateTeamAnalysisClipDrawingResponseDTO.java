package com.soccer.platform.dto.teamclipdrawing;


import lombok.AllArgsConstructor;
import lombok.Getter;

// 팀 분석 클립 드로잉 생성 응답 DTO

@Getter
@AllArgsConstructor
public class CreateTeamAnalysisClipDrawingResponseDTO {

    private Integer drawingId;
    private String message;
}
