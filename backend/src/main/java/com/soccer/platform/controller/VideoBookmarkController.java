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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.dto.videobookmark.CreateVideoBookmarkRequestDTO;
import com.soccer.platform.dto.videobookmark.UpdateVideoBookmarkRequestDTO;
import com.soccer.platform.dto.videobookmark.VideoBookmarkResponseDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.videobookmark.VideoBookmarkService;

import lombok.RequiredArgsConstructor;

/*
 * 영상 북마크 Controller
 * 지도자와 분석관이 경기 원본 영상, 팀 분석 클립,
 * 선수 개인 분석 클립에서 개인 북마크를 관리하는 API를 제공
 */
@RestController
@RequiredArgsConstructor
public class VideoBookmarkController {

    private final VideoBookmarkService videoBookmarkService;

    // 현재 재생 중인 영상에 개인 북마크를 등록
    @PostMapping("/api/management/video-bookmarks")
    public ResponseEntity createVideoBookmark(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody CreateVideoBookmarkRequestDTO request
    ) {
        VideoBookmarkResponseDTO response =
            videoBookmarkService.createVideoBookmark(
                request,
                principal
            );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    /*
     * 현재 재생 중인 영상에서 작성한 본인의 북마크 목록을 조회
     * teamClipId와 playerClipId가 모두 없으면
     * 경기 원본 영상 북마크를 조회
     */
    @GetMapping("/api/management/video-bookmarks")
    public ResponseEntity findMyVideoBookmarks(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestParam(name = "matchVideoId")
        Integer matchVideoId,
        @RequestParam(
            name = "teamClipId",
            required = false
        )
        Integer teamClipId,
        @RequestParam(
            name = "playerClipId",
            required = false
        )
        Integer playerClipId
    ) {
        List<VideoBookmarkResponseDTO> response =
            videoBookmarkService.findMyVideoBookmarks(
                matchVideoId,
                teamClipId,
                playerClipId,
                principal
            );

        return ResponseEntity.ok(response);
    }

    // 작성자 본인의 북마크 제목, 메모, 시간을 수정
    @PatchMapping(
        "/api/management/video-bookmarks/{bookmarkId}"
    )
    public ResponseEntity updateVideoBookmark(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable(name = "bookmarkId")
        Integer bookmarkId,
        @RequestBody UpdateVideoBookmarkRequestDTO request
    ) {
        VideoBookmarkResponseDTO response =
            videoBookmarkService.updateVideoBookmark(
                bookmarkId,
                request,
                principal
            );

        return ResponseEntity.ok(response);
    }

    /*
     * 작성자 본인의 북마크를 소프트 삭제
     * ANALYST도 본인이 작성한 북마크는 삭제할 수 있으므로
     * 지도자 전용 경로가 아닌 관리용 경로를 사용
     */
    @DeleteMapping(
        "/api/management/video-bookmarks/{bookmarkId}"
    )
    public ResponseEntity deleteVideoBookmark(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable(name = "bookmarkId")
        Integer bookmarkId
    ) {
        videoBookmarkService.deleteVideoBookmark(
            bookmarkId,
            principal
        );

        return ResponseEntity.noContent().build();
    }
}