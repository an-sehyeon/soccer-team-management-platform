package com.soccer.platform.service.playerrecordevent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.common.constants.PlayerRecordClipSourceTypeEnum;
import com.soccer.platform.common.constants.PlayerRecordEventTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventWithClipLinkRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerRecordEntity;
import com.soccer.platform.entity.PlayerRecordEventEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.GameVideoUploadRepository;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.repository.PlayerRecordEventClipRepository;
import com.soccer.platform.repository.PlayerRecordEventRepository;
import com.soccer.platform.repository.PlayerRecordRepository;
import com.soccer.platform.repository.PlayerVideoClipRepository;
import com.soccer.platform.repository.TeamVideoClipRepository;

import lombok.RequiredArgsConstructor;

// 선수 기록 이벤트 Validator
// 클립 연결 이벤트 요청, 접근 권한, 클립 상태와 중복을 검증한다.
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerRecordEventValidator {

    private static final int MIN_EVENT_TIME_SEC = 0;
    private static final int MAX_EVENT_MEMO_LENGTH = 255;

    private final MemberRepository memberRepository;
    private final GameVideoUploadRepository gameVideoUploadRepository;
    private final PlayerRecordRepository playerRecordRepository;
    private final PlayerRecordEventRepository playerRecordEventRepository;
    private final PlayerRecordEventClipRepository
            playerRecordEventClipRepository;
    private final TeamVideoClipRepository teamVideoClipRepository;
    private final PlayerVideoClipRepository playerVideoClipRepository;

    /*
     * 선수 기록 이벤트와 클립 연결 등록 요청값을 검증한다.
     *
     * 이벤트 시간과 value는 요청받지 않는다.
     * 이벤트 시간은 선택한 클립에서 조회하고 value는 1로 적용한다.
     */
    public void validateCreateWithClipLinkRequest(
            CreatePlayerRecordEventWithClipLinkRequestDTO request
    ) {
        if (request == null) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_EVENT_REQUEST
            );
        }

        validateRequiredCreateIds(
                request.getUploadId(),
                request.getPlayerId()
        );

        parseEventType(request.getEventType());
        validateEventMemo(request.getEventMemo());
        validateClipLinkRequest(request);
    }

    /*
     * 원본 경기 영상 길이 기준 이벤트 시간 범위를 검증한다.
     *
     * 클립 연결 등록 시 선택한 클립의 시작·종료 시간을 전달한다.
     */
    public void validateEventTimeRangeWithinMatchVideo(
            GameVideoUploadEntity matchVideo,
            Integer eventStartTimeSec,
            Integer eventEndTimeSec
    ) {
        validateEventTimeRangeValues(
                eventStartTimeSec,
                eventEndTimeSec
        );

        if (matchVideo == null
                || matchVideo.getDurationSec() == null) {
            throw new CustomException(
                    ErrorCode.MATCH_VIDEO_DURATION_NOT_READY
            );
        }

        if (eventEndTimeSec > matchVideo.getDurationSec()) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_EVENT_TIME
            );
        }
    }

    // 관리용 선수 기록 이벤트 접근 권한을 검증한다.
    public void validateManagementAccess(MemberEntity loginMember) {
        if (loginMember == null
                || loginMember.getMemberRole() == null) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED
            );
        }

        MemberRoleEnum memberRole = loginMember.getMemberRole();

        if (memberRole != MemberRoleEnum.COACH
                && memberRole != MemberRoleEnum.ANALYST) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED
            );
        }
    }

    // 로그인 선수가 자신의 선수 기록 이벤트 목록에 접근하는지 검증한다.
    public void validatePlayerOwnRecordAccess(
            Integer loginMemberId,
            PlayerRecordEntity playerRecord
    ) {
        if (loginMemberId == null
                || playerRecord == null
                || playerRecord.getPlayer() == null) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_EVENT_ACCESS_DENIED
            );
        }

        Integer recordPlayerId =
                playerRecord.getPlayer().getId();

        if (!loginMemberId.equals(recordPlayerId)) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_EVENT_ACCESS_DENIED
            );
        }
    }

    // 로그인 선수가 자신의 선수 기록 이벤트 상세에 접근하는지 검증한다.
    public void validatePlayerOwnEventAccess(
            Integer loginMemberId,
            PlayerRecordEventEntity event
    ) {
        if (loginMemberId == null
                || event == null
                || event.getPlayerRecord() == null
                || event.getPlayerRecord().getPlayer() == null) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_EVENT_ACCESS_DENIED
            );
        }

        Integer eventPlayerId =
                event.getPlayerRecord()
                        .getPlayer()
                        .getId();

        if (!loginMemberId.equals(eventPlayerId)) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_EVENT_ACCESS_DENIED
            );
        }
    }

    // 선수 기록 대상 회원이 PLAYER 역할인지 검증한다.
    public void validatePlayerRole(MemberEntity player) {
        if (player == null
                || player.getMemberRole() != MemberRoleEnum.PLAYER) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_PLAYER
            );
        }
    }

    /*
     * 팀 분석 클립과 선수 기록의 원본 경기 영상이
     * 일치하는지 검증한다.
     */
    public void validateTeamClipMatchesPlayerRecord(
            PlayerRecordEntity playerRecord,
            TeamVideoClipEntity teamVideoClip
    ) {
        if (playerRecord == null
                || playerRecord.getGameVideoUpload() == null
                || teamVideoClip == null
                || teamVideoClip.getGameVideoUpload() == null) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH
            );
        }

        Integer recordUploadId =
                playerRecord.getGameVideoUpload().getId();

        Integer clipUploadId =
                teamVideoClip.getGameVideoUpload().getId();

        if (!recordUploadId.equals(clipUploadId)) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH
            );
        }
    }

    /*
     * 선수 개인 분석 클립과 선수 기록의 경기 영상 및
     * 대상 선수가 모두 일치하는지 검증한다.
     */
    public void validatePlayerClipMatchesPlayerRecord(
            PlayerRecordEntity playerRecord,
            PlayerVideoClipEntity playerVideoClip
    ) {
        if (playerRecord == null
                || playerRecord.getGameVideoUpload() == null
                || playerRecord.getPlayer() == null
                || playerVideoClip == null
                || playerVideoClip.getGameVideoUpload() == null
                || playerVideoClip.getPlayer() == null) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH
            );
        }

        Integer recordUploadId =
                playerRecord.getGameVideoUpload().getId();

        Integer clipUploadId =
                playerVideoClip.getGameVideoUpload().getId();

        if (!recordUploadId.equals(clipUploadId)) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH
            );
        }

        Integer recordPlayerId =
                playerRecord.getPlayer().getId();

        Integer clipPlayerId =
                playerVideoClip.getPlayer().getId();

        if (!recordPlayerId.equals(clipPlayerId)) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_EVENT_PLAYER_MISMATCH
            );
        }
    }

    // 팀 분석 클립이 READY 상태인지 검증한다.
    public void validateTeamClipReady(
            TeamVideoClipEntity teamVideoClip
    ) {
        if (teamVideoClip == null
                || teamVideoClip.getStatus()
                        != VideoUploadStatusEnum.READY) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_EVENT_CLIP_NOT_READY
            );
        }
    }

    // 선수 개인 분석 클립이 READY 상태인지 검증한다.
    public void validatePlayerClipReady(
            PlayerVideoClipEntity playerVideoClip
    ) {
        if (playerVideoClip == null
                || playerVideoClip.getStatus()
                        != VideoUploadStatusEnum.READY) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_EVENT_CLIP_NOT_READY
            );
        }
    }

    /*
     * 선택한 클립에 같은 이벤트 유형의 활성 연결이 있는지 검증한다.
     *
     * 같은 클립에 다른 이벤트 유형을 등록하는 것은 허용한다.
     */
    public void validateDuplicateClipEventType(
            PlayerRecordClipSourceTypeEnum clipSourceType,
            PlayerRecordEventTypeEnum eventType,
            Integer teamClipId,
            Integer playerClipId
    ) {
        if (clipSourceType == null) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE
            );
        }

        if (eventType == null) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_EVENT_TYPE
            );
        }

        boolean duplicateExists;

        if (clipSourceType
                == PlayerRecordClipSourceTypeEnum.TEAM_ANALYSIS) {
            validateTeamClipIds(teamClipId, playerClipId);

            duplicateExists =
                    playerRecordEventClipRepository
                            .existsActiveTeamClipEventType(
                                    PlayerRecordClipSourceTypeEnum
                                            .TEAM_ANALYSIS,
                                    teamClipId,
                                    eventType
                            );
        } else if (clipSourceType
                == PlayerRecordClipSourceTypeEnum.PLAYER_ANALYSIS) {
            validatePlayerClipIds(teamClipId, playerClipId);

            duplicateExists =
                    playerRecordEventClipRepository
                            .existsActivePlayerClipEventType(
                                    PlayerRecordClipSourceTypeEnum
                                            .PLAYER_ANALYSIS,
                                    playerClipId,
                                    eventType
                            );
        } else {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE
            );
        }

        if (duplicateExists) {
            throw new CustomException(
                    ErrorCode.DUPLICATE_PLAYER_RECORD_EVENT_CLIP
            );
        }
    }

    // 삭제되지 않은 로그인 회원을 조회한다.
    public MemberEntity findActiveLoginMember(
            Integer loginMemberId
    ) {
        if (loginMemberId == null) {
            throw new CustomException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        return memberRepository
                .findByIdAndIsDeletedFalse(loginMemberId)
                .orElseThrow(
                        () -> new CustomException(
                                ErrorCode.MEMBER_NOT_FOUND
                        )
                );
    }

    // 기록 대상 선수를 조회하고 PLAYER 역할인지 검증한다.
    public MemberEntity findActivePlayer(Integer playerId) {
        if (playerId == null) {
            throw new CustomException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        MemberEntity player =
                memberRepository
                        .findByIdAndIsDeletedFalse(playerId)
                        .orElseThrow(
                                () -> new CustomException(
                                        ErrorCode.MEMBER_NOT_FOUND
                                )
                        );

        validatePlayerRole(player);

        return player;
    }

    // 삭제되지 않은 경기 영상을 조회한다.
    public GameVideoUploadEntity findActiveMatchVideo(
            Integer uploadId
    ) {
        if (uploadId == null) {
            throw new CustomException(
                    ErrorCode.MATCH_VIDEO_NOT_FOUND
            );
        }

        return gameVideoUploadRepository
                .findByIdAndIsDeletedFalse(uploadId)
                .orElseThrow(
                        () -> new CustomException(
                                ErrorCode.MATCH_VIDEO_NOT_FOUND
                        )
                );
    }

    // 삭제되지 않은 선수 요약 기록을 조회한다.
    public PlayerRecordEntity findActivePlayerRecord(
            Integer recordId
    ) {
        if (recordId == null) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_NOT_FOUND
            );
        }

        return playerRecordRepository
                .findByIdAndIsDeletedFalse(recordId)
                .orElseThrow(
                        () -> new CustomException(
                                ErrorCode.PLAYER_RECORD_NOT_FOUND
                        )
                );
    }

    // 삭제되지 않은 선수 기록 이벤트를 조회한다.
    public PlayerRecordEventEntity findActivePlayerRecordEvent(
            Integer eventId
    ) {
        if (eventId == null) {
            throw new CustomException(
                    ErrorCode.PLAYER_RECORD_EVENT_NOT_FOUND
            );
        }

        return playerRecordEventRepository
                .findByIdAndIsDeletedFalse(eventId)
                .orElseThrow(
                        () -> new CustomException(
                                ErrorCode.PLAYER_RECORD_EVENT_NOT_FOUND
                        )
                );
    }

    // 선수 기록 이벤트 연결용 팀 분석 클립을 잠금 조회한다.
    public TeamVideoClipEntity findActiveTeamVideoClipForUpdate(
            Integer teamClipId
    ) {
        if (teamClipId == null) {
            throw new CustomException(
                    ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND
            );
        }

        return teamVideoClipRepository
                .findByIdAndIsDeletedFalseForUpdate(teamClipId)
                .orElseThrow(
                        () -> new CustomException(
                                ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND
                        )
                );
    }

    // 선수 기록 이벤트 연결용 선수 개인 분석 클립을 잠금 조회한다.
    public PlayerVideoClipEntity
            findActivePlayerVideoClipForUpdate(
                    Integer playerClipId
            ) {
        if (playerClipId == null) {
            throw new CustomException(
                    ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND
            );
        }

        return playerVideoClipRepository
                .findByIdAndIsDeletedFalseForUpdate(playerClipId)
                .orElseThrow(
                        () -> new CustomException(
                                ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND
                        )
                );
    }

    // 문자열 이벤트 유형을 Enum으로 변환한다.
    public PlayerRecordEventTypeEnum parseEventType(
            String eventType
    ) {
        if (eventType == null || eventType.isBlank()) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_EVENT_TYPE
            );
        }

        try {
            return PlayerRecordEventTypeEnum.valueOf(
                    eventType.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_EVENT_TYPE
            );
        }
    }

    // 문자열 클립 출처 유형을 Enum으로 변환한다.
    public PlayerRecordClipSourceTypeEnum parseClipSourceType(
            String clipSourceType
    ) {
        if (clipSourceType == null
                || clipSourceType.isBlank()) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE
            );
        }

        try {
            return PlayerRecordClipSourceTypeEnum.valueOf(
                    clipSourceType.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE
            );
        }
    }

    // 클립 연결 등록에 필요한 경기 영상 ID와 선수 ID를 검증한다.
    private void validateRequiredCreateIds(
            Integer uploadId,
            Integer playerId
    ) {
        if (uploadId == null || playerId == null) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_EVENT_REQUEST
            );
        }
    }

    // clipSourceType과 두 클립 ID의 조합을 검증한다.
    private void validateClipLinkRequest(
            CreatePlayerRecordEventWithClipLinkRequestDTO request
    ) {
        PlayerRecordClipSourceTypeEnum clipSourceType =
                parseClipSourceType(
                        request.getClipSourceType()
                );

        if (clipSourceType
                == PlayerRecordClipSourceTypeEnum.TEAM_ANALYSIS) {
            validateTeamClipIds(
                    request.getTeamClipId(),
                    request.getPlayerClipId()
            );
            return;
        }

        if (clipSourceType
                == PlayerRecordClipSourceTypeEnum.PLAYER_ANALYSIS) {
            validatePlayerClipIds(
                    request.getTeamClipId(),
                    request.getPlayerClipId()
            );
            return;
        }

        throw new CustomException(
                ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE
        );
    }

    // 팀 분석 클립 연결 ID 조합을 검증한다.
    private void validateTeamClipIds(
            Integer teamClipId,
            Integer playerClipId
    ) {
        if (teamClipId == null || playerClipId != null) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE
            );
        }
    }

    // 선수 개인 분석 클립 연결 ID 조합을 검증한다.
    private void validatePlayerClipIds(
            Integer teamClipId,
            Integer playerClipId
    ) {
        if (playerClipId == null || teamClipId != null) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE
            );
        }
    }

    // 이벤트 시작·종료 시간의 기본 범위를 검증한다.
    private void validateEventTimeRangeValues(
            Integer eventStartTimeSec,
            Integer eventEndTimeSec
    ) {
        if (eventStartTimeSec == null
                || eventEndTimeSec == null
                || eventStartTimeSec < MIN_EVENT_TIME_SEC
                || eventEndTimeSec <= eventStartTimeSec) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_EVENT_TIME
            );
        }
    }

    // 이벤트 메모가 DB 최대 길이를 초과하지 않는지 검증한다.
    private void validateEventMemo(String eventMemo) {
        if (eventMemo != null
                && eventMemo.length()
                        > MAX_EVENT_MEMO_LENGTH) {
            throw new CustomException(
                    ErrorCode.INVALID_PLAYER_RECORD_EVENT_REQUEST
            );
        }
    }
}