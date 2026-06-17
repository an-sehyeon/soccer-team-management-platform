package com.soccer.platform.dto.playerclip;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 개인 분석 클립 페이지 응답 DTO

@Getter
@AllArgsConstructor
public class PlayerAnalysisClipPageResponseDTO {

	private List<PlayerAnalysisClipListResponseDTO> playerClips;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
	
}
