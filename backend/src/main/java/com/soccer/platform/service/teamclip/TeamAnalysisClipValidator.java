package com.soccer.platform.service.teamclip;

import org.springframework.data.domain.Pageable;
import com.soccer.platform.common.constants.TeamVideoClipTypeEnum;
import com.soccer.platform.dto.teamanalysisclip.UpdateTeamAnalysisClipWithDrawingsRequestDTO;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.soccer.platform.common.constants.VideoUploadStatusEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipWithDrawingsRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.UpdateTeamAnalysisClipRequestDTO;
import com.soccer.platform.entity.GameVideoUploadEntity;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.TeamVideoClipRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.MatchVideoQueryService;
import com.soccer.platform.service.common.MemberQueryService;
import com.soccer.platform.service.common.PageRequestValidator;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;

/*
 * 팀 분석 클립 Validator
 *
 * 팀 분석 클립 기능에서 사용하는 권한 검증, 요청값 검증, Entity 조회를 담당한다.
 */

@Component
@RequiredArgsConstructor
public class TeamAnalysisClipValidator {

    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_COMMENT_LENGTH = 255;

    private final TeamVideoClipRepository teamVideoClipRepository;
    private final PermissionValidator permissionValidator;
    private final PageRequestValidator pageRequestValidator;
    private final MatchVideoQueryService matchVideoQueryService;
    private final MemberQueryService memberQueryService;

    // 팀 분석 클립 등록/수정 권한 검증
    public void validateCanCreateOrUpdate(CustomUserPrincipal principal) {
        permissionValidator.requireCoachOrAnalyst(
                principal,
                ErrorCode.TEAM_ANALYSIS_CLIP_ACCESS_DENIED
        );
    }

    // 팀 분석 클립 조회 권한 검증
    public void validateCanRead(CustomUserPrincipal principal) {
        permissionValidator.requireAuthenticatedServiceUser(
                principal,
                ErrorCode.TEAM_ANALYSIS_CLIP_ACCESS_DENIED
        );
    }

    // 팀 분석 클립 삭제 권한 검증
    public void validateCanDelete(CustomUserPrincipal principal) {
        permissionValidator.requireCoach(
                principal,
                ErrorCode.TEAM_ANALYSIS_CLIP_ACCESS_DENIED
        );
    }

    // 생성 중인 팀 분석 클립 수정 방지
    public void validateCanUpdateGenerationTarget(TeamVideoClipEntity teamClip) {
        if (teamClip == null) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND);
        }

        if (teamClip.getStatus() == VideoUploadStatusEnum.PROCESSING) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_PROCESSING_CANNOT_UPDATE);
        }
    }

    // 로그인 회원 조회
    public MemberEntity findLoginMember(CustomUserPrincipal principal) {
        return memberQueryService.findLoginMember(
                principal,
                ErrorCode.MEMBER_NOT_FOUND
        );
    }

    // 삭제되지 않은 원본 경기 영상 조회
    public GameVideoUploadEntity findActiveMatchVideo(Integer matchVideoId) {
        return matchVideoQueryService.findActiveMatchVideoById(
                matchVideoId,
                ErrorCode.MATCH_VIDEO_NOT_FOUND
        );
    }

    // 영상 길이 정보가 준비된 원본 경기 영상 조회
    public GameVideoUploadEntity findActiveMatchVideoWithDuration(Integer matchVideoId) {
        return matchVideoQueryService.findActiveMatchVideoWithDuration(
                matchVideoId,
                ErrorCode.MATCH_VIDEO_NOT_FOUND,
                ErrorCode.MATCH_VIDEO_DURATION_NOT_READY
        );
    }
    
 // 선택 입력된 팀 분석 클립 유형 변환
    public TeamVideoClipTypeEnum parseNullableClipType(String clipType) {
        if (clipType == null || clipType.isBlank()) {
            return null;
        }

        try {
            return TeamVideoClipTypeEnum.valueOf(clipType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }
    }

    // 삭제되지 않은 팀 분석 클립 조회
    public TeamVideoClipEntity findActiveTeamClip(Integer teamClipId) {
        if (teamClipId == null) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ANALYSIS_CLIP_REQUEST);
        }

        return teamVideoClipRepository.findByIdAndIsDeletedFalse(teamClipId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND));
    }

    // matchVideoId 필터가 있는 목록 조회 Pageable 생성
    public Pageable createMatchVideoFilteredPageable(Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "startTimeSec")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"));

        return pageRequestValidator.createPageable(page, size, sort);
    }

    // 기본 팀 분석 클립 목록 Pageable 생성
    public Pageable createDefaultPageable(Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        return pageRequestValidator.createPageable(page, size, sort);
    }

    // 팀 분석 클립 등록 요청 검증
    public void validateCreateRequest(CreateTeamAnalysisClipRequestDTO request) {
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

    // 팀 분석 클립과 드로잉 통합 등록 요청 검증
    public void validateCreateWithDrawingsRequest(
            CreateTeamAnalysisClipWithDrawingsRequestDTO request
    ) {
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
    public void validateUpdateRequest(UpdateTeamAnalysisClipRequestDTO request) {
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

    // 클립 시간 구간 검증
    public void validateClipTimeRange(
            Integer startTimeSec,
            Integer endTimeSec,
            Integer durationSec
    ) {
        if (startTimeSec == null || endTimeSec == null) {
            throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
        }

        if (startTimeSec < 0 || endTimeSec < 0 || startTimeSec >= endTimeSec) {
            throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
        }

        if (durationSec == null || durationSec <= 0) {
            throw new CustomException(ErrorCode.MATCH_VIDEO_DURATION_NOT_READY);
        }

        if (endTimeSec > durationSec) {
            throw new CustomException(ErrorCode.CLIP_TIME_EXCEEDS_VIDEO_DURATION);
        }
    }

    // 빈 문자열 정리
    public String trimNullableText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
    
    // 팀 분석 클립과 드로잉 통합 수정 요청 검증
    public void validateUpdateWithDrawingsRequest(
            UpdateTeamAnalysisClipWithDrawingsRequestDTO request
    ) {
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
        validateMaxLength(title, MAX_TITLE_LENGTH);

        if (comment != null && !comment.trim().isEmpty()) {
            validateMaxLength(comment, MAX_COMMENT_LENGTH);
        }

        if (startTimeSec == null || endTimeSec == null) {
            throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
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
}