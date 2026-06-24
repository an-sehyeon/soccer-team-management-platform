// 로그인 입력 화면과 로그인 API 연결을 처리하는 페이지 파일

import { useState, type SyntheticEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { getInitialRoute } from "../utils/authRoute";
import { isMobileOrTablet } from "../utils/device";
import { getApiErrorMessage } from "../utils/apiError";
import { ROUTES } from "../constants/routes";

export function LoginPage() {
  const navigate = useNavigate();
  const { loginWithCredentials } = useAuth();

  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 로그인 폼 제출 처리
  async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!loginId.trim() || !password.trim()) {
      setErrorMessage("아이디와 비밀번호를 입력해주세요.");
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");

      const member = await loginWithCredentials({
        loginId,
        password,
      });

      const nextRoute = getInitialRoute(member.memberRole, isMobileOrTablet());

      navigate(nextRoute, { replace: true });
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main>
      <h1>로그인</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="loginId">아이디</label>
          <input
            id="loginId"
            type="text"
            value={loginId}
            onChange={(event) => setLoginId(event.target.value)}
            autoComplete="username"
          />
        </div>

        <div>
          <label htmlFor="password">비밀번호</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
          />
        </div>

        {errorMessage && <p>{errorMessage}</p>}

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "로그인 중..." : "로그인"}
        </button>
      </form>

      <p>
        계정이 없나요? <Link to={ROUTES.SIGN_UP}>회원가입</Link>
      </p>
    </main>
  );
}
