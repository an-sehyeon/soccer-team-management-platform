// 경기 영상 목록, 상세, 수정, 삭제 화면을 관리하는 파일

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";
import { useAuth } from "../hooks/useAuth";
import { getApiErrorMessage } from "../utils/apiError";
import { createVideoSourceUrl } from "../utils/videoUrl";
import { ROUTES } from "../constants/routes";
import {
  deleteMatchVideo,
  getMatchVideoDetail,
  getMatchVideos,
  updateMatchVideo,
} from "../api/matchVideoApi";
import type {
  MatchResult,
  MatchVideoDetailResponse,
  MatchVideoListItem,
  UpdateMatchVideoRequest,
} from "../types/matchVideo";

const INITIAL_FORM_STATE: UpdateMatchVideoRequest = {
  title: "",
  gameDate: "",
  place: "",
  homeScore: 0,
  awayScore: 0,
  matchResult: "WIN",
};

function formatDuration(durationSec: number | null) {
  if (durationSec === null || durationSec <= 0) {
    return "길이 정보 없음";
  }

  const hours = Math.floor(durationSec / 3600);
  const minutes = Math.floor((durationSec % 3600) / 60);
  const seconds = durationSec % 60;

  if (hours > 0) {
    return `${hours}시간 ${minutes}분 ${seconds}초`;
  }

  return `${minutes}분 ${seconds}초`;
}

function toDateTimeLocalValue(dateTime: string) {
  return dateTime.slice(0, 16);
}

function toServerDateTimeValue(dateTimeLocalValue: string) {
  if (dateTimeLocalValue.length === 16) {
    return `${dateTimeLocalValue}:00`;
  }

  return dateTimeLocalValue;
}

function createFormFromDetail(
  detail: MatchVideoDetailResponse,
): UpdateMatchVideoRequest {
  return {
    title: detail.title,
    gameDate: toDateTimeLocalValue(detail.gameDate),
    place: detail.place,
    homeScore: detail.homeScore,
    awayScore: detail.awayScore,
    matchResult: detail.matchResult,
  };
}

export default function MatchVideoPage() {
  const navigate = useNavigate();
  const { member } = useAuth();

  const [matchVideos, setMatchVideos] = useState<MatchVideoListItem[]>([]);
  const [selectedVideo, setSelectedVideo] =
    useState<MatchVideoDetailResponse | null>(null);

  const [form, setForm] = useState<UpdateMatchVideoRequest>(INITIAL_FORM_STATE);
  const [isEditMode, setIsEditMode] = useState(false);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const isManager =
    member?.memberRole === "COACH" || member?.memberRole === "ANALYST";
  const isCoach = member?.memberRole === "COACH";

  useEffect(() => {
    fetchMatchVideos(0);
  }, []);

  async function fetchMatchVideos(nextPage: number) {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const response = await getMatchVideos(nextPage, 20);

      setMatchVideos(response.matchVideos);
      setPage(response.page);
      setTotalPages(response.totalPages);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }

  async function handleSelectVideo(matchVideoId: number) {
    try {
      setErrorMessage("");
      setSuccessMessage("");

      const detail = await getMatchVideoDetail(matchVideoId);

      setSelectedVideo(detail);
      setIsEditMode(false);
      setForm(createFormFromDetail(detail));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    }
  }

  function handleChangeForm(
    field: keyof UpdateMatchVideoRequest,
    value: string,
  ) {
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

  function validateUpdateForm() {
    if (!form.title.trim()) {
      return "경기 제목을 입력해주세요.";
    }

    if (!form.gameDate) {
      return "경기 일시를 입력해주세요.";
    }

    if (!form.place.trim()) {
      return "장소를 입력해주세요.";
    }

    if (!Number.isInteger(Number(form.homeScore))) {
      return "홈팀 점수는 숫자로 입력해주세요.";
    }

    if (!Number.isInteger(Number(form.awayScore))) {
      return "원정팀 점수는 숫자로 입력해주세요.";
    }

    if (Number(form.homeScore) < 0 || Number(form.awayScore) < 0) {
      return "점수는 0 이상으로 입력해주세요.";
    }

    if (Number(form.homeScore) > 255 || Number(form.awayScore) > 255) {
      return "점수는 255 이하로 입력해주세요.";
    }

    return "";
  }

  function handleCancelEdit() {
    if (selectedVideo) {
      setForm(createFormFromDetail(selectedVideo));
    }

    setIsEditMode(false);
    setErrorMessage("");
  }

  async function handleUpdateSubmit(event: { preventDefault: () => void }) {
    event.preventDefault();

    if (!selectedVideo) {
      setErrorMessage("수정할 경기 영상을 먼저 선택해주세요.");
      return;
    }

    const validationMessage = validateUpdateForm();

    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");
      setSuccessMessage("");

      const updateRequest = {
        title: form.title.trim(),
        gameDate: toServerDateTimeValue(form.gameDate),
        place: form.place.trim(),
        homeScore: Number(form.homeScore),
        awayScore: Number(form.awayScore),
        matchResult: form.matchResult,
      };

      const updatedVideo = await updateMatchVideo(
        selectedVideo.matchVideoId,
        updateRequest,
      );

      setSelectedVideo(updatedVideo);
      setForm(createFormFromDetail(updatedVideo));
      setSuccessMessage("경기 영상 정보가 수정되었습니다.");
      setIsEditMode(false);

      await fetchMatchVideos(page);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!selectedVideo) {
      setErrorMessage("삭제할 경기 영상을 먼저 선택해주세요.");
      return;
    }

    const confirmed = window.confirm("선택한 경기 영상을 삭제하시겠습니까?");

    if (!confirmed) {
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");
      setSuccessMessage("");

      const response = await deleteMatchVideo(selectedVideo.matchVideoId);

      setSuccessMessage(response.message);
      setSelectedVideo(null);
      setForm(INITIAL_FORM_STATE);
      setIsEditMode(false);

      await fetchMatchVideos(0);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <AuthenticatedLayout title="경기 영상">
      <section>
        <h2>경기 영상 목록</h2>

        {isLoading && <p>경기 영상을 불러오는 중입니다.</p>}
        {errorMessage && <p>{errorMessage}</p>}
        {successMessage && <p>{successMessage}</p>}

        {isManager && (
          <button
            type="button"
            onClick={() => navigate(ROUTES.MATCH_VIDEO_CREATE)}
          >
            경기 영상 등록
          </button>
        )}

        {matchVideos.length === 0 && !isLoading ? (
          <p>등록된 경기 영상이 없습니다.</p>
        ) : (
          <ul>
            {matchVideos.map((matchVideo) => (
              <li key={matchVideo.matchVideoId}>
                <button
                  type="button"
                  onClick={() => handleSelectVideo(matchVideo.matchVideoId)}
                >
                  {matchVideo.title}
                </button>

                <p>
                  {matchVideo.gameDate} / {matchVideo.place}
                </p>
                <p>
                  {matchVideo.homeScore} : {matchVideo.awayScore} /{" "}
                  {matchVideo.matchResult}
                </p>
                <p>
                  상태: {matchVideo.status} / 길이:{" "}
                  {formatDuration(matchVideo.durationSec)}
                </p>
              </li>
            ))}
          </ul>
        )}

        <div>
          <button
            type="button"
            disabled={page <= 0}
            onClick={() => fetchMatchVideos(page - 1)}
          >
            이전
          </button>

          <span>
            {page + 1} / {Math.max(totalPages, 1)}
          </span>

          <button
            type="button"
            disabled={totalPages === 0 || page + 1 >= totalPages}
            onClick={() => fetchMatchVideos(page + 1)}
          >
            다음
          </button>
        </div>
      </section>

      {selectedVideo && (
        <section>
          <h2>경기 영상 상세</h2>

          <MatchVideoPlayer
            matchVideoId={selectedVideo.matchVideoId}
            videoUrl={selectedVideo.url}
          />

          <p>제목: {selectedVideo.title}</p>
          <p>경기일시: {selectedVideo.gameDate}</p>
          <p>장소: {selectedVideo.place}</p>
          <p>
            점수: {selectedVideo.homeScore} : {selectedVideo.awayScore}
          </p>
          <p>결과: {selectedVideo.matchResult}</p>
          <p>상태: {selectedVideo.status}</p>
          <p>영상 길이: {formatDuration(selectedVideo.durationSec)}</p>
          <p>업로더: {selectedVideo.uploaderName}</p>

          {isManager && (
            <button
              type="button"
              onClick={() => setIsEditMode(true)}
              disabled={isSubmitting}
            >
              메타데이터 수정
            </button>
          )}

          {isCoach && (
            <button
              type="button"
              onClick={handleDelete}
              disabled={isSubmitting}
            >
              삭제
            </button>
          )}
        </section>
      )}

      {isManager && isEditMode && selectedVideo && (
        <section>
          <h2>경기 영상 메타데이터 수정</h2>

          <form onSubmit={handleUpdateSubmit}>
            <MatchVideoFormFields
              form={form}
              onChangeForm={handleChangeForm}
              onChangeMatchResult={handleChangeMatchResult}
            />

            <button type="submit" disabled={isSubmitting}>
              수정 저장
            </button>

            <button
              type="button"
              onClick={handleCancelEdit}
              disabled={isSubmitting}
            >
              취소
            </button>
          </form>
        </section>
      )}
    </AuthenticatedLayout>
  );
}

type MatchVideoPlayerProps = {
  matchVideoId: number;
  videoUrl: string;
};

function MatchVideoPlayer({ matchVideoId, videoUrl }: MatchVideoPlayerProps) {
  const videoSourceUrl = createVideoSourceUrl(videoUrl);

  return (
    <video
      key={`${matchVideoId}-${videoSourceUrl}`}
      controls
      preload="metadata"
      src={videoSourceUrl}
      style={{ width: "100%", maxWidth: "900px", backgroundColor: "#000" }}
    >
      브라우저에서 video 태그를 지원하지 않습니다.
    </video>
  );
}

type MatchVideoFormFieldsProps = {
  form: UpdateMatchVideoRequest;
  onChangeForm: (field: keyof UpdateMatchVideoRequest, value: string) => void;
  onChangeMatchResult: (value: string) => void;
};

function MatchVideoFormFields({
  form,
  onChangeForm,
  onChangeMatchResult,
}: MatchVideoFormFieldsProps) {
  return (
    <>
      <div>
        <label htmlFor="title">경기 제목</label>
        <input
          id="title"
          type="text"
          value={form.title}
          onChange={(event) => onChangeForm("title", event.target.value)}
        />
      </div>

      <div>
        <label htmlFor="gameDate">경기 일시</label>
        <input
          id="gameDate"
          type="datetime-local"
          value={form.gameDate}
          onChange={(event) => onChangeForm("gameDate", event.target.value)}
        />
      </div>

      <div>
        <label htmlFor="place">장소</label>
        <input
          id="place"
          type="text"
          value={form.place}
          onChange={(event) => onChangeForm("place", event.target.value)}
        />
      </div>

      <div>
        <label htmlFor="homeScore">홈팀 점수</label>
        <input
          id="homeScore"
          type="number"
          min="0"
          value={form.homeScore}
          onChange={(event) => onChangeForm("homeScore", event.target.value)}
        />
      </div>

      <div>
        <label htmlFor="awayScore">원정팀 점수</label>
        <input
          id="awayScore"
          type="number"
          min="0"
          value={form.awayScore}
          onChange={(event) => onChangeForm("awayScore", event.target.value)}
        />
      </div>

      <div>
        <label htmlFor="matchResult">경기 결과</label>
        <select
          id="matchResult"
          value={form.matchResult}
          onChange={(event) => onChangeMatchResult(event.target.value)}
        >
          <option value="WIN">승</option>
          <option value="DRAW">무</option>
          <option value="LOSS">패</option>
        </select>
      </div>
    </>
  );
}
