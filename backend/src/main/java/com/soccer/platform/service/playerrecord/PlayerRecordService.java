package com.soccer.platform.service.playerrecord;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerrecord.CreatePlayerRecordRequestDTO;
import com.soccer.platform.dto.playerrecord.CreatePlayerRecordResponseDTO;
import com.soccer.platform.dto.playerrecord.PlayerRecordDetailResponseDTO;
import com.soccer.platform.dto.playerrecord.PlayerRecordListResponseDTO;
import com.soccer.platform.dto.playerrecord.PlayerRecordPageResponseDTO;
import com.soccer.platform.dto.playerrecord.UpdatePlayerRecordRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerRecordEntity;
import com.soccer.platform.repository.PlayerRecordRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.MatchVideoQueryService;
import com.soccer.platform.service.common.MemberQueryService;
import com.soccer.platform.service.common.PageRequestValidator;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;

// 선수 기록 관리 Service

@Service
@RequiredArgsConstructor
@Transactional
public class PlayerRecordService {

	private final PlayerRecordRepository playerRecordRepository;
    private final PermissionValidator permissionValidator;
    private final MemberQueryService memberQueryService;
    private final MatchVideoQueryService matchVideoQueryService;
    private final PageRequestValidator pageRequestValidator;
    private final PlayerRecordValidator playerRecordValidator;

 // 선수 기록 등록
    public CreatePlayerRecordResponseDTO createPlayerRecord(
            CustomUserPrincipal principal,
            CreatePlayerRecordRequestDTO request
    ) {
        permissionValidator.requireCoachOrAnalyst(
                principal,
                ErrorCode.PLAYER_RECORD_MANAGE_ACCESS_DENIED
        );

        playerRecordValidator.validateCreateRequest(request);

        MemberEntity recorder = memberQueryService.findLoginMember(
                principal,
                ErrorCode.MEMBER_NOT_FOUND
        );

        GameVideoUploadEntity gameVideoUpload = matchVideoQueryService.findActiveMatchVideoById(
                request.getUploadId(),
                ErrorCode.MATCH_VIDEO_NOT_FOUND
        );

        MemberEntity player = memberQueryService.findActivePlayerById(
                request.getPlayerId(),
                ErrorCode.MEMBER_NOT_FOUND,
                ErrorCode.INVALID_PLAYER_RECORD_PLAYER
        );

        playerRecordValidator.validateDuplicateForCreate(gameVideoUpload, player);

        PlayerRecordEntity playerRecord = PlayerRecordEntity.create(
                gameVideoUpload,
                player,
                recorder,
                request
        );

        PlayerRecordEntity savedPlayerRecord = playerRecordRepository.save(playerRecord);

        return new CreatePlayerRecordResponseDTO(
                savedPlayerRecord.getId(),
                "선수 기록이 등록되었습니다."
        );
    }

    // 관리용 선수 기록 목록 조회
    @Transactional(readOnly = true)
    public PlayerRecordPageResponseDTO findPlayerRecordsForManagement(
            CustomUserPrincipal principal,
            Integer page,
            Integer size,
            Integer uploadId,
            Integer playerId
    ) {
        permissionValidator.requireCoachOrAnalyst(
                principal,
                ErrorCode.PLAYER_RECORD_MANAGE_ACCESS_DENIED
        );

        Pageable pageable = pageRequestValidator.createPageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        GameVideoUploadEntity gameVideoUpload = null;
        MemberEntity player = null;

        if (uploadId != null) {
            gameVideoUpload = matchVideoQueryService.findActiveMatchVideoById(
                    uploadId,
                    ErrorCode.MATCH_VIDEO_NOT_FOUND
            );
        }

        if (playerId != null) {
            player = memberQueryService.findActivePlayerById(
                    playerId,
                    ErrorCode.MEMBER_NOT_FOUND,
                    ErrorCode.INVALID_PLAYER_RECORD_PLAYER
            );
        }

        Page<PlayerRecordEntity> playerRecordPage = findPlayerRecordPage(
                gameVideoUpload,
                player,
                pageable
        );

        return toPageResponseDTO(playerRecordPage);
    }

    // 관리용 선수 기록 상세 조회
    @Transactional(readOnly = true)
    public PlayerRecordDetailResponseDTO findPlayerRecordDetailForManagement(
            CustomUserPrincipal principal,
            Integer recordId
    ) {
        permissionValidator.requireCoachOrAnalyst(
                principal,
                ErrorCode.PLAYER_RECORD_MANAGE_ACCESS_DENIED
        );

        PlayerRecordEntity playerRecord = findActivePlayerRecord(recordId);

        return toDetailResponseDTO(playerRecord);
    }

    // 선수 기록 수정
    public PlayerRecordDetailResponseDTO updatePlayerRecord(
            CustomUserPrincipal principal,
            Integer recordId,
            UpdatePlayerRecordRequestDTO request
    ) {
        permissionValidator.requireCoachOrAnalyst(
                principal,
                ErrorCode.PLAYER_RECORD_MANAGE_ACCESS_DENIED
        );

        playerRecordValidator.validateUpdateRequest(request);

        PlayerRecordEntity playerRecord = findActivePlayerRecord(recordId);

        MemberEntity lastModifier = memberQueryService.findLoginMember(
                principal,
                ErrorCode.MEMBER_NOT_FOUND
        );

        GameVideoUploadEntity gameVideoUpload = matchVideoQueryService.findActiveMatchVideoById(
                request.getUploadId(),
                ErrorCode.MATCH_VIDEO_NOT_FOUND
        );

        MemberEntity player = memberQueryService.findActivePlayerById(
                request.getPlayerId(),
                ErrorCode.MEMBER_NOT_FOUND,
                ErrorCode.INVALID_PLAYER_RECORD_PLAYER
        );

        playerRecordValidator.validateDuplicateForUpdate(
                recordId,
                gameVideoUpload,
                player
        );

        playerRecord.update(
                gameVideoUpload,
                player,
                lastModifier,
                request
        );

        return toDetailResponseDTO(playerRecord);
    }

    // 선수 기록 삭제
    public void deletePlayerRecord(
            CustomUserPrincipal principal,
            Integer recordId
    ) {
        permissionValidator.requireCoachOrAnalyst(
                principal,
                ErrorCode.PLAYER_RECORD_MANAGE_ACCESS_DENIED
        );

        PlayerRecordEntity playerRecord = findActivePlayerRecord(recordId);

        playerRecord.softDelete();
    }

    // 선수 본인 기록 목록 조회
    @Transactional(readOnly = true)
    public PlayerRecordPageResponseDTO findMyPlayerRecords(
            CustomUserPrincipal principal,
            Integer page,
            Integer size
    ) {
        permissionValidator.requirePlayer(
                principal,
                ErrorCode.PLAYER_RECORD_ACCESS_DENIED
        );

        Pageable pageable = pageRequestValidator.createPageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        MemberEntity loginPlayer = memberQueryService.findLoginMember(
                principal,
                ErrorCode.MEMBER_NOT_FOUND
        );

        Page<PlayerRecordEntity> playerRecordPage = playerRecordRepository
                .findByPlayerAndIsDeletedFalse(
                        loginPlayer,
                        pageable
                );

        return toPageResponseDTO(playerRecordPage);
    }

    // 선수 본인 기록 상세 조회
    @Transactional(readOnly = true)
    public PlayerRecordDetailResponseDTO findMyPlayerRecordDetail(
            CustomUserPrincipal principal,
            Integer recordId
    ) {
        permissionValidator.requirePlayer(
                principal,
                ErrorCode.PLAYER_RECORD_ACCESS_DENIED
        );

        PlayerRecordEntity playerRecord = findActivePlayerRecord(recordId);

        playerRecordValidator.validatePlayerOwnRecord(
                principal.getMemberId(),
                playerRecord
        );

        return toDetailResponseDTO(playerRecord);
    }

    // 삭제되지 않은 선수 기록 조회
    private PlayerRecordEntity findActivePlayerRecord(Integer recordId) {
        if (recordId == null) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_NOT_FOUND);
        }

        return playerRecordRepository.findByIdAndIsDeletedFalse(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_RECORD_NOT_FOUND));
    }

    // 선수 기록 목록 조건 조회
    private Page<PlayerRecordEntity> findPlayerRecordPage(
            GameVideoUploadEntity gameVideoUpload,
            MemberEntity player,
            Pageable pageable
    ) {
        if (gameVideoUpload != null && player != null) {
            return playerRecordRepository.findByGameVideoUploadAndPlayerAndIsDeletedFalse(
                    gameVideoUpload,
                    player,
                    pageable
            );
        }

        if (gameVideoUpload != null) {
            return playerRecordRepository.findByGameVideoUploadAndIsDeletedFalse(
                    gameVideoUpload,
                    pageable
            );
        }

        if (player != null) {
            return playerRecordRepository.findByPlayerAndIsDeletedFalse(
                    player,
                    pageable
            );
        }

        return playerRecordRepository.findByIsDeletedFalse(pageable);
    }

    // 선수 기록 상세 응답 변환
    private PlayerRecordDetailResponseDTO toDetailResponseDTO(PlayerRecordEntity playerRecord) {
        MemberEntity player = playerRecord.getPlayer();
        MemberEntity recorder = playerRecord.getRecorder();
        MemberEntity lastModifier = playerRecord.getLastModifier();
        GameVideoUploadEntity gameVideoUpload = playerRecord.getGameVideoUpload();

        Integer lastModifierId = null;
        String lastModifierName = null;

        if (lastModifier != null) {
            lastModifierId = lastModifier.getId();
            lastModifierName = lastModifier.getName();
        }

        return new PlayerRecordDetailResponseDTO(
                playerRecord.getId(),
                gameVideoUpload.getId(),
                gameVideoUpload.getTitle(),
                player.getId(),
                player.getName(),
                recorder.getId(),
                recorder.getName(),
                lastModifierId,
                lastModifierName,
                playerRecord.getMinutesPlayed(),
                playerRecord.getGoals(),
                playerRecord.getAssists(),
                playerRecord.getShots(),
                playerRecord.getShotsOnTarget(),
                playerRecord.getPasses(),
                playerRecord.getSuccessfulPasses(),
                playerRecord.getDribbles(),
                playerRecord.getSuccessfulDribbles(),
                playerRecord.getTackles(),
                playerRecord.getInterceptions(),
                playerRecord.getClearances(),
                playerRecord.getSaves(),
                playerRecord.getYellowCards(),
                playerRecord.getRedCards(),
                playerRecord.getMemo(),
                playerRecord.getCreatedAt(),
                playerRecord.getUpdatedAt()
        );
    }

    // 선수 기록 목록 응답 변환
    private PlayerRecordListResponseDTO toListResponseDTO(PlayerRecordEntity playerRecord) {
        MemberEntity player = playerRecord.getPlayer();
        MemberEntity recorder = playerRecord.getRecorder();
        MemberEntity lastModifier = playerRecord.getLastModifier();
        GameVideoUploadEntity gameVideoUpload = playerRecord.getGameVideoUpload();

        Integer lastModifierId = null;
        String lastModifierName = null;

        if (lastModifier != null) {
            lastModifierId = lastModifier.getId();
            lastModifierName = lastModifier.getName();
        }

        return new PlayerRecordListResponseDTO(
                playerRecord.getId(),
                gameVideoUpload.getId(),
                gameVideoUpload.getTitle(),
                player.getId(),
                player.getName(),
                recorder.getId(),
                recorder.getName(),
                lastModifierId,
                lastModifierName,
                playerRecord.getCreatedAt(),
                playerRecord.getUpdatedAt()
        );
    }

    // 선수 기록 페이지 응답 변환
    private PlayerRecordPageResponseDTO toPageResponseDTO(
            Page<PlayerRecordEntity> playerRecordPage
    ) {
        List<PlayerRecordListResponseDTO> records = playerRecordPage.getContent()
                .stream()
                .map(this::toListResponseDTO)
                .toList();

        return new PlayerRecordPageResponseDTO(
                records,
                playerRecordPage.getNumber(),
                playerRecordPage.getSize(),
                playerRecordPage.getTotalElements(),
                playerRecordPage.getTotalPages()
        );
    }
}