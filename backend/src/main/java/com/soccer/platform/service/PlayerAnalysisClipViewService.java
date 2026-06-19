package com.soccer.platform.service;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.playerclipview.PlayerClipViewPageResponseDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.entity.PlayerVideoClipViewEntity;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.repository.PlayerVideoClipViewRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;


// 선수 개인 분석 클립 조회 기록 Service
// 선수가 본인 개인 분석 클립을 상세 조회했을 때 조회 기록을 저장
// 지도자/분석관이 특정 선수의 개인 분석 클립 조회 기록을 확인
// 선수가 본인 조회 기록을 확인
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerAnalysisClipViewService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PlayerVideoClipViewRepository playerVideoClipViewRepository;
    private final MemberRepository memberRepository;

    
    // 선수 개인 분석 클립 조회 기록 저장 또는 갱신
    @Transactional
    public void recordViewIfPlayer(
            PlayerVideoClipEntity playerVideoClip,
            CustomUserPrincipal principal
    ) {
        if (!isRole(principal, "PLAYER")) {
            return;
        }

        MemberEntity targetPlayer = playerVideoClip.getPlayer();

        if (!Objects.equals(targetPlayer.getId(), principal.getMemberId())) {
            throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_ACCESS_DENIED);
        }

        LocalDateTime now = LocalDateTime.now();

        playerVideoClipViewRepository.findByPlayerVideoClipAndMember(playerVideoClip, targetPlayer)
                .ifPresentOrElse(
                        existingView -> existingView.increaseViewCount(now),
                        () -> saveFirstView(playerVideoClip, targetPlayer, now)
                );
    }

    
    // 관리용 특정 선수 조회 기록 목록 조회
    public PlayerClipViewPageResponseDTO findPlayerViewHistoriesForManagement(
            Integer playerId,
            int page,
            int size,
            CustomUserPrincipal principal
    ) {
        validateManagementRole(principal);
        validatePageRequest(page, size);

        MemberEntity player = findPlayerMember(playerId);
        Pageable pageable = PageRequest.of(page, size);

        Page<PlayerVideoClipViewEntity> viewPage =
                playerVideoClipViewRepository.findByMemberAndIsDeletedFalse(player, pageable);

        return PlayerClipViewPageResponseDTO.from(viewPage);
    }

    
    // 선수 본인 조회 기록 목록 조회
    public PlayerClipViewPageResponseDTO findMyViewHistories(
            int page,
            int size,
            CustomUserPrincipal principal
    ) {
        validatePlayerRole(principal);
        validatePageRequest(page, size);

        MemberEntity player = findPlayerMember(principal.getMemberId());
        Pageable pageable = PageRequest.of(page, size);

        Page<PlayerVideoClipViewEntity> viewPage =
                playerVideoClipViewRepository.findByMemberAndIsDeletedFalse(player, pageable);

        return PlayerClipViewPageResponseDTO.from(viewPage);
    }

    
    // 최초 조회 기록 저장
    private void saveFirstView(
            PlayerVideoClipEntity playerVideoClip,
            MemberEntity targetPlayer,
            LocalDateTime viewedAt
    ) {
        PlayerVideoClipViewEntity view = PlayerVideoClipViewEntity.createFirstView(
                playerVideoClip,
                targetPlayer,
                viewedAt
        );

        playerVideoClipViewRepository.save(view);
    }

    
    // 관리 권한 검증
    private void validateManagementRole(CustomUserPrincipal principal) {
        if (!isRole(principal, "COACH") && !isRole(principal, "ANALYST")) {
            throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_VIEW_ACCESS_DENIED);
        }
    }

    
    // 선수 권한 검증
    private void validatePlayerRole(CustomUserPrincipal principal) {
        if (!isRole(principal, "PLAYER")) {
            throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_VIEW_ACCESS_DENIED);
        }
    }

    
    // 조회 대상 선수 확인
    private MemberEntity findPlayerMember(Integer playerId) {
        MemberEntity member = memberRepository.findByIdAndIsDeletedFalse(playerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!"PLAYER".equals(String.valueOf(member.getMemberRole()))) {
            throw new CustomException(ErrorCode.PLAYER_ANALYSIS_CLIP_VIEW_ACCESS_DENIED);
        }

        return member;
    }

    
    // 페이지 요청 값 검증
    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > MAX_PAGE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }
    }

    
    // 로그인 회원 역할 확인
    private boolean isRole(CustomUserPrincipal principal, String role) {
        return role.equals(String.valueOf(principal.getMemberRole()));
    }
}