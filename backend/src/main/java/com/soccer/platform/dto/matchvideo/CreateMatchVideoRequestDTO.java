package com.soccer.platform.dto.matchvideo;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.MatchResultEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * 경기 영상 등록 요청 DTO
 */

@Getter
@AllArgsConstructor
public class CreateMatchVideoRequestDTO {

	private String title;
    private LocalDateTime gameDate;
    private String place;
    private Integer homeScore;
    private Integer awayScore;
    private MatchResultEnum matchResult;
	
	// controller에서 multipart 요청 파라미터를 dto로 변환
	// 영상 파일은 dto에 포함하지 않고 multipartfile로 따로 전달
	public static CreateMatchVideoRequestDTO of(
            String title,
            LocalDateTime gameDate,
            String place,
            Integer homeScore,
            Integer awayScore,
            MatchResultEnum matchResult
    ) {
        return new CreateMatchVideoRequestDTO(
                title,
                gameDate,
                place,
                homeScore,
                awayScore,
                matchResult
        );
    }
}