package com.soccer.platform.controller;

import org.springframework.http.HttpStatus;import org.springframework.web.bind.annotation.PutMapping;

import com.soccer.platform.dto.teamanalysisclip.UpdateTeamAnalysisClipWithDrawingsRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.UpdateTeamAnalysisClipWithDrawingsResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipWithDrawingsRequestDTO;
import com.soccer.platform.dto.teamanalysisclip.CreateTeamAnalysisClipWithDrawingsResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipDetailResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.TeamAnalysisClipPageResponseDTO;
import com.soccer.platform.dto.teamanalysisclip.UpdateTeamAnalysisClipRequestDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.teamclip.TeamAnalysisClipService;

import lombok.RequiredArgsConstructor;

/*
 * 팀 분석 클립 Controller
 * 팀 분석 클립 등록, 조회, 수정, 삭제 API를 제공
 */

@RestController
@RequiredArgsConstructor
public class TeamAnalysisClipController {

    private final TeamAnalysisClipService teamAnalysisClipService;

 // 팀 분석 클립 목록 조회
    @GetMapping("/api/team-analysis-clips")
    public ResponseEntity<TeamAnalysisClipPageResponseDTO> findTeamAnalysisClips(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(name = "matchVideoId", required = false) Integer matchVideoId,
            @RequestParam(name = "clipType", required = false) String clipType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        TeamAnalysisClipPageResponseDTO response = teamAnalysisClipService.findTeamAnalysisClips(
                principal,
                matchVideoId,
                clipType,
                page,
                size
        );

        return ResponseEntity.ok(response);
    }

    // 팀 분석 클립 상세 조회
    @GetMapping("/api/team-analysis-clips/{teamClipId}")
    public ResponseEntity<TeamAnalysisClipDetailResponseDTO> findTeamAnalysisClipDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable(name = "teamClipId") Integer teamClipId
    ) {
        TeamAnalysisClipDetailResponseDTO response = teamAnalysisClipService.findTeamAnalysisClipDetail(
                principal,
                teamClipId
        );

        return ResponseEntity.ok(response);
    }

    // 팀 분석 클립 등록
    @PostMapping("/api/management/team-analysis-clips")
    public ResponseEntity<CreateTeamAnalysisClipResponseDTO> createTeamAnalysisClip(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody CreateTeamAnalysisClipRequestDTO request
    ) {
        CreateTeamAnalysisClipResponseDTO response = teamAnalysisClipService.createTeamAnalysisClip(
                principal,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 팀 분석 클립과 드로잉 통합 등록
    @PostMapping("/api/management/team-analysis-clips/with-drawings")
    public ResponseEntity<CreateTeamAnalysisClipWithDrawingsResponseDTO> createTeamAnalysisClipWithDrawings(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody CreateTeamAnalysisClipWithDrawingsRequestDTO request
    ) {
        CreateTeamAnalysisClipWithDrawingsResponseDTO response = teamAnalysisClipService
                .createTeamAnalysisClipWithDrawings(principal, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 팀 분석 클립 수정
    @PatchMapping("/api/management/team-analysis-clips/{teamClipId}")
    public ResponseEntity<TeamAnalysisClipDetailResponseDTO> updateTeamAnalysisClip(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable(name = "teamClipId") Integer teamClipId,
            @RequestBody UpdateTeamAnalysisClipRequestDTO request
    ) {
        TeamAnalysisClipDetailResponseDTO response = teamAnalysisClipService.updateTeamAnalysisClip(
                principal,
                teamClipId,
                request
        );

        return ResponseEntity.ok(response);
    }
    
    // 팀 분석 클립과 드로잉 통합 수정
    @PutMapping("/api/management/team-analysis-clips/{teamClipId}/with-drawings")
    public ResponseEntity<UpdateTeamAnalysisClipWithDrawingsResponseDTO> updateTeamAnalysisClipWithDrawings(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable(name = "teamClipId") Integer teamClipId,
            @RequestBody UpdateTeamAnalysisClipWithDrawingsRequestDTO request
    ) {
        UpdateTeamAnalysisClipWithDrawingsResponseDTO response = teamAnalysisClipService
                .updateTeamAnalysisClipWithDrawings(
                        principal,
                        teamClipId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    // 팀 분석 클립 삭제
    @DeleteMapping("/api/coach/team-analysis-clips/{teamClipId}")
    public ResponseEntity<Void> deleteTeamAnalysisClip(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable(name = "teamClipId") Integer teamClipId
    ) {
        teamAnalysisClipService.deleteTeamAnalysisClip(principal, teamClipId);

        return ResponseEntity.noContent().build();
    }
}