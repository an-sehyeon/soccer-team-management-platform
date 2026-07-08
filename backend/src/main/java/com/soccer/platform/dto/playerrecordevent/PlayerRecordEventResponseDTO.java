package com.soccer.platform.dto.playerrecordevent;

import java.time.LocalDateTime;
import java.util.List;

import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerRecordEntity;
import com.soccer.platform.entity.PlayerRecordEventEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 기록 이벤트 상세 응답 DTO
@Getter
@AllArgsConstructor
public class PlayerRecordEventResponseDTO {

    private Integer eventId;
    private Integer recordId;
    private Integer uploadId;
    private String matchVideoTitle;
    private Integer playerId;
    private String playerName;
    private Integer createdById;
    private String createdByName;
    private String eventType;
    private Integer eventStartTimeSec;
    private Integer eventEndTimeSec;
    private Integer value;
    private String eventMemo;
    private List<PlayerRecordEventClipResponseDTO> clips;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PlayerRecordEventResponseDTO from(
        PlayerRecordEventEntity event,
        List<PlayerRecordEventClipResponseDTO> clips
    ) {
        PlayerRecordEntity playerRecord = event.getPlayerRecord();
        GameVideoUploadEntity matchVideo = playerRecord.getGameVideoUpload();
        MemberEntity player = playerRecord.getPlayer();
        MemberEntity createdBy = event.getCreatedBy();

        return new PlayerRecordEventResponseDTO(
            event.getId(),
            playerRecord.getId(),
            matchVideo.getId(),
            matchVideo.getTitle(),
            player.getId(),
            player.getName(),
            createdBy.getId(),
            createdBy.getName(),
            event.getEventType().name(),
            event.getEventStartTimeSec(),
            event.getEventEndTimeSec(),
            event.getValue(),
            event.getMemo(),
            clips,
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }
}