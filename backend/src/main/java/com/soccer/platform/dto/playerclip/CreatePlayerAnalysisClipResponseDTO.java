package com.soccer.platform.dto.playerclip;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 개인 분석 클립 등록 응답 DTO

@Getter
@AllArgsConstructor
public class CreatePlayerAnalysisClipResponseDTO {

	private Integer playerClipId;
    private String message;
	
}
