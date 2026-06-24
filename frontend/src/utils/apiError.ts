// API 요청 실패 시 사용자에게 보여줄 에러 메시지를 추출하는 파일

import axios from "axios";

interface ApiErrorResponse {
  message?: string;
}

// API 에러 메시지 추출
export function getApiErrorMessage(error: unknown) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    return error.response?.data?.message ?? "요청 처리 중 오류가 발생했습니다.";
  }

  return "알 수 없는 오류가 발생했습니다.";
}
