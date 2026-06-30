package com.soccer.platform.dto.member;

import com.soccer.platform.entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 선수 선택 드롭다운 응답 DTO
// 선수 개인 분석 클립 등록/수정 화면에서
// 대상 선수 선택에 필요한 최소 정보만 반환

@Getter
@AllArgsConstructor
public class PlayerSelectResponseDTO {
	
	private Integer playerId;
	private String name;
	private Integer grade;
	private Integer uniformNumber;
	
	public static PlayerSelectResponseDTO from(MemberEntity player) {
		return new PlayerSelectResponseDTO(
				player.getId(),
				player.getName(),
				player.getGrade(),
				player.getUniformNumber()
		);
	}

}
