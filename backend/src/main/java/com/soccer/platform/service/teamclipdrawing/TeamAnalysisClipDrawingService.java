package com.soccer.platform.service.teamclipdrawing;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.dto.teamclipdrawing.CreateTeamAnalysisClipDrawingRequestDTO;
import com.soccer.platform.dto.teamclipdrawing.CreateTeamAnalysisClipDrawingResponseDTO;
import com.soccer.platform.dto.teamclipdrawing.TeamAnalysisClipDrawingResponseDTO;
import com.soccer.platform.dto.teamclipdrawing.UpdateTeamAnalysisClipDrawingRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.TeamVideoClipDrawingEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.TeamVideoClipDrawingRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

/*
 * 팀 분석 클립 드로잉 Service
 * 
 * - 팀 분석 클립 드로잉 등록
 * - 팀 분석 클립 드로잉 목록 조회
 * - 팀 분석 클립 드로잉 상세 조회
 * - 팀 분석 클립 드로잉 수정
 * - 팀 분석 클립 드로잉 삭제   
 * 
 * - drawingData는 프론트 캔버스 JSON을 문자열로 변환해서 DB에 저장.
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamAnalysisClipDrawingService {

    private final TeamVideoClipDrawingRepository teamVideoClipDrawingRepository;
    private final TeamAnalysisClipDrawingValidator teamAnalysisClipDrawingValidator;

    // 팀 분석 클립 드로잉 등록
    @Transactional
    public CreateTeamAnalysisClipDrawingResponseDTO createDrawing(
            Integer teamClipId,
            CreateTeamAnalysisClipDrawingRequestDTO request,
            CustomUserPrincipal principal
    ) {
        teamAnalysisClipDrawingValidator.validateCanCreateOrUpdate(principal);
        teamAnalysisClipDrawingValidator.validateCreateRequest(request);

        MemberEntity member = teamAnalysisClipDrawingValidator.findLoginMember(principal);
        TeamVideoClipEntity teamVideoClip =
                teamAnalysisClipDrawingValidator.findActiveTeamVideoClip(teamClipId);

        teamAnalysisClipDrawingValidator.validateDrawingTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                teamVideoClip
        );

        String drawingData =
                teamAnalysisClipDrawingValidator.convertDrawingDataToString(
                        request.getDrawingData()
                );

        TeamVideoClipDrawingEntity drawing = new TeamVideoClipDrawingEntity();
        drawing.setTeamVideoClip(teamVideoClip);
        drawing.setMember(member);
        drawing.setDrawingType(request.getDrawingType());
        drawing.setStartTimeSec(request.getStartTimeSec());
        drawing.setEndTimeSec(request.getEndTimeSec());
        drawing.setDrawingData(drawingData);
        drawing.setIsDeleted(false);

        TeamVideoClipDrawingEntity savedDrawing =
                teamVideoClipDrawingRepository.save(drawing);

        return new CreateTeamAnalysisClipDrawingResponseDTO(
                savedDrawing.getId(),
                "팀 분석 클립 드로잉이 등록되었습니다."
        );
    }

    // 팀 분석 클립 드로잉 목록 조회
    public List<TeamAnalysisClipDrawingResponseDTO> findDrawingsByTeamClip(
            Integer teamClipId,
            CustomUserPrincipal principal
    ) {
        teamAnalysisClipDrawingValidator.validateCanRead(principal);

        TeamVideoClipEntity teamVideoClip =
                teamAnalysisClipDrawingValidator.findActiveTeamVideoClip(teamClipId);

        return teamVideoClipDrawingRepository
                .findByTeamVideoClipAndIsDeletedFalseOrderByStartTimeSecAscIdAsc(teamVideoClip)
                .stream()
                .map(teamAnalysisClipDrawingValidator::toResponseDTO)
                .toList();
    }

    // 팀 분석 클립 드로잉 상세 조회
    public TeamAnalysisClipDrawingResponseDTO findDrawingDetail(
            Integer drawingId,
            CustomUserPrincipal principal
    ) {
        teamAnalysisClipDrawingValidator.validateCanRead(principal);

        TeamVideoClipDrawingEntity drawing =
                teamAnalysisClipDrawingValidator.findActiveDrawing(drawingId);

        teamAnalysisClipDrawingValidator.findConnectedActiveTeamVideoClip(drawing);

        return teamAnalysisClipDrawingValidator.toResponseDTO(drawing);
    }

    // 팀 분석 클립 드로잉 수정
    @Transactional
    public TeamAnalysisClipDrawingResponseDTO updateDrawing(
            Integer drawingId,
            UpdateTeamAnalysisClipDrawingRequestDTO request,
            CustomUserPrincipal principal
    ) {
        teamAnalysisClipDrawingValidator.validateCanCreateOrUpdate(principal);
        teamAnalysisClipDrawingValidator.validateUpdateRequest(request);

        TeamVideoClipDrawingEntity drawing =
                teamAnalysisClipDrawingValidator.findActiveDrawing(drawingId);

        TeamVideoClipEntity teamVideoClip =
                teamAnalysisClipDrawingValidator.findConnectedActiveTeamVideoClip(drawing);

        teamAnalysisClipDrawingValidator.validateDrawingTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                teamVideoClip
        );

        String drawingData =
                teamAnalysisClipDrawingValidator.convertDrawingDataToString(
                        request.getDrawingData()
                );

        drawing.setDrawingType(request.getDrawingType());
        drawing.setStartTimeSec(request.getStartTimeSec());
        drawing.setEndTimeSec(request.getEndTimeSec());
        drawing.setDrawingData(drawingData);

        return teamAnalysisClipDrawingValidator.toResponseDTO(drawing);
    }

    // 팀 분석 클립 드로잉 삭제
    @Transactional
    public void deleteDrawing(
            Integer drawingId,
            CustomUserPrincipal principal
    ) {
        teamAnalysisClipDrawingValidator.validateCanDelete(principal);

        TeamVideoClipDrawingEntity drawing =
                teamAnalysisClipDrawingValidator.findActiveDrawing(drawingId);

        teamAnalysisClipDrawingValidator.findConnectedActiveTeamVideoClip(drawing);

        drawing.setIsDeleted(true);
    }
}