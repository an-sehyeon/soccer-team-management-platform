package com.soccer.platform.dto.teamanalysisclip;

import com.soccer.platform.common.constants.VideoUploadStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 팀 분석 클립과 드로잉 통합 수정 응답 DTO

@Getter
@AllArgsConstructor
public class UpdateTeamAnalysisClipWithDrawingsResponseDTO {

    private Integer teamClipId;
    private VideoUploadStatusEnum status;
    private boolean fileGenerationRequested;
    private String message;
}