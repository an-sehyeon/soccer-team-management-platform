package com.soccer.platform.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 웹 리소스 설정 클래스
// 로컬에 저장된 경기 영상 파일과 선수 개인 분석 클립 파일을 브라우저에서 접근 가능하도록 정적 리소스 경로를 매핑
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${app.upload.match-video-dir}")
	private String matchVideoUploadDir;

	@Value("${app.upload.match-video-url-prefix}")
	private String matchVideoUrlPrefix;

	@Value("${app.upload.player-analysis-clip-dir}")
	private String playerAnalysisClipUploadDir;

	@Value("${app.upload.player-analysis-clip-url-prefix}")
	private String playerAnalysisClipUrlPrefix;

	/*
	 * 업로드된 영상 파일 정적 리소스 매핑
	 *
	 * 1. 경기 원본 영상: /uploads/match-videos/**
	 * 2. 선수 개인 분석 클립: /uploads/player-analysis-clips/**
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		addUploadResourceHandler(
			registry,
			matchVideoUrlPrefix,
			matchVideoUploadDir
		);

		addUploadResourceHandler(
			registry,
			playerAnalysisClipUrlPrefix,
			playerAnalysisClipUploadDir
		);
	}

	// 업로드 URL prefix와 실제 로컬 디렉터리를 연결
	private void addUploadResourceHandler(
		ResourceHandlerRegistry registry,
		String urlPrefix,
		String uploadDir
	) {
		Path uploadPath = Paths.get(uploadDir)
			.toAbsolutePath()
			.normalize();

		String resourceLocation = uploadPath.toUri().toString();

		registry.addResourceHandler(urlPrefix + "/**")
			.addResourceLocations(resourceLocation + "/");
	}
}