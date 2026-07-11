// 모바일과 태블릿에서 모든 역할이 사용하는 홈 화면 파일

import { useNavigate } from "react-router-dom";

import { ROUTES, createMatchVideoAnalysisRoute } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";
import { getFrontendPermissions } from "../utils/rolePermission";

export function MobileHomePage() {
  const navigate = useNavigate();
  const { member, logout } = useAuth();
  const permissions = getFrontendPermissions(member);

  const isManager =
    member?.memberRole === "COACH" || member?.memberRole === "ANALYST";

  function handleLogout() {
    logout();
    navigate(ROUTES.LOGIN);
  }

  function createDescription(actions: string[]) {
    return actions.join(", ");
  }

  const scheduleActions = [
    "조회",
    ...(permissions.canCreateSchedule ? ["등록"] : []),
    ...(permissions.canUpdateSchedule ? ["수정"] : []),
    ...(permissions.canDeleteSchedule ? ["삭제"] : []),
  ];

  const noticeActions = [
    "조회",
    ...(permissions.canCreateNotice ? ["등록"] : []),
    ...(permissions.canUpdateNotice ? ["수정"] : []),
    ...(permissions.canDeleteNotice ? ["삭제"] : []),
  ];

  const matchVideoActions = [
    "조회",
    ...(permissions.canCreateMatchVideo ? ["등록"] : []),
    ...(permissions.canUpdateMatchVideo ? ["수정"] : []),
    ...(permissions.canDeleteMatchVideo ? ["삭제"] : []),
  ];

  const teamAnalysisClipActions = [
    "목록 조회",
    "상세 조회",
    ...(permissions.canDeleteTeamAnalysisClip ? ["삭제"] : []),
  ];

  const playerAnalysisClipActions = [
    "목록 조회",
    "상세 조회",
    ...(permissions.canDeletePlayerAnalysisClip ? ["삭제"] : []),
  ];

  const playerRecordActions = [
    "조회",
    ...(permissions.canCreatePlayerRecord ? ["등록"] : []),
    ...(permissions.canUpdatePlayerRecord ? ["수정"] : []),
    ...(permissions.canDeletePlayerRecord ? ["삭제"] : []),
  ];

  return (
    <AuthenticatedLayout title="모바일 홈">
      <main className="page">
        <section className="page-header">
          <div>
            <h1>모바일 홈</h1>
            <p>모바일에서 주요 팀 운영 기능과 분석 영상을 확인합니다.</p>
          </div>

          <div className="button-row">
            <button type="button" onClick={handleLogout}>
              로그아웃
            </button>
          </div>
        </section>

        <section className="dashboard-grid">
          <article className="card">
            <h2>스케줄</h2>
            <p>{createDescription(scheduleActions)}</p>

            <div className="button-row">
              <button type="button" onClick={() => navigate(ROUTES.SCHEDULE)}>
                스케줄
              </button>
            </div>
          </article>

          <article className="card">
            <h2>공지사항</h2>
            <p>{createDescription(noticeActions)}</p>

            <div className="button-row">
              <button type="button" onClick={() => navigate(ROUTES.NOTICE)}>
                공지사항
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
                경기 영상
              </button>
            </div>

            {isManager && (
              <div className="button-row">
                {permissions.canCreateTeamAnalysisClip && (
                  <button
                    type="button"
                    onClick={() =>
                      navigate(
                        createMatchVideoAnalysisRoute({
                          analysisMode: "team-clip-create",
                        }),
                      )
                    }
                  >
                    팀 클립 등록
                  </button>
                )}

                {permissions.canCreatePlayerAnalysisClip && (
                  <button
                    type="button"
                    onClick={() =>
                      navigate(
                        createMatchVideoAnalysisRoute({
                          analysisMode: "player-clip-create",
                        }),
                      )
                    }
                  >
                    선수 클립 등록
                  </button>
                )}

                {permissions.canCreatePlayerRecord && (
                  <button
                    type="button"
                    onClick={() =>
                      navigate(
                        createMatchVideoAnalysisRoute({
                          analysisMode: "player-record-event",
                        }),
                      )
                    }
                  >
                    기록 이벤트 등록
                  </button>
                )}
              </div>
            )}
          </article>

          <article className="card">
            <h2>팀 분석 클립</h2>
            <p>{createDescription(teamAnalysisClipActions)}</p>

            <div className="button-row">
              <button
                type="button"
                onClick={() => navigate(ROUTES.TEAM_ANALYSIS_CLIP)}
              >
                팀 분석 영상
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
                개인 분석 영상
              </button>
            </div>
          </article>

          <article className="card">
            <h2>선수 기록</h2>
            <p>{createDescription(playerRecordActions)}</p>

            <div className="button-row">
              <button
                type="button"
                onClick={() => navigate(ROUTES.PLAYER_RECORD)}
              >
                선수 기록
              </button>
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
                  회원 승인
                </button>
              </div>
            </article>
          )}
        </section>
      </main>
    </AuthenticatedLayout>
  );
}
