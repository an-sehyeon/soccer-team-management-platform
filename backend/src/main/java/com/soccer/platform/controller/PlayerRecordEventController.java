package com.soccer.platform.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventResponseDTO;
import com.soccer.platform.dto.playerrecordevent.CreatePlayerRecordEventWithClipLinkRequestDTO;
import com.soccer.platform.dto.playerrecordevent.PlayerRecordEventListResponseDTO;
import com.soccer.platform.dto.playerrecordevent.PlayerRecordEventResponseDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.playerrecordevent.PlayerRecordEventService;

import lombok.RequiredArgsConstructor;

// 선수 기록 이벤트 Controller
// 분석 클립 연결 이벤트 등록과 관리용·선수 본인 조회 API를 제공한다.
@RestController
@RequiredArgsConstructor
public class PlayerRecordEventController {

    private final PlayerRecordEventService playerRecordEventService;

    /*
     * 관리용 선수 기록 이벤트와 분석 클립 연결 등록
     *
     * COACH와 ANALYST만 사용할 수 있다.
     * 이벤트 시간과 value는 요청받지 않고 선택한 클립을 기준으로 결정한다.
     */
    @PostMapping(
            "/api/management/player-record-events/with-clip-link"
    )
    public ResponseEntity<CreatePlayerRecordEventResponseDTO>
            createPlayerRecordEventWithClipLink(
                    @AuthenticationPrincipal
                    CustomUserPrincipal principal,

                    @RequestBody
                    CreatePlayerRecordEventWithClipLinkRequestDTO request
            ) {
        CreatePlayerRecordEventResponseDTO response =
                playerRecordEventService
                        .createPlayerRecordEventWithClipLink(
                                principal,
                                request
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 관리용 특정 선수 기록의 이벤트 목록 조회
    @GetMapping(
            "/api/management/player-records/{recordId}/events"
    )
    public ResponseEntity<PlayerRecordEventListResponseDTO>
            findPlayerRecordEventsForManagement(
                    @AuthenticationPrincipal
                    CustomUserPrincipal principal,

                    @PathVariable("recordId")
                    Integer recordId
            ) {
        PlayerRecordEventListResponseDTO response =
                playerRecordEventService
                        .findPlayerRecordEventsForManagement(
                                principal,
                                recordId
                        );

        return ResponseEntity.ok(response);
    }

    // 관리용 선수 기록 이벤트 상세 조회
    @GetMapping(
            "/api/management/player-record-events/{eventId}"
    )
    public ResponseEntity<PlayerRecordEventResponseDTO>
            findPlayerRecordEventDetailForManagement(
                    @AuthenticationPrincipal
                    CustomUserPrincipal principal,

                    @PathVariable("eventId")
                    Integer eventId
            ) {
        PlayerRecordEventResponseDTO response =
                playerRecordEventService
                        .findPlayerRecordEventDetailForManagement(
                                principal,
                                eventId
                        );

        return ResponseEntity.ok(response);
    }

    // 선수 본인 특정 기록의 이벤트 목록 조회
    @GetMapping(
            "/api/player/me/player-records/{recordId}/events"
    )
    public ResponseEntity<PlayerRecordEventListResponseDTO>
            findMyPlayerRecordEvents(
                    @AuthenticationPrincipal
                    CustomUserPrincipal principal,

                    @PathVariable("recordId")
                    Integer recordId
            ) {
        PlayerRecordEventListResponseDTO response =
                playerRecordEventService
                        .findMyPlayerRecordEvents(
                                principal,
                                recordId
                        );

        return ResponseEntity.ok(response);
    }

    // 선수 본인 기록 이벤트 상세 조회
    @GetMapping(
            "/api/player/me/player-record-events/{eventId}"
    )
    public ResponseEntity<PlayerRecordEventResponseDTO>
            findMyPlayerRecordEventDetail(
                    @AuthenticationPrincipal
                    CustomUserPrincipal principal,

                    @PathVariable("eventId")
                    Integer eventId
            ) {
        PlayerRecordEventResponseDTO response =
                playerRecordEventService
                        .findMyPlayerRecordEventDetail(
                                principal,
                                eventId
                        );

        return ResponseEntity.ok(response);
    }
}