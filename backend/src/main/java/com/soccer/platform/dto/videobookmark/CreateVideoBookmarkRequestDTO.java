package com.soccer.platform.dto.videobookmark;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 영상 북마크 등록 요청 DTO
 * 현재 재생 중인 영상에 따라 다음 조합을 사용
 *
 * 경기 원본 영상:
 * - matchVideoId 필수
 * - teamClipId null
 * - playerClipId null
 *
 * 팀 분석 클립:
 * - matchVideoId 필수
 * - teamClipId 필수
 * - playerClipId null
 *
 * 선수 개인 분석 클립:
 * - matchVideoId 필수
 * - teamClipId null
 * - playerClipId 필수
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateVideoBookmarkRequestDTO {

    private Integer matchVideoId;
    private Integer teamClipId;
    private Integer playerClipId;

    /*
     * 현재 북마크 대상 영상 기준 시간
     * 경기 원본 영상이면 원본 영상 기준 초,
     * 분석 클립이면 생성된 클립 영상 내부 기준 초다.
     */
    private Integer bookmarkTimeSec;
    private String title;
    private String memo;
}