// 경기 영상 목록, 상세, 수정, 삭제와 분석 작업 패널 렌더링을 관리하는 파일

import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import { getManagementPlayerAnalysisClipDetail } from "../api/playerAnalysisClipApi";
import { getTeamAnalysisClipDetail } from "../api/teamAnalysisClipApi";
import PlayerAnalysisClipEditorPanel from "../components/analysis/PlayerAnalysisClipEditorPanel";
import PlayerRecordEventEditorPanel from "../components/analysis/PlayerRecordEventEditorPanel";
import TeamAnalysisClipEditorPanel from "../components/analysis/TeamAnalysisClipEditorPanel";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";
import { useAuth } from "../hooks/useAuth";
import { getApiErrorMessage } from "../utils/apiError";
import { createVideoSourceUrl } from "../utils/videoUrl";
import {
  ROUTES,
  createMatchVideoAnalysisRoute,
  type MatchVideoAnalysisMode,
} from "../constants/routes";
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

type AnalysisMode = "none" | MatchVideoAnalysisMode;

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

function parseAnalysisMode(searchParams: URLSearchParams): AnalysisMode {
  const analysisMode = searchParams.get("analysisMode");

  if (
    analysisMode === "team-clip-create" ||
    analysisMode === "team-clip-edit" ||
    analysisMode === "player-clip-create" ||
    analysisMode === "player-clip-edit" ||
    analysisMode === "player-record-event"
  ) {
    return analysisMode;
  }

  return "none";
}

function parseNumberSearchParam(searchParams: URLSearchParams, key: string) {
  const value = searchParams.get(key);

  if (value === null) {
    return undefined;
  }

  const parsedValue = Number(value);

  return Number.isInteger(parsedValue) && parsedValue > 0
    ? parsedValue
    : undefined;
}

export default function MatchVideoPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { member } = useAuth();

  const analysisMode = parseAnalysisMode(searchParams);
  const teamClipId = parseNumberSearchParam(searchParams, "teamClipId");
  const playerClipId = parseNumberSearchParam(searchParams, "playerClipId");

  const [matchVideos, setMatchVideos] = useState<MatchVideoListItem[]>([]);
  const [selectedVideo, setSelectedVideo] =
    useState<MatchVideoDetailResponse | null>(null);
  const [form, setForm] = useState<UpdateMatchVideoRequest>(INITIAL_FORM_STATE);

  const [isEditMode, setIsEditMode] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const isManager =
    member?.memberRole === "COACH" || member?.memberRole === "ANALYST";
  const isCoach = member?.memberRole === "COACH";
  const isAnalysisModeActive = analysisMode !== "none";

  useEffect(() => {
    let ignore = false;

    async function fetchInitialMatchVideos() {
      try {
        const response = await getMatchVideos(0, 20);

        if (ignore) {
          return;
        }

        setMatchVideos(response.matchVideos);
        setPage(response.page);
        setTotalPages(response.totalPages);
        setErrorMessage("");
      } catch (error) {
        if (ignore) {
          return;
        }

        setErrorMessage(getApiErrorMessage(error));
      } finally {
        if (!ignore) {
          setIsLoading(false);
        }
      }
    }

    void fetchInitialMatchVideos();

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    let ignore = false;

    async function fetchTeamClipEditTargetVideo() {
      if (analysisMode !== "team-clip-edit" || teamClipId === undefined) {
        return;
      }

      try {
        const clipDetail = await getTeamAnalysisClipDetail(teamClipId);

        if (ignore) {
          return;
        }

        const matchVideoDetail = await getMatchVideoDetail(
          clipDetail.matchVideoId,
        );

        if (ignore) {
          return;
        }

        setSelectedVideo(matchVideoDetail);
        setForm(createFormFromDetail(matchVideoDetail));
        setIsEditMode(false);
        setErrorMessage("");
      } catch (error) {
        if (ignore) {
          return;
        }

        setSelectedVideo(null);
        setErrorMessage(
          `수정할 팀 분석 클립의 경기 영상을 불러오지 못했습니다. ${getApiErrorMessage(
            error,
          )}`,
        );
      }
    }

    void fetchTeamClipEditTargetVideo();

    return () => {
      ignore = true;
    };
  }, [analysisMode, teamClipId]);

  useEffect(() => {
    let ignore = false;

    async function fetchPlayerClipEditTargetVideo() {
      if (analysisMode !== "player-clip-edit" || playerClipId === undefined) {
        return;
      }

      try {
        const clipDetail =
          await getManagementPlayerAnalysisClipDetail(playerClipId);

        if (ignore) {
          return;
        }

        const matchVideoDetail = await getMatchVideoDetail(
          clipDetail.matchVideoId,
        );

        if (ignore) {
          return;
        }

        setSelectedVideo(matchVideoDetail);
        setForm(createFormFromDetail(matchVideoDetail));
        setIsEditMode(false);
        setErrorMessage("");
      } catch (error) {
        if (ignore) {
          return;
        }

        setSelectedVideo(null);
        setErrorMessage(
          `수정할 선수 개인 분석 클립의 경기 영상을 불러오지 못했습니다. ${getApiErrorMessage(
            error,
          )}`,
        );
      }
    }

    void fetchPlayerClipEditTargetVideo();

    return () => {
      ignore = true;
    };
  }, [analysisMode, playerClipId]);

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

      if (
        analysisMode === "team-clip-edit" ||
        analysisMode === "player-clip-edit"
      ) {
        navigate(ROUTES.MATCH_VIDEO, { replace: true });
      }
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    }
  }

  function handleChangeAnalysisMode(nextAnalysisMode: MatchVideoAnalysisMode) {
    navigate(
      createMatchVideoAnalysisRoute({
        analysisMode: nextAnalysisMode,
      }),
      { replace: true },
    );
  }

  function handleCloseAnalysisMode() {
    navigate(ROUTES.MATCH_VIDEO, { replace: true });
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

  async function handleUpdateSubmit(event: FormEvent<HTMLFormElement>) {
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

      const updateRequest: UpdateMatchVideoRequest = {
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
      handleCloseAnalysisMode();

      await fetchMatchVideos(0);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleAnalysisSaved() {
    setSuccessMessage("분석 작업이 저장되었습니다.");
  }

  return (
    <AuthenticatedLayout title="경기 영상">
      <main className="page">
        <section className="page-header">
          <div>
            <h1>경기 영상</h1>
            <p>경기 영상을 조회하고 분석 작업의 기준 영상을 선택합니다.</p>
          </div>

          {isManager && (
            <div className="button-row">
              <button
                type="button"
                onClick={() => navigate(ROUTES.MATCH_VIDEO_CREATE)}
              >
                경기 영상 등록
              </button>
            </div>
          )}
        </section>

        {isLoading && <p>경기 영상을 불러오는 중입니다.</p>}
        {errorMessage && <p className="error-message">{errorMessage}</p>}
        {successMessage && <p className="success-message">{successMessage}</p>}

        {isManager && isAnalysisModeActive && selectedVideo === null && (
          <section className="card">
            <h2>분석 작업을 진행할 경기 영상 준비</h2>
            <p className="helper-text">
              수정 모드는 기존 클립의 원본 경기 영상을 자동으로 불러옵니다. 등록
              모드라면 아래 경기 영상 목록에서 작업할 영상을 선택하세요.
            </p>
          </section>
        )}

        <section className="content-grid">
          <article className="card">
            <h2>경기 영상 목록</h2>

            {matchVideos.length === 0 && !isLoading ? (
              <p>등록된 경기 영상이 없습니다.</p>
            ) : (
              <ul className="item-list">
                {matchVideos.map((matchVideo) => (
                  <li key={matchVideo.matchVideoId}>
                    <button
                      type="button"
                      onClick={() => handleSelectVideo(matchVideo.matchVideoId)}
                    >
                      <strong>{matchVideo.title}</strong>
                      <span>
                        {matchVideo.gameDate} / {matchVideo.place}
                      </span>
                      <span>
                        {matchVideo.homeScore} : {matchVideo.awayScore} /{" "}
                        {matchVideo.matchResult}
                      </span>
                      <span>
                        상태: {matchVideo.status} / 길이:{" "}
                        {formatDuration(matchVideo.durationSec)}
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            )}

            <div className="button-row">
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
                disabled={page + 1 >= totalPages}
                onClick={() => fetchMatchVideos(page + 1)}
              >
                다음
              </button>
            </div>
          </article>

          {selectedVideo && !isAnalysisModeActive && (
            <article className="card">
              <h2>경기 영상 상세</h2>

              <MatchVideoPlayer
                matchVideoId={selectedVideo.matchVideoId}
                videoUrl={selectedVideo.url}
              />

              <dl className="detail-list">
                <div>
                  <dt>제목</dt>
                  <dd>{selectedVideo.title}</dd>
                </div>
                <div>
                  <dt>경기일시</dt>
                  <dd>{selectedVideo.gameDate}</dd>
                </div>
                <div>
                  <dt>장소</dt>
                  <dd>{selectedVideo.place}</dd>
                </div>
                <div>
                  <dt>점수</dt>
                  <dd>
                    {selectedVideo.homeScore} : {selectedVideo.awayScore}
                  </dd>
                </div>
                <div>
                  <dt>결과</dt>
                  <dd>{selectedVideo.matchResult}</dd>
                </div>
                <div>
                  <dt>상태</dt>
                  <dd>{selectedVideo.status}</dd>
                </div>
                <div>
                  <dt>영상 길이</dt>
                  <dd>{formatDuration(selectedVideo.durationSec)}</dd>
                </div>
                <div>
                  <dt>업로더</dt>
                  <dd>{selectedVideo.uploaderName}</dd>
                </div>
              </dl>

              {isManager && (
                <>
                  <div className="button-row">
                    <button
                      type="button"
                      onClick={() => setIsEditMode(true)}
                      disabled={isSubmitting}
                    >
                      메타데이터 수정
                    </button>

                    {isCoach && (
                      <button
                        type="button"
                        onClick={handleDelete}
                        disabled={isSubmitting}
                      >
                        삭제
                      </button>
                    )}
                  </div>

                  <div className="button-row">
                    <button
                      type="button"
                      onClick={() =>
                        handleChangeAnalysisMode("team-clip-create")
                      }
                    >
                      팀 분석 클립 등록
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        handleChangeAnalysisMode("player-clip-create")
                      }
                    >
                      선수 개인 분석 클립 등록
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        handleChangeAnalysisMode("player-record-event")
                      }
                    >
                      선수 기록 이벤트 등록
                    </button>
                  </div>
                </>
              )}
            </article>
          )}

          {isManager && selectedVideo && isAnalysisModeActive && (
            <div className="analysis-panel-area">
              <section className="card">
                <div className="button-row">
                  <button type="button" onClick={handleCloseAnalysisMode}>
                    분석 작업 닫기
                  </button>
                </div>
              </section>

              {(analysisMode === "team-clip-create" ||
                analysisMode === "team-clip-edit") && (
                <TeamAnalysisClipEditorPanel
                  key={`team-clip-${analysisMode}-${
                    selectedVideo.matchVideoId
                  }-${teamClipId ?? "new"}`}
                  mode={analysisMode === "team-clip-edit" ? "edit" : "create"}
                  matchVideo={selectedVideo}
                  teamClipId={teamClipId}
                  onSaved={handleAnalysisSaved}
                />
              )}

              {(analysisMode === "player-clip-create" ||
                analysisMode === "player-clip-edit") && (
                <PlayerAnalysisClipEditorPanel
                  key={`player-clip-${analysisMode}-${
                    selectedVideo.matchVideoId
                  }-${playerClipId ?? "new"}`}
                  mode={analysisMode === "player-clip-edit" ? "edit" : "create"}
                  matchVideo={selectedVideo}
                  playerClipId={playerClipId}
                  onSaved={handleAnalysisSaved}
                />
              )}

              {analysisMode === "player-record-event" && (
                <PlayerRecordEventEditorPanel
                  key={`player-record-event-${selectedVideo.matchVideoId}`}
                  matchVideo={selectedVideo}
                  onSaved={handleAnalysisSaved}
                />
              )}
            </div>
          )}
        </section>

        {isManager && isEditMode && selectedVideo && !isAnalysisModeActive && (
          <section className="card">
            <h2>경기 영상 메타데이터 수정</h2>

            <form onSubmit={handleUpdateSubmit} className="form-grid">
              <MatchVideoFormFields
                form={form}
                onChangeForm={handleChangeForm}
                onChangeMatchResult={handleChangeMatchResult}
              />

              <div className="button-row">
                <button type="submit" disabled={isSubmitting}>
                  수정 저장
                </button>
                <button type="button" onClick={handleCancelEdit}>
                  취소
                </button>
              </div>
            </form>
          </section>
        )}
      </main>
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
    <video key={matchVideoId} controls width="100%">
      <source src={videoSourceUrl} type="video/mp4" />
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
      <label>
        경기 제목
        <input
          type="text"
          value={form.title}
          onChange={(event) => onChangeForm("title", event.target.value)}
        />
      </label>

      <label>
        경기 일시
        <input
          type="datetime-local"
          value={form.gameDate}
          onChange={(event) => onChangeForm("gameDate", event.target.value)}
        />
      </label>

      <label>
        장소
        <input
          type="text"
          value={form.place}
          onChange={(event) => onChangeForm("place", event.target.value)}
        />
      </label>

      <label>
        홈팀 점수
        <input
          type="number"
          min="0"
          max="255"
          value={form.homeScore}
          onChange={(event) => onChangeForm("homeScore", event.target.value)}
        />
      </label>

      <label>
        원정팀 점수
        <input
          type="number"
          min="0"
          max="255"
          value={form.awayScore}
          onChange={(event) => onChangeForm("awayScore", event.target.value)}
        />
      </label>

      <label>
        경기 결과
        <select
          value={form.matchResult}
          onChange={(event) => onChangeMatchResult(event.target.value)}
        >
          <option value="WIN">승</option>
          <option value="DRAW">무</option>
          <option value="LOSS">패</option>
        </select>
      </label>
    </>
  );
}
