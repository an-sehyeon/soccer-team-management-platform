// 로그인한 사용자의 역할에 따라 스케줄 조회와 COACH 전용 관리 기능을 제공하는 화면 파일

import { useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import {
  createSchedule,
  deleteSchedule,
  getScheduleDetail,
  getSchedules,
  updateSchedule,
} from "../api/scheduleApi";
import type {
  ScheduleIntensity,
  ScheduleRequest,
  ScheduleResponse,
  ScheduleType,
} from "../types/schedule";
import { useAuth } from "../hooks/useAuth";
import { getApiErrorMessage } from "../utils/apiError";

type ScheduleFormState = {
  scheduleDateTime: string;
  place: string;
  scheduleType: ScheduleType;
  intensity: ScheduleIntensity | "";
  comment: string;
};

const scheduleTypeOptions: { value: ScheduleType; label: string }[] = [
  { value: "TRAINING", label: "훈련" },
  { value: "MATCH", label: "경기" },
  { value: "MEETING", label: "미팅" },
  { value: "EVENT", label: "팀 행사" },
  { value: "EXTERNAL", label: "외부 일정" },
  { value: "ETC", label: "기타" },
];

const intensityOptions: { value: ScheduleIntensity; label: string }[] = [
  { value: "HIGH", label: "높음" },
  { value: "MEDIUM", label: "보통" },
  { value: "LOW", label: "낮음" },
];

const initialForm: ScheduleFormState = {
  scheduleDateTime: "",
  place: "",
  scheduleType: "TRAINING",
  intensity: "",
  comment: "",
};

const formatLocalDate = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
};

const getMonthRange = (date: Date) => {
  const firstDay = new Date(date.getFullYear(), date.getMonth(), 1);
  const lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0);

  return {
    startDate: formatLocalDate(firstDay),
    endDate: formatLocalDate(lastDay),
  };
};

const toDateTimeLocalValue = (dateTime: string): string => {
  if (!dateTime) {
    return "";
  }

  return dateTime.slice(0, 16);
};

const toServerDateTime = (dateTimeLocalValue: string): string => {
  if (dateTimeLocalValue.length === 16) {
    return `${dateTimeLocalValue}:00`;
  }

  return dateTimeLocalValue;
};

const getScheduleTypeLabel = (scheduleType: ScheduleType): string => {
  return (
    scheduleTypeOptions.find((option) => option.value === scheduleType)
      ?.label ?? scheduleType
  );
};

const getIntensityLabel = (
  intensity: ScheduleIntensity | null | undefined,
): string => {
  if (!intensity) {
    return "없음";
  }

  return (
    intensityOptions.find((option) => option.value === intensity)?.label ??
    intensity
  );
};

const formatScheduleDateTime = (dateTime: string): string => {
  if (!dateTime) {
    return "-";
  }

  return new Date(dateTime).toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

export default function SchedulePage() {
  const { member } = useAuth();

  const isCoach = member?.memberRole === "COACH";

  const initialMonthRange = useMemo(() => getMonthRange(new Date()), []);

  const [startDate, setStartDate] = useState(initialMonthRange.startDate);
  const [endDate, setEndDate] = useState(initialMonthRange.endDate);
  const [schedules, setSchedules] = useState<ScheduleResponse[]>([]);
  const [selectedSchedule, setSelectedSchedule] =
    useState<ScheduleResponse | null>(null);
  const [form, setForm] = useState<ScheduleFormState>(initialForm);
  const [editingScheduleId, setEditingScheduleId] = useState<number | null>(
    null,
  );
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    let isMounted = true;

    const fetchInitialSchedules = async () => {
      try {
        const response = await getSchedules({
          startDate: initialMonthRange.startDate,
          endDate: initialMonthRange.endDate,
        });

        if (!isMounted) {
          return;
        }

        setSchedules(Array.isArray(response) ? response : []);
      } catch (error) {
        if (!isMounted) {
          return;
        }

        setErrorMessage(getApiErrorMessage(error));
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void fetchInitialSchedules();

    return () => {
      isMounted = false;
    };
  }, [initialMonthRange.startDate, initialMonthRange.endDate]);

  const handleSearch = async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await getSchedules({
        startDate,
        endDate,
      });

      setSchedules(Array.isArray(response) ? response : []);
      setSelectedSchedule(null);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (
    field: keyof ScheduleFormState,
    value: ScheduleFormState[keyof ScheduleFormState],
  ) => {
    setForm((prevForm) => ({
      ...prevForm,
      [field]: value,
    }));
  };

  const createRequestBody = (): ScheduleRequest => {
    return {
      scheduleDateTime: toServerDateTime(form.scheduleDateTime),
      place: form.place.trim(),
      scheduleType: form.scheduleType,
      intensity: form.intensity === "" ? null : form.intensity,
      comment: form.comment.trim() === "" ? null : form.comment.trim(),
    };
  };

  const resetForm = () => {
    setForm(initialForm);
    setEditingScheduleId(null);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!isCoach) {
      return;
    }

    setIsSaving(true);
    setErrorMessage("");

    try {
      const requestBody = createRequestBody();

      if (editingScheduleId) {
        await updateSchedule(editingScheduleId, requestBody);
      } else {
        await createSchedule(requestBody);
      }

      resetForm();
      await handleSearch();
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsSaving(false);
    }
  };

  const handleSelectSchedule = async (scheduleId: number) => {
    setErrorMessage("");

    try {
      const response = await getScheduleDetail(scheduleId);
      setSelectedSchedule(response);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    }
  };

  const handleEdit = (schedule: ScheduleResponse) => {
    if (!isCoach) {
      return;
    }

    setEditingScheduleId(schedule.scheduleId);
    setForm({
      scheduleDateTime: toDateTimeLocalValue(schedule.scheduleDateTime),
      place: schedule.place,
      scheduleType: schedule.scheduleType,
      intensity: schedule.intensity ?? "",
      comment: schedule.comment ?? "",
    });
  };

  const handleDelete = async (scheduleId: number) => {
    if (!isCoach) {
      return;
    }

    const isConfirmed = window.confirm("스케줄을 삭제하시겠습니까?");

    if (!isConfirmed) {
      return;
    }

    setIsSaving(true);
    setErrorMessage("");

    try {
      await deleteSchedule(scheduleId);

      if (selectedSchedule?.scheduleId === scheduleId) {
        setSelectedSchedule(null);
      }

      if (editingScheduleId === scheduleId) {
        resetForm();
      }

      await handleSearch();
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <main className="page-container">
      <section className="page-header">
        <div>
          <p className="page-subtitle">Schedule</p>
          <h1>스케줄</h1>
          <p className="page-description">
            팀 훈련, 경기, 미팅, 행사 일정을 확인합니다.
            {isCoach && " 지도자는 스케줄을 등록, 수정, 삭제할 수 있습니다."}
          </p>
        </div>
      </section>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      <section className="card">
        <div className="card-header">
          <h2>조회 기간</h2>
          <button type="button" onClick={handleSearch} disabled={isLoading}>
            {isLoading ? "조회 중" : "조회"}
          </button>
        </div>

        <div className="form-row">
          <label>
            시작일
            <input
              type="date"
              value={startDate}
              onChange={(event) => setStartDate(event.target.value)}
            />
          </label>

          <label>
            종료일
            <input
              type="date"
              value={endDate}
              onChange={(event) => setEndDate(event.target.value)}
            />
          </label>
        </div>
      </section>

      {isCoach && (
        <section className="card">
          <div className="card-header">
            <h2>{editingScheduleId ? "스케줄 수정" : "스케줄 등록"}</h2>
            {editingScheduleId && (
              <button type="button" onClick={resetForm} disabled={isSaving}>
                수정 취소
              </button>
            )}
          </div>

          <form onSubmit={handleSubmit} className="schedule-form">
            <div className="form-row">
              <label>
                일정 날짜와 시간
                <input
                  type="datetime-local"
                  value={form.scheduleDateTime}
                  onChange={(event) =>
                    handleChange("scheduleDateTime", event.target.value)
                  }
                  required
                />
              </label>

              <label>
                장소
                <input
                  type="text"
                  value={form.place}
                  onChange={(event) =>
                    handleChange("place", event.target.value)
                  }
                  maxLength={30}
                  required
                />
              </label>
            </div>

            <div className="form-row">
              <label>
                일정 유형
                <select
                  value={form.scheduleType}
                  onChange={(event) =>
                    handleChange(
                      "scheduleType",
                      event.target.value as ScheduleType,
                    )
                  }
                  required
                >
                  {scheduleTypeOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                운동 강도
                <select
                  value={form.intensity}
                  onChange={(event) =>
                    handleChange(
                      "intensity",
                      event.target.value as ScheduleIntensity | "",
                    )
                  }
                >
                  <option value="">없음</option>
                  {intensityOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <label>
              상세 내용
              <textarea
                value={form.comment}
                onChange={(event) =>
                  handleChange("comment", event.target.value)
                }
                maxLength={255}
                rows={4}
              />
            </label>

            <button type="submit" disabled={isSaving}>
              {isSaving
                ? "저장 중"
                : editingScheduleId
                  ? "수정하기"
                  : "등록하기"}
            </button>
          </form>
        </section>
      )}

      <section className="card">
        <div className="card-header">
          <h2>스케줄 목록</h2>
          <span>{schedules.length}개</span>
        </div>

        {isLoading ? (
          <p className="empty-message">스케줄을 조회하고 있습니다.</p>
        ) : schedules.length === 0 ? (
          <p className="empty-message">조회된 스케줄이 없습니다.</p>
        ) : (
          <ul className="schedule-list">
            {schedules.map((schedule) => (
              <li key={schedule.scheduleId} className="schedule-item">
                <button
                  type="button"
                  className="schedule-content"
                  onClick={() => void handleSelectSchedule(schedule.scheduleId)}
                >
                  <strong>{getScheduleTypeLabel(schedule.scheduleType)}</strong>
                  <span>
                    {formatScheduleDateTime(schedule.scheduleDateTime)}
                  </span>
                  <span>{schedule.place}</span>
                  <span>강도: {getIntensityLabel(schedule.intensity)}</span>
                  {schedule.comment && <p>{schedule.comment}</p>}
                </button>

                {isCoach && (
                  <div className="schedule-actions">
                    <button
                      type="button"
                      onClick={() => handleEdit(schedule)}
                      disabled={isSaving}
                    >
                      수정
                    </button>
                    <button
                      type="button"
                      onClick={() => void handleDelete(schedule.scheduleId)}
                      disabled={isSaving}
                    >
                      삭제
                    </button>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>

      {selectedSchedule && (
        <section className="card">
          <div className="card-header">
            <h2>스케줄 상세</h2>
          </div>

          <dl className="detail-list">
            <div>
              <dt>일정 유형</dt>
              <dd>{getScheduleTypeLabel(selectedSchedule.scheduleType)}</dd>
            </div>

            <div>
              <dt>일정 날짜와 시간</dt>
              <dd>
                {formatScheduleDateTime(selectedSchedule.scheduleDateTime)}
              </dd>
            </div>

            <div>
              <dt>장소</dt>
              <dd>{selectedSchedule.place}</dd>
            </div>

            <div>
              <dt>운동 강도</dt>
              <dd>{getIntensityLabel(selectedSchedule.intensity)}</dd>
            </div>

            <div>
              <dt>상세 내용</dt>
              <dd>{selectedSchedule.comment ?? "없음"}</dd>
            </div>
          </dl>
        </section>
      )}
    </main>
  );
}
