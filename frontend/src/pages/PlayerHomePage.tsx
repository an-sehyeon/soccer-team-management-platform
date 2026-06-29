// PC에서 선수가 사용하는 홈 화면 파일

import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ROUTES } from "../constants/routes";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";

export function PlayerHomePage() {
  const navigate = useNavigate();
  const { logout } = useAuth();

  function handleLogout() {
    logout();
    navigate(ROUTES.LOGIN);
  }

  function handlePreparingFeature(featureName: string) {
    alert(`${featureName} 화면은 아직 준비 중입니다.`);
  }

  return (
    <AuthenticatedLayout title="선수 홈">
      <section>
        <h2>스케줄</h2>
        <p>조회</p>

        <button type="button" onClick={() => navigate(ROUTES.SCHEDULE)}>
          스케줄 조회
        </button>
      </section>

      <section>
        <h2>공지사항</h2>
        <p>조회</p>

        <button type="button" onClick={() => navigate(ROUTES.NOTICE)}>
          공지사항 조회
        </button>
      </section>

      <section>
        <h2>경기 영상</h2>
        <p>조회</p>

        <button type="button" onClick={() => navigate(ROUTES.MATCH_VIDEO)}>
          경기 영상 조회
        </button>
      </section>

      <section>
        <h2>팀 분석 클립</h2>
        <p>조회</p>

        <button
          type="button"
          onClick={() => handlePreparingFeature("팀 분석 클립 조회")}
        >
          팀 분석 클립 조회
        </button>
      </section>

      <section>
        <h2>내 개인 분석 클립</h2>
        <p>조회</p>

        <button
          type="button"
          onClick={() => handlePreparingFeature("내 개인 분석 클립 조회")}
        >
          내 개인 분석 클립 조회
        </button>
      </section>

      <section>
        <h2>내 기록</h2>
        <p>조회</p>

        <button
          type="button"
          onClick={() => handlePreparingFeature("내 기록 조회")}
        >
          내 기록 조회
        </button>
      </section>

      <button type="button" onClick={handleLogout}>
        로그아웃
      </button>
    </AuthenticatedLayout>
  );
}
