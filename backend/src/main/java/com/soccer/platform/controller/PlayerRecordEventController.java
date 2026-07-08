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
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventRequestDTO;
import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventResponseDTO;
import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventWithClipLinkRequestDTO;
import com.soccer.platform.dto.playerrecordevent.PlayerRecordEventListResponseDTO;
import com.soccer.platform.dto.playerrecordevent.PlayerRecordEventResponseDTO;
import com.soccer.platform.dto.playerrecordevent.UpdatePlayerRecordEventRequestDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.playerrecordevent.PlayerRecordEventService;

import lombok.RequiredArgsConstructor;

// 선수 기록 이벤트 Controller
// 경기별 선수 기록에 연결되는 개별 이벤트와 선택 클립 연결 API를 관리
@RestController
@RequiredArgsConstructor
public class PlayerRecordEventController {

    private final PlayerRecordEventService playerRecordEventService;

    // 관리용 선수 기록 이벤트 등록
    @PostMapping("/api/management/player-record-events")
    public ResponseEntity createPlayerRecordEvent(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody CreatePlayerRecordEventRequestDTO request
    ) {
        CreatePlayerRecordEventResponseDTO response = playerRecordEventService.createPlayerRecordEvent(
            principal,
            request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 관리용 선수 기록 이벤트 + 클립 연결 등록
    @PostMapping("/api/management/player-record-events/with-clip-link")
    public ResponseEntity createPlayerRecordEventWithClipLink(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody CreatePlayerRecordEventWithClipLinkRequestDTO request
    ) {
        CreatePlayerRecordEventResponseDTO response = playerRecordEventService.createPlayerRecordEventWithClipLink(
            principal,
            request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 관리용 특정 선수 기록의 이벤트 목록 조회
    @GetMapping("/api/management/player-records/{recordId}/events")
    public ResponseEntity findPlayerRecordEventsForManagement(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable("recordId") Integer recordId
    ) {
        PlayerRecordEventListResponseDTO response = playerRecordEventService.findPlayerRecordEventsForManagement(
            principal,
            recordId
        );

        return ResponseEntity.ok(response);
    }

    // 관리용 선수 기록 이벤트 상세 조회
    @GetMapping("/api/management/player-record-events/{eventId}")
    public ResponseEntity findPlayerRecordEventDetailForManagement(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable("eventId") Integer eventId
    ) {
        PlayerRecordEventResponseDTO response = playerRecordEventService.findPlayerRecordEventDetailForManagement(
            principal,
            eventId
        );

        return ResponseEntity.ok(response);
    }

    // 관리용 선수 기록 이벤트 수정
    @PatchMapping("/api/management/player-record-events/{eventId}")
    public ResponseEntity updatePlayerRecordEvent(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable("eventId") Integer eventId,
        @RequestBody UpdatePlayerRecordEventRequestDTO request
    ) {
        PlayerRecordEventResponseDTO response = playerRecordEventService.updatePlayerRecordEvent(
            principal,
            eventId,
            request
        );

        return ResponseEntity.ok(response);
    }

    // 관리용 선수 기록 이벤트 삭제
    @DeleteMapping("/api/management/player-record-events/{eventId}")
    public ResponseEntity deletePlayerRecordEvent(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable("eventId") Integer eventId
    ) {
        playerRecordEventService.deletePlayerRecordEvent(
            principal,
            eventId
        );

        return ResponseEntity.noContent().build();
    }

    // 선수 본인 특정 기록의 이벤트 목록 조회
    @GetMapping("/api/player/me/player-records/{recordId}/events")
    public ResponseEntity findMyPlayerRecordEvents(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable("recordId") Integer recordId
    ) {
        PlayerRecordEventListResponseDTO response = playerRecordEventService.findMyPlayerRecordEvents(
            principal,
            recordId
        );

        return ResponseEntity.ok(response);
    }

    // 선수 본인 기록 이벤트 상세 조회
    @GetMapping("/api/player/me/player-record-events/{eventId}")
    public ResponseEntity findMyPlayerRecordEventDetail(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable("eventId") Integer eventId
    ) {
        PlayerRecordEventResponseDTO response = playerRecordEventService.findMyPlayerRecordEventDetail(
            principal,
            eventId
        );

        return ResponseEntity.ok(response);
    }
}