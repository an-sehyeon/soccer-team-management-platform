// PC에서 선수가 사용하는 선수 홈 화면 파일

import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";

export function PlayerHomePage() {
  return (
    <AuthenticatedLayout title="선수 홈">
      <section>
        <h2>선수 메뉴</h2>

        <ul>
          <li>오늘 일정</li>
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
