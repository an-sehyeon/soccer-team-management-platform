package com.soccer.platform.service.notice;

import org.springframework.stereotype.Component;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.notice.CreateNoticeRequestDTO;
import com.soccer.platform.dto.notice.UpdateNoticeRequestDTO;

// 공지사항 요청값 Validator
 
@Component
public class NoticeValidator {

    private static final int MAX_TITLE_LENGTH = 255;

    // 공지사항 등록 요청값 검증
    public void validateCreateRequest(CreateNoticeRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_NOTICE_REQUEST);
        }

        validateTitle(request.getTitle());
        validateContent(request.getContent());
        validateImportantValue(request.getIsImportant());
    }

    // 공지사항 수정 요청값 검증
    public void validateUpdateRequest(UpdateNoticeRequestDTO request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_NOTICE_REQUEST);
        }

        validateTitle(request.getTitle());
        validateContent(request.getContent());
        validateImportantValue(request.getIsImportant());
    }

    // 공지사항 제목 검증
    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_NOTICE_REQUEST);
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            throw new CustomException(ErrorCode.INVALID_NOTICE_REQUEST);
        }
    }

    // 공지사항 내용 검증
    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_NOTICE_REQUEST);
        }
    }

    // 중요 공지 여부 검증
    private void validateImportantValue(Boolean isImportant) {
        if (isImportant == null) {
            throw new CustomException(ErrorCode.INVALID_NOTICE_REQUEST);
        }
    }
}