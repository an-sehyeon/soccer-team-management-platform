package com.soccer.platform.dto.playerrecord;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 기록 목록 응답 DTO

@Getter
@AllArgsConstructor
public class PlayerRecordListResponseDTO {

    private Integer recordId;
    private Integer uploadId;
    private String matchVideoTitle;
    private Integer playerId;
    private String playerName;
    private Integer recorderId;
    private String recorderName;
    private Integer lastModifierId;
    private String lastModifierName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}