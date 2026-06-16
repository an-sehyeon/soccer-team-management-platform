package com.soccer.platform.dto.auth;

import com.soccer.platform.common.constants.ApprovalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 회원가입 응답 DTO
 *
 * 회원가입 신청 완료 후 클라이언트에 가입 상태를 알려준다.
 */
@Getter
@AllArgsConstructor
public class SignUpResponseDTO {

    private Integer memberId;
    private ApprovalStatusEnum approvalStatus;
    private String message;

    public static SignUpResponseDTO pending(Integer memberId) {
        return new SignUpResponseDTO(
                memberId,
                ApprovalStatusEnum.PENDING,
                "회원가입 신청이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다."
        );
    }
}