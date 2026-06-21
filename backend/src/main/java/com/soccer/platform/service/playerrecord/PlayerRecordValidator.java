package com.soccer.platform.service.playerrecord;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerrecord.CreatePlayerRecordRequestDTO;
import com.soccer.platform.dto.playerrecord.UpdatePlayerRecordRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerRecordEntity;
import com.soccer.platform.repository.PlayerRecordRepository;

import lombok.RequiredArgsConstructor;

// 선수 기록 전용 Validator
// 선수 기록 기능에만 필요한 요청값 검증, 중복 기록 검증, 선수 본인 기록 접근 검증 담당.

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerRecordValidator {

    private static final int MIN_TINYINT_UNSIGNED_VALUE = 0;
    private static final int MAX_TINYINT_UNSIGNED_VALUE = 255;

    private final PlayerRecordRepository playerRecordRepository;

    /*
     * 선수 기록 등록 요청값을 검증
     * 1. uploadId, playerId 필수값을 검증
     * 2. 기록 수치가 0~255 범위인지 검증
     * 3. 성공 수가 시도 수보다 크지 않은지 검증
     */
    public void validateCreateRequest(CreatePlayerRecordRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }

        validateRequiredIds(request.getUploadId(), request.getPlayerId());

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

    // 선수 기록 수정 요청값을 검증
    public void validateUpdateRequest(UpdatePlayerRecordRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }

        validateRequiredIds(request.getUploadId(), request.getPlayerId());

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

    /*
     * 선수 기록 등록 중복 여부를 검증
     * 같은 경기 영상과 같은 선수 조합의 활성 기록은 1개만 허용
     */
    public void validateDuplicateForCreate(
            GameVideoUploadEntity matchVideo,
            MemberEntity player
    ) {
        boolean exists = playerRecordRepository
                .existsByGameVideoUploadAndPlayerAndIsDeletedFalse(matchVideo, player);

        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_PLAYER_RECORD);
        }
    }

    /*
     * 선수 기록 수정 중복 여부를 검증
     * 자기 자신을 제외하고 같은 경기 영상과 같은 선수 조합의 활성 기록이 있으면 실패
     */
    public void validateDuplicateForUpdate(
            Integer recordId,
            GameVideoUploadEntity matchVideo,
            MemberEntity player
    ) {
        boolean exists = playerRecordRepository
                .existsByGameVideoUploadAndPlayerAndIdNotAndIsDeletedFalse(
                        matchVideo,
                        player,
                        recordId
                );

        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_PLAYER_RECORD);
        }
    }

    // 선수 본인 기록 접근 여부 검증
    public void validatePlayerOwnRecord(
            Integer loginMemberId,
            PlayerRecordEntity playerRecord
    ) {
        if (loginMemberId == null || playerRecord == null || playerRecord.getPlayer() == null) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_ACCESS_DENIED);
        }

        Integer recordPlayerId = playerRecord.getPlayer().getId();

        if (!loginMemberId.equals(recordPlayerId)) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_ACCESS_DENIED);
        }
    }

    private void validateRequiredIds(Integer uploadId, Integer playerId) {
        if (uploadId == null || playerId == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }
    }

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
        validateTinyIntUnsigned(minutesPlayed);
        validateTinyIntUnsigned(goals);
        validateTinyIntUnsigned(assists);
        validateTinyIntUnsigned(shots);
        validateTinyIntUnsigned(shotsOnTarget);
        validateTinyIntUnsigned(passes);
        validateTinyIntUnsigned(successfulPasses);
        validateTinyIntUnsigned(dribbles);
        validateTinyIntUnsigned(successfulDribbles);
        validateTinyIntUnsigned(tackles);
        validateTinyIntUnsigned(interceptions);
        validateTinyIntUnsigned(clearances);
        validateTinyIntUnsigned(saves);
        validateTinyIntUnsigned(yellowCards);
        validateTinyIntUnsigned(redCards);

        validateSuccessCount(shotsOnTarget, shots);
        validateSuccessCount(successfulPasses, passes);
        validateSuccessCount(successfulDribbles, dribbles);
    }

    private void validateTinyIntUnsigned(Integer value) {
        if (value == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }

        if (value < MIN_TINYINT_UNSIGNED_VALUE || value > MAX_TINYINT_UNSIGNED_VALUE) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }
    }

    private void validateSuccessCount(Integer successCount, Integer totalCount) {
        if (successCount > totalCount) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }
    }
}