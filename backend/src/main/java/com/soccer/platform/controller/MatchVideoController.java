package com.soccer.platform.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.soccer.platform.common.constants.MatchResultEnum;
import com.soccer.platform.dto.matchvideo.CreateMatchVideoRequestDTO;
import com.soccer.platform.dto.matchvideo.CreateMatchVideoResponseDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoDetailResponseDTO;
import com.soccer.platform.dto.matchvideo.MatchVideoPageResponseDTO;
import com.soccer.platform.dto.matchvideo.UpdateMatchVideoRequestDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.matchvideo.MatchVideoService;

import lombok.RequiredArgsConstructor;

/*
 * 경기 영상 Controller
 * 경기 원본 영상 등록, 조회, 수정, 삭제 API를 제공
 */
@RestController
@RequiredArgsConstructor
public class MatchVideoController {

    private final MatchVideoService matchVideoService;

    // 경기 영상 목록 조회
    @GetMapping("/api/match-videos")
    public ResponseEntity<MatchVideoPageResponseDTO> findMatchVideos(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        MatchVideoPageResponseDTO response = matchVideoService.findMatchVideos(
                principal,
                page,
                size
        );

        return ResponseEntity.ok(response);
    }

    // 경기 영상 상세 조회
    @GetMapping("/api/match-videos/{matchVideoId}")
    public ResponseEntity<MatchVideoDetailResponseDTO> findMatchVideoDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable(name = "matchVideoId") Integer matchVideoId
    ) {
        MatchVideoDetailResponseDTO response = matchVideoService.findMatchVideoDetail(
                principal,
                matchVideoId
        );

        return ResponseEntity.ok(response);
    }

    // 경기 영상 등록
    @PostMapping(
            value = "/api/management/match-videos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CreateMatchVideoResponseDTO> createMatchVideo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(name = "videoFile") MultipartFile videoFile,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "gameDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime gameDate,
            @RequestParam(name = "place") String place,
            @RequestParam(name = "homeScore") Integer homeScore,
            @RequestParam(name = "awayScore") Integer awayScore,
            @RequestParam(name = "matchResult") MatchResultEnum matchResult
    ) {
        CreateMatchVideoRequestDTO requestDTO = CreateMatchVideoRequestDTO.of(
                title,
                gameDate,
                place,
                homeScore,
                awayScore,
                matchResult
        );

        CreateMatchVideoResponseDTO responseDTO =
                matchVideoService.createMatchVideo(principal, videoFile, requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // 경기 영상 수정
    @PatchMapping("/api/management/match-videos/{matchVideoId}")
    public ResponseEntity<MatchVideoDetailResponseDTO> updateMatchVideo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable(name = "matchVideoId") Integer matchVideoId,
            @RequestBody UpdateMatchVideoRequestDTO request
    ) {
        MatchVideoDetailResponseDTO response = matchVideoService.updateMatchVideo(
                principal,
                matchVideoId,
                request
        );

        return ResponseEntity.ok(response);
    }

    // 경기 영상 삭제
    @DeleteMapping("/api/coach/match-videos/{matchVideoId}")
    public ResponseEntity<Void> deleteMatchVideo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable(name = "matchVideoId") Integer matchVideoId
    ) {
        matchVideoService.deleteMatchVideo(
                principal,
                matchVideoId
        );

        return ResponseEntity.noContent().build();
    }
}