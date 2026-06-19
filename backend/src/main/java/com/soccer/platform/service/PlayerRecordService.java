package com.soccer.platform.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.MemberRoleEnum;
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
import com.soccer.platform.repository.GameVideoUploadRepository;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.repository.PlayerRecordRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

// 선수 기록 관리 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerRecordService {

    private static final int MAX_TINYINT_UNSIGNED_VALUE = 255;
    private static final int MAX_PAGE_SIZE = 100;

    private final PlayerRecordRepository playerRecordRepository;
    private final GameVideoUploadRepository gameVideoUploadRepository;
    private final MemberRepository memberRepository;

    // 선수 기록 등록
    @Transactional
    public CreatePlayerRecordResponseDTO createPlayerRecord(
            CustomUserPrincipal principal,
            CreatePlayerRecordRequestDTO request
    ) {
        checkManagePermission(principal);
        normalizeCreateRequest(request);
        validateCreateRequest(request);

        MemberEntity recorder = findMemberById(principal.getMemberId());
        GameVideoUploadEntity gameVideoUpload = findGameVideoUploadById(request.getUploadId());
        MemberEntity player = findPlayerById(request.getPlayerId());

        if (playerRecordRepository.existsByGameVideoUploadAndPlayerAndIsDeletedFalse(gameVideoUpload, player)) {
            throw new CustomException(ErrorCode.DUPLICATE_PLAYER_RECORD);
        }

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
    public PlayerRecordPageResponseDTO findManagementPlayerRecords(
            CustomUserPrincipal principal,
            Integer uploadId,
            Integer playerId,
            int page,
            int size
    ) {
        checkManagePermission(principal);
        validatePageRequest(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        GameVideoUploadEntity gameVideoUpload = null;
        MemberEntity player = null;

        if (uploadId != null) {
            gameVideoUpload = findGameVideoUploadById(uploadId);
        }

        if (playerId != null) {
            player = findPlayerById(playerId);
        }

        Page<PlayerRecordEntity> playerRecords = findPlayerRecordsByFilter(
                gameVideoUpload,
                player,
                pageable
        );

        return toPageResponse(playerRecords);
    }

    // 관리용 선수 기록 상세 조회
    public PlayerRecordDetailResponseDTO findManagementPlayerRecordDetail(
            CustomUserPrincipal principal,
            Integer recordId
    ) {
        checkManagePermission(principal);

        PlayerRecordEntity playerRecord = findPlayerRecordById(recordId);

        return toDetailResponse(playerRecord);
    }

    // 선수 본인 기록 목록 조회
    public PlayerRecordPageResponseDTO findMyPlayerRecords(
            CustomUserPrincipal principal,
            int page,
            int size
    ) {
        checkPlayerPermission(principal);
        validatePageRequest(page, size);

        MemberEntity player = findPlayerById(principal.getMemberId());

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<PlayerRecordEntity> playerRecords = playerRecordRepository.findByPlayerAndIsDeletedFalse(
                player,
                pageable
        );

        return toPageResponse(playerRecords);
    }

    // 선수 본인 기록 상세 조회
    public PlayerRecordDetailResponseDTO findMyPlayerRecordDetail(
            CustomUserPrincipal principal,
            Integer recordId
    ) {
        checkPlayerPermission(principal);

        PlayerRecordEntity playerRecord = findPlayerRecordById(recordId);

        if (!playerRecord.getPlayer().getId().equals(principal.getMemberId())) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_ACCESS_DENIED);
        }

        return toDetailResponse(playerRecord);
    }

    // 선수 기록 수정
    @Transactional
    public PlayerRecordDetailResponseDTO updatePlayerRecord(
            CustomUserPrincipal principal,
            Integer recordId,
            UpdatePlayerRecordRequestDTO request
    ) {
        checkManagePermission(principal);
        normalizeUpdateRequest(request);
        validateUpdateRequest(request);

        PlayerRecordEntity playerRecord = findPlayerRecordById(recordId);
        GameVideoUploadEntity gameVideoUpload = findGameVideoUploadById(request.getUploadId());
        MemberEntity player = findPlayerById(request.getPlayerId());
        MemberEntity lastModifier = findMemberById(principal.getMemberId());

        boolean duplicated = playerRecordRepository.existsByGameVideoUploadAndPlayerAndIsDeletedFalseAndIdNot(
                gameVideoUpload,
                player,
                recordId
        );

        if (duplicated) {
            throw new CustomException(ErrorCode.DUPLICATE_PLAYER_RECORD);
        }

        playerRecord.update(
                gameVideoUpload,
                player,
                lastModifier,
                request
        );

        return toDetailResponse(playerRecord);
    }

    // 선수 기록 삭제
    @Transactional
    public void deletePlayerRecord(
            CustomUserPrincipal principal,
            Integer recordId
    ) {
        checkManagePermission(principal);

        PlayerRecordEntity playerRecord = findPlayerRecordById(recordId);
        playerRecord.softDelete();
    }

    // 관리 권한 검증
    private void checkManagePermission(CustomUserPrincipal principal) {
        MemberRoleEnum memberRole = principal.getMemberRole();

        if (memberRole != MemberRoleEnum.COACH && memberRole != MemberRoleEnum.ANALYST) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_MANAGE_ACCESS_DENIED);
        }
    }

    // 선수 본인 조회 권한 검증
    private void checkPlayerPermission(CustomUserPrincipal principal) {
        if (principal.getMemberRole() != MemberRoleEnum.PLAYER) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_ACCESS_DENIED);
        }
    }

    // 회원 조회
    private MemberEntity findMemberById(Integer memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 기록 대상 선수 조회
    private MemberEntity findPlayerById(Integer playerId) {
        MemberEntity player = findMemberById(playerId);

        if (player.getMemberRole() != MemberRoleEnum.PLAYER) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_PLAYER);
        }

        return player;
    }

    // 경기 영상 조회
    private GameVideoUploadEntity findGameVideoUploadById(Integer uploadId) {
        return gameVideoUploadRepository.findByIdAndIsDeletedFalse(uploadId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_VIDEO_NOT_FOUND));
    }

    // 선수 기록 조회
    private PlayerRecordEntity findPlayerRecordById(Integer recordId) {
        return playerRecordRepository.findByIdAndIsDeletedFalse(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_RECORD_NOT_FOUND));
    }

    // 관리용 목록 필터 분기
    private Page<PlayerRecordEntity> findPlayerRecordsByFilter(
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

    /*
     * 등록 요청 기본값 보정
     * DB는 DEFAULT 0이지만 JPA가 null을 직접 넣을 수 있으므로
     * 서버에서 null 값을 0으로 보정
     */
    private void normalizeCreateRequest(CreatePlayerRecordRequestDTO request) {
        request.setGoals(defaultZero(request.getGoals()));
        request.setAssists(defaultZero(request.getAssists()));
        request.setShots(defaultZero(request.getShots()));
        request.setShotsOnTarget(defaultZero(request.getShotsOnTarget()));
        request.setPasses(defaultZero(request.getPasses()));
        request.setSuccessfulPasses(defaultZero(request.getSuccessfulPasses()));
        request.setDribbles(defaultZero(request.getDribbles()));
        request.setSuccessfulDribbles(defaultZero(request.getSuccessfulDribbles()));
        request.setTackles(defaultZero(request.getTackles()));
        request.setInterceptions(defaultZero(request.getInterceptions()));
        request.setClearances(defaultZero(request.getClearances()));
        request.setSaves(defaultZero(request.getSaves()));
        request.setYellowCards(defaultZero(request.getYellowCards()));
        request.setRedCards(defaultZero(request.getRedCards()));
    }

    /*
     * 수정 요청 기본값 보정
     * DB는 DEFAULT 0이지만 JPA가 null을 직접 넣을 수 있으므로
     * 서버에서 null 값을 0으로 보정
     */
    private void normalizeUpdateRequest(UpdatePlayerRecordRequestDTO request) {
        request.setGoals(defaultZero(request.getGoals()));
        request.setAssists(defaultZero(request.getAssists()));
        request.setShots(defaultZero(request.getShots()));
        request.setShotsOnTarget(defaultZero(request.getShotsOnTarget()));
        request.setPasses(defaultZero(request.getPasses()));
        request.setSuccessfulPasses(defaultZero(request.getSuccessfulPasses()));
        request.setDribbles(defaultZero(request.getDribbles()));
        request.setSuccessfulDribbles(defaultZero(request.getSuccessfulDribbles()));
        request.setTackles(defaultZero(request.getTackles()));
        request.setInterceptions(defaultZero(request.getInterceptions()));
        request.setClearances(defaultZero(request.getClearances()));
        request.setSaves(defaultZero(request.getSaves()));
        request.setYellowCards(defaultZero(request.getYellowCards()));
        request.setRedCards(defaultZero(request.getRedCards()));
    }

    // null이면 0으로 변환
    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    // 등록 요청 검증
    private void validateCreateRequest(CreatePlayerRecordRequestDTO request) {
        if (request.getUploadId() == null || request.getPlayerId() == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }

        validateRecordValues(
                request.getMinutesPlayed(),
                request.getGoals(),
                request.getAssists(),
                request.getShots(),
                request.getShotsOnTarget(),
                request.getPasses(),
                request.getSuccessfulPasses(),
                request.getDribbles(),
                request.getSuccessfulDribbles(),
                request.getTackles(),
                request.getInterceptions(),
                request.getClearances(),
                request.getSaves(),
                request.getYellowCards(),
                request.getRedCards()
        );
    }

    // 수정 요청 검증
    private void validateUpdateRequest(UpdatePlayerRecordRequestDTO request) {
        if (request.getUploadId() == null || request.getPlayerId() == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }

        validateRecordValues(
                request.getMinutesPlayed(),
                request.getGoals(),
                request.getAssists(),
                request.getShots(),
                request.getShotsOnTarget(),
                request.getPasses(),
                request.getSuccessfulPasses(),
                request.getDribbles(),
                request.getSuccessfulDribbles(),
                request.getTackles(),
                request.getInterceptions(),
                request.getClearances(),
                request.getSaves(),
                request.getYellowCards(),
                request.getRedCards()
        );
    }

    // 선수 기록 수치 검증
    private void validateRecordValues(
            Integer minutesPlayed,
            Integer goals,
            Integer assists,
            Integer shots,
            Integer shotsOnTarget,
            Integer passes,
            Integer successfulPasses,
            Integer dribbles,
            Integer successfulDribbles,
            Integer tackles,
            Integer interceptions,
            Integer clearances,
            Integer saves,
            Integer yellowCards,
            Integer redCards
    ) {
        // 출전 시간은 아직 입력하지 않을 수 있으므로 null 허용
        validateNullableTinyIntValue(minutesPlayed);

        // TINYINT UNSIGNED 컬럼에 저장되는 값은 0~255 범위만 허용
        validateTinyIntValue(goals);
        validateTinyIntValue(assists);
        validateTinyIntValue(shots);
        validateTinyIntValue(shotsOnTarget);
        validateTinyIntValue(passes);
        validateTinyIntValue(successfulPasses);
        validateTinyIntValue(dribbles);
        validateTinyIntValue(successfulDribbles);
        validateTinyIntValue(tackles);
        validateTinyIntValue(interceptions);
        validateTinyIntValue(clearances);
        validateTinyIntValue(saves);
        validateTinyIntValue(yellowCards);
        validateTinyIntValue(redCards);

        // 유효 슈팅 수는 전체 슈팅 수보다 클 수 없음
        if (shotsOnTarget > shots) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }

        // 패스 성공 수는 패스 시도 수보다 클 수 없음
        if (successfulPasses > passes) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }

        // 드리블 성공 수는 드리블 시도 수보다 클 수 없음
        if (successfulDribbles > dribbles) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }
    }

    // null을 허용하지 않는 TINYINT UNSIGNED 값 검증
    private void validateTinyIntValue(Integer value) {
        // null이면 NOT NULL 컬럼에 저장할 수 없으므로 실패 처리
        if (value == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }

        // TINYINT UNSIGNED 저장 가능 범위는 0~255
        if (value < 0 || value > MAX_TINYINT_UNSIGNED_VALUE) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }
    }

    // null을 허용하는 TINYINT UNSIGNED 값 검증
    private void validateNullableTinyIntValue(Integer value) {
        // minutesPlayed는 null 허용 컬럼이므로 값이 없으면 검증하지 않음
        if (value == null) {
            return;
        }

        // 값이 들어온 경우에는 TINYINT UNSIGNED 범위인 0~255만 허용
        if (value < 0 || value > MAX_TINYINT_UNSIGNED_VALUE) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }
    }

    // 페이지 요청 검증
    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > MAX_PAGE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }
    }

    // 페이지 응답 변환
    private PlayerRecordPageResponseDTO toPageResponse(Page<PlayerRecordEntity> playerRecords) {
        return new PlayerRecordPageResponseDTO(
                playerRecords.getContent()
                        .stream()
                        .map(this::toListResponse)
                        .toList(),
                playerRecords.getNumber(),
                playerRecords.getSize(),
                playerRecords.getTotalElements(),
                playerRecords.getTotalPages()
        );
    }

    // 목록 응답 변환
    private PlayerRecordListResponseDTO toListResponse(PlayerRecordEntity playerRecord) {
        MemberEntity lastModifier = playerRecord.getLastModifier();

        return new PlayerRecordListResponseDTO(
                playerRecord.getId(),
                playerRecord.getGameVideoUpload().getId(),
                playerRecord.getGameVideoUpload().getTitle(),
                playerRecord.getPlayer().getId(),
                playerRecord.getPlayer().getName(),
                playerRecord.getRecorder().getId(),
                playerRecord.getRecorder().getName(),
                lastModifier == null ? null : lastModifier.getId(),
                lastModifier == null ? null : lastModifier.getName(),
                playerRecord.getCreatedAt(),
                playerRecord.getUpdatedAt()
        );
    }

    // 상세 응답 변환
    private PlayerRecordDetailResponseDTO toDetailResponse(PlayerRecordEntity playerRecord) {
        MemberEntity lastModifier = playerRecord.getLastModifier();

        return new PlayerRecordDetailResponseDTO(
                playerRecord.getId(),
                playerRecord.getGameVideoUpload().getId(),
                playerRecord.getGameVideoUpload().getTitle(),
                playerRecord.getPlayer().getId(),
                playerRecord.getPlayer().getName(),
                playerRecord.getRecorder().getId(),
                playerRecord.getRecorder().getName(),
                lastModifier == null ? null : lastModifier.getId(),
                lastModifier == null ? null : lastModifier.getName(),
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
}