package com.soccer.platform.service.videobookmark;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.videobookmark.CreateVideoBookmarkRequestDTO;
import com.soccer.platform.dto.videobookmark.UpdateVideoBookmarkRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.entity.VideoBookmarkEntity;
import com.soccer.platform.repository.PlayerVideoClipRepository;
import com.soccer.platform.repository.TeamVideoClipRepository;
import com.soccer.platform.repository.VideoBookmarkRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.MatchVideoQueryService;
import com.soccer.platform.service.common.MemberQueryService;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;

/*
 * 영상 북마크 Validator
 * 영상 북마크 등록, 조회, 수정, 삭제에 필요한 다음 검증을 담당
 *
 * - COACH, ANALYST 관리 권한 검증
 * - 북마크 작성자 본인 여부 검증
 * - 북마크 요청값 검증
 * - 경기 영상과 분석 클립 연결 관계 검증
 * - 북마크 대상 영상 READY 상태 검증
 * - 북마크 시간이 대상 영상 길이 안에 있는지 검증
 */
@Component
@RequiredArgsConstructor
public class VideoBookmarkValidator {

    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_MEMO_LENGTH = 255;

    private final VideoBookmarkRepository videoBookmarkRepository;
    private final TeamVideoClipRepository teamVideoClipRepository;
    private final PlayerVideoClipRepository playerVideoClipRepository;

    private final PermissionValidator permissionValidator;
    private final MemberQueryService memberQueryService;
    private final MatchVideoQueryService matchVideoQueryService;

    /*
     * 영상 북마크 관리 권한을 검증
     * 북마크는 개인 분석 작업 데이터이므로 COACH와 ANALYST 모두
     * 본인 북마크를 등록, 조회, 수정, 삭제할 수 있다.
     */
    public void validateCanManage(CustomUserPrincipal principal) {
        permissionValidator.requireCoachOrAnalyst(
            principal,
            ErrorCode.VIDEO_BOOKMARK_MANAGE_ACCESS_DENIED
        );
    }

    // 현재 로그인한 삭제되지 않은 회원을 조회
    public MemberEntity findLoginMember(CustomUserPrincipal principal) {
        return memberQueryService.findLoginMember(
            principal,
            ErrorCode.MEMBER_NOT_FOUND
        );
    }

    // 북마크 등록 요청값을 검증
    public void validateCreateRequest(
        CreateVideoBookmarkRequestDTO request
    ) {
        if (request == null) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_REQUEST
            );
        }

        if (request.getMatchVideoId() == null) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_REQUEST
            );
        }

        validateSourceCombination(
            request.getTeamClipId(),
            request.getPlayerClipId()
        );

        validateBookmarkValues(
            request.getBookmarkTimeSec(),
            request.getTitle(),
            request.getMemo()
        );
    }
    
    /*
     * 북마크 목록 조회 대상 영상 조합을 검증
     * teamClipId와 playerClipId가 모두 null이면 경기 원본 영상,
     * 둘 중 하나만 존재하면 해당 분석 클립 북마크로 처리
     */
    public void validateListRequest(
        Integer matchVideoId,
        Integer teamClipId,
        Integer playerClipId
    ) {
        if (matchVideoId == null) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_REQUEST
            );
        }

        validateSourceCombination(teamClipId, playerClipId);
    }

    /*
     * 북마크 수정 요청값을 검증
     * PATCH 요청이지만 수정 화면의 제목, 메모, 시간을
     * 모두 전달하는 방식으로 처리
     */
    public void validateUpdateRequest(
        UpdateVideoBookmarkRequestDTO request
    ) {
        if (request == null) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_REQUEST
            );
        }

        validateBookmarkValues(
            request.getBookmarkTimeSec(),
            request.getTitle(),
            request.getMemo()
        );
    }

    /*
     * 삭제되지 않은 경기 영상을 조회하고 READY 상태를 검증
     * 팀 분석 클립이나 선수 개인 분석 클립 북마크에서도
     * 요청 matchVideoId와 클립 원본 경기의 일치 여부를 확인하기 위해 사용
     */
    public GameVideoUploadEntity findReadyMatchVideo(
        Integer matchVideoId
    ) {
        GameVideoUploadEntity matchVideo =
            matchVideoQueryService.findActiveMatchVideoById(
                matchVideoId,
                ErrorCode.MATCH_VIDEO_NOT_FOUND
            );

        validateTargetReady(matchVideo.getStatus());

        return matchVideo;
    }

    /*
     * 경기 원본 영상 북마크에 사용할 영상을 조회
     * 원본 경기 북마크 시간 검증에는 durationSec가 필요하므로
     * 영상 길이 준비 여부까지 확인
     */
    public GameVideoUploadEntity findReadyMatchVideoWithDuration(
        Integer matchVideoId
    ) {
        GameVideoUploadEntity matchVideo =
            matchVideoQueryService.findActiveMatchVideoWithDuration(
                matchVideoId,
                ErrorCode.MATCH_VIDEO_NOT_FOUND,
                ErrorCode.MATCH_VIDEO_DURATION_NOT_READY
            );

        validateTargetReady(matchVideo.getStatus());

        return matchVideo;
    }

    // 삭제되지 않은 READY 상태 팀 분석 클립을 조회
    public TeamVideoClipEntity findReadyTeamClip(Integer teamClipId) {
        if (teamClipId == null) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_SOURCE
            );
        }

        TeamVideoClipEntity teamVideoClip =
            teamVideoClipRepository.findByIdAndIsDeletedFalse(teamClipId)
                .orElseThrow(
                    () -> new CustomException(
                        ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND
                    )
                );

        validateTargetReady(teamVideoClip.getStatus());

        return teamVideoClip;
    }

    // 삭제되지 않은 READY 상태 선수 개인 분석 클립을 조회
    public PlayerVideoClipEntity findReadyPlayerClip(
        Integer playerClipId
    ) {
        if (playerClipId == null) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_SOURCE
            );
        }

        PlayerVideoClipEntity playerVideoClip =
            playerVideoClipRepository
                .findByIdAndIsDeletedFalse(playerClipId)
                .orElseThrow(
                    () -> new CustomException(
                        ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND
                    )
                );

        validateTargetReady(playerVideoClip.getStatus());

        return playerVideoClip;
    }

    /*
     * 삭제되지 않은 북마크를 조회하고 작성자 본인 여부를 검증
     * 다른 사용자의 북마크는 COACH라도 수정하거나 삭제할 수 없다.
     */
    public VideoBookmarkEntity findOwnedActiveBookmark(
        Integer bookmarkId,
        CustomUserPrincipal principal
    ) {
        if (bookmarkId == null) {
            throw new CustomException(
                ErrorCode.VIDEO_BOOKMARK_NOT_FOUND
            );
        }

        VideoBookmarkEntity videoBookmark =
            videoBookmarkRepository.findById(bookmarkId)
                .orElseThrow(
                    () -> new CustomException(
                        ErrorCode.VIDEO_BOOKMARK_NOT_FOUND
                    )
                );

        if (Boolean.TRUE.equals(videoBookmark.getIsDeleted())) {
            throw new CustomException(
                ErrorCode.VIDEO_BOOKMARK_NOT_FOUND
            );
        }

        validateBookmarkOwner(videoBookmark, principal);

        return videoBookmark;
    }

    // 팀 분석 클립이 요청 경기 영상에서 생성된 클립인지 검증
    public void validateTeamClipMatchVideo(
        TeamVideoClipEntity teamVideoClip,
        GameVideoUploadEntity matchVideo
    ) {
        if (
            teamVideoClip == null
                || teamVideoClip.getGameVideoUpload() == null
                || teamVideoClip.getGameVideoUpload().getId() == null
                || matchVideo == null
                || matchVideo.getId() == null
        ) {
            throw new CustomException(
                ErrorCode.VIDEO_BOOKMARK_MATCH_VIDEO_MISMATCH
            );
        }

        Integer clipMatchVideoId =
            teamVideoClip.getGameVideoUpload().getId();

        if (!clipMatchVideoId.equals(matchVideo.getId())) {
            throw new CustomException(
                ErrorCode.VIDEO_BOOKMARK_MATCH_VIDEO_MISMATCH
            );
        }
    }

    // 선수 개인 분석 클립이 요청 경기 영상에서 생성된 클립인지 검증
    public void validatePlayerClipMatchVideo(
        PlayerVideoClipEntity playerVideoClip,
        GameVideoUploadEntity matchVideo
    ) {
        if (
            playerVideoClip == null
                || playerVideoClip.getGameVideoUpload() == null
                || playerVideoClip.getGameVideoUpload().getId() == null
                || matchVideo == null
                || matchVideo.getId() == null
        ) {
            throw new CustomException(
                ErrorCode.VIDEO_BOOKMARK_MATCH_VIDEO_MISMATCH
            );
        }

        Integer clipMatchVideoId =
            playerVideoClip.getGameVideoUpload().getId();

        if (!clipMatchVideoId.equals(matchVideo.getId())) {
            throw new CustomException(
                ErrorCode.VIDEO_BOOKMARK_MATCH_VIDEO_MISMATCH
            );
        }
    }

    // 경기 원본 영상 기준 북마크 시간을 검증
    public void validateMatchVideoBookmarkTime(
        Integer bookmarkTimeSec,
        GameVideoUploadEntity matchVideo
    ) {
        if (matchVideo == null) {
            throw new CustomException(
                ErrorCode.MATCH_VIDEO_NOT_FOUND
            );
        }

        Integer durationSec = matchVideo.getDurationSec();

        if (durationSec == null || durationSec <= 0) {
            throw new CustomException(
                ErrorCode.MATCH_VIDEO_DURATION_NOT_READY
            );
        }

        validateBookmarkTimeWithinDuration(
            bookmarkTimeSec,
            durationSec
        );
    }

    // 생성된 팀 분석 클립 영상 내부 기준 북마크 시간을 검증
    public void validateTeamClipBookmarkTime(
        Integer bookmarkTimeSec,
        TeamVideoClipEntity teamVideoClip
    ) {
        if (teamVideoClip == null) {
            throw new CustomException(
                ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND
            );
        }

        Integer clipDurationSec = calculateClipDuration(
            teamVideoClip.getStartTimeSec(),
            teamVideoClip.getEndTimeSec()
        );

        validateBookmarkTimeWithinDuration(
            bookmarkTimeSec,
            clipDurationSec
        );
    }

    // 생성된 선수 개인 분석 클립 영상 내부 기준 북마크 시간을 검증
    public void validatePlayerClipBookmarkTime(
        Integer bookmarkTimeSec,
        PlayerVideoClipEntity playerVideoClip
    ) {
        if (playerVideoClip == null) {
            throw new CustomException(
                ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND
            );
        }

        Integer clipDurationSec = calculateClipDuration(
            playerVideoClip.getStartTimeSec(),
            playerVideoClip.getEndTimeSec()
        );

        validateBookmarkTimeWithinDuration(
            bookmarkTimeSec,
            clipDurationSec
        );
    }

    // 북마크 제목을 저장 가능한 형태로 정리
    public String trimRequiredTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_REQUEST
            );
        }

        String trimmedTitle = title.trim();

        if (trimmedTitle.length() > MAX_TITLE_LENGTH) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_REQUEST
            );
        }

        return trimmedTitle;
    }

    /*
     * 선택 입력 문자열을 저장 가능한 형태로 정리
     * null 또는 공백만 입력한 경우 null을 반환한다.
     */
    public String trimNullableMemo(String memo) {
        if (!StringUtils.hasText(memo)) {
            return null;
        }

        String trimmedMemo = memo.trim();

        if (trimmedMemo.length() > MAX_MEMO_LENGTH) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_REQUEST
            );
        }

        return trimmedMemo;
    }

    /*
     * teamClipId와 playerClipId를 동시에 전달하지 않았는지 검증
     * 두 값이 모두 null인 경우 경기 원본 영상 북마크로 처리한다.
     */
    private void validateSourceCombination(
        Integer teamClipId,
        Integer playerClipId
    ) {
        if (teamClipId != null && playerClipId != null) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_SOURCE
            );
        }
    }

    // 북마크 공통 입력값을 검증
    private void validateBookmarkValues(
        Integer bookmarkTimeSec,
        String title,
        String memo
    ) {
        if (bookmarkTimeSec == null || bookmarkTimeSec < 0) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_TIME
            );
        }

        trimRequiredTitle(title);
        trimNullableMemo(memo);
    }

    // 북마크 작성자와 로그인 사용자가 일치하는지 검증
    private void validateBookmarkOwner(
        VideoBookmarkEntity videoBookmark,
        CustomUserPrincipal principal
    ) {
        if (
            videoBookmark == null
                || videoBookmark.getMember() == null
                || videoBookmark.getMember().getId() == null
                || principal == null
                || principal.getMemberId() == null
        ) {
            throw new CustomException(
                ErrorCode.VIDEO_BOOKMARK_ACCESS_DENIED
            );
        }

        if (
            !videoBookmark.getMember()
                .getId()
                .equals(principal.getMemberId())
        ) {
            throw new CustomException(
                ErrorCode.VIDEO_BOOKMARK_ACCESS_DENIED
            );
        }
    }

    // 대상 영상이 실제 재생 가능한 READY 상태인지 검증
    private void validateTargetReady(
        VideoUploadStatusEnum status
    ) {
        if (status != VideoUploadStatusEnum.READY) {
            throw new CustomException(
                ErrorCode.VIDEO_BOOKMARK_TARGET_NOT_READY
            );
        }
    }

    // 북마크 시간이 영상 길이 범위 안인지 검증
    private void validateBookmarkTimeWithinDuration(
        Integer bookmarkTimeSec,
        Integer durationSec
    ) {
        if (
            bookmarkTimeSec == null
                || durationSec == null
                || durationSec <= 0
                || bookmarkTimeSec < 0
                || bookmarkTimeSec > durationSec
        ) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_TIME
            );
        }
    }

    // 원본 영상 시작·종료 시간을 이용해 생성 클립 길이를 계산
    private Integer calculateClipDuration(
        Integer startTimeSec,
        Integer endTimeSec
    ) {
        if (
            startTimeSec == null
                || endTimeSec == null
                || startTimeSec < 0
                || endTimeSec <= startTimeSec
        ) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_TIME
            );
        }

        return endTimeSec - startTimeSec;
    }
}