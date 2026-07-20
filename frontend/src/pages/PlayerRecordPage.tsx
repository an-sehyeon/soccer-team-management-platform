// 선수 기록 목록, 상세와 연결된 선수 기록 이벤트를 조회하는 페이지 컴포넌트

import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import {
  getManagementPlayerRecordEventDetail,
  getManagementPlayerRecordEvents,
  getMyPlayerRecordEventDetail,
  getMyPlayerRecordEvents,
} from "../api/playerRecordEventApi";
import {
  getManagementPlayerRecordDetail,
  getManagementPlayerRecords,
  getMyPlayerRecordDetail,
  getMyPlayerRecords,
} from "../api/playerRecordApi";
import { useAuth } from "../hooks/useAuth";
import { AuthenticatedLayout } from "../layouts/AuthenticatedLayout";
import {
  PLAYER_RECORD_STAT_LABELS,
  type PlayerRecordDetailResponse,
  type PlayerRecordListItem,
} from "../types/playerRecord";
import {
  PLAYER_RECORD_CLIP_SOURCE_TYPE_LABELS,
  PLAYER_RECORD_EVENT_TYPE_LABELS,
  type PlayerRecordEventResponse,
} from "../types/playerRecordEvent";
import { getApiErrorMessage } from "../utils/apiError";

const RECORD_PAGE_SIZE = 20;

type AppliedSearchFilters = {
  uploadId?: number;
  playerId?: number;
};

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString();
}

function formatSeconds(value: number) {
  const safeValue = Number.isFinite(value) ? Math.max(0, value) : 0;
  const minutes = Math.floor(safeValue / 60);
  const seconds = Math.floor(safeValue % 60);

  return `${minutes}:${String(seconds).padStart(2, "0")}`;
}

function parseOptionalPositiveInteger(value: string) {
  const trimmedValue = value.trim();

  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);

  if (!Number.isInteger(parsedValue) || parsedValue <= 0) {
    return undefined;
  }

  return parsedValue;
}

export default function PlayerRecordPage() {
  const { member } = useAuth();

  const isPlayer = member?.memberRole === "PLAYER";

  const [records, setRecords] = useState<PlayerRecordListItem[]>([]);
  const [selectedRecordId, setSelectedRecordId] = useState<number | null>(null);
  const [selectedRecord, setSelectedRecord] =
    useState<PlayerRecordDetailResponse | null>(null);

  const [events, setEvents] = useState<PlayerRecordEventResponse[]>([]);
  const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
  const [selectedEvent, setSelectedEvent] =
    useState<PlayerRecordEventResponse | null>(null);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [filterUploadId, setFilterUploadId] = useState("");
  const [filterPlayerId, setFilterPlayerId] = useState("");
  const [appliedFilters, setAppliedFilters] = useState<AppliedSearchFilters>(
    {},
  );

  const [recordListLoading, setRecordListLoading] = useState(true);
  const [recordDetailLoading, setRecordDetailLoading] = useState(false);
  const [eventDetailLoading, setEventDetailLoading] = useState(false);

  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;

    async function loadRecords() {
      setRecordListLoading(true);

      try {
        const response = isPlayer
          ? await getMyPlayerRecords(page, RECORD_PAGE_SIZE)
          : await getManagementPlayerRecords({
              page,
              size: RECORD_PAGE_SIZE,
              uploadId: appliedFilters.uploadId,
              playerId: appliedFilters.playerId,
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

          const currentRecordExists =
            currentRecordId !== null &&
            response.records.some(
              (record) => record.recordId === currentRecordId,
            );

          return currentRecordExists
            ? currentRecordId
            : response.records[0].recordId;
        });
      } catch (error) {
        if (ignore) {
          return;
        }

        setRecords([]);
        setTotalPages(0);
        setSelectedRecordId(null);
        setSelectedRecord(null);
        setEvents([]);
        setSelectedEventId(null);
        setSelectedEvent(null);
        setErrorMessage(
          `선수 기록 목록을 불러오지 못했습니다. ${getApiErrorMessage(error)}`,
        );
      } finally {
        if (!ignore) {
          setRecordListLoading(false);
        }
      }
    }

    void loadRecords();

    return () => {
      ignore = true;
    };
  }, [appliedFilters.playerId, appliedFilters.uploadId, isPlayer, page]);

  useEffect(() => {
    let ignore = false;

    async function loadSelectedRecord() {
      if (selectedRecordId === null) {
        setSelectedRecord(null);
        setEvents([]);
        setSelectedEventId(null);
        setSelectedEvent(null);
        setRecordDetailLoading(false);
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

        setSelectedEventId((currentEventId) => {
          if (recordEventsResponse.events.length === 0) {
            return null;
          }

          const currentEventExists =
            currentEventId !== null &&
            recordEventsResponse.events.some(
              (recordEvent) => recordEvent.eventId === currentEventId,
            );

          return currentEventExists
            ? currentEventId
            : recordEventsResponse.events[0].eventId;
        });
      } catch (error) {
        if (ignore) {
          return;
        }

        setSelectedRecord(null);
        setEvents([]);
        setSelectedEventId(null);
        setSelectedEvent(null);
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

    void loadSelectedRecord();

    return () => {
      ignore = true;
    };
  }, [isPlayer, selectedRecordId]);

  useEffect(() => {
    let ignore = false;

    async function loadSelectedEvent() {
      if (selectedEventId === null) {
        setSelectedEvent(null);
        setEventDetailLoading(false);
        return;
      }

      setEventDetailLoading(true);

      try {
        const response = isPlayer
          ? await getMyPlayerRecordEventDetail(selectedEventId)
          : await getManagementPlayerRecordEventDetail(selectedEventId);

        if (ignore) {
          return;
        }

        setSelectedEvent(response);
        setErrorMessage(null);
      } catch (error) {
        if (ignore) {
          return;
        }

        setSelectedEvent(null);
        setErrorMessage(
          `선수 기록 이벤트 상세 정보를 불러오지 못했습니다. ${getApiErrorMessage(
            error,
          )}`,
        );
      } finally {
        if (!ignore) {
          setEventDetailLoading(false);
        }
      }
    }

    void loadSelectedEvent();

    return () => {
      ignore = true;
    };
  }, [isPlayer, selectedEventId]);

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const uploadId = parseOptionalPositiveInteger(filterUploadId);
    const playerId = parseOptionalPositiveInteger(filterPlayerId);

    if (filterUploadId.trim() && uploadId === undefined) {
      setErrorMessage("경기 영상 ID는 1 이상의 정수로 입력하세요.");
      return;
    }

    if (filterPlayerId.trim() && playerId === undefined) {
      setErrorMessage("선수 ID는 1 이상의 정수로 입력하세요.");
      return;
    }

    setErrorMessage(null);
    setPage(0);
    setSelectedRecordId(null);
    setSelectedRecord(null);
    setEvents([]);
    setSelectedEventId(null);
    setSelectedEvent(null);
    setAppliedFilters({
      uploadId,
      playerId,
    });
  }

  function handleResetSearch() {
    setFilterUploadId("");
    setFilterPlayerId("");
    setAppliedFilters({});
    setPage(0);
    setSelectedRecordId(null);
    setSelectedRecord(null);
    setEvents([]);
    setSelectedEventId(null);
    setSelectedEvent(null);
    setErrorMessage(null);
  }

  function handleSelectRecord(recordId: number) {
    setSelectedRecordId(recordId);
    setSelectedEventId(null);
    setSelectedEvent(null);
    setErrorMessage(null);
  }

  function handleSelectEvent(eventId: number) {
    setSelectedEventId(eventId);
    setErrorMessage(null);
  }

  return (
    <AuthenticatedLayout title="선수 기록">
      <main className="page">
        <section className="page-header">
          <div>
            <h1>선수 기록</h1>
            <p>
              경기별 선수 요약 기록과 분석 클립에 연결된 선수 기록 이벤트를
              조회합니다.
            </p>
          </div>
        </section>

        {errorMessage && (
          <section className="error-message" role="alert">
            {errorMessage}
          </section>
        )}

        {!isPlayer && (
          <section className="card">
            <h2>선수 기록 검색</h2>

            <form className="form-grid" onSubmit={handleSearchSubmit}>
              <label>
                경기 영상 ID
                <input
                  type="number"
                  min="1"
                  step="1"
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
                  step="1"
                  value={filterPlayerId}
                  onChange={(event) => setFilterPlayerId(event.target.value)}
                  placeholder="전체"
                />
              </label>

              <div className="button-row">
                <button type="submit">검색</button>

                <button type="button" onClick={handleResetSearch}>
                  초기화
                </button>
              </div>
            </form>
          </section>
        )}

        <section className="card">
          <div className="section-heading-row">
            <div>
              <h2>{isPlayer ? "내 기록 목록" : "선수 기록 목록"}</h2>

              {!isPlayer && (
                <p className="helper-text">
                  선수 기록 등록과 분석 클립 연결은 경기 영상 화면에서
                  진행합니다.
                </p>
              )}
            </div>
          </div>

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
                      <th>최초 기록자</th>
                      <th>마지막 수정자</th>
                      <th>수정일</th>
                      <th>상세</th>
                    </tr>
                  </thead>

                  <tbody>
                    {records.map((record) => (
                      <tr key={record.recordId}>
                        <td>{record.matchVideoTitle}</td>
                        <td>{record.playerName}</td>
                        <td>{record.recorderName}</td>
                        <td>{record.lastModifierName || "-"}</td>
                        <td>{formatDateTime(record.updatedAt)}</td>
                        <td>
                          <button
                            type="button"
                            aria-pressed={selectedRecordId === record.recordId}
                            onClick={() => handleSelectRecord(record.recordId)}
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
                  disabled={page <= 0}
                  onClick={() =>
                    setPage((currentPage) => Math.max(0, currentPage - 1))
                  }
                >
                  이전
                </button>

                <span>
                  {page + 1} / {Math.max(totalPages, 1)}
                </span>

                <button
                  type="button"
                  disabled={totalPages === 0 || page + 1 >= totalPages}
                  onClick={() => setPage((currentPage) => currentPage + 1)}
                >
                  다음
                </button>
              </div>
            </>
          )}
        </section>

        <section className="card">
          <h2>선수 기록 상세</h2>

          {recordDetailLoading ? (
            <p>선수 기록 상세 정보를 불러오는 중입니다.</p>
          ) : selectedRecord === null ? (
            <p>선택된 선수 기록이 없습니다.</p>
          ) : (
            <>
              <dl className="detail-grid">
                <div>
                  <dt>기록 ID</dt>
                  <dd>{selectedRecord.recordId}</dd>
                </div>

                <div>
                  <dt>경기</dt>
                  <dd>{selectedRecord.matchVideoTitle}</dd>
                </div>

                <div>
                  <dt>경기 영상 ID</dt>
                  <dd>{selectedRecord.uploadId}</dd>
                </div>

                <div>
                  <dt>선수</dt>
                  <dd>{selectedRecord.playerName}</dd>
                </div>

                <div>
                  <dt>선수 ID</dt>
                  <dd>{selectedRecord.playerId}</dd>
                </div>

                <div>
                  <dt>최초 기록자</dt>
                  <dd>{selectedRecord.recorderName}</dd>
                </div>

                <div>
                  <dt>마지막 수정자</dt>
                  <dd>{selectedRecord.lastModifierName || "-"}</dd>
                </div>

                <div>
                  <dt>생성일</dt>
                  <dd>{formatDateTime(selectedRecord.createdAt)}</dd>
                </div>

                <div>
                  <dt>수정일</dt>
                  <dd>{formatDateTime(selectedRecord.updatedAt)}</dd>
                </div>
              </dl>

              <h3>경기 기록</h3>

              <dl className="detail-grid">
                {PLAYER_RECORD_STAT_LABELS.map((stat) => (
                  <div key={stat.key}>
                    <dt>{stat.label}</dt>
                    <dd>
                      {selectedRecord[stat.key]}
                      {stat.key === "minutesPlayed" ? "분" : ""}
                    </dd>
                  </div>
                ))}
              </dl>

              <div>
                <h3>경기 전체 메모</h3>
                <p>{selectedRecord.memo || "-"}</p>
              </div>
            </>
          )}
        </section>

        <section className="card">
          <h2>선수 기록 이벤트</h2>

          {recordDetailLoading ? (
            <p>선수 기록 이벤트를 불러오는 중입니다.</p>
          ) : selectedRecord === null ? (
            <p>선수 기록을 먼저 선택하세요.</p>
          ) : events.length === 0 ? (
            <p>연결된 선수 기록 이벤트가 없습니다.</p>
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
                    <th>등록일</th>
                    <th>상세</th>
                  </tr>
                </thead>

                <tbody>
                  {events.map((recordEvent) => (
                    <tr key={recordEvent.eventId}>
                      <td>
                        {PLAYER_RECORD_EVENT_TYPE_LABELS[recordEvent.eventType]}
                      </td>

                      <td>
                        {formatSeconds(recordEvent.eventStartTimeSec)}
                        {" ~ "}
                        {formatSeconds(recordEvent.eventEndTimeSec)}
                      </td>

                      <td>{recordEvent.value}</td>
                      <td>{recordEvent.eventMemo || "-"}</td>

                      <td>
                        {recordEvent.clips.length === 0 ? (
                          "연결 클립 없음"
                        ) : (
                          <ul>
                            {recordEvent.clips.map((clip) => (
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

                      <td>{recordEvent.createdByName}</td>
                      <td>{formatDateTime(recordEvent.createdAt)}</td>

                      <td>
                        <button
                          type="button"
                          aria-pressed={selectedEventId === recordEvent.eventId}
                          onClick={() => handleSelectEvent(recordEvent.eventId)}
                        >
                          보기
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="card">
          <h2>선수 기록 이벤트 상세</h2>

          {eventDetailLoading ? (
            <p>선수 기록 이벤트 상세 정보를 불러오는 중입니다.</p>
          ) : selectedEvent === null ? (
            <p>선택된 선수 기록 이벤트가 없습니다.</p>
          ) : (
            <>
              <dl className="detail-grid">
                <div>
                  <dt>이벤트 ID</dt>
                  <dd>{selectedEvent.eventId}</dd>
                </div>

                <div>
                  <dt>기록 ID</dt>
                  <dd>{selectedEvent.recordId}</dd>
                </div>

                <div>
                  <dt>경기</dt>
                  <dd>{selectedEvent.matchVideoTitle}</dd>
                </div>

                <div>
                  <dt>선수</dt>
                  <dd>{selectedEvent.playerName}</dd>
                </div>

                <div>
                  <dt>이벤트 유형</dt>
                  <dd>
                    {PLAYER_RECORD_EVENT_TYPE_LABELS[selectedEvent.eventType]}
                  </dd>
                </div>

                <div>
                  <dt>이벤트 시간</dt>
                  <dd>
                    {formatSeconds(selectedEvent.eventStartTimeSec)}
                    {" ~ "}
                    {formatSeconds(selectedEvent.eventEndTimeSec)}
                  </dd>
                </div>

                <div>
                  <dt>기록 반영값</dt>
                  <dd>{selectedEvent.value}</dd>
                </div>

                <div>
                  <dt>등록자</dt>
                  <dd>{selectedEvent.createdByName}</dd>
                </div>

                <div>
                  <dt>생성일</dt>
                  <dd>{formatDateTime(selectedEvent.createdAt)}</dd>
                </div>

                <div>
                  <dt>수정일</dt>
                  <dd>{formatDateTime(selectedEvent.updatedAt)}</dd>
                </div>
              </dl>

              <div>
                <h3>이벤트 메모</h3>
                <p>{selectedEvent.eventMemo || "-"}</p>
              </div>

              <div>
                <h3>연결 클립</h3>

                {selectedEvent.clips.length === 0 ? (
                  <p>연결된 분석 클립이 없습니다.</p>
                ) : (
                  <ul>
                    {selectedEvent.clips.map((clip) => (
                      <li key={clip.eventClipId}>
                        <strong>
                          {
                            PLAYER_RECORD_CLIP_SOURCE_TYPE_LABELS[
                              clip.clipSourceType
                            ]
                          }
                        </strong>
                        {" - "}
                        {clip.teamClipTitle ||
                          clip.playerClipTitle ||
                          "제목 없음"}
                        {" ("}
                        {clip.teamClipId !== null
                          ? `팀 클립 ID: ${clip.teamClipId}`
                          : `선수 클립 ID: ${clip.playerClipId}`}
                        {")"}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </>
          )}
        </section>
      </main>
    </AuthenticatedLayout>
  );
}
