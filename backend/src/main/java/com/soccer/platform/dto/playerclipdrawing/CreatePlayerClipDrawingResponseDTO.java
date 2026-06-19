package com.soccer.platform.dto.playerclipdrawing;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 개인 분석 클립 드로잉 등록 응답 DTO

@Getter
@AllArgsConstructor
public class CreatePlayerClipDrawingResponseDTO {

    private Integer drawingId;
    private String message;
}