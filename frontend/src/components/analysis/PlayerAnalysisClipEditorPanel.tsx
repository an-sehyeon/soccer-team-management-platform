// 경기 영상 기준 선수 개인 분석 클립 등록/수정과 드로잉 작성을 담당하는 패널 컴포넌트

import { useEffect, useMemo, useRef, useState } from "react";
import type { FormEvent } from "react";

import {
  createPlayerAnalysisClipWithDrawings,
  getManagementPlayerAnalysisClipDetail,
  getManagementPlayers,
  updatePlayerAnalysisClipWithDrawings,
} from "../../api/playerAnalysisClipApi";
import { getPlayerAnalysisClipDrawings } from "../../api/playerAnalysisClipDrawingApi";
import PlayerAnalysisDrawingCanvas from "../PlayerAnalysisDrawingCanvas";
import type { MatchVideoDetailResponse } from "../../types/matchVideo";
import {
  PLAYER_ANALYSIS_CLIP_TYPE_OPTIONS,
  type CreatePlayerAnalysisClipDrawingItemRequest,
  type PlayerAnalysisClipType,
  type PlayerSelectItem,
} from "../../types/playerAnalysisClip";
import type {
  PlayerAnalysisClipDrawingData,
  PlayerAnalysisClipDrawingResponse,
  PlayerAnalysisClipDrawingType,
} from "../../types/playerAnalysisClipDrawing";
import {
  PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_LABELS,
  PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_OPTIONS,
} from "../../types/playerAnalysisClipDrawing";
import { getApiErrorMessage } from "../../utils/apiError";
import { createVideoSourceUrl } from "../../utils/videoUrl";

type PlayerAnalysisClipEditorPanelProps = {
  mode: "create" | "edit";
  matchVideo: MatchVideoDetailResponse;
  playerClipId?: number;
  initialStartTimeSec?: number;
  initialEndTimeSec?: number;
  onSaved?: () => void;
};

type PlayerClipFormState = {
  playerId: string;
  clipType: PlayerAnalysisClipType;
  title: string;
  comment: string;
  startTimeSec: string;
  endTimeSec: string;
};

type DrawingFormState = {
  drawingType: PlayerAnalysisClipDrawingType;
  startTimeSec: string;
  endTimeSec: string;
  drawingText: string;
};

type EditorDrawing = CreatePlayerAnalysisClipDrawingItemRequest & {
  localId: string;
};

const INITIAL_FORM_STATE: PlayerClipFormState = {
  playerId: "",
  clipType: "PLAYER_GOOD",
  title: "",
  comment: "",
  startTimeSec: "0",
  endTimeSec: "1",
};

const INITIAL_DRAWING_FORM_STATE: DrawingFormState = {
  drawingType: "ARROW",
  startTimeSec: "0",
  endTimeSec: "1",
  drawingText: "",
};

function toNumber(value: string) {
  const parsedValue = Number(value);

  return Number.isFinite(parsedValue) ? parsedValue : 0;
}

function createLocalId() {
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function createInitialTimeRange(
  matchVideo: MatchVideoDetailResponse,
  initialStartTimeSec?: number,
  initialEndTimeSec?: number,
) {
  const durationSec = matchVideo.durationSec;

  if (durationSec === null || durationSec <= 0) {
    return {
      startTimeSec: 0,
      endTimeSec: 0,
    };
  }

  const requestedStartTimeSec =
    initialStartTimeSec !== undefined && Number.isInteger(initialStartTimeSec)
      ? initialStartTimeSec
      : 0;

  const safeStartTimeSec = Math.min(
    Math.max(requestedStartTimeSec, 0),
    Math.max(durationSec - 1, 0),
  );

  const requestedEndTimeSec =
    initialEndTimeSec !== undefined && Number.isInteger(initialEndTimeSec)
      ? initialEndTimeSec
      : safeStartTimeSec + 1;

  const safeEndTimeSec = Math.min(
    Math.max(requestedEndTimeSec, safeStartTimeSec + 1),
    durationSec,
  );

  return {
    startTimeSec: safeStartTimeSec,
    endTimeSec: safeEndTimeSec,
  };
}

function createInitialFormState(
  matchVideo: MatchVideoDetailResponse,
  initialStartTimeSec?: number,
  initialEndTimeSec?: number,
): PlayerClipFormState {
  const initialTimeRange = createInitialTimeRange(
    matchVideo,
    initialStartTimeSec,
    initialEndTimeSec,
  );

  return {
    ...INITIAL_FORM_STATE,
    startTimeSec: String(initialTimeRange.startTimeSec),
    endTimeSec: String(initialTimeRange.endTimeSec),
  };
}

function createInitialDrawingFormState(
  matchVideo: MatchVideoDetailResponse,
): DrawingFormState {
  return {
    ...INITIAL_DRAWING_FORM_STATE,
    endTimeSec:
      matchVideo.durationSec !== null && matchVideo.durationSec > 1 ? "1" : "0",
  };
}

function isValidDrawingData(drawingData: PlayerAnalysisClipDrawingData) {
  return (
    drawingData !== null &&
    typeof drawingData === "object" &&
    !Array.isArray(drawingData) &&
    Object.keys(drawingData).length > 0
  );
}

function createPlayerLabel(player: PlayerSelectItem) {
  const uniformNumberText =
    player.uniformNumber === null ? "등번호 없음" : `${player.uniformNumber}번`;
  const gradeText = player.grade === null ? "학년 없음" : `${player.grade}학년`;

  return `${uniformNumberText} · ${player.name} · ${gradeText}`;
}

function convertEditorDrawingsToCanvasDrawings(
  drawings: EditorDrawing[],
  playerClipId: number,
): PlayerAnalysisClipDrawingResponse[] {
  return drawings.map((drawing, index) => ({
    drawingId: -1 * (index + 1),
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

function convertSavedDrawingToEditorDrawing(
  drawing: PlayerAnalysisClipDrawingResponse,
): EditorDrawing {
  return {
    localId: `server-${drawing.drawingId}`,
    drawingType: drawing.drawingType,
    startTimeSec: drawing.startTimeSec,
    endTimeSec: drawing.endTimeSec,
    drawingData: drawing.drawingData,
  };
}

export default function PlayerAnalysisClipEditorPanel({
  mode,
  matchVideo,
  playerClipId,
  initialStartTimeSec,
  initialEndTimeSec,
  onSaved,
}: PlayerAnalysisClipEditorPanelProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);

  const [players, setPlayers] = useState<PlayerSelectItem[]>([]);
  const [form, setForm] = useState<PlayerClipFormState>(() =>
    createInitialFormState(
      matchVideo,
      mode === "create" ? initialStartTimeSec : undefined,
      mode === "create" ? initialEndTimeSec : undefined,
    ),
  );
  const [drawingForm, setDrawingForm] = useState<DrawingFormState>(() =>
    createInitialDrawingFormState(matchVideo),
  );
  const [drawings, setDrawings] = useState<EditorDrawing[]>([]);

  const [isDrawingMode, setIsDrawingMode] = useState(false);
  const [currentOriginalTimeSec, setCurrentOriginalTimeSec] = useState(0);
  const [currentClipTimeSec, setCurrentClipTimeSec] = useState(0);
  const [isPlayingSelectedRange, setIsPlayingSelectedRange] = useState(false);

  const [isPlayerLoading, setIsPlayerLoading] = useState(true);
  const [isEditDataLoading, setIsEditDataLoading] = useState(
    mode === "edit" && playerClipId !== undefined,
  );
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const clipStartTimeSec = toNumber(form.startTimeSec);
  const clipEndTimeSec = toNumber(form.endTimeSec);
  const clipDurationSec =
    clipEndTimeSec > clipStartTimeSec ? clipEndTimeSec - clipStartTimeSec : 0;

  const canvasDrawings = useMemo(
    () => convertEditorDrawingsToCanvasDrawings(drawings, playerClipId ?? 0),
    [drawings, playerClipId],
  );

  const hasInvalidEditState = mode === "edit" && playerClipId === undefined;

  useEffect(() => {
    let ignore = false;

    async function fetchPlayers() {
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

        setErrorMessage(
          `선수 목록을 불러오지 못했습니다. ${getApiErrorMessage(error)}`,
        );
      } finally {
        if (!ignore) {
          setIsPlayerLoading(false);
        }
      }
    }

    void fetchPlayers();

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    let ignore = false;

    async function fetchEditData() {
      if (mode !== "edit" || playerClipId === undefined) {
        return;
      }

      try {
        const [clipDetail, drawingList] = await Promise.all([
          getManagementPlayerAnalysisClipDetail(playerClipId),
          getPlayerAnalysisClipDrawings(playerClipId),
        ]);

        if (ignore) {
          return;
        }

        if (clipDetail.matchVideoId !== matchVideo.matchVideoId) {
          setErrorMessage(
            "선택한 경기 영상과 수정하려는 선수 개인 분석 클립의 원본 영상이 다릅니다.",
          );
          setIsEditDataLoading(false);
          return;
        }

        setForm({
          playerId: String(clipDetail.playerId),
          clipType: clipDetail.clipType,
          title: clipDetail.title,
          comment: clipDetail.comment ?? "",
          startTimeSec: String(clipDetail.startTimeSec),
          endTimeSec: String(clipDetail.endTimeSec),
        });

        setDrawingForm({
          drawingType: "ARROW",
          startTimeSec: "0",
          endTimeSec:
            clipDetail.endTimeSec > clipDetail.startTimeSec ? "1" : "0",
          drawingText: "",
        });

        setDrawings(
          Array.isArray(drawingList)
            ? drawingList.map(convertSavedDrawingToEditorDrawing)
            : [],
        );

        setErrorMessage("");
      } catch (error) {
        if (ignore) {
          return;
        }

        setErrorMessage(
          `선수 개인 분석 클립 수정 정보를 불러오지 못했습니다. ${getApiErrorMessage(
            error,
          )}`,
        );
      } finally {
        if (!ignore) {
          setIsEditDataLoading(false);
        }
      }
    }

    void fetchEditData();

    return () => {
      ignore = true;
    };
  }, [matchVideo.matchVideoId, mode, playerClipId]);

  useEffect(() => {
    const video = videoRef.current;

    if (!video || clipStartTimeSec < 0) {
      return;
    }

    function moveToClipStart() {
      const v = videoRef.current;
      if (!v) return;
      v.currentTime = clipStartTimeSec;
      setCurrentOriginalTimeSec(clipStartTimeSec);
      setCurrentClipTimeSec(0);
    }

    video.addEventListener("loadedmetadata", moveToClipStart);

    return () => {
      video.removeEventListener("loadedmetadata", moveToClipStart);
    };
  }, [clipStartTimeSec, matchVideo.url]);

  function handleChangeForm(field: keyof PlayerClipFormState, value: string) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }));
  }

  function handleChangeDrawingForm(
    field: keyof DrawingFormState,
    value: string,
  ) {
    setDrawingForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }));
  }

  function getCurrentOriginalTimeFromVideo() {
    const video = videoRef.current;

    if (!video) {
      return currentOriginalTimeSec;
    }

    return Math.max(0, video.currentTime);
  }

  function getCurrentClipTimeFromVideo() {
    const relativeTimeSec =
      getCurrentOriginalTimeFromVideo() - clipStartTimeSec;

    return Math.min(Math.max(relativeTimeSec, 0), clipDurationSec);
  }

  function handleVideoTimeUpdate() {
    const video = videoRef.current;

    if (!video) {
      return;
    }

    const originalTimeSec = Math.max(0, video.currentTime);
    const relativeTimeSec = Math.max(0, originalTimeSec - clipStartTimeSec);
    const safeRelativeTimeSec = Math.min(relativeTimeSec, clipDurationSec);

    setCurrentOriginalTimeSec(originalTimeSec);
    setCurrentClipTimeSec(safeRelativeTimeSec);

    if (isPlayingSelectedRange && originalTimeSec >= clipEndTimeSec) {
      video.pause();
      setIsPlayingSelectedRange(false);
    }
  }

  function handleVideoPause() {
    setIsPlayingSelectedRange(false);
  }

  function handleSetClipStartTimeFromCurrentTime() {
    const currentTimeSec = Math.floor(getCurrentOriginalTimeFromVideo());
    const durationSec = matchVideo.durationSec;

    setForm((currentForm) => {
      const maxStartTimeSec =
        durationSec !== null ? Math.max(0, durationSec - 1) : currentTimeSec;
      const nextStartTimeSec = Math.min(currentTimeSec, maxStartTimeSec);
      const currentEndTimeSec = toNumber(currentForm.endTimeSec);
      const nextEndTimeSec =
        currentEndTimeSec <= nextStartTimeSec
          ? nextStartTimeSec + 1
          : currentEndTimeSec;

      return {
        ...currentForm,
        startTimeSec: String(nextStartTimeSec),
        endTimeSec:
          durationSec !== null
            ? String(Math.min(nextEndTimeSec, durationSec))
            : String(nextEndTimeSec),
      };
    });
  }

  function handleSetClipEndTimeFromCurrentTime() {
    const currentTimeSec = Math.floor(getCurrentOriginalTimeFromVideo());
    const durationSec = matchVideo.durationSec;

    setForm((currentForm) => {
      const currentStartTimeSec = toNumber(currentForm.startTimeSec);
      const minimumEndTimeSec = currentStartTimeSec + 1;
      const rawEndTimeSec = Math.max(currentTimeSec, minimumEndTimeSec);

      return {
        ...currentForm,
        endTimeSec:
          durationSec !== null
            ? String(Math.min(rawEndTimeSec, durationSec))
            : String(rawEndTimeSec),
      };
    });
  }

  function handlePlaySelectedRange() {
    const video = videoRef.current;

    if (!video || clipDurationSec <= 0) {
      return;
    }

    video.currentTime = clipStartTimeSec;
    setCurrentOriginalTimeSec(clipStartTimeSec);
    setCurrentClipTimeSec(0);
    setIsPlayingSelectedRange(true);
    void video.play();
  }

  function handleSetDrawingStartTimeFromCurrentTime() {
    setDrawingForm((currentForm) => ({
      ...currentForm,
      startTimeSec: String(Math.floor(getCurrentClipTimeFromVideo())),
    }));
  }

  function handleSetDrawingEndTimeFromCurrentTime() {
    setDrawingForm((currentForm) => ({
      ...currentForm,
      endTimeSec: String(Math.floor(getCurrentClipTimeFromVideo())),
    }));
  }

  function validateClipForm() {
    if (hasInvalidEditState) {
      return "수정할 선수 개인 분석 클립 ID가 올바르지 않습니다.";
    }

    if (!form.playerId) {
      return "대상 선수를 선택해주세요.";
    }

    if (form.title.trim() === "") {
      return "선수 개인 분석 클립 제목을 입력해주세요.";
    }

    if (matchVideo.durationSec === null || matchVideo.durationSec <= 0) {
      return "경기 영상 길이 정보가 없어 선수 개인 분석 클립을 저장할 수 없습니다.";
    }

    if (clipStartTimeSec < 0 || clipEndTimeSec < 0) {
      return "클립 시작/종료 시간은 0 이상이어야 합니다.";
    }

    if (clipEndTimeSec <= clipStartTimeSec) {
      return "클립 종료 시간은 시작 시간보다 커야 합니다.";
    }

    if (clipEndTimeSec > matchVideo.durationSec) {
      return "클립 종료 시간이 경기 영상 길이를 초과할 수 없습니다.";
    }

    return "";
  }

  function validateDrawingTime() {
    const drawingStartTimeSec = toNumber(drawingForm.startTimeSec);
    const drawingEndTimeSec = toNumber(drawingForm.endTimeSec);

    if (clipDurationSec <= 0) {
      return "먼저 올바른 클립 시작/종료 시간을 설정해주세요.";
    }

    if (drawingStartTimeSec < 0 || drawingEndTimeSec < 0) {
      return "드로잉 시작/종료 시간은 0 이상이어야 합니다.";
    }

    if (drawingEndTimeSec <= drawingStartTimeSec) {
      return "드로잉 종료 시간은 시작 시간보다 커야 합니다.";
    }

    if (drawingEndTimeSec > clipDurationSec) {
      return "드로잉 종료 시간은 생성될 선수 개인 분석 클립 길이를 초과할 수 없습니다.";
    }

    return "";
  }

  function handleDraftDrawingData(drawingData: PlayerAnalysisClipDrawingData) {
    const validationMessage = validateDrawingTime();

    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    if (!isValidDrawingData(drawingData)) {
      setErrorMessage("드로잉 데이터가 올바르지 않습니다.");
      return;
    }

    setDrawings((currentDrawings) => [
      ...currentDrawings,
      {
        localId: createLocalId(),
        drawingType: drawingForm.drawingType,
        startTimeSec: toNumber(drawingForm.startTimeSec),
        endTimeSec: toNumber(drawingForm.endTimeSec),
        drawingData,
      },
    ]);
    setErrorMessage("");
  }

  function handleRemoveDrawing(localId: string) {
    setDrawings((currentDrawings) =>
      currentDrawings.filter((drawing) => drawing.localId !== localId),
    );
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const validationMessage = validateClipForm();

    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");
      setSuccessMessage("");

      const request = {
        playerId: toNumber(form.playerId),
        clipType: form.clipType,
        title: form.title.trim(),
        comment: form.comment.trim() || null,
        startTimeSec: clipStartTimeSec,
        endTimeSec: clipEndTimeSec,
        drawings: drawings.map((drawing) => ({
          drawingType: drawing.drawingType,
          startTimeSec: drawing.startTimeSec,
          endTimeSec: drawing.endTimeSec,
          drawingData: drawing.drawingData,
        })),
      };

      const response =
        mode === "edit" && playerClipId !== undefined
          ? await updatePlayerAnalysisClipWithDrawings(playerClipId, request)
          : await createPlayerAnalysisClipWithDrawings({
              matchVideoId: matchVideo.matchVideoId,
              ...request,
            });

      setSuccessMessage(response.message);

      if (mode === "create") {
        setForm(
          createInitialFormState(
            matchVideo,
            initialStartTimeSec,
            initialEndTimeSec,
          ),
        );
        setDrawingForm(createInitialDrawingFormState(matchVideo));
        setDrawings([]);
        setIsDrawingMode(false);
      }

      onSaved?.();
    } catch (error) {
      setErrorMessage(
        mode === "edit"
          ? `선수 개인 분석 클립 수정에 실패했습니다. ${getApiErrorMessage(
              error,
            )}`
          : `선수 개인 분석 클립 생성에 실패했습니다. ${getApiErrorMessage(
              error,
            )}`,
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="card">
      <h2>
        {mode === "edit"
          ? "선수 개인 분석 클립 수정"
          : "선수 개인 분석 클립 등록"}
      </h2>
      <p className="helper-text">
        선택한 경기 영상을 기준으로 대상 선수의 개인 분석 클립 구간을 설정하고
        드로잉을 작성합니다.
      </p>

      {isEditDataLoading && (
        <p>선수 개인 분석 클립 수정 정보를 불러오는 중입니다.</p>
      )}
      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {successMessage && <p className="success-message">{successMessage}</p>}

      <div className="video-canvas-wrap">
        <video
          ref={videoRef}
          key={matchVideo.matchVideoId}
          controls
          width="100%"
          onTimeUpdate={handleVideoTimeUpdate}
          onPause={handleVideoPause}
        >
          <source src={createVideoSourceUrl(matchVideo.url)} type="video/mp4" />
          브라우저에서 video 태그를 지원하지 않습니다.
        </video>

        <PlayerAnalysisDrawingCanvas
          drawings={canvasDrawings}
          currentTimeSec={currentClipTimeSec}
          isDrawingMode={isDrawingMode}
          drawingType={drawingForm.drawingType}
          drawingText={drawingForm.drawingText}
          onDraftDrawingData={handleDraftDrawingData}
        />
      </div>

      <div className="button-row">
        <button type="button" onClick={handleSetClipStartTimeFromCurrentTime}>
          현재 시간을 클립 시작으로 설정
        </button>
        <button type="button" onClick={handleSetClipEndTimeFromCurrentTime}>
          현재 시간을 클립 종료로 설정
        </button>
        <button type="button" onClick={handlePlaySelectedRange}>
          선택 구간 재생
        </button>
      </div>

      <p className="helper-text">
        현재 원본 영상 시간: {Math.floor(currentOriginalTimeSec)}초 / 현재 클립
        기준 시간: {Math.floor(currentClipTimeSec)}초
      </p>

      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          대상 선수
          <select
            value={form.playerId}
            onChange={(event) =>
              handleChangeForm("playerId", event.target.value)
            }
            required
          >
            <option value="">선수 선택</option>
            {isPlayerLoading ? (
              <option value="" disabled>
                선수 목록 불러오는 중
              </option>
            ) : (
              players.map((player) => (
                <option key={player.playerId} value={player.playerId}>
                  {createPlayerLabel(player)}
                </option>
              ))
            )}
          </select>
        </label>

        <label>
          클립 유형
          <select
            value={form.clipType}
            onChange={(event) =>
              handleChangeForm(
                "clipType",
                event.target.value as PlayerAnalysisClipType,
              )
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
            onChange={(event) => handleChangeForm("title", event.target.value)}
            placeholder="예: 전환 수비 위치 수정"
            required
          />
        </label>

        <label>
          코멘트
          <textarea
            value={form.comment}
            onChange={(event) =>
              handleChangeForm("comment", event.target.value)
            }
            placeholder="선수에게 전달할 분석 코멘트"
          />
        </label>

        <label>
          클립 시작 시간(초)
          <input
            type="number"
            min="0"
            max={matchVideo.durationSec ?? undefined}
            value={form.startTimeSec}
            onChange={(event) =>
              handleChangeForm("startTimeSec", event.target.value)
            }
            required
          />
        </label>

        <label>
          클립 종료 시간(초)
          <input
            type="number"
            min="1"
            max={matchVideo.durationSec ?? undefined}
            value={form.endTimeSec}
            onChange={(event) =>
              handleChangeForm("endTimeSec", event.target.value)
            }
            required
          />
        </label>

        <label>
          드로잉 유형
          <select
            value={drawingForm.drawingType}
            onChange={(event) =>
              handleChangeDrawingForm(
                "drawingType",
                event.target.value as PlayerAnalysisClipDrawingType,
              )
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
            min="0"
            value={drawingForm.startTimeSec}
            onChange={(event) =>
              handleChangeDrawingForm("startTimeSec", event.target.value)
            }
          />
        </label>

        <label>
          드로잉 종료 시간(초)
          <input
            type="number"
            min="1"
            value={drawingForm.endTimeSec}
            onChange={(event) =>
              handleChangeDrawingForm("endTimeSec", event.target.value)
            }
          />
        </label>

        <label>
          텍스트 드로잉 문구
          <input
            type="text"
            value={drawingForm.drawingText}
            onChange={(event) =>
              handleChangeDrawingForm("drawingText", event.target.value)
            }
            placeholder="TEXT 유형에서 사용할 문구"
          />
        </label>

        <div className="button-row">
          <button
            type="button"
            onClick={handleSetDrawingStartTimeFromCurrentTime}
          >
            현재 시간을 드로잉 시작으로 설정
          </button>
          <button
            type="button"
            onClick={handleSetDrawingEndTimeFromCurrentTime}
          >
            현재 시간을 드로잉 종료로 설정
          </button>
          <button
            type="button"
            onClick={() => setIsDrawingMode((currentValue) => !currentValue)}
          >
            {isDrawingMode ? "드로잉 모드 끄기" : "드로잉 모드 켜기"}
          </button>
        </div>

        {drawings.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>유형</th>
                  <th>시간</th>
                  <th>삭제</th>
                </tr>
              </thead>
              <tbody>
                {drawings.map((drawing) => (
                  <tr key={drawing.localId}>
                    <td>
                      {
                        PLAYER_ANALYSIS_CLIP_DRAWING_TYPE_LABELS[
                          drawing.drawingType
                        ]
                      }
                    </td>
                    <td>
                      {drawing.startTimeSec}초 ~ {drawing.endTimeSec}초
                    </td>
                    <td>
                      <button
                        type="button"
                        onClick={() => handleRemoveDrawing(drawing.localId)}
                      >
                        삭제
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="button-row">
          <button
            type="submit"
            disabled={isSubmitting || isEditDataLoading || hasInvalidEditState}
          >
            {mode === "edit"
              ? "선수 개인 분석 클립 수정 요청"
              : "선수 개인 분석 클립 생성 요청"}
          </button>
        </div>
      </form>
    </section>
  );
}
