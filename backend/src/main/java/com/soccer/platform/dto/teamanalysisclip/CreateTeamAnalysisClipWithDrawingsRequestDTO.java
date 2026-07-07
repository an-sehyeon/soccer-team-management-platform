package com.soccer.platform.dto.teamanalysisclip;

import java.util.List;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;

import lombok.Getter;
import lombok.Setter;

// 팀 분석 클립과 드로잉 통합 생성 요청 DTO

@Getter
@Setter
public class CreateTeamAnalysisClipWithDrawingsRequestDTO {

    private Integer matchVideoId;
    private TeamVideoClipTypeEnum clipType;
    private String title;
    private String comment;
    private Integer startTimeSec;
    private Integer endTimeSec;
    private List<CreateTeamAnalysisClipDrawingItemRequestDTO> drawings;
}