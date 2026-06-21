package com.soccer.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.platform.dto.auth.MemberApprovalRequestDTO;
import com.soccer.platform.dto.auth.MemberApprovalResponseDTO;
import com.soccer.platform.dto.auth.PendingMemberResponseDTO;
import com.soccer.platform.dto.auth.BulkMemberApprovalRequestDTO;
import com.soccer.platform.security.CustomUserPrincipal;
import com.soccer.platform.service.memberauth.MemberAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * 관리자 회원 관리 Controller
 * 
 * 관리자 계정이 가입 대기 회원을 조회하고,
 * 회원 가입을 승인 또는 거절하는 API를 제공.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
public class AdminMemberController {

	private final MemberAuthService memberAuthService;
	
	
	// 가입 승인 대기 회원 목록 조회 API
	@GetMapping("/pending")
    public ResponseEntity<List<PendingMemberResponseDTO>> findPendingMembers(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<PendingMemberResponseDTO> response =
                memberAuthService.findPendingMembers(principal.getMemberId());

        return ResponseEntity.ok(response);
    }
	
	// 회원 가입 승인/거절 API
	@PatchMapping("/{memberId}/approval")
    public ResponseEntity<MemberApprovalResponseDTO> updateMemberApproval(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("memberId") Integer memberId,
            @Valid @RequestBody MemberApprovalRequestDTO request
    ) {
        MemberApprovalResponseDTO response =
                memberAuthService.updateMemberApproval(
                        principal.getMemberId(),
                        memberId,
                        request
                );

        return ResponseEntity.ok(response);
    }
	
	
	// 회원 가입 일괄 승인/거절 API
	@PatchMapping("/bulk-approval")
	public ResponseEntity<List<MemberApprovalResponseDTO>> updateMembersApproval(
	        @AuthenticationPrincipal CustomUserPrincipal principal,
	        @Valid @RequestBody BulkMemberApprovalRequestDTO request
	) {
	    List<MemberApprovalResponseDTO> response =
	            memberAuthService.updateMembersApproval(
	                    principal.getMemberId(),
	                    request
	            );

	    return ResponseEntity.ok(response);
	}

}
