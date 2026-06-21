package com.soccer.platform.service.playerclipdrawing;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.dto.playerclipdrawing.CreatePlayerClipDrawingRequestDTO;
import com.soccer.platform.dto.playerclipdrawing.CreatePlayerClipDrawingResponseDTO;
import com.soccer.platform.dto.playerclipdrawing.PlayerClipDrawingResponseDTO;
import com.soccer.platform.dto.playerclipdrawing.UpdatePlayerClipDrawingRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipDrawingEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.repository.PlayerVideoClipDrawingRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

/*
 * 선수 개인 분석 클립 드로잉 Service
 * 선수 개인 분석 클립에 연결된 드로잉 데이터를 등록, 조회, 수정, 삭제한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerClipDrawingService {

    private final PlayerVideoClipDrawingRepository playerVideoClipDrawingRepository;
    private final PlayerClipDrawingValidator playerClipDrawingValidator;

    // 선수 개인 분석 클립 드로잉 등록
    @Transactional
    public CreatePlayerClipDrawingResponseDTO createDrawing(
            Integer playerClipId,
            CreatePlayerClipDrawingRequestDTO request,
            CustomUserPrincipal principal
    ) {
        playerClipDrawingValidator.validateCanCreateOrUpdate(principal);
        playerClipDrawingValidator.validateCreateRequest(request);

        PlayerVideoClipEntity playerVideoClip =
                playerClipDrawingValidator.findActivePlayerClip(playerClipId);

        MemberEntity writer = playerClipDrawingValidator.findLoginMember(principal);

        playerClipDrawingValidator.validateDrawingTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                playerVideoClip
        );

        String drawingData =
                playerClipDrawingValidator.convertDrawingDataToString(
                        request.getDrawingData()
                );

        PlayerVideoClipDrawingEntity drawing = new PlayerVideoClipDrawingEntity();
        drawing.setPlayerVideoClip(playerVideoClip);
        drawing.setMember(writer);
        drawing.setDrawingType(request.getDrawingType());
        drawing.setStartTimeSec(request.getStartTimeSec());
        drawing.setEndTimeSec(request.getEndTimeSec());
        drawing.setDrawingData(drawingData);
        drawing.setIsDeleted(false);

        PlayerVideoClipDrawingEntity savedDrawing =
                playerVideoClipDrawingRepository.save(drawing);

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
        PlayerVideoClipEntity playerVideoClip =
                playerClipDrawingValidator.findActivePlayerClip(playerClipId);

        playerClipDrawingValidator.validateCanReadPlayerClipDrawing(
                playerVideoClip,
                principal
        );

        return playerVideoClipDrawingRepository
                .findByPlayerVideoClipAndIsDeletedFalseOrderByStartTimeSecAscIdAsc(playerVideoClip)
                .stream()
                .map(playerClipDrawingValidator::toResponseDTO)
                .toList();
    }

    // 선수 개인 분석 클립 드로잉 상세 조회
    public PlayerClipDrawingResponseDTO findDrawingDetail(
            Integer drawingId,
            CustomUserPrincipal principal
    ) {
        PlayerVideoClipDrawingEntity drawing =
                playerClipDrawingValidator.findActiveDrawing(drawingId);

        PlayerVideoClipEntity playerVideoClip =
                playerClipDrawingValidator.findConnectedActivePlayerClip(drawing);

        playerClipDrawingValidator.validateCanReadPlayerClipDrawing(
                playerVideoClip,
                principal
        );

        return playerClipDrawingValidator.toResponseDTO(drawing);
    }

    // 선수 개인 분석 클립 드로잉 수정
    @Transactional
    public PlayerClipDrawingResponseDTO updateDrawing(
            Integer drawingId,
            UpdatePlayerClipDrawingRequestDTO request,
            CustomUserPrincipal principal
    ) {
        playerClipDrawingValidator.validateCanCreateOrUpdate(principal);
        playerClipDrawingValidator.validateUpdateRequest(request);

        PlayerVideoClipDrawingEntity drawing =
                playerClipDrawingValidator.findActiveDrawing(drawingId);

        PlayerVideoClipEntity playerVideoClip =
                playerClipDrawingValidator.findConnectedActivePlayerClip(drawing);

        playerClipDrawingValidator.validateDrawingTimeRange(
                request.getStartTimeSec(),
                request.getEndTimeSec(),
                playerVideoClip
        );

        String drawingData =
                playerClipDrawingValidator.convertDrawingDataToString(
                        request.getDrawingData()
                );

        drawing.setDrawingType(request.getDrawingType());
        drawing.setStartTimeSec(request.getStartTimeSec());
        drawing.setEndTimeSec(request.getEndTimeSec());
        drawing.setDrawingData(drawingData);

        return playerClipDrawingValidator.toResponseDTO(drawing);
    }

    // 선수 개인 분석 클립 드로잉 삭제
    @Transactional
    public void deleteDrawing(
            Integer drawingId,
            CustomUserPrincipal principal
    ) {
        playerClipDrawingValidator.validateCanDelete(principal);

        PlayerVideoClipDrawingEntity drawing =
                playerClipDrawingValidator.findActiveDrawing(drawingId);

        playerClipDrawingValidator.findConnectedActivePlayerClip(drawing);

        drawing.setIsDeleted(true);
    }
}