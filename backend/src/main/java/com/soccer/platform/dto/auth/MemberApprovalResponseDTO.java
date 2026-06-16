package com.soccer.platform.dto.auth;

import com.soccer.platform.common.constants.ApprovalStatusEnum;
import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 회원 가입 승인/거절 응답 DTO
 *
 * 관리자가 회원 승인 상태를 변경한 뒤
 * 변경된 회원 정보를 반환한다.
 */
@Getter
@AllArgsConstructor
public class MemberApprovalResponseDTO {

    private Integer memberId;
    private String loginId;
    private String name;
    private MemberRoleEnum memberRole;
    private ApprovalStatusEnum approvalStatus;
    private String message;

    public static MemberApprovalResponseDTO from(MemberEntity member) {
        return new MemberApprovalResponseDTO(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getMemberRole(),
                member.getApprovalStatus(),
                createMessage(member.getApprovalStatus())
        );
    }

    private static String createMessage(ApprovalStatusEnum approvalStatus) {
        if (approvalStatus == ApprovalStatusEnum.APPROVED) {
            return "회원 가입을 승인했습니다.";
        }

        if (approvalStatus == ApprovalStatusEnum.REJECTED) {
            return "회원 가입을 거절했습니다.";
        }

        return "회원 승인 상태가 변경되었습니다.";
    }
}