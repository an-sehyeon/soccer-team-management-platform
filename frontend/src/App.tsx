// 앱 전체 라우팅 구조를 정의하는 파일

import { Navigate, Route, Routes } from "react-router-dom";
import { LoginPage } from "./pages/LoginPage";
import { SignUpPage } from "./pages/SignUpPage";
import { DashboardHomePage } from "./pages/DashboardHomePage";
import { PlayerHomePage } from "./pages/PlayerHomePage";
import { MobileHomePage } from "./pages/MobileHomePage";
import { ProtectedRoute } from "./routes/ProtectedRoute";
import { PublicOnlyRoute } from "./routes/PublicOnlyRoute";
import { RoleRoute } from "./routes/RoleRoute";
import { ROUTES } from "./constants/routes";
import { AdminMemberApprovalPage } from "./pages/AdminMemberApprovalPage";
import { AdminOnlyRoute } from "./routes/AdminOnlyRoute";
import SchedulePage from "./pages/SchedulePage";
import NoticePage from "./pages/NoticePage";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to={ROUTES.LOGIN} replace />} />

      <Route
        path={ROUTES.LOGIN}
        element={
          <PublicOnlyRoute>
            <LoginPage />
          </PublicOnlyRoute>
        }
      />

      <Route
        path={ROUTES.SIGN_UP}
        element={
          <PublicOnlyRoute>
            <SignUpPage />
          </PublicOnlyRoute>
        }
      />

      <Route
        path={ROUTES.DASHBOARD}
        element={
          <ProtectedRoute>
            <RoleRoute allowedRoles={["COACH", "ANALYST"]}>
              <DashboardHomePage />
            </RoleRoute>
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.MEMBER_APPROVAL}
        element={
          <ProtectedRoute>
            <AdminOnlyRoute>
              <AdminMemberApprovalPage />
            </AdminOnlyRoute>
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.PLAYER}
        element={
          <ProtectedRoute>
            <RoleRoute allowedRoles={["PLAYER"]}>
              <PlayerHomePage />
            </RoleRoute>
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.MOBILE}
        element={
          <ProtectedRoute>
            <MobileHomePage />
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.SCHEDULE}
        element={
          <ProtectedRoute>
            <SchedulePage />
          </ProtectedRoute>
        }
      />

      <Route
        path={ROUTES.NOTICE}
        element={
          <ProtectedRoute>
            <NoticePage />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to={ROUTES.LOGIN} replace />} />
    </Routes>
  );
}

export default App;
