package com.soccer.platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.dto.playerclipview.PlayerClipViewPageResponseDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.playerclipview.PlayerAnalysisClipViewService;

import lombok.RequiredArgsConstructor;

// 선수 개인 분석 클립 조회 기록 Controller
// 지도자/분석관의 선수별 조회 기록 확인과 선수 본인의 조회 기록 확인 API를 제공.

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlayerAnalysisClipViewController {

    private final PlayerAnalysisClipViewService playerAnalysisClipViewService;

    // 관리용 특정 선수의 개인 분석 클립 조회 기록 목록 조회
    // COACH, ANALYST가 특정 선수의 개인 분석 클립 조회 기록을 확인할 때 사용
    @GetMapping("/management/players/{playerId}/player-analysis-clip-views")
    public ResponseEntity<PlayerClipViewPageResponseDTO> findPlayerViewHistoriesForManagement(
        @PathVariable(name = "playerId") Integer playerId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        PlayerClipViewPageResponseDTO response =
            playerAnalysisClipViewService.findPlayerViewHistoriesForManagement(
                playerId,
                page,
                size,
                principal
            );

        return ResponseEntity.ok(response);
    }

    // 선수 본인 개인 분석 클립 조회 기록 목록 조회
    @GetMapping("/player/me/player-analysis-clip-views")
    public ResponseEntity<PlayerClipViewPageResponseDTO> findMyViewHistories(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        PlayerClipViewPageResponseDTO response =
            playerAnalysisClipViewService.findMyViewHistories(
                page,
                size,
                principal
            );

        return ResponseEntity.ok(response);
    }
}