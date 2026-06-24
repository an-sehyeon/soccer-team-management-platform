// 관리자 회원 승인 화면에서 사용하는 요청, 응답 타입을 정의하는 파일

import type { ApprovalStatus, MemberRole } from "./auth";

export interface PendingMember {
  memberId: number;
  loginId: string;
  name: string;
  phone: string;
  memberRole: MemberRole;
  approvalStatus: ApprovalStatus;
  grade?: number | null;
  uniformNumber?: number | null;
  isCaptain?: boolean;
  almaMater?: string | null;
  createdAt?: string;
}

export interface MemberApprovalRequest {
  approvalStatus: "APPROVED" | "REJECTED";
}

export interface MemberApprovalResponse {
  memberId: number;
  loginId: string;
  name: string;
  memberRole: MemberRole;
  approvalStatus: ApprovalStatus;
  message?: string;
}

export interface BulkMemberApprovalRequest {
  memberIds: number[];
  approvalStatus: "APPROVED" | "REJECTED";
}

export interface BulkMemberApprovalResponse {
  approvedMembers?: MemberApprovalResponse[];
  rejectedMembers?: MemberApprovalResponse[];
  members?: MemberApprovalResponse[];
  message?: string;
}
