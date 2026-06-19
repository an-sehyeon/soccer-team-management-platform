package com.soccer.platform.controller;

import org.springframework.http.HttpStatus;
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

import com.soccer.platform.dto.playerrecord.CreatePlayerRecordRequestDTO;
import com.soccer.platform.dto.playerrecord.CreatePlayerRecordResponseDTO;
import com.soccer.platform.dto.playerrecord.PlayerRecordDetailResponseDTO;
import com.soccer.platform.dto.playerrecord.PlayerRecordPageResponseDTO;
import com.soccer.platform.dto.playerrecord.UpdatePlayerRecordRequestDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.PlayerRecordService;

import lombok.RequiredArgsConstructor;

// 선수 기록 관리 Controller

@RestController
@RequiredArgsConstructor
public class PlayerRecordController {

    private final PlayerRecordService playerRecordService;

    // 관리용 선수 기록 등록
    @PostMapping("/api/management/player-records")
    public ResponseEntity<CreatePlayerRecordResponseDTO> createPlayerRecord(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody CreatePlayerRecordRequestDTO request
    ) {
        CreatePlayerRecordResponseDTO response = playerRecordService.createPlayerRecord(
                principal,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 관리용 선수 기록 목록 조회
    @GetMapping("/api/management/player-records")
    public ResponseEntity<PlayerRecordPageResponseDTO> findManagementPlayerRecords(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(name = "uploadId", required = false) Integer uploadId,
            @RequestParam(name = "playerId", required = false) Integer playerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        PlayerRecordPageResponseDTO response = playerRecordService.findManagementPlayerRecords(
                principal,
                uploadId,
                playerId,
                page,
                size
        );

        return ResponseEntity.ok(response);
    }

    // 관리용 선수 기록 상세 조회
    @GetMapping("/api/management/player-records/{recordId}")
    public ResponseEntity<PlayerRecordDetailResponseDTO> findManagementPlayerRecordDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("recordId") Integer recordId
    ) {
        PlayerRecordDetailResponseDTO response = playerRecordService.findManagementPlayerRecordDetail(
                principal,
                recordId
        );

        return ResponseEntity.ok(response);
    }

    // 관리용 선수 기록 수정
    @PatchMapping("/api/management/player-records/{recordId}")
    public ResponseEntity<PlayerRecordDetailResponseDTO> updatePlayerRecord(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("recordId") Integer recordId,
            @RequestBody UpdatePlayerRecordRequestDTO request
    ) {
        PlayerRecordDetailResponseDTO response = playerRecordService.updatePlayerRecord(
                principal,
                recordId,
                request
        );

        return ResponseEntity.ok(response);
    }

    // 관리용 선수 기록 삭제
    @DeleteMapping("/api/management/player-records/{recordId}")
    public ResponseEntity<Void> deletePlayerRecord(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("recordId") Integer recordId
    ) {
        playerRecordService.deletePlayerRecord(
                principal,
                recordId
        );

        return ResponseEntity.noContent().build();
    }

    // 선수 본인 기록 목록 조회
    @GetMapping("/api/player/me/player-records")
    public ResponseEntity<PlayerRecordPageResponseDTO> findMyPlayerRecords(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        PlayerRecordPageResponseDTO response = playerRecordService.findMyPlayerRecords(
                principal,
                page,
                size
        );

        return ResponseEntity.ok(response);
    }

    // 선수 본인 기록 상세 조회
    @GetMapping("/api/player/me/player-records/{recordId}")
    public ResponseEntity<PlayerRecordDetailResponseDTO> findMyPlayerRecordDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("recordId") Integer recordId
    ) {
        PlayerRecordDetailResponseDTO response = playerRecordService.findMyPlayerRecordDetail(
                principal,
                recordId
        );

        return ResponseEntity.ok(response);
    }
}