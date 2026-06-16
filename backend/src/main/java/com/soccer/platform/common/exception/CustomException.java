package com.soccer.platform.common.exception;

import lombok.Getter;

/*
 * 서비스 공통 예외
 * 
 * 비즈니스 로직에서 예외가 필요한 경우 사용.
 * 예를 들어 로그인ID 중복, 승인 대기 회원 로그인, 권한 없음 등을 처리.
 */
@Getter
public class CustomException extends RuntimeException{
	
	private final ErrorCode errorCode;
	
	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

}
