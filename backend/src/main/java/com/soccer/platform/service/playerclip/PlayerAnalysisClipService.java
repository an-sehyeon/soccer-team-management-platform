package com.soccer.platform.service.playerclip;

import java.nio.file.Path;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.soccer.platform.common.constants.PlayerClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipDrawingItemRequestDTO;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipRequestDTO;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipResponseDTO;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipWithDrawingsRequestDTO;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipWithDrawingsResponseDTO;
import com.soccer.platform.dto.playerclip.PlayerAnalysisClipDetailResponseDTO;
import com.soccer.platform.dto.playerclip.PlayerAnalysisClipListResponseDTO;
import com.soccer.platform.dto.playerclip.PlayerAnalysisClipPageResponseDTO;
import com.soccer.platform.dto.playerclip.UpdatePlayerAnalysisClipRequestDTO;
import com.soccer.platform.dto.playerclip.UpdatePlayerAnalysisClipWithDrawingsRequestDTO;
import com.soccer.platform.dto.playerclip.UpdatePlayerAnalysisClipWithDrawingsResponseDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipDrawingEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.repository.PlayerVideoClipDrawingRepository;
import com.soccer.platform.repository.PlayerVideoClipRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.playerclipdrawing.PlayerClipDrawingValidator;
import com.soccer.platform.service.playerclipview.PlayerAnalysisClipViewService;
import com.soccer.platform.service.videobookmark.VideoBookmarkService;

import lombok.RequiredArgsConstructor;

/*
 * 선수 개인 분석 클립 Service
 * 선수 개인 피드백용 클립을 등록, 조회, 수정, 삭제
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerAnalysisClipService {

	private final PlayerVideoClipRepository playerVideoClipRepository;
	private final PlayerVideoClipDrawingRepository playerVideoClipDrawingRepository;
	private final PlayerAnalysisClipValidator playerAnalysisClipValidator;
	private final PlayerClipDrawingValidator playerClipDrawingValidator;
	private final PlayerAnalysisClipViewService playerAnalysisClipViewService;
	private final PlayerAnalysisClipLocalFileService playerAnalysisClipLocalFileService;
	private final PlayerAnalysisClipGenerationAsyncService playerAnalysisClipGenerationAsyncService;
	private final VideoBookmarkService videoBookmarkService;

	// 선수 개인 분석 클립 등록
	@Transactional
	public CreatePlayerAnalysisClipResponseDTO createPlayerAnalysisClip(
			CreatePlayerAnalysisClipRequestDTO request,
			CustomUserPrincipal principal
	) {
		playerAnalysisClipValidator.validateCanCreateOrUpdate(principal);
		playerAnalysisClipValidator.validateCreateRequest(request);

		GameVideoUploadEntity matchVideo = playerAnalysisClipValidator.findValidMatchVideoWithDuration(
				request.getMatchVideoId()
		);

		MemberEntity player = playerAnalysisClipValidator.findValidPlayer(request.getPlayerId());
		MemberEntity editor = playerAnalysisClipValidator.findEditor(principal);

		playerAnalysisClipValidator.validateClipTimeRange(
				request.getStartTimeSec(),
				request.getEndTimeSec(),
				matchVideo
		);

		Path originalVideoFilePath = playerAnalysisClipLocalFileService.resolveOriginalMatchVideoFilePath(matchVideo);

		PlayerVideoClipEntity playerVideoClip = createProcessingPlayerVideoClip(
				matchVideo,
				player,
				editor,
				request.getClipType(),
				request.getTitle(),
				request.getComment(),
				request.getStartTimeSec(),
				request.getEndTimeSec()
		);

		PlayerVideoClipEntity savedPlayerVideoClip = playerVideoClipRepository.save(playerVideoClip);

		PlayerAnalysisClipGenerationCommand generationCommand = createGenerationCommand(
				savedPlayerVideoClip,
				originalVideoFilePath,
				null
		);

		requestPlayerAnalysisClipFileGenerationAfterCommit(generationCommand);

		return new CreatePlayerAnalysisClipResponseDTO(
				savedPlayerVideoClip.getId(),
				"선수 개인 분석 클립 파일 생성이 요청되었습니다."
		);
	}

	// 선수 개인 분석 클립과 드로잉 통합 등록
	@Transactional
	public CreatePlayerAnalysisClipWithDrawingsResponseDTO createPlayerAnalysisClipWithDrawings(
			CreatePlayerAnalysisClipWithDrawingsRequestDTO request,
			CustomUserPrincipal principal
	) {
		playerAnalysisClipValidator.validateCanCreateOrUpdate(principal);
		playerAnalysisClipValidator.validateCreateWithDrawingsRequest(request);

		GameVideoUploadEntity matchVideo = playerAnalysisClipValidator.findValidMatchVideoWithDuration(
				request.getMatchVideoId()
		);

		MemberEntity player = playerAnalysisClipValidator.findValidPlayer(request.getPlayerId());
		MemberEntity editor = playerAnalysisClipValidator.findEditor(principal);

		playerAnalysisClipValidator.validateClipTimeRange(
				request.getStartTimeSec(),
				request.getEndTimeSec(),
				matchVideo
		);

		Path originalVideoFilePath = playerAnalysisClipLocalFileService.resolveOriginalMatchVideoFilePath(matchVideo);

		PlayerVideoClipEntity playerVideoClip = createProcessingPlayerVideoClip(
				matchVideo,
				player,
				editor,
				request.getClipType(),
				request.getTitle(),
				request.getComment(),
				request.getStartTimeSec(),
				request.getEndTimeSec()
		);

		PlayerVideoClipEntity savedPlayerVideoClip = playerVideoClipRepository.save(playerVideoClip);

		saveDrawingsIfExists(
				request.getDrawings(),
				savedPlayerVideoClip,
				editor
		);

		PlayerAnalysisClipGenerationCommand generationCommand = createGenerationCommand(
				savedPlayerVideoClip,
				originalVideoFilePath,
				null
		);

		requestPlayerAnalysisClipFileGenerationAfterCommit(generationCommand);

		return new CreatePlayerAnalysisClipWithDrawingsResponseDTO(
				savedPlayerVideoClip.getId(),
				savedPlayerVideoClip.getStatus(),
				"선수 개인 분석 클립 생성 작업이 등록되었습니다."
		);
	}

	// 관리용 선수 개인 분석 클립 목록 조회
	public PlayerAnalysisClipPageResponseDTO findPlayerAnalysisClipsForManagement(
			Integer matchVideoId,
			Integer playerId,
			PlayerClipTypeEnum clipType,
			Pageable pageable,
			CustomUserPrincipal principal
	) {
		playerAnalysisClipValidator.validateCanViewManagement(principal);

		GameVideoUploadEntity matchVideo = null;

		if (matchVideoId != null) {
			matchVideo = playerAnalysisClipValidator.findValidMatchVideo(matchVideoId);
		}

		MemberEntity player = null;

		if (playerId != null) {
			player = playerAnalysisClipValidator.findValidPlayer(playerId);
		}

		Page<PlayerVideoClipEntity> playerClipPage = findPlayerClipPageByManagementConditions(
				matchVideo,
				player,
				clipType,
				pageable
		);

		return toPlayerAnalysisClipPageResponseDTO(playerClipPage);
	}

	// 선수 본인 개인 분석 클립 목록 조회
	public PlayerAnalysisClipPageResponseDTO findMyPlayerAnalysisClips(
			Integer matchVideoId,
			PlayerClipTypeEnum clipType,
			Pageable pageable,
			CustomUserPrincipal principal
	) {
		playerAnalysisClipValidator.validateCanViewMyPlayerClip(principal);

		MemberEntity player = playerAnalysisClipValidator.findValidPlayer(principal.getMemberId());

		GameVideoUploadEntity matchVideo = null;

		if (matchVideoId != null) {
			matchVideo = playerAnalysisClipValidator.findValidMatchVideo(matchVideoId);
		}

		Page<PlayerVideoClipEntity> playerClipPage = findMyPlayerClipPageByConditions(
				player,
				matchVideo,
				clipType,
				pageable
		);

		return toPlayerAnalysisClipPageResponseDTO(playerClipPage);
	}

	// 관리용 선수 개인 분석 클립 상세 조회
	public PlayerAnalysisClipDetailResponseDTO findPlayerAnalysisClipDetailForManagement(
			Integer playerClipId,
			CustomUserPrincipal principal
	) {
		playerAnalysisClipValidator.validateCanViewManagement(principal);

		PlayerVideoClipEntity playerVideoClip = playerAnalysisClipValidator.findValidPlayerAnalysisClip(playerClipId);

		return toPlayerAnalysisClipDetailResponseDTO(playerVideoClip);
	}

	// 선수 본인 개인 분석 클립 상세 조회
	@Transactional
	public PlayerAnalysisClipDetailResponseDTO findMyPlayerAnalysisClipDetail(
			Integer playerClipId,
			CustomUserPrincipal principal
	) {
		playerAnalysisClipValidator.validateCanViewMyPlayerClip(principal);

		PlayerVideoClipEntity playerVideoClip = playerAnalysisClipValidator.findValidPlayerAnalysisClip(playerClipId);

		playerAnalysisClipValidator.validateMyPlayerClipAccess(playerVideoClip, principal);

		playerAnalysisClipViewService.recordViewIfPlayer(playerVideoClip, principal);

		return toPlayerAnalysisClipDetailResponseDTO(playerVideoClip);
	}

	// 선수 개인 분석 클립 수정
	@Transactional
	public PlayerAnalysisClipDetailResponseDTO updatePlayerAnalysisClip(
	    Integer playerClipId,
	    UpdatePlayerAnalysisClipRequestDTO request,
	    CustomUserPrincipal principal
	) {
	    playerAnalysisClipValidator.validateCanCreateOrUpdate(principal);
	    playerAnalysisClipValidator.validateUpdateRequest(request);

	    PlayerVideoClipEntity playerVideoClip =
	        playerAnalysisClipValidator
	            .findValidPlayerAnalysisClip(playerClipId);

	    playerAnalysisClipValidator
	        .validateCanUpdateGenerationTarget(playerVideoClip);

	    GameVideoUploadEntity matchVideo =
	        playerAnalysisClipValidator
	            .findValidMatchVideoWithDuration(
	                request.getMatchVideoId()
	            );

	    MemberEntity player =
	        playerAnalysisClipValidator
	            .findValidPlayer(request.getPlayerId());

	    playerAnalysisClipValidator.validateClipTimeRange(
	        request.getStartTimeSec(),
	        request.getEndTimeSec(),
	        matchVideo
	    );

	    boolean bookmarkInvalidationRequired =
	        isPlayerClipBookmarkInvalidationRequired(
	            playerVideoClip,
	            matchVideo,
	            request.getStartTimeSec(),
	            request.getEndTimeSec()
	        );

	    Path originalVideoFilePath =
	        playerAnalysisClipLocalFileService
	            .resolveOriginalMatchVideoFilePath(matchVideo);

	    String previousClipUrl = playerVideoClip.getUrl();

	    if (bookmarkInvalidationRequired) {
	        videoBookmarkService
	            .softDeleteBookmarksByPlayerClipId(playerClipId);
	    }

	    playerVideoClip.setGameVideoUpload(matchVideo);
	    playerVideoClip.setPlayer(player);
	    playerVideoClip.setClipType(request.getClipType());
	    playerVideoClip.setTitle(request.getTitle().trim());
	    playerVideoClip.setComment(
	        playerAnalysisClipValidator.trimNullableText(
	            request.getComment()
	        )
	    );
	    playerVideoClip.setStartTimeSec(
	        request.getStartTimeSec()
	    );
	    playerVideoClip.setEndTimeSec(
	        request.getEndTimeSec()
	    );
	    playerVideoClip.setUrl(null);
	    playerVideoClip.setStatus(
	        VideoUploadStatusEnum.PROCESSING
	    );

	    PlayerAnalysisClipGenerationCommand generationCommand =
	        createGenerationCommand(
	            playerVideoClip,
	            originalVideoFilePath,
	            previousClipUrl
	        );

	    requestPlayerAnalysisClipFileGenerationAfterCommit(
	        generationCommand
	    );

	    return toPlayerAnalysisClipDetailResponseDTO(
	        playerVideoClip
	    );
	}
	
	// 선수 개인 분석 클립과 드로잉 통합 수정
	@Transactional
	public UpdatePlayerAnalysisClipWithDrawingsResponseDTO
	    updatePlayerAnalysisClipWithDrawings(
	        Integer playerClipId,
	        UpdatePlayerAnalysisClipWithDrawingsRequestDTO request,
	        CustomUserPrincipal principal
	    ) {

	    playerAnalysisClipValidator
	        .validateCanCreateOrUpdate(principal);

	    playerAnalysisClipValidator
	        .validateUpdateWithDrawingsRequest(request);

	    PlayerVideoClipEntity playerVideoClip =
	        playerAnalysisClipValidator
	            .findValidPlayerAnalysisClip(playerClipId);

	    playerAnalysisClipValidator
	        .validateCanUpdateGenerationTarget(playerVideoClip);

	    GameVideoUploadEntity matchVideo =
	        playerAnalysisClipValidator
	            .findValidMatchVideoWithDuration(
	                playerVideoClip
	                    .getGameVideoUpload()
	                    .getId()
	            );

	    MemberEntity player =
	        playerAnalysisClipValidator
	            .findValidPlayer(request.getPlayerId());

	    playerAnalysisClipValidator.validateClipTimeRange(
	        request.getStartTimeSec(),
	        request.getEndTimeSec(),
	        matchVideo
	    );

	    boolean clipTimeChanged =
	        isPlayerClipTimeChanged(
	            playerVideoClip,
	            request.getStartTimeSec(),
	            request.getEndTimeSec()
	        );

	    boolean fileGenerationRequired =
	        isPlayerClipFileGenerationRequired(
	            playerVideoClip,
	            request
	        );

	    Path originalVideoFilePath = null;
	    String previousClipUrl = playerVideoClip.getUrl();

	    if (fileGenerationRequired) {
	        originalVideoFilePath =
	            playerAnalysisClipLocalFileService
	                .resolveOriginalMatchVideoFilePath(matchVideo);
	    }

	    if (clipTimeChanged) {
	        videoBookmarkService
	            .softDeleteBookmarksByPlayerClipId(playerClipId);
	    }

	    playerVideoClip.setPlayer(player);
	    playerVideoClip.setClipType(request.getClipType());
	    playerVideoClip.setTitle(request.getTitle().trim());
	    playerVideoClip.setComment(
	        playerAnalysisClipValidator.trimNullableText(
	            request.getComment()
	        )
	    );
	    playerVideoClip.setStartTimeSec(
	        request.getStartTimeSec()
	    );
	    playerVideoClip.setEndTimeSec(
	        request.getEndTimeSec()
	    );

	    softDeleteExistingDrawings(playerVideoClip);

	    MemberEntity editor =
	        playerAnalysisClipValidator.findEditor(principal);

	    saveDrawingsIfExists(
	        request.getDrawings(),
	        playerVideoClip,
	        editor
	    );

	    if (fileGenerationRequired) {
	        playerVideoClip.setUrl(null);
	        playerVideoClip.setStatus(
	            VideoUploadStatusEnum.PROCESSING
	        );

	        PlayerAnalysisClipGenerationCommand generationCommand =
	            createGenerationCommand(
	                playerVideoClip,
	                originalVideoFilePath,
	                previousClipUrl
	            );

	        requestPlayerAnalysisClipFileGenerationAfterCommit(
	            generationCommand
	        );
	    }

	    String responseMessage;

	    if (clipTimeChanged) {
	        responseMessage =
	            "선수 개인 분석 클립 수정 후 파일 재생성이 "
	                + "요청되었습니다. 기존 클립이 수정되어 "
	                + "해당 클립에서 생성한 북마크가 모두 "
	                + "삭제되었습니다.";
	    } else if (fileGenerationRequired) {
	        responseMessage =
	            "선수 개인 분석 클립 수정 후 파일 재생성이 "
	                + "요청되었습니다.";
	    } else {
	        responseMessage =
	            "선수 개인 분석 클립과 드로잉이 수정되었습니다.";
	    }

	    return new UpdatePlayerAnalysisClipWithDrawingsResponseDTO(
	        playerVideoClip.getId(),
	        playerVideoClip.getStatus(),
	        fileGenerationRequired,
	        responseMessage
	    );
	}

	// 선수 개인 분석 클립 삭제
	@Transactional
	public void deletePlayerAnalysisClip(
	    Integer playerClipId,
	    CustomUserPrincipal principal
	) {
	    playerAnalysisClipValidator.validateCanDelete(principal);

	    PlayerVideoClipEntity playerVideoClip =
	        playerAnalysisClipValidator
	            .findValidPlayerAnalysisClip(playerClipId);

	    playerVideoClip.setIsDeleted(true);

	    videoBookmarkService
	        .softDeleteBookmarksByPlayerClipId(playerClipId);
	}

	// 생성 중 상태의 선수 개인 분석 클립 Entity 생성
	private PlayerVideoClipEntity createProcessingPlayerVideoClip(
			GameVideoUploadEntity matchVideo,
			MemberEntity player,
			MemberEntity editor,
			PlayerClipTypeEnum clipType,
			String title,
			String comment,
			Integer startTimeSec,
			Integer endTimeSec
	) {
		PlayerVideoClipEntity playerVideoClip = new PlayerVideoClipEntity();
		playerVideoClip.setGameVideoUpload(matchVideo);
		playerVideoClip.setEditor(editor);
		playerVideoClip.setPlayer(player);
		playerVideoClip.setClipType(clipType);
		playerVideoClip.setTitle(title.trim());
		playerVideoClip.setComment(playerAnalysisClipValidator.trimNullableText(comment));
		playerVideoClip.setStartTimeSec(startTimeSec);
		playerVideoClip.setEndTimeSec(endTimeSec);
		playerVideoClip.setUrl(null);
		playerVideoClip.setStatus(VideoUploadStatusEnum.PROCESSING);
		playerVideoClip.setIsDeleted(false);

		return playerVideoClip;
	}

	// 선수 개인 분석 클립 파일 생성 명령 생성
	private PlayerAnalysisClipGenerationCommand createGenerationCommand(
			PlayerVideoClipEntity playerVideoClip,
			Path originalVideoFilePath,
			String previousClipUrl
	) {
		GeneratedPlayerAnalysisClipFile generatedClipFile = playerAnalysisClipLocalFileService.createGeneratedClipFile(
				playerVideoClip.getId()
		);

		return new PlayerAnalysisClipGenerationCommand(
				playerVideoClip.getId(),
				originalVideoFilePath,
				generatedClipFile,
				playerVideoClip.getStartTimeSec(),
				playerVideoClip.getEndTimeSec(),
				previousClipUrl
		);
	}

	// 드로잉 목록 저장
	private void saveDrawingsIfExists(
			List<CreatePlayerAnalysisClipDrawingItemRequestDTO> drawings,
			PlayerVideoClipEntity playerVideoClip,
			MemberEntity editor
	) {
		if (drawings == null || drawings.isEmpty()) {
			return;
		}

		for (CreatePlayerAnalysisClipDrawingItemRequestDTO drawingRequest : drawings) {
			saveDrawing(
					drawingRequest,
					playerVideoClip,
					editor
			);
		}
	}

	// 드로잉 저장
	private void saveDrawing(
			CreatePlayerAnalysisClipDrawingItemRequestDTO drawingRequest,
			PlayerVideoClipEntity playerVideoClip,
			MemberEntity editor
	) {
		validateDrawingItemRequest(drawingRequest);

		playerClipDrawingValidator.validateDrawingTimeRange(
				drawingRequest.getStartTimeSec(),
				drawingRequest.getEndTimeSec(),
				playerVideoClip
		);

		PlayerVideoClipDrawingEntity drawing = new PlayerVideoClipDrawingEntity();
		drawing.setPlayerVideoClip(playerVideoClip);
		drawing.setMember(editor);
		drawing.setDrawingType(drawingRequest.getDrawingType());
		drawing.setStartTimeSec(drawingRequest.getStartTimeSec());
		drawing.setEndTimeSec(drawingRequest.getEndTimeSec());
		drawing.setDrawingData(playerClipDrawingValidator.convertDrawingDataToString(drawingRequest.getDrawingData()));
		drawing.setIsDeleted(false);

		playerVideoClipDrawingRepository.save(drawing);
	}

	// 통합 등록 드로잉 항목 검증
	private void validateDrawingItemRequest(CreatePlayerAnalysisClipDrawingItemRequestDTO drawingRequest) {
		if (drawingRequest == null) {
			throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
		}

		if (drawingRequest.getDrawingType() == null) {
			throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
		}

		if (drawingRequest.getStartTimeSec() == null || drawingRequest.getEndTimeSec() == null) {
			throw new CustomException(ErrorCode.INVALID_DRAWING_TIME_RANGE);
		}
	}

	// 트랜잭션 커밋 후 선수 개인 분석 클립 파일 생성 요청
	private void requestPlayerAnalysisClipFileGenerationAfterCommit(
			PlayerAnalysisClipGenerationCommand generationCommand
	) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			playerAnalysisClipGenerationAsyncService.generatePlayerAnalysisClipFileAsync(generationCommand);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				playerAnalysisClipGenerationAsyncService.generatePlayerAnalysisClipFileAsync(generationCommand);
			}

		});
	}

	// 관리용 목록 조회 조건 분기
	private Page<PlayerVideoClipEntity> findPlayerClipPageByManagementConditions(
			GameVideoUploadEntity matchVideo,
			MemberEntity player,
			PlayerClipTypeEnum clipType,
			Pageable pageable
	) {
		if (matchVideo != null && player != null && clipType != null) {
			return playerVideoClipRepository.findByPlayerAndGameVideoUploadAndClipTypeAndIsDeletedFalse(
					player,
					matchVideo,
					clipType,
					pageable
			);
		}

		if (matchVideo != null && player != null) {
			return playerVideoClipRepository.findByPlayerAndGameVideoUploadAndIsDeletedFalse(
					player,
					matchVideo,
					pageable
			);
		}

		if (matchVideo != null && clipType != null) {
			return playerVideoClipRepository.findByGameVideoUploadAndClipTypeAndIsDeletedFalse(
					matchVideo,
					clipType,
					pageable
			);
		}

		if (player != null && clipType != null) {
			return playerVideoClipRepository.findByPlayerAndClipTypeAndIsDeletedFalse(
					player,
					clipType,
					pageable
			);
		}

		if (matchVideo != null) {
			return playerVideoClipRepository.findByGameVideoUploadAndIsDeletedFalse(
					matchVideo,
					pageable
			);
		}

		if (player != null) {
			return playerVideoClipRepository.findByPlayerAndIsDeletedFalse(
					player,
					pageable
			);
		}

		if (clipType != null) {
			return playerVideoClipRepository.findByClipTypeAndIsDeletedFalse(
					clipType,
					pageable
			);
		}

		return playerVideoClipRepository.findByIsDeletedFalse(pageable);
	}

	// 선수 본인 목록 조회 조건 분기
	private Page<PlayerVideoClipEntity> findMyPlayerClipPageByConditions(
			MemberEntity player,
			GameVideoUploadEntity matchVideo,
			PlayerClipTypeEnum clipType,
			Pageable pageable
	) {
		if (matchVideo != null && clipType != null) {
			return playerVideoClipRepository.findByPlayerAndGameVideoUploadAndClipTypeAndIsDeletedFalse(
					player,
					matchVideo,
					clipType,
					pageable
			);
		}

		if (matchVideo != null) {
			return playerVideoClipRepository.findByPlayerAndGameVideoUploadAndIsDeletedFalse(
					player,
					matchVideo,
					pageable
			);
		}

		if (clipType != null) {
			return playerVideoClipRepository.findByPlayerAndClipTypeAndIsDeletedFalse(
					player,
					clipType,
					pageable
			);
		}

		return playerVideoClipRepository.findByPlayerAndIsDeletedFalse(
				player,
				pageable
		);
	}

	// 선수 개인 분석 클립 페이지 응답 변환
	private PlayerAnalysisClipPageResponseDTO toPlayerAnalysisClipPageResponseDTO(
			Page<PlayerVideoClipEntity> playerClipPage
	) {
		return new PlayerAnalysisClipPageResponseDTO(
				playerClipPage.getContent()
						.stream()
						.map(this::toPlayerAnalysisClipListResponseDTO)
						.toList(),
				playerClipPage.getNumber(),
				playerClipPage.getSize(),
				playerClipPage.getTotalElements(),
				playerClipPage.getTotalPages()
		);
	}

	// 선수 개인 분석 클립 목록 응답 변환
	private PlayerAnalysisClipListResponseDTO toPlayerAnalysisClipListResponseDTO(
			PlayerVideoClipEntity playerVideoClip
	) {
		return new PlayerAnalysisClipListResponseDTO(
				playerVideoClip.getId(),
				playerVideoClip.getGameVideoUpload().getId(),
				playerVideoClip.getGameVideoUpload().getTitle(),
				playerVideoClip.getPlayer().getId(),
				playerVideoClip.getPlayer().getName(),
				playerVideoClip.getClipType(),
				playerVideoClip.getTitle(),
				playerVideoClip.getStatus(),
				playerVideoClip.getEditor().getId(),
				playerVideoClip.getEditor().getName(),
				playerVideoClip.getCreatedAt()
		);
	}

	// 선수 개인 분석 클립 상세 응답 변환
	private PlayerAnalysisClipDetailResponseDTO toPlayerAnalysisClipDetailResponseDTO(
			PlayerVideoClipEntity playerVideoClip
	) {
		return PlayerAnalysisClipDetailResponseDTO.from(playerVideoClip);
	}
	
	/*
	 * 선수 개인 분석 클립의 시작 또는 종료 시간이
	 * 변경됐는지 확인.
	 * 제목, 코멘트, 클립 유형, 대상 선수, 드로잉만
	 * 변경된 경우 연결 북마크는 유지.
	 */
	private boolean isPlayerClipTimeChanged(
	    PlayerVideoClipEntity playerVideoClip,
	    Integer requestedStartTimeSec,
	    Integer requestedEndTimeSec
	) {
	    return !playerVideoClip.getStartTimeSec()
	        .equals(requestedStartTimeSec)
	        || !playerVideoClip.getEndTimeSec()
	            .equals(requestedEndTimeSec);
	}

	// 선수 개인 분석 클립 파일 재생성 필요 여부 판단
	private boolean isPlayerClipFileGenerationRequired(
	        PlayerVideoClipEntity playerVideoClip,
	        UpdatePlayerAnalysisClipWithDrawingsRequestDTO request
	) {
	    if (playerVideoClip.getStatus() == VideoUploadStatusEnum.FAILED) {
	        return true;
	    }

	    if (playerVideoClip.getUrl() == null || playerVideoClip.getUrl().isBlank()) {
	        return true;
	    }

	    return !playerVideoClip.getStartTimeSec().equals(request.getStartTimeSec())
	            || !playerVideoClip.getEndTimeSec().equals(request.getEndTimeSec());
	}

	// 기존 선수 개인 분석 드로잉 전체 소프트 삭제
	private void softDeleteExistingDrawings(PlayerVideoClipEntity playerVideoClip) {
	    List<PlayerVideoClipDrawingEntity> existingDrawings =
	            playerVideoClipDrawingRepository
	                    .findByPlayerVideoClipAndIsDeletedFalseOrderByStartTimeSecAscIdAsc(
	                            playerVideoClip
	                    );

	    for (PlayerVideoClipDrawingEntity drawing : existingDrawings) {
	        drawing.setIsDeleted(true);
	    }
	}
	
	/*
	 * 기존 선수 개인 분석 클립 북마크를 삭제해야 하는지 판단.
	 * 원본 경기 영상이나 영상 시간 구간이 변경되면
	 * 기존 북마크의 영상 기준이 달라지므로 모두 삭제
	 */
	private boolean isPlayerClipBookmarkInvalidationRequired(
	    PlayerVideoClipEntity playerVideoClip,
	    GameVideoUploadEntity requestedMatchVideo,
	    Integer requestedStartTimeSec,
	    Integer requestedEndTimeSec
	) {
	    boolean matchVideoChanged =
	        !playerVideoClip
	            .getGameVideoUpload()
	            .getId()
	            .equals(requestedMatchVideo.getId());

	    return matchVideoChanged
	        || isPlayerClipTimeChanged(
	            playerVideoClip,
	            requestedStartTimeSec,
	            requestedEndTimeSec
	        );
	}
	
}