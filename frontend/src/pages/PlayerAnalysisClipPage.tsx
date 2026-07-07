// 선수 개인 분석 클립 목록/상세/삭제와 편집기 이동을 제공하는 페이지
import { useContext, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import {
  deletePlayerAnalysisClip,
  getManagementPlayerAnalysisClipDetail,
  getManagementPlayerAnalysisClips,
  getManagementPlayers,
  getMyPlayerAnalysisClipDetail,
  getMyPlayerAnalysisClips,
} from "../api/playerAnalysisClipApi";
import { getMatchVideos } from "../api/matchVideoApi";
import { getPlayerAnalysisClipDrawings } from "../api/playerAnalysisClipDrawingApi";
import PlayerAnalysisDrawingCanvas from "../components/PlayerAnalysisDrawingCanvas";
import { ROUTES, createPlayerAnalysisClipEditRoute } from "../constants/routes";
import { AuthContext } from "../contexts/authContext";
import type { MatchVideoListItem } from "../types/matchVideo";
import type {
  PlayerAnalysisClipDetailResponse,
  PlayerAnalysisClipListItem,
  PlayerAnalysisClipStatus,
  PlayerAnalysisClipType,
  PlayerSelectItem,
} from "../types/playerAnalysisClip";
import {
  PLAYER_ANALYSIS_CLIP_STATUS_LABELS,
  PLAYER_ANALYSIS_CLIP_TYPE_LABELS,
  PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS,
} from "../types/playerAnalysisClip";
import type { PlayerAnalysisClipDrawingResponse } from "../types/playerAnalysisClipDrawing";
import { createVideoSourceUrl } from "../utils/videoUrl";

function getApiErrorMessage(error: unknown, fallbackMessage: string) {
  if (axios.isAxiosError<{ message?: string }>(error)) {
    const message = error.response?.data?.message;

    if (message) {
      return `${fallbackMessage} / ${message}`;
    }

    if (error.message) {
      return `${fallbackMessage} / ${error.message}`;
    }
  }

  if (error instanceof Error && error.message) {
    return `${fallbackMessage} / ${error.message}`;
  }

  return fallbackMessage;
}

function getVideoStatusLabel(status: string) {
  const statusLabels: Record<string, string> = {
    UPLOADING: "업로드 중",
    PROCESSING: "처리 중",
    READY: "재생 가능",
    FAILED: "실패",
  };

  return statusLabels[status] ?? status;
}

function getPlayerClipStatusLabel(status: PlayerAnalysisClipStatus) {
  return PLAYER_ANALYSIS_CLIP_STATUS_LABELS[status] ?? status;
}

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

function createPlayerLabel(player: PlayerSelectItem) {
  const uniformNumberText =
    player.uniformNumber === null ? "등번호 없음" : `${player.uniformNumber}번`;
  const gradeText = player.grade === null ? "학년 없음" : `${player.grade}학년`;

  return `${uniformNumberText} · ${player.name} · ${gradeText}`;
}

export default function PlayerAnalysisClipPage() {
  const authContext = useContext(AuthContext);
  const member = authContext?.member ?? null;
  const navigate = useNavigate();
  const detailVideoRef = useRef<HTMLVideoElement | null>(null);

  const [clips, setClips] = useState<PlayerAnalysisClipListItem[]>([]);
  const [matchVideos, setMatchVideos] = useState<MatchVideoListItem[]>([]);
  const [players, setPlayers] = useState<PlayerSelectItem[]>([]);
  const [selectedClip, setSelectedClip] =
    useState<PlayerAnalysisClipDetailResponse | null>(null);
  const [selectedClipDrawings, setSelectedClipDrawings] = useState<
    PlayerAnalysisClipDrawingResponse[]
  >([]);
  const [selectedClipCurrentTimeSec, setSelectedClipCurrentTimeSec] =
    useState(0);
  const [selectedMatchVideoFilter, setSelectedMatchVideoFilter] = useState(0);
  const [selectedPlayerFilter, setSelectedPlayerFilter] = useState(0);
  const [selectedClipTypeFilter, setSelectedClipTypeFilter] = useState<
    PlayerAnalysisClipType | ""
  >("");
  const [message, setMessage] = useState("");
  const [listErrorMessage, setListErrorMessage] = useState("");
  const [detailErrorMessage, setDetailErrorMessage] = useState("");
  const [videoErrorMessage, setVideoErrorMessage] = useState("");

  const canManagePlayerClip =
    member?.memberRole === "COACH" || member?.memberRole === "ANALYST";
  const canDeletePlayerClip = member?.memberRole === "COACH";

  const selectedClipSourceUrl = useMemo(() => {
    if (!selectedClip?.playerClipUrl || selectedClip.status !== "READY") {
      return "";
    }

    return createVideoSourceUrl(selectedClip.playerClipUrl);
  }, [selectedClip]);

  const fetchPlayerAnalysisClips = async () => {
    if (!member) {
      return;
    }

    if (member.memberRole === "PLAYER") {
      const response = await getMyPlayerAnalysisClips({
        page: 0,
        size: 20,
        matchVideoId:
          selectedMatchVideoFilter > 0 ? selectedMatchVideoFilter : undefined,
        clipType:
          selectedClipTypeFilter === "" ? undefined : selectedClipTypeFilter,
      });

      setClips(Array.isArray(response.playerClips) ? response.playerClips : []);
      return;
    }

    if (member.memberRole === "COACH" || member.memberRole === "ANALYST") {
      const response = await getManagementPlayerAnalysisClips({
        page: 0,
        size: 20,
        matchVideoId:
          selectedMatchVideoFilter > 0 ? selectedMatchVideoFilter : undefined,
        playerId: selectedPlayerFilter > 0 ? selectedPlayerFilter : undefined,
        clipType:
          selectedClipTypeFilter === "" ? undefined : selectedClipTypeFilter,
      });

      setClips(Array.isArray(response.playerClips) ? response.playerClips : []);
    }
  };

  const reloadPlayerAnalysisClips = async () => {
    try {
      await fetchPlayerAnalysisClips();
      setListErrorMessage("");
    } catch (error) {
      setListErrorMessage(
        getApiErrorMessage(
          error,
          "선수 개인 분석 클립 목록을 불러오지 못했습니다.",
        ),
      );
    }
  };

  const loadPlayerAnalysisClipDetail = async (playerClipId: number) => {
    if (!member) {
      setDetailErrorMessage("로그인 사용자 정보를 확인할 수 없습니다.");
      return;
    }

    try {
      setMessage("");
      setDetailErrorMessage("");
      setVideoErrorMessage("");
      setSelectedClipDrawings([]);
      setSelectedClipCurrentTimeSec(0);

      const detail =
        member.memberRole === "PLAYER"
          ? await getMyPlayerAnalysisClipDetail(playerClipId)
          : await getManagementPlayerAnalysisClipDetail(playerClipId);

      setSelectedClip(detail);

      if (detail.status !== "READY") {
        return;
      }

      const drawings = await getPlayerAnalysisClipDrawings(playerClipId);
      setSelectedClipDrawings(Array.isArray(drawings) ? drawings : []);
    } catch (error) {
      setSelectedClip(null);
      setSelectedClipDrawings([]);
      setVideoErrorMessage("");
      setDetailErrorMessage(
        getApiErrorMessage(
          error,
          "선수 개인 분석 클립 상세 정보를 불러오지 못했습니다.",
        ),
      );
    }
  };

  const handleDelete = async () => {
    if (!selectedClip) {
      return;
    }

    const isConfirmed = window.confirm(
      "선택한 선수 개인 분석 클립을 삭제하시겠습니까?",
    );

    if (!isConfirmed) {
      return;
    }

    try {
      setMessage("");
      setDetailErrorMessage("");
      setVideoErrorMessage("");

      await deletePlayerAnalysisClip(selectedClip.playerClipId);

      setSelectedClip(null);
      setSelectedClipDrawings([]);
      setSelectedClipCurrentTimeSec(0);
      setMessage("선수 개인 분석 클립이 삭제되었습니다.");

      await reloadPlayerAnalysisClips();
    } catch (error) {
      setDetailErrorMessage(
        getApiErrorMessage(error, "선수 개인 분석 클립 삭제에 실패했습니다."),
      );
    }
  };

  const handleDetailVideoTimeUpdate = () => {
    const video = detailVideoRef.current;

    if (!video) {
      return;
    }

    setSelectedClipCurrentTimeSec(video.currentTime);
  };

  const handleDetailVideoLoadedMetadata = () => {
    const video = detailVideoRef.current;

    if (!video) {
      return;
    }

    setSelectedClipCurrentTimeSec(video.currentTime);
    setVideoErrorMessage("");
  };

  const handleDetailVideoError = () => {
    setVideoErrorMessage(
      "클립 영상을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.",
    );
  };

  const handleReplayClip = () => {
    const video = detailVideoRef.current;

    if (!video) {
      return;
    }

    video.currentTime = 0;
    setSelectedClipCurrentTimeSec(0);
    void video.play();
  };

  useEffect(() => {
    if (!member?.memberRole) {
      return;
    }

    let ignore = false;
    const role = member.memberRole;

    async function fetchClips() {
      try {
        if (role === "PLAYER") {
          const response = await getMyPlayerAnalysisClips({
            page: 0,
            size: 20,
            matchVideoId:
              selectedMatchVideoFilter > 0
                ? selectedMatchVideoFilter
                : undefined,
            clipType:
              selectedClipTypeFilter === ""
                ? undefined
                : selectedClipTypeFilter,
          });

          if (!ignore) {
            setClips(
              Array.isArray(response.playerClips) ? response.playerClips : [],
            );
            setListErrorMessage("");
          }

          return;
        }

        const response = await getManagementPlayerAnalysisClips({
          page: 0,
          size: 20,
          matchVideoId:
            selectedMatchVideoFilter > 0 ? selectedMatchVideoFilter : undefined,
          playerId: selectedPlayerFilter > 0 ? selectedPlayerFilter : undefined,
          clipType:
            selectedClipTypeFilter === "" ? undefined : selectedClipTypeFilter,
        });

        if (!ignore) {
          setClips(
            Array.isArray(response.playerClips) ? response.playerClips : [],
          );
          setListErrorMessage("");
        }
      } catch (error) {
        if (!ignore) {
          setListErrorMessage(
            getApiErrorMessage(
              error,
              "선수 개인 분석 클립 목록을 불러오지 못했습니다.",
            ),
          );
        }
      }
    }

    void fetchClips();

    return () => {
      ignore = true;
    };
  }, [
    member?.memberRole,
    selectedMatchVideoFilter,
    selectedPlayerFilter,
    selectedClipTypeFilter,
  ]);

  useEffect(() => {
    let ignore = false;

    async function fetchMatchVideos() {
      try {
        const response = await getMatchVideos(0, 50);

        if (!ignore) {
          setMatchVideos(
            Array.isArray(response.matchVideos) ? response.matchVideos : [],
          );
        }
      } catch (error) {
        if (!ignore) {
          setListErrorMessage(
            getApiErrorMessage(error, "경기 영상 목록을 불러오지 못했습니다."),
          );
        }
      }
    }

    void fetchMatchVideos();

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    if (!canManagePlayerClip) {
      return;
    }

    let ignore = false;

    async function fetchPlayers() {
      try {
        const response = await getManagementPlayers();

        if (!ignore) {
          setPlayers(Array.isArray(response) ? response : []);
        }
      } catch (error) {
        if (!ignore) {
          setListErrorMessage(
            getApiErrorMessage(error, "선수 목록을 불러오지 못했습니다."),
          );
        }
      }
    }

    void fetchPlayers();

    return () => {
      ignore = true;
    };
  }, [canManagePlayerClip]);

  return (
    <main className="page-container">
      <h1>선수 개인 분석 클립</h1>

      {message && <p className="success-message">{message}</p>}
      {listErrorMessage && <p className="error-message">{listErrorMessage}</p>}
      {detailErrorMessage && (
        <p className="error-message">{detailErrorMessage}</p>
      )}

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
                  {matchVideo.title} · {getVideoStatusLabel(matchVideo.status)}
                </option>
              ))}
            </select>
          </label>

          {canManagePlayerClip && (
            <label>
              대상 선수
              <select
                value={selectedPlayerFilter}
                onChange={(event) =>
                  setSelectedPlayerFilter(Number(event.target.value))
                }
              >
                <option value={0}>전체 선수</option>
                {players.map((player) => (
                  <option key={player.playerId} value={player.playerId}>
                    {createPlayerLabel(player)}
                  </option>
                ))}
              </select>
            </label>
          )}

          <label>
            클립 유형
            <select
              value={selectedClipTypeFilter}
              onChange={(event) =>
                setSelectedClipTypeFilter(
                  event.target.value as PlayerAnalysisClipType | "",
                )
              }
            >
              <option value="">전체 유형</option>
              {PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
        </div>

        {canManagePlayerClip && (
          <div className="button-row">
            <button
              type="button"
              onClick={() => navigate(ROUTES.PLAYER_ANALYSIS_CLIP_CREATE)}
            >
              선수 개인 분석 클립 등록
            </button>
          </div>
        )}
      </section>

      <section className="content-grid">
        <div className="card">
          <h2>선수 개인 분석 클립 목록</h2>

          {clips.length === 0 ? (
            <p>조회된 선수 개인 분석 클립이 없습니다.</p>
          ) : (
            <ul className="list">
              {clips.map((clip) => (
                <li key={clip.playerClipId}>
                  <button
                    type="button"
                    className="list-item-button"
                    onClick={() => {
                      void loadPlayerAnalysisClipDetail(clip.playerClipId);
                    }}
                  >
                    <strong>{clip.title}</strong>
                    <span>{clip.matchVideoTitle}</span>
                    <span>
                      {PLAYER_ANALYSIS_CLIP_TYPE_LABELS[clip.clipType]} ·{" "}
                      {getPlayerClipStatusLabel(clip.status)}
                    </span>
                    <span>대상 선수: {clip.playerName}</span>
                    <span>작성자: {clip.editorName}</span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="card">
          <h2>선수 개인 분석 클립 상세</h2>

          {!selectedClip ? (
            <p>목록에서 선수 개인 분석 클립을 선택해주세요.</p>
          ) : (
            <div className="detail-stack">
              {(selectedClip.status === "PROCESSING" ||
                selectedClip.status === "UPLOADING") && (
                <p>클립 파일을 생성 중입니다.</p>
              )}

              {selectedClip.status === "FAILED" && (
                <p>클립 파일 생성에 실패했습니다.</p>
              )}

              {selectedClip.status === "READY" && selectedClipSourceUrl && (
                <>
                  <div style={{ position: "relative", width: "100%" }}>
                    <video
                      key={`${selectedClip.playerClipId}-${selectedClip.playerClipUrl}`}
                      ref={detailVideoRef}
                      src={selectedClipSourceUrl}
                      controls
                      preload="metadata"
                      onLoadedMetadata={handleDetailVideoLoadedMetadata}
                      onTimeUpdate={handleDetailVideoTimeUpdate}
                      onError={handleDetailVideoError}
                      className="video-player"
                      style={{ display: "block", width: "100%" }}
                    />

                    <PlayerAnalysisDrawingCanvas
                      drawings={selectedClipDrawings}
                      currentTimeSec={selectedClipCurrentTimeSec}
                      isDrawingMode={false}
                      drawingType="ARROW"
                      drawingText=""
                      onDraftDrawingData={() => undefined}
                    />
                  </div>

                  {videoErrorMessage && (
                    <p className="error-message">{videoErrorMessage}</p>
                  )}

                  <div className="button-row">
                    <button type="button" onClick={handleReplayClip}>
                      처음부터 보기
                    </button>
                  </div>
                </>
              )}

              <div className="detail-box">
                <p>
                  <strong>원본 경기 영상:</strong>{" "}
                  {selectedClip.matchVideoTitle}
                </p>
                <p>
                  <strong>대상 선수:</strong> {selectedClip.playerName}
                </p>
                <p>
                  <strong>클립 유형:</strong>{" "}
                  {PLAYER_ANALYSIS_CLIP_TYPE_LABELS[selectedClip.clipType]}
                </p>
                <p>
                  <strong>상태:</strong>{" "}
                  {getPlayerClipStatusLabel(selectedClip.status)}
                </p>
                <p>
                  <strong>제목:</strong> {selectedClip.title}
                </p>
                <p>
                  <strong>코멘트:</strong>{" "}
                  {selectedClip.comment || "등록된 코멘트가 없습니다."}
                </p>
                <p>
                  <strong>원본 기준 구간:</strong>{" "}
                  {formatSeconds(selectedClip.startTimeSec)} ~{" "}
                  {formatSeconds(selectedClip.endTimeSec)}
                </p>
                <p>
                  <strong>작성자:</strong> {selectedClip.editorName}
                </p>
              </div>

              {canManagePlayerClip && (
                <div className="button-row">
                  <button
                    type="button"
                    onClick={() =>
                      navigate(
                        createPlayerAnalysisClipEditRoute(
                          selectedClip.playerClipId,
                        ),
                      )
                    }
                  >
                    수정
                  </button>

                  {canDeletePlayerClip && (
                    <button type="button" onClick={handleDelete}>
                      삭제
                    </button>
                  )}
                </div>
              )}
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
