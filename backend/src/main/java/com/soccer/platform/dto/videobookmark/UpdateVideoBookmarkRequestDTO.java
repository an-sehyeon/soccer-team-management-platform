package com.soccer.platform.dto.videobookmark;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * 영상 북마크 수정 요청 DTO
 * 북마크 제목, 메모, 시간만 수정 가능
 * 북마크가 연결된 경기 영상, 팀 분석 클립,
 * 선수 개인 분석 클립은 수정 불가
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateVideoBookmarkRequestDTO {

    private Integer bookmarkTimeSec;
    private String title;
    private String memo;
}