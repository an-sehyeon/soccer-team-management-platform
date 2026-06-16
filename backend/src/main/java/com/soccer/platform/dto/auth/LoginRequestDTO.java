package com.soccer.platform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * 로그인 요청DTO
 * 
 * 사용자가 입력한 로그인 ID와 비밀번호를 전달받는다.
 */
@Getter
@NoArgsConstructor
public class LoginRequestDTO {

	@NotBlank(message = "ID를 입력해주세요")
	private String loginId;
	
	@NotBlank(message = "비밀번호를 입력해주세요")
	private String password;
}
