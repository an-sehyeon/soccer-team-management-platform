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
// 선수 기록 요청값, 중복 기록, 선수 본인 접근 권한을 검증한다.
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerRecordValidator {

    private static final int MIN_TINYINT_UNSIGNED_VALUE = 0;
    private static final int MAX_TINYINT_UNSIGNED_VALUE = 255;

    private final PlayerRecordRepository playerRecordRepository;

    /*
     * 선수 기록 등록 요청값을 검증한다.
     * 1. uploadId와 playerId 필수값을 검증한다.
     * 2. 모든 기록 수치가 0~255 범위인지 검증한다.
     *
     * 유효 슈팅과 슈팅, 성공 패스와 패스 등 기록 간 추가 정합성 검증은
     * 현재 정책에서 적용하지 않는다.
     */
    public void validateCreateRequest(CreatePlayerRecordRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }

        validateRequiredIds(
                request.getUploadId(),
                request.getPlayerId()
        );

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
     * 선수 기록 수정 요청값을 검증한다.
     * 모든 기록 수치는 0~255 범위만 검증한다.
     */
    public void validateUpdateRequest(UpdatePlayerRecordRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_VALUE);
        }

        validateRequiredIds(
                request.getUploadId(),
                request.getPlayerId()
        );

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
     * 선수 기록 등록 중복 여부를 검증한다.
     * 같은 경기 영상과 같은 선수 조합의 활성 기록은 1개만 허용한다.
     */
    public void validateDuplicateForCreate(
            GameVideoUploadEntity matchVideo,
            MemberEntity player
    ) {
        boolean exists = playerRecordRepository
                .existsByGameVideoUploadAndPlayerAndIsDeletedFalse(
                        matchVideo,
                        player
                );

        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_PLAYER_RECORD);
        }
    }

    /*
     * 선수 기록 수정 중복 여부를 검증한다.
     * 자기 자신을 제외하고 같은 경기 영상과 같은 선수 조합의
     * 활성 기록이 있으면 수정할 수 없다.
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

    // 로그인한 선수가 자신의 기록에 접근하는지 검증한다.
    public void validatePlayerOwnRecord(
            Integer loginMemberId,
            PlayerRecordEntity playerRecord
    ) {
        if (loginMemberId == null
                || playerRecord == null
                || playerRecord.getPlayer() == null) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_ACCESS_DENIED
            );
        }

        Integer recordPlayerId = playerRecord.getPlayer().getId();

        if (!loginMemberId.equals(recordPlayerId)) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_ACCESS_DENIED
            );
        }
    }

    // 선수 기록 등록·수정에 필요한 필수 ID를 검증한다.
    private void validateRequiredIds(
            Integer uploadId,
            Integer playerId
    ) {
        if (uploadId == null || playerId == null) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_VALUE
            );
        }
    }

    // 선수 기록의 모든 수치가 DB 저장 범위에 포함되는지 검증한다.
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
    }

    // MySQL TINYINT UNSIGNED 저장 범위인 0~255를 검증한다.
    private void validateTinyIntUnsigned(Integer value) {
        if (value == null) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_VALUE
            );
        }

        if (value < MIN_TINYINT_UNSIGNED_VALUE
                || value > MAX_TINYINT_UNSIGNED_VALUE) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_VALUE
            );
        }
    }
}