package com.soccer.platform.dto.auth;

import java.util.List;

import com.soccer.platform.common.constants.ApprovalStatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * 회원 일괄 승인/거절 요청 DTO
 *
 * 관리자가 승인 대기 목록에서 여러 회원을 선택해
 * 한 번에 승인 또는 거절할 때 사용한다.
 */
@Getter
@NoArgsConstructor
public class BulkMemberApprovalRequestDTO {

    @NotNull(message = "승인 처리할 회원 목록은 필수입니다.")
    private List<Integer> memberIds;

    @NotNull(message = "승인 상태는 필수입니다.")
    private ApprovalStatusEnum approvalStatus;
}