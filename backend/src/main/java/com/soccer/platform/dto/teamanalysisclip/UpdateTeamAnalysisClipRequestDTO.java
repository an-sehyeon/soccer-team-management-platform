package com.soccer.platform.dto.teamanalysisclip;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * 팀 분석 클립 수정 요청 DTO
 * 클립 제목, 메모, 유형, 시간 구간을 수정한다.
 */
@Getter
@NoArgsConstructor
public class UpdateTeamAnalysisClipRequestDTO {

    private TeamVideoClipTypeEnum clipType;
    private String title;
    private String comment;
    private Integer startTimeSec;
    private Integer endTimeSec;
}