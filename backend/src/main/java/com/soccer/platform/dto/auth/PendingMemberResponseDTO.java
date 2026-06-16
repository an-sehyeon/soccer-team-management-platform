package com.soccer.platform.dto.auth;

import java.time.LocalDateTime;

import com.soccer.platform.common.constants.ApprovalStatusEnum;
import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 가입 승인 대기 회원 응답 DTO
 *
 * 관리자가 승인 대기 회원 목록을 조회할 때 사용한다.
 */
@Getter
@AllArgsConstructor
public class PendingMemberResponseDTO {

    private Integer memberId;
    private String loginId;
    private String name;
    private String phone;
    private MemberRoleEnum memberRole;
    private ApprovalStatusEnum approvalStatus;
    private Integer grade;
    private String almaMater;
    private Integer uniformNumber;
    private LocalDateTime createdAt;

    public static PendingMemberResponseDTO from(MemberEntity member) {
        return new PendingMemberResponseDTO(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getPhone(),
                member.getMemberRole(),
                member.getApprovalStatus(),
                member.getGrade(),
                member.getAlmaMater(),
                member.getUniformNumber(),
                member.getCreatedAt()
        );
    }
}