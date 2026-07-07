package com.soccer.platform.service.playerclip;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipRequestDTO;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipWithDrawingsRequestDTO;
import com.soccer.platform.dto.playerclip.UpdatePlayerAnalysisClipRequestDTO;
import com.soccer.platform.dto.playerclip.UpdatePlayerAnalysisClipWithDrawingsRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.repository.PlayerVideoClipRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.MatchVideoQueryService;
import com.soccer.platform.service.common.MemberQueryService;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;

/*
 * 선수 개인 분석 클립 Validator
 * 권한, 요청값, 원본 영상, 대상 선수, 클립 시간 범위를 검증
 */
@Component
@RequiredArgsConstructor
public class PlayerAnalysisClipValidator {

	private static final int MAX_TITLE_LENGTH = 255;
	private static final int MAX_COMMENT_LENGTH = 255;

	private final PlayerVideoClipRepository playerVideoClipRepository;
	private final PermissionValidator permissionValidator;
	private final MemberQueryService memberQueryService;
	private final MatchVideoQueryService matchVideoQueryService;

	// 선수 개인 분석 클립 등록/수정 권한 검증
	public void validateCanCreateOrUpdate(CustomUserPrincipal principal) {
		permissionValidator.requireCoachOrAnalyst(
				principal,
				ErrorCode.ACCESS_DENIED
		);
	}

	// 선수 개인 분석 클립 삭제 권한 검증
	public void validateCanDelete(CustomUserPrincipal principal) {
		permissionValidator.requireCoach(
				principal,
				ErrorCode.ACCESS_DENIED
		);
	}

	// 관리용 선수 개인 분석 클립 조회 권한 검증
	public void validateCanViewManagement(CustomUserPrincipal principal) {
		permissionValidator.requireCoachOrAnalyst(
				principal,
				ErrorCode.ACCESS_DENIED
		);
	}

	// 선수 본인 개인 분석 클립 조회 권한 검증
	public void validateCanViewMyPlayerClip(CustomUserPrincipal principal) {
		permissionValidator.requirePlayer(
				principal,
				ErrorCode.ACCESS_DENIED
		);
	}

	// 등록 요청 검증
	public void validateCreateRequest(CreatePlayerAnalysisClipRequestDTO request) {
		if (request == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}

		validateRequiredIds(request.getMatchVideoId(), request.getPlayerId());

		validateRequiredClipValues(
				request.getTitle(),
				request.getClipType(),
				request.getStartTimeSec(),
				request.getEndTimeSec(),
				request.getComment()
		);
	}

	// 선수 개인 분석 클립과 드로잉 통합 등록 요청 검증
	public void validateCreateWithDrawingsRequest(CreatePlayerAnalysisClipWithDrawingsRequestDTO request) {
		if (request == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}

		validateRequiredIds(request.getMatchVideoId(), request.getPlayerId());

		validateRequiredClipValues(
				request.getTitle(),
				request.getClipType(),
				request.getStartTimeSec(),
				request.getEndTimeSec(),
				request.getComment()
		);
	}

	// 수정 요청 검증
	public void validateUpdateRequest(UpdatePlayerAnalysisClipRequestDTO request) {
		if (request == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}

		validateRequiredIds(request.getMatchVideoId(), request.getPlayerId());

		validateRequiredClipValues(
				request.getTitle(),
				request.getClipType(),
				request.getStartTimeSec(),
				request.getEndTimeSec(),
				request.getComment()
		);
	}
	
	// 선수 개인 분석 클립과 드로잉 통합 수정 요청 검증
	public void validateUpdateWithDrawingsRequest(UpdatePlayerAnalysisClipWithDrawingsRequestDTO request) {
	    if (request == null) {
	        throw new CustomException(ErrorCode.INVALID_REQUEST);
	    }

	    if (request.getPlayerId() == null) {
	        throw new CustomException(ErrorCode.INVALID_REQUEST);
	    }

	    validateRequiredClipValues(
	            request.getTitle(),
	            request.getClipType(),
	            request.getStartTimeSec(),
	            request.getEndTimeSec(),
	            request.getComment()
	    );
	}
	
	// 생성 중인 선수 개인 분석 클립 수정 방지
	public void validateCanUpdateGenerationTarget(PlayerVideoClipEntity playerVideoClip) {
		if (playerVideoClip == null) {
			throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND);
		}

		if (playerVideoClip.getStatus() == VideoUploadStatusEnum.PROCESSING) {
			throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_PROCESSING_CANNOT_UPDATE);
		}
	}

	// 원본 경기 영상 조회
	public GameVideoUploadEntity findValidMatchVideo(Integer matchVideoId) {
		return matchVideoQueryService.findActiveMatchVideoById(
				matchVideoId,
				ErrorCode.MATCH_VIDEO_NOT_FOUND
		);
	}

	// 영상 길이 정보가 준비된 원본 경기 영상 조회
	public GameVideoUploadEntity findValidMatchVideoWithDuration(Integer matchVideoId) {
		return matchVideoQueryService.findActiveMatchVideoWithDuration(
				matchVideoId,
				ErrorCode.MATCH_VIDEO_NOT_FOUND,
				ErrorCode.MATCH_VIDEO_DURATION_NOT_READY
		);
	}

	// 대상 선수 조회
	public MemberEntity findValidPlayer(Integer playerId) {
		return memberQueryService.findActivePlayerById(
				playerId,
				ErrorCode.MEMBER_NOT_FOUND,
				ErrorCode.INVALID_PLAYER_ROLE
		);
	}

	// 편집자 회원 조회
	public MemberEntity findEditor(CustomUserPrincipal principal) {
		return memberQueryService.findLoginMember(
				principal,
				ErrorCode.MEMBER_NOT_FOUND
		);
	}

	// 삭제되지 않은 선수 개인 분석 클립 조회
	public PlayerVideoClipEntity findValidPlayerAnalysisClip(Integer playerClipId) {
		if (playerClipId == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}

		return playerVideoClipRepository.findByIdAndIsDeletedFalse(playerClipId)
				.orElseThrow(() -> new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND));
	}

	// 선수 본인 클립 접근 검증
	public void validateMyPlayerClipAccess(
			PlayerVideoClipEntity playerVideoClip,
			CustomUserPrincipal principal
	) {
		if (playerVideoClip == null
				|| playerVideoClip.getPlayer() == null
				|| playerVideoClip.getPlayer().getId() == null
				|| principal == null
				|| principal.getMemberId() == null) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		if (!playerVideoClip.getPlayer().getId().equals(principal.getMemberId())) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
	}

	// 클립 시간 범위 검증
	public void validateClipTimeRange(
			Integer startTimeSec,
			Integer endTimeSec,
			GameVideoUploadEntity matchVideo
	) {
		if (startTimeSec == null || endTimeSec == null) {
			throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
		}

		if (matchVideo == null) {
			throw new CustomException(ErrorCode.MATCH_VIDEO_NOT_FOUND);
		}

		Integer durationSec = matchVideo.getDurationSec();

		if (durationSec == null || durationSec <= 0) {
			throw new CustomException(ErrorCode.MATCH_VIDEO_DURATION_NOT_READY);
		}

		if (startTimeSec < 0 || endTimeSec < 0) {
			throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
		}

		if (startTimeSec >= endTimeSec) {
			throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
		}

		if (endTimeSec > durationSec) {
			throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
		}
	}

	// 빈 문자열 정리
	public String trimNullableText(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		return value.trim();
	}

	// 필수 ID 검증
	private void validateRequiredIds(Integer matchVideoId, Integer playerId) {
		if (matchVideoId == null || playerId == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
	}

	// 클립 필수값 검증
	private void validateRequiredClipValues(
			String title,
			Object clipType,
			Integer startTimeSec,
			Integer endTimeSec,
			String comment
	) {
		if (!StringUtils.hasText(title)) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}

		validateMaxLength(title, MAX_TITLE_LENGTH);

		if (clipType == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}

		if (startTimeSec == null || endTimeSec == null) {
			throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
		}

		if (comment != null && !comment.trim().isEmpty()) {
			validateMaxLength(comment, MAX_COMMENT_LENGTH);
		}
	}

	// 문자열 길이 검증
	private void validateMaxLength(String value, int maxLength) {
		if (value.trim().length() > maxLength) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
	}

}