package com.soccer.platform.dto.playerclip;

import java.util.List;

import com.soccer.platform.common.constants.PlayerClipTypeEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 선수 개인 분석 클립과 드로잉 통합 수정 요청 DTO

@Getter
@Setter
@NoArgsConstructor
public class UpdatePlayerAnalysisClipWithDrawingsRequestDTO {

    private Integer playerId;
    private PlayerClipTypeEnum clipType;
    private String title;
    private String comment;
    private Integer startTimeSec;
    private Integer endTimeSec;
    private List<CreatePlayerAnalysisClipDrawingItemRequestDTO> drawings;
}