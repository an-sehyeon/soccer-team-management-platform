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
    INVALID_PAGE_REQUEST(HttpStatus.BAD_REQUEST, "페이지 요청 값이 올바르지 않습니다."),
    
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
    MANAGEMENT_PLAYER_LIST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "관리용 선수 목록 조회 권한이 없습니다."),
    
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
	
	// 선수 기록
	PLAYER_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "선수 기록을 찾을 수 없습니다."),
	PLAYER_RECORD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "선수 기록에 접근할 권한이 없습니다."),
	PLAYER_RECORD_MANAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "선수 기록 관리 권한이 없습니다."),
	DUPLICATE_PLAYER_RECORD(HttpStatus.CONFLICT, "이미 해당 경기의 선수 기록이 존재합니다."),
	INVALID_PLAYER_RECORD_PLAYER(HttpStatus.BAD_REQUEST, "선수 기록 대상은 선수 역할만 가능합니다."),
	INVALID_PLAYER_RECORD_VALUE(HttpStatus.BAD_REQUEST, "선수 기록 값이 올바르지 않습니다."),
	
    // 선수 기록 이벤트
    PLAYER_RECORD_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "선수 기록 이벤트를 찾을 수 없습니다."),
    PLAYER_RECORD_EVENT_CLIP_NOT_FOUND(HttpStatus.NOT_FOUND, "선수 기록 이벤트 클립 연결을 찾을 수 없습니다."),
    PLAYER_RECORD_EVENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "선수 기록 이벤트에 접근할 권한이 없습니다."),
    PLAYER_RECORD_EVENT_MANAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "선수 기록 이벤트 관리 권한이 없습니다."),
    INVALID_PLAYER_RECORD_EVENT_REQUEST(HttpStatus.BAD_REQUEST, "선수 기록 이벤트 요청 값이 올바르지 않습니다."),
    INVALID_PLAYER_RECORD_EVENT_TYPE(HttpStatus.BAD_REQUEST, "선수 기록 이벤트 유형이 올바르지 않습니다."),
    INVALID_PLAYER_RECORD_CLIP_SOURCE_TYPE(HttpStatus.BAD_REQUEST, "선수 기록 이벤트 클립 출처 유형이 올바르지 않습니다."),
    INVALID_PLAYER_RECORD_EVENT_TIME(HttpStatus.BAD_REQUEST, "선수 기록 이벤트 시간이 올바르지 않습니다."),
    INVALID_PLAYER_RECORD_EVENT_VALUE(HttpStatus.BAD_REQUEST, "선수 기록 이벤트 값이 올바르지 않습니다."),
    PLAYER_RECORD_CLIP_MATCH_VIDEO_MISMATCH(HttpStatus.BAD_REQUEST, "선수 기록 이벤트와 연결할 클립의 경기 영상이 일치하지 않습니다."),
    PLAYER_RECORD_EVENT_PLAYER_MISMATCH(HttpStatus.BAD_REQUEST, "선수 기록 대상 선수와 선수 개인 분석 클립 대상 선수가 일치하지 않습니다."),
	
	// 스케줄
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 스케줄입니다."),
	SCHEDULE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "스케줄을 관리할 권한이 없습니다."),
	INVALID_SCHEDULE_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 스케줄 유형입니다."),
	INVALID_SCHEDULE_DATE_RANGE(HttpStatus.BAD_REQUEST, "스케줄 조회 기간이 올바르지 않습니다."),
	INVALID_SCHEDULE_INTENSITY(HttpStatus.BAD_REQUEST, "올바르지 않은 훈련 강도입니다."),
	INVALID_SCHEDULE_DATETIME(HttpStatus.BAD_REQUEST, "스케줄 날짜와 시간이 올바르지 않습니다."),
	REQUIRED_SCHEDULE_PLACE(HttpStatus.BAD_REQUEST, "스케줄 장소는 필수입니다."),
	
	// 공지사항
	NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),
	INVALID_NOTICE_REQUEST(HttpStatus.NOT_FOUND, "유효하지 않은 공지사항 형식입니다."),
	NOTICE_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "공지사항 등록 권한이 없습니다."),
	NOTICE_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "공지사항 수정 권한이 없습니다."),
	NOTICE_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "공지사항 삭제 권한이 없습니다."),
	NOTICE_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "공지사항 제목은 필수입니다."),
	NOTICE_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "공지사항 내용은 필수입니다."),
	NOTICE_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "공지사항 제목은 255자를 초과할 수 없습니다."),
	
	// 경기 영상 업로드
	MATCH_VIDEO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "경기 영상에 접근할 권한이 없습니다."),
	MATCH_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "경기 영상을 찾을 수 없습니다."),
	INVALID_VIDEO_DURATION(HttpStatus.BAD_REQUEST, "영상 길이는 1초 이상이어야 합니다."),
	INVALID_MATCH_VIDEO_REQUEST(HttpStatus.BAD_REQUEST, "경기 영상 요청값이 올바르지 않습니다."),
	MATCH_VIDEO_DURATION_NOT_READY(HttpStatus.BAD_REQUEST, "원본 영상 길이 정보가 준비되지 않았습니다."),
	
	// 팀 분석 클립
	TEAM_ANALYSIS_CLIP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "팀 분석 클립 접근 권한이 없습니다."),
	TEAM_ANALYSIS_CLIP_NOT_FOUND(HttpStatus.NOT_FOUND, "팀 분석 클립을 찾을 수 없습니다."),
	INVALID_TEAM_ANALYSIS_CLIP_REQUEST(HttpStatus.BAD_REQUEST, "팀 분석 클립 요청 값이 올바르지 않습니다."),
	INVALID_CLIP_TIME_RANGE(HttpStatus.BAD_REQUEST, "클립 시작 시간과 종료 시간이 올바르지 않습니다."),
	CLIP_TIME_EXCEEDS_VIDEO_DURATION(HttpStatus.BAD_REQUEST, "클립 시간이 원본 영상 길이를 초과했습니다."),
	TEAM_ANALYSIS_CLIP_PROCESSING_CANNOT_UPDATE(
	        HttpStatus.BAD_REQUEST,
	        "팀 분석 클립 파일 생성 중에는 수정할 수 없습니다."
	),
	TEAM_ANALYSIS_CLIP_DIRECTORY_CREATE_FAILED(
	        HttpStatus.INTERNAL_SERVER_ERROR,
	        "팀 분석 클립 저장 디렉터리 생성에 실패했습니다."
	),
	TEAM_ANALYSIS_CLIP_ORIGINAL_FILE_NOT_FOUND(
	        HttpStatus.NOT_FOUND,
	        "팀 분석 클립 생성을 위한 원본 영상 파일을 찾을 수 없습니다."
	),
	TEAM_ANALYSIS_CLIP_FILE_GENERATION_FAILED(
	        HttpStatus.INTERNAL_SERVER_ERROR,
	        "팀 분석 클립 파일 생성에 실패했습니다."
	),
	
	// 팀 분석 클립 드로잉
	TEAM_ANALYSIS_CLIP_DRAWING_NOT_FOUND(HttpStatus.NOT_FOUND, "팀 분석 클립 드로잉을 찾을 수 없습니다."),
	TEAM_ANALYSIS_CLIP_DRAWING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "팀 분석 클립 드로잉 권한이 없습니다."),
	INVALID_DRAWING_TIME_RANGE(HttpStatus.BAD_REQUEST, "드로잉 시간 범위가 올바르지 않습니다."),
	DRAWING_TIME_OUT_OF_CLIP_RANGE(HttpStatus.BAD_REQUEST, "드로잉 시간은 클립 영상 시간 범위 안에 있어야 합니다."),
	EMPTY_DRAWING_DATA(HttpStatus.BAD_REQUEST, "드로잉 데이터는 비어 있을 수 없습니다."),
	INVALID_DRAWING_DATA(HttpStatus.BAD_REQUEST, "드로잉 데이터 형식이 올바르지 않습니다."),
	
	// 선수 분석 클립
	PLAYER_ANALYSIS_CLIP_NOT_FOUND(HttpStatus.NOT_FOUND, "선수 개인 분석 클립을 찾을 수 없습니다."),
	INVALID_PLAYER_ROLE(HttpStatus.BAD_REQUEST, "대상 선수 역할이 올바르지 않습니다."),
	PLAYER_ANALYSIS_CLIP_DIRECTORY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "선수 개인 분석 클립 저장 디렉터리 생성에 실패했습니다."),
	PLAYER_ANALYSIS_CLIP_ORIGINAL_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "선수 개인 분석 클립 생성에 필요한 원본 영상 파일을 찾을 수 없습니다."),
	PLAYER_ANALYSIS_CLIP_FILE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "선수 개인 분석 클립 파일 생성에 실패했습니다."),
	PLAYER_ANALYSIS_CLIP_PROCESSING_CANNOT_UPDATE(HttpStatus.BAD_REQUEST, "생성 중인 선수 개인 분석 클립은 수정할 수 없습니다."),
	
	// 선수 개인 분석 클립 조회 기록
	PLAYER_ANALYSIS_CLIP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "선수 개인 분석 클립에 접근할 권한이 없습니다."),
	PLAYER_ANALYSIS_CLIP_VIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "선수 개인 분석 클립 조회 기록에 접근할 권한이 없습니다."),
	
	// 선수 개인 분석 클립 드로잉
	PLAYER_CLIP_DRAWING_NOT_FOUND(HttpStatus.NOT_FOUND, "선수 개인 분석 클립 드로잉을 찾을 수 없습니다."),
	PLAYER_CLIP_DRAWING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 선수 개인 분석 클립 드로잉에 접근할 권한이 없습니다."),
	PLAYER_CLIP_DRAWING_MANAGE_FORBIDDEN(HttpStatus.FORBIDDEN, "선수 개인 분석 클립 드로잉을 등록하거나 수정할 권한이 없습니다."),
	PLAYER_CLIP_DRAWING_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "선수 개인 분석 클립 드로잉을 삭제할 권한이 없습니다."),
	DRAWING_DATA_REQUIRED(HttpStatus.BAD_REQUEST, "드로잉 데이터는 필수입니다."),
	
	// 경기 영상 업로드
	MATCH_VIDEO_FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "경기 영상 파일 저장에 실패했습니다."),
	MATCH_VIDEO_UPLOAD_DIRECTORY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "경기 영상 업로드 디렉터리 생성에 실패했습니다."),
	MATCH_VIDEO_DURATION_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "경기 영상 길이 추출에 실패했습니다."),
	INVALID_MATCH_VIDEO_DURATION(HttpStatus.BAD_REQUEST, "경기 영상 길이가 올바르지 않습니다."),
	MATCH_VIDEO_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "경기 영상 파일은 필수입니다."),
	INVALID_MATCH_VIDEO_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않은 경기 영상 파일 확장자입니다."),
	INVALID_MATCH_VIDEO_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "허용되지 않은 경기 영상 파일 형식입니다."),
	MATCH_VIDEO_FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "경기 영상 파일 크기가 허용 범위를 초과했습니다."),
	
    // 영상 북마크
    VIDEO_BOOKMARK_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "영상 북마크를 찾을 수 없습니다."
    ),
    VIDEO_BOOKMARK_ACCESS_DENIED(
        HttpStatus.FORBIDDEN,
        "해당 영상 북마크에 접근할 권한이 없습니다."
    ),
    VIDEO_BOOKMARK_MANAGE_ACCESS_DENIED(
        HttpStatus.FORBIDDEN,
        "영상 북마크 관리 권한이 없습니다."
    ),
    INVALID_VIDEO_BOOKMARK_REQUEST(
        HttpStatus.BAD_REQUEST,
        "영상 북마크 요청 값이 올바르지 않습니다."
    ),
    INVALID_VIDEO_BOOKMARK_SOURCE(
        HttpStatus.BAD_REQUEST,
        "영상 북마크 대상 영상 정보가 올바르지 않습니다."
    ),
    VIDEO_BOOKMARK_MATCH_VIDEO_MISMATCH(
        HttpStatus.BAD_REQUEST,
        "북마크 대상 클립과 경기 영상이 일치하지 않습니다."
    ),
    INVALID_VIDEO_BOOKMARK_TIME(
        HttpStatus.BAD_REQUEST,
        "영상 북마크 시간이 대상 영상 범위를 벗어났습니다."
    ),
    VIDEO_BOOKMARK_TARGET_NOT_READY(
        HttpStatus.BAD_REQUEST,
        "재생 가능한 상태의 영상에만 북마크를 등록할 수 있습니다."
    );

	private final HttpStatus httpStatus;
	private final String message;
	
	ErrorCode(HttpStatus httpStatus, String message){
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
