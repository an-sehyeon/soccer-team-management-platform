package com.soccer.platform.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/*
 * 전역 예외 처리 클래스
 * 
 * Controller와 Service에서 발생한 예외를 공통 응답 형식으로 변환.
 * 
 * 처리대상
 * - CustomException
 * - 요청 DTO 검증 실패
 * - 예상하지 못한 서버 오류
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	// 서비스에서 직접 발생시킨 비즈니스 예외 처리
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(
			CustomException exception,
			HttpServletRequest request
	){
		ErrorCode errorCode = exception.getErrorCode();
		
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ErrorResponse.of(errorCode, request.getRequestURI()));
	}
	
	/*
	 * @Valid 검증 실패 처리
	 * 
	 * SignUpRequest, LoginRequest 같은 요청 DTO에서
     * 필수값 누락 또는 형식 오류가 발생했을 때 처리한다.
	 */
	 @ExceptionHandler(MethodArgumentNotValidException.class)
	    public ResponseEntity<ErrorResponse> handleValidationException(
	            MethodArgumentNotValidException exception,
	            HttpServletRequest request
	    ) {
	        String message = exception.getBindingResult()
	                .getFieldErrors()
	                .stream()
	                .findFirst()
	                .map(fieldError -> fieldError.getDefaultMessage())
	                .orElse(ErrorCode.INVALID_REQUEST.getMessage());

	        return ResponseEntity
	                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
	                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST, message, request.getRequestURI()));
	    }

	    /**
	     * 예상하지 못한 서버 오류 처리
	     *
	     * 운영 환경에서는 exception 메시지를 그대로 응답하지 않는다.
	     */
	    @ExceptionHandler(Exception.class)
	    public ResponseEntity<ErrorResponse> handleException(
	            Exception exception,
	            HttpServletRequest request
	    ) {
	    	
	    	exception.printStackTrace();
	    	
	        return ResponseEntity
	                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
	                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI()));
	    }
}
