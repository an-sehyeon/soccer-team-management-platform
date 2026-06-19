package com.soccer.platform.dto.playerrecord;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 기록 페이지 응답 DTO

@Getter
@AllArgsConstructor
public class PlayerRecordPageResponseDTO {

    private List<PlayerRecordListResponseDTO> records;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}