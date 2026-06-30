package com.soccer.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.dto.member.PlayerSelectResponseDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.member.PlayerQueryService;

import lombok.RequiredArgsConstructor;

// 선수 개인 분석 클립 관리 화면용 선수 조회 컨트롤러
// 드롭다운용 선수 목록 제공

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/management/players")
public class ManagementPlayerController {
	
	private final PlayerQueryService playerQueryService;

    // 선수 선택 목록 조회
    // COACH, ANALYST만 사용 가능
    @GetMapping
    public ResponseEntity<List<PlayerSelectResponseDTO>> getPlayers(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<PlayerSelectResponseDTO> response = playerQueryService.findPlayersForManagement(principal);
        return ResponseEntity.ok(response);
    }
}