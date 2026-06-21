package com.soccer.platform.service.memberauth;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.ApprovalStatusEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.auth.BulkMemberApprovalRequestDTO;
import com.soccer.platform.dto.auth.MemberApprovalRequestDTO;
import com.soccer.platform.dto.auth.MemberApprovalResponseDTO;
import com.soccer.platform.dto.auth.PendingMemberResponseDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

/*
 관리자 회원 승인 서비스
 - 가입 승인 대기 회원 조회, 단일 승인/거절, 일괄 승인/거절을 처리
 
 승인 대기 목록 조회
 단일 승인/거절
 일괄 승인/거절
*/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberApprovalService {
	
	private final MemberRepository memberRepository;
    private final MemberAuthValidator memberAuthValidator;
    
    // 가입 승인 대기 회원 목록 조회
    public List<PendingMemberResponseDTO> findPendingMembers(Integer adminMemberId) {
        memberAuthValidator.validateAdminMember(adminMemberId);

        return memberRepository.findByApprovalStatusAndIsDeletedFalse(ApprovalStatusEnum.PENDING)
                .stream()
                .map(PendingMemberResponseDTO::from)
                .toList();
    }
    
    // 회원 가입 승인/거절 처리
    @Transactional
    public MemberApprovalResponseDTO updateMemberApproval(
            Integer adminMemberId,
            Integer targetMemberId,
            MemberApprovalRequestDTO request
    ) {
        memberAuthValidator.validateAdminMember(adminMemberId);
        memberAuthValidator.validateChangeableApprovalStatus(request.getApprovalStatus());

        MemberEntity targetMember = memberAuthValidator.getActiveMember(targetMemberId);

        memberAuthValidator.validateNotAdminApprovalTarget(targetMember);

        targetMember.setApprovalStatus(request.getApprovalStatus());

        return MemberApprovalResponseDTO.from(targetMember);
    }
    
    // 회원가입 일괄 승인/거절 처리
    @Transactional
    public List<MemberApprovalResponseDTO> updateMembersApproval(
            Integer adminMemberId,
            BulkMemberApprovalRequestDTO request
    ) {
        memberAuthValidator.validateAdminMember(adminMemberId);
        memberAuthValidator.validateSelectedMemberIds(request.getMemberIds());
        memberAuthValidator.validateChangeableApprovalStatus(request.getApprovalStatus());

        List<MemberEntity> targetMembers =
                memberRepository.findByIdInAndIsDeletedFalse(request.getMemberIds());

        if (targetMembers.size() != request.getMemberIds().size()) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        for (MemberEntity targetMember : targetMembers) {
            memberAuthValidator.validateNotAdminApprovalTarget(targetMember);

            targetMember.setApprovalStatus(request.getApprovalStatus());
        }

        return targetMembers.stream()
                .map(MemberApprovalResponseDTO::from)
                .toList();
    }

}
