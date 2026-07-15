// 경기 영상 기준 선수 기록 이벤트 등록과 분석 클립 연결을 담당하는 패널 컴포넌트

import { useEffect, useRef, useState } from "react";
import type { FormEvent } from "react";

import {
  createPlayerRecordEvent,
  createPlayerRecordEventWithClipLink,
} from "../../api/playerRecordEventApi";
import {
  getManagementPlayerAnalysisClips,
  getManagementPlayers,
} from "../../api/playerAnalysisClipApi";
import { getTeamAnalysisClips } from "../../api/teamAnalysisClipApi";
import type { MatchVideoDetailResponse } from "../../types/matchVideo";
import type {
  PlayerAnalysisClipListItem,
  PlayerSelectItem,
} from "../../types/playerAnalysisClip";
import type { TeamAnalysisClipListItem } from "../../types/teamAnalysisClip";
import {
  PLAYER_RECORD_EVENT_TYPE_OPTIONS,
  type PlayerRecordClipSourceType,
  type PlayerRecordEventType,
} from "../../types/playerRecordEvent";
import { getApiErrorMessage } from "../../utils/apiError";
import { createVideoSourceUrl } from "../../utils/videoUrl";

type ClipLinkMode = "NONE" | PlayerRecordClipSourceType;

type PlayerRecordEventFormState = {
  playerId: string;
  eventType: PlayerRecordEventType;
  eventStartTimeSec: string;
  eventEndTimeSec: string;
  value: string;
  eventMemo: string;
  clipLinkMode: ClipLinkMode;
  teamClipId: string;
  playerClipId: string;
};

type PlayerRecordEventEditorPanelProps = {
  matchVideo: MatchVideoDetailResponse;
  initialStartTimeSec?: number;
  initialEndTimeSec?: number;
  onSaved?: () => void;
};

const INITIAL_EVENT_FORM_STATE: PlayerRecordEventFormState = {
  playerId: "",
  eventType: "ETC",
  eventStartTimeSec: "0",
  eventEndTimeSec: "1",
  value: "1",
  eventMemo: "",
  clipLinkMode: "NONE",
  teamClipId: "",
  playerClipId: "",
};

function toNumber(value: string) {
  const parsedValue = Number(value);

  return Number.isFinite(parsedValue) ? parsedValue : 0;
}

function formatVideoTime(timeSec: number) {
  if (!Number.isFinite(timeSec) || timeSec < 0) {
    return "00:00";
  }

  const totalSeconds = Math.floor(timeSec);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) {
    return [
      String(hours).padStart(2, "0"),
      String(minutes).padStart(2, "0"),
      String(seconds).padStart(2, "0"),
    ].join(":");
  }

  return [
    String(minutes).padStart(2, "0"),
    String(seconds).padStart(2, "0"),
  ].join(":");
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
): PlayerRecordEventFormState {
  const initialTimeRange = createInitialTimeRange(
    matchVideo,
    initialStartTimeSec,
    initialEndTimeSec,
  );

  return {
    ...INITIAL_EVENT_FORM_STATE,
    eventStartTimeSec: String(initialTimeRange.startTimeSec),
    eventEndTimeSec: String(initialTimeRange.endTimeSec),
  };
}

export default function PlayerRecordEventEditorPanel({
  matchVideo,
  initialStartTimeSec,
  initialEndTimeSec,
  onSaved,
}: PlayerRecordEventEditorPanelProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);

  const [players, setPlayers] = useState<PlayerSelectItem[]>([]);
  const [teamClips, setTeamClips] = useState<TeamAnalysisClipListItem[]>([]);
  const [playerClips, setPlayerClips] = useState<PlayerAnalysisClipListItem[]>(
    [],
  );

  const [eventForm, setEventForm] = useState<PlayerRecordEventFormState>(() =>
    createInitialFormState(matchVideo, initialStartTimeSec, initialEndTimeSec),
  );

  const [currentVideoTimeSec, setCurrentVideoTimeSec] = useState(
    initialStartTimeSec ?? 0,
  );
  const [isPlayerLoading, setIsPlayerLoading] = useState(true);
  const [isClipLoading, setIsClipLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

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

    async function fetchLinkableClips() {
      try {
        setIsClipLoading(true);

        const teamClipResponse = await getTeamAnalysisClips({
          page: 0,
          size: 100,
          matchVideoId: matchVideo.matchVideoId,
        });

        const selectedPlayerId = eventForm.playerId
          ? Number(eventForm.playerId)
          : null;

        const playerClipResponse =
          selectedPlayerId === null
            ? null
            : await getManagementPlayerAnalysisClips({
                page: 0,
                size: 100,
                matchVideoId: matchVideo.matchVideoId,
                playerId: selectedPlayerId,
              });

        if (ignore) {
          return;
        }

        setTeamClips(
          Array.isArray(teamClipResponse.teamAnalysisClips)
            ? teamClipResponse.teamAnalysisClips.filter(
                (clip) => clip.status === "READY",
              )
            : [],
        );

        setPlayerClips(
          playerClipResponse && Array.isArray(playerClipResponse.playerClips)
            ? playerClipResponse.playerClips.filter(
                (clip) => clip.status === "READY",
              )
            : [],
        );
      } catch (error) {
        if (ignore) {
          return;
        }

        setTeamClips([]);
        setPlayerClips([]);
        setErrorMessage(
          `연결 가능한 분석 클립 목록을 불러오지 못했습니다. ${getApiErrorMessage(
            error,
          )}`,
        );
      } finally {
        if (!ignore) {
          setIsClipLoading(false);
        }
      }
    }

    void fetchLinkableClips();

    return () => {
      ignore = true;
    };
  }, [eventForm.playerId, matchVideo.matchVideoId]);

  function handleVideoLoadedMetadata() {
    const video = videoRef.current;

    if (!video) {
      return;
    }

    const requestedTimeSec = toNumber(eventForm.eventStartTimeSec);
    const nextTimeSec =
      Number.isFinite(video.duration) && video.duration > 0
        ? Math.min(Math.max(requestedTimeSec, 0), video.duration)
        : Math.max(requestedTimeSec, 0);

    video.pause();
    video.currentTime = nextTimeSec;
    setCurrentVideoTimeSec(nextTimeSec);
  }

  function handleVideoTimeUpdate() {
    const video = videoRef.current;

    if (!video) {
      return;
    }

    setCurrentVideoTimeSec(Math.max(0, video.currentTime));
  }

  function handleSetEventStartTimeFromCurrentTime() {
    const durationSec = matchVideo.durationSec;
    const currentTimeSec = Math.floor(currentVideoTimeSec);

    setEventForm((currentForm) => {
      const maxStartTimeSec =
        durationSec !== null ? Math.max(0, durationSec - 1) : currentTimeSec;
      const nextStartTimeSec = Math.min(currentTimeSec, maxStartTimeSec);
      const currentEndTimeSec = toNumber(currentForm.eventEndTimeSec);
      const nextEndTimeSec =
        currentEndTimeSec <= nextStartTimeSec
          ? nextStartTimeSec + 1
          : currentEndTimeSec;

      return {
        ...currentForm,
        eventStartTimeSec: String(nextStartTimeSec),
        eventEndTimeSec:
          durationSec !== null
            ? String(Math.min(nextEndTimeSec, durationSec))
            : String(nextEndTimeSec),
      };
    });
  }

  function handleSetEventEndTimeFromCurrentTime() {
    const durationSec = matchVideo.durationSec;
    const currentTimeSec = Math.floor(currentVideoTimeSec);

    setEventForm((currentForm) => {
      const currentStartTimeSec = toNumber(currentForm.eventStartTimeSec);
      const minimumEndTimeSec = currentStartTimeSec + 1;
      const nextEndTimeSec = Math.max(currentTimeSec, minimumEndTimeSec);

      return {
        ...currentForm,
        eventEndTimeSec:
          durationSec !== null
            ? String(Math.min(nextEndTimeSec, durationSec))
            : String(nextEndTimeSec),
      };
    });
  }

  function handleMoveToEventStartTime() {
    const video = videoRef.current;

    if (!video) {
      return;
    }

    const requestedTimeSec = toNumber(eventForm.eventStartTimeSec);
    const nextTimeSec =
      Number.isFinite(video.duration) && video.duration > 0
        ? Math.min(Math.max(requestedTimeSec, 0), video.duration)
        : Math.max(requestedTimeSec, 0);

    video.pause();
    video.currentTime = nextTimeSec;
    setCurrentVideoTimeSec(nextTimeSec);
  }

  function handleChangeEventForm(
    field: keyof PlayerRecordEventFormState,
    value: string,
  ) {
    setEventForm((currentForm) => {
      if (field === "playerId") {
        return {
          ...currentForm,
          playerId: value,
          playerClipId: "",
        };
      }

      if (field === "clipLinkMode") {
        return {
          ...currentForm,
          clipLinkMode: value as ClipLinkMode,
          teamClipId: "",
          playerClipId: "",
        };
      }

      return {
        ...currentForm,
        [field]: value,
      };
    });
  }

  function validateEventForm() {
    if (!eventForm.playerId) {
      return "기록 대상 선수를 선택해주세요.";
    }

    if (matchVideo.durationSec === null || matchVideo.durationSec <= 0) {
      return "경기 영상 길이 정보가 없어 선수 기록 이벤트를 등록할 수 없습니다.";
    }

    const eventStartTimeSec = toNumber(eventForm.eventStartTimeSec);
    const eventEndTimeSec = toNumber(eventForm.eventEndTimeSec);
    const value = toNumber(eventForm.value);

    if (!Number.isInteger(eventStartTimeSec) || eventStartTimeSec < 0) {
      return "이벤트 시작 시간은 0 이상의 정수로 입력해주세요.";
    }

    if (!Number.isInteger(eventEndTimeSec) || eventEndTimeSec <= 0) {
      return "이벤트 종료 시간은 1 이상의 정수로 입력해주세요.";
    }

    if (eventEndTimeSec <= eventStartTimeSec) {
      return "이벤트 종료 시간은 시작 시간보다 커야 합니다.";
    }

    if (eventEndTimeSec > matchVideo.durationSec) {
      return "이벤트 종료 시간이 경기 영상 길이를 초과할 수 없습니다.";
    }

    if (!Number.isInteger(value) || value <= 0 || value > 255) {
      return "기록 수치는 1 이상 255 이하의 정수로 입력해주세요.";
    }

    if (eventForm.clipLinkMode === "TEAM_ANALYSIS" && !eventForm.teamClipId) {
      return "연결할 팀 분석 클립을 선택해주세요.";
    }

    if (
      eventForm.clipLinkMode === "PLAYER_ANALYSIS" &&
      !eventForm.playerClipId
    ) {
      return "연결할 선수 개인 분석 클립을 선택해주세요.";
    }

    return "";
  }

  async function handleCreatePlayerRecordEvent(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    const validationMessage = validateEventForm();

    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");
      setSuccessMessage("");

      const baseRequest = {
        uploadId: matchVideo.matchVideoId,
        playerId: toNumber(eventForm.playerId),
        eventType: eventForm.eventType,
        eventStartTimeSec: toNumber(eventForm.eventStartTimeSec),
        eventEndTimeSec: toNumber(eventForm.eventEndTimeSec),
        value: toNumber(eventForm.value),
        eventMemo: eventForm.eventMemo.trim() || null,
      };

      const response =
        eventForm.clipLinkMode === "NONE"
          ? await createPlayerRecordEvent(baseRequest)
          : await createPlayerRecordEventWithClipLink({
              ...baseRequest,
              clipSourceType: eventForm.clipLinkMode,
              teamClipId:
                eventForm.clipLinkMode === "TEAM_ANALYSIS"
                  ? toNumber(eventForm.teamClipId)
                  : null,
              playerClipId:
                eventForm.clipLinkMode === "PLAYER_ANALYSIS"
                  ? toNumber(eventForm.playerClipId)
                  : null,
            });

      setSuccessMessage(
        `선수 기록 이벤트가 등록되었습니다. 이벤트 ID: ${response.eventId}`,
      );

      const resetForm = createInitialFormState(
        matchVideo,
        initialStartTimeSec,
        initialEndTimeSec,
      );

      setEventForm(resetForm);
      setCurrentVideoTimeSec(toNumber(resetForm.eventStartTimeSec));

      const video = videoRef.current;

      if (video) {
        video.pause();
        video.currentTime = toNumber(resetForm.eventStartTimeSec);
      }

      onSaved?.();
    } catch (error) {
      setErrorMessage(
        `선수 기록 이벤트를 등록하지 못했습니다. ${getApiErrorMessage(error)}`,
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="card">
      <h2>선수 기록 이벤트 등록</h2>

      <p className="helper-text">
        경기 영상을 확인하면서 이벤트 구간을 지정하고, 필요하면 같은 경기의 팀
        분석 클립 또는 같은 선수의 개인 분석 클립을 연결합니다.
      </p>

      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {successMessage && <p className="success-message">{successMessage}</p>}

      <div className="video-canvas-wrap">
        <video
          ref={videoRef}
          key={matchVideo.matchVideoId}
          controls
          width="100%"
          onLoadedMetadata={handleVideoLoadedMetadata}
          onTimeUpdate={handleVideoTimeUpdate}
          onSeeked={handleVideoTimeUpdate}
        >
          <source src={createVideoSourceUrl(matchVideo.url)} type="video/mp4" />
          브라우저에서 video 태그를 지원하지 않습니다.
        </video>
      </div>

      <div className="button-row">
        <button type="button" onClick={handleSetEventStartTimeFromCurrentTime}>
          현재 시간을 이벤트 시작으로 설정
        </button>

        <button type="button" onClick={handleSetEventEndTimeFromCurrentTime}>
          현재 시간을 이벤트 종료로 설정
        </button>

        <button type="button" onClick={handleMoveToEventStartTime}>
          이벤트 시작 시간으로 이동
        </button>
      </div>

      <p className="helper-text">
        현재 영상 시간: {formatVideoTime(currentVideoTimeSec)} (
        {Math.floor(currentVideoTimeSec)}초)
      </p>

      {isPlayerLoading ? (
        <p>선수 목록을 불러오는 중입니다.</p>
      ) : (
        <form className="form-grid" onSubmit={handleCreatePlayerRecordEvent}>
          <label>
            클립 연결 방식
            <select
              value={eventForm.clipLinkMode}
              onChange={(event) =>
                handleChangeEventForm("clipLinkMode", event.target.value)
              }
            >
              <option value="NONE">클립 없이 등록</option>
              <option value="TEAM_ANALYSIS">팀 분석 클립 연결</option>
              <option value="PLAYER_ANALYSIS">선수 개인 분석 클립 연결</option>
            </select>
          </label>

          <label>
            대상 선수
            <select
              value={eventForm.playerId}
              onChange={(event) =>
                handleChangeEventForm("playerId", event.target.value)
              }
              required
            >
              <option value="">선수 선택</option>

              {players.map((player) => (
                <option key={player.playerId} value={player.playerId}>
                  {player.name}
                  {player.uniformNumber !== null
                    ? ` #${player.uniformNumber}`
                    : ""}
                  {player.grade !== null ? ` / ${player.grade}학년` : ""}
                </option>
              ))}
            </select>
          </label>

          {eventForm.clipLinkMode === "TEAM_ANALYSIS" && (
            <fieldset>
              <legend>연결할 팀 분석 클립 선택</legend>

              {isClipLoading && <p>팀 분석 클립 목록을 불러오는 중입니다.</p>}

              {!isClipLoading && teamClips.length === 0 && (
                <p>
                  현재 경기에서 연결할 수 있는 READY 팀 분석 클립이 없습니다.
                </p>
              )}

              {!isClipLoading && teamClips.length > 0 && (
                <ul className="item-list">
                  {teamClips.map((clip) => (
                    <li key={clip.teamClipId}>
                      <label>
                        <input
                          type="radio"
                          name="teamClipId"
                          value={clip.teamClipId}
                          checked={
                            eventForm.teamClipId === String(clip.teamClipId)
                          }
                          onChange={(event) =>
                            handleChangeEventForm(
                              "teamClipId",
                              event.target.value,
                            )
                          }
                        />

                        <strong>{clip.title}</strong>

                        <span>
                          원본 영상 기준 {clip.startTimeSec}초 ~{" "}
                          {clip.endTimeSec}초
                        </span>
                      </label>
                    </li>
                  ))}
                </ul>
              )}
            </fieldset>
          )}

          {eventForm.clipLinkMode === "PLAYER_ANALYSIS" && (
            <fieldset>
              <legend>연결할 선수 개인 분석 클립 선택</legend>

              {!eventForm.playerId && (
                <p>
                  대상 선수를 먼저 선택하면 해당 선수의 READY 개인 분석 클립
                  목록이 표시됩니다.
                </p>
              )}

              {eventForm.playerId && isClipLoading && (
                <p>선수 개인 분석 클립 목록을 불러오는 중입니다.</p>
              )}

              {eventForm.playerId &&
                !isClipLoading &&
                playerClips.length === 0 && (
                  <p>
                    현재 경기와 선택한 선수에 연결할 수 있는 READY 개인 분석
                    클립이 없습니다.
                  </p>
                )}

              {eventForm.playerId &&
                !isClipLoading &&
                playerClips.length > 0 && (
                  <ul className="item-list">
                    {playerClips.map((clip) => (
                      <li key={clip.playerClipId}>
                        <label>
                          <input
                            type="radio"
                            name="playerClipId"
                            value={clip.playerClipId}
                            checked={
                              eventForm.playerClipId ===
                              String(clip.playerClipId)
                            }
                            onChange={(event) =>
                              handleChangeEventForm(
                                "playerClipId",
                                event.target.value,
                              )
                            }
                          />

                          <strong>{clip.title}</strong>

                          <span>대상 선수: {clip.playerName}</span>
                        </label>
                      </li>
                    ))}
                  </ul>
                )}
            </fieldset>
          )}

          <label>
            이벤트 유형
            <select
              value={eventForm.eventType}
              onChange={(event) =>
                handleChangeEventForm(
                  "eventType",
                  event.target.value as PlayerRecordEventType,
                )
              }
            >
              {PLAYER_RECORD_EVENT_TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            이벤트 시작 시간(초)
            <input
              type="number"
              min="0"
              max={matchVideo.durationSec ?? undefined}
              value={eventForm.eventStartTimeSec}
              onChange={(event) =>
                handleChangeEventForm("eventStartTimeSec", event.target.value)
              }
              required
            />
          </label>

          <label>
            이벤트 종료 시간(초)
            <input
              type="number"
              min="1"
              max={matchVideo.durationSec ?? undefined}
              value={eventForm.eventEndTimeSec}
              onChange={(event) =>
                handleChangeEventForm("eventEndTimeSec", event.target.value)
              }
              required
            />
          </label>

          <label>
            기록 수치
            <input
              type="number"
              min="1"
              max="255"
              value={eventForm.value}
              onChange={(event) =>
                handleChangeEventForm("value", event.target.value)
              }
              required
            />
          </label>

          <label>
            이벤트 메모
            <textarea
              value={eventForm.eventMemo}
              onChange={(event) =>
                handleChangeEventForm("eventMemo", event.target.value)
              }
              placeholder="이 장면에 대한 기록 메모"
            />
          </label>

          <div className="button-row">
            <button type="submit" disabled={isSubmitting || isClipLoading}>
              {isSubmitting ? "등록 중..." : "이벤트 등록"}
            </button>
          </div>
        </form>
      )}
    </section>
  );
}
