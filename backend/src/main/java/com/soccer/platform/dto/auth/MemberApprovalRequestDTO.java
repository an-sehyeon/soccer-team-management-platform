package com.soccer.platform.dto.auth;

import com.soccer.platform.common.constants.ApprovalStatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 가입 승인/거절 요청 DTO
 *
 * 관리자가 가입 대기 회원의 승인 상태를 변경할 때 사용.
 *
 * 허용 상태
 * - APPROVED
 * - REJECTED
 */
@Getter
@NoArgsConstructor
public class MemberApprovalRequestDTO {

    @NotNull(message = "승인 상태는 필수입니다.")
    private ApprovalStatusEnum approvalStatus;
}