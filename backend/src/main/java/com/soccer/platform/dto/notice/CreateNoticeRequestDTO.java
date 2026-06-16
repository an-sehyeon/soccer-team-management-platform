package com.soccer.platform.dto.notice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * 공지사항 등록 요청 DTO
 * 지도자가 공지사항을 등록할 때 사용하는 요청 값이다.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateNoticeRequestDTO {
    private String title;
    private String content;
    private Boolean isImportant;
}