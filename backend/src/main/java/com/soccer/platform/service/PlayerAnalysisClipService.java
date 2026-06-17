package com.soccer.platform.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.common.constants.PlayerClipTypeEnum;
import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipRequestDTO;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipResponseDTO;
import com.soccer.platform.dto.playerclip.PlayerAnalysisClipDetailResponseDTO;
import com.soccer.platform.dto.playerclip.PlayerAnalysisClipListResponseDTO;
import com.soccer.platform.dto.playerclip.PlayerAnalysisClipPageResponseDTO;
import com.soccer.platform.dto.playerclip.UpdatePlayerAnalysisClipRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.repository.GameVideoUploadRepository;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.repository.PlayerVideoClipRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

/*
 * 선수 개인 분석 클립 Service
 *
 * 선수 개인 피드백용 클립을 등록, 조회, 수정, 삭제
 *
 * - 지도자/분석관의 개인 클립 관리 권한 검증
 * - 선수 본인 개인 클립 조회 권한 검증
 * - 원본 경기 영상 존재 여부 검증
 * - 대상 선수가 실제 PLAYER 역할인지 검증
 * - 클립 시간 구간 검증
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PlayerAnalysisClipService {

    private final PlayerVideoClipRepository playerVideoClipRepository;
    private final GameVideoUploadRepository gameVideoUploadRepository;
    private final MemberRepository memberRepository;


    /*
     * 선수 개인 분석 클립 등록
     *
     * - 로그인한 사용자가 COACH 또는 ANALYST인지 확인
     * - 원본 경기 영상이 존재하고 삭제되지 않았는지 확인
     * - 대상 회원이 실제 PLAYER 역할인지 확인
     * - 클립 제목, 유형, 시작/종료 시간을 검증
     */
    public CreatePlayerAnalysisClipResponseDTO createPlayerAnalysisClip(
            CreatePlayerAnalysisClipRequestDTO request,
            CustomUserPrincipal principal
    ) {
        checkCanCreateOrUpdate(principal);

        GameVideoUploadEntity matchVideo = findValidMatchVideo(request.getMatchVideoId());
        MemberEntity player = findValidPlayer(request.getPlayerId());
        MemberEntity editor = findEditor(principal.getMemberId());

        validateClipRequest(
                request.getTitle(),
                request.getClipType(),
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                matchVideo
        );

        PlayerVideoClipEntity playerVideoClip = new PlayerVideoClipEntity();
        playerVideoClip.setGameVideoUpload(matchVideo);
        playerVideoClip.setEditor(editor);
        playerVideoClip.setPlayer(player);
        playerVideoClip.setClipType(request.getClipType());
        playerVideoClip.setTitle(request.getTitle());
        playerVideoClip.setComment(request.getComment());
        playerVideoClip.setStartTimeSec(request.getStartTimeSec());
        playerVideoClip.setEndTimeSec(request.getEndTimeSec());
        playerVideoClip.setUrl(null);
        playerVideoClip.setStatus(VideoUploadStatusEnum.READY);
        playerVideoClip.setIsDeleted(false);

        PlayerVideoClipEntity savedPlayerVideoClip = playerVideoClipRepository.save(playerVideoClip);

        return new CreatePlayerAnalysisClipResponseDTO(
                savedPlayerVideoClip.getId(),
                "선수 개인 분석 클립이 등록되었습니다."
        );
    }


    // 관리용 선수 개인 분석 클립 목록 조회
    @Transactional(readOnly = true)
    public PlayerAnalysisClipPageResponseDTO findPlayerAnalysisClipsForManagement(
            Integer matchVideoId,
            Integer playerId,
            PlayerClipTypeEnum clipType,
            Pageable pageable,
            CustomUserPrincipal principal
    ) {
        checkCanViewManagementList(principal);

        GameVideoUploadEntity matchVideo = null;
        if (matchVideoId != null) {
            matchVideo = findValidMatchVideo(matchVideoId);
        }

        MemberEntity player = null;
        if (playerId != null) {
            player = findValidPlayer(playerId);
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
    @Transactional(readOnly = true)
    public PlayerAnalysisClipPageResponseDTO findMyPlayerAnalysisClips(
            Integer matchVideoId,
            PlayerClipTypeEnum clipType,
            Pageable pageable,
            CustomUserPrincipal principal
    ) {
        checkCanViewMyPlayerClip(principal);

        MemberEntity player = findValidPlayer(principal.getMemberId());

        GameVideoUploadEntity matchVideo = null;
        if (matchVideoId != null) {
            matchVideo = findValidMatchVideo(matchVideoId);
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
    @Transactional(readOnly = true)
    public PlayerAnalysisClipDetailResponseDTO findPlayerAnalysisClipDetailForManagement(
            Integer playerClipId,
            CustomUserPrincipal principal
    ) {
        checkCanViewManagementList(principal);

        PlayerVideoClipEntity playerVideoClip = findValidPlayerAnalysisClip(playerClipId);

        checkMatchVideoIsNotDeleted(playerVideoClip.getGameVideoUpload());

        return toPlayerAnalysisClipDetailResponseDTO(playerVideoClip);
    }

    // 선수 본인 개인 분석 클립 상세 조회
    @Transactional(readOnly = true)
    public PlayerAnalysisClipDetailResponseDTO findMyPlayerAnalysisClipDetail(
            Integer playerClipId,
            CustomUserPrincipal principal
    ) {
        checkCanViewMyPlayerClip(principal);

        PlayerVideoClipEntity playerVideoClip = findValidPlayerAnalysisClip(playerClipId);

        if (!playerVideoClip.getPlayer().getId().equals(principal.getMemberId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        checkMatchVideoIsNotDeleted(playerVideoClip.getGameVideoUpload());

        return toPlayerAnalysisClipDetailResponseDTO(playerVideoClip);
    }


    // 선수 개인 분석 클립 수정
    public PlayerAnalysisClipDetailResponseDTO updatePlayerAnalysisClip(
            Integer playerClipId,
            UpdatePlayerAnalysisClipRequestDTO request,
            CustomUserPrincipal principal
    ) {
        checkCanCreateOrUpdate(principal);

        PlayerVideoClipEntity playerVideoClip = findValidPlayerAnalysisClip(playerClipId);

        GameVideoUploadEntity matchVideo = findValidMatchVideo(request.getMatchVideoId());
        MemberEntity player = findValidPlayer(request.getPlayerId());

        validateClipRequest(
                request.getTitle(),
                request.getClipType(),
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                matchVideo
        );

        playerVideoClip.setGameVideoUpload(matchVideo);
        playerVideoClip.setPlayer(player);
        playerVideoClip.setClipType(request.getClipType());
        playerVideoClip.setTitle(request.getTitle());
        playerVideoClip.setComment(request.getComment());
        playerVideoClip.setStartTimeSec(request.getStartTimeSec());
        playerVideoClip.setEndTimeSec(request.getEndTimeSec());

        return toPlayerAnalysisClipDetailResponseDTO(playerVideoClip);
    }


    // 선수 개인 분석 클립 삭제
    public void deletePlayerAnalysisClip(
            Integer playerClipId,
            CustomUserPrincipal principal
    ) {
        checkCanDelete(principal);

        PlayerVideoClipEntity playerVideoClip = findValidPlayerAnalysisClip(playerClipId);

        playerVideoClip.setIsDeleted(true);
    }


    /*
     * 관리용 목록 조회 조건 분기
     *
     * matchVideo, player, clipType 조합에 따라 Repository 조회 메서드를 선택한다.
     */
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

    // 선수 개인 분석 클립 목록 응답 DTO 변환
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
                playerVideoClip.getStartTimeSec(),
                playerVideoClip.getEndTimeSec(),
                playerVideoClip.getStatus(),
                playerVideoClip.getEditor().getId(),
                playerVideoClip.getEditor().getName(),
                playerVideoClip.getCreatedAt()
        );
    }


    // 선수 개인 분석 클립 등록/수정 권한 검증
    private void checkCanCreateOrUpdate(CustomUserPrincipal principal) {
        if (principal.getMemberRole() == MemberRoleEnum.COACH
                || principal.getMemberRole() == MemberRoleEnum.ANALYST) {
            return;
        }

        throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    // 삭제되지 않은 선수 개인 분석 클립 조회
    private PlayerVideoClipEntity findValidPlayerAnalysisClip(Integer playerClipId) {
        if (playerClipId == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return playerVideoClipRepository.findByIdAndIsDeletedFalse(playerClipId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND));
    }

    // 선수 개인 분석 클립 삭제 권한 검증
    private void checkCanDelete(CustomUserPrincipal principal) {
        if (principal.getMemberRole() == MemberRoleEnum.COACH) {
            return;
        }

        throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    // 선수 개인 분석 클립 관리용 조회 권한 검증
    private void checkCanViewManagementList(CustomUserPrincipal principal) {
        if (principal.getMemberRole() == MemberRoleEnum.COACH
                || principal.getMemberRole() == MemberRoleEnum.ANALYST) {
            return;
        }

        throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    // 선수 본인 개인 분석 클립 조회 권한 검증
    private void checkCanViewMyPlayerClip(CustomUserPrincipal principal) {
        if (principal.getMemberRole() == MemberRoleEnum.PLAYER) {
            return;
        }

        throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    // 원본 경기 영상 조회 및 삭제 여부 검증
    // 선수 개인 분석 클립은 반드시 존재하는 원본 경기 영상 기준으로 생성되어야 한다.
    private GameVideoUploadEntity findValidMatchVideo(Integer matchVideoId) {
        if (matchVideoId == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return gameVideoUploadRepository.findByIdAndIsDeletedFalse(matchVideoId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_VIDEO_NOT_FOUND));
    }

    // 연결된 원본 경기 영상 삭제 여부 검증
    private void checkMatchVideoIsNotDeleted(GameVideoUploadEntity matchVideo) {
        if (matchVideo == null || Boolean.TRUE.equals(matchVideo.getIsDeleted())) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_NOT_FOUND);
        }
    }

    // 대상 선수 조회 및 역할 검증
    private MemberEntity findValidPlayer(Integer playerId) {
        if (playerId == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        MemberEntity player = memberRepository.findByIdAndIsDeletedFalse(playerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (player.getMemberRole() != MemberRoleEnum.PLAYER) {
            throw new CustomException(ErrorCode.INVALID_PLAYER_ROLE);
        }

        return player;
    }

    /*
     * 로그인한 편집자 회원 조회
     * 개인 분석 클립의 editor_id에는 로그인한 지도자 또는 분석관 ID를 저장한다.
     */
    private MemberEntity findEditor(Integer editorId) {
        return memberRepository.findByIdAndIsDeletedFalse(editorId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /*
     * 클립 입력값 검증
     * 제목, 클립 유형, 시작 시간, 종료 시간, 원본 영상 길이를 검증한다.
     */
    private void validateClipRequest(
            String title,
            Object clipType,
            Integer startTimeSec,
            Integer endTimeSec,
            GameVideoUploadEntity matchVideo
    ) {
        if (!StringUtils.hasText(title)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (clipType == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (startTimeSec == null || endTimeSec == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
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

    // 선수 개인 분석 클립 상세 응답 DTO 변환
    private PlayerAnalysisClipDetailResponseDTO toPlayerAnalysisClipDetailResponseDTO(
            PlayerVideoClipEntity playerVideoClip
    ) {
        return new PlayerAnalysisClipDetailResponseDTO(
                playerVideoClip.getId(),
                playerVideoClip.getGameVideoUpload().getId(),
                playerVideoClip.getGameVideoUpload().getTitle(),
                playerVideoClip.getGameVideoUpload().getUrl(),
                playerVideoClip.getPlayer().getId(),
                playerVideoClip.getPlayer().getName(),
                playerVideoClip.getClipType(),
                playerVideoClip.getTitle(),
                playerVideoClip.getComment(),
                playerVideoClip.getStartTimeSec(),
                playerVideoClip.getEndTimeSec(),
                playerVideoClip.getStatus(),
                playerVideoClip.getEditor().getId(),
                playerVideoClip.getEditor().getName(),
                playerVideoClip.getCreatedAt(),
                playerVideoClip.getUpdatedAt()
        );
    }
}