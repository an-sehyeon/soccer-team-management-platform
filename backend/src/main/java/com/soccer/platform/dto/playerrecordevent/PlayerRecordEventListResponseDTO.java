package com.soccer.platform.dto.playerrecordevent;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 기록 이벤트 목록 응답 DTO
@Getter
@AllArgsConstructor
public class PlayerRecordEventListResponseDTO {

    private List<PlayerRecordEventResponseDTO> events;
}