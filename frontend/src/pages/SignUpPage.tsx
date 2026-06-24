// 회원가입 화면을 표시하는 페이지 파일

import { useState, type SyntheticEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { signUp } from "../api/authApi";
import type { MemberRole, SignUpRequest } from "../types/auth";
import { getApiErrorMessage } from "../utils/apiError";
import { ROUTES } from "../constants/routes";

export function SignUpPage() {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [memberRole, setMemberRole] = useState<MemberRole>("PLAYER");

  const [grade, setGrade] = useState("");
  const [uniformNumber, setUniformNumber] = useState("");
  const [isCaptain, setIsCaptain] = useState(false);
  const [almaMater, setAlmaMater] = useState("");

  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  // 회원가입 폼 제출 처리
  async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();

    setErrorMessage("");

    if (!loginId.trim() || !password.trim() || !name.trim() || !phone.trim()) {
      setErrorMessage("필수 입력값을 모두 입력해주세요.");
      return;
    }

    if (memberRole === "PLAYER") {
      if (!grade.trim() || !uniformNumber.trim()) {
        setErrorMessage("선수는 학년과 등번호를 반드시 입력해야 합니다.");
        return;
      }

      const gradeNumber = Number(grade);
      const uniformNumberValue = Number(uniformNumber);

      if (Number.isNaN(gradeNumber) || gradeNumber < 1 || gradeNumber > 4) {
        setErrorMessage("선수 학년은 1~4 사이로 입력해주세요.");
        return;
      }

      if (
        Number.isNaN(uniformNumberValue) ||
        uniformNumberValue < 1 ||
        uniformNumberValue > 255
      ) {
        setErrorMessage("등번호는 1~99 사이로 입력해주세요.");
        return;
      }
    }

    const request: SignUpRequest = {
      loginId,
      password,
      name,
      phone,
      memberRole,
    };

    if (memberRole === "PLAYER") {
      request.grade = Number(grade);
      request.uniformNumber = Number(uniformNumber);
      request.isCaptain = isCaptain;

      if (almaMater.trim()) {
        request.almaMater = almaMater;
      }
    }

    try {
      setIsSubmitting(true);

      const response = await signUp(request);

      alert(response.message);
      navigate("/login", { replace: true });
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main>
      <h1>회원가입</h1>

      <form onSubmit={handleSubmit}>
        <fieldset>
          <legend>역할 선택</legend>

          <label>
            <input
              type="radio"
              name="memberRole"
              value="PLAYER"
              checked={memberRole === "PLAYER"}
              onChange={() => setMemberRole("PLAYER")}
            />
            선수
          </label>

          <label>
            <input
              type="radio"
              name="memberRole"
              value="COACH"
              checked={memberRole === "COACH"}
              onChange={() => setMemberRole("COACH")}
            />
            지도자
          </label>

          <label>
            <input
              type="radio"
              name="memberRole"
              value="ANALYST"
              checked={memberRole === "ANALYST"}
              onChange={() => setMemberRole("ANALYST")}
            />
            분석관
          </label>
        </fieldset>

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
            autoComplete="new-password"
          />
        </div>

        <div>
          <label htmlFor="name">이름</label>
          <input
            id="name"
            type="text"
            value={name}
            onChange={(event) => setName(event.target.value)}
            autoComplete="name"
          />
        </div>

        <div>
          <label htmlFor="phone">휴대폰 번호</label>
          <input
            id="phone"
            type="text"
            value={phone}
            onChange={(event) => setPhone(event.target.value)}
            placeholder="01012345678"
            autoComplete="tel"
          />
        </div>

        {memberRole === "PLAYER" && (
          <section>
            <h2>선수 추가 정보</h2>

            <div>
              <label htmlFor="grade">학년</label>
              <input
                id="grade"
                type="number"
                min="1"
                max="4"
                value={grade}
                onChange={(event) => setGrade(event.target.value)}
              />
            </div>

            <div>
              <label htmlFor="uniformNumber">등번호</label>
              <input
                id="uniformNumber"
                type="number"
                min="1"
                max="255"
                value={uniformNumber}
                onChange={(event) => setUniformNumber(event.target.value)}
              />
            </div>

            <div>
              <label>
                <input
                  type="checkbox"
                  checked={isCaptain}
                  onChange={(event) => setIsCaptain(event.target.checked)}
                />
                주장 여부
              </label>
            </div>

            <div>
              <label htmlFor="almaMater">출신학교</label>
              <input
                id="almaMater"
                type="text"
                value={almaMater}
                onChange={(event) => setAlmaMater(event.target.value)}
              />
            </div>
          </section>
        )}

        {errorMessage && <p>{errorMessage}</p>}

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "가입 신청 중..." : "회원가입"}
        </button>
      </form>

      <p>
        이미 계정이 있나요? <Link to={ROUTES.LOGIN}>로그인</Link>
      </p>
    </main>
  );
}
