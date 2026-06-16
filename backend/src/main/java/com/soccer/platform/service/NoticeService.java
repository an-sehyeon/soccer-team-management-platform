package com.soccer.platform.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.notice.CreateNoticeRequestDTO;
import com.soccer.platform.dto.notice.NoticeDetailResponseDTO;
import com.soccer.platform.dto.notice.NoticePageResponseDTO;
import com.soccer.platform.dto.notice.UpdateNoticeRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.NoticeEntity;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.repository.NoticeRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

/**
 * 공지사항 관리 Service
 *
 * 공지사항 등록, 조회, 수정, 삭제 기능을 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    // 공지사항 등록
    @Transactional
    public NoticeDetailResponseDTO createNotice(
        CustomUserPrincipal loginUser,
        CreateNoticeRequestDTO request
    ) {
        checkCoachPermission(loginUser, ErrorCode.NOTICE_CREATE_FORBIDDEN);
        validateNoticeRequest(request.getTitle(), request.getContent());

        MemberEntity writer = findLoginMember(loginUser.getMemberId());

        NoticeEntity notice = new NoticeEntity(
            writer,
            request.getTitle().trim(),
            request.getContent().trim(),
            request.getIsImportant()
        );

        NoticeEntity savedNotice = noticeRepository.save(notice);

        return NoticeDetailResponseDTO.from(savedNotice);
    }

    // 공지사항 목록 조회
    public NoticePageResponseDTO findNoticePage(
        int page,
        int size,
        boolean importantOnly
    ) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Order.desc("isImportant"),
                Sort.Order.desc("createdAt")
            )
        );

        Page<NoticeEntity> noticePage;

        if (importantOnly) {
            noticePage = noticeRepository.findByIsDeletedFalseAndIsImportantTrue(pageable);
        } else {
            noticePage = noticeRepository.findByIsDeletedFalse(pageable);
        }

        return NoticePageResponseDTO.from(noticePage);
    }

    // 공지사항 상세 조회
    public NoticeDetailResponseDTO findNoticeDetail(Integer noticeId) {
        NoticeEntity notice = findNotice(noticeId);

        return NoticeDetailResponseDTO.from(notice);
    }

    // 공지사항 수정
    @Transactional
    public NoticeDetailResponseDTO updateNotice(
        CustomUserPrincipal loginUser,
        Integer noticeId,
        UpdateNoticeRequestDTO request
    ) {
        checkCoachPermission(loginUser, ErrorCode.NOTICE_UPDATE_FORBIDDEN);
        validateNoticeRequest(request.getTitle(), request.getContent());

        NoticeEntity notice = findNotice(noticeId);

        notice.updateNotice(
            request.getTitle().trim(),
            request.getContent().trim(),
            request.getIsImportant()
        );

        return NoticeDetailResponseDTO.from(notice);
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(
        CustomUserPrincipal loginUser,
        Integer noticeId
    ) {
        checkCoachPermission(loginUser, ErrorCode.NOTICE_DELETE_FORBIDDEN);

        NoticeEntity notice = findNotice(noticeId);

        notice.setIsDeleted(true);
    }

    /*
     * 지도자 권한 확인
     * COACH가 아니면 공지사항 등록, 수정, 삭제를 막는다.
     * isAdmin 값은 공지사항 권한 판단에 사용하지 않는다.
     */
    private void checkCoachPermission(
        CustomUserPrincipal loginUser,
        ErrorCode errorCode
    ) {
        if (loginUser.getMemberRole() != MemberRoleEnum.COACH) {
            throw new CustomException(errorCode);
        }
    }

    /*
     * 공지사항 입력값 검증
     * 제목과 내용은 필수이다.
     */
    private void validateNoticeRequest(String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            throw new CustomException(ErrorCode.NOTICE_TITLE_REQUIRED);
        }

        if (title.trim().length() > 255) {
            throw new CustomException(ErrorCode.NOTICE_TITLE_TOO_LONG);
        }

        if (content == null || content.trim().isEmpty()) {
            throw new CustomException(ErrorCode.NOTICE_CONTENT_REQUIRED);
        }
    }

    /*
     * 로그인 회원 조회
     * 공지사항 작성자 연결을 위해 로그인 회원 Entity를 조회한다.
     */
    private MemberEntity findLoginMember(Integer memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /*
     * 삭제되지 않은 공지사항 조회
     * 상세 조회, 수정, 삭제에서 공통으로 사용한다.
     */
    private NoticeEntity findNotice(Integer noticeId) {
        return noticeRepository.findByIdAndIsDeletedFalse(noticeId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
    }
}