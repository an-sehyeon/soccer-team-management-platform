// 모바일과 태블릿에서 모든 역할이 공통으로 사용하는 홈 화면 파일

import { useNavigate } from "react-router-dom";
import { ROUTES } from "../constants/routes";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";

export function MobileHomePage() {
  const navigate = useNavigate();
  return (
    <AuthenticatedLayout title="모바일 홈">
      <section>
        <h2>공통 모바일 메뉴</h2>

        <ul>
          <li>스케줄</li>
          <li>
            <button type="button" onClick={() => navigate(ROUTES.SCHEDULE)}>
              {" "}
              공지사항
            </button>
          </li>
          <li>경기 원본 영상</li>
          <li>팀 분석 영상</li>
          <li>내 개인 분석 영상</li>
          <li>내 기록</li>
        </ul>
      </section>
    </AuthenticatedLayout>
  );
}
