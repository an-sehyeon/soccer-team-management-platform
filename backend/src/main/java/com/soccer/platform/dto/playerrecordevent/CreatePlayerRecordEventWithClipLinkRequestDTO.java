package com.soccer.platform.dto.playerrecordevent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 선수 기록 이벤트와 분석 클립 연결 등록 요청 DTO
// 이벤트 시간과 반영값은 요청받지 않고 선택한 클립을 기준으로 백엔드가 결정한다.
@Getter
@Setter
@NoArgsConstructor
public class CreatePlayerRecordEventWithClipLinkRequestDTO {

    private Integer uploadId;
    private Integer playerId;
    private String eventType;
    private String eventMemo;
    private String clipSourceType;
    private Integer teamClipId;
    private Integer playerClipId;
}