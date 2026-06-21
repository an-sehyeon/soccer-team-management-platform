package com.soccer.platform.service.memberauth;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.soccer.platform.common.constants.ApprovalStatusEnum;
import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.auth.SignUpRequestDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

/*
 회원 인증 컴포넌트
 - 회원가입, 로그인, 내 정보 조회, 관리자 승인 기능에서 사용하는 검증 로직을 담당
 회원가입 검증
 로그인 검증
 관리자 검증
 승인 상태 검증
 선수 등번호/학년 검증
 */

@Component
@RequiredArgsConstructor
public class MemberAuthValidator {
	
	private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
	
	// 로그인 대상 회원 조회
	public MemberEntity getLoginMember(String loginId) {
        return memberRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));
    }
	
	// 활성 회원 조회
	// 내 정보 조회, 관리자 검증, 승인 대상 조회에서 사용
	public MemberEntity getActiveMember(Integer memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
	
	// 회원가입 요청 검증
	public void validateSignUpRequest(SignUpRequestDTO request) {
        validateDuplicateLoginId(request.getLoginId());
        validateJoinableMemberRole(request.getMemberRole());
        validateRoleBasedRequiredFields(request);
        validatePassword(request.getLoginId(), request.getPassword());
        validateDuplicateUniformNumber(request);
    }
	
	// 로그인 비밀번호 검증
	public void validatePasswordMatch(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }
    }
	
	// 승인 완료 회원인지 검증
	public void validateApprovedMember(MemberEntity member) {
        if (member.getApprovalStatus() == ApprovalStatusEnum.PENDING) {
            throw new CustomException(ErrorCode.PENDING_MEMBER);
        }

        if (member.getApprovalStatus() == ApprovalStatusEnum.REJECTED) {
            throw new CustomException(ErrorCode.REJECTED_MEMBER);
        }

        if (member.getApprovalStatus() != ApprovalStatusEnum.APPROVED) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }
    }
	
	// 관리자 회원 검증
	// 가입 승인은 isAdmin = true인 회원만 가능
	// 관리자도 승인 완료 상태여야 실제 관리 기능 사용 가능
	public void validateAdminMember(Integer adminMemberId) {
        MemberEntity adminMember = getActiveMember(adminMemberId);

        if (!Boolean.TRUE.equals(adminMember.getIsAdmin())) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        if (adminMember.getApprovalStatus() != ApprovalStatusEnum.APPROVED) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
	
	// 변경 가능한 승인 상태 검증
	public void validateChangeableApprovalStatus(ApprovalStatusEnum approvalStatus) {
        if (approvalStatus == null) {
            throw new CustomException(ErrorCode.INVALID_APPROVAL_STATUS);
        }

        if (
                approvalStatus != ApprovalStatusEnum.APPROVED
                        && approvalStatus != ApprovalStatusEnum.REJECTED
        ) {
            throw new CustomException(ErrorCode.INVALID_APPROVAL_STATUS);
        }
    }
	
	// 일괄 승인/거절 대상 회원 ID 검증
	// 체크박스에서 아무도 선택하지 않은 상태로 요청하는 것을 막기 위함
	public void validateSelectedMemberIds(List<Integer> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_MEMBER_SELECTION);
        }
    }
	
	// 승인/거절 대상 관리자 계정 검증
	// 관리자 계정은 승인/거절 대상이 불가능
	public void validateNotAdminApprovalTarget(MemberEntity targetMember) {
        if (Boolean.TRUE.equals(targetMember.getIsAdmin())) {
            throw new CustomException(ErrorCode.CANNOT_APPROVE_ADMIN);
        }
    }
	
	// 로그인 ID 중복 검증
	private void validateDuplicateLoginId(String loginId) {
        if (memberRepository.existsBexistsByLoginId(loginId)) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
    }

	// 가입 가능한 역할 검증
	private void validateJoinableMemberRole(MemberRoleEnum memberRole) {
        if (memberRole == null) {
            throw new CustomException(ErrorCode.INVALID_MEMBER_ROLE);
        }

        if (
                memberRole != MemberRoleEnum.COACH
                        && memberRole != MemberRoleEnum.PLAYER
                        && memberRole != MemberRoleEnum.ANALYST
        ) {
            throw new CustomException(ErrorCode.INVALID_MEMBER_ROLE);
        }
    }
	
	// 비밀번호 정책 검증
	// DTO에서 1차 길이 검증을 하지만 서비스에도 검증을 수행
	private void validatePassword(String loginId, String password) {
        if (password == null || password.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        if (password.equals(loginId)) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
    }
	
	/*
	  역할별 회원가입 필수값 검증
	  - 선수는 학년과 등번호가 필수
	  - 학년은 1~4 사이만 허용
	  - 지도자와 분석관은 전용 정보를 필수로 입력하지 않는다.
	 */
	private void validateRoleBasedRequiredFields(SignUpRequestDTO request) {
        if (request.getMemberRole() != MemberRoleEnum.PLAYER) {
            return;
        }

        if (request.getGrade() == null) {
            throw new CustomException(ErrorCode.PLAYER_GRADE_REQUIRED);
        }

        if (request.getGrade() < 1 || request.getGrade() > 4) {
            throw new CustomException(ErrorCode.INVALID_GRADE);
        }

        if (request.getUniformNumber() == null) {
            throw new CustomException(ErrorCode.PLAYER_UNIFORM_NUMBER_REQUIRED);
        }
    }
	
	// 선수 등번호 중복 검증
	// 삭제되지 않은 선수 중 승인 대기 또는 승인 완료 상태의 등번호와 중복되면 가입을 막는다
	// 승인 거절된 회원의 등번호는 재사용 가능
	private void validateDuplicateUniformNumber(SignUpRequestDTO request) {
        if (request.getMemberRole() != MemberRoleEnum.PLAYER) {
            return;
        }

        boolean existsUniformNumber = memberRepository
                .existsByUniformNumberAndMemberRoleAndApprovalStatusInAndIsDeletedFalse(
                        request.getUniformNumber(),
                        MemberRoleEnum.PLAYER,
                        List.of(
                                ApprovalStatusEnum.PENDING,
                                ApprovalStatusEnum.APPROVED
                        )
                );

        if (existsUniformNumber) {
            throw new CustomException(ErrorCode.DUPLICATE_UNIFORM_NUMBER);
        }
    }
	
	// 저장할 학년 값 검증
	public Integer getGradeForSave(SignUpRequestDTO request) {
        if (request.getMemberRole() == MemberRoleEnum.PLAYER) {
            return request.getGrade();
        }

        return null;
    }
	
	// 저장할 출신학교 값 검증
	public String getAlmaMaterForSave(SignUpRequestDTO request) {
        if (request.getMemberRole() == MemberRoleEnum.PLAYER) {
            return request.getAlmaMater();
        }

        return null;
    }
	
	// 저장할 등번호 값 결정
	public Integer getUniformNumberForSave(SignUpRequestDTO request) {
        if (request.getMemberRole() == MemberRoleEnum.PLAYER) {
            return request.getUniformNumber();
        }

        return null;
    }

}
