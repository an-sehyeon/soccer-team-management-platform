package com.soccer.platform.service.notice;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.notice.CreateNoticeRequestDTO;
import com.soccer.platform.dto.notice.NoticeDetailResponseDTO;
import com.soccer.platform.dto.notice.NoticePageResponseDTO;
import com.soccer.platform.dto.notice.UpdateNoticeRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.NoticeEntity;
import com.soccer.platform.repository.NoticeRepository;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.MemberQueryService;
import com.soccer.platform.service.common.PageRequestValidator;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;

/**
 * 공지사항 관리 Service
 *
 * 공지사항 등록, 조회, 수정, 삭제 기능을 처리
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final PermissionValidator permissionValidator;
    private final MemberQueryService memberQueryService;
    private final PageRequestValidator pageRequestValidator;
    private final NoticeValidator noticeValidator;

    // 공지사항 등록
    public NoticeDetailResponseDTO createNotice(
            CustomUserPrincipal principal,
            CreateNoticeRequestDTO request
    ) {
        permissionValidator.requireCoach(
                principal,
                ErrorCode.ACCESS_DENIED
        );

        noticeValidator.validateCreateRequest(request);

        MemberEntity writer = memberQueryService.findLoginMember(
                principal,
                ErrorCode.MEMBER_NOT_FOUND
        );

        NoticeEntity notice = new NoticeEntity();
        notice.setMember(writer);
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setIsImportant(request.getIsImportant());
        notice.setIsDeleted(false);
        
        NoticeEntity savedNotice = noticeRepository.save(notice);

        return toDetailResponseDTO(savedNotice);
    }

    // 공지사항 목록 조회
    @Transactional(readOnly = true)
    public NoticePageResponseDTO findNotices(
            CustomUserPrincipal principal,
            Integer page,
            Integer size,
            Boolean importantOnly
    ) {
        permissionValidator.requireAuthenticatedServiceUser(
                principal,
                ErrorCode.ACCESS_DENIED
        );

        Pageable pageable = pageRequestValidator.createPageable(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("isImportant"),
                        Sort.Order.desc("createdAt")
                )
        );

        Page<NoticeEntity> noticePage;

        if (Boolean.TRUE.equals(importantOnly)) {
            noticePage = noticeRepository.findByIsImportantTrueAndIsDeletedFalse(pageable);
        } else {
            noticePage = noticeRepository.findByIsDeletedFalse(pageable);
        }

        return toPageResponseDTO(noticePage);
    }

    // 공지사항 상세 조회
    @Transactional(readOnly = true)
    public NoticeDetailResponseDTO findNoticeDetail(
            CustomUserPrincipal principal,
            Integer noticeId
    ) {
        permissionValidator.requireAuthenticatedServiceUser(
                principal,
                ErrorCode.ACCESS_DENIED
        );

        NoticeEntity notice = findActiveNotice(noticeId);

        return toDetailResponseDTO(notice);
    }

    // 공지사항 수정
    public NoticeDetailResponseDTO updateNotice(
            CustomUserPrincipal principal,
            Integer noticeId,
            UpdateNoticeRequestDTO request
    ) {
        permissionValidator.requireCoach(
                principal,
                ErrorCode.ACCESS_DENIED
        );

        noticeValidator.validateUpdateRequest(request);

        NoticeEntity notice = findActiveNotice(noticeId);

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setIsImportant(request.getIsImportant());

        return toDetailResponseDTO(notice);
    }

    // 공지사항 삭제
    public void deleteNotice(
            CustomUserPrincipal principal,
            Integer noticeId
    ) {
        permissionValidator.requireCoach(
                principal,
                ErrorCode.ACCESS_DENIED
        );

        NoticeEntity notice = findActiveNotice(noticeId);

        notice.setIsDeleted(true);
    }

    // 삭제되지 않은 공지사항 조회
    private NoticeEntity findActiveNotice(Integer noticeId) {
        if (noticeId == null) {
            throw new CustomException(ErrorCode.NOTICE_NOT_FOUND);
        }

        return noticeRepository.findByIdAndIsDeletedFalse(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
    }

    // 공지사항 상세 응답 변환
    private NoticeDetailResponseDTO toDetailResponseDTO(NoticeEntity notice) {

        return NoticeDetailResponseDTO.from(notice);
    }


    // 공지사항 페이지 응답 변환
    private NoticePageResponseDTO toPageResponseDTO(Page<NoticeEntity> noticePage) {

        return NoticePageResponseDTO.from(noticePage);
    }
}