// 선수 기록 목록, 상세, 등록, 수정, 삭제와 기록 이벤트 목록/수정을 관리하는 페이지 컴포넌트

import type { FormEvent } from "react";
import { useEffect, useState } from "react";

import {
  deletePlayerRecordEvent,
  getManagementPlayerRecordEvents,
  getMyPlayerRecordEvents,
  updatePlayerRecordEvent,
} from "../api/playerRecordEventApi";
import {
  createPlayerRecord,
  deletePlayerRecord,
  getManagementPlayerRecordDetail,
  getManagementPlayerRecords,
  getMyPlayerRecordDetail,
  getMyPlayerRecords,
  updatePlayerRecord,
} from "../api/playerRecordApi";
import { useAuth } from "../hooks/useAuth";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";
import type {
  CreatePlayerRecordRequest,
  PlayerRecordDetailResponse,
  PlayerRecordListItem,
} from "../types/playerRecord";
import {
  PLAYER_RECORD_CLIP_SOURCE_TYPE_LABELS,
  PLAYER_RECORD_EVENT_TYPE_LABELS,
  PLAYER_RECORD_EVENT_TYPE_OPTIONS,
  type PlayerRecordEventResponse,
  type PlayerRecordEventType,
  type UpdatePlayerRecordEventRequest,
} from "../types/playerRecordEvent";
import { getApiErrorMessage } from "../utils/apiError";
import { getFrontendPermissions } from "../utils/rolePermission";

const RECORD_PAGE_SIZE = 20;

type PlayerRecordFormState = {
  uploadId: string;
  playerId: string;
  minutesPlayed: string;
  goals: string;
  assists: string;
  shots: string;
  shotsOnTarget: string;
  passes: string;
  successfulPasses: string;
  dribbles: string;
  successfulDribbles: string;
  tackles: string;
  interceptions: string;
  clearances: string;
  saves: string;
  yellowCards: string;
  redCards: string;
  memo: string;
};

type PlayerRecordEventEditFormState = {
  eventType: PlayerRecordEventType;
  eventStartTimeSec: string;
  eventEndTimeSec: string;
  value: string;
  eventMemo: string;
};

const EMPTY_RECORD_FORM: PlayerRecordFormState = {
  uploadId: "",
  playerId: "",
  minutesPlayed: "0",
  goals: "0",
  assists: "0",
  shots: "0",
  shotsOnTarget: "0",
  passes: "0",
  successfulPasses: "0",
  dribbles: "0",
  successfulDribbles: "0",
  tackles: "0",
  interceptions: "0",
  clearances: "0",
  saves: "0",
  yellowCards: "0",
  redCards: "0",
  memo: "",
};

const EMPTY_EVENT_EDIT_FORM: PlayerRecordEventEditFormState = {
  eventType: "ETC",
  eventStartTimeSec: "0",
  eventEndTimeSec: "1",
  value: "1",
  eventMemo: "",
};

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString();
}

function formatSeconds(value: number) {
  const safeValue = Number.isFinite(value) ? value : 0;
  const minutes = Math.floor(safeValue / 60);
  const seconds = safeValue % 60;

  return `${minutes}:${String(seconds).padStart(2, "0")}`;
}

function toNumber(value: string) {
  const parsedValue = Number(value);

  return Number.isFinite(parsedValue) ? parsedValue : 0;
}

function createFormStateFromRecord(
  record: PlayerRecordDetailResponse,
): PlayerRecordFormState {
  return {
    uploadId: String(record.uploadId),
    playerId: String(record.playerId),
    minutesPlayed: String(record.minutesPlayed),
    goals: String(record.goals),
    assists: String(record.assists),
    shots: String(record.shots),
    shotsOnTarget: String(record.shotsOnTarget),
    passes: String(record.passes),
    successfulPasses: String(record.successfulPasses),
    dribbles: String(record.dribbles),
    successfulDribbles: String(record.successfulDribbles),
    tackles: String(record.tackles),
    interceptions: String(record.interceptions),
    clearances: String(record.clearances),
    saves: String(record.saves),
    yellowCards: String(record.yellowCards),
    redCards: String(record.redCards),
    memo: record.memo ?? "",
  };
}

function createRequestFromRecordForm(
  form: PlayerRecordFormState,
): CreatePlayerRecordRequest {
  return {
    uploadId: toNumber(form.uploadId),
    playerId: toNumber(form.playerId),
    minutesPlayed: toNumber(form.minutesPlayed),
    goals: toNumber(form.goals),
    assists: toNumber(form.assists),
    shots: toNumber(form.shots),
    shotsOnTarget: toNumber(form.shotsOnTarget),
    passes: toNumber(form.passes),
    successfulPasses: toNumber(form.successfulPasses),
    dribbles: toNumber(form.dribbles),
    successfulDribbles: toNumber(form.successfulDribbles),
    tackles: toNumber(form.tackles),
    interceptions: toNumber(form.interceptions),
    clearances: toNumber(form.clearances),
    saves: toNumber(form.saves),
    yellowCards: toNumber(form.yellowCards),
    redCards: toNumber(form.redCards),
    memo: form.memo.trim() || null,
  };
}

function createEventEditFormState(
  event: PlayerRecordEventResponse,
): PlayerRecordEventEditFormState {
  return {
    eventType: event.eventType,
    eventStartTimeSec: String(event.eventStartTimeSec),
    eventEndTimeSec: String(event.eventEndTimeSec),
    value: String(event.value),
    eventMemo: event.eventMemo ?? "",
  };
}

function createRequestFromEventEditForm(
  form: PlayerRecordEventEditFormState,
): UpdatePlayerRecordEventRequest {
  return {
    eventType: form.eventType,
    eventStartTimeSec: toNumber(form.eventStartTimeSec),
    eventEndTimeSec: toNumber(form.eventEndTimeSec),
    value: toNumber(form.value),
    eventMemo: form.eventMemo.trim() || null,
  };
}

export default function PlayerRecordPage() {
  const { member } = useAuth();
  const permissions = getFrontendPermissions(member);

  const isPlayer = member?.memberRole === "PLAYER";
  const canManagePlayerRecord =
    permissions.canCreatePlayerRecord ||
    permissions.canUpdatePlayerRecord ||
    permissions.canDeletePlayerRecord;

  const [records, setRecords] = useState<PlayerRecordListItem[]>([]);
  const [selectedRecordId, setSelectedRecordId] = useState<number | null>(null);
  const [selectedRecord, setSelectedRecord] =
    useState<PlayerRecordDetailResponse | null>(null);
  const [events, setEvents] = useState<PlayerRecordEventResponse[]>([]);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [filterUploadId, setFilterUploadId] = useState("");
  const [filterPlayerId, setFilterPlayerId] = useState("");

  const [recordForm, setRecordForm] =
    useState<PlayerRecordFormState>(EMPTY_RECORD_FORM);
  const [editingRecordId, setEditingRecordId] = useState<number | null>(null);

  const [editingEventId, setEditingEventId] = useState<number | null>(null);
  const [eventEditForm, setEventEditForm] =
    useState<PlayerRecordEventEditFormState>(EMPTY_EVENT_EDIT_FORM);

  const [recordListLoading, setRecordListLoading] = useState(true);
  const [recordDetailLoading, setRecordDetailLoading] = useState(false);
  const [isSubmittingRecord, setIsSubmittingRecord] = useState(false);
  const [isSubmittingEvent, setIsSubmittingEvent] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  async function loadRecords() {
    setRecordListLoading(true);

    try {
      const response = isPlayer
        ? await getMyPlayerRecords(page, RECORD_PAGE_SIZE)
        : await getManagementPlayerRecords({
            page,
            size: RECORD_PAGE_SIZE,
            uploadId: filterUploadId ? Number(filterUploadId) : undefined,
            playerId: filterPlayerId ? Number(filterPlayerId) : undefined,
          });

      setRecords(response.records);
      setTotalPages(response.totalPages);
      setErrorMessage(null);

      setSelectedRecordId((currentRecordId) => {
        if (response.records.length === 0) {
          return null;
        }

        const hasCurrentRecord =
          currentRecordId !== null &&
          response.records.some(
            (record) => record.recordId === currentRecordId,
          );

        return hasCurrentRecord
          ? currentRecordId
          : response.records[0].recordId;
      });
    } catch (error) {
      setRecords([]);
      setSelectedRecordId(null);
      setErrorMessage(
        `선수 기록 목록을 불러오지 못했습니다. ${getApiErrorMessage(error)}`,
      );
    } finally {
      setRecordListLoading(false);
    }
  }

  async function loadSelectedRecord(recordId: number) {
    setRecordDetailLoading(true);

    try {
      const [recordDetailResponse, recordEventsResponse] = isPlayer
        ? await Promise.all([
            getMyPlayerRecordDetail(recordId),
            getMyPlayerRecordEvents(recordId),
          ])
        : await Promise.all([
            getManagementPlayerRecordDetail(recordId),
            getManagementPlayerRecordEvents(recordId),
          ]);

      setSelectedRecord(recordDetailResponse);
      setEvents(recordEventsResponse.events);
      setErrorMessage(null);

      if (!isPlayer) {
        setRecordForm(createFormStateFromRecord(recordDetailResponse));
        setEditingRecordId(recordDetailResponse.recordId);
      }
    } catch (error) {
      setSelectedRecord(null);
      setEvents([]);
      setErrorMessage(
        `선수 기록 상세 정보를 불러오지 못했습니다. ${getApiErrorMessage(
          error,
        )}`,
      );
    } finally {
      setRecordDetailLoading(false);
    }
  }

  useEffect(() => {
    let ignore = false;

    async function run() {
      setRecordListLoading(true);

      try {
        const response = isPlayer
          ? await getMyPlayerRecords(page, RECORD_PAGE_SIZE)
          : await getManagementPlayerRecords({
              page,
              size: RECORD_PAGE_SIZE,
              uploadId: filterUploadId ? Number(filterUploadId) : undefined,
              playerId: filterPlayerId ? Number(filterPlayerId) : undefined,
            });

        if (ignore) {
          return;
        }

        setRecords(response.records);
        setTotalPages(response.totalPages);
        setErrorMessage(null);

        setSelectedRecordId((currentRecordId) => {
          if (response.records.length === 0) {
            return null;
          }

          const hasCurrentRecord =
            currentRecordId !== null &&
            response.records.some(
              (record) => record.recordId === currentRecordId,
            );

          return hasCurrentRecord
            ? currentRecordId
            : response.records[0].recordId;
        });
      } catch (error) {
        if (ignore) {
          return;
        }

        setRecords([]);
        setSelectedRecordId(null);
        setErrorMessage(
          `선수 기록 목록을 불러오지 못했습니다. ${getApiErrorMessage(error)}`,
        );
      } finally {
        if (!ignore) {
          setRecordListLoading(false);
        }
      }
    }

    run();

    return () => {
      ignore = true;
    };
  }, [filterPlayerId, filterUploadId, isPlayer, page]);

  useEffect(() => {
    let ignore = false;

    async function run() {
      if (selectedRecordId === null) {
        setSelectedRecord(null);
        setEvents([]);
        return;
      }

      setRecordDetailLoading(true);

      try {
        const [recordDetailResponse, recordEventsResponse] = isPlayer
          ? await Promise.all([
              getMyPlayerRecordDetail(selectedRecordId),
              getMyPlayerRecordEvents(selectedRecordId),
            ])
          : await Promise.all([
              getManagementPlayerRecordDetail(selectedRecordId),
              getManagementPlayerRecordEvents(selectedRecordId),
            ]);

        if (ignore) {
          return;
        }

        setSelectedRecord(recordDetailResponse);
        setEvents(recordEventsResponse.events);
        setErrorMessage(null);

        if (!isPlayer) {
          setRecordForm(createFormStateFromRecord(recordDetailResponse));
          setEditingRecordId(recordDetailResponse.recordId);
        }
      } catch (error) {
        if (ignore) {
          return;
        }

        setSelectedRecord(null);
        setEvents([]);
        setErrorMessage(
          `선수 기록 상세 정보를 불러오지 못했습니다. ${getApiErrorMessage(
            error,
          )}`,
        );
      } finally {
        if (!ignore) {
          setRecordDetailLoading(false);
        }
      }
    }

    run();

    return () => {
      ignore = true;
    };
  }, [isPlayer, selectedRecordId]);

  function handleRecordFormChange(
    field: keyof PlayerRecordFormState,
    value: string,
  ) {
    setRecordForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }));
  }

  function handleEventEditFormChange(
    field: keyof PlayerRecordEventEditFormState,
    value: string,
  ) {
    setEventEditForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }));
  }

  function resetRecordForm() {
    setEditingRecordId(null);
    setRecordForm(EMPTY_RECORD_FORM);
  }

  function resetEventEditForm() {
    setEditingEventId(null);
    setEventEditForm(EMPTY_EVENT_EDIT_FORM);
  }

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPage(0);
  }

  async function handleRecordSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!canManagePlayerRecord) {
      return;
    }

    const request = createRequestFromRecordForm(recordForm);

    if (request.uploadId <= 0 || request.playerId <= 0) {
      setErrorMessage("경기 영상 ID와 선수 ID를 입력하세요.");
      return;
    }

    if (
      request.shotsOnTarget > request.shots ||
      request.successfulPasses > request.passes ||
      request.successfulDribbles > request.dribbles
    ) {
      setErrorMessage(
        "유효 슈팅, 성공 패스, 성공 드리블은 각각 전체 시도 수보다 클 수 없습니다.",
      );
      return;
    }

    setIsSubmittingRecord(true);

    try {
      if (editingRecordId === null) {
        const response = await createPlayerRecord(request);
        await loadRecords();
        setSelectedRecordId(response.recordId);
        resetRecordForm();
      } else {
        const response = await updatePlayerRecord(editingRecordId, request);
        await loadRecords();
        await loadSelectedRecord(response.recordId);
      }

      setErrorMessage(null);
    } catch (error) {
      setErrorMessage(
        `선수 기록을 저장하지 못했습니다. ${getApiErrorMessage(error)}`,
      );
    } finally {
      setIsSubmittingRecord(false);
    }
  }

  function handleStartEditEvent(event: PlayerRecordEventResponse) {
    setEditingEventId(event.eventId);
    setEventEditForm(createEventEditFormState(event));
  }

  async function handleUpdateEventSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (editingEventId === null || selectedRecordId === null) {
      return;
    }

    const request = createRequestFromEventEditForm(eventEditForm);

    if (request.eventStartTimeSec < 0) {
      setErrorMessage("이벤트 시작 시간은 0 이상이어야 합니다.");
      return;
    }

    if (request.eventEndTimeSec <= request.eventStartTimeSec) {
      setErrorMessage("이벤트 종료 시간은 시작 시간보다 커야 합니다.");
      return;
    }

    if (request.value <= 0 || request.value > 255) {
      setErrorMessage("기록 수치는 1 이상 255 이하로 입력하세요.");
      return;
    }

    setIsSubmittingEvent(true);

    try {
      await updatePlayerRecordEvent(editingEventId, request);
      await loadSelectedRecord(selectedRecordId);
      resetEventEditForm();
      setErrorMessage(null);
    } catch (error) {
      setErrorMessage(
        `선수 기록 이벤트를 수정하지 못했습니다. ${getApiErrorMessage(error)}`,
      );
    } finally {
      setIsSubmittingEvent(false);
    }
  }

  async function handleDeleteRecord(recordId: number) {
    if (!window.confirm("이 선수 기록을 삭제할까요?")) {
      return;
    }

    try {
      await deletePlayerRecord(recordId);
      resetRecordForm();
      resetEventEditForm();
      setSelectedRecordId(null);
      setSelectedRecord(null);
      setEvents([]);
      await loadRecords();
      setErrorMessage(null);
    } catch (error) {
      setErrorMessage(
        `선수 기록을 삭제하지 못했습니다. ${getApiErrorMessage(error)}`,
      );
    }
  }

  async function handleDeleteEvent(eventId: number) {
    if (!window.confirm("이 선수 기록 이벤트를 삭제할까요?")) {
      return;
    }

    try {
      await deletePlayerRecordEvent(eventId);

      if (selectedRecordId !== null) {
        await loadSelectedRecord(selectedRecordId);
      }

      if (editingEventId === eventId) {
        resetEventEditForm();
      }

      setErrorMessage(null);
    } catch (error) {
      setErrorMessage(
        `선수 기록 이벤트를 삭제하지 못했습니다. ${getApiErrorMessage(error)}`,
      );
    }
  }

  return (
    <AuthenticatedLayout title="선수 기록">
      <main className="page">
        <section className="page-header">
          <div>
            <h1>선수 기록</h1>
            <p>
              경기별 선수 요약 기록과 장면 기반 선수 기록 이벤트를 확인합니다.
            </p>
          </div>
        </section>

        {errorMessage && <p className="error-message">{errorMessage}</p>}

        {!isPlayer && (
          <section className="card">
            <h2>선수 기록 검색</h2>
            <form className="form-grid" onSubmit={handleSearchSubmit}>
              <label>
                경기 영상 ID
                <input
                  type="number"
                  min="1"
                  value={filterUploadId}
                  onChange={(event) => setFilterUploadId(event.target.value)}
                  placeholder="전체"
                />
              </label>

              <label>
                선수 ID
                <input
                  type="number"
                  min="1"
                  value={filterPlayerId}
                  onChange={(event) => setFilterPlayerId(event.target.value)}
                  placeholder="전체"
                />
              </label>

              <div className="button-row">
                <button type="submit">검색</button>
                <button
                  type="button"
                  onClick={() => {
                    setFilterUploadId("");
                    setFilterPlayerId("");
                    setPage(0);
                  }}
                >
                  초기화
                </button>
              </div>
            </form>
          </section>
        )}

        <section className="content-grid">
          <article className="card">
            <h2>{isPlayer ? "내 기록 목록" : "선수 기록 목록"}</h2>

            {recordListLoading ? (
              <p>선수 기록 목록을 불러오는 중입니다.</p>
            ) : records.length === 0 ? (
              <p>조회할 선수 기록이 없습니다.</p>
            ) : (
              <>
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>경기</th>
                        <th>선수</th>
                        <th>기록자</th>
                        <th>마지막 수정자</th>
                        <th>수정일</th>
                        <th>선택</th>
                      </tr>
                    </thead>
                    <tbody>
                      {records.map((record) => (
                        <tr
                          key={record.recordId}
                          className={
                            selectedRecordId === record.recordId
                              ? "selected-row"
                              : undefined
                          }
                        >
                          <td>{record.matchVideoTitle}</td>
                          <td>{record.playerName}</td>
                          <td>{record.recorderName}</td>
                          <td>{record.lastModifierName || "-"}</td>
                          <td>{formatDateTime(record.updatedAt)}</td>
                          <td>
                            <button
                              type="button"
                              onClick={() => {
                                resetEventEditForm();
                                setSelectedRecordId(record.recordId);
                              }}
                            >
                              보기
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="button-row">
                  <button
                    type="button"
                    disabled={page === 0}
                    onClick={() => setPage((currentPage) => currentPage - 1)}
                  >
                    이전
                  </button>
                  <span>
                    {page + 1} / {Math.max(totalPages, 1)}
                  </span>
                  <button
                    type="button"
                    disabled={page + 1 >= totalPages}
                    onClick={() => setPage((currentPage) => currentPage + 1)}
                  >
                    다음
                  </button>
                </div>
              </>
            )}
          </article>

          <article className="card">
            <h2>선수 기록 상세</h2>

            {recordDetailLoading ? (
              <p>선수 기록 상세 정보를 불러오는 중입니다.</p>
            ) : selectedRecord === null ? (
              <p>선택된 선수 기록이 없습니다.</p>
            ) : (
              <>
                <dl className="detail-list">
                  <div>
                    <dt>경기</dt>
                    <dd>{selectedRecord.matchVideoTitle}</dd>
                  </div>
                  <div>
                    <dt>선수</dt>
                    <dd>{selectedRecord.playerName}</dd>
                  </div>
                  <div>
                    <dt>출전 시간</dt>
                    <dd>{selectedRecord.minutesPlayed}분</dd>
                  </div>
                  <div>
                    <dt>득점 / 도움</dt>
                    <dd>
                      {selectedRecord.goals} / {selectedRecord.assists}
                    </dd>
                  </div>
                  <div>
                    <dt>슈팅 / 유효 슈팅</dt>
                    <dd>
                      {selectedRecord.shots} / {selectedRecord.shotsOnTarget}
                    </dd>
                  </div>
                  <div>
                    <dt>패스 / 성공 패스</dt>
                    <dd>
                      {selectedRecord.passes} /{" "}
                      {selectedRecord.successfulPasses}
                    </dd>
                  </div>
                  <div>
                    <dt>드리블 / 성공 드리블</dt>
                    <dd>
                      {selectedRecord.dribbles} /{" "}
                      {selectedRecord.successfulDribbles}
                    </dd>
                  </div>
                  <div>
                    <dt>태클</dt>
                    <dd>{selectedRecord.tackles}</dd>
                  </div>
                  <div>
                    <dt>인터셉트</dt>
                    <dd>{selectedRecord.interceptions}</dd>
                  </div>
                  <div>
                    <dt>클리어링</dt>
                    <dd>{selectedRecord.clearances}</dd>
                  </div>
                  <div>
                    <dt>세이브</dt>
                    <dd>{selectedRecord.saves}</dd>
                  </div>
                  <div>
                    <dt>경고 / 퇴장</dt>
                    <dd>
                      {selectedRecord.yellowCards} / {selectedRecord.redCards}
                    </dd>
                  </div>
                  <div>
                    <dt>경기 기록 메모</dt>
                    <dd>{selectedRecord.memo || "-"}</dd>
                  </div>
                </dl>

                {permissions.canDeletePlayerRecord && (
                  <div className="button-row">
                    <button
                      type="button"
                      onClick={() =>
                        handleDeleteRecord(selectedRecord.recordId)
                      }
                    >
                      선수 기록 삭제
                    </button>
                  </div>
                )}
              </>
            )}
          </article>
        </section>

        {canManagePlayerRecord && (
          <section className="card">
            <h2>
              {editingRecordId === null ? "선수 기록 등록" : "선수 기록 수정"}
            </h2>

            <form className="form-grid" onSubmit={handleRecordSubmit}>
              <label>
                경기 영상 ID
                <input
                  type="number"
                  min="1"
                  value={recordForm.uploadId}
                  onChange={(event) =>
                    handleRecordFormChange("uploadId", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                선수 ID
                <input
                  type="number"
                  min="1"
                  value={recordForm.playerId}
                  onChange={(event) =>
                    handleRecordFormChange("playerId", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                출전 시간
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.minutesPlayed}
                  onChange={(event) =>
                    handleRecordFormChange("minutesPlayed", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                득점
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.goals}
                  onChange={(event) =>
                    handleRecordFormChange("goals", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                도움
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.assists}
                  onChange={(event) =>
                    handleRecordFormChange("assists", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                슈팅
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.shots}
                  onChange={(event) =>
                    handleRecordFormChange("shots", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                유효 슈팅
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.shotsOnTarget}
                  onChange={(event) =>
                    handleRecordFormChange("shotsOnTarget", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                패스
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.passes}
                  onChange={(event) =>
                    handleRecordFormChange("passes", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                성공 패스
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.successfulPasses}
                  onChange={(event) =>
                    handleRecordFormChange(
                      "successfulPasses",
                      event.target.value,
                    )
                  }
                  required
                />
              </label>

              <label>
                드리블
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.dribbles}
                  onChange={(event) =>
                    handleRecordFormChange("dribbles", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                성공 드리블
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.successfulDribbles}
                  onChange={(event) =>
                    handleRecordFormChange(
                      "successfulDribbles",
                      event.target.value,
                    )
                  }
                  required
                />
              </label>

              <label>
                태클
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.tackles}
                  onChange={(event) =>
                    handleRecordFormChange("tackles", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                인터셉트
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.interceptions}
                  onChange={(event) =>
                    handleRecordFormChange("interceptions", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                클리어링
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.clearances}
                  onChange={(event) =>
                    handleRecordFormChange("clearances", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                세이브
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.saves}
                  onChange={(event) =>
                    handleRecordFormChange("saves", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                경고
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.yellowCards}
                  onChange={(event) =>
                    handleRecordFormChange("yellowCards", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                퇴장
                <input
                  type="number"
                  min="0"
                  max="255"
                  value={recordForm.redCards}
                  onChange={(event) =>
                    handleRecordFormChange("redCards", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                경기 기록 메모
                <textarea
                  value={recordForm.memo}
                  onChange={(event) =>
                    handleRecordFormChange("memo", event.target.value)
                  }
                  placeholder="경기 전체 선수 기록 메모"
                />
              </label>

              <div className="button-row">
                <button type="submit" disabled={isSubmittingRecord}>
                  {editingRecordId === null ? "등록" : "수정"}
                </button>
                <button type="button" onClick={resetRecordForm}>
                  새 기록 입력
                </button>
              </div>
            </form>
          </section>
        )}

        <section className="card">
          <h2>선수 기록 이벤트</h2>

          {selectedRecord === null ? (
            <p>선수 기록을 먼저 선택하세요.</p>
          ) : events.length === 0 ? (
            <p>등록된 선수 기록 이벤트가 없습니다.</p>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>유형</th>
                    <th>시간</th>
                    <th>값</th>
                    <th>메모</th>
                    <th>연결 클립</th>
                    <th>등록자</th>
                    <th>관리</th>
                  </tr>
                </thead>
                <tbody>
                  {events.map((event) => (
                    <tr key={event.eventId}>
                      <td>
                        {PLAYER_RECORD_EVENT_TYPE_LABELS[event.eventType]}
                      </td>
                      <td>
                        {formatSeconds(event.eventStartTimeSec)} ~{" "}
                        {formatSeconds(event.eventEndTimeSec)}
                      </td>
                      <td>{event.value}</td>
                      <td>{event.eventMemo || "-"}</td>
                      <td>
                        {event.clips.length === 0 ? (
                          "클립 없음"
                        ) : (
                          <ul>
                            {event.clips.map((clip) => (
                              <li key={clip.eventClipId}>
                                {
                                  PLAYER_RECORD_CLIP_SOURCE_TYPE_LABELS[
                                    clip.clipSourceType
                                  ]
                                }
                                {" - "}
                                {clip.teamClipTitle ||
                                  clip.playerClipTitle ||
                                  "제목 없음"}
                              </li>
                            ))}
                          </ul>
                        )}
                      </td>
                      <td>{event.createdByName}</td>
                      <td>
                        {canManagePlayerRecord ? (
                          <div className="button-row">
                            {permissions.canUpdatePlayerRecord && (
                              <button
                                type="button"
                                onClick={() => handleStartEditEvent(event)}
                              >
                                수정
                              </button>
                            )}

                            {permissions.canDeletePlayerRecord && (
                              <button
                                type="button"
                                onClick={() => handleDeleteEvent(event.eventId)}
                              >
                                삭제
                              </button>
                            )}
                          </div>
                        ) : (
                          "-"
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {canManagePlayerRecord && (
            <p className="helper-text">
              이벤트 신규 등록과 팀 분석 클립 또는 선수 개인 분석 클립 연결은
              경기 영상 화면에서 진행합니다.
            </p>
          )}
        </section>

        {permissions.canUpdatePlayerRecord && editingEventId !== null && (
          <section className="card">
            <h2>선수 기록 이벤트 수정</h2>

            <form className="form-grid" onSubmit={handleUpdateEventSubmit}>
              <label>
                이벤트 유형
                <select
                  value={eventEditForm.eventType}
                  onChange={(event) =>
                    handleEventEditFormChange(
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
                  value={eventEditForm.eventStartTimeSec}
                  onChange={(event) =>
                    handleEventEditFormChange(
                      "eventStartTimeSec",
                      event.target.value,
                    )
                  }
                  required
                />
              </label>

              <label>
                이벤트 종료 시간(초)
                <input
                  type="number"
                  min="1"
                  value={eventEditForm.eventEndTimeSec}
                  onChange={(event) =>
                    handleEventEditFormChange(
                      "eventEndTimeSec",
                      event.target.value,
                    )
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
                  value={eventEditForm.value}
                  onChange={(event) =>
                    handleEventEditFormChange("value", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                이벤트 메모
                <textarea
                  value={eventEditForm.eventMemo}
                  onChange={(event) =>
                    handleEventEditFormChange("eventMemo", event.target.value)
                  }
                  placeholder="이벤트 단위 메모"
                />
              </label>

              <div className="button-row">
                <button type="submit" disabled={isSubmittingEvent}>
                  이벤트 수정 저장
                </button>
                <button type="button" onClick={resetEventEditForm}>
                  취소
                </button>
              </div>
            </form>
          </section>
        )}
      </main>
    </AuthenticatedLayout>
  );
}
