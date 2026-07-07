// 팀 분석 클립 목록, 상세 조회, 삭제, 등록/수정 페이지 이동을 제공하는 페이지
import { useContext, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

import { getMatchVideos } from "../api/matchVideoApi";
import {
  deleteTeamAnalysisClip,
  getTeamAnalysisClipDetail,
  getTeamAnalysisClips,
} from "../api/teamAnalysisClipApi";
import { getTeamAnalysisClipDrawings } from "../api/teamAnalysisClipDrawingApi";
import TeamAnalysisDrawingCanvas from "../components/TeamAnalysisDrawingCanvas";
import { ROUTES, createTeamAnalysisClipEditRoute } from "../constants/routes";
import { AuthContext } from "../contexts/authContext";
import type { MatchVideoListItem } from "../types/matchVideo";
import type {
  TeamAnalysisClipDetailResponse,
  TeamAnalysisClipListItem,
  TeamAnalysisClipType,
} from "../types/teamAnalysisClip";
import {
  TEAM_ANALYSIS_CLIP_TYPE_LABELS,
  TEAM_ANALYSIS_CLIP_TYPE_OPTIONS,
} from "../types/teamAnalysisClip";
import type { TeamAnalysisClipDrawingResponse } from "../types/teamAnalysisClipDrawing";
import { createVideoSourceUrl } from "../utils/videoUrl";

function formatSeconds(totalSeconds: number | null | undefined) {
  if (
    totalSeconds === null ||
    totalSeconds === undefined ||
    !Number.isFinite(totalSeconds)
  ) {
    return "확인 불가";
  }

  const safeSeconds = Math.max(0, Math.floor(totalSeconds));
  const minutes = Math.floor(safeSeconds / 60);
  const seconds = safeSeconds % 60;

  return `${safeSeconds}초 (${minutes}:${String(seconds).padStart(2, "0")})`;
}

function getNumberField(source: unknown, fieldNames: string[]) {
  if (source === null || typeof source !== "object") {
    return null;
  }

  const record = source as Record<string, unknown>;

  for (const fieldName of fieldNames) {
    const value = record[fieldName];

    if (typeof value === "number" && Number.isFinite(value)) {
      return value;
    }

    if (typeof value === "string" && value.trim() !== "") {
      const parsedValue = Number(value);

      if (Number.isFinite(parsedValue)) {
        return parsedValue;
      }
    }
  }

  return null;
}

function getStringField(source: unknown, fieldNames: string[]) {
  if (source === null || typeof source !== "object") {
    return "";
  }

  const record = source as Record<string, unknown>;

  for (const fieldName of fieldNames) {
    const value = record[fieldName];

    if (typeof value === "string" && value.trim() !== "") {
      return value;
    }
  }

  return "";
}

function getClipStartTimeSec(clip: TeamAnalysisClipDetailResponse | null) {
  return getNumberField(clip, [
    "startTimeSec",
    "startTime",
    "clipStartTimeSec",
  ]);
}

function getClipEndTimeSec(clip: TeamAnalysisClipDetailResponse | null) {
  return getNumberField(clip, ["endTimeSec", "endTime", "clipEndTimeSec"]);
}

function getClipVideoUrl(clip: TeamAnalysisClipDetailResponse | null) {
  return getStringField(clip, ["teamClipUrl", "clipUrl", "url"]);
}

function getClipStatusMessage(status: string) {
  if (status === "PROCESSING") {
    return "팀 분석 클립 파일을 생성 중입니다. 잠시 후 다시 확인해주세요.";
  }

  if (status === "FAILED") {
    return "팀 분석 클립 파일 생성에 실패했습니다. 수정 페이지에서 다시 저장해 재생성을 요청할 수 있습니다.";
  }

  if (status === "UPLOADING") {
    return "팀 분석 클립 파일 생성 준비 중입니다.";
  }

  return "";
}

export default function TeamAnalysisClipPage() {
  const navigate = useNavigate();
  const authContext = useContext(AuthContext);
  const member = authContext?.member ?? null;

  const videoRef = useRef<HTMLVideoElement | null>(null);

  const [clips, setClips] = useState<TeamAnalysisClipListItem[]>([]);
  const [matchVideos, setMatchVideos] = useState<MatchVideoListItem[]>([]);
  const [selectedClip, setSelectedClip] =
    useState<TeamAnalysisClipDetailResponse | null>(null);
  const [drawings, setDrawings] = useState<TeamAnalysisClipDrawingResponse[]>(
    [],
  );

  const [selectedClipTypeFilter, setSelectedClipTypeFilter] = useState<
    TeamAnalysisClipType | ""
  >("");
  const [selectedMatchVideoFilter, setSelectedMatchVideoFilter] = useState(0);

  const [currentTimeSec, setCurrentTimeSec] = useState(0);
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoadingDetail, setIsLoadingDetail] = useState(false);

  const canCreateOrUpdateTeamClip =
    member?.memberRole === "COACH" || member?.memberRole === "ANALYST";
  const canDeleteTeamClip = member?.memberRole === "COACH";

  const selectedClipStartTimeSec = getClipStartTimeSec(selectedClip);
  const selectedClipEndTimeSec = getClipEndTimeSec(selectedClip);
  const selectedClipVideoUrl = getClipVideoUrl(selectedClip);

  const selectedClipDurationSec =
    selectedClipStartTimeSec !== null && selectedClipEndTimeSec !== null
      ? Math.max(0, selectedClipEndTimeSec - selectedClipStartTimeSec)
      : null;

  const canPlaySelectedClip =
    selectedClip?.status === "READY" && selectedClipVideoUrl !== "";

  const handleMoveToCreatePage = () => {
    navigate(ROUTES.TEAM_ANALYSIS_CLIP_CREATE);
  };

  const handleMoveToEditPage = () => {
    if (!selectedClip) {
      return;
    }

    navigate(createTeamAnalysisClipEditRoute(selectedClip.teamClipId));
  };

  const handleLoadTeamAnalysisClipDetail = async (teamClipId: number) => {
    try {
      setErrorMessage("");
      setIsLoadingDetail(true);

      const [clipDetail, clipDrawings] = await Promise.all([
        getTeamAnalysisClipDetail(teamClipId),
        getTeamAnalysisClipDrawings(teamClipId),
      ]);

      setSelectedClip(clipDetail);
      setDrawings(Array.isArray(clipDrawings) ? clipDrawings : []);
      setCurrentTimeSec(0);
    } catch {
      setErrorMessage("팀 분석 클립 상세 정보를 불러오지 못했습니다.");
    } finally {
      setIsLoadingDetail(false);
    }
  };

  const handleDeleteSelectedClip = async () => {
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

      await deleteTeamAnalysisClip(selectedClip.teamClipId);

      setClips((prev) =>
        prev.filter((clip) => clip.teamClipId !== selectedClip.teamClipId),
      );
      setSelectedClip(null);
      setDrawings([]);
      setCurrentTimeSec(0);

      alert("팀 분석 클립이 삭제되었습니다.");
    } catch {
      alert("팀 분석 클립 삭제에 실패했습니다.");
    }
  };

  const handleReplayClip = () => {
    const video = videoRef.current;

    if (!video || !canPlaySelectedClip) {
      return;
    }

    video.currentTime = 0;
    setCurrentTimeSec(0);
    void video.play();
  };

  const handleVideoLoadedMetadata = () => {
    const video = videoRef.current;

    if (!video) {
      return;
    }

    video.currentTime = 0;
    setCurrentTimeSec(0);
  };

  const handleVideoTimeUpdate = () => {
    const video = videoRef.current;

    if (!video) {
      return;
    }

    const nextCurrentTimeSec = Math.max(0, video.currentTime);
    setCurrentTimeSec(nextCurrentTimeSec);

    if (
      selectedClipDurationSec !== null &&
      selectedClipDurationSec > 0 &&
      nextCurrentTimeSec >= selectedClipDurationSec
    ) {
      video.pause();
    }
  };

  useEffect(() => {
    let ignore = false;

    async function fetchMatchVideos() {
      try {
        const response = await getMatchVideos(0, 100);

        if (!ignore) {
          setMatchVideos(
            Array.isArray(response.matchVideos) ? response.matchVideos : [],
          );
        }
      } catch {
        if (!ignore) {
          setErrorMessage("경기 영상 목록을 불러오지 못했습니다.");
        }
      }
    }

    fetchMatchVideos();

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    let ignore = false;

    async function fetchTeamAnalysisClips() {
      try {
        const response = await getTeamAnalysisClips({
          page: 0,
          size: 100,
          matchVideoId:
            selectedMatchVideoFilter > 0 ? selectedMatchVideoFilter : undefined,
          clipType:
            selectedClipTypeFilter === "" ? undefined : selectedClipTypeFilter,
        });

        if (!ignore) {
          setErrorMessage("");
          setClips(
            Array.isArray(response.teamAnalysisClips)
              ? response.teamAnalysisClips
              : [],
          );
        }
      } catch {
        if (!ignore) {
          setClips([]);
          setErrorMessage("팀 분석 클립 목록을 불러오지 못했습니다.");
        }
      }
    }

    fetchTeamAnalysisClips();

    return () => {
      ignore = true;
    };
  }, [selectedMatchVideoFilter, selectedClipTypeFilter]);

  return (
    <main className="page">
      <section className="page-header">
        <div>
          <h1>팀 분석 클립</h1>
          <p>생성된 팀 분석 클립을 조회하고 재생합니다.</p>
        </div>

        {canCreateOrUpdateTeamClip && (
          <div className="button-row">
            <button type="button" onClick={handleMoveToCreatePage}>
              팀 분석 클립 등록
            </button>
          </div>
        )}
      </section>

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

      <section className="content-grid">
        <section className="card">
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
                    onClick={() =>
                      handleLoadTeamAnalysisClipDetail(clip.teamClipId)
                    }
                  >
                    <strong>{clip.title}</strong>
                    <span>{clip.matchVideoTitle}</span>
                    <span>
                      {TEAM_ANALYSIS_CLIP_TYPE_LABELS[clip.clipType]}
                      {clip.status !== "READY" ? ` · ${clip.status}` : ""}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="card">
          <h2>팀 분석 클립 상세</h2>

          {isLoadingDetail && (
            <p>팀 분석 클립 상세 정보를 불러오는 중입니다.</p>
          )}

          {!isLoadingDetail && !selectedClip && (
            <p>목록에서 팀 분석 클립을 선택해주세요.</p>
          )}

          {!isLoadingDetail && selectedClip && (
            <div className="detail-stack">
              {canPlaySelectedClip ? (
                <div
                  style={{
                    position: "relative",
                    width: "100%",
                  }}
                >
                  <video
                    ref={videoRef}
                    src={createVideoSourceUrl(selectedClipVideoUrl)}
                    controls
                    onLoadedMetadata={handleVideoLoadedMetadata}
                    onTimeUpdate={handleVideoTimeUpdate}
                    onSeeked={handleVideoTimeUpdate}
                    className="video-player"
                    style={{
                      width: "100%",
                      display: "block",
                    }}
                  />

                  <TeamAnalysisDrawingCanvas
                    drawings={drawings}
                    currentTimeSec={currentTimeSec}
                    isDrawingMode={false}
                    drawingType="ARROW"
                    drawingText=""
                    onDraftDrawingData={() => undefined}
                  />
                </div>
              ) : (
                <div className="detail-box">
                  <p>{getClipStatusMessage(selectedClip.status)}</p>
                </div>
              )}

              {canPlaySelectedClip && (
                <div className="button-row">
                  <button type="button" onClick={handleReplayClip}>
                    처음부터 다시 보기
                  </button>
                </div>
              )}

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
                  <strong>원본 영상 기준 구간:</strong>{" "}
                  {selectedClipStartTimeSec !== null
                    ? `${selectedClipStartTimeSec}초`
                    : "확인 불가"}{" "}
                  ~{" "}
                  {selectedClipEndTimeSec !== null
                    ? `${selectedClipEndTimeSec}초`
                    : "확인 불가"}
                </p>
                <p>
                  <strong>생성된 클립 길이:</strong>{" "}
                  {formatSeconds(selectedClipDurationSec)}
                </p>
                <p>
                  <strong>현재 재생 시간:</strong>{" "}
                  {formatSeconds(currentTimeSec)}
                </p>
              </div>

              {canCreateOrUpdateTeamClip && (
                <div className="button-row">
                  <button type="button" onClick={handleMoveToEditPage}>
                    수정
                  </button>

                  {canDeleteTeamClip && (
                    <button type="button" onClick={handleDeleteSelectedClip}>
                      삭제
                    </button>
                  )}
                </div>
              )}
            </div>
          )}
        </section>
      </section>
    </main>
  );
}
