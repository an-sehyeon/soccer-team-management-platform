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
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipDetailResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipListResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipPageResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.UpdateTeamAnalysisClipRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.GameVideoUploadRepository;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.repository.TeamVideoClipRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

/*
 * 팀 분석 클립 Service
 * 원본 경기 영상 기준으로 팀 전체가 볼 수 있는 분석 클립을 관리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TeamAnalysisClipService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TeamVideoClipRepository teamVideoClipRepository;
    private final GameVideoUploadRepository gameVideoUploadRepository;
    private final MemberRepository memberRepository;

    // 팀 분석 클립 등록
    public Integer createTeamAnalysisClip(
            CustomUserPrincipal principal,
            CreateTeamAnalysisClipRequestDTO request
    ) {
        validateCanCreateOrUpdate(principal);
        validateCreateRequest(request);

        GameVideoUploadEntity matchVideo = findActiveMatchVideo(request.getMatchVideoId());
        validateClipTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                matchVideo.getDurationSec()
        );

        MemberEntity editor = findLoginMember(principal.getMemberId());

        TeamVideoClipEntity teamClip = new TeamVideoClipEntity();
        teamClip.setGameVideoUpload(matchVideo);
        teamClip.setMember(editor);
        teamClip.setClipType(request.getClipType());
        teamClip.setTitle(request.getTitle().trim());
        teamClip.setComment(trimNullableText(request.getComment()));
        teamClip.setStartTimeSec(request.getStartTimeSec());
        teamClip.setEndTimeSec(request.getEndTimeSec());
        teamClip.setUrl(null);
        teamClip.setStatus(VideoUploadStatusEnum.READY);
        teamClip.setIsDeleted(false);

        TeamVideoClipEntity savedTeamClip = teamVideoClipRepository.save(teamClip);

        return savedTeamClip.getId();
    }

    // 팀 분석 클립 목록 조회
    @Transactional(readOnly = true)
    public TeamAnalysisClipPageResponseDTO findTeamAnalysisClips(
            CustomUserPrincipal principal,
            Integer matchVideoId,
            int page,
            int size
    ) {
        validateCanRead(principal);
        validatePageRequest(page, size);

        Page<TeamAnalysisClipListResponseDTO> teamClipPage;

        if (matchVideoId != null) {
            findActiveMatchVideo(matchVideoId);

            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.ASC, "startTimeSec")
                            .and(Sort.by(Sort.Direction.DESC, "createdAt"))
            );

            teamClipPage = teamVideoClipRepository
                    .findByGameVideoUpload_IdAndIsDeletedFalse(matchVideoId, pageable)
                    .map(TeamAnalysisClipListResponseDTO::from);
        } else {
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );

            teamClipPage = teamVideoClipRepository
                    .findByIsDeletedFalse(pageable)
                    .map(TeamAnalysisClipListResponseDTO::from);
        }

        return TeamAnalysisClipPageResponseDTO.from(teamClipPage);
    }

    // 팀 분석 클립 상세 조회
    @Transactional(readOnly = true)
    public TeamAnalysisClipDetailResponseDTO findTeamAnalysisClipDetail(
            CustomUserPrincipal principal,
            Integer teamClipId
    ) {
        validateCanRead(principal);

        TeamVideoClipEntity teamClip = findActiveTeamClip(teamClipId);

        return TeamAnalysisClipDetailResponseDTO.from(teamClip);
    }

    // 팀 분석 클립 수정
    public TeamAnalysisClipDetailResponseDTO updateTeamAnalysisClip(
            CustomUserPrincipal principal,
            Integer teamClipId,
            UpdateTeamAnalysisClipRequestDTO request
    ) {
        validateCanCreateOrUpdate(principal);
        validateUpdateRequest(request);

        TeamVideoClipEntity teamClip = findActiveTeamClip(teamClipId);

        validateClipTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                teamClip.getGameVideoUpload().getDurationSec()
        );

        teamClip.setClipType(request.getClipType());
        teamClip.setTitle(request.getTitle().trim());
        teamClip.setComment(trimNullableText(request.getComment()));
        teamClip.setStartTimeSec(request.getStartTimeSec());
        teamClip.setEndTimeSec(request.getEndTimeSec());

        return TeamAnalysisClipDetailResponseDTO.from(teamClip);
    }

    // 팀 분석 클립 삭제
    public void deleteTeamAnalysisClip(
            CustomUserPrincipal principal,
            Integer teamClipId
    ) {
        validateCanDelete(principal);

        TeamVideoClipEntity teamClip = findActiveTeamClip(teamClipId);

        teamClip.setIsDeleted(true);
    }

    // 팀 분석 클립 등록/수정 권한 검증
    private void validateCanCreateOrUpdate(CustomUserPrincipal principal) {
        String memberRole = String.valueOf(principal.getMemberRole());

        if (!"COACH".equals(memberRole) && !"ANALYST".equals(memberRole)) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_ACCESS_DENIED);
        }
    }

    // 팀 분석 클립 조회 권한 검증
    private void validateCanRead(CustomUserPrincipal principal) {
        String memberRole = String.valueOf(principal.getMemberRole());

        if (!"COACH".equals(memberRole)
                && !"ANALYST".equals(memberRole)
                && !"PLAYER".equals(memberRole)) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_ACCESS_DENIED);
        }
    }

    // 팀 분석 클립 삭제 권한 검증
    private void validateCanDelete(CustomUserPrincipal principal) {
        String memberRole = String.valueOf(principal.getMemberRole());

        if (!"COACH".equals(memberRole)) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_ACCESS_DENIED);
        }
    }

    // 로그인 회원 조회
    private MemberEntity findLoginMember(Integer memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 삭제되지 않은 원본 경기 영상 조회
    private GameVideoUploadEntity findActiveMatchVideo(Integer matchVideoId) {
        if (matchVideoId == null) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }

        return gameVideoUploadRepository.findByIdAndIsDeletedFalse(matchVideoId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_VIDEO_NOT_FOUND));
    }

    // 삭제되지 않은 팀 분석 클립 조회
    private TeamVideoClipEntity findActiveTeamClip(Integer teamClipId) {
        if (teamClipId == null) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }

        return teamVideoClipRepository.findByIdAndIsDeletedFalse(teamClipId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND));
    }

    // 팀 분석 클립 등록 요청 검증
    private void validateCreateRequest(CreateTeamAnalysisClipRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }

        if (request.getMatchVideoId() == null) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }

        validateTeamClipRequiredValues(
                request.getClipType(),
                request.getTitle(),
                request.getComment(),
                request.getStartTimeSec(),
                request.getEndTimeSec()
        );
    }

    // 팀 분석 클립 수정 요청 검증
    private void validateUpdateRequest(UpdateTeamAnalysisClipRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }

        validateTeamClipRequiredValues(
                request.getClipType(),
                request.getTitle(),
                request.getComment(),
                request.getStartTimeSec(),
                request.getEndTimeSec()
        );
    }

    // 팀 분석 클립 공통 필수값 검증
    private void validateTeamClipRequiredValues(
            Object clipType,
            String title,
            String comment,
            Integer startTimeSec,
            Integer endTimeSec
    ) {
        if (clipType == null) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }

        validateRequiredText(title);
        validateMaxLength(title, 255);

        if (comment != null) {
            validateMaxLength(comment, 255);
        }

        if (startTimeSec == null || endTimeSec == null) {
            throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
        }
    }

    // 클립 시간 구간 검증
    private void validateClipTimeRange(
            Integer startTimeSec,
            Integer endTimeSec,
            Integer durationSec
    ) {
        if (startTimeSec == null || endTimeSec == null) {
            throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
        }

        if (startTimeSec < 0 || endTimeSec <= 0 || startTimeSec >= endTimeSec) {
            throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
        }

        if (durationSec == null || durationSec <= 0) {
            throw new CustomException(ErrorCode.INVALID_VIDEO_DURATION);
        }

        if (endTimeSec > durationSec) {
            throw new CustomException(ErrorCode.CLIP_TIME_EXCEEDS_VIDEO_DURATION);
        }
    }

    // 필수 문자열 검증
    private void validateRequiredText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }
    }

    // 문자열 길이 검증
    private void validateMaxLength(String value, int maxLength) {
        if (value.trim().length() > maxLength) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }
    }

    // 빈 문자열 정리
    private String trimNullableText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    // 페이지 요청값 검증
    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }
    }
}