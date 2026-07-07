package com.soccer.platform.dto.teamanalysisclip;

import com.soccer.platform.common.constants.VideoUploadStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 팀 분석 클립 생성 응답 DTO

@Getter
@AllArgsConstructor
public class CreateTeamAnalysisClipResponseDTO {

    private Integer teamClipId;
    private VideoUploadStatusEnum status;
    private String message;
}