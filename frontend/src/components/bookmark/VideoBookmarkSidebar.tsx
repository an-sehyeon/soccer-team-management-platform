// 현재 재생 중인 영상의 개인 북마크 등록, 조회, 수정, 삭제를 담당하는 사이드바 컴포넌트

import { useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";

import {
  createVideoBookmark,
  deleteVideoBookmark,
  getVideoBookmarks,
  updateVideoBookmark,
} from "../../api/videoBookmarkApi";
import type {
  CreateVideoBookmarkRequest,
  UpdateVideoBookmarkRequest,
  VideoBookmarkResponse,
} from "../../types/videoBookmark";
import { getApiErrorMessage } from "../../utils/apiError";

type VideoBookmarkSidebarProps = {
  isOpen: boolean;
  matchVideoId: number;
  teamClipId?: number;
  playerClipId?: number;
  currentTimeSec: number;
  selectedBookmarkId?: number;
  onClose: () => void;
  onSelectBookmark: (bookmark: VideoBookmarkResponse) => void;
  onBookmarkDeleted?: (bookmarkId: number) => void;
};

function formatBookmarkTime(timeSec: number) {
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

function formatDateTime(dateTime: string) {
  const date = new Date(dateTime);

  if (Number.isNaN(date.getTime())) {
    return dateTime;
  }

  return date.toLocaleString("ko-KR");
}

function getSourceLabel(teamClipId?: number, playerClipId?: number) {
  if (teamClipId !== undefined) {
    return "팀 분석 클립";
  }

  if (playerClipId !== undefined) {
    return "선수 개인 분석 클립";
  }

  return "경기 원본 영상";
}

export default function VideoBookmarkSidebar({
  isOpen,
  matchVideoId,
  teamClipId,
  playerClipId,
  currentTimeSec,
  selectedBookmarkId,
  onClose,
  onSelectBookmark,
  onBookmarkDeleted,
}: VideoBookmarkSidebarProps) {
  const [bookmarks, setBookmarks] = useState<VideoBookmarkResponse[]>([]);

  const [loadedSourceKey, setLoadedSourceKey] = useState("");

  const [createTitle, setCreateTitle] = useState("");

  const [createMemo, setCreateMemo] = useState("");

  const [editingBookmarkId, setEditingBookmarkId] = useState<number>();

  const [editTitle, setEditTitle] = useState("");

  const [editMemo, setEditMemo] = useState("");

  const [editTimeSec, setEditTimeSec] = useState("");

  const [isSubmitting, setIsSubmitting] = useState(false);

  const [errorMessage, setErrorMessage] = useState("");

  const [successMessage, setSuccessMessage] = useState("");

  const sourceKey = useMemo(
    () =>
      [matchVideoId, teamClipId ?? "match", playerClipId ?? "match"].join(":"),
    [matchVideoId, playerClipId, teamClipId],
  );

  const sourceLabel = getSourceLabel(teamClipId, playerClipId);

  const safeCurrentTimeSec =
    Number.isFinite(currentTimeSec) && currentTimeSec >= 0
      ? Math.floor(currentTimeSec)
      : 0;

  const isLoading = isOpen && loadedSourceKey !== sourceKey;

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        onClose();
      }
    }

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen, onClose]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    let ignore = false;

    async function fetchBookmarks() {
      try {
        const response = await getVideoBookmarks({
          matchVideoId,
          teamClipId,
          playerClipId,
        });

        if (ignore) {
          return;
        }

        setBookmarks(Array.isArray(response) ? response : []);
        setErrorMessage("");
      } catch (error) {
        if (ignore) {
          return;
        }

        setBookmarks([]);
        setErrorMessage(
          `북마크 목록을 불러오지 못했습니다. ${getApiErrorMessage(error)}`,
        );
      } finally {
        if (!ignore) {
          setLoadedSourceKey(sourceKey);
        }
      }
    }

    void fetchBookmarks();

    return () => {
      ignore = true;
    };
  }, [isOpen, matchVideoId, playerClipId, sourceKey, teamClipId]);

  function validateCreateForm() {
    if (!createTitle.trim()) {
      return "북마크 제목을 입력해주세요.";
    }

    if (createTitle.trim().length > 255) {
      return "북마크 제목은 255자 이하로 입력해주세요.";
    }

    if (createMemo.trim().length > 255) {
      return "북마크 메모는 255자 이하로 입력해주세요.";
    }

    return "";
  }

  function validateUpdateForm() {
    if (!editTitle.trim()) {
      return "북마크 제목을 입력해주세요.";
    }

    if (editTitle.trim().length > 255) {
      return "북마크 제목은 255자 이하로 입력해주세요.";
    }

    if (editMemo.trim().length > 255) {
      return "북마크 메모는 255자 이하로 입력해주세요.";
    }

    const parsedTimeSec = Number(editTimeSec);

    if (!Number.isInteger(parsedTimeSec) || parsedTimeSec < 0) {
      return "북마크 시간은 0 이상의 정수로 입력해주세요.";
    }

    return "";
  }

  async function handleCreateSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const validationMessage = validateCreateForm();

    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    const request: CreateVideoBookmarkRequest = {
      matchVideoId,
      teamClipId: teamClipId ?? null,
      playerClipId: playerClipId ?? null,
      bookmarkTimeSec: safeCurrentTimeSec,
      title: createTitle.trim(),
      memo: createMemo.trim() || null,
    };

    try {
      setIsSubmitting(true);
      setErrorMessage("");
      setSuccessMessage("");

      const createdBookmark = await createVideoBookmark(request);

      setBookmarks((previousBookmarks) => [
        createdBookmark,
        ...previousBookmarks,
      ]);

      setCreateTitle("");
      setCreateMemo("");

      setSuccessMessage(
        `${formatBookmarkTime(
          createdBookmark.bookmarkTimeSec,
        )} 시점에 북마크를 저장했습니다.`,
      );
    } catch (error) {
      setErrorMessage(
        `북마크를 저장하지 못했습니다. ${getApiErrorMessage(error)}`,
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleStartEdit(bookmark: VideoBookmarkResponse) {
    setEditingBookmarkId(bookmark.bookmarkId);
    setEditTitle(bookmark.title);
    setEditMemo(bookmark.memo ?? "");
    setEditTimeSec(String(bookmark.bookmarkTimeSec));
    setErrorMessage("");
    setSuccessMessage("");
  }

  function handleCancelEdit() {
    setEditingBookmarkId(undefined);
    setEditTitle("");
    setEditMemo("");
    setEditTimeSec("");
    setErrorMessage("");
  }

  async function handleUpdateSubmit(
    event: FormEvent<HTMLFormElement>,
    bookmarkId: number,
  ) {
    event.preventDefault();

    const validationMessage = validateUpdateForm();

    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }

    const request: UpdateVideoBookmarkRequest = {
      bookmarkTimeSec: Number(editTimeSec),
      title: editTitle.trim(),
      memo: editMemo.trim() || null,
    };

    try {
      setIsSubmitting(true);
      setErrorMessage("");
      setSuccessMessage("");

      const updatedBookmark = await updateVideoBookmark(bookmarkId, request);

      setBookmarks((previousBookmarks) =>
        previousBookmarks.map((bookmark) =>
          bookmark.bookmarkId === bookmarkId ? updatedBookmark : bookmark,
        ),
      );

      setEditingBookmarkId(undefined);
      setEditTitle("");
      setEditMemo("");
      setEditTimeSec("");
      setSuccessMessage("북마크가 수정되었습니다.");
    } catch (error) {
      setErrorMessage(
        `북마크를 수정하지 못했습니다. ${getApiErrorMessage(error)}`,
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete(bookmarkId: number) {
    const confirmed = window.confirm("선택한 북마크를 삭제하시겠습니까?");

    if (!confirmed) {
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");
      setSuccessMessage("");

      await deleteVideoBookmark(bookmarkId);

      setBookmarks((previousBookmarks) =>
        previousBookmarks.filter(
          (bookmark) => bookmark.bookmarkId !== bookmarkId,
        ),
      );

      if (editingBookmarkId === bookmarkId) {
        setEditingBookmarkId(undefined);
        setEditTitle("");
        setEditMemo("");
        setEditTimeSec("");
      }

      onBookmarkDeleted?.(bookmarkId);
      setSuccessMessage("북마크가 삭제되었습니다.");
    } catch (error) {
      setErrorMessage(
        `북마크를 삭제하지 못했습니다. ${getApiErrorMessage(error)}`,
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  if (!isOpen) {
    return null;
  }

  return (
    <>
      <button
        type="button"
        className="video-bookmark-backdrop"
        aria-label="북마크 사이드바 닫기"
        onClick={onClose}
      />

      <aside
        className="video-bookmark-sidebar"
        role="dialog"
        aria-modal="true"
        aria-label="영상 북마크"
      >
        <div className="video-bookmark-sidebar__header">
          <div>
            <h2>북마크</h2>
            <p>{sourceLabel}</p>
          </div>

          <button
            type="button"
            onClick={onClose}
            aria-label="북마크 사이드바 닫기"
          >
            닫기
          </button>
        </div>

        <section className="video-bookmark-sidebar__create">
          <h3>현재 시점 저장</h3>

          <p>
            현재 재생 시간:{" "}
            <strong>{formatBookmarkTime(safeCurrentTimeSec)}</strong>
          </p>

          <form onSubmit={handleCreateSubmit}>
            <label>
              북마크 제목
              <input
                type="text"
                value={createTitle}
                maxLength={255}
                onChange={(event) => setCreateTitle(event.target.value)}
                disabled={isSubmitting}
                placeholder="확인할 장면을 입력해주세요."
              />
            </label>

            <label>
              메모
              <textarea
                value={createMemo}
                maxLength={255}
                onChange={(event) => setCreateMemo(event.target.value)}
                disabled={isSubmitting}
                placeholder="선택 입력"
              />
            </label>

            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "저장 중..." : "현재 시점 북마크 저장"}
            </button>
          </form>
        </section>

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

        <section className="video-bookmark-sidebar__list">
          <h3>북마크 목록</h3>

          {isLoading && <p>북마크를 불러오는 중입니다.</p>}

          {!isLoading && bookmarks.length === 0 && (
            <p>현재 영상에 등록된 북마크가 없습니다.</p>
          )}

          {!isLoading &&
            bookmarks.map((bookmark) => {
              const isEditing = editingBookmarkId === bookmark.bookmarkId;

              const isSelected = selectedBookmarkId === bookmark.bookmarkId;

              return (
                <article
                  key={bookmark.bookmarkId}
                  className={[
                    "video-bookmark-item",
                    isSelected ? "video-bookmark-item--selected" : "",
                  ]
                    .filter(Boolean)
                    .join(" ")}
                >
                  {isEditing ? (
                    <form
                      onSubmit={(event) =>
                        handleUpdateSubmit(event, bookmark.bookmarkId)
                      }
                    >
                      <label>
                        시간(초)
                        <input
                          type="number"
                          min={0}
                          step={1}
                          value={editTimeSec}
                          onChange={(event) =>
                            setEditTimeSec(event.target.value)
                          }
                          disabled={isSubmitting}
                        />
                      </label>

                      <label>
                        제목
                        <input
                          type="text"
                          value={editTitle}
                          maxLength={255}
                          onChange={(event) => setEditTitle(event.target.value)}
                          disabled={isSubmitting}
                        />
                      </label>

                      <label>
                        메모
                        <textarea
                          value={editMemo}
                          maxLength={255}
                          onChange={(event) => setEditMemo(event.target.value)}
                          disabled={isSubmitting}
                        />
                      </label>

                      <div className="video-bookmark-item__actions">
                        <button type="submit" disabled={isSubmitting}>
                          수정 저장
                        </button>

                        <button
                          type="button"
                          onClick={handleCancelEdit}
                          disabled={isSubmitting}
                        >
                          취소
                        </button>
                      </div>
                    </form>
                  ) : (
                    <>
                      <button
                        type="button"
                        className="video-bookmark-item__select"
                        aria-pressed={isSelected}
                        onClick={() => onSelectBookmark(bookmark)}
                      >
                        <strong>
                          {formatBookmarkTime(bookmark.bookmarkTimeSec)}
                        </strong>

                        <span>{bookmark.title}</span>

                        {bookmark.memo && <small>{bookmark.memo}</small>}
                      </button>

                      <p className="video-bookmark-item__created-at">
                        등록: {formatDateTime(bookmark.createdAt)}
                      </p>

                      <div className="video-bookmark-item__actions">
                        <button
                          type="button"
                          onClick={() => handleStartEdit(bookmark)}
                          disabled={isSubmitting}
                        >
                          수정
                        </button>

                        <button
                          type="button"
                          onClick={() => handleDelete(bookmark.bookmarkId)}
                          disabled={isSubmitting}
                        >
                          삭제
                        </button>
                      </div>
                    </>
                  )}
                </article>
              );
            })}
        </section>
      </aside>
    </>
  );
}
