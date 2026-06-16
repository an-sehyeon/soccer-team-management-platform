package com.soccer.platform.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.matchvideo.CreateMatchVideoRequestDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoDetailResponseDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoListResponseDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoPageResponseDTO;
import com.soccer.platform.dto.matchvideo.UpdateMatchVideoRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.repository.GameVideoUploadRepository;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

/*
 * 경기 영상 업로드 Service
 * 경기 원본 영상 URL과 경기 기본 정보를 관리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MatchVideoService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_TINYINT_UNSIGNED_VALUE = 255;

    private final GameVideoUploadRepository gameVideoUploadRepository;
    private final MemberRepository memberRepository;

    // 경기 영상 등록
    public Integer createMatchVideo(
            CustomUserPrincipal principal,
            CreateMatchVideoRequestDTO request
    ) {
        validateCanCreateOrUpdate(principal);
        validateCreateRequest(request);
        validateDurationSec(request.getDurationSec());

        MemberEntity uploader = findLoginMember(principal.getMemberId());

        GameVideoUploadEntity matchVideo = new GameVideoUploadEntity();
        matchVideo.setMember(uploader);
        matchVideo.setUrl(request.getUrl().trim());
        matchVideo.setDurationSec(request.getDurationSec());
        matchVideo.setTitle(request.getTitle().trim());
        matchVideo.setGameDate(request.getGameDate());
        matchVideo.setPlace(request.getPlace().trim());
        matchVideo.setHomeScore(request.getHomeScore());
        matchVideo.setAwayScore(request.getAwayScore());
        matchVideo.setMatchResult(request.getMatchResult());
        matchVideo.setStatus(VideoUploadStatusEnum.READY);
        matchVideo.setIsDeleted(false);

        GameVideoUploadEntity savedMatchVideo = gameVideoUploadRepository.save(matchVideo);

        return savedMatchVideo.getId();
    }

    // 경기 영상 목록 조회
    @Transactional(readOnly = true)
    public MatchVideoPageResponseDTO findMatchVideos(
            CustomUserPrincipal principal,
            int page,
            int size
    ) {
        validateCanRead(principal);
        validatePageRequest(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "gameDate")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        Page<MatchVideoListResponseDTO> matchVideoPage = gameVideoUploadRepository
                .findByIsDeletedFalse(pageable)
                .map(MatchVideoListResponseDTO::from);

        return MatchVideoPageResponseDTO.from(matchVideoPage);
    }

    // 경기 영상 상세 조회
    @Transactional(readOnly = true)
    public MatchVideoDetailResponseDTO findMatchVideoDetail(
            CustomUserPrincipal principal,
            Integer matchVideoId
    ) {
        validateCanRead(principal);

        GameVideoUploadEntity matchVideo = findActiveMatchVideo(matchVideoId);

        return MatchVideoDetailResponseDTO.from(matchVideo);
    }

    // 경기 영상 수정
    public MatchVideoDetailResponseDTO updateMatchVideo(
            CustomUserPrincipal principal,
            Integer matchVideoId,
            UpdateMatchVideoRequestDTO request
    ) {
        validateCanCreateOrUpdate(principal);
        validateUpdateRequest(request);
        validateDurationSec(request.getDurationSec());

        GameVideoUploadEntity matchVideo = findActiveMatchVideo(matchVideoId);

        matchVideo.setUrl(request.getUrl().trim());
        matchVideo.setDurationSec(request.getDurationSec());
        matchVideo.setTitle(request.getTitle().trim());
        matchVideo.setGameDate(request.getGameDate());
        matchVideo.setPlace(request.getPlace().trim());
        matchVideo.setHomeScore(request.getHomeScore());
        matchVideo.setAwayScore(request.getAwayScore());
        matchVideo.setMatchResult(request.getMatchResult());

        return MatchVideoDetailResponseDTO.from(matchVideo);
    }

    // 경기 영상 삭제
    public void deleteMatchVideo(
            CustomUserPrincipal principal,
            Integer matchVideoId
    ) {
        validateCanDelete(principal);

        GameVideoUploadEntity matchVideo = findActiveMatchVideo(matchVideoId);

        matchVideo.setIsDeleted(true);
    }

    // 경기 영상 등록/수정 권한 검증
    private void validateCanCreateOrUpdate(CustomUserPrincipal principal) {
        String memberRole = String.valueOf(principal.getMemberRole());

        if (!"COACH".equals(memberRole) && !"ANALYST".equals(memberRole)) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_ACCESS_DENIED);
        }
    }

    // 경기 영상 조회 권한 검증
    private void validateCanRead(CustomUserPrincipal principal) {
        String memberRole = String.valueOf(principal.getMemberRole());

        if (!"COACH".equals(memberRole)
                && !"ANALYST".equals(memberRole)
                && !"PLAYER".equals(memberRole)) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_ACCESS_DENIED);
        }
    }

    // 경기 영상 삭제 권한 검증
    private void validateCanDelete(CustomUserPrincipal principal) {
        String memberRole = String.valueOf(principal.getMemberRole());

        if (!"COACH".equals(memberRole)) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_ACCESS_DENIED);
        }
    }

    // 로그인 회원 조회
    private MemberEntity findLoginMember(Integer memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 삭제되지 않은 경기 영상 조회
    private GameVideoUploadEntity findActiveMatchVideo(Integer matchVideoId) {
        if (matchVideoId == null) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        return gameVideoUploadRepository.findByIdAndIsDeletedFalse(matchVideoId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_VIDEO_NOT_FOUND));
    }

    // 경기 영상 등록 요청 검증
    private void validateCreateRequest(CreateMatchVideoRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        validateMatchVideoRequiredValues(
                request.getUrl(),
                request.getTitle(),
                request.getGameDate(),
                request.getPlace(),
                request.getHomeScore(),
                request.getAwayScore(),
                request.getMatchResult()
        );
    }

    // 경기 영상 수정 요청 검증
    private void validateUpdateRequest(UpdateMatchVideoRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        validateMatchVideoRequiredValues(
                request.getUrl(),
                request.getTitle(),
                request.getGameDate(),
                request.getPlace(),
                request.getHomeScore(),
                request.getAwayScore(),
                request.getMatchResult()
        );
    }

    // 경기 영상 공통 필수값 검증
    private void validateMatchVideoRequiredValues(
            String url,
            String title,
            Object gameDate,
            String place,
            Integer homeScore,
            Integer awayScore,
            Object matchResult
    ) {
        validateRequiredText(url);
        validateRequiredText(title);
        validateRequiredText(place);

        if (gameDate == null) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        if (matchResult == null) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        validateScore(homeScore);
        validateScore(awayScore);

        validateMaxLength(url, 255);
        validateMaxLength(title, 255);
        validateMaxLength(place, 255);
    }

    // 필수 문자열 검증
    private void validateRequiredText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }
    }

    // 문자열 길이 검증
    private void validateMaxLength(String value, int maxLength) {
        if (value.trim().length() > maxLength) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }
    }

    // 경기 점수 검증
    private void validateScore(Integer score) {
        if (score == null || score < 0 || score > MAX_TINYINT_UNSIGNED_VALUE) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }
    }

    // 페이지 요청값 검증
    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_MATCH_VIDEO_REQUEST);
        }
    }

    // 원본 경기 영상 길이 검증
    private void validateDurationSec(Integer durationSec) {
        if (durationSec == null || durationSec <= 0) {
            throw new CustomException(ErrorCode.INVALID_VIDEO_DURATION);
        }
    }
}