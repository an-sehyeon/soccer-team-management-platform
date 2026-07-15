package com.soccer.platform.service.videobookmark;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.videobookmark.CreateVideoBookmarkRequestDTO;
import com.soccer.platform.dto.videobookmark.UpdateVideoBookmarkRequestDTO;
import com.soccer.platform.dto.videobookmark.VideoBookmarkResponseDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.entity.VideoBookmarkEntity;
import com.soccer.platform.repository.VideoBookmarkRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

/*
 * 영상 북마크 Service
 * 지도자와 분석관이 경기 원본 영상, 팀 분석 클립,
 * 선수 개인 분석 클립에서 개인 북마크를 관리.
 * 북마크는 작성자 본인만 조회, 수정, 삭제할 수 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoBookmarkService {

    private final VideoBookmarkRepository videoBookmarkRepository;
    private final VideoBookmarkValidator videoBookmarkValidator;

    /*
     * 현재 재생 중인 영상에 북마크를 등록
     * teamClipId와 playerClipId의 존재 여부에 따라
     * 경기 원본, 팀 분석 클립, 선수 개인 분석 클립을 구분한다.
     */
    @Transactional
    public VideoBookmarkResponseDTO createVideoBookmark(
        CreateVideoBookmarkRequestDTO request,
        CustomUserPrincipal principal
    ) {
        videoBookmarkValidator.validateCanManage(principal);
        videoBookmarkValidator.validateCreateRequest(request);

        MemberEntity member =
            videoBookmarkValidator.findLoginMember(principal);

        String title =
            videoBookmarkValidator.trimRequiredTitle(
                request.getTitle()
            );

        String memo =
            videoBookmarkValidator.trimNullableMemo(
                request.getMemo()
            );

        VideoBookmarkEntity videoBookmark;

        if (request.getTeamClipId() != null) {
            videoBookmark = createTeamClipBookmark(
                request,
                member,
                title,
                memo
            );
        } else if (request.getPlayerClipId() != null) {
            videoBookmark = createPlayerClipBookmark(
                request,
                member,
                title,
                memo
            );
        } else {
            videoBookmark = createMatchVideoBookmark(
                request,
                member,
                title,
                memo
            );
        }

        VideoBookmarkEntity savedVideoBookmark =
            videoBookmarkRepository.save(videoBookmark);

        return VideoBookmarkResponseDTO.from(savedVideoBookmark);
    }

    /*
     * 현재 재생 중인 영상에서 작성한 본인의 북마크를 조회
     * 목록은 Repository에서 최신 등록순으로 정렬한다.
     */
    public List<VideoBookmarkResponseDTO> findMyVideoBookmarks(
        Integer matchVideoId,
        Integer teamClipId,
        Integer playerClipId,
        CustomUserPrincipal principal
    ) {
        videoBookmarkValidator.validateCanManage(principal);
        videoBookmarkValidator.validateListRequest(
            matchVideoId,
            teamClipId,
            playerClipId
        );

        if (teamClipId != null) {
            validateTeamClipSource(matchVideoId, teamClipId);

            return videoBookmarkRepository
                .findMyTeamClipBookmarks(
                    matchVideoId,
                    teamClipId,
                    principal.getMemberId()
                )
                .stream()
                .map(VideoBookmarkResponseDTO::from)
                .toList();
        }

        if (playerClipId != null) {
            validatePlayerClipSource(matchVideoId, playerClipId);

            return videoBookmarkRepository
                .findMyPlayerClipBookmarks(
                    matchVideoId,
                    playerClipId,
                    principal.getMemberId()
                )
                .stream()
                .map(VideoBookmarkResponseDTO::from)
                .toList();
        }

        videoBookmarkValidator.findReadyMatchVideo(matchVideoId);

        return videoBookmarkRepository
            .findMyMatchVideoBookmarks(
                matchVideoId,
                principal.getMemberId()
            )
            .stream()
            .map(VideoBookmarkResponseDTO::from)
            .toList();
    }

    /*
     * 작성자 본인의 북마크 제목, 메모, 시간을 수정
     * 북마크 대상 영상은 변경하지 않는다.
     */
    @Transactional
    public VideoBookmarkResponseDTO updateVideoBookmark(
        Integer bookmarkId,
        UpdateVideoBookmarkRequestDTO request,
        CustomUserPrincipal principal
    ) {
        videoBookmarkValidator.validateCanManage(principal);
        videoBookmarkValidator.validateUpdateRequest(request);

        VideoBookmarkEntity videoBookmark =
            videoBookmarkValidator.findOwnedActiveBookmark(
                bookmarkId,
                principal
            );

        validateBookmarkTargetAndTime(
            videoBookmark,
            request.getBookmarkTimeSec()
        );

        String title =
            videoBookmarkValidator.trimRequiredTitle(
                request.getTitle()
            );

        String memo =
            videoBookmarkValidator.trimNullableMemo(
                request.getMemo()
            );

        videoBookmark.update(
            request.getBookmarkTimeSec(),
            title,
            memo
        );

        return VideoBookmarkResponseDTO.from(videoBookmark);
    }

    /*
     * 작성자 본인의 북마크를 소프트 삭제
     * 북마크를 이용해 분석 클립이나 기록 이벤트를 등록해도
     * 자동 삭제하지 않고 사용자가 직접 삭제
     */
    @Transactional
    public void deleteVideoBookmark(
        Integer bookmarkId,
        CustomUserPrincipal principal
    ) {
        videoBookmarkValidator.validateCanManage(principal);

        VideoBookmarkEntity videoBookmark =
            videoBookmarkValidator.findOwnedActiveBookmark(
                bookmarkId,
                principal
            );

        videoBookmark.softDelete();
    }
    
    /*
     * 팀 분석 클립에 연결된 활성 북마크를 모두 소프트 삭제.
     * 팀 분석 클립의 시작·종료 시간이 변경되거나
     * 클립이 삭제될 때 같은 트랜잭션 안에서 호출
     */
    @Transactional
    public int softDeleteBookmarksByTeamClipId(
        Integer teamClipId
    ) {
        if (teamClipId == null) {
            throw new CustomException(
                ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND
            );
        }

        return videoBookmarkRepository
            .softDeleteActiveBookmarksByTeamClipId(teamClipId);
    }

    /*
     * 선수 개인 분석 클립에 연결된 활성 북마크를 모두 소프트 삭제.
     * 선수 개인 분석 클립의 시작·종료 시간이 변경되거나
     * 클립이 삭제될 때 같은 트랜잭션 안에서 호출
     */
    @Transactional
    public int softDeleteBookmarksByPlayerClipId(
        Integer playerClipId
    ) {
        if (playerClipId == null) {
            throw new CustomException(
                ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND
            );
        }

        return videoBookmarkRepository
            .softDeleteActiveBookmarksByPlayerClipId(playerClipId);
    }

    // 경기 원본 영상 북마크 Entity를 생성
    private VideoBookmarkEntity createMatchVideoBookmark(
        CreateVideoBookmarkRequestDTO request,
        MemberEntity member,
        String title,
        String memo
    ) {
        GameVideoUploadEntity matchVideo =
            videoBookmarkValidator
                .findReadyMatchVideoWithDuration(
                    request.getMatchVideoId()
                );

        videoBookmarkValidator.validateMatchVideoBookmarkTime(
            request.getBookmarkTimeSec(),
            matchVideo
        );

        return VideoBookmarkEntity.createMatchVideoBookmark(
            matchVideo,
            member,
            request.getBookmarkTimeSec(),
            title,
            memo
        );
    }

    // 팀 분석 클립 북마크 Entity를 생성
    private VideoBookmarkEntity createTeamClipBookmark(
        CreateVideoBookmarkRequestDTO request,
        MemberEntity member,
        String title,
        String memo
    ) {
        GameVideoUploadEntity matchVideo =
            videoBookmarkValidator.findReadyMatchVideo(
                request.getMatchVideoId()
            );

        TeamVideoClipEntity teamVideoClip =
            videoBookmarkValidator.findReadyTeamClip(
                request.getTeamClipId()
            );

        videoBookmarkValidator.validateTeamClipMatchVideo(
            teamVideoClip,
            matchVideo
        );

        videoBookmarkValidator.validateTeamClipBookmarkTime(
            request.getBookmarkTimeSec(),
            teamVideoClip
        );

        return VideoBookmarkEntity.createTeamClipBookmark(
            matchVideo,
            teamVideoClip,
            member,
            request.getBookmarkTimeSec(),
            title,
            memo
        );
    }

    // 선수 개인 분석 클립 북마크 Entity를 생성
    private VideoBookmarkEntity createPlayerClipBookmark(
        CreateVideoBookmarkRequestDTO request,
        MemberEntity member,
        String title,
        String memo
    ) {
        GameVideoUploadEntity matchVideo =
            videoBookmarkValidator.findReadyMatchVideo(
                request.getMatchVideoId()
            );

        PlayerVideoClipEntity playerVideoClip =
            videoBookmarkValidator.findReadyPlayerClip(
                request.getPlayerClipId()
            );

        videoBookmarkValidator.validatePlayerClipMatchVideo(
            playerVideoClip,
            matchVideo
        );

        videoBookmarkValidator.validatePlayerClipBookmarkTime(
            request.getBookmarkTimeSec(),
            playerVideoClip
        );

        return VideoBookmarkEntity.createPlayerClipBookmark(
            matchVideo,
            playerVideoClip,
            member,
            request.getBookmarkTimeSec(),
            title,
            memo
        );
    }

    // 팀 분석 클립 북마크 목록 조회 대상을 검증
    private void validateTeamClipSource(
        Integer matchVideoId,
        Integer teamClipId
    ) {
        GameVideoUploadEntity matchVideo =
            videoBookmarkValidator.findReadyMatchVideo(matchVideoId);

        TeamVideoClipEntity teamVideoClip =
            videoBookmarkValidator.findReadyTeamClip(teamClipId);

        videoBookmarkValidator.validateTeamClipMatchVideo(
            teamVideoClip,
            matchVideo
        );
    }

    // 선수 개인 분석 클립 북마크 목록 조회 대상을 검증
    private void validatePlayerClipSource(
        Integer matchVideoId,
        Integer playerClipId
    ) {
        GameVideoUploadEntity matchVideo =
            videoBookmarkValidator.findReadyMatchVideo(matchVideoId);

        PlayerVideoClipEntity playerVideoClip =
            videoBookmarkValidator.findReadyPlayerClip(playerClipId);

        videoBookmarkValidator.validatePlayerClipMatchVideo(
            playerVideoClip,
            matchVideo
        );
    }

    /*
     * 기존 북마크 대상 영상을 다시 조회하고 수정 시간을 검증
     * 삭제되거나 READY 상태가 아닌 영상의 북마크는 수정할 수 없다.
     */
    private void validateBookmarkTargetAndTime(
        VideoBookmarkEntity videoBookmark,
        Integer bookmarkTimeSec
    ) {
        if (videoBookmark.getTeamVideoClip() != null) {
            validateTeamBookmarkTargetAndTime(
                videoBookmark,
                bookmarkTimeSec
            );
            return;
        }

        if (videoBookmark.getPlayerVideoClip() != null) {
            validatePlayerBookmarkTargetAndTime(
                videoBookmark,
                bookmarkTimeSec
            );
            return;
        }

        if (
            videoBookmark.getGameVideoUpload() == null
                || videoBookmark.getGameVideoUpload().getId() == null
        ) {
            throw new CustomException(
                ErrorCode.INVALID_VIDEO_BOOKMARK_SOURCE
            );
        }

        GameVideoUploadEntity matchVideo =
            videoBookmarkValidator
                .findReadyMatchVideoWithDuration(
                    videoBookmark
                        .getGameVideoUpload()
                        .getId()
                );

        videoBookmarkValidator.validateMatchVideoBookmarkTime(
            bookmarkTimeSec,
            matchVideo
        );
    }

    // 팀 분석 클립 북마크 수정 시간을 검증
    private void validateTeamBookmarkTargetAndTime(
        VideoBookmarkEntity videoBookmark,
        Integer bookmarkTimeSec
    ) {
        Integer matchVideoId =
            videoBookmark.getGameVideoUpload().getId();

        Integer teamClipId =
            videoBookmark.getTeamVideoClip().getId();

        GameVideoUploadEntity matchVideo =
            videoBookmarkValidator.findReadyMatchVideo(matchVideoId);

        TeamVideoClipEntity teamVideoClip =
            videoBookmarkValidator.findReadyTeamClip(teamClipId);

        videoBookmarkValidator.validateTeamClipMatchVideo(
            teamVideoClip,
            matchVideo
        );

        videoBookmarkValidator.validateTeamClipBookmarkTime(
            bookmarkTimeSec,
            teamVideoClip
        );
    }

    // 선수 개인 분석 클립 북마크 수정 시간을 검증
    private void validatePlayerBookmarkTargetAndTime(
        VideoBookmarkEntity videoBookmark,
        Integer bookmarkTimeSec
    ) {
        Integer matchVideoId =
            videoBookmark.getGameVideoUpload().getId();

        Integer playerClipId =
            videoBookmark.getPlayerVideoClip().getId();

        GameVideoUploadEntity matchVideo =
            videoBookmarkValidator.findReadyMatchVideo(matchVideoId);

        PlayerVideoClipEntity playerVideoClip =
            videoBookmarkValidator.findReadyPlayerClip(playerClipId);

        videoBookmarkValidator.validatePlayerClipMatchVideo(
            playerVideoClip,
            matchVideo
        );

        videoBookmarkValidator.validatePlayerClipBookmarkTime(
            bookmarkTimeSec,
            playerVideoClip
        );
    }
}