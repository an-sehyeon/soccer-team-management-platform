package com.soccer.platform.service.teamclipdrawing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.teamclipdrawing.CreateTeamAnalysisClipDrawingRequestDTO;
import com.soccer.platform.dto.teamclipdrawing.TeamAnalysisClipDrawingResponseDTO;
import com.soccer.platform.dto.teamclipdrawing.UpdateTeamAnalysisClipDrawingRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.TeamVideoClipDrawingEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.TeamVideoClipDrawingRepository;
import com.soccer.platform.repository.TeamVideoClipRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.MemberQueryService;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;

/*
 * 팀 분석 클립 드로잉 Validator
 * 팀 분석 클립 드로잉 권한, 조회, 시간 범위, drawingData 변환을 처리한다.
 * 드로잉 시간은 생성된 팀 분석 클립 영상 기준 초로 검증한다.
 */

@Component
@RequiredArgsConstructor
public class TeamAnalysisClipDrawingValidator {

    private final TeamVideoClipDrawingRepository teamVideoClipDrawingRepository;
    private final TeamVideoClipRepository teamVideoClipRepository;
    private final MemberQueryService memberQueryService;
    private final PermissionValidator permissionValidator;
    private final ObjectMapper objectMapper;

    // 드로잉 등록/수정 권한 검증
    public void validateCanCreateOrUpdate(CustomUserPrincipal principal) {
        permissionValidator.requireCoachOrAnalyst(
                principal,
                ErrorCode.TEAM_ANALYSIS_CLIP_DRAWING_ACCESS_DENIED
        );
    }

    // 드로잉 조회 권한 검증
    public void validateCanRead(CustomUserPrincipal principal) {
        permissionValidator.requireAuthenticatedServiceUser(
                principal,
                ErrorCode.TEAM_ANALYSIS_CLIP_DRAWING_ACCESS_DENIED
        );
    }

    // 드로잉 삭제 권한 검증
    public void validateCanDelete(CustomUserPrincipal principal) {
        permissionValidator.requireCoach(
                principal,
                ErrorCode.TEAM_ANALYSIS_CLIP_DRAWING_ACCESS_DENIED
        );
    }

    // 로그인 회원 조회
    public MemberEntity findLoginMember(CustomUserPrincipal principal) {
        return memberQueryService.findLoginMember(
                principal,
                ErrorCode.MEMBER_NOT_FOUND
        );
    }

    // 삭제되지 않은 팀 분석 클립 조회
    public TeamVideoClipEntity findActiveTeamVideoClip(Integer teamClipId) {
        if (teamClipId == null) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND);
        }

        return teamVideoClipRepository.findByIdAndIsDeletedFalse(teamClipId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND));
    }

    // 삭제되지 않은 드로잉 조회
    public TeamVideoClipDrawingEntity findActiveDrawing(Integer drawingId) {
        if (drawingId == null) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_DRAWING_NOT_FOUND);
        }

        return teamVideoClipDrawingRepository.findByIdAndIsDeletedFalse(drawingId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_DRAWING_NOT_FOUND));
    }

    // 드로잉에 연결된 삭제되지 않은 팀 분석 클립 조회
    public TeamVideoClipEntity findConnectedActiveTeamVideoClip(
            TeamVideoClipDrawingEntity drawing
    ) {
        if (drawing == null
                || drawing.getTeamVideoClip() == null
                || drawing.getTeamVideoClip().getId() == null) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND);
        }

        return findActiveTeamVideoClip(drawing.getTeamVideoClip().getId());
    }

    // 드로잉 등록 요청 검증
    public void validateCreateRequest(CreateTeamAnalysisClipDrawingRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_DRAWING_DATA);
        }

        validateDrawingType(request.getDrawingType());
    }

    // 드로잉 수정 요청 검증
    public void validateUpdateRequest(UpdateTeamAnalysisClipDrawingRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_DRAWING_DATA);
        }

        validateDrawingType(request.getDrawingType());
    }

    // 드로잉 시간 범위 검증
    public void validateDrawingTimeRange(
            Integer drawingStartTimeSec,
            Integer drawingEndTimeSec,
            TeamVideoClipEntity teamVideoClip
    ) {
        if (drawingStartTimeSec == null || drawingEndTimeSec == null) {
            throw new CustomException(ErrorCode.INVALID_DRAWING_TIME_RANGE);
        }

        if (drawingStartTimeSec < 0 || drawingEndTimeSec < 0) {
            throw new CustomException(ErrorCode.INVALID_DRAWING_TIME_RANGE);
        }

        if (drawingStartTimeSec >= drawingEndTimeSec) {
            throw new CustomException(ErrorCode.INVALID_DRAWING_TIME_RANGE);
        }

        if (teamVideoClip == null
                || teamVideoClip.getStartTimeSec() == null
                || teamVideoClip.getEndTimeSec() == null) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND);
        }

        int clipDurationSec = teamVideoClip.getEndTimeSec() - teamVideoClip.getStartTimeSec();

        if (clipDurationSec <= 0) {
            throw new CustomException(ErrorCode.INVALID_CLIP_TIME_RANGE);
        }

        if (drawingEndTimeSec > clipDurationSec) {
            throw new CustomException(ErrorCode.DRAWING_TIME_OUT_OF_CLIP_RANGE);
        }
    }

    // drawingData JSON을 DB 저장용 문자열로 변환
    public String convertDrawingDataToString(JsonNode drawingData) {
        if (drawingData == null || drawingData.isNull()) {
            throw new CustomException(ErrorCode.EMPTY_DRAWING_DATA);
        }

        if ((drawingData.isObject() || drawingData.isArray()) && drawingData.size() == 0) {
            throw new CustomException(ErrorCode.EMPTY_DRAWING_DATA);
        }

        try {
            return objectMapper.writeValueAsString(drawingData);
        } catch (JsonProcessingException exception) {
            throw new CustomException(ErrorCode.INVALID_DRAWING_DATA);
        }
    }

    // DB에 저장된 drawingData 문자열을 응답용 JSON으로 변환
    public JsonNode convertDrawingDataToJson(String drawingData) {
        try {
            return objectMapper.readTree(drawingData);
        } catch (JsonProcessingException exception) {
            throw new CustomException(ErrorCode.INVALID_DRAWING_DATA);
        }
    }

    // Entity를 응답 DTO로 변환
    public TeamAnalysisClipDrawingResponseDTO toResponseDTO(
            TeamVideoClipDrawingEntity drawing
    ) {
        JsonNode drawingData = convertDrawingDataToJson(drawing.getDrawingData());

        return TeamAnalysisClipDrawingResponseDTO.from(drawing, drawingData);
    }

    // 드로잉 타입 검증
    private void validateDrawingType(Object drawingType) {
        if (drawingType == null) {
            throw new CustomException(ErrorCode.INVALID_DRAWING_DATA);
        }
    }
}