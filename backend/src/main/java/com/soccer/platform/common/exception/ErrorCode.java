package com.soccer.platform.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/*
 * API 예외 코드
 * 
 * 서비스에서 발생할 수 있는 예외를 HTTP 상태 코드와 메시지로 관리한다.
 * 회원가입, 로그인 JWT 인증, 권한 검증에서 공통으로 사용한다.
 * */
@Getter
public enum ErrorCode {
	
	// 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 회원가입
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 로그인 ID입니다."),
    INVALID_MEMBER_ROLE(HttpStatus.BAD_REQUEST, "가입할 수 없는 회원 역할입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다."),
    PLAYER_GRADE_REQUIRED(HttpStatus.BAD_REQUEST, "선수는 학년을 필수로 입력해야 합니다."),
    PLAYER_UNIFORM_NUMBER_REQUIRED(HttpStatus.BAD_REQUEST, "선수는 등번호를 필수로 입력해야 합니다."),
    INVALID_GRADE(HttpStatus.BAD_REQUEST, "학년은 1부터 4까지만 입력할 수 있습니다."),
    DUPLICATE_UNIFORM_NUMBER(HttpStatus.CONFLICT, "이미 사용 중인 등번호입니다."),

    // 로그인
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    PENDING_MEMBER(HttpStatus.FORBIDDEN, "관리자 승인 후 로그인이 가능합니다."),
    REJECTED_MEMBER(HttpStatus.FORBIDDEN, "가입 승인이 거절된 계정입니다."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    DELETED_MEMBER(HttpStatus.UNAUTHORIZED, "탈퇴 또는 삭제된 회원입니다."),

    // 인증/JWT
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "인증 토큰이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 인증 토큰입니다."),

    // 권한
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다."),
	
	// 가입 승인
    EMPTY_MEMBER_SELECTION(HttpStatus.BAD_REQUEST, "승인 처리할 회원을 선택해야 합니다."),
	INVALID_APPROVAL_STATUS(HttpStatus.BAD_REQUEST, "변경할 수 없는 승인 상태입니다."),
	CANNOT_APPROVE_ADMIN(HttpStatus.BAD_REQUEST, "관리자 계정의 승인 상태는 이 API에서 변경할 수 없습니다."),
	
	// 스케줄
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 스케줄입니다."),
	SCHEDULE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "스케줄을 관리할 권한이 없습니다."),
	INVALID_SCHEDULE_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 스케줄 유형입니다."),
	INVALID_SCHEDULE_INTENSITY(HttpStatus.BAD_REQUEST, "올바르지 않은 훈련 강도입니다."),
	INVALID_SCHEDULE_DATETIME(HttpStatus.BAD_REQUEST, "스케줄 날짜와 시간이 올바르지 않습니다."),
	REQUIRED_SCHEDULE_PLACE(HttpStatus.BAD_REQUEST, "스케줄 장소는 필수입니다."),
	
	// 공지사항
	NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),
	NOTICE_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "공지사항 등록 권한이 없습니다."),
	NOTICE_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "공지사항 수정 권한이 없습니다."),
	NOTICE_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "공지사항 삭제 권한이 없습니다."),
	NOTICE_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "공지사항 제목은 필수입니다."),
	NOTICE_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "공지사항 내용은 필수입니다."),
	NOTICE_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "공지사항 제목은 255자를 초과할 수 없습니다."),
	
	// 경기 영상 업로드
	MATCH_VIDEO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "경기 영상에 접근할 권한이 없습니다."),
	MATCH_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "경기 영상을 찾을 수 없습니다."),
	INVALID_MATCH_VIDEO_REQUEST(HttpStatus.BAD_REQUEST, "경기 영상 요청값이 올바르지 않습니다.");

	private final HttpStatus httpStatus;
	private final String message;
	
	ErrorCode(HttpStatus httpStatus, String message){
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
