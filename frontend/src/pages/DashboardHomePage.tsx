// PC에서 지도자와 분석관이 사용하는 관리 홈 화면 파일
import { useNavigate } from "react-router-dom";

import { ROUTES } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";
import { getFrontendPermissions } from "../utils/rolePermission";

export function DashboardHomePage() {
  const navigate = useNavigate();
  const { member, logout } = useAuth();

  const permissions = getFrontendPermissions(member);

  function handleLogout() {
    logout();
    navigate(ROUTES.LOGIN);
  }

  function handlePreparingFeature(featureName: string) {
    alert(`${featureName} 화면은 아직 준비 중입니다.`);
  }

  function createDescription(actions: string[]) {
    return actions.join(", ");
  }

  const matchVideoActions = [
    "조회",
    ...(permissions.canCreateMatchVideo ? ["등록"] : []),
    ...(permissions.canUpdateMatchVideo ? ["수정"] : []),
    ...(permissions.canDeleteMatchVideo ? ["삭제"] : []),
  ];

  const teamAnalysisClipActions = [
    "조회",
    ...(permissions.canCreateTeamAnalysisClip ? ["등록"] : []),
    ...(permissions.canUpdateTeamAnalysisClip ? ["수정"] : []),
    ...(permissions.canDeleteTeamAnalysisClip ? ["삭제"] : []),
  ];

  const playerAnalysisClipActions = [
    "조회",
    ...(permissions.canCreatePlayerAnalysisClip ? ["등록"] : []),
    ...(permissions.canUpdatePlayerAnalysisClip ? ["수정"] : []),
    ...(permissions.canDeletePlayerAnalysisClip ? ["삭제"] : []),
  ];

  const playerRecordActions = [
    "조회",
    ...(permissions.canCreatePlayerRecord ? ["등록"] : []),
    ...(permissions.canUpdatePlayerRecord ? ["수정"] : []),
    ...(permissions.canDeletePlayerRecord ? ["삭제"] : []),
  ];

  return (
    <AuthenticatedLayout title="관리 대시보드">
      <main className="page">
        <section className="page-header">
          <div>
            <h1>관리 대시보드</h1>
            <p>
              지도자와 분석관이 팀 운영, 경기 영상, 분석 클립을 관리하는
              화면입니다.
            </p>
          </div>

          <div className="button-row">
            <button type="button" onClick={handleLogout}>
              로그아웃
            </button>
          </div>
        </section>

        <section className="content-grid">
          <article className="card">
            <h2>스케줄</h2>
            <p>
              {permissions.canCreateSchedule
                ? "스케줄 조회, 등록, 수정, 삭제"
                : "스케줄 조회"}
            </p>
            <div className="button-row">
              <button type="button" onClick={() => navigate(ROUTES.SCHEDULE)}>
                스케줄 관리
              </button>
            </div>
          </article>

          <article className="card">
            <h2>공지사항</h2>
            <p>
              {permissions.canCreateNotice
                ? "공지사항 조회, 등록, 수정, 삭제"
                : "공지사항 조회"}
            </p>
            <div className="button-row">
              <button type="button" onClick={() => navigate(ROUTES.NOTICE)}>
                공지사항 관리
              </button>
            </div>
          </article>

          <article className="card">
            <h2>경기 영상</h2>
            <p>{createDescription(matchVideoActions)}</p>
            <div className="button-row">
              <button
                type="button"
                onClick={() => navigate(ROUTES.MATCH_VIDEO)}
              >
                경기 영상 관리
              </button>

              {permissions.canCreateMatchVideo && (
                <button
                  type="button"
                  onClick={() => navigate(ROUTES.MATCH_VIDEO_CREATE)}
                >
                  경기 영상 등록
                </button>
              )}
            </div>
          </article>

          <article className="card">
            <h2>팀 분석 클립</h2>
            <p>{createDescription(teamAnalysisClipActions)}</p>
            <div className="button-row">
              <button
                type="button"
                onClick={() => navigate(ROUTES.TEAM_ANALYSIS_CLIP)}
              >
                팀 분석 클립 관리
              </button>
            </div>
          </article>

          <article className="card">
            <h2>선수 개인 분석 클립</h2>
            <p>{createDescription(playerAnalysisClipActions)}</p>
            <div className="button-row">
              <button
                type="button"
                onClick={() => navigate(ROUTES.PLAYER_ANALYSIS_CLIP)}
              >
                선수 개인 분석 클립 관리
              </button>
            </div>
          </article>

          <article className="card">
            <h2>선수 기록</h2>
            <p>{createDescription(playerRecordActions)}</p>
            <div className="button-row">
              <button
                type="button"
                onClick={() => handlePreparingFeature("선수 기록 조회")}
              >
                선수 기록 조회
              </button>

              {permissions.canCreatePlayerRecord && (
                <button
                  type="button"
                  onClick={() => handlePreparingFeature("선수 기록 등록")}
                >
                  선수 기록 등록
                </button>
              )}

              {permissions.canUpdatePlayerRecord && (
                <button
                  type="button"
                  onClick={() => handlePreparingFeature("선수 기록 수정")}
                >
                  선수 기록 수정
                </button>
              )}

              {permissions.canDeletePlayerRecord && (
                <button
                  type="button"
                  onClick={() => handlePreparingFeature("선수 기록 삭제")}
                >
                  선수 기록 삭제
                </button>
              )}
            </div>
          </article>

          {permissions.canManageMembers && (
            <article className="card">
              <h2>회원 승인</h2>
              <p>승인 대기 회원 조회, 승인, 거절</p>
              <div className="button-row">
                <button
                  type="button"
                  onClick={() => navigate(ROUTES.MEMBER_APPROVAL)}
                >
                  회원 승인 관리
                </button>
              </div>
            </article>
          )}
        </section>
      </main>
    </AuthenticatedLayout>
  );
}
