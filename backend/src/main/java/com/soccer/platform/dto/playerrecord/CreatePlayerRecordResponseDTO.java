package com.soccer.platform.dto.playerrecord;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 기록 등록 응답 DTO

@Getter
@AllArgsConstructor
public class CreatePlayerRecordResponseDTO {

    private Integer recordId;
    private String message;
}