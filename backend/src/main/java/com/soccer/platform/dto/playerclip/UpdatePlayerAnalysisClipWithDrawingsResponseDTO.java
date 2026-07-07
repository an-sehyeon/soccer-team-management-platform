package com.soccer.platform.dto.playerclip;

import com.soccer.platform.common.constants.VideoUploadStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 개인 분석 클립과 드로잉 통합 수정 응답 DTO

@Getter
@AllArgsConstructor
public class UpdatePlayerAnalysisClipWithDrawingsResponseDTO {

    private Integer playerClipId;
    private VideoUploadStatusEnum status;
    private boolean fileGenerationRequested;
    private String message;
}