package com.soccer.platform.dto.playerclipview;

import java.time.LocalDateTime;

import com.soccer.platform.entity.PlayerVideoClipViewEntity;

import lombok.Builder;
import lombok.Getter;

// 선수 클립 조회 기록 응답 DTO

@Getter
@Builder
public class PlayerClipViewResponseDTO {
	
	private Integer viewId;
    private Integer playerClipId;
    private String playerClipTitle;
    private Integer playerId;
    private String playerName;
    private LocalDateTime firstViewedAt;
    private LocalDateTime lastViewedAt;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // Entity를 조회 기록 응답 DTO로 변환
    public static PlayerClipViewResponseDTO from(PlayerVideoClipViewEntity view) {
        return PlayerClipViewResponseDTO.builder()
                .viewId(view.getId())
                .playerClipId(view.getPlayerVideoClip().getId())
                .playerClipTitle(view.getPlayerVideoClip().getTitle())
                .playerId(view.getMember().getId())
                .playerName(view.getMember().getName())
                .firstViewedAt(view.getFirstViewedAt())
                .lastViewedAt(view.getLastViewedAt())
                .viewCount(view.getViewCount())
                .createdAt(view.getCreatedAt())
                .updatedAt(view.getUpdatedAt())
                .build();
    }
}