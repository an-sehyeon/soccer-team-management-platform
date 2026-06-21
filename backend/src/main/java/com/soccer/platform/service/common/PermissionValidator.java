package com.soccer.platform.service.common;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.security.CustomUserPrincipal;

// 공통 권한 검증 Validator

@Component
public class PermissionValidator {

    private static final String ROLE_COACH = "COACH";
    private static final String ROLE_ANALYST = "ANALYST";
    private static final String ROLE_PLAYER = "PLAYER";

    
    // 지도자 권한 검증
    // 지도자만 가능한 등록, 수정, 삭제 기능에서 사용
    public void requireCoach(CustomUserPrincipal principal, ErrorCode errorCode) {
        requireRole(principal, errorCode, ROLE_COACH);
    }

    // 지도자 또는 분석관 권한 검증
    // 관리용 등록, 수정, 조회 기능에서 사용.
    public void requireCoachOrAnalyst(CustomUserPrincipal principal, ErrorCode errorCode) {
        requireRole(principal, errorCode, ROLE_COACH, ROLE_ANALYST);
    }

    // 선수 권한 검증
    public void requirePlayer(CustomUserPrincipal principal, ErrorCode errorCode) {
        requireRole(principal, errorCode, ROLE_PLAYER);
    }

    // 로그인 사용자 전체 조회 권한 검증
    // COACH, ANALYST, PLAYER 모두 조회 가능한 공통 조회 API에서 사용
    public void requireAuthenticatedServiceUser(CustomUserPrincipal principal, ErrorCode errorCode) {
        requireRole(principal, errorCode, ROLE_COACH, ROLE_ANALYST, ROLE_PLAYER);
    }

    // 허용된 역할인지 검증
    // - 로그인 회원 역할을 문자열로 변환
    // - 허용 역할 목록에 포함되는지 확인
    public void requireRole(
            CustomUserPrincipal principal,
            ErrorCode errorCode,
            String... allowedRoles
    ) {
        if (principal == null || principal.getMemberRole() == null) {
            throw new CustomException(errorCode);
        }

        String loginMemberRole = String.valueOf(principal.getMemberRole());

        boolean allowed = Arrays.stream(allowedRoles)
                .filter(Objects::nonNull)
                .anyMatch(allowedRole -> allowedRole.equals(loginMemberRole));

        if (!allowed) {
            throw new CustomException(errorCode);
        }
    }

    // 현재 로그인 사용자가 선수인지 확인
    // 조회 기록 저장처럼 선수인 경우에만 부가 처리를 할 때 사용
    public boolean isPlayer(CustomUserPrincipal principal) {
        return hasRole(principal, ROLE_PLAYER);
    }

    // 현재 로그인 사용작가 지도자 또는 분석관인지 확인
    public boolean isCoachOrAnalyst(CustomUserPrincipal principal) {
        return hasRole(principal, ROLE_COACH) || hasRole(principal, ROLE_ANALYST);
    }

    // 현재 로그인 사용자가 지도자인지 확인
    public boolean isCoach(CustomUserPrincipal principal) {
        return hasRole(principal, ROLE_COACH);
    }

    // 현재 로그인 사용자가 분석관인지 확인
    public boolean isAnalyst(CustomUserPrincipal principal) {
        return hasRole(principal, ROLE_ANALYST);
    }

    private boolean hasRole(CustomUserPrincipal principal, String role) {
        if (principal == null || principal.getMemberRole() == null) {
            return false;
        }

        return role.equals(String.valueOf(principal.getMemberRole()));
    }
}