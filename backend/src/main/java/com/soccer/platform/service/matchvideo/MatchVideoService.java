package com.soccer.platform.service.matchvideo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.dto.matchvideo.CreateMatchVideoRequestDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoDetailResponseDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoListResponseDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoPageResponseDTO;
import com.soccer.platform.dto.matchvideo.UpdateMatchVideoRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.repository.GameVideoUploadRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

/*
 * 경기 영상 업로드 Service
 * 경기 원본 영상 URL과 경기 기본 정보를 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchVideoService {

    private final GameVideoUploadRepository gameVideoUploadRepository;
    private final MatchVideoValidator matchVideoValidator;
    
    // 경기 영상 등록
    @Transactional
    public Integer createMatchVideo(
            CustomUserPrincipal principal,
            CreateMatchVideoRequestDTO request
    ) {
        matchVideoValidator.validateCanCreateOrUpdate(principal);
        matchVideoValidator.validateCreateRequest(request);
        matchVideoValidator.validateDurationSec(request.getDurationSec());

        MemberEntity uploader = matchVideoValidator.findLoginMember(principal.getMemberId());

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
    public MatchVideoPageResponseDTO findMatchVideos(
            CustomUserPrincipal principal,
            int page,
            int size
    ) {
        matchVideoValidator.validateCanRead(principal);

        Pageable pageable = matchVideoValidator.createMatchVideoPageable(page, size);

        Page<MatchVideoListResponseDTO> matchVideoPage = gameVideoUploadRepository
                .findByIsDeletedFalse(pageable)
                .map(MatchVideoListResponseDTO::from);

        return MatchVideoPageResponseDTO.from(matchVideoPage);
    }

    // 경기 영상 상세 조회
    public MatchVideoDetailResponseDTO findMatchVideoDetail(
            CustomUserPrincipal principal,
            Integer matchVideoId
    ) {
        matchVideoValidator.validateCanRead(principal);

        GameVideoUploadEntity matchVideo = matchVideoValidator.findActiveMatchVideo(matchVideoId);

        return MatchVideoDetailResponseDTO.from(matchVideo);
    }

    // 경기 영상 수정
    @Transactional
    public MatchVideoDetailResponseDTO updateMatchVideo(
            CustomUserPrincipal principal,
            Integer matchVideoId,
            UpdateMatchVideoRequestDTO request
    ) {
        matchVideoValidator.validateCanCreateOrUpdate(principal);
        matchVideoValidator.validateUpdateRequest(request);
        matchVideoValidator.validateDurationSec(request.getDurationSec());

        GameVideoUploadEntity matchVideo = matchVideoValidator.findActiveMatchVideo(matchVideoId);

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
    @Transactional
    public void deleteMatchVideo(
            CustomUserPrincipal principal,
            Integer matchVideoId
    ) {
        matchVideoValidator.validateCanDelete(principal);

        GameVideoUploadEntity matchVideo = matchVideoValidator.findActiveMatchVideo(matchVideoId);

        matchVideo.setIsDeleted(true);
    }

}