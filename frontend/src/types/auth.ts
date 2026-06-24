// 인증 관련 요청, 응답, 회원 역할 타입을 정의하는 파일

export type MemberRole = "COACH" | "ANALYST" | "PLAYER";

export type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface LoginRequest {
  loginId: string;
  password: string;
}

export interface AuthMember {
  memberId: number;
  loginId: string;
  name: string;
  memberRole: MemberRole;
  isAdmin: boolean;
  isCaptain: boolean;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  member: AuthMember;
}

export interface SignUpRequest {
  loginId: string;
  password: string;
  name: string;
  phone: string;
  memberRole: MemberRole;
  grade?: number;
  uniformNumber?: number;
  isCaptain?: boolean;
  almaMater?: string;
}

export interface SignUpResponse {
  memberId: number;
  approvalStatus: ApprovalStatus;
  message: string;
}

export interface MeResponse {
  memberId: number;
  loginId: string;
  name: string;
  phone: string;
  memberRole: MemberRole;
  approvalStatus: ApprovalStatus;
  isAdmin: boolean;
  isCaptain: boolean;
  grade?: number | null;
  uniformNumber?: number | null;
  almaMater?: string | null;
}
