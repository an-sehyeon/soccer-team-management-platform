package com.soccer.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// 비동기 작업 설정 클래스
// 선수 개인 분석 클립 파일 생성처럼 요청 응답과 분리해서 처리해야 하는 작업에 사용
@Configuration
@EnableAsync
public class AsyncConfig {

}