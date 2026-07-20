// 경기 영상 기준 선수 요약 기록 등록과 분석 클립 연결을 담당하는 패널 컴포넌트

import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import {
  getManagementPlayerAnalysisClips,
  getManagementPlayers,
} from "../../api/playerAnalysisClipApi";
import {
  createPlayerRecord,
  getManagementPlayerRecordDetail,
  getManagementPlayerRecords,
  updatePlayerRecord,
} from "../../api/playerRecordApi";
import { createPlayerRecordEventWithClipLink } from "../../api/playerRecordEventApi";
import { getTeamAnalysisClips } from "../../api/teamAnalysisClipApi";
import type { MatchVideoDetailResponse } from "../../types/matchVideo";
import {
  PLAYER_ANALYSIS_CLIP_TYPE_LABELS,
  type PlayerAnalysisClipListItem,
  type PlayerSelectItem,
} from "../../types/playerAnalysisClip";
import {
  PLAYER_RECORD_STAT_LABELS,
  type CreatePlayerRecordRequest,
  type PlayerRecordDetailResponse,
} from "../../types/playerRecord";
import {
  PLAYER_RECORD_EVENT_TYPE_OPTIONS,
  type PlayerRecordEventType,
} from "../../types/playerRecordEvent";
import {
  TEAM_ANALYSIS_CLIP_TYPE_LABELS,
  type TeamAnalysisClipListItem,
} from "../../types/teamAnalysisClip";
import { getApiErrorMessage } from "../../utils/apiError";

type PlayerRecordEditorPanelProps = {
  matchVideo: MatchVideoDetailResponse;
  onSaved?: () => void;
};

type PlayerRecordRegistrationMode =
  | "SUMMARY"
  | "TEAM_ANALYSIS"
  | "PLAYER_ANALYSIS";

type PlayerRecordStatKey = (typeof PLAYER_RECORD_STAT_LABELS)[number]["key"];

type PlayerRecordStatValues = Record<PlayerRecordStatKey, number>;

const MIN_RECORD_VALUE = 0;
const MAX_RECORD_VALUE = 255;
const CLIP_PAGE_SIZE = 100;

function createEmptyRecordValues(): PlayerRecordStatValues {
  return {
    minutesPlayed: 0,
    goals: 0,
    assists: 0,
    shots: 0,
    shotsOnTarget: 0,
    passes: 0,
    successfulPasses: 0,
    dribbles: 0,
    successfulDribbles: 0,
    tackles: 0,
    interceptions: 0,
    clearances: 0,
    saves: 0,
    yellowCards: 0,
    redCards: 0,
  };
}

function createRecordValuesFromDetail(
  record: PlayerRecordDetailResponse,
): PlayerRecordStatValues {
  return {
    minutesPlayed: record.minutesPlayed,
    goals: record.goals,
    assists: record.assists,
    shots: record.shots,
    shotsOnTarget: record.shotsOnTarget,
    passes: record.passes,
    successfulPasses: record.successfulPasses,
    dribbles: record.dribbles,
    successfulDribbles: record.successfulDribbles,
    tackles: record.tackles,
    interceptions: record.interceptions,
    clearances: record.clearances,
    saves: record.saves,
    yellowCards: record.yellowCards,
    redCards: record.redCards,
  };
}

function clampRecordValue(value: number) {
  return Math.min(MAX_RECORD_VALUE, Math.max(MIN_RECORD_VALUE, value));
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

/**
 * 경기 영상이 변경되면 내부 편집기를 새로 마운트한다.
 *
 * 이전 경기에서 선택한 선수, 분석 클립과 기록값이
 * 새 경기의 편집 상태에 남지 않도록 한다.
 */
export default function PlayerRecordEditorPanel(
  props: PlayerRecordEditorPanelProps,
) {
  return (
    <PlayerRecordEditorPanelContent
      key={props.matchVideo.matchVideoId}
      {...props}
    />
  );
}

function PlayerRecordEditorPanelContent({
  matchVideo,
  onSaved,
}: PlayerRecordEditorPanelProps) {
  const [registrationMode, setRegistrationMode] =
    useState<PlayerRecordRegistrationMode>("SUMMARY");

  const [players, setPlayers] = useState<PlayerSelectItem[]>([]);
  const [teamClips, setTeamClips] = useState<TeamAnalysisClipListItem[]>([]);
  const [playerClips, setPlayerClips] = useState<PlayerAnalysisClipListItem[]>(
    [],
  );

  const [summaryPlayerId, setSummaryPlayerId] = useState("");
  const [recordId, setRecordId] = useState<number | null>(null);
  const [recordValues, setRecordValues] = useState<PlayerRecordStatValues>(() =>
    createEmptyRecordValues(),
  );
  const [recordMemo, setRecordMemo] = useState("");
  const [isRecordLookupBlocked, setIsRecordLookupBlocked] = useState(false);

  const [teamClipPlayerId, setTeamClipPlayerId] = useState("");
  const [selectedTeamClipId, setSelectedTeamClipId] = useState("");

  const [selectedPlayerClipId, setSelectedPlayerClipId] = useState("");

  const [selectedEventType, setSelectedEventType] = useState<
    PlayerRecordEventType | ""
  >("");
  const [eventMemo, setEventMemo] = useState("");

  const [isPlayerLoading, setIsPlayerLoading] = useState(true);
  const [isClipLoading, setIsClipLoading] = useState(true);
  const [isRecordLoading, setIsRecordLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const isMatchVideoReady = matchVideo.status === "READY";

  const selectedPlayerClip =
    playerClips.find(
      (clip) => clip.playerClipId === Number(selectedPlayerClipId),
    ) ?? null;

  const isSummaryFormDisabled =
    isPlayerLoading ||
    isRecordLoading ||
    isSaving ||
    isRecordLookupBlocked ||
    !isMatchVideoReady;

  const isClipLinkFormDisabled =
    isPlayerLoading || isClipLoading || isSaving || !isMatchVideoReady;

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

        setPlayers([]);
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
        const [teamClipResponse, playerClipResponse] = await Promise.all([
          getTeamAnalysisClips({
            page: 0,
            size: CLIP_PAGE_SIZE,
            matchVideoId: matchVideo.matchVideoId,
          }),
          getManagementPlayerAnalysisClips({
            page: 0,
            size: CLIP_PAGE_SIZE,
            matchVideoId: matchVideo.matchVideoId,
          }),
        ]);

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
          Array.isArray(playerClipResponse.playerClips)
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
          `연결 가능한 분석 클립을 불러오지 못했습니다. ${getApiErrorMessage(
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
  }, [matchVideo.matchVideoId]);

  useEffect(() => {
    if (registrationMode !== "SUMMARY" || !summaryPlayerId) {
      return;
    }

    const playerId = Number(summaryPlayerId);

    if (!Number.isInteger(playerId) || playerId <= 0) {
      return;
    }

    let ignore = false;

    async function fetchExistingPlayerRecord() {
      try {
        const recordPageResponse = await getManagementPlayerRecords({
          page: 0,
          size: 2,
          uploadId: matchVideo.matchVideoId,
          playerId,
        });

        if (ignore) {
          return;
        }

        if (
          recordPageResponse.totalElements > 1 ||
          recordPageResponse.records.length > 1
        ) {
          setIsRecordLookupBlocked(true);
          setErrorMessage(
            "동일 경기와 선수에 여러 개의 기록이 존재합니다. 관리자 확인이 필요합니다.",
          );
          return;
        }

        const existingRecord = recordPageResponse.records[0];

        if (!existingRecord) {
          setRecordId(null);
          setRecordValues(createEmptyRecordValues());
          setRecordMemo("");
          return;
        }

        const recordDetail = await getManagementPlayerRecordDetail(
          existingRecord.recordId,
        );

        if (ignore) {
          return;
        }

        setRecordId(recordDetail.recordId);
        setRecordValues(createRecordValuesFromDetail(recordDetail));
        setRecordMemo(recordDetail.memo ?? "");
      } catch (error) {
        if (ignore) {
          return;
        }

        setRecordId(null);
        setRecordValues(createEmptyRecordValues());
        setRecordMemo("");
        setIsRecordLookupBlocked(true);
        setErrorMessage(
          `기존 선수 기록을 불러오지 못했습니다. ${getApiErrorMessage(error)}`,
        );
      } finally {
        if (!ignore) {
          setIsRecordLoading(false);
        }
      }
    }

    void fetchExistingPlayerRecord();

    return () => {
      ignore = true;
    };
  }, [matchVideo.matchVideoId, registrationMode, summaryPlayerId]);

  function clearMessages() {
    setErrorMessage("");
    setSuccessMessage("");
  }

  function resetSummaryForm() {
    setSummaryPlayerId("");
    setRecordId(null);
    setRecordValues(createEmptyRecordValues());
    setRecordMemo("");
    setIsRecordLoading(false);
    setIsRecordLookupBlocked(false);
  }

  function resetClipLinkForm() {
    setTeamClipPlayerId("");
    setSelectedTeamClipId("");
    setSelectedPlayerClipId("");
    setSelectedEventType("");
    setEventMemo("");
  }

  function handleRegistrationModeChange(
    nextMode: PlayerRecordRegistrationMode,
  ) {
    if (isSaving || registrationMode === nextMode) {
      return;
    }

    setRegistrationMode(nextMode);
    resetSummaryForm();
    resetClipLinkForm();
    clearMessages();
  }

  function handleSummaryPlayerChange(playerId: string) {
    if (isSaving) {
      return;
    }

    setSummaryPlayerId(playerId);
    setRecordId(null);
    setRecordValues(createEmptyRecordValues());
    setRecordMemo("");
    setIsRecordLookupBlocked(false);
    setIsRecordLoading(Boolean(playerId));
    clearMessages();
  }

  function handleRecordValueChange(
    statKey: PlayerRecordStatKey,
    amount: number,
  ) {
    if (isSummaryFormDisabled) {
      return;
    }

    setRecordValues((currentValues) => ({
      ...currentValues,
      [statKey]: clampRecordValue(currentValues[statKey] + amount),
    }));

    clearMessages();
  }

  function handleTeamClipPlayerChange(playerId: string) {
    if (isSaving) {
      return;
    }

    setTeamClipPlayerId(playerId);
    setSelectedEventType("");
    setEventMemo("");
    clearMessages();
  }

  function handleTeamClipChange(teamClipId: string) {
    if (isSaving) {
      return;
    }

    setSelectedTeamClipId(teamClipId);
    setSelectedEventType("");
    setEventMemo("");
    clearMessages();
  }

  function handlePlayerClipChange(playerClipId: string) {
    if (isSaving) {
      return;
    }

    setSelectedPlayerClipId(playerClipId);
    setSelectedEventType("");
    setEventMemo("");
    clearMessages();
  }

  async function handleSummarySubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!isMatchVideoReady) {
      setErrorMessage(
        "READY 상태의 경기 영상에서만 선수 기록을 등록할 수 있습니다.",
      );
      return;
    }

    const playerId = Number(summaryPlayerId);

    if (!Number.isInteger(playerId) || playerId <= 0) {
      setErrorMessage("기록 대상 선수를 선택하세요.");
      return;
    }

    if (isRecordLookupBlocked) {
      setErrorMessage(
        "기존 선수 기록을 정상적으로 확인하지 못해 저장할 수 없습니다.",
      );
      return;
    }

    if (isRecordLoading || isSaving) {
      return;
    }

    const request: CreatePlayerRecordRequest = {
      uploadId: matchVideo.matchVideoId,
      playerId,
      ...recordValues,
      memo: recordMemo.trim() || null,
    };

    setIsSaving(true);
    clearMessages();

    try {
      if (recordId === null) {
        const response = await createPlayerRecord(request);

        setRecordId(response.recordId);
        setSuccessMessage("선수 기록을 등록했습니다.");
      } else {
        await updatePlayerRecord(recordId, request);

        setSuccessMessage("선수 기록을 수정했습니다.");
      }

      onSaved?.();
    } catch (error) {
      setErrorMessage(
        `선수 기록을 저장하지 못했습니다. ${getApiErrorMessage(error)}`,
      );
    } finally {
      setIsSaving(false);
    }
  }

  async function handleClipLinkSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!isMatchVideoReady) {
      setErrorMessage(
        "READY 상태의 경기 영상에서만 선수 기록을 등록할 수 있습니다.",
      );
      return;
    }

    if (!selectedEventType) {
      setErrorMessage("선수 기록 이벤트 유형을 선택하세요.");
      return;
    }

    if (registrationMode === "TEAM_ANALYSIS") {
      const playerId = Number(teamClipPlayerId);
      const teamClipId = Number(selectedTeamClipId);

      if (!Number.isInteger(playerId) || playerId <= 0) {
        setErrorMessage("기록 대상 선수를 선택하세요.");
        return;
      }

      if (!Number.isInteger(teamClipId) || teamClipId <= 0) {
        setErrorMessage("연결할 팀 분석 클립을 선택하세요.");
        return;
      }

      setIsSaving(true);
      clearMessages();

      try {
        const response = await createPlayerRecordEventWithClipLink({
          uploadId: matchVideo.matchVideoId,
          playerId,
          eventType: selectedEventType,
          eventMemo: eventMemo.trim() || null,
          clipSourceType: "TEAM_ANALYSIS",
          teamClipId,
          playerClipId: null,
        });

        setSelectedEventType("");
        setEventMemo("");
        setSuccessMessage(
          `팀 분석 클립을 선수 기록에 연결했습니다. 이벤트 ID: ${response.eventId}`,
        );

        onSaved?.();
      } catch (error) {
        setErrorMessage(getApiErrorMessage(error));
      } finally {
        setIsSaving(false);
      }

      return;
    }

    if (registrationMode === "PLAYER_ANALYSIS") {
      if (!selectedPlayerClip) {
        setErrorMessage("연결할 선수 개인 분석 클립을 선택하세요.");
        return;
      }

      setIsSaving(true);
      clearMessages();

      try {
        const response = await createPlayerRecordEventWithClipLink({
          uploadId: matchVideo.matchVideoId,
          playerId: selectedPlayerClip.playerId,
          eventType: selectedEventType,
          eventMemo: eventMemo.trim() || null,
          clipSourceType: "PLAYER_ANALYSIS",
          teamClipId: null,
          playerClipId: selectedPlayerClip.playerClipId,
        });

        setSelectedEventType("");
        setEventMemo("");
        setSuccessMessage(
          `선수 개인 분석 클립을 선수 기록에 연결했습니다. 이벤트 ID: ${response.eventId}`,
        );

        onSaved?.();
      } catch (error) {
        setErrorMessage(getApiErrorMessage(error));
      } finally {
        setIsSaving(false);
      }
    }
  }

  return (
    <section className="card player-record-editor-panel">
      <div className="section-heading-row">
        <div>
          <h2>선수 기록 등록</h2>

          <p className="helper-text">
            선수 요약 기록을 한 번에 저장하거나 현재 경기의 분석 클립을 선수
            기록 이벤트에 연결합니다.
          </p>
        </div>
      </div>

      <dl className="detail-grid">
        <div>
          <dt>경기</dt>
          <dd>{matchVideo.title}</dd>
        </div>

        <div>
          <dt>경기 영상 ID</dt>
          <dd>{matchVideo.matchVideoId}</dd>
        </div>

        <div>
          <dt>영상 상태</dt>
          <dd>{matchVideo.status}</dd>
        </div>
      </dl>

      {!isMatchVideoReady && (
        <p className="error-message" role="alert">
          READY 상태의 경기 영상에서만 선수 기록을 등록할 수 있습니다.
        </p>
      )}

      {errorMessage && (
        <p className="error-message" role="alert">
          {errorMessage}
        </p>
      )}

      {successMessage && (
        <p className="success-message" role="status">
          {successMessage}
        </p>
      )}

      <div className="player-record-mode-selector">
        <h3>등록 방식</h3>

        <div className="button-row">
          <button
            type="button"
            aria-pressed={registrationMode === "SUMMARY"}
            disabled={isSaving}
            onClick={() => handleRegistrationModeChange("SUMMARY")}
          >
            클립 없이 등록
          </button>

          <button
            type="button"
            aria-pressed={registrationMode === "TEAM_ANALYSIS"}
            disabled={isSaving}
            onClick={() => handleRegistrationModeChange("TEAM_ANALYSIS")}
          >
            팀 분석 클립 연결
          </button>

          <button
            type="button"
            aria-pressed={registrationMode === "PLAYER_ANALYSIS"}
            disabled={isSaving}
            onClick={() => handleRegistrationModeChange("PLAYER_ANALYSIS")}
          >
            선수 개인 분석 클립 연결
          </button>
        </div>
      </div>

      {registrationMode === "SUMMARY" && (
        <form onSubmit={handleSummarySubmit}>
          <div className="section-heading-row">
            <div>
              <h3>클립 없이 선수 기록 등록</h3>

              <p className="helper-text">
                현재 경기와 선수의 기존 기록을 불러온 뒤 전체 기록값을 한 번에
                저장합니다.
              </p>
            </div>
          </div>

          <label>
            대상 선수
            <select
              value={summaryPlayerId}
              disabled={isPlayerLoading || isSaving || !isMatchVideoReady}
              onChange={(event) =>
                handleSummaryPlayerChange(event.target.value)
              }
            >
              <option value="">
                {isPlayerLoading
                  ? "선수 목록을 불러오는 중입니다."
                  : "선수를 선택하세요."}
              </option>

              {players.map((player) => (
                <option key={player.playerId} value={player.playerId}>
                  {player.name}
                  {player.uniformNumber !== null
                    ? ` · ${player.uniformNumber}번`
                    : ""}
                  {player.grade !== null ? ` · ${player.grade}학년` : ""}
                </option>
              ))}
            </select>
          </label>

          {!isPlayerLoading && players.length === 0 && (
            <p className="helper-text">등록 가능한 선수가 없습니다.</p>
          )}

          {isRecordLoading ? (
            <p>기존 선수 기록을 불러오는 중입니다.</p>
          ) : summaryPlayerId ? (
            <>
              <p className="helper-text">
                {recordId === null
                  ? "기존 기록이 없어 모든 값이 0으로 표시됩니다."
                  : `기존 기록을 수정합니다. 기록 ID: ${recordId}`}
              </p>

              <div className="player-record-counter-grid">
                {PLAYER_RECORD_STAT_LABELS.map((stat) => {
                  const currentValue = recordValues[stat.key];

                  return (
                    <article className="player-record-counter" key={stat.key}>
                      <h3>{stat.label}</h3>

                      <div className="player-record-counter-controls">
                        <button
                          type="button"
                          aria-label={`${stat.label} 감소`}
                          disabled={
                            isSummaryFormDisabled ||
                            currentValue <= MIN_RECORD_VALUE
                          }
                          onClick={() => handleRecordValueChange(stat.key, -1)}
                        >
                          -
                        </button>

                        <output aria-label={`${stat.label} 현재 값`}>
                          {currentValue}
                          {stat.key === "minutesPlayed" ? "분" : ""}
                        </output>

                        <button
                          type="button"
                          aria-label={`${stat.label} 증가`}
                          disabled={
                            isSummaryFormDisabled ||
                            currentValue >= MAX_RECORD_VALUE
                          }
                          onClick={() => handleRecordValueChange(stat.key, 1)}
                        >
                          +
                        </button>
                      </div>
                    </article>
                  );
                })}
              </div>

              <label>
                경기 전체 메모
                <textarea
                  value={recordMemo}
                  maxLength={255}
                  disabled={isSummaryFormDisabled}
                  onChange={(event) => {
                    setRecordMemo(event.target.value);
                    clearMessages();
                  }}
                  placeholder="경기 전체 선수 기록에 대한 메모를 입력하세요."
                />
              </label>

              <p className="helper-text">{recordMemo.length} / 255자</p>

              <div className="button-row">
                <button type="submit" disabled={isSummaryFormDisabled}>
                  {isSaving
                    ? "저장 중..."
                    : recordId === null
                      ? "선수 기록 등록"
                      : "선수 기록 수정"}
                </button>
              </div>
            </>
          ) : (
            <p className="helper-text">
              대상 선수를 선택하면 현재 경기의 기존 기록을 조회합니다.
            </p>
          )}
        </form>
      )}

      {registrationMode === "TEAM_ANALYSIS" && (
        <form onSubmit={handleClipLinkSubmit}>
          <div className="section-heading-row">
            <div>
              <h3>팀 분석 클립 연결</h3>

              <p className="helper-text">
                현재 경기의 READY 팀 분석 클립 하나와 선수 기록 유형 하나를
                연결합니다.
              </p>
            </div>
          </div>

          <label>
            대상 선수
            <select
              value={teamClipPlayerId}
              disabled={isClipLinkFormDisabled}
              onChange={(event) =>
                handleTeamClipPlayerChange(event.target.value)
              }
            >
              <option value="">
                {isPlayerLoading
                  ? "선수 목록을 불러오는 중입니다."
                  : "선수를 선택하세요."}
              </option>

              {players.map((player) => (
                <option key={player.playerId} value={player.playerId}>
                  {player.name}
                  {player.uniformNumber !== null
                    ? ` · ${player.uniformNumber}번`
                    : ""}
                  {player.grade !== null ? ` · ${player.grade}학년` : ""}
                </option>
              ))}
            </select>
          </label>

          <label>
            팀 분석 클립
            <select
              value={selectedTeamClipId}
              disabled={isClipLinkFormDisabled}
              onChange={(event) => handleTeamClipChange(event.target.value)}
            >
              <option value="">
                {isClipLoading
                  ? "팀 분석 클립을 불러오는 중입니다."
                  : "팀 분석 클립을 선택하세요."}
              </option>

              {teamClips.map((clip) => (
                <option key={clip.teamClipId} value={clip.teamClipId}>
                  {clip.title}
                  {" · "}
                  {TEAM_ANALYSIS_CLIP_TYPE_LABELS[clip.clipType]}
                  {" · "}
                  {formatVideoTime(clip.startTimeSec)}
                  {" ~ "}
                  {formatVideoTime(clip.endTimeSec)}
                </option>
              ))}
            </select>
          </label>

          {!isClipLoading && teamClips.length === 0 && (
            <p className="helper-text">
              현재 경기에 연결할 수 있는 READY 팀 분석 클립이 없습니다.
            </p>
          )}

          <label>
            선수 기록 유형
            <select
              value={selectedEventType}
              disabled={isClipLinkFormDisabled}
              onChange={(event) => {
                setSelectedEventType(
                  event.target.value as PlayerRecordEventType | "",
                );
                clearMessages();
              }}
            >
              <option value="">선수 기록 유형을 선택하세요.</option>

              {PLAYER_RECORD_EVENT_TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            이벤트 메모
            <textarea
              value={eventMemo}
              maxLength={255}
              disabled={isClipLinkFormDisabled}
              onChange={(event) => {
                setEventMemo(event.target.value);
                clearMessages();
              }}
              placeholder="선택 입력"
            />
          </label>

          <p className="helper-text">
            이벤트 시간과 기록 수치는 선택한 클립을 기준으로 백엔드에서 자동
            저장합니다.
          </p>

          <div className="button-row">
            <button type="submit" disabled={isClipLinkFormDisabled}>
              {isSaving ? "연결 중..." : "팀 분석 클립 연결"}
            </button>
          </div>
        </form>
      )}

      {registrationMode === "PLAYER_ANALYSIS" && (
        <form onSubmit={handleClipLinkSubmit}>
          <div className="section-heading-row">
            <div>
              <h3>선수 개인 분석 클립 연결</h3>

              <p className="helper-text">
                현재 경기의 READY 개인 분석 클립을 선택하면 클립 대상 선수가
                기록 대상 선수로 사용됩니다.
              </p>
            </div>
          </div>

          <label>
            선수 개인 분석 클립
            <select
              value={selectedPlayerClipId}
              disabled={isClipLinkFormDisabled}
              onChange={(event) => handlePlayerClipChange(event.target.value)}
            >
              <option value="">
                {isClipLoading
                  ? "선수 개인 분석 클립을 불러오는 중입니다."
                  : "선수 개인 분석 클립을 선택하세요."}
              </option>

              {playerClips.map((clip) => (
                <option key={clip.playerClipId} value={clip.playerClipId}>
                  {clip.title}
                  {" · "}
                  {PLAYER_ANALYSIS_CLIP_TYPE_LABELS[clip.clipType]}
                  {" · 대상 선수: "}
                  {clip.playerName}
                </option>
              ))}
            </select>
          </label>

          {!isClipLoading && playerClips.length === 0 && (
            <p className="helper-text">
              현재 경기에 연결할 수 있는 READY 선수 개인 분석 클립이 없습니다.
            </p>
          )}

          <div className="readonly-field">
            <span>기록 대상 선수</span>

            <strong>
              {selectedPlayerClip
                ? `${selectedPlayerClip.playerName} · 선수 ID ${selectedPlayerClip.playerId}`
                : "선수 개인 분석 클립을 선택하세요."}
            </strong>
          </div>

          <label>
            선수 기록 유형
            <select
              value={selectedEventType}
              disabled={isClipLinkFormDisabled}
              onChange={(event) => {
                setSelectedEventType(
                  event.target.value as PlayerRecordEventType | "",
                );
                clearMessages();
              }}
            >
              <option value="">선수 기록 유형을 선택하세요.</option>

              {PLAYER_RECORD_EVENT_TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            이벤트 메모
            <textarea
              value={eventMemo}
              maxLength={255}
              disabled={isClipLinkFormDisabled}
              onChange={(event) => {
                setEventMemo(event.target.value);
                clearMessages();
              }}
              placeholder="선택 입력"
            />
          </label>

          <p className="helper-text">
            이벤트 시간과 기록 수치는 선택한 클립을 기준으로 백엔드에서 자동
            저장합니다.
          </p>

          <div className="button-row">
            <button type="submit" disabled={isClipLinkFormDisabled}>
              {isSaving ? "연결 중..." : "선수 개인 분석 클립 연결"}
            </button>
          </div>
        </form>
      )}
    </section>
  );
}
