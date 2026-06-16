package com.soccer.platform.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soccer.platform.common.constants.ApprovalStatusEnum;
import com.soccer.platform.common.constants.MemberRoleEnum;
import com.soccer.platform.entity.MemberEntity;

/*
 * 회원 Repository
 * 
 * 회원가입, 로그인, 내 정보 조회, 가입 승인 기능에서
 * 회원 정보를 조회하기 위한 Repository.
 * 
 * 주요 역할
 * - 로그인 ID 중복 확인
 * - 로그인 ID로 회원 조회
 * - 회원ID로 정상 회원 조회
 * - 승인 대기 회원 목록 조회
 * */
public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {

	/*
	 * 로그인ID 중복 확인
	 * 
	 * 회원가입 시 같은 loginId가 이미 존재하는지 확인한다
	 * login_id는 DB에서 UNIQUE 제약 조건이 있으므로
	 * 삭제된 회원이라도 같은 loginId 재사용은 우선 막는다.
	 * */
	boolean existsBexistsByLoginId(String loginId);
	
	/*
	 * 로그인ID로 삭제되지 않은 회원 조회
	 * 
	 * 로그인 처리 시 사용.
	 * 삭제된 회원은 로그인 대상에서 제외한다.
	 * */
	Optional<MemberEntity> findByLoginIdAndIsDeletedFalse(String loginId);
	
	/*
	 * 회원ID로 삭제되지 않은 회원 조회
	 * 
	 * JWT 인증 이후 현재 로그인 회원을 다시 확인할 때 사용한다.
	 * */
	Optional<MemberEntity> findByIdAndIsDeletedFalse(Integer id);
	
	/*
	 * 승인 상태별 회웜 목록 조회
	 * 
	 * 관리자 화면에서 PENDING 회원 목록을 조회할 때 사용.
	 * */
	List<MemberEntity> findByApprovalStatusAndIsDeletedFalse(ApprovalStatusEnum approvalStatus);
	
	/*
	 * 선수 등번호 중복 확인
	 * 삭제되지 않은 선수 중 PENDING, APPROVED 상태의 등번호 중복을 확인
     * REJECTED 회원의 등번호는 재사용 가능하게 본다
	 */
	boolean existsByUniformNumberAndMemberRoleAndApprovalStatusInAndIsDeletedFalse(
            Integer uniformNumber,
            MemberRoleEnum memberRole,
            Collection<ApprovalStatusEnum> approvalStatuses
    );
	
	/*
	 * 여러 회원 ID로 삭제되지 않은 회원 목록 조회
	 * 관리자 일괄 승인/거절 처리에서 사용
	 */
	List<MemberEntity> findByIdInAndIsDeletedFalse(List<Integer> ids);
	
	
}
