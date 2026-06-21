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

import com.soccer.platform.dto.matchvideo.CreateMatchVideoRequestDTO;
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
    @PostMapping("/api/management/match-videos")
    public ResponseEntity<Integer> createMatchVideo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody CreateMatchVideoRequestDTO request
    ) {
        Integer matchVideoId = matchVideoService.createMatchVideo(
                principal,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(matchVideoId);
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