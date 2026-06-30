package com.soccer.platform.service.member;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.ApprovalStatusEnum;
import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.member.PlayerSelectResponseDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;

// 관리 화면용 선수 조회 서비스
// 선수 목록을 조회

@Service
@RequiredArgsConstructor
@Transactional
public class PlayerQueryService {
	
	private final MemberRepository memberRepository;
	
	/*
     * 관리용 선수 선택 목록 조회
     *
     * 1. 로그인 사용자가 COACH 또는 ANALYST인지 확인
     * 2. 승인 완료된 활성 PLAYER 회원만 조회
     * 3. 등번호, 이름 오름차순으로 정렬된 결과를 반환
     *
     * - isAdmin=true만으로는 허용하지 않는다.
     * - PLAYER는 이 API를 사용할 수 없다.
     */
    public List<PlayerSelectResponseDTO> findPlayersForManagement(CustomUserPrincipal principal) {
        validateManagementPlayerReadPermission(principal);

        List<MemberEntity> players =
                memberRepository.findByMemberRoleAndApprovalStatusAndIsDeletedFalseOrderByUniformNumberAscNameAsc(
                        MemberRoleEnum.PLAYER,
                        ApprovalStatusEnum.APPROVED
                );

        return players.stream()
                .map(PlayerSelectResponseDTO::from)
                .toList();
    }

    private void validateManagementPlayerReadPermission(CustomUserPrincipal principal) {
        if (principal == null) {
            throw new CustomException(ErrorCode.MANAGEMENT_PLAYER_LIST_ACCESS_DENIED);
        }

        MemberRoleEnum memberRole = principal.getMemberRole();

        if (memberRole != MemberRoleEnum.COACH && memberRole != MemberRoleEnum.ANALYST) {
            throw new CustomException(ErrorCode.MANAGEMENT_PLAYER_LIST_ACCESS_DENIED);
        }
    }
}
