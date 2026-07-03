package com.soccer.platform.service.playerclipdrawing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerclipdrawing.CreatePlayerClipDrawingRequestDTO;
import com.soccer.platform.dto.playerclipdrawing.PlayerClipDrawingResponseDTO;
import com.soccer.platform.dto.playerclipdrawing.UpdatePlayerClipDrawingRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipDrawingEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.repository.PlayerVideoClipDrawingRepository;
import com.soccer.platform.repository.PlayerVideoClipRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.MemberQueryService;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;

/*
 * 선수 개인 분석 클립 드로잉 Validator
 * 권한, 선수 개인 클립, 드로잉, 시간 범위, drawingData 변환을 처리
 */
@Component
@RequiredArgsConstructor
public class PlayerClipDrawingValidator {

	private final PlayerVideoClipDrawingRepository playerVideoClipDrawingRepository;
	private final PlayerVideoClipRepository playerVideoClipRepository;
	private final MemberQueryService memberQueryService;
	private final PermissionValidator permissionValidator;
	private final ObjectMapper objectMapper;

	// 드로잉 등록/수정 권한 검증
	public void validateCanCreateOrUpdate(CustomUserPrincipal principal) {
		permissionValidator.requireCoachOrAnalyst(
				principal,
				ErrorCode.PLAYER_CLIP_DRAWING_MANAGE_FORBIDDEN
		);
	}

	// 드로잉 삭제 권한 검증
	public void validateCanDelete(CustomUserPrincipal principal) {
		permissionValidator.requireCoach(
				principal,
				ErrorCode.PLAYER_CLIP_DRAWING_DELETE_FORBIDDEN
		);
	}

	// 선수 개인 분석 클립 드로잉 조회 권한 검증
	public void validateCanReadPlayerClipDrawing(
			PlayerVideoClipEntity playerVideoClip,
			CustomUserPrincipal principal
	) {
		permissionValidator.requireAuthenticatedServiceUser(
				principal,
				ErrorCode.PLAYER_CLIP_DRAWING_ACCESS_DENIED
		);

		if (permissionValidator.isCoachOrAnalyst(principal)) {
			return;
		}

		if (permissionValidator.isPlayer(principal)) {
			validatePlayerOwnClip(playerVideoClip, principal);
			return;
		}

		throw new CustomException(ErrorCode.PLAYER_CLIP_DRAWING_ACCESS_DENIED);
	}

	// 로그인 회원 조회
	public MemberEntity findLoginMember(CustomUserPrincipal principal) {
		return memberQueryService.findLoginMember(
				principal,
				ErrorCode.MEMBER_NOT_FOUND
		);
	}

	// 삭제되지 않은 선수 개인 분석 클립 조회
	public PlayerVideoClipEntity findActivePlayerClip(Integer playerClipId) {
		if (playerClipId == null) {
			throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND);
		}

		return playerVideoClipRepository.findByIdAndIsDeletedFalse(playerClipId)
				.orElseThrow(() -> new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND));
	}

	// 삭제되지 않은 드로잉 조회
	public PlayerVideoClipDrawingEntity findActiveDrawing(Integer drawingId) {
		if (drawingId == null) {
			throw new CustomException(ErrorCode.PLAYER_CLIP_DRAWING_NOT_FOUND);
		}

		return playerVideoClipDrawingRepository.findByIdAndIsDeletedFalse(drawingId)
				.orElseThrow(() -> new CustomException(ErrorCode.PLAYER_CLIP_DRAWING_NOT_FOUND));
	}

	// 드로잉에 연결된 선수 개인 분석 클립 조회
	public PlayerVideoClipEntity findConnectedActivePlayerClip(
			PlayerVideoClipDrawingEntity drawing
	) {
		if (drawing == null
				|| drawing.getPlayerVideoClip() == null
				|| drawing.getPlayerVideoClip().getId() == null) {
			throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND);
		}

		return findActivePlayerClip(drawing.getPlayerVideoClip().getId());
	}

	// 드로잉 등록 요청 검증
	public void validateCreateRequest(CreatePlayerClipDrawingRequestDTO request) {
		if (request == null) {
			throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
		}

		validateDrawingType(request.getDrawingType());
		validateDrawingData(request.getDrawingData());
	}

	// 드로잉 수정 요청 검증
	public void validateUpdateRequest(UpdatePlayerClipDrawingRequestDTO request) {
		if (request == null) {
			throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
		}

		validateDrawingType(request.getDrawingType());
		validateDrawingData(request.getDrawingData());
	}

	// 드로잉 시간 검증
	public void validateDrawingTimeRange(
			Integer drawingStartTimeSec,
			Integer drawingEndTimeSec,
			PlayerVideoClipEntity playerVideoClip
	) {
		if (drawingStartTimeSec == null || drawingEndTimeSec == null) {
			throw new CustomException(ErrorCode.INVALID_DRAWING_TIME_RANGE);
		}

		if (drawingStartTimeSec < 0 || drawingEndTimeSec < 0) {
			throw new CustomException(ErrorCode.INVALID_DRAWING_TIME_RANGE);
		}

		if (drawingStartTimeSec >= drawingEndTimeSec) {
			throw new CustomException(ErrorCode.INVALID_DRAWING_TIME_RANGE);
		}

		if (playerVideoClip == null
				|| playerVideoClip.getStartTimeSec() == null
				|| playerVideoClip.getEndTimeSec() == null) {
			throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND);
		}

		Integer clipDurationSec = playerVideoClip.getEndTimeSec() - playerVideoClip.getStartTimeSec();

		if (clipDurationSec <= 0) {
			throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
		}

		if (drawingEndTimeSec > clipDurationSec) {
			throw new CustomException(ErrorCode.DRAWING_TIME_OUT_OF_CLIP_RANGE);
		}
	}

	// drawingData JSON을 DB 저장용 문자열로 변환
	public String convertDrawingDataToString(JsonNode drawingData) {
		validateDrawingData(drawingData);

		try {
			return objectMapper.writeValueAsString(drawingData);
		} catch (JsonProcessingException exception) {
			throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
		}
	}

	// DB에 저장된 drawingData 문자열을 응답용 JSON으로 변환
	public JsonNode convertDrawingDataToJson(String drawingData) {
		try {
			return objectMapper.readTree(drawingData);
		} catch (JsonProcessingException exception) {
			throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
		}
	}

	// Entity를 응답 DTO로 변환
	public PlayerClipDrawingResponseDTO toResponseDTO(
			PlayerVideoClipDrawingEntity drawing
	) {
		return new PlayerClipDrawingResponseDTO(
				drawing.getId(),
				drawing.getPlayerVideoClip().getId(),
				drawing.getDrawingType(),
				drawing.getStartTimeSec(),
				drawing.getEndTimeSec(),
				convertDrawingDataToJson(drawing.getDrawingData()),
				drawing.getMember().getId(),
				drawing.getMember().getName(),
				drawing.getCreatedAt(),
				drawing.getUpdatedAt()
		);
	}

	// 선수 본인 클립 여부 검증
	private void validatePlayerOwnClip(
			PlayerVideoClipEntity playerVideoClip,
			CustomUserPrincipal principal
	) {
		if (playerVideoClip == null
				|| playerVideoClip.getPlayer() == null
				|| playerVideoClip.getPlayer().getId() == null
				|| principal == null
				|| principal.getMemberId() == null) {
			throw new CustomException(ErrorCode.PLAYER_CLIP_DRAWING_ACCESS_DENIED);
		}

		if (!playerVideoClip.getPlayer().getId().equals(principal.getMemberId())) {
			throw new CustomException(ErrorCode.PLAYER_CLIP_DRAWING_ACCESS_DENIED);
		}
	}

	// 드로잉 타입 검증
	private void validateDrawingType(Object drawingType) {
		if (drawingType == null) {
			throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
		}
	}

	// 드로잉 JSON 데이터 검증
	private void validateDrawingData(JsonNode drawingData) {
		if (drawingData == null || drawingData.isNull()) {
			throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
		}

		if ((drawingData.isObject() || drawingData.isArray()) && drawingData.size() == 0) {
			throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
		}
	}

}