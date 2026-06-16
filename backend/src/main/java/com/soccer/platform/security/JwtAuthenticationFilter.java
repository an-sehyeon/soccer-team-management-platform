package com.soccer.platform.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccer.platform.common.constants.ApprovalStatusEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.common.exception.ErrorResponse;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.repository.MemberRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/*
 * JWT 인증 필터
 *
 * API 요청의 Authorization 헤더에서 JWT Access Token을 추출하고,
 * 토큰이 유효하면 Spring Security 인증 객체를 생성한다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    /*
     * 요청마다 한 번 실행되는 JWT 인증 처리
     *
     * 토큰이 없으면 인증 처리를 하지 않고 다음 필터로 넘긴다.
     * 토큰이 있는데 잘못된 경우에는 401 응답을 직접 반환한다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            if (StringUtils.hasText(token)) {
                authenticateByToken(token, request);
            }

            filterChain.doFilter(request, response);
        } catch (CustomException exception) {
            sendErrorResponse(response, request, exception.getErrorCode());
        }
    }

    /*
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    /*
     * JWT 토큰 기반 인증 처리
     *
     * 토큰 검증 후 회원을 조회하고,
     * 정상 회원이면 SecurityContext에 인증 정보를 저장한다.
     */
    private void authenticateByToken(String token, HttpServletRequest request) {
        jwtTokenProvider.validateToken(token);

        Integer memberId = jwtTokenProvider.getMemberId(token);

        MemberEntity member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        validateApprovedMember(member);

        CustomUserPrincipal principal = CustomUserPrincipal.from(member);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 승인 완료 회원인지 검증
     *
     * 토큰이 유효하더라도 회원이 승인 대기/거절 상태로 변경되었으면 접근을 차단한다.
     */
    private void validateApprovedMember(MemberEntity member) {
        if (member.getApprovalStatus() != ApprovalStatusEnum.APPROVED) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * JWT 필터 예외 응답 처리
     *
     * 필터에서 발생한 예외는 GlobalExceptionHandler로 가지 않기 때문에
     * 여기서 직접 JSON 응답을 내려준다.
     */
    private void sendErrorResponse(
            HttpServletResponse response,
            HttpServletRequest request,
            ErrorCode errorCode
    ) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}