// 팀 분석 클립 목록, 상세, 등록, 수정, 삭제와 드로잉 연동 화면을 제공하는 페이지

import { useContext, useEffect, useRef, useState } from "react";
import {
  createTeamAnalysisClip,
  deleteTeamAnalysisClip,
  getTeamAnalysisClipDetail,
  getTeamAnalysisClips,
  updateTeamAnalysisClip,
} from "../api/teamAnalysisClipApi";
import {
  createTeamAnalysisClipDrawing,
  deleteTeamAnalysisClipDrawing,
  getTeamAnalysisClipDrawings,
  updateTeamAnalysisClipDrawing,
} from "../api/teamAnalysisClipDrawingApi";
import { getMatchVideos } from "../api/matchVideoApi";
import TeamAnalysisDrawingCanvas from "../components/TeamAnalysisDrawingCanvas";
import { AuthContext } from "../contexts/authContext";
import type { MatchVideoListItem } from "../types/matchVideo";
import type {
  CreateTeamAnalysisClipRequest,
  TeamAnalysisClipDetailResponse,
  TeamAnalysisClipListItem,
  TeamAnalysisClipType,
  UpdateTeamAnalysisClipRequest,
} from "../types/teamAnalysisClip";
import {
  TEAM_ANALYSIS_CLIP_TYPE_LABELS,
  TEAM_ANALYSIS_CLIP_TYPE_OPTIONS,
} from "../types/teamAnalysisClip";
import type {
  CreateTeamAnalysisClipDrawingRequest,
  TeamAnalysisClipDrawingResponse,
  TeamAnalysisClipDrawingType,
} from "../types/teamAnalysisClipDrawing";
import {
  TEAM_ANALYSIS_CLIP_DRAWING_TYPE_LABELS,
  TEAM_ANALYSIS_CLIP_DRAWING_TYPE_OPTIONS,
} from "../types/teamAnalysisClipDrawing";
import { createVideoSourceUrl } from "../utils/videoUrl";

const initialCreateForm: CreateTeamAnalysisClipRequest = {
  matchVideoId: 0,
  clipType: "HIGHLIGHT",
  title: "",
  comment: "",
  startTimeSec: 0,
  endTimeSec: 0,
};

const initialUpdateForm: UpdateTeamAnalysisClipRequest = {
  clipType: "HIGHLIGHT",
  title: "",
  comment: "",
  startTimeSec: 0,
  endTimeSec: 0,
};

const initialDrawingForm: CreateTeamAnalysisClipDrawingRequest = {
  drawingType: "ARROW",
  startTimeSec: 0,
  endTimeSec: 0,
  drawingData: {},
};

function createInitialDrawingFormByClip(
  clip: TeamAnalysisClipDetailResponse,
  currentTime?: number,
): CreateTeamAnalysisClipDrawingRequest {
  const currentTimeSec = Math.floor(currentTime ?? clip.startTimeSec);
  const maxStartTimeSec = Math.max(clip.startTimeSec, clip.endTimeSec - 1);

  const startTimeSec = Math.min(
    Math.max(clip.startTimeSec, currentTimeSec),
    maxStartTimeSec,
  );

  const endTimeSec = Math.min(startTimeSec + 5, clip.endTimeSec);

  return {
    drawingType: "ARROW",
    startTimeSec,
    endTimeSec,
    drawingData: {},
  };
}

export default function TeamAnalysisClipPage() {
  const authContext = useContext(AuthContext);
  const member = authContext?.member ?? null;

  const videoRef = useRef<HTMLVideoElement | null>(null);

  const [clips, setClips] = useState<TeamAnalysisClipListItem[]>([]);
  const [matchVideos, setMatchVideos] = useState<MatchVideoListItem[]>([]);
  const [selectedClip, setSelectedClip] =
    useState<TeamAnalysisClipDetailResponse | null>(null);

  const [createForm, setCreateForm] =
    useState<CreateTeamAnalysisClipRequest>(initialCreateForm);

  const [updateForm, setUpdateForm] =
    useState<UpdateTeamAnalysisClipRequest>(initialUpdateForm);

  const [selectedClipTypeFilter, setSelectedClipTypeFilter] = useState<
    TeamAnalysisClipType | ""
  >("");

  const [selectedMatchVideoFilter, setSelectedMatchVideoFilter] =
    useState<number>(0);

  const [isEditing, setIsEditing] = useState(false);
  const [message, setMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const [drawings, setDrawings] = useState<TeamAnalysisClipDrawingResponse[]>(
    [],
  );
  const [currentTimeSec, setCurrentTimeSec] = useState(0);
  const [isDrawingMode, setIsDrawingMode] = useState(false);
  const [drawingText, setDrawingText] = useState("");
  const [drawingForm, setDrawingForm] =
    useState<CreateTeamAnalysisClipDrawingRequest>(initialDrawingForm);
  const [editingDrawingId, setEditingDrawingId] = useState<number | null>(null);
  const [drawingMessage, setDrawingMessage] = useState("");
  const [drawingErrorMessage, setDrawingErrorMessage] = useState("");

  const canManageTeamClip =
    member?.memberRole === "COACH" || member?.memberRole === "ANALYST";

  const canDeleteTeamClip = member?.memberRole === "COACH";

  const canManageDrawing =
    member?.memberRole === "COACH" || member?.memberRole === "ANALYST";

  const canDeleteDrawing = member?.memberRole === "COACH";

  const loadTeamAnalysisClips = async () => {
    try {
      const response = await getTeamAnalysisClips({
        page: 0,
        size: 20,
        matchVideoId:
          selectedMatchVideoFilter > 0 ? selectedMatchVideoFilter : undefined,
        clipType:
          selectedClipTypeFilter === "" ? undefined : selectedClipTypeFilter,
      });

      setClips(
        Array.isArray(response.teamAnalysisClips)
          ? response.teamAnalysisClips
          : [],
      );
    } catch {
      setErrorMessage("팀 분석 클립 목록을 불러오지 못했습니다.");
    }
  };

  const loadTeamAnalysisClipDrawings = async (teamClipId: number) => {
    try {
      setDrawingErrorMessage("");

      const response = await getTeamAnalysisClipDrawings(teamClipId);

      setDrawings(Array.isArray(response) ? response : []);
    } catch {
      setDrawingErrorMessage("팀 분석 클립 드로잉 목록을 불러오지 못했습니다.");
    }
  };

  const loadTeamAnalysisClipDetail = async (teamClipId: number) => {
    try {
      setErrorMessage("");
      setMessage("");
      setDrawingMessage("");
      setDrawingErrorMessage("");

      const detail = await getTeamAnalysisClipDetail(teamClipId);

      setSelectedClip(detail);
      setIsEditing(false);
      setUpdateForm({
        clipType: detail.clipType,
        title: detail.title,
        comment: detail.comment ?? "",
        startTimeSec: detail.startTimeSec,
        endTimeSec: detail.endTimeSec,
      });

      setDrawings([]);
      setEditingDrawingId(null);
      setIsDrawingMode(false);
      setDrawingText("");
      setCurrentTimeSec(detail.startTimeSec);
      setDrawingForm(createInitialDrawingFormByClip(detail));

      await loadTeamAnalysisClipDrawings(detail.teamClipId);
    } catch {
      setErrorMessage("팀 분석 클립 상세 정보를 불러오지 못했습니다.");
    }
  };

  const handleCreateSubmit = async (event: { preventDefault: () => void }) => {
    event.preventDefault();

    if (createForm.matchVideoId <= 0) {
      setErrorMessage("원본 경기 영상을 선택해주세요.");
      return;
    }

    if (createForm.title.trim() === "") {
      setErrorMessage("클립 제목을 입력해주세요.");
      return;
    }

    if (createForm.startTimeSec >= createForm.endTimeSec) {
      setErrorMessage("시작 시간은 종료 시간보다 작아야 합니다.");
      return;
    }

    try {
      setErrorMessage("");
      setMessage("");

      const request: CreateTeamAnalysisClipRequest = {
        matchVideoId: createForm.matchVideoId,
        clipType: createForm.clipType,
        title: createForm.title.trim(),
        comment:
          createForm.comment === null || createForm.comment.trim() === ""
            ? null
            : createForm.comment.trim(),
        startTimeSec: createForm.startTimeSec,
        endTimeSec: createForm.endTimeSec,
      };

      const response = await createTeamAnalysisClip(request);

      setMessage(response.message);
      setCreateForm(initialCreateForm);
      await loadTeamAnalysisClips();
    } catch {
      setErrorMessage("팀 분석 클립 등록에 실패했습니다.");
    }
  };

  const handleUpdateSubmit = async (event: { preventDefault: () => void }) => {
    event.preventDefault();

    if (!selectedClip) {
      setErrorMessage("수정할 팀 분석 클립을 선택해주세요.");
      return;
    }

    if (updateForm.title.trim() === "") {
      setErrorMessage("클립 제목을 입력해주세요.");
      return;
    }

    if (updateForm.startTimeSec >= updateForm.endTimeSec) {
      setErrorMessage("시작 시간은 종료 시간보다 작아야 합니다.");
      return;
    }

    try {
      setErrorMessage("");
      setMessage("");

      const request: UpdateTeamAnalysisClipRequest = {
        clipType: updateForm.clipType,
        title: updateForm.title.trim(),
        comment:
          updateForm.comment === null || updateForm.comment.trim() === ""
            ? null
            : updateForm.comment.trim(),
        startTimeSec: updateForm.startTimeSec,
        endTimeSec: updateForm.endTimeSec,
      };

      const updatedClip = await updateTeamAnalysisClip(
        selectedClip.teamClipId,
        request,
      );

      setSelectedClip(updatedClip);
      setIsEditing(false);
      setMessage("팀 분석 클립이 수정되었습니다.");

      setCurrentTimeSec(updatedClip.startTimeSec);
      setDrawingForm(
        createInitialDrawingFormByClip(
          updatedClip,
          videoRef.current?.currentTime,
        ),
      );

      await loadTeamAnalysisClips();
      await loadTeamAnalysisClipDrawings(updatedClip.teamClipId);
    } catch {
      setErrorMessage("팀 분석 클립 수정에 실패했습니다.");
    }
  };

  const handleDelete = async () => {
    if (!selectedClip) {
      return;
    }

    const isConfirmed = window.confirm(
      "선택한 팀 분석 클립을 삭제하시겠습니까? 원본 경기 영상은 삭제되지 않습니다.",
    );

    if (!isConfirmed) {
      return;
    }

    try {
      setErrorMessage("");
      setMessage("");
      setDrawingMessage("");
      setDrawingErrorMessage("");

      await deleteTeamAnalysisClip(selectedClip.teamClipId);

      setSelectedClip(null);
      setIsEditing(false);
      setDrawings([]);
      setEditingDrawingId(null);
      setIsDrawingMode(false);
      setDrawingText("");
      setDrawingForm(initialDrawingForm);
      setCurrentTimeSec(0);
      setMessage("팀 분석 클립이 삭제되었습니다.");
      await loadTeamAnalysisClips();
    } catch {
      setErrorMessage("팀 분석 클립 삭제에 실패했습니다.");
    }
  };

  const handlePlayClip = () => {
    const video = videoRef.current;

    if (!video || !selectedClip) {
      return;
    }

    video.currentTime = selectedClip.startTimeSec;
    setCurrentTimeSec(selectedClip.startTimeSec);
    void video.play();
  };

  const handleReplayClip = () => {
    const video = videoRef.current;

    if (!video || !selectedClip) {
      return;
    }

    video.currentTime = selectedClip.startTimeSec;
    setCurrentTimeSec(selectedClip.startTimeSec);
    void video.play();
  };

  const handleVideoTimeUpdate = () => {
    const video = videoRef.current;

    if (!video || !selectedClip) {
      return;
    }

    setCurrentTimeSec(video.currentTime);

    if (video.currentTime >= selectedClip.endTimeSec) {
      video.pause();
    }
  };

  const validateDrawingForm = () => {
    if (!selectedClip) {
      setDrawingErrorMessage("팀 분석 클립을 먼저 선택해주세요.");
      return false;
    }

    if (drawingForm.startTimeSec >= drawingForm.endTimeSec) {
      setDrawingErrorMessage("드로잉 시작 시간은 종료 시간보다 작아야 합니다.");
      return false;
    }

    if (
      drawingForm.startTimeSec < selectedClip.startTimeSec ||
      drawingForm.endTimeSec > selectedClip.endTimeSec
    ) {
      setDrawingErrorMessage(
        "드로잉 시간은 선택한 팀 분석 클립 구간 안에 있어야 합니다.",
      );
      return false;
    }

    if (Object.keys(drawingForm.drawingData).length === 0) {
      setDrawingErrorMessage("영상 위 캔버스에서 드로잉을 먼저 작성해주세요.");
      return false;
    }

    return true;
  };

  const handleCreateDrawingSubmit = async (event: {
    preventDefault: () => void;
  }) => {
    event.preventDefault();

    if (!selectedClip || !validateDrawingForm()) {
      return;
    }

    try {
      setDrawingMessage("");
      setDrawingErrorMessage("");

      const response = await createTeamAnalysisClipDrawing(
        selectedClip.teamClipId,
        drawingForm,
      );

      setDrawingMessage(response.message);
      setEditingDrawingId(null);
      setIsDrawingMode(false);
      setDrawingText("");
      setDrawingForm(
        createInitialDrawingFormByClip(
          selectedClip,
          videoRef.current?.currentTime,
        ),
      );

      await loadTeamAnalysisClipDrawings(selectedClip.teamClipId);
    } catch {
      setDrawingErrorMessage("팀 분석 클립 드로잉 등록에 실패했습니다.");
    }
  };

  const handleUpdateDrawingSubmit = async (event: {
    preventDefault: () => void;
  }) => {
    event.preventDefault();

    if (!selectedClip || editingDrawingId === null || !validateDrawingForm()) {
      return;
    }

    try {
      setDrawingMessage("");
      setDrawingErrorMessage("");

      await updateTeamAnalysisClipDrawing(editingDrawingId, drawingForm);

      setDrawingMessage("팀 분석 클립 드로잉이 수정되었습니다.");
      setEditingDrawingId(null);
      setIsDrawingMode(false);
      setDrawingText("");
      setDrawingForm(
        createInitialDrawingFormByClip(
          selectedClip,
          videoRef.current?.currentTime,
        ),
      );

      await loadTeamAnalysisClipDrawings(selectedClip.teamClipId);
    } catch {
      setDrawingErrorMessage("팀 분석 클립 드로잉 수정에 실패했습니다.");
    }
  };

  const handleStartUpdateDrawing = (
    drawing: TeamAnalysisClipDrawingResponse,
  ) => {
    setEditingDrawingId(drawing.drawingId);
    setDrawingForm({
      drawingType: drawing.drawingType,
      startTimeSec: drawing.startTimeSec,
      endTimeSec: drawing.endTimeSec,
      drawingData: drawing.drawingData,
    });

    const textValue =
      typeof drawing.drawingData.text === "string"
        ? drawing.drawingData.text
        : "";

    setDrawingText(textValue);
    setIsDrawingMode(false);
    setDrawingMessage("");
    setDrawingErrorMessage("");
  };

  const handleCancelDrawingEdit = () => {
    if (!selectedClip) {
      return;
    }

    setEditingDrawingId(null);
    setIsDrawingMode(false);
    setDrawingText("");
    setDrawingForm(
      createInitialDrawingFormByClip(
        selectedClip,
        videoRef.current?.currentTime,
      ),
    );
  };

  const handleDeleteDrawing = async (drawingId: number) => {
    if (!selectedClip) {
      return;
    }

    const isConfirmed = window.confirm(
      "선택한 드로잉을 삭제하시겠습니까? 팀 분석 클립과 원본 경기 영상은 삭제되지 않습니다.",
    );

    if (!isConfirmed) {
      return;
    }

    try {
      setDrawingMessage("");
      setDrawingErrorMessage("");

      await deleteTeamAnalysisClipDrawing(drawingId);

      setDrawingMessage("팀 분석 클립 드로잉이 삭제되었습니다.");
      await loadTeamAnalysisClipDrawings(selectedClip.teamClipId);
    } catch {
      setDrawingErrorMessage("팀 분석 클립 드로잉 삭제에 실패했습니다.");
    }
  };

  const handleSetDrawingRangeFromCurrentTime = () => {
    if (!selectedClip) {
      return;
    }

    const videoCurrentTimeSec = Math.floor(
      videoRef.current?.currentTime ?? selectedClip.startTimeSec,
    );

    const maxStartTimeSec = Math.max(
      selectedClip.startTimeSec,
      selectedClip.endTimeSec - 1,
    );

    const startTimeSec = Math.min(
      Math.max(selectedClip.startTimeSec, videoCurrentTimeSec),
      maxStartTimeSec,
    );

    const endTimeSec = Math.min(startTimeSec + 5, selectedClip.endTimeSec);

    setDrawingForm((prev) => ({
      ...prev,
      startTimeSec,
      endTimeSec,
    }));
  };

  useEffect(() => {
    let isMounted = true;

    getTeamAnalysisClips({
      page: 0,
      size: 20,
      matchVideoId:
        selectedMatchVideoFilter > 0 ? selectedMatchVideoFilter : undefined,
      clipType:
        selectedClipTypeFilter === "" ? undefined : selectedClipTypeFilter,
    })
      .then((response) => {
        if (!isMounted) {
          return;
        }

        setClips(
          Array.isArray(response.teamAnalysisClips)
            ? response.teamAnalysisClips
            : [],
        );
      })
      .catch(() => {
        if (!isMounted) {
          return;
        }

        setErrorMessage("팀 분석 클립 목록을 불러오지 못했습니다.");
      });

    return () => {
      isMounted = false;
    };
  }, [selectedClipTypeFilter, selectedMatchVideoFilter]);

  useEffect(() => {
    let isMounted = true;

    getMatchVideos(0, 50)
      .then((response) => {
        if (!isMounted) {
          return;
        }

        setMatchVideos(
          Array.isArray(response.matchVideos) ? response.matchVideos : [],
        );
      })
      .catch(() => {
        if (!isMounted) {
          return;
        }

        setErrorMessage("경기 영상 목록을 불러오지 못했습니다.");
      });

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    const video = videoRef.current;

    if (!video || !selectedClip) {
      return;
    }

    const moveToClipStart = () => {
      video.currentTime = selectedClip.startTimeSec;
      setCurrentTimeSec(selectedClip.startTimeSec);
    };

    video.addEventListener("loadedmetadata", moveToClipStart);

    return () => {
      video.removeEventListener("loadedmetadata", moveToClipStart);
    };
  }, [selectedClip]);

  return (
    <main className="page-container">
      <section className="page-header">
        <div>
          <h1>팀 분석 클립</h1>
          <p>
            원본 경기 영상의 특정 구간을 팀 전체 공유용 분석 클립으로
            관리합니다.
          </p>
        </div>
      </section>

      {message && <p className="success-message">{message}</p>}
      {errorMessage && <p className="error-message">{errorMessage}</p>}

      <section className="card">
        <h2>조회 조건</h2>

        <div className="form-grid">
          <label>
            경기 영상
            <select
              value={selectedMatchVideoFilter}
              onChange={(event) =>
                setSelectedMatchVideoFilter(Number(event.target.value))
              }
            >
              <option value={0}>전체 경기 영상</option>
              {matchVideos.map((matchVideo) => (
                <option
                  key={matchVideo.matchVideoId}
                  value={matchVideo.matchVideoId}
                >
                  {matchVideo.title}
                </option>
              ))}
            </select>
          </label>

          <label>
            클립 유형
            <select
              value={selectedClipTypeFilter}
              onChange={(event) =>
                setSelectedClipTypeFilter(
                  event.target.value as TeamAnalysisClipType | "",
                )
              }
            >
              <option value="">전체 유형</option>
              {TEAM_ANALYSIS_CLIP_TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
        </div>
      </section>

      {canManageTeamClip && (
        <section className="card">
          <h2>팀 분석 클립 등록</h2>

          <form onSubmit={handleCreateSubmit} className="form-grid">
            <label>
              원본 경기 영상
              <select
                value={createForm.matchVideoId}
                onChange={(event) =>
                  setCreateForm((prev) => ({
                    ...prev,
                    matchVideoId: Number(event.target.value),
                  }))
                }
              >
                <option value={0}>경기 영상을 선택하세요</option>
                {matchVideos.map((matchVideo) => (
                  <option
                    key={matchVideo.matchVideoId}
                    value={matchVideo.matchVideoId}
                  >
                    {matchVideo.title}
                  </option>
                ))}
              </select>
            </label>

            <label>
              클립 유형
              <select
                value={createForm.clipType}
                onChange={(event) =>
                  setCreateForm((prev) => ({
                    ...prev,
                    clipType: event.target.value as TeamAnalysisClipType,
                  }))
                }
              >
                {TEAM_ANALYSIS_CLIP_TYPE_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>

            <label>
              제목
              <input
                type="text"
                value={createForm.title}
                onChange={(event) =>
                  setCreateForm((prev) => ({
                    ...prev,
                    title: event.target.value,
                  }))
                }
                placeholder="예: 전방 압박 성공 장면"
              />
            </label>

            <label>
              코멘트
              <textarea
                value={createForm.comment ?? ""}
                onChange={(event) =>
                  setCreateForm((prev) => ({
                    ...prev,
                    comment: event.target.value,
                  }))
                }
                placeholder="클립에 대한 분석 코멘트를 입력하세요."
              />
            </label>

            <label>
              시작 시간(초)
              <input
                type="number"
                min={0}
                value={createForm.startTimeSec}
                onChange={(event) =>
                  setCreateForm((prev) => ({
                    ...prev,
                    startTimeSec: Number(event.target.value),
                  }))
                }
              />
            </label>

            <label>
              종료 시간(초)
              <input
                type="number"
                min={0}
                value={createForm.endTimeSec}
                onChange={(event) =>
                  setCreateForm((prev) => ({
                    ...prev,
                    endTimeSec: Number(event.target.value),
                  }))
                }
              />
            </label>

            <button type="submit">팀 분석 클립 등록</button>
          </form>
        </section>
      )}

      <section className="content-grid">
        <div className="card">
          <h2>팀 분석 클립 목록</h2>

          {clips.length === 0 ? (
            <p>등록된 팀 분석 클립이 없습니다.</p>
          ) : (
            <ul className="list">
              {clips.map((clip) => (
                <li key={clip.teamClipId}>
                  <button
                    type="button"
                    className="list-item-button"
                    onClick={() => loadTeamAnalysisClipDetail(clip.teamClipId)}
                  >
                    <strong>{clip.title}</strong>
                    <span>{clip.matchVideoTitle}</span>
                    <span>
                      {TEAM_ANALYSIS_CLIP_TYPE_LABELS[clip.clipType]} ·{" "}
                      {clip.startTimeSec}초 ~ {clip.endTimeSec}초
                    </span>
                    <span>작성자: {clip.editorName}</span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="card">
          <h2>팀 분석 클립 상세</h2>

          {!selectedClip ? (
            <p>목록에서 팀 분석 클립을 선택해주세요.</p>
          ) : (
            <div className="detail-stack">
              <div
                style={{
                  position: "relative",
                  width: "100%",
                }}
              >
                <video
                  ref={videoRef}
                  src={createVideoSourceUrl(selectedClip.matchVideoUrl)}
                  controls
                  onTimeUpdate={handleVideoTimeUpdate}
                  className="video-player"
                  style={{
                    width: "100%",
                    display: "block",
                  }}
                />

                <TeamAnalysisDrawingCanvas
                  drawings={drawings}
                  currentTimeSec={currentTimeSec}
                  isDrawingMode={isDrawingMode}
                  drawingType={drawingForm.drawingType}
                  drawingText={drawingText}
                  onDraftDrawingData={(drawingData) =>
                    setDrawingForm((prev) => ({
                      ...prev,
                      drawingData,
                    }))
                  }
                />
              </div>

              <div className="button-row">
                <button type="button" onClick={handlePlayClip}>
                  클립 구간 재생
                </button>
                <button type="button" onClick={handleReplayClip}>
                  처음부터 다시 보기
                </button>
              </div>

              <div className="detail-box">
                <p>
                  <strong>원본 경기 영상:</strong>{" "}
                  {selectedClip.matchVideoTitle}
                </p>
                <p>
                  <strong>클립 유형:</strong>{" "}
                  {TEAM_ANALYSIS_CLIP_TYPE_LABELS[selectedClip.clipType]}
                </p>
                <p>
                  <strong>제목:</strong> {selectedClip.title}
                </p>
                <p>
                  <strong>코멘트:</strong>{" "}
                  {selectedClip.comment || "등록된 코멘트가 없습니다."}
                </p>
                <p>
                  <strong>구간:</strong> {selectedClip.startTimeSec}초 ~{" "}
                  {selectedClip.endTimeSec}초
                </p>
                <p>
                  <strong>현재 재생 시간:</strong> {Math.floor(currentTimeSec)}
                  초
                </p>
                <p>
                  <strong>작성자:</strong> {selectedClip.editorName}
                </p>
              </div>

              {canManageTeamClip && (
                <div className="button-row">
                  <button
                    type="button"
                    onClick={() => setIsEditing((prev) => !prev)}
                  >
                    {isEditing ? "수정 취소" : "수정"}
                  </button>

                  {canDeleteTeamClip && (
                    <button type="button" onClick={handleDelete}>
                      삭제
                    </button>
                  )}
                </div>
              )}

              {isEditing && canManageTeamClip && (
                <form onSubmit={handleUpdateSubmit} className="form-grid">
                  <p className="notice-text">
                    원본 경기 영상은 수정할 수 없습니다. 원본 영상을 변경해야
                    한다면 기존 클립을 삭제하고 새 클립을 등록하세요.
                  </p>

                  <label>
                    원본 경기 영상
                    <input
                      type="text"
                      value={selectedClip.matchVideoTitle}
                      disabled
                    />
                  </label>

                  <label>
                    클립 유형
                    <select
                      value={updateForm.clipType}
                      onChange={(event) =>
                        setUpdateForm((prev) => ({
                          ...prev,
                          clipType: event.target.value as TeamAnalysisClipType,
                        }))
                      }
                    >
                      {TEAM_ANALYSIS_CLIP_TYPE_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label>
                    제목
                    <input
                      type="text"
                      value={updateForm.title}
                      onChange={(event) =>
                        setUpdateForm((prev) => ({
                          ...prev,
                          title: event.target.value,
                        }))
                      }
                    />
                  </label>

                  <label>
                    코멘트
                    <textarea
                      value={updateForm.comment ?? ""}
                      onChange={(event) =>
                        setUpdateForm((prev) => ({
                          ...prev,
                          comment: event.target.value,
                        }))
                      }
                    />
                  </label>

                  <label>
                    시작 시간(초)
                    <input
                      type="number"
                      min={0}
                      value={updateForm.startTimeSec}
                      onChange={(event) =>
                        setUpdateForm((prev) => ({
                          ...prev,
                          startTimeSec: Number(event.target.value),
                        }))
                      }
                    />
                  </label>

                  <label>
                    종료 시간(초)
                    <input
                      type="number"
                      min={0}
                      value={updateForm.endTimeSec}
                      onChange={(event) =>
                        setUpdateForm((prev) => ({
                          ...prev,
                          endTimeSec: Number(event.target.value),
                        }))
                      }
                    />
                  </label>

                  <button type="submit">수정 저장</button>
                </form>
              )}

              <div className="detail-box">
                <h3>팀 분석 클립 드로잉</h3>

                {drawingMessage && (
                  <p className="success-message">{drawingMessage}</p>
                )}
                {drawingErrorMessage && (
                  <p className="error-message">{drawingErrorMessage}</p>
                )}

                {canManageDrawing && (
                  <form
                    onSubmit={
                      editingDrawingId === null
                        ? handleCreateDrawingSubmit
                        : handleUpdateDrawingSubmit
                    }
                    className="form-grid"
                  >
                    <label>
                      드로잉 유형
                      <select
                        value={drawingForm.drawingType}
                        onChange={(event) => {
                          setDrawingForm((prev) => ({
                            ...prev,
                            drawingType: event.target
                              .value as TeamAnalysisClipDrawingType,
                            drawingData: {},
                          }));
                          setIsDrawingMode(false);
                        }}
                      >
                        {TEAM_ANALYSIS_CLIP_DRAWING_TYPE_OPTIONS.map(
                          (option) => (
                            <option key={option.value} value={option.value}>
                              {option.label}
                            </option>
                          ),
                        )}
                      </select>
                    </label>

                    {drawingForm.drawingType === "TEXT" && (
                      <label>
                        텍스트
                        <input
                          type="text"
                          value={drawingText}
                          onChange={(event) =>
                            setDrawingText(event.target.value)
                          }
                          placeholder="영상 위에 표시할 텍스트"
                        />
                      </label>
                    )}

                    <label>
                      드로잉 시작 시간(초)
                      <input
                        type="number"
                        min={selectedClip.startTimeSec}
                        max={selectedClip.endTimeSec}
                        value={drawingForm.startTimeSec}
                        onChange={(event) =>
                          setDrawingForm((prev) => ({
                            ...prev,
                            startTimeSec: Number(event.target.value),
                          }))
                        }
                      />
                    </label>

                    <label>
                      드로잉 종료 시간(초)
                      <input
                        type="number"
                        min={selectedClip.startTimeSec}
                        max={selectedClip.endTimeSec}
                        value={drawingForm.endTimeSec}
                        onChange={(event) =>
                          setDrawingForm((prev) => ({
                            ...prev,
                            endTimeSec: Number(event.target.value),
                          }))
                        }
                      />
                    </label>

                    <div className="button-row">
                      <button
                        type="button"
                        onClick={handleSetDrawingRangeFromCurrentTime}
                      >
                        현재 시간부터 5초 설정
                      </button>

                      <button
                        type="button"
                        onClick={() => setIsDrawingMode((prev) => !prev)}
                      >
                        {isDrawingMode ? "드로잉 종료" : "영상 위에 그리기"}
                      </button>
                    </div>

                    <label>
                      생성된 드로잉 JSON
                      <textarea
                        value={JSON.stringify(drawingForm.drawingData, null, 2)}
                        readOnly
                      />
                    </label>

                    <div className="button-row">
                      <button type="submit">
                        {editingDrawingId === null
                          ? "드로잉 등록"
                          : "드로잉 수정 저장"}
                      </button>

                      {editingDrawingId !== null && (
                        <button type="button" onClick={handleCancelDrawingEdit}>
                          드로잉 수정 취소
                        </button>
                      )}
                    </div>

                    <p className="notice-text">
                      {drawingForm.drawingType === "TEXT"
                        ? "텍스트는 영상 위 원하는 위치를 클릭하면 JSON이 생성됩니다."
                        : "선/화살표/원/박스/영역은 영상 위에서 드래그하면 JSON이 생성됩니다."}
                    </p>
                  </form>
                )}

                {drawings.length === 0 ? (
                  <p>등록된 드로잉이 없습니다.</p>
                ) : (
                  <ul className="list">
                    {drawings.map((drawing) => (
                      <li key={drawing.drawingId}>
                        <div className="detail-box">
                          <p>
                            <strong>
                              {
                                TEAM_ANALYSIS_CLIP_DRAWING_TYPE_LABELS[
                                  drawing.drawingType
                                ]
                              }
                            </strong>{" "}
                            · {drawing.startTimeSec}초 ~ {drawing.endTimeSec}초
                          </p>
                          <p>작성자: {drawing.writerName}</p>

                          {canManageDrawing && (
                            <div className="button-row">
                              <button
                                type="button"
                                onClick={() =>
                                  handleStartUpdateDrawing(drawing)
                                }
                              >
                                수정
                              </button>

                              {canDeleteDrawing && (
                                <button
                                  type="button"
                                  onClick={() =>
                                    handleDeleteDrawing(drawing.drawingId)
                                  }
                                >
                                  삭제
                                </button>
                              )}
                            </div>
                          )}
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
