package com.soccer.platform.dto.matchvideo;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.MatchResultEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * 경기 영상 수정 요청 DTO
 */

@Getter
@Setter
@NoArgsConstructor
public class UpdateMatchVideoRequestDTO {

	private String url;
	private String title;
	private LocalDateTime  gameDate;
	private String place;
	private Integer homeScore;
	private Integer awayScore;
	private MatchResultEnum matchResult;
}
