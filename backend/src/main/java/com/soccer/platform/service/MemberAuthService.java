package com.soccer.platform.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.ApprovalStatusEnum;
import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.common.exception.CustomException;
import com.soccer.platform.common.exception.ErrorCode;
import com.soccer.platform.dto.auth.BulkMemberApprovalRequestDTO;
import com.soccer.platform.dto.auth.LoginRequestDTO;
import com.soccer.platform.dto.auth.LoginResponseDTO;
import com.soccer.platform.dto.auth.MemberApprovalRequestDTO;
import com.soccer.platform.dto.auth.MemberApprovalResponseDTO;
import com.soccer.platform.dto.auth.MemberMeResponseDTO;
import com.soccer.platform.dto.auth.PendingMemberResponseDTO;
import com.soccer.platform.dto.auth.SignUpRequestDTO;
import com.soccer.platform.dto.auth.SignUpResponseDTO;
import com.soccer.platform.entity.MemberEntity;
import com.soccer.platform.repository.MemberRepository;
import com.soccer.platform.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

/*
 * 회원 인증 서비스
 * 
 * 회원가입, 로그인, 내 정보 조회 등 인증과 관련된 비즈니스 로직을 처리한다.
 * 
 * 현재 구현 범위
 * - 회원가입 처리
 * 
 * 추후 확장 예정
 * - 로그인 처리
 * - JWT 발급
 * - 내 정보 조회
 * - 가입 승인 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAuthService {
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	
	/*
	 * 로그인 처리
	 * 
	 * 1. 로그인ID로 삭제되지 않은 회원 조회.
	 * 2. 비밀번호 일치하는지 확인.
	 * 3. 승인 상태가 APPROVED인지 확인.
	 * 4. 마지막 로그인 시간을 갱신
	 * 5. JWT Access Token을 발급.
	 * 6. 로그인 응답 DTO를 반환.
	 * 
	 * - 로그인 ID가 없거나 비밀번호가 틀릴 경우 같은 메시지로 처리한다.
	 * - 승인 대기/거절 회원은 별도 메시지로 처리.
	 */
	@Transactional
	public LoginResponseDTO login(LoginRequestDTO request) {
		MemberEntity member = memberRepository.findByLoginIdAndIsDeletedFalse(request.getLoginId())
					.orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));
		
		 validatePasswordMatch(request.getPassword(), member.getPassword());
		 validateApprovedMember(member);
		 
		 member.setLastLoginAt(LocalDateTime.now());
		 
		 String accessToken = jwtTokenProvider.createAccessToken(member);
		 
		 return LoginResponseDTO.of(accessToken, member);
	}
	


	/*
	 * 회원가입 처리
	 * 
	 * 처리 흐름
	 * 1.로그인ID 중복 여부 확인
	 * 2.가입 가능한 역할인지 확인
	 * 3.비밀번호 형식 추가 검증
	 * 4.비밀번호를 BCryp로 암호화
	 * 5.신규 회원을 PENDING 상태로 저장
	 * 
	 * 주의사항
	 * - 일반 회원가입으로 관리자 계정은 만들지 않는다.
	 * - 회원가입 직후 자동 로그인은 하지 않는다.
	 * - 승인 완료 전까지 로그인할 수 없다.
	 */
	@Transactional
	public SignUpResponseDTO signUp(SignUpRequestDTO request) {
	    validateDuplicateLoginId(request.getLoginId());
	    validateJoinableMemberRole(request.getMemberRole());
	    validateRoleBasedRequiredFields(request);
	    validatePassword(request.getLoginId(), request.getPassword());
	    validateDuplicateUniformNumber(request.getUniformNumber());

	    String encodedPassword = passwordEncoder.encode(request.getPassword());

	    MemberEntity member = new MemberEntity();
	    member.setLoginId(request.getLoginId());
	    member.setPassword(encodedPassword);
	    member.setName(request.getName());
	    member.setPhone(request.getPhone());
	    member.setMemberRole(request.getMemberRole());

	    member.setGrade(getGradeForSave(request));
	    member.setAlmaMater(getAlmaMaterForSave(request));
	    member.setUniformNumber(getUniformNumberForSave(request));

	    member.setApprovalStatus(ApprovalStatusEnum.PENDING);
	    member.setIsCaptain(false);
	    member.setIsAdmin(false);
	    member.setIsDeleted(false);

	    MemberEntity savedMember = memberRepository.save(member);

	    return SignUpResponseDTO.pending(savedMember.getId());
	}
	
	

	/*
	 * 내 정보 조회
	 * 
	 * 1. JWT 인증으로 확인된 회원ID를 받는다.
	 * 2. 삭제되지 않은 회원을 조회.
	 * 3. 승인 완료 회원인지 확인.
	 * 4. 현재 회원 정보 반환.
	 */
	public MemberMeResponseDTO getMyInfo(Integer memberId) {
	    MemberEntity member = memberRepository.findByIdAndIsDeletedFalse(memberId)
	            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

	    validateApprovedMember(member);

	    return MemberMeResponseDTO.from(member);
	}
	
	
	/*
	 * 가입 승인 대기 회원 목록 조회
	 * 
	 * 1. 요청한 사용자가 관리자인지 확인
	 * 2. PENDING 상태의 삭제되지 않은 회원 목록 조회.
	 * 3. 관리자 화면에서 사용할 응답 DTO로 변환
	 */
	public List<PendingMemberResponseDTO> findPendingMembers(Integer adminMemberId) {
	    validateAdminMember(adminMemberId);

	    return memberRepository.findByApprovalStatusAndIsDeletedFalse(ApprovalStatusEnum.PENDING)
	            .stream()
	            .map(PendingMemberResponseDTO::from)
	            .toList();
	}
	
	
	/**
	 * 회원 가입 승인/거절 처리
	 *
	 * 1. 요청한 사용자가 관리자인지 확인.
	 * 2. 대상 회원이 존재하는지 확인.
	 * 3. 변경 가능한 승인 상태인지 확인.
	 * 4. 관리자 계정은 변경하지 못하게 막는다.
	 * 5. 승인 상태를 APPROVED 또는 REJECTED로 변경한다.
	 */
	@Transactional
	public MemberApprovalResponseDTO updateMemberApproval(
	        Integer adminMemberId,
	        Integer targetMemberId,
	        MemberApprovalRequestDTO request
	) {
	    validateAdminMember(adminMemberId);
	    validateChangeableApprovalStatus(request.getApprovalStatus());

	    MemberEntity targetMember = memberRepository.findByIdAndIsDeletedFalse(targetMemberId)
	            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

	    if (Boolean.TRUE.equals(targetMember.getIsAdmin())) {
	        throw new CustomException(ErrorCode.CANNOT_APPROVE_ADMIN);
	    }

	    targetMember.setApprovalStatus(request.getApprovalStatus());

	    return MemberApprovalResponseDTO.from(targetMember);
	}
	
	
	//회원 가입 일괄 승인/거절 처리
	@Transactional
	public List<MemberApprovalResponseDTO> updateMembersApproval(
	        Integer adminMemberId,
	        BulkMemberApprovalRequestDTO request
	) {
	    validateAdminMember(adminMemberId);
	    validateSelectedMemberIds(request.getMemberIds());
	    validateChangeableApprovalStatus(request.getApprovalStatus());

	    List<MemberEntity> targetMembers =
	            memberRepository.findByIdInAndIsDeletedFalse(request.getMemberIds());

	    if (targetMembers.size() != request.getMemberIds().size()) {
	        throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
	    }

	    for (MemberEntity targetMember : targetMembers) {
	        if (Boolean.TRUE.equals(targetMember.getIsAdmin())) {
	            throw new CustomException(ErrorCode.CANNOT_APPROVE_ADMIN);
	        }

	        targetMember.setApprovalStatus(request.getApprovalStatus());
	    }

	    return targetMembers.stream()
	            .map(MemberApprovalResponseDTO::from)
	            .toList();
	}
	
	
	
	/**
	 * 변경 가능한 승인 상태 검증
	 *
	 * 관리자 승인 API에서는 APPROVED 또는 REJECTED만 허용.
	 */
	private void validateChangeableApprovalStatus(ApprovalStatusEnum approvalStatus) {
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



	/**
	 * 관리자 회원 검증
	 *
	 * 가입 승인 기능은 isAdmin = true인 회원만 사용할 수 있다.
	 * 또한 관리자도 승인 완료 상태여야 실제 관리 기능을 사용할 수 있다.
	 */
	private void validateAdminMember(Integer adminMemberId) {
	    MemberEntity adminMember = memberRepository.findByIdAndIsDeletedFalse(adminMemberId)
	            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

	    if (!Boolean.TRUE.equals(adminMember.getIsAdmin())) {
	        throw new CustomException(ErrorCode.ADMIN_ONLY);
	    }

	    if (adminMember.getApprovalStatus() != ApprovalStatusEnum.APPROVED) {
	        throw new CustomException(ErrorCode.ACCESS_DENIED);
	    }
	}



	// 로그인ID 중복 검증
	// 같은 로그인 ID가 이미 존재하면 회원가입을 막는다
	private void validateDuplicateLoginId(String loginId) {
		if(memberRepository.existsBexistsByLoginId(loginId)) {
			throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
		}
		
	}
		
	// 가입 가능한 역할 검증
	// COACH, PLAYER, ANALYST 가입을 허용
	// 관리자 권한은 일반 회원가입으로 부여하지 않는다
	private void validateJoinableMemberRole(MemberRoleEnum memberRole) {
		if(memberRole == null) {
			throw new CustomException(ErrorCode.INVALID_MEMBER_ROLE);
		}
		
		if(
				memberRole != MemberRoleEnum.COACH
					&& memberRole != MemberRoleEnum.PLAYER
					&& memberRole != MemberRoleEnum.ANALYST
		) {
			throw new CustomException(ErrorCode.INVALID_MEMBER_ROLE);
		}
		
	}	

	// 비밀번호 정책 검증
	// DTO에서 1차 길이 검증을 하지만 서비스에도 운영 정책에 필요한 검증을 수행
	private void validatePassword(String loginId, String password) {
		if(password == null || password.isBlank()) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
		}
		
		if(password.equals(loginId)) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
		}
	}
	
	// 저장할 학년 값 결정
	// 선수만 학년을 저장하낟.
	// 지도자와 분석관은 프론트 조작으로 값이 넘어와도 저장하지 않는다.
	private Integer getGradeForSave(SignUpRequestDTO request) {
	    if (request.getMemberRole() == MemberRoleEnum.PLAYER) {
	        return request.getGrade();
	    }

	    return null;
	}

	// 저장할 출신학교 값 결정
	// 선수만 출신학교를 저장한다.
	private String getAlmaMaterForSave(SignUpRequestDTO request) {
	    if (request.getMemberRole() == MemberRoleEnum.PLAYER) {
	        return request.getAlmaMater();
	    }

	    return null;
	}

	// 저장할 등번호 값 결정
	// 선수만 등번호를 저장한다.
	// 지도자와 분석관은 프론트에서 값이 넘어와도 저장하지 않는다.
	private Integer getUniformNumberForSave(SignUpRequestDTO request) {
	    if (request.getMemberRole() == MemberRoleEnum.PLAYER) {
	        return request.getUniformNumber();
	    }

	    return null;
	}
	
	 // 역할별 회원가입 필수값 검증
	 // 선수는 학년과 등번호가 필수이다.
	 // 학년은 1~4 사이만 허용한다.	
	 // 지도자와 분석관은 선수 전용 정보를 필수로 입력하지 않는다.
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
	
	/*
	 * 승인 완료 회원인지 검증
	 * 
	 * 승인 대기 또는 승인 거절 회원은 로그인 불가.
	 */
	private void validateApprovedMember(MemberEntity member) {
		if(member.getApprovalStatus() == ApprovalStatusEnum.PENDING) {
			throw new CustomException(ErrorCode.PENDING_MEMBER);
		}
		
		if(member.getApprovalStatus() == ApprovalStatusEnum.PENDING) {
			throw new CustomException(ErrorCode.REJECTED_MEMBER);
		}
		
		if(member.getApprovalStatus() != ApprovalStatusEnum.APPROVED) {
			throw new CustomException(ErrorCode.LOGIN_FAILED);
		}
		
	}


	/*
	 * 로그인 비밀번호 검증
	 * 
	 * 입력 비밀번호와 DB에 저장된 BCrypt 해시 값 비교.
	 */
	private void validatePasswordMatch(String rawPassword, String encodedPassword) {
		if(!passwordEncoder.matches(rawPassword, encodedPassword)) {
			throw new CustomException(ErrorCode.LOGIN_FAILED);
		}
	}
	
	/*
	 * 선수 등번호 중복 검증
	 * 삭제되지 않은 선수 중 승인 대기 또는 승인 완료 상태의 등번호와 중복되면 가입을 막는다.
	 * 승인 거절된 회원의 등번호는 재사용 가능하게 본다.
	 */
	private void validateDuplicateUniformNumber(Integer uniformNumber) {
	    boolean existsUniformNumber = memberRepository
	            .existsByUniformNumberAndMemberRoleAndApprovalStatusInAndIsDeletedFalse(
	                    uniformNumber,
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
	
	
	/*
	 * 일괄 승인/거절 대상 회원 ID 검증
	 * 체크박스에서 아무도 선택하지 않은 상태로 요청하는 것을 막는다.
	 */
	private void validateSelectedMemberIds(List<Integer> memberIds) {
	    if (memberIds == null || memberIds.isEmpty()) {
	        throw new CustomException(ErrorCode.EMPTY_MEMBER_SELECTION);
	    }
	}
	
	
	
}
