package com.soccer.platform.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerclipdrawing.CreatePlayerClipDrawingRequestDTO;
import com.soccer.platform.dto.playerclipdrawing.CreatePlayerClipDrawingResponseDTO;
import com.soccer.platform.dto.playerclipdrawing.PlayerClipDrawingResponseDTO;
import com.soccer.platform.dto.playerclipdrawing.UpdatePlayerClipDrawingRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipDrawingEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.repository.PlayerVideoClipDrawingRepository;
import com.soccer.platform.repository.PlayerVideoClipRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

/*
 * 선수 개인 분석 클립 드로잉 Service
 * 
 * 선수 개인 분석 클립에 연결된 드로잉 데이터를 등록, 조회, 수정, 삭제한다.
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerClipDrawingService {

	private static final String ROLE_COACH = "COACH";
	private static final String ROLE_ANALYST = "ANALYST";
	private static final String ROLE_PLAYER = "PLAYER";
	
	private final PlayerVideoClipDrawingRepository playerVideoClipDrawingRepository;
	private final PlayerVideoClipRepository playerVideoClipRepository;
	private final MemberRepository memberRepository;
	private final ObjectMapper objectMapper;
	
	// 선수 개인 분석 클립 드로잉 등록
	@Transactional
	public CreatePlayerClipDrawingResponseDTO createDrawing(
			Integer playerClipId,
			CreatePlayerClipDrawingRequestDTO request,
			CustomUserPrincipal principal
	) {
		validateCanCreateOrUpdate(principal);
		
		PlayerVideoClipEntity playerVideoClip = findActivePlayerClip(playerClipId);
		MemberEntity writer = findActiveMember(principal.getMemberId());
		
		validateDrawingTimeRange(
				request.getStartTimeSec(),
				request.getEndTimeSec(),
				playerVideoClip
		);
		
		validateDrawingData(request.getDrawingData());
		
		PlayerVideoClipDrawingEntity drawing = new PlayerVideoClipDrawingEntity();
        drawing.setPlayerVideoClip(playerVideoClip);
        drawing.setMember(writer);
        drawing.setDrawingType(request.getDrawingType());
        drawing.setStartTimeSec(request.getStartTimeSec());
        drawing.setEndTimeSec(request.getEndTimeSec());
        drawing.setDrawingData(request.getDrawingData().toString());
        drawing.setIsDeleted(false);

        PlayerVideoClipDrawingEntity savedDrawing = playerVideoClipDrawingRepository.save(drawing);

        return new CreatePlayerClipDrawingResponseDTO(
                savedDrawing.getId(),
                "선수 개인 분석 클립 드로잉이 등록되었습니다."
        );
    }
		
	// 선수 개인 분석 클립 드로잉 목록 조회
	public List<PlayerClipDrawingResponseDTO> findDrawings(
            Integer playerClipId,
            CustomUserPrincipal principal
    ) {
        PlayerVideoClipEntity playerVideoClip = findActivePlayerClip(playerClipId);

        validateCanReadPlayerClipDrawing(playerVideoClip, principal);

        return playerVideoClipDrawingRepository
                .findByPlayerVideoClipAndIsDeletedFalseOrderByStartTimeSecAscIdAsc(playerVideoClip)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
	
	// 선수 개인 분석 클립 드로잉 상세 조회
	public PlayerClipDrawingResponseDTO findDrawingDetail(
            Integer drawingId,
            CustomUserPrincipal principal
    ) {
        PlayerVideoClipDrawingEntity drawing = findActiveDrawing(drawingId);
        PlayerVideoClipEntity playerVideoClip = drawing.getPlayerVideoClip();

        validatePlayerVideoClipNotDeleted(playerVideoClip);
        validateCanReadPlayerClipDrawing(playerVideoClip, principal);

        return toResponseDTO(drawing);
    }
	
	// 선수 개인 분석 클립 드로잉 수정
	@Transactional
    public PlayerClipDrawingResponseDTO updateDrawing(
            Integer drawingId,
            UpdatePlayerClipDrawingRequestDTO request,
            CustomUserPrincipal principal
    ) {
        validateCanCreateOrUpdate(principal);

        PlayerVideoClipDrawingEntity drawing = findActiveDrawing(drawingId);
        PlayerVideoClipEntity playerVideoClip = drawing.getPlayerVideoClip();

        validatePlayerVideoClipNotDeleted(playerVideoClip);
        validateDrawingTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                playerVideoClip
        );
        validateDrawingData(request.getDrawingData());

        drawing.setDrawingType(request.getDrawingType());
        drawing.setStartTimeSec(request.getStartTimeSec());
        drawing.setEndTimeSec(request.getEndTimeSec());
        drawing.setDrawingData(request.getDrawingData().toString());

        return toResponseDTO(drawing);
    }

	
	// 선수 개인 분석 클립 드로잉 삭제
	@Transactional
    public void deleteDrawing(
            Integer drawingId,
            CustomUserPrincipal principal
    ) {
        validateCanDelete(principal);

        PlayerVideoClipDrawingEntity drawing = findActiveDrawing(drawingId);

        drawing.setIsDeleted(true);
    }

    /**
     * 드로잉 등록/수정 권한 검증
     */
    private void validateCanCreateOrUpdate(CustomUserPrincipal principal) {
        String memberRole = String.valueOf(principal.getMemberRole());

        if (!ROLE_COACH.equals(memberRole) && !ROLE_ANALYST.equals(memberRole)) {
            throw new CustomException(ErrorCode.PLAYER_CLIP_DRAWING_MANAGE_FORBIDDEN);
        }
    }
    
    
    // 드로잉 삭제 권한 검증
    private void validateCanDelete(CustomUserPrincipal principal) {
        String memberRole = String.valueOf(principal.getMemberRole());

        if (!ROLE_COACH.equals(memberRole)) {
            throw new CustomException(ErrorCode.PLAYER_CLIP_DRAWING_DELETE_FORBIDDEN);
        }
    }
    
    // 선수 개인 분석 클립 드로잉 조회 권한 검증
    // 선수는 본인에게 지정된 개인 분석 클립의 드로잉만 조회 가능
    private void validateCanReadPlayerClipDrawing(
            PlayerVideoClipEntity playerVideoClip,
            CustomUserPrincipal principal
    ) {
        String memberRole = String.valueOf(principal.getMemberRole());

        if (ROLE_COACH.equals(memberRole) || ROLE_ANALYST.equals(memberRole)) {
            return;
        }

        if (ROLE_PLAYER.equals(memberRole)) {
            Integer clipPlayerId = playerVideoClip.getPlayer().getId();
            Integer loginMemberId = principal.getMemberId();

            if (!clipPlayerId.equals(loginMemberId)) {
                throw new CustomException(ErrorCode.PLAYER_CLIP_DRAWING_ACCESS_DENIED);
            }

            return;
        }

        throw new CustomException(ErrorCode.PLAYER_CLIP_DRAWING_ACCESS_DENIED);
    }
    
    
    // 삭제되지 않은 선수 개인 분석 클립 조회
    private PlayerVideoClipEntity findActivePlayerClip(Integer playerClipId) {
        return playerVideoClipRepository.findByIdAndIsDeletedFalse(playerClipId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND));
    }
    
    
    // 삭제되지 않은 드로잉 조회
    private PlayerVideoClipDrawingEntity findActiveDrawing(Integer drawingId) {
        return playerVideoClipDrawingRepository.findByIdAndIsDeletedFalse(drawingId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYER_CLIP_DRAWING_NOT_FOUND));
    }
    
    
    // 삭제되지 않은 회원 조회
    private MemberEntity findActiveMember(Integer memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
    
    
    // 선수 개인 분석 클립 삭제 여부 검증
    private void validatePlayerVideoClipNotDeleted(PlayerVideoClipEntity playerVideoClip) {
        if (playerVideoClip == null || Boolean.TRUE.equals(playerVideoClip.getIsDeleted())) {
            throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_NOT_FOUND);
        }
    }
    
    
    // 드로잉 시간 검증
    // 드로잉 시간은 연결된 선수 개인 분석 클립 시간 범위 안에 있어야 한다.
    private void validateDrawingTimeRange(
            Integer drawingStartTimeSec,
            Integer drawingEndTimeSec,
            PlayerVideoClipEntity playerVideoClip
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

        if (drawingStartTimeSec < playerVideoClip.getStartTimeSec()
                || drawingEndTimeSec > playerVideoClip.getEndTimeSec()) {
            throw new CustomException(ErrorCode.DRAWING_TIME_OUT_OF_CLIP_RANGE);
        }
    }
    
    
    // 드로잉 JSON 데이터 검증
    private void validateDrawingData(JsonNode drawingData) {
        if (drawingData == null || drawingData.isNull() || drawingData.isEmpty()) {
            throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
        }
    }
    
    
    // Entity를 응답 DTO로 변환
    private PlayerClipDrawingResponseDTO toResponseDTO(PlayerVideoClipDrawingEntity drawing) {
        return new PlayerClipDrawingResponseDTO(
                drawing.getId(),
                drawing.getPlayerVideoClip().getId(),
                drawing.getDrawingType(),
                drawing.getStartTimeSec(),
                drawing.getEndTimeSec(),
                parseDrawingData(drawing.getDrawingData()),
                drawing.getMember().getId(),
                drawing.getMember().getName(),
                drawing.getCreatedAt(),
                drawing.getUpdatedAt()
        );
    }
    
    
    // DB에 문자열로 저장된 JSON 데이터를 응답용 JsonNode로 변환
    private JsonNode parseDrawingData(String drawingData) {
        try {
            return objectMapper.readTree(drawingData);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.DRAWING_DATA_REQUIRED);
        }
    }
	
}
