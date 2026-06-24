// 백엔드 API 요청에 공통 설정과 Access Token을 적용하는 Axios 인스턴스 파일

import axios from "axios";
import { getAccessToken, removeAccessToken } from "../utils/tokenStorage";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// 백엔드 API 공통 Axios 인스턴스
export const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청마다 Access Token을 Authorization 헤더에 추가
axiosInstance.interceptors.request.use((config) => {
  const accessToken = getAccessToken();

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

// 인증 실패 응답을 받으면 저장된 토큰 삭제
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      removeAccessToken();
    }

    return Promise.reject(error);
  },
);
