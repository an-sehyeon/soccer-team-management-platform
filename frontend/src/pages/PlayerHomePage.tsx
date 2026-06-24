// PC에서 선수가 사용하는 선수 홈 화면 파일

import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";
import { ROUTES } from "../constants/routes";
import { useNavigate } from "react-router-dom";

export function PlayerHomePage() {
  const navigate = useNavigate();
  return (
    <AuthenticatedLayout title="선수 홈">
      <section>
        <h2>선수 메뉴</h2>

        <ul>
          <li>
            <button type="button" onClick={() => navigate(ROUTES.SCHEDULE)}>
              스케줄
            </button>
          </li>
          <li>공지사항</li>
          <li>경기 원본 영상</li>
          <li>팀 분석 영상</li>
          <li>내 개인 분석 영상</li>
          <li>내 기록</li>
        </ul>
      </section>
    </AuthenticatedLayout>
  );
}
