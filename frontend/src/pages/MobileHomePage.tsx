// 모바일과 태블릿에서 모든 역할이 사용하는 홈 화면 파일

import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { getFrontendPermissions } from "../utils/rolePermission";
import { ROUTES } from "../constants/routes";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";

export function MobileHomePage() {
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
    <AuthenticatedLayout title="모바일 홈">
      <section>
        <h2>스케줄</h2>
        <p>{createDescription(scheduleActions)}</p>

        <button type="button" onClick={() => navigate(ROUTES.SCHEDULE)}>
          스케줄 조회
        </button>

        {permissions.canCreateSchedule && (
          <button type="button" onClick={() => navigate(ROUTES.SCHEDULE)}>
            스케줄 등록
          </button>
        )}

        {permissions.canUpdateSchedule && (
          <button type="button" onClick={() => navigate(ROUTES.SCHEDULE)}>
            스케줄 수정
          </button>
        )}

        {permissions.canDeleteSchedule && (
          <button type="button" onClick={() => navigate(ROUTES.SCHEDULE)}>
            스케줄 삭제
          </button>
        )}
      </section>

      <section>
        <h2>공지사항</h2>
        <p>{createDescription(noticeActions)}</p>

        <button type="button" onClick={() => navigate(ROUTES.NOTICE)}>
          공지사항 조회
        </button>

        {permissions.canCreateNotice && (
          <button type="button" onClick={() => navigate(ROUTES.NOTICE)}>
            공지사항 등록
          </button>
        )}

        {permissions.canUpdateNotice && (
          <button type="button" onClick={() => navigate(ROUTES.NOTICE)}>
            공지사항 수정
          </button>
        )}

        {permissions.canDeleteNotice && (
          <button type="button" onClick={() => navigate(ROUTES.NOTICE)}>
            공지사항 삭제
          </button>
        )}
      </section>

      <section>
        <h2>경기 영상</h2>
        <p>{createDescription(matchVideoActions)}</p>

        <button type="button" onClick={() => navigate(ROUTES.MATCH_VIDEO)}>
          경기 영상 조회
        </button>

        {permissions.canCreateMatchVideo && (
          <button
            type="button"
            onClick={() => navigate(ROUTES.MATCH_VIDEO_CREATE)}
          >
            경기 영상 등록
          </button>
        )}

        {permissions.canUpdateMatchVideo && (
          <button type="button" onClick={() => navigate(ROUTES.MATCH_VIDEO)}>
            경기 영상 수정
          </button>
        )}

        {permissions.canDeleteMatchVideo && (
          <button type="button" onClick={() => navigate(ROUTES.MATCH_VIDEO)}>
            경기 영상 삭제
          </button>
        )}
      </section>

      <section>
        <h2>팀 분석 클립</h2>
        <p>{createDescription(teamAnalysisClipActions)}</p>

        <button
          type="button"
          onClick={() => handlePreparingFeature("팀 분석 클립 조회")}
        >
          팀 분석 클립 조회
        </button>

        {permissions.canCreateTeamAnalysisClip && (
          <button
            type="button"
            onClick={() => handlePreparingFeature("팀 분석 클립 등록")}
          >
            팀 분석 클립 등록
          </button>
        )}

        {permissions.canUpdateTeamAnalysisClip && (
          <button
            type="button"
            onClick={() => handlePreparingFeature("팀 분석 클립 수정")}
          >
            팀 분석 클립 수정
          </button>
        )}

        {permissions.canDeleteTeamAnalysisClip && (
          <button
            type="button"
            onClick={() => handlePreparingFeature("팀 분석 클립 삭제")}
          >
            팀 분석 클립 삭제
          </button>
        )}
      </section>

      <section>
        <h2>선수 개인 분석 클립</h2>
        <p>{createDescription(playerAnalysisClipActions)}</p>

        <button
          type="button"
          onClick={() => handlePreparingFeature("선수 개인 분석 클립 조회")}
        >
          선수 개인 분석 클립 조회
        </button>

        {permissions.canCreatePlayerAnalysisClip && (
          <button
            type="button"
            onClick={() => handlePreparingFeature("선수 개인 분석 클립 등록")}
          >
            선수 개인 분석 클립 등록
          </button>
        )}

        {permissions.canUpdatePlayerAnalysisClip && (
          <button
            type="button"
            onClick={() => handlePreparingFeature("선수 개인 분석 클립 수정")}
          >
            선수 개인 분석 클립 수정
          </button>
        )}

        {permissions.canDeletePlayerAnalysisClip && (
          <button
            type="button"
            onClick={() => handlePreparingFeature("선수 개인 분석 클립 삭제")}
          >
            선수 개인 분석 클립 삭제
          </button>
        )}
      </section>

      <section>
        <h2>선수 기록</h2>
        <p>{createDescription(playerRecordActions)}</p>

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
      </section>

      {permissions.canManageMembers && (
        <section>
          <h2>회원 승인</h2>
          <p>승인 대기 회원 조회, 승인, 거절</p>

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
