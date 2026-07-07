// 선수 개인 분석 클립 등록/수정 전용 편집기 페이지
import { type FormEvent, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getMatchVideoDetail, getMatchVideos } from "../api/matchVideoApi";
import {
  createPlayerAnalysisClipWithDrawings,
  getManagementPlayerAnalysisClipDetail,
  getManagementPlayers,
  updatePlayerAnalysisClipWithDrawings,
} from "../api/playerAnalysisClipApi";
import { getPlayerAnalysisClipDrawings } from "../api/playerAnalysisClipDrawingApi";
import PlayerAnalysisDrawingCanvas from "../components/PlayerAnalysisDrawingCanvas";
import { ROUTES, createPlayerAnalysisClipEditRoute } from "../constants/routes";
import type {
  MatchVideoDetailResponse,
  MatchVideoListItem,
} from "../types/matchVideo";
import type {
  CreatePlayerAnalysisClipDrawingItemRequest,
  PlayerAnalysisClipDetailResponse,
  PlayerAnalysisClipEditorForm,
  PlayerAnalysisClipType,
  PlayerSelectItem,
} from "../types/playerAnalysisClip";
import { PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS } from "../types/playerAnalysisClip";
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

type EditorDrawing = CreatePlayerAnalysisClipDrawingItemRequest & {
  localId: string;
};

type SelectedMatchVideoState = {
  matchVideoId: number;
  detail: MatchVideoDetailResponse | null;
};

const initialForm: PlayerAnalysisClipEditorForm = {
  matchVideoId: 0,
  playerId: 0,
  clipType: "PLAYER_GOOD",
  title: "",
  comment: "",
  startTimeSec: 0,
  endTimeSec: 0,
};

const initialDrawingForm: Omit<
  CreatePlayerAnalysisClipDrawingItemRequest,
  "drawingData"
> = {
  drawingType: "ARROW",
  startTimeSec: 0,
  endTimeSec: 0,
};

function createLocalId() {
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function getSafeNumber(value: unknown, fallbackValue = 0) {
  if (typeof value === "number" && Number.isFinite(value)) {
    return value;
  }

  if (typeof value === "string" && value.trim() !== "") {
    const parsedValue = Number(value);

    if (Number.isFinite(parsedValue)) {
      return parsedValue;
    }
  }

  return fallbackValue;
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

function isValidObjectData(drawingData: PlayerAnalysisClipDrawingData) {
  if (
    drawingData === null ||
    typeof drawingData !== "object" ||
    Array.isArray(drawingData)
  ) {
    return false;
  }

  return Object.keys(drawingData).length > 0;
}

function convertEditorDrawingsToCanvasDrawings(
  drawings: EditorDrawing[],
  playerClipId: number,
): PlayerAnalysisClipDrawingResponse[] {
  return drawings.map((drawing, index) => ({
    drawingId: index + 1,
    playerClipId,
    drawingType: drawing.drawingType,
    startTimeSec: drawing.startTimeSec,
    endTimeSec: drawing.endTimeSec,
    drawingData: drawing.drawingData,
    writerId: 0,
    writerName: "저장 전",
    createdAt: "",
    updatedAt: "",
  }));
}

function convertDraftDrawingsToCanvasDrawings(
  draftDrawings: EditorDrawing[],
  playerClipId: number,
  clipDurationSec: number,
): PlayerAnalysisClipDrawingResponse[] {
  return draftDrawings.map((drawing, index) => ({
    drawingId: -1 * (index + 1),
    playerClipId,
    drawingType: drawing.drawingType,
    startTimeSec: 0,
    endTimeSec: Math.max(clipDurationSec, 1),
    drawingData: drawing.drawingData,
    writerId: 0,
    writerName: "작성 중",
    createdAt: "",
    updatedAt: "",
  }));
}

function convertSavedDrawingToEditorDrawing(
  drawing: PlayerAnalysisClipDrawingResponse,
): EditorDrawing {
  return {
    localId: `saved-${drawing.drawingId}`,
    drawingType: drawing.drawingType,
    startTimeSec: getSafeNumber(drawing.startTimeSec),
    endTimeSec: getSafeNumber(drawing.endTimeSec),
    drawingData: drawing.drawingData,
  };
}

export default function PlayerAnalysisClipEditorPage() {
  const navigate = useNavigate();
  const { playerClipId } = useParams();
  const videoRef = useRef<HTMLVideoElement | null>(null);

  const isEditMode = playerClipId !== undefined;
  const parsedPlayerClipId = Number.parseInt(playerClipId ?? "", 10);
  const hasInvalidEditPlayerClipId =
    isEditMode && Number.isNaN(parsedPlayerClipId);

  const [form, setForm] = useState<PlayerAnalysisClipEditorForm>(initialForm);
  const [matchVideos, setMatchVideos] = useState<MatchVideoListItem[]>([]);
  const [players, setPlayers] = useState<PlayerSelectItem[]>([]);
  const [selectedMatchVideoState, setSelectedMatchVideoState] =
    useState<SelectedMatchVideoState>({
      matchVideoId: 0,
      detail: null,
    });
  const [editingClip, setEditingClip] =
    useState<PlayerAnalysisClipDetailResponse | null>(null);
  const [drawings, setDrawings] = useState<EditorDrawing[]>([]);
  const [draftDrawings, setDraftDrawings] = useState<EditorDrawing[]>([]);
  const [drawingForm, setDrawingForm] =
    useState<Omit<CreatePlayerAnalysisClipDrawingItemRequest, "drawingData">>(
      initialDrawingForm,
    );
  const [isDrawingMode, setIsDrawingMode] = useState(false);
  const [drawingText, setDrawingText] = useState("");
  const [currentOriginalTimeSec, setCurrentOriginalTimeSec] = useState(0);
  const [currentClipTimeSec, setCurrentClipTimeSec] = useState(0);
  const [isPlayingSelectedRange, setIsPlayingSelectedRange] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const selectedMatchVideo = useMemo(() => {
    if (form.matchVideoId <= 0) {
      return null;
    }

    if (selectedMatchVideoState.matchVideoId !== form.matchVideoId) {
      return null;
    }

    return selectedMatchVideoState.detail;
  }, [form.matchVideoId, selectedMatchVideoState]);

  const safeClipStartTimeSec = getSafeNumber(form.startTimeSec);
  const safeClipEndTimeSec = getSafeNumber(form.endTimeSec);
  const clipDurationSec =
    safeClipEndTimeSec > safeClipStartTimeSec
      ? safeClipEndTimeSec - safeClipStartTimeSec
      : 0;

  const shouldShowEditorSections =
    isEditMode || (selectedMatchVideo !== null && form.playerId > 0);

  const canvasDrawings = useMemo(() => {
    const currentPlayerClipId = Number.isNaN(parsedPlayerClipId)
      ? 0
      : parsedPlayerClipId;

    const savedCanvasDrawings = convertEditorDrawingsToCanvasDrawings(
      drawings,
      currentPlayerClipId,
    );

    const draftCanvasDrawings = convertDraftDrawingsToCanvasDrawings(
      draftDrawings,
      currentPlayerClipId,
      clipDurationSec,
    );

    return [...savedCanvasDrawings, ...draftCanvasDrawings];
  }, [drawings, draftDrawings, parsedPlayerClipId, clipDurationSec]);

  const pageTitle = isEditMode
    ? "선수 개인 분석 클립 수정"
    : "선수 개인 분석 클립 등록";

  const submitButtonText = isEditMode
    ? "선수 개인 분석 클립 수정 저장"
    : "선수 개인 분석 클립 생성 요청";

  const clipDurationText =
    clipDurationSec > 0 ? formatSeconds(clipDurationSec) : "클립 시간 설정 전";

  const drawingTimeGuideText =
    clipDurationSec > 0
      ? `생성될 선수 개인 분석 클립 기준 0초 ~ ${clipDurationSec}초`
      : "클립 시간 설정 후 표시됩니다.";

  const originalVideoSourceUrl = selectedMatchVideo?.url
    ? createVideoSourceUrl(selectedMatchVideo.url)
    : "";

  const handleBackToList = () => {
    navigate(ROUTES.PLAYER_ANALYSIS_CLIP);
  };

  const handleMoveToEditPage = (createdPlayerClipId: number) => {
    navigate(createPlayerAnalysisClipEditRoute(createdPlayerClipId));
  };

  const getOriginalVideoDurationSec = () => {
    if (
      selectedMatchVideo?.durationSec !== null &&
      selectedMatchVideo?.durationSec !== undefined &&
      Number.isFinite(selectedMatchVideo.durationSec)
    ) {
      return selectedMatchVideo.durationSec;
    }

    const video = videoRef.current;

    if (!video || Number.isNaN(video.duration)) {
      return null;
    }

    return Math.floor(video.duration);
  };

  const getCurrentOriginalTimeFromVideo = () => {
    const video = videoRef.current;

    if (!video) {
      return currentOriginalTimeSec;
    }

    return Math.max(0, video.currentTime);
  };

  const getCurrentClipTimeFromVideo = () => {
    const relativeTimeSec =
      getCurrentOriginalTimeFromVideo() - safeClipStartTimeSec;

    return Math.min(Math.max(relativeTimeSec, 0), clipDurationSec);
  };

  const handlePlaySelectedRange = () => {
    const video = videoRef.current;

    if (!video || clipDurationSec <= 0) {
      return;
    }

    video.currentTime = safeClipStartTimeSec;
    setCurrentOriginalTimeSec(safeClipStartTimeSec);
    setCurrentClipTimeSec(0);
    setIsPlayingSelectedRange(true);
    void video.play();
  };

  const handleVideoTimeUpdate = () => {
    const video = videoRef.current;

    if (!video) {
      return;
    }

    const originalTimeSec = Math.max(0, video.currentTime);
    const relativeTimeSec = Math.max(0, originalTimeSec - safeClipStartTimeSec);
    const safeRelativeTimeSec = Math.min(relativeTimeSec, clipDurationSec);

    setCurrentOriginalTimeSec(originalTimeSec);
    setCurrentClipTimeSec(safeRelativeTimeSec);

    if (isPlayingSelectedRange && originalTimeSec >= safeClipEndTimeSec) {
      video.pause();
      setIsPlayingSelectedRange(false);
    }
  };

  const handleVideoPause = () => {
    setIsPlayingSelectedRange(false);
  };

  const handleSetClipStartTimeFromCurrentTime = () => {
    const currentTimeSec = Math.floor(getCurrentOriginalTimeFromVideo());
    const durationSec = getOriginalVideoDurationSec();

    setForm((prev) => {
      const maxStartTimeSec =
        durationSec !== null ? Math.max(0, durationSec - 1) : currentTimeSec;
      const nextStartTimeSec = Math.min(currentTimeSec, maxStartTimeSec);
      const prevEndTimeSec = getSafeNumber(prev.endTimeSec);
      const nextEndTimeSec =
        prevEndTimeSec <= nextStartTimeSec
          ? nextStartTimeSec + 1
          : prevEndTimeSec;

      return {
        ...prev,
        startTimeSec: nextStartTimeSec,
        endTimeSec:
          durationSec !== null
            ? Math.min(nextEndTimeSec, durationSec)
            : nextEndTimeSec,
      };
    });
  };

  const handleSetClipEndTimeFromCurrentTime = () => {
    const currentTimeSec = Math.floor(getCurrentOriginalTimeFromVideo());
    const durationSec = getOriginalVideoDurationSec();

    setForm((prev) => {
      const prevStartTimeSec = getSafeNumber(prev.startTimeSec);
      const minimumEndTimeSec = prevStartTimeSec + 1;
      const rawEndTimeSec = Math.max(currentTimeSec, minimumEndTimeSec);

      return {
        ...prev,
        endTimeSec:
          durationSec !== null
            ? Math.min(rawEndTimeSec, durationSec)
            : rawEndTimeSec,
      };
    });
  };

  const handleResetClipTime = () => {
    const durationSec = getOriginalVideoDurationSec();
    const nextEndTimeSec = durationSec !== null ? durationSec : 0;

    setForm((prev) => ({
      ...prev,
      startTimeSec: 0,
      endTimeSec: nextEndTimeSec,
    }));

    setDrawingForm((prev) => ({
      ...prev,
      startTimeSec: 0,
      endTimeSec: nextEndTimeSec > 0 ? Math.min(5, nextEndTimeSec) : 0,
    }));

    setCurrentOriginalTimeSec(0);
    setCurrentClipTimeSec(0);
    setIsPlayingSelectedRange(false);

    const video = videoRef.current;

    if (video) {
      video.currentTime = 0;
      video.pause();
    }
  };

  const handleSetDrawingStartTimeFromCurrentTime = () => {
    const currentTimeSec = Math.floor(getCurrentClipTimeFromVideo());

    setDrawingForm((prev) => ({
      ...prev,
      startTimeSec: currentTimeSec,
    }));
  };

  const handleSetDrawingEndTimeFromCurrentTime = () => {
    const currentTimeSec = Math.floor(getCurrentClipTimeFromVideo());

    setDrawingForm((prev) => ({
      ...prev,
      endTimeSec: currentTimeSec,
    }));
  };

  const handleResetDrawingTime = () => {
    setDrawingForm((prev) => ({
      ...prev,
      startTimeSec: 0,
      endTimeSec: clipDurationSec > 0 ? Math.min(5, clipDurationSec) : 0,
    }));
  };

  const getDrawingTimeRangeErrorMessage = () => {
    const drawingStartTimeSec = getSafeNumber(drawingForm.startTimeSec);
    const drawingEndTimeSec = getSafeNumber(drawingForm.endTimeSec);

    if (clipDurationSec <= 0) {
      return "먼저 올바른 클립 시작/종료 시간을 설정해주세요.";
    }

    if (drawingStartTimeSec < 0 || drawingEndTimeSec < 0) {
      return "드로잉 시작/종료 시간은 0 이상이어야 합니다.";
    }

    if (drawingStartTimeSec >= drawingEndTimeSec) {
      return "드로잉 시작 시간은 종료 시간보다 작아야 합니다.";
    }

    if (drawingEndTimeSec > clipDurationSec) {
      return "드로잉 종료 시간이 생성될 선수 개인 분석 클립 길이를 초과할 수 없습니다.";
    }

    return "";
  };

  const getClipFormErrorMessage = () => {
    if (hasInvalidEditPlayerClipId) {
      return "수정할 선수 개인 분석 클립 ID가 올바르지 않습니다.";
    }

    if (form.matchVideoId <= 0) {
      return "원본 경기 영상을 선택해주세요.";
    }

    if (form.playerId <= 0) {
      return "대상 선수를 선택해주세요.";
    }

    if (form.title.trim() === "") {
      return "클립 제목을 입력해주세요.";
    }

    if (safeClipStartTimeSec < 0 || safeClipEndTimeSec < 0) {
      return "시작 시간과 종료 시간은 0 이상이어야 합니다.";
    }

    if (safeClipStartTimeSec >= safeClipEndTimeSec) {
      return "시작 시간은 종료 시간보다 작아야 합니다.";
    }

    if (
      selectedMatchVideo?.durationSec !== null &&
      selectedMatchVideo?.durationSec !== undefined &&
      Number.isFinite(selectedMatchVideo.durationSec) &&
      safeClipEndTimeSec > selectedMatchVideo.durationSec
    ) {
      return "종료 시간이 원본 경기 영상 길이를 초과했습니다.";
    }

    return "";
  };

  const validateDraftDrawings = () => {
    if (draftDrawings.length === 0) {
      setErrorMessage("먼저 영상 위 캔버스에서 드로잉을 작성해주세요.");
      return false;
    }

    const timeRangeErrorMessage = getDrawingTimeRangeErrorMessage();

    if (timeRangeErrorMessage) {
      setErrorMessage(timeRangeErrorMessage);
      return false;
    }

    const hasInvalidDrawingData = draftDrawings.some(
      (drawing) => !isValidObjectData(drawing.drawingData),
    );

    if (hasInvalidDrawingData) {
      setErrorMessage("작성 중인 드로잉 데이터가 올바르지 않습니다.");
      return false;
    }

    return true;
  };

  const handleDraftDrawingData = (
    drawingData: PlayerAnalysisClipDrawingData,
  ) => {
    setErrorMessage("");

    const timeRangeErrorMessage = getDrawingTimeRangeErrorMessage();

    if (timeRangeErrorMessage) {
      setErrorMessage(timeRangeErrorMessage);
      return;
    }

    if (!isValidObjectData(drawingData)) {
      setErrorMessage("드로잉 데이터가 올바르지 않습니다.");
      return;
    }

    setDraftDrawings((prev) => [
      ...prev,
      {
        localId: createLocalId(),
        drawingType: drawingForm.drawingType,
        startTimeSec: getSafeNumber(drawingForm.startTimeSec),
        endTimeSec: getSafeNumber(drawingForm.endTimeSec),
        drawingData,
      },
    ]);
  };

  const handleAddDraftDrawings = () => {
    setErrorMessage("");

    if (!validateDraftDrawings()) {
      return;
    }

    setDrawings((prev) => [...prev, ...draftDrawings]);
    setDraftDrawings([]);
  };

  const handleRemoveDraftDrawing = (localId: string) => {
    setDraftDrawings((prev) =>
      prev.filter((drawing) => drawing.localId !== localId),
    );
  };

  const handleRemoveDrawing = (localId: string) => {
    setDrawings((prev) =>
      prev.filter((drawing) => drawing.localId !== localId),
    );
  };

  const handleClearDraftDrawings = () => {
    setDraftDrawings([]);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setErrorMessage("");

    const clipFormErrorMessage = getClipFormErrorMessage();

    if (clipFormErrorMessage) {
      alert(clipFormErrorMessage);
      return;
    }

    if (draftDrawings.length > 0) {
      alert(
        "작성 중인 드로잉이 있습니다. 먼저 저장 전 드로잉 목록에 추가해주세요.",
      );
      return;
    }

    try {
      setIsSubmitting(true);

      const requestDrawings: CreatePlayerAnalysisClipDrawingItemRequest[] =
        drawings.map((drawing) => ({
          drawingType: drawing.drawingType,
          startTimeSec: getSafeNumber(drawing.startTimeSec),
          endTimeSec: getSafeNumber(drawing.endTimeSec),
          drawingData: drawing.drawingData,
        }));

      if (isEditMode) {
        const response = await updatePlayerAnalysisClipWithDrawings(
          parsedPlayerClipId,
          {
            playerId: form.playerId,
            clipType: form.clipType,
            title: form.title.trim(),
            comment: form.comment.trim() === "" ? null : form.comment.trim(),
            startTimeSec: safeClipStartTimeSec,
            endTimeSec: safeClipEndTimeSec,
            drawings: requestDrawings,
          },
        );

        alert(response.message);
        navigate(ROUTES.PLAYER_ANALYSIS_CLIP);
        return;
      }

      const response = await createPlayerAnalysisClipWithDrawings({
        matchVideoId: form.matchVideoId,
        playerId: form.playerId,
        clipType: form.clipType,
        title: form.title.trim(),
        comment: form.comment.trim() === "" ? null : form.comment.trim(),
        startTimeSec: safeClipStartTimeSec,
        endTimeSec: safeClipEndTimeSec,
        drawings: requestDrawings,
      });

      alert(response.message);
      handleMoveToEditPage(response.playerClipId);
    } catch {
      alert(
        isEditMode
          ? "선수 개인 분석 클립 수정에 실패했습니다."
          : "선수 개인 분석 클립 등록에 실패했습니다.",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

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
      } catch {
        if (!ignore) {
          setErrorMessage("경기 영상 목록을 불러오지 못했습니다.");
        }
      }
    }

    void fetchMatchVideos();

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    let ignore = false;

    async function fetchPlayers() {
      try {
        const response = await getManagementPlayers();

        if (!ignore) {
          setPlayers(Array.isArray(response) ? response : []);
        }
      } catch {
        if (!ignore) {
          setErrorMessage("선수 목록을 불러오지 못했습니다.");
        }
      }
    }

    void fetchPlayers();

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    if (form.matchVideoId <= 0) {
      return;
    }

    let ignore = false;
    const targetMatchVideoId = form.matchVideoId;

    async function fetchMatchVideoDetail() {
      try {
        const response = await getMatchVideoDetail(targetMatchVideoId);

        if (!ignore) {
          setSelectedMatchVideoState({
            matchVideoId: targetMatchVideoId,
            detail: response,
          });

          const durationSec = response.durationSec;

          if (
            !isEditMode &&
            durationSec !== null &&
            durationSec !== undefined
          ) {
            setForm((prev) => ({
              ...prev,
              startTimeSec: 0,
              endTimeSec: durationSec,
            }));

            setDrawingForm((prev) => ({
              ...prev,
              startTimeSec: 0,
              endTimeSec: Math.min(5, durationSec),
            }));
          }
        }
      } catch {
        if (!ignore) {
          setErrorMessage("선택한 원본 경기 영상을 불러오지 못했습니다.");
        }
      }
    }

    void fetchMatchVideoDetail();

    return () => {
      ignore = true;
    };
  }, [form.matchVideoId, isEditMode]);

  useEffect(() => {
    if (!isEditMode || Number.isNaN(parsedPlayerClipId)) {
      return;
    }

    let ignore = false;

    async function fetchEditData() {
      try {
        const [clipDetail, savedDrawings] = await Promise.all([
          getManagementPlayerAnalysisClipDetail(parsedPlayerClipId),
          getPlayerAnalysisClipDrawings(parsedPlayerClipId),
        ]);

        if (ignore) {
          return;
        }

        const startTimeSec = getSafeNumber(clipDetail.startTimeSec);
        const rawEndTimeSec = getSafeNumber(clipDetail.endTimeSec);
        const endTimeSec =
          rawEndTimeSec > startTimeSec ? rawEndTimeSec : startTimeSec;

        setEditingClip(clipDetail);
        setForm({
          matchVideoId: clipDetail.matchVideoId,
          playerId: clipDetail.playerId,
          clipType: clipDetail.clipType,
          title: clipDetail.title,
          comment: clipDetail.comment ?? "",
          startTimeSec,
          endTimeSec,
        });

        setDrawings(
          Array.isArray(savedDrawings)
            ? savedDrawings.map(convertSavedDrawingToEditorDrawing)
            : [],
        );

        setDrawingForm({
          ...initialDrawingForm,
          startTimeSec: 0,
          endTimeSec: Math.min(5, Math.max(0, endTimeSec - startTimeSec)),
        });
      } catch {
        if (!ignore) {
          setErrorMessage(
            "선수 개인 분석 클립 수정 정보를 불러오지 못했습니다.",
          );
        }
      }
    }

    void fetchEditData();

    return () => {
      ignore = true;
    };
  }, [isEditMode, parsedPlayerClipId]);

  useEffect(() => {
    const video = videoRef.current;

    if (!video || safeClipStartTimeSec < 0) {
      return;
    }

    const moveToClipStart = () => {
      video.currentTime = safeClipStartTimeSec;
      setCurrentOriginalTimeSec(safeClipStartTimeSec);
      setCurrentClipTimeSec(0);
    };

    video.addEventListener("loadedmetadata", moveToClipStart);

    return () => {
      video.removeEventListener("loadedmetadata", moveToClipStart);
    };
  }, [selectedMatchVideo?.url, safeClipStartTimeSec]);

  return (
    <main className="page-container">
      <h1>{pageTitle}</h1>
      <p>원본 영상을 보면서 클립 구간과 드로잉 구간을 설정합니다.</p>

      <button type="button" onClick={handleBackToList}>
        목록으로 돌아가기
      </button>

      {hasInvalidEditPlayerClipId && (
        <p className="error-message">
          수정할 선수 개인 분석 클립 ID가 올바르지 않습니다.
        </p>
      )}

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      <form onSubmit={handleSubmit} className="detail-stack">
        <section className="card">
          <h2>원본 영상 및 대상 선수 선택</h2>

          <div className="form-grid">
            <label>
              원본 경기 영상
              <select
                value={form.matchVideoId}
                disabled={isEditMode}
                onChange={(event) =>
                  setForm((prev) => ({
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
                value={form.playerId}
                onChange={(event) =>
                  setForm((prev) => ({
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
          </div>

          {!shouldShowEditorSections && (
            <p className="notice-text">
              원본 경기 영상과 대상 선수를 선택하면 클립 정보, 드로잉 작성, 최종
              저장 영역이 표시됩니다.
            </p>
          )}

          {selectedMatchVideo && (
            <>
              <div style={{ position: "relative", width: "100%" }}>
                <video
                  ref={videoRef}
                  src={originalVideoSourceUrl}
                  controls
                  preload="metadata"
                  onTimeUpdate={handleVideoTimeUpdate}
                  onPause={handleVideoPause}
                  className="video-player"
                  style={{ display: "block", width: "100%" }}
                />

                {shouldShowEditorSections && (
                  <PlayerAnalysisDrawingCanvas
                    drawings={canvasDrawings}
                    currentTimeSec={currentClipTimeSec}
                    isDrawingMode={isDrawingMode}
                    drawingType={drawingForm.drawingType}
                    drawingText={drawingText}
                    onDraftDrawingData={handleDraftDrawingData}
                  />
                )}
              </div>

              <div className="button-row">
                <button
                  type="button"
                  onClick={handleSetClipStartTimeFromCurrentTime}
                >
                  클립 시작 시간 설정
                </button>
                <button
                  type="button"
                  onClick={handleSetClipEndTimeFromCurrentTime}
                >
                  클립 종료 시간 설정
                </button>
                <button type="button" onClick={handleResetClipTime}>
                  클립 시간 초기화
                </button>
                <button type="button" onClick={handlePlaySelectedRange}>
                  선택 구간 재생
                </button>
                <button
                  type="button"
                  onClick={handleSetDrawingStartTimeFromCurrentTime}
                >
                  드로잉 시작 시간 설정
                </button>
                <button
                  type="button"
                  onClick={handleSetDrawingEndTimeFromCurrentTime}
                >
                  드로잉 종료 시간 설정
                </button>
                <button type="button" onClick={handleResetDrawingTime}>
                  드로잉 시간 초기화
                </button>
              </div>

              <div className="detail-box">
                <p>
                  <strong>원본 영상 총 길이:</strong>{" "}
                  {formatSeconds(selectedMatchVideo.durationSec)}
                </p>
                <p>
                  <strong>현재 원본 영상 시간:</strong>{" "}
                  {formatSeconds(currentOriginalTimeSec)}
                </p>
                <p>
                  <strong>현재 클립 기준 시간:</strong>{" "}
                  {formatSeconds(currentClipTimeSec)}
                </p>
                <p>
                  <strong>선택된 클립 구간:</strong> {safeClipStartTimeSec}초 ~{" "}
                  {safeClipEndTimeSec}초
                </p>
                <p>
                  <strong>생성될 클립 길이:</strong> {clipDurationText}
                </p>
                <p>
                  <strong>드로잉 시간 기준:</strong> {drawingTimeGuideText}
                </p>
              </div>
            </>
          )}
        </section>

        {shouldShowEditorSections && (
          <>
            <section className="card">
              <h2>클립 정보</h2>

              {isEditMode && editingClip && (
                <p className="notice-text">
                  수정 대상: {editingClip.title} / 현재 상태:{" "}
                  {editingClip.status}
                </p>
              )}

              <div className="form-grid">
                <label>
                  클립 유형
                  <select
                    value={form.clipType}
                    onChange={(event) =>
                      setForm((prev) => ({
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
                    value={form.title}
                    onChange={(event) =>
                      setForm((prev) => ({
                        ...prev,
                        title: event.target.value,
                      }))
                    }
                    placeholder="예: 압박 전환이 늦은 장면"
                  />
                </label>

                <label>
                  코멘트
                  <textarea
                    value={form.comment}
                    onChange={(event) =>
                      setForm((prev) => ({
                        ...prev,
                        comment: event.target.value,
                      }))
                    }
                    placeholder="선수에게 전달할 분석 코멘트를 입력하세요."
                  />
                </label>

                <label>
                  클립 시작 시간(초)
                  <input type="number" value={safeClipStartTimeSec} readOnly />
                </label>

                <label>
                  클립 종료 시간(초)
                  <input type="number" value={safeClipEndTimeSec} readOnly />
                </label>
              </div>

              <p className="notice-text">
                클립 시작/종료 시간은 상단 원본 영상 아래 버튼으로 설정합니다.
              </p>
            </section>

            <section className="card">
              <h2>드로잉 작성</h2>

              <div className="form-grid">
                <label>
                  드로잉 유형
                  <select
                    value={drawingForm.drawingType}
                    onChange={(event) =>
                      setDrawingForm((prev) => ({
                        ...prev,
                        drawingType: event.target
                          .value as PlayerAnalysisClipDrawingType,
                      }))
                    }
                  >
                    {PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  드로잉 시작 시간(초)
                  <input
                    type="number"
                    value={getSafeNumber(drawingForm.startTimeSec)}
                    readOnly
                  />
                </label>

                <label>
                  드로잉 종료 시간(초)
                  <input
                    type="number"
                    value={getSafeNumber(drawingForm.endTimeSec)}
                    readOnly
                  />
                </label>

                {drawingForm.drawingType === "TEXT" && (
                  <label>
                    텍스트
                    <input
                      type="text"
                      value={drawingText}
                      onChange={(event) => setDrawingText(event.target.value)}
                      placeholder="영상 위에 표시할 텍스트"
                    />
                  </label>
                )}
              </div>

              <div className="button-row">
                <button
                  type="button"
                  onClick={() => {
                    if (
                      !isDrawingMode &&
                      drawingForm.drawingType === "TEXT" &&
                      drawingText.trim() === ""
                    ) {
                      setErrorMessage("텍스트 드로잉 내용을 입력해주세요.");
                      return;
                    }

                    setErrorMessage("");
                    setIsDrawingMode((prev) => !prev);
                  }}
                >
                  {isDrawingMode ? "드로잉 모드 끄기" : "드로잉 모드 켜기"}
                </button>

                <button type="button" onClick={handleAddDraftDrawings}>
                  작성 중 드로잉 전체를 저장 전 목록에 추가
                </button>

                <button type="button" onClick={handleClearDraftDrawings}>
                  작성 중 드로잉 전체 지우기
                </button>
              </div>

              <p className="notice-text">
                드로잉 시작/종료 시간은 상단 원본 영상 아래 버튼으로 설정합니다.
                드로잉 모드를 켠 뒤 영상 위 캔버스에서 여러 개의 드로잉을
                연속으로 작성할 수 있습니다.
              </p>

              <div className="detail-box">
                <p>
                  <strong>작성 중 드로잉 개수:</strong> {draftDrawings.length}개
                </p>
                <p>
                  <strong>현재 설정된 드로잉 시간:</strong>{" "}
                  {getSafeNumber(drawingForm.startTimeSec)}초 ~{" "}
                  {getSafeNumber(drawingForm.endTimeSec)}초
                </p>
              </div>

              {draftDrawings.length > 0 && (
                <ul className="list">
                  {draftDrawings.map((drawing) => (
                    <li key={drawing.localId}>
                      <div className="detail-box">
                        <p>
                          <strong>작성 중 유형:</strong>{" "}
                          {
                            PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_LABELS[
                              drawing.drawingType
                            ]
                          }
                        </p>
                        <p>
                          <strong>적용 예정 시간:</strong>{" "}
                          {drawing.startTimeSec}초 ~ {drawing.endTimeSec}초
                        </p>
                        <button
                          type="button"
                          onClick={() =>
                            handleRemoveDraftDrawing(drawing.localId)
                          }
                        >
                          작성 중 목록에서 제거
                        </button>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </section>

            <section className="card">
              <h2>저장 전 드로잉 목록</h2>

              {drawings.length === 0 ? (
                <p>추가된 드로잉이 없습니다.</p>
              ) : (
                <ul className="list">
                  {drawings.map((drawing) => (
                    <li key={drawing.localId}>
                      <div className="detail-box">
                        <p>
                          <strong>유형:</strong>{" "}
                          {
                            PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_LABELS[
                              drawing.drawingType
                            ]
                          }
                        </p>
                        <p>
                          <strong>시간:</strong> {drawing.startTimeSec}초 ~{" "}
                          {drawing.endTimeSec}초
                        </p>
                        <button
                          type="button"
                          onClick={() => handleRemoveDrawing(drawing.localId)}
                        >
                          목록에서 제거
                        </button>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </section>

            <section className="card">
              <h2>최종 저장</h2>

              <p className="notice-text">
                저장 버튼을 누르면 클립 정보와 저장 전 드로잉 목록이 한 번에
                백엔드로 전송됩니다. 작성 중 드로잉이 남아 있으면 먼저 저장 전
                목록에 추가해야 합니다.
              </p>

              <div className="button-row">
                <button
                  type="submit"
                  disabled={isSubmitting || hasInvalidEditPlayerClipId}
                >
                  {isSubmitting ? "저장 중..." : submitButtonText}
                </button>

                <button type="button" onClick={handleBackToList}>
                  취소
                </button>
              </div>
            </section>
          </>
        )}
      </form>
    </main>
  );
}
