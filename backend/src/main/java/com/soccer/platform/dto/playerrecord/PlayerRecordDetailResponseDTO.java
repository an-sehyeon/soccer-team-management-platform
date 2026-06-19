package com.soccer.platform.dto.playerrecord;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 기록 상세 응답 DTO

@Getter
@AllArgsConstructor
public class PlayerRecordDetailResponseDTO {

    private Integer recordId;
    private Integer uploadId;
    private String matchVideoTitle;
    private Integer playerId;
    private String playerName;
    private Integer recorderId;
    private String recorderName;
    private Integer lastModifierId;
    private String lastModifierName;
    private Integer minutesPlayed;
    private Integer goals;
    private Integer assists;
    private Integer shots;
    private Integer shotsOnTarget;
    private Integer passes;
    private Integer successfulPasses;
    private Integer dribbles;
    private Integer successfulDribbles;
    private Integer tackles;
    private Integer interceptions;
    private Integer clearances;
    private Integer saves;
    private Integer yellowCards;
    private Integer redCards;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}