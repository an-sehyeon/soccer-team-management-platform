// 공지사항 목록 조회와 COACH 전용 등록, 수정, 삭제 화면을 제공하는 파일

import { useContext, useEffect, useState } from "react";
import {
  createNotice,
  deleteNotice,
  getNoticeDetail,
  getNotices,
  updateNotice,
} from "../api/noticeApi";
import { AuthContext } from "../contexts/authContext";
import type {
  CreateNoticeRequest,
  NoticeDetailResponse,
  NoticeListResponse,
  UpdateNoticeRequest,
} from "../types/notice";

const NOTICE_PAGE_SIZE = 10;

const EMPTY_NOTICE_FORM: CreateNoticeRequest = {
  title: "",
  content: "",
  isImportant: false,
};

function NoticePage() {
  const authContext = useContext(AuthContext);
  const member = authContext?.member ?? null;

  const [notices, setNotices] = useState<NoticeListResponse[]>([]);
  const [selectedNotice, setSelectedNotice] =
    useState<NoticeDetailResponse | null>(null);

  const [noticeForm, setNoticeForm] =
    useState<CreateNoticeRequest>(EMPTY_NOTICE_FORM);

  const [editingNoticeId, setEditingNoticeId] = useState<number | null>(null);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [importantOnly, setImportantOnly] = useState(false);

  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const isCoach = member?.memberRole === "COACH";

  const fetchNoticeDetail = async (noticeId: number) => {
    try {
      setErrorMessage("");
      setSuccessMessage("");

      const response = await getNoticeDetail(noticeId);

      setSelectedNotice(response);
    } catch (error) {
      console.error(error);
      setErrorMessage("공지사항 상세 정보를 불러오지 못했습니다.");
    }
  };

  useEffect(() => {
    let isMounted = true;

    getNotices({
      page,
      size: NOTICE_PAGE_SIZE,
      importantOnly,
    })
      .then((response) => {
        if (!isMounted) {
          return;
        }

        setNotices(response.notices);
        setTotalPages(response.totalPages);
        setErrorMessage("");
      })
      .catch((error) => {
        if (!isMounted) {
          return;
        }

        console.error(error);
        setErrorMessage("공지사항 목록을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!isMounted) {
          return;
        }

        setIsLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [page, importantOnly]);

  const reloadNotices = async () => {
    try {
      setIsLoading(true);

      const response = await getNotices({
        page,
        size: NOTICE_PAGE_SIZE,
        importantOnly,
      });

      setNotices(response.notices);
      setTotalPages(response.totalPages);
      setErrorMessage("");
    } catch (error) {
      console.error(error);
      setErrorMessage("공지사항 목록을 불러오지 못했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleChangeTitle = (title: string) => {
    setNoticeForm((previousForm) => ({
      ...previousForm,
      title,
    }));
  };

  const handleChangeContent = (content: string) => {
    setNoticeForm((previousForm) => ({
      ...previousForm,
      content,
    }));
  };

  const handleChangeImportant = (isImportant: boolean) => {
    setNoticeForm((previousForm) => ({
      ...previousForm,
      isImportant,
    }));
  };

  const resetForm = () => {
    setNoticeForm(EMPTY_NOTICE_FORM);
    setEditingNoticeId(null);
  };

  const validateNoticeForm = () => {
    if (!noticeForm.title.trim()) {
      setErrorMessage("공지사항 제목을 입력해주세요.");
      return false;
    }

    if (!noticeForm.content.trim()) {
      setErrorMessage("공지사항 내용을 입력해주세요.");
      return false;
    }

    return true;
  };

  const handleSubmitNotice = async (event: { preventDefault: () => void }) => {
    event.preventDefault();

    if (!isCoach) {
      setErrorMessage("공지사항을 등록하거나 수정할 권한이 없습니다.");
      return;
    }

    if (!validateNoticeForm()) {
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");
      setSuccessMessage("");

      if (editingNoticeId) {
        const request: UpdateNoticeRequest = {
          title: noticeForm.title.trim(),
          content: noticeForm.content.trim(),
          isImportant: noticeForm.isImportant,
        };

        const updatedNotice = await updateNotice(editingNoticeId, request);

        setSelectedNotice(updatedNotice);
        setSuccessMessage("공지사항이 수정되었습니다.");
      } else {
        const request: CreateNoticeRequest = {
          title: noticeForm.title.trim(),
          content: noticeForm.content.trim(),
          isImportant: noticeForm.isImportant,
        };

        const createdNotice = await createNotice(request);

        setSelectedNotice(createdNotice);
        setSuccessMessage("공지사항이 등록되었습니다.");
      }

      resetForm();
      await reloadNotices();
    } catch (error) {
      console.error(error);
      setErrorMessage("공지사항 저장에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleStartEdit = (notice: NoticeDetailResponse) => {
    if (!isCoach) {
      return;
    }

    setEditingNoticeId(notice.noticeId);
    setNoticeForm({
      title: notice.title,
      content: notice.content,
      isImportant: notice.isImportant,
    });
    setSuccessMessage("");
    setErrorMessage("");
  };

  const handleDeleteNotice = async (noticeId: number) => {
    if (!isCoach) {
      setErrorMessage("공지사항을 삭제할 권한이 없습니다.");
      return;
    }

    const confirmed = window.confirm("공지사항을 삭제하시겠습니까?");

    if (!confirmed) {
      return;
    }

    try {
      setErrorMessage("");
      setSuccessMessage("");

      await deleteNotice(noticeId);

      setSelectedNotice(null);
      resetForm();
      setSuccessMessage("공지사항이 삭제되었습니다.");
      await reloadNotices();
    } catch (error) {
      console.error(error);
      setErrorMessage("공지사항 삭제에 실패했습니다.");
    }
  };

  const handleChangeImportantOnly = (checked: boolean) => {
    setIsLoading(true);
    setImportantOnly(checked);
    setPage(0);
    setSelectedNotice(null);
    setSuccessMessage("");
    setErrorMessage("");
  };

  const handleMovePreviousPage = () => {
    setIsLoading(true);
    setPage((previousPage) => Math.max(previousPage - 1, 0));
    setSelectedNotice(null);
  };

  const handleMoveNextPage = () => {
    setIsLoading(true);

    setPage((previousPage) => {
      if (totalPages === 0) {
        return previousPage;
      }

      return Math.min(previousPage + 1, totalPages - 1);
    });

    setSelectedNotice(null);
  };

  return (
    <main className="page-container">
      <section className="page-header">
        <div>
          <h1>공지사항</h1>
          <p>팀 공지와 중요 전달 사항을 확인합니다.</p>
        </div>
      </section>

      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {successMessage && <p className="success-message">{successMessage}</p>}

      {isCoach && (
        <section className="card">
          <h2>{editingNoticeId ? "공지사항 수정" : "공지사항 등록"}</h2>

          <form onSubmit={handleSubmitNotice} className="form-section">
            <label>
              제목
              <input
                type="text"
                value={noticeForm.title}
                onChange={(event) => handleChangeTitle(event.target.value)}
                placeholder="공지사항 제목을 입력하세요."
              />
            </label>

            <label>
              내용
              <textarea
                value={noticeForm.content}
                onChange={(event) => handleChangeContent(event.target.value)}
                placeholder="공지사항 내용을 입력하세요."
                rows={6}
              />
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={noticeForm.isImportant}
                onChange={(event) =>
                  handleChangeImportant(event.target.checked)
                }
              />
              중요 공지로 등록
            </label>

            <div className="button-row">
              <button type="submit" disabled={isSubmitting}>
                {editingNoticeId ? "수정 저장" : "공지 등록"}
              </button>

              {editingNoticeId && (
                <button type="button" onClick={resetForm}>
                  수정 취소
                </button>
              )}
            </div>
          </form>
        </section>
      )}

      <section className="card">
        <div className="section-header">
          <h2>공지사항 목록</h2>

          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={importantOnly}
              onChange={(event) =>
                handleChangeImportantOnly(event.target.checked)
              }
            />
            중요 공지만 보기
          </label>
        </div>

        {isLoading ? (
          <p>공지사항을 불러오는 중입니다.</p>
        ) : notices.length === 0 ? (
          <p>등록된 공지사항이 없습니다.</p>
        ) : (
          <ul className="item-list">
            {notices.map((notice) => (
              <li key={notice.noticeId} className="item-card">
                <button
                  type="button"
                  onClick={() => fetchNoticeDetail(notice.noticeId)}
                  className="item-button"
                >
                  <div>
                    <strong>
                      {notice.isImportant && "[중요] "}
                      {notice.title}
                    </strong>
                    <p>
                      작성자: {notice.writerName} · 작성일: {notice.createdAt}
                    </p>
                  </div>
                </button>
              </li>
            ))}
          </ul>
        )}

        <div className="button-row">
          <button
            type="button"
            onClick={handleMovePreviousPage}
            disabled={page === 0}
          >
            이전
          </button>

          <span>
            {totalPages === 0 ? 0 : page + 1} / {totalPages}
          </span>

          <button
            type="button"
            onClick={handleMoveNextPage}
            disabled={totalPages === 0 || page >= totalPages - 1}
          >
            다음
          </button>
        </div>
      </section>

      {selectedNotice && (
        <section className="card">
          <div className="section-header">
            <h2>
              {selectedNotice.isImportant && "[중요] "}
              {selectedNotice.title}
            </h2>

            {isCoach && (
              <div className="button-row">
                <button
                  type="button"
                  onClick={() => handleStartEdit(selectedNotice)}
                >
                  수정
                </button>

                <button
                  type="button"
                  onClick={() => handleDeleteNotice(selectedNotice.noticeId)}
                >
                  삭제
                </button>
              </div>
            )}
          </div>

          <p>
            작성자: {selectedNotice.writerName} · 작성일:{" "}
            {selectedNotice.createdAt}
          </p>

          <p>수정일: {selectedNotice.updatedAt}</p>

          <div className="content-box" style={{ whiteSpace: "pre-wrap" }}>
            {selectedNotice.content}
          </div>
        </section>
      )}
    </main>
  );
}

export default NoticePage;
