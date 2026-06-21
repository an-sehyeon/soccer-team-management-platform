package com.soccer.platform.service.playerclipview;

import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.common.MemberQueryService;
import com.soccer.platform.service.common.PageRequestValidator;
import com.soccer.platform.service.common.PermissionValidator;

import lombok.RequiredArgsConstructor;

// 선수 개인 분석 클립 조회 기록 Validator
// 조회 기록 권한, 조회 대상 선수, 페이지 요청을 검증
@Component
@RequiredArgsConstructor
public class PlayerAnalysisClipViewValidator {

    private final PermissionValidator permissionValidator;
    private final MemberQueryService memberQueryService;
    private final PageRequestValidator pageRequestValidator;

    // 관리용 조회 기록 조회 권한 검증
    public void validateCanViewManagement(CustomUserPrincipal principal) {
        permissionValidator.requireCoachOrAnalyst(
                principal,
                ErrorCode.PLAYER_ANALYSIS_CLIP_VIEW_ACCESS_DENIED
        );
    }

    // 선수 본인 조회 기록 조회 권한 검증
    public void validateCanViewMyHistories(CustomUserPrincipal principal) {
        permissionValidator.requirePlayer(
                principal,
                ErrorCode.PLAYER_ANALYSIS_CLIP_VIEW_ACCESS_DENIED
        );
    }

    // 조회 기록 저장 대상 검증
    public void validateRecordableViewTarget(
            PlayerVideoClipEntity playerVideoClip,
            CustomUserPrincipal principal
    ) {
        if (playerVideoClip == null
                || playerVideoClip.getPlayer() == null
                || playerVideoClip.getPlayer().getId() == null
                || principal == null
                || principal.getMemberId() == null) {
            throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_ACCESS_DENIED);
        }

        if (!Objects.equals(playerVideoClip.getPlayer().getId(), principal.getMemberId())) {
            throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_ACCESS_DENIED);
        }
    }

    // 조회 대상 선수 조회
    public MemberEntity findPlayerMember(Integer playerId) {
        return memberQueryService.findActivePlayerById(
                playerId,
                ErrorCode.MEMBER_NOT_FOUND,
                ErrorCode.PLAYER_ANALYSIS_CLIP_VIEW_ACCESS_DENIED
        );
    }

    // 페이지 요청값 검증 후 Pageable 생성
    public Pageable createPageable(Integer page, Integer size) {
        return pageRequestValidator.createPageable(page, size);
    }

    // 로그인 사용자가 선수인지 확인
    public boolean isPlayer(CustomUserPrincipal principal) {
        return permissionValidator.isPlayer(principal);
    }
}