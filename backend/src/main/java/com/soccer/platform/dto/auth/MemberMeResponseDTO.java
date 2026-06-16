package com.soccer.platform.dto.auth;

import com.soccer.platform.common.constants.ApprovalStatusEnum;
import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * 내 정보 조회 응답 DTO
 * 
 * 현재 로그인한 회원의 기본 정보를 반환.
 */

@Getter
@AllArgsConstructor
public class MemberMeResponseDTO {

	private Integer memberId;
    private String loginId;
    private String name;
    private String phone;
    private MemberRoleEnum memberRole;
    private ApprovalStatusEnum approvalStatus;
    private Boolean isAdmin;
    private Boolean isCaptain;
    private Integer grade;
    private String almaMater;
    private Integer uniformNumber;
    
    public static MemberMeResponseDTO from(MemberEntity member) {
        return new MemberMeResponseDTO(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getPhone(),
                member.getMemberRole(),
                member.getApprovalStatus(),
                member.getIsAdmin(),
                member.getIsCaptain(),
                member.getGrade(),
                member.getAlmaMater(),
                member.getUniformNumber()
        );
    }
}
