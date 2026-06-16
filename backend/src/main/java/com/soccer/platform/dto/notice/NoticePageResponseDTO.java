package com.soccer.platform.dto.notice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.soccer.platform.entity.NoticeEntity;

import lombok.Getter;

/**
 * 공지사항 목록 페이지 응답 DTO
 * 공지사항 목록과 페이지 정보를 함께 반환한다.
 */
@Getter
public class NoticePageResponseDTO {

    private List<NoticeListResponseDTO> notices;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    private NoticePageResponseDTO(Page<NoticeEntity> noticePage) {
        this.notices = noticePage.getContent()
            .stream()
            .map(NoticeListResponseDTO::from)
            .collect(Collectors.toList());

        this.page = noticePage.getNumber();
        this.size = noticePage.getSize();
        this.totalElements = noticePage.getTotalElements();
        this.totalPages = noticePage.getTotalPages();
        this.hasNext = noticePage.hasNext();
    }

    public static NoticePageResponseDTO from(Page<NoticeEntity> noticePage) {
        return new NoticePageResponseDTO(noticePage);
    }
}