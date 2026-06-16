package com.soccer.platform.dto.auth;

import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * 로그인 응답DTO
 * 
 * 로그인 성공 시 JWT Access Token과
 * 현재 로그인한 회원의 최소 정보를 반환.
 */

@Getter
@AllArgsConstructor
public class LoginResponseDTO {

	private String accessToken;
	private String tokenType;
	private MemberInfo member;
	
	public static LoginResponseDTO of(String accessToken, MemberEntity member) {
		return new LoginResponseDTO(
				accessToken, 
				"Bearer", 
				MemberInfo.from(member)
		);
	}
	
	/*
	 * 로그인 회원 요약 정보
	 * 
	 * 프론트에서 화면 분기와 권한 판단에 필요한 최소 정보만 내려준다.
	 * 비밀번호, 휴대폰 번호 같은 민감 정보는 포함하지 않는다.
	 */
	@Getter
	@AllArgsConstructor
	public static class MemberInfo {
		private Integer memberId;
		private String loginId;
		private String name;
		private MemberRoleEnum memberRole;
		private Boolean isAdmin;
		private Boolean isCaptain;
		
		public static MemberInfo from(MemberEntity member) {
			return new MemberInfo(
					member.getId(),
					member.getLoginId(),
					member.getName(),
					member.getMemberRole(),
					member.getIsAdmin(),
					member.getIsCaptain()
				);
		}
	}
}
