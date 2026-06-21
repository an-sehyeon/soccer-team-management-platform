package com.soccer.platform.service.memberauth;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soccer.platform.common.constants.ApprovalStatusEnum;
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
 * 회원가입, 로그인, 내 정보 조회 등 인증과 관련된 비즈니스 로직을 처리
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
	private final MemberAuthValidator memberAuthValidator;
	private final AdminMemberApprovalService adminMemberApprovalService;
	
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
		MemberEntity member = memberAuthValidator.getLoginMember(request.getLoginId());
		
		memberAuthValidator.validatePasswordMatch(request.getPassword(), member.getPassword());
		memberAuthValidator.validateApprovedMember(member);
		
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
        memberAuthValidator.validateSignUpRequest(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        MemberEntity member = new MemberEntity();
        member.setLoginId(request.getLoginId());
        member.setPassword(encodedPassword);
        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setMemberRole(request.getMemberRole());

        member.setGrade(memberAuthValidator.getGradeForSave(request));
        member.setAlmaMater(memberAuthValidator.getAlmaMaterForSave(request));
        member.setUniformNumber(memberAuthValidator.getUniformNumberForSave(request));

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
        MemberEntity member = memberAuthValidator.getActiveMember(memberId);

        memberAuthValidator.validateApprovedMember(member);

        return MemberMeResponseDTO.from(member);
    }

    /*
     * 가입 승인 대기 회원 목록 조회
     * 실제 가입 승인 대기 목록 조회 책임은 AdminMemberApprovalService가 처리
     */
    public List<PendingMemberResponseDTO> findPendingMembers(Integer adminMemberId) {
        return adminMemberApprovalService.findPendingMembers(adminMemberId);
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
        return adminMemberApprovalService.updateMemberApproval(
                adminMemberId,
                targetMemberId,
                request
        );
    }
	
	
	//회원 가입 일괄 승인/거절 처리
    @Transactional
    public List<MemberApprovalResponseDTO> updateMembersApproval(
            Integer adminMemberId,
            BulkMemberApprovalRequestDTO request
    ) {
        return adminMemberApprovalService.updateMembersApproval(adminMemberId, request);
    }
}