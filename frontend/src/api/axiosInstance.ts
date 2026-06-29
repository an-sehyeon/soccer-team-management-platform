// 공통 axios 인스턴스와 인증 헤더 처리를 관리하는 파일

import axios from "axios";
import { getAccessToken, removeAccessToken } from "../utils/tokenStorage";

export const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

// 요청마다 Access Token이 있으면 Authorization 헤더 추가
axiosInstance.interceptors.request.use((config) => {
  const accessToken = getAccessToken();

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

// 인증 실패 시 Access Token 제거
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      removeAccessToken();
    }

    return Promise.reject(error);
  },
);
