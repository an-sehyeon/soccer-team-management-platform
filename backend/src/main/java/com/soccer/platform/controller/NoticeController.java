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

import com.soccer.platform.dto.notice.CreateNoticeRequestDTO;
import com.soccer.platform.dto.notice.NoticeDetailResponseDTO;
import com.soccer.platform.dto.notice.NoticePageResponseDTO;
import com.soccer.platform.dto.notice.UpdateNoticeRequestDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.NoticeService;

import lombok.RequiredArgsConstructor;

/*
 * 공지사항 Controller
 * 공지사항 등록, 조회, 수정, 삭제 API를 제공한다.
 */
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /*
     * 공지사항 목록 조회
     * 요청 예시
     * GET /api/notices?page=0&size=10&importantOnly=false
     */
    @GetMapping("/api/notices")
    public ResponseEntity<NoticePageResponseDTO> findNoticePage(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "importantOnly", defaultValue = "false") boolean importantOnly
    ) {
        NoticePageResponseDTO response = noticeService.findNoticePage(
            page,
            size,
            importantOnly
        );

        return ResponseEntity.ok(response);
    }

    /*
     * 공지사항 상세 조회
     * 요청 예시
     * GET /api/notices/1
     */
    @GetMapping("/api/notices/{noticeId}")
    public ResponseEntity<NoticeDetailResponseDTO> findNoticeDetail(
        @PathVariable(name = "noticeId") Integer noticeId
    ) {
        NoticeDetailResponseDTO response = noticeService.findNoticeDetail(noticeId);

        return ResponseEntity.ok(response);
    }

    /*
     * 공지사항 등록
     * 요청 예시
     * POST /api/coach/notices
     */
    @PostMapping("/api/coach/notices")
    public ResponseEntity<NoticeDetailResponseDTO> createNotice(
        @AuthenticationPrincipal CustomUserPrincipal loginUser,
        @RequestBody CreateNoticeRequestDTO request
    ) {
        NoticeDetailResponseDTO response = noticeService.createNotice(
            loginUser,
            request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * 공지사항 수정
     * 요청 예시
     * PATCH /api/coach/notices/1
     */
    @PatchMapping("/api/coach/notices/{noticeId}")
    public ResponseEntity<NoticeDetailResponseDTO> updateNotice(
        @AuthenticationPrincipal CustomUserPrincipal loginUser,
        @PathVariable(name = "noticeId") Integer noticeId,
        @RequestBody UpdateNoticeRequestDTO request
    ) {
        NoticeDetailResponseDTO response = noticeService.updateNotice(
            loginUser,
            noticeId,
            request
        );

        return ResponseEntity.ok(response);
    }

    /*
     * 공지사항 삭제
     * 요청 예시
     * DELETE /api/coach/notices/1
     */
    @DeleteMapping("/api/coach/notices/{noticeId}")
    public ResponseEntity<Void> deleteNotice(
        @AuthenticationPrincipal CustomUserPrincipal loginUser,
        @PathVariable(name = "noticeId") Integer noticeId
    ) {
        noticeService.deleteNotice(loginUser, noticeId);

        return ResponseEntity.noContent().build();
    }
}