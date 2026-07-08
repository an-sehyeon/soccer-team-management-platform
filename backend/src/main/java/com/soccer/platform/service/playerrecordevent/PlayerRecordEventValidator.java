package com.soccer.platform.service.playerrecordevent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.common.constants.PlayerRecordClipSourceTypeEnum;
import com.soccer.platform.common.constants.PlayerRecordEventTypeEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventRequestDTO;
import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventWithClipLinkRequestDTO;
import com.soccer.platform.dto.playerrecordevent.UpdatePlayerRecordEventRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerRecordEntity;
import com.soccer.platform.entity.PlayerRecordEventEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.GameVideoUploadRepository;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.repository.PlayerRecordEventRepository;
import com.soccer.platform.repository.PlayerRecordRepository;
import com.soccer.platform.repository.PlayerVideoClipRepository;
import com.soccer.platform.repository.TeamVideoClipRepository;

import lombok.RequiredArgsConstructor;

// 선수 기록 이벤트 Validator
// 선수 기록 이벤트 생성, 수정, 삭제, 조회에 필요한 요청값/권한/클립 연결 검증을 담당한다.
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerRecordEventValidator {

    private static final int MIN_EVENT_TIME_SEC = 0;
    private static final int MIN_EVENT_VALUE = 1;
    private static final int MAX_EVENT_VALUE = 255;
    private static final int MAX_EVENT_MEMO_LENGTH = 255;

    private final MemberRepository memberRepository;
    private final GameVideoUploadRepository gameVideoUploadRepository;
    private final PlayerRecordRepository playerRecordRepository;
    private final PlayerRecordEventRepository playerRecordEventRepository;
    private final TeamVideoClipRepository teamVideoClipRepository;
    private final PlayerVideoClipRepository playerVideoClipRepository;

    // 선수 기록 이벤트 등록 요청값 검증
    public void validateCreateRequest(CreatePlayerRecordEventRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_REQUEST);
        }

        validateRequiredCreateIds(request.getUploadId(), request.getPlayerId());
        parseEventType(request.getEventType());
        validateEventTimeRangeValues(
            request.getEventStartTimeSec(),
            request.getEventEndTimeSec()
        );
        validateEventValue(request.getValue());
        validateEventMemo(request.getEventMemo());
    }

    // 선수 기록 이벤트 + 클립 연결 등록 요청값 검증
    public void validateCreateWithClipLinkRequest(CreatePlayerRecordEventWithClipLinkRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_REQUEST);
        }

        validateRequiredCreateIds(request.getUploadId(), request.getPlayerId());
        parseEventType(request.getEventType());
        validateEventTimeRangeValues(
            request.getEventStartTimeSec(),
            request.getEventEndTimeSec()
        );
        validateEventValue(request.getValue());
        validateEventMemo(request.getEventMemo());
        validateClipLinkRequest(request);
    }

    // 선수 기록 이벤트 수정 요청값 검증
    public void validateUpdateRequest(UpdatePlayerRecordEventRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_REQUEST);
        }

        parseEventType(request.getEventType());
        validateEventTimeRangeValues(
            request.getEventStartTimeSec(),
            request.getEventEndTimeSec()
        );
        validateEventValue(request.getValue());
        validateEventMemo(request.getEventMemo());
    }

    // 원본 경기 영상 길이 기준 이벤트 시간 범위 검증
    public void validateEventTimeRangeWithinMatchVideo(
        GameVideoUploadEntity matchVideo,
        Integer eventStartTimeSec,
        Integer eventEndTimeSec
    ) {
        validateEventTimeRangeValues(eventStartTimeSec, eventEndTimeSec);

        if (matchVideo == null || matchVideo.getDurationSec() == null) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_DURATION_NOT_READY);
        }

        if (eventEndTimeSec > matchVideo.getDurationSec()) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_TIME);
        }
    }

    // 관리용 API 접근 권한 검증
    public void validateManagementAccess(MemberEntity loginMember) {
        if (loginMember == null || loginMember.getMemberRole() == null) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED);
        }

        MemberRoleEnum loginMemberRole = loginMember.getMemberRole();

        if (loginMemberRole != MemberRoleEnum.COACH && loginMemberRole != MemberRoleEnum.ANALYST) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED);
        }
    }

    // 선수 본인 기록 이벤트 목록 접근 검증
    public void validatePlayerOwnRecordAccess(Integer loginMemberId, PlayerRecordEntity playerRecord) {
        if (loginMemberId == null || playerRecord == null || playerRecord.getPlayer() == null) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_EVENT_ACCESS_DENIED);
        }

        Integer recordPlayerId = playerRecord.getPlayer().getId();

        if (!loginMemberId.equals(recordPlayerId)) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_EVENT_ACCESS_DENIED);
        }
    }

    // 선수 본인 기록 이벤트 상세 접근 검증
    public void validatePlayerOwnEventAccess(Integer loginMemberId, PlayerRecordEventEntity event) {
        if (
            loginMemberId == null
                || event == null
                || event.getPlayerRecord() == null
                || event.getPlayerRecord().getPlayer() == null
        ) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_EVENT_ACCESS_DENIED);
        }

        Integer eventPlayerId = event.getPlayerRecord().getPlayer().getId();

        if (!loginMemberId.equals(eventPlayerId)) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_EVENT_ACCESS_DENIED);
        }
    }

    // 기록 대상 선수가 선수 역할인지 검증
    public void validatePlayerRole(MemberEntity player) {
        if (player == null || player.getMemberRole() != MemberRoleEnum.PLAYER) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_PLAYER);
        }
    }

    // 팀 분석 클립과 선수 기록의 경기 영상 일치 여부 검증
    public void validateTeamClipMatchesPlayerRecord(
        PlayerRecordEntity playerRecord,
        TeamVideoClipEntity teamVideoClip
    ) {
        if (
            playerRecord == null
                || playerRecord.getGameVideoUpload() == null
                || teamVideoClip == null
                || teamVideoClip.getGameVideoUpload() == null
        ) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH);
        }

        Integer recordUploadId = playerRecord.getGameVideoUpload().getId();
        Integer teamClipUploadId = teamVideoClip.getGameVideoUpload().getId();

        if (!recordUploadId.equals(teamClipUploadId)) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH);
        }
    }

    // 선수 개인 분석 클립과 선수 기록의 경기 영상 및 대상 선수 일치 여부 검증
    public void validatePlayerClipMatchesPlayerRecord(
        PlayerRecordEntity playerRecord,
        PlayerVideoClipEntity playerVideoClip
    ) {
        if (
            playerRecord == null
                || playerRecord.getGameVideoUpload() == null
                || playerRecord.getPlayer() == null
                || playerVideoClip == null
                || playerVideoClip.getGameVideoUpload() == null
                || playerVideoClip.getPlayer() == null
        ) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH);
        }

        Integer recordUploadId = playerRecord.getGameVideoUpload().getId();
        Integer playerClipUploadId = playerVideoClip.getGameVideoUpload().getId();

        if (!recordUploadId.equals(playerClipUploadId)) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH);
        }

        Integer recordPlayerId = playerRecord.getPlayer().getId();
        Integer clipPlayerId = playerVideoClip.getPlayer().getId();

        if (!recordPlayerId.equals(clipPlayerId)) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_EVENT_PLAYER_MISMATCH);
        }
    }

    // 로그인 회원 조회
    public MemberEntity findActiveLoginMember(Integer loginMemberId) {
        if (loginMemberId == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return memberRepository.findByIdAndIsDeletedFalse(loginMemberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 선수 조회
    public MemberEntity findActivePlayer(Integer playerId) {
        if (playerId == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        MemberEntity player = memberRepository.findByIdAndIsDeletedFalse(playerId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        validatePlayerRole(player);

        return player;
    }

    // 경기 영상 조회
    public GameVideoUploadEntity findActiveMatchVideo(Integer uploadId) {
        if (uploadId == null) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_NOT_FOUND);
        }

        return gameVideoUploadRepository.findByIdAndIsDeletedFalse(uploadId)
            .orElseThrow(() -> new CustomException(ErrorCode.MATCH_VIDEO_NOT_FOUND));
    }

    // 선수 기록 조회
    public PlayerRecordEntity findActivePlayerRecord(Integer recordId) {
        if (recordId == null) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_NOT_FOUND);
        }

        return playerRecordRepository.findByIdAndIsDeletedFalse(recordId)
            .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_RECORD_NOT_FOUND));
    }

    // 선수 기록 이벤트 조회
    public PlayerRecordEventEntity findActivePlayerRecordEvent(Integer eventId) {
        if (eventId == null) {
            throw new CustomException(ErrorCode.PLAYER_RECORD_EVENT_NOT_FOUND);
        }

        return playerRecordEventRepository.findByIdAndIsDeletedFalse(eventId)
            .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_RECORD_EVENT_NOT_FOUND));
    }

    // 팀 분석 클립 조회
    public TeamVideoClipEntity findActiveTeamVideoClip(Integer teamClipId) {
        if (teamClipId == null) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND);
        }

        return teamVideoClipRepository.findByIdAndIsDeletedFalse(teamClipId)
            .orElseThrow(() -> new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND));
    }

    // 선수 개인 분석 클립 조회
    public PlayerVideoClipEntity findActivePlayerVideoClip(Integer playerClipId) {
        if (playerClipId == null) {
            throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND);
        }

        return playerVideoClipRepository.findByIdAndIsDeletedFalse(playerClipId)
            .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND));
    }

    // 이벤트 유형 파싱
    public PlayerRecordEventTypeEnum parseEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_TYPE);
        }

        try {
            return PlayerRecordEventTypeEnum.valueOf(eventType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_TYPE);
        }
    }

    // 클립 출처 유형 파싱
    public PlayerRecordClipSourceTypeEnum parseClipSourceType(String clipSourceType) {
        if (clipSourceType == null || clipSourceType.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE);
        }

        try {
            return PlayerRecordClipSourceTypeEnum.valueOf(clipSourceType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE);
        }
    }

    private void validateRequiredCreateIds(Integer uploadId, Integer playerId) {
        if (uploadId == null || playerId == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_REQUEST);
        }
    }

    private void validateClipLinkRequest(CreatePlayerRecordEventWithClipLinkRequestDTO request) {
        PlayerRecordClipSourceTypeEnum clipSourceType = parseClipSourceType(request.getClipSourceType());

        if (clipSourceType == PlayerRecordClipSourceTypeEnum.TEAM_ANALYSIS) {
            validateTeamClipLinkRequest(request);
            return;
        }

        if (clipSourceType == PlayerRecordClipSourceTypeEnum.PLAYER_ANALYSIS) {
            validatePlayerClipLinkRequest(request);
            return;
        }

        throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE);
    }

    private void validateTeamClipLinkRequest(CreatePlayerRecordEventWithClipLinkRequestDTO request) {
        if (request.getTeamClipId() == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_REQUEST);
        }

        if (request.getPlayerClipId() != null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE);
        }
    }

    private void validatePlayerClipLinkRequest(CreatePlayerRecordEventWithClipLinkRequestDTO request) {
        if (request.getPlayerClipId() == null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_REQUEST);
        }

        if (request.getTeamClipId() != null) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE);
        }
    }

    private void validateEventTimeRangeValues(
        Integer eventStartTimeSec,
        Integer eventEndTimeSec
    ) {
        if (
            eventStartTimeSec == null
                || eventEndTimeSec == null
                || eventStartTimeSec < MIN_EVENT_TIME_SEC
                || eventEndTimeSec <= eventStartTimeSec
        ) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_TIME);
        }
    }

    private void validateEventValue(Integer value) {
        if (value == null || value < MIN_EVENT_VALUE || value > MAX_EVENT_VALUE) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_VALUE);
        }
    }

    private void validateEventMemo(String eventMemo) {
        if (eventMemo != null && eventMemo.length() > MAX_EVENT_MEMO_LENGTH) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_REQUEST);
        }
    }
}