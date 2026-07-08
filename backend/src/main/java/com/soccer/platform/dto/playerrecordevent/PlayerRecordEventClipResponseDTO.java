package com.soccer.platform.dto.playerrecordevent;

import com.soccer.platform.entity.PlayerRecordEventClipEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 기록 이벤트 클립 연결 응답 DTO
@Getter
@AllArgsConstructor
public class PlayerRecordEventClipResponseDTO {

    private Integer eventClipId;
    private String clipSourceType;
    private Integer teamClipId;
    private String teamClipTitle;
    private Integer playerClipId;
    private String playerClipTitle;

    public static PlayerRecordEventClipResponseDTO from(PlayerRecordEventClipEntity eventClip) {
        TeamVideoClipEntity teamVideoClip = eventClip.getTeamVideoClip();
        PlayerVideoClipEntity playerVideoClip = eventClip.getPlayerVideoClip();

        Integer teamClipId = teamVideoClip == null ? null : teamVideoClip.getId();
        String teamClipTitle = teamVideoClip == null ? null : teamVideoClip.getTitle();

        Integer playerClipId = playerVideoClip == null ? null : playerVideoClip.getId();
        String playerClipTitle = playerVideoClip == null ? null : playerVideoClip.getTitle();

        return new PlayerRecordEventClipResponseDTO(
            eventClip.getId(),
            eventClip.getClipSourceType().name(),
            teamClipId,
            teamClipTitle,
            playerClipId,
            playerClipTitle
        );
    }
}