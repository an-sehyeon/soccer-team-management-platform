// 경기 영상 기준 팀 분석 클립 등록/수정과 드로잉 작성을 담당하는 패널 컴포넌트

import { useEffect, useMemo, useRef, useState } from "react";
import type { FormEvent } from "react";

import {
  createTeamAnalysisClipWithDrawings,
  getTeamAnalysisClipDetail,
  updateTeamAnalysisClipWithDrawings,
} from "../../api/teamAnalysisClipApi";
import { getTeamAnalysisClipDrawings } from "../../api/teamAnalysisClipDrawingApi";
import TeamAnalysisDrawingCanvas from "../TeamAnalysisDrawingCanvas";
import type { MatchVideoDetailResponse } from "../../types/matchVideo";
import {
  TEAM_ANALYSIS_CLIP_TYPE_OPTIONS,
  type TeamAnalysisClipType,
} from "../../types/teamAnalysisClip";
import type {
  CreateTeamAnalysisClipDrawingRequest,
  TeamAnalysisClipDrawingData,
  TeamAnalysisClipDrawingResponse,
  TeamAnalysisClipDrawingType,
} from "../../types/teamAnalysisClipDrawing";
import {
  TEAM_ANALYSIS_CLIP_DRAWING_TYPE_LABELS,
  TEAM_ANALYSIS_CLIP_DRAWING_TYPE_OPTIONS,
} from "../../types/teamAnalysisClipDrawing";
import { getApiErrorMessage } from "../../utils/apiError";
import { createVideoSourceUrl } from "../../utils/videoUrl";

type TeamAnalysisClipEditorPanelProps = {
  mode: "create" | "edit";
  matchVideo: MatchVideoDetailResponse;
  teamClipId?: number;
  onSaved?: () => void;
};

type TeamClipFormState = {
  clipType: TeamAnalysisClipType;
  title: string;
  comment: string;
  startTimeSec: string;
  endTimeSec: string;
};

type DrawingFormState = {
  drawingType: TeamAnalysisClipDrawingType;
  startTimeSec: string;
  endTimeSec: string;
  drawingText: string;
};

type EditorDrawing = CreateTeamAnalysisClipDrawingRequest & {
  localId: string;
};

const INITIAL_FORM_STATE: TeamClipFormState = {
  clipType: "HIGHLIGHT",
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

function createInitialFormState(
  matchVideo: MatchVideoDetailResponse,
): TeamClipFormState {
  return {
    ...INITIAL_FORM_STATE,
    endTimeSec:
      matchVideo.durationSec !== null && matchVideo.durationSec > 1 ? "1" : "0",
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

function isValidDrawingData(drawingData: TeamAnalysisClipDrawingData) {
  return (
    drawingData !== null &&
    typeof drawingData === "object" &&
    !Array.isArray(drawingData) &&
    Object.keys(drawingData).length > 0
  );
}

function convertEditorDrawingsToCanvasDrawings(
  drawings: EditorDrawing[],
  teamClipId: number,
): TeamAnalysisClipDrawingResponse[] {
  return drawings.map((drawing, index) => ({
    drawingId: -1 * (index + 1),
    teamClipId,
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
  drawing: TeamAnalysisClipDrawingResponse,
): EditorDrawing {
  return {
    localId: `server-${drawing.drawingId}`,
    drawingType: drawing.drawingType,
    startTimeSec: drawing.startTimeSec,
    endTimeSec: drawing.endTimeSec,
    drawingData: drawing.drawingData,
  };
}

export default function TeamAnalysisClipEditorPanel({
  mode,
  matchVideo,
  teamClipId,
  onSaved,
}: TeamAnalysisClipEditorPanelProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);

  const [form, setForm] = useState<TeamClipFormState>(() =>
    createInitialFormState(matchVideo),
  );
  const [drawingForm, setDrawingForm] = useState<DrawingFormState>(() =>
    createInitialDrawingFormState(matchVideo),
  );
  const [drawings, setDrawings] = useState<EditorDrawing[]>([]);

  const [isDrawingMode, setIsDrawingMode] = useState(false);
  const [currentOriginalTimeSec, setCurrentOriginalTimeSec] = useState(0);
  const [currentClipTimeSec, setCurrentClipTimeSec] = useState(0);
  const [isPlayingSelectedRange, setIsPlayingSelectedRange] = useState(false);

  const [isEditDataLoading, setIsEditDataLoading] = useState(mode === "edit");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const clipStartTimeSec = toNumber(form.startTimeSec);
  const clipEndTimeSec = toNumber(form.endTimeSec);
  const clipDurationSec =
    clipEndTimeSec > clipStartTimeSec ? clipEndTimeSec - clipStartTimeSec : 0;

  const canvasDrawings = useMemo(
    () => convertEditorDrawingsToCanvasDrawings(drawings, teamClipId ?? 0),
    [drawings, teamClipId],
  );

  const hasInvalidEditState = mode === "edit" && teamClipId === undefined;

  useEffect(() => {
    let ignore = false;

    async function fetchEditData() {
      if (mode !== "edit" || teamClipId === undefined) {
        return;
      }

      try {
        const [clipDetail, drawingList] = await Promise.all([
          getTeamAnalysisClipDetail(teamClipId),
          getTeamAnalysisClipDrawings(teamClipId),
        ]);

        if (ignore) {
          return;
        }

        if (clipDetail.matchVideoId !== matchVideo.matchVideoId) {
          setErrorMessage(
            "선택한 경기 영상과 수정하려는 팀 분석 클립의 원본 영상이 다릅니다.",
          );
          setIsEditDataLoading(false);
          return;
        }

        setForm({
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
          `팀 분석 클립 수정 정보를 불러오지 못했습니다. ${getApiErrorMessage(
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
  }, [matchVideo.matchVideoId, mode, teamClipId]);

  useEffect(() => {
    const video = videoRef.current;

    if (!video || clipStartTimeSec < 0) {
      return;
    }

    function moveToClipStart() {
      video!.currentTime = clipStartTimeSec;
      setCurrentOriginalTimeSec(clipStartTimeSec);
      setCurrentClipTimeSec(0);
    }

    video.addEventListener("loadedmetadata", moveToClipStart);

    return () => {
      video.removeEventListener("loadedmetadata", moveToClipStart);
    };
  }, [clipStartTimeSec, matchVideo.url]);

  function handleChangeForm(field: keyof TeamClipFormState, value: string) {
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
      return "수정할 팀 분석 클립 ID가 올바르지 않습니다.";
    }

    if (form.title.trim() === "") {
      return "팀 분석 클립 제목을 입력해주세요.";
    }

    if (matchVideo.durationSec === null || matchVideo.durationSec <= 0) {
      return "경기 영상 길이 정보가 없어 팀 분석 클립을 저장할 수 없습니다.";
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
      return "드로잉 종료 시간은 생성될 클립 길이를 초과할 수 없습니다.";
    }

    return "";
  }

  function handleDraftDrawingData(drawingData: TeamAnalysisClipDrawingData) {
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
        mode === "edit" && teamClipId !== undefined
          ? await updateTeamAnalysisClipWithDrawings(teamClipId, request)
          : await createTeamAnalysisClipWithDrawings({
              matchVideoId: matchVideo.matchVideoId,
              ...request,
            });

      setSuccessMessage(response.message);

      if (mode === "create") {
        setForm(createInitialFormState(matchVideo));
        setDrawingForm(createInitialDrawingFormState(matchVideo));
        setDrawings([]);
        setIsDrawingMode(false);
      }

      onSaved?.();
    } catch (error) {
      setErrorMessage(
        mode === "edit"
          ? `팀 분석 클립 수정에 실패했습니다. ${getApiErrorMessage(error)}`
          : `팀 분석 클립 생성에 실패했습니다. ${getApiErrorMessage(error)}`,
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="card">
      <h2>{mode === "edit" ? "팀 분석 클립 수정" : "팀 분석 클립 등록"}</h2>
      <p className="helper-text">
        선택한 경기 영상을 기준으로 팀 분석 클립 구간을 설정하고 드로잉을
        작성합니다.
      </p>

      {isEditDataLoading && <p>팀 분석 클립 수정 정보를 불러오는 중입니다.</p>}
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

        <TeamAnalysisDrawingCanvas
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
          클립 유형
          <select
            value={form.clipType}
            onChange={(event) =>
              handleChangeForm(
                "clipType",
                event.target.value as TeamAnalysisClipType,
              )
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
            value={form.title}
            onChange={(event) => handleChangeForm("title", event.target.value)}
            placeholder="예: 전방 압박 성공 장면"
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
            placeholder="클립에 대한 분석 코멘트"
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
                event.target.value as TeamAnalysisClipDrawingType,
              )
            }
          >
            {TEAM_ANALYSIS_CLIP_DRAWING_TYPE_OPTIONS.map((option) => (
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
                        TEAM_ANALYSIS_CLIP_DRAWING_TYPE_LABELS[
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
              ? "팀 분석 클립 수정 요청"
              : "팀 분석 클립 생성 요청"}
          </button>
        </div>
      </form>
    </section>
  );
}
