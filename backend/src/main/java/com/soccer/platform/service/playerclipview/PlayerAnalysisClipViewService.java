package com.soccer.platform.service.playerclipview;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.dto.playerclipview.PlayerClipViewPageResponseDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.entity.PlayerVideoClipEntity;
import com.soccer.platform.entity.PlayerVideoClipViewEntity;
import com.soccer.platform.repository.PlayerVideoClipViewRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

// 선수 개인 분석 클립 조회 기록 Service
// 선수의 개인 분석 클립 상세 조회 기록을 저장하고 조회
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerAnalysisClipViewService {

    private final PlayerVideoClipViewRepository playerVideoClipViewRepository;
    private final PlayerAnalysisClipViewValidator playerAnalysisClipViewValidator;

    // 선수 개인 분석 클립 조회 기록 저장 또는 갱신
    @Transactional
    public void recordViewIfPlayer(
            PlayerVideoClipEntity playerVideoClip,
            CustomUserPrincipal principal
    ) {
        if (!playerAnalysisClipViewValidator.isPlayer(principal)) {
            return;
        }

        playerAnalysisClipViewValidator.validateRecordableViewTarget(
                playerVideoClip,
                principal
        );

        MemberEntity targetPlayer = playerVideoClip.getPlayer();
        LocalDateTime now = LocalDateTime.now();

        playerVideoClipViewRepository.findByPlayerVideoClipAndMember(
                        playerVideoClip,
                        targetPlayer
                )
                .ifPresentOrElse(
                        existingView -> updateExistingView(existingView, now),
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
        playerAnalysisClipViewValidator.validateCanViewManagement(principal);

        MemberEntity player = playerAnalysisClipViewValidator.findPlayerMember(playerId);
        Pageable pageable = playerAnalysisClipViewValidator.createPageable(page, size);

        Page<PlayerVideoClipViewEntity> viewPage =
                playerVideoClipViewRepository.findByMemberAndIsDeletedFalse(
                        player,
                        pageable
                );

        return PlayerClipViewPageResponseDTO.from(viewPage);
    }

    // 선수 본인 조회 기록 목록 조회
    public PlayerClipViewPageResponseDTO findMyViewHistories(
            int page,
            int size,
            CustomUserPrincipal principal
    ) {
        playerAnalysisClipViewValidator.validateCanViewMyHistories(principal);

        MemberEntity player =
                playerAnalysisClipViewValidator.findPlayerMember(principal.getMemberId());

        Pageable pageable = playerAnalysisClipViewValidator.createPageable(page, size);

        Page<PlayerVideoClipViewEntity> viewPage =
                playerVideoClipViewRepository.findByMemberAndIsDeletedFalse(
                        player,
                        pageable
                );

        return PlayerClipViewPageResponseDTO.from(viewPage);
    }

    // 기존 조회 기록 갱신
    private void updateExistingView(
            PlayerVideoClipViewEntity existingView,
            LocalDateTime viewedAt
    ) {
        if (Boolean.TRUE.equals(existingView.getIsDeleted())) {
            existingView.setIsDeleted(false);
        }

        existingView.increaseViewCount(viewedAt);
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
}