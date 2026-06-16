package com.soccer.platform.common.exception;

import java.time.LocalDateTime;

import lombok.Getter;

/*
 * API 예외 응답 DTO
 * 
 * 클라이언트에 일관된 예외 응답 형식을 내려주기 위해 사용.
 */
@Getter
public class ErrorResponse {
	private final LocalDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String path;

    private ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String code,
            String message,
            String path
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getHttpStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                path
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getHttpStatus().value(),
                errorCode.name(),
                message,
                path
        );
    }

}
