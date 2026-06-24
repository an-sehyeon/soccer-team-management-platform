// 로그인 후 공통 화면 구조와 로그아웃 처리를 관리하는 레이아웃 파일

import type { ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";
import { getInitialRoute } from "../utils/authRoute";
import { isMobileOrTablet } from "../utils/device";

interface AuthenticatedLayoutProps {
  title: string;
  children: ReactNode;
}

// 인증된 사용자 공통 레이아웃
export function AuthenticatedLayout({
  title,
  children,
}: AuthenticatedLayoutProps) {
  const navigate = useNavigate();
  const { member, logout } = useAuth();

  // 역할과 기기에 맞는 홈으로 이동
  function handleGoHome() {
    if (!member) {
      navigate(ROUTES.LOGIN);
      return;
    }

    const nextRoute = getInitialRoute(member.memberRole, isMobileOrTablet());

    navigate(nextRoute);
  }

  // 로그아웃 후 로그인 화면으로 이동
  function handleLogout() {
    logout();
    navigate(ROUTES.LOGIN);
  }

  return (
    <main>
      <header>
        <h1>{title}</h1>

        <div>
          <p>이름: {member?.name}</p>
          <p>역할: {member?.memberRole}</p>
        </div>

        <nav>
          <button type="button" onClick={handleGoHome}>
            홈
          </button>

          <button type="button" onClick={handleLogout}>
            로그아웃
          </button>
        </nav>
      </header>

      <hr />

      {children}
    </main>
  );
}
