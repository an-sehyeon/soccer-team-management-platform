package com.soccer.platform.service.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

// 회원 조회 전용 Service
// 삭제되지 않은 회원 조회와 선수 역할 검증을 공통으로 처리

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private static final String ROLE_PLAYER = "PLAYER";

    private final MemberRepository memberRepository;

    // 로그인 회원을 삭제되지 않은 회원인지 DB에서 조회
    public MemberEntity findLoginMember(
            CustomUserPrincipal principal,
            ErrorCode notFoundErrorCode
    ) {
        if (principal == null || principal.getMemberId() == null) {
            throw new CustomException(notFoundErrorCode);
        }

        return findActiveMemberById(principal.getMemberId(), notFoundErrorCode);
    }

    // 삭제되지 않은 회원 조회
    public MemberEntity findActiveMemberById(
            Integer memberId,
            ErrorCode notFoundErrorCode
    ) {
        if (memberId == null) {
            throw new CustomException(notFoundErrorCode);
        }

        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new CustomException(notFoundErrorCode));
    }

    // 삭제되지 않은 회원(선수) 조회
    // 선수 기록, 선수 개인 분석 클립 생성/수정에서 대상 선수를 검증할 때 사용
    public MemberEntity findActivePlayerById(
            Integer playerId,
            ErrorCode notFoundErrorCode,
            ErrorCode invalidPlayerErrorCode
    ) {
        MemberEntity player = findActiveMemberById(playerId, notFoundErrorCode);

        if (!ROLE_PLAYER.equals(String.valueOf(player.getMemberRole()))) {
            throw new CustomException(invalidPlayerErrorCode);
        }

        return player;
    }

    // 특정 회원이 선수 역할인지 검증
    public void validatePlayerRole(
            MemberEntity member,
            ErrorCode invalidPlayerErrorCode
    ) {
        if (member == null || !ROLE_PLAYER.equals(String.valueOf(member.getMemberRole()))) {
            throw new CustomException(invalidPlayerErrorCode);
        }
    }
}