package com.soccer.platform.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.teamclipdrawing.CreateTeamAnalysisClipDrawingRequestDTO;
import com.soccer.platform.dto.teamclipdrawing.CreateTeamAnalysisClipDrawingResponseDTO;
import com.soccer.platform.dto.teamclipdrawing.TeamAnalysisClipDrawingResponseDTO;
import com.soccer.platform.dto.teamclipdrawing.UpdateTeamAnalysisClipDrawingRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.TeamVideoClipDrawingEntity;
import com.soccer.platform.entity.TeamVideoClipEntity;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.repository.TeamVideoClipDrawingRepository;
import com.soccer.platform.repository.TeamVideoClipRepository;
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
public class TeamAnalysisClipDrawingService {

	private final TeamVideoClipDrawingRepository teamVideoClipDrawingRepository;
	private final TeamVideoClipRepository teamVideoClipRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
	
	// 팀 분석 클립 드로잉 등록
    @Transactional
    public CreateTeamAnalysisClipDrawingResponseDTO createDrawing(
            Integer teamClipId,
            CreateTeamAnalysisClipDrawingRequestDTO request,
            CustomUserPrincipal principal
    ) {
        checkCreateOrUpdatePermission(principal);

        MemberEntity member = findActiveMember(principal.getMemberId());
        TeamVideoClipEntity teamVideoClip = findActiveTeamVideoClip(teamClipId);

        validateDrawingTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                teamVideoClip
        );

        String drawingData = convertDrawingDataToString(request.getDrawingData());

        TeamVideoClipDrawingEntity drawing = new TeamVideoClipDrawingEntity();
        drawing.setTeamVideoClip(teamVideoClip);
        drawing.setMember(member);
        drawing.setDrawingType(request.getDrawingType());
        drawing.setStartTimeSec(request.getStartTimeSec());
        drawing.setEndTimeSec(request.getEndTimeSec());
        drawing.setDrawingData(drawingData);

        TeamVideoClipDrawingEntity savedDrawing = teamVideoClipDrawingRepository.save(drawing);

        return new CreateTeamAnalysisClipDrawingResponseDTO(
                savedDrawing.getId(),
                "팀 분석 클립 드로잉이 등록되었습니다."
        );
    }

    /*
     * 팀 분석 클립 드로잉 목록 조회
     *
     * - 지도자, 분석관, 선수 모두 조회할 수 있다.
     * - 팀 분석 클립이 존재하고 삭제되지 않았는지 확인한다.
     * - 삭제되지 않은 드로잉만 시작 시간 순서로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<TeamAnalysisClipDrawingResponseDTO> findDrawingsByTeamClip(
            Integer teamClipId,
            CustomUserPrincipal principal
    ) {
        checkReadPermission(principal);

        TeamVideoClipEntity teamVideoClip = findActiveTeamVideoClip(teamClipId);

        return teamVideoClipDrawingRepository
                .findByTeamVideoClipAndIsDeletedFalseOrderByStartTimeSecAscIdAsc(teamVideoClip)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /*
     * 팀 분석 클립 드로잉 상세 조회
     *
     * - 드로잉이 존재하고 삭제되지 않았는지 확인한다.
     * - 연결된 팀 분석 클립이 삭제되지 않았는지 확인한다.
     */
    @Transactional(readOnly = true)
    public TeamAnalysisClipDrawingResponseDTO findDrawingDetail(
            Integer drawingId,
            CustomUserPrincipal principal
    ) {
        checkReadPermission(principal);

        TeamVideoClipDrawingEntity drawing = findActiveDrawing(drawingId);
        validateConnectedTeamVideoClipIsActive(drawing);

        return toResponseDTO(drawing);
    }

    /*
     * 팀 분석 클립 드로잉 수정
     *
     * 1. 지도자/분석관 권한인지 확인
     * 2. 기존 드로잉이 존재하고 삭제되지 않았는지 확인
     * 3. 연결된 팀 분석 클립이 삭제되지 않았는지 확인
     * 4. 수정할 드로잉 시간이 팀 분석 클립 시간 범위 안에 있는지 확인
     * 5. drawingData JSON을 문자열로 변환
     * 6. 드로잉 정보를 수정
     */
    @Transactional
    public TeamAnalysisClipDrawingResponseDTO updateDrawing(
            Integer drawingId,
            UpdateTeamAnalysisClipDrawingRequestDTO request,
            CustomUserPrincipal principal
    ) {
        checkCreateOrUpdatePermission(principal);

        TeamVideoClipDrawingEntity drawing = findActiveDrawing(drawingId);
        TeamVideoClipEntity teamVideoClip = drawing.getTeamVideoClip();

        validateConnectedTeamVideoClipIsActive(drawing);

        validateDrawingTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                teamVideoClip
        );

        String drawingData = convertDrawingDataToString(request.getDrawingData());

        drawing.setDrawingType(request.getDrawingType());
        drawing.setStartTimeSec(request.getStartTimeSec());
        drawing.setEndTimeSec(request.getEndTimeSec());
        drawing.setDrawingData(drawingData);

        return toResponseDTO(drawing);
    }
    
    /*
     * 팀 분석 클립 드로잉 삭제
     *
     * - 지도자만 삭제할 수 있다.
     * - 실제 DB 삭제가 아니라 isDeleted = true로 처리한다.
     */
    @Transactional
    public void deleteDrawing(
            Integer drawingId,
            CustomUserPrincipal principal
    ) {
        checkDeletePermission(principal);

        TeamVideoClipDrawingEntity drawing = findActiveDrawing(drawingId);
        validateConnectedTeamVideoClipIsActive(drawing);

        drawing.setIsDeleted(true);
    }

    /*
     * 드로잉 등록/수정 권한 검증
     * 지도자와 분석관만 등록/수정할 수 있다.
     */
    private void checkCreateOrUpdatePermission(CustomUserPrincipal principal) {
        MemberRoleEnum memberRole = principal.getMemberRole();

        if (memberRole != MemberRoleEnum.COACH && memberRole != MemberRoleEnum.ANALYST) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_DRAWING_ACCESS_DENIED);
        }
    }

    /*
     * 드로잉 조회 권한 검증
     *
     * 지도자, 분석관, 선수 모두 조회할 수 있다.
     */
    private void checkReadPermission(CustomUserPrincipal principal) {
        MemberRoleEnum memberRole = principal.getMemberRole();

        if (
                memberRole != MemberRoleEnum.COACH
                        && memberRole != MemberRoleEnum.ANALYST
                        && memberRole != MemberRoleEnum.PLAYER
        ) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_DRAWING_ACCESS_DENIED);
        }
    }

    /*
     * 드로잉 삭제 권한 검증
     *
     * 지도자만 삭제할 수 있다.
     */
    private void checkDeletePermission(CustomUserPrincipal principal) {
        if (principal.getMemberRole() != MemberRoleEnum.COACH) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_DRAWING_ACCESS_DENIED);
        }
    }

    // 삭제되지 않은 회원 조회
    private MemberEntity findActiveMember(Integer memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 삭제되지 않은 팀 분석 클립 조회
    private TeamVideoClipEntity findActiveTeamVideoClip(Integer teamClipId) {
        return teamVideoClipRepository.findByIdAndIsDeletedFalse(teamClipId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND));
    }

    // 삭제되지 않은 드로잉 조회
    private TeamVideoClipDrawingEntity findActiveDrawing(Integer drawingId) {
        return teamVideoClipDrawingRepository.findByIdAndIsDeletedFalse(drawingId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_DRAWING_NOT_FOUND));
    }

    /*
     * 드로잉에 연결된 팀 분석 클립이 유효한지 확인
     *
     * 드로잉 자체는 살아 있어도 연결된 팀 분석 클립이 삭제되었다면
     * 해당 드로잉은 조회/수정/삭제 대상이 될 수 없다.
     */
    private void validateConnectedTeamVideoClipIsActive(TeamVideoClipDrawingEntity drawing) {
        TeamVideoClipEntity teamVideoClip = drawing.getTeamVideoClip();

        if (teamVideoClip == null || Boolean.TRUE.equals(teamVideoClip.getIsDeleted())) {
            throw new CustomException(ErrorCode.TEAM_ANALYSIS_CLIP_NOT_FOUND);
        }
    }

    /*
     * 드로잉 시간 범위 검증
     *
     * 검증 기준
     * - 시작 시간은 종료 시간보다 작아야 한다.
     * - 드로잉 시작 시간은 팀 분석 클립 시작 시간보다 빠를 수 없다.
     * - 드로잉 종료 시간은 팀 분석 클립 종료 시간보다 늦을 수 없다.
     */
    private void validateDrawingTimeRange(
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

        if (
                drawingStartTimeSec < teamVideoClip.getStartTimeSec()
                        || drawingEndTimeSec > teamVideoClip.getEndTimeSec()
        ) {
            throw new CustomException(ErrorCode.DRAWING_TIME_OUT_OF_CLIP_RANGE);
        }
    }

    // drawingData JSON을 DB 저장용 문자열로 변환
    private String convertDrawingDataToString(JsonNode drawingData) {
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
    private JsonNode convertDrawingDataToJson(String drawingData) {
        try {
            return objectMapper.readTree(drawingData);
        } catch (JsonProcessingException exception) {
            throw new CustomException(ErrorCode.INVALID_DRAWING_DATA);
        }
    }

    // Entity를 응답 DTO로 변환
    private TeamAnalysisClipDrawingResponseDTO toResponseDTO(TeamVideoClipDrawingEntity drawing) {
        JsonNode drawingData = convertDrawingDataToJson(drawing.getDrawingData());

        return TeamAnalysisClipDrawingResponseDTO.from(drawing, drawingData);
    }
}