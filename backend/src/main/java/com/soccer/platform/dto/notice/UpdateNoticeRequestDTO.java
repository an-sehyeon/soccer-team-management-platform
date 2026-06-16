package com.soccer.platform.dto.notice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * 공지사항 수정 요청 DTO
 * 지도자가 기존 공지사항을 수정할 때 사용하는 요청 값이다.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateNoticeRequestDTO {
    private String title;
    private String content;
    private Boolean isImportant;
}