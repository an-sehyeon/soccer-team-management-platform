package com.soccer.platform.dto.teamanalysisclip;

import java.util.List;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 팀 분석 클립과 드로잉 통합 수정 요청 DTO

@Getter
@Setter
@NoArgsConstructor
public class UpdateTeamAnalysisClipWithDrawingsRequestDTO {

    private TeamVideoClipTypeEnum clipType;
    private String title;
    private String comment;
    private Integer startTimeSec;
    private Integer endTimeSec;
    private List<CreateTeamAnalysisClipDrawingItemRequestDTO> drawings;
}