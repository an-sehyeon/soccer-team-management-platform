// PC에서 지도자와 분석관이 사용하는 관리 홈 화면 파일

import { useNavigate } from "react-router-dom";

import { ROUTES, createMatchVideoAnalysisRoute } from "../constants/routes";
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

  function createDescription(actions: string[]) {
    return actions.join(", ");
  }

  const matchVideoActions = [
    "조회",
    ...(permissions.canCreateMatchVideo ? ["등록"] : []),
    ...(permissions.canUpdateMatchVideo ? ["수정"] : []),
    ...(permissions.canDeleteMatchVideo ? ["삭제"] : []),
    ...(permissions.canCreateTeamAnalysisClip ? ["팀 클립 등록"] : []),
    ...(permissions.canCreatePlayerAnalysisClip ? ["선수 클립 등록"] : []),
    ...(permissions.canCreatePlayerRecord ? ["선수 기록 이벤트 등록"] : []),
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
    ...(permissions.canCreatePlayerRecord ? ["이벤트 등록"] : []),
    ...(permissions.canUpdatePlayerRecord ? ["이벤트 수정"] : []),
  ];

  return (
    <AuthenticatedLayout title="관리 대시보드">
      <main className="page">
        <section className="page-header">
          <div>
            <h1>관리 대시보드</h1>
            <p>
              지도자와 분석관이 팀 운영, 경기 영상, 분석 클립, 선수 기록을
              관리하는 화면입니다.
            </p>
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

            {(permissions.canCreateTeamAnalysisClip ||
              permissions.canCreatePlayerAnalysisClip ||
              permissions.canCreatePlayerRecord) && (
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
                          analysisMode: "player-record-create",
                        }),
                      )
                    }
                  >
                    선수 기록 이벤트 등록
                  </button>
                )}
              </div>
            )}
          </article>

          <article className="card">
            <h2>팀 분석 클립</h2>
            <p>{createDescription(teamAnalysisClipActions)}</p>
            <p className="helper-text">
              등록, 수정, 드로잉 작업은 경기 영상 관리 화면에서 진행합니다.
            </p>

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
            <p className="helper-text">
              등록, 수정, 드로잉 작업은 경기 영상 관리 화면에서 진행합니다.
            </p>

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
                onClick={() => navigate(ROUTES.PLAYER_RECORD)}
              >
                선수 기록 관리
              </button>

              {permissions.canCreatePlayerRecord && (
                <button
                  type="button"
                  onClick={() =>
                    navigate(
                      createMatchVideoAnalysisRoute({
                        analysisMode: "player-record-create",
                      }),
                    )
                  }
                >
                  경기 영상에서 이벤트 등록
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
