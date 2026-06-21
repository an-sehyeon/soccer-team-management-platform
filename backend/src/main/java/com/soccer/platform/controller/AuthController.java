package com.soccer.platform.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.dto.auth.LoginRequestDTO;
import com.soccer.platform.dto.auth.LoginResponseDTO;
import com.soccer.platform.dto.auth.MemberMeResponseDTO;
import com.soccer.platform.dto.auth.SignUpRequestDTO;
import com.soccer.platform.dto.auth.SignUpResponseDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.memberauth.MemberAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * 인증 API Controller
 * 
 * 회원가입, 로그인, 내 정보 조회 등 인증 관련 API를 처리.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
	
	private final MemberAuthService memberAuthService;
	
	/*
	 * 회원가입 API
	 * 
	 * 처리 흐름
     * 1. 회원가입 요청 값을 검증한다.
     * 2. 로그인 ID 중복 여부를 확인한다.
     * 3. 역할별 필수 입력값을 검증한다.
     * 4. 비밀번호를 암호화한다.
     * 5. PENDING 상태로 회원가입 신청을 저장한다.
     *
     * 주의사항
     * - 회원가입 직후 자동 로그인하지 않는다.
     * - 관리자 승인 후 로그인할 수 있다.
     */
	@PostMapping("/sign-up")
	public ResponseEntity<SignUpResponseDTO> signup(
			@Valid @RequestBody SignUpRequestDTO request
	){
		SignUpResponseDTO response = memberAuthService.signUp(request);
		
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(response);
	}
	
	
	/*
	 * 로그인 API
	 * 
	 * 1. 로그인 ID와 비밀번호를 검증.
	 * 2. 승인 완료 회원인지 확인.
	 * 3. 마지막 로그인 시간을 갱신
	 * 4. JWT Access Token을 발급.
	 * 5. 토큰과 회원 요약 정보를 반환
	 * 
	 * - PENDING 회원은 로그인 불가
	 * - REJECTED 회원도 로그인 불가
	 * - 로그인 ID 없음과 비밀번호 불일치는 같은 메시지로 응답.
	 */
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(
			@Valid @RequestBody LoginRequestDTO request
	){
		LoginResponseDTO response = memberAuthService.login(request);
		
		return ResponseEntity.ok(response);
	}
	
	/*
	 * 내 정보 조회 API
	 * 
	 * JWT 인증이 완료된 현재 로그인 회원 정보를 반환.
	 * - Authorization 헤더에 Bearer 토큰이 필요하다.
	 * - 승인 완료 회원만 조회할 수 있다.
	 */
	@GetMapping("/me")
	public ResponseEntity<MemberMeResponseDTO> getMyInfo(
	        @AuthenticationPrincipal CustomUserPrincipal principal
	) {
	    MemberMeResponseDTO response = memberAuthService.getMyInfo(principal.getMemberId());

	    return ResponseEntity.ok(response);
	}
}
