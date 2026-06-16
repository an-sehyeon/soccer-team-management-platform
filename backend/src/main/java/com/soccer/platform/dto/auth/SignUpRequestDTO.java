package com.soccer.platform.dto.auth;

import com.soccer.platform.common.constants.MemberRoleEnum;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * 회원가입 요청 DTO
 * 
 * 회원가입 화면에서 회원 기본 정보를 전달받는다.
 * 
 * 주의사항
 * - isAdmin 값은 요청으로 받지 않는다.
 * - approvalStatus 값도 요청으로 받지 않는다.
 * - 신규 가입자는 Service에서 PENDING 상태로 저장하낟.
 */
@Getter
@NoArgsConstructor
public class SignUpRequestDTO {
	
	@NotBlank(message = "로그인 ID는 필수입니다.")
    @Size(max = 255, message = "로그인 ID는 255자 이하로 입력해야 합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하로 입력해야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 20, message = "이름은 20자 이하로 입력해야 합니다.")
    private String name;

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(
            regexp = "^01[0-9]{8,9}$",
            message = "휴대폰 번호 형식이 올바르지 않습니다. 예: 01012345678"
    )
    private String phone;

    @NotNull(message = "회원 역할은 필수입니다.")
    private MemberRoleEnum memberRole;

    @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
    @Max(value = 4, message = "학년은 4 이하로 입력해야 합니다.")
    private Integer grade;

    @Size(max = 30, message = "출신학교는 30자 이하로 입력해야 합니다.")
    private String almaMater;

    @Min(value = 1, message = "등번호는 1 이상이어야 합니다.")
    @Max(value = 255, message = "등번호는 255 이하로 입력해야 합니다.")
    private Integer uniformNumber;

}
