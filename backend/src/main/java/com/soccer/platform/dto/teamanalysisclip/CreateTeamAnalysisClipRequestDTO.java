package com.soccer.platform.dto.teamanalysisclip;

import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * 팀 분석 클립 등록 요청 DTO
 * 원본 경기 영상 ID와 클립 시간 구간 정보를 받는다.
 */
@Getter
@NoArgsConstructor
public class CreateTeamAnalysisClipRequestDTO {

    private Integer matchVideoId;
    private TeamVideoClipTypeEnum clipType;
    private String title;
    private String comment;
    private Integer startTimeSec;
    private Integer endTimeSec;
}