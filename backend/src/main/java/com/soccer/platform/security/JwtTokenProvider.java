package com.soccer.platform.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.entity.MemberEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;


/*
 * JWT 토큰 제공 클래스
 * 
 * 로그인 성공 시 Access Token을 생성하고
 * API 요청 시 전달된 Access Token을 검증한다.
 */

@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String jwtSecret;
	
	@Value("${jwt.access-token-expiration-ms}")
	private long accessTokenExpirationsMs;
	
	/*
	 * Access Token 생성
	 * 
	 * JWT에는 권한 검증에 필요한 최소 정보만 담는다.
	 * 비밀번호, 휴대폰 번호 같은 민감 정보는 절대 넣지 않는다.
	 */
	public String createAccessToken(MemberEntity member) {
		Instant now = Instant.now();
		Instant expiration = now.plusMillis(accessTokenExpirationsMs);
		
		return Jwts.builder()
				.subject(String.valueOf(member.getId()))
				.claim("loginId", member.getLoginId())
				.claim("role", member.getMemberRole())
				.claim("isAdmin", member.getIsAdmin())
				.claim("isCaptain", member.getIsCaptain())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiration))
				.signWith(getSigningKey())
				.compact();
				
	}
	
	/*
	 * Access Token에서 회원 ID 추출
	 * 
	 * JWT subject에 저장된 회원 PK를 Integer로 변환.
	 */
	public Integer getMemberId(String token) {
		Claims claims = parseClaims(token);
		
		try {
			return Integer.valueOf(claims.getSubject());
		}catch(NumberFormatException exception) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}
	
	/*
	 * JWT Claims 파싱
	 * 
	 * 토큰 검증과 회원ID 추출에서 공통으로 사용한다.
	 */
	private Claims parseClaims(String token) {
		try {
			return Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();
		}catch(ExpiredJwtException exception) {
			throw new CustomException(ErrorCode.EXPIRED_TOKEN);
		}catch(JwtException | IllegalArgumentException exception) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}
	
	
	/*
	 * JWT 서명 키 생성
	 * 
	 * HS256 서명을 위해 32자 이상 Secret Key를 사용.
	 */
	private SecretKey getSigningKey() {
		byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	
	/*
	 * Access Token 검증
	 *
	 * 토큰의 서명과 만료 시간을 확인한다.
	 * 문제가 있으면 CustomException을 발생시킨다.
	 */
	public void validateToken(String token) {
	    parseClaims(token);
	}
}
