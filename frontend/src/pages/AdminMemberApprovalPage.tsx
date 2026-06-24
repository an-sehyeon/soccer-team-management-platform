// 관리자 회원 승인 목록 조회와 승인/거절 처리를 담당하는 페이지 파일

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getPendingMembers,
  updateBulkMemberApproval,
  updateMemberApproval,
} from "../api/adminMemberApi";
import { ROUTES } from "../constants/routes";
import type { ApprovalStatus, MemberRole } from "../types/auth";
import type { PendingMember } from "../types/adminMember";
import { getApiErrorMessage } from "../utils/apiError";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";

type ApprovalActionStatus = Extract<ApprovalStatus, "APPROVED" | "REJECTED">;

export function AdminMemberApprovalPage() {
  const navigate = useNavigate();

  const [pendingMembers, setPendingMembers] = useState<PendingMember[]>([]);
  const [selectedMemberIds, setSelectedMemberIds] = useState<number[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // 최초 화면 진입 시 승인 대기 회원 목록 조회
  useEffect(() => {
    void loadPendingMembers();
  }, []);

  // 승인 대기 회원 목록 조회
  async function loadPendingMembers() {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const members = await getPendingMembers();

      setPendingMembers(members);
      setSelectedMemberIds([]);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }

  // 역할 한글 표시
  function getRoleLabel(memberRole: MemberRole) {
    if (memberRole === "COACH") {
      return "지도자";
    }

    if (memberRole === "ANALYST") {
      return "분석관";
    }

    return "선수";
  }

  // 선택 여부 확인
  function isSelected(memberId: number) {
    return selectedMemberIds.includes(memberId);
  }

  // 단일 회원 선택 토글
  function handleToggleMember(memberId: number) {
    setSelectedMemberIds((prevSelectedIds) => {
      if (prevSelectedIds.includes(memberId)) {
        return prevSelectedIds.filter((selectedId) => selectedId !== memberId);
      }

      return [...prevSelectedIds, memberId];
    });
  }

  // 전체 선택 토글
  function handleToggleAll() {
    if (selectedMemberIds.length === pendingMembers.length) {
      setSelectedMemberIds([]);
      return;
    }

    setSelectedMemberIds(pendingMembers.map((member) => member.memberId));
  }

  // 단일 회원 승인/거절 처리
  async function handleSingleApproval(
    memberId: number,
    approvalStatus: ApprovalActionStatus,
  ) {
    const confirmMessage =
      approvalStatus === "APPROVED"
        ? "해당 회원을 승인하시겠습니까?"
        : "해당 회원을 거절하시겠습니까?";

    if (!window.confirm(confirmMessage)) {
      return;
    }

    try {
      setIsProcessing(true);
      setErrorMessage("");

      await updateMemberApproval(memberId, { approvalStatus });

      alert(
        approvalStatus === "APPROVED"
          ? "회원 승인이 완료되었습니다."
          : "회원 거절이 완료되었습니다.",
      );

      await loadPendingMembers();
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsProcessing(false);
    }
  }

  // 선택 회원 일괄 승인/거절 처리
  async function handleBulkApproval(approvalStatus: ApprovalActionStatus) {
    if (selectedMemberIds.length === 0) {
      alert("처리할 회원을 선택해주세요.");
      return;
    }

    const confirmMessage =
      approvalStatus === "APPROVED"
        ? "선택한 회원을 일괄 승인하시겠습니까?"
        : "선택한 회원을 일괄 거절하시겠습니까?";

    if (!window.confirm(confirmMessage)) {
      return;
    }

    try {
      setIsProcessing(true);
      setErrorMessage("");

      await updateBulkMemberApproval({
        memberIds: selectedMemberIds,
        approvalStatus,
      });

      alert(
        approvalStatus === "APPROVED"
          ? "선택 회원 승인이 완료되었습니다."
          : "선택 회원 거절이 완료되었습니다.",
      );

      await loadPendingMembers();
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsProcessing(false);
    }
  }

  // 대시보드로 이동
  function handleBackToDashboard() {
    navigate(ROUTES.DASHBOARD);
  }

  if (isLoading) {
    return <div>승인 대기 회원을 불러오는 중입니다.</div>;
  }

  return (
    <AuthenticatedLayout title="회원 승인 관리">
      <button type="button" onClick={handleBackToDashboard}>
        대시보드로 돌아가기
      </button>

      {errorMessage && <p>{errorMessage}</p>}

      <section>
        <h2>승인 대기 회원 목록</h2>

        <div>
          <button
            type="button"
            onClick={() => void loadPendingMembers()}
            disabled={isProcessing}
          >
            새로고침
          </button>

          <button
            type="button"
            onClick={() => void handleBulkApproval("APPROVED")}
            disabled={isProcessing}
          >
            선택 승인
          </button>

          <button
            type="button"
            onClick={() => void handleBulkApproval("REJECTED")}
            disabled={isProcessing}
          >
            선택 거절
          </button>
        </div>

        {pendingMembers.length === 0 ? (
          <p>승인 대기 중인 회원이 없습니다.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>
                  <input
                    type="checkbox"
                    checked={selectedMemberIds.length === pendingMembers.length}
                    onChange={handleToggleAll}
                    disabled={isProcessing}
                  />
                </th>
                <th>아이디</th>
                <th>이름</th>
                <th>연락처</th>
                <th>역할</th>
                <th>선수 정보</th>
                <th>가입일</th>
                <th>처리</th>
              </tr>
            </thead>

            <tbody>
              {pendingMembers.map((member) => (
                <tr key={member.memberId}>
                  <td>
                    <input
                      type="checkbox"
                      checked={isSelected(member.memberId)}
                      onChange={() => handleToggleMember(member.memberId)}
                      disabled={isProcessing}
                    />
                  </td>

                  <td>{member.loginId}</td>
                  <td>{member.name}</td>
                  <td>{member.phone}</td>
                  <td>{getRoleLabel(member.memberRole)}</td>

                  <td>
                    {member.memberRole === "PLAYER" ? (
                      <>
                        <div>학년: {member.grade ?? "-"}</div>
                        <div>등번호: {member.uniformNumber ?? "-"}</div>
                        <div>주장: {member.isCaptain ? "예" : "아니오"}</div>
                        <div>출신학교: {member.almaMater ?? "-"}</div>
                      </>
                    ) : (
                      "-"
                    )}
                  </td>

                  <td>{member.createdAt ?? "-"}</td>

                  <td>
                    <button
                      type="button"
                      onClick={() =>
                        void handleSingleApproval(member.memberId, "APPROVED")
                      }
                      disabled={isProcessing}
                    >
                      승인
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        void handleSingleApproval(member.memberId, "REJECTED")
                      }
                      disabled={isProcessing}
                    >
                      거절
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </AuthenticatedLayout>
  );
}
