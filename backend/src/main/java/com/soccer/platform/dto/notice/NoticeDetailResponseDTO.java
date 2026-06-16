package com.soccer.platform.dto.notice;

import java.time.LocalDateTime;

import com.soccer.platform.entity.NoticeEntity;

import lombok.Getter;

/*
 * 공지사항 상세 응답 DTO
 * 상세 화면에서는 제목, 본문, 작성자, 작성일 정보를 모두 반환한다.
 */
@Getter
public class NoticeDetailResponseDTO {

    private Integer noticeId;
    private String title;
    private String content;
    private Boolean isImportant;
    private Integer writerId;
    private String writerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private NoticeDetailResponseDTO(NoticeEntity notice) {
        this.noticeId = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.isImportant = notice.getIsImportant();
        this.writerId = notice.getMember().getId();
        this.writerName = notice.getMember().getName();
        this.createdAt = notice.getCreatedAt();
        this.updatedAt = notice.getUpdatedAt();
    }

    public static NoticeDetailResponseDTO from(NoticeEntity notice) {
        return new NoticeDetailResponseDTO(notice);
    }
}