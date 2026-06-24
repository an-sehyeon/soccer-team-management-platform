// PC에서 지도자와 분석관이 사용하는 관리 홈 화면 파일

import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { getFrontendPermissions } from "../utils/rolePermission";
import { ROUTES } from "../constants/routes";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";

export function DashboardHomePage() {
  const navigate = useNavigate();
  const { member, logout } = useAuth();
  const permissions = getFrontendPermissions(member);

  // 로그아웃 후 로그인 화면으로 이동
  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <AuthenticatedLayout title="대시보드">
      <section>
        <h2>스케줄 관리</h2>
        <p>스케줄 목록 조회</p>

        {permissions.canCreateSchedule && (
          <button type="button">스케줄 등록</button>
        )}

        {permissions.canUpdateSchedule && (
          <button type="button">스케줄 수정</button>
        )}

        {permissions.canDeleteSchedule && (
          <button type="button">스케줄 삭제</button>
        )}
      </section>

      <section>
        <h2>공지사항 관리</h2>
        <p>공지사항 목록 조회</p>

        {permissions.canCreateNotice && (
          <button type="button">공지사항 등록</button>
        )}

        {permissions.canUpdateNotice && (
          <button type="button">공지사항 수정</button>
        )}

        {permissions.canDeleteNotice && (
          <button type="button">공지사항 삭제</button>
        )}
      </section>

      <section>
        <h2>경기 영상 관리</h2>
        <p>경기 영상 목록 조회</p>

        {permissions.canCreateMatchVideo && (
          <button type="button">경기 영상 등록</button>
        )}

        {permissions.canUpdateMatchVideo && (
          <button type="button">경기 영상 수정</button>
        )}

        {permissions.canDeleteMatchVideo && (
          <button type="button">경기 영상 삭제</button>
        )}
      </section>

      <section>
        <h2>팀 분석 클립 관리</h2>
        <p>팀 분석 클립 목록 조회</p>

        {permissions.canCreateTeamAnalysisClip && (
          <button type="button">팀 분석 클립 등록</button>
        )}

        {permissions.canUpdateTeamAnalysisClip && (
          <button type="button">팀 분석 클립 수정</button>
        )}

        {permissions.canDeleteTeamAnalysisClip && (
          <button type="button">팀 분석 클립 삭제</button>
        )}
      </section>

      <section>
        <h2>선수 개인 분석 클립 관리</h2>
        <p>선수 개인 분석 클립 목록 조회</p>

        {permissions.canCreatePlayerAnalysisClip && (
          <button type="button">선수 개인 분석 클립 등록</button>
        )}

        {permissions.canUpdatePlayerAnalysisClip && (
          <button type="button">선수 개인 분석 클립 수정</button>
        )}

        {permissions.canDeletePlayerAnalysisClip && (
          <button type="button">선수 개인 분석 클립 삭제</button>
        )}
      </section>

      <section>
        <h2>드로잉 관리</h2>
        <p>팀/선수 개인 분석 클립 드로잉 조회</p>

        {permissions.canCreateDrawing && (
          <button type="button">드로잉 등록</button>
        )}

        {permissions.canUpdateDrawing && (
          <button type="button">드로잉 수정</button>
        )}

        {permissions.canDeleteDrawing && (
          <button type="button">드로잉 삭제</button>
        )}
      </section>

      <section>
        <h2>선수 기록 관리</h2>
        <p>선수 기록 목록 조회</p>

        {permissions.canCreatePlayerRecord && (
          <button type="button">선수 기록 등록</button>
        )}

        {permissions.canUpdatePlayerRecord && (
          <button type="button">선수 기록 수정</button>
        )}

        {permissions.canDeletePlayerRecord && (
          <button type="button">선수 기록 삭제</button>
        )}
      </section>

      {permissions.canManageMembers && (
        <section>
          <h2>회원 승인 관리</h2>
          <p>승인 대기 회원 조회 및 승인/거절</p>
          <button
            type="button"
            onClick={() => navigate(ROUTES.MEMBER_APPROVAL)}
          >
            회원 승인 관리
          </button>
        </section>
      )}

      <button type="button" onClick={handleLogout}>
        로그아웃
      </button>
    </AuthenticatedLayout>
  );
}
