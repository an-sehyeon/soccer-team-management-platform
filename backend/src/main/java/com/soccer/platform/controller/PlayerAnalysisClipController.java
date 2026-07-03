package com.soccer.platform.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.common.constants.PlayerClipTypeEnum;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipRequestDTO;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipResponseDTO;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipWithDrawingsRequestDTO;
import com.soccer.platform.dto.playerclip.CreatePlayerAnalysisClipWithDrawingsResponseDTO;
import com.soccer.platform.dto.playerclip.PlayerAnalysisClipDetailResponseDTO;
import com.soccer.platform.dto.playerclip.PlayerAnalysisClipPageResponseDTO;
import com.soccer.platform.dto.playerclip.UpdatePlayerAnalysisClipRequestDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.playerclip.PlayerAnalysisClipService;

import lombok.RequiredArgsConstructor;

// 선수 개인 분석 클립 Controller
// 선수 개인 분석 클립 등록, 조회, 수정, 삭제 API
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlayerAnalysisClipController {

	private final PlayerAnalysisClipService playerAnalysisClipService;

	// 관리용 선수 개인 분석 클립 목록 조회
	@GetMapping("/management/player-analysis-clips")
	public ResponseEntity<PlayerAnalysisClipPageResponseDTO> findPlayerAnalysisClipsForManagement(
			@RequestParam(name = "matchVideoId", required = false) Integer matchVideoId,
			@RequestParam(name = "playerId", required = false) Integer playerId,
			@RequestParam(name = "clipType", required = false) PlayerClipTypeEnum clipType,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		Pageable pageable = PageRequest.of(page, size);

		PlayerAnalysisClipPageResponseDTO response = playerAnalysisClipService.findPlayerAnalysisClipsForManagement(
				matchVideoId,
				playerId,
				clipType,
				pageable,
				principal
		);

		return ResponseEntity.ok(response);
	}

	// 관리용 선수 개인 분석 클립 상세 조회
	@GetMapping("/management/player-analysis-clips/{playerClipId}")
	public ResponseEntity<PlayerAnalysisClipDetailResponseDTO> findPlayerAnalysisClipDetailForManagement(
			@PathVariable(name = "playerClipId") Integer playerClipId,
			@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		PlayerAnalysisClipDetailResponseDTO response = playerAnalysisClipService.findPlayerAnalysisClipDetailForManagement(
				playerClipId,
				principal
		);

		return ResponseEntity.ok(response);
	}

	// 선수 개인 분석 클립 등록
	@PostMapping("/management/player-analysis-clips")
	public ResponseEntity<CreatePlayerAnalysisClipResponseDTO> createPlayerAnalysisClip(
			@RequestBody CreatePlayerAnalysisClipRequestDTO request,
			@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		CreatePlayerAnalysisClipResponseDTO response = playerAnalysisClipService.createPlayerAnalysisClip(
				request,
				principal
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 선수 개인 분석 클립과 드로잉 통합 등록
	@PostMapping("/management/player-analysis-clips/with-drawings")
	public ResponseEntity<CreatePlayerAnalysisClipWithDrawingsResponseDTO> createPlayerAnalysisClipWithDrawings(
			@RequestBody CreatePlayerAnalysisClipWithDrawingsRequestDTO request,
			@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		CreatePlayerAnalysisClipWithDrawingsResponseDTO response = playerAnalysisClipService.createPlayerAnalysisClipWithDrawings(
				request,
				principal
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 선수 개인 분석 클립 수정
	@PatchMapping("/management/player-analysis-clips/{playerClipId}")
	public ResponseEntity<PlayerAnalysisClipDetailResponseDTO> updatePlayerAnalysisClip(
			@PathVariable(name = "playerClipId") Integer playerClipId,
			@RequestBody UpdatePlayerAnalysisClipRequestDTO request,
			@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		PlayerAnalysisClipDetailResponseDTO response = playerAnalysisClipService.updatePlayerAnalysisClip(
				playerClipId,
				request,
				principal
		);

		return ResponseEntity.ok(response);
	}

	// 선수 개인 분석 클립 삭제
	@DeleteMapping("/coach/player-analysis-clips/{playerClipId}")
	public ResponseEntity<Void> deletePlayerAnalysisClip(
			@PathVariable(name = "playerClipId") Integer playerClipId,
			@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		playerAnalysisClipService.deletePlayerAnalysisClip(playerClipId, principal);

		return ResponseEntity.noContent().build();
	}

	// 선수 본인 개인 분석 클립 목록 조회
	@GetMapping("/player/me/player-analysis-clips")
	public ResponseEntity<PlayerAnalysisClipPageResponseDTO> findMyPlayerAnalysisClips(
			@RequestParam(name = "matchVideoId", required = false) Integer matchVideoId,
			@RequestParam(name = "clipType", required = false) PlayerClipTypeEnum clipType,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		Pageable pageable = PageRequest.of(page, size);

		PlayerAnalysisClipPageResponseDTO response = playerAnalysisClipService.findMyPlayerAnalysisClips(
				matchVideoId,
				clipType,
				pageable,
				principal
		);

		return ResponseEntity.ok(response);
	}

	// 선수 본인 개인 분석 클립 상세 조회
	@GetMapping("/player/me/player-analysis-clips/{playerClipId}")
	public ResponseEntity<PlayerAnalysisClipDetailResponseDTO> findMyPlayerAnalysisClipDetail(
			@PathVariable(name = "playerClipId") Integer playerClipId,
			@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		PlayerAnalysisClipDetailResponseDTO response = playerAnalysisClipService.findMyPlayerAnalysisClipDetail(
				playerClipId,
				principal
		);

		return ResponseEntity.ok(response);
	}

}