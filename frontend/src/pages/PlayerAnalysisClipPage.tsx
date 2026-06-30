// 선수 개인 분석 클립 목록, 상세, 등록, 수정, 삭제 화면을 제공하는 페이지

import { useContext, useEffect, useRef, useState } from "react";
import axios from "axios";
import {
  createPlayerAnalysisClip,
  deletePlayerAnalysisClip,
  getManagementPlayerAnalysisClipDetail,
  getManagementPlayerAnalysisClips,
  getManagementPlayers,
  getMyPlayerAnalysisClipDetail,
  getMyPlayerAnalysisClips,
  updatePlayerAnalysisClip,
} from "../api/playerAnalysisClipApi";
import { getMatchVideos } from "../api/matchVideoApi";
import { AuthContext } from "../contexts/authContext";
import type { MatchVideoListItem } from "../types/matchVideo";
import type {
  CreatePlayerAnalysisClipRequest,
  PlayerAnalysisClipDetailResponse,
  PlayerAnalysisClipListItem,
  PlayerAnalysisClipType,
  PlayerSelectItem,
  UpdatePlayerAnalysisClipRequest,
} from "../types/playerAnalysisClip";
import {
  PLAYER_ANALYSIS_CLIP_TYPE_LABELS,
  PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS,
} from "../types/playerAnalysisClip";
import { createVideoSourceUrl } from "../utils/videoUrl";

const initialCreateForm: CreatePlayerAnalysisClipRequest = {
  matchVideoId: 0,
  playerId: 0,
  clipType: "PLAYER_GOOD",
  title: "",
  comment: "",
  startTimeSec: 0,
  endTimeSec: 0,
};

const initialUpdateForm: UpdatePlayerAnalysisClipRequest = {
  matchVideoId: 0,
  playerId: 0,
  clipType: "PLAYER_GOOD",
  title: "",
  comment: "",
  startTimeSec: 0,
  endTimeSec: 0,
};

type ApiErrorResponse = {
  timestamp?: string;
  status?: number;
  code?: string;
  message?: string;
  path?: string;
};

function getApiErrorMessage(error: unknown, fallbackMessage: string) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const status = error.response?.status;
    const code = error.response?.data?.code;
    const message = error.response?.data?.message;
    const path = error.response?.data?.path;

    if (status || code || message || path) {
      return [
        fallbackMessage,
        status ? `상태: ${status}` : null,
        code ? `코드: ${code}` : null,
        message ? `메시지: ${message}` : null,
        path ? `경로: ${path}` : null,
      ]
        .filter(Boolean)
        .join(" / ");
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

export default function PlayerAnalysisClipPage() {
  const authContext = useContext(AuthContext);
  const member = authContext?.member ?? null;

  const videoRef = useRef<HTMLVideoElement | null>(null);

  const [clips, setClips] = useState<PlayerAnalysisClipListItem[]>([]);
  const [matchVideos, setMatchVideos] = useState<MatchVideoListItem[]>([]);
  const [players, setPlayers] = useState<PlayerSelectItem[]>([]);
  const [selectedClip, setSelectedClip] =
    useState<PlayerAnalysisClipDetailResponse | null>(null);

  const [createForm, setCreateForm] =
    useState<CreatePlayerAnalysisClipRequest>(initialCreateForm);

  const [updateForm, setUpdateForm] =
    useState<UpdatePlayerAnalysisClipRequest>(initialUpdateForm);

  const [selectedClipTypeFilter, setSelectedClipTypeFilter] = useState<
    PlayerAnalysisClipType | ""
  >("");

  const [selectedMatchVideoFilter, setSelectedMatchVideoFilter] =
    useState<number>(0);

  const [selectedPlayerFilter, setSelectedPlayerFilter] = useState<number>(0);

  const [currentTimeSec, setCurrentTimeSec] = useState(0);
  const [isEditing, setIsEditing] = useState(false);
  const [message, setMessage] = useState("");
  const [listErrorMessage, setListErrorMessage] = useState("");
  const [detailErrorMessage, setDetailErrorMessage] = useState("");
  const [formErrorMessage, setFormErrorMessage] = useState("");

  const canManagePlayerClip =
    member?.memberRole === "COACH" || member?.memberRole === "ANALYST";

  const canDeletePlayerClip = member?.memberRole === "COACH";

  const createPlayerLabel = (player: PlayerSelectItem) => {
    const uniformNumberText =
      player.uniformNumber === null
        ? "등번호 없음"
        : `${player.uniformNumber}번`;

    const gradeText =
      player.grade === null ? "학년 없음" : `${player.grade}학년`;

    return `${uniformNumberText} · ${player.name} · ${gradeText}`;
  };

  const validateClipForm = (
    form: CreatePlayerAnalysisClipRequest | UpdatePlayerAnalysisClipRequest,
  ) => {
    if (form.matchVideoId <= 0) {
      setFormErrorMessage("원본 경기 영상을 선택해주세요.");
      return false;
    }

    if (form.playerId <= 0) {
      setFormErrorMessage("대상 선수를 선택해주세요.");
      return false;
    }

    if (form.title.trim() === "") {
      setFormErrorMessage("클립 제목을 입력해주세요.");
      return false;
    }

    if (form.startTimeSec < 0 || form.endTimeSec < 0) {
      setFormErrorMessage("시작 시간과 종료 시간은 0 이상이어야 합니다.");
      return false;
    }

    if (form.startTimeSec >= form.endTimeSec) {
      setFormErrorMessage("시작 시간은 종료 시간보다 작아야 합니다.");
      return false;
    }

    const selectedMatchVideo = matchVideos.find(
      (matchVideo) => matchVideo.matchVideoId === form.matchVideoId,
    );

    if (
      selectedMatchVideo?.durationSec !== null &&
      selectedMatchVideo?.durationSec !== undefined &&
      form.endTimeSec > selectedMatchVideo.durationSec
    ) {
      setFormErrorMessage("종료 시간이 원본 경기 영상 길이를 초과했습니다.");
      return false;
    }

    setFormErrorMessage("");
    return true;
  };

  const fetchPlayerAnalysisClipsForEffect = async (
    loginMemberRole: string,
    matchVideoFilter: number,
    playerFilter: number,
    clipTypeFilter: PlayerAnalysisClipType | "",
  ) => {
    if (loginMemberRole === "PLAYER") {
      const response = await getMyPlayerAnalysisClips({
        page: 0,
        size: 20,
        matchVideoId: matchVideoFilter > 0 ? matchVideoFilter : undefined,
        clipType: clipTypeFilter === "" ? undefined : clipTypeFilter,
      });

      return Array.isArray(response.playerClips) ? response.playerClips : [];
    }

    if (loginMemberRole === "COACH" || loginMemberRole === "ANALYST") {
      const response = await getManagementPlayerAnalysisClips({
        page: 0,
        size: 20,
        matchVideoId: matchVideoFilter > 0 ? matchVideoFilter : undefined,
        playerId: playerFilter > 0 ? playerFilter : undefined,
        clipType: clipTypeFilter === "" ? undefined : clipTypeFilter,
      });

      return Array.isArray(response.playerClips) ? response.playerClips : [];
    }

    return [];
  };

  const reloadPlayerAnalysisClips = async () => {
    if (!member) {
      return;
    }

    try {
      const nextClips = await fetchPlayerAnalysisClipsForEffect(
        member.memberRole,
        selectedMatchVideoFilter,
        selectedPlayerFilter,
        selectedClipTypeFilter,
      );

      setClips(nextClips);
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
      setDetailErrorMessage("");
      setFormErrorMessage("");
      setMessage("");

      const detail =
        member.memberRole === "PLAYER"
          ? await getMyPlayerAnalysisClipDetail(playerClipId)
          : await getManagementPlayerAnalysisClipDetail(playerClipId);

      setSelectedClip(detail);
      setIsEditing(false);
      setCurrentTimeSec(detail.startTimeSec);

      setUpdateForm({
        matchVideoId: detail.matchVideoId,
        playerId: detail.playerId,
        clipType: detail.clipType,
        title: detail.title,
        comment: detail.comment ?? "",
        startTimeSec: detail.startTimeSec,
        endTimeSec: detail.endTimeSec,
      });
    } catch (error) {
      setSelectedClip(null);
      setIsEditing(false);
      setDetailErrorMessage(
        getApiErrorMessage(
          error,
          "선수 개인 분석 클립 상세 정보를 불러오지 못했습니다.",
        ),
      );
    }
  };

  const handleCreateSubmit = async (event: { preventDefault: () => void }) => {
    event.preventDefault();

    if (!validateClipForm(createForm)) {
      return;
    }

    try {
      setFormErrorMessage("");
      setDetailErrorMessage("");
      setMessage("");

      const request: CreatePlayerAnalysisClipRequest = {
        matchVideoId: createForm.matchVideoId,
        playerId: createForm.playerId,
        clipType: createForm.clipType,
        title: createForm.title.trim(),
        comment:
          createForm.comment === null || createForm.comment.trim() === ""
            ? null
            : createForm.comment.trim(),
        startTimeSec: createForm.startTimeSec,
        endTimeSec: createForm.endTimeSec,
      };

      const response = await createPlayerAnalysisClip(request);

      setMessage(response.message);
      setCreateForm(initialCreateForm);
      await reloadPlayerAnalysisClips();
    } catch (error) {
      setFormErrorMessage(
        getApiErrorMessage(error, "선수 개인 분석 클립 등록에 실패했습니다."),
      );
    }
  };

  const handleUpdateSubmit = async (event: { preventDefault: () => void }) => {
    event.preventDefault();

    if (!selectedClip) {
      setFormErrorMessage("수정할 선수 개인 분석 클립을 선택해주세요.");
      return;
    }

    if (!validateClipForm(updateForm)) {
      return;
    }

    try {
      setFormErrorMessage("");
      setDetailErrorMessage("");
      setMessage("");

      const request: UpdatePlayerAnalysisClipRequest = {
        matchVideoId: updateForm.matchVideoId,
        playerId: updateForm.playerId,
        clipType: updateForm.clipType,
        title: updateForm.title.trim(),
        comment:
          updateForm.comment === null || updateForm.comment.trim() === ""
            ? null
            : updateForm.comment.trim(),
        startTimeSec: updateForm.startTimeSec,
        endTimeSec: updateForm.endTimeSec,
      };

      const updatedClip = await updatePlayerAnalysisClip(
        selectedClip.playerClipId,
        request,
      );

      setSelectedClip(updatedClip);
      setIsEditing(false);
      setCurrentTimeSec(updatedClip.startTimeSec);
      setMessage("선수 개인 분석 클립이 수정되었습니다.");

      await reloadPlayerAnalysisClips();
    } catch (error) {
      setFormErrorMessage(
        getApiErrorMessage(error, "선수 개인 분석 클립 수정에 실패했습니다."),
      );
    }
  };

  const handleDelete = async () => {
    if (!selectedClip) {
      return;
    }

    const isConfirmed = window.confirm(
      "선택한 선수 개인 분석 클립을 삭제하시겠습니까? 원본 경기 영상은 삭제되지 않습니다.",
    );

    if (!isConfirmed) {
      return;
    }

    try {
      setFormErrorMessage("");
      setDetailErrorMessage("");
      setMessage("");

      await deletePlayerAnalysisClip(selectedClip.playerClipId);

      setSelectedClip(null);
      setIsEditing(false);
      setCurrentTimeSec(0);
      setUpdateForm(initialUpdateForm);
      setMessage("선수 개인 분석 클립이 삭제되었습니다.");

      await reloadPlayerAnalysisClips();
    } catch (error) {
      setFormErrorMessage(
        getApiErrorMessage(error, "선수 개인 분석 클립 삭제에 실패했습니다."),
      );
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

  useEffect(() => {
    if (!member?.memberRole) {
      return;
    }

    let ignore = false;

    const fetchClips = async () => {
      try {
        const nextClips = await fetchPlayerAnalysisClipsForEffect(
          member.memberRole,
          selectedMatchVideoFilter,
          selectedPlayerFilter,
          selectedClipTypeFilter,
        );

        if (ignore) {
          return;
        }

        setClips(nextClips);
        setListErrorMessage("");
      } catch (error) {
        if (ignore) {
          return;
        }

        setListErrorMessage(
          getApiErrorMessage(
            error,
            "선수 개인 분석 클립 목록을 불러오지 못했습니다.",
          ),
        );
      }
    };

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

    const fetchMatchVideos = async () => {
      try {
        const response = await getMatchVideos(0, 50);

        if (ignore) {
          return;
        }

        setMatchVideos(
          Array.isArray(response.matchVideos) ? response.matchVideos : [],
        );
      } catch (error) {
        if (ignore) {
          return;
        }

        setListErrorMessage(
          getApiErrorMessage(error, "경기 영상 목록을 불러오지 못했습니다."),
        );
      }
    };

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

    const fetchPlayers = async () => {
      try {
        const response = await getManagementPlayers();

        if (ignore) {
          return;
        }

        setPlayers(Array.isArray(response) ? response : []);
      } catch (error) {
        if (ignore) {
          return;
        }

        setListErrorMessage(
          getApiErrorMessage(error, "선수 목록을 불러오지 못했습니다."),
        );
      }
    };

    void fetchPlayers();

    return () => {
      ignore = true;
    };
  }, [canManagePlayerClip]);

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
          <h1>선수 개인 분석 클립</h1>
          <p>
            원본 경기 영상의 특정 구간을 선수 개인 피드백용 클립으로 관리합니다.
          </p>
        </div>
      </section>

      {message && <p className="success-message">{message}</p>}
      {listErrorMessage && <p className="error-message">{listErrorMessage}</p>}
      {detailErrorMessage && (
        <p className="error-message">{detailErrorMessage}</p>
      )}
      {formErrorMessage && <p className="error-message">{formErrorMessage}</p>}

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
      </section>

      {canManagePlayerClip && (
        <section className="card">
          <h2>선수 개인 분석 클립 등록</h2>

          {players.length === 0 ? (
            <p className="notice-text">
              등록 가능한 선수가 없습니다. 승인 완료된 선수 계정을 먼저
              확인해주세요.
            </p>
          ) : (
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
                대상 선수
                <select
                  value={createForm.playerId}
                  onChange={(event) =>
                    setCreateForm((prev) => ({
                      ...prev,
                      playerId: Number(event.target.value),
                    }))
                  }
                >
                  <option value={0}>대상 선수를 선택하세요</option>
                  {players.map((player) => (
                    <option key={player.playerId} value={player.playerId}>
                      {createPlayerLabel(player)}
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
                      clipType: event.target.value as PlayerAnalysisClipType,
                    }))
                  }
                >
                  {PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS.map((option) => (
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
                  placeholder="예: 전진 패스 선택 가능 장면"
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
                  placeholder="선수에게 전달할 피드백 코멘트를 입력하세요."
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

              <button type="submit">선수 개인 분석 클립 등록</button>
            </form>
          )}
        </section>
      )}

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
                      {clip.startTimeSec}초 ~ {clip.endTimeSec}초
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
              <video
                ref={videoRef}
                src={createVideoSourceUrl(selectedClip.matchVideoUrl)}
                controls
                onTimeUpdate={handleVideoTimeUpdate}
                className="video-player"
              />

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
                  <strong>대상 선수:</strong> {selectedClip.playerName}
                </p>
                <p>
                  <strong>클립 유형:</strong>{" "}
                  {PLAYER_ANALYSIS_CLIP_TYPE_LABELS[selectedClip.clipType]}
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

              {canManagePlayerClip && (
                <div className="button-row">
                  <button
                    type="button"
                    onClick={() => setIsEditing((prev) => !prev)}
                  >
                    {isEditing ? "수정 취소" : "수정"}
                  </button>

                  {canDeletePlayerClip && (
                    <button type="button" onClick={handleDelete}>
                      삭제
                    </button>
                  )}
                </div>
              )}

              {isEditing && canManagePlayerClip && (
                <form onSubmit={handleUpdateSubmit} className="form-grid">
                  <label>
                    원본 경기 영상
                    <select
                      value={updateForm.matchVideoId}
                      onChange={(event) =>
                        setUpdateForm((prev) => ({
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
                    대상 선수
                    <select
                      value={updateForm.playerId}
                      onChange={(event) =>
                        setUpdateForm((prev) => ({
                          ...prev,
                          playerId: Number(event.target.value),
                        }))
                      }
                    >
                      <option value={0}>대상 선수를 선택하세요</option>
                      {players.map((player) => (
                        <option key={player.playerId} value={player.playerId}>
                          {createPlayerLabel(player)}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label>
                    클립 유형
                    <select
                      value={updateForm.clipType}
                      onChange={(event) =>
                        setUpdateForm((prev) => ({
                          ...prev,
                          clipType: event.target
                            .value as PlayerAnalysisClipType,
                        }))
                      }
                    >
                      {PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS.map((option) => (
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
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
