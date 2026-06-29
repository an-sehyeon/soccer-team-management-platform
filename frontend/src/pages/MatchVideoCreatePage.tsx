// 경기 영상 등록 전용 화면을 제공하는 파일

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";
import { createMatchVideo } from "../api/matchVideoApi";
import { getApiErrorMessage } from "../utils/apiError";
import { ROUTES } from "../constants/routes";
import type { MatchResult } from "../types/matchVideo";

type CreateMatchVideoForm = {
  title: string;
  gameDate: string;
  place: string;
  homeScore: number;
  awayScore: number;
  matchResult: MatchResult;
};

const INITIAL_FORM: CreateMatchVideoForm = {
  title: "",
  gameDate: "",
  place: "",
  homeScore: 0,
  awayScore: 0,
  matchResult: "WIN",
};

function toServerDateTimeValue(dateTimeLocalValue: string) {
  if (dateTimeLocalValue.length === 16) {
    return `${dateTimeLocalValue}:00`;
  }

  return dateTimeLocalValue;
}

export default function MatchVideoCreatePage() {
  const navigate = useNavigate();

  const [form, setForm] = useState<CreateMatchVideoForm>(INITIAL_FORM);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  function handleChangeForm(field: keyof CreateMatchVideoForm, value: string) {
    setForm((prevForm) => ({
      ...prevForm,
      [field]:
        field === "homeScore" || field === "awayScore" ? Number(value) : value,
    }));
  }

  function handleChangeMatchResult(value: string) {
    setForm((prevForm) => ({
      ...prevForm,
      matchResult: value as MatchResult,
    }));
  }

  function validateForm() {
    if (!selectedFile) {
      return "업로드할 mp4 경기 영상을 선택해주세요.";
    }

    const isMp4File =
      selectedFile.type === "video/mp4" ||
      selectedFile.name.toLowerCase().endsWith(".mp4");

    if (!isMp4File) {
      return "mp4 파일만 업로드할 수 있습니다.";
    }

    if (!form.title.trim()) {
      return "경기 제목을 입력해주세요.";
    }

    if (!form.gameDate) {
      return "경기 일시를 입력해주세요.";
    }

    if (!form.place.trim()) {
      return "장소를 입력해주세요.";
    }

    if (form.homeScore < 0 || form.awayScore < 0) {
      return "점수는 0 이상으로 입력해주세요.";
    }

    return "";
  }

  async function handleCreateSubmit(event: { preventDefault: () => void }) {
    event.preventDefault();

    const validationMessage = validateForm();

    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    if (!selectedFile) {
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");

      await createMatchVideo({
        videoFile: selectedFile,
        title: form.title,
        gameDate: toServerDateTimeValue(form.gameDate),
        place: form.place,
        homeScore: form.homeScore,
        awayScore: form.awayScore,
        matchResult: form.matchResult,
      });

      alert("경기 영상이 등록되었습니다.");
      navigate(ROUTES.MATCH_VIDEO);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <AuthenticatedLayout title="경기 영상 등록">
      <section>
        <h2>경기 영상 등록</h2>

        {errorMessage && <p>{errorMessage}</p>}

        <form onSubmit={handleCreateSubmit}>
          <div>
            <label htmlFor="videoFile">경기 영상 파일</label>
            <input
              id="videoFile"
              type="file"
              accept="video/mp4"
              onChange={(event) =>
                setSelectedFile(event.target.files?.[0] ?? null)
              }
            />
          </div>

          <div>
            <label htmlFor="title">경기 제목</label>
            <input
              id="title"
              type="text"
              value={form.title}
              onChange={(event) =>
                handleChangeForm("title", event.target.value)
              }
            />
          </div>

          <div>
            <label htmlFor="gameDate">경기 일시</label>
            <input
              id="gameDate"
              type="datetime-local"
              value={form.gameDate}
              onChange={(event) =>
                handleChangeForm("gameDate", event.target.value)
              }
            />
          </div>

          <div>
            <label htmlFor="place">장소</label>
            <input
              id="place"
              type="text"
              value={form.place}
              onChange={(event) =>
                handleChangeForm("place", event.target.value)
              }
            />
          </div>

          <div>
            <label htmlFor="homeScore">홈팀 점수</label>
            <input
              id="homeScore"
              type="number"
              min="0"
              value={form.homeScore}
              onChange={(event) =>
                handleChangeForm("homeScore", event.target.value)
              }
            />
          </div>

          <div>
            <label htmlFor="awayScore">원정팀 점수</label>
            <input
              id="awayScore"
              type="number"
              min="0"
              value={form.awayScore}
              onChange={(event) =>
                handleChangeForm("awayScore", event.target.value)
              }
            />
          </div>

          <div>
            <label htmlFor="matchResult">경기 결과</label>
            <select
              id="matchResult"
              value={form.matchResult}
              onChange={(event) => handleChangeMatchResult(event.target.value)}
            >
              <option value="WIN">승</option>
              <option value="DRAW">무</option>
              <option value="LOSS">패</option>
            </select>
          </div>

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "업로드 중..." : "등록"}
          </button>

          <button
            type="button"
            onClick={() => navigate(ROUTES.MATCH_VIDEO)}
            disabled={isSubmitting}
          >
            목록으로
          </button>
        </form>
      </section>
    </AuthenticatedLayout>
  );
}
