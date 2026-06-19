package com.soccer.platform.dto.playerrecord;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 선수 기록 등록 요청 DTO

@Getter
@Setter
@NoArgsConstructor
public class CreatePlayerRecordRequestDTO {

    private Integer uploadId;
    private Integer playerId;
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
}