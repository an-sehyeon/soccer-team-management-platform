package com.soccer.platform.service.playerrecordevent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.PlayerRecordClipSourceTypeEnum;
import com.soccer.platform.common.constants.PlayerRecordEventTypeEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerrecord.CreatePlayerRecordRequestDTO;
import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventRequestDTO;
import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventResponseDTO;
import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventWithClipLinkRequestDTO;
import com.soccer.platform.dto.playerrecordevent.PlayerRecordEventClipResponseDTO;
import com.soccer.platform.dto.playerrecordevent.PlayerRecordEventListResponseDTO;
import com.soccer.platform.dto.playerrecordevent.PlayerRecordEventResponseDTO;
import com.soccer.platform.dto.playerrecordevent.UpdatePlayerRecordEventRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerRecordEntity;
import com.soccer.platform.entity.PlayerRecordEventClipEntity;
import com.soccer.platform.entity.PlayerRecordEventEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.PlayerRecordEventClipRepository;
import com.soccer.platform.repository.PlayerRecordEventRepository;
import com.soccer.platform.repository.PlayerRecordRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.MatchVideoQueryService;
import com.soccer.platform.service.common.MemberQueryService;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;

// 선수 기록 이벤트 Service
// 경기별 선수 요약 기록에 개별 이벤트를 저장하고, 선택적으로 분석 클립과 연결한다.
@Service
@RequiredArgsConstructor
@Transactional
public class PlayerRecordEventService {

    private static final int DEFAULT_RECORD_VALUE = 0;
    private static final int MAX_SUMMARY_VALUE = 255;

    private final PlayerRecordRepository playerRecordRepository;
    private final PlayerRecordEventRepository playerRecordEventRepository;
    private final PlayerRecordEventClipRepository playerRecordEventClipRepository;

    private final PermissionValidator permissionValidator;
    private final MemberQueryService memberQueryService;
    private final MatchVideoQueryService matchVideoQueryService;
    private final PlayerRecordEventValidator playerRecordEventValidator;

    // 선수 기록 이벤트 등록
    public CreatePlayerRecordEventResponseDTO createPlayerRecordEvent(
        CustomUserPrincipal principal,
        CreatePlayerRecordEventRequestDTO request
    ) {
        permissionValidator.requireCoachOrAnalyst(
            principal,
            ErrorCode.PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED
        );

        playerRecordEventValidator.validateCreateRequest(request);

        MemberEntity createdBy = memberQueryService.findLoginMember(
            principal,
            ErrorCode.MEMBER_NOT_FOUND
        );

        GameVideoUploadEntity matchVideo = matchVideoQueryService.findActiveMatchVideoById(
            request.getUploadId(),
            ErrorCode.MATCH_VIDEO_NOT_FOUND
        );

        playerRecordEventValidator.validateEventTimeRangeWithinMatchVideo(
            matchVideo,
            request.getEventStartTimeSec(),
            request.getEventEndTimeSec()
        );

        MemberEntity player = memberQueryService.findActivePlayerById(
            request.getPlayerId(),
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_PLAYER_RECORD_PLAYER
        );

        PlayerRecordEntity playerRecord = findOrCreatePlayerRecord(
            matchVideo,
            player,
            createdBy
        );

        PlayerRecordEventTypeEnum eventType = playerRecordEventValidator.parseEventType(
            request.getEventType()
        );

        PlayerRecordEventEntity playerRecordEvent = PlayerRecordEventEntity.create(
            playerRecord,
            eventType,
            request.getEventStartTimeSec(),
            request.getEventEndTimeSec(),
            request.getValue(),
            request.getEventMemo(),
            createdBy
        );

        PlayerRecordEventEntity savedEvent = playerRecordEventRepository.save(playerRecordEvent);

        applyEventValueToSummary(
            playerRecord,
            savedEvent.getEventType(),
            savedEvent.getValue()
        );

        return toCreateResponseDTO(savedEvent);
    }

    // 선수 기록 이벤트 + 클립 연결 등록
    public CreatePlayerRecordEventResponseDTO createPlayerRecordEventWithClipLink(
        CustomUserPrincipal principal,
        CreatePlayerRecordEventWithClipLinkRequestDTO request
    ) {
        permissionValidator.requireCoachOrAnalyst(
            principal,
            ErrorCode.PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED
        );

        playerRecordEventValidator.validateCreateWithClipLinkRequest(request);

        MemberEntity createdBy = memberQueryService.findLoginMember(
            principal,
            ErrorCode.MEMBER_NOT_FOUND
        );

        GameVideoUploadEntity matchVideo = matchVideoQueryService.findActiveMatchVideoById(
            request.getUploadId(),
            ErrorCode.MATCH_VIDEO_NOT_FOUND
        );

        playerRecordEventValidator.validateEventTimeRangeWithinMatchVideo(
            matchVideo,
            request.getEventStartTimeSec(),
            request.getEventEndTimeSec()
        );

        MemberEntity player = memberQueryService.findActivePlayerById(
            request.getPlayerId(),
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_PLAYER_RECORD_PLAYER
        );

        PlayerRecordEntity playerRecord = findOrCreatePlayerRecord(
            matchVideo,
            player,
            createdBy
        );

        PlayerRecordEventTypeEnum eventType = playerRecordEventValidator.parseEventType(
            request.getEventType()
        );

        PlayerRecordClipSourceTypeEnum clipSourceType = playerRecordEventValidator.parseClipSourceType(
            request.getClipSourceType()
        );

        TeamVideoClipEntity teamVideoClip = null;
        PlayerVideoClipEntity playerVideoClip = null;

        if (clipSourceType == PlayerRecordClipSourceTypeEnum.TEAM_ANALYSIS) {
            teamVideoClip = playerRecordEventValidator.findActiveTeamVideoClip(
                request.getTeamClipId()
            );

            playerRecordEventValidator.validateTeamClipMatchesPlayerRecord(
                playerRecord,
                teamVideoClip
            );
        }

        if (clipSourceType == PlayerRecordClipSourceTypeEnum.PLAYER_ANALYSIS) {
            playerVideoClip = playerRecordEventValidator.findActivePlayerVideoClip(
                request.getPlayerClipId()
            );

            playerRecordEventValidator.validatePlayerClipMatchesPlayerRecord(
                playerRecord,
                playerVideoClip
            );
        }

        PlayerRecordEventEntity playerRecordEvent = PlayerRecordEventEntity.create(
            playerRecord,
            eventType,
            request.getEventStartTimeSec(),
            request.getEventEndTimeSec(),
            request.getValue(),
            request.getEventMemo(),
            createdBy
        );

        PlayerRecordEventEntity savedEvent = playerRecordEventRepository.save(playerRecordEvent);

        PlayerRecordEventClipEntity eventClip = createEventClipLink(
            savedEvent,
            clipSourceType,
            teamVideoClip,
            playerVideoClip
        );

        playerRecordEventClipRepository.save(eventClip);

        applyEventValueToSummary(
            playerRecord,
            savedEvent.getEventType(),
            savedEvent.getValue()
        );

        return toCreateResponseDTO(savedEvent);
    }

    // 관리용 선수 기록 이벤트 목록 조회
    @Transactional(readOnly = true)
    public PlayerRecordEventListResponseDTO findPlayerRecordEventsForManagement(
        CustomUserPrincipal principal,
        Integer recordId
    ) {
        permissionValidator.requireCoachOrAnalyst(
            principal,
            ErrorCode.PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED
        );

        PlayerRecordEntity playerRecord = playerRecordEventValidator.findActivePlayerRecord(recordId);

        List<PlayerRecordEventEntity> events = playerRecordEventRepository
            .findByPlayerRecordAndIsDeletedFalseOrderByEventStartTimeSecAscCreatedAtAsc(playerRecord);

        return toListResponseDTO(events);
    }

    // 관리용 선수 기록 이벤트 상세 조회
    @Transactional(readOnly = true)
    public PlayerRecordEventResponseDTO findPlayerRecordEventDetailForManagement(
        CustomUserPrincipal principal,
        Integer eventId
    ) {
        permissionValidator.requireCoachOrAnalyst(
            principal,
            ErrorCode.PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED
        );

        PlayerRecordEventEntity event = playerRecordEventValidator.findActivePlayerRecordEvent(eventId);

        return toDetailResponseDTO(event);
    }

    // 선수 기록 이벤트 수정
    public PlayerRecordEventResponseDTO updatePlayerRecordEvent(
        CustomUserPrincipal principal,
        Integer eventId,
        UpdatePlayerRecordEventRequestDTO request
    ) {
        permissionValidator.requireCoachOrAnalyst(
            principal,
            ErrorCode.PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED
        );

        playerRecordEventValidator.validateUpdateRequest(request);

        PlayerRecordEventEntity event = playerRecordEventValidator.findActivePlayerRecordEvent(eventId);
        PlayerRecordEntity playerRecord = event.getPlayerRecord();

        playerRecordEventValidator.validateEventTimeRangeWithinMatchVideo(
            playerRecord.getGameVideoUpload(),
            request.getEventStartTimeSec(),
            request.getEventEndTimeSec()
        );

        PlayerRecordEventTypeEnum previousEventType = event.getEventType();
        Integer previousValue = event.getValue();

        PlayerRecordEventTypeEnum nextEventType = playerRecordEventValidator.parseEventType(
            request.getEventType()
        );

        rollbackEventValueFromSummary(
            playerRecord,
            previousEventType,
            previousValue
        );

        event.update(
            nextEventType,
            request.getEventStartTimeSec(),
            request.getEventEndTimeSec(),
            request.getValue(),
            request.getEventMemo()
        );

        applyEventValueToSummary(
            playerRecord,
            nextEventType,
            request.getValue()
        );

        return toDetailResponseDTO(event);
    }

    // 선수 기록 이벤트 삭제
    public void deletePlayerRecordEvent(
        CustomUserPrincipal principal,
        Integer eventId
    ) {
        permissionValidator.requireCoachOrAnalyst(
            principal,
            ErrorCode.PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED
        );

        PlayerRecordEventEntity event = playerRecordEventValidator.findActivePlayerRecordEvent(eventId);

        rollbackEventValueFromSummary(
            event.getPlayerRecord(),
            event.getEventType(),
            event.getValue()
        );

        List<PlayerRecordEventClipEntity> eventClips = playerRecordEventClipRepository
            .findByPlayerRecordEventAndIsDeletedFalse(event);

        eventClips.forEach(PlayerRecordEventClipEntity::softDelete);

        event.softDelete();
    }

    // 선수 본인 기록 이벤트 목록 조회
    @Transactional(readOnly = true)
    public PlayerRecordEventListResponseDTO findMyPlayerRecordEvents(
        CustomUserPrincipal principal,
        Integer recordId
    ) {
        permissionValidator.requirePlayer(
            principal,
            ErrorCode.PLAYER_RECORD_EVENT_ACCESS_DENIED
        );

        PlayerRecordEntity playerRecord = playerRecordEventValidator.findActivePlayerRecord(recordId);

        playerRecordEventValidator.validatePlayerOwnRecordAccess(
            principal.getMemberId(),
            playerRecord
        );

        List<PlayerRecordEventEntity> events = playerRecordEventRepository
            .findByPlayerRecordAndIsDeletedFalseOrderByEventStartTimeSecAscCreatedAtAsc(playerRecord);

        return toListResponseDTO(events);
    }

    // 선수 본인 기록 이벤트 상세 조회
    @Transactional(readOnly = true)
    public PlayerRecordEventResponseDTO findMyPlayerRecordEventDetail(
        CustomUserPrincipal principal,
        Integer eventId
    ) {
        permissionValidator.requirePlayer(
            principal,
            ErrorCode.PLAYER_RECORD_EVENT_ACCESS_DENIED
        );

        PlayerRecordEventEntity event = playerRecordEventValidator.findActivePlayerRecordEvent(eventId);

        playerRecordEventValidator.validatePlayerOwnEventAccess(
            principal.getMemberId(),
            event
        );

        return toDetailResponseDTO(event);
    }

    // 같은 경기 영상과 같은 선수의 활성 player_record를 찾고, 없으면 기본값 0으로 자동 생성
    private PlayerRecordEntity findOrCreatePlayerRecord(
        GameVideoUploadEntity matchVideo,
        MemberEntity player,
        MemberEntity recorder
    ) {
        return playerRecordRepository
            .findFirstByGameVideoUploadAndPlayerAndIsDeletedFalse(matchVideo, player)
            .orElseGet(() -> createDefaultPlayerRecord(matchVideo, player, recorder));
    }

    // 기록 이벤트 등록용 기본 player_record 생성
    private PlayerRecordEntity createDefaultPlayerRecord(
        GameVideoUploadEntity matchVideo,
        MemberEntity player,
        MemberEntity recorder
    ) {
        CreatePlayerRecordRequestDTO defaultRequest = new CreatePlayerRecordRequestDTO();

        defaultRequest.setUploadId(matchVideo.getId());
        defaultRequest.setPlayerId(player.getId());
        defaultRequest.setMinutesPlayed(DEFAULT_RECORD_VALUE);
        defaultRequest.setGoals(DEFAULT_RECORD_VALUE);
        defaultRequest.setAssists(DEFAULT_RECORD_VALUE);
        defaultRequest.setShots(DEFAULT_RECORD_VALUE);
        defaultRequest.setShotsOnTarget(DEFAULT_RECORD_VALUE);
        defaultRequest.setPasses(DEFAULT_RECORD_VALUE);
        defaultRequest.setSuccessfulPasses(DEFAULT_RECORD_VALUE);
        defaultRequest.setDribbles(DEFAULT_RECORD_VALUE);
        defaultRequest.setSuccessfulDribbles(DEFAULT_RECORD_VALUE);
        defaultRequest.setTackles(DEFAULT_RECORD_VALUE);
        defaultRequest.setInterceptions(DEFAULT_RECORD_VALUE);
        defaultRequest.setClearances(DEFAULT_RECORD_VALUE);
        defaultRequest.setSaves(DEFAULT_RECORD_VALUE);
        defaultRequest.setYellowCards(DEFAULT_RECORD_VALUE);
        defaultRequest.setRedCards(DEFAULT_RECORD_VALUE);
        defaultRequest.setMemo(null);

        PlayerRecordEntity playerRecord = PlayerRecordEntity.create(
            matchVideo,
            player,
            recorder,
            defaultRequest
        );

        return playerRecordRepository.save(playerRecord);
    }

    // 이벤트 클립 연결 Entity 생성
    private PlayerRecordEventClipEntity createEventClipLink(
        PlayerRecordEventEntity event,
        PlayerRecordClipSourceTypeEnum clipSourceType,
        TeamVideoClipEntity teamVideoClip,
        PlayerVideoClipEntity playerVideoClip
    ) {
        if (clipSourceType == PlayerRecordClipSourceTypeEnum.TEAM_ANALYSIS) {
            return PlayerRecordEventClipEntity.createTeamClipLink(
                event,
                teamVideoClip
            );
        }

        if (clipSourceType == PlayerRecordClipSourceTypeEnum.PLAYER_ANALYSIS) {
            return PlayerRecordEventClipEntity.createPlayerClipLink(
                event,
                playerVideoClip
            );
        }

        throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE);
    }

    // 이벤트 수치를 player_record 요약 수치에 반영
    private void applyEventValueToSummary(
        PlayerRecordEntity playerRecord,
        PlayerRecordEventTypeEnum eventType,
        Integer value
    ) {
        applySummaryDelta(playerRecord, eventType, normalizeValue(value));
    }

    // 이벤트 수치를 player_record 요약 수치에서 차감
    private void rollbackEventValueFromSummary(
        PlayerRecordEntity playerRecord,
        PlayerRecordEventTypeEnum eventType,
        Integer value
    ) {
        applySummaryDelta(playerRecord, eventType, -normalizeValue(value));
    }

    // 이벤트 유형별 요약 수치 갱신
    private void applySummaryDelta(
        PlayerRecordEntity playerRecord,
        PlayerRecordEventTypeEnum eventType,
        int delta
    ) {
        if (playerRecord == null || eventType == null || delta == 0) {
            return;
        }

        switch (eventType) {
            case GOAL -> playerRecord.setGoals(
                calculateAdjustedSummaryValue(playerRecord.getGoals(), delta)
            );

            case ASSIST -> playerRecord.setAssists(
                calculateAdjustedSummaryValue(playerRecord.getAssists(), delta)
            );

            case SHOT -> playerRecord.setShots(
                calculateAdjustedSummaryValue(playerRecord.getShots(), delta)
            );

            case SHOT_ON_TARGET -> {
                playerRecord.setShots(
                    calculateAdjustedSummaryValue(playerRecord.getShots(), delta)
                );
                playerRecord.setShotsOnTarget(
                    calculateAdjustedSummaryValue(playerRecord.getShotsOnTarget(), delta)
                );
            }

            case PASS -> playerRecord.setPasses(
                calculateAdjustedSummaryValue(playerRecord.getPasses(), delta)
            );

            case SUCCESSFUL_PASS -> {
                playerRecord.setPasses(
                    calculateAdjustedSummaryValue(playerRecord.getPasses(), delta)
                );
                playerRecord.setSuccessfulPasses(
                    calculateAdjustedSummaryValue(playerRecord.getSuccessfulPasses(), delta)
                );
            }

            case DRIBBLE -> playerRecord.setDribbles(
                calculateAdjustedSummaryValue(playerRecord.getDribbles(), delta)
            );

            case SUCCESSFUL_DRIBBLE -> {
                playerRecord.setDribbles(
                    calculateAdjustedSummaryValue(playerRecord.getDribbles(), delta)
                );
                playerRecord.setSuccessfulDribbles(
                    calculateAdjustedSummaryValue(playerRecord.getSuccessfulDribbles(), delta)
                );
            }

            case TACKLE -> playerRecord.setTackles(
                calculateAdjustedSummaryValue(playerRecord.getTackles(), delta)
            );

            case INTERCEPTION -> playerRecord.setInterceptions(
                calculateAdjustedSummaryValue(playerRecord.getInterceptions(), delta)
            );

            case CLEARANCE -> playerRecord.setClearances(
                calculateAdjustedSummaryValue(playerRecord.getClearances(), delta)
            );

            case SAVE -> playerRecord.setSaves(
                calculateAdjustedSummaryValue(playerRecord.getSaves(), delta)
            );

            case YELLOW_CARD -> playerRecord.setYellowCards(
                calculateAdjustedSummaryValue(playerRecord.getYellowCards(), delta)
            );

            case RED_CARD -> playerRecord.setRedCards(
                calculateAdjustedSummaryValue(playerRecord.getRedCards(), delta)
            );

            case ETC -> {
                // ETC는 요약 수치에 반영하지 않는다.
            }
        }
    }

    // 이벤트 값 정규화
    private int normalizeValue(Integer value) {
        if (value == null || value <= 0) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_VALUE);
        }

        return value;
    }

    // player_record 요약 수치 보정
    private Integer calculateAdjustedSummaryValue(
        Integer currentValue,
        int delta
    ) {
        int safeCurrentValue = currentValue == null ? DEFAULT_RECORD_VALUE : currentValue;
        int adjustedValue = safeCurrentValue + delta;

        if (adjustedValue < DEFAULT_RECORD_VALUE) {
            return DEFAULT_RECORD_VALUE;
        }

        if (adjustedValue > MAX_SUMMARY_VALUE) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_RECORD_EVENT_VALUE);
        }

        return adjustedValue;
    }

    // 이벤트 생성 응답 변환
    private CreatePlayerRecordEventResponseDTO toCreateResponseDTO(
        PlayerRecordEventEntity event
    ) {
        PlayerRecordEntity playerRecord = event.getPlayerRecord();
        GameVideoUploadEntity matchVideo = playerRecord.getGameVideoUpload();
        MemberEntity player = playerRecord.getPlayer();

        return new CreatePlayerRecordEventResponseDTO(
            event.getId(),
            playerRecord.getId(),
            matchVideo.getId(),
            matchVideo.getTitle(),
            player.getId(),
            player.getName(),
            event.getEventType().name(),
            event.getEventStartTimeSec(),
            event.getEventEndTimeSec(),
            event.getValue(),
            event.getMemo(),
            event.getCreatedAt()
        );
    }

    // 이벤트 상세 응답 변환
    private PlayerRecordEventResponseDTO toDetailResponseDTO(
        PlayerRecordEventEntity event
    ) {
        List<PlayerRecordEventClipEntity> eventClips = playerRecordEventClipRepository
            .findByPlayerRecordEventAndIsDeletedFalse(event);

        List<PlayerRecordEventClipResponseDTO> clipResponses = eventClips.stream()
            .map(PlayerRecordEventClipResponseDTO::from)
            .toList();

        return PlayerRecordEventResponseDTO.from(event, clipResponses);
    }

    // 이벤트 목록 응답 변환
    private PlayerRecordEventListResponseDTO toListResponseDTO(
        List<PlayerRecordEventEntity> events
    ) {
        if (events == null || events.isEmpty()) {
            return new PlayerRecordEventListResponseDTO(List.of());
        }

        List<PlayerRecordEventClipEntity> eventClips = playerRecordEventClipRepository
            .findByPlayerRecordEventInAndIsDeletedFalse(events);

        Map<Integer, List<PlayerRecordEventClipResponseDTO>> clipResponsesByEventId = eventClips.stream()
            .collect(
                Collectors.groupingBy(
                    eventClip -> eventClip.getPlayerRecordEvent().getId(),
                    Collectors.mapping(
                        PlayerRecordEventClipResponseDTO::from,
                        Collectors.toList()
                    )
                )
            );

        List<PlayerRecordEventResponseDTO> eventResponses = events.stream()
            .map(event -> PlayerRecordEventResponseDTO.from(
                event,
                clipResponsesByEventId.getOrDefault(event.getId(), List.of())
            ))
            .toList();

        return new PlayerRecordEventListResponseDTO(eventResponses);
    }
}