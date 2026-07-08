package com.soccer.platform.dto.playerrecordevent;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 기록 이벤트 등록 응답 DTO
@Getter
@AllArgsConstructor
public class CreatePlayerRecordEventResponseDTO {

    private Integer eventId;
    private Integer recordId;
    private Integer uploadId;
    private String matchVideoTitle;
    private Integer playerId;
    private String playerName;
    private String eventType;
    private Integer eventStartTimeSec;
    private Integer eventEndTimeSec;
    private Integer value;
    private String eventMemo;
    private LocalDateTime createdAt;
}