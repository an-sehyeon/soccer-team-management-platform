package com.soccer.platform.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.dto.teamclipdrawing.CreateTeamAnalysisClipDrawingRequestDTO;
import com.soccer.platform.dto.teamclipdrawing.CreateTeamAnalysisClipDrawingResponseDTO;
import com.soccer.platform.dto.teamclipdrawing.TeamAnalysisClipDrawingResponseDTO;
import com.soccer.platform.dto.teamclipdrawing.UpdateTeamAnalysisClipDrawingRequestDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.TeamAnalysisClipDrawingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * 팀 분석 클립 드로잉 Controller
 * 
 * 팀 분석 클립에 연결된 드로잉 등록, 조회, 수정, 삭제 API
 * 
 * 조회 : 지도자, 분석관, 선수 공통
 * 등록/수정 : 지도자, 분석관
 * 삭제 : 지도자
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TeamAnalysisClipDrawingController {

	
	private final TeamAnalysisClipDrawingService teamAnalysisClipDrawingService;
	
	// 팀 분석 클립 드로잉 목록 조회
	@GetMapping("/team-analysis-clips/{teamClipId}/drawings")
    public ResponseEntity<List<TeamAnalysisClipDrawingResponseDTO>> findDrawingsByTeamClip(
            @PathVariable("teamClipId") Integer teamClipId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<TeamAnalysisClipDrawingResponseDTO> response =
                teamAnalysisClipDrawingService.findDrawingsByTeamClip(teamClipId, principal);

        return ResponseEntity.ok(response);
    }
	
	// 팀 분석 클립 드로잉 상세 조회
	@GetMapping("/team-analysis-clip-drawings/{drawingId}")
    public ResponseEntity<TeamAnalysisClipDrawingResponseDTO> findDrawingDetail(
            @PathVariable("drawingId") Integer drawingId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        TeamAnalysisClipDrawingResponseDTO response =
                teamAnalysisClipDrawingService.findDrawingDetail(drawingId, principal);

        return ResponseEntity.ok(response);
    }
	
	// 팀 분석 클립 드로잉 등록
	@PostMapping("/management/team-analysis-clips/{teamClipId}/drawings")
    public ResponseEntity<CreateTeamAnalysisClipDrawingResponseDTO> createDrawing(
            @PathVariable("teamClipId") Integer teamClipId,
            @Valid @RequestBody CreateTeamAnalysisClipDrawingRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CreateTeamAnalysisClipDrawingResponseDTO response =
                teamAnalysisClipDrawingService.createDrawing(teamClipId, request, principal);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
	
	// 팀 분석 클립 드로잉 수정
	@PatchMapping("/management/team-analysis-clip-drawings/{drawingId}")
    public ResponseEntity<TeamAnalysisClipDrawingResponseDTO> updateDrawing(
            @PathVariable("drawingId") Integer drawingId,
            @Valid @RequestBody UpdateTeamAnalysisClipDrawingRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        TeamAnalysisClipDrawingResponseDTO response =
                teamAnalysisClipDrawingService.updateDrawing(drawingId, request, principal);

        return ResponseEntity.ok(response);
    }
	
	// 팀 분석 클립 드로잉 삭제
	@DeleteMapping("/coach/team-analysis-clip-drawings/{drawingId}")
    public ResponseEntity<Void> deleteDrawing(
            @PathVariable("drawingId") Integer drawingId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        teamAnalysisClipDrawingService.deleteDrawing(drawingId, principal);

        return ResponseEntity.noContent().build();
    }
}
