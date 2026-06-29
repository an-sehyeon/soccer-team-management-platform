package com.soccer.platform.service.matchvideo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.dto.matchvideo.CreateMatchVideoRequestDTO;
import com.soccer.platform.dto.matchvideo.CreateMatchVideoResponseDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoDetailResponseDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoListResponseDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoPageResponseDTO;
import com.soccer.platform.dto.matchvideo.UpdateMatchVideoRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.repository.GameVideoUploadRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.metadata.MatchVideoMetadataExtractor;
import com.soccer.platform.service.storage.MatchVideoStorageService;
import com.soccer.platform.service.storage.StoredMatchVideoFile;

import lombok.RequiredArgsConstructor;

/*
 * 경기 영상 업로드 Service
 * 경기 원본 영상 파일, 접근 URL, 영상 길이, 경기 기본 정보를 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchVideoService {

    private final GameVideoUploadRepository gameVideoUploadRepository;
    private final MatchVideoValidator matchVideoValidator;
    private final MatchVideoStorageService matchVideoStorageService;
    private final MatchVideoMetadataExtractor matchVideoMetadataExtractor;
    
    // 경기 영상 등록
    /*
     * 1. 역할 권한 확인
     * 2. 경기 메타데이터 검증
     * 3. 영상 파일을 저장소에 저장
     * 4. 저장된 파일 경로 기준으로 영상 길이 추출
     * 5. 영상 접근 URL, 영상 길이, 상태를 DB에 저장
     * 6. 경기 영상 등록 완료 응답 반환
     */
    @Transactional
    public CreateMatchVideoResponseDTO createMatchVideo(
            CustomUserPrincipal principal,
            MultipartFile videoFile,
            CreateMatchVideoRequestDTO requestDTO
    ) {
        matchVideoValidator.validateCanManageMatchVideo(principal);
        matchVideoValidator.validateCreateRequest(requestDTO);

        MemberEntity uploader = matchVideoValidator.findLoginMember(principal.getMemberId());

        StoredMatchVideoFile storedFile = matchVideoStorageService.store(videoFile);
        Integer durationSec = matchVideoMetadataExtractor.extractDurationSec(storedFile.getStoredFilePath());

        GameVideoUploadEntity matchVideo = new GameVideoUploadEntity();
        matchVideo.setMember(uploader);
        matchVideo.setUrl(storedFile.getAccessUrl());
        matchVideo.setTitle(requestDTO.getTitle());
        matchVideo.setGameDate(requestDTO.getGameDate());
        matchVideo.setPlace(requestDTO.getPlace());
        matchVideo.setHomeScore(requestDTO.getHomeScore());
        matchVideo.setAwayScore(requestDTO.getAwayScore());
        matchVideo.setMatchResult(requestDTO.getMatchResult());
        matchVideo.setDurationSec(durationSec);
        matchVideo.setStatus(VideoUploadStatusEnum.READY);
        matchVideo.setIsDeleted(false);

        GameVideoUploadEntity savedMatchVideo = gameVideoUploadRepository.save(matchVideo);

        return CreateMatchVideoResponseDTO.of(savedMatchVideo.getId());
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

        GameVideoUploadEntity matchVideo = matchVideoValidator.findActiveMatchVideo(matchVideoId);

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