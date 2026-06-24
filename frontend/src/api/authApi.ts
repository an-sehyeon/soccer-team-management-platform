// 로그인, 회원가입, 내 정보 조회 API 요청 함수를 관리하는 파일

import { axiosInstance } from "./axiosInstance";
import type {
  LoginRequest,
  LoginResponse,
  MeResponse,
  SignUpRequest,
  SignUpResponse,
} from "../types/auth";

// 로그인 API 요청
export async function login(request: LoginRequest) {
  const response = await axiosInstance.post<LoginResponse>(
    "/api/auth/login",
    request,
  );
  return response.data;
}

// 회원가입 API 요청
export async function signUp(request: SignUpRequest) {
  const response = await axiosInstance.post<SignUpResponse>(
    "api/auth/sign-up",
    request,
  );
  return response.data;
}

// 내 정보 조회 API 요청
export async function getMe() {
  const response = await axiosInstance.get<MeResponse>("api/auth/me");
  return response.data;
}
