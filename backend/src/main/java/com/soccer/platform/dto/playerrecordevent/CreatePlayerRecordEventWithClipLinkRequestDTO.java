package com.soccer.platform.dto.playerrecordevent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 선수 기록 이벤트와 클립 연결 동시 등록 요청 DTO
@Getter
@Setter
@NoArgsConstructor
public class CreatePlayerRecordEventWithClipLinkRequestDTO {

    private Integer uploadId;
    private Integer playerId;
    private String eventType;
    private Integer eventStartTimeSec;
    private Integer eventEndTimeSec;
    private Integer value;
    private String eventMemo;
    private String clipSourceType;
    private Integer teamClipId;
    private Integer playerClipId;
}