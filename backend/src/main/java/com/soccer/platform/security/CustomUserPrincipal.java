package com.soccer.platform.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.entity.MemberEntity;

import lombok.Getter;

/*
 * 현재 로그인 회원 인증 정보
 * 
 * JWT 인증이 성공한 회원 정보를 Spring Security 인증 객체에 담기 위해 사용.
 */

@Getter
public class CustomUserPrincipal {

	private final Integer memberId;
	private final String loginId;
	private final String name;
	private final MemberRoleEnum memberRole;
	private final Boolean isAdmin;
	private final Boolean isCaptain;
	
	private CustomUserPrincipal(
			Integer memberId,
			String loginId,
			String name,
			MemberRoleEnum memberRole,
			Boolean isAdmin,
			Boolean isCaptain
	) {
		this.memberId = memberId;
		this.loginId = loginId;
		this.name = name;
		this.memberRole =memberRole;
		this.isAdmin = isAdmin;
		this.isCaptain = isCaptain;
	}
	
	public static CustomUserPrincipal from(MemberEntity member) {
		return new CustomUserPrincipal(
				member.getId(),
				member.getLoginId(),
				member.getName(),
				member.getMemberRole(),
				member.getIsAdmin(),
				member.getIsCaptain()
		);
	}
	
	/*
	 * Spring Security 권한 목록 생성
	 * 
	 * 역할 권한은 ROLE_COACH, ROLE_PLAYER, ROLE_ANALYST 형태로 저장.
	 * 관리자 계정은 ROLE_ADMIN도 함께 가진다.
	 */
	public Collection<? extends GrantedAuthority> getAuthorities(){
		if(Boolean.TRUE.equals(isAdmin)) {
			return List.of(
					new SimpleGrantedAuthority("ROLE_" + memberRole.name()),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
			);
		}
		
		return List.of(
				new SimpleGrantedAuthority("ROLE_" + memberRole.name())
		);
	}
}
