// 경기 영상 기준 선수 기록 이벤트 등록과 분석 클립 연결을 담당하는 패널 컴포넌트

import { useEffect, useState } from "react";
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

function createInitialFormState(
  matchVideo: MatchVideoDetailResponse,
): PlayerRecordEventFormState {
  return {
    ...INITIAL_EVENT_FORM_STATE,
    eventEndTimeSec:
      matchVideo.durationSec !== null && matchVideo.durationSec > 1 ? "1" : "0",
  };
}

export default function PlayerRecordEventEditorPanel({
  matchVideo,
  onSaved,
}: PlayerRecordEventEditorPanelProps) {
  const [players, setPlayers] = useState<PlayerSelectItem[]>([]);
  const [teamClips, setTeamClips] = useState<TeamAnalysisClipListItem[]>([]);
  const [playerClips, setPlayerClips] = useState<PlayerAnalysisClipListItem[]>(
    [],
  );

  const [eventForm, setEventForm] = useState<PlayerRecordEventFormState>(() =>
    createInitialFormState(matchVideo),
  );

  const [isPlayerLoading, setIsPlayerLoading] = useState(true);
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

        setPlayers(response);
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

    fetchPlayers();

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    let ignore = false;

    async function fetchLinkableClips() {
      try {
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
          teamClipResponse.teamAnalysisClips.filter(
            (clip) => clip.status === "READY",
          ),
        );

        setPlayerClips(
          playerClipResponse
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
      }
    }

    fetchLinkableClips();

    return () => {
      ignore = true;
    };
  }, [eventForm.playerId, matchVideo.matchVideoId]);

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
      setEventForm(createInitialFormState(matchVideo));
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
        선택한 경기 영상 기준으로 선수 기록 이벤트를 등록하고, 필요하면 팀 분석
        클립 또는 선수 개인 분석 클립과 연결합니다.
      </p>

      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {successMessage && <p className="success-message">{successMessage}</p>}

      {isPlayerLoading ? (
        <p>선수 목록을 불러오는 중입니다.</p>
      ) : (
        <form className="form-grid" onSubmit={handleCreatePlayerRecordEvent}>
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

          {eventForm.clipLinkMode === "TEAM_ANALYSIS" && (
            <label>
              팀 분석 클립
              <select
                value={eventForm.teamClipId}
                onChange={(event) =>
                  handleChangeEventForm("teamClipId", event.target.value)
                }
                required
              >
                <option value="">팀 분석 클립 선택</option>
                {teamClips.map((clip) => (
                  <option key={clip.teamClipId} value={clip.teamClipId}>
                    {clip.title} / {clip.startTimeSec}초 ~ {clip.endTimeSec}초
                  </option>
                ))}
              </select>
            </label>
          )}

          {eventForm.clipLinkMode === "PLAYER_ANALYSIS" && (
            <label>
              선수 개인 분석 클립
              <select
                value={eventForm.playerClipId}
                onChange={(event) =>
                  handleChangeEventForm("playerClipId", event.target.value)
                }
                required
              >
                <option value="">선수 개인 분석 클립 선택</option>
                {playerClips.map((clip) => (
                  <option key={clip.playerClipId} value={clip.playerClipId}>
                    {clip.title} / {clip.playerName}
                  </option>
                ))}
              </select>
            </label>
          )}

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
            <button type="submit" disabled={isSubmitting}>
              이벤트 등록
            </button>
          </div>
        </form>
      )}
    </section>
  );
}
