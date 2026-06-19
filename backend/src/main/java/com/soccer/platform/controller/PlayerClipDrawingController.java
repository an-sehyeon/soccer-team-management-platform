package com.soccer.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.dto.playerclipdrawing.CreatePlayerClipDrawingRequestDTO;
import com.soccer.platform.dto.playerclipdrawing.CreatePlayerClipDrawingResponseDTO;
import com.soccer.platform.dto.playerclipdrawing.PlayerClipDrawingResponseDTO;
import com.soccer.platform.dto.playerclipdrawing.UpdatePlayerClipDrawingRequestDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.PlayerClipDrawingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


// 선수 개인 분석 클립 드로잉 Controller
// 선수 개인 분석 클립에 연결된 드로잉 등록, 조회, 수정, 삭제API 제공

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
public class PlayerClipDrawingController {
	
	private final PlayerClipDrawingService playerClipDrawingService;
	
	// 선수 분석 클립 드로잉 목록 조회
	@GetMapping("/player-analysis-clips/{playerClipId}/drawings")
	public ResponseEntity<List<PlayerClipDrawingResponseDTO>> findDrawings(
			@PathVariable("playerClipId") Integer playerCliipId,
			@AuthenticationPrincipal CustomUserPrincipal principal
	){
		List<PlayerClipDrawingResponseDTO> response = playerClipDrawingService.findDrawings(playerCliipId, principal);
		
		return ResponseEntity.ok(response);
	}
	
	// 선수 분석 클립 드로잉 상세 조회
	@GetMapping("/player-analysis-clip-drawings/{drawingId}")
	public ResponseEntity<PlayerClipDrawingResponseDTO> findDrawingDetail(
			@PathVariable("drawingId") Integer drawingId,
			@AuthenticationPrincipal CustomUserPrincipal principal
	){
		PlayerClipDrawingResponseDTO response = playerClipDrawingService.findDrawingDetail(drawingId, principal);
		
		return ResponseEntity.ok(response);
	}
	
	
	// 선수 분석 클립 드로잉 등록
	@PostMapping("/management/player-analysis-clips/{playerClipId}/drawings")
	public ResponseEntity<CreatePlayerClipDrawingResponseDTO>createDrawing(
			@PathVariable("playerClipId") Integer playClipId,
			@Valid @RequestBody CreatePlayerClipDrawingRequestDTO request,
			@AuthenticationPrincipal CustomUserPrincipal principal
	){
		CreatePlayerClipDrawingResponseDTO response = playerClipDrawingService.createDrawing(playClipId, request, principal);
		
		return ResponseEntity.ok(response);
	}
	
	// 선수 분석 클립 드로잉 수정
	@PatchMapping("/management/player-analysis-clip-drawings/{drawingId}")
    public ResponseEntity<PlayerClipDrawingResponseDTO> updateDrawing(
            @PathVariable("drawingId") Integer drawingId,
            @Valid @RequestBody UpdatePlayerClipDrawingRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        PlayerClipDrawingResponseDTO response =
                playerClipDrawingService.updateDrawing(drawingId, request, principal);

        return ResponseEntity.ok(response);
    }


	// 선수 분석 클립 드로잉 삭제
    @DeleteMapping("/coach/player-analysis-clip-drawings/{drawingId}")
    public ResponseEntity<Void> deleteDrawing(
            @PathVariable("drawingId") Integer drawingId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        playerClipDrawingService.deleteDrawing(drawingId, principal);

        return ResponseEntity.noContent().build();
    }
	

}
