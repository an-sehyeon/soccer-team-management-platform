// 관리자 회원 승인 관련 API 요청 함수를 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  BulkMemberApprovalRequest,
  BulkMemberApprovalResponse,
  MemberApprovalRequest,
  MemberApprovalResponse,
  PendingMember,
} from "../types/adminMember";

// 승인 대기 회원 목록 조회 API 요청
export async function getPendingMembers() {
  const response = await axiosInstance.get<PendingMember[]>(
    "/api/admin/members/pending",
  );

  return response.data;
}

// 단일 회원 승인/거절 API 요청
export async function updateMemberApproval(
  memberId: number,
  request: MemberApprovalRequest,
) {
  const response = await axiosInstance.patch<MemberApprovalResponse>(
    `/api/admin/members/${memberId}/approval`,
    request,
  );

  return response.data;
}

// 선택 회원 일괄 승인/거절 API 요청
export async function updateBulkMemberApproval(
  request: BulkMemberApprovalRequest,
) {
  const response = await axiosInstance.patch<BulkMemberApprovalResponse>(
    "/api/admin/members/bulk-approval",
    request,
  );

  return response.data;
}
