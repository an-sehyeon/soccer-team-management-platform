// 선수 개인 분석 클립 목록, 상세, 등록, 수정, 삭제와 드로잉 프론트 연동 화면을 제공하는 페이지
import { useContext, useEffect, useMemo, useRef, useState } from "react";
import type { FormEvent } from "react";
import axios from "axios";
import {
  createPlayerAnalysisClipWithDrawings,
  deletePlayerAnalysisClip,
  getManagementPlayerAnalysisClipDetail,
  getManagementPlayerAnalysisClips,
  getManagementPlayers,
  getMyPlayerAnalysisClipDetail,
  getMyPlayerAnalysisClips,
  updatePlayerAnalysisClip,
} from "../api/playerAnalysisClipApi";
import { getMatchVideoDetail, getMatchVideos } from "../api/matchVideoApi";
import { getPlayerAnalysisClipDrawings } from "../api/playerAnalysisClipDrawingApi";
import PlayerAnalysisDrawingCanvas from "../components/PlayerAnalysisDrawingCanvas";
import { AuthContext } from "../contexts/authContext";
import type {
  MatchVideoDetailResponse,
  MatchVideoListItem,
} from "../types/matchVideo";
import type {
  CreatePlayerAnalysisClipDrawingItemRequest,
  CreatePlayerAnalysisClipRequest,
  CreatePlayerAnalysisClipWithDrawingsRequest,
  PlayerAnalysisClipDetailResponse,
  PlayerAnalysisClipListItem,
  PlayerAnalysisClipStatus,
  PlayerAnalysisClipType,
  PlayerSelectItem,
  UpdatePlayerAnalysisClipRequest,
} from "../types/playerAnalysisClip";
import {
  PLAYER_ANALYSIS_CLIP_STATUS_LABELS,
  PLAYER_ANALYSIS_CLIP_TYPE_LABELS,
  PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS,
} from "../types/playerAnalysisClip";
import type {
  PlayerAnalysisClipDrawingData,
  PlayerAnalysisClipDrawingResponse,
  PlayerAnalysisClipDrawingType,
} from "../types/playerAnalysisClipDrawing";
import {
  PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_LABELS,
  PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_OPTIONS,
} from "../types/playerAnalysisClipDrawing";
import { createVideoSourceUrl } from "../utils/videoUrl";
const DEFAULT_DRAWING_DURATION_SEC = 3;
const initialClipForm: CreatePlayerAnalysisClipRequest = {
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
export default function PlayerAnalysisClipPage() {
  const authContext = useContext(AuthContext);
  const member = authContext?.member ?? null;
  const detailVideoRef = useRef<HTMLVideoElement | null>(null);
  const createPreviewVideoRef = useRef<HTMLVideoElement | null>(null);
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
  const [selectedClipVideoErrorMessage, setSelectedClipVideoErrorMessage] =
    useState("");
  const [createForm, setCreateForm] =
    useState<CreatePlayerAnalysisClipRequest>(initialClipForm);
  const [updateForm, setUpdateForm] =
    useState<UpdatePlayerAnalysisClipRequest>(initialClipForm);
  const [isUpdateMode, setIsUpdateMode] = useState(false);
  const [createMatchVideoDetail, setCreateMatchVideoDetail] =
    useState<MatchVideoDetailResponse | null>(null);
  const [createPreviewCurrentTimeSec, setCreatePreviewCurrentTimeSec] =
    useState(0);
  const [createDrawings, setCreateDrawings] = useState<
    CreatePlayerAnalysisClipDrawingItemRequest[]
  >([]);
  const [isCreateDrawingMode, setIsCreateDrawingMode] = useState(false);
  const [createDrawingType, setCreateDrawingType] =
    useState<PlayerAnalysisClipDrawingType>("ARROW");
  const [createDrawingText, setCreateDrawingText] = useState("");
  const [createDrawingStartTimeSec, setCreateDrawingStartTimeSec] = useState(0);
  const [createDrawingEndTimeSec, setCreateDrawingEndTimeSec] = useState(
    DEFAULT_DRAWING_DURATION_SEC,
  );
  const [selectedClipTypeFilter, setSelectedClipTypeFilter] = useState<
    PlayerAnalysisClipType | ""
  >("");
  const [selectedMatchVideoFilter, setSelectedMatchVideoFilter] = useState(0);
  const [selectedPlayerFilter, setSelectedPlayerFilter] = useState(0);
  const [message, setMessage] = useState("");
  const [listErrorMessage, setListErrorMessage] = useState("");
  const [detailErrorMessage, setDetailErrorMessage] = useState("");
  const [formErrorMessage, setFormErrorMessage] = useState("");
  const canManagePlayerClip =
    member?.memberRole === "COACH" || member?.memberRole === "ANALYST";
  const canDeletePlayerClip = member?.memberRole === "COACH";
  const createClipDurationSec = Math.max(
    createForm.endTimeSec - createForm.startTimeSec,
    0,
  );
  const createCurrentClipTimeSec = Math.max(
    0,
    Math.min(
      createPreviewCurrentTimeSec - createForm.startTimeSec,
      createClipDurationSec,
    ),
  );
  const createPreviewSourceUrl = createMatchVideoDetail?.url
    ? createVideoSourceUrl(createMatchVideoDetail.url)
    : "";
  const selectedClipSourceUrl =
    selectedClip?.playerClipUrl && selectedClip.status === "READY"
      ? createVideoSourceUrl(selectedClip.playerClipUrl)
      : "";
  const createCanvasDrawings = useMemo<PlayerAnalysisClipDrawingResponse[]>(
    () =>
      createDrawings.map((drawing, index) => ({
        drawingId: -(index + 1),
        playerClipId: 0,
        drawingType: drawing.drawingType,
        startTimeSec: drawing.startTimeSec,
        endTimeSec: drawing.endTimeSec,
        drawingData: drawing.drawingData,
        writerId: member?.memberId ?? 0,
        writerName: member?.name ?? "작성자",
        createdAt: "",
        updatedAt: "",
      })),
    [createDrawings, member?.memberId, member?.name],
  );
  const createPlayerLabel = (player: PlayerSelectItem) => {
    const uniformNumberText =
      player.uniformNumber === null
        ? "등번호 없음"
        : `${player.uniformNumber}번`;
    const gradeText =
      player.grade === null ? "학년 없음" : `${player.grade}학년`;
    return `${uniformNumberText} · ${player.name} · ${gradeText}`;
  };
  const resetCreateDrawingState = () => {
    setCreateDrawings([]);
    setIsCreateDrawingMode(false);
    setCreateDrawingStartTimeSec(0);
    setCreateDrawingEndTimeSec(DEFAULT_DRAWING_DURATION_SEC);
  };
  const validateCreateForm = (form: CreatePlayerAnalysisClipRequest) => {
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
    if (!selectedMatchVideo) {
      setFormErrorMessage("선택한 원본 경기 영상을 찾을 수 없습니다.");
      return false;
    }
    const matchVideoDurationSec =
      createMatchVideoDetail?.durationSec ?? selectedMatchVideo.durationSec;
    if (matchVideoDurationSec === null || matchVideoDurationSec === undefined) {
      setFormErrorMessage("원본 경기 영상 길이 정보를 확인할 수 없습니다.");
      return false;
    }
    if (matchVideoDurationSec <= 0) {
      setFormErrorMessage("원본 경기 영상 길이 정보가 준비되지 않았습니다.");
      return false;
    }
    if (form.endTimeSec > matchVideoDurationSec) {
      setFormErrorMessage("종료 시간이 원본 경기 영상 길이를 초과했습니다.");
      return false;
    }
    setFormErrorMessage("");
    return true;
  };
  const validateUpdateForm = (form: UpdatePlayerAnalysisClipRequest) => {
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
      setSelectedClipVideoErrorMessage("");
      setSelectedClipDrawings([]);
      setSelectedClipCurrentTimeSec(0);
      setIsUpdateMode(false);
      const detail =
        member.memberRole === "PLAYER"
          ? await getMyPlayerAnalysisClipDetail(playerClipId)
          : await getManagementPlayerAnalysisClipDetail(playerClipId);
      setSelectedClip(detail);
      setUpdateForm({
        matchVideoId: detail.matchVideoId,
        playerId: detail.playerId,
        clipType: detail.clipType,
        title: detail.title,
        comment: detail.comment ?? "",
        startTimeSec: detail.startTimeSec,
        endTimeSec: detail.endTimeSec,
      });
      if (detail.status !== "READY") {
        return;
      }
      const drawings = await getPlayerAnalysisClipDrawings(playerClipId);
      setSelectedClipDrawings(Array.isArray(drawings) ? drawings : []);
    } catch (error) {
      setSelectedClip(null);
      setSelectedClipDrawings([]);
      setSelectedClipVideoErrorMessage("");
      setDetailErrorMessage(
        getApiErrorMessage(
          error,
          "선수 개인 분석 클립 상세 정보를 불러오지 못했습니다.",
        ),
      );
    }
  };
  const handleCreateSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setMessage("");
    setFormErrorMessage("");
    if (!validateCreateForm(createForm)) {
      return;
    }
    if (createDrawings.length === 0) {
      setFormErrorMessage("드로잉을 1개 이상 추가해주세요.");
      return;
    }
    try {
      setDetailErrorMessage("");
      const request: CreatePlayerAnalysisClipWithDrawingsRequest = {
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
        drawings: createDrawings,
      };
      const response = await createPlayerAnalysisClipWithDrawings(request);
      setMessage(
        `선수 개인 분석 클립 등록 요청이 완료되었습니다. 현재 상태: ${getPlayerClipStatusLabel(response.status)}`,
      );
      setCreateForm(initialClipForm);
      setCreateMatchVideoDetail(null);
      setCreatePreviewCurrentTimeSec(0);
      resetCreateDrawingState();
      await reloadPlayerAnalysisClips();
    } catch (error) {
      setFormErrorMessage(
        getApiErrorMessage(error, "선수 개인 분석 클립 등록에 실패했습니다."),
      );
    }
  };
  const handleUpdateSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedClip) {
      setFormErrorMessage("수정할 클립을 선택해주세요.");
      return;
    }
    setMessage("");
    setFormErrorMessage("");
    if (!validateUpdateForm(updateForm)) {
      return;
    }
    try {
      setDetailErrorMessage("");
      setSelectedClipVideoErrorMessage("");
      const response = await updatePlayerAnalysisClip(
        selectedClip.playerClipId,
        {
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
        },
      );
      setSelectedClip(response);
      setSelectedClipCurrentTimeSec(0);
      setSelectedClipDrawings([]);
      setUpdateForm({
        matchVideoId: response.matchVideoId,
        playerId: response.playerId,
        clipType: response.clipType,
        title: response.title,
        comment: response.comment ?? "",
        startTimeSec: response.startTimeSec,
        endTimeSec: response.endTimeSec,
      });
      setIsUpdateMode(false);
      setMessage(
        `선수 개인 분석 클립 수정 요청이 완료되었습니다. 현재 상태: ${getPlayerClipStatusLabel(response.status)}`,
      );
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
      "선택한 선수 개인 분석 클립을 삭제하시겠습니까?",
    );
    if (!isConfirmed) {
      return;
    }
    try {
      setMessage("");
      setFormErrorMessage("");
      setDetailErrorMessage("");
      await deletePlayerAnalysisClip(selectedClip.playerClipId);
      setSelectedClip(null);
      setSelectedClipDrawings([]);
      setSelectedClipCurrentTimeSec(0);
      setSelectedClipVideoErrorMessage("");
      setIsUpdateMode(false);
      setMessage("선수 개인 분석 클립이 삭제되었습니다.");
      await reloadPlayerAnalysisClips();
    } catch (error) {
      setFormErrorMessage(
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
    setSelectedClipVideoErrorMessage("");
  };
  const handleDetailVideoError = () => {
    setSelectedClipVideoErrorMessage(
      "클립 영상을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.",
    );
  };
  const handleDetailReplayClip = () => {
    const video = detailVideoRef.current;
    if (!video) {
      return;
    }
    video.currentTime = 0;
    setSelectedClipCurrentTimeSec(0);
    void video.play();
  };
  const handleCreatePreviewTimeUpdate = () => {
    const video = createPreviewVideoRef.current;
    if (!video) {
      return;
    }
    setCreatePreviewCurrentTimeSec(video.currentTime);
    if (
      createForm.endTimeSec > createForm.startTimeSec &&
      video.currentTime >= createForm.endTimeSec
    ) {
      video.pause();
    }
  };
  const handlePlayCreateClipRange = () => {
    if (!validateCreateForm(createForm)) {
      return;
    }
    const video = createPreviewVideoRef.current;
    if (!video) {
      setFormErrorMessage("원본 경기 영상을 불러온 뒤 재생해주세요.");
      return;
    }
    video.currentTime = createForm.startTimeSec;
    setCreatePreviewCurrentTimeSec(createForm.startTimeSec);
    void video.play();
  };
  const handleSetCreateStartTime = () => {
    const currentTime = Math.floor(createPreviewCurrentTimeSec);
    setCreateForm((prev) => ({
      ...prev,
      startTimeSec: currentTime,
      endTimeSec:
        prev.endTimeSec <= currentTime ? currentTime + 1 : prev.endTimeSec,
    }));
    resetCreateDrawingState();
  };
  const handleSetCreateEndTime = () => {
    const currentTime = Math.floor(createPreviewCurrentTimeSec);
    setCreateForm((prev) => ({ ...prev, endTimeSec: currentTime }));
    resetCreateDrawingState();
  };
  const handleDraftDrawingData = (
    nextDrawingData: PlayerAnalysisClipDrawingData,
  ) => {
    if (createClipDurationSec <= 0) {
      setFormErrorMessage("클립 시작/종료 시간을 먼저 입력해주세요.");
      return;
    }

    if (createDrawingType === "TEXT" && createDrawingText.trim() === "") {
      setFormErrorMessage("텍스트 드로잉 내용을 입력해주세요.");
      return;
    }

    if (createDrawingStartTimeSec < 0 || createDrawingEndTimeSec < 0) {
      setFormErrorMessage(
        "드로잉 시작 시간과 종료 시간은 0 이상이어야 합니다.",
      );
      return;
    }

    if (createDrawingStartTimeSec >= createDrawingEndTimeSec) {
      setFormErrorMessage("드로잉 시작 시간은 종료 시간보다 작아야 합니다.");
      return;
    }

    if (createDrawingEndTimeSec > createClipDurationSec) {
      setFormErrorMessage("드로잉 종료 시간이 클립 길이를 초과했습니다.");
      return;
    }

    const nextDrawing: CreatePlayerAnalysisClipDrawingItemRequest = {
      drawingType: createDrawingType,
      startTimeSec: createDrawingStartTimeSec,
      endTimeSec: createDrawingEndTimeSec,
      drawingData: nextDrawingData,
    };

    setCreateDrawings((prev) => [...prev, nextDrawing]);
    setFormErrorMessage("");
  };
  const handleRemoveCreateDrawing = (drawingIndex: number) => {
    setCreateDrawings((prev) =>
      prev.filter((_, index) => index !== drawingIndex),
    );
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
    if (createForm.matchVideoId <= 0) {
      return;
    }
    let ignore = false;
    const fetchCreateMatchVideoDetail = async () => {
      try {
        const detail = await getMatchVideoDetail(createForm.matchVideoId);
        if (ignore) {
          return;
        }
        setCreateMatchVideoDetail(detail);
        setFormErrorMessage("");
      } catch (error) {
        if (ignore) {
          return;
        }
        setCreateMatchVideoDetail(null);
        setFormErrorMessage(
          getApiErrorMessage(
            error,
            "등록용 경기 영상 정보를 불러오지 못했습니다.",
          ),
        );
      }
    };
    void fetchCreateMatchVideoDetail();
    return () => {
      ignore = true;
    };
  }, [createForm.matchVideoId]);
  return (
    <main>
      <h1>선수 개인 분석 클립</h1>{" "}
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
            {" "}
            경기 영상
            <select
              value={selectedMatchVideoFilter}
              onChange={(event) =>
                setSelectedMatchVideoFilter(Number(event.target.value))
              }
            >
              <option value={0}>전체 경기 영상</option>{" "}
              {matchVideos.map((matchVideo) => (
                <option
                  key={matchVideo.matchVideoId}
                  value={matchVideo.matchVideoId}
                >
                  {" "}
                  {matchVideo.title}
                </option>
              ))}
            </select>
          </label>{" "}
          {canManagePlayerClip && (
            <label>
              {" "}
              대상 선수
              <select
                value={selectedPlayerFilter}
                onChange={(event) =>
                  setSelectedPlayerFilter(Number(event.target.value))
                }
              >
                <option value={0}>전체 선수</option>{" "}
                {players.map((player) => (
                  <option key={player.playerId} value={player.playerId}>
                    {" "}
                    {createPlayerLabel(player)}
                  </option>
                ))}
              </select>
            </label>
          )}
          <label>
            {" "}
            클립 유형
            <select
              value={selectedClipTypeFilter}
              onChange={(event) =>
                setSelectedClipTypeFilter(
                  event.target.value as PlayerAnalysisClipType | "",
                )
              }
            >
              <option value="">전체 유형</option>{" "}
              {PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {" "}
                  {option.label}
                </option>
              ))}
            </select>
          </label>
        </div>
      </section>{" "}
      {canManagePlayerClip && (
        <section className="card">
          <h2>선수 개인 분석 클립 등록</h2>{" "}
          {players.length === 0 ? (
            <p>등록 가능한 선수가 없습니다.</p>
          ) : (
            <form onSubmit={handleCreateSubmit} className="form-grid">
              <label>
                {" "}
                원본 경기 영상
                <select
                  value={createForm.matchVideoId}
                  onChange={(event) => {
                    const nextMatchVideoId = Number(event.target.value);
                    setCreateForm((prev) => ({
                      ...prev,
                      matchVideoId: nextMatchVideoId,
                    }));
                    setCreateMatchVideoDetail(null);
                    setCreatePreviewCurrentTimeSec(0);
                    resetCreateDrawingState();
                  }}
                >
                  <option value={0}>경기 영상을 선택하세요</option>{" "}
                  {matchVideos.map((matchVideo) => (
                    <option
                      key={matchVideo.matchVideoId}
                      value={matchVideo.matchVideoId}
                    >
                      {" "}
                      {matchVideo.title} ·{" "}
                      {getVideoStatusLabel(matchVideo.status)}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                {" "}
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
                  <option value={0}>대상 선수를 선택하세요</option>{" "}
                  {players.map((player) => (
                    <option key={player.playerId} value={player.playerId}>
                      {" "}
                      {createPlayerLabel(player)}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                {" "}
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
                  {" "}
                  {PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {" "}
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                {" "}
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
                />
              </label>
              <label>
                {" "}
                코멘트
                <textarea
                  value={createForm.comment ?? ""}
                  onChange={(event) =>
                    setCreateForm((prev) => ({
                      ...prev,
                      comment: event.target.value,
                    }))
                  }
                />
              </label>
              <label>
                {" "}
                시작 시간(초)
                <input
                  type="number"
                  min={0}
                  value={createForm.startTimeSec}
                  onChange={(event) => {
                    setCreateForm((prev) => ({
                      ...prev,
                      startTimeSec: Number(event.target.value),
                    }));
                    resetCreateDrawingState();
                  }}
                />
              </label>
              <label>
                {" "}
                종료 시간(초)
                <input
                  type="number"
                  min={0}
                  value={createForm.endTimeSec}
                  onChange={(event) => {
                    setCreateForm((prev) => ({
                      ...prev,
                      endTimeSec: Number(event.target.value),
                    }));
                    resetCreateDrawingState();
                  }}
                />
              </label>{" "}
              {createPreviewSourceUrl && (
                <div className="detail-stack">
                  <h3>원본 경기 영상</h3>
                  <div style={{ position: "relative", width: "100%" }}>
                    <video
                      ref={createPreviewVideoRef}
                      src={createPreviewSourceUrl}
                      controls
                      onTimeUpdate={handleCreatePreviewTimeUpdate}
                      className="video-player"
                      style={{ display: "block", width: "100%" }}
                    />{" "}
                    {isCreateDrawingMode && createClipDurationSec > 0 && (
                      <PlayerAnalysisDrawingCanvas
                        drawings={createCanvasDrawings}
                        currentTimeSec={createCurrentClipTimeSec}
                        isDrawingMode={isCreateDrawingMode}
                        drawingType={createDrawingType}
                        drawingText={createDrawingText}
                        onDraftDrawingData={handleDraftDrawingData}
                      />
                    )}
                  </div>
                  <div className="button-row">
                    <button type="button" onClick={handlePlayCreateClipRange}>
                      {" "}
                      선택 구간 재생
                    </button>
                    <button type="button" onClick={handleSetCreateStartTime}>
                      {" "}
                      현재 시간을 시작으로 설정
                    </button>
                    <button type="button" onClick={handleSetCreateEndTime}>
                      {" "}
                      현재 시간을 종료로 설정
                    </button>
                  </div>
                  <p>
                    {" "}
                    원본 영상 현재 시간:{" "}
                    {Math.floor(createPreviewCurrentTimeSec)}초 / 클립 기준 현재
                    시간: {Math.floor(createCurrentClipTimeSec)}초 / 클립 길이:{" "}
                    {createClipDurationSec}초
                  </p>
                  <div className="form-grid">
                    <label>
                      {" "}
                      드로잉 유형
                      <select
                        value={createDrawingType}
                        onChange={(event) =>
                          setCreateDrawingType(
                            event.target.value as PlayerAnalysisClipDrawingType,
                          )
                        }
                      >
                        {" "}
                        {PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_OPTIONS.map(
                          (option) => (
                            <option key={option.value} value={option.value}>
                              {" "}
                              {option.label}
                            </option>
                          ),
                        )}
                      </select>
                    </label>
                    <label>
                      텍스트
                      <input
                        type="text"
                        value={createDrawingText}
                        onChange={(event) =>
                          setCreateDrawingText(event.target.value)
                        }
                      />
                    </label>
                    <label>
                      드로잉 시작 시간(초)
                      <input
                        type="number"
                        min={0}
                        value={createDrawingStartTimeSec}
                        onChange={(event) =>
                          setCreateDrawingStartTimeSec(
                            Number(event.target.value),
                          )
                        }
                      />
                    </label>

                    <label>
                      드로잉 종료 시간(초)
                      <input
                        type="number"
                        min={0}
                        value={createDrawingEndTimeSec}
                        onChange={(event) =>
                          setCreateDrawingEndTimeSec(Number(event.target.value))
                        }
                      />
                    </label>
                  </div>
                  <div className="button-row">
                    <button
                      type="button"
                      onClick={() => {
                        if (
                          !isCreateDrawingMode &&
                          createDrawingType === "TEXT" &&
                          createDrawingText.trim() === ""
                        ) {
                          setFormErrorMessage(
                            "텍스트 드로잉 내용을 입력해주세요.",
                          );
                          return;
                        }
                        setFormErrorMessage("");
                        setIsCreateDrawingMode((prev) => !prev);
                      }}
                    >
                      {" "}
                      {isCreateDrawingMode
                        ? "드로잉 모드 끄기"
                        : "드로잉 모드 켜기"}
                    </button>
                  </div>{" "}
                  {createDrawings.length > 0 && (
                    <div className="detail-box">
                      <h3>추가된 드로잉</h3>
                      <ul className="list">
                        {" "}
                        {createDrawings.map((drawing, index) => (
                          <li key={`${drawing.drawingType}-${index}`}>
                            <span>
                              {" "}
                              {
                                PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_LABELS[
                                  drawing.drawingType
                                ]
                              }{" "}
                              · {drawing.startTimeSec}초 ~ {drawing.endTimeSec}
                              초{" "}
                            </span>
                            <button
                              type="button"
                              onClick={() => handleRemoveCreateDrawing(index)}
                            >
                              {" "}
                              제거
                            </button>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              )}
              <button type="submit">등록</button>
            </form>
          )}
        </section>
      )}
      <section className="content-grid">
        <div className="card">
          <h2>선수 개인 분석 클립 목록</h2>{" "}
          {clips.length === 0 ? (
            <p>조회된 선수 개인 분석 클립이 없습니다.</p>
          ) : (
            <ul className="list">
              {" "}
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
                      {" "}
                      {PLAYER_ANALYSIS_CLIP_TYPE_LABELS[clip.clipType]} ·{" "}
                      {getPlayerClipStatusLabel(clip.status)}{" "}
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
          <h2>선수 개인 분석 클립 상세</h2>{" "}
          {!selectedClip ? (
            <p>목록에서 선수 개인 분석 클립을 선택해주세요.</p>
          ) : (
            <div className="detail-stack">
              {" "}
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
                  </div>{" "}
                  {selectedClipVideoErrorMessage && (
                    <p className="error-message">
                      {" "}
                      {selectedClipVideoErrorMessage}
                    </p>
                  )}
                  <div className="button-row">
                    <button type="button" onClick={handleDetailReplayClip}>
                      {" "}
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
                  <strong>작성자:</strong> {selectedClip.editorName}
                </p>
              </div>{" "}
              {canManagePlayerClip && (
                <div className="detail-box">
                  <div className="button-row">
                    <button
                      type="button"
                      onClick={() => {
                        setIsUpdateMode((prev) => !prev);
                        setFormErrorMessage("");
                      }}
                    >
                      {" "}
                      {isUpdateMode ? "수정 취소" : "수정"}
                    </button>{" "}
                    {canDeletePlayerClip && (
                      <button type="button" onClick={handleDelete}>
                        {" "}
                        삭제
                      </button>
                    )}
                  </div>{" "}
                  {isUpdateMode && (
                    <form onSubmit={handleUpdateSubmit} className="form-grid">
                      <label>
                        {" "}
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
                          <option value={0}>경기 영상을 선택하세요</option>{" "}
                          {matchVideos.map((matchVideo) => (
                            <option
                              key={matchVideo.matchVideoId}
                              value={matchVideo.matchVideoId}
                            >
                              {" "}
                              {matchVideo.title} ·{" "}
                              {getVideoStatusLabel(matchVideo.status)}
                            </option>
                          ))}
                        </select>
                      </label>
                      <label>
                        {" "}
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
                          <option value={0}>대상 선수를 선택하세요</option>{" "}
                          {players.map((player) => (
                            <option
                              key={player.playerId}
                              value={player.playerId}
                            >
                              {" "}
                              {createPlayerLabel(player)}
                            </option>
                          ))}
                        </select>
                      </label>
                      <label>
                        {" "}
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
                          {" "}
                          {PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS.map((option) => (
                            <option key={option.value} value={option.value}>
                              {" "}
                              {option.label}
                            </option>
                          ))}
                        </select>
                      </label>
                      <label>
                        {" "}
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
                        {" "}
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
                        {" "}
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
                        {" "}
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
          )}
        </div>
      </section>
    </main>
  );
}
