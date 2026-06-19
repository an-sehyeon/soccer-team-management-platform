package com.soccer.platform.dto.playerclipdrawing;

import com.fasterxml.jackson.databind.JsonNode;
import com.soccer.platform.common.constants.DrawingTypeEnum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// 선수 개인 분석 클립 드로잉 등록 요청 DTO
@Getter
@Setter
public class CreatePlayerClipDrawingRequestDTO {

    @NotNull(message = "드로잉 타입은 필수입니다.")
    private DrawingTypeEnum drawingType;

    @NotNull(message = "드로잉 시작 시간은 필수입니다.")
    @Min(value = 0, message = "드로잉 시작 시간은 0 이상이어야 합니다.")
    private Integer startTimeSec;

    @NotNull(message = "드로잉 종료 시간은 필수입니다.")
    @Min(value = 0, message = "드로잉 종료 시간은 0 이상이어야 합니다.")
    private Integer endTimeSec;

    @NotNull(message = "드로잉 데이터는 필수입니다.")
    private JsonNode drawingData;
}