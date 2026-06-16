package com.soccer.platform.dto.notice;

import java.time.LocalDateTime;

import com.soccer.platform.entity.NoticeEntity;

import lombok.Getter;

/**
 * 공지사항 목록 응답 DTO
 * 목록 화면에서는 긴 본문 내용까지 내려주지 않고,
 * 공지사항을 구분하는 데 필요한 기본 정보만 반환한다.
 */
@Getter
public class NoticeListResponseDTO {
	
    private Integer noticeId;
    private String title;
    private Boolean isImportant;
    private Integer writerId;
    private String writerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private NoticeListResponseDTO(NoticeEntity notice) {
        this.noticeId = notice.getId();
        this.title = notice.getTitle();
        this.isImportant = notice.getIsImportant();
        this.writerId = notice.getMember().getId();
        this.writerName = notice.getMember().getName();
        this.createdAt = notice.getCreatedAt();
        this.updatedAt = notice.getUpdatedAt();
    }

    public static NoticeListResponseDTO from(NoticeEntity notice) {
        return new NoticeListResponseDTO(notice);
    }
}