// 공지사항 화면과 API 연동에서 사용하는 요청, 응답, 페이징 타입을 정의하는 파일

export interface NoticeListResponse {
  noticeId: number;
  title: string;
  isImportant: boolean;
  writerId: number;
  writerName: string;
  createdAt: string;
  updatedAt: string;
}

export interface NoticeDetailResponse {
  noticeId: number;
  title: string;
  content: string;
  isImportant: boolean;
  writerId: number;
  writerName: string;
  createdAt: string;
  updatedAt: string;
}

export interface NoticePageResponse {
  notices: NoticeListResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface NoticeListSearchParams {
  page: number;
  size: number;
  importantOnly?: boolean;
}

export interface CreateNoticeRequest {
  title: string;
  content: string;
  isImportant: boolean;
}

export interface UpdateNoticeRequest {
  title: string;
  content: string;
  isImportant: boolean;
}
