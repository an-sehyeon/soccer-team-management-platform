package com.soccer.platform.dto.playerclip;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.PlayerClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.entity.PlayerVideoClipEntity;

import lombok.Getter;

// 선수 개인 분석 클립 상세 응답 DTO
// 상세 재생은 playerClipUrl 기준으로 처리하고, startTimeSec/endTimeSec는 수정 폼 초기값과 파일 재생성 요청용으로만 사용
@Getter
public class PlayerAnalysisClipDetailResponseDTO {

	private final Integer playerClipId;
	private final Integer matchVideoId;
	private final String matchVideoTitle;
	private final String playerClipUrl;
	private final Integer playerId;
	private final String playerName;
	private final PlayerClipTypeEnum clipType;
	private final String title;
	private final String comment;
	private final Integer startTimeSec;
	private final Integer endTimeSec;
	private final VideoUploadStatusEnum status;
	private final Integer editorId;
	private final String editorName;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	private PlayerAnalysisClipDetailResponseDTO(PlayerVideoClipEntity playerVideoClip) {
		this.playerClipId = playerVideoClip.getId();
		this.matchVideoId = playerVideoClip.getGameVideoUpload().getId();
		this.matchVideoTitle = playerVideoClip.getGameVideoUpload().getTitle();
		this.playerClipUrl = playerVideoClip.getUrl();
		this.playerId = playerVideoClip.getPlayer().getId();
		this.playerName = playerVideoClip.getPlayer().getName();
		this.clipType = playerVideoClip.getClipType();
		this.title = playerVideoClip.getTitle();
		this.comment = playerVideoClip.getComment();
		this.startTimeSec = playerVideoClip.getStartTimeSec();
		this.endTimeSec = playerVideoClip.getEndTimeSec();
		this.status = playerVideoClip.getStatus();
		this.editorId = playerVideoClip.getEditor().getId();
		this.editorName = playerVideoClip.getEditor().getName();
		this.createdAt = playerVideoClip.getCreatedAt();
		this.updatedAt = playerVideoClip.getUpdatedAt();
	}

	public static PlayerAnalysisClipDetailResponseDTO from(PlayerVideoClipEntity playerVideoClip) {
		return new PlayerAnalysisClipDetailResponseDTO(playerVideoClip);
	}
}